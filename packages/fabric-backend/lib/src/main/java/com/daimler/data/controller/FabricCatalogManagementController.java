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
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogResponseVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.catalogManagement.FabricCatalogManagementService;
import com.daimler.data.service.fabric.FabricWorkspaceService;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;

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

    @ApiOperation(value = "Publish a new catalog.", nickname = "publishCatalogRequest", notes = "This endpoint will be used to publish a new fabric catalog.", response = PublishCatalogResponseVO.class, tags = {
            "fabric-catalog-management", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure ", response = PublishCatalogResponseVO.class),
            @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/catalog/publish", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.POST)
    public ResponseEntity<PublishCatalogResponseVO> publishCatalogRequest(
            @ApiParam(value = "The catalog to publish.", required = true) @Valid @RequestBody PublishCatalogRequestVO publishCatalogRequest) {

        PublishCatalogResponseVO responseVO = new PublishCatalogResponseVO();

        GenericMessage responseMessage = new GenericMessage();

        FabricWorkspaceVO existingFabricWorkspace = fabricWorkspaceService
                .getById(publishCatalogRequest.getWorkspaceId());
        if (existingFabricWorkspace == null
                || !publishCatalogRequest.getWorkspaceId().equalsIgnoreCase(existingFabricWorkspace.getId())) {
            log.warn("No Fabric Workspace found with id {}", publishCatalogRequest.getWorkspaceId());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        CreatedByVO requestUser = this.userStore.getVO();
        String creatorId = existingFabricWorkspace.getCreatedBy().getId();

        if (!requestUser.getId().equalsIgnoreCase(creatorId)
                && !userStore.getUserInfo().hasProjectAdminAccess(publishCatalogRequest.getWorkspaceId())) {
            log.error(
                    "Fabric workspace {} {} doesnt belong to User or user not admin {} , Not authorized to publish catalog.",
                    publishCatalogRequest.getWorkspaceId(), existingFabricWorkspace.getName(), requestUser.getId());
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        try {

            openMetadataClient.getUserByFqn(requestUser.getId());
            responseMessage = service.publishCatalogMetaData(publishCatalogRequest);
            if (("SUCCESS").equalsIgnoreCase(responseMessage.getSuccess())) {
                responseVO.setResponses(responseMessage);
                return new ResponseEntity<>(responseVO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
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
