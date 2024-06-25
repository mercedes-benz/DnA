package com.daimler.data.service.forecast;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspacesCollectionDto;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(value="fabricWorkspaces.startup.workspaceprovisioning", havingValue = "true", matchIfMissing = false)
@Component
@Slf4j
public class WorkspaceProvisioningMigrationService {

	@Autowired
	private FabricWorkspaceClient fabricWorkspaceClient;
	
	@PostConstruct
	public void init() {
		
		 WorkspacesCollectionDto collection = fabricWorkspaceClient.getAllWorkspacesDetails();
		log.info("Fetched all fabric workspaces from API successfully");
		if(collection!=null && collection.getValues()!=null && !collection.getValues().isEmpty()) {
			log.info("Workspaces available, proceeding with provisioning for each");
			for(WorkspaceDetailDto workspace: collection.getValues()) {
					fabricWorkspaceClient.provisionWorkspace(workspace.getId());
			}
		}
		
	}
	
	
}
