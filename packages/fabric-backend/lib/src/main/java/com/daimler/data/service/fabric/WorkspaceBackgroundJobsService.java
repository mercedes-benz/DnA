package com.daimler.data.service.fabric;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
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
//		
//		 WorkspacesCollectionDto collection = fabricWorkspaceClient.getAllWorkspacesDetails();
//		log.info("Fetched all fabric workspaces from API successfully");
//		if(collection!=null && collection.getValue()!=null && !collection.getValue().isEmpty()) {
//			log.info("Workspaces available, proceeding with provisioning for each");
//			for(WorkspaceDetailDto workspace: collection.getValue()) {
//					fabricWorkspaceClient.provisionWorkspace(workspace.getId());
//					fabricWorkspaceClient.addGroup(workspace.getId());
//			}
//		}
//	}
	
	
	@Scheduled(cron = "0 0/3 * * * *")
	public void updateWorkspacesJob() {		
		List<FabricWorkspaceVO> workspaceVOs = fabricService.getAll();
			log.info("Fetched all fabric workspaces from service successfully during scheduled job");
			if(workspaceVOs!=null && !workspaceVOs.isEmpty()) {
				log.info("During scheduled job, fetch success. Workspaces available, proceeding with processing user management for each");
				for(FabricWorkspaceVO workspaceVO: workspaceVOs) {
					if(workspaceVO!=null && workspaceVO.getStatus()!=null && ConstantsUtility.INPROGRESS_STATE.equalsIgnoreCase(workspaceVO.getStatus().getState())){
						FabricWorkspaceStatusVO currentStatus = workspaceVO.getStatus();
						FabricWorkspaceStatusVO updatedStatus = new FabricWorkspaceStatusVO();
						FabricWorkspaceVO tempWorkspaceVO =  workspaceVO;
						try {
							updatedStatus = fabricService.processWorkspaceUserManagement(currentStatus, workspaceVO.getName(), workspaceVO.getCreatedBy().getId(), workspaceVO.getId());
							tempWorkspaceVO.setStatus(updatedStatus);
							try {
								fabricService.create(tempWorkspaceVO);
							}catch(Exception saveException) {
								log.error("During scheduled job, failed to update the workspace with latest status {} for workspace {} and id {} with exception {}",
											updatedStatus.getState(), workspaceVO.getName(), workspaceVO.getId(), saveException.getMessage());
							}
						
						}catch(Exception e) {
							log.error("During scheduled job, failed to process workspace user management for workspace {} and id {} with exception {}", workspaceVO.getName(), workspaceVO.getId(), e.getMessage());
						}
					}
				}
			}
	}
	
}
