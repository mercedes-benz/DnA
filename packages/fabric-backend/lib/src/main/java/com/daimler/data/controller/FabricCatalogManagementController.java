package com.daimler.data.controller;

import java.util.List;
import java.util.ArrayList;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.application.client.OpenMetadataClient;
import com.daimler.data.controller.exceptions.EntityNotFoundException;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.controller.exceptions.OpenMetadataClientException;
import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadata;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementCustomRepository;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.LakehouseObjectsResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricCatalogManagement.UpdateDDXGroupsRequestVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.catalogManagement.FabricCatalogManagementService;
import com.daimler.data.service.fabric.FabricWorkspaceService;
import com.daimler.data.util.FabricWorkspaceUtility;

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
    @Autowired 
    private FabricWorkspaceUtility utility;

    @Autowired
    private FabricWorkspaceClient fabricWorkspaceClient;

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
        if(existingCatalog != null && existingCatalog.getData() != null && existingCatalog.getData().getMetadata().getServiceName() !=null){
             log.error("Catalog already exists for name {}", existingCatalog.getData().getMetadata().getServiceName());
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
        CreatedByVO requestUser = this.userStore.getVO();
        String creatorId = existingFabricWorkspace.getCreatedBy().getId();

        if (!requestUser.getId().equalsIgnoreCase(creatorId)
                && !utility.hasProjectAdminAccess(requestUser.getId(), workspaceId)) {
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
			message.setMessage("User " + requestUser.getId() + " not found in CDC. Please log in to CDC first and try again.");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("User:{} not found in CDC (OpenMetadata). Details: {} ", userStore.getUserInfo().getId(), e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
        } catch (OpenMetadataClientException e) {
            GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to publish fabric workspace catalog: unable to connect to CDC. " + e.getMessage());
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("CDC connection error while publishing catalog for user:{}, error:{}", userStore.getUserInfo().getId(), e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
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
                    && !utility.hasProjectAdminAccess(requestUser.getId(), workspaceId)) {
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
			message.setMessage("User " + requestUser.getId() + " not found in CDC. Please log in to CDC first and try again.");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("User:{} not found in CDC (OpenMetadata). Details: {} ", userStore.getUserInfo().getId(), e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
        } catch (OpenMetadataClientException e) {
            GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update fabric workspace catalog: unable to connect to CDC. " + e.getMessage());
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("CDC connection error while updating catalog for user:{}, error:{}", userStore.getUserInfo().getId(), e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update fabric workspace catalog due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
            responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			log.error("Exception occurred:{} while updating fabric workspace catalog...", e.getMessage());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    @ApiOperation(value = "update groups from ddx.", nickname = "updateGroupsFromDDX", notes = "This endpoint will be used to update groups from ddx.", response = GenericMessage.class, tags={ "fabric-catalog-management", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure ", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Workspace, Lakehouse, or Group not found", response = GenericMessage.class),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/catalog/ddx/group-update/{ddxId}/{workspaceId}/{lakehouseId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> updateGroupsFromDDX(@ApiParam(value = "The groups update request from DDX." ,required=true )  @Valid @RequestBody UpdateDDXGroupsRequestVO updateDDXGroupsRequest,@ApiParam(value = "The ID of DDX data product .",required=true) @PathVariable("ddxId") String ddxId,@ApiParam(value = "The ID of the workspace.",required=true) @PathVariable("workspaceId") String workspaceId,@ApiParam(value = "The ID of Lakehouse.",required=true) @PathVariable("lakehouseId") String lakehouseId){

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        List<MessageDescription> warnings = new ArrayList<>();
        
        // Validate workspace exists
        try {
            com.daimler.data.dto.fabric.WorkspacesCollectionDto workspaces = fabricWorkspaceClient.getAllWorkspacesDetails();
            boolean workspaceExists = false;
            if (workspaces != null && workspaces.getValue() != null) {
                workspaceExists = workspaces.getValue().stream()
                    .anyMatch(w -> w != null && workspaceId.equals(w.getId()));
            }
            if (!workspaceExists) {
                String errorMsg = "Workspace not found";
                log.error("Workspace {} not found or error: {}", workspaceId, errorMsg);
                MessageDescription error = new MessageDescription();
                error.setMessage("Workspace not found: " + workspaceId);
                errors.add(error);
                responseMessage.setErrors(errors);
                responseMessage.setWarnings(warnings);
                responseMessage.setSuccess("FAILED");
                return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error validating workspace {}: {}", workspaceId, e.getMessage());
            MessageDescription error = new MessageDescription();
            error.setMessage("Error validating workspace: " + e.getMessage());
            errors.add(error);
            responseMessage.setErrors(errors);
            responseMessage.setWarnings(warnings);
            responseMessage.setSuccess("FAILED");
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }

        // Validate lakehouse exists
        try {
            com.daimler.data.dto.fabric.LakehouseCollectionDto lakehouses = fabricWorkspaceClient.listLakehouses(workspaceId);
            boolean lakehouseExists = false;
            if (lakehouses != null && lakehouses.getValue() != null) {
                lakehouseExists = lakehouses.getValue().stream()
                    .anyMatch(lh -> lh != null && lakehouseId.equals(lh.getId()));
            }
            if (!lakehouseExists) {
                log.error("Lakehouse {} not found in workspace {}", lakehouseId, workspaceId);
                MessageDescription error = new MessageDescription();
                error.setMessage("Lakehouse not found: " + lakehouseId + " in workspace: " + workspaceId);
                errors.add(error);
                responseMessage.setErrors(errors);
                responseMessage.setWarnings(warnings);
                responseMessage.setSuccess("FAILED");
                return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error validating lakehouse {} in workspace {}: {}", lakehouseId, workspaceId, e.getMessage());
            MessageDescription error = new MessageDescription();
            error.setMessage("Error validating lakehouse: " + e.getMessage());
            errors.add(error);
            responseMessage.setErrors(errors);
            responseMessage.setWarnings(warnings);
            responseMessage.setSuccess("FAILED");
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }
        
        try {

        
            // Validate request
            if (updateDDXGroupsRequest == null || updateDDXGroupsRequest.getGroups() == null || updateDDXGroupsRequest.getGroups().isEmpty()) {
                log.error("Invalid request: groups list is empty or null");
                MessageDescription error = new MessageDescription();
                error.setMessage("Invalid request: groups list cannot be empty");
                errors.add(error);
                responseMessage.setErrors(errors);
                responseMessage.setWarnings(warnings);
                responseMessage.setSuccess("FAILED");
                return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
            }

            int successCount = 0;
            int failureCount = 0;

            // Check if single group is null or empty - return 400 Bad Request
            if (updateDDXGroupsRequest.getGroups().size() == 1) {
                String singleGroup = updateDDXGroupsRequest.getGroups().get(0);
                if (singleGroup == null || singleGroup.trim().isEmpty()) {
                    MessageDescription error = new MessageDescription();
                    error.setMessage("Invalid request: group ID cannot be empty or null");
                    errors.add(error);
                    responseMessage.setErrors(errors);
                    responseMessage.setWarnings(warnings);
                    responseMessage.setSuccess("FAILED");
                    return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
                }
            }

            // Process each group
            for (String groupId : updateDDXGroupsRequest.getGroups()) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    MessageDescription warning = new MessageDescription();
                    warning.setMessage("Skipping empty or null group ID");
                    warnings.add(warning);
                    continue;
                }

                String trimmedGroupId = groupId.trim();

                // Validate group exists
                try {
                    if (!fabricWorkspaceClient.checkGroupExists(trimmedGroupId)) {
                        failureCount++;
                        MessageDescription error = new MessageDescription();
                        error.setMessage("Group not found: " + trimmedGroupId);
                        errors.add(error);
                        log.error("Group {} not found", trimmedGroupId);
                        continue;
                    }
                } catch (Exception e) {
                    failureCount++;
                    MessageDescription error = new MessageDescription();
                    error.setMessage("Error validating group: " + trimmedGroupId + " - " + e.getMessage());
                    errors.add(error);
                    log.error("Error checking if group {} exists: {}", trimmedGroupId, e.getMessage());
                    continue;
                }

                // Role is hardcoded to "Viewer" - type field is only for identification
                String role = "Viewer";

                try {
                    com.daimler.data.dto.fabric.RoleAssignmentResponseDto roleResponse = 
                        fabricWorkspaceClient.assignRoleToWorkspace(workspaceId, trimmedGroupId, role);
                    
                    if (roleResponse != null && roleResponse.getErrorCode() == null) {
                        successCount++;
                        log.info("Successfully assigned role {} to group {} for workspace {}", role, trimmedGroupId, workspaceId);
                    } else {
                        failureCount++;
                        MessageDescription error = new MessageDescription();
                        String errorMsg = String.format("Failed to assign role %s to group %s: %s", 
                            role, trimmedGroupId, 
                            roleResponse != null && roleResponse.getMessage() != null ? roleResponse.getMessage() : "Unknown error");
                        error.setMessage(errorMsg);
                        errors.add(error);
                        log.error("Failed to assign role {} to group {} for workspace {}: {}", 
                            role, trimmedGroupId, workspaceId, 
                            roleResponse != null ? roleResponse.getMessage() : "Unknown error");
                    }
                } catch (Exception e) {
                    failureCount++;
                    MessageDescription error = new MessageDescription();
                    error.setMessage(String.format("Exception while assigning role %s to group %s: %s", 
                        role, trimmedGroupId, e.getMessage()));
                    errors.add(error);
                    log.error("Exception while assigning role {} to group {} for workspace {}: {}", 
                        role, trimmedGroupId, workspaceId, e.getMessage(), e);
                }
            }

            // Prepare response
            if (errors.isEmpty() && successCount > 0) {
                responseMessage.setSuccess("SUCCESS");
                MessageDescription successMsg = new MessageDescription();
                // successMsg.setMessage(String.format("Successfully processed %d group(s)", successCount));
                // warnings.add(successMsg);
            } else if (successCount > 0 && failureCount > 0) {
                responseMessage.setSuccess("PARTIAL_SUCCESS");
                MessageDescription partialMsg = new MessageDescription();
                partialMsg.setMessage(String.format("Partially successful: %d succeeded, %d failed", successCount, failureCount));
                warnings.add(partialMsg);
            } else {
                responseMessage.setSuccess("FAILED");
            }

            responseMessage.setErrors(errors);
            responseMessage.setWarnings(warnings);

            // Return appropriate HTTP status
            if ("SUCCESS".equals(responseMessage.getSuccess())) {
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else if ("PARTIAL_SUCCESS".equals(responseMessage.getSuccess())) {
                return new ResponseEntity<>(responseMessage, HttpStatus.MULTI_STATUS);
            } else if (!errors.isEmpty()) {
                // Return NOT_FOUND if we have specific validation errors
                return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument in updateGroupsFromDDX for workspace {}: {}", workspaceId, e.getMessage());
            MessageDescription error = new MessageDescription();
            error.setMessage("Invalid argument: " + e.getMessage());
            errors.add(error);
            responseMessage.setErrors(errors);
            responseMessage.setWarnings(warnings);
            responseMessage.setSuccess("FAILED");
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error in updateGroupsFromDDX for workspace {}, lakehouse {}: {}", workspaceId, lakehouseId, e.getMessage(), e);
            MessageDescription error = new MessageDescription();
            error.setMessage("Unexpected error occurred: " + e.getMessage());
            errors.add(error);
            responseMessage.setErrors(errors);
            responseMessage.setWarnings(warnings);
            responseMessage.setSuccess("FAILED");
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = "get lakehouse objects.", nickname = "getLakehouseObjects", notes = "This endpoint will be used to get lakehouse objects.", response = LakehouseObjectsResponseVO.class, tags={ "fabric-catalog-management", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = LakehouseObjectsResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/catalog/ddx/fabric-lakehouses/objects",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<LakehouseObjectsResponseVO> getLakehouseObjects(@NotNull @ApiParam(value = "The ID of the workspace.", required = true) @Valid @RequestParam(value = "workspaceId", required = true) String workspaceId,@NotNull @ApiParam(value = "The ID of Lakehouse.", required = true) @Valid @RequestParam(value = "lakehouseId", required = true) String lakehouseId,@ApiParam(value = "The name of schema.") @Valid @RequestParam(value = "schemaName", required = false) String schemaName){
        LakehouseObjectsResponseVO responseVO = new LakehouseObjectsResponseVO();
        try {
            responseVO = service.getLakehouseObjects(workspaceId, lakehouseId, schemaName);
            
            if (responseVO != null && responseVO.getResponseCode() != null) {
                try {
                    int statusCode = Integer.parseInt(responseVO.getResponseCode());
                    return new ResponseEntity<>(responseVO, HttpStatus.valueOf(statusCode));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid response code format: {}", responseVO.getResponseCode());
                    return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            
            return new ResponseEntity<>(responseVO, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred while fetching lakehouse objects: {}", e.getMessage());
            responseVO.setErrorMessage("Exception occurred while fetching lakehouse objects: " + e.getMessage());
            responseVO.setResponseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

