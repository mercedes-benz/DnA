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

import com.daimler.data.adapter.hasura.HasuraClient;
import com.daimler.data.adapter.hasura.HasuraUserInfoInsertGenericResponse;
import com.daimler.data.api.userinfo.UsersApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.client.teamsApi.TeamsApiClient;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.solution.SolutionCollectionResponseVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.solution.TransparencyVO;
import com.daimler.data.dto.userinfo.*;
import com.daimler.data.service.userinfo.UserInfoService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@Api(value = "UserInfo API", tags = { "users" })
@RequestMapping("/api")
@Slf4j
public class UserInfoController implements UsersApi {

	private static Logger logger = LoggerFactory.getLogger(UserInfoController.class);
	
	@Autowired
	private UserInfoService userInfoService;
	
	@Autowired
	private UserInfoAssembler userinfoAssembler;

	@Autowired
	private UserStore userStore;

	@Autowired
	private TeamsApiClient teamsApiClient;

	@Value("${teamsApi.enabled}")
	private boolean teamsApiEnabled;
  
	@Autowired
	private HasuraClient hasuraClient;
	
	@Value("${dna.user.techUserPrefix}")
	private String techUserPrefix;
	
	@Value("${dna.feature.technicalUserOnboarding}")
	private boolean isTechUserOnboarding;


	@Override
	@ApiOperation(value = "Get all available users.", nickname = "getAll", notes = "Get all users. This endpoints will be used to Get all valid available user maintenance records.", response = UsersCollection.class, tags = {
			"users", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = UsersCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/users", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<UsersCollection> getAll(
			@ApiParam(value = "searchTerm to filter users. SearchTerm is a keywords which are used to search userId,name and email of users. Example \"DEMOUSER, DEMO, demouser@email\"") @Valid @RequestParam(value = "searchTerm", required = false) String searchTerm,
			@ApiParam(value = "page size to limit the number of solutions, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "page number from which listing of solutions should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "Sort users based on given column, example name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort users based on given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		  List<UserInfoVO> usersInfo;
		  UsersCollection usersCollection = new UsersCollection();
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
			if (teamsApiEnabled && StringUtils.hasText(searchTerm)) {
				logger.info("Fetching user information with given identifier from TeamsApi.");					
					usersCollection = teamsApiClient.getTeamsApiUserInfoDetails(searchTerm,offset);
				if (!ObjectUtils.isEmpty(usersCollection) && usersCollection != null && usersCollection.getRecords().size() > 0) {
						log.info("Returning all users details from TeamsApi");
						return new ResponseEntity<>(usersCollection, HttpStatus.OK);					
				}
			}
			if (ObjectUtils.isEmpty(usersCollection) || Objects.isNull(usersCollection) || (usersCollection != null && (usersCollection.getRecords() == null || usersCollection.getRecords().size() == 0 || usersCollection.getTotalCount() == null))) {
				UsersCollection usersCollection2 = new UsersCollection();
				logger.info("Fetching user information with given identifier from DB.");
				usersInfo = userInfoService.getAllWithFilters(searchTerm, limit, offset, sortBy, sortOrder);
				Long count = userInfoService.getCountWithFilters(searchTerm);
				if (!ObjectUtils.isEmpty(usersInfo)) {
					usersCollection2.setRecords(usersInfo);
					usersCollection2.setTotalCount(count.intValue());
					log.debug("Returning all users details from DB");
					return new ResponseEntity<>(usersCollection2, HttpStatus.OK);
				} else {
					log.debug("No user details found from DB");
					return new ResponseEntity<>(usersCollection2, HttpStatus.NO_CONTENT);
				}
			}

		} catch (Exception e) {
			log.error("Failed while fetching users details with exception {}", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@Override
	public ResponseEntity<SolutionCollectionResponseVO> getAllBookmarks(@NotNull @Valid String userId) {
		SolutionCollectionResponseVO response = new SolutionCollectionResponseVO();
		try {
			List<SolutionVO> solutionVOList = userInfoService.getAllBookMarkedSolutionsForUser(userId);
			if (solutionVOList == null) {
				List<MessageDescription> notFoundMessages = new ArrayList<>();
				MessageDescription notFoundMessage = new MessageDescription();
				notFoundMessage.setMessage("No User favorites found!");
				notFoundMessages.add(notFoundMessage);
				response.setErrors(notFoundMessages);
				log.debug("No user bookmarks found");
				return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
			} else {
				response.setRecords(solutionVOList);
				log.debug("Returning user bookmarks");
				return new ResponseEntity<>(response, HttpStatus.OK);

			}
		} catch (NoSuchElementException e) {
			log.error("Error {} occured while fetching user bookmarks", e.getLocalizedMessage());
			List<MessageDescription> notFoundMessages = new ArrayList<>();
			MessageDescription notFoundMessage = new MessageDescription();
			notFoundMessage.setMessage("Invalid userid!");
			notFoundMessages.add(notFoundMessage);
			response.setErrors(notFoundMessages);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@ApiOperation(value = "Get specific user for a given userid.", nickname = "getById", notes = "Get specific user for a given userid. This endpoints will be used to Get specific user for a given userid.", response = UserInfoVO.class, tags = {
			"users",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = UserInfoVO.class),
			@ApiResponse(code = 400, message = "Malformed syntax."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/users/{id}", produces = {"application/json"}, consumes = {
			"application/json"}, method = RequestMethod.GET)
	public ResponseEntity<UserInfoVO> getById(
			@ApiParam(value = "Id of the user for which information to be fetched", required = true) @PathVariable("id") String id) {
		UserInfoVO userInfoVO = null;
		if (id != null) {
			userInfoVO = userInfoService.getById(id);
			return new ResponseEntity<>(userInfoVO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(userInfoVO, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@ApiOperation(value = "Number of users.", nickname = "getNumberOfUsers", notes = "Get number of users. This endpoints will be used to get all valid available users records.", response = TransparencyVO.class, tags={ "users", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/users/transparency",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfUsers() {
		TransparencyVO transparencyVO = new TransparencyVO();
		try {
			Integer count = userInfoService.getNumberOfUsers();
			transparencyVO.setCount(count);
			log.debug("Returning user count successfully");
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Failed while fetching users count with exception {}", e.getMessage());
			return new ResponseEntity<>(transparencyVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Update User", nickname = "update", notes = "Update User. This endpoint can be used to update user details", response = UserInfoVO.class, tags = {
			"users",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = UserInfoVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/users", produces = {"application/json"}, consumes = {
			"application/json"}, method = RequestMethod.PUT)
	public ResponseEntity<UserInfoVO> update(
			@ApiParam(value = "Request body contains the details of the updated user", required = true) @Valid @RequestBody UserRequestVO userRequestVO) {
		try {
			if (userRequestVO.getData() != null && userRequestVO.getData().getId() != null) {
				UserInfoVO userInfoVO = userRequestVO.getData();
				UserInfoVO currentUserData = userInfoService.getById(userInfoVO.getId());
				//To set key existing data if missing in request
				userinfoAssembler.setCurrentUserData(currentUserData, userInfoVO);
				Boolean isAdmin = false;
				CreatedByVO loggedInUser = this.userStore.getVO();
				String userId = loggedInUser != null ? loggedInUser.getId() : null;
				if (userId != null && !"".equalsIgnoreCase(userId)) {
					if (currentUserData != null) {
						UserInfoVO loggedInUserData = userInfoService.getById(userId);
						List<UserRoleVO> userRoles = loggedInUserData.getRoles();
						if (userRoles != null && !userRoles.isEmpty())
							isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
					}
				}
				if (!isAdmin) {
					logger.info("Only user with Admin role can change roles");
					return new ResponseEntity<>(userInfoVO, HttpStatus.UNAUTHORIZED);
				}
				if (!rolesUpdated(userRequestVO, currentUserData)) {
					userInfoVO.setToken(currentUserData.getToken());
				} else {
					userInfoVO.setToken(null);
				}
				userInfoService.create(userInfoVO);
				log.debug("user details updated successfully for userid {}", userRequestVO.getData().getId());
				return new ResponseEntity<>(userInfoVO, HttpStatus.OK);
			} else {
				log.debug("user details update failed for userid {}. Bad request", userRequestVO.getData().getId());
				return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<BookmarkResponseVO> updateBookmark(@Valid BookmarkRequestVO bookmarkRequestVO) {
		BookmarkResponseVO response = new BookmarkResponseVO();
		try {
			boolean deleteBookmark = bookmarkRequestVO.isDeleteBookmark() != null ? bookmarkRequestVO.isDeleteBookmark()
					: false;

			UserInfoVO responseVO = userInfoService.updateBookMarkedSolutions(bookmarkRequestVO.getId(),
					bookmarkRequestVO.getFavoriteUsecases(), deleteBookmark);
			// No need to send token back.
			responseVO.setToken(null);
			response.setData(responseVO);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (NoSuchElementException e) {
			List<MessageDescription> notFoundmessages = new ArrayList<>();
			MessageDescription notFoundmessage = new MessageDescription();
			notFoundmessage.setMessage("Invalid UserID/Bookmark Id's");
			notFoundmessages.add(notFoundmessage);
			response.setErrors(notFoundmessages);
			log.debug("user bookmarks update successfully for bookmarkid {}", bookmarkRequestVO.getId());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			List<MessageDescription> notFoundmessages = new ArrayList<>();
			MessageDescription notFoundmessage = new MessageDescription();
			notFoundmessage.setMessage("An  internal error occurred");
			notFoundmessages.add(notFoundmessage);
			response.setErrors(notFoundmessages);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	@ApiOperation(value = "create technical User", nickname = "onboardTechnicalUser", notes = "create technical User. This endpoint can be used to onboard technical User (userid of technical user to be onboarded is mandatory, system will default the other properties)", response = UserInfoVO.class, tags={ "users", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = UserInfoVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/users",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<UserInfoVO> onboardTechnicalUser(@ApiParam(value = "Request body contains the details of the updated user" ,required=true )  @Valid @RequestBody UserRequestVO userRequestVO) {
		try {
			
			if(!isTechUserOnboarding) {
				logger.info("Techuser onboading disabled.");
				return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.SERVICE_UNAVAILABLE);
			}
			if (userRequestVO.getData() != null && userRequestVO.getData().getId() != null) {
				UserInfoVO userInfoVO = userRequestVO.getData();
				if(userInfoVO == null || userInfoVO.getId() == null ||  !(userInfoVO != null && userInfoVO.getId() != null && userInfoVO.getId().toLowerCase().startsWith(techUserPrefix.toLowerCase()))) {
					logger.info("Invalid data provided, please check the user request data.");
					return new ResponseEntity<>(userInfoVO, HttpStatus.BAD_REQUEST);
				}
				
				Boolean isAdmin = false;
				CreatedByVO loggedInUser = this.userStore.getVO();
				String userId = loggedInUser != null ? loggedInUser.getId() : null;
				if (userId != null && !"".equalsIgnoreCase(userId)) {
						UserInfoVO loggedInUserData = userInfoService.getById(userId);
						List<UserRoleVO> userRoles = loggedInUserData.getRoles();
						if (userRoles != null && !userRoles.isEmpty())
							isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
				}
				if (!isAdmin) {
					logger.info("Only user with Admin role can change roles");
					return new ResponseEntity<>(userInfoVO, HttpStatus.UNAUTHORIZED);
				}
				UserInfoVO existingUser = userInfoService.getById(userInfoVO.getId());
				if(existingUser!=null && existingUser.getId()!= null) {
					logger.info("Failed to onboarding already existing user {} ", existingUser.getId());
					return new ResponseEntity<>(userInfoVO, HttpStatus.CONFLICT);
				}
				HasuraUserInfoInsertGenericResponse response  = hasuraClient.createTechnicalUser(userInfoVO);
				if (response != null) {
					log.info("Completed process {} with the response status {}", userRequestVO.getData().getId(), response.getStatus());
					return new ResponseEntity<>(response.getUserInfoVO(), response.getStatus());
				}
			} else {
				log.debug("user details update failed for userid {}. Bad request", userRequestVO.getData().getId());
				return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.BAD_REQUEST);
			}
		}catch(Exception e) {
			log.error("Failed to add user {} with exception {}", userRequestVO.getData().getId(), e.getMessage());
			return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.BAD_REQUEST);
		}
		return null;
	}

	private boolean rolesUpdated(UserRequestVO updatedUser, UserInfoVO currentUser) {
		if (updatedUser != null && currentUser != null) {
			List<UserRoleVO> updatedUserRoles = updatedUser.getData().getRoles();
			List<UserRoleVO> currentUserRoles = currentUser.getRoles();
			if (updatedUserRoles != null && currentUserRoles != null) {
				if (updatedUserRoles.size() != currentUserRoles.size()) {
					log.debug("User {} role updated successfully", updatedUser.getData().getId());
					return true;
				} else {
					for (UserRoleVO currentUserRole : currentUserRoles) {
						boolean roleFound = false;
						for (UserRoleVO updatedUserRole : updatedUserRoles) {
							if (updatedUserRole.getId().equals(currentUserRole.getId())) {
								roleFound = true;
								break;
							}
						}
						if (!roleFound) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	

	}
