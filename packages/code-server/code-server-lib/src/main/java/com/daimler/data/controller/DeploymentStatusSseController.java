package com.daimler.data.controller;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerBuildDeploy;
import com.daimler.data.db.json.CodeServerBuildDetails;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkSpaceCodeServerBuildDeployRepository;
import com.daimler.data.db.repo.workspace.WorkspaceCustomBuildDeployRepo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.service.ArgoCdService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api")
public class DeploymentStatusSseController {

    @Autowired
    private WorkspaceCustomRepository workspaceRepository;

    @Autowired
    private ArgoCdService argoCdService;

    @Autowired
    private WorkspaceCustomBuildDeployRepo buildDeployCustomRepo;

    @Autowired
    private WorkSpaceCodeServerBuildDeployRepository buildDeployRepo;

    @Value("${deployment.stuckThresholdMinutes:15}")
    private int stuckThresholdMinutes;

    /** Marker persisted in lastDeploymentError so reconcilers can recognise a user-cancelled deployment. */
    public static final String USER_CANCELLED_MARKER = "Deployment cancelled by user";

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/workspace/deployment/stream/{projectName}/{environment}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDeploymentStatus(
            @PathVariable String projectName,
            @PathVariable String environment) {

        log.info("Starting SSE stream for project: {}, environment: {}", projectName, environment);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            int errorCount = 0;
            int maxErrors = 5;
            int maxIterations = 600;
            int iteration = 0;
            boolean seenInProgress = false;
            // Minimum iterations before accepting terminal status from ArgoCD.
            // This prevents false "DEPLOYED" when re-deploying an already-deployed app,
            // because ArgoCD still shows the old deployment as Healthy+Succeeded
            // before the new sync starts.
            int minIterationsBeforeTerminal = 5; // ~15 seconds at 3s interval
            
            try {
                while (iteration < maxIterations) {
                    iteration++;
                    try {
                        Map<String, Object> statusData = getDeploymentStatusData(projectName, environment);
                        
                        emitter.send(SseEmitter.event()
                                .name("deployment-status")
                                .data(objectMapper.writeValueAsString(statusData)));

                        String status = (String) statusData.get("currentStatus");
                        log.debug("SSE iteration {}: status={} seenInProgress={} for {}/{}", iteration, status, seenInProgress, projectName, environment);
                        
                        if ("DEPLOY_REQUESTED".equals(status) || "BUILDING".equals(status)) {
                            seenInProgress = true;
                        }
                        
                        if ("BUILT".equals(status) || "BUILD_FAILED".equals(status)) {
                            log.info("Build finished with status: {} after {} iterations", status, iteration);
                            emitter.send(SseEmitter.event()
                                    .name("build-complete")
                                    .data(objectMapper.writeValueAsString(statusData)));
                            break;
                        }
                        
                        if ("DEPLOYED".equals(status) || "FAILED".equals(status) || "DEPLOYMENT_FAILED".equals(status)) {
                            String opMsg = (String) statusData.get("argocdOperationMessage");
                            boolean userCancelled = Boolean.TRUE.equals(statusData.get("userCancelled"));
                            boolean argoConfirmedFailed = "DEPLOYMENT_FAILED".equals(status) && 
                                (("Failed".equalsIgnoreCase((String) statusData.get("argocdLastSyncPhase"))) ||
                                 (opMsg != null && (opMsg.toLowerCase().contains("failed") || opMsg.toLowerCase().contains("error"))));
                            // Only accept terminal status if we've seen the deployment actually start,
                            // or enough time has passed to rule out stale ArgoCD state.
                            // A user-cancelled deployment is always terminal, even on the very first iteration.
                            if (userCancelled || seenInProgress || iteration >= minIterationsBeforeTerminal || argoConfirmedFailed) {
                                log.info("Deployment finished with status: {} after {} iterations (seenInProgress={})", status, iteration, seenInProgress);
                                emitter.send(SseEmitter.event()
                                        .name("deployment-complete")
                                        .data(objectMapper.writeValueAsString(statusData)));
                                break;
                            } else {
                                log.info("SSE iteration {}: ignoring early terminal status {} (waiting for new sync to start)", iteration, status);
                            }
                        }
                        
                        if ("ERROR".equals(status) || "NOT_FOUND".equals(status)) {
                            errorCount++;
                            log.warn("SSE got error status (attempt {}/{}): {}", errorCount, maxErrors, statusData.get("message"));
                            if (errorCount >= maxErrors) {
                                log.error("Too many errors, closing SSE stream after {} iterations", iteration);
                                emitter.send(SseEmitter.event()
                                        .name("deployment-error")
                                        .data(objectMapper.writeValueAsString(statusData)));
                                break;
                            }
                        } else {
                            errorCount = 0;
                        }

                        Thread.sleep(3000);

                    } catch (IOException e) {
                        log.warn("Client disconnected from SSE stream at iteration {}", iteration);
                        emitter.completeWithError(e);
                        return;
                    } catch (InterruptedException e) {
                        log.warn("SSE stream interrupted at iteration {}", iteration);
                        Thread.currentThread().interrupt();
                        emitter.completeWithError(e);
                        return;
                    }
                }
                
                if (iteration >= maxIterations) {
                    log.warn("SSE stream reached max iterations ({}) for {}/{}", maxIterations, projectName, environment);
                    Map<String, Object> timeoutData = new HashMap<>();
                    timeoutData.put("currentStatus", "TIMEOUT");
                    timeoutData.put("message", "Deployment monitoring timed out after 30 minutes");
                    timeoutData.put("projectName", projectName);
                    timeoutData.put("environment", environment);
                    emitter.send(SseEmitter.event()
                            .name("deployment-timeout")
                            .data(objectMapper.writeValueAsString(timeoutData)));
                }
                
                emitter.complete();
                log.info("SSE stream completed successfully after {} iterations", iteration);

            } catch (Exception e) {
                log.error("Error in SSE stream", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onCompletion(() -> log.info("SSE emitter completed for {}/{}", projectName, environment));
        emitter.onTimeout(() -> log.warn("SSE emitter timeout for {}/{}", projectName, environment));
        emitter.onError((ex) -> log.error("SSE emitter error for {}/{}", projectName, environment, ex));

        return emitter;
    }

    private Map<String, Object> getDeploymentStatusData(String projectName, String environment) {
        Map<String, Object> data = new HashMap<>();
        data.put("projectName", projectName);
        data.put("environment", environment);
        data.put("timestamp", new Date());

        try {
            CodeServerWorkspaceNsql entity = workspaceRepository.findbyProjectName(projectName);

            if (entity == null || entity.getData() == null || entity.getData().getProjectDetails() == null) {
                data.put("currentStatus", "NOT_FOUND");
                data.put("message", "No deployment data found for project: " + projectName);
                return data;
            }
            
            CodeServerDeploymentDetails deploymentDetails;
            List<DeploymentAudit> auditLogs;

            if ("int".equalsIgnoreCase(environment)) {
                deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
            } else if ("prod".equalsIgnoreCase(environment)) {
                deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
            } else {
                data.put("currentStatus", "ERROR");
                data.put("message", "Invalid environment. Use 'int' or 'prod'");
                return data;
            }

            // Check workspace-level status first
            String lastBuildOrDeployStatus = entity.getData().getProjectDetails().getLastBuildOrDeployedStatus();
            String lastBuildOrDeployedEnv = entity.getData().getProjectDetails().getLastBuildOrDeployedEnv();
            
            log.debug("Project {}, Environment {}: deploymentDetails={}, lastBuildOrDeployStatus={}, lastBuildOrDeployedEnv={}", 
                projectName, environment, (deploymentDetails != null ? "exists" : "null"), lastBuildOrDeployStatus, lastBuildOrDeployedEnv);
            
            // If deployment details are null but workspace shows deployment activity for this environment
            if (deploymentDetails == null) {
                // Check if there's any deployment activity for this environment at workspace level
                // Only return NO_DEPLOYMENT if there's truly no deployment data for this specific environment
                if (lastBuildOrDeployStatus == null) {
                    // No workspace-level status at all - truly no deployment
                    data.put("currentStatus", "NO_DEPLOYMENT");
                    data.put("message", "No deployment details found for environment: " + environment);
                    log.info("Project {}, Environment {}: NO_DEPLOYMENT - no workspace status", projectName, environment);
                    return data;
                }
                
                if (!environment.equalsIgnoreCase(lastBuildOrDeployedEnv)) {
                    // There's a status but for a different environment - this environment has no deployment
                    data.put("currentStatus", "NO_DEPLOYMENT");
                    data.put("message", "No deployment details found for environment: " + environment);
                    log.info("Project {}, Environment {}: NO_DEPLOYMENT - status for different env ({})", 
                        projectName, environment, lastBuildOrDeployedEnv);
                    return data;
                }
                // Continue with workspace-level status - don't return early, we need to fetch ArgoCD data
                log.info("Project {}, Environment {}: Using workspace-level status: {}", projectName, environment, lastBuildOrDeployStatus);
            }
            
            String dbStatus;
            if (deploymentDetails != null && deploymentDetails.getLastDeploymentStatus() != null) {
                dbStatus = deploymentDetails.getLastDeploymentStatus();
            } else if (lastBuildOrDeployStatus != null && environment.equalsIgnoreCase(lastBuildOrDeployedEnv)) {
                // Use workspace-level status when deployment details are null or incomplete
                dbStatus = lastBuildOrDeployStatus;
            } else {
                dbStatus = "UNKNOWN";
            }
            
            // Only populate deployment details if the object exists
            if (deploymentDetails != null) {
                data.put("version", deploymentDetails.getLastDeployedVersion());
                data.put("branch", deploymentDetails.getLastDeployedBranch());
                data.put("deployedOn", deploymentDetails.getLastDeployedOn());
                data.put("deployedBy", deploymentDetails.getLastDeployedBy());
                data.put("deploymentUrl", deploymentDetails.getDeploymentUrl());
                auditLogs = deploymentDetails.getDeploymentAuditLogs();
            } else {
                // deploymentDetails is null, set auditLogs to null
                auditLogs = null;
            }
            DeploymentAudit latestAudit = null;
            if (auditLogs != null && !auditLogs.isEmpty()) {
                latestAudit = auditLogs.stream()
                    .filter(audit -> audit.getTriggeredOn() != null)
                    .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                    .findFirst()
                    .orElse(auditLogs.get(auditLogs.size() - 1));
                    
                data.put("commitId", latestAudit.getCommitId());
                data.put("triggeredBy", latestAudit.getTriggeredBy());
                data.put("triggeredOn", latestAudit.getTriggeredOn());
            }
            if (latestAudit == null) {
                CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
                if (buildDeployEntity != null && buildDeployEntity.getData() != null) {
                    List<DeploymentAudit> buildDeployAuditLogs = "int".equalsIgnoreCase(environment)
                            ? buildDeployEntity.getData().getIntDeploymentAuditLogs()
                            : buildDeployEntity.getData().getProdDeploymentAuditLogs();
                    if (buildDeployAuditLogs != null && !buildDeployAuditLogs.isEmpty()) {
                        latestAudit = buildDeployAuditLogs.stream()
                                .filter(audit -> audit.getTriggeredOn() != null)
                                .max(Comparator.comparing(DeploymentAudit::getTriggeredOn))
                                .orElse(null);
                        if (latestAudit != null) {
                            data.put("commitId", latestAudit.getCommitId());
                            data.put("triggeredBy", latestAudit.getTriggeredBy());
                            data.put("triggeredOn", latestAudit.getTriggeredOn());
                        }
                    }
                }
            }

            String argoHealthStatus = "UNAVAILABLE";
            String argoSyncStatus = "UNAVAILABLE";
            String argoLastSyncPhase = "";
            String confirmedArgoFailure = null;
            boolean imageMatchesDesired = true;
            boolean deploymentEvidenceReady = true;
            String expectedVersion = latestAudit != null ? latestAudit.getVersion() : null;
            if (expectedVersion == null || expectedVersion.isEmpty()) {
                CodeServerBuildDetails buildDetails = "int".equalsIgnoreCase(environment)
                        ? entity.getData().getProjectDetails().getIntBuildDetails()
                        : entity.getData().getProjectDetails().getProdBuildDetails();
                expectedVersion = buildDetails != null ? buildDetails.getVersion() : null;
            }
            Date deployTriggerTime = latestAudit != null ? latestAudit.getTriggeredOn() : null;
            
            try {
                String argoAppName = projectName.toLowerCase() + "-" + environment;
                String token = argoCdService.getArgoToken();
                ResponseEntity<String> argoResponse = argoCdService.getStatusOfArgoApp(token, argoAppName);
                
                if (argoResponse != null && argoResponse.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(argoResponse.getBody());
                    argoHealthStatus = rootNode.path("status").path("health").path("status").asText("");
                    argoSyncStatus = rootNode.path("status").path("sync").path("status").asText("");
                    argoLastSyncPhase = rootNode.path("status").path("operationState").path("phase").asText("");
                    confirmedArgoFailure = argoCdService.getConfirmedDeploymentFailure(
                            rootNode, argoAppName, deployTriggerTime);
                    
                    String argoOperationMessage = rootNode.path("status").path("operationState").path("message").asText("");

                    data.put("argocdHealthStatus", argoHealthStatus);
                    data.put("argocdSyncStatus", argoSyncStatus);
                    data.put("argocdLastSyncPhase", argoLastSyncPhase);
                    data.put("argocdAppUrl", argoCdService.getArgocdBaseUrl() + "/applications/" + argoAppName);
                    if (argoOperationMessage != null && !argoOperationMessage.isEmpty()) {
                        data.put("argocdOperationMessage", argoOperationMessage);
                    }

                    if ("DEPLOY_REQUESTED".equalsIgnoreCase(dbStatus) && deployTriggerTime != null) {
                        imageMatchesDesired = argoCdService.isDesiredImageDeployed(rootNode, argoAppName, expectedVersion);
                        deploymentEvidenceReady = argoCdService.isDeploymentReady(
                                rootNode, argoAppName, expectedVersion, deployTriggerTime);
                    } else {
                        imageMatchesDesired = argoCdService.isDesiredImageDeployed(rootNode, argoAppName);
                        if ("DEPLOY_REQUESTED".equalsIgnoreCase(dbStatus)) {
                            deploymentEvidenceReady = imageMatchesDesired;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch ArgoCD status: {}", e.getMessage());
                data.put("argocdHealthStatus", "UNAVAILABLE");
            }

            // Recognise a user-cancelled deployment via the lastDeploymentError marker. Such a
            // deployment is terminal (DEPLOYMENT_FAILED) and must never be recomputed to DEPLOYING/DEPLOYED,
            // even though the old ReplicaSet pods often keep running healthy on the previous image.
            boolean userCancelled = deploymentDetails != null
                && deploymentDetails.getLastDeploymentError() != null
                && deploymentDetails.getLastDeploymentError().startsWith(USER_CANCELLED_MARKER)
                && "DEPLOYMENT_FAILED".equalsIgnoreCase(dbStatus);
            data.put("userCancelled", userCancelled);

            String argoOperationMsg = (String) data.get("argocdOperationMessage");
            String actualStatus = determineActualStatus(dbStatus, argoHealthStatus, argoSyncStatus, argoLastSyncPhase,
                    argoOperationMsg, confirmedArgoFailure, imageMatchesDesired, deploymentEvidenceReady, userCancelled);
            data.put("currentStatus", actualStatus);

            // While a deployment is in progress, surface crash-loop and stuck-threshold signals
            // so the UI can decide whether to enable the "Cancel Deployment" action.
            data.put("newPodCrashLooping", false);
            data.put("deployingThresholdExceeded", false);
            boolean deploymentInProgress = "DEPLOYING".equals(actualStatus)
                    || "DEPLOY_REQUESTED".equals(actualStatus)
                    || "DEPLOYING".equals(dbStatus)
                    || "DEPLOY_REQUESTED".equals(dbStatus);
            if (deploymentInProgress) {
                try {
                    String argoAppName = projectName.toLowerCase() + "-" + environment;
                    String token = argoCdService.getArgoToken();
                    Map<String, Object> crashStatus = argoCdService.getNewPodCrashLoopStatus(token, argoAppName);
                    data.put("newPodCrashLooping", crashStatus.get("newPodCrashLooping"));
                    if (crashStatus.get("crashLoopReason") != null) {
                        data.put("crashLoopReason", crashStatus.get("crashLoopReason"));
                    }
                } catch (Exception e) {
                    log.debug("Could not evaluate crash-loop status for {}/{}: {}", projectName, environment, e.getMessage());
                }

                Object triggeredOn = data.get("triggeredOn");
                if (triggeredOn instanceof Date) {
                    long elapsedMinutes = (System.currentTimeMillis() - ((Date) triggeredOn).getTime()) / 60000L;
                    data.put("deployingThresholdExceeded", elapsedMinutes >= stuckThresholdMinutes);
                }
            }
            
            if ("DEPLOYMENT_FAILED".equals(actualStatus) || "RESTART_FAILED".equals(actualStatus)) {
                // First check if error is stored in database
                String storedError = deploymentDetails.getLastDeploymentError();
                if (storedError != null && !storedError.isEmpty()) {
                    data.put("errorMessage", storedError);
                } else {
                    // Fall back to live ArgoCD message
                    String errorMsg = (String) data.get("argocdOperationMessage");
                    if (errorMsg != null && !errorMsg.isEmpty()) {
                        data.put("errorMessage", errorMsg);
                    } else {
                        // Generic fallback messages
                        if ("Degraded".equalsIgnoreCase(argoHealthStatus)) {
                            data.put("errorMessage", "Deployment failed: pods are in degraded state. Check pod logs for details.");
                        } else if ("Failed".equalsIgnoreCase(argoLastSyncPhase)) {
                            data.put("errorMessage", "Deployment failed: sync to cluster failed. Check ArgoCD for details.");
                        } else {
                            data.put("errorMessage", "Deployment failed. Check ArgoCD for details.");
                        }
                    }
                }
            }
            
            log.debug("Status for {}-{}: DB={}, ArgoHealth={}, ArgoSync={}, LastSyncPhase={}, Actual={}", 
                projectName, environment, dbStatus, argoHealthStatus, argoSyncStatus, argoLastSyncPhase, actualStatus);

        } catch (Exception e) {
            log.error("Error fetching deployment status for {}/{}: {}", projectName, environment, e.getMessage());
            data.put("currentStatus", "ERROR");
            data.put("message", "Failed to fetch deployment status: " + e.getMessage());
        }

        return data;
    }
    
    private String determineActualStatus(String dbStatus, String argoHealth, String syncStatus, String lastSyncPhase,
                                         String operationMessage, String confirmedFailureMessage,
                                         boolean imageMatchesDesired,
                                         boolean deploymentEvidenceReady, boolean userCancelled) {
        // A user-cancelled deployment is terminal. Never recompute it to DEPLOYING/DEPLOYED even
        // if ArgoCD still reports the old ReplicaSet pods as Healthy on the previous image.
        if (userCancelled) {
            return "DEPLOYMENT_FAILED";
        }

        if ("UNAVAILABLE".equals(argoHealth) || argoHealth == null || argoHealth.isEmpty()) {
            return dbStatus;
        }
        
        if (confirmedFailureMessage != null) {
            return "DEPLOYMENT_FAILED";
        }
        
        if ("Healthy".equalsIgnoreCase(argoHealth)) {
            if ("DEPLOY_REQUESTED".equalsIgnoreCase(dbStatus)) {
                if (!deploymentEvidenceReady) {
                    log.info("ArgoCD is Healthy but new sync/image evidence is incomplete - treating as DEPLOYING");
                    return "DEPLOYING";
                }
                return "DEPLOYED";
            }
            if ("BUILDING".equalsIgnoreCase(dbStatus)) {
                return dbStatus;
            }
            if (!imageMatchesDesired) {
                log.info("ArgoCD is Healthy but running stale image - treating as DEPLOYING (waiting for new sync)");
                return "DEPLOYING";
            }
            return "DEPLOYED";
        }
        
        return dbStatus != null ? dbStatus : "DEPLOY_REQUESTED";
    }

    @GetMapping(value = "/workspace/deployment/syncerror/{projectName}/{environment}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getSyncError(
            @PathVariable String projectName,
            @PathVariable String environment) {
        Map<String, String> result = new HashMap<>();
        try {
            // First check if error is stored in DB
            CodeServerWorkspaceNsql entity = workspaceRepository.findbyProjectName(projectName);
            if (entity != null && entity.getData() != null && entity.getData().getProjectDetails() != null) {
                CodeServerDeploymentDetails details = "int".equalsIgnoreCase(environment)
                        ? entity.getData().getProjectDetails().getIntDeploymentDetails()
                        : entity.getData().getProjectDetails().getProdDeploymentDetails();
                if (details != null && details.getLastDeploymentError() != null && !details.getLastDeploymentError().isEmpty()) {
                    result.put("errorMessage", details.getLastDeploymentError());
                    return ResponseEntity.ok(result);
                }
            }

            // Fallback: fetch from ArgoCD
            String argoAppName = projectName.toLowerCase() + "-" + environment;
            String token = argoCdService.getArgoToken();
            ResponseEntity<String> argoResponse = argoCdService.getStatusOfArgoApp(token, argoAppName);
            if (argoResponse != null && argoResponse.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(argoResponse.getBody());
                String message = rootNode.path("status").path("operationState").path("message").asText("");
                if (!message.isEmpty()) {
                    result.put("errorMessage", message);
                    return ResponseEntity.ok(result);
                }
            }
            result.put("errorMessage", "No error details available");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching sync error for {}/{}: {}", projectName, environment, e.getMessage());
            result.put("errorMessage", "Failed to fetch error details");
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Streams container logs from ONLY the pods running the version currently being deployed
     * (matched by desired image tag), never the old/previous version's pods. Event names match
     * the frontend contract: {@code pod-info}, {@code pod-logs}, {@code pod-logs-complete},
     * {@code pod-logs-error}.
     */
    @GetMapping(value = "/workspace/deployment/podlogs/stream/{projectName}/{environment}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPodLogs(
            @PathVariable String projectName,
            @PathVariable String environment) {

        log.info("Starting pod-logs SSE stream for project: {}, environment: {}", projectName, environment);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String appName = projectName.toLowerCase() + "-" + environment;

        final AtomicBoolean active = new AtomicBoolean(true);
        final List<HttpURLConnection> openConnections = Collections.synchronizedList(new ArrayList<>());
        final Object sendLock = new Object();

        Runnable cleanup = () -> {
            active.set(false);
            synchronized (openConnections) {
                for (HttpURLConnection c : openConnections) {
                    try { c.disconnect(); } catch (Exception ignore) { /* best effort */ }
                }
            }
        };
        emitter.onCompletion(cleanup::run);
        emitter.onTimeout(() -> { cleanup.run(); emitter.complete(); });
        emitter.onError((ex) -> cleanup.run());

        executor.execute(() -> {
            try {
                String token = argoCdService.getArgoToken();

                // Wait briefly for the new-version pod(s) to appear (image pull / scheduling).
                List<ArgoCdService.PodInfo> pods = new ArrayList<>();
                boolean fallbackSelection = false;
                boolean resourceTreeAvailable = false;
                for (int attempt = 0; attempt < 10 && active.get(); attempt++) {
                    ArgoCdService.PodSelectionResult selection =
                            argoCdService.getNewVersionPodSelection(token, appName);
                    pods = selection.getPods();
                    fallbackSelection = selection.isFallbackSelection();
                    resourceTreeAvailable = selection.isResourceTreeAvailable();
                    if (!pods.isEmpty()) {
                        break;
                    }
                    Thread.sleep(3000);
                }

                if (pods.isEmpty()) {
                    synchronized (sendLock) {
                        Map<String, Object> err = new HashMap<>();
                        err.put("message", resourceTreeAvailable
                                ? "No managed pods found for the version being deployed yet"
                                : "Could not read the ArgoCD resource tree");
                        err.put("projectName", projectName);
                        err.put("environment", environment);
                        emitter.send(SseEmitter.event().name("pod-logs-error")
                                .data(objectMapper.writeValueAsString(err)));
                    }
                    emitter.complete();
                    return;
                }

                List<String> podNames = new ArrayList<>();
                for (ArgoCdService.PodInfo pod : pods) {
                    podNames.add(pod.getPodName());
                }
                synchronized (sendLock) {
                    Map<String, Object> podInfo = new HashMap<>();
                    podInfo.put("pods", podNames);
                    podInfo.put("projectName", projectName);
                    podInfo.put("environment", environment);
                    podInfo.put("podsSelectedByFallback", fallbackSelection);
                    emitter.send(SseEmitter.event().name("pod-info")
                            .data(objectMapper.writeValueAsString(podInfo)));
                }

                List<Thread> streamThreads = new ArrayList<>();
                for (String podName : podNames) {
                    Thread t = new Thread(() -> {
                        try {
                            argoCdService.streamPodLogs(token, appName, podName, environment, active,
                                openConnections::add,
                                (logLine) -> {
                                    try {
                                        synchronized (sendLock) {
                                            Map<String, Object> logData = new HashMap<>();
                                            logData.put("podName", podName);
                                            logData.put("content", logLine);
                                            emitter.send(SseEmitter.event().name("pod-logs")
                                                    .data(objectMapper.writeValueAsString(logData)));
                                        }
                                    } catch (IOException ioe) {
                                        active.set(false);
                                    }
                                });
                        } catch (Exception e) {
                            log.debug("Pod log stream ended for {} ({}): {}", podName, appName, e.getMessage());
                        }
                    });
                    t.setDaemon(true);
                    streamThreads.add(t);
                    t.start();
                }

                for (Thread t : streamThreads) {
                    t.join();
                }

                synchronized (sendLock) {
                    Map<String, Object> complete = new HashMap<>();
                    complete.put("message", "Log stream ended");
                    complete.put("projectName", projectName);
                    complete.put("environment", environment);
                    emitter.send(SseEmitter.event().name("pod-logs-complete")
                            .data(objectMapper.writeValueAsString(complete)));
                }
                emitter.complete();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                cleanup.run();
                emitter.complete();
            } catch (Exception e) {
                log.error("Error in pod-logs SSE stream for {}/{}: {}", projectName, environment, e.getMessage());
                try {
                    synchronized (sendLock) {
                        Map<String, Object> err = new HashMap<>();
                        err.put("message", "Failed to stream pod logs: " + e.getMessage());
                        emitter.send(SseEmitter.event().name("pod-logs-error")
                                .data(objectMapper.writeValueAsString(err)));
                    }
                } catch (Exception ignore) { /* client likely gone */ }
                cleanup.run();
                emitter.complete();
            }
        });

        return emitter;
    }

    /**
     * Cancels an in-progress deployment: terminates the ArgoCD sync operation and persists the
     * deployment as DEPLOYMENT_FAILED using the same mechanism as any other failure path, tagging
     * the error with {@link #USER_CANCELLED_MARKER} so reconcilers keep it terminal.
     */
    @DeleteMapping(value = "/workspace/deployment/cancel/{projectName}/{environment}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> cancelDeployment(
            @PathVariable String projectName,
            @PathVariable String environment) {
        Map<String, String> result = new HashMap<>();
        try {
            if (!"int".equalsIgnoreCase(environment) && !"prod".equalsIgnoreCase(environment)) {
                result.put("status", "FAILED");
                result.put("message", "Invalid environment. Use 'int' or 'prod'");
                return ResponseEntity.badRequest().body(result);
            }

            CodeServerWorkspaceNsql entity = workspaceRepository.findbyProjectName(projectName);
            if (entity == null || entity.getData() == null || entity.getData().getProjectDetails() == null) {
                result.put("status", "FAILED");
                result.put("message", "No deployment found for project: " + projectName);
                return ResponseEntity.status(404).body(result);
            }

            CodeServerDeploymentDetails deploymentDetails = "int".equalsIgnoreCase(environment)
                    ? entity.getData().getProjectDetails().getIntDeploymentDetails()
                    : entity.getData().getProjectDetails().getProdDeploymentDetails();
            if (deploymentDetails == null) {
                result.put("status", "FAILED");
                result.put("message", "No deployment details found for environment: " + environment);
                return ResponseEntity.status(404).body(result);
            }
            String storedProjectName = entity.getData().getProjectDetails().getProjectName();

            String argoAppName = projectName.toLowerCase() + "-" + environment;
            String token = argoCdService.getArgoToken();
            String terminateResult = argoCdService.terminateOperation(token, argoAppName);
            log.info("Cancel deployment for {}: ArgoCD terminate result={}", argoAppName, terminateResult);

            java.util.Date cancelledOn = new java.util.Date();
            GenericMessage workspaceUpdate = workspaceRepository.updateCancelledDeploymentStatus(
                    storedProjectName,
                    environment,
                    "DEPLOYMENT_FAILED",
                    USER_CANCELLED_MARKER,
                    cancelledOn);
            result.put("argoTerminateResult", terminateResult);
            result.put("workspaceUpdateStatus",
                    workspaceUpdate == null ? "FAILED" : workspaceUpdate.getSuccess());
            if (workspaceUpdate == null || !"SUCCESS".equalsIgnoreCase(workspaceUpdate.getSuccess())) {
                result.put("status", "FAILED");
                result.put("message", "Failed to persist deployment cancellation");
                return ResponseEntity.status(500).body(result);
            }

            updateBuildDeployAuditToCancelled(projectName, environment);

            result.put("status", "SUCCESS");
            result.put("message", "Deployment cancelled; ArgoCD terminate result: " + terminateResult);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to cancel deployment for {}/{}: {}", projectName, environment, e.getMessage(), e);
            result.put("status", "FAILED");
            result.put("message", "Failed to cancel deployment: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /** Marks the latest in-progress deployment audit entry as DEPLOYMENT_FAILED (mirrors DeploymentStatusMonitorJob). */
    private void updateBuildDeployAuditToCancelled(String projectName, String environment) {
        try {
            CodeServerBuildDeployNsql buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
            if (buildDeployEntity == null || buildDeployEntity.getData() == null) {
                return;
            }
            CodeServerBuildDeploy data = buildDeployEntity.getData();
            List<DeploymentAudit> auditLogs = "int".equalsIgnoreCase(environment)
                    ? data.getIntDeploymentAuditLogs()
                    : data.getProdDeploymentAuditLogs();
            if (auditLogs == null || auditLogs.isEmpty()) {
                return;
            }
            for (int i = auditLogs.size() - 1; i >= 0; i--) {
                String auditStatus = auditLogs.get(i).getDeploymentStatus();
                if ("DEPLOY_REQUESTED".equalsIgnoreCase(auditStatus) || "DEPLOYING".equalsIgnoreCase(auditStatus)
                        || "APPROVAL_PENDING".equalsIgnoreCase(auditStatus)) {
                    auditLogs.get(i).setDeploymentStatus("DEPLOYMENT_FAILED");
                    buildDeployEntity.setData(data);
                    buildDeployRepo.save(buildDeployEntity);
                    log.info("Marked deployment audit entry as DEPLOYMENT_FAILED (user cancel) for {}-{} at index {}", projectName, environment, i);
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update build deploy audit log on cancel for {}-{}: {}", projectName, environment, e.getMessage());
        }
    }
}
