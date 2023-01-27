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
import java.util.Collections;
import java.util.Comparator;
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

import com.daimler.data.api.divisions.DivisionsApi;
import com.daimler.data.api.divisions.SubdivisionsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.divisions.DivisionCollection;
import com.daimler.data.dto.divisions.DivisionRequestVO;
import com.daimler.data.dto.divisions.DivisionResponseVO;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionCollection;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.service.division.DivisionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Division API", tags = { "divisions" })
@RequestMapping("/api")
public class DivisionController implements DivisionsApi, SubdivisionsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(DivisionController.class);

	@Autowired
	private DivisionService divisionService;

	@Override
	@ApiOperation(value = "Get all available divisions.", nickname = "getAll", notes = "Get all divisions. This endpoints will be used to Get all valid available divisions maintenance records.", response = DivisionCollection.class, tags = {
			"divisions", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all divisions", response = DivisionCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/divisions", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DivisionCollection> getAll(
			@ApiParam(value = "Ids of the division for which sub-divisions are to be fetched") @Valid @RequestParam(value = "ids", required = false) List<String> ids,
			@ApiParam(value = "Sort divisions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		try {
			DivisionCollection divisionCollection = new DivisionCollection();
			LOGGER.debug("Fetching Divisions for given Ids:{}", ids);
			List<DivisionVO> divisions = divisionService.getDivisionsByIds(ids);
			if (!ObjectUtils.isEmpty(divisions)) {
				if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
					divisions.sort(Comparator.comparing(DivisionVO :: getName, String.CASE_INSENSITIVE_ORDER));
				}
				if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
					divisions.sort(Comparator.comparing(DivisionVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
				}
				divisionCollection.addAll(divisions);
				return new ResponseEntity<>(divisionCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(divisionCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Exception Occured: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	@ApiOperation(value = "Get all available subdivisions for a given division id.", nickname = "getById", notes = "Get all subdivisions. This endpoints will be used to Get all valid available subdivisions for a given division-id maintenance records.", response = SubdivisionCollection.class, tags = {
			"subdivisions", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = SubdivisionCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subdivisions/{id}", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<SubdivisionCollection> getById(
			@ApiParam(value = "Id of the division for which sub-divisions are tobe fetched", required = true) @PathVariable("id") String id) {
		final List<SubdivisionVO> subdivisions = divisionService.getSubDivisionsById(id);
		SubdivisionCollection subdivisionCollection = new SubdivisionCollection();
		if (subdivisions != null && subdivisions.size() > 0) {
			subdivisionCollection.addAll(subdivisions);
			LOGGER.debug("Returning all available subdivisions");
			return new ResponseEntity<>(subdivisionCollection, HttpStatus.OK);
		} else {
			LOGGER.debug("No subdivisions found, returning empty");
			return new ResponseEntity<>(subdivisionCollection, HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Add a new division.", nickname = "create", notes = "Add a new non existing division which is used in providing solution.", response = DivisionResponseVO.class, tags = {
			"divisions", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = DivisionResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/divisions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<DivisionResponseVO> create(
			@ApiParam(value = "Request body that contains data required for creating a new division", required = true) @Valid @RequestBody DivisionRequestVO divisionRequestVO) {
		try {
			return divisionService.createDivision(divisionRequestVO);
		} catch (Exception e) {
			LOGGER.error("Failed to create new division with exception {} ", e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to create due to internal error. " + e.getMessage());
			messages.add(message);
			DivisionResponseVO response = new DivisionResponseVO();
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the division identified by given ID.", nickname = "delete", notes = "Deletes the division identified by given ID", response = GenericMessage.class, tags = {
			"divisions", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/divisions/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the division", required = true) @PathVariable("id") String id) {
		try {
			return divisionService.deleteDivision(id);
		} catch (Exception e) {
			LOGGER.error("Failed while delete division {} with exception {}", id, e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Update the division identified by given ID.", nickname = "update", notes = "Update the division identified by given ID", response = DivisionResponseVO.class, tags = {
			"divisions", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully updated.", response = DivisionResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/divisions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<DivisionResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating division.", required = true) @Valid @RequestBody DivisionRequestVO divisionRequestVO) {
		try {
			return divisionService.updateDivision(divisionRequestVO);
		} catch (Exception e) {
			LOGGER.error("Division with id {} cannot be edited. Failed due to internal error {} ",
					divisionRequestVO.getData().getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			DivisionResponseVO response = new DivisionResponseVO();
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
