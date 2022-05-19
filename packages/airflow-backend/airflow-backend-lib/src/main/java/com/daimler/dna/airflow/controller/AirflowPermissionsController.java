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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//import com.daimler.dna.airflow.api.PermissionsApi;
import com.daimler.dna.airflow.dto.AirflowPermissionVO;
import com.daimler.dna.airflow.dto.AirflowPermissionsResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.service.PermissionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/*@RestController
@RequestMapping("/api/v1")
@Api(value = "Permission", tags = { "permissions" })*/
public class AirflowPermissionsController /* implements PermissionsApi */ {
	/*
	 * private static Logger LOGGER =
	 * LoggerFactory.getLogger(AirflowPermissionsController.class);
	 * 
	 * @Autowired private PermissionService service;
	 * 
	 * 
	 * 
	 * @ApiOperation(value = "Returns existing permissions from the airflow",
	 * nickname = "getAirflowPermissions", notes =
	 * "Returns existing permissions from the airflow", response =
	 * AirflowPermissionsResponseWrapperVO.class, tags = { "permissions", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Successfully downloaded.", response =
	 * AirflowPermissionsResponseWrapperVO.class),
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
	 * @RequestMapping(value = "/permissions", method = RequestMethod.GET) public
	 * ResponseEntity<AirflowPermissionsResponseWrapperVO> getAirflowPermissions() {
	 * AirflowPermissionsResponseWrapperVO response = new
	 * AirflowPermissionsResponseWrapperVO(); GenericMessage gm = new
	 * GenericMessage(); List<AirflowPermissionVO> permissionList =
	 * service.getAllPermission(); response.setPermissions(permissionList);
	 * response.setResponse(gm); return new
	 * ResponseEntity<AirflowPermissionsResponseWrapperVO>(response, HttpStatus.OK);
	 * }
	 */
}
