package com.daimler.data.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.text.ParseException;
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

import com.daimler.data.api.fabricWorkspace.FabricWorkspacesApi;
import com.daimler.data.api.fabricWorkspace.LovsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.application.client.AuthoriserClient;
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.MicrosoftGroupDetailDto;
import com.daimler.data.dto.fabricWorkspace.CreateRoleRequestVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricLakehouseCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricShortcutsCollectionVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceRoleRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceUpdateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.RolesVO;
import com.daimler.data.dto.fabricWorkspace.DnaRoleCollectionVO;
import com.daimler.data.dto.fabricWorkspace.ShortcutCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.ShortcutVO;
import com.daimler.data.service.fabric.FabricWorkspaceService;

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
public class FabricWorkspaceController implements FabricWorkspacesApi, LovsApi
{
	@Autowired
	private FabricWorkspaceService service;

	@Autowired
	private UserStore userStore;

	@Autowired
	private AuthoriserClient identityClient;

	@Autowired
	private FabricWorkspaceClient fabricWorkspaceClient;
	
	@Value("${fabricWorkspaces.subgroupPrefix}")
	private String subgroupPrefix;
	
	@Value("${authoriser.applicationId}")
	private String applicationId;
	
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
		if(workspaceRequestVO==null || workspaceRequestVO.getName()==null || workspaceRequestVO.getTypeOfProject() ==null ) {
			log.error("Fabric workspace project mandatory fields cannot be null, please check and send valid input");
			MessageDescription invalidMsg = new MessageDescription("Fabric workspace name/type of project cannot be null, please check and send valid input");
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(workspaceRequestVO);
			responseVO.setResponses(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}else {
				if(workspaceRequestVO.getDescription()==null || workspaceRequestVO.getDivision() == null || workspaceRequestVO.getDataClassification() ==null
				|| workspaceRequestVO.isHasPii() == null || workspaceRequestVO.isTermsOfUse() == null || workspaceRequestVO.getCostCenter() == null || workspaceRequestVO.getDepartment() == null){
					log.error("Fabric workspace project mandatory fields cannot be null for project, please check and send valid input.");
					MessageDescription invalidMsg = new MessageDescription("Fabric workspace project mandatory fields cannot be null for project, please check and send valid input.");
					errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
					errorMessage.addErrors(invalidMsg);
					responseVO.setData(workspaceRequestVO);
					responseVO.setResponses(errorMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
		}
		workspaceRequestVO.setName(workspaceRequestVO.getName().trim());
		if(workspaceRequestVO!=null && workspaceRequestVO.getName()!=null && "Admin monitoring".equalsIgnoreCase(workspaceRequestVO.getName())) {
			log.error("Fabric workspace project name cannot be Admin monitoring, cannot use reserve keyword. Please send valid input");
			MessageDescription invalidMsg = new MessageDescription("Fabric workspace project name cannot be Admin monitoring, cannot use reserve keyword. Please send valid input");
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(workspaceRequestVO);
			responseVO.setResponses(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}
		if(workspaceRequestVO!=null && workspaceRequestVO.getName()!=null && workspaceRequestVO.getName().matches(".*[^a-zA-Z0-9-_].*")) {
			log.error("Fabric workspace project name {} may only consist of letters, numbers, hyphens, or an underscore. Please send valid input",workspaceRequestVO.getName());
			MessageDescription invalidMsg = new MessageDescription("Fabric workspace project may only consist of letters, numbers, hyphens, or an underscore. Please send valid input");
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(workspaceRequestVO);
			responseVO.setResponses(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}

		// if(workspaceRequestVO.getSecondaryRoleApproverId()!=null && !"".equalsIgnoreCase(workspaceRequestVO.getSecondaryRoleApproverId()))
		// {
		// 	CreatedByVO secondaryRoleApproverDetails = identityClient.getUserDetails(workspaceRequestVO.getSecondaryRoleApproverId());
		// 		if(!(secondaryRoleApproverDetails != null && (secondaryRoleApproverDetails.getId()!=null || !"".equalsIgnoreCase(workspaceRequestVO.getSecondaryRoleApproverId())))){
		// 			log.error("couldnt get the secondary role approver details for id: {}.",workspaceRequestVO.getSecondaryRoleApproverId());
		// 			MessageDescription invalidMsg = new MessageDescription("couldnt get the secondary role approver details, please provide valid userId.");
		// 			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
		// 			errorMessage.addErrors(invalidMsg);
		// 			responseVO.setData(workspaceRequestVO);
		// 			responseVO.setResponses(errorMessage);
		// 			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		// 		}
		// }

		// if(workspaceRequestVO.getCustomEntitlementName()!=null && !"".equalsIgnoreCase(workspaceRequestVO.getCustomEntitlementName()))
		// {
		// 	EntiltlemetDetailsDto entitlementDetails = identityClient.getEntitlement(workspaceRequestVO.getCustomEntitlementName());
		// 		if(!(entitlementDetails!=null && entitlementDetails.getEntitlementId()!=null)){
		// 				log.error("couldnt get custom entitlement details for name: {}.",workspaceRequestVO.getCustomEntitlementName());
		// 				MessageDescription invalidMsg = new MessageDescription("couldnt get the custom entitlement details, please provide valid entitlement name.");
		// 				errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
		// 				errorMessage.addErrors(invalidMsg);
		// 				responseVO.setData(workspaceRequestVO);
		// 				responseVO.setResponses(errorMessage);
		// 				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		// 		}
		// }
		if(workspaceRequestVO.getCustomGroupName()!=null && !"".equalsIgnoreCase(workspaceRequestVO.getCustomGroupName())){
			MicrosoftGroupDetailDto searchResult = fabricWorkspaceClient.searchGroup(workspaceRequestVO.getCustomGroupName());
					if(! (searchResult!=null && searchResult.getId()!=null)) {
						GenericMessage failedResponse = new GenericMessage();
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("couldnt get group details for name:"+workspaceRequestVO.getCustomGroupName()+ " Failed to create workspace");
						messages.add(message);
						failedResponse.addErrors(message);
						failedResponse.setSuccess(HttpStatus.BAD_REQUEST.name());
						responseVO.setData(workspaceRequestVO);
						responseVO.setResponses(failedResponse);
						log.error("couldnt get group details for name {}, Failed to create workspace ",workspaceRequestVO.getCustomGroupName());
						return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
					}else{
						workspaceRequestVO.setCustomGroupName(searchResult.getDisplayName());
					}			
		}

		CreatedByVO requestUser = this.userStore.getVO();
		List<MessageDescription> errors = new ArrayList<>();

		if(!isTechnicalUser(requestUser.getId())){
			workspaceRequestVO.setCreatedBy(requestUser);
			workspaceRequestVO.setInitiatedBy(null);
		}else{

			if(workspaceCreateVO.getAliasOwnerId()!=null && !"".equalsIgnoreCase(workspaceCreateVO.getAliasOwnerId())){
				CreatedByVO aliasOwnerDetails = identityClient.getUserDetails(workspaceCreateVO.getAliasOwnerId());
				if(aliasOwnerDetails != null && (aliasOwnerDetails.getId()!=null || !"".equalsIgnoreCase(workspaceCreateVO.getAliasOwnerId())) ){
					workspaceRequestVO.setCreatedBy(aliasOwnerDetails);
					workspaceRequestVO.setInitiatedBy(requestUser.getId());
				}else{
					GenericMessage failedResponse = new GenericMessage();
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("couldnt get alias owner details for id:"+workspaceCreateVO.getAliasOwnerId()+ " Failed to create workspace");
					messages.add(message);
					failedResponse.addErrors(message);
					failedResponse.setSuccess(HttpStatus.BAD_REQUEST.name());
					responseVO.setData(workspaceRequestVO);
					responseVO.setResponses(failedResponse);
					log.error("couldnt get alias user details for id:{}",workspaceCreateVO.getAliasOwnerId());
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}else{
				GenericMessage failedResponse = new GenericMessage();
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Technical user cannot create fabric workspace without alias owner, Bad Request...");
					messages.add(message);
					failedResponse.addErrors(message);
					failedResponse.setSuccess("FAILED");
					responseVO.setData(workspaceRequestVO);
					responseVO.setResponses(failedResponse);
					log.error("Technical user cannot create fabric workspace without alias owner, for workspace name {}",workspaceRequestVO.getName());
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
		}
		try {
			workspaceRequestVO.setId(null);
			workspaceRequestVO.setCreatedOn(new Date());
			ResponseEntity<FabricWorkspaceResponseVO> responseFromService = service.createWorkspace(workspaceRequestVO);
			return responseFromService;
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
		FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		String initiatedBy = Optional.ofNullable(existingFabricWorkspace.getInitiatedBy()).orElse("");
		if(!requestUser.getId().equalsIgnoreCase(creatorId) && !requestUser.getId().equalsIgnoreCase(initiatedBy)) {
				log.warn("Fabric workspace {} {} doesnt belong to User {} , Not authorized to use others project",id,existingFabricWorkspace.getName(),requestUser.getId()	);
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}else {
			GenericMessage deleteResponse = service.delete(id,false);
			if(deleteResponse!=null) {
				if("SUCCESS".equalsIgnoreCase(deleteResponse.getSuccess())) {
					return new ResponseEntity<>(deleteResponse, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(deleteResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else {
				GenericMessage response = new GenericMessage();
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription("");
				errors.add(errMsg);
				response.setErrors(errors);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
    }
	
	@ApiOperation(value = "Delete lakehouse for a given workspace and lakehouse Id.", nickname = "deleteLakehouse", notes = "Delete lakehouse for a given identifier.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/lakehouses/{lakehouseId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteLakehouse(@ApiParam(value = "Workspace ID to be deleted",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Workspace ID to be deleted",required=true) @PathVariable("lakehouseId") String lakehouseId){
		FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		if(!requestUser.getId().equalsIgnoreCase(creatorId)) {
				log.warn("Fabric workspace {} {} doesnt belong to User {} , Not authorized to use others project",id,existingFabricWorkspace.getName(),requestUser.getId()	);
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}else {
			GenericMessage deleteResponse = service.deleteLakehouse(id,lakehouseId);
			if(deleteResponse!=null) {
				if("SUCCESS".equalsIgnoreCase(deleteResponse.getSuccess())) {
					return new ResponseEntity<>(deleteResponse, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(deleteResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else {
				GenericMessage response = new GenericMessage();
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription("");
				errors.add(errMsg);
				response.setErrors(errors);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
    }
	
	@ApiOperation(value = "Create lakehouse for a given workspace and lakehouse Id.", nickname = "createLakehouse", notes = "Create lakehouse for a given identifier.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/lakehouses",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> createLakehouse(@ApiParam(value = "Workspace ID",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Request Body that contains data required for creating a new workspace lakehouse" ,required=true )  @Valid @RequestBody FabricLakehouseCreateRequestVO createRequestVO){
		FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		if(!requestUser.getId().equalsIgnoreCase(creatorId)) {
				log.warn("Fabric workspace {} {} doesnt belong to User {} , Not authorized to use others project",id,existingFabricWorkspace.getName(),requestUser.getId()	);
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}else {
			GenericMessage createLakehouseResponse = service.createLakehouse(id,createRequestVO);
			if(createLakehouseResponse!=null) {
				if("SUCCESS".equalsIgnoreCase(createLakehouseResponse.getSuccess())) {
					return new ResponseEntity<>(createLakehouseResponse, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(createLakehouseResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else {
				GenericMessage response = new GenericMessage();
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription("");
				errors.add(errMsg);
				response.setErrors(errors);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
    }
	
	@ApiOperation(value = "Create lakehouse s3 shortcut for a given workspace and lakehouse Id.", nickname = "createS3Shortcut", notes = "Create lakehouse s3 shortcut for a given identifier.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/lakehouses/{lakehouseId}/shortcuts",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<FabricShortcutsCollectionVO> getLakehouseS3Shortcut(@ApiParam(value = "Workspace ID",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Workspace ID to be deleted",required=true) @PathVariable("lakehouseId") String lakehouseId){
		FabricShortcutsCollectionVO response = service.getLakehouseS3Shortcuts(id,lakehouseId);
		if(response!=null && response.getTotalCount() == 0) {
			return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "Create lakehouse s3 shortcut for a given workspace and lakehouse Id.", nickname = "createS3Shortcut", notes = "Create lakehouse s3 shortcut for a given identifier.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/lakehouses/{lakehouseId}/shortcuts",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> createLakehouseS3Shortcut(@ApiParam(value = "Workspace ID",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Workspace ID to be deleted",required=true) @PathVariable("lakehouseId") String lakehouseId,
    		@ApiParam(value = "Request Body that contains data required for creating a new workspace lakehouse s3 shortcut" ,required=true )  @Valid @RequestBody ShortcutCreateRequestVO createRequestVO){
		FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		if(!requestUser.getId().equalsIgnoreCase(creatorId) && ! userStore.getUserInfo().hasProjectAdminAccess(id)) {
				log.warn("Fabric workspace {} {} doesnt belong to User or user not admin {} , Not authorized to use others project",id,existingFabricWorkspace.getName(),requestUser.getId()	);
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}else {
			String email = requestUser.getEmail();
			GenericMessage createLakehouseS3ShortcutResponse = service.createLakehouseS3Shortcut(id,lakehouseId,createRequestVO,email);
			if(createLakehouseS3ShortcutResponse!=null) {
				if("SUCCESS".equalsIgnoreCase(createLakehouseS3ShortcutResponse.getSuccess())) {
					return new ResponseEntity<>(createLakehouseS3ShortcutResponse, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(createLakehouseS3ShortcutResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else {
				GenericMessage response = new GenericMessage();
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription("");
				errors.add(errMsg);
				response.setErrors(errors);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
    }
	
	@ApiOperation(value = "Create lakehouse s3 shortcut for a given workspace and lakehouse Id.", nickname = "createS3Shortcut", notes = "Create lakehouse s3 shortcut for a given identifier.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/lakehouses/{lakehouseId}/shortcuts/{shortcutId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteLakehouseS3Shortcut(@ApiParam(value = "Workspace ID",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "lakehouseid",required=true) @PathVariable("lakehouseId") String lakehouseId,
    		@ApiParam(value = "shortcut id to be deleted",required=true) @PathVariable("shortcutId") String shortcutId){
		FabricWorkspaceVO existingFabricWorkspace = service.getById(id);
		if(existingFabricWorkspace==null || !id.equalsIgnoreCase(existingFabricWorkspace.getId())) {
			log.warn("No Fabric Workspace found with id {}", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		if(!requestUser.getId().equalsIgnoreCase(creatorId) && ! userStore.getUserInfo().hasProjectAdminAccess(id)) {
				log.warn("Fabric workspace {} {} doesnt belong to User or user not admin {} , Not authorized to use others project",id,existingFabricWorkspace.getName(),requestUser.getId()	);
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}else {
			String path = "";
			boolean isPresent = false;
			FabricShortcutsCollectionVO response = service.getLakehouseS3Shortcuts(id,lakehouseId);
			if(response!=null && response.getRecords()!= null && !response.getRecords().isEmpty()) {
				List<ShortcutVO> records = response.getRecords();
				if(records!=null && !records.isEmpty()){
					for(ShortcutVO record : records) {
						if(record!=null && record.getName()!=null && record.getName().equalsIgnoreCase(shortcutId)) {
							isPresent = true;
							path = record.getPath() != null ? record.getPath() : "/Files";
						}
					}
				}
			}
			if(!isPresent) {
				log.warn("No Shortcut {} found with workspace id {} and lakehouse {}", shortcutId, id, lakehouseId);
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
			GenericMessage createLakehouseS3ShortcutResponse = service.deleteLakehouseS3Shortcut(id,lakehouseId,path+"/"+shortcutId);
			if(createLakehouseS3ShortcutResponse!=null) {
				if("SUCCESS".equalsIgnoreCase(createLakehouseS3ShortcutResponse.getSuccess())) {
					return new ResponseEntity<>(createLakehouseS3ShortcutResponse, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(createLakehouseS3ShortcutResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else {
				GenericMessage errResponse = new GenericMessage();
				errResponse.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription("");
				errors.add(errMsg);
				errResponse.setErrors(errors);
				return new ResponseEntity<>(errResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
    }
	

	@Override
	@ApiOperation(value = "List of values for available workspaces", nickname = "getWorkspacesLov", notes = "Get all workspaces. This endpoints will be used to get all valid available fabric workspace records.", response = FabricWorkspacesCollectionVO.class, tags={ "lovs", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = FabricWorkspacesCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/lov/fabric-workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<FabricWorkspacesCollectionVO> getWorkspacesLov(@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
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
		collection = service.getAllLov(limit, offset);
		HttpStatus responseCode = collection.getRecords()!=null && !collection.getRecords().isEmpty() ? HttpStatus.OK : HttpStatus.NO_CONTENT;
		return new ResponseEntity<>(collection, responseCode);
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
		String user = "";
		List<String> allEntitlementsList = new ArrayList<>();
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) 
			limit = defaultLimit;
		
		
		if(this.userStore.getUserInfo() ==null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null || "".equalsIgnoreCase(this.userStore.getVO().getId().trim())) 
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			
		CreatedByVO requestUser = this.userStore.getVO();
		UserInfo currentUserInfo = this.userStore.getUserInfo();
		allEntitlementsList =  currentUserInfo.getEntitlement_group();
		user = requestUser.getId();
		collection = service.getAll(limit, offset, user, allEntitlementsList, isTechnicalUser(user));
		HttpStatus responseCode = collection.getRecords()!=null && !collection.getRecords().isEmpty() ? HttpStatus.OK : HttpStatus.NO_CONTENT;
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
		UserInfo currentUserInfo = this.userStore.getUserInfo();
		List<String> allEntitlementsList = currentUserInfo.getEntitlement_group();
		List<String> filteredEntitlements = new ArrayList<>();
		if(allEntitlementsList!=null && !allEntitlementsList.isEmpty()) {
			filteredEntitlements = allEntitlementsList.stream().filter(n-> n.contains( applicationId + "." + subgroupPrefix ) && n.contains(id)).collect(Collectors.toList());
		}
		String creatorId = existingFabricWorkspace.getCreatedBy().getId();
		if(!requestUser.getId().equalsIgnoreCase(creatorId) && (filteredEntitlements==null || filteredEntitlements.isEmpty())) {
				log.warn("Fabric workspace {} {} does not belong to User {} , Not authorized to use others project",id,existingFabricWorkspace.getName(),requestUser.getId()	);
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
			if(workspaceUpdateRequestVO.getProcedureId()!=null)
				existingFabricWorkspace.setProcedureId(workspaceUpdateRequestVO.getProcedureId());
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
			
			if(workspaceUpdateRequestVO.getName()!=null)
				existingFabricWorkspace.setName(workspaceUpdateRequestVO.getName());
			if(workspaceUpdateRequestVO.getDescription()!=null)
				existingFabricWorkspace.setDescription(workspaceUpdateRequestVO.getDescription());

			existingFabricWorkspace.setRelatedReports(workspaceUpdateRequestVO.getRelatedReports());
			existingFabricWorkspace.setRelatedSolutions(workspaceUpdateRequestVO.getRelatedSolutions());
			try {
				SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
				Date now = isoFormat.parse(isoFormat.format(new Date()));
				existingFabricWorkspace.setLastModifiedOn(now);
				FabricWorkspaceVO updatedRecord = service.updateFabricProject(existingFabricWorkspace);
				responseVO.setData(updatedRecord);
				responses.setSuccess("SUCCESS");
				responses.setErrors(new ArrayList<>());
				responses.setWarnings(new ArrayList<>());
				responseVO.setResponses(responses);
				log.info("Fabric workspace {} {}  updated successfully",id,existingFabricWorkspace.getName());
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			}catch(Exception e) {
				existingFabricWorkspace.setLastModifiedOn(new Date());
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


	@Override
	@ApiOperation(value = "request a  fabric workspace role for a user.", nickname = "requestRole", notes = "request a  fabric workspace role for a user.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/rolerequest",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> requestRole(@ApiParam(value = "",required=true) @PathVariable("id") String id,@ApiParam(value = "Request Body that contains data required for requesting a workspace role" ,required=true )  @Valid @RequestBody FabricWorkspaceRoleRequestVO roleRequestVO){
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		UserInfo userInfo = this.userStore.getUserInfo();
		String authToken = userInfo.getAuthToken();
		try{

			if(roleRequestVO.getData().getRoleList()==null || roleRequestVO.getData().getRoleList().isEmpty()){
				errors.add(new MessageDescription("Failed to request roles for the user, Atleast one Role Id should be there. Bad Request "));
				response.setErrors(errors);
				response.setWarnings(warnings);
				response.setSuccess("FAILED");
				log.error("Failed to request roles for the user, Atleast one Role Id should be there. Bad Request");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if(roleRequestVO.getData().getReason().length()<20){
				errors.add(new MessageDescription("Failed to request roles for the user, Reason should be atleast of 20 characters. Bad Request "));
				response.setErrors(errors);
				response.setWarnings(warnings);
				response.setSuccess("FAILED");
				log.error("Failed to request roles for the user, Reason should be atleast of 20 characters. Bad Request");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			List<RolesVO> roleList = roleRequestVO.getData().getRoleList();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			for(RolesVO role : roleList){

				LocalDate validFrom = LocalDate.parse(role.getValidFrom(), formatter);
            	LocalDate validTo = LocalDate.parse(role.getValidTo(), formatter);
				if(validTo.isBefore(validFrom)){
					errors.add(new MessageDescription("Failed to request roles for the user, validTo date must be after validFrom date. Bad Request "));
					response.setErrors(errors);
					response.setWarnings(warnings);
					response.setSuccess("FAILED");
					log.error("Failed to request roles for the user,  validTo date must be after validFrom date. Bad Request");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				response = service.requestRoles(roleRequestVO,userInfo.getId(),authToken);
				log.info("Sucessfully requested roles for  user {}, Fabric workspace {} ",id,userInfo.getId());
				return new ResponseEntity<>(response, HttpStatus.OK);

			}


		}catch(Exception e){
			errors.add(new MessageDescription("Failed to request roles for the user  with exception " + e.getMessage()));
				response.setErrors(errors);
				response.setWarnings(warnings);
				response.setSuccess("FAILED");
				log.error("Failed to request role  for user {}, Fabric workspace {} with exception {} ",id,userInfo.getId(),e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}

	@Override
	@ApiOperation(value = "create new role from dna application.", nickname = "createRole", notes = "create new role from dna application.", response = GenericMessage.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/createrole",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> createRole(@ApiParam(value = "Request Body that contains data required for requesting a workspace role" ,required=true )  @Valid @RequestBody CreateRoleRequestVO roleRequestVO){
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		UserInfo userInfo = this.userStore.getUserInfo();
		try{

			response = service.createGenericRole(roleRequestVO,userInfo.getId());
			if("SUCCESS".equalsIgnoreCase(response.getSuccess())){
				log.info("Sucessfully created role for  user {}",userInfo.getId());
				return new ResponseEntity<>(response, HttpStatus.OK);
			}else if("CONFLICT".equalsIgnoreCase(response.getSuccess())){
				log.info(" Role Already Exists.");
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			}else{
				errors.add(new MessageDescription("Failed to create roles with error"));
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}catch(Exception e){
			errors.add(new MessageDescription("Failed to create roles for the user  with exception " + e.getMessage()));
			response.setErrors(errors);
			response.setWarnings(warnings);
			response.setSuccess("FAILED");
			log.error("Failed to create role with exception {} ",e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "get all dna roles for a user.", nickname = "getAllUserDnaRoles", notes = "get all dna roles for a user", response = DnaRoleCollectionVO.class, tags={ "fabric-workspaces", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = DnaRoleCollectionVO.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/fabric-workspaces/{id}/dnaroles",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<DnaRoleCollectionVO> getAllUserDnaRoles(@ApiParam(value = "",required=true) @PathVariable("id") String id){
		UserInfo userInfo = this.userStore.getUserInfo();
		String authToken = userInfo.getAuthToken();
		DnaRoleCollectionVO roleCollection = new DnaRoleCollectionVO();
		try{

			roleCollection = service.getAllUserDnaRoles(id,authToken);

			if(roleCollection.getData().getRoles().isEmpty()){
				return new ResponseEntity<>(roleCollection, HttpStatus.NO_CONTENT);
			}else{
				return new ResponseEntity<>(roleCollection, HttpStatus.OK);
			}

		}catch(Exception e){
				log.error("Failed to request role  for user {}, Fabric workspace {} with exception {} ",id,userInfo.getId(),e.getMessage());
				return new ResponseEntity<>(roleCollection, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	public static boolean isTechnicalUser(String id) {
        if (id.length() == 7 && id.startsWith("TE")) {
            String numericPart = id.substring(2);
            if (numericPart.matches("\\d{5}")) {
                return true;
            }
        }
        return false;
    }

    
}
