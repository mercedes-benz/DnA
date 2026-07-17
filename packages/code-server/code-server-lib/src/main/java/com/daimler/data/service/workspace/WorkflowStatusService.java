package com.daimler.data.service.workspace;
 
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.daimler.data.application.client.GitClient;
import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.BuildAudit;
import com.daimler.data.db.json.CodeServerBuildDetails;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkspaceCustomBuildDeployRepo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.dto.GitHubWorkflowJobsResponseDto;
import com.daimler.data.dto.GitHubWorkflowRunDto;
import com.daimler.data.dto.GitRunIdDetailsDto;
import com.daimler.data.dto.workspace.WorkflowActivityVO;
import com.daimler.data.dto.workspace.WorkflowJobVO;
import com.daimler.data.dto.workspace.WorkflowStatusVO;
import com.daimler.data.dto.workspace.WorkflowStepVO;
 
import lombok.extern.slf4j.Slf4j;
 
/**
 * Builds the aggregated {@link WorkflowStatusVO} for the Deployment Status Panel.
 * <p>
 * Source of truth is the existing platform state machine (workspace_nsql) plus
 * the GitHub Actions run/jobs/steps. This service does NOT introduce a new
 * tracking mechanism — it aggregates what the CI callbacks
 * ({@code getGitJobRunId}/{@code updateWorkspace}) and the existing GitHub
 * reconciliation already record, and is invoked from {@code getById} only when
 * a refresh is triggered by the user.
 */
@Service
@Slf4j
public class WorkflowStatusService {
 
    @Autowired
    private WorkspaceCustomRepository workspaceCustomRepository;
 
    @Autowired
    private WorkspaceCustomBuildDeployRepo buildDeployCustomRepo;
 
    @Autowired
    private GitClient gitClient;
 
    public WorkflowStatusVO getWorkflowStatus(String projectName) {
        WorkflowStatusVO vo = new WorkflowStatusVO();
        vo.setProjectName(projectName);
 
        GitRunIdDetailsDto dto = workspaceCustomRepository.getGitRunId(projectName);
        if (dto == null || dto.getStatus() == null) {
            vo.setOverallStatus("NONE");
            vo.setPhase("NONE");
            vo.setRunStarted(false);
            vo.setMessage("No build or deploy activity found for this workspace.");
            return vo;
        }
 
        String status = dto.getStatus();
        String environment = dto.getEnvironment();
        String runId = dto.getGitjobRunId();
        boolean runStarted = runId != null && !runId.isBlank();
 
        vo.setOverallStatus(status);
        vo.setEnvironment(environment);
        vo.setRunId(runId);
        vo.setRunStarted(runStarted);
        vo.setPhase(resolvePhase(status, runStarted));
 
        // Enrich from DB (branch/commit/version/triggeredBy + activity feed + last error)
        populateFromDb(vo, projectName, environment, status, dto);
 
        // Queue state: request created but GitHub has not produced a run id yet.
        if (!runStarted) {
            long waitedMin = dto.getLastBuildOrDeployedOn() == null ? 0
                    : Duration.between(dto.getLastBuildOrDeployedOn().toInstant(), Instant.now()).toMinutes();
            vo.setMessage("Build request received. Waiting for GitHub to start the workflow… (queued "
                    + waitedMin + " min). The build has not started yet.");
            return vo;
        }
 
        // Run id exists → enrich with live GitHub run + jobs/steps.
        try {
            GitHubWorkflowRunDto run = gitClient.getWorkflowRun(runId);
            if (run != null) {
                if (run.getName() != null) {
                    vo.setWorkflowName(run.getName());
                }
                if (run.getRunNumber() != null) {
                    vo.setRunNumber(String.valueOf(run.getRunNumber()));
                }
                if (run.getHeadBranch() != null) {
                    vo.setBranch(run.getHeadBranch());
                }
                if (run.getHeadSha() != null) {
                    vo.setCommitSha(run.getHeadSha());
                }
                if (run.getHtmlUrl() != null) {
                    vo.setGithubActionUrl(run.getHtmlUrl());
                }
                Date started = run.getRunStartedAt() != null ? run.getRunStartedAt() : run.getCreatedAt();
                if (started != null) {
                    vo.setStartedAt(started);
                }
                vo.setUpdatedAt(run.getUpdatedAt());
                if (started != null) {
                    Date end = isTerminal(status) && run.getUpdatedAt() != null ? run.getUpdatedAt() : new Date();
                    vo.setElapsedSeconds(Math.max(0, (end.getTime() - started.getTime()) / 1000));
                }
            } else {
                vo.setStale(true);
            }
 
            GitHubWorkflowJobsResponseDto jobsResponse = gitClient.getBuildDeployJob(runId);
            if (jobsResponse != null && jobsResponse.getJobs() != null) {
                vo.setJobs(mapJobs(jobsResponse.getJobs()));
            } else if (run == null) {
                vo.setStale(true);
                vo.setMessage("Live GitHub status is temporarily unavailable. Showing last known state.");
            }
        } catch (Exception e) {
            log.warn("Failed to enrich workflow status from GitHub for project {} runId {}: {}",
                    projectName, runId, e.getMessage());
            vo.setStale(true);
            vo.setMessage("Live GitHub status is temporarily unavailable. Showing last known state.");
        }
 
        return vo;
    }
 
    private void populateFromDb(WorkflowStatusVO vo, String projectName, String environment,
                                String status, GitRunIdDetailsDto dto) {
        boolean isInt = !"prod".equalsIgnoreCase(environment);
        try {
            CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyProjectName(projectName);
            if (entity != null && entity.getData() != null && entity.getData().getProjectDetails() != null) {
                vo.setRepository(safeString(entity.getData().getProjectDetails().getGitRepoName()));
                CodeServerBuildDetails build = isInt
                        ? entity.getData().getProjectDetails().getIntBuildDetails()
                        : entity.getData().getProjectDetails().getProdBuildDetails();
                CodeServerDeploymentDetails deploy = isInt
                        ? entity.getData().getProjectDetails().getIntDeploymentDetails()
                        : entity.getData().getProjectDetails().getProdDeploymentDetails();
 
                if (build != null && build.getLastBuildBranch() != null) {
                    vo.setBranch(build.getLastBuildBranch());
                }
                if (deploy != null && deploy.getLastDeploymentError() != null) {
                    vo.setLastError(deploy.getLastDeploymentError());
                }
            }
        } catch (Exception e) {
            log.debug("Could not read workspace details for {}: {}", projectName, e.getMessage());
        }
 
        // Activity feed + fallback branch/commit/triggeredBy from audit history.
        try {
            CodeServerBuildDeployNsql bd = buildDeployCustomRepo.findByProjectName(projectName);
            if (bd != null && bd.getData() != null) {
                List<WorkflowActivityVO> activity = new ArrayList<>();
 
                List<BuildAudit> builds = isInt ? bd.getData().getIntBuildAuditLogs()
                        : bd.getData().getProdBuildAuditLogs();
                List<DeploymentAudit> deploys = isInt ? bd.getData().getIntDeploymentAuditLogs()
                        : bd.getData().getProdDeploymentAuditLogs();
 
                DeploymentAudit latestDeploy = latestDeploy(deploys);
                BuildAudit latestBuild = latestBuild(builds);
 
                if (latestDeploy != null) {
                    if (vo.getTriggeredBy() == null) vo.setTriggeredBy(latestDeploy.getTriggeredBy());
                    if (vo.getCommitSha() == null) vo.setCommitSha(latestDeploy.getCommitId());
                    if (vo.getBranch() == null) vo.setBranch(latestDeploy.getBranch());
                }
                if (latestBuild != null) {
                    if (vo.getTriggeredBy() == null) vo.setTriggeredBy(latestBuild.getTriggeredBy());
                    if (vo.getCommitSha() == null) vo.setCommitSha(latestBuild.getCommitId());
                    if (vo.getBranch() == null) vo.setBranch(latestBuild.getBranch());
                }
 
                if (builds != null) {
                    for (BuildAudit b : builds) {
                        if (b.getTriggeredOn() != null) {
                            activity.add(activityOf(b.getTriggeredOn(),
                                    "Build " + safeString(b.getBuildStatus()) + " on " + safeString(b.getBranch())));
                        }
                    }
                }
                if (deploys != null) {
                    for (DeploymentAudit d : deploys) {
                        if (d.getTriggeredOn() != null) {
                            activity.add(activityOf(d.getTriggeredOn(),
                                    "Deploy " + safeString(d.getDeploymentStatus()) + " on " + safeString(d.getBranch())));
                        }
                    }
                }
                activity.sort(Comparator.comparing(WorkflowActivityVO::getTs).reversed());
                vo.setActivity(activity.size() > 8 ? activity.subList(0, 8) : activity);
            }
        } catch (Exception e) {
            log.debug("Could not read audit logs for {}: {}", projectName, e.getMessage());
        }
    }
 
    private List<WorkflowJobVO> mapJobs(List<GitHubWorkflowJobsResponseDto.Job> jobs) {
        List<WorkflowJobVO> result = new ArrayList<>();
        for (GitHubWorkflowJobsResponseDto.Job job : jobs) {
            WorkflowJobVO jvo = new WorkflowJobVO();
            jvo.setId(job.getId());
            jvo.setName(job.getName());
            jvo.setStatus(job.getStatus());
            jvo.setConclusion(job.getConclusion());
            jvo.setHtmlUrl(job.getHtmlUrl());
            jvo.setDurationSeconds(durationSeconds(job.getStartedAt(), job.getCompletedAt()));
 
            List<WorkflowStepVO> steps = new ArrayList<>();
            int total = 0;
            int completed = 0;
            String currentStep = null;
            if (job.getSteps() != null) {
                for (GitHubWorkflowJobsResponseDto.Step step : job.getSteps()) {
                    total++;
                    if ("completed".equalsIgnoreCase(step.getStatus())) {
                        completed++;
                    } else if (currentStep == null
                            && ("in_progress".equalsIgnoreCase(step.getStatus())
                            || "queued".equalsIgnoreCase(step.getStatus()))) {
                        currentStep = step.getName();
                    }
                    WorkflowStepVO svo = new WorkflowStepVO();
                    svo.setName(step.getName());
                    svo.setStatus(step.getStatus());
                    svo.setConclusion(step.getConclusion());
                    svo.setNumber(step.getNumber());
                    svo.setDurationSeconds(durationSeconds(step.getStartedAt(), step.getCompletedAt()));
                    steps.add(svo);
                }
            }
            jvo.setSteps(steps);
            jvo.setTotalSteps(total);
            jvo.setCompletedSteps(completed);
            jvo.setProgress(total > 0 ? (int) Math.round((completed * 100.0) / total) : 0);
            jvo.setCurrentStep(currentStep);
            result.add(jvo);
        }
        return result;
    }
 
    private WorkflowActivityVO activityOf(Date ts, String message) {
        WorkflowActivityVO avo = new WorkflowActivityVO();
        avo.setTs(ts);
        avo.setMessage(message);
        return avo;
    }
 
    private String resolvePhase(String status, boolean runStarted) {
        if (status == null) return "NONE";
        String s = status.toUpperCase();
        if (!runStarted && (s.endsWith("_REQUESTED"))) {
            return "QUEUED";
        }
        switch (s) {
            case "BUILD_REQUESTED":
                return "BUILDING";
            case "DEPLOY_REQUESTED":
            case "DEPLOYING":
            case "RESTART_REQUESTED":
                return "DEPLOYING";
            case "BUILD_SUCCESS":
            case "DEPLOYED":
            case "RESTARTED":
                return "DONE";
            case "BUILD_FAILED":
            case "DEPLOYMENT_FAILED":
            case "RESTART_FAILED":
            case "FAILED":
                return "FAILED";
            default:
                return runStarted ? "RUNNING" : "QUEUED";
        }
    }
 
    private boolean isTerminal(String status) {
        if (status == null) return false;
        switch (status.toUpperCase()) {
            case "BUILD_SUCCESS":
            case "BUILD_FAILED":
            case "DEPLOYED":
            case "DEPLOYMENT_FAILED":
            case "RESTARTED":
            case "RESTART_FAILED":
                return true;
            default:
                return false;
        }
    }
 
    private Long durationSeconds(Date start, Date end) {
        if (start == null) return null;
        Date e = end != null ? end : new Date();
        return Math.max(0, (e.getTime() - start.getTime()) / 1000);
    }
 
    private BuildAudit latestBuild(List<BuildAudit> logs) {
        if (logs == null || logs.isEmpty()) return null;
        return logs.stream().filter(a -> a.getTriggeredOn() != null)
                .max(Comparator.comparing(BuildAudit::getTriggeredOn)).orElse(null);
    }
 
    private DeploymentAudit latestDeploy(List<DeploymentAudit> logs) {
        if (logs == null || logs.isEmpty()) return null;
        return logs.stream().filter(a -> a.getTriggeredOn() != null)
                .max(Comparator.comparing(DeploymentAudit::getTriggeredOn)).orElse(null);
    }
 
    private String safeString(String s) {
        return s == null ? "" : s;
    }
}