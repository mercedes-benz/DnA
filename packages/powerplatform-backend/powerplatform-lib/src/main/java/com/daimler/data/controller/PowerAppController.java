package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.powerapps.PowerappsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.powerapps.PowerAppCollectionVO;
import com.daimler.data.dto.powerapps.PowerAppCreateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppResponseVO;
import com.daimler.data.dto.powerapps.PowerAppUpdateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.fabric.PowerAppService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Power Apps APIs")
@RequestMapping("/api")
@Slf4j
public class PowerAppController implements PowerappsApi
{
	@Autowired
	private PowerAppService service;

	@Autowired
	private UserStore userStore;
	
	@Override
	@ApiOperation(value = "Creates a new power app subscription request.", nickname = "create", notes = "Creates a new power app subscription request", response = PowerAppResponseVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = PowerAppResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<PowerAppResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new workspace" ,required=true )  @Valid @RequestBody PowerAppCreateRequestVO powerAppCreateVO){
		return null;
	}

	
	@Override
    @ApiOperation(value = "Get all power platform subscriptions for the user.", nickname = "getAll", notes = "Get all platform subscriptions. This endpoints will be used to get all valid available platform subscription records.", response = PowerAppCollectionVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = PowerAppCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PowerAppCollectionVO> getAll(@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
    		@ApiParam(value = "Sort workspaces by a given variable like name, createdOn", allowableValues = "name, createdOn") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort solutions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder){
    	return null;
    }

	@Override
    @ApiOperation(value = "Get power app subscription details for a given Id.", nickname = "getById", notes = " This endpoints will get power app subscription details for a given identifier.", response = PowerAppVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = PowerAppVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PowerAppVO> getById(@ApiParam(value = "Power platform subscription ID to be fetched",required=true) @PathVariable("id") String id){
		return null;
	}

	@Override
    @ApiOperation(value = "Update existing power app subscription details .", nickname = "update", notes = "Updates an existing power app subscription.", response = PowerAppResponseVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Updated successfully", response = PowerAppResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "No workspace found to update", response = GenericMessage.class),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<PowerAppResponseVO> update(@ApiParam(value = "Power app subscription ID to be updated",required=true) @PathVariable("id") String id,@ApiParam(value = "Request Body that contains data required for updating an existing workspace" ,required=true )  @Valid @RequestBody PowerAppUpdateRequestVO powerappUpdateRequestVO){
		return null;
	}
    
}
