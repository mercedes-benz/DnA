package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.fabricWorkspaceAdmin.FabricWorkspacesAdminApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.dto.adaProjects.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.service.capacity.CapacityService;
import com.daimler.data.service.fabric.FabricWorkspaceAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Fabric APIs")
@RequestMapping("/api")
@Slf4j
public class FabricWorkspaceAdminController implements FabricWorkspacesAdminApi
{
    @Autowired
	private FabricWorkspaceAdminService service;

	@Autowired
	private CapacityService capacityService;	

	@Autowired
	private UserStore userStore;

	@ApiOperation(value = "Get all workspaces for Fabric Admin users.", nickname = "getAllForFabricAdmin", notes = "Fetches all valid fabric workspace records. This endpoint is available only for users who have the FabricAdmin role.", response = FabricWorkspacesCollectionVO.class, tags={ "fabric-workspaces-admin", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns list of workspaces for Fabric Admin.", response = FabricWorkspacesCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "User is not a Fabric Admin."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/fabric-workspaces/admin/workspaces",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<FabricWorkspacesCollectionVO> getAllForFabricAdmin(
		@ApiParam(value = "Page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
		@ApiParam(value = "Page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
		@ApiParam(value = "Sort workspaces by a given variable like name or createdOn", allowableValues = "name, createdOn") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
		@ApiParam(value = "Sort workspaces based on the given order (asc or desc)", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
		@ApiParam(value = "Search term to filter workspaces by name.") @Valid @RequestParam(value = "search", required = false) String search)
		{
			int defaultLimit = 15;
			if (offset == null || offset < 0) offset = 0;
			if (limit == null || limit < 0) limit = defaultLimit;

			if (this.userStore.getUserInfo() == null ||
					this.userStore.getVO() == null ||
					this.userStore.getVO().getId() == null ||
					"".equalsIgnoreCase(this.userStore.getVO().getId().trim())) {
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}

			UserInfo currentUserInfo = this.userStore.getUserInfo();
			// log.info("User roles from session/token: {}", currentUserInfo.getUserRole());
			if (!currentUserInfo.hasFabricAdminAccess()) {
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}

			FabricWorkspacesCollectionVO collection = service.getAllForFabricAdmin(limit, offset, search);
			HttpStatus responseCode = (collection.getRecords() != null && !collection.getRecords().isEmpty()) ? HttpStatus.OK : HttpStatus.NO_CONTENT;
			return new ResponseEntity<>(collection, responseCode);
		}

	@Override
	@ApiOperation(value = "Get capacity details by region.", nickname = "getCapacityByRegion", notes = "Fetches capacity record for the given region. This endpoint is available only for users who have the FabricAdmin role.", response = CapacityVO.class, tags={ "fabric-workspaces-admin", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns capacity details for the given region.", response = CapacityVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "User is not a Fabric Admin."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/fabric-workspaces/admin/capacity/{region}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<CapacityVO> getCapacityByRegion(
		@ApiParam(value = "Region identifier for the capacity (e.g. global, eu, us).", required = true) @PathVariable("region") String region) {
		if (this.userStore.getUserInfo() == null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null ||
					"".equalsIgnoreCase(this.userStore.getVO().getId().trim())) {
			log.warn("Unauthorized access attempt to getCapacityByRegion for region: {}", region);
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		UserInfo currentUserInfo = this.userStore.getUserInfo();
		if (!currentUserInfo.hasFabricAdminAccess()) {
			log.warn("Access denied for getCapacityByRegion - user does not have FabricAdmin role");
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		if (region == null || region.trim().isEmpty()) {
			log.error("getCapacityByRegion called with null or empty region");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			log.info("Fetching capacity for region: {}", region);
			CapacityVO capacity = capacityService.getCapacityByRegion(region.trim());
			if (capacity == null) {
				log.info("No capacity found for region: {}", region);
				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(capacity, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception occurred while fetching capacity for region {}: {}", region, e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Create or update a capacity record.", nickname = "createOrUpdateCapacity", notes = "Creates a new capacity record or updates an existing one for the given region. This endpoint is available only for users who have the FabricAdmin role.", response = CapacityVO.class, tags={ "fabric-workspaces-admin", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Capacity created or updated successfully.", response = CapacityVO.class),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "User is not a Fabric Admin."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/fabric-workspaces/admin/capacity",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<CapacityVO> createOrUpdateCapacity(
		@ApiParam(value = "Capacity details to create or update.", required = true) @Valid @RequestBody CapacityVO capacityVO) {
		if (this.userStore.getUserInfo() == null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null ||
					"".equalsIgnoreCase(this.userStore.getVO().getId().trim())) {
			log.warn("Unauthorized access attempt to createOrUpdateCapacity");
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		UserInfo currentUserInfo = this.userStore.getUserInfo();
		if (!currentUserInfo.hasFabricAdminAccess()) {
			log.warn("Access denied for createOrUpdateCapacity - user does not have FabricAdmin role");
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		if (capacityVO == null || capacityVO.getRegion() == null || capacityVO.getRegion().trim().isEmpty()) {
			log.error("createOrUpdateCapacity called with null/empty capacity or missing region");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			String region = capacityVO.getRegion().trim();
			log.info("Creating or updating capacity for region: {}", region);
			CapacityVO result = capacityService.createOrUpdateCapacity(capacityVO, region);
			log.info("Capacity created/updated successfully for region: {}", region);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			log.error("Invalid input for createOrUpdateCapacity: {}", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error("Exception occurred while creating/updating capacity: {}", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Delete capacity for a given region.", nickname = "deleteCapacityByRegion", notes = "Deletes the capacity record for the given region. This endpoint is available only for users who have the FabricAdmin role.", response = CapacityVO.class, tags={ "fabric-workspaces-admin", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Capacity deleted successfully.", response = CapacityVO.class),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "User is not a Fabric Admin."),
        @ApiResponse(code = 404, message = "Capacity not found for the given region."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/fabric-workspaces/admin/capacity/{region}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<CapacityVO> deleteCapacityByRegion(
		@ApiParam(value = "Region identifier for the capacity to be deleted.", required = true) @PathVariable("region") String region) {
		if (this.userStore.getUserInfo() == null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null ||
					"".equalsIgnoreCase(this.userStore.getVO().getId().trim())) {
			log.warn("Unauthorized access attempt to deleteCapacityByRegion for region: {}", region);
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		UserInfo currentUserInfo = this.userStore.getUserInfo();
		if (!currentUserInfo.hasFabricAdminAccess()) {
			log.warn("Access denied for deleteCapacityByRegion - user does not have FabricAdmin role");
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		if (region == null || region.trim().isEmpty()) {
			log.error("deleteCapacityByRegion called with null or empty region");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			log.info("Deleting capacity for region: {}", region);
			CapacityVO deleted = capacityService.deleteCapacityByRegion(region.trim());
			if (deleted == null) {
				log.info("No capacity found to delete for region: {}", region);
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
			log.info("Capacity deleted successfully for region: {}", region);
			return new ResponseEntity<>(deleted, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			log.error("Invalid input for deleteCapacityByRegion: {}", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error("Exception occurred while deleting capacity for region {}: {}", region, e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
