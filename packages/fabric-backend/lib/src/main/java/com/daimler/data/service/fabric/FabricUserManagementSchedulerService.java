package com.daimler.data.service.fabric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.AuthoriserClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FabricUserManagementSchedulerService {

	@Autowired
	private FabricWorkspaceService fabricWorkspaceService;
	
	@Autowired
	private AuthoriserClient identityClient;
	
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
	
	
	
	@Scheduled(cron = "0 0/3 * * * *")
	public void updateRunsCron() {		
		
	}
}
