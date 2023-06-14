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

import com.daimler.data.dto.userinfo.TransparencyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.userinfo.UserInfoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "UserInfo API", tags = { "users" })
@RequestMapping("/api")
@Slf4j
public class UserInfoController {

	private static Logger logger = LoggerFactory.getLogger(UserInfoController.class);
	
	@Autowired
	private UserInfoService userInfoService;
	
	@Autowired
	private UserInfoAssembler userinfoAssembler;
	
	@ApiOperation(value = "Get specific user for a given userid.", nickname = "getById", notes = "Get specific user for a given userid. This endpoints will be used to Get specific user for a given userid.", response = UserInfoVO.class, tags = {
			"users",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = UserInfoVO.class),
			@ApiResponse(code = 400, message = "Malformed syntax."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/users/{id}", produces = {"application/json"}, consumes = {
			"application/json"}, method = RequestMethod.GET)
	public ResponseEntity<UserInfoVO> getById(
			@ApiParam(value = "Id of the user for which information to be fetched", required = true) @PathVariable("id") String id) {
		UserInfoVO userInfoVO = null;
		if (id != null) {
			userInfoVO = userInfoService.getById(id);
			return new ResponseEntity<>(userInfoVO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(userInfoVO, HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Get total count of users.", nickname = "getCountOfUsers", notes = "Get total count of users. This endpoints will be used to Get total count of users.", response = TransparencyVO.class, tags = {
			"users",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = TransparencyVO.class),
			@ApiResponse(code = 400, message = "Malformed syntax."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/users/transparency", produces = {"application/json"}, consumes = {
			"application/json"}, method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getCountOfUsers() {
		TransparencyVO transparencyVO = new TransparencyVO();
		try {
			Integer count = userInfoService.getNumberOfUsers();
			transparencyVO.setCount(count);
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		} catch (Exception e){
			return new ResponseEntity<>(transparencyVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	

}
