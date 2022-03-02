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

import com.daimler.data.api.widget.WidgetsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.dto.widget.WidgetCollection;
import com.daimler.data.dto.widget.WidgetRequestVO;
import com.daimler.data.dto.widget.WidgetVO;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.service.widget.WidgetService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(value = "Widget API", tags = { "widgets" })
@RequestMapping("/api")
@Slf4j
public class WidgetController implements WidgetsApi {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private WidgetService widgetService;

	@ApiOperation(value = "Adds a new widget.", nickname = "createWidget", notes = "Adds a new non existing widget", response = WidgetVO.class, tags = {
			"widgets", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = WidgetVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/widgets", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<WidgetVO> createWidget(
			@ApiParam(value = "Request Body that contains data required for creating a new widget", required = true) @Valid @RequestBody WidgetRequestVO widgetRequestVO) {
		WidgetVO requestWidgetVO = widgetRequestVO.getData();
		try {
			WidgetVO existingWidgetVO = widgetService.getByUniqueliteral("name", requestWidgetVO.getName());
			if (existingWidgetVO != null && existingWidgetVO.getName() != null)
				return new ResponseEntity<>(existingWidgetVO, HttpStatus.CONFLICT);
			requestWidgetVO.setId(null);
			WidgetVO widgetVO = widgetService.create(requestWidgetVO);
			if (widgetVO != null && widgetVO.getId() != null) {
				log.info("Widget {} created successfully with id {} ", requestWidgetVO.getName(), widgetVO.getId());
				return new ResponseEntity<>(widgetVO, HttpStatus.CREATED);
			} else {
				log.info("Widget {} creation  unsuccessfully ", requestWidgetVO.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Failed while creating widget {} with exception", requestWidgetVO.getName(), e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Deletes the widget identified by given ID.", nickname = "delete", notes = "Deletes the widget identified by given ID", response = com.daimler.data.controller.exceptions.GenericMessage.class, tags = {
			"widgets", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/widgets/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the widget", required = true) @PathVariable("id") String id) {
		try {
//    		CreatedByVO currentUser = this.userStore.getVO();
//            String userId = currentUser!= null ? currentUser.getId() : "";
//            if(userId != null && !"".equalsIgnoreCase(userId)) {
//            	UserInfoVO userInfoVO = userInfoService.getById(userId);
//            	if(userInfoVO!=null) {
//            		List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
//            		if(userRoleVOs!=null && !userRoleVOs.isEmpty()) {
//            			boolean isAdmin = userRoleVOs.stream().anyMatch(n-> "Admin".equalsIgnoreCase(n.getName()));
//            			if(userId==null || !isAdmin) {
//            	            MessageDescription notAuthorizedMsg = new MessageDescription();
//            	            notAuthorizedMsg.setMessage("Not authorized to delete Widgets. Only user with admin role can edit.");
//            	            GenericErrorMessage errorMessage = new GenericErrorMessage(notAuthorizedMsg);
//            	            return new ResponseEntity<>(errorMessage,HttpStatus.FORBIDDEN);
//            			}
//            		}
//            	}
//            }
			boolean flag = widgetService.deleteById(id);
			if (flag) {
				log.info("widget with id {} deleted successfully", id);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				MessageDescription invalidMsg = new MessageDescription("Given id doesnt exist.");
				GenericMessage errorMessageDto = new GenericMessage();
				errorMessageDto.addErrors(invalidMsg);
				log.info("Widget cannot be deleted. No record found for given id {}", id);
				return new ResponseEntity<>(errorMessageDto, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			log.error("Failed to delete widget with id {}, exception msg {} ", id,e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessageDto = new GenericMessage();
			errorMessageDto.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessageDto, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get all available widgets.", nickname = "getAll", notes = "Get all widgets. This endpoints will be used to Get all valid available widgets maintenance records.", response = WidgetCollection.class, tags = {
			"widgets", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = WidgetCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/widgets", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<WidgetCollection> getAll() {
		final List<WidgetVO> widgets = widgetService.getAll();
		WidgetCollection widgetCollection = new WidgetCollection();
		if (widgets != null && widgets.size() > 0) {
			widgetCollection.addAll(widgets);
			return new ResponseEntity<>(widgetCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(widgetCollection, HttpStatus.NO_CONTENT);
		}
	}

}
