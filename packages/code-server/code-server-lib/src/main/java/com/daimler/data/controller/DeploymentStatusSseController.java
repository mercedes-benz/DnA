package com.daimler.data.controller;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkspaceCustomBuildDeployRepo;
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
    private WorkspaceCustomBuildDeployRepo buildDeployRepo;

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
            try {
                while (true) {
                    try {
                        Map<String, Object> statusData = getDeploymentStatusData(projectName, environment);
                        
                        emitter.send(SseEmitter.event()
                                .name("deployment-status")
                                .data(objectMapper.writeValueAsString(statusData)));

                        String status = (String) statusData.get("currentStatus");
                        if ("DEPLOYED".equals(status) || "FAILED".equals(status) || "ERROR".equals(status)) {
                            log.info("Deployment finished with status: {}", status);
                            emitter.send(SseEmitter.event()
                                    .name("deployment-complete")
                                    .data(objectMapper.writeValueAsString(statusData)));
                            break;
                        }

                        Thread.sleep(3000);

                    } catch (IOException e) {
                        log.warn("Client disconnected from SSE stream");
                        emitter.completeWithError(e);
                        return;
                    } catch (InterruptedException e) {
                        log.warn("SSE stream interrupted");
                        Thread.currentThread().interrupt();
                        emitter.completeWithError(e);
                        return;
                    }
                }
                emitter.complete();
                log.info("SSE stream completed successfully");

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
            CodeServerBuildDeployNsql entity = buildDeployRepo.findByProjectName(projectName);

            if (entity == null) {
                data.put("currentStatus", "NOT_FOUND");
                data.put("message", "No deployment data found for project: " + projectName);
                return data;
            }
            List<DeploymentAudit> auditLogs;

            if ("int".equalsIgnoreCase(environment)) {
                auditLogs = entity.getData().getIntDeploymentAuditLogs();
            } else if ("prod".equalsIgnoreCase(environment)) {
                auditLogs = entity.getData().getProdDeploymentAuditLogs();
            } else {
                data.put("currentStatus", "ERROR");
                data.put("message", "Invalid environment. Use 'int' or 'prod'");
                return data;
            }

            if (auditLogs == null || auditLogs.isEmpty()) {
                data.put("currentStatus", "NO_DEPLOYMENT");
                data.put("message", "No deployment history found");
                return data;
            }

            DeploymentAudit latestAudit = auditLogs.get(auditLogs.size() - 1);

            data.put("currentStatus", latestAudit.getDeploymentStatus() != null ? 
                     latestAudit.getDeploymentStatus() : "UNKNOWN");
            data.put("version", latestAudit.getVersion());
            data.put("branch", latestAudit.getBranch());
            data.put("commitId", latestAudit.getCommitId());
            data.put("triggeredBy", latestAudit.getTriggeredBy());
            data.put("triggeredOn", latestAudit.getTriggeredOn());
            data.put("deployedOn", latestAudit.getDeployedOn());

            try {
                String argoAppName = projectName + "-" + environment;
                String token = argoCdService.getArgoToken();
                ResponseEntity<String> argoResponse = argoCdService.getStatusOfArgoApp(token, argoAppName);
                
                if (argoResponse != null && argoResponse.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(argoResponse.getBody());
                    String healthStatus = rootNode.path("status").path("health").path("status").asText("");
                    String syncStatus = rootNode.path("status").path("sync").path("status").asText("");
                    
                    data.put("argocdHealthStatus", healthStatus);
                    data.put("argocdSyncStatus", syncStatus);
                    data.put("argocdAppUrl", argoCdService.getArgocdBaseUrl() + "/applications/" + argoAppName);
                } else {
                    data.put("argocdHealthStatus", "UNAVAILABLE");
                }
            } catch (Exception e) {
                log.debug("Could not fetch ArgoCD status: {}", e.getMessage());
                data.put("argocdHealthStatus", "UNAVAILABLE");
            }

        } catch (Exception e) {
            log.error("Error fetching deployment status for {}/{}: {}", projectName, environment, e.getMessage());
            data.put("currentStatus", "ERROR");
            data.put("message", "Failed to fetch deployment status: " + e.getMessage());
        }

        return data;
    }
}
