package com.daimler.data.application.client;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.WorkspaceUpdateRequestVO;
import com.daimler.data.service.workspace.WorkspaceService;

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
	
	@ApiOperation(value = "Update workspace Project for a given Id.", nickname = "updateWorkspace", notes = "update workspace Project for a given identifier.", response = GenericMessage.class, tags={ "code-server", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{userId}/{name}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<GenericMessage> updateWorkspace(@ApiParam(value = "Name of workspace that needs to be updated",required=true) @PathVariable("name") String name,
    		@ApiParam(value = "user for which workspaces needs to be updated",required=true) @PathVariable("userId") String userId,
    		@ApiParam(value = "Request Body that contains data required for updating code server workbench status for user" ,required=true )  @Valid @RequestBody WorkspaceUpdateRequestVO updateRequestVO){
//		CodeServerWorkspaceVO existingVO = service.getByUniqueliteral(userId,"name", name);
//		if (existingVO != null && existingVO.getOwner()!=null) {
//			String owner = existingVO.getOwner();
//			if(!userId.equalsIgnoreCase(owner)) {
//				MessageDescription notAuthorizedMsg = new MessageDescription();
//				notAuthorizedMsg.setMessage(
//						"Not authorized to update other's workspace. User does not have privileges.");
//				GenericMessage errorMessage = new GenericMessage();
//				errorMessage.addErrors(notAuthorizedMsg);
//				log.info("User {} cannot update workspace {}, insufficient privileges. Workspace name: {} and owner is {}", userId,name,owner);
//				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
//			}
////			if(updateRequestVO.getLastDeployedOn()!=null)
////				existingVO.setLastDeployedOn(updateRequestVO.getLastDeployedOn());
//			String existingStatus = existingVO.getStatus();
//			String latestStatus = updateRequestVO.getStatus().name();
//			boolean invalidStatus = false;
//			switch(existingStatus) {
//				case "CREATE_REQUESTED": 
//					if(!(latestStatus.equalsIgnoreCase("CREATED") || latestStatus.equalsIgnoreCase("CREATE_FAILED")))
//						invalidStatus = true;
//					break;
//				case "DELETE_REQUESTED": 
//					if(!(latestStatus.equalsIgnoreCase("DELETED") || latestStatus.equalsIgnoreCase("DELETE_FAILED") || latestStatus.equalsIgnoreCase("UNDEPLOYED") || latestStatus.equalsIgnoreCase("UNDEPLOY_FAILED") ))
//						invalidStatus = true;
//					break;
//				case "DEPLOY_REQUESTED": 
//					if(!(latestStatus.equalsIgnoreCase("DEPLOYED") || latestStatus.equalsIgnoreCase("DEPLOYMENT_FAILED")))
//						invalidStatus = true;
//					break;
//				case "UNDEPLOY_REQUESTED": 
//					if(!(latestStatus.equalsIgnoreCase("UNDEPLOYED") || latestStatus.equalsIgnoreCase("UNDEPLOY_FAILED")))
//						invalidStatus = true;
//					break;
//				default:
//					break;
//			  
//			}
//			if(invalidStatus) {
//				log.info("workspace {} is in status {} , cannot be changed to invalid status {} ",name, existingStatus, latestStatus);
//				MessageDescription invalidMsg = new MessageDescription("Cannot change workspace status from " + existingStatus + " to " + latestStatus + ". Invalid status.");
//				GenericMessage errorMessage = new GenericMessage();
//				errorMessage.addErrors(invalidMsg);
//				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
//			}
//			existingVO.setLastDeployedOn(new Date());
//			existingVO.setStatus(latestStatus);
//			GenericMessage responseMessage = service.update(existingVO);
//			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
//		}else {
//			log.info("workspace {} doesnt exists for User {} ",existingVO.getName(), userId);
//			MessageDescription invalidMsg = new MessageDescription("No Workspace with the given id");
//			GenericMessage errorMessage = new GenericMessage();
//			errorMessage.addErrors(invalidMsg);
//			log.error("No workspace found with id {}, failed to update", existingVO.getName());
//			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
//		}
		return null;
    }
}
