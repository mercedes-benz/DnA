package com.daimler.data.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.repo.workspace.WorkSpaceCodeServerBuildDeployRepository;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.service.workspace.WorkspaceService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WorkspaceLogsMigrate {

    @Autowired
	private WorkspaceService service;

	@Value("${workspace.logs.migrate}")
	private boolean migrateWorkspace;

    @Autowired
	private WorkspaceCustomRepository customRepository;

    @PostConstruct
	public void init() {
		if (migrateWorkspace) {
            GenericMessage migrateWorkspaceMsg = null;
			try {
				log.info("Workspace logs Migration Start");
				// CodeServerWorkspaceNsql codeserverNsql = customRepository.findByWorkspaceId("ws2572");
				List<CodeServerWorkspaceNsql> workspaceNsql = customRepository.findAll();
				for(CodeServerWorkspaceNsql codeserverNsql: workspaceNsql){
					 if(Objects.nonNull(codeserverNsql)){							
							if(codeserverNsql.getData().getWorkspaceOwner().getId().equalsIgnoreCase(codeserverNsql.getData().getProjectDetails().getProjectOwner().getId())){
								log.info("Workspace logs Migration Started for Workspace "+ codeserverNsql.getData().getWorkspaceId());														
								migrateWorkspaceMsg = service.migrateWorkspaceLogs(codeserverNsql);							
							}
						if(Objects.nonNull(migrateWorkspaceMsg)) {
							if(Objects.nonNull(migrateWorkspaceMsg.getErrors()) && migrateWorkspaceMsg.getErrors().size() > 0){ 
							List<MessageDescription> error =	migrateWorkspaceMsg.getErrors();
							log.info("Error occured while migrating logs of Workspace "+codeserverNsql.getData().getWorkspaceId());
							for(MessageDescription msg: error){
								log.info("Error Message " + msg.getMessage());
							}
							} else if(Objects.nonNull(migrateWorkspaceMsg.getSuccess())){
								log.info("Migration of Workspace logs "+codeserverNsql.getData().getWorkspaceId() +" : "+ migrateWorkspaceMsg.getSuccess().toString());
							}
						}
					}	
				}
                
            } catch (Exception e) {
                log.error("Exception occured while migrating workspaces logs ",e);
            }

            }
        }
}
