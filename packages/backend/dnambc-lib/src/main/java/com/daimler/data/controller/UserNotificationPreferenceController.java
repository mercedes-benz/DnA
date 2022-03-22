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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.usernotificationpref.NotificationPreferencesApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.usernotificationpref.UserNotificationPrefRequestVO;
import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;
import com.daimler.data.service.usernotificationpref.UserNotificationPrefService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "User Notification Preference API", tags = { "userNotificationPreferences" })
@RequestMapping("/api")
@Slf4j
public class UserNotificationPreferenceController implements NotificationPreferencesApi{
	
	@Autowired
	public UserNotificationPrefService userNotificationPrefService;
	
	@Override
	@ApiOperation(value = "Get user notification preferences.", nickname = "getUserNotificationPreferences", notes = "Get user notification preferences", response = UserNotificationPrefVO.class, tags={ "userNotificationPreferences", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = UserNotificationPrefVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/notification-preferences",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<UserNotificationPrefVO> getUserNotificationPreferences(@NotNull @ApiParam(value = "shortId of the user", required = true) @Valid @RequestParam(value = "userId", required = true) String userId){
		UserNotificationPrefVO preferencesVO = new UserNotificationPrefVO();
		try {
			preferencesVO = userNotificationPrefService.getByUniqueliteral("userId", userId);
			log.debug("Fetched user notification preferences successfully.");
			return new ResponseEntity<>(preferencesVO, HttpStatus.OK);
		}catch(Exception e) {
			log.error("Error occured while fetching notification preferences for user {}", userId);
		}
		return null;	
	}

	@Override
    @ApiOperation(value = "Saves user notification preference.", nickname = "saveUserNotificationPreferences", notes = "Saves user notification preference.", response = UserNotificationPrefVO.class, tags={ "userNotificationPreferences", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = UserNotificationPrefVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/notification-preferences",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<UserNotificationPrefVO> saveUserNotificationPreferences(@ApiParam(value = "Request Body that contains data required for saving user notification preference" ,required=true )  @Valid @RequestBody UserNotificationPrefRequestVO userNotificationPrefRequestVO){
		UserNotificationPrefVO preferencesRequestVO = userNotificationPrefRequestVO.getData();
		UserNotificationPrefVO responseVO = new UserNotificationPrefVO();
		try {
			responseVO = userNotificationPrefService.create(preferencesRequestVO);
			log.debug("Saved user notification preferences successfully.");
			return new ResponseEntity<>(responseVO, HttpStatus.OK);
		}catch(Exception e) {
			log.error("Error occured while saving notification preferences for user {}", preferencesRequestVO.getUserId());
		}
		return null;	
	}

}
