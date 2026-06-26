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
import com.daimler.dna.notifications.common.producer.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    @Autowired
    private KafkaProducerService kafkaProducer;


    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    @SchedulerLock(name = "deploymentStatusMonitorJob", lockAtMostFor = "2m", lockAtLeastFor = "5s")
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
                
                // Source latestAudit from the build_deploy_nsql entity (the authoritative audit history)
                // rather than deployment.getDeploymentAuditLogs() which is typically null in workspace_nsql.
                DeploymentAudit latestAudit = null;
                CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
                if (buildDeployEntity != null) {
                    List<DeploymentAudit> buildDeployAuditLogs = "int".equalsIgnoreCase(environment)
                            ? buildDeployEntity.getData().getIntDeploymentAuditLogs()
                            : buildDeployEntity.getData().getProdDeploymentAuditLogs();
                    if (buildDeployAuditLogs != null && !buildDeployAuditLogs.isEmpty()) {
                        latestAudit = buildDeployAuditLogs.stream()
                            .filter(audit -> audit.getTriggeredOn() != null)
                            .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                            .findFirst()
                            .orElse(null);
                    }
                }
                // Fallback to the embedded deployment audit logs if build_deploy_nsql had nothing
                if (latestAudit == null && deployment.getDeploymentAuditLogs() != null && !deployment.getDeploymentAuditLogs().isEmpty()) {
                    latestAudit = deployment.getDeploymentAuditLogs().stream()
                        .filter(audit -> audit.getTriggeredOn() != null)
                        .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                        .findFirst()
                        .orElse(null);
                }
                
                // Set lastDeployedBy to the user who triggered the deployment
                // Look up full UserInfo from collaborators list, fallback to project owner
                UserInfo deployedByUser = resolveTriggeredByUser(workspace, latestAudit);
                if ("DEPLOYED".equals(targetStatus) || "RESTARTED".equals(targetStatus)) {
                    // Always overwrite on successful deploys to reflect the actual deployer
                    if (deployedByUser != null) {
                        deployment.setLastDeployedBy(deployedByUser);
                    }
                } else if (deployment.getLastDeployedBy() == null) {
                    // For failures, only set if not already set
                    if (deployedByUser != null) {
                        deployment.setLastDeployedBy(deployedByUser);
                    }
                }
                
                if ("DEPLOYED".equals(targetStatus) || "RESTARTED".equals(targetStatus)) {
                    deployment.setLastDeployedOn(new Date());
                } else if (deployment.getLastDeployedOn() == null) {
                    deployment.setLastDeployedOn(new Date());
                }
                
                if ("DEPLOYED".equals(targetStatus) || "RESTARTED".equals(targetStatus)) {
                    // For successful terminal states, always overwrite branch/version/gitjobRunID
                    // to reflect the actual deployed code
                    if (latestAudit != null && latestAudit.getBranch() != null) {
                        deployment.setLastDeployedBranch(latestAudit.getBranch());
                    } else if ("int".equalsIgnoreCase(environment) && workspace.getData().getProjectDetails().getIntBuildDetails() != null) {
                        deployment.setLastDeployedBranch(workspace.getData().getProjectDetails().getIntBuildDetails().getLastBuildBranch());
                    } else if ("prod".equalsIgnoreCase(environment) && workspace.getData().getProjectDetails().getProdBuildDetails() != null) {
                        deployment.setLastDeployedBranch(workspace.getData().getProjectDetails().getProdBuildDetails().getLastBuildBranch());
                    }

                    if (latestAudit != null && latestAudit.getVersion() != null) {
                        deployment.setLastDeployedVersion(latestAudit.getVersion());
                    } else if ("int".equalsIgnoreCase(environment) && workspace.getData().getProjectDetails().getIntBuildDetails() != null) {
                        deployment.setLastDeployedVersion(workspace.getData().getProjectDetails().getIntBuildDetails().getVersion());
                    } else if ("prod".equalsIgnoreCase(environment) && workspace.getData().getProjectDetails().getProdBuildDetails() != null) {
                        deployment.setLastDeployedVersion(workspace.getData().getProjectDetails().getProdBuildDetails().getVersion());
                    }

                    if (latestAudit != null && latestAudit.getGitjobRunID() != null) {
                        deployment.setGitjobRunID(latestAudit.getGitjobRunID());
                    }
                } else {
                    // For failure states, only populate if currently null (preserve previous values)
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

                // Send deployment notification
                sendDeploymentNotification(workspace, deployment, projectName, environment, targetStatus);
                
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

    private UserInfo resolveTriggeredByUser(CodeServerWorkspaceNsql workspace, DeploymentAudit latestAudit) {
        UserInfo projectOwner = workspace.getData().getProjectDetails().getProjectOwner();
        if (latestAudit == null || latestAudit.getTriggeredBy() == null) {
            return projectOwner;
        }
        String triggeredById = latestAudit.getTriggeredBy();

        // Check if projectOwner is the triggering user
        if (projectOwner != null && triggeredById.equalsIgnoreCase(projectOwner.getId())) {
            return projectOwner;
        }

        // Look up from collaborators list
        List<UserInfo> collaborators = workspace.getData().getProjectDetails().getProjectCollaborators();
        if (collaborators != null) {
            for (UserInfo collaborator : collaborators) {
                if (collaborator != null && triggeredById.equalsIgnoreCase(collaborator.getId())) {
                    return collaborator;
                }
            }
        }

        // Fallback: create a minimal UserInfo with just the triggeredBy ID
        UserInfo minimalUser = new UserInfo();
        minimalUser.setId(triggeredById);
        return minimalUser;
    }

    private void sendDeploymentNotification(CodeServerWorkspaceNsql workspace,
                                            CodeServerDeploymentDetails deployment,
                                            String projectName, String environment,
                                            String targetStatus) {
        try {
            String eventType;
            String message;
            String envLabel = "prod".equalsIgnoreCase(environment) ? "Production" : environment;
            String resourceID = workspace.getData().getWorkspaceId();
            UserInfo deployedBy = deployment.getLastDeployedBy();
            UserInfo projectOwner = workspace.getData().getProjectDetails().getProjectOwner();
            String userId = deployedBy != null ? deployedBy.getId() : (projectOwner != null ? projectOwner.getId() : "");
            String branch = deployment.getLastDeployedBranch() != null ? deployment.getLastDeployedBranch() : "";
            String version = deployment.getLastDeployedVersion() != null ? deployment.getLastDeployedVersion() : "";

            switch (targetStatus) {
                case "DEPLOYED":
                    eventType = "Codespace-Deploy";
                    message = "Successfully deployed Codespace " + projectName + " with branch " + branch
                            + " version " + version + " on " + envLabel + " triggered by " + userId;
                    break;
                case "DEPLOYMENT_FAILED":
                    eventType = "Codespace-Deploy Failed";
                    message = "Failed to deploy Codespace " + projectName + " with branch " + branch
                            + " on " + envLabel + " triggered by " + userId;
                    break;
                case "RESTARTED":
                    eventType = "Codespace-Deploy";
                    message = "Successfully restarted Codespace " + projectName + " on " + envLabel
                            + " triggered by " + userId;
                    break;
                case "RESTART_FAILED":
                    eventType = "Codespace-Deploy Failed";
                    message = "Failed to restart Codespace " + projectName + " on " + envLabel
                            + " triggered by " + userId;
                    break;
                default:
                    return;
            }

            List<String> teamMembers = new ArrayList<>();
            List<String> teamMembersEmails = new ArrayList<>();
            // Notify the project owner
            if (projectOwner != null) {
                if (projectOwner.getId() != null) {
                    teamMembers.add(projectOwner.getId());
                }
                if (projectOwner.getEmail() != null) {
                    teamMembersEmails.add(projectOwner.getEmail());
                }
            }
            // Also notify the deployer if different from project owner
            if (deployedBy != null && deployedBy.getId() != null
                    && (projectOwner == null || !deployedBy.getId().equalsIgnoreCase(projectOwner.getId()))) {
                teamMembers.add(deployedBy.getId());
                if (deployedBy.getEmail() != null) {
                    teamMembersEmails.add(deployedBy.getEmail());
                }
            }

            kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
            log.info("Sent deployment notification for {}-{}: eventType={}, status={}", projectName, environment, eventType, targetStatus);
        } catch (Exception e) {
            log.warn("Failed to send deployment notification for {}-{}: {}", projectName, environment, e.getMessage());
        }
    }
}
