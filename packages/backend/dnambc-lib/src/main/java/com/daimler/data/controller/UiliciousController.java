package com.daimler.data.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.daimler.data.api.uilicious.UiliciousWorkspacesApi;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;

import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateRequestVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateResponseVO;

import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.service.uiliciousWorkspace.UiliciousWorkspaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Uilicious API's")
@RequestMapping("/api")
@Slf4j
public class UiliciousController implements UiliciousWorkspacesApi {

        @Autowired
        private UiliciousWorkspaceService uiliciousWorkspaceService;

        @Override
        @ApiOperation(value = "Get list of workspaces for Uilicious", nickname = "getUiliciousWorkspaces", notes = "Returns Uilicious workspaces for the current user (or for the provided email). Each workspace includes metadata such as spaceName, link, lean governance and userRole. ", response = UiliciousWorkspacesCollectionVO.class, tags = {
                        "uilicious-workspaces", })
        @ApiResponses(value = {
                        @ApiResponse(code = 200, message = "Successful fetch of Uilicious workspace records.", response = UiliciousWorkspacesCollectionVO.class),
                        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
                        @ApiResponse(code = 400, message = "Bad request."),
                        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
                        @ApiResponse(code = 403, message = "Request is not authorized."),
                        @ApiResponse(code = 405, message = "Method not allowed."),
                        @ApiResponse(code = 500, message = "Internal server error.") })
        @RequestMapping(value = "/uilicious-workspaces", produces = { "application/json" }, consumes = {
                        "application/json" }, method = RequestMethod.GET)
        public ResponseEntity<UiliciousWorkspacesCollectionVO> getUiliciousWorkspaces(
                        @ApiParam(value = "Page number from which listing of workspaces should start. Example: 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
                        @ApiParam(value = "Page size to limit the number of workspaces. Example: 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
                        @ApiParam(value = "Sort order (asc or desc).", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
                // log.info("Request received to fetch Uilicious workspaces"+ offset +" "+ limit
                // +" "+ sortOrder);
                try {
                        UiliciousWorkspacesCollectionVO workspaces = uiliciousWorkspaceService.getUiliciousWorkspaces(
                                        offset, limit,
                                        sortOrder);
                        if (workspaces == null) {
                                log.warn("Something went wrong with uilicious api");
                                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                        // setTotalRecords
                        if (workspaces.getItems() == null || workspaces.getItems().isEmpty()) {
                                log.info("No workspaces found — returning 204 No Content");
                                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                        }

                        return ResponseEntity.ok(workspaces);
                } catch (RuntimeException e) {
                        // Check if it's a Uilicious server unavailability issue
                        if (e.getMessage() != null
                                        && e.getMessage().contains("Something went wrong with Uilicious server/tool")) {
                                log.error("Uilicious server is unavailable: {}", e.getMessage());
                                GenericMessage errorMessage = new GenericMessage();
                                errorMessage.setSuccess("false");
                                MessageDescription desc = new MessageDescription();
                                desc.setMessage("Something went wrong with Uilicious server/tool. Please try again later.");
                                errorMessage.addErrors(desc);
                                return new ResponseEntity(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
                        }
                        log.error("Unexpected error while fetching Uilicious workspaces: {}", e.getMessage(), e);
                        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @Override
        @ApiOperation(value = "Update lean governance for Uilicious workspace", nickname = "updateUiliciousWorkspace", notes = "Updates the lean governance information for an existing Uilicious workspace identified by accountId.", response = UiliciousWorkspaceUpdateResponseVO.class, tags = {
                        "uilicious-workspaces", })
        @ApiResponses(value = {
                        @ApiResponse(code = 200, message = "Lean governance updated successfully", response = UiliciousWorkspaceUpdateResponseVO.class),
                        @ApiResponse(code = 204, message = "No content to update."),
                        @ApiResponse(code = 400, message = "Bad request - Invalid accountId or lean governance data."),
                        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
                        @ApiResponse(code = 403, message = "Request is not authorized."),
                        @ApiResponse(code = 404, message = "Workspace not found for the provided accountId."),
                        @ApiResponse(code = 405, message = "Method not allowed."),
                        @ApiResponse(code = 500, message = "Internal server error.") })
        @RequestMapping(value = "/uilicious-workspaces", produces = { "application/json" }, consumes = {
                        "application/json" }, method = RequestMethod.PUT)
        public ResponseEntity<UiliciousWorkspaceUpdateResponseVO> updateUiliciousWorkspace(
                        @ApiParam(value = "Request Body that contains accountId and lean governance data to be updated", required = true) @Valid @RequestBody UiliciousWorkspaceUpdateRequestVO uiliciousWorkspaceUpdateRequestVO) {

                log.info("Request received to update Uilicious workspace for accountId: {}",
                                uiliciousWorkspaceUpdateRequestVO.getAccountId());

                try {
                        // Call service to update the workspace
                        UiliciousWorkspaceUpdateResponseVO response = uiliciousWorkspaceService
                                        .updateUiliciousWorkspace(uiliciousWorkspaceUpdateRequestVO);

                        if (response == null) {
                                log.warn("Workspace not found for accountId: {}",
                                                uiliciousWorkspaceUpdateRequestVO.getAccountId());
                                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                        }

                        log.info("Successfully updated workspace for accountId: {}", response.getAccountId());
                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Invalid request data: {}", e.getMessage());
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                } catch (RuntimeException e) {
                        // Check if it's a Uilicious server unavailability issue
                        if (e.getMessage() != null
                                        && e.getMessage().contains("Something went wrong with Uilicious server/tool")) {
                                log.error("Uilicious server is unavailable during workspace update: {}",
                                                e.getMessage());
                                GenericMessage errorMessage = new GenericMessage();
                                errorMessage.setSuccess("false");
                                MessageDescription desc = new MessageDescription();
                                desc.setMessage("Something went wrong with Uilicious server/tool. Please try again later.");
                                errorMessage.addErrors(desc);
                                return new ResponseEntity(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
                        }
                        log.error("Unexpected runtime error while updating workspace: {}", e.getMessage(), e);
                        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (Exception e) {
                        log.error("Error updating workspace: {}", e.getMessage(), e);
                        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }
}
