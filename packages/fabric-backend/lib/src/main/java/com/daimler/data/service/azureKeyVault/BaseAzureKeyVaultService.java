package com.daimler.data.service.azureKeyVault;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.AzureManagementClient;
import com.daimler.data.assembler.AzureKeyVaultAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.AzureKeyVaultNsql;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.repo.keyvault.AzureKeyVaultCustomRepository;
import com.daimler.data.db.repo.keyvault.AzureKeyVaultRepository;
import com.daimler.data.dto.azureKeyVault.KeyVaultNameAvailabilityResponseDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultResponseDto;
import com.daimler.data.dto.azureKeyVault.RoleAssignmentResponseDto;
import com.daimler.data.dto.azureKeyVault.AzurePrincipalDto;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultResponseVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultCollaboratorVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultCollectionVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseAzureKeyVaultService extends BaseCommonService<KeyVaultVO, AzureKeyVaultNsql, String>
		implements AzureKeyVaultService {

	@Override
	public List<AzurePrincipalDto> searchPrincipals(String search) {
		if (search == null || search.isBlank() || search.trim().length() < 3) {
			return List.of();
		}
		return azureManagementClient.searchPrincipals(search);
	}

	@Autowired
	private AzureManagementClient azureManagementClient;

	@Autowired
	private AzureKeyVaultCustomRepository customRepo;

	@Autowired
	private AzureKeyVaultRepository jpaRepo;

	@Autowired
	private AzureKeyVaultAssembler assembler;

	@Autowired
	private UserStore userStore;

	@Override
	public KeyVaultCollectionVO getAllKeyVaults(int limit, int offset, String createdBy) {
		KeyVaultCollectionVO collection = new KeyVaultCollectionVO();
		GenericMessage message = new GenericMessage();
		List<MessageDescription> errors = null;
		List<MessageDescription> warnings = null;

		try {
			List<AzureKeyVaultNsql> keyVaults;
			
			if (createdBy != null && !createdBy.isBlank()) {
				String collaboratorIdentifier = null;
				try {
					collaboratorIdentifier = userStore.getVO().getEmail();
				} catch (Exception ignored) {
					log.warn("Unable to resolve current user's email for collaborator lookup");
				}
				keyVaults = customRepo.findAllByCreatorOrCollaborator(createdBy, collaboratorIdentifier, limit, offset);
			} else {
				log.warn("Attempt to fetch Key Vaults with no createdBy user ID.");
				keyVaults = new ArrayList<>();
			}

			List<KeyVaultVO> keyVaultVOs = keyVaults.stream()
					.map(keyVault -> assembler.toVo(keyVault))
					.collect(Collectors.toList());
			
			collection.setRecords(keyVaultVOs);
			collection.setTotalCount(keyVaultVOs.size());
			message.setSuccess("SUCCESS");

		} catch (Exception e) {
			log.error("Error fetching Azure Key Vaults", e);
			errors = List.of(new MessageDescription("Failed to fetch Key Vaults with error: " + e.getMessage()));
			message.setErrors(errors);
			message.setSuccess("ERROR");
			collection.responses(message);
			return collection;
		}

		if (errors != null) {
			message.setErrors(errors);
		}
		if (warnings != null) {
			message.setWarnings(warnings);
		}
		collection.responses(message);
		return collection;
	}

	@Override
	public ResponseEntity<KeyVaultResponseVO> createKeyVault(KeyVaultVO vo) {
		KeyVaultResponseVO responseData = new KeyVaultResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();

		try {
			String keyVaultName = vo.getKeyVaultName();
			CreatedByVO currentUser = userStore.getVO();
        	String userEmail = currentUser.getEmail();

			KeyVaultNameAvailabilityResponseDto availabilityResponse = azureManagementClient.checkKeyVaultNameAvailability(keyVaultName);

			if (availabilityResponse == null || !availabilityResponse.getNameAvailable()) {
				String errorDetail;
				HttpStatus httpStatus;
				if (availabilityResponse == null) {
					errorDetail = "Name availability check returned no response. Please try again later.";
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				} else if ("Invalid".equalsIgnoreCase(availabilityResponse.getReason())) {
					errorDetail = availabilityResponse.getMessage() != null
							? availabilityResponse.getMessage()
							: "Key Vault name '" + keyVaultName + "' is invalid. A vault's name must be between 3-24 alphanumeric characters, begin with a letter, end with a letter or digit, and not contain consecutive hyphens.";
					httpStatus = HttpStatus.BAD_REQUEST;
				} else if ("AlreadyExists".equalsIgnoreCase(availabilityResponse.getReason())) {
					errorDetail = availabilityResponse.getMessage() != null
							? availabilityResponse.getMessage()
							: "Key Vault name '" + keyVaultName + "' is already taken. Please choose a different name.";
					httpStatus = HttpStatus.CONFLICT;
				} else {
					errorDetail = availabilityResponse.getMessage() != null
							? availabilityResponse.getMessage()
							: "Key Vault name '" + keyVaultName + "' is not available.";
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				}
				MessageDescription message = new MessageDescription(errorDetail);
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				log.error("Key Vault name {} not available (reason: {}): {}", keyVaultName, availabilityResponse != null ? availabilityResponse.getReason() : "null", errorDetail);
				return new ResponseEntity<>(responseData, httpStatus);
			}

			log.info("Key Vault name {} is available", keyVaultName);

			KeyVaultResponseDto keyVaultResponse = azureManagementClient.createOrUpdateKeyVault(keyVaultName);

			if (keyVaultResponse == null || keyVaultResponse.getErrorCode() != null) {
				MessageDescription message = new MessageDescription(
						keyVaultResponse != null ? keyVaultResponse.getMessage() : "Failed to create Key Vault");
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				log.error("Failed to create Key Vault: {}", keyVaultName);
				return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			String userPrincipalId = azureManagementClient.getUserPrincipalId(userEmail);

			if (userPrincipalId == null) {
				MessageDescription message = new MessageDescription("Key Vault created but failed to find user with email: "
						+ userEmail + ". Role assignment failed.");
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				log.error("Key Vault {} created but user {} not found for role assignment", keyVaultName, userEmail);
				return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			RoleAssignmentResponseDto roleResponse = azureManagementClient.assignRoleToUser(keyVaultName,
					userPrincipalId, "officer");
			if (roleResponse != null && roleResponse.getErrorCode() != null
					&& !"409".equals(roleResponse.getErrorCode())) {
				MessageDescription message = new MessageDescription(
						"Key Vault created but role assignment failed: " + roleResponse.getMessage());
				warnings.add(message);
				log.warn("Key Vault {} created but role assignment failed", keyVaultName);
			}
			provisionAddedCollaborators(keyVaultName, vo, warnings);
			vo.setLocation(keyVaultResponse.getLocation());
			vo.setCreatedOn(new Date());
			vo.setCreatedBy(currentUser);

			KeyVaultVO savedRecord = null;
			try {
				savedRecord = super.create(vo); 
				log.info("Key Vault {} with id {} saved to database successfully", keyVaultName, savedRecord.getId());
			} catch (Exception e) {
				log.error("Failed to save Key Vault record to database: {}", e.getMessage());
				MessageDescription message = new MessageDescription(
						"Key Vault created in Azure but failed to save to database: " + e.getMessage());
				warnings.add(message);
			}

			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			responseData.setData(savedRecord);
			responseData.setResponses(responseMessage);

			log.info("Successfully created Azure Key Vault {} and assigned role to user {}", keyVaultName, userEmail);
			return new ResponseEntity<>(responseData, HttpStatus.CREATED);

		} catch (Exception e) {
			log.error("Failed to create Azure Key Vault with exception: {}", e.getMessage(), e);
			MessageDescription errorMessage = new MessageDescription(
					"Failed to create Azure Key Vault with exception: " + e.getMessage());
			errors.add(errorMessage);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			responseData.setData(vo);
			responseData.setResponses(responseMessage);
			return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<KeyVaultResponseVO> updateKeyVault(KeyVaultVO vo) {
		KeyVaultResponseVO responseData = new KeyVaultResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();

		try {
			String keyVaultName = vo.getKeyVaultName();
			CreatedByVO currentUser = userStore.getVO();

			KeyVaultVO existingKeyVault = super.getById(vo.getId());
			
			if (existingKeyVault == null) {
				MessageDescription message = new MessageDescription("Key Vault not found with id: " + vo.getId());
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				log.error("Key Vault not found with id: {}", vo.getId());
				return new ResponseEntity<>(responseData, HttpStatus.NOT_FOUND);
			}

			String currentUserId = currentUser == null ? null : currentUser.getId();
			String ownerId = existingKeyVault.getCreatedBy() == null ? null : existingKeyVault.getCreatedBy().getId();
			if (currentUserId == null || ownerId == null || !ownerId.equalsIgnoreCase(currentUserId)) {
				MessageDescription message = new MessageDescription("Only the Key Vault owner can update collaborators.");
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				return new ResponseEntity<>(responseData, HttpStatus.FORBIDDEN);
			}

			String existingKeyVaultName = existingKeyVault.getKeyVaultName();
			boolean nameChanged = !existingKeyVaultName.equals(keyVaultName);

			if (nameChanged) {
				log.error("Attempted to change Key Vault name from {} to {}. Azure does not allow renaming Key Vaults.", 
						existingKeyVaultName, keyVaultName);
				MessageDescription message = new MessageDescription(
						"Key Vault name cannot be changed. Azure does not support renaming Key Vaults.");
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
			}

			vo.setKeyVaultName(existingKeyVaultName); 
        	vo.setLocation(existingKeyVault.getLocation());
			vo.setCreatedBy(existingKeyVault.getCreatedBy()); 
			vo.setCreatedOn(existingKeyVault.getCreatedOn()); 

			provisionUpdatedCollaborators(keyVaultName, existingKeyVault, vo, warnings);

			KeyVaultVO updatedRecord = null;
			try {
				AzureKeyVaultNsql updatedEntity = assembler.toEntity(vo);
				jpaRepo.save(updatedEntity);
				updatedRecord = vo;
				log.info("Key Vault {} with id {} updated successfully in database", keyVaultName, updatedRecord.getId());
			} catch (Exception e) {
				log.error("Failed to update Key Vault record in database: {}", e.getMessage());
				MessageDescription message = new MessageDescription(
						"Key Vault data governance fields failed to update in database: " + e.getMessage());
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				responseData.setData(vo);
				responseData.setResponses(responseMessage);
				return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			responseData.setData(updatedRecord);
			responseData.setResponses(responseMessage);

			log.info("Successfully updated Azure Key Vault {} with id {}", keyVaultName, updatedRecord.getId());
			return new ResponseEntity<>(responseData, HttpStatus.OK);

		} catch (Exception e) {
			log.error("Failed to update Azure Key Vault with exception: {}", e.getMessage(), e);
			MessageDescription errorMessage = new MessageDescription(
					"Failed to update Azure Key Vault with exception: " + e.getMessage());
			errors.add(errorMessage);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			responseData.setData(vo);
			responseData.setResponses(responseMessage);
			return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void provisionAddedCollaborators(String keyVaultName, KeyVaultVO vo, List<MessageDescription> warnings) {
		if (vo.getCollaborators() == null) {
			return;
		}
		java.util.Set<String> identifiers = new java.util.HashSet<>();
		for (KeyVaultCollaboratorVO collaborator : vo.getCollaborators()) {
			if (collaborator == null || collaborator.getIdentifier() == null
					|| !identifiers.add(collaborator.getIdentifier().toLowerCase())) {
				continue;
			}
			provisionCollaborator(keyVaultName, collaborator, warnings);
		}
	}

	private void provisionUpdatedCollaborators(String keyVaultName, KeyVaultVO existing, KeyVaultVO updated,
			List<MessageDescription> warnings) {
		List<KeyVaultCollaboratorVO> existingCollaborators = existing.getCollaborators() == null
				? new ArrayList<>() : existing.getCollaborators();
		List<KeyVaultCollaboratorVO> updatedCollaborators = updated.getCollaborators() == null
				? new ArrayList<>() : updated.getCollaborators();
		java.util.Map<String, KeyVaultCollaboratorVO> existingByIdentifier = existingCollaborators.stream()
				.filter(c -> c.getIdentifier() != null)
				.collect(Collectors.toMap(c -> c.getIdentifier().toLowerCase(), c -> c, (left, right) -> left));
		for (KeyVaultCollaboratorVO collaborator : updatedCollaborators) {
			if (collaborator == null || collaborator.getIdentifier() == null) {
				continue;
			}
			KeyVaultCollaboratorVO old = existingByIdentifier.get(collaborator.getIdentifier().toLowerCase());
			if (old != null && collaborator.getRoleAssignmentId() == null) {
				collaborator.setObjectId(old.getObjectId());
				collaborator.setPrincipalType(old.getPrincipalType());
				collaborator.setRole(old.getRole());
				collaborator.setRoleAssignmentId(old.getRoleAssignmentId());
			}
			if (old == null || old.getRoleAssignmentId() == null) {
				provisionCollaborator(keyVaultName, collaborator, warnings);
			}
		}
		java.util.Set<String> retained = updatedCollaborators.stream()
				.filter(c -> c.getIdentifier() != null)
				.map(c -> c.getIdentifier().toLowerCase()).collect(Collectors.toSet());
		for (KeyVaultCollaboratorVO old : existingCollaborators) {
			if (old.getIdentifier() != null && !retained.contains(old.getIdentifier().toLowerCase())
					&& old.getRoleAssignmentId() != null) {
				RoleAssignmentResponseDto response = azureManagementClient.removeRoleAssignment(
						keyVaultName, old.getRoleAssignmentId());
				if (response.getErrorCode() != null && !"404".equals(response.getErrorCode())) {
					warnings.add(new MessageDescription("Failed to remove collaborator " + old.getIdentifier()
							+ ": " + response.getMessage()));
				}
			}
		}
	}

	private void provisionCollaborator(String keyVaultName, KeyVaultCollaboratorVO collaborator,
			List<MessageDescription> warnings) {
		String kind = collaborator.getKind() == null ? "USER" : collaborator.getKind();
		AzurePrincipalDto principal = azureManagementClient.resolvePrincipal(collaborator.getIdentifier(), kind);
		if (principal == null || principal.getId() == null) {
			warnings.add(new MessageDescription("Collaborator could not be resolved: " + collaborator.getIdentifier()));
			return;
		}
		RoleAssignmentResponseDto response = azureManagementClient.assignRoleToUser(keyVaultName, principal.getId(),
				"user", principal.getPrincipalType());
		collaborator.setObjectId(principal.getId());
		collaborator.setPrincipalType(principal.getPrincipalType());
		collaborator.setRole("Crypto User");
		collaborator.setRoleAssignmentId(response == null ? null : response.getRoleAssignmentId());
		if (response != null && response.getErrorCode() != null && !"409".equals(response.getErrorCode())) {
			warnings.add(new MessageDescription("Failed to assign collaborator " + collaborator.getIdentifier()
					+ ": " + response.getMessage()));
		}
	}
}
