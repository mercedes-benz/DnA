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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import com.daimler.data.dto.attachment.FileDetailsVO;
import com.daimler.data.dto.solution.*;
import com.daimler.data.service.attachment.AttachmentService;
import org.apache.commons.lang3.StringUtils;
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

import com.daimler.data.api.solution.ChangelogsApi;
import com.daimler.data.api.solution.MalwarescanApi;
import com.daimler.data.api.solution.SolutionsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.SolutionAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.userinfo.UserFavoriteUseCaseVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Solution API", tags = { "solutions" })
@RequestMapping("/api")
@Slf4j
@SuppressWarnings(value = "unused")
public class SolutionController implements SolutionsApi, ChangelogsApi, MalwarescanApi {

	private static Logger LOGGER = LoggerFactory.getLogger(SolutionController.class);

	@Autowired
	private SolutionService solutionService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private SolutionAssembler solutionAssembler;

	@Autowired
	private AttachmentService attachmentService;

	@Override
	@ApiOperation(value = "Adds a new solution.", nickname = "create", notes = "Adds a new non existing solution.", response = SolutionResponseVO.class, tags = {
			"solutions", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Solution added successfully", response = SolutionResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/solutions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<SolutionResponseVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new solution", required = true) @Valid @RequestBody SolutionRequestVO solutionRequestVO) {
		SolutionVO requestSolutionVO = solutionRequestVO.getData();
		SolutionResponseVO response = new SolutionResponseVO();
		try {
			String uniqueProductName = requestSolutionVO.getProductName();
			SolutionVO existingSolutionVO = solutionService.getByUniqueliteral("productName", uniqueProductName);
			if (existingSolutionVO != null && existingSolutionVO.getProductName() != null) {
				response.setData(existingSolutionVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Solution already exists.");
				messages.add(message);
				response.setErrors(messages);
				LOGGER.debug("Solution {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			}
			requestSolutionVO.setCreatedBy(this.userStore.getVO());
			requestSolutionVO.setCreatedDate(new Date());
			requestSolutionVO.setId(null);
			requestSolutionVO.setProductName(uniqueProductName);

			if (requestSolutionVO.isPublish() == null)
				requestSolutionVO.setPublish(false);

			// Adding change logs for Solution Digital Value
			// if(null!=requestSolutionVO.getDigitalValue() ||
			// null!=existingSolutionVO.getDigitalValue()) {
			// requestSolutionVO.setDigitalValue(solutionAssembler.digitalValueCompare(requestSolutionVO.getDigitalValue(),
			// null, userStore));
			// }

			// Calculating DIgital Value
			if (null != requestSolutionVO.getDigitalValue()) {
				ValueCalculatorVO valueCalculatorVO = solutionAssembler
						.valueCalculator(requestSolutionVO.getDigitalValue());
				requestSolutionVO.getDigitalValue().setValueCalculator(valueCalculatorVO);
			}

			SolutionVO solutionVO = solutionService.create(requestSolutionVO);
			if (solutionVO != null && solutionVO.getId() != null) {
				response.setData(solutionVO);
				LOGGER.debug("Solution {} created successfully", uniqueProductName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				response.setData(requestSolutionVO);
				response.setErrors(messages);
				LOGGER.error("Solution {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating solution {} ", e.getMessage(),
					requestSolutionVO.getProductName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			response.setData(requestSolutionVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<SolutionCollection> getAll(
			@ApiParam(value = "Filtering solutions based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of locations of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "location", required = false) String location,
			@ApiParam(value = "List of IDs of divisions and subdivisions under each division of solutions. Example [{1,[2,3]},{2,[1]},{3,[4,5]}]") @Valid @RequestParam(value = "division", required = false) String division,
			@ApiParam(value = "List of IDs of current phase of solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "phase", required = false) String phase,
			@ApiParam(value = "List of IDs of dataVolume of dataSources for solutions, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "dataVolume", required = false) String dataVolume,
			@ApiParam(value = "ID of current project status of solutions, Example 1") @Valid @RequestParam(value = "projectstatus", required = false) String projectstatus,
			@ApiParam(value = "ID of useCaseType of solutions. 1.MyBookmarks or 2.MySolutions , Example 1", allowableValues = "1, 2") @Valid @RequestParam(value = "useCaseType", required = false) String useCaseType,
			@ApiParam(value = "searchTerm to filter solutions. SearchTerm is comma seperated search keywords which are used to search Tags and ProductName of solutions. Example \"BAT, java\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "searchTerm to filter solutions. Example Java,R") @Valid @RequestParam(value = "tags", required = false) String tags,
			@ApiParam(value = "Filtering solutions based on digital value, values true or false", defaultValue = "false") @Valid @RequestParam(value = "hasDigitalValue", required = false, defaultValue = "false") Boolean hasDigitalValue,
			@ApiParam(value = "Filtering solutions based on notebook value, values true or false", defaultValue = "false") @Valid @RequestParam(value = "hasNotebook", required = false, defaultValue = "false") Boolean hasNotebook,
			@ApiParam(value = "page number from which listing of solutions should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of solutions, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort solutions by a given variable like name, phase, division, location or status") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort solutions based on the given order, example asc,desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		try {
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
			String[] phases = null;
			List<String> phasesList = new ArrayList<>();
			if (phase != null && !"".equals(phase))
				phases = phase.split(",");
			if (phases != null && phases.length > 0)
				phasesList = Arrays.asList(phases);

			String[] dataVolumes = null;
			List<String> dataVolumesList = new ArrayList<>();
			if (dataVolume != null && !"".equals(dataVolume))
				dataVolumes = dataVolume.split(",");
			if (dataVolumes != null && dataVolumes.length > 0)
				dataVolumesList = Arrays.asList(dataVolumes);

			List<Map<String, List<String>>> divisionsList = new ArrayList<Map<String, List<String>>>();
			if (!StringUtils.isEmpty(division)) {
				boolean hasEmptySubdivision = false;
				LOGGER.debug("Checking for EMPTY subdivision in query");
				if (division.contains(ConstantsUtility.EMPTY_VALUE)) {
					hasEmptySubdivision = true;
				}
				Map<String, List<String>> divisionMap = new HashMap<String, List<String>>();
				List<String> subdivisionsList = new ArrayList<String>();
				division = division.substring(1, division.length() - 1);
				String[] divisionSplit = division.split("},", 0);
				for (int i = 0; i < divisionSplit.length; i++) {
					divisionSplit[i] = divisionSplit[i].replaceAll("[\\{\\}\\[\\]]", "");
					String[] divSubdivSplitArray = divisionSplit[i].split(",");
					subdivisionsList = new ArrayList<String>();
					divisionMap = new HashMap<String, List<String>>();
					if (null != divSubdivSplitArray) {
						if (divSubdivSplitArray.length > 1) {
							for (int j = 1; j < divSubdivSplitArray.length; j++) {
								subdivisionsList.add(divSubdivSplitArray[j]);
							}
						}
						if (hasEmptySubdivision && !subdivisionsList.contains(ConstantsUtility.EMPTY_VALUE)) {
							LOGGER.debug("Appending EMPTY in subdivisionList");
							subdivisionsList.add(ConstantsUtility.EMPTY_VALUE);
						}
						divisionMap.put(divSubdivSplitArray[0], subdivisionsList);
					}
					divisionsList.add(divisionMap);
				}
			}

			String[] locations = null;
			List<String> locationsList = new ArrayList<>();
			if (location != null && !"".equals(location))
				locations = location.split(",");
			if (locations != null && locations.length > 0)
				locationsList = Arrays.asList(locations);

			String[] statuses = null;
			List<String> statusesList = new ArrayList<>();
			if (projectstatus != null && !"".equals(projectstatus))
				statuses = projectstatus.split(",");
			if (statuses != null && statuses.length > 0)
				statusesList = Arrays.asList(statuses);

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

			List<String> searchTerms = new ArrayList<>();
			if (searchTerm != null && !"".equalsIgnoreCase(searchTerm)) {
				searchTerms = Arrays.asList(searchTerm.split(","));
			}
			List<String> listOfTags = new ArrayList<>();
			if (tags != null && !"".equalsIgnoreCase(tags)) {
				listOfTags = Arrays.asList(tags.split(","));
			}
			Long count = solutionService.getCount(published, phasesList, dataVolumesList, division, locationsList,
					statusesList, useCaseType, userId, isAdmin, bookmarkedSolutions, searchTerms, listOfTags,
					divisionsAdmin, hasDigitalValue, hasNotebook);
			SolutionCollection solutionCollection = new SolutionCollection();

			if (count < offset)
				offset = 0;

			List<SolutionVO> solutionVOListVO = solutionService.getAllWithFilters(published, phasesList,
					dataVolumesList, division, locationsList, statusesList, useCaseType, userId, isAdmin,
					bookmarkedSolutions, searchTerms, listOfTags, divisionsAdmin, hasDigitalValue, hasNotebook, offset,
					limit, sortBy, sortOrder);
			LOGGER.debug("Solutions fetched successfully");
			if ("locations".equalsIgnoreCase(sortBy)) {
				List<SolutionVO> sortedSolutionVOList = this.sortSolutionsBasedOnLocations(solutionVOListVO, sortOrder);
				int fromIndex = offset > 0 ? (offset > count ? 0 : offset) : 0;
				int listSize = sortedSolutionVOList.size();
				if (limit == 0)
					limit = listSize;
				int limitAndFromIndexSum = fromIndex + limit;
				int toIndex = limitAndFromIndexSum > listSize ? listSize : limitAndFromIndexSum;
				solutionVOListVO = sortedSolutionVOList != null && !sortedSolutionVOList.isEmpty()
						? sortedSolutionVOList.subList(fromIndex, toIndex)
						: sortedSolutionVOList;
				LOGGER.debug("Sorted solutions fetched based on locations");
			}

			if (solutionVOListVO != null && solutionVOListVO.size() > 0) {
				solutionCollection = solutionAssembler.applyBookMarkflag(solutionVOListVO, bookmarkedSolutions, userId);
				if (!isAdmin)
					solutionCollection = solutionAssembler.maskDigitalValues(solutionVOListVO, userId, true);
				solutionCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(solutionCollection, HttpStatus.OK);
			} else {
				solutionCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(solutionCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch solutions with exception {} ", e.getMessage());
			throw e;
		}

	}

	private List<SolutionVO> sortSolutionsBasedOnLocations(List<SolutionVO> solutionVOListVO, String sortOrder) {
		if (solutionVOListVO != null && !solutionVOListVO.isEmpty()) {
			if ("asc".equalsIgnoreCase(sortOrder))
				solutionVOListVO.forEach(n -> n.getLocations()
						.sort(Comparator.comparing(SolutionLocationVO::getName, String.CASE_INSENSITIVE_ORDER)));
			else
				solutionVOListVO.forEach(n -> n.getLocations().sort(
						Comparator.comparing(SolutionLocationVO::getName, String.CASE_INSENSITIVE_ORDER).reversed()));
			Collections.sort(solutionVOListVO, new Comparator<SolutionVO>() {
				@Override
				public int compare(SolutionVO vo1, SolutionVO vo2) {
					String loc1 = toLocationsNameString(vo1.getLocations());
					String loc2 = toLocationsNameString(vo2.getLocations());
					if (loc1 == null) {
						if (loc2 == null)
							return 0;
						else
							return "asc".equalsIgnoreCase(sortOrder) ? -1 : 1;
					} else {
						if (loc2 == null)
							return "asc".equalsIgnoreCase(sortOrder) ? 1 : -1;
						else {
							int value = loc1.compareToIgnoreCase(loc2);
							return "asc".equalsIgnoreCase(sortOrder) ? value : -1 * value;
						}
					}
				}
			});
		}
		return solutionVOListVO;
	}

	private String toLocationsNameString(List<SolutionLocationVO> locations) {
		String names = "";
		if (locations != null && !locations.isEmpty()) {
			names = locations.stream().map(n -> n.getName()).collect(Collectors.joining(","));
		}
		return names;
	}

	@Override
	public ResponseEntity<SolutionVO> getById(String id) {
		Boolean isAdmin = false;
		Boolean isDivisionAdmin = false;
		List<String> divisionAdmins = new ArrayList<>();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		if (userId != null && !"".equalsIgnoreCase(userId)) {
			UserInfoVO userInfoVO = userInfoService.getById(userId);
			if (userInfoVO != null) {
				divisionAdmins = userInfoVO.getDivisionAdmins();
				List<UserRoleVO> userRoles = userInfoVO.getRoles();
				if (userRoles != null && !userRoles.isEmpty()) {
					// To check for Admin
					isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
					// To check for divisionAdmin
					isDivisionAdmin = userRoles.stream().anyMatch(n -> "DivisionAdmin".equals(n.getName()));
				}
			}
		}
		SolutionVO solutionVO = solutionService.getById(id);
		if (solutionVO != null) {
			// To check if divisionAdmin has access of solution division
			isDivisionAdmin = isDivisionAdmin && !ObjectUtils.isEmpty(divisionAdmins)
					&& divisionAdmins.contains(solutionVO.getDivision().getName());
			if (Boolean.FALSE.equals(isAdmin) && Boolean.FALSE.equals(isDivisionAdmin)) {
				solutionVO = solutionAssembler.maskDigitalValue(solutionVO, userId, false);
			}
			LOGGER.debug("Solution {} fetched successfully", id);
			return new ResponseEntity<>(solutionVO, HttpStatus.OK);
		} else {
			LOGGER.debug("No solution {} found", id);
			return new ResponseEntity<>(new SolutionVO(), HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Get all published solutions.", nickname = "getNumberOfPublishedSolutions", notes = "Get published solutions. This endpoints will be used to get number of published available solutions records.", response = TransparencyVO.class, tags = {
			"solutions", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/solutions/transparency", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfPublishedSolutions() {
		try {
			TransparencyVO transparencyVO = new TransparencyVO();
			Integer count =solutionService.getCountBasedPublishSolution(true);
			transparencyVO.setCount(count);
			log.debug("Returning solution count successfully");
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e) {
			log.error("Failed while fetching solution count with exception {}", e.getMessage());
			return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "update existing solution.", nickname = "update", notes = "update existing solution.", response = SolutionResponseVO.class, tags = {
			"solutions", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Solution updated successfully", response = SolutionResponseVO.class),
			@ApiResponse(code = 204, message = "Solution not found", response = SolutionResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/solutions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<SolutionResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating an existing solution", required = true) @Valid @RequestBody SolutionRequestVO solutionRequestVO) {
		SolutionVO requestSolutionVO = solutionRequestVO.getData();
		SolutionResponseVO response = new SolutionResponseVO();
		try {
			String id = requestSolutionVO.getId();
			List<MessageDescription> notFoundmessages = new ArrayList<>();
			MessageDescription notFoundmessage = new MessageDescription();
			notFoundmessage.setMessage("No Solution found for given id. Update cannot happen");
			notFoundmessages.add(notFoundmessage);
			response.setErrors(notFoundmessages);
			ResponseEntity<SolutionResponseVO> notFoundResponse = new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
			if (id == null || id.isEmpty() || id.trim().isEmpty()) {
				LOGGER.debug("No solution found for given id {} , updated cannot happen.", id);
				return notFoundResponse;
			}
			SolutionVO existingSolutionVO = solutionService.getById(id);
			SolutionVO mergedsolutionVO = null;
			if (requestSolutionVO.isPublish() == null)
				requestSolutionVO.setPublish(false);
			if (existingSolutionVO != null && existingSolutionVO.getId() != null) {
				// FIX for createby issue. This should have been sent from the client but
				// missing.
				CreatedByVO createdBy = existingSolutionVO.getCreatedBy();
				String creatorId = "";
				if (createdBy != null)
					creatorId = createdBy.getId();
				CreatedByVO currentUser = this.userStore.getVO();
				String userId = currentUser != null ? currentUser.getId() : "";
				if (userId != null && !"".equalsIgnoreCase(userId)) {
					UserInfoVO userInfoVO = userInfoService.getById(userId);
					if (userInfoVO != null) {
						List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
						if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
							boolean isDivisionAdmin = userRoleVOs.stream()
									.anyMatch(n -> "DivisionAdmin".equalsIgnoreCase(n.getName()))
									&& !ObjectUtils.isEmpty(userInfoVO.getDivisionAdmins()) && userInfoVO
											.getDivisionAdmins().contains(existingSolutionVO.getDivision().getName());
							boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
							boolean isTeamMember = existingSolutionVO.getTeam().stream()
									.anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()));
							if (userId == null || (!userId.equalsIgnoreCase(creatorId) && !isAdmin && !isDivisionAdmin)
									&& !isTeamMember) {
								List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
								MessageDescription notAuthorizedMsg = new MessageDescription();
								notAuthorizedMsg.setMessage(
										"Not authorized to edit solution. Only user who created the solution or with admin role can edit.");
								notAuthorizedMsgs.add(notAuthorizedMsg);
								response.setErrors(notAuthorizedMsgs);
								LOGGER.debug("Solution {} cannot be edited. User {} not authorized", id, userId);
								ResponseEntity<SolutionResponseVO> notAuthorizedResponse = new ResponseEntity<>(
										response, HttpStatus.FORBIDDEN);
								return notAuthorizedResponse;
							}
						}
					}
				}
				requestSolutionVO.setCreatedBy(createdBy);
				requestSolutionVO.setCreatedDate(existingSolutionVO.getCreatedDate());
				requestSolutionVO.lastModifiedDate(new Date());
				// Apply closeDate if status of the solution is changed from Active to Closed
				if (requestSolutionVO.getProjectStatus().getName().equals("Active")
						|| requestSolutionVO.getProjectStatus().getName().equals("On hold")) {
					requestSolutionVO.setCloseDate(null);
				} else if (requestSolutionVO.getProjectStatus().getName().equals("Closed")) {
					SolutionProjectStatusVO currentProjectStatus = existingSolutionVO.getProjectStatus();
					if (currentProjectStatus != null && (currentProjectStatus.getName().equals("Active")
							|| currentProjectStatus.getName().equals("On hold"))) {
						requestSolutionVO.setCloseDate(new Date());
					} else {
						requestSolutionVO.setCloseDate(existingSolutionVO.getCloseDate());
					}
				}

				// Adding change logs for Solution Digital Value
				Gson gson = new Gson();
				if (null != existingSolutionVO.getDigitalValue()
						&& !gson.toJson(existingSolutionVO.getDigitalValue())
								.equals(ConstantsUtility.SOLUTIONDIGITALVALUEVO_EMPTY)
						&& !gson.toJson(existingSolutionVO.getDigitalValue())
								.equals(ConstantsUtility.SOLUTIONDIGITALVALUEVO_EMPTY_1)) {
					requestSolutionVO.setDigitalValue(solutionAssembler.digitalValueCompare(
							requestSolutionVO.getDigitalValue(), existingSolutionVO.getDigitalValue(), currentUser));
				}

				// Calculating DIgital Value
				if (null != requestSolutionVO.getDigitalValue()) {
					SolutionDigitalValueVO digitalValueRequest = new SolutionDigitalValueVO();
					digitalValueRequest = SolutionAssembler.cloneDigitalValueVO(requestSolutionVO.getDigitalValue());
					ValueCalculatorVO valueCalculatorVO = solutionAssembler.valueCalculator(digitalValueRequest);
					requestSolutionVO.getDigitalValue().setValueCalculator(valueCalculatorVO);
				}

				mergedsolutionVO = solutionService.create(requestSolutionVO);
				if (mergedsolutionVO != null && mergedsolutionVO.getId() != null) {
					response.setData(mergedsolutionVO);
					response.setErrors(null);
					return new ResponseEntity<>(response, HttpStatus.OK);
				} else {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to update due to internal error");
					messages.add(message);
					response.setData(requestSolutionVO);
					response.setErrors(messages);
					LOGGER.debug("Solution {} cannot be edited. Failed with unknown internal error", id);
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				LOGGER.debug("No Solution {} found to edit.", id);
				return notFoundResponse;
			}
		} catch (Exception e) {
			LOGGER.error("Solution {} cannot be edited. Failed due to internal error {} ", requestSolutionVO.getId(),
					e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			response.setData(requestSolutionVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Delete Solution for a given Id.", nickname = "delete", notes = "Delete solution for a given identifier.", response = GenericMessage.class, tags = {
			"solutions", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/solutions/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Solution ID to be deleted", required = true) @PathVariable("id") String id) {
		try {
			SolutionVO solution = null;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				solution = solutionService.getById(id);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isDivisionAdmin = userRoleVOs.stream()
								.anyMatch(n -> "DivisionAdmin".equalsIgnoreCase(n.getName()))
								&& !ObjectUtils.isEmpty(userInfoVO.getDivisionAdmins())
								&& userInfoVO.getDivisionAdmins().contains(solution.getDivision().getName());
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						String createdBy = solution.getCreatedBy() != null ? solution.getCreatedBy().getId() : null;
						boolean isOwner = (createdBy != null && createdBy.equals(userId));
						boolean isTeamMember = solution.getTeam().stream()
								.anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()));
						if (!isAdmin && !isOwner && !isTeamMember && !isDivisionAdmin) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to delete solution. Only the solution owner or an admin can delete the solution.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							// log.error(notAuthorizedMsg.getMessage());
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			List<FileDetailsVO> files = solution.getAttachments();
			    String productName =solution.getProductName();
			String keyName = null;
				if (files != null) {
					for (FileDetailsVO file : files) {
						keyName = file.getId();
						String fileName = file.getFileName();
						try {
							attachmentService.deleteFileFromS3Bucket(keyName);

						} catch (Exception e) {
							log.error("File {} is failed to delete from solution {} with an exception {}",fileName,productName, e.getMessage());
						}
					}
				}

			solutionService.deleteById(id);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			LOGGER.info("Solution {} deleted successfully", id);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			MessageDescription invalidMsg = new MessageDescription("No Solution with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			LOGGER.error("No Solution with the given id {} , couldnt delete.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete solution with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * getChangeLogsById fetch change logs for solution of given id
	 * 
	 * @param id
	 * @return SolutionChangeLogsCollectionVO
	 */
	@Override
	public ResponseEntity<SolutionChangeLogCollectionVO> getChangeLogsBySolutionId(String id) {
		List<ChangeLogVO> changeLogsVO = solutionService.getChangeLogsBySolutionId(id);
		if (changeLogsVO != null && !changeLogsVO.isEmpty()) {
			SolutionChangeLogCollectionVO solutionChangeLogCollectionVO = new SolutionChangeLogCollectionVO();
			solutionChangeLogCollectionVO.setData(changeLogsVO);
			return new ResponseEntity<>(solutionChangeLogCollectionVO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new SolutionChangeLogCollectionVO(), HttpStatus.NO_CONTENT);
		}

	}

	@Override
	@ApiOperation(value = "Unsubscribe Malware Scan for a given solutionId", nickname = "unsubscribeMalwareScan", notes = "Unsubscribe Malware Scan for a given solutionId", response = GenericMessage.class, tags = {
			"solutions", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/malwarescan/unsubscribe/{solutionId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<GenericMessage> unsubscribeMalwareScan(
			@ApiParam(value = "Solution ID", required = true) @PathVariable("solutionId") String solutionId) {
		return solutionService.malwareScanUnsubscribe(solutionId);
	}

}
