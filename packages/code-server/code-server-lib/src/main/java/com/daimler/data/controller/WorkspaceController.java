/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import com.daimler.data.dto.workspace.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.workspace.CodeServerApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.GitClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CloudServiceProviderEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CpuCapacityEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.EnvironmentEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.OperatingSystemEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RamSizeEnum;
import com.daimler.data.service.workspace.WorkspaceService;
import com.daimler.data.util.ConstantsUtility;

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
public class WorkspaceController  implements CodeServerApi{

	@Autowired
	private WorkspaceService service;

	@Autowired
	private UserStore userStore;
	
	@Autowired
	private GitClient gitClient;	

	@Override
	@ApiOperation(value = "remove collaborator from workspace project for a given Id.", nickname = "removeCollab", notes = "remove collaborator from workspace project for a given identifier.", response = CodeServerWorkspaceVO.class, tags={ "code-server", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/workspaces/{id}/collaborator/{userid}",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> removeCollab(
			@ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id")String id,
			@ApiParam(value = "User ID to be deleted", required = true) @PathVariable("userid") String userid) {
		CreatedByVO currentUser = this.userStore.getVO();
		String currentUserUserId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO vo = service.getById(currentUserUserId, id);
		GenericMessage responseMessage = new GenericMessage();

		if (vo == null || vo.getWorkspaceId() == null) {
			log.debug("No workspace found, returning empty");
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errorMessage = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("No workspace found for given id and the user");
			errorMessage.add(msg);
			emptyResponse.addErrors(msg);
			emptyResponse.setSuccess("FAILED");
			emptyResponse.setErrors(errorMessage);
			return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
		}

		if (!(vo != null && vo.getWorkspaceOwner() != null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(currentUserUserId))) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage(
					"Not authorized to update workspace. User does not have privileges.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			log.info("User {} cannot update workspace, insufficient privileges. Workspace name: {}", currentUserUserId, vo.getWorkspaceId());
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}


		// To check is user is already a part of workspace.
		boolean isCollabroratorAlreadyExits = false;
		if (vo.getProjectDetails().getProjectCollaborators() != null) {
			for (UserInfoVO collaborator : vo.getProjectDetails().getProjectCollaborators()) {
				if (userid != null) {
					if (collaborator.getId().equalsIgnoreCase(userid)) {
						isCollabroratorAlreadyExits = true;
					}
				}
			}
		}

		if (isCollabroratorAlreadyExits) {
			responseMessage = service.removeCollabById(currentUserUserId, vo, userid);
		} else {
			log.error("User is not part of a collaborator list");
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("User is not part of a collaborator list");
			errors.add(msg);
			emptyResponse.setSuccess("FAILED");
			emptyResponse.setErrors(errors);
			return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}

	
	@Override
	@ApiOperation(value = "Add collaborator to existing workspace Project for a given Id.", nickname = "addCollab", notes = "Add collaborator to existing workspace Project ", response = CodeServerWorkspaceVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}/collaborator",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> addCollab(@ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id, @ApiParam(value = "Userinfo to add collaborator to project", required = true) @Valid @RequestBody UserInfoVO userRequestDto) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO vo = service.getById(userId, id);
		GenericMessage responseMessage = new GenericMessage();

		if (vo == null || vo.getWorkspaceId() == null) {
			log.debug("No workspace found, returning empty");
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errorMessage = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("No workspace found for given id and the user");
			errorMessage.add(msg);
			emptyResponse.addErrors(msg);
			emptyResponse.setSuccess("FAILED");
			emptyResponse.setErrors(errorMessage);
			return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
		}

		if (!(vo != null && vo.getWorkspaceOwner() != null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage(
					"Not authorized to update workspace. User does not have privileges.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			log.info("User {} cannot update workspace, insufficient privileges. Workspace name: {}", userId, vo.getWorkspaceId());
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}

		// To check is user is already a part of workspace.
		boolean isCollabroratorAlreadyExits = false;
		if (vo.getProjectDetails().getProjectCollaborators() != null) {
			for (UserInfoVO collaborator : vo.getProjectDetails().getProjectCollaborators()) {
				if (userRequestDto.getId() != null) {
					if (collaborator.getId().equalsIgnoreCase(userRequestDto.getId())) {
						isCollabroratorAlreadyExits = true;
					}
				}
			}
		}

		if (isCollabroratorAlreadyExits || userRequestDto.getId() == null) {
			log.error("User is already part of a collaborator");
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("User is already part of a collaborator");
			errors.add(msg);
			emptyResponse.setErrors(errors);
			emptyResponse.setSuccess("FAILED");
			return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		}

		responseMessage = service.addCollabById(userId, vo, userRequestDto);

		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Initialize Workbench for user.", nickname = "initializeWorkspace", notes = "Initialize workbench for collab user", response = InitializeWorkspaceResponseVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeWorkspaceResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<InitializeWorkspaceResponseVO> initializeWorkspace(@ApiParam(value = "Workspace ID to be fetched",required=true) @PathVariable("id") String id,@ApiParam(value = "Request Body that contains data required for intialize code server workbench for user" ,required=true )  @Valid @RequestBody InitializeCollabWorkspaceRequestVO initializeCollabWSRequestVO){
		HttpStatus responseStatus = HttpStatus.OK;
		CreatedByVO currentUser = this.userStore.getVO();
		InitializeWorkspaceResponseVO responseMessage = new InitializeWorkspaceResponseVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO collabUserVO = service.getById(userId, id);
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		responseMessage.setData(collabUserVO);
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess("FAILED");
		if (collabUserVO != null && collabUserVO.getWorkspaceId()!=null ) {
			String status = collabUserVO.getStatus();
			if(status!=null) {
				if(!ConstantsUtility.COLLABREQUESTEDSTATE.equalsIgnoreCase(status)) {
					MessageDescription errMsg = new MessageDescription("Cannot reinitiate the workbench");
					errors.add(errMsg);
					responseMessage.setErrors(errors);
					return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
				}
			}
		}else {
			MessageDescription errMsg = new MessageDescription("Cannot reinitiate the workbench");
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			responseMessage.setData(null);
			return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
		}
		String password = initializeCollabWSRequestVO.getPassword();
		String pat = initializeCollabWSRequestVO.getPat();
		
		InitializeWorkspaceResponseVO responseData = service.initiateWorkspace(collabUserVO, pat, password);
		return new ResponseEntity<>(responseData, responseStatus);
	}


	@ApiOperation(value = "Create Workbench for user in code-server.", nickname = "createWorkspace", notes = "Create workspace for user in code-server with given password", response = InitializeWorkspaceResponseVO.class, tags={ "code-server", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeWorkspaceResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/workspaces/{id}/projectowner",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.PATCH)
	@Override
	public ResponseEntity<GenericMessage> reassignOwner(@ApiParam(value = "Workspace ID to be fetched",required=true) @PathVariable("id") String id,
		@ApiParam(value = "UserId to add collaborator as Owner", required = true) @Valid @RequestBody UserIdVO userIdDto) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO vo = service.getById(userId, id);
		GenericMessage responseMessage = new GenericMessage();

		if (userIdDto.getId() == null) {
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errorMessage = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("User Id is required");
			errorMessage.add(msg);
			emptyResponse.addErrors(msg);
			emptyResponse.setSuccess("FAILED");
			emptyResponse.setErrors(errorMessage);
			return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		}

		if (vo == null || vo.getWorkspaceId() == null) {
			log.debug("No workspace found, returning empty");
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errorMessage = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("No workspace found for given id and the user");
			errorMessage.add(msg);
			emptyResponse.addErrors(msg);
			emptyResponse.setSuccess("FAILED");
			emptyResponse.setErrors(errorMessage);
			return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
		}

		if (!(vo != null && vo.getWorkspaceOwner() != null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage(
					"Not authorized to reassign owner of workspace. User does not have privileges.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			log.info("User {} cannot reassign owner, insufficient privileges. Workspace name: {}", userId, vo.getWorkspaceId());
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}

		// To check is user is already a part of workspace.
		boolean isCollabroratorAlreadyExits = false;
		UserInfoVO newOwnerDeatils = new UserInfoVO();
		if (vo.getProjectDetails().getProjectCollaborators() != null) {
			for (UserInfoVO collaborator : vo.getProjectDetails().getProjectCollaborators()) {
				if (userIdDto.getId() != null) {
					if (collaborator.getId().equalsIgnoreCase(userIdDto.getId())) {
						newOwnerDeatils = collaborator;
						isCollabroratorAlreadyExits = true;
					}
				}
			}
		}

		if (isCollabroratorAlreadyExits) {
			responseMessage = service.reassignOwner(currentUser, vo, newOwnerDeatils);
		} else {
			log.error("User is not part of a collaborator list");
			GenericMessage emptyResponse = new GenericMessage();
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription msg = new MessageDescription();
			msg.setMessage("User is not part of a collaborator list");
			errors.add(msg);
			emptyResponse.setSuccess("FAILED");
			emptyResponse.setErrors(errors);
			return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}

	@ApiOperation(value = "Create Workbench for user in code-server.", nickname = "createWorkspace", notes = "Create workspace for user in code-server with given password", response = InitializeWorkspaceResponseVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeWorkspaceResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<InitializeWorkspaceResponseVO> createWorkspace(@ApiParam(value = "Request Body that contains data required for intialize code server workbench for user" ,required=true )  @Valid @RequestBody InitializeWorkspaceRequestVO codeServerRequestVO){
		HttpStatus responseStatus = HttpStatus.OK;
		CreatedByVO currentUser = this.userStore.getVO();
		UserInfoVO currentUserVO = new UserInfoVO();
		BeanUtils.copyProperties(currentUser, currentUserVO);
		
		InitializeWorkspaceResponseVO responseMessage = new InitializeWorkspaceResponseVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO reqVO = codeServerRequestVO.getData();
		String password = codeServerRequestVO.getPassword();
		String pat = codeServerRequestVO.getPat();
		CodeServerWorkspaceVO existingVO = service.getByProjectName(userId,reqVO.getProjectDetails().getProjectName());
		if (existingVO != null && existingVO.getWorkspaceId() != null) {
			responseMessage.setData(existingVO);
			responseMessage.setSuccess("EXISTING");
			log.info("workspace {} already exists for User {} ",reqVO.getProjectDetails().getProjectName() , userId);
			return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
		}		
		if(reqVO.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")){
			String publicUrl = reqVO.getProjectDetails().getRecipeDetails().getRepodetails();
			if("".equals(publicUrl) || publicUrl == null) {
				List<MessageDescription> errorMessage = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No Repodetails found for given public recipe");
				errorMessage.add(msg);
				responseMessage.setErrors(errorMessage);
				return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
			}
		}
		currentUserVO.setGitUserName(reqVO.getGitUserName());
		reqVO.setWorkspaceOwner(currentUserVO);
		reqVO.setId(null);
		reqVO.setWorkspaceId(null);
		reqVO.setWorkspaceUrl("");
		reqVO.setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
		reqVO.getProjectDetails().setGitRepoName(reqVO.getProjectDetails().getProjectName());
		reqVO.getProjectDetails().setIntDeploymentDetails(new CodeServerDeploymentDetailsVO());
		reqVO.getProjectDetails().setProjectOwner(currentUserVO);
		reqVO.getProjectDetails().setProdDeploymentDetails(new CodeServerDeploymentDetailsVO());
		CodeServerRecipeDetailsVO newRecipeVO = reqVO.getProjectDetails().getRecipeDetails();
		newRecipeVO.setCloudServiceProvider(CloudServiceProviderEnum.DHC_CAAS);
		newRecipeVO.setCpuCapacity(CpuCapacityEnum._1);
		newRecipeVO.setEnvironment(EnvironmentEnum.DEVELOPMENT);
		newRecipeVO.setOperatingSystem(OperatingSystemEnum.DEBIAN_OS_11);
		newRecipeVO.setRecipeId(reqVO.getProjectDetails().getRecipeDetails().getRecipeId());
		newRecipeVO.setRepodetails(reqVO.getProjectDetails().getRecipeDetails().getRepodetails());
		newRecipeVO.setRamSize(RamSizeEnum._1);
		reqVO.getProjectDetails().setRecipeDetails(newRecipeVO);
		responseMessage = service.createWorkspace(reqVO,pat,password);		
		if("SUCCESS".equalsIgnoreCase(responseMessage.getSuccess())) {
			responseStatus = HttpStatus.CREATED;
			log.info("User {} created workspace {}", userId,reqVO.getProjectDetails().getProjectName());
		}else {
			log.info("Failed while creating workspace {} for user {}", reqVO.getProjectDetails().getProjectName(), userId);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(responseMessage, responseStatus);
	}


    @ApiOperation(value = "Delete workspace for a given Id.", nickname = "deleteWorkspace", notes = "Delete workspace for a given identifier.", response = GenericMessage.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteWorkspace(@ApiParam(value = "Workspace ID to be deleted",required=true) @PathVariable("id") String id){
    	try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			CodeServerWorkspaceVO vo = service.getById(userId,id);
			if(vo==null || vo.getWorkspaceId()==null) {
				log.debug("No workspace found, returning empty");
				GenericMessage emptyResponse = new GenericMessage();
				List<MessageDescription> warnings = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No workspace found for given id and the user");
				warnings.add(msg);
				return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			}
			if(!(vo!=null && vo.getWorkspaceOwner()!=null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot delete workspace, insufficient privileges. Workspace name: {}", userId,vo.getWorkspaceId());
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			if("DELETE_REQUESTED".equalsIgnoreCase(vo.getStatus())) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Delete workspace already requested. Your workspace is getting deleted.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addWarnings(notAuthorizedMsg);
				log.info("User {} already requested delete workspace {}", userId,vo.getWorkspaceId());
				return new ResponseEntity<>(errorMessage, HttpStatus.OK);
			}
			GenericMessage responseMsg = service.deleteById(userId,id);
			log.info("User {} deleted workspace {}", userId,vo.getWorkspaceId());
			return new ResponseEntity<>(responseMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No Workspace with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.error("No workspace found with id {}, failed to delete", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to delete workspace {}, with exception {}", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }


    @ApiOperation(value = "Deploy workspace Project for a given Id.", nickname = "deployWorkspaceProject", notes = "deploy workspace Project for a given identifier.", response = GenericMessage.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}/deploy",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> deployWorkspaceProject(@ApiParam(value = "Workspace ID for the project to be deployed",required=true) @PathVariable("id") String id,@ApiParam(value = "Workspace ID for the project to be deployed" ,required=true )  @Valid @RequestBody ManageDeployRequestDto deployRequestDto){
    	try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			CodeServerWorkspaceVO vo = service.getById(userId,id);
			if(vo==null || vo.getWorkspaceId()==null) {
				log.debug("No workspace found, returning empty");
				GenericMessage emptyResponse = new GenericMessage();
				List<MessageDescription> warnings = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No workspace found for given id and the user");
				warnings.add(msg);
				return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			}
			List<String> authorizedUsers = new ArrayList<>();
			if(vo.getProjectDetails()!=null && vo.getProjectDetails().getProjectOwner()!=null ) {
				String owner = vo.getProjectDetails().getProjectOwner().getId();
				authorizedUsers.add(owner);
			}
			if(vo.getProjectDetails().getProjectCollaborators()!= null && !vo.getProjectDetails().getProjectCollaborators().isEmpty()) {
				List<String> collabUsers= vo.getProjectDetails().getProjectCollaborators().stream().map(n->n.getId()).collect(Collectors.toList());
				authorizedUsers.addAll(collabUsers);
			}
			if(!authorizedUsers.contains(userId)) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to deploy project for workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot deploy project for workspace {}, insufficient privileges.", userId,vo.getWorkspaceId());
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			if("default".equalsIgnoreCase(vo.getProjectDetails().getRecipeDetails().getRecipeId().name())) {
				MessageDescription invalidTypeMsg = new MessageDescription();
				invalidTypeMsg.setMessage(
						"Invalid type, cannot deploy this type of recipe");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(invalidTypeMsg);
				log.info("User {} cannot deploy project of recipe {} for workspace {}, invalid type.", userId, vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			}
			String environment = "int";
			String branch = "main";
			if(deployRequestDto!=null && !"int".equalsIgnoreCase(deployRequestDto.getTargetEnvironment().name())){
				environment = "prod";
			}
			if(deployRequestDto!=null && deployRequestDto.getBranch()!=null) {
				branch = deployRequestDto.getBranch();
			}
			GenericMessage responseMsg = service.deployWorkspace(userId, id, environment, branch);
			if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				log.info("User {} deployed workspace {} project {}", userId,vo.getWorkspaceId(),vo.getProjectDetails().getRecipeDetails().getRecipeId().name());
			}			
			return new ResponseEntity<>(responseMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No Workspace with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.error("No workspace found with id {}, failed to deploy", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to deploy workspace {}, with exception {}", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to deploy due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @ApiOperation(value = "undeploy workspace project for a given Id.", nickname = "undeployWorkspaceProject", notes = "undeploy workspace project for a given identifier.", response = GenericMessage.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}/deploy",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> undeployWorkspaceProject(@ApiParam(value = "Workspace ID for the project to be undeployed",required=true) @PathVariable("id") String id,@ApiParam(value = "Workspace ID for the project to be deployed" ,required=true )  @Valid @RequestBody ManageDeployRequestDto deployRequestDto){
    	try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			CodeServerWorkspaceVO vo = service.getById(userId,id);
			if(vo==null || vo.getWorkspaceId()==null) {
				log.debug("No workspace found, returning empty");
				GenericMessage emptyResponse = new GenericMessage();
				List<MessageDescription> warnings = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No workspace found for given id and the user");
				warnings.add(msg);
				return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			}
			List<String> authorizedUsers = new ArrayList<>();
			if(vo.getProjectDetails()!=null && vo.getProjectDetails().getProjectOwner()!=null ) {
				String owner = vo.getProjectDetails().getProjectOwner().getId();
				authorizedUsers.add(owner);
			}
			if(vo.getProjectDetails().getProjectCollaborators()!= null && !vo.getProjectDetails().getProjectCollaborators().isEmpty()) {
				List<String> collabUsers= vo.getProjectDetails().getProjectCollaborators().stream().map(n->n.getId()).collect(Collectors.toList());
				authorizedUsers.addAll(collabUsers);
			}
			if(!authorizedUsers.contains(userId)) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to undeploy project for workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot undeploy project for workspace {}, insufficient privileges.", userId,vo.getWorkspaceId());
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			if("default".equalsIgnoreCase(vo.getProjectDetails().getRecipeDetails().getRecipeId().name())) {
				MessageDescription invalidTypeMsg = new MessageDescription();
				invalidTypeMsg.setMessage(
						"Invalid type, cannot undeploy this type of recipe");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(invalidTypeMsg);
				log.info("User {} cannot undeploy project of recipe {} for workspace {}, invalid type.", userId, vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			}
			String environment = "int";
			String branch = "main";
			if(deployRequestDto!=null && !"int".equalsIgnoreCase(deployRequestDto.getTargetEnvironment().name())){
				environment = "prod";
			}
			if(deployRequestDto!=null && deployRequestDto.getBranch()!=null) {
				branch = deployRequestDto.getBranch();
			}
			GenericMessage responseMsg = service.undeployWorkspace(userId, id, environment, branch);
			if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				log.info("User {} undeployed workspace {} project {}", userId,vo.getWorkspaceId(),vo.getProjectDetails().getRecipeDetails().getRecipeId().name());
			}		
			return new ResponseEntity<>(responseMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No Workspace with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.error("No workspace found with id {}, failed to deploy", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to undeploy workspace {}, with exception {}", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to undeploy due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }


    @ApiOperation(value = "Get all codeServer workspaces for the user.", nickname = "getAll", notes = "Get all codeServer workspaces for the user.", response = WorkspaceCollectionVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = WorkspaceCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<WorkspaceCollectionVO> getAll(@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,@ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit){
    	CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if(offset==null) {
			offset = 0;
		}
		if(limit==null) {
			limit = 0;
		}
		 
    	final List<CodeServerWorkspaceVO> workspaces = service.getAll(userId,offset,limit);
    	WorkspaceCollectionVO collection = new WorkspaceCollectionVO();
    	collection.setTotalCount(service.getCount(userId));
		log.debug("Sending all workspaces");
		if (workspaces != null && workspaces.size() > 0) {
			collection.setRecords(workspaces);
			return new ResponseEntity<>(collection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(collection, HttpStatus.NO_CONTENT);
		}
    }


    @ApiOperation(value = "Get workspace details for a given Id.", nickname = "getById", notes = "Get workspace details for a given Id.", response = CodeServerWorkspaceVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<CodeServerWorkspaceVO> getById(@ApiParam(value = "Workspace ID to be fetched",required=true) @PathVariable("id") String id){
    	
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		CodeServerWorkspaceVO vo = service.getById(userId,id);
		if (vo != null && vo.getWorkspaceId()!=null) {
			if(!(vo.getWorkspaceOwner()!=null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to view this workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot view other's workspace, insufficient privileges. Workspace name: {}", userId,vo.getWorkspaceId());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
			log.info("Returning workspace details");
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} else {
			log.debug("No workspace found, returning empty");
			return new ResponseEntity<>(vo, HttpStatus.NOT_FOUND);
		}
    }


    @ApiOperation(value = "Get workspace details for a given Id.", nickname = "getByName", notes = "Get workspace details for a given Id.", response = CodeServerWorkspaceVO.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/status/{name}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<CodeServerWorkspaceVO> getByName(@ApiParam(value = "Workspace name to be fetched",required=true) @PathVariable("name") String name){
    	CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		CodeServerWorkspaceVO vo = service.getByUniqueliteral(userId,"workspaceId",name);
		if (vo != null && vo.getWorkspaceId()!=null) {
			if(!(vo.getWorkspaceOwner()!=null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to view this workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot view other's workspace, insufficient privileges. Workspace name: {}", userId,vo.getWorkspaceId());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
			log.info("Returning workspace details");
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} else {
			log.debug("No workspace found, returning empty");
			return new ResponseEntity<>(vo, HttpStatus.NOT_FOUND);
		}
    }

	@Override
	@ApiOperation(value = "Number of workspace.", nickname = "getNumberOfWorkspace", notes = "Get number of workspace. This endpoints will be used to get all valid available workspace records.", response = TransparencyVO.class, tags={ "code-server", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/workspaces/transparency",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfWorkspace() {
		try {

			Integer count = service.getTotalCountOfWorkSpace();
			TransparencyVO transparencyVO = new TransparencyVO();
			transparencyVO.setCount(count);
			log.info("Workspace count fetched successfully");
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e){
			log.error("Failed to fetch count of workspaces with exception {} ", e.getMessage());
			return  new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


}
