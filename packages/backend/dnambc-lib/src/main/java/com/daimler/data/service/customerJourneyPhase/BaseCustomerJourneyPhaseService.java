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

package com.daimler.data.service.customerJourneyPhase;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CustomerJourneyPhaseNsql;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseResponseVO;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseVO;
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
public class BaseCustomerJourneyPhaseService extends BaseCommonService<CustomerJourneyPhaseVO, CustomerJourneyPhaseNsql, String>
		implements CustomerJourneyPhaseService {


	public BaseCustomerJourneyPhaseService() {
		super();
	}
	
	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	private static Logger LOGGER = LoggerFactory.getLogger(BaseCustomerJourneyPhaseService.class);
	
	@Override
	@Transactional
	public ResponseEntity<CustomerJourneyPhaseResponseVO> updateCustomerJourneyPhase(
			CustomerJourneyPhaseVO customerJourneyPhaseVO) {

		CustomerJourneyPhaseResponseVO response = new CustomerJourneyPhaseResponseVO();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (userInfoService.isAdmin(userId)) {
			String id = customerJourneyPhaseVO.getId();
			CustomerJourneyPhaseVO existingVO = super.getById(id);
			CustomerJourneyPhaseVO updatedCustomerJourneyPhaseVO = null;
			if (existingVO != null && existingVO.getId() != null) {
				String uniqueCustomerJourneyPhaseName = customerJourneyPhaseVO.getName();
				CustomerJourneyPhaseVO existingCustomerJourneyPhaseVO = super.getByUniqueliteral("name", customerJourneyPhaseVO.getName());
				if (existingCustomerJourneyPhaseVO != null && existingCustomerJourneyPhaseVO.getName() != null
						&& existingCustomerJourneyPhaseVO.getId().equals(id)) {
					response.setData(existingCustomerJourneyPhaseVO);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("CustomerJourneyPhase already exists.");
					messages.add(message);
					response.setErrors(messages);
					LOGGER.info("CustomerJourneyPhase {} already exists, returning as CONFLICT", uniqueCustomerJourneyPhaseName);
					return new ResponseEntity<>(response, HttpStatus.CONFLICT);
				}						
				updatedCustomerJourneyPhaseVO = super.create(customerJourneyPhaseVO);
				response.setData(updatedCustomerJourneyPhaseVO);
				response.setErrors(null);
				LOGGER.info("CustomerJourneyPhase with id {} updated successfully", id);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No CustomerJourneyPhase found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				response.setErrors(notFoundmessages);
				LOGGER.info("No CustomerJourneyPhase found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		}
		else {
			List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to update CustomerJourneyPhase. Only user with admin role can update.");
			notAuthorizedMsgs.add(notAuthorizedMsg);
			response.setErrors(notAuthorizedMsgs);
			LOGGER.debug("CustomerJourneyPhase with id {} cannot be edited. User not authorized", customerJourneyPhaseVO.getId());
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}
	
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteCustomerJourneyPhase(String id) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (!userInfoService.isAdmin(userId)) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to delete CustomerJourneyPhase. User does not have admin privileges.");
			LOGGER.debug("CustomerJourneyPhase with id {} cannot be deleted. User {} not authorized", id, userId);
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}
		CustomerJourneyPhaseVO existingVO = super.getById(id);
		if (existingVO != null && existingVO.getId().equalsIgnoreCase(id) && existingVO.getName() != null) {
			String customerJourneyPhaseName = existingVO != null ? existingVO.getName() : "";
			deleteById(id);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			LOGGER.info("CustomerJourneyPhase {} deleted successfully", customerJourneyPhaseName);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		}
		else {
			MessageDescription notFoundmessage = new MessageDescription();
			notFoundmessage.setMessage("No CustomerJourneyPhase found for given id. Delete cannot happen");
			LOGGER.info("No CustomerJourneyPhase found for given id {} , delete cannot happen.", id);
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notFoundmessage);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		}
	}

}
