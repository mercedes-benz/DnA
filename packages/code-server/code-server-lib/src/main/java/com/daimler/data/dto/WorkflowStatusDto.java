package com.daimler.data.dto;
 
import java.io.Serializable;
import java.util.Date;
import java.util.List;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
/**
 * Aggregated, UI-ready view of a CodeSpace Build &amp; Deploy for the
 * real-time Deployment Status Panel. It merges the platform state machine
 * ({@code lastBuildOrDeployedStatus}), the GitHub Actions workflow run and its
 * jobs/steps, and safe deep-links. The GitHub PAT never leaves the backend;
 * only derived data + links reach the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WorkflowStatusDto implements Serializable {
 
    /** state machine value: BUILD_REQUESTED, BUILD_SUCCESS, DEPLOYED, ... */
    private String overallStatus;
    /** coarse phase for the UI stepper: QUEUED, BUILDING, DEPLOYING, RECONCILING, DONE, FAILED */
    private String phase;
    /** false while status is *_REQUESTED and no GitHub run id exists yet (Queue state) */
    private boolean runStarted;
 
    private String projectName;
    private String environment;
 
    private String workflowName;
    private String runId;
    private String runNumber;
    private String branch;
    private String commitSha;
    private String triggeredBy;
    private String repository;
    private Date startedAt;
    private Date updatedAt;
    private Long elapsedSeconds;
 
    private List<JobVO> jobs;
    private List<ActivityVO> activity;
 
    private String githubActionUrl;
 
    private boolean stale;
    private String lastError;
    /** human-readable hint, e.g. queue / timeout messages */
    private String message;
 
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class JobVO implements Serializable {
        private Long id;
        private String name;
        private String status;
        private String conclusion;
        private int completedSteps;
        private int totalSteps;
        private int progress;
        private String currentStep;
        private Long durationSeconds;
        private String htmlUrl;
        private List<StepVO> steps;
    }
 
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class StepVO implements Serializable {
        private String name;
        private String status;
        private String conclusion;
        private int number;
        private Long durationSeconds;
    }
 
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class ActivityVO implements Serializable {
        private Date ts;
        private String message;
    }
}