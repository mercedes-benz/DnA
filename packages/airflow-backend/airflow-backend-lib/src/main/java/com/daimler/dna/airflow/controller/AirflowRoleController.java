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

//import com.daimler.dna.airflow.api.RolesApi;
import com.daimler.dna.airflow.dto.AirflowResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleVO;
import com.daimler.dna.airflow.dto.AirflowUserVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.service.RoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/*@RestController
@RequestMapping("/api/v1")
@Api(value = "Roles", tags = { "roles" })*/
public class AirflowRoleController /* implements RolesApi */ {

	/*
	 * private static Logger LOGGER =
	 * LoggerFactory.getLogger(AirflowRoleController.class);
	 * 
	 * @Autowired private RoleService rs;
	 * 
	 * @ApiOperation(value = "Create roles in airflow", nickname = "createRoles",
	 * notes = "Airflow roles will be created with this api", response =
	 * AirflowRoleResponseWrapperVO.class, tags = { "roles", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 201, message = "Returns message of succes or failure ",
	 * response = AirflowRoleResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowRoleResponseWrapperVO.class),
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
	 * @RequestMapping(value = "/roles", method = RequestMethod.POST) public
	 * ResponseEntity<AirflowRoleResponseWrapperVO> createRoles(
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create roles in airflow", required =
	 * true) @Valid @RequestBody AirflowRoleVO airflowRoleVo) {
	 * AirflowRoleResponseWrapperVO response = new AirflowRoleResponseWrapperVO();
	 * rs.createRole(airflowRoleVo); GenericMessage gm = new GenericMessage();
	 * gm.setSuccess("success"); response.setResponse(gm); return new
	 * ResponseEntity<AirflowRoleResponseWrapperVO>(response, HttpStatus.CREATED); }
	 * 
	 * @ApiOperation(value = "Returns existing roles from the airflow", nickname =
	 * "getAirflowRoles", notes = "Returns existing roles from the airflow",
	 * response = AirflowRoleResponseWrapperVO.class, tags = { "roles", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Successfully downloaded.", response =
	 * AirflowRoleResponseWrapperVO.class),
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
	 * @RequestMapping(value = "/roles", method = RequestMethod.GET) public
	 * ResponseEntity<AirflowRoleResponseWrapperVO> getAirflowRoles() {
	 * AirflowRoleResponseWrapperVO response = new AirflowRoleResponseWrapperVO();
	 * GenericMessage gm = new GenericMessage(); List<AirflowRoleVO> list =
	 * rs.getAllAirflowRoles(); response.setAirflowRoleVO(list);
	 * response.setResponse(gm); return new
	 * ResponseEntity<AirflowRoleResponseWrapperVO>(response, HttpStatus.OK); }
	 * 
	 * @ApiOperation(value = "update roles in airflow", nickname = "updateRoles",
	 * notes = "update existing Airflow roles will with this api", response =
	 * AirflowRoleResponseWrapperVO.class, tags = { "roles", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Returns message of succes or failure ",
	 * response = AirflowRoleResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowRoleResponseWrapperVO.class),
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
	 * @RequestMapping(value = "/roles/{name}", method = RequestMethod.PUT) public
	 * ResponseEntity<AirflowRoleResponseWrapperVO> updateRoles(
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create roles in airflow", required =
	 * true) @PathVariable("name") String name,
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create roles in airflow", required =
	 * true) @Valid @RequestBody AirflowRoleVO airflowRoleVo) { return null; }
	 * 
	 * @ApiOperation(value = "update roles in airflow", nickname = "updateRoles_0",
	 * notes = "update existing Airflow roles will with this api", response =
	 * AirflowRoleResponseWrapperVO.class, tags = { "roles", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Returns message of succes or failure ",
	 * response = AirflowRoleResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowRoleResponseWrapperVO.class),
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
	 * @RequestMapping(value = "/roles/{name}", method = RequestMethod.DELETE)
	 * public ResponseEntity<AirflowRoleResponseWrapperVO> updateRoles_0(
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create roles in airflow", required =
	 * true) @PathVariable("name") String name) { return null; }
	 */
}
