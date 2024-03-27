package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.fabricWorkspace.FabricWorkspacesApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceUpdateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.service.forecast.FabricWorkspaceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Forecast APIs")
@RequestMapping("/api")
@Slf4j
public class FabricWorkspaceController implements FabricWorkspacesApi
{
	@Autowired
	private FabricWorkspaceService service;

	@Autowired
	private UserStore userStore;
	
	@Override
	@ApiOperation(value = "Adds a new fabric workspace.", nickname = "create", notes = "Adds a new non existing workspace.", response = FabricWorkspaceResponseVO.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = FabricWorkspaceResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<FabricWorkspaceResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new workspace" ,required=true )  @Valid @RequestBody FabricWorkspaceCreateRequestVO workspaceCreateVO){
		FabricWorkspaceResponseVO responseVO = new FabricWorkspaceResponseVO();
		GenericMessage errorMessage = new GenericMessage();
		FabricWorkspaceVO data = new FabricWorkspaceVO();
		FabricWorkspaceVO workspaceRequestVO = workspaceCreateVO.getData();
		if(workspaceRequestVO==null || workspaceRequestVO.getName()==null || workspaceRequestVO.getTypeOfProject() ==null || workspaceRequestVO.getDescription()==null 
				|| workspaceRequestVO.getDivision() == null || workspaceRequestVO.getSubDivision() == null || workspaceRequestVO.getDepartment() == null || workspaceRequestVO.getDataClassification() ==null
				|| workspaceRequestVO.isHasPii() == null || workspaceRequestVO.isTermsOfUse() == null || workspaceRequestVO.getCostCenter() == null) {
			log.error("Fabric workspace project mandatory fields cannot be null, please check and send valid input");
			MessageDescription invalidMsg = new MessageDescription("Fabric workspace project mandatory fields cannot be null, please check and send valid input");
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(workspaceRequestVO);
			responseVO.setResponses(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}
		if(workspaceRequestVO!=null && workspaceRequestVO.getName()!=null && "Admin monitoring".equalsIgnoreCase(workspaceRequestVO.getName())) {
			log.error("Fabric workspace project name cannot be Admin monitoring, cannot use reserve keyword. Please send valid input");
			MessageDescription invalidMsg = new MessageDescription("Fabric workspace project name cannot be Admin monitoring, cannot use reserve keyword. Please send valid input");
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(workspaceRequestVO);
			responseVO.setResponses(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			workspaceRequestVO.setCreatedBy(requestUser);
			workspaceRequestVO.setId(null);
			workspaceRequestVO.setCreatedOn(new Date());
			FabricWorkspaceResponseVO responseFromService = service.createWorkspace(workspaceRequestVO);
			if(responseFromService!=null && responseFromService.getResponses()!=null && "SUCCESS".equalsIgnoreCase(responseFromService.getResponses().getSuccess()) ) {
				return new ResponseEntity<>(responseFromService, HttpStatus.CREATED);
			}else {
				GenericMessage failedResponse = new GenericMessage();
				List<MessageDescription> messages = new ArrayList<>();
				if(responseFromService!=null && responseFromService.getResponses()!=null && responseFromService.getResponses().getErrors()!= null && !responseFromService.getResponses().getErrors().isEmpty())
				{
					failedResponse.setErrors(responseFromService.getResponses().getErrors());
				}else {
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to create workspace due to internal error");
					messages.add(message);
					failedResponse.addErrors(message);
				}
				failedResponse.setSuccess("FAILED");
				responseVO.setData(workspaceRequestVO);
				responseVO.setResponses(failedResponse);
				log.error("Exception occurred while creating fabric workspace project {} ", workspaceRequestVO.getName());
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to create workspace due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(workspaceRequestVO);
			responseVO.setResponses(failedResponse);
			log.error("Exception occurred:{} while creating fabric workspace project {} ", e.getMessage(), workspaceRequestVO.getName());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
    @ApiOperation(value = "Delete workspace for a given Id.", nickname = "delete", notes = "Delete workspace for a given identifier.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> delete(@ApiParam(value = "Workspace ID to be deleted",required=true) @PathVariable("id") String id){
    	
    	return null;
    }

    @Override
    @ApiOperation(value = "Get all workspaces for the user.", nickname = "getAll", notes = "Get all workspaces. This endpoints will be used to get all valid available fabric workspace records.", response = FabricWorkspacesCollectionVO.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = FabricWorkspacesCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<FabricWorkspacesCollectionVO> getAll(
    		@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
    		@ApiParam(value = "Sort workspaces by a given variable like name, createdOn", allowableValues = "name, createdOn") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort solutions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder){
    	FabricWorkspacesCollectionVO collection = new FabricWorkspacesCollectionVO();
		int defaultLimit = 15;
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
		}
		List<FabricWorkspaceVO> records =  new ArrayList<>();
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
    @ApiOperation(value = "Get workspace for a given Id.", nickname = "getById", notes = "Get workspace for a given identifier. This endpoints will be used to get fabric workspace for the given identifier.", response = FabricWorkspacesCollectionVO.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = FabricWorkspacesCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<FabricWorkspaceVO> getById(@ApiParam(value = "Fabric Workspace ID to be fetched",required=true) @PathVariable("id") String id){
    	
    	FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		
		if(!requestUser.getId().equalsIgnoreCase(creatorId)) {
				log.warn("Fabric workspace doesnt belong to User, Not authorized to use other projects",id,existingFabricWorkspace.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}else {
				return new ResponseEntity<>(existingFabricWorkspace, HttpStatus.OK);
		}
    }

	@Override
	@ApiOperation(value = "Update existing workspace.", nickname = "update", notes = "Updates an existing workspace.", response = FabricWorkspaceResponseVO.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Updated successfully", response = FabricWorkspaceResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "No workspace found to update", response = GenericMessage.class),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
	public ResponseEntity<FabricWorkspaceResponseVO> update(@ApiParam(value = "Fabric Workspace ID to be updated",required=true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating an existing workspace" ,required=true )  @Valid @RequestBody FabricWorkspaceUpdateRequestVO workspaceUpdateRequestVO){
		FabricWorkspaceResponseVO responseVO = new FabricWorkspaceResponseVO();
		GenericMessage responses = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			errors.add(new MessageDescription("Record not found"));
			responseVO.setData(null);
			responses.setErrors(errors);
			responses.setSuccess("FAILED");
			responseVO.setResponses(responses);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		if(!requestUser.getId().equalsIgnoreCase(creatorId)) {
				log.warn("Fabric workspace doesnt belong to User, Not authorized to update",id,existingFabricWorkspace.getName());
				errors.add(new MessageDescription("User is not the owner of the workspace. Not authorized to update."));
				responseVO.setData(null);
				responses.setErrors(errors);
				responses.setSuccess("FAILED");
				responseVO.setResponses(responses);
				return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}else {
			
			if(workspaceUpdateRequestVO.getArcherId()!=null)
				existingFabricWorkspace.setArcherId(workspaceUpdateRequestVO.getArcherId());
			if(workspaceUpdateRequestVO.getCostCenter()!=null)
				existingFabricWorkspace.setCostCenter(workspaceUpdateRequestVO.getCostCenter());
			if(workspaceUpdateRequestVO.getDataClassification()!=null)
				existingFabricWorkspace.setDataClassification(workspaceUpdateRequestVO.getDataClassification());
			if(workspaceUpdateRequestVO.getTypeOfProject()!=null)
				existingFabricWorkspace.setTypeOfProject(workspaceUpdateRequestVO.getTypeOfProject());
			if(workspaceUpdateRequestVO.getDivisionId()!=null)
				existingFabricWorkspace.setDivisionId(workspaceUpdateRequestVO.getDivisionId());
			if(workspaceUpdateRequestVO.getDivision()!=null)
				existingFabricWorkspace.setDivision(workspaceUpdateRequestVO.getDivision());
			if(workspaceUpdateRequestVO.getSubDivisionId()!=null)
				existingFabricWorkspace.setSubDivisionId(workspaceUpdateRequestVO.getSubDivisionId());
			if(workspaceUpdateRequestVO.getSubDivision()!=null)
				existingFabricWorkspace.setSubDivision(workspaceUpdateRequestVO.getSubDivision());
			if(workspaceUpdateRequestVO.getDepartment()!=null)
				existingFabricWorkspace.setDepartment(workspaceUpdateRequestVO.getDepartment());
			if(workspaceUpdateRequestVO.getTags()!=null)
				existingFabricWorkspace.setTags(workspaceUpdateRequestVO.getTags());
			if(workspaceUpdateRequestVO.isTermsOfUse()!=null)
				existingFabricWorkspace.setTermsOfUse(workspaceUpdateRequestVO.isTermsOfUse());
			if(workspaceUpdateRequestVO.isHasPii()!=null)
				existingFabricWorkspace.setHasPii(workspaceUpdateRequestVO.isHasPii());
			if(workspaceUpdateRequestVO.getInternalOrder()!=null)
				existingFabricWorkspace.setInternalOrder(workspaceUpdateRequestVO.getInternalOrder());
				
			try {
				FabricWorkspaceVO updatedRecord = service.create(existingFabricWorkspace);
				responseVO.setData(updatedRecord);
				responses.setSuccess("SUCCESS");
				responses.setErrors(new ArrayList<>());
				responses.setWarnings(new ArrayList<>());
				responseVO.setResponses(responses);
				log.info("Fabric workspace {} {}  updated successfully",id,existingFabricWorkspace.getName());
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			}catch(Exception e) {
				errors.add(new MessageDescription("Failed to update record with exception " + e.getMessage()));
				responseVO.setData(null);
				responses.setErrors(errors);
				responses.setSuccess("FAILED");
				responseVO.setResponses(responses);
				log.error("Failed to update Fabric workspace {} {} with exception {} ",id,existingFabricWorkspace.getName(),e.getMessage());
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
    
}
