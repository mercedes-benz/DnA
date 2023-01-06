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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.department.DepartmentsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.department.DepartmentCollection;
import com.daimler.data.dto.department.DepartmentRequestVO;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.department.DepartmentService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Department API", tags = { "departments" })
@RequestMapping("/api")
@Slf4j
public class DepartmentController implements DepartmentsApi {
	
	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private UserStore userStore;
	
	@Autowired
	private UserInfoService userInfoService;

	@Override
	@ApiOperation(value = "Adds a new department.", nickname = "create", notes = "Adds a new non existing department which is used in providing solution.", response = DepartmentVO.class, tags={ "departments", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = DepartmentVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/departments",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<DepartmentVO> create(
    		@ApiParam(value = "Request Body that contains data required for creating a new department" ,required=true )  @RequestBody DepartmentRequestVO departmentRequestVO) {

		DepartmentVO requestDepartmentVO = departmentRequestVO.getData();
		try {
			DepartmentVO existingDepartmentVO = departmentService.getByUniqueliteral("name", requestDepartmentVO.getName());
			if (existingDepartmentVO != null && existingDepartmentVO.getName() != null)
				return new ResponseEntity<>(existingDepartmentVO, HttpStatus.CONFLICT);
			requestDepartmentVO.setId(null);
			DepartmentVO departmentVO = departmentService.create(requestDepartmentVO);
			if (departmentVO != null && departmentVO.getId() != null) {
				log.debug("Department {} created successfully", requestDepartmentVO.getName());
				return new ResponseEntity<>(departmentVO, HttpStatus.CREATED);
			} else {
				log.debug("Department {} creation failed with unknown error ", requestDepartmentVO.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Failed to create department {} with exception {} ", requestDepartmentVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Get all available departments.", nickname = "getAll", notes = "Get all departments. This endpoints will be used to Get all valid available departments maintenance records.", response = DepartmentCollection.class, tags={ "departments", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = DepartmentCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/departments",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<DepartmentCollection> getAll(
    		@ApiParam(value = "Sort departments by a given variable like departmentName", allowableValues = "departmentName") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,@ApiParam(value = "Sort departments based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		final List<DepartmentVO> departments = departmentService.getAll();		
		DepartmentCollection departmentCollection = new DepartmentCollection();
		log.debug("Sending all algorithms");
		if (departments != null && departments.size() > 0) {
			if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				Comparator<DepartmentVO> comparator = (a1, a2) -> (a1.getName().compareTo(a2.getName()));
				Collections.sort(departments, comparator);
			}
			if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				Comparator<DepartmentVO> comparator = (a1, a2) -> (a2.getName().compareTo(a1.getName()));
				Collections.sort(departments, comparator);
			}
			departmentCollection.addAll(departments);
			return new ResponseEntity<>(departmentCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(departmentCollection, HttpStatus.NO_CONTENT);
		}
	}

	@Override
	 @ApiOperation(value = "Deletes the department identified by given ID.", nickname = "delete", notes = "Deletes the department identified by given ID", response = GenericMessage.class, tags={ "departments", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/departments/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the department", required = true) @PathVariable("id") String id) {
		CreatedByVO currentUser = this.userStore.getVO();		
		try {
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						if (userId == null || !isAdmin) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to delete department. User does not have admin privileges.");
							log.debug("User not authorized to delete department. Doesnt have admin privileges");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			DepartmentVO department = departmentService.getById(id);
			String departmentName = department != null ? department.getName() : "";
			String userName = departmentService.currentUserName(currentUser);
			String eventMessage = "department  " + departmentName + " has been deleted by Admin " + userName;
			departmentService.deleteDepartment(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			log.info("department with id {} deleted successfully", id);
			successMsg.setSuccess("success");
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception {} while deleting department with id {}, ID not found", e.getLocalizedMessage(), id);
			MessageDescription invalidMsg = new MessageDescription("No department with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to delete department with id {} with exception {} ", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete department due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
}
