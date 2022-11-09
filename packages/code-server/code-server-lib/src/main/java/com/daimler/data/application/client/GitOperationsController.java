package com.daimler.data.application.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.dto.GitBranchesCollectionDto;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;

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
	
	@ApiOperation(value = "Get all branches for given git repo", nickname = "getGitBranches", notes = "Get all branches for given git repo", response = CodeServerWorkspaceVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{repo}/branches",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<GitBranchesCollectionDto> getGitBranches(@ApiParam(value = "git repo name for which branches needed to be fetched",required=true) @PathVariable("repo") String repo){
		GitBranchesCollectionDto branchesCollection = gitClient.getBranchesFromRepo(null, repo);
		return new ResponseEntity<>(branchesCollection,HttpStatus.OK);
	}
	
}
