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

import com.daimler.data.api.userwidgetpref.WidgetDataApi;
import com.daimler.data.api.userwidgetpref.WidgetPreferenceApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.userwidgetpref.UserWidgetData;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceCollection;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceRequestVO;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.service.userwidgetpreference.UserWidgetPrefService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(value = "User Widget Preference API", tags = {"userWidgetPreference"})
@RequestMapping("/api")
public class UserWidgetPreferenceController implements WidgetPreferenceApi, WidgetDataApi {

    @Autowired
    private UserStore userStore;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserWidgetPrefService userWidgetPrefService;

    @ApiOperation(value = "Adds a new UserWidgetPreference.", nickname = "createUserWidgetPreference", notes = "Adds a new widget preference for the user", response = UserWidgetPreferenceVO.class, tags = {"userWidgetPreference",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure ", response = UserWidgetPreferenceVO.class),
            @ApiResponse(code = 400, message = "Bad Request", response = com.daimler.data.controller.exceptions.GenericMessage.class),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/widget-preference",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public ResponseEntity<UserWidgetPreferenceVO> createUserWidgetPreference(@ApiParam(value = "Request Body that contains data required for creating a new widgetPreference", required = true) @Valid @RequestBody UserWidgetPreferenceRequestVO userWidgetPreferenceRequestVO) {
        UserWidgetPreferenceVO requestUserWidgetPrefVO = userWidgetPreferenceRequestVO.getData();
        boolean requesteCreated = true;
        try {
            UserWidgetPreferenceVO existingUserWidgetPrefVO = userWidgetPrefService.getByUniqueliteral("userId", requestUserWidgetPrefVO.getUserId());
            if (existingUserWidgetPrefVO != null && existingUserWidgetPrefVO.getUserId() != null) {
                //return new ResponseEntity<>(existingUserWidgetPrefVO, HttpStatus.CONFLICT);
            	requestUserWidgetPrefVO.setId(existingUserWidgetPrefVO.getId());
            	requesteCreated = false;
            } else
            	requestUserWidgetPrefVO.setId(null);
            UserWidgetPreferenceVO userWidgetPrefVO = userWidgetPrefService.create(requestUserWidgetPrefVO);
            if (userWidgetPrefVO != null && userWidgetPrefVO.getId() != null) {
                return new ResponseEntity<>(userWidgetPrefVO, requesteCreated ? HttpStatus.CREATED : HttpStatus.OK);
            } else
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Deletes a UserWidget Preference identified by given ID.", nickname = "delete", notes = "Deletes the UserWidget Preference identified by given ID", response = com.daimler.data.controller.exceptions.GenericMessage.class, tags = {"userWidgetPreference",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted.", response = com.daimler.data.controller.exceptions.GenericMessage.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 404, message = "Invalid id, record not found."),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/widget-preference/{id}",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> delete(@ApiParam(value = "Id of the UserWidget  Preference", required = true) @PathVariable("id") String id) {
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
//            	            notAuthorizedMsg.setMessage("Not authorized to delete Widget Preferences. Only user with admin role can edit.");
//            	            GenericErrorMessage errorMessage = new GenericErrorMessage(notAuthorizedMsg);
//            	            return new ResponseEntity<>(errorMessage,HttpStatus.FORBIDDEN);
//            			}
//            		}
//            	}
//            }
            boolean flag = userWidgetPrefService.deleteById(id);
            if (flag) {
            	GenericMessage successMsg = new GenericMessage();
                 successMsg.setSuccess("success");
                return new ResponseEntity<>(successMsg, HttpStatus.OK);
            } else {
            	 MessageDescription invalidMsg = new MessageDescription("Given id doesnt exist.");
            	 GenericMessage errorMessage = new GenericMessage();
                 errorMessage.addErrors(invalidMsg);
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(exceptionMsg);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Get all available User widget Preferences.", nickname = "getAll", notes = "Get all UserWidgetPreferences. This endpoints will be used to Get all valid available User Widget Preferences.", response = UserWidgetPreferenceCollection.class, tags = {"userWidgetPreference",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = UserWidgetPreferenceCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/widget-preference",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<UserWidgetPreferenceCollection> getAll(@ApiParam(value = "UserId to fetch his widget preferences.") @Valid @RequestParam(value = "userId", required = false) String userId) {
        UserWidgetPreferenceCollection userWidgetPreferenceCollection = new UserWidgetPreferenceCollection();
        if (!StringUtils.isEmpty(userId)) {
            UserWidgetPreferenceVO existingUserWidgetPrefVO = userWidgetPrefService.getByUniqueliteral("userId", userId);
            if (existingUserWidgetPrefVO != null) {
                userWidgetPreferenceCollection.add(existingUserWidgetPrefVO);
            }
        } else {
            final List<UserWidgetPreferenceVO> userWidgetPreferences = userWidgetPrefService.getAll();
            if (userWidgetPreferences != null && userWidgetPreferences.size() > 0) {
                userWidgetPreferenceCollection.addAll(userWidgetPreferences);
            }
        }
        return new ResponseEntity<>(userWidgetPreferenceCollection, userWidgetPreferenceCollection.size() > 0 ? HttpStatus.OK : HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Get data for the widget identified by given ID.", nickname = "getWidgetData", notes = "Get data for the widget identified by given ID", response = UserWidgetData.class, tags = {"userWidgetPreference",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = UserWidgetData.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/widget-data/{id}",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<UserWidgetData> getWidgetData(@ApiParam(value = "Id of the widget", required = true) @PathVariable("id") String id) {
        return null;
    }
}
