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

package com.daimler.data.service.marketingCommunicationChannel;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.MarketingCommunicationChannelNsql;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelResponseVO;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.userinfo.UserInfoService;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseMarketingCommunicationChannelService extends BaseCommonService<MarketingCommunicationChannelVO, MarketingCommunicationChannelNsql, String>
		implements MarketingCommunicationChannelService {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;


	public BaseMarketingCommunicationChannelService() {
		super();
	}
	
	private static Logger LOGGER = LoggerFactory.getLogger(BaseMarketingCommunicationChannelService.class);

	@Override
	@Transactional
	public ResponseEntity<MarketingCommunicationChannelResponseVO> updateMarketingCommunicationChannel(
			MarketingCommunicationChannelVO marketingCommunicationChannelVO) {
		MarketingCommunicationChannelResponseVO response = new MarketingCommunicationChannelResponseVO();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (userInfoService.isAdmin(userId)) {
			String id = marketingCommunicationChannelVO.getId();
			MarketingCommunicationChannelVO existingVO = super.getById(id);
			MarketingCommunicationChannelVO updatedMarketingCommunicationChannelVO = null;
			if (existingVO != null && existingVO.getId() != null) {
				String uniquemarketingCommunicationChannelName = marketingCommunicationChannelVO.getName();
				MarketingCommunicationChannelVO existingMarketingCommunicationChannelVO = super.getByUniqueliteral("name", marketingCommunicationChannelVO.getName());
				if (existingMarketingCommunicationChannelVO != null && existingMarketingCommunicationChannelVO.getName() != null
						&& existingMarketingCommunicationChannelVO.getId().equals(id)) {
					response.setData(existingMarketingCommunicationChannelVO);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("MarketingCommunicationChannel already exists.");
					messages.add(message);
					response.setErrors(messages);
					LOGGER.debug("MarketingCommunicationChannel {} already exists, returning as CONFLICT", uniquemarketingCommunicationChannelName);
					return new ResponseEntity<>(response, HttpStatus.CONFLICT);
				}						
				updatedMarketingCommunicationChannelVO = super.create(marketingCommunicationChannelVO);
				response.setData(updatedMarketingCommunicationChannelVO);
				response.setErrors(null);
				LOGGER.debug("MarketingCommunicationChannel with id {} updated successfully", id);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No MarketingCommunicationChannel found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				response.setErrors(notFoundmessages);
				LOGGER.debug("No MarketingCommunicationChannel found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		}
		else {
			List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to update MarketingCommunicationChannel. Only user with admin role can update.");
			notAuthorizedMsgs.add(notAuthorizedMsg);
			response.setErrors(notAuthorizedMsgs);
			LOGGER.debug("MarketingCommunicationChannel with id {} cannot be edited. User not authorized", marketingCommunicationChannelVO.getId());
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteMarketingCommunicationChannel(String id) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (!userInfoService.isAdmin(userId)) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to delete MarketingCommunicationChannel. User does not have admin privileges.");
			LOGGER.debug("MarketingCommunicationChannel with id {} cannot be deleted. User {} not authorized", id, userId);
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}
		MarketingCommunicationChannelVO existingVO = super.getById(id);
		if (existingVO != null && existingVO.getId().equalsIgnoreCase(id) && existingVO.getName() != null) {
			String marketingCommunicationChannelName = existingVO != null ? existingVO.getName() : "";
			deleteById(id);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			LOGGER.info("MarketingCommunicationChannel {} deleted successfully", marketingCommunicationChannelName);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		}
		else {
			MessageDescription notFoundmessage = new MessageDescription();
			notFoundmessage.setMessage("No MarketingCommunicationChannel found for given id. Delete cannot happen");
			LOGGER.info("No MarketingCommunicationChannel found for given id {} , delete cannot happen.", id);
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notFoundmessage);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		}
		

	}

}
