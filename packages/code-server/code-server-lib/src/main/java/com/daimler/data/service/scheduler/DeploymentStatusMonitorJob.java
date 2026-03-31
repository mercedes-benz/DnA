package com.daimler.data.service.scheduler;

import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.service.ArgoCdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Component
@Slf4j
public class DeploymentStatusMonitorJob {

    @Autowired
    private WorkspaceCustomRepository workspaceCustomRepository;

    @Autowired
    private ArgoCdService argoCdService;


    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void monitorDeploymentStatus() {
        try {
            log.debug("Starting deployment status monitoring job");
            
            String argoToken = argoCdService.getArgoToken();
            if (argoToken == null) {
                log.warn("Unable to get ArgoCD token, skipping deployment status check");
                return;
            }

            List<CodeServerWorkspaceNsql> workspaces = workspaceCustomRepository.findAll();
            int checkedCount = 0;
            int updatedCount = 0;

            for (CodeServerWorkspaceNsql workspace : workspaces) {
                if (workspace.getData() == null || workspace.getData().getProjectDetails() == null) {
                    continue;
                }

                String projectName = workspace.getData().getProjectDetails().getProjectName();
                if (projectName == null) {
                    continue;
                }

                CodeServerDeploymentDetails intDeployment = workspace.getData().getProjectDetails().getIntDeploymentDetails();
                if (intDeployment != null && "DEPLOYING".equalsIgnoreCase(intDeployment.getLastDeploymentStatus())) {
                    checkedCount++;
                    if (checkAndUpdateDeployment(argoToken, workspace, intDeployment, projectName, "int")) {
                        updatedCount++;
                    }
                }

                CodeServerDeploymentDetails prodDeployment = workspace.getData().getProjectDetails().getProdDeploymentDetails();
                if (prodDeployment != null && "DEPLOYING".equalsIgnoreCase(prodDeployment.getLastDeploymentStatus())) {
                    checkedCount++;
                    if (checkAndUpdateDeployment(argoToken, workspace, prodDeployment, projectName, "prod")) {
                        updatedCount++;
                    }
                }
            }

            if (checkedCount > 0) {
                log.info("Deployment status monitoring completed - Checked: {}, Updated: {}", checkedCount, updatedCount);
            }
        } catch (Exception e) {
            log.error("Error in deployment status monitoring job", e);
        }
    }

    private boolean checkAndUpdateDeployment(String argoToken, CodeServerWorkspaceNsql workspace, 
                                            CodeServerDeploymentDetails deployment, 
                                            String projectName, String environment) {
        try {
            String appName = projectName.toLowerCase() + "-" + environment;
            String argoStatus = argoCdService.checkArgoAppDeploymentStatus(argoToken, appName);

            if ("DEPLOYED".equals(argoStatus) || "FAILED".equals(argoStatus)) {
                log.info("Updating deployment status for {} from DEPLOYING to {}", appName, argoStatus);
                
                deployment.setLastDeploymentStatus(argoStatus);
                
                DeploymentAudit latestAudit = null;
                if (deployment.getDeploymentAuditLogs() != null && !deployment.getDeploymentAuditLogs().isEmpty()) {
                    latestAudit = deployment.getDeploymentAuditLogs().stream()
                        .filter(audit -> "DEPLOY_REQUESTED".equalsIgnoreCase(audit.getDeploymentStatus()))
                        .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                        .findFirst()
                        .orElse(null);
                }
                
                if ("DEPLOYED".equals(argoStatus)) {
                    if (deployment.getLastDeployedBy() == null) {
                        deployment.setLastDeployedBy(workspace.getData().getWorkspaceOwner());
                    }
                    if (deployment.getLastDeployedOn() == null) {
                        deployment.setLastDeployedOn(new Date());
                    }
                    
                    if (latestAudit != null) {
                        if (deployment.getLastDeployedBranch() == null && latestAudit.getBranch() != null) {
                            deployment.setLastDeployedBranch(latestAudit.getBranch());
                        }
                        if (deployment.getLastDeployedVersion() == null && latestAudit.getVersion() != null) {
                            deployment.setLastDeployedVersion(latestAudit.getVersion());
                        }
                        if (deployment.getGitjobRunID() == null && latestAudit.getGitjobRunID() != null) {
                            deployment.setGitjobRunID(latestAudit.getGitjobRunID());
                        }
                    }
                }
                if (latestAudit != null) {
                    latestAudit.setDeploymentStatus(argoStatus);
                    if ("DEPLOYED".equals(argoStatus)) {
                        latestAudit.setDeployedOn(new Date());
                    }
                    log.info("Updated audit log status to {} for deployment at {}", argoStatus, latestAudit.getTriggeredOn());
                }

                workspaceCustomRepository.updateDeploymentDetails(projectName, environment, deployment, argoStatus);
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to check ArgoCD status for {}-{}: {}", projectName, environment, e.getMessage());
        }
        return false;
    }
}
