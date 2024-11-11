package com.daimler.data.controller;

import javax.validation.Valid;

import com.daimler.data.api.workspace.buildDeploy.CodeServerBuildServiceApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.workspace.buildDeploy.BuildRequestVo;
import com.daimler.data.dto.workspace.buildDeploy.BuildResponseVo;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.List;

@RestController
@Api(value = "CodeServerBuildService", description = "the CodeServerBuildService API")
@RequestMapping(value = "/api/1.0")
@Slf4j
public class BuildDeployController implements CodeServerBuildServiceApi {
    
    @Override
    @ApiOperation(value = "Trigger Build by the user in code-server.", nickname = "triggerBuild", notes = "Trigger Build by the user in code-server for", response = BuildResponseVo.class, tags={ "code-server-build-service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = BuildResponseVo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/build/{action}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<BuildResponseVo>  triggerBuild(@ApiParam(value = "Request Body that contains data required for triggering build in code server workbench by the user" ,required=true )  @Valid @RequestBody BuildRequestVo buildRequestVo){
        return null;
    }
        
  
}
