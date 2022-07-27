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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.entityid.EntityidsApi;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.entityid.EntityIdCollection;
import com.daimler.data.dto.entityid.EntityIdVO;
import com.daimler.data.service.entityid.EntityIdService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "entityids API", tags = { "entityids" })
@RequestMapping("/api")
public class EntityIdController implements EntityidsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(EntityIdController.class);

	@Autowired
	private EntityIdService entityIdService;

	@Override
	@ApiOperation(value = "Get all available entityIds", nickname = "getAll", notes = "Get all available entityIds", response = EntityIdCollection.class, tags = {
			"entityids", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = EntityIdCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/entityids", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<EntityIdCollection> getAll(
			@ApiParam(value = "page number from which listing of records should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of records. Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort records by a given variable like entityId, entityName", allowableValues = "entityId, entityName") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort records based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		try {
			EntityIdCollection entityIdCollection = new EntityIdCollection();

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

			Long count = entityIdService.getCount();
			if (count < offset)
				offset = 0;

			List<EntityIdVO> entityIdList = null;
			if (sortOrder.equals("asc")) {
				entityIdList = entityIdService.getAllSortedByUniqueLiteral(limit, offset, sortBy,
						CommonDataRepositoryImpl.SORT_TYPE.ASC);
			} else if (sortOrder.equals(("desc"))) {
				entityIdList = entityIdService.getAllSortedByUniqueLiteral(limit, offset, sortBy,
						CommonDataRepositoryImpl.SORT_TYPE.DESC);
			}

			if (!ObjectUtils.isEmpty(entityIdList)) {
				entityIdCollection.setTotalCount(count.intValue());
				entityIdCollection.setRecords(entityIdList);
				LOGGER.info("Returning all entityId details successfully");
				return new ResponseEntity<>(entityIdCollection, HttpStatus.OK);
			} else {
				LOGGER.info("No entityId details found");
				return new ResponseEntity<>(entityIdCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to fetch records with exception {} ", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
