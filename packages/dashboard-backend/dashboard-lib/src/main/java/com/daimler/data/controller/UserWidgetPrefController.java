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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.userwidgetpref.WidgetPreferenceApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceCollection;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceRequestVO;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;
import com.daimler.data.service.userwidgetpref.UserWidgetPrefService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "User Widget Preference API", tags = { "userWidgetPreference" })
@RequestMapping("/api")
public class UserWidgetPrefController implements WidgetPreferenceApi {

	@Autowired
	private UserWidgetPrefService userWidgetPrefService;

	@Override
	@ApiOperation(value = "Adds a new UserWidgetPreference.", nickname = "createUserWidgetPreference", notes = "Adds a new widget preference for the user", response = UserWidgetPreferenceVO.class, tags = {
			"userWidgetPreference", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = UserWidgetPreferenceVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/widget-preference", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<UserWidgetPreferenceVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new widgetPreference", required = true) @Valid @RequestBody UserWidgetPreferenceRequestVO userWidgetPreferenceRequestVO) {
		return userWidgetPrefService.createUserWidgetPreference(userWidgetPreferenceRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Deletes a User Widget Preference identified by given ID.", nickname = "delete", notes = "Deletes the User Widget Preference identified by given ID", response = GenericMessage.class, tags = {
			"userWidgetPreference", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/widget-preference/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the User Widget Preference", required = true) @PathVariable("id") String id) {
		return userWidgetPrefService.deleteUserWidgetPreference(id);
	}

	@Override
	@ApiOperation(value = "Get all available User Widget Preferences.", nickname = "getAll", notes = "Get all UserWidgetPreferences. This endpoints will be used to get all valid available User Widget Preferences.", response = UserWidgetPreferenceCollection.class, tags = {
			"userWidgetPreference", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = UserWidgetPreferenceCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/widget-preference", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<UserWidgetPreferenceCollection> getAll(
			@ApiParam(value = "UserId to fetch his widget preferences.") @Valid @RequestParam(value = "userId", required = false) String userId) {
		return userWidgetPrefService.getAllUserWidgetPreference(userId);
	}
}
