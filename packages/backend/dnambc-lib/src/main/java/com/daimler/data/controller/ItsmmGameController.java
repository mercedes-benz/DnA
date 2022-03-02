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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.api.itsmmgame.ItsmmgameApi;
import com.daimler.data.application.itsmm.ItsmmConstants;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.attachment.FileDetailsVO;
import com.daimler.data.dto.attachment.UploadResponseVO;
import com.daimler.data.dto.itsmmgame.*;
import com.daimler.data.service.attachment.AttachmentService;
import com.daimler.data.service.itsmmgame.ItsmmGameEventService;
import com.daimler.data.service.itsmmgame.ItsmmGameUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Itsmm Game API", tags = { "itsmmgame" })
@RequestMapping("/api")
@ConditionalOnExpression("${dna.feature.itsmm}")
public class ItsmmGameController implements ItsmmgameApi {

	private static Logger LOGGER = LoggerFactory.getLogger(ItsmmGameController.class);

	@Autowired
	private AttachmentService attachmentService;

	@Autowired
	ItsmmConstants itsmmConstants;

	@Autowired
	ItsmmGameUserService itsmmUserService;

	@Autowired
	ItsmmGameEventService itsmmEventService;

	private GenericMessage getErrorResponse(String id) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		LOGGER.error("User {} - Game event details not present in db. Notify Admin to maintain game event details.",
				id);
		errMsg = "Game event details not present in db. Notify Admin to maintain game event details.";
		msgDesciption.setMessage(errMsg);
		errors.add(msgDesciption);
		responseMessage.setErrors(errors);
		return responseMessage;
	}

	private ResponseEntity<ItsmmGameUserDetailsResponseVO> getUserErrorResponse(String id) {
		GenericMessage responseMessage = getErrorResponse(id);
		ItsmmGameUserDetailsResponseVO responseVO = new ItsmmGameUserDetailsResponseVO();
		HttpStatus responseStatus = null;
		responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		responseVO.setData(null);
		responseVO.setResponseMessage(responseMessage);
		return new ResponseEntity<>(responseVO, responseStatus);
	}

	private ResponseEntity<ItsmmGameEventDetailsResponseVO> getGameErrorResponse(String id) {
		GenericMessage responseMessage = getErrorResponse(id);
		ItsmmGameEventDetailsResponseVO responseVO = new ItsmmGameEventDetailsResponseVO();
		HttpStatus responseStatus = null;
		responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		responseVO.setData(null);
		responseVO.setResponseMessage(responseMessage);
		return new ResponseEntity<>(responseVO, responseStatus);
	}

	private ResponseEntity<ItsmmGameViewAnswerResponseVO> getViewAnswerErrorResponse(String id) {
		GenericMessage responseMessage = getErrorResponse(id);
		ItsmmGameViewAnswerResponseVO responseVO = new ItsmmGameViewAnswerResponseVO();
		HttpStatus responseStatus = null;
		responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		responseVO.setData(null);
		responseVO.setCorrectAnswer(null);
		responseVO.setResponseMessage(responseMessage);
		return new ResponseEntity<>(responseVO, responseStatus);
	}

	@Override
	@ApiOperation(value = "start user game session.", nickname = "startUserGameSession", notes = "Start ITSMM Game for user.", response = ItsmmGameUserDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = ItsmmGameUserDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/{id}/start", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ItsmmGameUserDetailsResponseVO> startUserGameSession(
			@ApiParam(value = "user shortId", required = true) @PathVariable("id") String id,
			@ApiParam(value = "participantKey to authorize user access", required = true) @RequestHeader(value = "participantKey", required = true) String participantKey) {
		LOGGER.info("User {} - Entered startUserGameSession", id);
		GenericMessage responseMessage = new GenericMessage();
		ItsmmGameUserDetailsResponseVO responseVO = new ItsmmGameUserDetailsResponseVO();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		boolean preConditionFailed = false;
		boolean hasWarning = false;
		String errMsg = "";
		String warningMsg = "";
		String successMsg = "";
		HttpStatus responseStatus = null;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		try {
			LOGGER.info("User {} - Enter itsmmEventService.getById(id)", id);
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
			LOGGER.info("User {} - Exit itsmmEventService.getById(id)", id);
		} catch (Exception e) {
			return getUserErrorResponse(id);
		}
		if (existingEventRecord == null) {
			LOGGER.info("User {} - Existing from startUserGameSession as event record not available", id);
			return getUserErrorResponse(id);
		}
		String systemParticipantKey = existingEventRecord.getParticipantKey();
		String userParticipantKey = participantKey;
		if (systemParticipantKey != null) {
			if (!systemParticipantKey.equals(userParticipantKey)) {
				LOGGER.debug("User {} - Unauthorized. Invalid Participant key.", id);
				errMsg = "Unauthorized. Invalid Participant key.";
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			LOGGER.debug("User {} - Error authorizing participant. Participant key not set in system.", id);
			errMsg = "Error authorizing participant. Participant key not set in system.";
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}
		if (!preConditionFailed && !existingEventRecord.isGameStarted()) {
			LOGGER.debug("User {} - Oops!! Event not started by host. Please wait and try again.", id);
			errMsg = "Oops!! Event not started by host. Please wait and try again.";
			responseStatus = HttpStatus.PRECONDITION_REQUIRED;
			preConditionFailed = true;
		}
		if (!preConditionFailed && existingEventRecord.isGameStopped()) {
			LOGGER.debug("User {} - Oops!! Event ended by host. Better luck next time.", id);
			errMsg = "Oops!! Event ended by host. Better luck next time.";
			responseStatus = HttpStatus.OK;
			preConditionFailed = true;
		}
		try {
			LOGGER.debug("User {} - Passed all user game start preconditions.", id);
			boolean notstarted = false;
			ItsmmGameUserDetailsVO existingUserRecord = itsmmUserService.getByUniqueliteral("shortId", id);
			if (existingUserRecord != null && existingUserRecord.getId() != null) {
				if (preConditionFailed) {
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					Date currDate = Calendar.getInstance().getTime();
					existingUserRecord.setCurrentServerTime(currDate);
					existingUserRecord.setGameStarted(existingEventRecord.isGameStarted());
					existingUserRecord.setGameStartTime(existingEventRecord.getGameStartTime());
					existingUserRecord.setGameStopped(existingEventRecord.isGameStopped());
					existingUserRecord.setGameStopTime(existingEventRecord.getGameStopTime());
					responseVO.setData(existingUserRecord);
					responseVO.setResponseMessage(responseMessage);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
				String userStatus = existingUserRecord.getUserEventStatus();
				if ("STARTED".equals(userStatus)) {
					LOGGER.debug("User {} - Game session started already.", id);
					warningMsg = "Game session started already.";
					hasWarning = true;
				}
				if (!hasWarning && "STOPPED".equals(userStatus)) {
					LOGGER.debug("User {} - Game session stopped already. Cannot restart.", id);
					warningMsg = "Game session stopped already. Cannot restart.";
					hasWarning = true;
				}
				if ("NOTSTARTED".equals(userStatus)) {
					notstarted = true;
				}
				if (hasWarning) {
					msgDesciption.setMessage(warningMsg);
					warnings.add(msgDesciption);
					responseMessage.setWarnings(warnings);
					existingUserRecord.setGameStarted(existingEventRecord.isGameStarted());
					existingUserRecord.setGameStartTime(existingEventRecord.getGameStartTime());
					existingUserRecord.setGameStopped(existingEventRecord.isGameStopped());
					existingUserRecord.setGameStopTime(existingEventRecord.getGameStopTime());
					Date currentDate = Calendar.getInstance().getTime();
					existingUserRecord.setCurrentServerTime(currentDate);
					responseVO.setData(existingUserRecord);
					responseVO.setResponseMessage(responseMessage);
					LOGGER.info("User {} - Sending response with warnings", id);
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}
			}
			if (existingUserRecord == null || existingUserRecord.getId() == null || notstarted) {
				if (preConditionFailed) {
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					Date currDate = Calendar.getInstance().getTime();
					ItsmmGameUserDetailsVO responseData = new ItsmmGameUserDetailsVO();
					responseData.setCurrentServerTime(currDate);
					responseData.setGameStarted(existingEventRecord.isGameStarted());
					responseData.setGameStartTime(existingEventRecord.getGameStartTime());
					responseData.setGameStopped(existingEventRecord.isGameStopped());
					responseData.setGameStopTime(existingEventRecord.getGameStopTime());
					responseData.setShortId(id);
					responseVO.setData(responseData);
					responseVO.setResponseMessage(responseMessage);
					LOGGER.info("User {} - Sending response since game not started", id);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
				ItsmmGameUserDetailsVO newRecord = new ItsmmGameUserDetailsVO();
				if (notstarted) {
					newRecord = existingUserRecord;
				}
				Date startDate = Calendar.getInstance().getTime();
				newRecord.setUserEventStartTime(startDate);
				newRecord.setUserEventStatus(itsmmConstants.GAME_STARTED_STATUS);
				newRecord.setShortId(id);
				ItsmmGameUserDetailsVO UserRecordCreateResponseVO = itsmmUserService.create(newRecord);
				UserRecordCreateResponseVO.setGameStarted(existingEventRecord.isGameStarted());
				UserRecordCreateResponseVO.setGameStartTime(existingEventRecord.getGameStartTime());
				UserRecordCreateResponseVO.setGameStopped(existingEventRecord.isGameStopped());
				UserRecordCreateResponseVO.setGameStopTime(existingEventRecord.getGameStopTime());
				UserRecordCreateResponseVO.setCurrentServerTime(startDate);
				responseMessage.setSuccess("Successfully started game session for user.");
				responseVO.setData(UserRecordCreateResponseVO);
				responseVO.setResponseMessage(responseMessage);
				LOGGER.info("User {} - Successfully started game session for user.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			}
		} catch (Exception e) {
			LOGGER.error("User {} - Exception occurred while fetching and updating usergameinfo {} ", id,
					e.getMessage());
			errMsg = "Exception occurred while fetching and updating usergameinfo. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@Override
	@ApiOperation(value = "submit user answer.", nickname = "submitUserAnswer", notes = "submit user answer.", response = ItsmmGameUserDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = ItsmmGameUserDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/{id}/answer", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ItsmmGameUserDetailsResponseVO> submitUserAnswer(
			@ApiParam(value = "user shortId", required = true) @PathVariable("id") String id,
			@ApiParam(value = "participantKey to authorize user access", required = true) @RequestHeader(value = "participantKey", required = true) String participantKey,
			@ApiParam(value = "Request body to submit the Game answer", required = true) @Valid @RequestBody ItsmmGameUserSubmitAnswerRequestVO itsmmGameSubmitAnswerRequestVO) {
		GenericMessage responseMessage = new GenericMessage();
		ItsmmGameUserDetailsResponseVO responseVO = new ItsmmGameUserDetailsResponseVO();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		boolean preConditionFailed = false;
		boolean hasWarning = false;
		String errMsg = "";
		String warningMsg = "";
		String successMsg = "";
		HttpStatus responseStatus = null;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
		} catch (Exception e) {
			return getUserErrorResponse(id);
		}
		if (existingEventRecord == null) {
			return getUserErrorResponse(id);
		}
		String systemParticipantKey = existingEventRecord.getParticipantKey();
		String userParticipantKey = participantKey;
		if (systemParticipantKey != null) {
			if (!systemParticipantKey.equals(userParticipantKey)) {
				errMsg = "Unauthorized. Invalid Participant key.";
				LOGGER.debug("User {} - Unauthorized. Invalid Participant key.", id);
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			errMsg = "Error authorizing participant. Participant key not set in system.";
			LOGGER.debug("User {} - Error authorizing participant. Participant key not set in system.", id);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}
		if (!preConditionFailed && !existingEventRecord.isGameStarted()) {
			errMsg = "Oops!! Event not started by host. Please wait and try again.";
			LOGGER.debug("User {} - Oops!! Event not started by host. Please wait and try again.", id);
			responseStatus = HttpStatus.PRECONDITION_REQUIRED;
			preConditionFailed = true;
		}
		if (!preConditionFailed && existingEventRecord.isGameStopped()) {
			errMsg = "Oops!! Event ended by host. Better luck next time.";
			LOGGER.debug("User {} - Oops!! Event ended by host. Better luck next time.", id);
			responseStatus = HttpStatus.OK;
			preConditionFailed = true;
		}
		try {
			ItsmmGameUserDetailsVO existingUserRecord = itsmmUserService.getByUniqueliteral("shortId", id);
			boolean notstarted = false;
			if (existingUserRecord != null && existingUserRecord.getId() != null) {
				if (preConditionFailed) {
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					Date currDate = Calendar.getInstance().getTime();
					existingUserRecord.setCurrentServerTime(currDate);
					existingUserRecord.setGameStarted(existingEventRecord.isGameStarted());
					existingUserRecord.setGameStartTime(existingEventRecord.getGameStartTime());
					existingUserRecord.setGameStopped(existingEventRecord.isGameStopped());
					existingUserRecord.setGameStopTime(existingEventRecord.getGameStopTime());
					responseVO.setData(existingUserRecord);
					responseVO.setResponseMessage(responseMessage);
					LOGGER.debug("User {} - Failed in preconditions check.", id);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
				String userStatus = existingUserRecord.getUserEventStatus();
				if ("STOPPED".equals(userStatus)) {
					warningMsg = "Answer already submitted and user game session stopped.";
					LOGGER.debug("User {} - Answer already submitted and user game session stopped.", id);
					msgDesciption.setMessage(warningMsg);
					warnings.add(msgDesciption);
					responseMessage.setWarnings(warnings);
					Date currDate = Calendar.getInstance().getTime();
					existingUserRecord.setCurrentServerTime(currDate);
					existingUserRecord.setGameStarted(existingEventRecord.isGameStarted());
					existingUserRecord.setGameStartTime(existingEventRecord.getGameStartTime());
					existingUserRecord.setGameStopped(existingEventRecord.isGameStopped());
					existingUserRecord.setGameStopTime(existingEventRecord.getGameStopTime());
					responseVO.setData(existingUserRecord);
					responseVO.setResponseMessage(responseMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}
				if ("STARTED".equals(userStatus)) {
					Date currDate = Calendar.getInstance().getTime();
					existingUserRecord.setUserEventEndTime(currDate);
					existingUserRecord.setUserEventStatus(itsmmConstants.GAME_STOPPED_STATUS);
					existingUserRecord.setAnswer(itsmmGameSubmitAnswerRequestVO.getAnswer());
					existingUserRecord.setCodeSnippet(itsmmGameSubmitAnswerRequestVO.getCodeSnippet());
					ItsmmGameUserDetailsVO UserRecordSubmitResponseVO = itsmmUserService.create(existingUserRecord);
					responseMessage.setSuccess("Successfully submitted answer and ended game session for user.");
					UserRecordSubmitResponseVO.setCurrentServerTime(currDate);
					UserRecordSubmitResponseVO.setGameStarted(existingEventRecord.isGameStarted());
					UserRecordSubmitResponseVO.setGameStartTime(existingEventRecord.getGameStartTime());
					UserRecordSubmitResponseVO.setGameStopped(existingEventRecord.isGameStopped());
					UserRecordSubmitResponseVO.setGameStopTime(existingEventRecord.getGameStopTime());
					responseVO.setData(UserRecordSubmitResponseVO);
					responseVO.setResponseMessage(responseMessage);
					LOGGER.debug("User {} - User answer successfully, sending response with data.", id);
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}
				if ("NOTSTARTED".equals(userStatus)) {
					notstarted = true;
					errMsg = "Start game session before submitting answer.";
					LOGGER.debug("User {} - Start game session before submitting answer.", id);
					responseStatus = HttpStatus.PRECONDITION_REQUIRED;
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					Date currDate = Calendar.getInstance().getTime();
					existingUserRecord.setUserEventEndTime(currDate);
					existingUserRecord.setUserEventStatus(itsmmConstants.GAME_STOPPED_STATUS);
					existingUserRecord.setAnswer(itsmmGameSubmitAnswerRequestVO.getAnswer());
					existingUserRecord.setCodeSnippet(itsmmGameSubmitAnswerRequestVO.getCodeSnippet());
					responseVO.setData(existingUserRecord);
					responseVO.setResponseMessage(responseMessage);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
			}
			if (existingUserRecord == null || existingUserRecord.getId() == null) {
				if (preConditionFailed) {
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					Date currDate = Calendar.getInstance().getTime();
					ItsmmGameUserDetailsVO responseData = new ItsmmGameUserDetailsVO();
					responseData.setCurrentServerTime(currDate);
					responseData.setGameStarted(existingEventRecord.isGameStarted());
					responseData.setGameStartTime(existingEventRecord.getGameStartTime());
					responseData.setGameStopped(existingEventRecord.isGameStopped());
					responseData.setGameStopTime(existingEventRecord.getGameStopTime());
					responseData.setShortId(id);
					responseVO.setData(responseData);
					responseVO.setResponseMessage(responseMessage);
					LOGGER.debug("User {} - Failed in preconditions check.", id);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
				errMsg = "Cannot submit answer without starting game session. Please start game";
				LOGGER.debug("User {} - Cannot submit answer without starting game session. Please start game.", id);
				responseStatus = HttpStatus.PRECONDITION_FAILED;
				msgDesciption.setMessage(errMsg);
				errors.add(msgDesciption);
				responseMessage.setErrors(errors);
				Date currDate = Calendar.getInstance().getTime();
				ItsmmGameUserDetailsVO responseData = new ItsmmGameUserDetailsVO();
				if (notstarted) {
					responseData.setId(existingUserRecord.getId());
				}
				responseData.setCurrentServerTime(currDate);
				responseData.setGameStarted(existingEventRecord.isGameStarted());
				responseData.setGameStartTime(existingEventRecord.getGameStartTime());
				responseData.setGameStopped(existingEventRecord.isGameStopped());
				responseData.setGameStopTime(existingEventRecord.getGameStopTime());
				responseData.setShortId(id);
				responseVO.setData(responseData);
				responseVO.setResponseMessage(responseMessage);
				return new ResponseEntity<>(responseVO, responseStatus);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching and updating usergameinfo {} ", e.getMessage());
			errMsg = "Exception occurred while fetching and updating usergameinfo. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@Override
	@ApiOperation(value = "View solution to correctAnswer.", nickname = "viewCorrectAnswer", notes = "view answer.", response = ItsmmGameViewAnswerResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns data containing correct solution and response message of success or failure", response = ItsmmGameViewAnswerResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/{id}/answer", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ItsmmGameViewAnswerResponseVO> viewCorrectAnswer(
			@ApiParam(value = "user shortId", required = true) @PathVariable("id") String id,
			@ApiParam(value = "participantKey to authorize user access", required = true) @RequestHeader(value = "participantKey", required = true) String participantKey) {
		GenericMessage responseMessage = new GenericMessage();
		ItsmmGameViewAnswerResponseVO responseVO = new ItsmmGameViewAnswerResponseVO();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		boolean preConditionFailed = false;
		boolean hasWarning = false;
		String errMsg = "";
		String warningMsg = "";
		String successMsg = "";
		HttpStatus responseStatus = null;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
		} catch (Exception e) {
			return getViewAnswerErrorResponse(id);
		}
		if (existingEventRecord == null) {
			return getViewAnswerErrorResponse(id);
		}
		String systemParticipantKey = existingEventRecord.getParticipantKey();
		String userParticipantKey = participantKey;
		if (systemParticipantKey != null) {
			if (!systemParticipantKey.equals(userParticipantKey)) {
				errMsg = "Unauthorized. Invalid Participant key.";
				LOGGER.info("User {} - Unauthorized. Invalid Participant key.", id);
				responseVO.setCorrectAnswer(null);
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			errMsg = "Error authorizing participant. Participant key not set in system.";
			LOGGER.info("User {} - Error authorizing participant. Participant key not set in system.", id);
			responseVO.setCorrectAnswer(null);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}

		if (!preConditionFailed && !existingEventRecord.isGameStarted()) {
			errMsg = "Oops!! Event not started by host. Please wait and try again.";
			LOGGER.info("User {} - Oops!! Event not started by host. Please wait and try again.", id);
			responseVO.setCorrectAnswer(null);
			responseStatus = HttpStatus.PRECONDITION_REQUIRED;
			preConditionFailed = true;
		}
		if (!preConditionFailed && existingEventRecord.isGameStopped()) {
			errMsg = "Oops!! Event ended by host. Better luck next time.";
			LOGGER.info("User {} - Oops!! Event ended by host. Better luck next time.", id);
			responseVO.setCorrectAnswer(existingEventRecord.getCorrectAnswer());
			responseStatus = HttpStatus.OK;
			preConditionFailed = true;
		}
		try {
			ItsmmGameUserDetailsVO existingUserRecord = itsmmUserService.getByUniqueliteral("shortId", id);

			if (existingUserRecord != null && existingUserRecord.getId() != null) {
				Date currDate = Calendar.getInstance().getTime();
				existingUserRecord.setCurrentServerTime(currDate);
				existingUserRecord.setGameStarted(existingEventRecord.isGameStarted());
				existingUserRecord.setGameStartTime(existingEventRecord.getGameStartTime());
				existingUserRecord.setGameStopped(existingEventRecord.isGameStopped());
				existingUserRecord.setGameStopTime(existingEventRecord.getGameStopTime());
				if (preConditionFailed) {
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					responseVO.correctAnswer(null);
					responseVO.setResponseMessage(responseMessage);
					responseVO.setData(existingUserRecord);
					LOGGER.info("User {} - Failed in preconditions check, returning", id);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
				String userStatus = existingUserRecord.getUserEventStatus();
				if ("NOTSTARTED".equals(userStatus)) {
					warningMsg = "User game session not started, Please start game session before seeing the solution.";
					msgDesciption.setMessage(warningMsg);
					warnings.add(msgDesciption);
					responseMessage.setWarnings(warnings);
					responseVO.setCorrectAnswer(null);
					responseVO.setResponseMessage(responseMessage);
					responseVO.setData(existingUserRecord);
					LOGGER.info(
							"User {} - User game session not started, Please start game session before seeing the solution",
							id);
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}
				if ("STOPPED".equals(userStatus)) {
					warningMsg = "Answer already submitted and user game session stopped.";
					msgDesciption.setMessage(warningMsg);
					warnings.add(msgDesciption);
					responseMessage.setWarnings(warnings);
					responseVO.setCorrectAnswer(existingEventRecord.getCorrectAnswer());
					responseVO.setResponseMessage(responseMessage);
					responseVO.setData(existingUserRecord);
					LOGGER.info("User {} - Answer already submitted and user game session stopped.", id);
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}
				if ("STARTED".equals(userStatus)) {
					existingUserRecord.setSolutionSeen(true);
					ItsmmGameUserDetailsVO UserSeenAnswerResponseVO = itsmmUserService.create(existingUserRecord);
					UserSeenAnswerResponseVO.setCurrentServerTime(currDate);
					UserSeenAnswerResponseVO.setGameStarted(existingEventRecord.isGameStarted());
					UserSeenAnswerResponseVO.setGameStartTime(existingEventRecord.getGameStartTime());
					UserSeenAnswerResponseVO.setGameStopped(existingEventRecord.isGameStopped());
					UserSeenAnswerResponseVO.setGameStopTime(existingEventRecord.getGameStopTime());
					responseMessage.setSuccess("Solution seen event is logged. All the best solving.");
					responseVO.setCorrectAnswer(existingEventRecord.getCorrectAnswer());
					responseVO.setResponseMessage(responseMessage);
					responseVO.setData(UserSeenAnswerResponseVO);
					LOGGER.info("User {} - Solution seen event is logged, returing.", id);
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}
			} else {
				Date currDate = Calendar.getInstance().getTime();
				ItsmmGameUserDetailsVO newRecord = new ItsmmGameUserDetailsVO();
				newRecord.setUserEventStatus("NOTSTARTED");
				newRecord.setShortId(id);
				ItsmmGameUserDetailsVO responseData = itsmmUserService.create(newRecord);
				responseData.setCurrentServerTime(currDate);
				responseData.setGameStarted(existingEventRecord.isGameStarted());
				responseData.setGameStartTime(existingEventRecord.getGameStartTime());
				responseData.setGameStopped(existingEventRecord.isGameStopped());
				responseData.setGameStopTime(existingEventRecord.getGameStopTime());
				if (preConditionFailed) {
					msgDesciption.setMessage(errMsg);
					errors.add(msgDesciption);
					responseMessage.setErrors(errors);
					responseVO.setResponseMessage(responseMessage);
					responseVO.setData(responseData);
					responseVO.setCorrectAnswer(null);
					LOGGER.info("User {} - Failed in preconditions check, returning", id);
					return new ResponseEntity<>(responseVO, responseStatus);
				}
				errMsg = "Cannot see answer without starting user game session. Please start game";
				responseStatus = HttpStatus.PRECONDITION_FAILED;
				msgDesciption.setMessage(errMsg);
				errors.add(msgDesciption);
				responseMessage.setErrors(errors);
				responseVO.setData(responseData);
				responseVO.setCorrectAnswer(null);
				responseVO.setResponseMessage(responseMessage);
				LOGGER.info("User {} - Cannot see answer without starting user game session. Please start game.", id);
				return new ResponseEntity<>(responseVO, responseStatus);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching and updating usergameinfo {} ", e.getMessage());
			errMsg = "Exception occurred while fetching and updating usergameinfo. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@Override
	@ApiOperation(value = "Get user game details.", nickname = "getUserGameDetails", notes = "Get all solutions. This endpoints will be used to get all valid available solution records.", response = ItsmmGameUserDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns data containing user game details and response message of success or failure", response = ItsmmGameUserDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/{id}/info", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ItsmmGameUserDetailsResponseVO> getUserGameDetails(
			@ApiParam(value = "user shortId", required = true) @PathVariable("id") String id,
			@ApiParam(value = "participantKey to authorize user access", required = true) @RequestHeader(value = "participantKey", required = true) String participantKey) {
		GenericMessage responseMessage = new GenericMessage();
		ItsmmGameUserDetailsResponseVO responseVO = new ItsmmGameUserDetailsResponseVO();
		ItsmmGameUserDetailsVO userDetails = new ItsmmGameUserDetailsVO();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		boolean preConditionFailed = false;
		boolean hasWarning = false;
		String errMsg = "";
		String warningMsg = "";
		String successMsg = "";
		HttpStatus responseStatus = null;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
		} catch (Exception e) {
			return getUserErrorResponse(id);
		}
		if (existingEventRecord == null) {
			return getUserErrorResponse(id);
		}
		String systemParticipantKey = existingEventRecord.getParticipantKey();
		String userParticipantKey = participantKey;
		if (systemParticipantKey != null) {
			if (!systemParticipantKey.equals(userParticipantKey)) {
				errMsg = "Unauthorized. Invalid Participant key.";
				LOGGER.debug("User {} - Unauthorized. Invalid Participant key.", id);
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			errMsg = "Error authorizing participant. Participant key not set in system.";
			LOGGER.debug("User {} - Error authorizing participant. Participant key not set in system.", id);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}
		if (preConditionFailed) {
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			Date currDate = Calendar.getInstance().getTime();
			ItsmmGameUserDetailsVO responseData = new ItsmmGameUserDetailsVO();
			responseData.setCurrentServerTime(currDate);
			responseData.setGameStarted(existingEventRecord.isGameStarted());
			responseData.setGameStartTime(existingEventRecord.getGameStartTime());
			responseData.setGameStopped(existingEventRecord.isGameStopped());
			responseData.setGameStopTime(existingEventRecord.getGameStopTime());
			responseData.setShortId(id);
			responseVO.setData(responseData);
			responseVO.setResponseMessage(responseMessage);
			LOGGER.debug("User {} - Precondition checks failed, sending response.", id);
			return new ResponseEntity<>(responseVO, responseStatus);
		}
		try {
			ItsmmGameUserDetailsVO existingUserRecord = itsmmUserService.getByUniqueliteral("shortId", id);
			if (existingUserRecord != null && existingUserRecord.getId() != null) {
				userDetails = existingUserRecord;
				Date currDate = Calendar.getInstance().getTime();
				userDetails.setCurrentServerTime(currDate);
				userDetails.setGameStarted(existingEventRecord.isGameStarted());
				userDetails.setGameStartTime(existingEventRecord.getGameStartTime());
				userDetails.setGameStopped(existingEventRecord.isGameStopped());
				userDetails.setGameStopTime(existingEventRecord.getGameStopTime());
				responseVO.setData(userDetails);
				responseVO.setResponseMessage(null);
			} else {
				ItsmmGameUserDetailsVO newUserRecord = new ItsmmGameUserDetailsVO();
				newUserRecord.setUserEventStatus(itsmmConstants.USER_GAME_NOSTARTED_STATUS);
				newUserRecord.setShortId(id);
				ItsmmGameUserDetailsVO newUserRecordResponse = itsmmUserService.create(newUserRecord);
				Date currDate = Calendar.getInstance().getTime();
				newUserRecordResponse.setCurrentServerTime(currDate);
				newUserRecordResponse.setGameStarted(existingEventRecord.isGameStarted());
				newUserRecordResponse.setGameStartTime(existingEventRecord.getGameStartTime());
				newUserRecordResponse.setGameStopped(existingEventRecord.isGameStopped());
				newUserRecordResponse.setGameStopTime(existingEventRecord.getGameStopTime());
				responseVO.setData(newUserRecordResponse);
				responseVO.setResponseMessage(null);
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching and updating usergameinfo {} ", e.getMessage());
			errMsg = "Exception occurred while fetching and updating usergameinfo. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "View game event info.", nickname = "getGameEventInfo", notes = "view game info.", response = ItsmmGameEventDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns data containing game info and response message of success or failure", response = ItsmmGameEventDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ItsmmGameEventDetailsResponseVO> getGameEventInfo(
			@ApiParam(value = "hostKey to authorize user access", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey) {

		ItsmmGameEventDetailsResponseVO responseVO = new ItsmmGameEventDetailsResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		boolean preConditionFailed = false;
		HttpStatus responseStatus = null;
		String userSentHostKey = hostKey;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
		} catch (Exception e) {
			return getGameErrorResponse(userSentHostKey);
		}
		if (existingEventRecord == null) {
			return getGameErrorResponse(userSentHostKey);
		}
		String systemHostKey = existingEventRecord.getHostKey();
		if (systemHostKey != null) {
			if (!systemHostKey.equals(userSentHostKey)) {
				errMsg = "Unauthorized. Invalid host key.";
				LOGGER.debug("Unauthorized. Invalid host key.");
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			errMsg = "Error authorizing host. Host key not set in system.";
			LOGGER.debug("Error authorizing host. Host key not set in system.");
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}
		if (preConditionFailed) {
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			LOGGER.debug("Returning with warnings");
			return new ResponseEntity<>(responseVO, responseStatus);
		}
		Date currDate = Calendar.getInstance().getTime();
		existingEventRecord.setCurrentServerTime(currDate);
		responseVO.setData(existingEventRecord);
		responseVO.setResponseMessage(null);
		LOGGER.debug("Returning success with data");
		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "start main game event.", nickname = "startGameEvent", notes = "Start ITSMM Game event.", response = ItsmmGameEventDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = ItsmmGameEventDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ItsmmGameEventDetailsResponseVO> startGameEvent(
			@ApiParam(value = "hostKey to authorize host access of starting game", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey) {
		ItsmmGameEventDetailsResponseVO responseVO = new ItsmmGameEventDetailsResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		boolean preConditionFailed = false;
		HttpStatus responseStatus = null;
		String userSentHostKey = hostKey;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		ItsmmGameEventDetailsVO returnEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
			if (existingEventRecord == null) {
				return getGameErrorResponse(userSentHostKey);
			}
		} catch (Exception e) {
			return getGameErrorResponse(userSentHostKey);
		}
		String systemHostKey = existingEventRecord.getHostKey();
		if (systemHostKey != null) {
			if (!systemHostKey.equals(userSentHostKey)) {
				errMsg = "Unauthorized. Invalid host key.";
				LOGGER.debug("Unauthorized. Invalid host key.");
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			errMsg = "Error authorizing host. Host key not set in system.";
			LOGGER.debug("Error authorizing host. Host key not set in system.");
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}

		if (existingEventRecord.isGameStarted()) {
			errMsg = "Event started already.";
			LOGGER.debug("Event started already.");
			responseStatus = HttpStatus.OK;
			preConditionFailed = true;
		}
		if (existingEventRecord.isGameStopped()) {
			errMsg = "Event already stopped.";
			LOGGER.debug("Event already stopped.");
			responseStatus = HttpStatus.OK;
			preConditionFailed = true;
		}
		if (preConditionFailed) {
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			Date currentDate = Calendar.getInstance().getTime();
			existingEventRecord.setCurrentServerTime(currentDate);
			responseVO.setData(existingEventRecord);
			responseVO.setResponseMessage(responseMessage);
			LOGGER.debug("Returning with warnings.");
			return new ResponseEntity<>(responseVO, responseStatus);
		}
		try {
			existingEventRecord.setGameStarted(true);
			Date startDate = Calendar.getInstance().getTime();
			existingEventRecord.setGameStartTime(startDate);
			returnEventRecord = itsmmEventService.create(existingEventRecord);
			returnEventRecord.setCurrentServerTime(startDate);
			responseVO.setData(returnEventRecord);
			responseVO.setResponseMessage(null);
			LOGGER.debug("Returning success with data.");
			return new ResponseEntity<>(responseVO, HttpStatus.OK);

		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching and updating gameEventinfo {} ", e.getMessage());
			errMsg = "Exception occurred while fetching and updating gameEventinfo. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "update main game event.", nickname = "updateGameEvent", notes = "update ITSMM Game event.", response = ItsmmGameEventDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = ItsmmGameEventDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<ItsmmGameEventDetailsResponseVO> updateGameEvent(
			@ApiParam(value = "hostKey to authorize host access of starting game", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey,
			@ApiParam(value = "Request body to update the Game", required = true) @Valid @RequestBody ItsmmGameEventDetailsVO itsmmGameInfoUpdateRequestVO) {
		ItsmmGameEventDetailsResponseVO responseVO = new ItsmmGameEventDetailsResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		String warningMsg = "";
		String successMsg = "";
		boolean preConditionFailed = false;
		boolean hasWarning = false;
		HttpStatus responseStatus = null;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		ItsmmGameEventDetailsVO returnEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
		} catch (Exception e) {
			return getGameErrorResponse(null);
		}
		if (existingEventRecord == null) {
			return getGameErrorResponse(null);
		} else {
			try {
				if (itsmmGameInfoUpdateRequestVO.getCorrectAnswer() != null)
					existingEventRecord.setCorrectAnswer(itsmmGameInfoUpdateRequestVO.getCorrectAnswer());
				existingEventRecord.setGameStarted(itsmmGameInfoUpdateRequestVO.isGameStarted());
				existingEventRecord.setGameStartTime(itsmmGameInfoUpdateRequestVO.getGameStartTime());
				existingEventRecord.setGameStopped(itsmmGameInfoUpdateRequestVO.isGameStopped());
				existingEventRecord.setGameStopTime(itsmmGameInfoUpdateRequestVO.getGameStopTime());
				// existingEventRecord.setHostKey(itsmmGameInfoUpdateRequestVO.getHostKey());
				// existingEventRecord.setParticipantKey(itsmmGameInfoUpdateRequestVO.getParticipantKey());
				returnEventRecord = itsmmEventService.create(existingEventRecord);
				Date currDate = Calendar.getInstance().getTime();
				returnEventRecord.setCurrentServerTime(currDate);
				responseVO.setData(returnEventRecord);
				responseVO.setResponseMessage(null);
				LOGGER.debug("Returning success with data.");
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			} catch (Exception e) {
				LOGGER.error("Exception occurred while fetching and updating gameEventinfo {} ", e.getMessage());
				errMsg = "Exception occurred while fetching and updating gameEventinfo. " + e.getMessage();
				msgDesciption.setMessage(errMsg);
				errors.add(msgDesciption);
				responseMessage.setErrors(errors);
				responseVO.setData(null);
				responseVO.setResponseMessage(responseMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@Override
	@ApiOperation(value = "stop game event", nickname = "stopGameEvent", notes = "stop game event", response = ItsmmGameEventDetailsResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns data containing  game info and response message of success or failure", response = ItsmmGameEventDetailsResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<ItsmmGameEventDetailsResponseVO> stopGameEvent(
			@ApiParam(value = "hostKey to authorize user access", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey) {
		ItsmmGameEventDetailsResponseVO responseVO = new ItsmmGameEventDetailsResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("Failed");
		List<MessageDescription> errors = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		boolean preConditionFailed = false;
		HttpStatus responseStatus = null;
		String userSentHostKey = hostKey;
		ItsmmGameEventDetailsVO existingEventRecord = null;
		ItsmmGameEventDetailsVO returnEventRecord = null;
		try {
			existingEventRecord = itsmmEventService.getById("itsmmGame001");
			if (existingEventRecord == null) {
				return getGameErrorResponse(userSentHostKey);
			}
		} catch (Exception e) {
			return getGameErrorResponse(userSentHostKey);
		}
		String systemHostKey = existingEventRecord.getHostKey();
		if (systemHostKey != null) {
			if (!systemHostKey.equals(userSentHostKey)) {
				errMsg = "Unauthorized. Invalid host key.";
				LOGGER.debug("Unauthorized. Invalid host key.");
				responseStatus = HttpStatus.UNAUTHORIZED;
				preConditionFailed = true;
			}
		} else {
			errMsg = "Error authorizing host. Host key not set in system.";
			LOGGER.debug("Error authorizing host. Host key not set in system.");
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			preConditionFailed = true;
		}
		if (!existingEventRecord.isGameStarted()) {
			errMsg = "Cannot stop game event without starting. Event not started.";
			LOGGER.debug("Cannot stop game event without starting. Event not started.");
			responseStatus = HttpStatus.PRECONDITION_FAILED;
			preConditionFailed = true;
		}
		if (existingEventRecord.isGameStopped()) {
			errMsg = "Event stopped already.";
			LOGGER.debug("Event stopped already.");
			responseStatus = HttpStatus.OK;
			preConditionFailed = true;
		}
		if (preConditionFailed) {
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			Date currentDate = Calendar.getInstance().getTime();
			existingEventRecord.setCurrentServerTime(currentDate);
			responseVO.setData(existingEventRecord);
			responseVO.setResponseMessage(responseMessage);
			LOGGER.debug("Returning after precondition failing");
			return new ResponseEntity<>(responseVO, responseStatus);
		}
		try {
			existingEventRecord.setGameStopped(true);
			Date stopDate = Calendar.getInstance().getTime();
			existingEventRecord.setGameStopTime(stopDate);
			returnEventRecord = itsmmEventService.create(existingEventRecord);
			returnEventRecord.setCurrentServerTime(stopDate);
			responseVO.setData(returnEventRecord);
			responseVO.setResponseMessage(null);
			LOGGER.debug("Returning success with data.");
			return new ResponseEntity<>(responseVO, HttpStatus.OK);

		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching and updating gameEventinfo {} ", e.getMessage());
			errMsg = "Exception occurred while fetching and updating gameEventinfo. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			responseMessage.setErrors(errors);
			responseVO.setData(null);
			responseVO.setResponseMessage(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get game results", nickname = "getGameResults", notes = "get game results", response = ItsmmGameUserResultsWraperVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns data containing game results", response = ItsmmGameUserResultsWraperVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/results", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ItsmmGameUserResultsWraperVO> getGameResults(
			@ApiParam(value = "hostKey to authorize user access", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey) {
		ItsmmGameUserResultsWraperVO vo = new ItsmmGameUserResultsWraperVO();
		GenericMessage genericMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		try {
			ItsmmGameEventDetailsVO existingEventRecord = itsmmEventService.getById("itsmmGame001");
			if (existingEventRecord != null) {
				String answer = existingEventRecord.getCorrectAnswer();
				vo = itsmmUserService.getResults(answer);
			}
			vo.responseMessage(null);
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch records {} ", e.getMessage());
			
			errMsg = "Exception occurred while deleting user game records. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			genericMessage.setErrors(errors);
			vo.setData(null);
			vo.setResponseMessage(genericMessage);
			return new ResponseEntity<>(vo, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all user records", nickname = "getGameUserRecords", notes = "get all user records", response = ItsmmGameUserRecordsVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns data containing game user records", response = ItsmmGameUserRecordsVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/records", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ItsmmGameUserRecordsVO> getGameUserRecords(
			@ApiParam(value = "hostKey to authorize user access", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey) {
		ItsmmGameUserRecordsVO vo = itsmmUserService.getRecords();
		return new ResponseEntity<>(vo, HttpStatus.OK);
	}

	@ApiOperation(value = "delete game records", nickname = "deleteRecords", notes = "delete game records", response = GenericMessage.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns after records are cleared", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/itsmmgame/event/records", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteRecords(
			@ApiParam(value = "hostKey to authorize user access", required = true) @RequestHeader(value = "hostKey", required = true) String hostKey) {
		GenericMessage genericMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		MessageDescription msgDesciption = new MessageDescription();
		String errMsg = "";
		HttpStatus responseStatus = null;
		try {
			itsmmUserService.deleteAll();
			genericMessage.setSuccess("Successfully deleted all user game records");
			responseStatus = HttpStatus.OK;
		} catch (Exception e) {
			LOGGER.error("Failed to delete user game records. {} ", e.getMessage());
			errMsg = "Exception occurred while deleting user game records. " + e.getMessage();
			msgDesciption.setMessage(errMsg);
			errors.add(msgDesciption);
			genericMessage.setErrors(errors);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(genericMessage, responseStatus);
	}

	@ApiOperation(value = "Uploads a new file.", nickname = "uploadTaskData", notes = "Uploads file.", response = UploadResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = UploadResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = UploadResponseVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/task-data", produces = { "application/json" }, consumes = {
			"multipart/form-data" }, method = RequestMethod.POST)
	public ResponseEntity<UploadResponseVO> uploadTaskData(
			@ApiParam(value = "Request Body that contains data or file contents", required = true) @Valid @RequestBody MultipartFile file) {
		UploadResponseVO responseVO = new UploadResponseVO();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			FileDetailsVO fileDetails = this.attachmentService.uploadFileToS3Bucket(file, "taskData");
			responseVO.setFileDetails(fileDetails);
		} catch (Exception e) {
			LOGGER.error("Exception occurred uploading taskData to s3 cloud", e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			errors.add(exceptionMsg);
			responseVO.setErrors(errors);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOGGER.info("Returning from uploading taskData to s3 cloud");
		return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Downloads the file", nickname = "getTaskData", notes = "Downloads the file for the given keyName", response = ByteArrayResource.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully downloaded.", response = ByteArrayResource.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/task-data", method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> getTaskData() {
		try {
			String keyName = "taskData";
			String fileName = "taskData.csv";
			byte[] content = this.attachmentService.getFile(keyName);
			ByteArrayResource resource = new ByteArrayResource(content);
			LOGGER.info("Returning from downloading taskData from s3 cloud");
			return ResponseEntity.ok().contentLength(content.length).contentType(contentType(fileName))
					.header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
		} catch (Exception e) {
			LOGGER.error("Exception occurred getting TaskData from s3 cloud", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	@ApiOperation(value = "Uploads a new file.", nickname = "uploadWarmUpData", notes = "Uploads file.", response = UploadResponseVO.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = UploadResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = UploadResponseVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/warmup-data", produces = { "application/json" }, consumes = {
			"multipart/form-data" }, method = RequestMethod.POST)
	public ResponseEntity<UploadResponseVO> uploadWarmUpData(
			@ApiParam(value = "Request Body that contains data or file contents", required = true) @Valid @RequestBody MultipartFile file) {
		UploadResponseVO responseVO = new UploadResponseVO();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			FileDetailsVO fileDetails = this.attachmentService.uploadFileToS3Bucket(file, "warmUpData");
			responseVO.setFileDetails(fileDetails);
		} catch (Exception e) {
			LOGGER.error("Exception occurred uploading WarmUpData to s3 cloud", e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			errors.add(exceptionMsg);
			responseVO.setErrors(errors);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOGGER.info("Returning from uploading WarmUpData to s3 cloud");
		return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Downloads the file", nickname = "getWarmUpData", notes = "Downloads the file for the given keyName", response = ByteArrayResource.class, tags = {
			"itsmmgame", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully downloaded.", response = ByteArrayResource.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/warmup-data", method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> getWarmUpData() {
		try {
			String keyName = "warmUpData";
			String fileName = "warmUpData.csv";
			byte[] content = this.attachmentService.getFile(keyName);
			ByteArrayResource resource = new ByteArrayResource(content);
			LOGGER.info("Returning from downloading WarmUpData from s3 cloud");
			return ResponseEntity.ok().contentLength(content.length).contentType(contentType(fileName))
					.header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
		} catch (Exception e) {
			LOGGER.error("Exception occurred getting WarmUpData from s3 cloud", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	private MediaType contentType(String fileName) {
		String[] arr = fileName.split("\\.");
		String type = arr[arr.length - 1];
		switch (type) {
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "png":
			return MediaType.IMAGE_PNG;
		case "jpg":
			return MediaType.IMAGE_JPEG;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

}
