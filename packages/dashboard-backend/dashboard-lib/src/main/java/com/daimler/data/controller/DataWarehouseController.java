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

import com.daimler.data.api.datawarehouse.DatawarehousesApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.datawarehouse.DataWarehouseCollection;
import com.daimler.data.dto.datawarehouse.DataWarehouseInUseVO;
import com.daimler.data.dto.datawarehouse.DataWarehouseRequestVO;
import com.daimler.data.dto.datawarehouse.DataWarehouseResponseVO;
import com.daimler.data.service.datawarehouse.DataWarehouseService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "DataWarehouse API", tags = { "datawarehouses" })
@RequestMapping("/api")
@SuppressWarnings(value = "unused")
public class DataWarehouseController implements DatawarehousesApi {

	private static Logger LOGGER = LoggerFactory.getLogger(DataWarehouseController.class);

	@Autowired
	private DataWarehouseService dataWarehouseService;

	@Override
	@ApiOperation(value = "Add a new dataWarehouse.", nickname = "create", notes = "Adds a new non existing dataWarehouse.", response = DataWarehouseResponseVO.class, tags = {
			"datawarehouses", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = DataWarehouseResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<DataWarehouseResponseVO> create(
			@ApiParam(value = "Request body that contains data required for creating a new dataWarehouse", required = true) @Valid @RequestBody DataWarehouseRequestVO dataWarehouseRequestVO) {
		return dataWarehouseService.createDataWarehouse(dataWarehouseRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Delete dataWarehouse for a given Id.", nickname = "delete", notes = "Delete dataWarehouse for a given identifier.", response = GenericMessage.class, tags = {
			"datawarehouses", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "DataWarehouse ID to be deleted", required = true) @PathVariable("id") String id) {
		return dataWarehouseService.deleteDataWarehouse(id);
	}

	@Override
	@ApiOperation(value = "Get all available dataWarehouse.", nickname = "getAll", notes = "Get all dataWarehouse. This endpoints will be used to get all valid available dataWarehouse records.", response = DataWarehouseCollection.class, tags = {
			"datawarehouses", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = DataWarehouseCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataWarehouseCollection> getAll() {
		return dataWarehouseService.getAllDataWarehouse();
	}

	@Override
	@ApiOperation(value = "Get dataWarehouse for a given id.", nickname = "getById", notes = "Get dataWarehouse for a given identifier. This endpoints will be used to get a dataWarehouse for a given identifier.", response = DataWarehouseInUseVO.class, tags = {
			"datawarehouses", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataWarehouseInUseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataWarehouseInUseVO> getById(
			@ApiParam(value = "dataWarehouse id to be fetched", required = true) @PathVariable("id") String id) {

		DataWarehouseInUseVO vo = dataWarehouseService.getById(id);
		if (vo != null) {
			LOGGER.debug("DataWarehouse {} fetched successfully", id);
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} else {
			LOGGER.debug("No dataWarehouse {} found", id);
			return new ResponseEntity<>(new DataWarehouseInUseVO(), HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Update existing dataWarehouse.", nickname = "update", notes = "Update an existing dataWarehouse.", response = DataWarehouseResponseVO.class, tags = {
			"datawarehouses", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = DataWarehouseResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<DataWarehouseResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for creating a new dataWarehouse", required = true) @Valid @RequestBody DataWarehouseRequestVO dataWarehouseRequestVO) {
		return dataWarehouseService.updateDataWarehouse(dataWarehouseRequestVO.getData());
	}

}
