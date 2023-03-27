package com.daimler.data.controller;

import com.daimler.data.api.userwidgetpref.UserWidgetPreferenceApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceCollection;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceRequestVO;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;
import com.daimler.data.service.userwidgetpref.UserWidgetPrefService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(value = "User Widget Preference API", tags = { "userWidgetPreference" })
@RequestMapping("/api")
@Slf4j
public class UserWidgetPreferenceController implements UserWidgetPreferenceApi {

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
            @ApiParam(value = "Request Body that contains data required for creating a new widgetPreference", required = true)
            @Valid @RequestBody UserWidgetPreferenceRequestVO userWidgetPreferenceRequestVO) {
        return userWidgetPrefService.createUserWidgetPreference(userWidgetPreferenceRequestVO.getData());
    }

    @Override
    @ApiOperation(value = "Get data for the widget identified by given ID.", nickname = "getWidgetData", notes = "Get data for the widget identified by given ID", response = UserWidgetPreferenceVO.class, tags = {
            "userWidgetPreference", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = UserWidgetPreferenceVO.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/widget-data/{id}", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<UserWidgetPreferenceVO> getWidgetData(
            @ApiParam(value = "Id of the User Widget Preference", required = true) @PathVariable("id") String id) {
            UserWidgetPreferenceVO userWidgetPreferenceVO = userWidgetPrefService.getById(id);
            if (userWidgetPreferenceVO == null) {
                log.info("No user widget data found, returning empty");
                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(userWidgetPreferenceVO, HttpStatus.OK);
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
        try {
            boolean flag =userWidgetPrefService.deleteById(id);
        if (flag) {
            GenericMessage successMsg = new GenericMessage();
            log.info("widget with id {} deleted successfully", id);
            successMsg.setSuccess("success");
            return new ResponseEntity<>(successMsg, HttpStatus.OK);
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
    };

    @ApiOperation(value = "Get all available User widget Preferences.", nickname = "getAll", notes = "Get all UserWidgetPreferences. This endpoints will be used to Get all valid available User Widget Preferences.", response = UserWidgetPreferenceCollection.class, tags = {
            "userWidgetPreference", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = UserWidgetPreferenceCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/widget-preference", produces = {"application/json"}, consumes = {
            "application/json"}, method = RequestMethod.GET)
    public ResponseEntity<UserWidgetPreferenceCollection> getAll(
            @ApiParam(value = "UserId to fetch his widget preferences.") @Valid @RequestParam(value = "userId", required = false) String userId) {
        UserWidgetPreferenceCollection userWidgetPreferenceCollection = new UserWidgetPreferenceCollection();
        if (!StringUtils.isEmpty(userId)) {
            UserWidgetPreferenceVO existingUserWidgetPrefVO = userWidgetPrefService.getByUniqueliteral("userId",
                    userId);
            if (existingUserWidgetPrefVO != null) {
                userWidgetPreferenceCollection.add(existingUserWidgetPrefVO);
            }
        } else {
            final List<UserWidgetPreferenceVO> userWidgetPreferences = userWidgetPrefService.getAll();
            if (userWidgetPreferences != null && userWidgetPreferences.size() > 0) {
                userWidgetPreferenceCollection.addAll(userWidgetPreferences);
            }
        }
        return new ResponseEntity<>(userWidgetPreferenceCollection,
                userWidgetPreferenceCollection.size() > 0 ? HttpStatus.OK : HttpStatus.NO_CONTENT);
    }
}
