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

package com.daimler.data.service.appsubscription;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.vault.support.VaultResponse;

import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.assembler.AppSubscriptionAssembler;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.AppSubscriptionNsql;
import com.daimler.data.db.jsonb.AppSubscription;
import com.daimler.data.db.repo.appsubscription.AppSubscriptionCustomRepository;
import com.daimler.data.db.repo.appsubscription.AppSubscriptionRepository;
import com.daimler.data.dto.VaultGenericResponse;
import com.daimler.data.dto.appsubscription.ApiKeyValidationResponseVO;
import com.daimler.data.dto.appsubscription.ApiKeyValidationVO;
import com.daimler.data.dto.appsubscription.SubscriptionExpireVO;
import com.daimler.data.dto.appsubscription.SubscriptionVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;

@Service
public class BaseAppSubscriptionService extends BaseCommonService<SubscriptionVO, AppSubscriptionNsql, String>
		implements AppSubscriptionService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseAppSubscriptionService.class);

	@Autowired
	private AppSubscriptionCustomRepository customRepo;
	@Autowired
	private AppSubscriptionRepository jpaRepo;
	@Autowired
	private AppSubscriptionAssembler appSubscriptionAssembler;

	@Autowired
	private VaultConfig vaultConfig;

	public BaseAppSubscriptionService() {
		super();
	}

	/**
	 * <p>
	 * To validate application key.
	 * </p>
	 * 
	 * @param ApiKeyValidationVO
	 */
	@Override
	public ResponseEntity<ApiKeyValidationResponseVO> validateApiKey(ApiKeyValidationVO apiKeyValidationVO) {
		ApiKeyValidationResponseVO validationResponseVO = new ApiKeyValidationResponseVO();
		HttpStatus httpStatus = null;
		List<MessageDescription> messages = new ArrayList<>();
		MessageDescription message = null;
		String uniqueAppId = apiKeyValidationVO.getAppId();
		SubscriptionVO existingSubscriptionVO = super.getByUniqueliteral("appId", apiKeyValidationVO.getAppId());
		if (existingSubscriptionVO != null && StringUtils.hasText(existingSubscriptionVO.getAppId())) {
			if (StringUtils.hasText(existingSubscriptionVO.getRecordStatus())
					&& existingSubscriptionVO.getRecordStatus().equals(ConstantsUtility.DELETED)) {
				LOGGER.info("Subscription is deleted by owner/admin for appId {} ", uniqueAppId);
				message = new MessageDescription();
				message.setMessage("Subscription is deleted by owner/admin.");
				messages.add(message);
				validationResponseVO.setValidApiKey(false);
				httpStatus = HttpStatus.BAD_REQUEST;
			} else if (existingSubscriptionVO.getExpireOn() != null
					&& existingSubscriptionVO.getExpireOn().before(new Date())) {
				LOGGER.info("Subscription is expired please contact admin. appId {} ",uniqueAppId);
				message = new MessageDescription();
				message.setMessage("Subscription is expired please contact admin.");
				messages.add(message);
				validationResponseVO.setValidApiKey(false);
				httpStatus = HttpStatus.OK;
			} else {
				LOGGER.debug("Entering validateApiKey for appId {}",uniqueAppId);
				VaultGenericResponse vaultResponse = vaultConfig.validateApiKey(apiKeyValidationVO.getAppId(),
						apiKeyValidationVO.getApiKey());
				if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
					LOGGER.info("Valid apikey for appId {}",uniqueAppId);
					existingSubscriptionVO.setUsageCount(
							existingSubscriptionVO.getUsageCount() != null ? existingSubscriptionVO.getUsageCount() + 1
									: 1);
					existingSubscriptionVO.setLastUsedOn(new Date());
					super.create(existingSubscriptionVO);
					validationResponseVO.setValidApiKey(true);
					httpStatus = HttpStatus.OK;
				} else {
					message = new MessageDescription();
					LOGGER.info("Failed to validate apikey for appId {}",uniqueAppId);
					message.setMessage("Failed to validate");
					messages.add(message);
					validationResponseVO.setValidApiKey(false);
					httpStatus = HttpStatus.NOT_FOUND;
				}
			}
		} else {
			message = new MessageDescription();
			LOGGER.info("Api Key is invalid for appId {}",uniqueAppId);
			message.setMessage("Api Key is invalid");
			messages.add(message);
			validationResponseVO.setValidApiKey(false);
			httpStatus = HttpStatus.NOT_FOUND;
		}

		validationResponseVO.setData(apiKeyValidationVO);
		if (!ObjectUtils.isEmpty(messages)) {
			validationResponseVO.setErrors(messages);
		} else {
			validationResponseVO.setErrors(null);
		}

		return new ResponseEntity<ApiKeyValidationResponseVO>(validationResponseVO, httpStatus);
	}

	/**
	 * create/update api key in vault
	 * 
	 * @param SubscriptionVO
	 * @return SubscriptionVO
	 */
	@Override
	@Transactional
	public SubscriptionVO createApiKey(SubscriptionVO requestSubscriptionVO, String userId) {
		String appId = UUID.randomUUID().toString();
		String apiKey = UUID.randomUUID().toString();
		VaultGenericResponse vaultResponse = vaultConfig.createApiKey(appId, apiKey);
		if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
			LOGGER.info("Valid apikey");
			requestSubscriptionVO.setAppId(appId);
			requestSubscriptionVO.setApiKey(apiKey);
			requestSubscriptionVO.setCreatedBy(userId);
			requestSubscriptionVO.setCreatedDate(new Date());
			requestSubscriptionVO.setUpdatedBy(userId);
			requestSubscriptionVO.setUpdatedDate(new Date());
			requestSubscriptionVO.setRecordStatus(ConstantsUtility.OPEN);
			requestSubscriptionVO.setUsageCount(0);
			SubscriptionVO responseSubscriptionVO = super.create(requestSubscriptionVO);
			responseSubscriptionVO.setApiKey(apiKey);
			return responseSubscriptionVO;
		} else {
			return null;
		}
	}

	/**
	 * get All Subscription details based on filter
	 * 
	 * @param userId
	 * @param isAdmin
	 * @param offset
	 * @param limit
	 * @return List<SubscriptionVO>
	 */
	@Override
	@Transactional
	public List<SubscriptionVO> getAllWithFilters(String userId, boolean isAdmin, String recordStatus, String appId,
			String sortBy, String sortOrder, int offset, int limit, String searchTerm) {
		List<AppSubscriptionNsql> appSubscriptionEntities = customRepo.getAllWithFilters(userId, isAdmin, recordStatus,
				appId, sortBy, sortOrder, offset, limit, null, searchTerm);
		if (!ObjectUtils.isEmpty(appSubscriptionEntities)) {
			List<SubscriptionVO> SubscriptionsVO = appSubscriptionEntities.stream()
					.map(n -> appSubscriptionAssembler.toVo(n)).collect(Collectors.toList());
			for (SubscriptionVO subscriptionVO : SubscriptionsVO) {
				VaultResponse vaultResponse = vaultConfig.getApiKeys(subscriptionVO.getAppId());
				if (vaultResponse != null && vaultResponse.getData() != null
						&& vaultResponse.getData().get(subscriptionVO.getAppId()) != null) {
					subscriptionVO.setApiKey(vaultResponse.getData().get(subscriptionVO.getAppId()).toString());
				}
			}
			return SubscriptionsVO;
		} else {
			return new ArrayList<>();
		}

	}

	/**
	 * get count of subscribed application
	 * 
	 * @param userId
	 * @param isAdmin
	 */
	@Override
	@Transactional
	public Long getCount(String userId, boolean isAdmin, String recordStatus, String appId, String searchTerm) {
		return customRepo.getCount(userId, isAdmin, recordStatus, appId, searchTerm);
	}

	/**
	 * Delete a subscription with given identifier
	 * 
	 * @param id
	 * @param userId
	 * @param existingSubscriptionVO
	 */
	@Override
	@Transactional
	public SubscriptionVO deleteSubscriptionById(String id, String userId, SubscriptionVO existingSubscriptionVO) {
		if (existingSubscriptionVO != null) {
			existingSubscriptionVO.setUpdatedBy(userId);
			existingSubscriptionVO.setUpdatedDate(new Date());
			existingSubscriptionVO.setRecordStatus(ConstantsUtility.DELETED);
			return super.create(existingSubscriptionVO);
		} else {
			LOGGER.info("No Subscription found for given identification id {}", id);
			return null;
		}
	}

	/**
	 * Refresh apiKey based on given appId
	 * 
	 * @param appId
	 * @return SubscriptionVO
	 */
	@Override
	@Transactional
	public SubscriptionVO refreshApiKeyByAppId(String appId, String userId, SubscriptionVO existingSubscriptionVO) {
		if (existingSubscriptionVO != null && StringUtils.hasText(existingSubscriptionVO.getAppId())) {
			String apiKey = UUID.randomUUID().toString();
			VaultGenericResponse vaultResponse = vaultConfig.createApiKey(appId, apiKey);
			if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
				LOGGER.info("Api Key refreshed successfully for appId {}", appId);
				existingSubscriptionVO.setApiKey(apiKey);
				existingSubscriptionVO.setUpdatedBy(userId);
				existingSubscriptionVO.setUpdatedDate(new Date());
				// subscriptionVO.setRecordStatus(ConstantsUtility.OPEN);
				SubscriptionVO responseSubscriptionVO = super.create(existingSubscriptionVO);
				responseSubscriptionVO.setApiKey(apiKey);
				return responseSubscriptionVO;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public void updateSolIdForSubscribedAppId(String appId, String solutionId) {
		AppSubscriptionNsql entity = null;
		if (StringUtils.hasText(appId)) {
			LOGGER.info("Fetching record for appId {}", appId);
			entity = customRepo.findbyUniqueLiteral("appId", appId);
		}
		if (entity != null && entity.getData() != null
				&& !entity.getData().getRecordStatus().equalsIgnoreCase(ConstantsUtility.DELETED)) {
			AppSubscription jsonb = entity.getData();
			jsonb.setSolutionId(solutionId);
			entity.setData(jsonb);
			customRepo.update(entity);
			LOGGER.info("Solution Provisioning successfull for appId {}", appId);
		} else {
			LOGGER.info("No Subscription found for Solution Provisioning. for appId {}", appId);
		}
	}

	@Override
	@Transactional
	public SubscriptionVO updateSubscription(String userId, SubscriptionExpireVO expireVO) {
		AppSubscriptionNsql responseEntity = null;
		AppSubscriptionNsql entity = customRepo.findbyUniqueLiteral("appId", expireVO.getAppId());
		if (!ObjectUtils.isEmpty(entity) && !ObjectUtils.isEmpty(entity.getData())) {
			entity.getData().setUpdatedBy(userId);
			entity.getData().setUpdatedDate(new Date());
			if (StringUtils.hasText(expireVO.getDescription())) {
				entity.getData().setDescription(expireVO.getDescription());
			}
			entity.getData().setExpiryDays(expireVO.getExpiryDays());
			entity.getData().setExpireOn(setExpiryDate(expireVO.getExpiryDays()));
			responseEntity = jpaRepo.save(entity);
		}
		LOGGER.info("Updating subscription for appId {} , expiryDays {} ", expireVO.getAppId(), expireVO.getExpiryDays());
		return appSubscriptionAssembler.toVo(responseEntity);
	}

	/**
	 * <p>
	 * To return App subscription identified by Id
	 * </p>
	 * 
	 * @param Id
	 * @return SubscriptionVO
	 */
	@Override
	@Transactional
	public SubscriptionVO getById(String id) {
		SubscriptionVO subscriptionVO = super.getById(id);
		if (!ObjectUtils.isEmpty(subscriptionVO)) {
			VaultResponse vaultResponse = vaultConfig.getApiKeys(subscriptionVO.getAppId());
			if (vaultResponse != null && vaultResponse.getData() != null
					&& vaultResponse.getData().get(subscriptionVO.getAppId()) != null) {
				subscriptionVO.setApiKey(vaultResponse.getData().get(subscriptionVO.getAppId()).toString());
			}
		}
		return subscriptionVO;
	}

	private Date setExpiryDate(Integer expiryDays) {
		Date expiryDate = null;
		if (expiryDays != null) {
			if (expiryDays == -1) {
				expiryDate = new Date();
			} else if (expiryDays >= 0) {
				expiryDate = new Date();
				expiryDate = DateUtils.addDays(expiryDate, expiryDays);
				expiryDate = DateUtils.setHours(expiryDate, 23);
				expiryDate = DateUtils.setMinutes(expiryDate, 59);
				expiryDate = DateUtils.setSeconds(expiryDate, 59);
				expiryDate = DateUtils.setMilliseconds(expiryDate, 999);
			} else {
				expiryDate = null;
			}
		}
		return expiryDate;
	}

	@Override
	public boolean isApplicationSubscriptionExist(SubscriptionVO requestSubscriptionVO, String userId) {
		List<AppSubscriptionNsql> entityList = customRepo.getAllWithFilters(userId, false, ConstantsUtility.OPEN, null, null, null, 0, 0,
				requestSubscriptionVO.getAppName(), null);
		return !ObjectUtils.isEmpty(entityList);
	}
}
