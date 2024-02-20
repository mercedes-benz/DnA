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

 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.workspace.CodeServerApi;
import com.daimler.data.api.workspace.admin.CodeServerAdminApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.GitClient;
import com.daimler.data.assembler.WorkspaceAssembler;
import com.daimler.data.auth.client.DnaAuthClient;
import com.daimler.data.auth.client.UserRequestVO;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodespaceSecurityRole;
import com.daimler.data.db.json.CodespaceSecurityUserRoleMap;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.dto.workspace.CodeServerDeploymentDetailsVO;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CloudServiceProviderEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CpuCapacityEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.EnvironmentEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.OperatingSystemEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RamSizeEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.CodeServerWorkspaceValidateVO;
import com.daimler.data.dto.workspace.CodespaceSecurityConfigLOV;
import com.daimler.data.dto.workspace.CodespaceSecurityConfigVO;
import com.daimler.data.dto.workspace.CodespaceSecurityEntitlementVO;
import com.daimler.data.dto.workspace.CodespaceSecurityRoleVO;
import com.daimler.data.dto.workspace.CodespaceSecurityUserRoleMapVO;
import com.daimler.data.dto.workspace.CreatedByVO;
import com.daimler.data.dto.workspace.DataGovernanceRequestInfo;
import com.daimler.data.dto.workspace.EntitlementCollectionVO;
import com.daimler.data.dto.workspace.InitializeCollabWorkspaceRequestVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceRequestVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;
import com.daimler.data.dto.workspace.RoleCollectionVO;
import com.daimler.data.dto.workspace.SecurityConfigRequestDto;
import com.daimler.data.dto.workspace.SecurityConfigResponseDto;
import com.daimler.data.dto.workspace.TransparencyVO;
import com.daimler.data.dto.workspace.UserIdVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.dto.workspace.WorkspaceCollectionVO;
import com.daimler.data.dto.workspace.admin.CodespaceSecurityConfigCollectionVO;
import com.daimler.data.dto.workspace.admin.CodespaceSecurityConfigDetailsVO;
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
 public class WorkspaceController implements CodeServerApi, CodeServerAdminApi {
 
	 @Autowired
	 private WorkspaceService service;
 
	 @Autowired
	 private UserStore userStore;
 
	 @Autowired
	 private GitClient gitClient;
 
	 @Autowired
	 private DnaAuthClient dnaAuthClient;
 
	 @Autowired
	 private WorkspaceAssembler workspaceAssembler;

	 @Autowired
	private WorkspaceCustomRepository workspaceCustomRepository;
 
	 @Override
	 @ApiOperation(value = "remove collaborator from workspace project for a given Id.", nickname = "removeCollab", notes = "remove collaborator from workspace project for a given identifier.", response = CodeServerWorkspaceVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/collaborator/{userid}", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.DELETE)
	 public ResponseEntity<GenericMessage> removeCollab(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id,
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
 
		 if (!vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(currentUserUserId)) {
			 MessageDescription notAuthorizedMsg = new MessageDescription();
			 notAuthorizedMsg.setMessage(
					 "Not authorized to update workspace. User does not have privileges.");
			 GenericMessage errorMessage = new GenericMessage();
			 errorMessage.addErrors(notAuthorizedMsg);
			 log.info("User {} cannot update workspace, insufficient privileges. Workspace name: {}", currentUserUserId,
					 vo.getWorkspaceId());
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
			 log.error("Invalid user {} for project {} removal, user not part of collaborators or user is owner. ",userid, vo.getProjectDetails().getProjectName() );
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
	 @ApiOperation(value = "Add collaborator to existing workspace Project for a given Id.", nickname = "addCollab", notes = "Add collaborator to existing workspace Project ", response = CodeServerWorkspaceVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/collaborator", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.POST)
	 public ResponseEntity<GenericMessage> addCollab(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id,
			 @ApiParam(value = "Userinfo to add collaborator to project", required = true) @Valid @RequestBody UserInfoVO userRequestDto) {
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
 
		 if (!vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(userId)) {
			 MessageDescription notAuthorizedMsg = new MessageDescription();
			 notAuthorizedMsg.setMessage(
					 "Not authorized to update workspace. User does not have privileges.");
			 GenericMessage errorMessage = new GenericMessage();
			 errorMessage.addErrors(notAuthorizedMsg);
			 log.info("User {} cannot update workspace, insufficient privileges. Workspace name: {}", userId,
					 vo.getWorkspaceId());
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
 
		 if (isCollabroratorAlreadyExits || userRequestDto.getId() == null || userId.equalsIgnoreCase(userRequestDto.getId())) {
			 log.error("User is already part of a collaborator");
			 GenericMessage emptyResponse = new GenericMessage();
			 List<MessageDescription> errors = new ArrayList<>();
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage("Invalid User, Please make sure that User is not empty and is not already part of project. Bad request");
			 errors.add(msg);
			 emptyResponse.setErrors(errors);
			 emptyResponse.setSuccess("FAILED");
			 return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		 }
 
		 responseMessage = service.addCollabById(userId, vo, userRequestDto);
 
		 return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	 }
 
	 @Override
	 @ApiOperation(value = "Save Codespace security configurations which include defining roles, entitlements, user-role mappings etc.", nickname = "saveSecurityConfig", notes = "Save Codespace security configurations", response = SecurityConfigResponseDto.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = SecurityConfigResponseDto.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.PATCH)
	 public ResponseEntity<SecurityConfigResponseDto> saveSecurityConfig(
			 @ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("id") String id,
			 @ApiParam(value = "request body for saving security config details of the project", required = true) @Valid @RequestBody SecurityConfigRequestDto configRequestDto) {
 
		 SecurityConfigResponseDto saveConfigResponse = new SecurityConfigResponseDto();
		 saveConfigResponse.setData(null);
		 CodespaceSecurityConfigVO data = configRequestDto.getData();
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
			 saveConfigResponse.setResponse(emptyResponse);
			 return new ResponseEntity<>(saveConfigResponse, HttpStatus.NOT_FOUND);
		 }
		if (vo.getStatus().equalsIgnoreCase("CREATED")) {
 
			 if (!vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(userId)) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Only owners can edit security configurations for workspace. Access Denied, user does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info(
						 "User {} cannot update security configurations for workspace, insufficient privileges. Workspace name: {}",
						 userId, vo.getWorkspaceId());
				 saveConfigResponse.setResponse(errorMessage);
				 return new ResponseEntity<>(saveConfigResponse, HttpStatus.FORBIDDEN);
			}
			if(data.isIsProtectedByDna()== null){
				data.isProtectedByDna(false);
			 }
			 if(data.isIsProtectedByDna()!=null && ! data.isIsProtectedByDna())
			 {
				 List<CodespaceSecurityEntitlementVO> entitlemtVOs = data.getEntitlements();
				 for(CodespaceSecurityEntitlementVO entitlement : entitlemtVOs)
				 {
					 entitlement.setApiList(new ArrayList<>());
				 }
			 }
			 List<CodespaceSecurityEntitlementVO> entitlementVo = data.getEntitlements();
			 Set<String> entitlementSet = new HashSet<>();
			 for(CodespaceSecurityEntitlementVO entitlement:entitlementVo )
			 {
				String name = entitlement.getName();
				if (!entitlementSet.add(name)) {
					MessageDescription badRequestMsg = new MessageDescription();
					badRequestMsg.setMessage(
							"Entitlement names should be unique. Bad request.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(badRequestMsg);
					saveConfigResponse.setResponse(errorMessage);
					log.info("Entitlement names should be unique. Bad request.");
					return new ResponseEntity<>(saveConfigResponse, HttpStatus.BAD_REQUEST);
				}
			 }
			 List<CodespaceSecurityRoleVO> roleVo = data.getRoles();
			 Set<String> roleSet = new HashSet<>();
			 for(CodespaceSecurityRoleVO role:roleVo)
			 {
				String name = role.getName();
				if (!roleSet.add(name)) {
					MessageDescription badRequestMsg = new MessageDescription();
					badRequestMsg.setMessage(
							"Role names should be unique. Bad request.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(badRequestMsg);
					saveConfigResponse.setResponse(errorMessage);
					log.info("Role names should be unique. Bad request.");
					return new ResponseEntity<>(saveConfigResponse, HttpStatus.BAD_REQUEST);
				}
				Set<String> roleEntitlementSet = new HashSet<>();
				for(CodespaceSecurityConfigLOV roleEntitlement : role.getRoleEntitlements()){
					String roleEntitlementName = roleEntitlement.getName();
					if (!roleEntitlementSet.add(roleEntitlementName)) {
						MessageDescription badRequestMsg = new MessageDescription();
						badRequestMsg.setMessage(
								"Role Entitlements  should be unique. Bad request.");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.addErrors(badRequestMsg);
						saveConfigResponse.setResponse(errorMessage);
						log.info("Role Entitlements should be unique. Bad request.");
						return new ResponseEntity<>(saveConfigResponse, HttpStatus.BAD_REQUEST);
					}
				}
			 }
			 List<CodespaceSecurityUserRoleMapVO> userRoleVo = data.getUserRoleMappings();
			 Set<String> userRoleSet = new HashSet<>();
			 for(CodespaceSecurityUserRoleMapVO userRole:userRoleVo)
			 {
				String shortId = userRole.getShortId();
				if (!userRoleSet.add(shortId)) {
					MessageDescription badRequestMsg = new MessageDescription();
					badRequestMsg.setMessage(
							"Users should be unique. Bad request.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(badRequestMsg);
					saveConfigResponse.setResponse(errorMessage);
					log.info("Users should be unique. Bad request.");
					return new ResponseEntity<>(saveConfigResponse, HttpStatus.BAD_REQUEST);
				}
				Set<String> uniqueRoles = new HashSet<>();
				for(CodespaceSecurityConfigLOV role : userRole.getRoles()){
					String roleName = role.getName();
					if (!uniqueRoles.add(roleName)) {
						MessageDescription badRequestMsg = new MessageDescription();
						badRequestMsg.setMessage(
								"User roles  should be unique. Bad request.");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.addErrors(badRequestMsg);
						saveConfigResponse.setResponse(errorMessage);
						log.info("User roles should be unique. Bad request.");
						return new ResponseEntity<>(saveConfigResponse, HttpStatus.BAD_REQUEST);
					}
				}
			 }
			
			 if (vo.getProjectDetails().getSecurityConfig() != null) {
				 if (vo.getProjectDetails().getSecurityConfig().getStatus() != null
						 && (vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("REQUESTED") || vo
								 .getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("ACCEPTED"))) {
					 MessageDescription notAuthorizedMsg = new MessageDescription();
					 notAuthorizedMsg.setMessage(
							 "Cannot edit security configurations for workspace when its in REQUESTED or ACCEPTED state. Bad request.");
					 GenericMessage errorMessage = new GenericMessage();
					 errorMessage.addErrors(notAuthorizedMsg);
					 saveConfigResponse.setResponse(errorMessage);
					 log.info(" cannot edit security configurations for workspace when its in {} state",
							 vo.getProjectDetails().getSecurityConfig().getStatus());
					 return new ResponseEntity<>(saveConfigResponse, HttpStatus.BAD_REQUEST);
				 }
				 if (vo.getProjectDetails().getSecurityConfig().getStatus() != null
						 && (vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("DRAFT") || vo
								 .getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("PUBLISHED"))) {
					 data = workspaceAssembler.generateSecurityConfigIds(data,vo.getProjectDetails().getProjectName());
					 data = workspaceAssembler.assembleSecurityConfig(vo,data);
					 vo.getProjectDetails().setSecurityConfig(data);
					 responseMessage = service.saveSecurityConfig(vo,false);
					 saveConfigResponse.setResponse(responseMessage);
					saveConfigResponse.setData(data);
					 return new ResponseEntity<>(saveConfigResponse, HttpStatus.OK);
				 }
			 }
			 data = workspaceAssembler.generateSecurityConfigIds(data,vo.getProjectDetails().getProjectName());
			 vo.getProjectDetails().setSecurityConfig(data);
			 //defaulting the security config status as DRAFT for the first time
			 vo.getProjectDetails().getSecurityConfig().setStatus("DRAFT");
			 responseMessage = service.saveSecurityConfig(vo,false);
			 saveConfigResponse.setResponse(responseMessage);
			vo = service.getById(userId, id);
			saveConfigResponse.setData(vo.getProjectDetails().getSecurityConfig());
			 return new ResponseEntity<>(saveConfigResponse, HttpStatus.OK);
		 } else {
			 GenericMessage emptyResponse = new GenericMessage();
			 List<MessageDescription> errorMessage = new ArrayList<>();
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage(" workspace not yet created for given id and the user");
			 errorMessage.add(msg);
			 emptyResponse.addErrors(msg);
			 emptyResponse.setSuccess("FAILED");
			 emptyResponse.setErrors(errorMessage);
			 saveConfigResponse.setData(null);
			 saveConfigResponse.setResponse(emptyResponse);
		 }
 
		 return new ResponseEntity<>(saveConfigResponse, HttpStatus.FORBIDDEN);
	 }
 
	 @Override
	 @ApiOperation(value = "Initialize Workbench for user.", nickname = "initializeWorkspace", notes = "Initialize workbench for collab user", response = InitializeWorkspaceResponseVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeWorkspaceResponseVO.class),
			 @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.PUT)
	 public ResponseEntity<InitializeWorkspaceResponseVO> initializeWorkspace(
			 @ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("id") String id,
			 @ApiParam(value = "Request Body that contains data required for intialize code server workbench for user", required = true) @Valid @RequestBody InitializeCollabWorkspaceRequestVO initializeCollabWSRequestVO) {
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
		 if (collabUserVO != null && collabUserVO.getWorkspaceId() != null) {
			 String status = collabUserVO.getStatus();
			 if (status != null) {
				 if (!ConstantsUtility.COLLABREQUESTEDSTATE.equalsIgnoreCase(status)
					&& !ConstantsUtility.CREATEFAILEDSTATE.equalsIgnoreCase(status)) {
					 MessageDescription errMsg = new MessageDescription("Cannot reinitiate the workbench");
					 errors.add(errMsg);
					 responseMessage.setErrors(errors);
					 return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
				 }
			 }
		 } else {
			 MessageDescription errMsg = new MessageDescription("Cannot reinitiate the workbench");
			 errors.add(errMsg);
			 responseMessage.setErrors(errors);
			 responseMessage.setData(null);
			 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
		 }
		 String pat = initializeCollabWSRequestVO.getPat();
		 
		 if(!ObjectUtils.isEmpty(collabUserVO.getProjectDetails().getProjectCollaborators())) {
			 String ownerUserId = collabUserVO.getProjectDetails().getProjectOwner().getId();
			 String projectName = collabUserVO.getProjectDetails().getProjectName();
			 CodeServerWorkspaceVO ownerCodespaceVO = service.getByProjectName(ownerUserId, projectName);
			 if(ownerCodespaceVO!= null && (!ownerCodespaceVO.getStatus().toUpperCase().equalsIgnoreCase(ConstantsUtility.CREATEDSTATE) && !ownerCodespaceVO.getWorkspaceId().equalsIgnoreCase(collabUserVO.getWorkspaceId()))) {
				 MessageDescription errMsg = new MessageDescription("Cannot intialize collaborator workbench as owner's codespace is not created yet. ");
				 errors.add(errMsg);
				 responseMessage.setErrors(errors);
				 return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
				 
			 }
		 }
		 
		 InitializeWorkspaceResponseVO responseData = service.initiateWorkspace(collabUserVO, pat);
		 return new ResponseEntity<>(responseData, responseStatus);
	 }
 
	 @ApiOperation(value = "Create Workbench for user in code-server.", nickname = "createWorkspace", notes = "Create workspace for user in code-server with given password", response = InitializeWorkspaceResponseVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeWorkspaceResponseVO.class),
			 @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/projectowner", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.PATCH)
	 @Override
	 public ResponseEntity<GenericMessage> reassignOwner(
			 @ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("id") String id,
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
 
		 if (!vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(userId)) {
			 MessageDescription notAuthorizedMsg = new MessageDescription();
			 notAuthorizedMsg.setMessage(
					 "Not authorized to reassign owner of workspace. User does not have privileges.");
			 GenericMessage errorMessage = new GenericMessage();
			 errorMessage.addErrors(notAuthorizedMsg);
			 log.info("User {} cannot reassign owner, insufficient privileges. Workspace name: {}", userId,
					 vo.getWorkspaceId());
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

		 if(vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("public") 
				|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("private")
				|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("bat")
				|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().equalsIgnoreCase("default") ) {
			 log.error("Invalid recipe type {} for Reassign action. for project {} ", vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase()
					 , vo.getProjectDetails().getProjectName());
			 GenericMessage emptyResponse = new GenericMessage();
			 List<MessageDescription> errors = new ArrayList<>();
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage("Invalid recipe type for reassign action. Bad request.");
			 errors.add(msg);
			 emptyResponse.setSuccess("FAILED");
			 emptyResponse.setErrors(errors);
			 return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		 }
		 
		 if (isCollabroratorAlreadyExits) {
			 responseMessage = service.reassignOwner(currentUser, vo, newOwnerDeatils);
		 } else {
			 log.error("User is not part of a collaborator list");
			 GenericMessage emptyResponse = new GenericMessage();
			 List<MessageDescription> errors = new ArrayList<>();
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage("User is not part of a collaborator list, can reassign only to existing team members.");
			 errors.add(msg);
			 emptyResponse.setSuccess("FAILED");
			 emptyResponse.setErrors(errors);
			 return new ResponseEntity<>(emptyResponse, HttpStatus.BAD_REQUEST);
		 }
 
		 return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	 }
 
	 @ApiOperation(value = "Create Workbench for user in code-server.", nickname = "createWorkspace", notes = "Create workspace for user in code-server with given password", response = InitializeWorkspaceResponseVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeWorkspaceResponseVO.class),
			 @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.POST)
	 public ResponseEntity<InitializeWorkspaceResponseVO> createWorkspace(
			 @ApiParam(value = "Request Body that contains data required for intialize code server workbench for user", required = true) @Valid @RequestBody InitializeWorkspaceRequestVO codeServerRequestVO) {
		 HttpStatus responseStatus = HttpStatus.OK;
		 CreatedByVO currentUser = this.userStore.getVO();
		 UserInfoVO currentUserVO = new UserInfoVO();
		 BeanUtils.copyProperties(currentUser, currentUserVO);
 
		 InitializeWorkspaceResponseVO responseMessage = new InitializeWorkspaceResponseVO();
		 String userId = currentUser != null ? currentUser.getId() : null;
		 CodeServerWorkspaceVO reqVO = codeServerRequestVO.getData();
		 String pat = codeServerRequestVO.getPat();
		 CodeServerWorkspaceVO existingVO = service.getByProjectName(reqVO.getProjectDetails().getProjectName());
		 if (existingVO != null && existingVO.getWorkspaceId() != null) {
			 responseMessage.setData(existingVO);
			 responseMessage.setSuccess("EXISTING");
			 log.info("workspace {} already exists for User {} ", reqVO.getProjectDetails().getProjectName(), userId);
			 return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
		 }
		 if (reqVO.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")
				 || reqVO.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
						 .startsWith("private")) {
			 String gitUrl = reqVO.getProjectDetails().getRecipeDetails().getRepodetails();
			 String[] url = gitUrl.split(",");
			 if (Objects.nonNull(url) && url.length == 1) {
				 log.info("Inside newUrl split block, adding default parameter to clone the project completely");
				 gitUrl = gitUrl + "/*";
				 log.info(gitUrl);
				 reqVO.getProjectDetails().getRecipeDetails().setRepodetails(gitUrl);
			 }
			 if ("".equals(gitUrl) || gitUrl == null) {
				 List<MessageDescription> errorMessage = new ArrayList<>();
				 MessageDescription msg = new MessageDescription();
				 msg.setMessage("No Repodetails found for given public/private recipe");
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
		 reqVO.getProjectDetails().setSecurityConfig(new CodespaceSecurityConfigVO());
		 CodeServerRecipeDetailsVO newRecipeVO = reqVO.getProjectDetails().getRecipeDetails();
		 newRecipeVO.setCloudServiceProvider(CloudServiceProviderEnum.DHC_CAAS);
		 newRecipeVO.setCpuCapacity(CpuCapacityEnum._1);
		 newRecipeVO.setEnvironment(EnvironmentEnum.DEVELOPMENT);
		 newRecipeVO.setOperatingSystem(OperatingSystemEnum.DEBIAN_OS_11);
		 newRecipeVO.setRecipeId(reqVO.getProjectDetails().getRecipeDetails().getRecipeId());
		 newRecipeVO.setRepodetails(reqVO.getProjectDetails().getRecipeDetails().getRepodetails());
		 newRecipeVO.setRamSize(RamSizeEnum._1);
		 reqVO.getProjectDetails().setRecipeDetails(newRecipeVO);
		 responseMessage = service.createWorkspace(reqVO, pat);
		 if ("SUCCESS".equalsIgnoreCase(responseMessage.getSuccess())) {
			 responseStatus = HttpStatus.CREATED;
			 log.info("User {} created workspace {}", userId, reqVO.getProjectDetails().getProjectName());
		 } else {
			 log.info("Failed while creating workspace {} for user {}", reqVO.getProjectDetails().getProjectName(),
					 userId);
			 responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		 }
		 return new ResponseEntity<>(responseMessage, responseStatus);
	 }
 
	 @ApiOperation(value = "Delete workspace for a given Id.", nickname = "deleteWorkspace", notes = "Delete workspace for a given identifier.", response = GenericMessage.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.DELETE)
	 public ResponseEntity<GenericMessage> deleteWorkspace(
			 @ApiParam(value = "Workspace ID to be deleted", required = true) @PathVariable("id") String id) {
		 try {
			 CreatedByVO currentUser = this.userStore.getVO();
			 String userId = currentUser != null ? currentUser.getId() : "";
			 CodeServerWorkspaceVO vo = service.getById(userId, id);
			 if (vo == null || vo.getWorkspaceId() == null) {
				 log.debug("No workspace found, returning empty");
				 GenericMessage emptyResponse = new GenericMessage();
				 List<MessageDescription> warnings = new ArrayList<>();
				 MessageDescription msg = new MessageDescription();
				 msg.setMessage("No workspace found for given id and the user");
				 warnings.add(msg);
				 return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			 }
			 if (!(vo != null && vo.getWorkspaceOwner() != null
					 && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Not authorized to delete workspace. User does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info("User {} cannot delete workspace, insufficient privileges. Workspace name: {}", userId,
						 vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			 }
			 if ("DELETE_REQUESTED".equalsIgnoreCase(vo.getStatus())) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Delete workspace already requested. Your workspace is getting deleted.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addWarnings(notAuthorizedMsg);
				 log.info("User {} already requested delete workspace {}", userId, vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.OK);
			 }
			 if (("CREATE_REQUESTED".equalsIgnoreCase(vo.getStatus())) || ("COLLABORATION_REQUESTED".equalsIgnoreCase(vo.getStatus()))){
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Cannot delete codespace as its not created yet. Bad Request");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addWarnings(notAuthorizedMsg);
				 log.info("Cannot delete workspace {} as its not created yet. Bad Request", vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			 }
			 if (vo != null && vo.getProjectDetails().getProjectOwner() != null
					 && vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(userId)
					 && vo.getProjectDetails().getProjectCollaborators() != null
					 && !vo.getProjectDetails().getProjectCollaborators().isEmpty()) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "You have collaborators in your project. Please transfer your ownership to any one of the collaborator before deleting this project codespace.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info(
						 "You have collaborators in your project. Please transfer your ownership to any one of the collaborator before deleting this project codespace");
				 return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			 }
			 GenericMessage responseMsg = service.deleteById(userId, id);
			 log.info("User {} deleted workspace {}", userId, vo.getWorkspaceId());
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
 
	 @ApiOperation(value = "Deploy workspace Project for a given Id.", nickname = "deployWorkspaceProject", notes = "deploy workspace Project for a given identifier.", response = GenericMessage.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/deploy", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.POST)
	 public ResponseEntity<GenericMessage> deployWorkspaceProject(
			 @ApiParam(value = "Workspace ID for the project to be deployed", required = true) @PathVariable("id") String id,
			 @ApiParam(value = "Workspace ID for the project to be deployed", required = true) @Valid @RequestBody ManageDeployRequestDto deployRequestDto) {
		 try {
			 CreatedByVO currentUser = this.userStore.getVO();
			 String userId = currentUser != null ? currentUser.getId() : "";
			 CodeServerWorkspaceVO vo = service.getById(userId, id);
			 if (vo == null || vo.getWorkspaceId() == null) {
				 log.debug("No workspace found, returning empty");
				 GenericMessage emptyResponse = new GenericMessage();
				 List<MessageDescription> warnings = new ArrayList<>();
				 MessageDescription msg = new MessageDescription();
				 msg.setMessage("No workspace found for given id and the user");
				 warnings.add(msg);
				 return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			 }
			 List<String> authorizedUsers = new ArrayList<>();
			 if (vo.getProjectDetails() != null && vo.getProjectDetails().getProjectOwner() != null) {
				 String owner = vo.getProjectDetails().getProjectOwner().getId();
				 authorizedUsers.add(owner);
			 }
			 if (vo.getProjectDetails().getProjectCollaborators() != null
					 && !vo.getProjectDetails().getProjectCollaborators().isEmpty()) {
				 List<String> collabUsers = vo.getProjectDetails().getProjectCollaborators().stream().map(n -> n.getId())
						 .collect(Collectors.toList());
				 authorizedUsers.addAll(collabUsers);
			 }
			 if (!authorizedUsers.contains(userId)) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Not authorized to deploy project for workspace. User does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info("User {} cannot deploy project for workspace {}, insufficient privileges.", userId,
						 vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			 }
			 if (vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("public") 
						|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("private")
						|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("bat")
						|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().equalsIgnoreCase("default")) {
				 MessageDescription invalidTypeMsg = new MessageDescription();
				 invalidTypeMsg.setMessage(
						 "Invalid type, cannot deploy this type of recipe");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(invalidTypeMsg);
				 log.info("User {} cannot deploy project of recipe {} for workspace {}, invalid type.", userId,
						 vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			 }
			 String environment = "int";
			 String branch = "main";
			 if (deployRequestDto != null && !"int".equalsIgnoreCase(deployRequestDto.getTargetEnvironment().name())) {
				 environment = "prod";
			 }
			 if (deployRequestDto != null && deployRequestDto.getBranch() != null) {
				 branch = deployRequestDto.getBranch();
			 }
			 String status = "";
			 if(environment.equalsIgnoreCase("int"))
			 {
				status = vo.getProjectDetails().getIntDeploymentDetails().getLastDeploymentStatus();
			 }
			 else
			 {
				status = vo.getProjectDetails().getProdDeploymentDetails().getLastDeploymentStatus();
			 }
			 if(status != null)
			 {
				if (status.equalsIgnoreCase("DEPLOY_REQUESTED")) {
					MessageDescription invalidTypeMsg = new MessageDescription();
					invalidTypeMsg.setMessage(
							"cannot deploy workspace since it is already in DEPLOY_REQUESTED state");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(invalidTypeMsg);
					log.info("User {} cannot deploy project of recipe {} for workspace {}, since it is alredy in DEPLOY_REQUESTED state.", userId,
							vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
					return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
				}
			 }
			 if ((Objects.nonNull(deployRequestDto.isSecureWithIAMRequired())
					 && deployRequestDto.isSecureWithIAMRequired())
					 && (Objects.nonNull(deployRequestDto.getTechnicalUserDetailsForIAMLogin()))) {
				 UserRequestVO userRequestVO = new UserRequestVO();
				 com.daimler.data.auth.client.UserInfoVO userInfoVO = new com.daimler.data.auth.client.UserInfoVO();
				 com.daimler.data.auth.client.UserInfoVO userInfoVOResponse = new com.daimler.data.auth.client.UserInfoVO();
				 userInfoVO.setId(deployRequestDto.getTechnicalUserDetailsForIAMLogin());
				 userRequestVO.setData(userInfoVO);
				 userInfoVOResponse = dnaAuthClient.onboardTechnicalUser(userRequestVO);
				 if (Objects.nonNull(userInfoVOResponse) && Objects.isNull(userInfoVOResponse.getId())) {
					 log.info(
							 "Failed to onboard/fetch technical user {}, returning from controller without triggering deploy action",
							 deployRequestDto.getTechnicalUserDetailsForIAMLogin());
					 MessageDescription exceptionMsg = new MessageDescription(
							 "Failed to onboard/fetch technical user, Please try again.");
					 GenericMessage errorMessage = new GenericMessage();
					 errorMessage.addErrors(exceptionMsg);
					 return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
				 }
			 }
			 GenericMessage responseMsg = service.deployWorkspace(userId, id, environment, branch,
					 deployRequestDto.isSecureWithIAMRequired(),
					 deployRequestDto.getTechnicalUserDetailsForIAMLogin());
//			 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				 log.info("User {} deployed workspace {} project {}", userId, vo.getWorkspaceId(),
						 vo.getProjectDetails().getRecipeDetails().getRecipeId().name());
//			 }
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
 
	 @ApiOperation(value = "undeploy workspace project for a given Id.", nickname = "undeployWorkspaceProject", notes = "undeploy workspace project for a given identifier.", response = GenericMessage.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/deploy", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.DELETE)
	 public ResponseEntity<GenericMessage> undeployWorkspaceProject(
			 @ApiParam(value = "Workspace ID for the project to be undeployed", required = true) @PathVariable("id") String id,
			 @ApiParam(value = "Workspace ID for the project to be deployed", required = true) @Valid @RequestBody ManageDeployRequestDto deployRequestDto) {
		 try {
			 CreatedByVO currentUser = this.userStore.getVO();
			 String userId = currentUser != null ? currentUser.getId() : "";
			 CodeServerWorkspaceVO vo = service.getById(userId, id);
			 if (vo == null || vo.getWorkspaceId() == null) {
				 log.debug("No workspace found, returning empty");
				 GenericMessage emptyResponse = new GenericMessage();
				 List<MessageDescription> warnings = new ArrayList<>();
				 MessageDescription msg = new MessageDescription();
				 msg.setMessage("No workspace found for given id and the user");
				 warnings.add(msg);
				 return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
			 }
			 List<String> authorizedUsers = new ArrayList<>();
			 if (vo.getProjectDetails() != null && vo.getProjectDetails().getProjectOwner() != null) {
				 String owner = vo.getProjectDetails().getProjectOwner().getId();
				 authorizedUsers.add(owner);
			 }
//			 if (vo.getProjectDetails().getProjectCollaborators() != null
//					 && !vo.getProjectDetails().getProjectCollaborators().isEmpty()) {
//				 List<String> collabUsers = vo.getProjectDetails().getProjectCollaborators().stream().map(n -> n.getId())
//						 .collect(Collectors.toList());
//				 authorizedUsers.addAll(collabUsers);
//			 }
			 if (!authorizedUsers.contains(userId)) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Not authorized to undeploy project for workspace. User does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info("User {} cannot undeploy project for workspace {}, insufficient privileges.", userId,
						 vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			 }
			 if (vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("public") 
						|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("private")
						|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("bat")
						|| vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().equalsIgnoreCase("default")) {
				 MessageDescription invalidTypeMsg = new MessageDescription();
				 invalidTypeMsg.setMessage(
						 "Invalid type, cannot undeploy this type of recipe");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(invalidTypeMsg);
				 log.info("User {} cannot undeploy project of recipe {} for workspace {}, invalid type.", userId,
						 vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			 }
			 String environment = "int";
			 String branch = "main";
			 if (deployRequestDto != null && !"int".equalsIgnoreCase(deployRequestDto.getTargetEnvironment().name())) {
				 environment = "prod";
			 }
			 if (deployRequestDto != null && deployRequestDto.getBranch() != null) {
				 branch = deployRequestDto.getBranch();
			 }
			 String status = "";
			 if(environment.equalsIgnoreCase("int"))
			 {
				status = vo.getProjectDetails().getIntDeploymentDetails().getLastDeploymentStatus();
			 }
			 else
			 {
				status = vo.getProjectDetails().getProdDeploymentDetails().getLastDeploymentStatus();
			 }
			if(status!=null)
			{
				 if (status.equalsIgnoreCase("DEPLOY_REQUESTED")) {
					MessageDescription invalidTypeMsg = new MessageDescription();
					invalidTypeMsg.setMessage(
							"cannot deploy workspace since it is already in DEPLOY_REQUESTED state wait until it deploy");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(invalidTypeMsg);
					log.info("User {} cannot undeploy project of recipe {} for workspace {}, since it is alredy in DEPLOY_REQUESTED state wait until its deployed.", userId,
							vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
					return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
				}
				if(status.equalsIgnoreCase("UNDEPLOY_REQUESTED")) {
				 MessageDescription invalidTypeMsg = new MessageDescription();
				 invalidTypeMsg.setMessage(
						 "cannot deploy workspace since it is already in UNDEPLOY_REQUESTED state");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(invalidTypeMsg);
				 log.info("User {} cannot deploy project of recipe {} for workspace {}, since it is alredy in UNDEPLOY_REQUESTED state.", userId,
						 vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
				 return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			 }
			}
			 GenericMessage responseMsg = service.undeployWorkspace(userId, id, environment, branch);
//			 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				 log.info("User {} undeployed workspace {} project {}", userId, vo.getWorkspaceId(),
						 vo.getProjectDetails().getRecipeDetails().getRecipeId().name());
//			 }
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
 
	 @ApiOperation(value = "Get all codeServer workspaces for the user.", nickname = "getAll", notes = "Get all codeServer workspaces for the user.", response = WorkspaceCollectionVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = WorkspaceCollectionVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<WorkspaceCollectionVO> getAll(
			 @ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			 @ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : "";
		 if (offset == null) {
			 offset = 0;
		 }
		 if (limit == null) {
			 limit = 0;
		 }
 
		 final List<CodeServerWorkspaceVO> workspaces = service.getAll(userId, offset, limit);
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
 
	 @ApiOperation(value = "Get workspace details for a given Id.", nickname = "getById", notes = "Get workspace details for a given Id.", response = CodeServerWorkspaceVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<CodeServerWorkspaceVO> getById(
			 @ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("id") String id) {
 
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : "";
		 CodeServerWorkspaceVO vo = service.getById(userId, id);
		 if (vo != null && vo.getWorkspaceId() != null) {
			 if (!(vo.getWorkspaceOwner() != null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Not authorized to view this workspace. User does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info("User {} cannot view other's workspace, insufficient privileges. Workspace name: {}", userId,
						 vo.getWorkspaceId());
				 return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			 }
			 log.info("Returning workspace details");
			 return new ResponseEntity<>(vo, HttpStatus.OK);
		 } else {
			 log.debug("No workspace found, returning empty");
			 return new ResponseEntity<>(vo, HttpStatus.NOT_FOUND);
		 }
	 }
 
	 @ApiOperation(value = "Get workspace details for a given Id.", nickname = "getByName", notes = "Get workspace details for a given Id.", response = CodeServerWorkspaceVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/status/{name}", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<CodeServerWorkspaceVO> getByName(
			 @ApiParam(value = "Workspace name to be fetched", required = true) @PathVariable("name") String name) {
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : "";
		 CodeServerWorkspaceVO vo = service.getByUniqueliteral(userId, "workspaceId", name);
		 if (vo != null && vo.getWorkspaceId() != null) {
			 if (!(vo.getWorkspaceOwner() != null && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Not authorized to view this workspace. User does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
				 log.info("User {} cannot view other's workspace, insufficient privileges. Workspace name: {}", userId,
						 vo.getWorkspaceId());
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
	 @ApiOperation(value = "Number of workspace.", nickname = "getNumberOfWorkspace", notes = "Get number of workspace. This endpoints will be used to get all valid available workspace records.", response = TransparencyVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/transparency", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<TransparencyVO> getNumberOfWorkspace() {
		 try {
 
			 Integer count = service.getTotalCountOfWorkSpace();
			 TransparencyVO transparencyVO = new TransparencyVO();
			 transparencyVO.setCount(count);
			 log.info("Workspace count fetched successfully");
			 return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		 } catch (Exception e) {
			 log.error("Failed to fetch count of workspaces with exception {} ", e.getMessage());
			 return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		 }
	 }
 
	 @ApiOperation(value = "To check given user has a access to workspace or not.", nickname = "validateCodespace", notes = "To check a user has access to workspace.", response = CodeServerWorkspaceValidateVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceValidateVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/{userid}/validate", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 @Override
	 public ResponseEntity<CodeServerWorkspaceValidateVO> validateCodespace(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id,
			 @ApiParam(value = "User ID to be validated", required = true) @PathVariable("userid") String userid) {
		 CodeServerWorkspaceValidateVO validateVO = service.validateCodespace(id, userid);
		 return new ResponseEntity<>(validateVO, HttpStatus.OK);
	 }
 
	 @Override
	 @ApiOperation(value = "Get Codespace security configurations  entitlements for workspace.", nickname = "getAllEntitlements", notes = "Get Codespace security configuration Entitlements", response = EntitlementCollectionVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = EntitlementCollectionVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config/entitlements", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<EntitlementCollectionVO> getAllEntitlements(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id) {
		 EntitlementCollectionVO entitlementCollectionVO = new EntitlementCollectionVO();
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : null;
		 CodeServerWorkspaceVO vo = service.getById(userId, id);
 
		 if (vo == null || vo.getWorkspaceId() == null) {
			 log.debug("No workspace found, returning empty");
			 return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		 }
 
		 if (!(vo != null && vo.getWorkspaceOwner() != null
				 && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
			 log.info(
					 "security configurations entitlements for workspace can be view only by owners and collaborators, insufficient privileges. Workspace name: {}",
					 userId, vo.getWorkspaceId());
			 return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		 }
		 if ((vo != null && vo.getProjectDetails().getSecurityConfig() != null && vo.getProjectDetails().getSecurityConfig().getStatus() == null)
				 || vo.getProjectDetails().getSecurityConfig() == null) {
 
			 log.info("No security configurations for workspace found");
			 return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		 }
		 List<CodespaceSecurityEntitlementVO> entitlementsVO = vo.getProjectDetails().getSecurityConfig()
				 .getEntitlements();
		 List<CodespaceSecurityConfigLOV> entitlementsList = new ArrayList<>();
		 if (entitlementsVO != null) {
			 for (CodespaceSecurityEntitlementVO entitlementVO : entitlementsVO) {
				CodespaceSecurityConfigLOV entitlementLOV = new CodespaceSecurityConfigLOV();
				 entitlementLOV.setId(entitlementVO.getId());
				 entitlementLOV.setName(entitlementVO.getName());
				 entitlementsList.add(entitlementLOV);
			 }
			 entitlementCollectionVO.setData(entitlementsList);
			 return new ResponseEntity<>(entitlementCollectionVO, HttpStatus.OK);
		 }
		 return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
	 }
 
	 @Override
	 @ApiOperation(value = "Get Codespace security configurations  roles for workspace.", nickname = "getAllRoles", notes = "Get Codespace security configuration roles", response = RoleCollectionVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = RoleCollectionVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config/roles", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<RoleCollectionVO> getAllRoles(String id) {
		 RoleCollectionVO roleCollectionVO = new RoleCollectionVO();
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : null;
		 CodeServerWorkspaceVO vo = service.getById(userId, id);
 
		 if (vo == null || vo.getWorkspaceId() == null) {
			 log.debug("No workspace found, returning empty");
			 return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		 }
 
		 if (!(vo != null && vo.getWorkspaceOwner() != null
				 && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
			 log.info(
					 "security configurations entitlements for workspace can be view only by Owners, insufficient privileges. Workspace name: {}",
					 userId, vo.getWorkspaceId());
			 return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		 }
		 if (vo != null && vo.getProjectDetails().getSecurityConfig() == null) {
 
			 log.info("No security configurations for workspace found");
			 return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		 }
		 List<CodespaceSecurityRoleVO> rolesVO = vo.getProjectDetails().getSecurityConfig().getRoles();
		 if (rolesVO != null) {
			 List<CodespaceSecurityConfigLOV> rolesList = new ArrayList<>();
			 for (CodespaceSecurityRoleVO roleVO : rolesVO) {
				CodespaceSecurityConfigLOV roleLOV = new CodespaceSecurityConfigLOV();
				 roleLOV.setId(roleVO.getId());
				 roleLOV.setName(roleVO.getName());
				 rolesList.add(roleLOV);
			 }
			 roleCollectionVO.setData(rolesList);
			 return new ResponseEntity<>(roleCollectionVO, HttpStatus.OK);
		 }
		 return new ResponseEntity<>( null,HttpStatus.NO_CONTENT);
	 }
 
	 @Override
	 @ApiOperation(value = "Get Codespace security configurations which include defining roles, entitlements, user-role mappings etc. for given ID", nickname = "saveSecurityConfig", notes = "Get codespace security configurations for Id", response = CodespaceSecurityConfigVO.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = CodespaceSecurityConfigVO.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.GET)
	 public ResponseEntity<CodespaceSecurityConfigVO> getSecurityConfig(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id) {
				CodespaceSecurityConfigVO getConfigResponse = new CodespaceSecurityConfigVO();
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findDataById(id);
		CodeServerWorkspaceVO vo = workspaceAssembler.toVo(entity);
 
		 if (vo == null || vo.getWorkspaceId() == null) {
			  log.debug("No workspace found, returning empty");
			 return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
 
		 }
		 if (!(vo != null && vo.getWorkspaceOwner() != null
				 && vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId)) && !(userStore.getUserInfo().hasCodespaceAdminAccess())) {
					MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "security configurations for workspace can be view only by workspace owners and Codespace admins. Access Denied, user does not have privileges.");
				 GenericMessage errorMessage = new GenericMessage();
				 errorMessage.addErrors(notAuthorizedMsg);
			 log.info(
					 "security configurations for workspace can be view only by workspace owners and Codespace admins, insufficient privileges. Workspace name: {}"
					,vo.getWorkspaceId());
			 return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		 }
		 if ((vo != null && vo.getProjectDetails().getSecurityConfig() != null && vo.getProjectDetails().getSecurityConfig().getStatus() == null)
				 || vo.getProjectDetails().getSecurityConfig() == null) {
 
			 log.info("No security configurations for workspace found");
			 return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		 }
		 getConfigResponse = vo.getProjectDetails().getSecurityConfig();
		 getConfigResponse.setProjectName(vo.getProjectDetails().getProjectName());
		 return new ResponseEntity<>(getConfigResponse, HttpStatus.OK);
	 }
 
	 @Override
	 @ApiOperation(value = " Change codespace security configurations to Requested state", nickname = "requestSecurityConfig", notes = "change state codespace security configurations to requested", response = GenericMessage.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config/request", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.POST)
	 public ResponseEntity<GenericMessage> requestSecurityConfig(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id) {
		 CodespaceSecurityConfigVO getConfigResponse = new CodespaceSecurityConfigVO();
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : null;
		 CodeServerWorkspaceVO vo = service.getById(userId, id);
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errorMessage = new ArrayList<>();
		 if (vo == null || vo.getWorkspaceId() == null) {
			 log.debug("No workspace found, returning empty");
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage("No workspace found for given id and the user");
			 errorMessage.add(msg);
			 responseMessage.setErrors(errorMessage);
			 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
 
		 }
 
		 if (!(vo != null && vo.getProjectDetails().getProjectOwner() != null
				 && vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(userId))) {
			 log.info(
					 "security configurations for workspace can be view/edit only by Owners, insufficient privileges. Workspace name: {}",
					 userId, vo.getWorkspaceId());
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage("security configurations for workspace can be view/edit only by Owners");
			 errorMessage.add(msg);
			 responseMessage.setErrors(errorMessage);
			 return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
		 }
		 if (vo != null && vo.getProjectDetails().getSecurityConfig() == null) {
 
			 log.info("No security configurations for workspace found");
			 MessageDescription msg = new MessageDescription();
			 msg.setMessage("No security configurations for workspace found");
			 errorMessage.add(msg);
			 responseMessage.setErrors(errorMessage);
			 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
		 }
		 if (vo.getProjectDetails().getSecurityConfig().getStatus() != null
				 && (vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("ACCEPTED")
						 || vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("REQUESTED"))) {
			 MessageDescription notAuthorizedMsg = new MessageDescription();
			 notAuthorizedMsg.setMessage(
					 "Cannot edit security configurations for workspace when its in REQUESTED or ACCEPTED state. Bad request.");
			 responseMessage.addErrors(notAuthorizedMsg);
			 log.info(" cannot edit security configurations for workspace when its in {} state",
					 vo.getProjectDetails().getSecurityConfig().getStatus());
			 return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
		 }
		 if (vo.getProjectDetails().getSecurityConfig().getStatus() != null
				 && (vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("DRAFT")
						 || vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("PUBLISHED"))) {
			 vo.getProjectDetails().getSecurityConfig().setStatus("REQUESTED");
			 SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			 vo.getProjectDetails().getSecurityConfig().setRequestedDate(dateFormatter.format(new Date()));
			 responseMessage = service.saveSecurityConfig(vo,false);
		 }
 
		 return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	 }
 
	 @Override
	 @ApiOperation(value = " Change codespace security configurations to accepted state", nickname = "acceptSecurityConfig", notes = "change state codespace security configurations to accepted", response = GenericMessage.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config/accept", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.POST)
	 public ResponseEntity<GenericMessage> acceptSecurityConfig(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id) {
		CodespaceSecurityConfigVO getConfigResponse = new CodespaceSecurityConfigVO();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		//  CodeServerWorkspaceVO vo = service.getById(userId, i);
		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findDataById(id);
		CodeServerWorkspaceVO vo = workspaceAssembler.toVo(entity);
		
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errorMessage = new ArrayList<>();
		 MessageDescription msg = new MessageDescription();
				
		 if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
 
			 if (vo == null || vo.getWorkspaceId() == null) {
				 log.debug("No workspace found, returning empty");
				 msg.setMessage("No workspace found ");
				 errorMessage.add(msg);
				 responseMessage.setErrors(errorMessage);
				 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			 }
 
			 if (vo != null && vo.getProjectDetails().getSecurityConfig() == null) {
 
				 log.info("No security configurations for workspace found");
				 msg.setMessage("No security configurations for workspace found");
				 errorMessage.add(msg);
				 responseMessage.setErrors(errorMessage);
				 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			 }
			 if (vo.getProjectDetails().getSecurityConfig().getStatus() != null
					 && vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("REQUESTED")) {
				 vo.getProjectDetails().getSecurityConfig().setStatus("ACCEPTED");
				responseMessage = service.saveSecurityConfig(vo, false);
					//responseMessage = service.updateSecurityConfigStatus(vo.getProjectDetails().getProjectName(),"ACCEPTED", userId,vo);
				 return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			 } else {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Cannot change status to ACCEPTED for workspace when its in DRAFT or PUBLISHED state. Denied.");
				 responseMessage.addErrors(notAuthorizedMsg);
				 log.info(" Cannot change status to ACCEPTED for workspace when its in {} state",
						 vo.getProjectDetails().getSecurityConfig().getStatus());
				 return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
			 }
		 } else {
			 log.info("Not authorized to update status. User does not have privileges. {}", userId, vo.getWorkspaceId());
			 msg.setMessage("Not authorized to update status. User does not have privileges.");
			 errorMessage.add(msg);
			 responseMessage.setErrors(errorMessage);
 
		 }
		 return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
 
	 }

	 @Override
	 @ApiOperation(value = " Change codespace security configurations to publish state", nickname = "publishSecurityConfig", notes = "change state codespace security configurations to publish", response = GenericMessage.class, tags = {
			 "code-server", })
	 @ApiResponses(value = {
			 @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			 @ApiResponse(code = 204, message = "Fetch complete, no content found."),
			 @ApiResponse(code = 400, message = "Bad request."),
			 @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			 @ApiResponse(code = 403, message = "Request is not authorized."),
			 @ApiResponse(code = 405, message = "Method not allowed"),
			 @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/workspaces/{id}/config/publish", produces = { "application/json" }, consumes = {
			 "application/json" }, method = RequestMethod.POST)
	 public ResponseEntity<GenericMessage> publishSecurityConfig(
			 @ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id) {
		 CodespaceSecurityConfigVO getConfigResponse = new CodespaceSecurityConfigVO();
		 CreatedByVO currentUser = this.userStore.getVO();
		 String userId = currentUser != null ? currentUser.getId() : null;
		// CodeServerWorkspaceVO vo = service.getById(userId, id);

		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findDataById(id);
		CodeServerWorkspaceVO vo = workspaceAssembler.toVo(entity);

		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errorMessage = new ArrayList<>();
		 MessageDescription msg = new MessageDescription();
 
		 if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
 
			 if (vo == null || vo.getWorkspaceId() == null) {
				 log.debug("No workspace found, returning empty");
				 msg.setMessage("No workspace found for given id and the user");
				 errorMessage.add(msg);
				 responseMessage.setErrors(errorMessage);
				 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			 }
 
			 if (vo != null && vo.getProjectDetails().getSecurityConfig() == null) {
 
				 log.info("No security configurations for workspace found");
				 msg.setMessage("No security configurations for workspace found");
				 errorMessage.add(msg);
				 responseMessage.setErrors(errorMessage);
				 return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			 }
			 if (vo.getProjectDetails().getSecurityConfig().getStatus() != null
					 && vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("ACCEPTED")) {
				 //vo.getProjectDetails().getSecurityConfig().setStatus("PUBLISHED");

				  GenericMessage securityConfigResponseMessage = new GenericMessage();
				// securityConfigResponseMessage = service.updateSecurityConfigStatus(vo.getProjectDetails().getProjectName(),"PUBLISHED", userId,vo);
				 //if(securityConfigResponseMessage.getSuccess().equalsIgnoreCase("SUCCESS")){
					vo.getProjectDetails().getSecurityConfig().setStatus("PUBLISHED");
					responseMessage = service.saveSecurityConfig(vo,true);
				// }
				 //vo.getProjectDetails().setPublishedSecuirtyConfig(vo.getProjectDetails().getSecurityConfig());
				 //responseMessage = service.saveSecurityConfig(vo);
				 return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			 } else {
				 MessageDescription notAuthorizedMsg = new MessageDescription();
				 notAuthorizedMsg.setMessage(
						 "Cannot change status to PUBLISHED when its in DRAFT or REQUESTED state. Denied.");
				 responseMessage.addErrors(notAuthorizedMsg);
				 log.info(" Cannot change status to PUBLISHED for workspace when its in {} state",
						 vo.getProjectDetails().getSecurityConfig().getStatus());
				 return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
			 }
		 } else {
			 log.info("Not authorized to update status. User does not have privileges. {}", userId, vo.getWorkspaceId());
			 msg.setMessage("Not authorized to update status. User does not have privileges.");
			 errorMessage.add(msg);
			 responseMessage.setErrors(errorMessage);
 
		 }
		 return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
 
	 }

	 @Override
	@ApiOperation(value = "Getting values of published security config for a workspace", nickname = "publishedSecurityConfigDetails", notes = "Get published security config details in codeserver workspace", response = CodespaceSecurityConfigVO.class, tags = {
			"code-server", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = CodespaceSecurityConfigVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/workspaces/{id}/config/publish", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<CodespaceSecurityConfigVO> getPublishedSecurityConfigDetails(@ApiParam(value = "Workspace ID for the project", required = true) @PathVariable("id") String id) {
		CodespaceSecurityConfigVO configPublishedDetailsVO = new CodespaceSecurityConfigVO();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		CodeServerWorkspaceVO vo = service.getById(userId, id);
		if (vo == null || vo.getWorkspaceId() == null) {
			log.debug("No workspace found, returning empty");
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		if (!(vo != null && vo.getWorkspaceOwner() != null
				&& vo.getWorkspaceOwner().getId().equalsIgnoreCase(userId))) {
			log.info(
					"security configurations for workspace can be view only by Owners, insufficient privileges. Workspace name: {}",
					userId, vo.getWorkspaceId());
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		if ((vo != null && vo.getProjectDetails().getSecurityConfig() != null && vo.getProjectDetails().getSecurityConfig().getStatus() == null)
				 || vo.getProjectDetails().getSecurityConfig() == null) {
			log.debug("No published security config found, returning empty");
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		}
		configPublishedDetailsVO = vo.getProjectDetails().getPublishedSecuirtyConfig();
		configPublishedDetailsVO.setProjectName(vo.getProjectDetails().getProjectName());
		return new ResponseEntity<>(configPublishedDetailsVO, HttpStatus.OK);
	}
   
   	@Override
	@ApiOperation(value = "Get all workspace security configurations which are in requested and accepted state, waiting for processing.", nickname = "getAllSecurityConfig", notes = "get codespace security configurations in requested state.", response = CodespaceSecurityConfigCollectionVO.class, tags = {
		"code-server", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = CodespaceSecurityConfigCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/workspaces/configs", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<CodespaceSecurityConfigCollectionVO> getWorkspaceConfigs( @ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			 @ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			 @ApiParam(value = "project name to get SecurityConfig.") @Valid @RequestParam(value = "projectName", required = false) String projectName) {
		CodespaceSecurityConfigCollectionVO configCollectionVo = new CodespaceSecurityConfigCollectionVO();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;

			int defaultLimit = 15;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			if(projectName == null ||"".equalsIgnoreCase(projectName)){
				projectName = null;
			}
		if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
			final List<CodespaceSecurityConfigDetailsVO> configDetailsVo = service.getAllSecurityConfigs(offset,limit,projectName);
			if(configDetailsVo != null && configDetailsVo.size() > 0)
			{
				configCollectionVo.setData(configDetailsVo);
				configCollectionVo.setTotalCount(configDetailsVo.size());
				return new ResponseEntity<>(configCollectionVo, HttpStatus.OK);
			}
			else
			{
				return new ResponseEntity<>(configCollectionVo, HttpStatus.NO_CONTENT);
			}
		} else {

			 log.info("Not authorized to view the security configs . User does not have privileges.", userId);
		 }
		 return new ResponseEntity<>(configCollectionVo, HttpStatus.FORBIDDEN);
	}

	@ApiOperation(value = "update Governance feilds", nickname = "updateGovernance", notes = "Update Governane feilds in workspace for user", response = CodeServerWorkspaceVO.class, tags = {
			"code-server", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = CodeServerWorkspaceVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/workspaces/{id}/datagovernance", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PATCH)
	@Override
	public ResponseEntity<GenericMessage> updateGovernance(
			@ApiParam(value = "workspace ID for the project", required = true) @PathVariable("id") String id,
			@ApiParam(value = "GovernanceData to update in project", required = true) @Valid @RequestBody DataGovernanceRequestInfo dataGovernanceInfo) {
		// TODO Auto-generated method stub
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

		if (!vo.getProjectDetails().getProjectOwner().getId().equalsIgnoreCase(userId)) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage(
					"Not authorized to update workspace. User does not have privileges.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			log.info("User {} cannot update workspace, insufficient privileges. Workspace name: {}", userId,
					vo.getWorkspaceId());
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}

		if (userId != null) {
			responseMessage = service.updateGovernancenceValues(userId, id, dataGovernanceInfo);
		}

		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}

 }
