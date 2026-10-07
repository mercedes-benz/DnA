package com.daimler.data.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.GitClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.db.repo.workspace.WorkspaceRepository;
import com.daimler.data.dto.workspace.GitWebHookDto;
import com.daimler.data.dto.PullRequestPayloadDto;
import com.daimler.data.dto.PushPayloadDto;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;
import com.daimler.data.dto.workspace.ManageDeployRequestDto.TargetEnvironmentEnum;
import com.daimler.data.service.workspace.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitWebHookService {

    @Autowired
    private WorkspaceRepository jpaRepo;

    @Autowired
    private GitClient gitClient;

    @Autowired
    private WorkspaceCustomRepository workspaceCustomRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @Value("${codeServer.git.webhook.secret}")
    private String secret;

    @Value("${codeServer.git.enterprise.url}")
    private String gheBaseUri;

    @Value("${codeServer.git.enterprise.gitUrl}")
    private String gitBaseUrl;

    private static final String HMAC_ALGO = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Instant> seen = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(30);

    private final Pattern VALID_URL = Pattern.compile(
            "^https://mercedes-benz\\.ghe\\.com/" +
                    "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?/" +
                    "[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?" +
                    "(?:\\.git)?/?$");

    private final Pattern EXTRACT_REPOSITORY = Pattern.compile(
            "^https?://[^/]+/([^/]+/[^/]+?)(?:\\.git)?/?$");

    /**
     * add web hook to the specified git repository to receive events such as push,
     * pull request, etc. The webhook will
     * be configured to point to the endpoint that receives GitHub hook events,
     * allowing the application to process these
     * events and trigger respective actions based on the event type and payload.
     */
    public GenericMessage addGitWebhook(GitWebHookDto gitDetails) {

        log.info("action=addGitWebhook status=started repoName={} projectName={}",
                gitDetails != null ? gitDetails.getRepoName() : null,
                gitDetails != null ? gitDetails.getProjectName() : null);

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        responseMessage.setErrors(errors);

        if (gitDetails == null || gitDetails.getRepoName() == null || gitDetails.getRepoName().isBlank() ||
                gitDetails.getIntRepoName() == null || gitDetails.getIntRepoName().isBlank() ||
                gitDetails.getProdRepoName() == null || gitDetails.getProdRepoName().isBlank()) {
            log.warn("action=addGitWebhook status=validation_failed reason=missing_repo_names gitDetails={}",
                    gitDetails);
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription(
                    "Invalid repository name, internal repository name, or production repository name provided");
            errors.add(errorMsg);
            return responseMessage;
        } else if (gitDetails.getProdRepoName().equals(gitDetails.getIntRepoName())) {
            log.warn("action=addGitWebhook status=validation_failed reason=same_branch repo={} branch={}",
                    gitDetails.getRepoName(), gitDetails.getIntRepoName());
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription(
                    "Staging and Production branch can't be same, please select diffrent branchs");
            errors.add(errorMsg);
            return responseMessage;
        }
        List<CodeServerWorkspaceNsql> workspaceList = workspaceCustomRepository
                .findAllByRepoName(gitDetails.getRepoName());
        log.info("action=addGitWebhook status=workspaces_fetched_by_repo repo={} count={}",
                gitDetails.getRepoName(), workspaceList != null ? workspaceList.size() : 0);

        boolean isMatchingBranchName = workspaceList != null && workspaceList.stream()
                .anyMatch(dbWorkspace -> dbWorkspace != null && dbWorkspace.getData() != null
                        && dbWorkspace.getData().getProjectDetails() != null
                        && dbWorkspace.getData().getProjectDetails().getProjectName() != null
                        && !dbWorkspace.getData().getProjectDetails().getProjectName()
                                .equals(gitDetails.getProjectName())
                        &&
                        ((dbWorkspace.getData().getProjectDetails().getIntAutoDeployBranchName() != null &&
                                dbWorkspace.getData().getProjectDetails().getIntAutoDeployBranchName()
                                        .equals(gitDetails.getIntRepoName()))
                                ||
                                (dbWorkspace.getData().getProjectDetails().getProdAutoDeployBranchName() != null &&
                                        dbWorkspace.getData().getProjectDetails().getProdAutoDeployBranchName()
                                                .equals(gitDetails.getProdRepoName()))));

        if (isMatchingBranchName) {
            log.warn("action=addGitWebhook status=validation_failed reason=branch_already_in_use repo={} project={}",
                    gitDetails.getRepoName(), gitDetails.getProjectName());
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription(
                    "The provided internal or production branch name is already in use by another Code space project. Please choose different branch names.");
            errors.add(errorMsg);
            return responseMessage;
        }

        if (workspaceList == null) {
            workspaceList = new ArrayList<>();
        }
        if (workspaceList.isEmpty()) {
            List<CodeServerWorkspaceNsql> dbWorkspaceList = workspaceCustomRepository
                    .findAllbyProjectName(gitDetails.getProjectName());
            log.info("action=addGitWebhook status=workspaces_fetched_by_project project={} count={}",
                    gitDetails.getProjectName(), dbWorkspaceList != null ? dbWorkspaceList.size() : 0);
            if (dbWorkspaceList != null) {
                workspaceList.addAll(dbWorkspaceList);
            }
        }
        if (workspaceList.isEmpty()) {
            log.warn("action=addGitWebhook status=no_workspace_found repo={}", gitDetails.getRepoName());
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription(
                    "No workspace found for repo: " + gitDetails.getRepoName());
            errors.add(errorMsg);
            return responseMessage;
        } else {

            List<CodeServerWorkspaceNsql> filterWorkspaceList = workspaceList.stream()
                    .filter(entity -> entity != null && entity.getData() != null
                            && entity.getData().getProjectDetails() != null
                            && entity.getData().getProjectDetails().getProjectName() != null
                            && entity.getData().getProjectDetails().getProjectName()
                                    .equals(gitDetails.getProjectName()))
                    .toList();

            log.info("action=addGitWebhook status=workspaces_filtered_by_project project={} count={}",
                    gitDetails.getProjectName(), filterWorkspaceList.size());

            if (filterWorkspaceList.isEmpty()) {
                log.warn("action=addGitWebhook status=no_workspace_found_for_project project={}",
                        gitDetails.getProjectName());
                responseMessage.setSuccess("FAILED");
                MessageDescription errorMsg = new MessageDescription(
                        "No workspace found for project: " + gitDetails.getProjectName());
                errors.add(errorMsg);
                return responseMessage;
            }

            CodeServerWorkspaceNsql firstEntity = filterWorkspaceList.get(0);
            if (firstEntity.getData() == null || firstEntity.getData().getProjectDetails() == null
                    || firstEntity.getData().getProjectDetails().getProjectOwner() == null) {
                log.error("action=addGitWebhook status=missing_project_owner project={}", gitDetails.getProjectName());
                errors.add(new MessageDescription(
                        "Project owner details not found for project: " + gitDetails.getProjectName()));
                return responseMessage;
            }
            String ownerId = firstEntity.getData().getProjectDetails().getProjectOwner().getId();
            CodeServerWorkspaceNsql workspaceNsql = filterWorkspaceList.stream()
                    .filter(entity -> entity.getData() != null && entity.getData().getWorkspaceOwner() != null
                            && ownerId != null
                            && ownerId.equals(entity.getData().getWorkspaceOwner().getId()))
                    .findFirst().orElse(null);

            if (workspaceNsql == null || workspaceNsql.getData() == null) {
                log.warn("action=addGitWebhook status=owner_workspace_not_found project={} ownerId={}",
                        gitDetails.getProjectName(), ownerId);
                errors.add(new MessageDescription(
                        "No the owner workspace is found in the DB please check with workpsace owner and try again!"));
                return responseMessage;
            }
            CodeServerWorkspace dbWorkspace = workspaceNsql.getData();
            log.info("action=addGitWebhook status=owner_workspace_resolved project={} ownerId={} workspaceId={}",
                    gitDetails.getProjectName(), ownerId, workspaceNsql.getId());

            String gitUrl = null;
            if (!VALID_URL.matcher(gitDetails.getRepoName()).matches()) {
                gitUrl = gheBaseUri + "/repos/DNA-CodeSpaces/" + gitDetails.getRepoName() + "/hooks";
            } else {
                gitUrl = gitBaseUrl + "/" + getRepositoryURL(dbWorkspace.getProjectDetails().getGitRepoName())
                        + "/hooks";
            }
            log.info("action=addGitWebhook status=git_url_resolved repo={} gitUrl={}", gitDetails.getRepoName(),
                    gitUrl);

            if (dbWorkspace.getProjectDetails() != null && dbWorkspace.getProjectDetails().getWebHookId() != null
                    && !dbWorkspace.getProjectDetails().getWebHookId().trim().isEmpty()) {

                String gitUrlWithWebHook = gitUrl + "/" + dbWorkspace.getProjectDetails().getWebHookId();

                log.info("action=addGitWebhook status=updating_existing_webhook repo={} webHookId={}",
                        gitDetails.getRepoName(), dbWorkspace.getProjectDetails().getWebHookId());
                gitClient.updateWebHookConfigurations(gitDetails, gitUrlWithWebHook,
                        dbWorkspace.getProjectDetails().getWebHookId());

                for (CodeServerWorkspaceNsql workspace : filterWorkspaceList) {
                    if (workspace == null || workspace.getData() == null
                            || workspace.getData().getProjectDetails() == null) {
                        log.warn("action=addGitWebhook status=skipping_invalid_workspace_entry");
                        continue;
                    }
                    if (gitDetails.getIntRepoName() != null) {
                        workspace.getData().getProjectDetails().setIntAutoDeployBranchName(gitDetails.getIntRepoName());
                    }
                    if (gitDetails.getProdRepoName() != null) {
                        workspace.getData().getProjectDetails()
                                .setProdAutoDeployBranchName(gitDetails.getProdRepoName());
                    }
                    workspace.getData().setAutoDeploy(gitDetails.isWebHookEnabled());
                    jpaRepo.save(workspace);
                    log.info("action=addGitWebhook status=webhook_updated workspaceId={} repo={}", workspace.getId(),
                            gitDetails.getRepoName());
                }

                if(!gitDetails.isWebHookEnabled()){
                    for(CodeServerWorkspaceNsql workspace : workspaceList){
                        if(workspace == null || workspace.getData() == null || workspace.getData().getProjectDetails() == null){
                            log.warn("action=addGitWebhook status=skipping_invalid_workspace_entry");
                            continue;
                        }
                        workspace.getData().setAutoDeploy(false);
                        jpaRepo.save(workspace);
                        log.info("action=addGitWebhook status=auto_deploy_disabled workspaceId={} repo={}", workspace.getId(), gitDetails.getRepoName());
                    }
                }
                responseMessage.setSuccess("SUCCESS");
                return responseMessage;
            }

            String webHookId = gitClient.addWebHookToRepo(gitDetails.getRepoName(), gitUrl);
            if (webHookId == null) {
                log.error("action=addGitWebhook status=webhook_creation_failed repo={}", gitDetails.getRepoName());
                responseMessage.setSuccess("FAILED");
                MessageDescription errorMsg = new MessageDescription(
                        "Failed to add webhook to repo: " + gitDetails.getRepoName());
                errors.add(errorMsg);
                return responseMessage;
            }
            log.info("action=addGitWebhook status=webhook_created repo={} webHookId={}", gitDetails.getRepoName(),
                    webHookId);

            for (CodeServerWorkspaceNsql workspace : filterWorkspaceList) {
                if (workspace == null || workspace.getData() == null
                        || workspace.getData().getProjectDetails() == null) {
                    log.warn("action=addGitWebhook status=skipping_invalid_workspace_entry");
                    continue;
                }
                workspace.getData().getProjectDetails().setWebHookId(webHookId);
                workspace.getData().getProjectDetails().setIntAutoDeployBranchName(gitDetails.getIntRepoName());
                workspace.getData().getProjectDetails().setProdAutoDeployBranchName(gitDetails.getProdRepoName());
                workspace.getData().setAutoDeploy(gitDetails.isWebHookEnabled());
                jpaRepo.save(workspace);
                log.info("action=addGitWebhook status=workspace_webhook_saved workspaceId={} repo={}",
                        workspace.getId(),
                        gitDetails.getRepoName());
            }
        }
        responseMessage.setSuccess("SUCCESS");
        log.info("action=addGitWebhook status=completed repo={} project={}", gitDetails.getRepoName(),
                gitDetails.getProjectName());

        return responseMessage;
    }

    /**
     * Returns true if this deliveryId was already processed (duplicate).
     * Registers it if not seen before.
     */
    public boolean isDuplicate(String deliveryId) {
        if (deliveryId == null) {
            log.warn("action=isDuplicate status=null_delivery_id treating_as_not_duplicate");
            return false;
        }
        Instant now = Instant.now();
        Instant prev = seen.putIfAbsent(deliveryId, now);
        if (prev == null) {
            log.debug("action=isDuplicate status=new_delivery deliveryId={}", deliveryId);
            return false;
        }
        if (prev.isBefore(now.minus(TTL))) {
            seen.put(deliveryId, now);
            log.debug("action=isDuplicate status=expired_entry_refreshed deliveryId={}", deliveryId);
            return false;
        }
        log.info("action=isDuplicate status=duplicate_detected deliveryId={}", deliveryId);
        return true;
    }

    /**
     * Verifies X-Hub-Signature-256 header against raw request body.
     */
    public boolean verify(String signatureHeader, byte[] rawBody) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            log.warn("action=verify status=missing_or_invalid_signature_header");
            return false;
        }
        if (rawBody == null) {
            log.warn("action=verify status=null_raw_body");
            return false;
        }
        if (secret == null || secret.isBlank()) {
            log.error("action=verify status=webhook_secret_not_configured");
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGO));
            byte[] expected = mac.doFinal(rawBody);
            String expectedHex = "sha256=" + bytesToHex(expected);

            // Constant-time comparison to prevent timing attacks
            boolean matches = MessageDigest.isEqual(
                    expectedHex.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8));
            log.debug("action=verify status=completed matches={}", matches);
            return matches;
        } catch (Exception e) {
            log.error("action=verify status=error error={}", e.getMessage(), e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * this method will process the GitHub hook events received from GitHub and
     * trigger the respective actions
     * based on the event type and payload. It will handle events such as push, pull
     * request, etc., and update
     * the workspace status, trigger builds/deployments, or perform other necessary
     * operations accordingly.
     */
    public ResponseEntity<GenericMessage> processGitHubHookEvent(String signature, String eventType, String deliveryId,
            String hookId, byte[] rawBody) {

        log.info("action=processGitHubHookEvent status=started deliveryId={} eventType={} hookId={}", deliveryId,
                eventType, hookId);

        // 1. Verify signature
        if (!verify(signature, rawBody)) {
            log.warn("action=processGitHubHookEvent status=signature_failed deliveryId={} eventType={} hookId={}",
                    deliveryId, eventType, hookId);
            GenericMessage responseMessage = new GenericMessage();
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription("Invalid signature");
            List<MessageDescription> errors = new ArrayList<>();
            errors.add(errorMsg);
            responseMessage.setErrors(errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        log.debug("action=processGitHubHookEvent status=signature_verified deliveryId={} eventType={} hookId={}",
                deliveryId, eventType, hookId);

        // 2. Replay protection — reject duplicate deliveries
        if (isDuplicate(deliveryId)) {
            log.warn("action=processGitHubHookEvent status=duplicate_rejected deliveryId={} eventType={} hookId={}",
                    deliveryId, eventType, hookId);
            GenericMessage responseMessage = new GenericMessage();
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription("Duplicate delivery");
            List<MessageDescription> errors = new ArrayList<>();
            errors.add(errorMsg);
            responseMessage.setErrors(errors);
            return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
        }
        log.info("action=processGitHubHookEvent status=accepted deliveryId={} eventType={} hookId={}", deliveryId,
                eventType, hookId);

        switch (eventType) {
            case "push":
                try {
                    log.info("action=processGitHubHookEvent status=parsing_push_payload deliveryId={} hookId={}",
                            deliveryId, hookId);
                    PushPayloadDto payload = objectMapper.readValue(rawBody, PushPayloadDto.class);
                    log.info(
                            "action=processGitHubHookEvent status=dispatching_push deliveryId={} hookId={} repo={} pusher={} ref={}",
                            deliveryId,
                            hookId,
                            payload.getRepository() != null ? payload.getRepository().getFullName() : "unknown",
                            payload.getPusher() != null ? payload.getPusher().getName() : "unknown",
                            payload.getRef() != null ? payload.getRef() : "unknown");
                    processEvent(payload, hookId);
                } catch (Exception e) {
                    log.error("action=processGitHubHookEvent status=push_processing_error deliveryId={} error={}",
                            deliveryId, e.getMessage(), e);
                    throw new RuntimeException("Failed to process push payload", e);
                }
                break;
            case "pull_request":
                try {
                    log.info("action=processGitHubHookEvent status=parsing_pr_payload deliveryId={} hookId={}",
                            deliveryId, hookId);
                    PullRequestPayloadDto payload = objectMapper.readValue(rawBody, PullRequestPayloadDto.class);
                    log.info(
                            "action=processGitHubHookEvent status=dispatching_pull_request deliveryId={} hookId={} repo={} user={} merged={} baseRef={}",
                            deliveryId,
                            hookId,
                            payload.getRepository() != null ? payload.getRepository().getFullName() : "unknown",
                            payload.getPullRequest() != null && payload.getPullRequest().getUser() != null
                                    ? payload.getPullRequest().getUser().getLogin()
                                    : "unknown",
                            payload.getPullRequest() != null && payload.getPullRequest().isMerged(),
                            payload.getPullRequest() != null && payload.getPullRequest().getBase() != null
                                    ? payload.getPullRequest().getBase().getRef()
                                    : "unknown");
                    processEvent(payload, hookId);
                } catch (Exception e) {
                    log.error(
                            "action=processGitHubHookEvent status=pr_processing_error deliveryId={} hookId={} error={}",
                            deliveryId, hookId, e.getMessage(), e);
                    throw new RuntimeException("Failed to process pull_request payload", e);
                }
                break;
            default:
                log.warn("action=processGitHubHookEvent status=unsupported_event deliveryId={} hookId={} eventType={}",
                        deliveryId, hookId, eventType);
                throw new UnsupportedOperationException("Unsupported event type: " + eventType);
        }
        log.info("action=processGitHubHookEvent status=completed deliveryId={} hookId={} eventType={}", deliveryId,
                hookId, eventType);
        return null;
    }

    @Async
    private void processEvent(Object payload, String hookId) {

        log.info("action=processEvent status=started payloadType={} hookId={}", payload.getClass().getSimpleName(),
                hookId);

        List<CodeServerWorkspaceNsql> workspaceList = null;
        ManageDeployRequestDto deployRequest = null;
        CodeServerWorkspaceNsql workspace = null;
        String repoFullName = null;
        final String gitUserName;
        final String branchName;
        String targetEnv = null;
        final String ownerId;
        String id = null;

        if (payload instanceof PushPayloadDto pushPayload) {
            if (pushPayload.getRepository() == null || pushPayload.getRepository().getFullName() == null
                    || pushPayload.getPusher() == null || pushPayload.getPusher().getName() == null
                    || pushPayload.getRef() == null) {
                log.warn("action=processEvent status=incomplete_push_payload hookId={}", hookId);
                return;
            }
            repoFullName = pushPayload.getRepository().getFullName().split("/")[1];
            gitUserName = pushPayload.getPusher().getName();
            branchName = pushPayload.getRef().replace("refs/heads/", "");
            log.info("action=processEvent status=push_payload_extracted repo={} pusher={} branch={}",
                    repoFullName, gitUserName, branchName);

        } else if (payload instanceof PullRequestPayloadDto pullRequestPayload) {
            if (pullRequestPayload.getRepository() == null || pullRequestPayload.getRepository().getFullName() == null
                    || pullRequestPayload.getPullRequest() == null
                    || pullRequestPayload.getPullRequest().getUser() == null
                    || pullRequestPayload.getPullRequest().getUser().getLogin() == null
                    || pullRequestPayload.getPullRequest().getBase() == null
                    || pullRequestPayload.getPullRequest().getBase().getRef() == null) {
                log.warn("action=processEvent status=incomplete_pull_request_payload hookId={}", hookId);
                return;
            }
            repoFullName = pullRequestPayload.getRepository().getFullName().split("/")[1];
            gitUserName = pullRequestPayload.getPullRequest().getUser().getLogin();
            branchName = pullRequestPayload.getPullRequest().getBase().getRef().replace("refs/heads/", "");
            log.info("action=processEvent status=pr_payload_extracted repo={} user={} branch={} merged={}",
                    repoFullName, gitUserName, branchName, pullRequestPayload.getPullRequest().isMerged());

            if (!pullRequestPayload.getPullRequest().isMerged()) {
                String headRef = pullRequestPayload.getPullRequest().getHead() != null
                        ? pullRequestPayload.getPullRequest().getHead().getRef()
                        : "unknown";
                log.info("action=processEvent status=pr_not_merged repo={} headRef={} skipping=true",
                        repoFullName, headRef);
                return;
            }

        } else {
            log.warn("action=processEvent status=unsupported_payload_type payloadType={}",
                    payload == null ? "null" : payload.getClass().getName());
            return;
        }

        workspaceList = workspaceCustomRepository.findAllByWebhookId(hookId);
        log.info("action=processEvent status=workspaces_fetched repo={} count={}", repoFullName,
                workspaceList != null ? workspaceList.size() : 0);

        if (workspaceList == null || workspaceList.isEmpty()) {
            log.info("action=processEvent status=no_workspaces_found_for_webhook repo={} hookId={}", repoFullName,
                    hookId);
            workspaceList = workspaceCustomRepository.findAllByRepoName(repoFullName);
            log.info("action=processEvent status=workspaces_fetched_by_repo repo={} count={}", repoFullName,
                    workspaceList != null ? workspaceList.size() : 0);
            if (workspaceList == null || workspaceList.isEmpty()) {
                log.info("action=processEvent status=no_workspaces_found_for_repo repo={} hookId={}", repoFullName,
                        hookId);
                return;
            }
        }

        List<CodeServerWorkspaceNsql> filterWorkspaceList = workspaceList.stream()
                .filter(dbWorkspace -> dbWorkspace.getData().getProjectDetails().getIntAutoDeployBranchName().equals(branchName) ||
                        dbWorkspace.getData().getProjectDetails().getProdAutoDeployBranchName().equals(branchName))
                .collect(Collectors.toList());

        if(filterWorkspaceList.isEmpty()) {
            log.info("action=processEvent status=no_workspaces_found_for_branch repo={} branch={} hookId={}",
                    repoFullName, branchName, hookId);
            return;
        }

        final String finalGitUserName = gitUserName;
        workspace = filterWorkspaceList.stream()
                .filter(wSpace -> wSpace != null && wSpace.getData() != null
                        && wSpace.getData().getWorkspaceOwner() != null
                        && wSpace.getData().getWorkspaceOwner().getGitUserName() != null
                        && wSpace.getData().getWorkspaceOwner().getGitUserName().equals(finalGitUserName)
                        && wSpace.getData().getStatus() != null
                        && wSpace.getData().getStatus().equals("CREATED"))
                .findFirst().orElse(null);

        if (workspace == null) {
            log.info(
                    "action=processEvent status=workspace_not_found_for_user repo={} gitUser={} fallback=project_owner",
                    repoFullName, gitUserName);

            CodeServerWorkspaceNsql firstWorkspace = filterWorkspaceList.get(0);
            if (firstWorkspace == null || firstWorkspace.getData() == null
                    || firstWorkspace.getData().getProjectDetails() == null
                    || firstWorkspace.getData().getProjectDetails().getProjectOwner() == null) {
                log.warn("action=processEvent status=no_project_owner_found repo={}", repoFullName);
                return;
            }
            ownerId = firstWorkspace.getData().getProjectDetails().getProjectOwner().getId();
            workspace = filterWorkspaceList.stream()
                    .filter(entity -> entity != null && entity.getData() != null
                            && entity.getData().getWorkspaceOwner() != null
                            && ownerId != null
                            && ownerId.equals(entity.getData().getWorkspaceOwner().getId()))
                    .findFirst().orElse(null);
            log.info("action=processEvent status=fallback_workspace_resolved repo={} ownerId={} found={}",
                    repoFullName, ownerId, workspace != null);
        } else if (workspace.getData() != null && workspace.getData().getWorkspaceOwner() != null) {
            ownerId = workspace.getData().getWorkspaceOwner().getId();
            log.info("action=processEvent status=workspace_resolved repo={} workspaceId={} ownerId={}",
                    repoFullName, workspace.getId(), ownerId);
        } else {
            ownerId = null;
            log.warn("action=processEvent status=workspace_owner_missing repo={} workspaceId={}",
                    repoFullName, workspace.getId());
        }

        id = workspace != null ? workspace.getId() : null;

        if (workspace == null || workspace.getData() == null || workspace.getData().getProjectDetails() == null
                || Boolean.FALSE.equals(workspace.getData().getAutoDeploy()) ||
                workspace.getData().getProjectDetails().getWebHookId() == null) {
            log.info("action=processEvent status=auto_deploy_skipped workspaceId={} autoDeploy={} reason={}",
                    id, workspace != null && workspace.getData() != null ? workspace.getData().getAutoDeploy() : null,
                    workspace == null || workspace.getData() == null || workspace.getData().getProjectDetails() == null
                            ? "workspace_not_found"
                            : workspace.getData().getProjectDetails().getWebHookId() == null
                                    || workspace.getData().getProjectDetails().getWebHookId().isBlank()
                                            ? "webhook_not_configured"
                                            : "auto_deploy_disabled");
            return;
        }

        deployRequest = new ManageDeployRequestDto();
        if (workspace.getData().getProjectDetails().getIntAutoDeployBranchName() != null
                && workspace.getData().getProjectDetails().getIntAutoDeployBranchName()
                        .equals(branchName)) {
            targetEnv = "int";
        } else if (workspace.getData().getProjectDetails().getProdAutoDeployBranchName() != null &&
                workspace.getData().getProjectDetails().getProdAutoDeployBranchName()
                        .equals(branchName)) {
            targetEnv = "prod";
        } else {
            log.info(
                    "action=processEvent status=branch_not_matched workspaceId={} branch={} intBranch={} prodBranch={}",
                    id, branchName,
                    workspace.getData().getProjectDetails().getIntAutoDeployBranchName(),
                    workspace.getData().getProjectDetails().getProdAutoDeployBranchName());
            return;
        }

        log.info("action=processEvent status=target_env_resolved workspaceId={} branch={} targetEnv={}",
                id, branchName, targetEnv);

        deployRequest.setTargetEnvironment(TargetEnvironmentEnum.fromValue(targetEnv));
        switch (targetEnv) {
            case "int" ->
                deployRequest.setBranch(workspace.getData().getProjectDetails().getIntAutoDeployBranchName());
            case "prod" ->
                deployRequest.setBranch(workspace.getData().getProjectDetails().getProdAutoDeployBranchName());
        }
        deployRequest.setRepo(repoFullName);
        deployRequest.setVersion("");
        deployRequest.setKeepBuildImage(false);

        log.info(
                "action=processEvent status=triggering_deployment workspaceId={} ownerId={} repo={} targetEnv={} branch={}",
                id, ownerId, repoFullName, targetEnv, deployRequest.getBranch());
        workspaceService.preValidateDeployment(deployRequest, id, ownerId, true);
        log.info("action=processEvent status=deployment_triggered workspaceId={} targetEnv={}", id, targetEnv);
    }

    private String getRepositoryURL(String url) {
        log.debug("action=getRepositoryURL status=started url={}", url);
        if (url == null || !VALID_URL.matcher(url).matches()) {
            log.error("action=getRepositoryURL status=invalid_url url={}", url);
            throw new IllegalArgumentException("Invalid GitHub repository URL: " + url);
        }

        Matcher matcher = EXTRACT_REPOSITORY.matcher(url);

        if (!matcher.matches()) {
            log.error("action=getRepositoryURL status=unable_to_extract_repository url={}", url);
            throw new IllegalArgumentException("Unable to extract repository from URL: " + url);
        }

        // Capture group 1 contains "owner/repository".
        String repository = matcher.group(1);
        log.debug("action=getRepositoryURL status=completed url={} repository={}", url, repository);
        return repository;
    }

}
