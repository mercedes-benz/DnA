package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// import com.daimler.data.api.fabricCatalogOnboarding.FabricCatalogOnboardingApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.fabric.DdxOnboardingResponseVO;
import com.daimler.data.dto.fabric.DdxOnboardingResultDto;
import com.daimler.data.dto.fabric.DdxOnboardingRequestDto;
import com.daimler.data.dto.fabric.DdxResponseDto;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.fabric.FabricWorkspaceService;
import com.daimler.data.service.fabric.DdxOnboardingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Fabric Catalog Onboarding APIs")
@RequestMapping("/api/fabric-workspaces")
@Slf4j
public class DdxOnboardingController {

	@Autowired
	private UserStore userStore;

	@Autowired
	private FabricWorkspaceService fabricWorkspaceService;

	@Autowired
	private DdxOnboardingService ddxOnboardingService;

	@ApiOperation(value = "Publish a new catalog for DDX onboarding.", nickname = "publishCatalogRequest", 
		notes = "This endpoint will be used to onboard data from fabric to databricks.", 
		response = DdxOnboardingResponseVO.class, tags = { "fabric-catalog-management" })
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "Returns message of success or failure", response = DdxOnboardingResponseVO.class),
		@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
		@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
		@ApiResponse(code = 403, message = "Request is not authorized."),
		@ApiResponse(code = 405, message = "Method not allowed"),
		@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/catalog/ddx/{workspaceId}/{lakehouseId}/publish", 
		produces = { "application/json" }, 
		consumes = { "application/json" }, 
		method = RequestMethod.POST)
	public ResponseEntity<DdxOnboardingResponseVO> publishCatalogRequest(
		@ApiParam(value = "The catalog to publish.", required = true) @Valid @RequestBody DdxOnboardingRequestDto publishDdxRequest,
		@ApiParam(value = "The ID of the workspace.", required = true) @PathVariable("workspaceId") String workspaceId,
		@ApiParam(value = "The ID of the lakehouse.", required = true) @PathVariable("lakehouseId") String lakehouseId) {

		DdxOnboardingResponseVO responseVO = new DdxOnboardingResponseVO();

		log.info("Publishing catalog for workspace: {} and lakehouse: {}", workspaceId, lakehouseId);

		try {
			// Validate workspace exists
			FabricWorkspaceVO existingFabricWorkspace = fabricWorkspaceService.getById(workspaceId);

			if (existingFabricWorkspace == null
					|| !workspaceId.equalsIgnoreCase(existingFabricWorkspace.getId())) {
				log.error("No Fabric Workspace found with id {}", workspaceId);
				GenericMessage errorResponse = new GenericMessage();
				errorResponse.setSuccess("FAILED");
				MessageDescription message = new MessageDescription();
				message.setMessage("Workspace not found with id: " + workspaceId);
				errorResponse.addErrors(message);
				responseVO.setResponses(errorResponse);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}

			// Validate user authorization
			String requestUserId = userStore.getVO().getId();
			CreatedByVO createdByVO = userStore.getVO();
			String creatorId = existingFabricWorkspace.getCreatedBy().getId();

			if (!requestUserId.equalsIgnoreCase(creatorId)
					&& !userStore.getUserInfo().hasProjectAdminAccess(workspaceId)) {
				log.error(
					"User {} is not authorized to publish catalog for workspace {}",
					requestUserId, workspaceId);
				GenericMessage errorResponse = new GenericMessage();
				errorResponse.setSuccess("FAILED");
				MessageDescription message = new MessageDescription();
				message.setMessage("User is not authorized to publish catalog for this workspace");
				errorResponse.addErrors(message);
				responseVO.setResponses(errorResponse);
				return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
			}

			DdxOnboardingResultDto serviceResult = ddxOnboardingService.onboardToDdx(publishDdxRequest, workspaceId, existingFabricWorkspace.getName(), lakehouseId, requestUserId, createdByVO);
			responseVO.setResponses(serviceResult.getResponseMessage());
			responseVO.setData(serviceResult.getDdxResponse());

			String status = serviceResult.getResponseMessage().getSuccess();
			if ("SUCCESS".equals(status)) {
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else if ("PARTIAL_SUCCESS".equals(status)) {
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			} else {
				responseVO.setData(null);
				DdxResponseDto ddxResp = serviceResult.getDdxResponse();
				if (ddxResp != null && ddxResp.getMessage() != null
						&& ddxResp.getMessage().toLowerCase().contains("already exists")) {
					return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
				}
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			log.error("Exception occurred while publishing DDX onboarding catalog: {}", e.getMessage(), e);
			GenericMessage failedResponse = new GenericMessage();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to publish catalog due to internal error: " + e.getMessage());
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(null);
			responseVO.setResponses(failedResponse);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}
}
