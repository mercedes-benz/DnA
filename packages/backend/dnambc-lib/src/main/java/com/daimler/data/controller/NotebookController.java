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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.adapter.jupyter.JupyterNotebookAdapter;
import com.daimler.data.adapter.jupyter.JupyterNotebookGenericResponse;
import com.daimler.data.adapter.jupyter.JupyterUserInfoDto;
import com.daimler.data.api.notebook.NotebooksApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.notebook.NotebookResponseVO;
import com.daimler.data.dto.notebook.NotebookVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.solution.TransparencyVO;
import com.daimler.data.service.notebook.NotebookService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Notebook API", tags = { "notebooks" })
@RequestMapping("/api")
@ConditionalOnExpression("${dna.feature.jupyternotebook}")
public class NotebookController implements NotebooksApi {

	@Autowired
	private NotebookService notebookService;

	@Autowired
	private UserStore userStore;
	
	@Value("${jupyternotebook.sleepTime}")
	private String sleepTime;

	@Autowired
	private JupyterNotebookAdapter notebookAdapter;

	// @Autowired
	// private UserInfoService userInfoService;

	private static Logger LOGGER = LoggerFactory.getLogger(NotebookController.class);

	@Override
	@ApiOperation(value = "Get notebooks details.", nickname = "getNotebookDetails", notes = "Get notebooks details.", response = NotebookVO.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = NotebookVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks", method = RequestMethod.GET)
	public ResponseEntity<NotebookVO> getNotebookDetails() {
		String userId = "";
		try {
			userId = getCurrentUser();
			NotebookVO existingNotebook = notebookService.getByUniqueliteral("userId", userId);
			if (existingNotebook != null && existingNotebook.getId() != null) {
				LOGGER.info("Fetched notebook for user {} ", userId);
				return new ResponseEntity<>(existingNotebook, HttpStatus.OK);
			} else {
				LOGGER.info("No notebook records found for given user {} ", userId);
				return new ResponseEntity<>(existingNotebook, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception occurred while fetching notebook for user {} with message {} ", userId, e.getMessage());
			LOGGER.error("Exception stacktrace for fetch user {} notebooks with message: {}",userId, sw.toString());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get number of notebooks.", nickname = "getNumberOfNotebooks", notes = "Get number of notebooks.", response = TransparencyVO.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks/transparency", method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfNotebooks() {
		try {
			TransparencyVO transparencyVO = new TransparencyVO();
			Integer count = notebookService.getTotalNumberOfNotebooks();
			transparencyVO.setCount(count);
			LOGGER.info("Returning notebook count successfully");
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("Failed while fetching notebook count with exception {}", e.getMessage());
			return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Edit notebook details", nickname = "updateNotebookDetails", notes = "Edit notebook details.", response = NotebookResponseVO.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully started.", response = NotebookResponseVO.class),
			@ApiResponse(code = 201, message = "Successfully started.", response = NotebookResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks", method = RequestMethod.PUT)
	public ResponseEntity<NotebookResponseVO> updateNotebookDetails(
			@ApiParam(value = "Request Body that contains data required for updating notebook detail", required = true) @Valid @RequestBody NotebookVO notebookVO) {
		String userId = getCurrentUser();
		GenericMessage resposeMessage = new GenericMessage();
		NotebookResponseVO responseVO = new NotebookResponseVO();
		NotebookVO responseNotebookVO = new NotebookVO();
		try {
			NotebookVO existingNotebook = notebookService.getByUniqueliteral("userId", userId);
			if (existingNotebook != null && existingNotebook.getId() != null) {
				existingNotebook.setDescription(notebookVO.getDescription());
				existingNotebook.setName(notebookVO.getName());
				responseNotebookVO = notebookService.create(existingNotebook);
				responseVO.setData(responseNotebookVO);
				responseVO.setResponseMesage(null);
				LOGGER.info("Notebook updated successfully by user {} ", userId);
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			} else {
				resposeMessage.setSuccess("Failed");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription();
				String msg = "Did not find existing notebook record to update";
				errMsg.setMessage(msg);
				errors.add(errMsg);
				resposeMessage.setErrors(errors);
				responseVO.setData(notebookVO);
				responseVO.setResponseMesage(resposeMessage);
				LOGGER.info("No record found. Failed to updated Notebook for user {} ", userId);
				return new ResponseEntity<>(responseVO, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching and updating notebook {} ", e.getMessage());
			resposeMessage.setSuccess("Failed");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription();
			String msg = "Failed to update notebook";
			errMsg.setMessage(msg);
			errors.add(errMsg);
			resposeMessage.setErrors(errors);
			responseVO.setData(notebookVO);
			responseVO.setResponseMesage(resposeMessage);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception stacktrace for update notebook of user {} is : {}",userId, sw.toString());
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "start existing notebooks using user", nickname = "startNotebook", notes = "start existing notebooks using user", response = com.daimler.data.controller.exceptions.GenericMessage.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully started.", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 201, message = "Successfully started.", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks/server", method = RequestMethod.POST)
	public ResponseEntity<NotebookResponseVO> startNotebook(
			@NotNull @ApiParam(value = "Is user new or already having existing notebook", required = true) @Valid @RequestParam(value = "newUser", required = true) String newUser,
			@ApiParam(value = "Request Body that contains data required for creating a new notebook detail", required = true) @Valid @RequestBody NotebookVO notebookVO) {
		String userId = getCurrentUser();
		try {
			GenericMessage resposeMessage = new GenericMessage();
			NotebookResponseVO responseVO = new NotebookResponseVO();
			NotebookVO responseNotebookVO = new NotebookVO();
			NotebookVO existingNotebook = notebookService.getByUniqueliteral("userId", userId);
			if (existingNotebook == null || existingNotebook.getId() == null) {
				JupyterUserInfoDto user = notebookAdapter.getJupyterUserDetails(userId);
				if (user == null || user.getName() == null) {
					String[] shortIds = { userId };
					List<JupyterUserInfoDto> createdUsers = notebookAdapter.createJupyterUser(shortIds);
					if (createdUsers == null || createdUsers.isEmpty()) {
						resposeMessage.setSuccess("Failed");
						List<MessageDescription> errors = new ArrayList<>();
						MessageDescription errMsg = new MessageDescription();
						String msg = "Failed to create Jupyter user.";
						errMsg.setMessage(msg);
						errors.add(errMsg);
						resposeMessage.setErrors(errors);
						responseVO.setData(null);
						responseVO.setResponseMesage(resposeMessage);
						LOGGER.info("Failed to create Jupyter user for {} ", userId);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				}
				int threadSleepTime = Integer.parseInt(sleepTime);
				LOGGER.info("Putting to sleep for {} after user {} onboarding",threadSleepTime, userId);
				Thread.sleep(threadSleepTime);
				LOGGER.info("Sleep time for new onboarded user {} is done and calling to create notebook record", userId);
				notebookVO.setCreatedOn(new Date());
				notebookVO.setUserId(userId);
				responseNotebookVO = notebookService.create(notebookVO);
				LOGGER.info("Added new notebook record for user {}", userId);
			} else
				responseNotebookVO = existingNotebook;
			JupyterNotebookGenericResponse startResponse = notebookAdapter.startJupyterUserNotebook(userId);
			if (startResponse != null && !"500".equals(startResponse.getStatus())) {
				resposeMessage.setSuccess(startResponse.getMessage());
				responseVO.setData(responseNotebookVO);
				responseVO.setResponseMesage(resposeMessage);
				LOGGER.info("Notebook started successfully for user {}", userId);
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			} else {
				resposeMessage.setSuccess("Failed");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription();
				String msg = "Failed to start.";
				if (startResponse != null)
					msg += " " + startResponse.getMessage();
				errMsg.setMessage(msg);
				errors.add(errMsg);
				resposeMessage.setErrors(errors);
				responseVO.setData(responseNotebookVO);
				responseVO.setResponseMesage(resposeMessage);
				LOGGER.info("Failed to start notebook for user {} ", userId);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception {} occurred while fetching notebook for user {} ", e.getMessage(), userId);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception stacktrace for start notebook for user {} is : {}",userId, sw.toString());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "stops existing notebooks using user", nickname = "stopNotebook", notes = "stop existing notebooks using user", response = com.daimler.data.controller.exceptions.GenericMessage.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully stopped.", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks/server", method = RequestMethod.DELETE)
	public ResponseEntity<com.daimler.data.controller.exceptions.GenericMessage> stopNotebook() {
		String userId = getCurrentUser();
		JupyterNotebookGenericResponse stopResponse = notebookAdapter.stopJupyterUserNotebook(userId);
		GenericMessage resposeMessage = new GenericMessage();
		if (stopResponse != null && !"500".equals(stopResponse.getStatus())) {
			resposeMessage.setSuccess(stopResponse.getMessage());
			LOGGER.info("Notebook stopped for user {} with response {} ", userId, stopResponse.getMessage());
			return new ResponseEntity<>(resposeMessage, HttpStatus.OK);
		} else {
			resposeMessage.setSuccess("Failed");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription();
			String msg = "Failed to stop.";
			if (stopResponse != null)
				msg += " " + stopResponse.getMessage();
			errMsg.setMessage(msg);
			errors.add(errMsg);
			resposeMessage.setErrors(errors);
			LOGGER.info("Failed to stop Notebook for user {} with response {} ", userId, stopResponse.getMessage());
			return new ResponseEntity<>(resposeMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String getCurrentUser() {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		/*
		 * Boolean isAdmin = false; if (userId != null && !"".equalsIgnoreCase(userId))
		 * { UserInfoVO userInfoVO = userInfoService.getById(userId); if (userInfoVO !=
		 * null) { List<UserRoleVO> userRoles = userInfoVO.getRoles();
		 * if(userRoles!=null && !userRoles.isEmpty()) isAdmin =
		 * userRoles.stream().anyMatch(role-> "admin".equalsIgnoreCase(role.getName()));
		 * } }
		 */
		return userId.toLowerCase();
	}

	@Override
	@ApiOperation(value = "Get notebook for a given Id.", nickname = "getById", notes = "Get notebook for a given identifier. This endpoints will be used to get a notebook for a given identifier.", response = NotebookVO.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = NotebookVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks/{id}", method = RequestMethod.GET)
	public ResponseEntity<NotebookVO> getById(
			@ApiParam(value = "Notebook ID to be fetched", required = true) @PathVariable("id") String id) {
		NotebookVO notebookVO = notebookService.getById(id);
		return notebookVO != null ? new ResponseEntity<>(notebookVO, HttpStatus.OK)
				: new ResponseEntity<>(notebookVO, HttpStatus.NO_CONTENT);
	}

}
