package com.daimler.data.service.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspacesCollectionDto;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(value="fabricWorkspaces.startup.workspaceprovisioning", havingValue = "true", matchIfMissing = false)
@Component
@Slf4j
public class WorkspaceBackgroundJobsService {

	@Autowired
	private FabricWorkspaceClient fabricWorkspaceClient;
	
	@Autowired
	private FabricWorkspaceService fabricService;

	
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
	
	
	@Scheduled(cron = "0 0/7 * * * *")
	public void updateWorkspacesJob() {	
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
							fabricService.delete(workspaceVO.getId());
							isDeleted = true;
						}
					}
					if(!isDeleted) {
						if(workspaceVO!=null && workspaceVO.getStatus()!=null && ConstantsUtility.INPROGRESS_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState())){
							FabricWorkspaceStatusVO currentStatus = workspaceVO.getStatus();
							FabricWorkspaceStatusVO updatedStatus = new FabricWorkspaceStatusVO();
							FabricWorkspaceVO tempWorkspaceVO =  workspaceVO;
							try {
								updatedStatus = fabricService.processWorkspaceUserManagement(currentStatus,updatedName, workspaceVO.getCreatedBy().getId(), workspaceVO.getId());
								tempWorkspaceVO.setStatus(updatedStatus);
								try {
									tempWorkspaceVO.setName(updatedName);
									tempWorkspaceVO.setDescription(updatedDescription);
									fabricService.create(tempWorkspaceVO);
								}catch(Exception saveException) {
									log.error("During scheduled job, failed to update the workspace with latest status {} for workspace {} and id {} with exception {}",
												updatedStatus.getState(), workspaceVO.getName(), workspaceVO.getId(), saveException.getMessage());
								}
							}catch(Exception e) {
								log.error("During scheduled job, failed to process workspace user management for workspace {} and id {} with exception {}", workspaceVO.getName(), workspaceVO.getId(), e.getMessage());
							}
						}
						if(workspaceVO!=null && workspaceVO.getStatus()!=null && ConstantsUtility.COMPLETED_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState())){
							FabricWorkspaceVO tempWorkspaceVO =  workspaceVO;
							List<GroupDetailsVO> updatedGroupDetails = fabricService.autoProcessGroupsUsers(workspaceVO.getStatus().getMicrosoftGroups(), updatedName, workspaceVO.getCreatedBy().getId(), workspaceVO.getId());
							tempWorkspaceVO.getStatus().setMicrosoftGroups(updatedGroupDetails);
							try {
								tempWorkspaceVO.setName(updatedName);
								tempWorkspaceVO.setDescription(updatedDescription);
								fabricService.create(tempWorkspaceVO);
							}catch(Exception saveException) {
								log.error("During scheduled job, failed to update the workspace with latest group assignments for workspace {} and id {} with exception {}", workspaceVO.getName(), workspaceVO.getId(), saveException.getMessage());
							}
						}
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			log.error("During scheduled job, failed to process workspaces user management with exception {}", e.getMessage());
		}
	}
	
}
