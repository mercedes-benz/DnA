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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.dashboard.DashboardApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DashboardAssembler;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.dashboard.DatasourceWidgetVO;
import com.daimler.data.dto.dashboard.LocationWidgetVO;
import com.daimler.data.dto.dashboard.MilestoneWidgetVO;
import com.daimler.data.dto.dashboard.SolCountWidgetResponseVO;
import com.daimler.data.dto.dashboard.SolDSWidgetResponseVO;
import com.daimler.data.dto.dashboard.SolDigitalValueWidgetResponseVO;
import com.daimler.data.dto.dashboard.SolDigitalValuesummaryResponseVO;
import com.daimler.data.dto.dashboard.SolDigitalValuesummaryVO;
import com.daimler.data.dto.dashboard.SolLocWidgetResponseVO;
import com.daimler.data.dto.dashboard.SolMilestoneWidgetResponseVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserFavoriteUseCaseVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.dashboard.DashboardService;
import com.daimler.data.service.userinfo.UserInfoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Dashboard API", tags = { "dashboard" })
@RequestMapping("/api")
public class DashboardController implements DashboardApi {
	private static Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private DashboardAssembler assembler;

	@Autowired
	private DashboardService dashboardService;

	@Override
	@ApiOperation(value = "Get datasources.", nickname = "getDatasources", notes = "Get datasources of solution with given filter.", response = SolDSWidgetResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolDSWidgetResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error.") })
	@RequestMapping(value = "/dashboard/datasources", method = RequestMethod.GET)
	public ResponseEntity<SolDSWidgetResponseVO> getDatasources(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}
			List<DatasourceWidgetVO> datasources = dashboardService.getSolDatasource(published, assembler.toList(phase),
					assembler.toList(dataVolume), division, assembler.toList(location), assembler.toList(projectstatus),
					useCaseType, userId, isAdmin, bookmarkedSolutions, assembler.toList(searchTerm),
					assembler.toList(tags), divisionsAdmin);

			SolDSWidgetResponseVO resVO = new SolDSWidgetResponseVO();
			resVO.setDataSources(datasources);
			return new ResponseEntity<>(resVO, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolDSWidgetResponseVO resVO = new SolDSWidgetResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get locations.", nickname = "getLocations", notes = "Get locations of solution with given filter.", response = SolLocWidgetResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolLocWidgetResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error.") })
	@RequestMapping(value = "/dashboard/locations", method = RequestMethod.GET)
	public ResponseEntity<SolLocWidgetResponseVO> getLocations(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}
			List<LocationWidgetVO> locations = dashboardService.getSolLocation(published, assembler.toList(phase),
					assembler.toList(dataVolume), division, assembler.toList(location), assembler.toList(projectstatus),
					useCaseType, userId, isAdmin, bookmarkedSolutions, assembler.toList(searchTerm),
					assembler.toList(tags), divisionsAdmin);

			SolLocWidgetResponseVO resVO = new SolLocWidgetResponseVO();
			resVO.setLocations(locations);
			return new ResponseEntity<>(resVO, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolLocWidgetResponseVO resVO = new SolLocWidgetResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get milestones.", nickname = "getMilestones", notes = "Get milestones of solution with given filter.", response = SolMilestoneWidgetResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolMilestoneWidgetResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dashboard/milestones", method = RequestMethod.GET)
	public ResponseEntity<SolMilestoneWidgetResponseVO> getMilestones(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {

			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}
			List<MilestoneWidgetVO> milestones = dashboardService.getSolMilestone(published, assembler.toList(phase),
					assembler.toList(dataVolume), division, assembler.toList(location), assembler.toList(projectstatus),
					useCaseType, userId, isAdmin, bookmarkedSolutions, assembler.toList(searchTerm),
					assembler.toList(tags), divisionsAdmin);

			SolMilestoneWidgetResponseVO resVO = new SolMilestoneWidgetResponseVO();
			resVO.setMilestones(milestones);
			return new ResponseEntity<>(resVO, HttpStatus.OK);
		} catch (Exception e) {
			
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolMilestoneWidgetResponseVO resVO = new SolMilestoneWidgetResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get count of solutions using DnA Notebook.", nickname = "getNotebookSolutionCount", notes = "Get count of solutions using DnA Notebook with given filter.", response = SolCountWidgetResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolCountWidgetResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dashboard/notebook/solutioncount", method = RequestMethod.GET)
	public ResponseEntity<SolCountWidgetResponseVO> getNotebookSolutionCount(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}
			Long totalCount = dashboardService.getSolCountWithNotebook(published, assembler.toList(phase),
					assembler.toList(dataVolume), division, assembler.toList(location), assembler.toList(projectstatus),
					useCaseType, userId, isAdmin, bookmarkedSolutions, assembler.toList(searchTerm),
					assembler.toList(tags), divisionsAdmin);

			SolCountWidgetResponseVO resVO = new SolCountWidgetResponseVO();
			resVO.setTotalCount(totalCount.intValue());
			return new ResponseEntity<>(resVO, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolCountWidgetResponseVO resVO = new SolCountWidgetResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get count of solutions.", nickname = "getSolutionCount", notes = "Get count of solutions using DnA Notebook with given filter.", response = SolCountWidgetResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolCountWidgetResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dashboard/solutioncount", method = RequestMethod.GET)
	public ResponseEntity<SolCountWidgetResponseVO> getSolutionCount(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
						
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}

			Long totalCount = dashboardService.getSolCount(published, assembler.toList(phase),
					assembler.toList(dataVolume), division, assembler.toList(location), assembler.toList(projectstatus),
					useCaseType, userId, isAdmin, bookmarkedSolutions, assembler.toList(searchTerm),
					assembler.toList(tags), divisionsAdmin);
			SolCountWidgetResponseVO resVO = new SolCountWidgetResponseVO();
			resVO.setTotalCount(totalCount.intValue());
			return new ResponseEntity<>(resVO, HttpStatus.OK);

		} catch (Exception e) {
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolCountWidgetResponseVO resVO = new SolCountWidgetResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get Digital values.", nickname = "getDigitalValueDetails", notes = "Get Digital Value details of solution with given filter.", response = SolDigitalValueWidgetResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolDigitalValueWidgetResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error.") })
	@RequestMapping(value = "/dashboard/digitalvalue", method = RequestMethod.GET)
	public ResponseEntity<SolDigitalValueWidgetResponseVO> getDigitalValueDetails(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}

			BigDecimal totalDigitalValue = dashboardService.getSolDigitalValue(published, assembler.toList(phase),
					assembler.toList(dataVolume), division, assembler.toList(location), assembler.toList(projectstatus),
					useCaseType, userId, isAdmin, bookmarkedSolutions, assembler.toList(searchTerm),
					assembler.toList(tags), divisionsAdmin);
			SolDigitalValueWidgetResponseVO resVO = new SolDigitalValueWidgetResponseVO();
			resVO.setTotalDigitalValue(totalDigitalValue);
			return new ResponseEntity<>(resVO, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolDigitalValueWidgetResponseVO resVO = new SolDigitalValueWidgetResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get summary of Digital values.", nickname = "getDigitalValuesummary", notes = "Get Digital Value summary of solution with given filter.", response = SolDigitalValuesummaryResponseVO.class, tags = {
			"dashboard", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure.", response = SolDigitalValuesummaryResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error.") })
	@RequestMapping(value = "/dashboard/digitalvaluesummary", method = RequestMethod.GET)
	public ResponseEntity<SolDigitalValuesummaryResponseVO> getDigitalValuesummary(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "tags to filter solutions. tags is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "tags", required = false) String tags) {
		try {
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			List<String> bookmarkedSolutions = new ArrayList<>();
			List<String> divisionsAdmin = new ArrayList<>();
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty()) {
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
						divisionsAdmin = userInfoVO.getDivisionAdmins();
					}
					List<UserFavoriteUseCaseVO> favSolutions = userInfoVO.getFavoriteUsecases();
					if (favSolutions != null && !favSolutions.isEmpty())
						bookmarkedSolutions = favSolutions.stream().map(n -> n.getUsecaseId())
								.collect(Collectors.toList());
				}
			}

			List<SolDigitalValuesummaryVO> solDigitalValuesummaryVO = dashboardService.getSolDigitalValueSummary(
					published, assembler.toList(phase), assembler.toList(dataVolume), division,
					assembler.toList(location), assembler.toList(projectstatus), useCaseType, userId, isAdmin,
					bookmarkedSolutions, assembler.toList(searchTerm), assembler.toList(tags), divisionsAdmin);
			SolDigitalValuesummaryResponseVO resVO = new SolDigitalValuesummaryResponseVO();
			resVO.setSolDigitalValuesummary(solDigitalValuesummaryVO);
			return new ResponseEntity<>(resVO, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Internal server error occured::{}", e.getMessage());
			SolDigitalValuesummaryResponseVO resVO = new SolDigitalValuesummaryResponseVO();
			List<MessageDescription> errors = Arrays.asList(new MessageDescription(e.getMessage()));
			resVO.setErrors(errors);
			return new ResponseEntity<>(resVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
