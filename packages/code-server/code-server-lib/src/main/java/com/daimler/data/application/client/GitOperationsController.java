package com.daimler.data.application.client;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.dto.GitBranchesCollectionDto;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.service.GitWebHookService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Workspace API", tags = { "code-server" })
@RequestMapping("/api")
@Slf4j
public class GitOperationsController {

	@Autowired
	private GitClient gitClient;
	
	@Autowired
	HttpServletRequest httpRequest;

    @Autowired
    private GitWebHookService gitWebHookService;
	
	@ApiOperation(value = "Get all branches for given git repo", nickname = "getGitBranches", notes = "Get all branches for given git repo", response = CodeServerWorkspaceVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/branches",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<GitBranchesCollectionDto> getGitBranches(
			@ApiParam(value = "git repo name for which branches needed to be fetched", required=true) @Valid @RequestParam(value = "repoDetail", required = true) String repoDetail) {
		Boolean isWorkspaceMigratedToGHE = (repoDetail != null && repoDetail.contains("ghe.com"));
		log.info("Fetching branches for repo: {} - Determined isWorkspaceMigratedToGHE: {} (will use {} server)", 
				repoDetail, isWorkspaceMigratedToGHE, isWorkspaceMigratedToGHE ? "GHE" : "git.i");
		GitBranchesCollectionDto branchesCollection = gitClient.getBranchesFromRepo(null, repoDetail, isWorkspaceMigratedToGHE);
		return new ResponseEntity<>(branchesCollection,HttpStatus.OK);
	}

    @ApiOperation(value = "post endpoint to receive data from github hooked codespaces repos for auto deployment", nickname = "receiveWebhookData", notes = "Post endpoint to receive data from github hooked codespaces repos for auto deployment", response = String.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = String.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/gitHook",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<String> receiveWebhookData(
        @RequestHeader("X-GitHub-Event") String eventType, @RequestHeader("X-GitHub-Delivery") String deliveryId,
        @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature, @RequestBody byte[] rawBody) {

        long startTime = System.currentTimeMillis();
        log.info("action=receiveWebhookData status=received deliveryId={} eventType={} bodySize={}",
                deliveryId, eventType, rawBody != null ? rawBody.length : 0);
        try {
            gitWebHookService.processGitHubHookEvent(signature, eventType, deliveryId, rawBody);
            long duration = System.currentTimeMillis() - startTime;
            log.info("action=receiveWebhookData status=success deliveryId={} eventType={} durationMs={}",
                    deliveryId, eventType, duration);
            return ResponseEntity.ok("Accepted");
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("action=receiveWebhookData status=error deliveryId={} eventType={} durationMs={} error={}",
                    deliveryId, eventType, duration, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process event");
        }
    }    
}