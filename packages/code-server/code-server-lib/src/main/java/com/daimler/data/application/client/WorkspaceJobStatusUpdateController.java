package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.auth.client.AuthenticatorClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.dto.workspace.WorkspaceUpdateRequestVO;
import com.daimler.data.service.workspace.WorkspaceService;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Workspace API", tags = { "code-server" })
@RequestMapping("/")
@Slf4j
public class WorkspaceJobStatusUpdateController  {

	@Autowired
	private WorkspaceService service;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private KafkaProducerService kafkaProducer;
	
	@Value("${codeServer.gitjob.pat}")
	private String personalAccessToken;
	
	@Value("${workspace.callKongApisFromBackend}")
	private boolean callKongApisFromBackend;
	
	@Autowired
	private AuthenticatorClient authenticatorClient;		
	
	@ApiOperation(value = "Update workspace Project for a given Id.", nickname = "updateWorkspace", notes = "update workspace Project for a given identifier.", response = GenericMessage.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{wsId}/{userId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<GenericMessage> updateWorkspace(@ApiParam(value = "wsId for which workspaces needs to be updated",required=true) @PathVariable("wsId") String wsId,
	@ApiParam(value = "user id for which workspaces needs to be updated",required=true) @PathVariable("userId") String userId,
	@ApiParam(value = "Request Body that contains data required for updating code server workbench status for user" ,required=true )  @Valid @RequestBody WorkspaceUpdateRequestVO updateRequestVO){
		
		String gitJobPat = httpRequest.getHeader("Authorization");
		if(!gitJobPat.equalsIgnoreCase(personalAccessToken)) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage(
					"Authentication failed");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			log.info("Authentication failed to use API. ");
			return new ResponseEntity<>(errorMessage, HttpStatus.UNAUTHORIZED);
		}
		CodeServerWorkspaceVO existingVO = service.findByWorkspaceId(wsId);	
		if (existingVO != null && existingVO.getWorkspaceId() != null) {
			String existingStatus = existingVO.getStatus();
			log.info("existingStatus  is {}",existingStatus);
			String latestStatus = updateRequestVO.getStatus().name();
			log.info("latestStatus  is {}",latestStatus);
			UserInfoVO ownerVO = existingVO.getWorkspaceOwner();
			boolean unauthorized = false; 
			String owner = ownerVO.getId();
			String[] manageJobStatuses = {"CREATED","CREATE_FAILED","DELETED","DELETE_FAILED"}; 
			boolean isManageJobStatus = Arrays.stream(manageJobStatuses).anyMatch(latestStatus::equals);
			if(isManageJobStatus) {
				if(!userId.equalsIgnoreCase(owner)) {
					unauthorized = true; 
				}
			}else {
				List<UserInfoVO> collabsVO = existingVO.getProjectDetails().getProjectCollaborators();
				String[] collabUserIds = {};
				boolean isCollabUser = Arrays.stream(manageJobStatuses).anyMatch(latestStatus::equals);
				if(!(isCollabUser || userId.equalsIgnoreCase(owner))) {
					unauthorized = true; 
				}
			}
			if(unauthorized) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to update other's workspace. User does not have privileges.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				log.info("User {} cannot update workspace {}, insufficient privileges. Workspace name: {} and owner is {}", userId,wsId,owner);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
//			if(updateRequestVO.getLastDeployedOn()!=null)
//				existingVO.setLastDeployedOn(updateRequestVO.getLastDeployedOn());
			String projectName = existingVO.getProjectDetails().getProjectName();
			String message = "";
			String eventType = "";
			boolean invalidStatus = false;
			String targetEnv = updateRequestVO.getTargetEnvironment();
			String branch = updateRequestVO.getBranch();		
			UserInfoVO workspaceOwner = existingVO.getWorkspaceOwner();
			String workspaceOwnerName = workspaceOwner.getFirstName() + " " + workspaceOwner.getLastName();	
			String resourceID = existingVO.getWorkspaceId();
			List<String> teamMembers = new ArrayList<>();
			List<String> teamMembersEmails = new ArrayList<>();
			UserInfoVO projectOwner = existingVO.getProjectDetails().getProjectOwner();
			teamMembers.add(projectOwner.getId());
			teamMembersEmails.add(projectOwner.getEmail());
			List<UserInfoVO> projectCollaborators = existingVO.getProjectDetails().getProjectCollaborators();
			if (Objects.nonNull(projectCollaborators)) {
				if (projectCollaborators.size() > 0) {
					for (UserInfoVO collab : projectCollaborators) {
						teamMembers.add(collab.getId());
						teamMembersEmails.add(collab.getEmail());
					}
				}
			}	
			switch(existingStatus) {
				case "CREATE_REQUESTED": 
					if(!(latestStatus.equalsIgnoreCase("CREATED") || latestStatus.equalsIgnoreCase("CREATE_FAILED")))
						invalidStatus = true;
					else {
						if(latestStatus.equalsIgnoreCase("CREATED")) {
							eventType = "Codespace-Create";
							log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
							message = "Codespace "+ projectName + " successfully created by user " + workspaceOwnerName;
						}														
						else {
							eventType = "Codespace-Create Failed";
							message = "Create failed, while initializing Codespace " +projectName +" for user "+ workspaceOwnerName;
						}													 
					}						
					break;
				case "COLLAB_REQUESTED": 
					if(!(latestStatus.equalsIgnoreCase("CREATED") || latestStatus.equalsIgnoreCase("CREATE_FAILED")))
						invalidStatus = true;
					break;
				case "DELETE_REQUESTED": 
					if(!(latestStatus.equalsIgnoreCase("DELETED") || latestStatus.equalsIgnoreCase("DELETE_FAILED") || latestStatus.equalsIgnoreCase("UNDEPLOYED") || latestStatus.equalsIgnoreCase("UNDEPLOY_FAILED") ))
						invalidStatus = true;
					break;
				default:
					break;
			  
			}
			if(invalidStatus) {
				log.info("workspace {} is in status {} , cannot be changed to invalid status {} ",wsId, existingStatus, latestStatus);
				MessageDescription invalidMsg = new MessageDescription("Cannot change workspace status from " + existingStatus + " to " + latestStatus + ". Invalid status.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(invalidMsg);
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			}
			String environment = "Staging";
			if(targetEnv.equalsIgnoreCase("prod")) {
				environment = "Production";
			}
			if(existingStatus.equals("CREATED")) {
				if(latestStatus.equalsIgnoreCase("BUILD_SUCCESS")) {
					eventType = "Codespace-build";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Successfully build Codespace "+ projectName + " with branch " + branch +" on " + environment + " triggered by " +userId;
				}
				if(latestStatus.equalsIgnoreCase("BUILD_FAILED")) {
					eventType = "Codespace-build Failed";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Failed to build Codespace " + projectName + " with branch " + branch +" on " +  environment + " triggered by " +userId;
				}
				if(latestStatus.equalsIgnoreCase("DEPLOYED")) {
					eventType = "Codespace-Deploy";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Successfully deployed Codespace "+ projectName + " with branch " + branch +" on " + environment + " triggered by " +userId;
				}													
				if(latestStatus.equalsIgnoreCase("DEPLOYMENT_FAILED")) {
					eventType = "Codespace-Deploy Failed";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Failed to deploy Codespace " + projectName + " with branch " + branch +" on " +  environment + " triggered by " +userId;
				}
				if(latestStatus.equalsIgnoreCase("UNDEPLOYED")) {
					eventType = "Codespace-UnDeploy";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Successfully undeployed Codespace "+ projectName + " with branch " + branch +" on " + environment + " triggered by " +userId;
				}													
				if(latestStatus.equalsIgnoreCase("UNDEPLOY_FAILED")) {
					eventType = "Codespace-UnDeploy Failed";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Failed to undeploy Codespace " + projectName + " with branch " + branch +" on " + environment + " triggered by " +userId;
				}
			}
			String gitJobRunId = updateRequestVO.getGitjobRunID();
			GenericMessage responseMessage = service.update(userId,wsId,projectName,existingStatus,latestStatus,targetEnv,branch,gitJobRunId,updateRequestVO.getVersion());
			log.info("Message details after update action {} and userid is {} and resourceID is {}",message,userId,resourceID);
			// if(callKongApisFromBackend) {
			// 	log.info("Calling Kong API's from backend and flag is {}", callKongApisFromBackend);
			// 	authenticatorClient.callingKongApis(name,name,null,false);
			// }			
			kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
		}else {
			log.info("workspace {} doesnt exists for User {} , or workspace already deleted and hence update not required",wsId, userId);
			MessageDescription invalidMsg = new MessageDescription("No Workspace with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.error("No workspace found with id {}, failed to update", wsId);
			return new ResponseEntity<>(errorMessage, HttpStatus.OK);
		}
    }
}
