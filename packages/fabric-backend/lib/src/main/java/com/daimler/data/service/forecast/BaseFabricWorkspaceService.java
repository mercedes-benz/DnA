package com.daimler.data.service.forecast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
					
					FabricWorkspaceStatusVO status = new FabricWorkspaceStatusVO();
					status.setState(ConstantsUtility.INPROGRESS_STATE);
					
					List<EntitlementDetailsVO> entitlements = new ArrayList<>();
					//create entitlements
					CreateEntitlementRequestDto adminEntitlementDto = new CreateEntitlementRequestDto();
					adminEntitlementDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
					adminEntitlementDto.setEntitlementId(vo.getName() + "_" + ConstantsUtility.PERMISSION_ADMIN);
					adminEntitlementDto.setDisplayName(vo.getName() + "_" + ConstantsUtility.PERMISSION_ADMIN);
					adminEntitlementDto.setDescription("Entitlement for workspace " + vo.getName() + " for Admin Privileges");
					adminEntitlementDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
					adminEntitlementDto.setDataClassificationInherited(false);
					adminEntitlementDto.setConnectivity(false);
					EntitlementDetailsVO adminEntitlement = new EntitlementDetailsVO();
					adminEntitlement.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN);
					adminEntitlement.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add entitlement {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName());
						EntiltlemetDetailsDto adminEntitlementCreateResponse = identityClient.createEntitlement(adminEntitlementDto);
						if(adminEntitlementCreateResponse!=null && adminEntitlementCreateResponse.getEntitlementId()!=null) {
							adminEntitlement.setEntitlementId(adminEntitlementCreateResponse.getEntitlementId());
							adminEntitlement.setState(ConstantsUtility.CREATED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName(), adminEntitlementCreateResponse.getEntitlementId());
						}else {
							adminEntitlement.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName());
						}
					}catch(Exception e) {
						adminEntitlement.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add entitlement {} for workspace {} . Failed to create entitlement with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName(), e.getMessage());
					}
					entitlements.add(adminEntitlement);
					
					CreateEntitlementRequestDto contributorEntitlementDto = new CreateEntitlementRequestDto();
					contributorEntitlementDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
					contributorEntitlementDto.setEntitlementId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorEntitlementDto.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorEntitlementDto.setDescription("Entitlement for workspace " + vo.getName() + " for Contributor Privileges");
					contributorEntitlementDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
					contributorEntitlementDto.setDataClassificationInherited(false);
					contributorEntitlementDto.setConnectivity(false);
					EntitlementDetailsVO contributorEntitlement = new EntitlementDetailsVO();
					contributorEntitlement.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorEntitlement.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add entitlement {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName());
						EntiltlemetDetailsDto contributorEntitlementCreateResponse = identityClient.createEntitlement(contributorEntitlementDto);
						if(contributorEntitlementCreateResponse!=null && contributorEntitlementCreateResponse.getEntitlementId()!=null) {
							contributorEntitlement.setEntitlementId(contributorEntitlementCreateResponse.getEntitlementId());
							contributorEntitlement.setState(ConstantsUtility.CREATED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName(), contributorEntitlementCreateResponse.getEntitlementId());
						}else {
							contributorEntitlement.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName());
						}
					}catch(Exception e) {
						contributorEntitlement.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add entitlement {} for workspace {} . Failed to create entitlement with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName(), e.getMessage());
					}
					entitlements.add(contributorEntitlement);
					
					CreateEntitlementRequestDto memberEntitlementDto = new CreateEntitlementRequestDto();
					memberEntitlementDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
					memberEntitlementDto.setEntitlementId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
					memberEntitlementDto.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
					memberEntitlementDto.setDescription("Entitlement for workspace " + vo.getName() + " for Member Privileges");
					memberEntitlementDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
					memberEntitlementDto.setDataClassificationInherited(false);
					memberEntitlementDto.setConnectivity(false);
					EntitlementDetailsVO memberEntitlement = new EntitlementDetailsVO();
					memberEntitlement.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
					memberEntitlement.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add entitlement {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName());
						EntiltlemetDetailsDto memberEntitlementCreateResponse = identityClient.createEntitlement(memberEntitlementDto);
						if(memberEntitlementCreateResponse!=null && memberEntitlementCreateResponse.getEntitlementId()!=null) {
							memberEntitlement.setEntitlementId(memberEntitlementCreateResponse.getEntitlementId());
							memberEntitlement.setState(ConstantsUtility.CREATED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName(), memberEntitlementCreateResponse.getEntitlementId());
						}else {
							memberEntitlement.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName());
						}
					}catch(Exception e) {
						memberEntitlement.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add entitlement {} for workspace {} . Failed to create entitlement with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName(), e.getMessage());
					}
					entitlements.add(memberEntitlement);
					
					CreateEntitlementRequestDto viewerEntitlementDto = new CreateEntitlementRequestDto();
					viewerEntitlementDto.setType(ConstantsUtility.ENTITLEMENT_TYPE);
					viewerEntitlementDto.setEntitlementId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
					viewerEntitlementDto.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
					viewerEntitlementDto.setDescription("Entitlement for workspace " + vo.getName() + " for Viewer Privileges");
					viewerEntitlementDto.setDataClassification(ConstantsUtility.DATACLASSIFICATION_CONFIDENTIAL);
					viewerEntitlementDto.setDataClassificationInherited(false);
					viewerEntitlementDto.setConnectivity(false);
					EntitlementDetailsVO viewerEntitlement = new EntitlementDetailsVO();
					viewerEntitlement.setDisplayName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
					viewerEntitlement.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add entitlement {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName());
						EntiltlemetDetailsDto viewerEntitlementCreateResponse = identityClient.createEntitlement(viewerEntitlementDto);
						if(viewerEntitlementCreateResponse!=null && viewerEntitlementCreateResponse.getEntitlementId()!=null) {
							viewerEntitlement.setEntitlementId(viewerEntitlementCreateResponse.getEntitlementId());
							viewerEntitlement.setState(ConstantsUtility.CREATED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName(), viewerEntitlementCreateResponse.getEntitlementId());
						}else {
							viewerEntitlement.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add entitlement {} for workspace {} . Entitlement creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName());
						}
					}catch(Exception e) {
						viewerEntitlement.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add entitlement {} for workspace {} . Failed to create entitlement with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName(), e.getMessage());
					}
					entitlements.add(viewerEntitlement);
					status.setEntitlements(entitlements);
					
					//create roles
					List<RoleDetailsVO> roles = new ArrayList<>();
					EntitlementDetailsVO dnaFabricEntitlementVO = new EntitlementDetailsVO();
					dnaFabricEntitlementVO.setDisplayName(dnaFabricEntitlementName);
					dnaFabricEntitlementVO.setEntitlementId(dnaFabricEntitlementId);
					dnaFabricEntitlementVO.setState(ConstantsUtility.CREATED_STATE);
					
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
					List<EntitlementDetailsVO> adminRoleEntitlements = new ArrayList<>();
					adminRoleEntitlements.add(dnaFabricEntitlementVO);
					adminRoleEntitlements.add(adminEntitlement);
					CreateRoleRequestDto adminRoleRequestDto = new CreateRoleRequestDto();
					adminRoleRequestDto.setAccessReview(accessReview);
					
					adminRoleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
					adminRoleRequestDto.setDataClassification("CONFIDENTIAL");
					adminRoleRequestDto.setDefaultValidityType("OPTIONAL");
					adminRoleRequestDto.setDeprovisioning(false);
					adminRoleRequestDto.setDescription("Admin role for workspace " + vo.getName());
//					adminRoleRequestDto.setDynamic(false);
					adminRoleRequestDto.setGlobalCentralAvailable(true);
					adminRoleRequestDto.setId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN);
					adminRoleRequestDto.setJobTitle(false);
					adminRoleRequestDto.setMarketAvailabilities(new ArrayList<>());
					adminRoleRequestDto.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN);
					adminRoleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
					adminRoleRequestDto.setNeedsCustomScopes(false);
					adminRoleRequestDto.setNeedsOrgScopes(false);
					adminRoleRequestDto.setNotificationsActive(true);
					adminRoleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
					adminRoleRequestDto.setRoleType("BUSINESS");
					adminRoleRequestDto.setSelfRequestable(true);
					adminRoleRequestDto.setWorkflowBased(true);
					adminRoleRequestDto.setWorkflowDefinition(workflow);
					RoleDetailsVO adminRoleVO = new RoleDetailsVO();
					adminRoleVO.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN);
					adminRoleVO.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add role {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName());
						CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(adminRoleRequestDto);
						if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
							adminRoleVO.setId(createRoleResponseDto.getId());
							adminRoleVO.setLink(identityRoleUrl+vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN);
							adminRoleVO.setState(ConstantsUtility.CREATED_STATE);
							adminRoleVO.setEntitlements(adminRoleEntitlements);
							adminRoleVO.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName(), createRoleResponseDto.getId());
						}else {
							adminRoleVO.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName());
						}
					}catch(Exception e) {
						adminRoleVO.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add role {} for workspace {} . Failed to create role with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_ADMIN, vo.getName(), e.getMessage());
					}
					roles.add(adminRoleVO);
					
					List<EntitlementDetailsVO> contributorRoleEntitlements = new ArrayList<>();
					contributorRoleEntitlements.add(dnaFabricEntitlementVO);
					contributorRoleEntitlements.add(contributorEntitlement);
					CreateRoleRequestDto contributorRoleRequestDto = new CreateRoleRequestDto();
					contributorRoleRequestDto.setAccessReview(accessReview);
					contributorRoleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
					contributorRoleRequestDto.setDataClassification("CONFIDENTIAL");
					contributorRoleRequestDto.setDefaultValidityType("OPTIONAL");
					contributorRoleRequestDto.setDeprovisioning(false);
					contributorRoleRequestDto.setDescription("Contributor role for workspace " + vo.getName());
//					contributorRoleRequestDto.setDynamic(false);
					contributorRoleRequestDto.setGlobalCentralAvailable(true);
					contributorRoleRequestDto.setId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorRoleRequestDto.setJobTitle(false);
					contributorRoleRequestDto.setMarketAvailabilities(new ArrayList<>());
					contributorRoleRequestDto.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorRoleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
					contributorRoleRequestDto.setNeedsCustomScopes(false);
					contributorRoleRequestDto.setNeedsOrgScopes(false);
					contributorRoleRequestDto.setNotificationsActive(true);
					contributorRoleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
					contributorRoleRequestDto.setRoleType("BUSINESS");
					contributorRoleRequestDto.setSelfRequestable(true);
					contributorRoleRequestDto.setWorkflowBased(true);
					contributorRoleRequestDto.setWorkflowDefinition(workflow);
					RoleDetailsVO contributorRoleVO = new RoleDetailsVO();
					contributorRoleVO.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
					contributorRoleVO.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add role {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName());
						CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(contributorRoleRequestDto);
						if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
							contributorRoleVO.setId(createRoleResponseDto.getId());
							contributorRoleVO.setLink(identityRoleUrl+vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR);
							contributorRoleVO.setState(ConstantsUtility.CREATED_STATE);
							contributorRoleVO.setEntitlements(contributorRoleEntitlements);
							contributorRoleVO.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName(), createRoleResponseDto.getId());
						}else {
							contributorRoleVO.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName());
						}
					}catch(Exception e) {
						contributorRoleVO.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add role {} for workspace {} . Failed to create role with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_CONTRIBUTOR, vo.getName(), e.getMessage());
					}
					roles.add(contributorRoleVO);
					
					List<EntitlementDetailsVO> viewerRoleEntitlements = new ArrayList<>();
					viewerRoleEntitlements.add(dnaFabricEntitlementVO);
					viewerRoleEntitlements.add(viewerEntitlement);
					CreateRoleRequestDto viewerRoleRequestDto = new CreateRoleRequestDto();
					viewerRoleRequestDto.setAccessReview(accessReview);
					viewerRoleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
					viewerRoleRequestDto.setDataClassification("CONFIDENTIAL");
					viewerRoleRequestDto.setDefaultValidityType("OPTIONAL");
					viewerRoleRequestDto.setDeprovisioning(false);
					viewerRoleRequestDto.setDescription("Viewer role for workspace " + vo.getName());
//					viewerRoleRequestDto.setDynamic(false);
					viewerRoleRequestDto.setGlobalCentralAvailable(true);
					viewerRoleRequestDto.setId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
					viewerRoleRequestDto.setJobTitle(false);
					viewerRoleRequestDto.setMarketAvailabilities(new ArrayList<>());
					viewerRoleRequestDto.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
					viewerRoleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
					viewerRoleRequestDto.setNeedsCustomScopes(false);
					viewerRoleRequestDto.setNeedsOrgScopes(false);
					viewerRoleRequestDto.setNotificationsActive(true);
					viewerRoleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
					viewerRoleRequestDto.setRoleType("BUSINESS");
					viewerRoleRequestDto.setSelfRequestable(true);
					viewerRoleRequestDto.setWorkflowBased(true);
					viewerRoleRequestDto.setWorkflowDefinition(workflow);
					RoleDetailsVO viewerRoleVO = new RoleDetailsVO();
					viewerRoleVO.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
					viewerRoleVO.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add role {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName());
						CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(viewerRoleRequestDto);
						if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
							viewerRoleVO.setId(createRoleResponseDto.getId());
							viewerRoleVO.setLink(identityRoleUrl+vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER);
							viewerRoleVO.setState(ConstantsUtility.CREATED_STATE);
							viewerRoleVO.setEntitlements(viewerRoleEntitlements);
							viewerRoleVO.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName(), createRoleResponseDto.getId());
						}else {
							viewerRoleVO.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName());
						}
					}catch(Exception e) {
						viewerRoleVO.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add role {} for workspace {} . Failed to create role with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_VIEWER, vo.getName(), e.getMessage());
					}
					roles.add(viewerRoleVO);
					
					List<EntitlementDetailsVO> memberRoleEntitlements = new ArrayList<>();
					memberRoleEntitlements.add(dnaFabricEntitlementVO);
					memberRoleEntitlements.add(memberEntitlement);
					CreateRoleRequestDto memberRoleRequestDto = new CreateRoleRequestDto();
					memberRoleRequestDto.setAccessReview(accessReview);
					memberRoleRequestDto.setCommunityAvailability(Arrays.asList(communityAvailabilitySplits));
					memberRoleRequestDto.setDataClassification("CONFIDENTIAL");
					memberRoleRequestDto.setDefaultValidityType("OPTIONAL");
					memberRoleRequestDto.setDeprovisioning(false);
					memberRoleRequestDto.setDescription("Member role for workspace " + vo.getName());
//					memberRoleRequestDto.setDynamic(false);
					memberRoleRequestDto.setGlobalCentralAvailable(true);
					memberRoleRequestDto.setId(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
					memberRoleRequestDto.setJobTitle(false);
					memberRoleRequestDto.setMarketAvailabilities(new ArrayList<>());
					memberRoleRequestDto.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
					memberRoleRequestDto.setNeedsAdditionalSelfRequestApproval(false);
					memberRoleRequestDto.setNeedsCustomScopes(false);
					memberRoleRequestDto.setNeedsOrgScopes(false);
					memberRoleRequestDto.setNotificationsActive(true);
					memberRoleRequestDto.setOrganizationAvailabilities(new ArrayList<>());
					memberRoleRequestDto.setRoleType("BUSINESS");
					memberRoleRequestDto.setSelfRequestable(true);
					memberRoleRequestDto.setWorkflowBased(true);
					memberRoleRequestDto.setWorkflowDefinition(workflow);
					RoleDetailsVO memberRoleVO = new RoleDetailsVO();
					memberRoleVO.setName(vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
					memberRoleVO.setState(ConstantsUtility.PENDING_STATE);
					try {
						log.info("Calling identity management system to add role {} for workspace {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName());
						CreateRoleResponseDto createRoleResponseDto = identityClient.createRole(memberRoleRequestDto);
						if(createRoleResponseDto!=null && createRoleResponseDto.getId()!=null) {
							memberRoleVO.setId(createRoleResponseDto.getId());
							memberRoleVO.setLink(identityRoleUrl+vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER);
							memberRoleVO.setState(ConstantsUtility.CREATED_STATE);
							memberRoleVO.setEntitlements(viewerRoleEntitlements);
							memberRoleVO.setAssignEntitlementsState(ConstantsUtility.ASSIGNED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role created successfully with id {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName(), createRoleResponseDto.getId());
						}else {
							memberRoleVO.setState(ConstantsUtility.FAILED_STATE);
							log.info("Called identity management system to add role {} for workspace {} . Role creat failed with unknown error", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName());
						}
					}catch(Exception e) {
						memberRoleVO.setState(ConstantsUtility.FAILED_STATE);
						log.error("Called identity management system to add role {} for workspace {} . Failed to create role with error {} ", vo.getName() + "_" +  ConstantsUtility.PERMISSION_MEMBER, vo.getName(), e.getMessage());
					}
					roles.add(memberRoleVO);
					status.setRoles(roles);
					
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
					
					status.setMicrosoftGroups(microsoftGroups);
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
