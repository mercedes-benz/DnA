
package com.daimler.data.service.uiliciousWorkspace;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.dto.uilicious.LeanGovernanceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateRequestVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateResponseVO;
import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.uiliciousWorkspace.UiliciousWorkspaceService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.daimler.data.controller.LoginController.UserInfo;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.db.repo.uilicious.UiliciousWorkspaceCustomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import  com.daimler.data.dto.uilicious.CreateUiliciousWorkspaceRequestVO;
import com.daimler.data.controller.LoginController;
import com.daimler.data.dto.UiliciousCreationDTO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.db.jsonb.solution.CreatedBy;

@Service
@Slf4j
public class BaseUiliciousWorkspaceService extends
		BaseCommonService<UiliciousCreationDTO, UiliciousWorkspaceNsql, String> implements UiliciousWorkspaceService {
	@Autowired
	private UserStore userStore;

	@Autowired
	private UiLiciousClient uiLiciousClient;

	@Autowired
	private UiliciousWorkspaceCustomRepository uiliciousWorkspaceCustomRepository;

	@Override
	public UiliciousWorkspacesCollectionVO getUiliciousWorkspaces(Integer offset, Integer limit, String sortOrder) {
		UiliciousWorkspacesCollectionVO response = new UiliciousWorkspacesCollectionVO();
		UserInfo currentUser = this.userStore.getUserInfo();
		List<LeanGovernanceVO> leanGovernance = new ArrayList<>();
		// log.info("Logged in user: "+ currentUser.getEmail());
		log.info("Logged in user: " + currentUser.getEmail());
		try {
			// Check if user workspace exists in database
			
			JsonNode existingWorkspace = uiliciousWorkspaceCustomRepository.findUiliciousWorkspacesByEmail(currentUser.getEmail());
			String accountId = null;
			
			if (existingWorkspace != null) {
				log.info("Found existing workspace in database for email: {}", currentUser.getEmail());
				
				// Extract lean governance from database if available
				if (existingWorkspace.has("leanGovernance") && !existingWorkspace.path("leanGovernance").isNull()) {
					JsonNode leanGovNode = existingWorkspace.path("leanGovernance");
					ObjectMapper mapper = new ObjectMapper();
					try {
						LeanGovernanceVO leanGov = mapper.convertValue(leanGovNode, LeanGovernanceVO.class);
						leanGovernance.add(leanGov);
						log.info("Lean governance found and added from database for email: {}", currentUser.getEmail());
					} catch (Exception e) {
						log.warn("Failed to convert lean governance from database: {}", e.getMessage());
					}
				}
				
				// Check if accountId field exists and is not null
				if (existingWorkspace.has("accountId") && !existingWorkspace.path("accountId").isNull() && 
				    !existingWorkspace.path("accountId").asText().trim().isEmpty()) {
					accountId = existingWorkspace.path("accountId").asText();
					log.info("Found existing accountId in database: {}", accountId);
				} else {
					// AccountId is null or empty, make API call to get it and try to update database
					log.info("AccountId is null in database, fetching from API for email: {}", currentUser.getEmail());
					String apiAccountId = uiLiciousClient.getUserAccountId(currentUser.getEmail(), 0, 10);
					
					if (apiAccountId != null && !apiAccountId.trim().isEmpty()) {
						// Try to update the database with the new accountId
						boolean updated = uiliciousWorkspaceCustomRepository.updateAccountIdByEmail(currentUser.getEmail(), apiAccountId);
						if (updated) {
							// Only assign to accountId if database update was successful
							accountId = apiAccountId;
							log.info("Successfully fetched and persisted accountId: {}", accountId);
						} else {
							// Keep accountId as null since database update failed
							log.warn("Failed to persist accountId to database for email: {}, keeping accountId as null", currentUser.getEmail());
						}
					} else {
						log.warn("Failed to fetch accountId from API for email: {}", currentUser.getEmail());
					}
				}
			} else {
				// No existing workspace found in database, accountId remains null
				log.info("No existing workspace found in database for email: {}, accountId will be null", currentUser.getEmail());
				// accountId remains null since we only want database values
			}
			
			// Set default values if not provided
			int start = (offset != null) ? offset : 0;
			int length = (limit != null) ? limit : 10;
			
			// UiliciousWorkspaceNsql workspace = uiliciousWorkspaceCustomRepository.findUiliciousWorkspacesByEmail(currentUser.getEmail());
			// if (workspace != null && workspace.getData() != null) {
			// 	ObjectMapper mapper = new ObjectMapper();
			// 	LeanGovernanceVO leanGov = mapper.convertValue(workspace.getData(), LeanGovernanceVO.class);
			// 	leanGovernance.add(leanGov);
			// }

			//List<LeanGovernanceVO> workspaces = uiLiciousClient.getWorkspaces(currentUser.getEmail(), start,length);
			// Call the client to get workspaces from Uilicious APIs
			List<UiliciousWorkspaceVO> workspaces = uiLiciousClient.getWorkspaces(currentUser.getEmail(), start,
					length);
			log.info("List of Workspaces: " + workspaces);
			if (workspaces != null && !workspaces.isEmpty()) {
				// Apply sorting if specified
				if (sortOrder != null && "desc".equalsIgnoreCase(sortOrder)) {
					workspaces.sort((w1, w2) -> w2.getSpaceName().compareToIgnoreCase(w1.getSpaceName()));
				} else {
					workspaces.sort((w1, w2) -> w1.getSpaceName().compareToIgnoreCase(w2.getSpaceName()));
				}

				response.setAccountId(accountId);
				response.setLeanGovernance(leanGovernance);
				response.setItems(workspaces);
				response.setTotalRecords(workspaces.size());
				log.info("Successfully fetched {} workspaces for email: {}",
						workspaces.size(), currentUser.getEmail());
			} else {
				response.setLeanGovernance(new ArrayList<>());
				response.setItems(new ArrayList<>());
				response.setTotalRecords(0);
				log.info("No workspaces found for email: {}", currentUser.getEmail());
				
			}

		} catch (RuntimeException e) {
			// Check if it's a Uilicious server unavailability issue
			if (e.getMessage() != null && 
			    (e.getMessage().contains("Uilicious server is unavailable") || 
			     e.getMessage().contains("Something went wrong with Uilicious server") ||
			     e.getMessage().contains("Failed to communicate with Uilicious server"))) {
				log.error("Uilicious server is down or unreachable for email: {}, error: {}",
						currentUser.getEmail(), e.getMessage());
				throw new RuntimeException("Something went wrong with Uilicious server/tool. Please try again later.", e);
			}
			log.error("Runtime error occurred while fetching Uilicious workspaces for email: {}, error: {}",
					currentUser.getEmail(), e.getMessage(), e);
			return null;
		} catch (Exception e) {
			log.error("Error occurred while fetching Uilicious workspaces for email: {}, error: {}",
					currentUser.getEmail(), e.getMessage(), e);
			return null;
		}

		return response;
	}

	@Override
	public UiliciousWorkspaceUpdateResponseVO updateUiliciousWorkspace(UiliciousWorkspaceUpdateRequestVO request) {
		log.info("Starting update for Uilicious workspace with accountId: {}", request.getAccountId());
		
		try {
			// Validate input
			if (request.getAccountId() == null || request.getAccountId().trim().isEmpty()) {
				log.error("AccountId is null or empty in update request");
				throw new IllegalArgumentException("AccountId cannot be null or empty");
			}
			
			if (request.getLeanGovernance() == null) {
				log.error("LeanGovernance is null in update request for accountId: {}", request.getAccountId());
				throw new IllegalArgumentException("LeanGovernance cannot be null");
			}
			
			// Get current user
			UserInfo currentUser = this.userStore.getUserInfo();
			log.info("Update requested by user: {}", currentUser.getEmail());
			
			// Check if workspace exists for this accountId and user email
			JsonNode existingWorkspace = uiliciousWorkspaceCustomRepository.findUiliciousWorkspacesByEmail(currentUser.getEmail());
			
			if (existingWorkspace == null) {
				log.warn("No workspace found in database for email: {} and accountId: {}", 
						currentUser.getEmail(), request.getAccountId());
				return null;
			}
			
			// Verify the accountId matches
			String dbAccountId = existingWorkspace.has("accountId") && !existingWorkspace.path("accountId").isNull() 
					? existingWorkspace.path("accountId").asText() 
					: null;
			
			if (dbAccountId == null || !dbAccountId.equals(request.getAccountId())) {
				log.warn("AccountId mismatch or not found. Expected: {}, Found: {}", 
						request.getAccountId(), dbAccountId);
				return null;
			}
			
			// Convert LeanGovernanceVO to JsonNode for database update
			ObjectMapper mapper = new ObjectMapper();
			JsonNode leanGovernanceNode = mapper.valueToTree(request.getLeanGovernance());
			
			// Update lean governance in database
			boolean updated = uiliciousWorkspaceCustomRepository.updateLeanGovernanceByAccountId(
					request.getAccountId(), 
					leanGovernanceNode
			);
			
			if (!updated) {
				log.error("Failed to update lean governance in database for accountId: {}", request.getAccountId());
				throw new RuntimeException("Failed to update workspace in database");
			}
			
			log.info("Successfully updated lean governance for accountId: {}", request.getAccountId());
			
			// Prepare response
			UiliciousWorkspaceUpdateResponseVO response = new UiliciousWorkspaceUpdateResponseVO();
			response.setAccountId(request.getAccountId());
			response.setLeanGovernance(request.getLeanGovernance());
			response.setMessage("Lean governance updated successfully");
			
			return response;
			
		} catch (IllegalArgumentException e) {
			log.error("Validation error during workspace update: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error occurred while updating Uilicious workspace for accountId: {}, error: {}",
					request.getAccountId(), e.getMessage(), e);
			throw new RuntimeException("Failed to update workspace", e);
		}
	}

	public String createUiliciousWorkspace(CreateUiliciousWorkspaceRequestVO request) {
		try {
			CreatedByVO currentUserInfo = this.userStore.getVO();
			UserInfo currentUser = this.userStore.getUserInfo();
			String userId = currentUserInfo.getId();
			String email = currentUser.getEmail();
			String firstName = currentUser.getFirstName();
			log.info("Creating Uilicious workspace with request calling to client: {}", request);
			String response = uiLiciousClient.createUiliciousWorkspace(email, userId, firstName);
			if (response != null && !response.contains("FAILURE")) {
				log.info("Successfully created Uilicious workspace with response: {}", response);
				String accountId = response;
				response = "SUCCESS";

				// persist creation details in database
				UiliciousCreationDTO creationdto = new UiliciousCreationDTO();
				creationdto.setAccountId(accountId);
				creationdto.setLeanGovernance(request.getLeanGovernance());
				CreatedBy createdBy = new CreatedBy();
				createdBy.setId(userId);
				createdBy.setFirstName(currentUser.getFirstName());
				createdBy.setLastName(currentUser.getLastName());
				createdBy.setEmail(currentUser.getEmail());
				createdBy.setDepartment(currentUser.getDepartment());
				createdBy.setMobileNumber(currentUser.getMobileNumber());
				creationdto.setCreatedBy(createdBy);
				// creationdto.setSpaceId(null);
				// creationdto.setSpaceName(null);
				creationdto.setId(null);
				UiliciousCreationDTO creationresponse = super.create(creationdto);
				if (creationresponse == null) {
					log.error("Failed to persist Uilicious workspace creation details in database for accountId: {}",
							accountId);
					throw new RuntimeException("Failed to persist Uilicious workspace creation details in database");
				}
				log.info("Uilicious workspace cretion details persisted in database: {}", creationresponse);

			} else {
				log.error("Failed to create Uilicious workspace via client, response: {}", response);
				response = "User workspace already exists or creation failed";
			}
			return response;
		} catch (Exception e) {
			log.error("Error occurred while creating Uilicious workspace in service with exception: {}", e.getMessage(),
					e);
			return null;
		}
	}	
}

