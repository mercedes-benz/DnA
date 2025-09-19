package com.daimler.data.controller;

import java.util.List;
import java.util.ArrayList;
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
import org.openmetadata.client.model.*;

import com.daimler.data.api.fabricCatalogManagement.FabricCatalogManagementApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.OpenMetadataClient;
import com.daimler.data.controller.exceptions.EntityNotFoundException;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadata;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementCustomRepository;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.catalogManagement.FabricCatalogManagementService;
import com.daimler.data.service.fabric.FabricWorkspaceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Fabric Catalog Management APIs")
@RequestMapping("/api/fabric-workspaces")
@Slf4j
public class FabricCatalogManagementController implements FabricCatalogManagementApi {

    @Autowired
    private UserStore userStore;

    @Autowired
    private FabricWorkspaceService fabricWorkspaceService;

    @Autowired
    private OpenMetadataClient openMetadataClient;

    @Autowired
    private FabricCatalogManagementService service;

    @Autowired
    private FabricCatalogManagementCustomRepository catalogCustomRepo;

    @Override
     @ApiOperation(value = "Publish a new catalog.", nickname = "publishCatalogRequest", notes = "This endpoint will be used to publish a new fabric catalog.", response = PublishCatalogResponseVO.class, tags={ "fabric-catalog-management", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = PublishCatalogResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/catalog/{workspaceId}/publish",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<PublishCatalogResponseVO> publishCatalogRequest(@ApiParam(value = "The catalog to publish." ,required=true )  @Valid @RequestBody PublishCatalogRequestVO publishCatalogRequest,@ApiParam(value = "The ID of the workspace.",required=true) @PathVariable("workspaceId") String workspaceId) {

        PublishCatalogResponseVO responseVO = new PublishCatalogResponseVO();

        FabricWorkspaceVO existingFabricWorkspace = fabricWorkspaceService
                .getById(workspaceId);
            
        if (existingFabricWorkspace == null
                || !workspaceId.equalsIgnoreCase(existingFabricWorkspace.getId())) {
            log.error("No Fabric Workspace found with id {}", workspaceId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        FabricCatalogMetadataNsql existingCatalog = catalogCustomRepo.findByServiceName(existingFabricWorkspace.getName()).orElse(null);
        if(existingCatalog.getData() != null && existingCatalog.getData().getMetadata().getServiceName() !=null){
             log.error("Catalog already exists for name {}", existingCatalog.getData().getMetadata().getServiceName());
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
        CreatedByVO requestUser = this.userStore.getVO();
        String creatorId = existingFabricWorkspace.getCreatedBy().getId();

        if (!requestUser.getId().equalsIgnoreCase(creatorId)
                && !userStore.getUserInfo().hasProjectAdminAccess(workspaceId)) {
            log.error(
                    "Fabric workspace {} {} doesnt belong to User or user not admin {} , Not authorized to publish catalog.",
                    workspaceId, existingFabricWorkspace.getName(), requestUser.getId());
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        try {

            openMetadataClient.getUserByFqn(requestUser.getId());
            responseVO = service.publishCatalogMetaData(publishCatalogRequest, existingFabricWorkspace);
            GenericMessage responseMessage = responseVO.getResponses();
            if (("SUCCESS").equalsIgnoreCase(responseMessage.getSuccess())) {
                return new ResponseEntity<>(responseVO, HttpStatus.OK);
            } else if (("CONFLICT").equalsIgnoreCase(responseMessage.getSuccess())) {
                return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (EntityNotFoundException e) {
             GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to publish fabric workspace catalog : user didn't log in to cdc");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("User:{} didnt logged into cdc {} ", userStore.getUserInfo().getId(), e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to publish fabric workspace catalog due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("Exception occurred:{} while publishing fabric workspace catalog...", e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
   @ApiOperation(value = "Get catalog by service name.", nickname = "getCatalogByServiceName", notes = "This endpoint will be used to retrieve a fabric catalog by its service name.", response = PublishCatalogResponseVO.class, tags={ "fabric-catalog-management", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = PublishCatalogResponseVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/catalog/{workspaceId}/{serviceName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PublishCatalogResponseVO> getCatalogByServiceName(@ApiParam(value = "The ID of the workspace.",required=true) @PathVariable("workspaceId") String workspaceId,@ApiParam(value = "The name of the service.",required=true) @PathVariable("serviceName") String serviceName){
        PublishCatalogResponseVO catalogMetadata = new PublishCatalogResponseVO();
        
        try{

            FabricWorkspaceVO existingFabricWorkspace = fabricWorkspaceService
                .getById(workspaceId);
            
            if (existingFabricWorkspace == null
                    || !workspaceId.equalsIgnoreCase(existingFabricWorkspace.getId())) {
                log.error("No Fabric Workspace found with id {}", workspaceId);
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }

            CreatedByVO requestUser = this.userStore.getVO();
            String creatorId = existingFabricWorkspace.getCreatedBy().getId();

            if (!requestUser.getId().equalsIgnoreCase(creatorId)
                    && !userStore.getUserInfo().hasProjectAdminAccess(workspaceId)) {
                log.error(
                        "Fabric workspace {} {} doesnt belong to User or user not admin {} , Not authorized to publish catalog.",
                        workspaceId, existingFabricWorkspace.getName(), requestUser.getId());
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }

            catalogMetadata = service.getCatalogMetadata(serviceName);
            if (catalogMetadata != null) {
                return new ResponseEntity<>(catalogMetadata, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (EntityNotFoundException e) {
            log.error("Service:{} not found in cdc", serviceName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while fetching catalog metadata: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = "update Published catalog.", nickname = "updatePublishedCatalogRequest", notes = "This endpoint will be used to update published fabric catalog.", response = PublishCatalogResponseVO.class, tags={ "fabric-catalog-management", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = PublishCatalogResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/catalog/{workspaceId}/publish",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<PublishCatalogResponseVO> updatePublishedCatalogRequest(@ApiParam(value = "The catalog to publish." ,required=true )  @Valid @RequestBody PublishCatalogRequestVO updateCatalogRequest,@ApiParam(value = "The ID of the workspace.",required=true) @PathVariable("workspaceId") String workspaceId){

        PublishCatalogResponseVO responseVO = new PublishCatalogResponseVO();

        FabricWorkspaceVO existingFabricWorkspace = fabricWorkspaceService
                .getById(workspaceId);
            
        if (existingFabricWorkspace == null
                || !workspaceId.equalsIgnoreCase(existingFabricWorkspace.getId())) {
            log.error("No Fabric Workspace found with id {}", workspaceId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        CreatedByVO requestUser = this.userStore.getVO();
        String creatorId = existingFabricWorkspace.getCreatedBy().getId();

        if (!requestUser.getId().equalsIgnoreCase(creatorId)
                && !userStore.getUserInfo().hasProjectAdminAccess(workspaceId)) {
            log.error(
                    "Fabric workspace {} {} doesnt belong to User or user not admin {} , Not authorized to publish catalog.",
                    workspaceId, existingFabricWorkspace.getName(), requestUser.getId());
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        try {

            openMetadataClient.getUserByFqn(requestUser.getId());
            responseVO = service.updateCatalogMetaData(updateCatalogRequest, existingFabricWorkspace);
            GenericMessage responseMessage = responseVO.getResponses();
            if (("SUCCESS").equalsIgnoreCase(responseMessage.getSuccess())) {
                return new ResponseEntity<>(responseVO, HttpStatus.OK);
            } else if (("CONFLICT").equalsIgnoreCase(responseMessage.getSuccess())) {
                return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (EntityNotFoundException e) {
             GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to publish fabric workspace catalog : user didn't log in to cdc");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("User:{} didnt logged into cdc {} ", userStore.getUserInfo().getId(), e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to publish fabric workspace catalog due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("Exception occurred:{} while publishing fabric workspace catalog...", e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
