package com.daimler.data.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.GitClient;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CodespaceOwnershipUpdater {

    @Autowired
    private WorkspaceCustomRepository customRepo;

    @Autowired
    private GitClient gitClient;

    //@PostConstruct
    public void init() {
        List<CodeServerWorkspaceNsql> listOfCodespaces = customRepo.findAllByUniqueLiteral();

        if (listOfCodespaces != null && !listOfCodespaces.isEmpty()) {
            for (CodeServerWorkspaceNsql entity : listOfCodespaces) {
                CodeServerWorkspace record = entity.getData();
                if(!record.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
						.startsWith("private") &&
						!record.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
						.equalsIgnoreCase("default")
						&& !record.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
								.startsWith("bat") && !record.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
								.startsWith("public"))
                {
                    if (entity.getData() != null && entity.getData().getWorkspaceOwner() != null && entity.getData().getProjectDetails() != null) {
                        String owner = entity.getData().getWorkspaceOwner().getId();
                        String repoName = entity.getData().getProjectDetails().getGitRepoName();
    
                        HttpStatus addAdminAccessToGitUser = gitClient.addAdminAccessToRepo(owner, repoName);
    
                        if (!addAdminAccessToGitUser.is2xxSuccessful()) {
                            log.info("Failed while updating user {} to admin", owner);
                        }
                    } else {
                        log.warn("Skipping entity with missing data: {}", entity.getId());
                    }

                }
            }
            log.info("Successfully migrated all codespaces");
        } else {
            log.info("No records in database");
        }
    }
}
