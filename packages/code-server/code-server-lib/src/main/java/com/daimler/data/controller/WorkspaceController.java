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

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.workspace.CodeServerApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.CreatedByVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceRequestVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;
import com.daimler.data.dto.workspace.WorkspaceCollectionVO;
import com.daimler.data.service.workspace.WorkspaceService;

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

	@ApiOperation(value = "Initialize/Create Workbench for user in code-server.", nickname = "createWorkspace", notes = "Create workspace for user in code-server with given password", response = InitializeWorkspaceResponseVO.class, tags={ "code-server", })
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
		InitializeWorkspaceResponseVO responseMessage = new InitializeWorkspaceResponseVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO reqVO = codeServerRequestVO.getData();
		reqVO.setOwner(userId);
		String password = codeServerRequestVO.getPassword();
		CodeServerWorkspaceVO existingVO = service.getByUniqueliteral(userId,"name", reqVO.getName());
		if (existingVO != null && existingVO.getName() != null) {
			responseMessage.setData(existingVO);
			responseMessage.setSuccess("EXISTING");
			log.info("workspace {} already exists for User {} ",reqVO.getName(), userId);
			return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
		}
		reqVO.setId(null);
		responseMessage = service.create(reqVO,password);
		if("SUCCESS".equalsIgnoreCase(responseMessage.getSuccess())) {
			responseStatus = HttpStatus.CREATED;
			log.info("User {} created workspace {}", userId,reqVO.getName());
		}else {
			log.info("Failed while creating workspace {} for user {}", reqVO.getName(), userId);
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
			if(vo==null || vo.getName()==null) {
				log.debug("No workspace found, returning empty");
				GenericMessage emptyResponse = new GenericMessage();
				List<MessageDescription> warnings = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No workspace found for given id and the user");
				warnings.add(msg);
				return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			}
			if(!(vo!=null && vo.getOwner()!=null && vo.getOwner().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot delete workspace, insufficient privileges. Workspace name: {}", userId,vo.getName());
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			if("DELETE_REQUESTED".equalsIgnoreCase(vo.getStatus())) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Delete workspace already requested. Your workspace is getting deleted.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addWarnings(notAuthorizedMsg);
				log.info("User {} already requested delete workspace {}", userId,vo.getName());
				return new ResponseEntity<>(errorMessage, HttpStatus.OK);
			}
			GenericMessage responseMsg = service.deleteById(userId,id);
			log.info("User {} deleted workspace {}", userId,vo.getName());
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


    @ApiOperation(value = "Deploy workspace Project for a given Id.", nickname = "deployWorkspaceProject", notes = "Delete workspace Project for a given identifier.", response = GenericMessage.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/deploy/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> deployWorkspaceProject(@ApiParam(value = "Workspace ID for the project to be deployed",required=true) @PathVariable("id") String id){
    	try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			CodeServerWorkspaceVO vo = service.getById(userId,id);
			if(vo==null || vo.getName()==null) {
				log.debug("No workspace found, returning empty");
				GenericMessage emptyResponse = new GenericMessage();
				List<MessageDescription> warnings = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No workspace found for given id and the user");
				warnings.add(msg);
				return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			}
			if(!(vo!=null && vo.getOwner()!=null && vo.getOwner().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to deploy project for workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot deploy project for workspace {}, insufficient privileges.", userId,vo.getName());
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			GenericMessage responseMsg = service.deployWorspace(userId,id);
			log.info("User {} deployed workspace {} project {}", userId,vo.getName(),vo.getRecipeId());
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
    @RequestMapping(value = "/workspaces/deploy/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> undeployWorkspaceProject(@ApiParam(value = "Workspace ID for the project to be undeployed",required=true) @PathVariable("id") String id){
    	try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			CodeServerWorkspaceVO vo = service.getById(userId,id);
			if(vo==null || vo.getName()==null) {
				log.debug("No workspace found, returning empty");
				GenericMessage emptyResponse = new GenericMessage();
				List<MessageDescription> warnings = new ArrayList<>();
				MessageDescription msg = new MessageDescription();
				msg.setMessage("No workspace found for given id and the user");
				warnings.add(msg);
				return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			}
			if(!(vo!=null && vo.getName()!=null && vo.getOwner()!=null && vo.getOwner().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to undeploy project for workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot undeploy project for workspace {}, insufficient privileges.", userId,vo.getName());
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			GenericMessage responseMsg = service.undeployWorspace(userId,id);
			log.info("User {} undeployed workspace {} project {}", userId,vo.getName(),vo.getRecipeId());
			return new ResponseEntity<>(responseMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No Workspace with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.error("No workspace found with id {}, failed to undeploy", id);
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
    public ResponseEntity<WorkspaceCollectionVO> getAll(){
    	CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
    	final List<CodeServerWorkspaceVO> workspaces = service.getAll(userId,0,0);
    	WorkspaceCollectionVO collection = new WorkspaceCollectionVO();
		log.debug("Sending all algorithms");
		if (workspaces != null && workspaces.size() > 0) {
			collection.setRecords(workspaces);
			collection.setTotalCount(service.getCount(userId));
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
		if (vo != null && vo.getName()!=null) {
			if(!(vo.getOwner()!=null && vo.getOwner().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to view this workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot view other's workspace, insufficient privileges. Workspace name: {}", userId,vo.getName());
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
		CodeServerWorkspaceVO vo = service.getByUniqueliteral(userId,"name",name);
		if (vo != null && vo.getName()!=null) {
			if(!(vo.getOwner()!=null && vo.getOwner().equalsIgnoreCase(userId))) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to view this workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot view other's workspace, insufficient privileges. Workspace name: {}", userId,vo.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
			log.info("Returning workspace details");
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} else {
			log.debug("No workspace found, returning empty");
			return new ResponseEntity<>(vo, HttpStatus.NOT_FOUND);
		}
    }


}
