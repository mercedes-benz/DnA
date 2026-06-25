package com.daimler.data.service.scheduler;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerBuildDeploy;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.json.UserInfo;
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
import java.util.Map;


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

                // Recovery: if top-level lastBuildOrDeployedStatus is RESTART_REQUESTED but
                // per-environment lastDeploymentStatus was never updated, force-check it.
                String topLevelStatus = workspace.getData().getProjectDetails().getLastBuildOrDeployedStatus();
                String topLevelEnv = workspace.getData().getProjectDetails().getLastBuildOrDeployedEnv();

                CodeServerDeploymentDetails intDeployment = workspace.getData().getProjectDetails().getIntDeploymentDetails();
                boolean intNeedsCheck = intDeployment != null && shouldCheckDeployment(intDeployment.getLastDeploymentStatus());
                // Recovery for stuck restarts: top-level says RESTART_REQUESTED for this env but per-env field was never updated
                if (!intNeedsCheck && intDeployment != null 
                        && "RESTART_REQUESTED".equalsIgnoreCase(topLevelStatus) 
                        && "int".equalsIgnoreCase(topLevelEnv)) {
                    log.info("Recovery: workspace {} has top-level RESTART_REQUESTED for int but per-env status is {}",
                            projectName, intDeployment.getLastDeploymentStatus());
                    intDeployment.setLastDeploymentStatus("RESTART_REQUESTED");
                    intNeedsCheck = true;
                }
                if (intNeedsCheck) {
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
                boolean prodNeedsCheck = prodDeployment != null && shouldCheckDeployment(prodDeployment.getLastDeploymentStatus());
                if (!prodNeedsCheck && prodDeployment != null 
                        && "RESTART_REQUESTED".equalsIgnoreCase(topLevelStatus) 
                        && "prod".equalsIgnoreCase(topLevelEnv)) {
                    log.info("Recovery: workspace {} has top-level RESTART_REQUESTED for prod but per-env status is {}",
                            projectName, prodDeployment.getLastDeploymentStatus());
                    prodDeployment.setLastDeploymentStatus("RESTART_REQUESTED");
                    prodNeedsCheck = true;
                }
                if (prodNeedsCheck) {
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
        return "DEPLOY_REQUESTED".equalsIgnoreCase(status) || "DEPLOYMENT_FAILED".equalsIgnoreCase(status)
                || "RESTART_REQUESTED".equalsIgnoreCase(status);
    }

    private boolean checkAndUpdateDeployment(String argoToken, CodeServerWorkspaceNsql workspace, 
                                            CodeServerDeploymentDetails deployment, 
                                            String projectName, String environment) {
        try {
            String appName = projectName.toLowerCase() + "-" + environment;
            String currentDbStatus = deployment.getLastDeploymentStatus();
            Map<String, String> argoResult = argoCdService.checkArgoAppDeploymentStatusWithError(argoToken, appName);
            String argoStatus = argoResult.get("status");
            String argoErrorMessage = argoResult.get("errorMessage");

            
            boolean needsUpdate = false;
            String targetStatus = argoStatus;
            
            if ("RESTART_REQUESTED".equalsIgnoreCase(currentDbStatus)) {
                // For restart: Healthy+Succeeded → RESTARTED, Degraded/Failed → RESTART_FAILED
                if ("DEPLOYED".equals(argoStatus)) {
                    targetStatus = "RESTARTED";
                    needsUpdate = true;
                } else if ("DEPLOYMENT_FAILED".equals(argoStatus)) {
                    targetStatus = "RESTART_FAILED";
                    needsUpdate = true;
                }
            } else if ("DEPLOYED".equals(argoStatus) && !"DEPLOYED".equalsIgnoreCase(currentDbStatus)) {
                needsUpdate = true;
            } else if ("DEPLOYMENT_FAILED".equals(argoStatus) && !"DEPLOYMENT_FAILED".equalsIgnoreCase(currentDbStatus)) {
                needsUpdate = true;
            }
            
            if (!needsUpdate && "DEPLOYMENT_FAILED".equalsIgnoreCase(currentDbStatus)) {
                if (deployment.getLastDeployedBy() == null || deployment.getLastDeployedOn() == null) {
                    log.info("Force updating {} - status is DEPLOYMENT_FAILED but critical fields are null", appName);
                    needsUpdate = true;
                    targetStatus = "DEPLOYMENT_FAILED"; // Keep same status but update fields
                }
            }

            if (needsUpdate) {
                log.info("Reconciling deployment status for {} from {} to {}", appName, currentDbStatus, targetStatus);
                
                deployment.setLastDeploymentStatus(targetStatus);
                if ("DEPLOYMENT_FAILED".equals(targetStatus) || "RESTART_FAILED".equals(targetStatus)) {
                    deployment.setLastDeploymentError(argoErrorMessage);
                } else {
                    deployment.setLastDeploymentError(null);
                }
                
                DeploymentAudit latestAudit = null;
                if (deployment.getDeploymentAuditLogs() != null && !deployment.getDeploymentAuditLogs().isEmpty()) {
                    latestAudit = deployment.getDeploymentAuditLogs().stream()
                        .filter(audit -> audit.getTriggeredOn() != null)
                        .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                        .findFirst()
                        .orElse(null);
                }
                
                if (deployment.getLastDeployedBy() == null) {
                    UserInfo projectOwner = workspace.getData().getProjectDetails().getProjectOwner();
                    if (projectOwner != null && projectOwner.getId() != null) {
                        deployment.setLastDeployedBy(new UserInfo(
                            projectOwner.getId(),
                            projectOwner.getFirstName(),
                            projectOwner.getLastName(),
                            projectOwner.getDepartment(),
                            projectOwner.getEmail(),
                            projectOwner.getMobileNumber(),
                            projectOwner.getGitUserName(),
                            projectOwner.getIsAdmin(),
                            projectOwner.getIsApprover()
                        ));
                    }
                }
                
                if ("DEPLOYED".equals(targetStatus) || "RESTARTED".equals(targetStatus)) {
                    deployment.setLastDeployedOn(new Date());
                } else if (deployment.getLastDeployedOn() == null) {
                    deployment.setLastDeployedOn(new Date());
                }
                
                if (latestAudit != null) {
                    boolean isSuccessful = "DEPLOYED".equals(targetStatus) || "RESTARTED".equals(targetStatus);
                    if (latestAudit.getBranch() != null && (isSuccessful || deployment.getLastDeployedBranch() == null)) {
                        deployment.setLastDeployedBranch(latestAudit.getBranch());
                    }
                    if (latestAudit.getVersion() != null && (isSuccessful || deployment.getLastDeployedVersion() == null)) {
                        deployment.setLastDeployedVersion(latestAudit.getVersion());
                    }
                    if (latestAudit.getGitjobRunID() != null && (isSuccessful || deployment.getGitjobRunID() == null)) {
                        deployment.setGitjobRunID(latestAudit.getGitjobRunID());
                    }
                }
                
                if (deployment.getLastDeployedBranch() == null || deployment.getLastDeployedVersion() == null) {
                    if ("int".equalsIgnoreCase(environment) && workspace.getData().getProjectDetails().getIntBuildDetails() != null) {
                        if (deployment.getLastDeployedBranch() == null) {
                            deployment.setLastDeployedBranch(workspace.getData().getProjectDetails().getIntBuildDetails().getLastBuildBranch());
                        }
                        if (deployment.getLastDeployedVersion() == null) {
                            deployment.setLastDeployedVersion(workspace.getData().getProjectDetails().getIntBuildDetails().getVersion());
                        }
                    } else if ("prod".equalsIgnoreCase(environment) && workspace.getData().getProjectDetails().getProdBuildDetails() != null) {
                        if (deployment.getLastDeployedBranch() == null) {
                            deployment.setLastDeployedBranch(workspace.getData().getProjectDetails().getProdBuildDetails().getLastBuildBranch());
                        }
                        if (deployment.getLastDeployedVersion() == null) {
                            deployment.setLastDeployedVersion(workspace.getData().getProjectDetails().getProdBuildDetails().getVersion());
                        }
                    }
                }
                if (latestAudit != null) {
                    latestAudit.setDeploymentStatus(targetStatus);
                    if ("DEPLOYED".equals(targetStatus) || "RESTARTED".equals(targetStatus)) {
                        latestAudit.setDeployedOn(new Date());
                    }
                    log.info("Updated audit log status to {} for deployment at {}", targetStatus, latestAudit.getTriggeredOn());
                }

                workspaceCustomRepository.updateDeploymentDetails(projectName, environment, deployment, targetStatus);
                
                // Also update deployment audit logs in the build deploy entity (used by frontend)
                updateBuildDeployAuditLog(projectName, environment, targetStatus);
                
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
            // Find the latest in-progress audit log and update its status
            boolean foundTerminalAfter = false;
            boolean updated = false;
            for (int i = auditLogs.size() - 1; i >= 0; i--) {
                DeploymentAudit audit = auditLogs.get(i);
                String auditStatus = audit.getDeploymentStatus();
                if ("DEPLOYED".equalsIgnoreCase(auditStatus) || "DEPLOYMENT_FAILED".equalsIgnoreCase(auditStatus)
                        || "RESTARTED".equalsIgnoreCase(auditStatus) || "RESTART_FAILED".equalsIgnoreCase(auditStatus)) {
                    foundTerminalAfter = true;
                }
                if ("DEPLOY_REQUESTED".equalsIgnoreCase(auditStatus) || "RESTART_REQUESTED".equalsIgnoreCase(auditStatus)) {
                    if (foundTerminalAfter) {
                        audit.setDeploymentStatus("DEPLOYMENT_FAILED");
                        log.info("Marked superseded audit log entry as DEPLOYMENT_FAILED for {}-{} at index {}", projectName, environment, i);
                    } else {
                        audit.setDeploymentStatus(argoStatus);
                        if ("DEPLOYED".equals(argoStatus) || "RESTARTED".equals(argoStatus)) {
                            audit.setDeployedOn(new Date());
                        }
                        log.info("Updated build deploy audit log status to {} for {}-{}", argoStatus, projectName, environment);
                    }
                    updated = true;
                    break;
                }
            }
            if (updated) {
                buildDeployEntity.setData(data);
                buildDeployRepo.save(buildDeployEntity);
            }
        } catch (Exception e) {
            log.warn("Failed to update build deploy audit log for {}-{}: {}", projectName, environment, e.getMessage());
        }
    }
}
