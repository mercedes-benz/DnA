package com.daimler.data.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final String HMAC_ALGO = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Instant> seen = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(30);

    /**
     * add web hook to the specified git repository to receive events such as push,
     * pull request, etc. The webhook will
     * be configured to point to the endpoint that receives GitHub hook events,
     * allowing the application to process these
     * events and trigger respective actions based on the event type and payload.
     */
    public GenericMessage addGitWebhook(GitWebHookDto gitDetails) {

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        responseMessage.setErrors(errors);

        if (gitDetails == null || gitDetails.getRepoName() == null || gitDetails.getRepoName().isBlank() ||
                gitDetails.getIntRepoName() == null || gitDetails.getIntRepoName().isBlank() ||
                gitDetails.getProdRepoName() == null || gitDetails.getProdRepoName().isBlank()) {
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription(
                    "Invalid repository name, internal repository name, or production repository name provided");
            errors.add(errorMsg);
            return responseMessage;
        } else if(gitDetails.getProdRepoName().equals(gitDetails.getIntRepoName())){
            MessageDescription errorMsg = new MessageDescription("Staging and Production branch can't be same, please select diffrent branchs");
            errors.add(errorMsg);
            return responseMessage;
        }
        List<CodeServerWorkspaceNsql> workspaceList = workspaceCustomRepository
                .findAllByRepoName(gitDetails.getRepoName());
        if (workspaceList == null || workspaceList.isEmpty()) {
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription(
                    "No workspace found for repo: " + gitDetails.getRepoName());
            errors.add(errorMsg);
            return responseMessage;
        } else {

            String ownerId = workspaceList.get(0).getData().getProjectDetails().getProjectOwner().getId();
            CodeServerWorkspaceNsql workspaceNsql = workspaceList.stream()
                    .filter(entity -> entity.getData().getWorkspaceOwner().getId().equals(ownerId))
                    .findFirst().orElse(null);

            if (workspaceNsql == null || workspaceNsql.getData() == null) {
                errors.add(new MessageDescription(
                        "No the owner workspace is found in the DB please check with workpsace owner and try again!"));
                return responseMessage;
            }
            CodeServerWorkspace dbWorkspace = workspaceNsql.getData();

            boolean isWorkspaceMigratedToGHE = (dbWorkspace != null &&
                    dbWorkspace.getProjectDetails().getRecipeDetails().getRepodetails().contains("ghe.com"));

            if (dbWorkspace.getProjectDetails().getWebHookId() != null
                    && !dbWorkspace.getProjectDetails().getWebHookId().trim().isEmpty()) {
                gitClient.updateWebHookConfigurations(gitDetails, isWorkspaceMigratedToGHE,
                        dbWorkspace.getProjectDetails().getWebHookId());
                for (CodeServerWorkspaceNsql workspace : workspaceList) {
                    if (gitDetails.getIntRepoName() != null) {
                        workspace.getData().getProjectDetails().setIntAutoDeployBranchName(gitDetails.getIntRepoName());
                    }
                    if (gitDetails.getProdRepoName() != null) {
                        workspace.getData().getProjectDetails()
                                .setProdAutoDeployBranchName(gitDetails.getProdRepoName());
                    }
                    workspace.getData().setAutoDeploy(gitDetails.isWebHookEnabled());
                    log.info("Webhook updated successfully for repo: {}", gitDetails.getRepoName());
                    jpaRepo.save(workspace);
                }
                responseMessage.setSuccess("SUCCESS");
                return responseMessage;
            }

            String webHookId = gitClient.addWebHookToRepo(gitDetails.getRepoName(), isWorkspaceMigratedToGHE);
            if (webHookId == null) {
                responseMessage.setSuccess("FAILED");
                MessageDescription errorMsg = new MessageDescription(
                        "Failed to add webhook to repo: " + gitDetails.getRepoName());
                errors.add(errorMsg);
                return responseMessage;
            }

            for (CodeServerWorkspaceNsql workspace : workspaceList) {
                workspace.getData().getProjectDetails().setWebHookId(webHookId);
                workspace.getData().getProjectDetails().setIntAutoDeployBranchName(gitDetails.getIntRepoName());
                workspace.getData().getProjectDetails().setProdAutoDeployBranchName(gitDetails.getProdRepoName());
                workspace.getData().setAutoDeploy(gitDetails.isWebHookEnabled());
                jpaRepo.save(workspace);
            }
        }
        responseMessage.setSuccess("SUCCESS");

        return responseMessage;
    }

    /**
     * Returns true if this deliveryId was already processed (duplicate).
     * Registers it if not seen before.
     */
    public boolean isDuplicate(String deliveryId) {
        Instant now = Instant.now();
        Instant prev = seen.putIfAbsent(deliveryId, now);
        if (prev == null)
            return false;
        if (prev.isBefore(now.minus(TTL))) {
            seen.put(deliveryId, now);
            return false;
        }
        return true;
    }

    /**
     * Verifies X-Hub-Signature-256 header against raw request body.
     */
    public boolean verify(String signatureHeader, byte[] rawBody) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGO));
            byte[] expected = mac.doFinal(rawBody);
            String expectedHex = "sha256=" + bytesToHex(expected);

            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    expectedHex.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
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
            byte[] rawBody) {

        // 1. Verify signature
        if (!verify(signature, rawBody)) {
            log.warn("[{}] Signature verification FAILED for event={}", deliveryId, eventType);
            GenericMessage responseMessage = new GenericMessage();
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription("Invalid signature");
            List<MessageDescription> errors = new ArrayList<>();
            errors.add(errorMsg);
            responseMessage.setErrors(errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }

        // 2. Replay protection — reject duplicate deliveries
        if (isDuplicate(deliveryId)) {
            log.warn("[{}] Duplicate delivery rejected for event={}", deliveryId, eventType);
            GenericMessage responseMessage = new GenericMessage();
            responseMessage.setSuccess("FAILED");
            MessageDescription errorMsg = new MessageDescription("Duplicate delivery");
            List<MessageDescription> errors = new ArrayList<>();
            errors.add(errorMsg);
            responseMessage.setErrors(errors);
            return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
        }
        log.debug("[{}] Accepted event={}", deliveryId, eventType);

        switch (eventType) {
            case "push":
                try {
                    PushPayloadDto payload = objectMapper.readValue(rawBody, PushPayloadDto.class);
                    processEvent(payload);
                } catch (Exception e) {
                    log.error("[{}] Failed to process push payload: {}", deliveryId, e.getMessage(), e);
                    throw new RuntimeException("Failed to process push payload", e);
                }
                break;
            case "pull_request":
                try {
                    PullRequestPayloadDto payload = objectMapper.readValue(rawBody, PullRequestPayloadDto.class);
                    processEvent(payload);
                } catch (Exception e) {
                    log.error("[{}] Failed to process pull_request payload: {}", deliveryId, e.getMessage(), e);
                    throw new RuntimeException("Failed to process pull_request payload", e);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported event type: " + eventType);
        }
        return null;
    }

    @Async
    private void processEvent(Object payload) {

        List<CodeServerWorkspaceNsql> workspaceList = null;
        ManageDeployRequestDto deployRequest = null;
        CodeServerWorkspaceNsql workspace = null;
        String repoFullName = null;
        final String gitUserName;
        String branchName = null;
        String targetEnv = null;
        final String ownerId;
        String id = null;

        if (payload instanceof PushPayloadDto pushPayload) {
            repoFullName = pushPayload.getRepository().getFullName().split("/")[1];
            gitUserName = pushPayload.getPusher().getName();
            branchName = pushPayload.getRef().replace("refs/heads/", "");

        } else if (payload instanceof PullRequestPayloadDto pullRequestPayload) {
            repoFullName = pullRequestPayload.getRepository().getFullName().split("/")[1];
            gitUserName = pullRequestPayload.getPullRequest().getUser().getLogin();
            branchName = pullRequestPayload.getPullRequest().getBase().getRef().replace("refs/heads/", "");

            if (!pullRequestPayload.getPullRequest().isMerged()) {
                log.info("Pull request {} is not merged for workspace {}", pullRequestPayload.getPullRequest().getHead().getRef(),
                        id);
                return;
            }

        } else {
            log.warn("Received unsupported event payload type: {}", payload.getClass().getName());
            return;
        }

        workspaceList = workspaceCustomRepository.findAllByRepoName(repoFullName);
        workspace = workspaceList.stream()
                .filter(wSpace -> wSpace.getData().getWorkspaceOwner().getGitUserName()
                        .equalsIgnoreCase(gitUserName) && wSpace.getData().getStatus().equals("CREATED"))
                .findFirst().orElse(null);

        if (workspace == null) {
            log.info(
                    "No workspace found associated with the PR merger for the repo: {} and user: {}, " +
                            "the committer is not collaborator or creator of the workspace, proceeding for the deployment with workspace creator ID",
                    repoFullName, gitUserName);

            ownerId = workspaceList.get(0).getData().getProjectDetails().getProjectOwner().getId();
            workspace = workspaceList.stream()
                    .filter(entity -> entity.getData().getWorkspaceOwner().getId().equals(ownerId))
                    .findFirst().orElse(null);
        } else {
            ownerId = workspace.getData().getWorkspaceOwner().getId();
        }

        id = workspace != null ? workspace.getId() : null;

        if (workspace == null || Boolean.FALSE.equals(workspace.getData().getAutoDeploy())) {
            log.info(
                    "no int or prod branch name found in the workspace, please update the branch names and try again");
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
            log.info("Pull request event head ref {} does not match any auto-deploy branches for workspace {}",
                    branchName, id);
            return;
        }

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

        workspaceService.preValidateDeployment(deployRequest, id, ownerId, true);
    }

}
