package com.daimler.data.service.fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.AuthoriserClient;
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.dto.fabric.UserRoleRequestDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspacesCollectionDto;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@ConditionalOnProperty(value="fabricWorkspaces.startup.workspaceprovisioning", havingValue = "true", matchIfMissing = false)
@Component
@Slf4j
public class WorkspaceBackgroundJobsService {

	@Autowired
	private FabricWorkspaceClient fabricWorkspaceClient;
	
	@Autowired
	private FabricWorkspaceService fabricService;

	@Autowired
	private AuthoriserClient identityClient;
	
	@Value("${authoriser.role.fabricRoleName}")
	private String fabricOperationsRoleName;
	
	@Value("${fabricWorkspaces.startup.onboardOwnersToFabricOperationsRole}")
	private String enableOwnersOnboardingToFabricRoleOnStartup;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
	
	@PostConstruct
	public void initForEnablingExistingOwnersToFabricRole() {
		if(enableOwnersOnboardingToFabricRoleOnStartup!=null && enableOwnersOnboardingToFabricRoleOnStartup.equalsIgnoreCase("true")) {
			FabricWorkspacesCollectionVO collection = fabricService.getAllLov(0,0);
			List<FabricWorkspaceVO> workspaceVOs = collection!=null ? collection.getRecords() : new ArrayList<>();
			log.info("Fetched all fabric workspaces from service successfully during scheduled job");
			if(workspaceVOs!=null && !workspaceVOs.isEmpty()) {
				log.info("During scheduled job, fetch success. Workspaces available, proceeding with processing user management for each");
				for(FabricWorkspaceVO workspaceVO: workspaceVOs) {
					try {
						String ownerId = workspaceVO.getCreatedBy().getId();
						Date validFromDate = new Date();//workspaceVO.getCreatedOn();
						String validFrom = sdf.format(validFromDate);
						Calendar calendar = Calendar.getInstance();
				        calendar.setTime(validFromDate);
				        calendar.add(Calendar.YEAR, 1);
				        Date validToDate = calendar.getTime();
						String validTo = sdf.format(validToDate);
						UserRoleRequestDto roleRequestDto = new UserRoleRequestDto();
						roleRequestDto.setReason("Onboarding owner to role to enable fabric operations.");
						roleRequestDto.setValidTo(validTo);
						roleRequestDto.setValidFrom(validFrom);
						HttpStatus status = identityClient.RequestRoleForUser(roleRequestDto, ownerId, fabricOperationsRoleName);
						if(status.is2xxSuccessful()){
				            log.info("Successfully onboarded owner {} of workspace {} : {} to role {} for enabling fabric operations", ownerId, workspaceVO.getId(), workspaceVO.getName(), fabricOperationsRoleName);
				        }else {
				        	log.error("Failed to onboarded owner {} of workspace {} : {} to role {} for enabling fabric operations", ownerId, workspaceVO.getId(), workspaceVO.getName(), fabricOperationsRoleName);
				        }
					}catch(Exception e) {
						log.error("Failed to onboard owner of workspace {} : {} to role {} ",workspaceVO.getId(),workspaceVO.getName(),fabricOperationsRoleName);
					}
				}
			}
		}
	}
	
//	@PostConstruct
//	public void init() {
//		List<FabricWorkspaceVO> workspaceVOs = fabricService.getAll();
//		log.info("Fetched all fabric workspaces from service successfully during init job");
//		if(workspaceVOs!=null && !workspaceVOs.isEmpty()) {
//			log.info("During init job, fetch success. Workspaces available, proceeding with processing user management bug fixing for each");
//			for(FabricWorkspaceVO workspaceVO: workspaceVOs) {
//				if(workspaceVO!=null && workspaceVO.getStatus()!=null && (ConstantsUtility.INPROGRESS_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState()) || ConstantsUtility.COMPLETED_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState()))){
//					FabricWorkspaceStatusVO currentStatus = workspaceVO.getStatus();
//					FabricWorkspaceStatusVO updatedStatus = new FabricWorkspaceStatusVO();
//					FabricWorkspaceVO tempWorkspaceVO =  workspaceVO;
//					try {
//						updatedStatus = fabricService.fixBugsInWorkspaceUserManagement(currentStatus, workspaceVO.getName(), workspaceVO.getCreatedBy().getId(), workspaceVO.getId());
//						tempWorkspaceVO.setStatus(updatedStatus);
//						try {
//							fabricService.create(tempWorkspaceVO);
//						}catch(Exception saveException) {
//							log.error("During scheduled job, failed to update the workspace with latest status {} for workspace {} and id {} with exception {}",
//										updatedStatus.getState(), workspaceVO.getName(), workspaceVO.getId(), saveException.getMessage());
//						}
//					}catch(Exception e) {
//						log.error("During scheduled job, failed to process workspace user management for workspace {} and id {} with exception {}", workspaceVO.getName(), workspaceVO.getId(), e.getMessage());
//					}
//				}
//			}
//		}
//	}
	
	@Scheduled(fixedDelay = 7 * 60 * 1000) // 7 minutes between COMPLETION and next start
	@SchedulerLock(
		name = "updateWorkspacesJob", 
		lockAtMostFor = "35m", 
		lockAtLeastFor = "10m" 
	)
	public void updateWorkspacesJob() {	
		log.info("Scheduled task started at {}", dateFormatter.format(new Date()));
		try {
			FabricWorkspacesCollectionVO collection = fabricService.getAllLov(0,0);
			WorkspacesCollectionDto collectionFromListWorkspaces = fabricWorkspaceClient.listWorkspaces();
			List<WorkspaceDetailDto> dtosFromFabric = new ArrayList<>();
			if(collectionFromListWorkspaces!=null && collectionFromListWorkspaces.getValue()!=null 
					&& !collectionFromListWorkspaces.getValue().isEmpty()) {
				dtosFromFabric = collectionFromListWorkspaces.getValue();
			}
			List<FabricWorkspaceVO> workspaceVOs = collection!=null ? collection.getRecords() : new ArrayList<>();
			log.info("Fetched all fabric workspaces from service successfully during scheduled job");
			if(workspaceVOs!=null && !workspaceVOs.isEmpty()) {
				log.info("During scheduled job, fetch success. Workspaces available, proceeding with processing user management for each");
				for(FabricWorkspaceVO workspaceVO: workspaceVOs) {
					String updatedName = workspaceVO.getName();
					String updatedDescription = workspaceVO.getDescription();
					boolean isDeleted = false;
					if(dtosFromFabric!=null && !dtosFromFabric.isEmpty()) {
						Optional<WorkspaceDetailDto> fabricWorkspaceDtoOptional = dtosFromFabric.stream().filter(n -> n.getId().equals(workspaceVO.getId())).findFirst();
						if(fabricWorkspaceDtoOptional!=null && fabricWorkspaceDtoOptional.isPresent()) {
							WorkspaceDetailDto fabricWorkspaceDto = fabricWorkspaceDtoOptional.get();
							if(fabricWorkspaceDto!=null && fabricWorkspaceDto.getId().equals(workspaceVO.getId())) {
								updatedName = fabricWorkspaceDto.getDisplayName();
								updatedDescription = fabricWorkspaceDto.getDescription();
							}
						}else {
							fabricService.delete(workspaceVO.getId(),true);
							isDeleted = true;
						}
					}
					if(!isDeleted) {
						if(workspaceVO!=null && workspaceVO.getStatus()!=null && ConstantsUtility.INPROGRESS_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState())){
							FabricWorkspaceStatusVO currentStatus = workspaceVO.getStatus();
							FabricWorkspaceStatusVO updatedStatus = new FabricWorkspaceStatusVO();
							try {
								log.info("During scheduled job, processing INPROGRESS workspace {}: {}", workspaceVO.getId(), workspaceVO.getName());
								updatedStatus = fabricService.processWorkspaceUserManagement(currentStatus,updatedName, workspaceVO.getCreatedBy().getId(), workspaceVO.getId(),workspaceVO.getCustomGroupName(), workspaceVO.getCustomGroupNameCollection());
								log.info("During scheduled job, processed INPROGRESS workspace {}: state={}, {}", workspaceVO.getId(), updatedStatus != null ? updatedStatus.getState() : null, formatStatusDetails(updatedStatus));
								try {
									fabricService.updateWorkspaceStatusAndDetails(workspaceVO.getId(), updatedStatus, updatedName, updatedDescription);
								}catch(Exception saveException) {
									log.error("During scheduled job, failed to update the workspace with latest status {} for workspace {} and id {} with exception {}",
												updatedStatus.getState(), workspaceVO.getName(), workspaceVO.getId(), saveException.getMessage());
								}
							}catch(Exception e) {
								log.error("During scheduled job, failed to process workspace user management for workspace {} and id {} with exception {}", workspaceVO.getName(), workspaceVO.getId(), e.getMessage());
							}
						}
						if(workspaceVO!=null && workspaceVO.getStatus()!=null && ConstantsUtility.COMPLETED_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState())){
							List<GroupDetailsVO> updatedGroupDetails = fabricService.autoProcessGroupsUsers(workspaceVO.getStatus().getMicrosoftGroups(), updatedName, workspaceVO.getCreatedBy().getId(), workspaceVO.getId(), workspaceVO.getCustomGroupName(), workspaceVO.getCustomGroupNameCollection());
							log.info("During scheduled job, processed COMPLETED workspace {}: groups={}", workspaceVO.getId(), formatGroups(updatedGroupDetails));
							try {
								fabricService.updateWorkspaceGroupsAndDetails(workspaceVO.getId(), updatedGroupDetails, updatedName, updatedDescription);
							}catch(Exception saveException) {
								log.error("During scheduled job, failed to update the workspace with latest group assignments for workspace {} and id {} with exception {}", workspaceVO.getName(), workspaceVO.getId(), saveException.getMessage());
							}
						}
					}
				}
			}
			log.info("Scheduled task completed at {}", dateFormatter.format(new Date()));
		}catch(Exception e) {
			e.printStackTrace();
			log.error("During scheduled job, failed to process workspaces user management with exception {}", e.getMessage());
		}
	}

	private String formatStatusDetails(FabricWorkspaceStatusVO status) {
		String roles = status != null && status.getRoles() != null
				? status.getRoles().stream()
						.map(role -> role != null ? role.getName() + "=" + role.getState() : "null")
						.collect(Collectors.joining(", "))
				: "";
		String entitlements = status != null && status.getEntitlements() != null
				? status.getEntitlements().stream()
						.map(entitlement -> entitlement != null
								? entitlement.getDisplayName() + "=" + entitlement.getState()
								: "null")
						.collect(Collectors.joining(", "))
				: "";
		return "roles=[" + roles + "], entitlements=[" + entitlements + "]";
	}

	private String formatGroups(List<GroupDetailsVO> groups) {
		if(groups == null) {
			return "";
		}
		return groups.stream()
				.map(group -> group != null ? group.getGroupName() + "=" + group.getState() : "null")
				.collect(Collectors.joining(", "));
	}
	
}
