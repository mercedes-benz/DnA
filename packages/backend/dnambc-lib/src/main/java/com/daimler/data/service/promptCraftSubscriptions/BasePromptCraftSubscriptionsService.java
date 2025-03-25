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

package com.daimler.data.service.promptCraftSubscriptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;

import com.daimler.data.assembler.PromptCraftSubscriptionsAssembler;
import com.daimler.data.application.auth.vault.VaultAuthClientImpl;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.service.promptCraftSubscriptions.PromptCraftSubscriptionsService;
import com.daimler.data.service.promptCraftSubscriptions.AsyncService;
import com.daimler.data.db.repo.promptCraftSubscriptions.PromptCraftSubscriptionsCustomRepository;
import com.daimler.data.db.repo.promptCraftSubscriptions.PromptCraftSubscriptionsRepository;
import com.daimler.data.service.common.BaseCommonService;

import com.daimler.data.db.entities.PromptCraftSubscriptionsNsql;
import com.daimler.data.db.jsonb.PromptCraftSubscriptions;
import com.daimler.data.client.promptCraft.PromptCraftClient;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import com.daimler.data.client.uiLicious.UiliciousStartCreationResponseDTO;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsResponseVO;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysVO;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.http.HttpHeaders;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class BasePromptCraftSubscriptionsService extends BaseCommonService<PromptCraftSubscriptionsVO, PromptCraftSubscriptionsNsql, String> implements PromptCraftSubscriptionsService {

	@Autowired
	private PromptCraftSubscriptionsCustomRepository customRepo;

	@Autowired
	private PromptCraftSubscriptionsRepository jpaRepo;

	@Autowired
	private PromptCraftSubscriptionsAssembler promptCraftSubscriptionsAssembler;

	@Autowired
	private  UiLiciousClient uiLiciousClient;

	@Autowired
	private VaultAuthClientImpl vaultAuthClient;

	@Autowired
	private PromptCraftClient promptCraftClient;

	@Autowired
	private AsyncService asyncService;
	

	public BasePromptCraftSubscriptionsService() {
		super();
	}
	
	@Override
	public List<PromptCraftSubscriptionsVO> getAll(int offset, int limit, String  userId){
		List<PromptCraftSubscriptionsNsql> subscriptions = customRepo.findAlluserSubscriptions(offset, limit, userId);

		if(subscriptions != null){
			List<PromptCraftSubscriptionsVO> subscriptionsVO = subscriptions.stream()
					.map(n -> promptCraftSubscriptionsAssembler.toVo(n)).collect(Collectors.toList());
			return subscriptionsVO;
		}else{
			return new ArrayList<>();
		}
	}
	
	@Override
	public PromptCraftSubscriptionsResponseVO createSubscription(PromptCraftSubscriptionsVO vo){

		PromptCraftSubscriptionsResponseVO response = new PromptCraftSubscriptionsResponseVO();
		PromptCraftSubscriptionsNsql savedSubscription = new PromptCraftSubscriptionsNsql();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try{

			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			vo.setCreatedOn(now);
			UiliciousStartCreationResponseDTO uiLiciousResponse = uiLiciousClient.startCreation(vo.getOrgName(),vo.getProjectName(),vo.getProjectMembers());
			if(uiLiciousResponse != null && uiLiciousResponse.getResponseStatus() == HttpStatus.OK ){
				vo.setRunId(uiLiciousResponse.getRunId());
				vo.setStatus("IN_PROGRESS");
				PromptCraftSubscriptionsNsql entity = promptCraftSubscriptionsAssembler.toEntity(vo);
				savedSubscription = jpaRepo.save(entity);
				asyncService.checkForKeysFromUiLicious(vo.getProjectName(),uiLiciousResponse.getRunId());
			}else{
				response.setData(vo);
				response.setSuccess("FAILED");
				MessageDescription msg = new MessageDescription();
				msg.setMessage("Failed while calling UiLicious to create Subscription. Please try again...");
				errors.add(msg);
				response.setErrors(errors);
				return response;
			}
			response.setData(vo);
			response.getData().setId(savedSubscription.getId());
			response.setSuccess("SUCCESS");
			return response;
		}catch(Exception e){
			log.error("Error Occured while craeting new Subscription for the projet {} error: {}",vo.getProjectName() ,e.getMessage());
			response.setData(vo);
			response.setSuccess("FAILED");
			return response;
		}
	}

	@Override
	public SubscriptionkeysVO getProjectKeys( String projectName){
		SubscriptionkeysVO keys = new SubscriptionkeysVO();
		ObjectMapper objectMapper = new ObjectMapper();
		try{
			 keys = vaultAuthClient.getSubscriptionKeys(projectName);

		}catch(Exception e){
			log.error("error while getting keys for project {} error : {}",projectName, e.getMessage());
		}
		return keys;
	}

	
	public String getPromptCraftSubscriptionUserID( String publicKey, String privateKey) {
		String response = null;
		JsonNode jsonResponse = promptCraftClient.promptCraftRegisterUser( publicKey, privateKey);
		if( jsonResponse != null) {
			response = jsonResponse.path("promptcraft-user-id").asText();
		}
		return response;
	}
}
