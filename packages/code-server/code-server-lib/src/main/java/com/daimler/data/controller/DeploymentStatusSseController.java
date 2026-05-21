package com.daimler.data.controller;

import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.service.ArgoCdService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api")
public class DeploymentStatusSseController {

    @Autowired
    private WorkspaceCustomRepository workspaceRepository;

    @Autowired
    private ArgoCdService argoCdService;

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
                        
                        // Track if we've seen an in-progress state (proves new sync has started)
                        if ("DEPLOYING".equals(status)) {
                            seenInProgress = true;
                        }
                        
                        if ("DEPLOYED".equals(status) || "FAILED".equals(status)) {
                            // Only accept terminal status if we've seen the deployment actually start,
                            // or enough time has passed to rule out stale ArgoCD state
                            if (seenInProgress || iteration >= minIterationsBeforeTerminal) {
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

            if (deploymentDetails == null) {
                data.put("currentStatus", "NO_DEPLOYMENT");
                data.put("message", "No deployment details found for environment: " + environment);
                return data;
            }
            
            String dbStatus = deploymentDetails.getLastDeploymentStatus() != null ? 
                     deploymentDetails.getLastDeploymentStatus() : "UNKNOWN";
            
            data.put("version", deploymentDetails.getLastDeployedVersion());
            data.put("branch", deploymentDetails.getLastDeployedBranch());
            data.put("deployedOn", deploymentDetails.getLastDeployedOn());
            data.put("deployedBy", deploymentDetails.getLastDeployedBy());
            data.put("deploymentUrl", deploymentDetails.getDeploymentUrl());
            
            auditLogs = deploymentDetails.getDeploymentAuditLogs();
            if (auditLogs != null && !auditLogs.isEmpty()) {
                DeploymentAudit latestAudit = auditLogs.stream()
                    .filter(audit -> audit.getTriggeredOn() != null)
                    .sorted((a1, a2) -> a2.getTriggeredOn().compareTo(a1.getTriggeredOn()))
                    .findFirst()
                    .orElse(auditLogs.get(auditLogs.size() - 1));
                    
                data.put("commitId", latestAudit.getCommitId());
                data.put("triggeredBy", latestAudit.getTriggeredBy());
                data.put("triggeredOn", latestAudit.getTriggeredOn());
            }

            String argoHealthStatus = "UNAVAILABLE";
            String argoSyncStatus = "UNAVAILABLE";
            String argoLastSyncPhase = "";
            
            try {
                String argoAppName = projectName + "-" + environment;
                String token = argoCdService.getArgoToken();
                ResponseEntity<String> argoResponse = argoCdService.getStatusOfArgoApp(token, argoAppName);
                
                if (argoResponse != null && argoResponse.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(argoResponse.getBody());
                    argoHealthStatus = rootNode.path("status").path("health").path("status").asText("");
                    argoSyncStatus = rootNode.path("status").path("sync").path("status").asText("");
                    argoLastSyncPhase = rootNode.path("status").path("operationState").path("phase").asText("");
                    
                    data.put("argocdHealthStatus", argoHealthStatus);
                    data.put("argocdSyncStatus", argoSyncStatus);
                    data.put("argocdLastSyncPhase", argoLastSyncPhase);
                    data.put("argocdAppUrl", argoCdService.getArgocdBaseUrl() + "/applications/" + argoAppName);
                }
            } catch (Exception e) {
                log.debug("Could not fetch ArgoCD status: {}", e.getMessage());
                data.put("argocdHealthStatus", "UNAVAILABLE");
            }

            String actualStatus = determineActualStatus(dbStatus, argoHealthStatus, argoLastSyncPhase);
            data.put("currentStatus", actualStatus);
            
            log.debug("Status for {}-{}: DB={}, ArgoHealth={}, ArgoSync={}, LastSyncPhase={}, Actual={}", 
                projectName, environment, dbStatus, argoHealthStatus, argoSyncStatus, argoLastSyncPhase, actualStatus);

        } catch (Exception e) {
            log.error("Error fetching deployment status for {}/{}: {}", projectName, environment, e.getMessage());
            data.put("currentStatus", "ERROR");
            data.put("message", "Failed to fetch deployment status: " + e.getMessage());
        }

        return data;
    }
    
    private String determineActualStatus(String dbStatus, String argoHealth, String lastSyncPhase) {
        if ("DEPLOYED".equalsIgnoreCase(dbStatus) || "FAILED".equalsIgnoreCase(dbStatus)) {
            log.debug("Using terminal DB status: {}", dbStatus);
            return dbStatus;
        }
        
        if ("UNAVAILABLE".equals(argoHealth) || argoHealth == null || argoHealth.isEmpty()) {
            return dbStatus;
        }
        
        // DEPLOYED: only when BOTH health=Healthy AND lastSyncPhase=Succeeded
        if ("Healthy".equalsIgnoreCase(argoHealth) && "Succeeded".equalsIgnoreCase(lastSyncPhase)) {
            return "DEPLOYED";
        }
        
        // DEPLOYING: sync is still running OR health is progressing (actively working)
        if ("Running".equalsIgnoreCase(lastSyncPhase) || "Progressing".equalsIgnoreCase(argoHealth)) {
            return "DEPLOYING";
        }
        
        // DEPLOYING: no sync phase yet (empty/null) means sync hasn't started
        if (lastSyncPhase == null || lastSyncPhase.isEmpty()) {
            return dbStatus;
        }
        
        // Everything else = FAILED
        return "FAILED";
    }

    @GetMapping(value = "/workspace/deployment/podlogs/stream/{projectName}/{environment}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPodLogs(
            @PathVariable String projectName,
            @PathVariable String environment,
            @RequestParam(defaultValue = "300") int sinceSeconds) {

        log.info("Starting pod logs SSE stream for project: {}, environment: {}", projectName, environment);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            int maxIterations = 200;
            int iteration = 0;
            Set<String> sentLogLines = new HashSet<>();

            try {
                String[] tokenHolder = new String[]{ argoCdService.getArgoToken() };
                String appName = projectName + "-" + environment;

                // Step 1: Get pods for this app
                List<Map<String, String>> pods = argoCdService.getAppPods(tokenHolder[0], appName);

                if (pods.isEmpty()) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("appName", appName);
                    errorData.put("message", "No pods found for application: " + appName);
                    emitter.send(SseEmitter.event()
                            .name("pod-logs-error")
                            .data(objectMapper.writeValueAsString(errorData)));
                    emitter.complete();
                    return;
                }

                // Send pod info
                Map<String, Object> podInfoData = new HashMap<>();
                podInfoData.put("appName", appName);
                podInfoData.put("pods", pods);
                emitter.send(SseEmitter.event()
                        .name("pod-info")
                        .data(objectMapper.writeValueAsString(podInfoData)));

                // Step 2: Poll for logs
                while (iteration < maxIterations) {
                    iteration++;
                    try {
                        // Refresh token periodically (every 50 iterations)
                        if (iteration % 50 == 0) {
                            tokenHolder[0] = argoCdService.getArgoToken();
                        }

                        for (Map<String, String> pod : pods) {
                            String podName = pod.get("name");
                            String namespace = pod.get("namespace");

                            List<String> logLines = argoCdService.getPodLogs(tokenHolder[0], appName, podName, namespace, sinceSeconds);

                            // Filter out already-sent lines
                            List<String> newLines = new ArrayList<>();
                            for (String line : logLines) {
                                if (sentLogLines.add(line)) {
                                    newLines.add(line);
                                }
                            }

                            if (!newLines.isEmpty()) {
                                Map<String, Object> logData = new HashMap<>();
                                logData.put("podName", podName);
                                logData.put("logs", newLines);
                                logData.put("timestamp", new Date());
                                emitter.send(SseEmitter.event()
                                        .name("pod-logs")
                                        .data(objectMapper.writeValueAsString(logData)));
                            }
                        }

                        Thread.sleep(5000);

                    } catch (IOException e) {
                        log.warn("Client disconnected from pod logs SSE stream at iteration {}", iteration);
                        emitter.completeWithError(e);
                        return;
                    } catch (InterruptedException e) {
                        log.warn("Pod logs SSE stream interrupted at iteration {}", iteration);
                        Thread.currentThread().interrupt();
                        emitter.completeWithError(e);
                        return;
                    }
                }

                // Timeout
                Map<String, Object> completeData = new HashMap<>();
                completeData.put("appName", appName);
                completeData.put("message", "Log streaming completed");
                emitter.send(SseEmitter.event()
                        .name("pod-logs-complete")
                        .data(objectMapper.writeValueAsString(completeData)));
                emitter.complete();
                log.info("Pod logs SSE stream completed after {} iterations for {}", iteration, appName);

            } catch (Exception e) {
                log.error("Error in pod logs SSE stream for {}-{}: {}", projectName, environment, e.getMessage());
                try {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("appName", projectName + "-" + environment);
                    errorData.put("message", "Failed to fetch pod logs: " + e.getMessage());
                    emitter.send(SseEmitter.event()
                            .name("pod-logs-error")
                            .data(objectMapper.writeValueAsString(errorData)));
                } catch (IOException ignored) {
                    // Client already disconnected
                }
                emitter.completeWithError(e);
            }
        });

        emitter.onCompletion(() -> log.info("Pod logs SSE emitter completed for {}/{}", projectName, environment));
        emitter.onTimeout(() -> log.warn("Pod logs SSE emitter timeout for {}/{}", projectName, environment));
        emitter.onError((ex) -> log.error("Pod logs SSE emitter error for {}/{}", projectName, environment, ex));

        return emitter;
    }
}
