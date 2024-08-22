package com.daimler.data.service.fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.forecast.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.forecast.FabricWorkspaceRepository;
import com.daimler.data.dto.fabric.AccessReviewDto;
import com.daimler.data.dto.fabric.AddGroupDto;
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.FabricGroupsCollectionDto;
import com.daimler.data.dto.fabric.MicrosoftGroupDetailDto;
import com.daimler.data.dto.fabric.ReviewerConfigDto;
import com.daimler.data.dto.fabric.WorkflowDefinitionDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspaceUpdateDto;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.EntitlementDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RoleDetailsVO;
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
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
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
				try {
					String id = entity.getId();
					log.info("Fetched fabric project record from db successfully for id {} ", id);
					WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
					if(dtoFromFabric!=null) {
						if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
								log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
								jpaRepo.deleteById(id);
								log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
						}else {
							entity.getData().setName(dtoFromFabric.getDisplayName());
							entity.getData().setDescription(dtoFromFabric.getDescription());
							jpaRepo.save(entity);
							FabricWorkspaceVO updatedVO = assembler.toVo(entity);
							vos.add(updatedVO);
						}
					}
				}catch(Exception e) {
					log.error("Failed to update Fabric workspace record of id {} during get all records",entity.getId());
					FabricWorkspaceVO updatedVO = assembler.toVo(entity);
					vos.add(updatedVO);
				}
			}
		}
		List<FabricWorkspaceVO> paginatedVOs = new ArrayList<>();
		int totalCount = 0;
		if(vos!=null && !vos.isEmpty()) {
			totalCount = vos.size();
			int newOffset = offset>vos.size() ? 0 : offset;
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
			for(FabricWorkspaceNsql existingEntity : allEntities) {
				if(existingEntity!=null) {
					List<String> filteredEntitlements = new ArrayList<>();
					if(allEntitlementsList!=null && !allEntitlementsList.isEmpty()) {
						filteredEntitlements = allEntitlementsList.stream().filter(n-> n.contains( applicationId + "." + subgroupPrefix + existingEntity.getId() + "_")).collect(Collectors.toList());
					}
					String creatorId = existingEntity.getData().getCreatedBy().getId();
					if(!(!user.equalsIgnoreCase(creatorId) && (filteredEntitlements==null || filteredEntitlements.isEmpty()))) {
						filteredEntities.add(existingEntity);
					}
				}
			}
		}
		if (filteredEntities != null && !filteredEntities.isEmpty()) {
			for(FabricWorkspaceNsql entity : filteredEntities) {
				try {
					String id = entity.getId();
					log.info("Fetched fabric project record from db successfully for id {} ", id);
					WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
					if(dtoFromFabric!=null) {
						if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
								log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
								jpaRepo.deleteById(id);
								log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
						}else {
							entity.getData().setName(dtoFromFabric.getDisplayName());
							entity.getData().setDescription(dtoFromFabric.getDescription());
							jpaRepo.save(entity);
							FabricWorkspaceVO updatedVO = assembler.toVo(entity);
							vos.add(updatedVO);
						}
					}
				}catch(Exception e) {
					log.error("Failed to update Fabric workspace record of id {} during get all records",entity.getId());
					FabricWorkspaceVO updatedVO = assembler.toVo(entity);
					vos.add(updatedVO);
				}
			}
		}
		List<FabricWorkspaceVO> paginatedVOs = new ArrayList<>();
		int totalCount = 0;
		if(vos!=null && !vos.isEmpty()) {
			totalCount = vos.size();
			int newOffset = offset>vos.size() ? 0 : offset;
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
		WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
		if(dtoFromFabric!=null) {
			if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
				log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
				try{
					jpaRepo.deleteById(id);
					log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
				}catch(Exception e) {
					log.error("Project id {} not found in Microsoft Fabric. Failed to remove from database, will remove in next fetch", id);
				}
				return null;
			}
			voFromDb.setName(dtoFromFabric.getDisplayName());
			voFromDb.setDescription(dtoFromFabric.getDescription());
			try {
				FabricWorkspaceNsql updatedEntity = assembler.toEntity(voFromDb);
				log.info("Successfully updated latest displayName and description from Fabric to Database for project id {}", id);
				jpaRepo.save(updatedEntity);
			}catch(Exception e) {
				log.error("Failed to update latest displayName and description from Fabric to Database for project id {} . Will be updated in next fetch", id);
			}
		}
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
					//check for admin group
					Optional<GroupDetailsVO> existingAdminGroup = groups!=null && !groups.isEmpty() ? groups.stream().filter(n->(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_ADMIN).equalsIgnoreCase(n.getGroupName())).findFirst() : null;
					GroupDetailsVO existingAdminGroupVO = null;
							if(existingAdminGroup!=null && existingAdminGroup.isPresent()) {
								existingAdminGroupVO = existingAdminGroup.get();
							}else {
								existingAdminGroupVO = new GroupDetailsVO();
								existingAdminGroupVO.setState(ConstantsUtility.PENDING_STATE);
								existingAdminGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_ADMIN);
							}
							
							GroupDetailsVO updatedAdminRoleGroupVO = this.callGroupAssign(existingAdminGroupVO, workspaceId, ConstantsUtility.PERMISSION_ADMIN);
							
					//check for contributor group
					Optional<GroupDetailsVO> existingContributorGroup = groups!=null && !groups.isEmpty() ? groups.stream().filter(n->(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_CONTRIBUTOR).equalsIgnoreCase(n.getGroupName())).findFirst() : null;
					GroupDetailsVO existingContributorGroupVO = null;
							if(existingContributorGroup!=null && existingContributorGroup.isPresent()) {
								existingContributorGroupVO = existingContributorGroup.get();
							}else {
								existingContributorGroupVO = new GroupDetailsVO();
								existingContributorGroupVO.setState(ConstantsUtility.PENDING_STATE);
								existingContributorGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_CONTRIBUTOR);
							}
							
							GroupDetailsVO updatedContributorGroupVO = this.callGroupAssign(existingContributorGroupVO, workspaceId, ConstantsUtility.PERMISSION_CONTRIBUTOR);
							
					//check for member group
					Optional<GroupDetailsVO> existingMemberGroup = groups!=null && !groups.isEmpty() ? groups.stream().filter(n->(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_MEMBER).equalsIgnoreCase(n.getGroupName())).findFirst() : null;
					GroupDetailsVO existingMemberGroupVO = null;
							if(existingMemberGroup!=null && existingMemberGroup.isPresent()) {
								existingMemberGroupVO = existingMemberGroup.get();
							}else {
								existingMemberGroupVO = new GroupDetailsVO();
								existingMemberGroupVO.setState(ConstantsUtility.PENDING_STATE);
								existingMemberGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_MEMBER);
							}
							
							GroupDetailsVO updatedMemberRoleGroupVO = this.callGroupAssign(existingMemberGroupVO, workspaceId, ConstantsUtility.PERMISSION_MEMBER);
							
					//check for viewer group
					Optional<GroupDetailsVO> existingViewerGroup = groups!=null && !groups.isEmpty() ? groups.stream().filter(n->(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(n.getGroupName())).findFirst() : null;
					GroupDetailsVO existingViewerGroupVO = null;
							if(existingViewerGroup!=null && existingViewerGroup.isPresent()) {
								existingViewerGroupVO = existingViewerGroup.get();
							}else {
								existingViewerGroupVO = new GroupDetailsVO();
								existingViewerGroupVO.setState(ConstantsUtility.PENDING_STATE);
								existingViewerGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_VIEWER);
							}
							
							GroupDetailsVO updatedViewerRoleGroupVO = this.callGroupAssign(existingViewerGroupVO, workspaceId, ConstantsUtility.PERMISSION_VIEWER);
					
					FabricGroupsCollectionDto	usersGroupsCollection =	fabricWorkspaceClient.getGroupUsersInfo(workspaceId);
					List<String> availableGroupNames = new ArrayList<>();
					if(usersGroupsCollection!=null && usersGroupsCollection.getValue()!=null && !usersGroupsCollection.getValue().isEmpty()) {
						availableGroupNames = usersGroupsCollection.getValue().stream().map(n->n.getDisplayName()).collect(Collectors.toList());
					}
					if(!ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedAdminRoleGroupVO.getState())) {
						if(availableGroupNames!=null && !availableGroupNames.isEmpty() && availableGroupNames.contains(updatedAdminRoleGroupVO.getGroupName())) {
							updatedAdminRoleGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
						}
					}
					if(!ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedContributorGroupVO.getState())) {
						if(availableGroupNames!=null && !availableGroupNames.isEmpty() && availableGroupNames.contains(updatedContributorGroupVO.getGroupName())) {
							updatedContributorGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
						}
					}
					if(!ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedMemberRoleGroupVO.getState())) {
						if(availableGroupNames!=null && !availableGroupNames.isEmpty() && availableGroupNames.contains(updatedMemberRoleGroupVO.getGroupName())) {
							updatedMemberRoleGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
						}
					}
					if(!ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedViewerRoleGroupVO.getState())) {
						if(availableGroupNames!=null && !availableGroupNames.isEmpty() && availableGroupNames.contains(updatedViewerRoleGroupVO.getGroupName())) {
							updatedViewerRoleGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
						}
					}
					updatedMicrosoftFabricGroups.add(updatedAdminRoleGroupVO);
					updatedMicrosoftFabricGroups.add(updatedContributorGroupVO);
					updatedMicrosoftFabricGroups.add(updatedMemberRoleGroupVO);
					updatedMicrosoftFabricGroups.add(updatedViewerRoleGroupVO);
					
					currentStatus.setMicrosoftGroups(updatedMicrosoftFabricGroups);
					
					if(adminEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							contributorEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							memberEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							viewerEntitlement.getState().equalsIgnoreCase(ConstantsUtility.CREATED_STATE) && 
							
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(adminRole.getAssignEntitlementsState()) &&
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(contributorRole.getAssignEntitlementsState()) &&
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(memberRole.getAssignEntitlementsState()) &&
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(viewerRole.getAssignEntitlementsState()) && 
							
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedAdminRoleGroupVO.getState()) && 
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedContributorGroupVO.getState()) && 
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedMemberRoleGroupVO.getState()) && 
							ConstantsUtility.ASSIGNED_STATE.equalsIgnoreCase(updatedViewerRoleGroupVO.getState()) ) {
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
						else if((dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_ADMIN).equalsIgnoreCase(userGroupDetail.getDisplayName())) {
							isAdminGroupAvailable = true;
							adminGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							adminGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_ADMIN);
							adminGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}
						else if((dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_CONTRIBUTOR).equalsIgnoreCase(userGroupDetail.getDisplayName())) {
							isContributorGroupAvailable = true;
							contributorGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							contributorGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_CONTRIBUTOR);
							contributorGroupVO.setGroupId(userGroupDetail.getIdentifier());

						}
						else if((dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_MEMBER).equalsIgnoreCase(userGroupDetail.getDisplayName())) {
							isMemberGroupAvailable = true;
							memberGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							memberGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_MEMBER);
							memberGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}
						else if((dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(userGroupDetail.getDisplayName())) {
							isViewerGroupAvailable = true;
							viewerGroupVO.setState(ConstantsUtility.ASSIGNED_STATE);
							viewerGroupVO.setGroupName(dnaGroupPrefix+workspaceId+ "_"+ ConstantsUtility.PERMISSION_VIEWER);
							viewerGroupVO.setGroupId(userGroupDetail.getIdentifier());
						}else {
							fabricWorkspaceClient.removeUserGroup(workspaceId, userGroupDetail.getDisplayName());
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
			adminGroupVO = this.callGroupAssign(adminGroupVO, workspaceId, ConstantsUtility.PERMISSION_ADMIN);
			updatedGroups.add(adminGroupVO);
		}
		if(isContributorGroupAvailable) {
			updatedGroups.add(contributorGroupVO);
		}else {
			contributorGroupVO = this.callGroupAssign(contributorGroupVO, workspaceId, ConstantsUtility.PERMISSION_CONTRIBUTOR);
			updatedGroups.add(contributorGroupVO);
		}
		if(isMemberGroupAvailable) {
			updatedGroups.add(memberGroupVO);
		}else {
			memberGroupVO = this.callGroupAssign(memberGroupVO, workspaceId, ConstantsUtility.PERMISSION_MEMBER);
			updatedGroups.add(memberGroupVO);
		}
		if(isViewerGroupAvailable) {
			updatedGroups.add(viewerGroupVO);
		}else {
			viewerGroupVO = this.callGroupAssign(viewerGroupVO, workspaceId, ConstantsUtility.PERMISSION_VIEWER);
			updatedGroups.add(viewerGroupVO);
		}
		return updatedGroups;
	}
	
	@Override
	@Transactional
	public GenericMessage delete(String id) {
		FabricWorkspaceVO existingWorkspace = this.getById(id);
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			ErrorResponseDto deleteResponse = fabricWorkspaceClient.deleteWorkspace(id);
			if(deleteResponse!=null && deleteResponse.getMessage() != null) {
					MessageDescription message = new MessageDescription();
					message.setMessage(deleteResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred:{} while deleting fabric workspace project {} ", id);
					return responseMessage;
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

}
