package com.daimler.data.controller;
 
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
 
import com.daimler.data.dto.WorkflowStatusDto;
import com.daimler.data.service.workspace.WorkflowStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import lombok.extern.slf4j.Slf4j;
 
/**
 * Serves the aggregated Build &amp; Deploy workflow status for the
 * Deployment Status Panel (Info icon). Complements the existing
 * {@code DeploymentStatusSseController} (which streams ArgoCD deploy health)
 * by exposing the GitHub Actions run/jobs/steps view + the queue-before-runId
 * window.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class WorkflowStatusController {
 
    @Autowired
    private WorkflowStatusService workflowStatusService;
 
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    /** One-shot aggregate — used on panel open and as the client polling fallback. */
    @GetMapping(value = "/workspace/workflow-status/{projectName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowStatusDto> getWorkflowStatus(@PathVariable String projectName) {
        return ResponseEntity.ok(workflowStatusService.getWorkflowStatus(projectName));
    }
 
    /** Near-real-time push. Emits {@code workflow-status} events until terminal / timeout. */
    @GetMapping(value = "/workspace/workflow-status/stream/{projectName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamWorkflowStatus(@PathVariable String projectName) {
        log.info("Starting workflow-status SSE stream for project: {}", projectName);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
 
        executor.execute(() -> {
            int iteration = 0;
            int maxIterations = 450;   // ~30 min at 4s
            int errorCount = 0;
            int maxErrors = 5;
            try {
                while (iteration < maxIterations) {
                    iteration++;
                    try {
                        WorkflowStatusDto data = workflowStatusService.getWorkflowStatus(projectName);
                        emitter.send(SseEmitter.event()
                                .name("workflow-status")
                                .data(objectMapper.writeValueAsString(data)));
 
                        String phase = data.getPhase();
                        if ("DONE".equals(phase) || "FAILED".equals(phase) || "NONE".equals(phase)) {
                            emitter.send(SseEmitter.event()
                                    .name("workflow-complete")
                                    .data(objectMapper.writeValueAsString(data)));
                            break;
                        }
                        errorCount = 0;
                        Thread.sleep(4000);
                    } catch (IOException e) {
                        log.warn("Client disconnected from workflow-status SSE for {} at iteration {}", projectName, iteration);
                        emitter.completeWithError(e);
                        return;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        emitter.completeWithError(e);
                        return;
                    } catch (Exception e) {
                        errorCount++;
                        log.warn("workflow-status SSE error {}/{} for {}: {}", errorCount, maxErrors, projectName, e.getMessage());
                        if (errorCount >= maxErrors) {
                            break;
                        }
                        Thread.sleep(4000);
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("Error in workflow-status SSE stream for {}", projectName, e);
                emitter.completeWithError(e);
            }
        });
 
        emitter.onCompletion(() -> log.info("workflow-status SSE completed for {}", projectName));
        emitter.onTimeout(() -> log.warn("workflow-status SSE timeout for {}", projectName));
        emitter.onError((ex) -> log.error("workflow-status SSE error for {}", projectName, ex));
        return emitter;
    }
}