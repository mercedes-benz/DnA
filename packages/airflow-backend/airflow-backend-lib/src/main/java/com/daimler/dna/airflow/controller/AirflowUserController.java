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

package com.daimler.dna.airflow.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//import com.daimler.dna.airflow.api.UsersApi;
import com.daimler.dna.airflow.dto.AirflowResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowUserVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/*@RestController
@RequestMapping("/api/v1")
@Api(value = "Users", tags = { "users" })*/
public class AirflowUserController /* implements UsersApi */ {
	/*
	 * private static Logger LOGGER =
	 * LoggerFactory.getLogger(AirflowUserController.class);
	 * 
	 * @Autowired private UserService us;
	 * 
	 * @ApiOperation(value = "Create users in airflow", nickname = "createUser",
	 * notes = "Airflow user will be onboarded with this api", response =
	 * AirflowResponseWrapperVO.class, tags = { "users", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 201, message = "Returns message of succes or failure ",
	 * response = AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Method not allowed"),
	 * 
	 * @ApiResponse(code = 500, message = "Internal error") })
	 * 
	 * @RequestMapping(value = "/users", method = RequestMethod.POST) public
	 * ResponseEntity<AirflowResponseWrapperVO> createUser(
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create user in airflow", required =
	 * true) @Valid @RequestBody AirflowUserVO airflowUserVo) { return
	 * us.createUser(airflowUserVo); }
	 * 
	 * @ApiOperation(value = "Returns existing users from the airflow", nickname =
	 * "getAirflowUsers", notes = "Returns existing users from the airflow",
	 * response = com.daimler.dna.airflow.exceptions.GenericMessage.class, tags = {
	 * "users", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Successfully downloaded.", response =
	 * com.daimler.dna.airflow.exceptions.GenericMessage.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad request"),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 404, message = "Invalid id, record not found."),
	 * 
	 * @ApiResponse(code = 500, message = "Internal error") })
	 * 
	 * @RequestMapping(value = "/users", method = RequestMethod.GET) public
	 * ResponseEntity<AirflowResponseWrapperVO> getAirflowUsers() {
	 * AirflowResponseWrapperVO response = new AirflowResponseWrapperVO();
	 * GenericMessage gm = new GenericMessage(); List<AirflowUserVO> list =
	 * us.getAllAirflowUsers(); response.setAirflowUserVO(list);
	 * response.setResponse(gm); return new
	 * ResponseEntity<AirflowResponseWrapperVO>(response, HttpStatus.OK); }
	 * 
	 * @ApiOperation(value = "deletes users in airflow", nickname = "deleteUser",
	 * notes = "Airflow user will be onboarded with this api", response =
	 * AirflowResponseWrapperVO.class, tags = { "users", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Returns message of succes or failure ",
	 * response = AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Method not allowed"),
	 * 
	 * @ApiResponse(code = 500, message = "Internal error") })
	 * 
	 * @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
	 * public ResponseEntity<AirflowResponseWrapperVO> deleteUser(
	 * 
	 * @ApiParam(value = "UserId in path", required = true) @PathVariable("userId")
	 * String userId) { return null; }
	 * 
	 * @ApiOperation(value = "update users in airflow", nickname = "updateUser",
	 * notes = "Existing Airflow user will be updated with this api", response =
	 * AirflowResponseWrapperVO.class, tags = { "users", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Returns message of succes or failure ",
	 * response = AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Method not allowed"),
	 * 
	 * @ApiResponse(code = 500, message = "Internal error") })
	 * 
	 * @RequestMapping(value = "/users/{userId}", method = RequestMethod.PUT) public
	 * ResponseEntity<AirflowResponseWrapperVO> updateUser(
	 * 
	 * @ApiParam(value = "UserId in path", required = true) @PathVariable("userId")
	 * String userId,
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create user in airflow", required =
	 * true) @Valid @RequestBody AirflowUserVO airflowUserVo) { return null; }
	 */

}
