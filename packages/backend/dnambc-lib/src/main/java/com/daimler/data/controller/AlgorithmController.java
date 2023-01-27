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

import com.daimler.data.api.algorithm.AlgorithmsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.algorithm.AlgorithmCollection;
import com.daimler.data.dto.algorithm.AlgorithmRequestVO;
import com.daimler.data.dto.algorithm.AlgorithmVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.algorithm.AlgorithmService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Algorithm API", tags = { "algorithms" })
@RequestMapping("/api")
@Slf4j
public class AlgorithmController implements AlgorithmsApi {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private AlgorithmService algorithmService;

	@Override
	@ApiOperation(value = "Adds a new algorithms.", nickname = "createAlgorithm", notes = "Adds a new non existing algorithms which is used in providing solution.", response = AlgorithmVO.class, tags = {
			"algorithms", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Algorithm added successfully", response = AlgorithmVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/algorithms", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<AlgorithmVO> createAlgorithm(@Valid AlgorithmRequestVO algorithmRequestVO) {

		AlgorithmVO requestAlgorithmVO = algorithmRequestVO.getData();
		try {
			AlgorithmVO existingAlgorithmVO = algorithmService.getByUniqueliteral("name", requestAlgorithmVO.getName());
			if (existingAlgorithmVO != null && existingAlgorithmVO.getName() != null)
				return new ResponseEntity<>(existingAlgorithmVO, HttpStatus.CONFLICT);
			requestAlgorithmVO.setId(null);
			AlgorithmVO algorithmVo = algorithmService.create(requestAlgorithmVO);
			if (algorithmVo != null && algorithmVo.getId() != null) {
				log.debug("Algorithm {} created successfully", requestAlgorithmVO.getName());
				return new ResponseEntity<>(algorithmVo, HttpStatus.CREATED);
			} else {
				log.debug("Algorithm {} creation failed with unknown error ", requestAlgorithmVO.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Failed to create algorithm {} with exception {} ", requestAlgorithmVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the algorithm identified by given ID.", nickname = "delete", notes = "Deletes the algorithm identified by given ID", response = GenericMessage.class, tags = {
			"algorithms", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/algorithms/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the algorithm", required = true) @PathVariable("id") String id) {
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
									"Not authorized to delete Algorithms. User does not have admin privileges.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							log.debug("User {} cannot delete algorithms, insufficient privileges", userId);
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			AlgorithmVO algorithm = algorithmService.getById(id);
			String algoName = algorithm != null ? algorithm.getName() : "";
			String userName = algorithmService.currentUserName(currentUser);
			String eventMessage = "Algorithm " + algoName + " has been deleted by Admin " + userName;
			algorithmService.deleteAlgorithm(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			log.debug("Algorithm {} deleted successfully", id);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No tag with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.error("No algorithm found with id {}, failed to delete", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to delete algorithm {}, with exception {}", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available algorithms.", nickname = "getAll", notes = "Get all algorithms. This endpoints will be used to Get all valid available algorithms maintenance records.", response = AlgorithmCollection.class, tags = {
			"algorithms", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all algorithms", response = AlgorithmCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/algorithms", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<AlgorithmCollection> getAll(
			@ApiParam(value = "Sort algorithms based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		final List<AlgorithmVO> algorithms = algorithmService.getAll();		
		AlgorithmCollection algorithmCollection = new AlgorithmCollection();
		log.debug("Sending all algorithms");
		if (algorithms != null && algorithms.size() > 0) {
			if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				algorithms.sort(Comparator.comparing(AlgorithmVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				algorithms.sort(Comparator.comparing(AlgorithmVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			algorithmCollection.addAll(algorithms);
			return new ResponseEntity<>(algorithmCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(algorithmCollection, HttpStatus.NO_CONTENT);
		}
	}

}
