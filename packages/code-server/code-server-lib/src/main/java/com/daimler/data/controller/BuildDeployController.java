package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.daimler.data.controller.exceptions.GenericMessage;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

public class BuildDeployController {
    
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
    ResponseEntity<BuildResponseVo> triggerBuild(@ApiParam(value = "Request Body that contains data required for triggering build in code server workbench by the user" ,required=true )  @Valid @RequestBody BuildRequestVo buildRequestVo){
        return null;
    }

}
