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

import com.daimler.data.dto.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.report.ReportsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.ReportAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.service.report.ReportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Report API", tags = { "reports" })
@RequestMapping("/api")
public class ReportController implements ReportsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private ReportAssembler reportAssembler;

	@Override
	@ApiOperation(value = "Add a new report.", nickname = "create", notes = "Adds a new non existing report.", response = ReportResponseVO.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = ReportResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ReportResponseVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new report", required = true) @Valid @RequestBody ReportRequestVO reportRequestVO) {
		return reportService.createReport(reportRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Update existing report.", nickname = "update", notes = "Update an existing report.", response = ReportResponseVO.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes", response = ReportResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<ReportResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for creating a new report", required = true) @Valid @RequestBody ReportRequestVO reportRequestVO) {
		return reportService.updateReport(reportRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Delete Report for a given Id.", nickname = "delete", notes = "Delete report for a given identifier.", response = GenericMessage.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Report ID to be deleted", required = true) @PathVariable("id") String id) {
		return reportService.deleteReport(id);
	}

	@Override
	@ApiOperation(value = "Get all available reports.", nickname = "getAll", notes = "Get all reports. This endpoints will be used to get all valid available report records.", response = ReportCollection.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ReportCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ReportCollection> getAll(
			@ApiParam(value = "Filtering reports based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "Project status of reports") @Valid @RequestParam(value = "status", required = false) String status,
			@ApiParam(value = "searchTerm to filter reports. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of reports. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter reports. tags is comma seperated search keywords which are used to search Tags of reports. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags,
			@ApiParam(value = "page number from which listing of reports should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of reports, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort reports by a given variable like productName, status", allowableValues = "productName, status, department, art") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort reports based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of reports. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of deparments. Example dep1,dep2,dep3") @Valid @RequestParam(value = "department", required = false) String department,
			@ApiParam(value = "List of processOwner. Example shortID1,shortId2") @Valid @RequestParam(value = "processOwner", required = false) String processOwner,
			@ApiParam(value = "List of art. Example art1,art2,art3") @Valid @RequestParam(value = "art", required = false) String art) {
		try {
			ReportCollection reportCollection = new ReportCollection();

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

			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			if (StringUtils.hasText(userId)) {
				isAdmin = this.userStore.getUserInfo().hasAdminAccess();
			}
			Long count = reportService.getCount(published, reportAssembler.toList(status), userId, isAdmin,
					reportAssembler.toList(searchTerm), reportAssembler.toList(tags), division,
					reportAssembler.toList(department), reportAssembler.toList(processOwner),
					reportAssembler.toList(art));
			if (count < offset)
				offset = 0;

			List<ReportVO> reports = reportService.getAllWithFilters(published, reportAssembler.toList(status), userId,
					isAdmin, reportAssembler.toList(searchTerm), reportAssembler.toList(tags), offset, limit, sortBy,
					sortOrder, division, reportAssembler.toList(department), reportAssembler.toList(processOwner),
					reportAssembler.toList(art));
			LOGGER.debug("Reports fetched successfully");
			if (!ObjectUtils.isEmpty(reports)) {
				reportCollection.setTotalCount(count.intValue());
				reportCollection.setRecords(reports);
				return new ResponseEntity<>(reportCollection, HttpStatus.OK);
			} else {
				reportCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(reportCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to fetch reports with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	@ApiOperation(value = "Get Report for a given Id.", nickname = "getById", notes = "Get report for a given identifier. This endpoints will be used to get a report for a given identifier.", response = ReportVO.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = ReportVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ReportVO> getById(
			@ApiParam(value = "Report ID to be fetched", required = true) @PathVariable("id") String id) {
		ReportVO reportVO = reportService.getById(id);
		if (reportVO != null) {
			LOGGER.debug("Report {} fetched successfully", id);
			return new ResponseEntity<>(reportVO, HttpStatus.OK);
		} else {
			LOGGER.debug("No Report {} found", id);
			return new ResponseEntity<>(new ReportVO(), HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Get number of published reports.", nickname = "getNumberOfPublishedReports", notes = "Get published reports. This endpoints will be used to get number of published available report records.", response = TransparencyVO.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports/transparency", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfPublishedReports() {
		try {
			Integer count = reportService.getCountBasedPublishReport(true);
			TransparencyVO transparencyVO = new TransparencyVO();
			transparencyVO.setCount(count);
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e){
			return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available processOwners.", nickname = "getAllProcessOwner", notes = "Get all processOwners. This endpoints will be used to get all valid available processOwners records.", response = ProcessOwnerCollection.class, tags = {
			"reports", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ProcessOwnerCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reports/processowners", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ProcessOwnerCollection> getAllProcessOwner() {
		return reportService.getProcessOwners();
	}

}
