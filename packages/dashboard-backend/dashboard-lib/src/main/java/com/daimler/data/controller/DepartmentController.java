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

import com.daimler.data.api.department.DepartmentsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.department.DepartmentCollection;
import com.daimler.data.dto.department.DepartmentRequestVO;
import com.daimler.data.dto.department.DepartmentResponseVO;
import com.daimler.data.dto.department.DepartmentUpdateRequestVO;
import com.daimler.data.service.department.DepartmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Department API", tags = { "departments" })
@RequestMapping("/api")
public class DepartmentController implements DepartmentsApi {

	@Autowired
	private DepartmentService departmentService;

	@Override
	@ApiOperation(value = "Add a new department.", nickname = "create", notes = "Add a new non existing department.", response = DepartmentResponseVO.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = DepartmentResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/departments", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<DepartmentResponseVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new department", required = true) @Valid @RequestBody DepartmentRequestVO departmentRequestVO) {
		return departmentService.createDepartment(departmentRequestVO);
	}

	@Override
	@ApiOperation(value = "Deletes the department identified by given ID.", nickname = "delete", notes = "Deletes the department identified by given ID", response = GenericMessage.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/departments/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the department", required = true) @PathVariable("id") Long id) {
		return departmentService.deleteDepartment(id);
	}

	@Override
	@ApiOperation(value = "Get all available department.", nickname = "getAll", notes = "Get all department. This endpoints will be used to get all valid available department records.", response = DepartmentCollection.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = DepartmentCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/departments", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DepartmentCollection> getAll(			
			@ApiParam(value = "Sort departments based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return departmentService.getAllDepartments(sortOrder);
	}

	@Override
	@ApiOperation(value = "Update the department identified by given ID.", nickname = "update", notes = "Update the department identified by given ID", response = DepartmentResponseVO.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully updated.", response = DepartmentResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/departments", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<DepartmentResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating department.", required = true) @Valid @RequestBody DepartmentUpdateRequestVO departmentUpdateRequestVO) {
		return departmentService.updateDepartment(departmentUpdateRequestVO);
	}

}
