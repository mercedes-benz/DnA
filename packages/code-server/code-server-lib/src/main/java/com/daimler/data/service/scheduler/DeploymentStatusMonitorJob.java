package com.daimler.data.service.scheduler;

import com.daimler.data.application.client.CodeServerClient;
import com.daimler.data.controller.DeploymentStatusSseController;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.BuildAudit;
import com.daimler.data.db.json.CodeServerBuildDeploy;
import com.daimler.data.db.json.CodeServerBuildDetails;
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

    @Autowired
    private CodeServerClient codeServerClient;

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

            List<CodeServerWorkspaceNsql> workspaces = workspaceCustomRepository.findDeploymentReconciliationWorkspaces();
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
                repairMissingFailureFields(workspace, intDeployment, projectName, "int");
                boolean intNeedsCheck = intDeployment != null && shouldCheckDeployment(intDeployment)
                        && !isUserCancelled(intDeployment);
                // Recovery for stuck restarts: top-level says RESTART_REQUESTED for this env but per-env field was never updated
                if (!intNeedsCheck && intDeployment != null 
                        && "RESTART_REQUESTED".equalsIgnoreCase(topLevelStatus) 
                        && "int".equalsIgnoreCase(topLevelEnv)) {
                    log.info("Recovery: workspace {} has top-level RESTART_REQUESTED for int but per-env status is {}",
                            projectName, intDeployment.getLastDeploymentStatus());
                    intDeployment.setLastDeploymentStatus("RESTART_REQUESTED");
                    intNeedsCheck = true;
                }
                if (intNeedsCheck && !hasDeploymentHistory(projectName, "int")) {
                    log.info("Clearing stale status for {}-int: no deployment audit logs exist", projectName);
                    intDeployment.setLastDeploymentStatus(null);
                    workspaceCustomRepository.updateDeploymentDetails(projectName, "int", intDeployment, null);
                    intNeedsCheck = false;
                }
                if (intNeedsCheck) {
                    checkedCount++;
                    if (checkAndUpdateDeployment(argoToken, workspace, intDeployment, projectName, "int")) {
                        updatedCount++;
                    }
                }
                CodeServerDeploymentDetails prodDeployment = workspace.getData().getProjectDetails().getProdDeploymentDetails();
                repairMissingFailureFields(workspace, prodDeployment, projectName, "prod");
                boolean prodNeedsCheck = prodDeployment != null && shouldCheckDeployment(prodDeployment)
                        && !isUserCancelled(prodDeployment);
                if (!prodNeedsCheck && prodDeployment != null 
                        && "RESTART_REQUESTED".equalsIgnoreCase(topLevelStatus) 
                        && "prod".equalsIgnoreCase(topLevelEnv)) {
                    log.info("Recovery: workspace {} has top-level RESTART_REQUESTED for prod but per-env status is {}",
                            projectName, prodDeployment.getLastDeploymentStatus());
                    prodDeployment.setLastDeploymentStatus("RESTART_REQUESTED");
                    prodNeedsCheck = true;
                }
                if (prodNeedsCheck && !hasDeploymentHistory(projectName, "prod")) {
                    log.info("Clearing stale status for {}-prod: no deployment audit logs exist", projectName);
                    prodDeployment.setLastDeploymentStatus(null);
                    workspaceCustomRepository.updateDeploymentDetails(projectName, "prod", prodDeployment, null);
                    prodNeedsCheck = false;
                }
                if (prodNeedsCheck) {
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

    private boolean hasDeploymentHistory(String projectName, String environment) {
        try {
            CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
            if (buildDeployEntity == null) {
                return false;
            }
            List<DeploymentAudit> auditLogs = "int".equalsIgnoreCase(environment)
                    ? buildDeployEntity.getData().getIntDeploymentAuditLogs()
                    : buildDeployEntity.getData().getProdDeploymentAuditLogs();
            return auditLogs != null && !auditLogs.isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check deployment history for {}-{}: {}", projectName, environment, e.getMessage());
            return true;
        }
    }

    @Scheduled(fixedDelayString = "#{${deployment.auditLogBackfillMinutes:10} * 60000}",
            initialDelayString = "#{${deployment.auditLogBackfillMinutes:10} * 60000}")
    @SchedulerLock(name = "deploymentAuditLogBackfillJob", lockAtMostFor = "15m", lockAtLeastFor = "5s")
    public void backfillStaleBuildDeployAuditLogs() {
        try {
            for (CodeServerWorkspaceNsql workspace : workspaceCustomRepository.findAll()) {
                if (workspace.getData() == null || workspace.getData().getProjectDetails() == null) {
                    continue;
                }
                String projectName = workspace.getData().getProjectDetails().getProjectName();
                if (projectName == null) {
                    continue;
                }
                CodeServerDeploymentDetails intDeployment = workspace.getData().getProjectDetails().getIntDeploymentDetails();
                if (intDeployment != null && "DEPLOYED".equalsIgnoreCase(intDeployment.getLastDeploymentStatus())) {
                    fixStaleBuildDeployAuditLog(projectName, "int");
                }
                CodeServerDeploymentDetails prodDeployment = workspace.getData().getProjectDetails().getProdDeploymentDetails();
                if (prodDeployment != null && "DEPLOYED".equalsIgnoreCase(prodDeployment.getLastDeploymentStatus())) {
                    fixStaleBuildDeployAuditLog(projectName, "prod");
                }
            }
        } catch (Exception e) {
            log.error("Error in stale build deploy audit log backfill", e);
        }
    }

    private boolean shouldCheckDeployment(CodeServerDeploymentDetails deployment) {
        if (deployment == null || deployment.getLastDeploymentStatus() == null) {
            return false;
        }
        String status = deployment.getLastDeploymentStatus();
        return "DEPLOY_REQUESTED".equalsIgnoreCase(status) || "RESTART_REQUESTED".equalsIgnoreCase(status);
    }

    private void repairMissingFailureFields(CodeServerWorkspaceNsql workspace,
            CodeServerDeploymentDetails deployment, String projectName, String environment) {
        if (deployment == null || !"DEPLOYMENT_FAILED".equalsIgnoreCase(deployment.getLastDeploymentStatus())
                || isUserCancelled(deployment)
                || (deployment.getLastDeployedBy() != null && deployment.getLastDeployedOn() != null)) {
            return;
        }
        try {
            DeploymentAudit latestAudit = findLatestDeploymentAudit(projectName, environment, deployment);
            UserInfo deployedByUser = resolveTriggeredByUser(workspace, latestAudit);
            if (deployment.getLastDeployedBy() == null && deployedByUser != null) {
                deployment.setLastDeployedBy(deployedByUser);
            }
            if (deployment.getLastDeployedOn() == null) {
                Date repairedOn = latestAudit != null && latestAudit.getTriggeredOn() != null
                        ? latestAudit.getTriggeredOn() : new Date();
                deployment.setLastDeployedOn(repairedOn);
                log.info("Repairing lastDeployedOn for {}-{} using {} timestamp",
                        projectName, environment,
                        latestAudit != null && latestAudit.getTriggeredOn() != null ? "audit" : "current time");
            }
            GenericMessage update = workspaceCustomRepository.updateReconciledDeploymentStatus(
                    projectName, environment, deployment, "DEPLOYMENT_FAILED");
            if (update != null && "SUCCESS".equalsIgnoreCase(update.getSuccess())) {
                log.info("Repaired missing failure fields for {}-{}", projectName, environment);
            } else {
                log.warn("Failed to repair missing failure fields for {}-{}", projectName, environment);
            }
        } catch (Exception e) {
            log.warn("Failed to repair missing failure fields for {}-{}: {}", projectName, environment, e.getMessage());
        }
    }

    private DeploymentAudit findLatestDeploymentAudit(String projectName, String environment,
            CodeServerDeploymentDetails deployment) {
        CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
        if (buildDeployEntity != null && buildDeployEntity.getData() != null) {
            List<DeploymentAudit> auditLogs = "int".equalsIgnoreCase(environment)
                    ? buildDeployEntity.getData().getIntDeploymentAuditLogs()
                    : buildDeployEntity.getData().getProdDeploymentAuditLogs();
            if (auditLogs != null && !auditLogs.isEmpty()) {
                DeploymentAudit latest = auditLogs.stream()
                        .filter(audit -> audit.getTriggeredOn() != null)
                        .max((a1, a2) -> a1.getTriggeredOn().compareTo(a2.getTriggeredOn()))
                        .orElse(null);
                if (latest != null) {
                    return latest;
                }
            }
        }
        if (deployment.getDeploymentAuditLogs() == null) {
            return null;
        }
        return deployment.getDeploymentAuditLogs().stream()
                .filter(audit -> audit.getTriggeredOn() != null)
                .max((a1, a2) -> a1.getTriggeredOn().compareTo(a2.getTriggeredOn()))
                .orElse(null);
    }

    /**
     * A deployment explicitly cancelled by the user is terminal and must never be reconciled back
     * to DEPLOYED/DEPLOYING. The old ReplicaSet pods often keep running healthy on the previous
     * image after a terminate, which would otherwise trip the "argo says DEPLOYED" reconciliation.
     */
    private boolean isUserCancelled(CodeServerDeploymentDetails deployment) {
        return deployment != null
                && "DEPLOYMENT_FAILED".equalsIgnoreCase(deployment.getLastDeploymentStatus())
                && deployment.getLastDeploymentError() != null
                && deployment.getLastDeploymentError().startsWith(DeploymentStatusSseController.USER_CANCELLED_MARKER);
    }

    private boolean checkAndUpdateDeployment(String argoToken, CodeServerWorkspaceNsql workspace, 
                                            CodeServerDeploymentDetails deployment, 
                                            String projectName, String environment) {
        try {
            if (isUserCancelled(deployment)) {
                log.debug("Skipping reconciliation for {}-{}: deployment was cancelled by user (terminal)", projectName, environment);
                return false;
            }
            String appName = projectName.toLowerCase() + "-" + environment;
            String currentDbStatus = deployment.getLastDeploymentStatus();
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
            if (latestAudit == null && deployment.getDeploymentAuditLogs() != null && !deployment.getDeploymentAuditLogs().isEmpty()) {
                latestAudit = deployment.getDeploymentAuditLogs().stream()
                    .filter(audit -> audit.getTriggeredOn() != null)
                    .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                    .findFirst()
                    .orElse(null);
            }

            String expectedVersion = latestAudit != null ? latestAudit.getVersion() : null;
            if (expectedVersion == null || expectedVersion.isEmpty()) {
                CodeServerBuildDetails buildDetails = "int".equalsIgnoreCase(environment)
                        ? workspace.getData().getProjectDetails().getIntBuildDetails()
                        : workspace.getData().getProjectDetails().getProdBuildDetails();
                expectedVersion = buildDetails != null ? buildDetails.getVersion() : null;
            }
            Date deployTriggerTime = latestAudit != null ? latestAudit.getTriggeredOn() : null;
            Map<String, String> argoResult;
            if ("DEPLOY_REQUESTED".equalsIgnoreCase(currentDbStatus)) {
                argoResult = argoCdService.checkArgoAppDeploymentStatusWithError(
                        argoToken, appName, expectedVersion, deployTriggerTime);
            } else {
                argoResult = argoCdService.checkArgoAppDeploymentStatusWithError(argoToken, appName);
            }
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

                GenericMessage workspaceUpdate = workspaceCustomRepository.updateReconciledDeploymentStatus(
                        projectName, environment, deployment, targetStatus);
                
                // Also update deployment audit logs in the build deploy entity (used by frontend)
                updateBuildDeployAuditLog(projectName, environment, targetStatus);
                if (workspaceUpdate == null || !"SUCCESS".equalsIgnoreCase(workspaceUpdate.getSuccess())) {
                    log.error("Workspace deployment status write failed for project={} environment={} status={}; skipping notification",
                            projectName, environment, targetStatus);
                    return false;
                }

                if ("DEPLOYED".equals(targetStatus)) {
                    logDeploymentCompletion(projectName, environment, deployment.getLastDeployedVersion(),
                            latestAudit != null ? latestAudit.getTriggeredOn() : null, buildDeployEntity, new Date());
                }

                // Clean up non-retained build images after successful deployment
                if ("DEPLOYED".equals(targetStatus)) {
                    String deployedVersion = deployment.getLastDeployedVersion();
                    cleanupNonRetainedBuildImages(projectName, environment, deployedVersion);
                }

                // Send deployment notification
                sendDeploymentNotification(workspace, deployment, projectName, environment, targetStatus);
                
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to check ArgoCD status for {}-{}: {}", projectName, environment, e.getMessage());
        }
        return false;
    }

    private void logDeploymentCompletion(String projectName, String environment, String version,
            Date deployTriggerTime, CodeServerBuildDeployNsql buildDeployEntity, Date completionTime) {
        String durationPart = "";
        if (deployTriggerTime != null && !deployTriggerTime.after(completionTime)) {
            long elapsedSeconds = (completionTime.getTime() - deployTriggerTime.getTime()) / 1000L;
            durationPart = String.format(" duration=%ds (%02d:%02d)", elapsedSeconds,
                    elapsedSeconds / 60, elapsedSeconds % 60);
        }

        String buildPart = "";
        BuildAudit buildAudit = findBuildAudit(buildDeployEntity, environment, version);
        if (buildAudit != null && buildAudit.getTriggeredOn() != null) {
            buildPart = " buildTriggeredAt=" + buildAudit.getTriggeredOn();
            if (buildAudit.getBuildOn() != null && !buildAudit.getBuildOn().before(buildAudit.getTriggeredOn())) {
                long buildSeconds = (buildAudit.getBuildOn().getTime() - buildAudit.getTriggeredOn().getTime()) / 1000L;
                buildPart += String.format(" buildDuration=%ds (%02d:%02d)", buildSeconds,
                        buildSeconds / 60, buildSeconds % 60);
            }
        }

        log.info("Deployment completed: project={} environment={} version={} deployTriggeredAt={} completedAt={}{}{}",
                projectName, environment, version, deployTriggerTime, completionTime, durationPart, buildPart);
    }

    private BuildAudit findBuildAudit(CodeServerBuildDeployNsql buildDeployEntity, String environment,
            String version) {
        if (buildDeployEntity == null || buildDeployEntity.getData() == null || version == null) {
            return null;
        }
        List<BuildAudit> buildAudits = "int".equalsIgnoreCase(environment)
                ? buildDeployEntity.getData().getIntBuildAuditLogs()
                : buildDeployEntity.getData().getProdBuildAuditLogs();
        if (buildAudits == null) {
            return null;
        }
        return buildAudits.stream()
                .filter(audit -> audit.getVersion() != null && version.equalsIgnoreCase(audit.getVersion()))
                .max((a1, a2) -> {
                    Date first = a1.getTriggeredOn();
                    Date second = a2.getTriggeredOn();
                    if (first == null && second == null) return 0;
                    if (first == null) return -1;
                    if (second == null) return 1;
                    return first.compareTo(second);
                })
                .orElse(null);
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
                if ("DEPLOY_REQUESTED".equalsIgnoreCase(auditStatus) || "RESTART_REQUESTED".equalsIgnoreCase(auditStatus)
                        || "DEPLOYING".equalsIgnoreCase(auditStatus)) {
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

    private void cleanupNonRetainedBuildImages(String projectName, String environment, String deployedVersion) {
        try {
            CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
            if (buildDeployEntity == null || buildDeployEntity.getData() == null) {
                return;
            }
            CodeServerBuildDeploy data = buildDeployEntity.getData();
            List<BuildAudit> buildAuditLogs = "int".equalsIgnoreCase(environment)
                    ? data.getIntBuildAuditLogs()
                    : data.getProdBuildAuditLogs();
            if (buildAuditLogs == null || buildAuditLogs.isEmpty()) {
                return;
            }
            boolean anyDeleted = false;
            for (BuildAudit build : buildAuditLogs) {
                if (build.getVersion() == null) {
                    continue;
                }
                if (deployedVersion != null && build.getVersion().equalsIgnoreCase(deployedVersion)) {
                    continue;
                }
                if (build.isKeepBuildImage() || build.isImageDeleted()) {
                    continue;
                }
                if (!"BUILD_SUCCESS".equalsIgnoreCase(build.getBuildStatus())) {
                    continue;
                }
                GenericMessage deleteResponse = codeServerClient.deleteBuild(projectName, build.getVersion());
                if ("SUCCESS".equalsIgnoreCase(deleteResponse.getSuccess())) {
                    build.setImageDeleted(true);
                    anyDeleted = true;
                    log.info("Cleaned up non-retained build image {}-{} version {}", projectName, environment, build.getVersion());
                } else {
                    log.warn("Failed to delete build image {}-{} version {} from registry", projectName, environment, build.getVersion());
                }
            }
            if (anyDeleted) {
                buildDeployEntity.setData(data);
                buildDeployRepo.save(buildDeployEntity);
            }
        } catch (Exception e) {
            log.warn("Failed to clean up build images for {}-{}: {}", projectName, environment, e.getMessage());
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
            // Collect all recipients: owner, collaborators, deployer — deduplicated by userId
            java.util.Set<String> addedUserIds = new java.util.HashSet<>();

            if (projectOwner != null && projectOwner.getId() != null && addedUserIds.add(projectOwner.getId())) {
                teamMembers.add(projectOwner.getId());
                if (projectOwner.getEmail() != null) {
                    teamMembersEmails.add(projectOwner.getEmail());
                }
            }
            List<UserInfo> collaborators = workspace.getData().getProjectDetails().getProjectCollaborators();
            if (collaborators != null) {
                for (UserInfo collaborator : collaborators) {
                    if (collaborator.getId() != null && addedUserIds.add(collaborator.getId())) {
                        teamMembers.add(collaborator.getId());
                        if (collaborator.getEmail() != null) {
                            teamMembersEmails.add(collaborator.getEmail());
                        }
                    }
                }
            }
            if (deployedBy != null && deployedBy.getId() != null && addedUserIds.add(deployedBy.getId())) {
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
