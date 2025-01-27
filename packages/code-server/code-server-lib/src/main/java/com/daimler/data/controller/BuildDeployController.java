package com.daimler.data.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.api.workspace.buildDeploy.CodeServerBuildDeployServiceApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import org.springframework.web.bind.annotation.PathVariable;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import com.daimler.data.dto.workspace.buildDeploy.ManageBuildRequestDto;
import com.daimler.data.dto.workspace.buildDeploy.CodeServerBuildLogsVO;
import com.daimler.data.application.auth.UserStore;
import org.springframework.beans.factory.annotation.Autowired;




@RestController
@Api(value = "Recipe API", tags = { "code-server-build-deploy-service" })
@RequestMapping("/api")
@Slf4j
public class BuildDeployController implements CodeServerBuildDeployServiceApi {

    @Autowired
	 private UserStore userStore;

    @Override
    @ApiOperation(value = "Build workspace Project for a given Id.", nickname = "buildWorkspaceProject", notes = "build workspace Project for a given identifier.", response = GenericMessage.class, tags={ "code-server-build-deploy-service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}/build",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> buildWorkspaceProject(@ApiParam(value = "Workspace ID to be fetched",required=true) @PathVariable("id") String id,@ApiParam(value = "request body for the project to be built" ,required=true )  @Valid @RequestBody ManageBuildRequestDto buildRequestDto){
        return null;
    }


    @Override
    @ApiOperation(value = "Get build logs for a given project name.", nickname = "getBuildLogsByProjectName", notes = "Get build logs for a given project name.", response = CodeServerBuildLogsVO.class, tags={ "code-server-build-deploy-service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerBuildLogsVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "workspace/{projectName}/buildLogs",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
   public ResponseEntity<CodeServerBuildLogsVO> getBuildLogsByProjectName(@ApiParam(value = "Workspace projectName to be fetched",required=true) @PathVariable("projectName") String projectName){
        return null;
    }
    
}
