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

import com.daimler.data.dto.model.TransparencyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.model.ModelsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.model.ModelCollection;
import com.daimler.data.dto.model.ModelRequestVO;
import com.daimler.data.dto.model.ModelResponseVO;
import com.daimler.data.registry.models.service.RegistryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Models API", tags = { "models" })
@RequestMapping("/api")
@Validated
public class RegistryController implements ModelsApi {

	@Autowired
	private RegistryService modelRegistryservice;

	@Override
	@ApiOperation(value = "Get all available models for requested user", nickname = "getAll", notes = "Get all models for requested user.", response = ModelCollection.class, tags = {
			"models", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = ModelCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/models", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ModelCollection> getAll(
			@ApiParam(value = "Authorization token.", required = true) @RequestHeader(value = "Authorization", required = true) String authorization) {
		return modelRegistryservice.getAllModels();
	}

	@Override
	@ApiOperation(value = "Number of models.", nickname = "getNumberOfModels", notes = "Get number of models. This endpoints will be used to get number of available models records.", response = TransparencyVO.class, tags={ "models", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/model/transparency",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfModels(String authorization) {
		try {
			TransparencyVO transparencyVO = new TransparencyVO();
			Integer count = modelRegistryservice.getCountOfModels();
			transparencyVO.setCount(count);
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e){
			return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}



	@Override
	@ApiOperation(value = "create a external url.", nickname = "create", notes = "create a external url", response = ModelResponseVO.class, tags = {
			"models", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = ModelResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 409, message = "Conflict", response = ModelResponseVO.class),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/model/externalurl", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ModelResponseVO> create(
			@ApiParam(value = "Authorization token.", required = true) @RequestHeader(value = "Authorization", required = true) String authorization,
			@ApiParam(value = "Request Body that contains data required for creating a external url", required = true) @Valid @RequestBody ModelRequestVO modelRequestVO) {
		return modelRegistryservice.generateExternalUrl(modelRequestVO);
	}

}
