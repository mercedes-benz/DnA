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

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.dataproduct.DataproductsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.dataproduct.DataProductCollection;
import com.daimler.data.dto.dataproduct.DataProductRequestVO;
import com.daimler.data.dto.dataproduct.DataProductResponseVO;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.service.dataproduct.DataProductService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "DataProduct API", tags = { "dataproducts" })
@RequestMapping("/api")
public class DataProductController implements DataproductsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(DataProductController.class);

	@Autowired
	private DataProductService dataProductService;

	@Override
	@ApiOperation(value = "Add a new dataproduct.", nickname = "create", notes = "Adds a new non existing dataproduct.", response = DataProductResponseVO.class, tags = {
			"dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success", response = DataProductResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<DataProductResponseVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new dataproduct", required = true) @Valid @RequestBody DataProductRequestVO dataProductRequestVO) {
		return dataProductService.createDataProduct(dataProductRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Delete dataProduct for a given Id.", nickname = "delete", notes = "Delete dataproduct for a given identifier.", response = GenericMessage.class, tags = {
			"dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "DataProduct ID to be deleted", required = true) @PathVariable("id") String id) {
		return dataProductService.deleteDataProduct(id);
	}

	@Override
	@ApiOperation(value = "Get all available dataproducts.", nickname = "getAll", notes = "Get all dataproducts. This endpoints will be used to get all valid available dataproduct records.", response = DataProductCollection.class, tags = {
			"dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataProductCollection> getAll(
			@ApiParam(value = "Filtering dataproducts based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "page number from which listing of dataproducts should start.") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of dataproducts.") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort dataproducts by a given variable.", allowableValues = "dataProductName") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort dataproducts based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		try {
			DataProductCollection dataProductCollection = new DataProductCollection();

			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			if (sortOrder != null && !sortOrder.equals("asc") && !sortOrder.equals("desc")) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
			if (sortOrder == null) {
				sortOrder = "asc";
			}

			Long count = dataProductService.getCount(published);
			if (count < offset)
				offset = 0;

			List<DataProductVO> dataProducts = dataProductService.getAllWithFilters(published, offset, limit, sortBy,
					sortOrder);
			LOGGER.info("DataProducts fetched successfully");
			if (!ObjectUtils.isEmpty(dataProducts)) {
				dataProductCollection.setTotalCount(count.intValue());
				dataProductCollection.setRecords(dataProducts);
				return new ResponseEntity<>(dataProductCollection, HttpStatus.OK);
			} else {
				dataProductCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(dataProductCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to fetch dataProducts with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	@ApiOperation(value = "Get dataProduct for a given Id.", nickname = "getById", notes = "Get dataproduct for a given identifier. This endpoints will be used to get a dataproduct for a given identifier.", response = DataProductVO.class, tags = {
			"dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataProductVO> getById(
			@ApiParam(value = "DataProduct ID to be fetched", required = true) @PathVariable("id") String id) {
		DataProductVO dataProductVO = dataProductService.getById(id);
		if (dataProductVO != null) {
			LOGGER.info("DataProduct with id {} fetched successfully", id);
			return new ResponseEntity<>(dataProductVO, HttpStatus.OK);
		} else {
			LOGGER.debug("No DataProduct {} found", id);
			return new ResponseEntity<>(new DataProductVO(), HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Update existing dataproduct.", nickname = "update", notes = "Update an existing dataproduct.", response = DataProductResponseVO.class, tags = {
			"dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success", response = DataProductResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<DataProductResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating dataproduct", required = true) @Valid @RequestBody DataProductRequestVO dataProductRequestVO) {
		return dataProductService.updateDataProduct(dataProductRequestVO.getData());
	}

}
