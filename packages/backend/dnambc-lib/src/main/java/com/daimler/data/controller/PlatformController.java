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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.platform.PlatformsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.platform.PlatformCollection;
import com.daimler.data.dto.platform.PlatformRequestVO;
import com.daimler.data.dto.platform.PlatformVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.platform.PlatformService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Platform API", tags = { "platforms" })
@RequestMapping("/api")
@Slf4j
public class PlatformController implements PlatformsApi {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private PlatformService platformService;

	@Override
	@ApiOperation(value = "Adds a new platforms.", nickname = "create", notes = "Adds a new non existing platforms which is used in providing solution.", response = PlatformVO.class, tags = {
			"platforms", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Platform added successfully", response = PlatformVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/platforms", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<PlatformVO> create(@Valid PlatformRequestVO platformRequestVO) {
		PlatformVO requestplatformVO = platformRequestVO.getData();
		try {
			PlatformVO existingplatformVO = platformService.getByUniqueliteral("name", requestplatformVO.getName());
			if (existingplatformVO != null && existingplatformVO.getName() != null) {
				log.info("Platform with name {} already exists, cannot create, returning with Conflict.",
						requestplatformVO.getName());
				return new ResponseEntity<>(existingplatformVO, HttpStatus.CONFLICT);
			}
			requestplatformVO.setId(null);
			PlatformVO platformVO = platformService.create(requestplatformVO);
			if (platformVO != null && platformVO.getId() != null) {
				log.info("Successfully created platform {}", requestplatformVO.getName());
				return new ResponseEntity<>(platformVO, HttpStatus.CREATED);
			} else
				log.info("Failed to create platform {} with unknown error", requestplatformVO.getName());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Exception {} occured while creating platform {} ", e.getLocalizedMessage(),
					requestplatformVO.getName());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the platform identified by given ID.", nickname = "delete", notes = "Deletes the platform identified by given ID", response = GenericMessage.class, tags = {
			"platforms", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/platforms/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the platform", required = true) @PathVariable("id") String id) {
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
									"Not authorized to delete Platforms. User does not have admin privileges.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							log.debug("User not authorized to delete platforms");
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			PlatformVO platform = platformService.getById(id);
			String platformName = platform != null ? platform.getName() : "";
			String userName = platformService.currentUserName(currentUser);
			String eventMessage = "Platform  " + platformName + " has been deleted by Admin " + userName;
			platformService.deletePlatform(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			log.info("Platform {} deleted successfully", id);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);

		} catch (EntityNotFoundException e) {
			log.error("No platform with id {} found, couldnt delete", id);
			MessageDescription invalidMsg = new MessageDescription("No tag with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception {} occured while deleting platform {} ", e.getLocalizedMessage(), id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available platforms.", nickname = "getAll", notes = "Get all platforms. This endpoints will be used to Get all valid available platforms maintenance records.", response = PlatformCollection.class, tags = {
			"platforms", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all platforms", response = PlatformCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/platforms", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<PlatformCollection> getAll(
			@ApiParam(value = "Sort platforms based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		final List<PlatformVO> platforms = platformService.getAll();		
		PlatformCollection platformCollection = new PlatformCollection();
		if (platforms != null && platforms.size() > 0) {
			if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				platforms.sort(Comparator.comparing(PlatformVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				platforms.sort(Comparator.comparing(PlatformVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			platformCollection.addAll(platforms);
			log.debug("Returning all available platforms");
			return new ResponseEntity<>(platformCollection, HttpStatus.OK);
		} else {
			log.debug("No platforms found, returning empty");
			return new ResponseEntity<>(platformCollection, HttpStatus.NO_CONTENT);
		}
	}

}
