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

import com.daimler.data.api.datacompliance.DatacomplianceApi;
import com.daimler.data.assembler.DataComplianceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.datacompliance.DataComplianceCollection;
import com.daimler.data.dto.datacompliance.DataComplianceRequestVO;
import com.daimler.data.dto.datacompliance.DataComplianceResponseVO;
import com.daimler.data.dto.datacompliance.DataComplianceVO;
import com.daimler.data.service.datacompliance.DataComplianceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "datacompliance API", tags = { "datacompliance" })
@RequestMapping("/api")
public class DataComplianceController implements DatacomplianceApi {

	private static Logger LOGGER = LoggerFactory.getLogger(DataComplianceController.class);

	@Autowired
	private DataComplianceService dataComplianceService;

	@Autowired
	private DataComplianceAssembler dataComplianceAssembler;

	@Override
	@ApiOperation(value = "Add a new datacompliance record.", nickname = "create", notes = "Adds a new non existing datacompliance record.", response = DataComplianceResponseVO.class, tags = {
			"datacompliance", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = DataComplianceResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datacompliance", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<DataComplianceResponseVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new record", required = true) @Valid @RequestBody DataComplianceRequestVO dataComplianceRequestVO) {
		return dataComplianceService.createDataCompliance(dataComplianceRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Delete datacompliance record for a given Id.", nickname = "delete", notes = "Delete datacompliance record for a given identifier.", response = GenericMessage.class, tags = {
			"datacompliance", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datacompliance/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "ID to be deleted", required = true) @PathVariable("id") String id) {
		return dataComplianceService.deleteDataCompliance(id);
	}

	@Override
	@ApiOperation(value = "Get all available datacompliance network list.", nickname = "getAll", notes = "Get all available datacompliance network list.", response = DataComplianceCollection.class, tags = {
			"datacompliance", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataComplianceCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datacompliance", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataComplianceCollection> getAll(
			@ApiParam(value = "Filtering data compliance details based on entityId.") @Valid @RequestParam(value = "entityId", required = false) String entityId,
			@ApiParam(value = "Filtering data compliance details based on entityName") @Valid @RequestParam(value = "entityName", required = false) String entityName,
			@ApiParam(value = "Filtering data compliance details based on entityCountry") @Valid @RequestParam(value = "entityCountry", required = false) String entityCountry,
			@ApiParam(value = "Filter using localComplianceOfficer. localComplianceOfficer is comma seperated search keywords.") @Valid @RequestParam(value = "localComplianceOfficer", required = false) String localComplianceOfficer,
			@ApiParam(value = "Filter using localComplianceResponsible. localComplianceResponsible is comma seperated search keywords.") @Valid @RequestParam(value = "localComplianceResponsible", required = false) String localComplianceResponsible,
			@ApiParam(value = "Filter using localComplianceSpecialist. localComplianceSpecialist is comma seperated search keywords.") @Valid @RequestParam(value = "localComplianceSpecialist", required = false) String localComplianceSpecialist,
			@ApiParam(value = "page number from which listing of records should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of records. Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort records by a given variable like entityId, entityName, entityCountry, localComplianceOfficer, etc", allowableValues = "entityId, entityName, entityCountry, localComplianceOfficer, localComplianceResponsible, localComplianceSpecialist") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort records based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		try {
			DataComplianceCollection dataComplianceCollection = new DataComplianceCollection();

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

			Long count = dataComplianceService.getCount(entityId, entityName, entityCountry,
					dataComplianceAssembler.toList(localComplianceOfficer),
					dataComplianceAssembler.toList(localComplianceResponsible),
					dataComplianceAssembler.toList(localComplianceSpecialist));
			if (count < offset)
				offset = 0;

			List<DataComplianceVO> dataCompliances = dataComplianceService.getAllWithFilters(entityId, entityName, entityCountry,
					dataComplianceAssembler.toList(localComplianceOfficer),
					dataComplianceAssembler.toList(localComplianceResponsible),
					dataComplianceAssembler.toList(localComplianceSpecialist), offset, limit, sortBy, sortOrder);
			LOGGER.info("DataCompliances entry fetched successfully");
			if (!ObjectUtils.isEmpty(dataCompliances)) {
				dataComplianceCollection.setTotalCount(count.intValue());
				dataComplianceCollection.setRecords(dataCompliances);
				return new ResponseEntity<>(dataComplianceCollection, HttpStatus.OK);
			} else {
				dataComplianceCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(dataComplianceCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to fetch records with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	@ApiOperation(value = "Update existing datacompliance record.", nickname = "update", notes = "Update an existing datacompliance record.", response = DataComplianceResponseVO.class, tags = {
			"datacompliance", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = DataComplianceResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datacompliance", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<DataComplianceResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating data compliance record", required = true) @Valid @RequestBody DataComplianceRequestVO dataComplianceRequestVO) {
		return dataComplianceService.updateDataCompliance(dataComplianceRequestVO.getData());
	}

}
