package com.daimler.data.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

import com.daimler.data.api.powerapps.PowerappsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.powerapps.CreatedByVO;
import com.daimler.data.dto.powerapps.DeveloperVO;
import com.daimler.data.dto.powerapps.PowerAppCollectionVO;
import com.daimler.data.dto.powerapps.PowerAppCreateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppCreateRequestWrapperVO;
import com.daimler.data.dto.powerapps.PowerAppResponseVO;
import com.daimler.data.dto.powerapps.PowerAppUpdateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.powerapp.PowerAppService;

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
	
	@Value("${powerapps.defaults.environment}")
	private String defaultEnvironment;
	
	@Value("${powerapps.defaults.productionAvailability}")
	private String prodAvailabilityDefault;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
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
    public ResponseEntity<PowerAppResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new workspace" ,required=true )  @Valid @RequestBody PowerAppCreateRequestWrapperVO powerAppCreateVO){
		if(powerAppCreateVO!= null) {
			PowerAppResponseVO responseVO = new PowerAppResponseVO();
			PowerAppCreateRequestVO projectCreateVO = powerAppCreateVO.getData();
			String name = projectCreateVO.getName();
			PowerAppVO existingApp = service.findbyUniqueLiteral(name);
			if(existingApp!=null && existingApp.getId()!=null) {
				log.error("Power App request with this name {} already exists , failed to create new request", name);
				MessageDescription invalidMsg = new MessageDescription("Power app request already exists with given name. Please retry with unique name");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.CONFLICT.name());
				errorMessage.addWarnings(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			if(projectCreateVO.getName() == null || "".equalsIgnoreCase(projectCreateVO.getName())
					|| projectCreateVO.getEnvOwnerId() == null || "".equalsIgnoreCase(projectCreateVO.getEnvOwnerId())
					|| projectCreateVO.getDepartment()  == null || "".equalsIgnoreCase(projectCreateVO.getDepartment())
					|| projectCreateVO.getBillingPlant()  == null || "".equalsIgnoreCase(projectCreateVO.getBillingPlant())
					|| projectCreateVO.getBillingCostCentre() == null || "".equalsIgnoreCase(projectCreateVO.getBillingCostCentre())) {
				MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, Please fill all mandatory fields.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
				errorMessage.addErrors(mandatoryFieldsError);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
			if(projectCreateVO.getEnvironment() == null || "".equalsIgnoreCase(projectCreateVO.getEnvironment().name())){
				projectCreateVO.setEnvironment(null);
			}
			CreatedByVO requestUser = this.userStore.getVO();
			List<MessageDescription> errors = new ArrayList<>();
			//proceed
		}else {
			PowerAppResponseVO responseVO = new PowerAppResponseVO();
			MessageDescription invalidMsg = new MessageDescription("Bad request, please fill all required fields and retry.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(null);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}
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
    		@ApiParam(value = "Sort workspaces by a given variable like name, requestedOn, state", allowableValues = "name, requestedOn, state") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort solutions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder){
		PowerAppCollectionVO collection = new PowerAppCollectionVO();
		int defaultLimit = 10;
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
		}
		List<PowerAppVO> records =  new ArrayList<>();
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		records = service.getAll(limit, offset, user);
		Long count = service.getCount(user);
		HttpStatus responseCode = HttpStatus.NO_CONTENT;
		if(records!=null && !records.isEmpty()) {
			collection.setRecords(records);
			collection.setTotalCount(count.intValue());
			responseCode = HttpStatus.OK;
		}
	return new ResponseEntity<>(collection, responseCode);
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
		PowerAppVO existingApp = service.getById(id);
		if(existingApp==null || !id.equalsIgnoreCase(existingApp.getId())) {
			log.warn("No app found with id {}, failed to fetch saved inputs for given power app request id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		List<String> appUsers = new ArrayList<>();
		appUsers.add(existingApp.getRequestedBy().getId());
		List<DeveloperVO> developers = existingApp.getDevelopers();
		if(developers!=null && !developers.isEmpty()) {
			developers.forEach(n-> appUsers.add(n.getUserDetails().getId()));
		}
		if(appUsers!=null && !appUsers.isEmpty()) {
			if(!appUsers.contains(requestUser.getId())) {
				log.warn("User not part of requested power platform application with id {} and name {}, Not authorized to use other projects",id,existingApp.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}else {
				return new ResponseEntity<>(existingApp, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
