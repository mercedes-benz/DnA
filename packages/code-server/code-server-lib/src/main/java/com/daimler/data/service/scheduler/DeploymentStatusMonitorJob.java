package com.daimler.data.service.scheduler;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerBuildDeploy;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkSpaceCodeServerBuildDeployRepository;
import com.daimler.data.db.repo.workspace.WorkspaceCustomBuildDeployRepo;
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

    @Autowired
    private WorkspaceCustomBuildDeployRepo buildDeployCustomRepo;

    @Autowired
    private WorkSpaceCodeServerBuildDeployRepository buildDeployRepo;


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
                if (intDeployment != null && shouldCheckDeployment(intDeployment.getLastDeploymentStatus())) {
                    checkedCount++;
                    if (checkAndUpdateDeployment(argoToken, workspace, intDeployment, projectName, "int")) {
                        updatedCount++;
                    }
                } else if (intDeployment != null && "DEPLOYED".equalsIgnoreCase(intDeployment.getLastDeploymentStatus())) {
                    // Fix stale build deploy entities for workspaces that were already updated
                    // before the build deploy audit log fix was deployed
                    fixStaleBuildDeployAuditLog(projectName, "int");
                }

                CodeServerDeploymentDetails prodDeployment = workspace.getData().getProjectDetails().getProdDeploymentDetails();
                if (prodDeployment != null && shouldCheckDeployment(prodDeployment.getLastDeploymentStatus())) {
                    checkedCount++;
                    if (checkAndUpdateDeployment(argoToken, workspace, prodDeployment, projectName, "prod")) {
                        updatedCount++;
                    }
                } else if (prodDeployment != null && "DEPLOYED".equalsIgnoreCase(prodDeployment.getLastDeploymentStatus())) {
                    fixStaleBuildDeployAuditLog(projectName, "prod");
                }
            }

            if (checkedCount > 0) {
                log.info("Deployment status monitoring completed - Checked: {}, Updated: {}", checkedCount, updatedCount);
            }
        } catch (Exception e) {
            log.error("Error in deployment status monitoring job", e);
        }
    }

    private boolean shouldCheckDeployment(String status) {
        if (status == null) return false;
        return "DEPLOYING".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
    }

    private boolean checkAndUpdateDeployment(String argoToken, CodeServerWorkspaceNsql workspace, 
                                            CodeServerDeploymentDetails deployment, 
                                            String projectName, String environment) {
        try {
            String appName = projectName.toLowerCase() + "-" + environment;
            String currentDbStatus = deployment.getLastDeploymentStatus();
            String argoStatus = argoCdService.checkArgoAppDeploymentStatus(argoToken, appName);

            
            boolean needsUpdate = false;
            if ("DEPLOYED".equals(argoStatus) && !"DEPLOYED".equalsIgnoreCase(currentDbStatus)) {
                needsUpdate = true;
            } else if ("FAILED".equals(argoStatus) && "DEPLOYING".equalsIgnoreCase(currentDbStatus)) {
                needsUpdate = true;
            }

            if (needsUpdate) {
                log.info("Reconciling deployment status for {} from {} to {}", appName, currentDbStatus, argoStatus);
                
                deployment.setLastDeploymentStatus(argoStatus);
                
                DeploymentAudit latestAudit = null;
                if (deployment.getDeploymentAuditLogs() != null && !deployment.getDeploymentAuditLogs().isEmpty()) {
                    latestAudit = deployment.getDeploymentAuditLogs().stream()
                        .filter(audit -> audit.getTriggeredOn() != null)
                        .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                        .findFirst()
                        .orElse(null);
                }
                
                // Set timestamp for both DEPLOYED and FAILED
                deployment.setLastDeployedOn(new Date());
                
                if ("DEPLOYED".equals(argoStatus)) {
                    if (deployment.getLastDeployedBy() == null) {
                        deployment.setLastDeployedBy(workspace.getData().getWorkspaceOwner());
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
                    latestAudit.setDeployedOn(new Date());
                    log.info("Updated audit log status to {} for deployment at {}", argoStatus, latestAudit.getTriggeredOn());
                }

                workspaceCustomRepository.updateDeploymentDetails(projectName, environment, deployment, argoStatus);
                
                // Also update deployment audit logs in the build deploy entity (used by frontend)
                updateBuildDeployAuditLog(projectName, environment, argoStatus);
                
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to check ArgoCD status for {}-{}: {}", projectName, environment, e.getMessage());
        }
        return false;
    }

    private void fixStaleBuildDeployAuditLog(String projectName, String environment) {
        // For workspaces already at DEPLOYED, check if the build deploy entity
        // still has stale DEPLOYING audit logs and fix them (no ArgoCD API call needed)
        updateBuildDeployAuditLog(projectName, environment, "DEPLOYED");
    }

    private void updateBuildDeployAuditLog(String projectName, String environment, String argoStatus) {
        try {
            CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
            if (buildDeployEntity == null) {
                return;
            }
            CodeServerBuildDeploy data = buildDeployEntity.getData();
            List<DeploymentAudit> auditLogs = "int".equalsIgnoreCase(environment)
                    ? data.getIntDeploymentAuditLogs()
                    : data.getProdDeploymentAuditLogs();
            if (auditLogs == null || auditLogs.isEmpty()) {
                return;
            }
            // Find the latest DEPLOYING audit log and update its status
            for (int i = auditLogs.size() - 1; i >= 0; i--) {
                DeploymentAudit audit = auditLogs.get(i);
                if ("DEPLOYING".equalsIgnoreCase(audit.getDeploymentStatus())) {
                    audit.setDeploymentStatus(argoStatus);
                    audit.setDeployedOn(new Date());
                    log.info("Updated build deploy audit log status to {} for {}-{}", argoStatus, projectName, environment);
                    break;
                }
            }
            buildDeployEntity.setData(data);
            buildDeployRepo.save(buildDeployEntity);
        } catch (Exception e) {
            log.warn("Failed to update build deploy audit log for {}-{}: {}", projectName, environment, e.getMessage());
        }
    }
}
