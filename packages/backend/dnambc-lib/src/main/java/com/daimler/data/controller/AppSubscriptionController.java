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
import java.util.List;

import javax.validation.Valid;

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

import com.daimler.data.api.appsubscription.SubscribeApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.appsubscription.ApiKeyValidationRequestVO;
import com.daimler.data.dto.appsubscription.ApiKeyValidationResponseVO;
import com.daimler.data.dto.appsubscription.ApiKeyValidationVO;
import com.daimler.data.dto.appsubscription.SubscriptionDetailsCollection;
import com.daimler.data.dto.appsubscription.SubscriptionExpireRequestVO;
import com.daimler.data.dto.appsubscription.SubscriptionExpireVO;
import com.daimler.data.dto.appsubscription.SubscriptionRequestVO;
import com.daimler.data.dto.appsubscription.SubscriptionResponseVO;
import com.daimler.data.dto.appsubscription.SubscriptionVO;
import com.daimler.data.dto.platform.PlatformVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.solution.SolutionPortfolioVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.appsubscription.AppSubscriptionService;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Subscription API", tags = { "subscribe" })
@RequestMapping("/api")
public class AppSubscriptionController implements SubscribeApi {
	private static Logger LOGGER = LoggerFactory.getLogger(AppSubscriptionController.class);

	@Autowired
	AppSubscriptionService vService;
	
	@Autowired
    private UserStore userStore;
	
	@Autowired
    private UserInfoService userInfoService;
	
	@Autowired
    private SolutionService solutionService;

	@Override
	@ApiOperation(value = "Validate API key.", nickname = "validateApiKey", notes = "Validate API key, check if user has sent valid API key.", response = ApiKeyValidationResponseVO.class, tags = {
			"subscribe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = ApiKeyValidationResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subscription/validate", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ApiKeyValidationResponseVO> validateApiKey(
			@ApiParam(value = "Request Body that contains data required to validate api key", required = true) @Valid @RequestBody ApiKeyValidationRequestVO apiKeyValidateRequestVO) {
		LOGGER.trace("Entering validateApiKey");
		ApiKeyValidationVO requestApiKeyValidationVO = apiKeyValidateRequestVO.getData();
		ApiKeyValidationResponseVO response = new ApiKeyValidationResponseVO();
		try {
			LOGGER.info("Validating api key..");
			return vService.validateApiKey(requestApiKeyValidationVO);
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{}", e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			response.setData(requestApiKeyValidationVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all subscribed application.", nickname = "getAll", notes = "Get all subscribed application. This endpoints will be used to Get all subscribed application records.", response = SubscriptionDetailsCollection.class, tags = {
			"subscribe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = SubscriptionDetailsCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subscription", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<SubscriptionDetailsCollection> getAll(
			@ApiParam(value = "Flag to check If user want to view in admin mode", defaultValue = "false") @Valid @RequestParam(value = "admin", required = false, defaultValue="false") Boolean admin,
			@ApiParam(value = "page number from which listing of subscribed service should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of subscribed service, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Application Id of subscribed service.") @Valid @RequestParam(value = "appId", required = false) String appId,
			@ApiParam(value = "Field which will be used to sort record.") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Order(asc,des) in which record will get sorted.") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		LOGGER.trace("Entering getAll");
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0) {
				offset = 0;
			}
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			String recordStatus = ConstantsUtility.OPEN;
			SubscriptionDetailsCollection subscriptionDetailsCollection = new SubscriptionDetailsCollection();
			Boolean isAdmin = false;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoles = userInfoVO.getRoles();
					if (userRoles != null && !userRoles.isEmpty())
						isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
				}
			}
			LOGGER.debug("Check whether user wants to view in admin mode");
			isAdmin = (admin && isAdmin) ? true : false;
			Long count = vService.getCount(userId, isAdmin, recordStatus, appId);
			if (count < offset) {
				offset = 0;
			}
				
			List<SubscriptionVO> subscriptionsVO = vService.getAllWithFilters(userId, isAdmin, recordStatus, appId, sortBy, sortOrder, offset, limit);
			if (!ObjectUtils.isEmpty(subscriptionsVO)) {
				subscriptionDetailsCollection.setData(subscriptionsVO);
				subscriptionDetailsCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(subscriptionDetailsCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(subscriptionDetailsCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching subscriptionDetails {} ", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Subscribe a new application.", nickname = "subscribeApplication", notes = "Subscribe a new application and create unique key which will be used in access services on DnA platform.", response = SubscriptionResponseVO.class, tags = {
			"subscribe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = SubscriptionResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subscription", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<SubscriptionResponseVO> subscribeApplication(
			@ApiParam(value = "Request Body that contains data required for subscribe a new application", required = true) @Valid @RequestBody SubscriptionRequestVO subscriptionRequestVO) {
		LOGGER.trace("Entering subscribeApplication");
		SubscriptionVO requestSubscriptionVO = subscriptionRequestVO.getData();
		SubscriptionResponseVO response = new SubscriptionResponseVO();
		try {
			CreatedByVO currentUser = this.userStore.getVO();
            String userId = currentUser != null ? currentUser.getId() : null;
            if(vService.isApplicationSubscriptionExist(requestSubscriptionVO, userId)) {
            	List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Application is already subscribed with name "+requestSubscriptionVO.getAppName());
				messages.add(message);
				response.setData(requestSubscriptionVO);
				response.setErrors(messages);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
			SubscriptionVO subscriptionVO = vService.createApiKey(requestSubscriptionVO,userId);
			if (subscriptionVO != null && subscriptionVO.getId() != null) {
				LOGGER.info("Service subscribed successfully");
				response.setData(subscriptionVO);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				response.setData(requestSubscriptionVO);
				response.setErrors(messages);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{}", e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			response.setData(requestSubscriptionVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Delete subscription of a service for a given Id.", nickname = "delete", notes = "Delete subscription of a service for a given identifier.", response = GenericMessage.class, tags={ "subscribe", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/subscription/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	@Override
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Subscription ID to be deleted", required = true) @PathVariable("id") String id) {
		try {
			SubscriptionVO existingSubscriptionVO = vService.getById(id);
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						String createdBy = StringUtils.hasText(existingSubscriptionVO.getCreatedBy())
								? existingSubscriptionVO.getCreatedBy()
								: null;
						boolean isOwner = (createdBy != null && createdBy.equals(userId));
						if (!isAdmin && !isOwner) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to delete subscription. Only subscriber or an admin can delete the subscription.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			SubscriptionVO SubscriptionVO = vService.deleteSubscriptionById(id,userId, existingSubscriptionVO);
			String solutionId = existingSubscriptionVO.getSolutionId();
			if(solutionId!=null) {
				SolutionVO solutionVO = solutionService.getById(solutionId);
				SolutionPortfolioVO portfolio = solutionVO.getPortfolio();
				if(portfolio!=null) {
					List<PlatformVO> platforms = portfolio.getPlatforms();
					List<PlatformVO> updatedPlatforms = new ArrayList<>();
					if(platforms!=null && !platforms.isEmpty()) {
						int size = platforms.size();
						for(PlatformVO platform: platforms) {
							if(platform != null && platform.getName()!= null) {
								if(!platform.getName().contains("Malware")) {
									updatedPlatforms.add(platform);
								}else {
									if(size<2)
										portfolio.setUsesExistingInternalPlatforms(false);
								}
							}
						}
					}
					portfolio.setPlatforms(updatedPlatforms);
					solutionVO.setPortfolio(portfolio);
					solutionService.create(solutionVO);
					LOGGER.info("On Subscription delete, updated Solution platform details successfully");
				}
				
			}
			if (SubscriptionVO != null && SubscriptionVO.getId() != null) {
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("Success");
				LOGGER.info("Subscription deleted successfully");
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				MessageDescription exceptionMsg = new MessageDescription("Failed to delete subscription");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(exceptionMsg);
				LOGGER.info("Subscription deletion Failed");
				return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to delete due to internal error {}", e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription(e.getMessage());
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Get Subscription for a given Id.", nickname = "getById", notes = "Get subscription for a given identifier. This endpoints will be used to get a subscription for a given identifier.", response = SubscriptionVO.class, tags = {
			"subscribe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = SubscriptionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subscription/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Override
	public ResponseEntity<SubscriptionVO> getById(
			@ApiParam(value = "Subscription ID to be fetched", required = true) @PathVariable("id") String id) {
		Boolean isAdmin = false;
		HttpStatus httpStatus = null;
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		if (userId != null && !"".equalsIgnoreCase(userId)) {
			UserInfoVO userInfoVO = userInfoService.getById(userId);
			if (userInfoVO != null) {
				List<UserRoleVO> userRoles = userInfoVO.getRoles();
				if (userRoles != null && !userRoles.isEmpty())
					isAdmin = userRoles.stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
			}
		}
		SubscriptionVO subscriptionVO = vService.getById(id);
		if (subscriptionVO != null) {
			if (!isAdmin && !userId.equals(subscriptionVO.getCreatedBy())) {
				subscriptionVO = null;
				httpStatus = HttpStatus.NO_CONTENT;
			} else {
				httpStatus = HttpStatus.OK;
			}
		} else {
			httpStatus = HttpStatus.NO_CONTENT;
		}
		return new ResponseEntity<>(subscriptionVO, httpStatus);
	}

	@Override
	@ApiOperation(value = "Refresh Subscription apiKey for a given appId.", nickname = "refreshApiKeyByAppId", notes = "Refresh subscription apiKey for a given identifier. This endpoints will be used to refresh a subscription apiKey for a given identifier.", response = SubscriptionVO.class, tags = {
			"subscribe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = SubscriptionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subscription/{appId}/refresh", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<SubscriptionResponseVO> refreshApiKeyByAppId(
			@ApiParam(value = "Subscription appId for which apiKey to be refreshed", required = true) @PathVariable("appId") String appId) {
		LOGGER.trace("Entering refreshApiKeyByAppId");
		SubscriptionResponseVO response = new SubscriptionResponseVO();
		try {
			SubscriptionVO existingSubscriptionVO = vService.getByUniqueliteral("appId", appId);
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						String createdBy = StringUtils.hasText(existingSubscriptionVO.getCreatedBy())
								? existingSubscriptionVO.getCreatedBy()
								: null;
						boolean isOwner = (createdBy != null && createdBy.equals(userId));
						if (!isAdmin && !isOwner) {
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to refresh subscription apiKey. Only subscriber or an admin can refresh the api key.");
							messages.add(notAuthorizedMsg);
							response.setErrors(messages);
							return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			
			SubscriptionVO subscriptionVO = vService.refreshApiKeyByAppId(appId,userId,existingSubscriptionVO);
			if(subscriptionVO!=null && subscriptionVO.getId()!=null) {
				LOGGER.info("Api Key refreshed successfully");
				response.setData(subscriptionVO);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}else {
				LOGGER.info("Invalid appId");
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription();
				errMsg.setMessage("Invalid appId");
				response.setErrors(messages);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		}catch (Exception e) {
			LOGGER.error("Error occured while refreshing apiKey: {}",e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription();
			errMsg.setMessage(e.getMessage());
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	@ApiOperation(value = "Update expiry days of a service subscription for a given Id.", nickname = "update", notes = "Set expiry of a service subscription for a given identifier.", response = SubscriptionResponseVO.class, tags = {
			"subscribe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Updated successfully", response = SubscriptionResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request.", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subscription", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@Override
	public ResponseEntity<SubscriptionResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating expiry of an existing subscription", required = true) @Valid @RequestBody SubscriptionExpireRequestVO expireRequestVO) {
		LOGGER.trace("Entering update subscription");
		SubscriptionExpireVO expireVO = expireRequestVO.getData();
		SubscriptionResponseVO response = new SubscriptionResponseVO();
		SubscriptionVO mergedSubscriptionVO = null;
		HttpStatus status = null;
		List<MessageDescription> messages = null;
		MessageDescription message = null;
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : null;
			if (userInfoService.isAdmin(userId)) {
				mergedSubscriptionVO = vService.updateSubscription(userId, expireVO);
				if (mergedSubscriptionVO != null && StringUtils.hasText(mergedSubscriptionVO.getId())) {
					status = HttpStatus.OK;
				} else {
					message = new MessageDescription();
					message.setMessage("No subscription found for given id. Update cannot happen");
					status = HttpStatus.NOT_FOUND;
				}
			} else {
				message = new MessageDescription();
				message.setMessage("Not authorized to update subscription. Only admin can update.");
				status = HttpStatus.FORBIDDEN;
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{}", e.getMessage());
			message = new MessageDescription();
			message.setMessage(e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		if (!ObjectUtils.isEmpty(message)) {
			messages = new ArrayList<MessageDescription>();
			messages.add(message);
			response.setErrors(messages);
		}
		response.setData(mergedSubscriptionVO);
		LOGGER.trace("Returning from update subscription");
		return new ResponseEntity<>(response, status);
	}


}
