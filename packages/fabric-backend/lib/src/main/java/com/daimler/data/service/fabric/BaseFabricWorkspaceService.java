package com.daimler.data.service.fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.ReviewerConfigDto;
import com.daimler.data.dto.fabric.WorkflowDefinitionDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspaceUpdateDto;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.EntitlementDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RoleDetailsVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;

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
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
	public BaseFabricWorkspaceService() {
		super();
	}

	@Override
	@Transactional
	public List<FabricWorkspaceVO> getAll( int limit,  int offset, String user) {
		List<FabricWorkspaceVO> vos = new ArrayList<>();
		List<FabricWorkspaceNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty()) {
			for(FabricWorkspaceNsql entity : entities) {
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
		return vos;
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
					
					
					GenericMessage addGroupResponse = fabricWorkspaceClient.addGroup(createResponse.getId());
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
					
					
					data.setStatus(status);
					FabricWorkspaceVO savedRecord = super.create(data);
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
	public CreateEntitlementRequestDto prepareEntitlementCreateRequestDto(String workspaceName, String permissionName) {
		CreateEntitlementRequestDto entitlementRequestDto = new CreateEntitlementRequestDto();
		entitlementRequestDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
		entitlementRequestDto.setEntitlementId(workspaceName + "_" + permissionName);
		entitlementRequestDto.setDisplayName(workspaceName + "_" + permissionName);
		entitlementRequestDto.setDescription("Entitlement for workspace " + workspaceName + " for " + permissionName + " Privileges");
		entitlementRequestDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
		entitlementRequestDto.setDataClassificationInherited(false);
		entitlementRequestDto.setConnectivity(false);
		return entitlementRequestDto;
	}
	
	@Override
	public EntitlementDetailsVO callEntitlementCreate(String workspaceName, String permissionName) {
		CreateEntitlementRequestDto createRequestDto = this.prepareEntitlementCreateRequestDto(workspaceName, permissionName);
		EntitlementDetailsVO requestedEntitlement = new EntitlementDetailsVO();
		requestedEntitlement.setDisplayName(workspaceName + "_" +  permissionName);
		requestedEntitlement.setState(ConstantsUtility.PENDING_STATE);
		try {
			log.info("Calling identity management system to add entitlement {} for workspace {} ", workspaceName + "_" +  permissionName, workspaceName);
			EntiltlemetDetailsDto entitlementCreateResponse = identityClient.createEntitlement(createRequestDto);
			if(entitlementCreateResponse!=null && entitlementCreateResponse.getEntitlementId()!=null) {
				requestedEntitlement.setEntitlementId(entitlementCreateResponse.getEntitlementId());
				requestedEntitlement.setState(ConstantsUtility.CREATED_STATE);
				log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement created successfully with id {} ", workspaceName + "_" +  permissionName, workspaceName, entitlementCreateResponse.getEntitlementId());
			}else {
				requestedEntitlement.setState(ConstantsUtility.FAILED_STATE);
				log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement creat failed with unknown error", workspaceName + "_" + permissionName, workspaceName);
			}
		}catch(Exception e) {
			requestedEntitlement.setState(ConstantsUtility.FAILED_STATE);
			log.error("Called identity management system to add entitlement {} for workspace {} . Failed to create entitlement with error {} ", workspaceName + "_" +  permissionName, workspaceName, e.getMessage());
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
				createRoleVO.setLink(identityRoleUrl+workspaceName + "_" +  ConstantsUtility.PERMISSION_ADMIN);
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
	
	public FabricWorkspaceStatusVO processWorkspaceUserManagement(FabricWorkspaceStatusVO currentStatus, String workspaceName) {
				if(currentStatus==null || ConstantsUtility.INPROGRESS_STATE.equalsIgnoreCase(currentStatus.getState())) {
					
					boolean isAdminEntitlementAvailable = false;
					boolean isContributorEntitlementAvailable = false;
					boolean isMemberEntitlementAvailable = false;
					boolean isViewerEntitlementAvailable = false;
					EntitlementDetailsVO adminEntitlement = new EntitlementDetailsVO();
					EntitlementDetailsVO contributorEntitlement = new EntitlementDetailsVO();
					EntitlementDetailsVO memberEntitlement = new EntitlementDetailsVO();
					EntitlementDetailsVO viewerEntitlement = new EntitlementDetailsVO();
					
					boolean isAdminRoleAvailable = false;
					boolean isContributorRoleAvailable = false;
					boolean isMemberRoleAvailable = false;
					boolean isViewerRoleAvailable = false;
					RoleDetailsVO adminRole = new RoleDetailsVO();
					RoleDetailsVO contributorRole = new RoleDetailsVO();
					RoleDetailsVO memberRole = new RoleDetailsVO();
					RoleDetailsVO viewerRole = new RoleDetailsVO();
					
					List<EntitlementDetailsVO> entitlements  = currentStatus.getEntitlements();
					List<EntitlementDetailsVO> updatedEntitlements  = new ArrayList<>();
					if(entitlements!=null && !entitlements.isEmpty()) {
						//check for admin entitlement
						Optional<EntitlementDetailsVO> existingAdminEntitlement = entitlements.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_ADMIN).equalsIgnoreCase(n.getDisplayName())).findFirst();
						if(existingAdminEntitlement.isPresent()) {
							EntitlementDetailsVO existingAdminEntitlementVO = existingAdminEntitlement.get();
							if(existingAdminEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingAdminEntitlementVO.getState())){
									EntitlementDetailsVO latestAdminEntitlement = this.callEntitlementCreate(workspaceName,ConstantsUtility.PERMISSION_ADMIN);
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
						}
						//check for contributor entitlement
						Optional<EntitlementDetailsVO> existingContributorEntitlement = entitlements.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_CONTRIBUTOR).equalsIgnoreCase(n.getDisplayName())).findFirst();
						if(existingContributorEntitlement.isPresent()) {
							EntitlementDetailsVO existingContributorEntitlementVO = existingContributorEntitlement.get();
							if(existingContributorEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingContributorEntitlementVO.getState())){
									EntitlementDetailsVO latestContributorEntitlement = this.callEntitlementCreate(workspaceName,ConstantsUtility.PERMISSION_CONTRIBUTOR);
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
						}
						//check for Member entitlement
						Optional<EntitlementDetailsVO> existingMemberEntitlement = entitlements.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_MEMBER).equalsIgnoreCase(n.getDisplayName())).findFirst();
						if(existingMemberEntitlement.isPresent()) {
							EntitlementDetailsVO existingMemberEntitlementVO = existingMemberEntitlement.get();
							if(existingMemberEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingMemberEntitlementVO.getState())){
									EntitlementDetailsVO latestMemberEntitlement = this.callEntitlementCreate(workspaceName,ConstantsUtility.PERMISSION_MEMBER);
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
						}
						//check for viewer entitlement
						Optional<EntitlementDetailsVO> existingViewerEntitlement = entitlements.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(n.getDisplayName())).findFirst();
						if(existingAdminEntitlement.isPresent()) {
							EntitlementDetailsVO existingViewerEntitlementVO = existingViewerEntitlement.get();
							if(existingViewerEntitlementVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingViewerEntitlementVO.getState())){
									EntitlementDetailsVO latestViewerEntitlement = this.callEntitlementCreate(workspaceName,ConstantsUtility.PERMISSION_VIEWER);
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
						}
					}
					currentStatus.setEntitlements(updatedEntitlements);
					
					List<RoleDetailsVO> roles  = currentStatus.getRoles();
					List<RoleDetailsVO> updatedRoles  = new ArrayList<>();
					if(roles!=null && !roles.isEmpty()) {
						//check for admin Role
						Optional<RoleDetailsVO> existingAdminRole = roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_ADMIN).equalsIgnoreCase(n.getName())).findFirst();
						if(existingAdminRole.isPresent()) {
							RoleDetailsVO existingAdminRoleVO = existingAdminRole.get();
							if(existingAdminRoleVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingAdminRoleVO.getState())){
									RoleDetailsVO latestAdminRole = this.callRoleCreate(workspaceName,ConstantsUtility.PERMISSION_ADMIN);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestAdminRole.getState())) {
										isAdminRoleAvailable = true;
									}
									updatedRoles.add(latestAdminRole);
									adminRole = latestAdminRole;
								}else {
									isAdminRoleAvailable = true;
									updatedRoles.add(existingAdminRoleVO);
									adminRole = existingAdminRoleVO;
								}
							}
						}
						//check for contributor Role
						Optional<RoleDetailsVO> existingContributorRole = roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_CONTRIBUTOR).equalsIgnoreCase(n.getName())).findFirst();
						if(existingContributorRole.isPresent()) {
							RoleDetailsVO existingContributorRoleVO = existingContributorRole.get();
							if(existingContributorRoleVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingContributorRoleVO.getState())){
									RoleDetailsVO latestContributorRole = this.callRoleCreate(workspaceName,ConstantsUtility.PERMISSION_CONTRIBUTOR);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestContributorRole.getState())) {
										isContributorRoleAvailable = true;
									}
									updatedRoles.add(latestContributorRole);
									contributorRole = latestContributorRole;
								}else {
									isContributorRoleAvailable = true;
									updatedRoles.add(existingContributorRoleVO);
									contributorRole = existingContributorRoleVO;
								}
							}
						}
						//check for Member Role
						Optional<RoleDetailsVO> existingMemberRole = roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_MEMBER).equalsIgnoreCase(n.getName())).findFirst();
						if(existingMemberRole.isPresent()) {
							RoleDetailsVO existingMemberRoleVO = existingMemberRole.get();
							if(existingMemberRoleVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingMemberRoleVO.getState())){
									RoleDetailsVO latestMemberRole = this.callRoleCreate(workspaceName,ConstantsUtility.PERMISSION_MEMBER);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestMemberRole.getState())) {
										isMemberRoleAvailable = true;
									}
									updatedRoles.add(latestMemberRole);
									memberRole = latestMemberRole;
								}else {
									isMemberRoleAvailable = true;
									updatedRoles.add(existingMemberRoleVO);
									memberRole = existingMemberRoleVO;
								}
							}
						}
						//check for viewer Role
						Optional<RoleDetailsVO> existingViewerRole = roles.stream().filter(n->(workspaceName + "_" + ConstantsUtility.PERMISSION_VIEWER).equalsIgnoreCase(n.getName())).findFirst();
						if(existingAdminRole.isPresent()) {
							RoleDetailsVO existingViewerRoleVO = existingViewerRole.get();
							if(existingViewerRoleVO.getState()!=null) {
								if(!ConstantsUtility.CREATED_STATE.equalsIgnoreCase(existingViewerRoleVO.getState())){
									RoleDetailsVO latestViewerRole = this.callRoleCreate(workspaceName,ConstantsUtility.PERMISSION_VIEWER);
									if(ConstantsUtility.CREATED_STATE.equalsIgnoreCase(latestViewerRole.getState())) {
										isViewerRoleAvailable = true;
									}
									updatedRoles.add(latestViewerRole);
									viewerRole = latestViewerRole;
								}else {
									isViewerRoleAvailable = true;
									updatedRoles.add(existingViewerRoleVO);
									viewerRole = existingViewerRoleVO;
								}
							}
						}
					}
					currentStatus.setRoles(updatedRoles);
					
					currentStatus.setRoles(roles);
					
					//adding group details
					List<GroupDetailsVO> microsoftGroups = new ArrayList<>();
					
					GroupDetailsVO adminGroup = new GroupDetailsVO();
					adminGroup.setGroupId("");
					adminGroup.setGroupName(dnaGroupPrefix+"ID_"+ConstantsUtility.PERMISSION_ADMIN);
					adminGroup.setState(ConstantsUtility.PENDING_STATE);
					microsoftGroups.add(adminGroup);
					
					GroupDetailsVO contributorGroup = new GroupDetailsVO();
					contributorGroup.setGroupId("");
					contributorGroup.setGroupName(dnaGroupPrefix+"ID_"+ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorGroup.setState(ConstantsUtility.PENDING_STATE);
					microsoftGroups.add(contributorGroup);
					
					GroupDetailsVO viewerGroup = new GroupDetailsVO();
					viewerGroup.setGroupId("");
					viewerGroup.setGroupName(dnaGroupPrefix+"ID_"+ConstantsUtility.PERMISSION_VIEWER);
					viewerGroup.setState(ConstantsUtility.PENDING_STATE);
					microsoftGroups.add(viewerGroup);
					
					GroupDetailsVO memberGroup = new GroupDetailsVO();
					memberGroup.setGroupId("");
					memberGroup.setGroupName(dnaGroupPrefix+"ID_"+ConstantsUtility.PERMISSION_MEMBER);
					memberGroup.setState(ConstantsUtility.PENDING_STATE);
					microsoftGroups.add(memberGroup);
					
					currentStatus.setMicrosoftGroups(microsoftGroups);
				}
//				
//				List<RoleDetailsVO> roles = new ArrayList<>();
//				EntitlementDetailsVO dnaFabricEntitlementVO = new EntitlementDetailsVO();
//				dnaFabricEntitlementVO.setDisplayName(dnaFabricEntitlementName);
//				dnaFabricEntitlementVO.setEntitlementId(dnaFabricEntitlementId);
//				dnaFabricEntitlementVO.setState(ConstantsUtility.CREATED_STATE);
//				
//				
//				//create roles
//				List<EntitlementDetailsVO> adminRoleEntitlements = new ArrayList<>();
//				adminRoleEntitlements.add(dnaFabricEntitlementVO);
//				adminRoleEntitlements.add(adminEntitlement);
				
				
				
//			}
//	
//				
//				List<EntitlementDetailsVO> memberRoleEntitlements = new ArrayList<>();
//				memberRoleEntitlements.add(dnaFabricEntitlementVO);
//				memberRoleEntitlements.add(memberEntitlement);
//				CreateRoleRequestDto memberRoleRequestDto = new CreateRoleRequestDto();
//				memberRoleRequestDto.setAccessReview(accessReview);
//				memberRoleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
//				memberRoleRequestDto.setDataClassification("CONFIDENTIAL");
//				memberRoleRequestDto.setDefaultValidityType("OPTIONAL");
//				memberRoleRequestDto.setDeprovisioning(false);
//				memberRoleRequestDto.setDescription("Member role for workspace " + vo.getName());
//				memberRoleRequestDto.setDynamic(false);
//				memberRoleRequestDto.setGlobalCentralAvailable(true);
//				memberRoleRequestDto.setId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
//				memberRoleRequestDto.setJobTitle(false);
//				memberRoleRequestDto.setMarketAvailabilities(new ArrayList<>());
//				memberRoleRequestDto.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
//				memberRoleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
//				memberRoleRequestDto.setNeedsCustomScopes(false);
//				memberRoleRequestDto.setNeedsOrgScopes(false);
//				memberRoleRequestDto.setNotificationsActive(true);
//				memberRoleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
//				memberRoleRequestDto.setRoleType("BUSINESS");
//				memberRoleRequestDto.setSelfRequestable(true);
//				memberRoleRequestDto.setWorkflowBased(true);
//				memberRoleRequestDto.setWorkflowDefinition(workflow);
//				RoleDetailsVO memberRoleVO = new RoleDetailsVO();
//				memberRoleVO.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
//				memberRoleVO.setState(ConstantsUtility.PENDING_STATE);
//				try {
//					log.info("Calling identity management system to add role {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName());
//					CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(memberRoleRequestDto);
//					if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
//						memberRoleVO.setId(createRoleResponseDto.getId());
//						memberRoleVO.setLink(identityRoleUrl+vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
//						memberRoleVO.setState(ConstantsUtility.CREATED_STATE);
//						memberRoleVO.setEntitlements(viewerRoleEntitlements);
//						memberRoleVO.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
//						log.info("Called identity management system to add role {} for workspace {} . Role created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName(), createRoleResponseDto.getId());
//					}else {
//						memberRoleVO.setState(ConstantsUtility.FAILED_STATE);
//						log.info("Called identity management system to add role {} for workspace {} . Role creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName());
//					}
//				}catch(Exception e) {
//					memberRoleVO.setState(ConstantsUtility.FAILED_STATE);
//					log.error("Called identity management system to add role {} for workspace {} . Failed to create role with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName(), e.getMessage());
//				}
//				roles.add(memberRoleVO);
	
				
	}
	
	@Override
	@Transactional
	public GenericMessage delete(String id) {
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
