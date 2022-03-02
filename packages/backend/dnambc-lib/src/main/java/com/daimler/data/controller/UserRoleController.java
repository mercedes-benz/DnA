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

import com.daimler.data.api.userrole.UserRolesApi;
import com.daimler.data.dto.userrole.UserRoleCollection;
import com.daimler.data.dto.userrole.UserRoleVO;
import com.daimler.data.service.userrole.UserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "UserRole API", tags = { "userroles" })
@RequestMapping("/api")
@Slf4j
public class UserRoleController implements UserRolesApi {

	@Autowired
	private UserRoleService userroleService;

	@Override
	@ApiOperation(value = "Get all available results.", nickname = "getAll", notes = "Get all userroles. This endpoints will be used to Get all valid available userroles maintenance records.", response = UserRoleCollection.class, tags = {
			"userroles", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all userroles", response = UserRoleCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/userRoles", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<UserRoleCollection> getAll() {
		final List<UserRoleVO> userrolesVo = userroleService.getAll();
		UserRoleCollection userroleCollection = new UserRoleCollection();
		if (userrolesVo != null && userrolesVo.size() > 0) {
			userroleCollection.addAll(userrolesVo);
			log.debug("Returning all userRoles");
			return new ResponseEntity<>(userroleCollection, HttpStatus.OK);
		} else {
			log.debug("No userRoles to return");
			return new ResponseEntity<>(userroleCollection, HttpStatus.NO_CONTENT);
		}
	}

}
