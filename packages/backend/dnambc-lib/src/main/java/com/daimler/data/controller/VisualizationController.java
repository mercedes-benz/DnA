/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.controller;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.visualization.VisualizationsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.dto.visualization.VisualizationCollection;
import com.daimler.data.dto.visualization.VisualizationRequestVO;
import com.daimler.data.dto.visualization.VisualizationVO;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.service.visualization.VisualizationService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Visualization API", tags = { "visualizations" })
@RequestMapping("/api")
@Slf4j
public class VisualizationController implements VisualizationsApi {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private VisualizationService visualizationService;

	@Override
	@ApiOperation(value = "Adds a new visualization.", nickname = "create", notes = "Adds a new non existing visualization which is used in providing solution.", response = VisualizationVO.class, tags = {
			"visualizations", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Visualization added successfully", response = VisualizationVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/visualizations", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<VisualizationVO> create(@Valid VisualizationRequestVO visualizationRequestVO) {
		VisualizationVO requestvisualizationVO = visualizationRequestVO.getData();
		try {
			VisualizationVO existingvisualizationVO = visualizationService.getByUniqueliteral("name",
					requestvisualizationVO.getName());
			if (existingvisualizationVO != null && existingvisualizationVO.getName() != null) {
				log.info("visualization {} already exists, creation failed", requestvisualizationVO.getName());
				return new ResponseEntity<>(existingvisualizationVO, HttpStatus.CONFLICT);
			}
			requestvisualizationVO.setId(null);
			VisualizationVO visualizationVO = visualizationService.create(requestvisualizationVO);
			if (visualizationVO != null && visualizationVO.getId() != null) {
				log.info("New visualization {} created successfully with id {}", requestvisualizationVO.getName(),
						visualizationVO.getId());
				return new ResponseEntity<>(visualizationVO, HttpStatus.CREATED);
			} else
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Failed to create new visualization {} , exception msg {}", requestvisualizationVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the visualization identified by given ID.", nickname = "delete", notes = "Deletes the visualization identified by given ID", response = GenericMessage.class, tags = {
			"visualizations", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/visualizations/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the visualization", required = true) @PathVariable("id") String id) {
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						if (userId == null || !isAdmin) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to delete Visualizations. User does not have admin privileges.");
							log.info("User not authorized to delete");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			VisualizationVO visualization = visualizationService.getById(id);
			String visualizationName = visualization != null ? visualization.getName() : "";
			String userName = visualizationService.currentUserName(currentUser);
			String eventMessage = "Visualization  " + visualizationName + " has been deleted by Admin " + userName;
			visualizationService.deleteVisualization(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			log.info("Visualiztion with id {} deleted successfully", id);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);

		} catch (EntityNotFoundException e) {
			log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No tag with the given id");
			log.info("Visualiztion with id {} doesnt exist, cannt be deleted", id);
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to delete visualization with id {}, exception msg {} ", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available visualizations.", nickname = "getAll", notes = "Get all visualizations. This endpoints will be used to Get all valid available visualizations maintenance records.", response = VisualizationCollection.class, tags = {
			"visualizations", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all visualizations", response = VisualizationCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/visualizations", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<VisualizationCollection> getAll() {
		final List<VisualizationVO> visualizations = visualizationService.getAll();
		VisualizationCollection visualizationCollection = new VisualizationCollection();
		if (visualizations != null && visualizations.size() > 0) {
			visualizationCollection.addAll(visualizations);
			return new ResponseEntity<>(visualizationCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(visualizationCollection, HttpStatus.NO_CONTENT);
		}
	}

}
