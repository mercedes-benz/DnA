package com.daimler.data.auth.client;


import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.service.workspace.WorkspaceService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
//@ConditionalOnExpression("${workspace.update}")
public class WorkspaceMigration {

	@Autowired
	private WorkspaceCustomRepository customRepository;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private WorkspaceService service;

	@Value("${workspace.migrate}")
	private boolean migrateWorkspace;

	@Value("${authenticator.uri}")
	private String authenticatorBaseUri;
			
	@PostConstruct
	public void init() {
		if (migrateWorkspace) {
			GenericMessage migrateWorkspaceMsg = null;
			try {
				log.info("Workspace Migration Start");
				List<CodeServerWorkspaceNsql> workspaceNsql = customRepository.findAll();
				for(CodeServerWorkspaceNsql codeserverNsql: workspaceNsql){
					 if(Objects.nonNull(codeserverNsql)){
						if(Objects.isNull(codeserverNsql.getData().getIsWorkspaceMigrated()) || !codeserverNsql.getData().getIsWorkspaceMigrated()) {
							log.info("Workspace Migration Started for Workspace "+ codeserverNsql.getData().getWorkspaceId());
							migrateWorkspaceMsg = service.migrateWorkspace(codeserverNsql);
						}
					}
				}
				log.info("old workspace ids for which service is not created yet are: {}", oldWsIds);
				if (Objects.nonNull(oldWsIds) && oldWsIds.size() > 0) {
					for (String oldWsId : oldWsIds) {
						authenticatorClient.callingKongApis(oldWsId,oldWsId,null,false,null,null,null,null,null,null, false,false,null);
					}
				}
				log.info("old workspace ids for which service already created and attaching authorization plugin as migration are: {}", oldWsIdsWithService);
				if (Objects.nonNull(oldWsIdsWithService) && oldWsIdsWithService.size() > 0) {
					for (String oldWsIdWithService : oldWsIdsWithService) {
						authenticatorClient.attachAppAuthoriserPluginToService(null, oldWsIdWithService , ConstantsUtility.DHC_CAAS);
					}
				}
			} catch (Exception e) {
				log.error("Exception occured while migrating workspaces ",e);
			}

		}
	}
}
