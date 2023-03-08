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

package com.daimler.dna.notifications.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.dto.userinfo.UsersCollection;
import com.daimler.dna.notifications.api.NotificationsApi;
import com.daimler.dna.notifications.common.dna.client.DnaNotificationPreferenceClient;
import com.daimler.dna.notifications.common.event.config.GenericEventRecord;
import com.daimler.dna.notifications.common.user.UserStore;
import com.daimler.dna.notifications.controller.exceptions.GenericMessage;
import com.daimler.dna.notifications.controller.exceptions.MessageDescription;
import com.daimler.dna.notifications.core.service.NotificationsService;
import com.daimler.dna.notifications.dto.CreatedByVO;
import com.daimler.dna.notifications.dto.MarkedSelection;
//import com.daimler.dna.notifications.dto.EventCategoriesCollectionVO;
import com.daimler.dna.notifications.dto.NotificationCollectionVO;
import com.daimler.dna.notifications.dto.NotificationRequestVO;
import com.daimler.dna.notifications.dto.NotificationRequestVOData;
import com.daimler.dna.notifications.dto.NotificationRequestVOData.ServiceUsersTypeEnum;
import com.daimler.dna.notifications.dto.NotificationVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Notifications API", tags = { "notifications" })
@RequestMapping("/api")
@SuppressWarnings(value = "unused")
public class NotificationsController implements NotificationsApi {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationsController.class);

	@Autowired
	private NotificationsService notificationService;
	
	@Autowired
	private DnaNotificationPreferenceClient dnaClient;
	
	@Autowired
	private UserStore userStore;

	@ApiOperation(value = "Get all notifications with filters.", nickname = "getAll", notes = "Get all notifications based on filter values", response = NotificationCollectionVO.class, tags = {
			"notifications", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = NotificationCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<NotificationCollectionVO> getAll(
			@NotNull @ApiParam(value = "shortId of the user", required = true) @Valid @RequestParam(value = "userId", required = true) String userId,
			@ApiParam(value = "category of event, example- SYSTEM, SOLUTION_UPDATE etc") @Valid @RequestParam(value = "eventCategory", required = false) String eventCategory,
			@ApiParam(value = "Read or Unread filter", allowableValues = "read, unread") @Valid @RequestParam(value = "readType", required = false) String readType,
			@ApiParam(value = "searchTerm to filter notifications. Example \"Solution abc\" or \"Notebook provisioning\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "page number from which listing of notifications should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of notifications. Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		LOG.debug(
				"Get messages requested by user {} for eventCategory {} and readType {} "
						+ "with searchTerm {} and offset {} limit {}",
				userId, eventCategory, readType, searchTerm, offset, limit);
		try {
			if (offset == null)
				offset = 0;
			if (limit == null)
				limit = 0;
			CreatedByVO currentUser = this.userStore.getVO();
			String currentUserId = currentUser != null ? currentUser.getId() : "";
			NotificationCollectionVO notAuthorizedCollection = new NotificationCollectionVO();						
			if (!currentUserId.equalsIgnoreCase(userId)) {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to view all notifications. Only current user's notifications can be viewed.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				notAuthorizedCollection.setErrors(notAuthorizedMsgs);
				notAuthorizedCollection.setCategories(new ArrayList<>());
				notAuthorizedCollection.setRecords(new ArrayList<>());
				notAuthorizedCollection.setTotalRecordCount(0);
				return new ResponseEntity<>(notAuthorizedCollection, HttpStatus.FORBIDDEN);
			}
			NotificationCollectionVO collectResult = notificationService.getAll(userId, eventCategory, readType,
					searchTerm, offset, limit);
			List<NotificationVO> list = collectResult.getRecords();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			Comparator<NotificationVO> compareByDateTime = (NotificationVO r1, NotificationVO r2) -> LocalDateTime
					.parse(r2.getDateTime(), formatter).compareTo(LocalDateTime.parse(r1.getDateTime(), formatter));
			Collections.sort(list, compareByDateTime);
			// applying limit and offset

			if (limit == 0 || (offset + limit) >= list.size())
				limit = list.size();
			else
				limit = offset + limit;
			if (list != null && !list.isEmpty())
				list = list.subList(offset, limit);
			LOG.debug("Applied limit and offset {} {} ", limit, offset);
			List<NotificationVO> unreadlist = list.stream().filter(n -> n.getIsRead().equalsIgnoreCase("false"))
					.collect(Collectors.toList());
			List<NotificationVO> readlist = list.stream().filter(n -> n.getIsRead().equalsIgnoreCase("true"))
					.collect(Collectors.toList());
			List<NotificationVO> result = new ArrayList<NotificationVO>();
			result.addAll(unreadlist);
			result.addAll(readlist);
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			SimpleDateFormat existingDateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			List<NotificationVO> isoDateFormattedResult = result.stream().map(n -> {
				String notificationTime = n.getDateTime();
				try {
					n.setDateTime(isoFormat.format(existingDateFormatter.parse(notificationTime)));
				} catch (Exception e) {
					LOG.error("Failed in parsing date for record {} of date {} for user {}", n.getId(),n.getDateTime(),userId);
				}
				return n;
			}).collect(Collectors.toList());
			collectResult.setRecords(isoDateFormattedResult);
			return new ResponseEntity<>(collectResult, HttpStatus.OK);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to fetch the reords due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOG.error(exceptionMsg.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Deletes the notifications identified by given ID.", nickname = "markAsDelete", notes = "Deletes the notifications identified by given ID", response = com.daimler.dna.notifications.controller.exceptions.GenericMessage.class, tags = {
			"notifications", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = com.daimler.dna.notifications.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<com.daimler.dna.notifications.controller.exceptions.GenericMessage> markAsDelete(
			@ApiParam(value = "request for marking messages as deleted for given user and selected messages", required = true) @Valid @RequestBody MarkedSelection requestVO) {

		try {
			notificationService.markMessageAsDelete(requestVO.getUserId(), requestVO.getMessageIds());
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("Deleted successfully");
			LOG.debug("{} Marked as deleted successfully by user {} ", requestVO.getMessageIds(),
					requestVO.getUserId());
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOG.error(exceptionMsg.getMessage());
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "marks the notification identified by given ID as read.", nickname = "markAsRead", notes = "marks the notification identified by given ID as read", response = com.daimler.dna.notifications.controller.exceptions.GenericMessage.class, tags = {
			"notifications", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Marked as read.", response = com.daimler.dna.notifications.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<com.daimler.dna.notifications.controller.exceptions.GenericMessage> markAsRead(
			@ApiParam(value = "request for marking messages as read for given user and selected messages", required = true) @Valid @RequestBody MarkedSelection requestVO) {

		try {
			notificationService.markMessageAsRead(requestVO.getUserId(), requestVO.getMessageIds());
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("Marked as read successfully");
			LOG.debug("{} Marked as read successfully by user {} ", requestVO.getMessageIds(), requestVO.getUserId());
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to update due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOG.error(exceptionMsg.getMessage());
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "service to publish a message to central events topic", nickname = "publishToCentralTopic", notes = "service to publish a message to central events topic", response = com.daimler.dna.notifications.controller.exceptions.GenericMessage.class, tags = {
			"notifications", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Published successfully.", response = com.daimler.dna.notifications.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<com.daimler.dna.notifications.controller.exceptions.GenericMessage> publishToCentralTopic(
			@ApiParam(value = "request for publishing message", required = true) @Valid @RequestBody NotificationRequestVO requestVO) {

		try {
			if (requestVO != null) {
				NotificationRequestVOData data = requestVO.getData();
				if (data != null) {
					GenericEventRecord record = new GenericEventRecord();
					record.setEventType(data.getEventType());
					record.setMailRequired(false);
					record.setChangeLogs(null);
					record.setMessage(data.getMessage());
					record.setPublishingAppName(data.getPublishingUser());
					record.setPublishingUser(data.getPublishingUser());
					ServiceUsersTypeEnum serviceType = data.getServiceUsersType();
					if("All".equalsIgnoreCase(serviceType.name())){
						UsersCollection usersCollection = dnaClient.getAllUsers();
						if(usersCollection!=null && usersCollection.getRecords()!= null && !usersCollection.getRecords().isEmpty()) {
							List<String> allUsers = new ArrayList<>();
							allUsers = usersCollection.getRecords().stream().map(n -> n.getId()).collect(Collectors.toList());
							record.setSubscribedUsers(allUsers);
							LOG.info("Broadcasting message to all users");
						}
					}else {
						record.setSubscribedUsers(data.getSubscribedUsers());
						LOG.info("Sending custom notification to users {}", data.getSubscribedUsers());
					}
					SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					record.setTime(dateFormatter.format(new Date()));
					record.setUuid(UUID.randomUUID().toString());
					notificationService.publishMessage(record);
					GenericMessage successMsg = new GenericMessage();
					successMsg.setSuccess("Published successfully");
					LOG.info("Published successfully by user {} of type {} ", data.getPublishingUser(),
							data.getEventType());
					return new ResponseEntity<>(successMsg, HttpStatus.OK);
				}
			}
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("Failed, unknown state.");
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to publish message due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOG.error("Failed to publish message with exception {} ", e.getMessage());
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
