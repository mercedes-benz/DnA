package com.daimler.data.controller;

import javax.servlet.ServletRequest;
import javax.validation.Valid;

import org.hibernate.mapping.Array;
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

import com.daimler.data.api.adaProjects.AdaProjectsApi;
import com.daimler.data.application.annotation.RequiresApiKeyAuthorization;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.json.ADAProjectDetails;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsCollectionVO;
import com.daimler.data.dto.adaProjects.CreateADAProjectResponseVO;
import com.daimler.data.dto.adaProjects.WorkspaceProjectAssociationVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.adaProjects.ADAProjectsService;
import com.daimler.data.service.fabric.FabricWorkspaceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "ADA Projects APIs")
@RequestMapping("/api/fabric-workspaces")
public class ADAProjectsController implements AdaProjectsApi{

    @Autowired
    private ADAProjectsService service;

    @Autowired
    private FabricWorkspaceService fabricWorkspaceService;

    @RequiresApiKeyAuthorization
    @Override
    @ApiOperation(value = "Create a new ADA Project", nickname = "createADAProject", notes = "This can only be done by the logged in user.", response = CreateADAProjectResponseVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "ADA Project Details created", response = CreateADAProjectResponseVO.class),
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 404, message = "ADA Project Details not found") })
    @RequestMapping(value = "/ada/projects",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<CreateADAProjectResponseVO> createADAProject(@ApiParam(value = "ADA Project object that needs to be created" ,required=true )  @Valid @RequestBody ADAProjectDetailsVO body){
        CreateADAProjectResponseVO response = new CreateADAProjectResponseVO();
        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> warnings = new ArrayList<>();
        List<MessageDescription> errors = new ArrayList<>();
        ADAProjectDetailsVO existingADAProject = service.getByUniqueliteral("projectID", body.getProjectID());
        if (existingADAProject == null) {
            GenericMessage createMessage  = service.createNewProject(body);
            
            if(createMessage.getSuccess().equals("CREATED")) {
                responseMessage.setSuccess("CREATED");
                response.setResponses(responseMessage);
                response.setData(body);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                errors.add(new MessageDescription("Failed to create project"));
                responseMessage.setErrors(errors);
                responseMessage.setSuccess("ERROR");
                response.setResponses(responseMessage);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else{
            log.warn("ADA Project with id {} already exists", body.getProjectID());
            errors.add(new MessageDescription("Project with ID " + body.getProjectID() + " already exists"));
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("CONFLICT");
            response.setResponses(responseMessage);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        
    }

    @RequiresApiKeyAuthorization
    @Override
    @ApiOperation(value = "Delete an ADA Project", nickname = "deleteADAProject", notes = "Delete an ADA Project by Id", response = GenericMessage.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "ADA Project deleted successfully", response = GenericMessage.class),
        @ApiResponse(code = 404, message = "ADA Project not found") })
    @RequestMapping(value = "/ada/project/{projectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteADAProject(@ApiParam(value = "ID of ADA Project to delete",required=true) @PathVariable("projectId") String projectId) {
        GenericMessage responseMessage = new GenericMessage();
        MessageDescription description = new MessageDescription();
        List<MessageDescription> warnings = new ArrayList<>();
        List<MessageDescription> errors = new ArrayList<>();
        ADAProjectDetailsVO existingADAProject = service.getByUniqueliteral("projectID", projectId);
        if (existingADAProject == null) {
            log.warn("No ADA Project found with id {}", projectId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        service.deleteById(existingADAProject.getId());
        responseMessage.setSuccess("SUCCESS");
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    @RequiresApiKeyAuthorization
    @Override
    @ApiOperation(value = "Get ADA Project by ID", nickname = "getADAProjectById", notes = "Returns a single ADA Project", response = ADAProjectDetailsVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = ADAProjectDetailsVO.class),
        @ApiResponse(code = 404, message = "ADA Project not found") })
    @RequestMapping(value = "/ada/project/{projectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<ADAProjectDetailsVO> getADAProjectById(@ApiParam(value = "ID of ADA Project to return",required=true) @PathVariable("projectId") String projectId) {
        ADAProjectDetailsVO existingADAProject = service.getByUniqueliteral("projectID", projectId);

		if(existingADAProject == null) {
            log.warn("No ADA Project found with id {}", projectId);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
        return new ResponseEntity<>(existingADAProject, HttpStatus.OK);
    }

    @RequiresApiKeyAuthorization
    @Override
    @ApiOperation(value = "Get all ADA Projects", nickname = "getAllADAProjects", notes = "Get all ADA Projects with pagination and sorting options", response = ADAProjectDetailsCollectionVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns paginated list of ADA Projects", response = ADAProjectDetailsCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials"),
        @ApiResponse(code = 403, message = "Request is not authorized"),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/ada/projects",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<ADAProjectDetailsCollectionVO> getAllADAProjects(@ApiParam(value = "Page number from which listing of ADA Projects should start. Example: 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,@ApiParam(value = "Page size to limit the number of ADA Projects. Example: 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {

        	ADAProjectDetailsCollectionVO collection = new ADAProjectDetailsCollectionVO();
            int defaultLimit = 15;
            if (offset == null || offset < 0)
                offset = 0;
            if (limit == null || limit < 0) {
                limit = defaultLimit;
            }
            collection = service.getAllProjects(limit, offset);
            if(!collection.getRecords().isEmpty()){
                collection.setTotalCount(collection.getRecords().size());
            }
            HttpStatus responseCode = collection.getRecords()!=null && !collection.getRecords().isEmpty() ? HttpStatus.OK : HttpStatus.NO_CONTENT;
            return new ResponseEntity<>(collection, responseCode);
    }

    @RequiresApiKeyAuthorization
    @Override
    @ApiOperation(value = "Update an existing ADA Project", nickname = "updateADAProject", notes = "Update an existing ADA Project by Id", response = CreateADAProjectResponseVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "ADA Project updated successfully", response = CreateADAProjectResponseVO.class),
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 404, message = "ADA Project not found") })
    @RequestMapping(value = "/ada/project/{projectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<CreateADAProjectResponseVO> updateADAProject(@ApiParam(value = "ID of ADA Project to update",required=true) @PathVariable("projectId") String projectId,@ApiParam(value = "ADA Project object with updated values" ,required=true )  @Valid @RequestBody ADAProjectDetailsVO body) {

        CreateADAProjectResponseVO response = new CreateADAProjectResponseVO();
        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> warnings = new ArrayList<>();
        List<MessageDescription> errors = new ArrayList<>();
        ADAProjectDetailsVO existingADAProject = service.getByUniqueliteral("projectID",projectId);
        if (existingADAProject != null) {
            GenericMessage createMessage  = service.updateProject(existingADAProject.getId(),body);

            if(createMessage.getSuccess().equals("UPDATED")) {
                responseMessage.setSuccess("UPDATED");
                response.setResponses(responseMessage);
                response.setData(body);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                errors.add(new MessageDescription("Failed to update project"));
                responseMessage.setErrors(errors);
                responseMessage.setSuccess("ERROR");
                response.setResponses(responseMessage);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else{
            log.warn("ADA Project with id {} not exists", body.getProjectID());
            errors.add(new MessageDescription("Project with ID " + body.getProjectID() + " not found"));
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("NOT_FOUND");
            response.setResponses(responseMessage);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        
    }

    @RequiresApiKeyAuthorization
    @ApiOperation(value = "Create/update workspace-project association", nickname = "createWorkspaceProjectAssociations", notes = "Create or update association between workspaces and ADA Projects", response = GenericMessage.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Workspace-project associations created successfully", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 404, message = "Workspace or ADA Project not found") })
    @RequestMapping(value = "/ada/workspace-project-association",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> createWorkspaceProjectAssociations(@ApiParam(value = "workspace-project association object" ,required=true )  @Valid @RequestBody WorkspaceProjectAssociationVO body){

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> warnings = new ArrayList<>();
        List<MessageDescription> errors = new ArrayList<>();
        FabricWorkspaceVO existingWorkspaceVO = new FabricWorkspaceVO();
        ADAProjectDetailsVO existingADAProject = new ADAProjectDetailsVO();

        existingADAProject = service.getByUniqueliteral("projectID", body.getProjectID());
        if(existingADAProject == null) {
            log.error("ADA Project with ID {} not found", body.getProjectID());
            errors.add(new MessageDescription("ADA Project with ID " + body.getProjectID() + " not found"));
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("NOT_FOUND");
            return new ResponseEntity<>(responseMessage,HttpStatus.NOT_FOUND);
        }
        existingWorkspaceVO = fabricWorkspaceService.getById(body.getWorkspaceID());
        if(existingWorkspaceVO == null || !body.getWorkspaceID().equalsIgnoreCase(existingWorkspaceVO.getId())) {
			log.warn("No Fabric Workspace found with id {}", body.getWorkspaceID());
            errors.add(new MessageDescription("Workspace with ID " + body.getWorkspaceID() + " not found"));
			responseMessage.setErrors(errors);
            responseMessage.setSuccess("NOT_FOUND");
			return new ResponseEntity<>(responseMessage,HttpStatus.NOT_FOUND);
		}

        GenericMessage message = service.createWorkspaceProjectAssociation(existingWorkspaceVO, body.getProjectID());
        if (message.getSuccess().equals("SUCCESS")) {
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } else {
            responseMessage.setErrors(message.getErrors());
            return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
