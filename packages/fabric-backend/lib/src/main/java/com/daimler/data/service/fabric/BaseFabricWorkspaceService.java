package com.daimler.data.service.fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.AuthoriserClient;
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.application.client.RSAEncryptionUtil;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.forecast.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.forecast.FabricWorkspaceRepository;
import com.daimler.data.dto.fabric.AccessReviewDto;
import com.daimler.data.dto.fabric.AddGroupDto;
import com.daimler.data.dto.fabric.CreateDatasourceRequestDto;
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateLakehouseDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.CredentialDetailsDto;
import com.daimler.data.dto.fabric.DatasourceResponseDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.FabricGroupsCollectionDto;
import com.daimler.data.dto.fabric.LakehouseCollectionDto;
import com.daimler.data.dto.fabric.LakehouseDto;
import com.daimler.data.dto.fabric.LakehouseResponseDto;
import com.daimler.data.dto.fabric.LakehouseS3ShortcutCollectionDto;
import com.daimler.data.dto.fabric.LakehouseS3ShortcutDto;
import com.daimler.data.dto.fabric.LakehouseS3ShortcutResponseDto;
import com.daimler.data.dto.fabric.MicrosoftGroupDetailDto;
import com.daimler.data.dto.fabric.ReviewerConfigDto;
import com.daimler.data.dto.fabric.S3CompatibleTargetDto;
import com.daimler.data.dto.fabric.ShortcutTargetDto;
import com.daimler.data.dto.fabric.UserRoleRequestDto;
import com.daimler.data.dto.fabric.WorkflowDefinitionDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspaceUpdateDto;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.CreateRoleRequestVO;
import com.daimler.data.dto.fabricWorkspace.EntitlementDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricLakehouseCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricLakehouseVO;
import com.daimler.data.dto.fabricWorkspace.FabricShortcutsCollectionVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceRoleRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RolesVO;
import com.daimler.data.dto.fabricWorkspace.DnaRoleCollectionVO;
import com.daimler.data.dto.fabricWorkspace.DnaRoleCollectionVOData;
import com.daimler.data.dto.fabricWorkspace.ShortcutCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.ShortcutVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class BaseFabricWorkspaceService extends BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricWorkspaceService{

	@Autowired
	private FabricWorkspaceClient fabricWorkspaceClient;
	
	@Autowired
	private FabricWorkspaceCustomRepository customRepo;
	
	@Autowired
	private FabricWorkspaceRepository jpaRepo;
	
	@Autowired
	private FabricWorkspaceAssembler assembler;
	
	@Autowired
	private AuthoriserClient identityClient;
	
	@Autowired
	private RSAEncryptionUtil encryptionUtil;
		
	@Value("${fabricWorkspaces.capacityId}")
	private String capacityId;
	
	@Value("${fabricWorkspaces.capacityName}")
	private String capacityName;
	
	@Value("${fabricWorkspaces.capacitySku}")
	private String capacitySku;
	
	@Value("${fabricWorkspaces.capacityRegion}")
	private String capacityRegion;
	
	@Value("${fabricWorkspaces.capacityState}")
	private String capacityState;
	
	@Value("${fabricWorkspaces.dnaGroupPrefix}")
	private String dnaGroupPrefix;
	
	@Value("${authoriser.community}")
	private String communityAvailability;
	
	@Value("${authoriser.workflowDefinitionId}")
	private String workflowDefinitionId;
	
	@Value("${authoriser.dnaFabricEntitlementName}")
	private String dnaFabricEntitlementName;
	
	@Value("${authoriser.dnaFabricEntitlementId}")
	private String dnaFabricEntitlementId;
	
	@Value("${authoriser.identityRoleUrl}")
	private String identityRoleUrl;
	
	
	@Value("${fabricWorkspaces.group.identifier}")
	private String onboardGroupIdenitifier;
	
	@Value("${fabricWorkspaces.group.displayName}")
	private String onboardGroupDisplayName;
	
	@Value("${fabricWorkspaces.subgroupPrefix}")
	private String subgroupPrefix;
	
	@Value("${authoriser.applicationId}")
	private String applicationId;
	
	@Value("${fabricWorkspaces.shortcutNameprefix}")
	private String shortcutNameprefix;
	
	@Value("${fabricWorkspaces.shortcutLocation}")
	private String shortcutLocation;
	
	@Value("${fabricWorkspaces.datasource.credentialType}")
	private String datasourceCredentialType;
	
	@Value("${fabricWorkspaces.datasource.encryptedConnection}")
	private String datasourceEncryptedConnection;
	
	@Value("${fabricWorkspaces.datasource.encryptionAlgorithm}")
	private String datasourceEncryptionAlgorithm;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
	@Value("${authoriser.role.fabricRoleName}")
	private String fabricOperationsRoleName;
	
	public BaseFabricWorkspaceService() {
		super();
	}
	
	@Override
	@Transactional
	public FabricWorkspacesCollectionVO getAllLov( int limit,  int offset) {
		FabricWorkspacesCollectionVO collectionVO = new FabricWorkspacesCollectionVO();
		List<FabricWorkspaceVO> vos = new ArrayList<>();
		List<FabricWorkspaceNsql> allEntities = customRepo.findAll(0,0);
		if (allEntities != null && !allEntities.isEmpty()) {
			for(FabricWorkspaceNsql entity : allEntities) {
//				try {
//					String id = entity.getId();
//					log.info("Fetched fabric project record from db successfully for id {} ", id);
//					WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
//					if(dtoFromFabric!=null) {
//						if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
//								log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
//								jpaRepo.deleteById(id);
//								log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
//						}else {
//							entity.getData().setName(dtoFromFabric.getDisplayName());
//							entity.getData().setDescription(dtoFromFabric.getDescription());
//							log.info("getAllLOV-record found-Updating- ID : {} Name : {} and Description : {}",id,dtoFromFabric.getDisplayName(),dtoFromFabric.getDescription());
//							jpaRepo.save(entity);
//							FabricWorkspaceVO updatedVO = assembler.toVo(entity);
//							vos.add(updatedVO);
//						}
//					}
//				}catch(Exception e) {
//					log.error("Failed to update Fabric workspace record of id {} during get all records",entity.getId());
//					FabricWorkspaceVO updatedVO = assembler.toVo(entity);
//					vos.add(updatedVO);
//				}
				FabricWorkspaceVO updatedVO = assembler.toVo(entity);
				vos.add(updatedVO);
			}
		}
		List<FabricWorkspaceVO> paginatedVOs = new ArrayList<>();
		int totalCount = 0;
		if(vos!=null && !vos.isEmpty()) {
			totalCount = vos.size();
			int newOffset = offset>vos.size() ? 0 : offset;
			if(limit==0)
				limit = vos.size();
			int newLimit = offset+limit > vos.size() ? vos.size() : offset+limit;
			paginatedVOs = vos.subList(newOffset, newLimit);
		}
		collectionVO.setRecords(paginatedVOs);
		collectionVO.setTotalCount(totalCount);
		return collectionVO;
	}

	@Override
	@Transactional
	public FabricWorkspacesCollectionVO getAll( int limit,  int offset, String user, List<String> allEntitlementsList) {
		FabricWorkspacesCollectionVO collectionVO = new FabricWorkspacesCollectionVO();
		List<FabricWorkspaceVO> vos = new ArrayList<>();
		List<FabricWorkspaceNsql> allEntities = customRepo.findAll(0,0);
		List<FabricWorkspaceNsql> filteredEntities = new ArrayList<>();
		if(allEntities!=null && !allEntities.isEmpty()) {
			if(user!=null && !"".equalsIgnoreCase(user.trim())){
				for(FabricWorkspaceNsql existingEntity : allEntities) {
					if(existingEntity!=null) {
						List<String> filteredEntitlements = new ArrayList<>();
						if(allEntitlementsList!=null && !allEntitlementsList.isEmpty()) {
							filteredEntitlements = allEntitlementsList.stream().filter(n-> n.startsWith(applicationId + "." + subgroupPrefix ) && n.contains(existingEntity.getId())).collect(Collectors.toList());
						}
						String creatorId = existingEntity.getData().getCreatedBy().getId();
						if(!(!user.equalsIgnoreCase(creatorId) && (filteredEntitlements==null || filteredEntitlements.isEmpty()))) {
							filteredEntities.add(existingEntity);
						}
					}
				}
			}else {
				filteredEntities.addAll(allEntities);
			}
		}
		if (filteredEntities != null && !filteredEntities.isEmpty()) {
			for(FabricWorkspaceNsql entity : filteredEntities) {
//				try {
//					String id = entity.getId();
//					log.info("Fetched fabric project record from db successfully for id {} ", id);
//					WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
//					if(dtoFromFabric!=null) {
//						if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
//								log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
//								jpaRepo.deleteById(id);
//								log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
//						}else {
//							entity.getData().setName(dtoFromFabric.getDisplayName());
//							entity.getData().setDescription(dtoFromFabric.getDescription());
//							log.info("getAll-record found-Updating- ID : {} Name : {} and Description : {}",id,dtoFromFabric.getDisplayName(),dtoFromFabric.getDescription());
//							jpaRepo.save(entity);
//							FabricWorkspaceVO updatedVO = assembler.toVo(entity);
//							vos.add(updatedVO);
//						}
//					}
//				}catch(Exception e) {
//					log.error("Failed to update Fabric workspace record of id {} during get all records",entity.getId());
//					FabricWorkspaceVO updatedVO = assembler.toVo(entity);
//					vos.add(updatedVO);
//				}
				FabricWorkspaceVO updatedVO = assembler.toVo(entity);
				vos.add(updatedVO);
			}
		}
		List<FabricWorkspaceVO> paginatedVOs = new ArrayList<>();
		int totalCount = 0;
		if(vos!=null && !vos.isEmpty()) {
			totalCount = vos.size();
			int newOffset = offset>vos.size() ? 0 : offset;
			if(limit == 0) {
				limit = totalCount;
			}
			int newLimit = offset+limit > vos.size() ? vos.size() : offset+limit;
			paginatedVOs = vos.subList(newOffset, newLimit);
		}
		collectionVO.setRecords(paginatedVOs);
		collectionVO.setTotalCount(totalCount);
		return collectionVO;
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}
	
	@Override
	@Transactional
	public FabricWorkspaceVO getById(String id) {
		FabricWorkspaceVO voFromDb =  super.getById(id);
		log.info("Fetched fabric project record from db successfully for id {} ", id);
//		WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
//		if(dtoFromFabric!=null) {
//			if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
//				log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
//				try{
//					jpaRepo.deleteById(id);
//					log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
//				}catch(Exception e) {
//					log.error("Project id {} not found in Microsoft Fabric. Failed to remove from database, will remove in next fetch", id);
//				}
//				return null;
//			}
//			log.info("getbyid-record found-Updating- ID : {} Name : {} and Description : {}",id,dtoFromFabric.getDisplayName(),dtoFromFabric.getDescription());
//			voFromDb.setName(dtoFromFabric.getDisplayName());
//			voFromDb.setDescription(dtoFromFabric.getDescription());
			try {
				LakehouseCollectionDto lakehousesCollection = fabricWorkspaceClient.listLakehouses(id);
				if(lakehousesCollection!=null && lakehousesCollection.getValue()!=null && !lakehousesCollection.getValue().isEmpty()) {
					List<LakehouseDto> value = lakehousesCollection.getValue();
					List<FabricLakehouseVO> lakehouseVOs = new ArrayList<>();
					lakehouseVOs = value.stream().map(n -> assembler.toLakehouseVOFromDto(n)).collect(Collectors.toList());
					voFromDb.setLakehouses(lakehouseVOs);
				}
				FabricWorkspaceNsql updatedEntity = assembler.toEntity(voFromDb);
				log.info("Successfully updated latest displayName and description from Fabric to Database for project id {}", id);
				jpaRepo.save(updatedEntity);
			}catch(Exception e) {
				log.error("Failed to update latest displayName and description from Fabric to Database for project id {} . Will be updated in next fetch", id);
			}
//		}
		return voFromDb;
	}

	
	@Override
	@Transactional
	public ResponseEntity<FabricWorkspaceResponseVO> createWorkspace(FabricWorkspaceVO vo) {
		FabricWorkspaceResponseVO responseData = new FabricWorkspaceResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		CreateWorkspaceDto createRequest = new CreateWorkspaceDto();
		createRequest.setDescription(vo.getDescription());
		createRequest.setDisplayName(vo.getName());
		try {
			WorkspaceDetailDto createResponse = fabricWorkspaceClient.createWorkspace(createRequest);
			if(createResponse!=null ) {
				if(createResponse.getErrorCode() != null ) {
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to create workspace. Error is : " + createResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					responseData.setData(vo);
					responseData.setResponses(responseMessage);
					log.error("Error occurred:{} while creating fabric workspace project {} ", createResponse.getErrorCode(), vo.getName());
					if("409".equalsIgnoreCase(createResponse.getErrorCode())) {
						return new ResponseEntity<>(responseData, HttpStatus.CONFLICT);
					}else {
						return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				if(createResponse.getId()!=null) {
					log.info("created fabric workspace project {} successfully with id {}", vo.getName(), createResponse.getId());
					GenericMessage addUserResponse = fabricWorkspaceClient.addUser(createResponse.getId(), vo.getCreatedBy().getEmail());
					if(addUserResponse == null || !"SUCCESS".equalsIgnoreCase(addUserResponse.getSuccess())) {
						log.error("Failed to add user {} to workspace {}", vo.getCreatedBy().getEmail(), createResponse.getId());
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to add user to created workspace " + vo.getName() + " with id" + createResponse.getId() + ". Please contact Admin.");
						errors.add(message);
						responseMessage.setErrors(errors);
						responseMessage.setSuccess("FAILED");
						responseData.setData(vo);
						responseData.setResponses(responseMessage);
						return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
					}else {
						log.info("Successfully added  user {} to workspace {} ", vo.getCreatedBy().getEmail(), createResponse.getId());
					}
					
					AddGroupDto addGroupDto = new AddGroupDto();
					addGroupDto.setDisplayName(onboardGroupDisplayName);
					addGroupDto.setIdentifier(onboardGroupIdenitifier);
					addGroupDto.setPrincipalType("Group");
					addGroupDto.setGroupUserAccessRight("Admin");
					GenericMessage addGroupResponse = fabricWorkspaceClient.addGroup(createResponse.getId(),addGroupDto);
					if(addGroupResponse == null || !"SUCCESS".equalsIgnoreCase(addGroupResponse.getSuccess())) {
						log.error("Failed to add default group to workspace {}", createResponse.getId());
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to add default group to created workspace " + vo.getName() + ". Please add Default Group to your workspace manually or contact Admin.");
						warnings.add(message);
					}else {
						log.info("Successfully added  default Group to workspace {} ", createResponse.getId());
					}
					
					FabricWorkspaceVO data = new FabricWorkspaceVO();
					BeanUtils.copyProperties(vo, data);
					data.setId(createResponse.getId());
					data.setHasPii(vo.isHasPii());
					
					ErrorResponseDto assignCapacityResponse = fabricWorkspaceClient.assignCapacity(createResponse.getId());
					CapacityVO capacityVO = new CapacityVO();
					if(assignCapacityResponse!=null && assignCapacityResponse.getErrorCode()!=null && "500".equalsIgnoreCase(assignCapacityResponse.getErrorCode())) {
						capacityVO = null;
						warnings.add(new MessageDescription("Failed to assign capacity, please reassign or update workspace to assign capacity automatically."));
					}else {
						capacityVO.setId(capacityId);
						capacityVO.setName(capacityName);
						capacityVO.setRegion(capacityRegion);
						capacityVO.setSku(capacitySku);
						capacityVO.setState(capacityState);
					}
					data.setCapacity(capacityVO);
					
					FabricWorkspaceStatusVO currentStatus = new FabricWorkspaceStatusVO();
					currentStatus.setState(ConstantsUtility.INPROGRESS_STATE);
					String creatorId = vo.getCreatedBy().getId(); 
					data.setStatus(this.processWorkspaceUserManagement(currentStatus, vo.getName(), creatorId,createResponse.getId()));
					FabricWorkspaceVO savedRecord = null;
					try{
						savedRecord = super.create(data);
					}catch(Exception e) {
						ObjectMapper mapper = new ObjectMapper();
						log.error("Failed to save record to db after processing usermanagement successfully for a new fabric record with data {}", mapper.writeValueAsString(data));
					}
					log.info("created workspace project {} with id {} saved to database successfully", vo.getName(), createResponse.getId());
					fabricWorkspaceClient.provisionWorkspace(createResponse.getId());
					
					try {
						String ownerId = vo.getCreatedBy().getId();
						Date validFromDate = vo.getCreatedOn();
						String validFrom = formatter.format(validFromDate);
						Calendar calendar = Calendar.getInstance();
				        calendar.setTime(validFromDate);
				        calendar.add(Calendar.YEAR, 1);
				        Date validToDate = calendar.getTime();
						String validTo = formatter.format(validToDate);
						UserRoleRequestDto roleRequestDto = new UserRoleRequestDto();
						roleRequestDto.setReason("Onboarding owner to role to enable fabric operations.");
						roleRequestDto.setValidTo(validTo);
						roleRequestDto.setValidFrom(validFrom);
						HttpStatus status = identityClient.RequestRoleForUser(roleRequestDto, ownerId, fabricOperationsRoleName,null);
						if(status.is2xxSuccessful()){
				            log.info("Successfully onboarded owner {} of workspace {} : {} to role {} for enabling fabric operations", ownerId, vo.getId(), vo.getName(), fabricOperationsRoleName);
				        }else {
				        	log.error("Failed to onboarded owner {} of workspace {} : {} to role {} for enabling fabric operations", ownerId, vo.getId(), vo.getName(), fabricOperationsRoleName);
				        }
					}catch(Exception e) {
						log.error("Failed to onboard owner of workspace {} : {} to role {} ",vo.getId(),vo.getName(),fabricOperationsRoleName);
					}
					responseData.setData(savedRecord);
					responseMessage.setSuccess("SUCCESS");
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseData.setResponses(responseMessage);
					return new ResponseEntity<>(responseData, HttpStatus.CREATED);
				}
				
			}
		}catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to create workspace due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseData.setData(vo);
			responseData.setResponses(failedResponse);
			log.error("Exception occurred:{} while creating fabric workspace project {} ", e.getMessage(), vo.getName());
			return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		GenericMessage failedResponse = new GenericMessage();
		List<MessageDescription> messages = new ArrayList<>();
		MessageDescription message = new MessageDescription();
		message.setMessage("Failed to create workspace due to internal error. Empty response from Fabric create workspace");
		messages.add(message);
		failedResponse.addErrors(message);
		failedResponse.setSuccess("FAILED");
		responseData.setData(vo);
		responseData.setResponses(failedResponse);
		log.error("Empty response for create workspace from fabric. Failed while creating fabric workspace project {} ", vo.getName());
		return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
	
	@Override
	public CreateEntitlementRequestDto prepareEntitlementCreateRequestDto(String workspaceName, String workspaceId, String permissionName) {
		CreateEntitlementRequestDto entitlementRequestDto = new CreateEntitlementRequestDto();
		entitlementRequestDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
		entitlementRequestDto.setEntitlementId(subgroupPrefix +  workspaceId + "_" + permissionName);
		entitlementRequestDto.setDisplayName(subgroupPrefix + workspaceId + "_" + permissionName);
		entitlementRequestDto.setDescription("Entitlement for workspace " + workspaceName + " for " + permissionName + " Privileges");
		entitlementRequestDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
		entitlementRequestDto.setDataClassificationInherited(false);
		entitlementRequestDto.setConnectivity(false);
		return entitlementRequestDto;
	}
	
	@Override
	public EntitlementDetailsVO callEntitlementCreate(String workspaceName, String workspaceId, String permissionName) {
		CreateEntitlementRequestDto createRequestDto = this.prepareEntitlementCreateRequestDto(workspaceName, workspaceId, permissionName);
		EntitlementDetailsVO requestedEntitlement = new EntitlementDetailsVO();
		requestedEntitlement.setDisplayName(subgroupPrefix +  workspaceId + "_" + permissionName);
		requestedEntitlement.setState(ConstantsUtility.PENDING_STATE);
		try {
			log.info("Calling identity management system to add entitlement {} for workspace {} ", subgroupPrefix  + workspaceId + "_" + permissionName, workspaceName);
			EntiltlemetDetailsDto getResponse = identityClient.getEntitlement(createRequestDto.getDisplayName());
			if(getResponse!=null && getResponse.getUuid()!=null) {
				requestedEntitlement.setEntitlementId(getResponse.getEntitlementId());
				requestedEntitlement.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to get entitlement {} for workspace {} . Entitlement fetched successfully with id {} ", workspaceName + "_" +  permissionName, workspaceName, getResponse.getUuid());
				return requestedEntitlement;
			}
			EntiltlemetDetailsDto entitlementCreateResponse = identityClient.createEntitlement(createRequestDto);
			if(entitlementCreateResponse!=null && entitlementCreateResponse.getEntitlementId()!=null) {
				requestedEntitlement.setEntitlementId(entitlementCreateResponse.getEntitlementId());
				requestedEntitlement.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement created successfully with id {} ", subgroupPrefix +  workspaceId + "_" + permissionName, workspaceName, entitlementCreateResponse.getEntitlementId());
			}else {
				requestedEntitlement.setState(ConstantsUtility.FAILED_STATE);
				log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement creat failed with unknown error", subgroupPrefix  + workspaceId + "_" + permissionName, workspaceName);
			}
		}catch(Exception e) {
			requestedEntitlement.setState(ConstantsUtility.FAILED_STATE);
			log.error("Called identity management system to add entitlement {} for workspace {} . Failed to create entitlement with error {} ", subgroupPrefix +  workspaceId + "_" + permissionName, workspaceName, e.getMessage());
		}
		return requestedEntitlement;
	}
	
	@Override
	public CreateRoleRequestDto prepareRoleCreateRequestDto(String workspaceName, String permissionName) {
		String[] communityAvailabilitySplits = communityAvailability.split(",");
		AccessReviewDto accessReview = new AccessReviewDto();
		accessReview.setEnabled(true);
		accessReview.setStartDate(formatter.format(new Date()));
		List<ReviewerConfigDto> reviewersConfig = new ArrayList<>();
		ReviewerConfigDto roleApprover = new ReviewerConfigDto();
		roleApprover.setType("ROLE_APPROVER");
		roleApprover.setUserCommunity(communityAvailabilitySplits[0]);
		reviewersConfig.add(roleApprover);
		accessReview.setReviewerConfigs(reviewersConfig);
		
		WorkflowDefinitionDto workflow = new WorkflowDefinitionDto();
		workflow.setId(Integer.valueOf(workflowDefinitionId));
		
		CreateRoleRequestDto adminRoleRequestDto = new CreateRoleRequestDto();
		adminRoleRequestDto.setAccessReview(accessReview);
		
		adminRoleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
		adminRoleRequestDto.setDataClassification("CONFIDENTIAL");
		adminRoleRequestDto.setDefaultValidityType("OPTIONAL");
		adminRoleRequestDto.setDeprovisioning(false);
		adminRoleRequestDto.setDescription(permissionName +" role for workspace " + workspaceName);
		adminRoleRequestDto.setDynamic(false);
		adminRoleRequestDto.setGlobalCentralAvailable(true);
		adminRoleRequestDto.setId(workspaceName + "_" +  permissionName);
		adminRoleRequestDto.setJobTitle(false);
		adminRoleRequestDto.setMarketAvailabilities(new ArrayList<>());
		adminRoleRequestDto.setName(workspaceName + "_" +  permissionName);
		adminRoleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
		adminRoleRequestDto.setNeedsCustomScopes(false);
		adminRoleRequestDto.setNeedsOrgScopes(false);
		adminRoleRequestDto.setNotificationsActive(true);
		adminRoleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
		adminRoleRequestDto.setRoleType("BUSINESS");
		adminRoleRequestDto.setSelfRequestable(true);
		adminRoleRequestDto.setWorkflowBased(true);
		adminRoleRequestDto.setWorkflowDefinition(workflow);
		return adminRoleRequestDto;
	}
	
	@Override
	public RoleDetailsVO callRoleCreate(String workspaceName, String permissionName) {
		CreateRoleRequestDto createRequestDto = this.prepareRoleCreateRequestDto(workspaceName,permissionName);
		RoleDetailsVO createRoleVO = new RoleDetailsVO();
		createRoleVO.setName(workspaceName + "_" +  permissionName);
		createRoleVO.setState(ConstantsUtility.PENDING_STATE);
		try {
			log.info("Calling identity management system to add role {} for workspace {} ", workspaceName + "_" + permissionName, workspaceName);
			CreateRoleResponseDto getResponse = identityClient.getRole(createRequestDto.getName());
			if(getResponse!=null && getResponse.getId()!=null) {
				createRoleVO.setId(getResponse.getId());
				createRoleVO.setLink(identityRoleUrl+workspaceName + "_" +  permissionName);
				createRoleVO.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to get role {} for workspace {} . Role fetched successfully with id {} ", workspaceName + "_" +  permissionName, workspaceName, getResponse.getId());
				return createRoleVO;
			}
			CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(createRequestDto);
			if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
				createRoleVO.setId(createRoleResponseDto.getId());
				createRoleVO.setLink(identityRoleUrl+workspaceName + "_" +  permissionName);
				createRoleVO.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to add role {} for workspace {} . Role created successfully with id {} ", workspaceName + "_" +  permissionName, workspaceName, createRoleResponseDto.getId());
			}else {
				createRoleVO.setState(ConstantsUtility.FAILED_STATE);
				log.info("Called identity management system to add role {} for workspace {} . Role creat failed with unknown error", workspaceName + "_" +  permissionName, workspaceName);
			}
		}catch(Exception e) {
			createRoleVO.setState(ConstantsUtility.FAILED_STATE);
			log.error("Called identity management system to add role {} for workspace {} . Failed to create role with error {} ", workspaceName + "_" + permissionName, workspaceName, e.getMessage());
		}
		return createRoleVO;
	}
	
	public RoleDetailsVO updateRoleDetails(EntitlementDetailsVO roleEntitlementVO, RoleDetailsVO existingRoleVO, String workspaceName, String permissionName, String creatorId) {
		EntitlementDetailsVO dnaFabricEntitlementVO = new EntitlementDetailsVO();
		dnaFabricEntitlementVO.setDisplayName(dnaFabricEntitlementName);
		dnaFabricEntitlementVO.setEntitlementId(dnaFabricEntitlementId);
		dnaFabricEntitlementVO.setState(ConstantsUtility.CREATED_STATE);
		
		RoleDetailsVO updatedRole = new RoleDetailsVO();
		Boolean isRoleAvailable = false;
		if(existingRoleVO.getState()!=null) {
			if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingRoleVO.getState())){
				RoleDetailsVO latestRole = this.callRoleCreate(workspaceName,permissionName);
				if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestRole.getState())) {
					isRoleAvailable = true;
				}
				updatedRole = latestRole;
			}else {
				isRoleAvailable = true;
				updatedRole = existingRoleVO;
			}
		}
		if(isRoleAvailable) {
			//add entitlements
			List<EntitlementDetailsVO> adminEntitlementsVO = new ArrayList<>();
			adminEntitlementsVO.add(dnaFabricEntitlementVO);
			adminEntitlementsVO.add(roleEntitlementVO);
			updatedRole.setEntitlements(adminEntitlementsVO);
			if(!ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedRole.getAssignEntitlementsState())) {
				HttpStatus assignAdminEntitlementStatus = identityClient.AssignEntitlementToRole(roleEntitlementVO.getEntitlementId(), updatedRole.getId());
				HttpStatus assignDnaEntitlementStatus = identityClient.AssignEntitlementToRole(dnaFabricEntitlementVO.getEntitlementId(), updatedRole.getId());
				if((assignAdminEntitlementStatus.is2xxSuccessful() || (assignAdminEntitlementStatus.compareTo(HttpStatus.CONFLICT) == 0)) && 
						(assignDnaEntitlementStatus.is2xxSuccessful() || (assignDnaEntitlementStatus.compareTo(HttpStatus.CONFLICT) == 0))) {
					updatedRole.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
				}else {
					updatedRole.setAssignEntitlementsState(ConstantsUtility.INPROGRESS_STATE);
				}
			}
			//assign Role Owner privileges
			if(updatedRole.getRoleOwner()==null || "".equalsIgnoreCase(updatedRole.getRoleOwner())) {
				HttpStatus assignRoleOwnerPrivileges = identityClient.AssignRoleOwnerPrivilegesToCreator(creatorId, updatedRole.getId());
				if(assignRoleOwnerPrivileges.is2xxSuccessful()) {
					updatedRole.setRoleOwner(creatorId);
				}
			}
			//assign Global Role Assigner privileges
			if(updatedRole.getRoleOwner()!=null && !"".equalsIgnoreCase(updatedRole.getRoleOwner()) 
					&& (updatedRole.getGlobalRoleAssigner()==null || "".equalsIgnoreCase(updatedRole.getGlobalRoleAssigner()))) {
				HttpStatus globalRoleAssignerPrivilegesStatus = identityClient.AssignGlobalRoleAssignerPrivilegesToCreator(creatorId, updatedRole.getId());
				if(globalRoleAssignerPrivilegesStatus.is2xxSuccessful()) {
					updatedRole.setGlobalRoleAssigner(creatorId);
				}
			}
			//assign Role Approver privileges
			if(updatedRole.getRoleOwner()!=null && !"".equalsIgnoreCase(updatedRole.getRoleOwner()) 
					&& (updatedRole.getGlobalRoleAssigner()!=null && !"".equalsIgnoreCase(updatedRole.getGlobalRoleAssigner()))
					&& (updatedRole.getRoleApprover()==null || "".equalsIgnoreCase(updatedRole.getRoleApprover()))) {
				HttpStatus roleApproverPrivilegesStatus = identityClient.AssignRoleApproverPrivilegesToCreator(creatorId, updatedRole.getId());
				if(roleApproverPrivilegesStatus.is2xxSuccessful()) {
					updatedRole.setRoleApprover(creatorId);
				}
			}
		}else {
			log.info("Role {} creation and assignment still in progress for {} ", existingRoleVO.getName() ,  workspaceName);
			updatedRole.setAssignEntitlementsState(ConstantsUtility.INPROGRESS_STATE);
			updatedRole.setRoleOwner("");
			updatedRole.setGlobalRoleAssigner("");
			updatedRole.setRoleApprover("");
		}
		return updatedRole;
	}
	
	@Override
	public GroupDetailsVO callGroupAssign(GroupDetailsVO existingGroupDetailsVO, String workspaceId, String permissionName) {
		if(existingGroupDetailsVO!=null) {
			if(existingGroupDetailsVO.getState()!=null) {
				if(!ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(existingGroupDetailsVO.getState())){
					String displayName = existingGroupDetailsVO.getGroupName();
					MicrosoftGroupDetailDto searchResult = fabricWorkspaceClient.searchGroup(displayName);
					if(searchResult!=null) {
						String groupIdentifier = searchResult.getId();
						if(searchResult.getId()!=null) {
							existingGroupDetailsVO.setGroupId(groupIdentifier);
							AddGroupDto addGroupDto = new AddGroupDto();
							addGroupDto.setDisplayName(displayName);
							addGroupDto.setIdentifier(groupIdentifier);
							addGroupDto.setPrincipalType("Group");
							addGroupDto.setGroupUserAccessRight(permissionName);
							GenericMessage addGroupResponse = fabricWorkspaceClient.addGroup(workspaceId,addGroupDto);
							if(addGroupResponse == null || !"SUCCESS".equalsIgnoreCase(addGroupResponse.getSuccess())) {
								log.error("Failed to add "+ displayName +" group to workspace {}", workspaceId);
								existingGroupDetailsVO.setState(ConstantsUtility.FAILED_STATE);
							}else {
								log.info("Successfully added "+ displayName +" Group to workspace {} ", workspaceId);
								existingGroupDetailsVO.setState(ConstantsUtility.ASSIGNED_STATE);
							}
						}
					}
				}
			}
		}
		return existingGroupDetailsVO;
	}
	
	@Override
	public FabricWorkspaceStatusVO fixBugsInWorkspaceUserManagement(FabricWorkspaceStatusVO currentStatus, String workspaceName, String creatorId, String workspaceId) {
		FabricWorkspaceStatusVO updatedVO = new FabricWorkspaceStatusVO();
		updatedVO = currentStatus;
		List<RoleDetailsVO> existingRoles = updatedVO.getRoles();
		List<RoleDetailsVO> updatedRoles = new ArrayList<>();
		for(RoleDetailsVO role : existingRoles) {
			RoleDetailsVO tempRole = role;
			String[] nameSplits = tempRole.getName().split(workspaceName+"_");
			tempRole.setLink(identityRoleUrl+workspaceName + "_" + nameSplits[1]);
			if(ConstantsUtility.PERMISSION_VIEWER.equalsIgnoreCase(nameSplits[1])) {
				Optional<EntitlementDetailsVO> existingViewerEntitlement = currentStatus.getEntitlements()!=null && !currentStatus.getEntitlements().isEmpty() ? 
						currentStatus.getEntitlements().stream().filter(n->(subgroupPrefix +  workspaceId + "_" + ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(n.getDisplayName())).findFirst() : null;
					EntitlementDetailsVO existingViewerEntitlementVO = null;
					if( existingViewerEntitlement!=null && existingViewerEntitlement.isPresent()) {
						existingViewerEntitlementVO = existingViewerEntitlement.get();
					}
				//remove contributor entitlement and add viewer entitlement
				List<EntitlementDetailsVO> roleEntitlementsVO = new ArrayList<>();
				EntitlementDetailsVO dnaFabricEntitlementVO = new EntitlementDetailsVO();
				dnaFabricEntitlementVO.setDisplayName(dnaFabricEntitlementName);
				dnaFabricEntitlementVO.setEntitlementId(dnaFabricEntitlementId);
				dnaFabricEntitlementVO.setState(ConstantsUtility.CREATED_STATE);
				roleEntitlementsVO.add(dnaFabricEntitlementVO);
				roleEntitlementsVO.add(existingViewerEntitlementVO);
				tempRole.setEntitlements(roleEntitlementsVO);
				HttpStatus removeDnaEntitlementStatus = identityClient.removeEntitlementFromRole(subgroupPrefix +  workspaceId + "_" + ConstantsUtility.PERMISSION_CONTRIBUTOR, role.getId());
				HttpStatus assignDnaEntitlementStatus = identityClient.AssignEntitlementToRole(subgroupPrefix +  workspaceId + "_" + ConstantsUtility.PERMISSION_VIEWER, role.getId());
					if(assignDnaEntitlementStatus.is2xxSuccessful() || (assignDnaEntitlementStatus.compareTo(HttpStatus.CONFLICT) == 0)) {
						tempRole.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
					}else {
						tempRole.setAssignEntitlementsState(ConstantsUtility.INPROGRESS_STATE);
					}
				}
			updatedRoles.add(tempRole);
		}
		updatedVO.setRoles(updatedRoles);
		return updatedVO;
	}
	
	
	@Override
	public FabricWorkspaceStatusVO processWorkspaceUserManagement(FabricWorkspaceStatusVO currentStatus, String workspaceName, String creatorId, String workspaceId) {
				if(ConstantsUtility.INPROGRESS_STATE.equalsIgnoreCase(currentStatus.getState())) {
					boolean isAdminEntitlementAvailable = false;
					boolean isContributorEntitlementAvailable = false;
					boolean isMemberEntitlementAvailable = false;
					boolean isViewerEntitlementAvailable = false;
					EntitlementDetailsVO adminEntitlement = new EntitlementDetailsVO();
					EntitlementDetailsVO contributorEntitlement = new EntitlementDetailsVO();
					EntitlementDetailsVO memberEntitlement = new EntitlementDetailsVO();
					EntitlementDetailsVO viewerEntitlement = new EntitlementDetailsVO();
					
					RoleDetailsVO adminRole = new RoleDetailsVO();
					RoleDetailsVO contributorRole = new RoleDetailsVO();
					RoleDetailsVO memberRole = new RoleDetailsVO();
					RoleDetailsVO viewerRole = new RoleDetailsVO();
					
					List<EntitlementDetailsVO> entitlements  = currentStatus.getEntitlements();
					List<EntitlementDetailsVO> updatedEntitlements  = new ArrayList<>();
						//check for admin entitlement
						Optional<EntitlementDetailsVO> existingAdminEntitlement = entitlements!=null && !entitlements.isEmpty() ? 
								entitlements.stream().filter(n->(subgroupPrefix + workspaceId + "_" + ConstantsUtility.PERMISSION_ADMIN).equalsIgnoreCase(n.getDisplayName())).findFirst() : null;
							EntitlementDetailsVO existingAdminEntitlementVO = null;
							if( existingAdminEntitlement!=null && existingAdminEntitlement.isPresent()) {
								existingAdminEntitlementVO = existingAdminEntitlement.get();
							}else {
								existingAdminEntitlementVO = new EntitlementDetailsVO();
								existingAdminEntitlementVO.setState(ConstantsUtility.PENDING_STATE);
							}
							if(existingAdminEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingAdminEntitlementVO.getState())){
									EntitlementDetailsVO latestAdminEntitlement = this.callEntitlementCreate(workspaceName,workspaceId,ConstantsUtility.PERMISSION_ADMIN);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestAdminEntitlement.getState())) {
										isAdminEntitlementAvailable = true;
									}
									updatedEntitlements.add(latestAdminEntitlement);
									adminEntitlement = latestAdminEntitlement;
								}else {
									isAdminEntitlementAvailable = true;
									updatedEntitlements.add(existingAdminEntitlementVO);
									adminEntitlement = existingAdminEntitlementVO;
								}
							}
						//check for contributor entitlement
						Optional<EntitlementDetailsVO> existingContributorEntitlement = entitlements!=null && !entitlements.isEmpty() ? 
								entitlements.stream().filter(n->(subgroupPrefix +  workspaceId + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR).equalsIgnoreCase(n.getDisplayName())).findFirst() : null;
							EntitlementDetailsVO existingContributorEntitlementVO = null;
							if( existingContributorEntitlement!=null && existingContributorEntitlement.isPresent()) {
								existingContributorEntitlementVO = existingContributorEntitlement.get();
							}else {
								existingContributorEntitlementVO = new EntitlementDetailsVO();
								existingContributorEntitlementVO.setState(ConstantsUtility.PENDING_STATE);
							}
							if(existingContributorEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingContributorEntitlementVO.getState())){
									EntitlementDetailsVO latestContributorEntitlement = this.callEntitlementCreate(workspaceName,workspaceId,ConstantsUtility.PERMISSION_CONTRIBUTOR);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestContributorEntitlement.getState())) {
										isContributorEntitlementAvailable = true;
									}
									updatedEntitlements.add(latestContributorEntitlement);
									contributorEntitlement = latestContributorEntitlement;
								}else {
									isContributorEntitlementAvailable = true;
									updatedEntitlements.add(existingContributorEntitlementVO);
									contributorEntitlement = existingContributorEntitlementVO;
								}
							}
						//check for Member entitlement
						Optional<EntitlementDetailsVO> existingMemberEntitlement = entitlements!=null && !entitlements.isEmpty() ? 
								entitlements.stream().filter(n->(subgroupPrefix + workspaceId + "_" + ConstantsUtility.PERMISSION_MEMBER).equalsIgnoreCase(n.getDisplayName())).findFirst() : null;
							EntitlementDetailsVO existingMemberEntitlementVO = null;
							if( existingMemberEntitlement!=null && existingMemberEntitlement.isPresent()) {
								existingMemberEntitlementVO = existingMemberEntitlement.get();
							}else {
								existingMemberEntitlementVO = new EntitlementDetailsVO();
								existingMemberEntitlementVO.setState(ConstantsUtility.PENDING_STATE);
							}
							if(existingMemberEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingMemberEntitlementVO.getState())){
									EntitlementDetailsVO latestMemberEntitlement = this.callEntitlementCreate(workspaceName,workspaceId,ConstantsUtility.PERMISSION_MEMBER);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestMemberEntitlement.getState())) {
										isMemberEntitlementAvailable = true;
									}
									updatedEntitlements.add(latestMemberEntitlement);
									memberEntitlement = latestMemberEntitlement;
								}else {
									isMemberEntitlementAvailable = true;
									updatedEntitlements.add(existingMemberEntitlementVO);
									memberEntitlement = existingMemberEntitlementVO;
								}
							}
						//check for viewer entitlement
						Optional<EntitlementDetailsVO> existingViewerEntitlement = entitlements!=null && !entitlements.isEmpty() ? 
								entitlements.stream().filter(n->(subgroupPrefix +  workspaceId + "_" + ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(n.getDisplayName())).findFirst() : null;
							EntitlementDetailsVO existingViewerEntitlementVO = null;
							if( existingViewerEntitlement!=null && existingViewerEntitlement.isPresent()) {
								existingViewerEntitlementVO = existingViewerEntitlement.get();
							}else {
								existingViewerEntitlementVO = new EntitlementDetailsVO();
								existingViewerEntitlementVO.setState(ConstantsUtility.PENDING_STATE);
							}
							if(existingViewerEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingViewerEntitlementVO.getState())){
									EntitlementDetailsVO latestViewerEntitlement = this.callEntitlementCreate(workspaceName,workspaceId,ConstantsUtility.PERMISSION_VIEWER);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestViewerEntitlement.getState())) {
										isViewerEntitlementAvailable = true;
									}
									updatedEntitlements.add(latestViewerEntitlement);
									viewerEntitlement = latestViewerEntitlement;
								}else {
									isViewerEntitlementAvailable = true;
									updatedEntitlements.add(existingViewerEntitlementVO);
									viewerEntitlement = existingViewerEntitlementVO;
								}
							}
					currentStatus.setEntitlements(updatedEntitlements);
					
					List<RoleDetailsVO> roles  = currentStatus.getRoles();
					List<RoleDetailsVO> updatedRoles  = new ArrayList<>();
						//check for admin Role
						Optional<RoleDetailsVO> existingAdminRole = roles!=null && !roles.isEmpty() ? roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_ADMIN).equalsIgnoreCase(n.getName())).findFirst() : null;
							RoleDetailsVO existingAdminRoleVO = null;
							if(existingAdminRole!=null && existingAdminRole.isPresent()) {
								existingAdminRoleVO = existingAdminRole.get();
							}else {
								existingAdminRoleVO = new RoleDetailsVO();
								existingAdminRoleVO.setState(ConstantsUtility.PENDING_STATE);
								existingAdminRoleVO.setLink(identityRoleUrl+workspaceName + "_" + ConstantsUtility.PERMISSION_ADMIN);
							}
							RoleDetailsVO updatedAdminRoleVO = this.updateRoleDetails(adminEntitlement, existingAdminRoleVO, workspaceName, ConstantsUtility.PERMISSION_ADMIN, creatorId);
							adminRole = updatedAdminRoleVO;
							updatedRoles.add(adminRole);
						//check for contributor Role
						Optional<RoleDetailsVO> existingContributorRole = roles!=null && !roles.isEmpty() ? roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_CONTRIBUTOR).equalsIgnoreCase(n.getName())).findFirst() : null;
							RoleDetailsVO existingContributorRoleVO = null;
							if(existingContributorRole!=null && existingContributorRole.isPresent()) {
								existingContributorRoleVO = existingContributorRole.get();
							}else {
								existingContributorRoleVO = new RoleDetailsVO();
								existingContributorRoleVO.setState(ConstantsUtility.PENDING_STATE);
								existingContributorRoleVO.setLink(identityRoleUrl+workspaceName + "_" + ConstantsUtility.PERMISSION_CONTRIBUTOR);
							}
							RoleDetailsVO updatedContributorRoleVO = this.updateRoleDetails(contributorEntitlement, existingContributorRoleVO, workspaceName, ConstantsUtility.PERMISSION_CONTRIBUTOR, creatorId);
							contributorRole = updatedContributorRoleVO;
							updatedRoles.add(contributorRole);
						//check for member Role
						Optional<RoleDetailsVO> existingMemberRole = roles!=null && !roles.isEmpty() ? roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_MEMBER).equalsIgnoreCase(n.getName())).findFirst() : null;
							RoleDetailsVO existingMemberRoleVO = null;
							if(existingMemberRole!=null && existingMemberRole.isPresent()) {
								existingMemberRoleVO = existingMemberRole.get();
							}else {
								existingMemberRoleVO = new RoleDetailsVO();
								existingMemberRoleVO.setState(ConstantsUtility.PENDING_STATE);
								existingMemberRoleVO.setLink(identityRoleUrl+workspaceName + "_" + ConstantsUtility.PERMISSION_MEMBER);
							}
							RoleDetailsVO updatedMemberRoleVO = this.updateRoleDetails(memberEntitlement, existingMemberRoleVO, workspaceName, ConstantsUtility.PERMISSION_MEMBER, creatorId);
							memberRole = updatedMemberRoleVO;
							updatedRoles.add(memberRole);
						//check for viewer Role
						Optional<RoleDetailsVO> existingViewerRole = roles!=null && !roles.isEmpty() ? roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(n.getName())).findFirst() : null;
							RoleDetailsVO existingViewerRoleVO = null;
							if(existingViewerRole!=null && existingViewerRole.isPresent()) {
								existingViewerRoleVO = existingViewerRole.get();
							}else {
								existingViewerRoleVO = new RoleDetailsVO();
								existingViewerRoleVO.setState(ConstantsUtility.PENDING_STATE);
								existingViewerRoleVO.setLink(identityRoleUrl+workspaceName + "_" + ConstantsUtility.PERMISSION_VIEWER);
							}
							RoleDetailsVO updatedViewerRoleVO = this.updateRoleDetails(viewerEntitlement, existingViewerRoleVO, workspaceName, ConstantsUtility.PERMISSION_VIEWER, creatorId);
							viewerRole = updatedViewerRoleVO;
							updatedRoles.add(viewerRole);
					currentStatus.setRoles(updatedRoles);
					
					//adding group details
					List<GroupDetailsVO> groups  = currentStatus.getMicrosoftGroups();
					List<GroupDetailsVO> updatedMicrosoftFabricGroups = new ArrayList<>();
					List<String> availableGroupNames = new ArrayList<>();
					boolean isAdminGroupAssigned = false;
					GroupDetailsVO adminGroupVO = new GroupDetailsVO();
					boolean isContributorGroupAssigned = false;
					GroupDetailsVO contributorGroupVO = new GroupDetailsVO();
					boolean isMemberGroupAssigned = false;
					GroupDetailsVO memberGroupVO = new GroupDetailsVO();
					boolean isViewerGroupAssigned = false;
					GroupDetailsVO viewerGroupVO = new GroupDetailsVO();
					boolean isAdminGroupAvailable = false;
					boolean isContributorGroupAvailable = false;
					boolean isMemberGroupAvailable = false;
					boolean isViewerGroupAvailable = false;
					//checks if groups are available
					if(groups!=null && !groups.isEmpty()) {
						for(GroupDetailsVO tempGrp : groups) {
							if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_ADMIN) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
								adminGroupVO = tempGrp;
								isAdminGroupAvailable = true;
							}
							if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_CONTRIBUTOR) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
								contributorGroupVO = tempGrp;
								isContributorGroupAvailable = true;
							}
							if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_MEMBER) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
								memberGroupVO = tempGrp;
								isMemberGroupAvailable = true;
							}
							if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_VIEWER) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
								viewerGroupVO = tempGrp;
								isViewerGroupAvailable = true;
							}
						}
					}
					if(!isAdminGroupAvailable) {
						adminGroupVO = new GroupDetailsVO();
						adminGroupVO.setState(ConstantsUtility.PENDING_STATE);
						adminGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_ADMIN);
					}
					if(!isContributorGroupAvailable) {
						contributorGroupVO = new GroupDetailsVO();
						contributorGroupVO.setState(ConstantsUtility.PENDING_STATE);
						contributorGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_CONTRIBUTOR);
					}
					if(!isMemberGroupAvailable) {
						memberGroupVO = new GroupDetailsVO();
						memberGroupVO.setState(ConstantsUtility.PENDING_STATE);
						memberGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_MEMBER);
					}
					if(!isViewerGroupAvailable) {
						viewerGroupVO = new GroupDetailsVO();
						viewerGroupVO.setState(ConstantsUtility.PENDING_STATE);
						viewerGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_VIEWER);
					}
					//check if groups are assigned
					FabricGroupsCollectionDto usersGroupsCollection =	fabricWorkspaceClient.getGroupUsersInfo(workspaceId);
					//change status if available and also assigned, if not create and assign
					if(usersGroupsCollection!=null && usersGroupsCollection.getValue()!=null && !usersGroupsCollection.getValue().isEmpty()) {
						for(AddGroupDto userGroupDetail : usersGroupsCollection.getValue()) {
							if(userGroupDetail!=null && !ConstantsUtility.GROUPPRINCIPAL_APP_TYPE.equalsIgnoreCase(userGroupDetail.getPrincipalType())) {
								if(userGroupDetail.getDisplayName().equalsIgnoreCase(adminGroupVO.getGroupName())) {
									isAdminGroupAssigned = true;
									adminGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
									adminGroupVO.setGroupId(userGroupDetail.getIdentifier());
								}
								if(userGroupDetail.getDisplayName().equalsIgnoreCase(contributorGroupVO.getGroupName())) {
									isContributorGroupAssigned = true;
									contributorGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
									contributorGroupVO.setGroupId(userGroupDetail.getIdentifier());
								}
								if(userGroupDetail.getDisplayName().equalsIgnoreCase(memberGroupVO.getGroupName())) {
									isMemberGroupAssigned = true;
									memberGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
									memberGroupVO.setGroupId(userGroupDetail.getIdentifier());
								}
								if(userGroupDetail.getDisplayName().equalsIgnoreCase(viewerGroupVO.getGroupName())) {
									isViewerGroupAssigned = true;
									viewerGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
									viewerGroupVO.setGroupId(userGroupDetail.getIdentifier());
								}
							}
						}
					}
					if(!isAdminGroupAssigned) {
						adminGroupVO = this.callGroupAssign(adminGroupVO, workspaceId, ConstantsUtility.PERMISSION_ADMIN);
					}
					if(!isContributorGroupAssigned) {
						contributorGroupVO = this.callGroupAssign(contributorGroupVO, workspaceId, ConstantsUtility.PERMISSION_CONTRIBUTOR);
					}
					if(!isMemberGroupAssigned) {
						memberGroupVO = this.callGroupAssign(memberGroupVO, workspaceId, ConstantsUtility.PERMISSION_MEMBER);
					}
					if(!isViewerGroupAssigned) {
						viewerGroupVO = this.callGroupAssign(viewerGroupVO, workspaceId, ConstantsUtility.PERMISSION_VIEWER);
					}
					
					updatedMicrosoftFabricGroups.add(adminGroupVO);
					updatedMicrosoftFabricGroups.add(contributorGroupVO);
					updatedMicrosoftFabricGroups.add(memberGroupVO);
					updatedMicrosoftFabricGroups.add(viewerGroupVO);
					
					currentStatus.setMicrosoftGroups(updatedMicrosoftFabricGroups);
					
					if(adminEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							contributorEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							memberEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							viewerEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(adminRole.getAssignEntitlementsState()) &&
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(contributorRole.getAssignEntitlementsState()) &&
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(memberRole.getAssignEntitlementsState()) &&
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(viewerRole.getAssignEntitlementsState()) && 
							
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(adminGroupVO.getState()) && 
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(contributorGroupVO.getState()) && 
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(memberGroupVO.getState()) && 
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(viewerGroupVO.getState()) ) {
						currentStatus.setState(ConstantsUtility.COMPLETED_STATE);
					}
				}
				return currentStatus;
	}
	
	@Override
	public List<GroupDetailsVO> autoProcessGroupsUsers(List<GroupDetailsVO> existingGroupsDetails, String workspaceName, String creatorId, String workspaceId) {
		List<GroupDetailsVO>  updatedGroups = new ArrayList<>();
		boolean isAdminGroupAvailable = false;
		GroupDetailsVO adminGroupVO = new GroupDetailsVO();
		boolean isContributorGroupAvailable = false;
		GroupDetailsVO contributorGroupVO = new GroupDetailsVO();
		boolean isMemberGroupAvailable = false;
		GroupDetailsVO memberGroupVO = new GroupDetailsVO();
		boolean isViewerGroupAvailable = false;
		GroupDetailsVO viewerGroupVO = new GroupDetailsVO();
		boolean isDefaultGroupAvailable = false;
		for(GroupDetailsVO tempGrp : existingGroupsDetails) {
			if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_ADMIN) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
				adminGroupVO = tempGrp;
			}
			if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_CONTRIBUTOR) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
				contributorGroupVO = tempGrp;
			}
			if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_MEMBER) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
				memberGroupVO = tempGrp;
			}
			if(tempGrp.getGroupName().contains(ConstantsUtility.PERMISSION_VIEWER) && tempGrp.getGroupName().contains(dnaGroupPrefix)) {
				viewerGroupVO = tempGrp;
			}
		}
		//check for all groups and users for cleanup
		FabricGroupsCollectionDto	usersGroupsCollection =	fabricWorkspaceClient.getGroupUsersInfo(workspaceId);
		if(usersGroupsCollection!=null && usersGroupsCollection.getValue()!=null && !usersGroupsCollection.getValue().isEmpty()) {
			for(AddGroupDto userGroupDetail : usersGroupsCollection.getValue()) {
				if(userGroupDetail!=null && !ConstantsUtility.GROUPPRINCIPAL_APP_TYPE.equalsIgnoreCase(userGroupDetail.getPrincipalType())) {
					if(ConstantsUtility.GROUPPRINCIPAL_USER_TYPE.equalsIgnoreCase(userGroupDetail.getPrincipalType())) {
						if(!userGroupDetail.getIdentifier().toLowerCase().contains(creatorId.toLowerCase()+"@")) {
							fabricWorkspaceClient.removeUserGroup(workspaceId, userGroupDetail.getIdentifier());
						}
					}
					else if(ConstantsUtility.GROUPPRINCIPAL_GROUP_TYPE.equalsIgnoreCase(userGroupDetail.getPrincipalType())) {
						if((onboardGroupDisplayName).equalsIgnoreCase(userGroupDetail.getDisplayName())) {
							isDefaultGroupAvailable = true;
						}
						else if(userGroupDetail.getDisplayName().equalsIgnoreCase(adminGroupVO.getGroupName())) {
							isAdminGroupAvailable = true;
							adminGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							adminGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}
						else if(userGroupDetail.getDisplayName().equalsIgnoreCase(contributorGroupVO.getGroupName())) {
							isContributorGroupAvailable = true;
							contributorGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							contributorGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}
						else if(userGroupDetail.getDisplayName().equalsIgnoreCase(memberGroupVO.getGroupName())) {
							isMemberGroupAvailable = true;
							memberGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							memberGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}
						else if(userGroupDetail.getDisplayName().equalsIgnoreCase(viewerGroupVO.getGroupName())) {
							isViewerGroupAvailable = true;
							viewerGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							viewerGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}else {
							//fabricWorkspaceClient.removeUserGroup(workspaceId, userGroupDetail.getDisplayName());
							log.info("Custom group {} assigned to workspace {} , ignoring.", userGroupDetail.getDisplayName(), workspaceId);
						}
					}
				}
			}
		}
		if(!isDefaultGroupAvailable) {
			AddGroupDto addGroupDto = new AddGroupDto();
			addGroupDto.setDisplayName(onboardGroupDisplayName);
			addGroupDto.setIdentifier(onboardGroupIdenitifier);
			addGroupDto.setPrincipalType("Group");
			addGroupDto.setGroupUserAccessRight("Admin");
			GenericMessage addGroupResponse = fabricWorkspaceClient.addGroup(workspaceId,addGroupDto);
			if(addGroupResponse == null || !"SUCCESS".equalsIgnoreCase(addGroupResponse.getSuccess())) {
				log.error("Failed to add default group to workspace {}", workspaceId);
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to add default group to created workspace " + workspaceId + ". Please add Default Group to your workspace manually or contact Admin.");
			}else {
				log.info("Successfully added  default Group to workspace {} ", workspaceId);
			}
		}
		if(isAdminGroupAvailable) {
			updatedGroups.add(adminGroupVO);
		}else {
			log.info("Admin group is missing for workspace {} after provisioning, trying to reassign",workspaceId);
			adminGroupVO = this.callGroupAssign(adminGroupVO, workspaceId, ConstantsUtility.PERMISSION_ADMIN);
			updatedGroups.add(adminGroupVO);
		}
		if(isContributorGroupAvailable) {
			updatedGroups.add(contributorGroupVO);
		}else {
			log.info("Contributor group is missing for workspace {} after provisioning, trying to reassign",workspaceId);
			contributorGroupVO = this.callGroupAssign(contributorGroupVO, workspaceId, ConstantsUtility.PERMISSION_CONTRIBUTOR);
			updatedGroups.add(contributorGroupVO);
		}
		if(isMemberGroupAvailable) {
			updatedGroups.add(memberGroupVO);
		}else {
			log.info("Member group is missing for workspace {} after provisioning, trying to reassign",workspaceId);
			memberGroupVO = this.callGroupAssign(memberGroupVO, workspaceId, ConstantsUtility.PERMISSION_MEMBER);
			updatedGroups.add(memberGroupVO);
		}
		if(isViewerGroupAvailable) {
			updatedGroups.add(viewerGroupVO);
		}else {
			log.info("Viewer group is missing for workspace {} after provisioning, trying to reassign",workspaceId);
			viewerGroupVO = this.callGroupAssign(viewerGroupVO, workspaceId, ConstantsUtility.PERMISSION_VIEWER);
			updatedGroups.add(viewerGroupVO);
		}
		return updatedGroups;
	}
	
	@Override
	@Transactional
	public GenericMessage delete(String id, boolean skipDeleteFabricWorkspace) {
		FabricWorkspaceVO existingWorkspace = this.getById(id);
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			if(!skipDeleteFabricWorkspace) {
				ErrorResponseDto deleteResponse = fabricWorkspaceClient.deleteWorkspace(id);
				if(deleteResponse!=null && deleteResponse.getMessage() != null) {
						MessageDescription message = new MessageDescription();
						message.setMessage(deleteResponse.getMessage());
						warnings.add(message);
//						responseMessage.setErrors(errors);
//						responseMessage.setSuccess("FAILED");
						log.warn("Error occurred:{} while deleting fabric workspace project {} ", id);
//						return responseMessage;
				}
			}
			if(existingWorkspace!=null && existingWorkspace.getStatus()!=null ) {
				if(existingWorkspace.getStatus().getEntitlements()!=null && !existingWorkspace.getStatus().getEntitlements().isEmpty()) {
					for(EntitlementDetailsVO entitlement : existingWorkspace.getStatus().getEntitlements()) {
						if(entitlement!=null && ConstantsUtility.CREATED_STATE.equalsIgnoreCase(entitlement.getState())) {
							GenericMessage deleteEntitlementResponse = identityClient.deleteEntitlement(entitlement.getEntitlementId());
							errors.addAll(deleteEntitlementResponse.getErrors());
							warnings.addAll(deleteEntitlementResponse.getWarnings());
						}
					}
				}
				if(existingWorkspace.getStatus().getRoles()!=null && !existingWorkspace.getStatus().getRoles().isEmpty()) {
					for(RoleDetailsVO role : existingWorkspace.getStatus().getRoles()) {
						if(role!=null && ConstantsUtility.CREATED_STATE.equalsIgnoreCase(role.getState())) {
							GenericMessage deleteRoleResponse = identityClient.deleteRole(role.getName());
							errors.addAll(deleteRoleResponse.getErrors());
							warnings.addAll(deleteRoleResponse.getWarnings());
						}
					}
				}
			}
			super.deleteById(id);
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to delete workspace with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred:{} while deleting fabric workspace project {} ", id);
			return responseMessage;
		}
	}
	

	@Override
	@Transactional
	public FabricWorkspaceVO updateFabricProject(FabricWorkspaceVO existingFabricWorkspace) {
		WorkspaceUpdateDto updateRequest = new WorkspaceUpdateDto();
		try {
			updateRequest.setDescription(existingFabricWorkspace.getDescription());
			updateRequest.setDisplayName(existingFabricWorkspace.getName());
			WorkspaceDetailDto updateResponse = fabricWorkspaceClient.updateWorkspace(existingFabricWorkspace.getId(), updateRequest);
		}catch(Exception e) {
			log.error("Failed to update project {} details in MicrosoftFabric, Will be updated in next action.", existingFabricWorkspace.getId());
		}
		FabricWorkspaceNsql updatedEntity = assembler.toEntity(existingFabricWorkspace);
		jpaRepo.save(updatedEntity);
		return existingFabricWorkspace;
	}

	@Override
	@Transactional
	public GenericMessage deleteLakehouse(String id, String lakehouseId) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			ErrorResponseDto deleteResponse = fabricWorkspaceClient.deleteLakehouse(id, lakehouseId);
			if(deleteResponse!=null && deleteResponse.getMessage() != null) {
					MessageDescription message = new MessageDescription();
					message.setMessage(deleteResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred:{} while deleting fabric workspace lakehouse {} ", id,lakehouseId);
					return responseMessage;
			}
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to delete lakehouse for workspace with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred:{} while deleting fabric workspace {} lakehouse {} ", id, lakehouseId);
			return responseMessage;
		}
	}
	
	@Override
	@Transactional
	public GenericMessage deleteLakehouseS3Shortcut(String id, String lakehouseId, String shortcutName) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			ErrorResponseDto deleteResponse = fabricWorkspaceClient.deleteShortcut(id, lakehouseId,shortcutName);
			if(deleteResponse!=null && deleteResponse.getMessage() != null) {
					MessageDescription message = new MessageDescription();
					message.setMessage(deleteResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred: while deleting fabric workspace id {} lakehouse {} shortcut {} ", id, lakehouseId, shortcutName);
					return responseMessage;
			}
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to delete shortcut for workspace lakehouse with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred: while deleting fabric workspace id {} lakehouse {} shortcut {} ", id, lakehouseId, shortcutName);
			return responseMessage;
		}
	}

	@Override
	public GenericMessage createLakehouse(String id,  FabricLakehouseCreateRequestVO createRequestVO) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		CreateLakehouseDto createLakehouseDto = new CreateLakehouseDto();
		try {
			createLakehouseDto.setDescription(createRequestVO.getDescription());
			createLakehouseDto.setDisplayName(createRequestVO.getName());
			LakehouseResponseDto createResponse = fabricWorkspaceClient.createLakehouse(id, createLakehouseDto);
			if(createResponse!=null && createResponse.getMessage() != null) {
					MessageDescription message = new MessageDescription();
					message.setMessage(createResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred:{} while creating fabric workspace lakehouse {} ", id);
					return responseMessage;
			}
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to creating lakehouse for workspace with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred:{} while creating fabric workspace {} lakehouse {} ", id, createRequestVO.getName());
			return responseMessage;
		}
	}

	@Override
	public GenericMessage createLakehouseS3Shortcut(String id, String lakehouseId,
			 ShortcutCreateRequestVO createRequestVO, String email) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		LakehouseS3ShortcutDto createLakehouseS3ShortcutDto = new LakehouseS3ShortcutDto();
		String connectionId = "";
		String randomSeriesNumber = UUID.randomUUID().toString();
		try {
			CreateDatasourceRequestDto createDatsourceReqDto = new CreateDatasourceRequestDto();
			createDatsourceReqDto.setDatasourcename(shortcutNameprefix+createRequestVO.getBucketname() + "_" + randomSeriesNumber);
			createDatsourceReqDto.setDatasourceType("extension");
			String connectionDetails = "{\"extensionDataSourceKind\": \"AmazonS3Compatible\",\"extensionDataSourcePath\": \"" + shortcutLocation + "\"}";
			createDatsourceReqDto.setConnectionDetails(connectionDetails);
			
			CredentialDetailsDto credentialsDto = new CredentialDetailsDto();
			credentialsDto.setCredentialType(datasourceCredentialType);
			credentialsDto.setEncryptedConnection(datasourceEncryptedConnection);
			credentialsDto.setEncryptionAlgorithm(datasourceEncryptionAlgorithm);
			credentialsDto.setPrivacyLevel("Organizational");
			
			String credentialsEncryptedString = "";
			credentialsEncryptedString = encryptionUtil.encryptCredentialDetails(createRequestVO.getAccessKey(), createRequestVO.getSecretKey());
			if(credentialsEncryptedString == null || "".equalsIgnoreCase(credentialsEncryptedString)) {
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to create shortcut, error occured while encrypting connection details with gateway's public key. Please retry again after a while.");
				errors.add(message);
				responseMessage.setErrors(errors);
				responseMessage.setSuccess("FAILED");
				log.error("Failed to create shortcut for workspace {} and lakehouse {} , error occured while encrypting connection details with gateway's public key", id, lakehouseId);
				return responseMessage;
			}
			credentialsDto.setCredentials(credentialsEncryptedString);
			
			credentialsDto.setSkipTestConnection(true);
			credentialsDto.setUseEndUserOAuth2Credentials(false);
			
			createDatsourceReqDto.setCredentialDetails(credentialsDto);
			DatasourceResponseDto createResponse = fabricWorkspaceClient.createDatasourceConnection(id, createDatsourceReqDto);
			if(createResponse!=null && createResponse.getMessage() != null || createResponse==null || (createResponse!=null && (createResponse.getId() ==null || "".equalsIgnoreCase(createResponse.getId())))) {
					MessageDescription message = new MessageDescription();
					message.setMessage(createResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred:{} while creating fabric workspace datasource connection for shortcut {} ", id);
					return responseMessage;
			}
			connectionId = createResponse.getId();
			
			try {
				GenericMessage addUserToConnectionResponse = fabricWorkspaceClient.addUserToDatasource(connectionId,email);
				if(addUserToConnectionResponse!=null && addUserToConnectionResponse.getErrors()!=null) {
					warnings.addAll(addUserToConnectionResponse.getErrors());
					warnings.addAll(addUserToConnectionResponse.getWarnings());
				}
			}catch(Exception e) {
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to add user to datasource connection " + connectionId + ". Please contact admin to add manually.");
				warnings.add(message);
			}
			
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Error occured while creating connection. Failed to creating lakehouse  s3 shortcut for workspace with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred:{} while creating connection for fabric s3 shortcut {} for workspace {} lakehouse {} ",createRequestVO.getBucketname(), id, lakehouseId);
			return responseMessage;
		}
		
		try {
			
			createLakehouseS3ShortcutDto.setName( shortcutNameprefix + randomSeriesNumber);
			createLakehouseS3ShortcutDto.setPath("Files/");
			ShortcutTargetDto targetDto = new ShortcutTargetDto();
			S3CompatibleTargetDto s3Target = new S3CompatibleTargetDto();
			s3Target.setBucket(createRequestVO.getBucketname());
			s3Target.setConnectionId(connectionId);
			s3Target.setLocation(shortcutLocation);
			s3Target.setSubpath("");
			targetDto.setS3Compatible(s3Target);
			
			createLakehouseS3ShortcutDto.setTarget(targetDto);
			LakehouseS3ShortcutResponseDto createResponse = fabricWorkspaceClient.createShortcut(id, lakehouseId, createLakehouseS3ShortcutDto);
			if(createResponse!=null && createResponse.getMessage() != null) {
					MessageDescription message = new MessageDescription();
					message.setMessage(createResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred:{} while creating fabric workspace lakehouse s3 shortcut {} ", id);
					return responseMessage;
			}
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to creating lakehouse  s3 shortcut for workspace with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred:{} while creating fabric  s3 shortcut {} for workspace {} lakehouse {} ",createRequestVO.getBucketname(), id, lakehouseId);
			return responseMessage;
		}
	}

	@Override
	public FabricShortcutsCollectionVO getLakehouseS3Shortcuts(String id, String lakehouseId) {
		FabricShortcutsCollectionVO collectionVO = new FabricShortcutsCollectionVO();
		LakehouseS3ShortcutCollectionDto collection = fabricWorkspaceClient.listLakehouseshortcuts(id, lakehouseId);
		if(collection!=null && collection.getValue()!=null && !collection.getValue().isEmpty()) {
			Integer totalRecords = collection.getValue().size();
			collectionVO.setTotalCount(totalRecords);
			List<ShortcutVO> records = new ArrayList<>();
			records = collection.getValue().stream().map(n -> assembler.toLakehouseShortcutVOFromDto(n)).collect(Collectors.toList());
			collectionVO.setRecords(records);
		}else {
			collectionVO.setTotalCount(0);
			collectionVO.setRecords(new ArrayList<>());
		}
		return collectionVO;
	}
	
	@Override
    public GenericMessage requestRoles(FabricWorkspaceRoleRequestVO roleRequestVO, String userId, String authToken){
        GenericMessage response = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        List<MessageDescription> warnings = new ArrayList<>();
 
        try{
            List<RolesVO> roleList = roleRequestVO.getData().getRoleList();
            for(RolesVO role : roleList){
                UserRoleRequestDto roleRequestDto = new UserRoleRequestDto();
                roleRequestDto.setReason(roleRequestVO.getData().getReason());
                roleRequestDto.setValidFrom(role.getValidFrom());
                roleRequestDto.setValidTo(role.getValidTo());
                HttpStatus status = identityClient.RequestRoleForUser(roleRequestDto, userId, role.getRoleID(),authToken);
                if(!status.is2xxSuccessful()){
                    warnings.add(new MessageDescription("Failed to request role for role id : "+role.getRoleID()+" please request role manually or try after sometime"));
                }
            }
        }
        catch(Exception e){
            errors.add(new MessageDescription("Failed to request roles for the user  with exception " + e.getMessage()));
            response.setErrors(errors);
            response.setSuccess("FAILED");
            log.error("Failed to request role  Fabric workspace with exception {} ",e.getMessage());
            return response;
        }
        response.setSuccess(!warnings.isEmpty() ? "WARNING" : "SUCCESS");
        response.setWarnings(warnings);
        response.setErrors(errors);
        return response;
    }

	@Override
	public GenericMessage createGenericRole(CreateRoleRequestVO roleRequestVO, String creatorId){
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try{

			CreateRoleResponseDto getResponse = identityClient.getRole(roleRequestVO.getData().getRoleName());
			if(getResponse!=null && getResponse.getId()!=null) {
				errors.add(new MessageDescription("Failed to create role : Role Already Exists."));
					response.setErrors(errors);
					response.setWarnings(warnings);
					response.setSuccess("CONFLICT");
					log.error("Failed to create role, Role Already Exists");
					return response;
			}else{
				RoleDetailsVO roleDetail = this.callGenericRoleCreate(roleRequestVO.getData().getRoleName());
				if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(roleDetail.getState())) {
					//assign Role Owner privileges
					if(roleDetail.getRoleOwner()==null || "".equalsIgnoreCase(roleDetail.getRoleOwner())) {
						HttpStatus assignRoleOwnerPrivileges = identityClient.AssignRoleOwnerPrivilegesToCreator(creatorId, roleDetail.getId());
						if(assignRoleOwnerPrivileges.is2xxSuccessful()) {
							roleDetail.setRoleOwner(creatorId);
						}else{
							warnings.add(new MessageDescription("Failed to assign role owner privilage role for user, please contact admin."));
						}
					}
					//assign Global Role Assigner privileges
					if(roleDetail.getRoleOwner()!=null && !"".equalsIgnoreCase(roleDetail.getRoleOwner()) 
							&& (roleDetail.getGlobalRoleAssigner()==null || "".equalsIgnoreCase(roleDetail.getGlobalRoleAssigner()))) {
						HttpStatus globalRoleAssignerPrivilegesStatus = identityClient.AssignGlobalRoleAssignerPrivilegesToCreator(creatorId, roleDetail.getId());
						if(globalRoleAssignerPrivilegesStatus.is2xxSuccessful()) {
							roleDetail.setGlobalRoleAssigner(creatorId);
						}else{
							warnings.add(new MessageDescription("Failed to assign global role assigner privilage role for user, please contact admin."));
						}
					}
					//assign Role Approver privileges
					if(roleDetail.getRoleOwner()!=null && !"".equalsIgnoreCase(roleDetail.getRoleOwner()) 
							&& (roleDetail.getGlobalRoleAssigner()!=null && !"".equalsIgnoreCase(roleDetail.getGlobalRoleAssigner()))
							&& (roleDetail.getRoleApprover()==null || "".equalsIgnoreCase(roleDetail.getRoleApprover()))) {
						HttpStatus roleApproverPrivilegesStatus = identityClient.AssignRoleApproverPrivilegesToCreator(creatorId, roleDetail.getId());
						if(roleApproverPrivilegesStatus.is2xxSuccessful()) {
							roleDetail.setRoleApprover(creatorId);
						}else{
							warnings.add(new MessageDescription("Failed to assign role approver privilage role for user, please contact admin."));
						}
					}
					//create entitlement
					EntitlementDetailsVO entitlementDetail = this.callGenericEntitlementCreate(roleRequestVO.getData().getRoleName());
					if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(entitlementDetail.getState())){
						//assign entitlement to role
						HttpStatus assignEntitlementToRoleStatus = identityClient.AssignEntitlementToRole(entitlementDetail.getEntitlementId(), roleDetail.getId());
						if((assignEntitlementToRoleStatus.is2xxSuccessful() || (assignEntitlementToRoleStatus.compareTo(HttpStatus.CONFLICT) == 0))) {
							response.setSuccess("SUCCESS");
							response.setErrors(errors);
							response.setWarnings(warnings);
							log.error("Generic role created successfully.");
							return response;
						}else {
							warnings.add(new MessageDescription("Failed to assign entitlement to role, please contact admin."));
						}
					}else{
						errors.add(new MessageDescription("Failed to create role : Error occured while creating entitlement, please try again."));
						response.setErrors(errors);
						response.setWarnings(warnings);
						response.setSuccess("FAILED");
						log.error("Failed to create role, Error while creating entitlement");
						return response;
					}
				}else{
					errors.add(new MessageDescription("Failed to create role : Error occured while creating role, please try again."));
					response.setErrors(errors);
					response.setWarnings(warnings);
					response.setSuccess("FAILED");
					log.error("Failed to create role, Error while creating role");
					return response;
				}
			}
		}catch(Exception e){
			errors.add(new MessageDescription("Failed to create role for the user  with exception " + e.getMessage()));
            response.setErrors(errors);
			response.setWarnings(warnings);
            response.setSuccess("FAILED");
            log.error("Failed to create role  Fabric workspace with exception {} ",e.getMessage());
            return response;
		}
		return response;
	}

	public CreateEntitlementRequestDto prepareGenericEntitlementCreateRequestDto(String entitlementName) {
		CreateEntitlementRequestDto entitlementRequestDto = new CreateEntitlementRequestDto();
		entitlementRequestDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
		entitlementRequestDto.setEntitlementId(entitlementName);
		entitlementRequestDto.setDisplayName(entitlementName);
		entitlementRequestDto.setDescription("Generic DNA Entitlement");
		entitlementRequestDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
		entitlementRequestDto.setDataClassificationInherited(false);
		entitlementRequestDto.setConnectivity(false);
		return entitlementRequestDto;
	}
	
	public EntitlementDetailsVO callGenericEntitlementCreate(String entitlementName) {
		CreateEntitlementRequestDto createRequestDto = this.prepareGenericEntitlementCreateRequestDto(entitlementName);
		EntitlementDetailsVO requestedEntitlement = new EntitlementDetailsVO();
		requestedEntitlement.setDisplayName(entitlementName);
		requestedEntitlement.setState(ConstantsUtility.PENDING_STATE);
		try {
			log.info("Calling identity management system to add Generic entitlement");
			EntiltlemetDetailsDto getResponse = identityClient.getEntitlement(createRequestDto.getDisplayName());
			if(getResponse!=null && getResponse.getUuid()!=null) {
				requestedEntitlement.setEntitlementId(getResponse.getEntitlementId());
				requestedEntitlement.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to get generic entitlement. Entitlement fetched successfully with id {} ", getResponse.getUuid());
				return requestedEntitlement;
			}
			EntiltlemetDetailsDto entitlementCreateResponse = identityClient.createEntitlement(createRequestDto);
			if(entitlementCreateResponse!=null && entitlementCreateResponse.getEntitlementId()!=null) {
				requestedEntitlement.setEntitlementId(entitlementCreateResponse.getEntitlementId());
				requestedEntitlement.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to add generic entitlement. Entitlement created successfully with id {} ", entitlementCreateResponse.getEntitlementId());
			}else {
				requestedEntitlement.setState(ConstantsUtility.FAILED_STATE);
				log.info("Called identity management system to add generic entitlement : {} . Entitlement creat failed with unknown error",entitlementName);
			}
		}catch(Exception e) {
			requestedEntitlement.setState(ConstantsUtility.FAILED_STATE);
			log.error("Called identity management system to add generic entitlement. Failed to create entitlement with error {} ", e.getMessage());
		}
		return requestedEntitlement;
	}

	public CreateRoleRequestDto prepareGenericRoleCreateRequestDto(String roleName) {
		String[] communityAvailabilitySplits = communityAvailability.split(",");
		AccessReviewDto accessReview = new AccessReviewDto();
		accessReview.setEnabled(true);
		accessReview.setStartDate(formatter.format(new Date()));
		List<ReviewerConfigDto> reviewersConfig = new ArrayList<>();
		ReviewerConfigDto roleApprover = new ReviewerConfigDto();
		roleApprover.setType("ROLE_APPROVER");
		roleApprover.setUserCommunity(communityAvailabilitySplits[0]);
		reviewersConfig.add(roleApprover);
		accessReview.setReviewerConfigs(reviewersConfig);
		
		WorkflowDefinitionDto workflow = new WorkflowDefinitionDto();
		workflow.setId(Integer.valueOf(workflowDefinitionId));
		
		CreateRoleRequestDto roleRequestDto = new CreateRoleRequestDto();
		roleRequestDto.setAccessReview(accessReview);
		
		roleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
		roleRequestDto.setDataClassification("CONFIDENTIAL");
		roleRequestDto.setDefaultValidityType("OPTIONAL");
		roleRequestDto.setDeprovisioning(false);
		roleRequestDto.setDescription("Generic DNA role");
		roleRequestDto.setDynamic(false);
		roleRequestDto.setGlobalCentralAvailable(true);
		roleRequestDto.setId(roleName);
		roleRequestDto.setJobTitle(false);
		roleRequestDto.setMarketAvailabilities(new ArrayList<>());
		roleRequestDto.setName(roleName);
		roleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
		roleRequestDto.setNeedsCustomScopes(false);
		roleRequestDto.setNeedsOrgScopes(false);
		roleRequestDto.setNotificationsActive(true);
		roleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
		roleRequestDto.setRoleType("BUSINESS");
		roleRequestDto.setSelfRequestable(true);
		roleRequestDto.setWorkflowBased(true);
		roleRequestDto.setWorkflowDefinition(workflow);
		return roleRequestDto;
	}
	
	public RoleDetailsVO callGenericRoleCreate(String roleName) {
		CreateRoleRequestDto createRequestDto = this.prepareGenericRoleCreateRequestDto(roleName);
		RoleDetailsVO createRoleVO = new RoleDetailsVO();
		createRoleVO.setName(roleName);
		try {
			log.info("Calling identity management system to add generic role");
			CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(createRequestDto);
			if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
				createRoleVO.setId(createRoleResponseDto.getId());
				createRoleVO.setLink(identityRoleUrl +roleName);
				createRoleVO.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to add generic role. Role created successfully with id {} ", createRoleResponseDto.getId());
			}else {
				createRoleVO.setState(ConstantsUtility.FAILED_STATE);
				log.info("Called identity management system to add generic role: {}. Role create failed with unknown error", roleName);
			}
		}catch(Exception e) {
			createRoleVO.setState(ConstantsUtility.FAILED_STATE);
			log.error("Called identity management system to add generic role. Failed to create role with error {} ", e.getMessage());
		}
		return createRoleVO;
	}

	@Override
public DnaRoleCollectionVO getAllUserDnaRoles(String id, String authToken) {
    DnaRoleCollectionVO dnaRoleCollection = new DnaRoleCollectionVO();
	DnaRoleCollectionVOData data = new DnaRoleCollectionVOData();
    List<String> roles = new ArrayList<>();
    try {
        List<String> roleList = identityClient.getAllUserManagableRoles(id, authToken);
        roles = roleList.stream()
                        .filter(role -> role.startsWith("DNA_"))
                        .collect(Collectors.toList());
		data.setRoles(roles);
        dnaRoleCollection.setData(data);
    } catch (Exception e) {
        log.error("Error occurred while getting user roles: {}", e.getMessage());
    }
    return dnaRoleCollection;
}

}
