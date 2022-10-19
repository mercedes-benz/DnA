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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.jwt.JwtApi;
import com.daimler.data.application.filter.JWTGenerator;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.jwt.JwtResponseVO;
import com.daimler.data.registry.config.VaultConfig;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "the Jwt API", tags = { "jwt" })
@RequestMapping("/api")
@Validated
public class JwtController implements JwtApi {

	private static Logger LOGGER = LoggerFactory.getLogger(JwtController.class);

	@Autowired
	private VaultConfig vaultConfig;

	@Override
	@ApiOperation(value = "Generate jwt to access models", nickname = "generateToken", notes = "Generate jwt to access models", response = JwtResponseVO.class, tags = {
			"jwt", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = JwtResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have valid credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/models/jwt", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<JwtResponseVO> generateToken(
			@ApiParam(value = "appId to generate token.", required = true) @RequestHeader(value = "appId", required = true) String appId,
			@ApiParam(value = "appKey to generate token", required = true) @RequestHeader(value = "appKey", required = true) String appKey) {
		JwtResponseVO response = new JwtResponseVO();
		try {
			if (!StringUtils.hasText(appId) || !StringUtils.hasText(appKey)) {
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			VaultResponse vaultResponse = vaultConfig.validateAppKey(appId, appKey);
			if (vaultResponse != null && vaultResponse.getData() != null && vaultResponse.getData().get(appId) != null
					&& vaultResponse.getData().get(appId).equals(appKey)) {
				LOGGER.info("AppKey is valid for appID {}", appId);
				String jwt = JWTGenerator.generateJWT(appId);
				response.setToken(jwt);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				LOGGER.info("Invalid AppKey for appId {}", appId);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("AppId or AppKey is invalid");
				messages.add(message);
				response.setErrors(messages);
				return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred: {} while generating jwt token", e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
