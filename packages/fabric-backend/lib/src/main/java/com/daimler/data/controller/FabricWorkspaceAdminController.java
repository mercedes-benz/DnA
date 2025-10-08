package com.daimler.data.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.daimler.data.api.fabricWorkspace.FabricWorkspacesApi;
import com.daimler.data.api.fabricWorkspace.LovsApi;
import com.daimler.data.api.fabricWorkspaceAdmin.FabricWorkspacesAdminApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
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
    public ResponseEntity<FabricWorkspacesCollectionVO> getAllForFabricAdmin(@ApiParam(value = "Page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,@ApiParam(value = "Page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,@ApiParam(value = "Sort workspaces by a given variable like name or createdOn", allowableValues = "name, createdOn") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,@ApiParam(value = "Sort workspaces based on the given order (asc or desc)", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder){
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
		log.info("User roles from session/token: {}", currentUserInfo.getUserRole());
		if (!currentUserInfo.hasFabricAdminAccess()) {
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}

    	FabricWorkspacesCollectionVO collection = service.getAllForFabricAdmin(limit, offset);
		HttpStatus responseCode = (collection.getRecords() != null && !collection.getRecords().isEmpty()) ? HttpStatus.OK : HttpStatus.NO_CONTENT;
		return new ResponseEntity<>(collection, responseCode);
	}


}