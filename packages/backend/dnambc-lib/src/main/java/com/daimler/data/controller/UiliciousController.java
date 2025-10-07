package com.daimler.data.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.uilicious.UiliciousWorkspacesApi;


import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;

import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;

import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.service.uiliciousWorkspace.UiliciousWorkspaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Uilicious API's")
@RequestMapping("/api")
@Slf4j
public class UiliciousController implements UiliciousWorkspacesApi
{
	
    @Autowired
	private UiliciousWorkspaceService uiliciousWorkspaceService;

	@Override
    @ApiOperation(value = "Get list of workspaces for Uilicious", nickname = "getUiliciousWorkspaces", notes = "Returns Uilicious workspaces for the current user (or for the provided email). Each workspace includes metadata such as spaceName, link, lean governance and userRole. ", response = UiliciousWorkspacesCollectionVO.class, tags={ "uilicious-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful fetch of Uilicious workspace records.", response = UiliciousWorkspacesCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/uilicious-workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<UiliciousWorkspacesCollectionVO> getUiliciousWorkspaces(@ApiParam(value = "Optional: user email to fetch workspaces for. If not provided, server uses session auth.") @Valid @RequestParam(value = "email", required = false) String email,@ApiParam(value = "Page number from which listing of workspaces should start. Example: 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,@ApiParam(value = "Page size to limit the number of workspaces. Example: 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,@ApiParam(value = "Sort order (asc or desc).", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder){
        
		return null;
	};
    
}


