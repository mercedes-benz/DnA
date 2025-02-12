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
import com.daimler.data.db.repo.promptCraftSubscriptions.PromptCraftSubscriptionsCustomRepository;
import com.daimler.data.db.repo.promptCraftSubscriptions.PromptCraftSubscriptionsRepository;
import com.daimler.data.service.common.BaseCommonService;

import com.daimler.data.db.entities.PromptCraftSubscriptionsNsql;
import com.daimler.data.db.jsonb.PromptCraftSubscriptions;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import com.daimler.data.client.uiLicious.UiliciousStartCreationResponseDTO;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsResponseVO;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysVO;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

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
	

	@Value("${promptsraftsubscriptions.uiLicious.privateKeyStepNumber}")
    private String privateKeyStepNumber;

	@Value("${promptsraftsubscriptions.uiLicious.publicKeyStepNumber}")
    private String publicKeyStepNumber;

	private static final int MAX_RETRIES = 12; // 2 minutes with 10 seconds interval
    private static final int RETRY_INTERVAL_MS = 10000; // 10 seconds

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
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try{

			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			vo.setCreatedOn(now);

			UiliciousStartCreationResponseDTO uiLiciousResponse = uiLiciousClient.startCreation(vo.getOrgname(),vo.getProjectName(),vo.getProjectMembers());
			if(uiLiciousResponse != null && uiLiciousResponse.getResponseStatus() == HttpStatus.OK ){
				vo.setRunId(uiLiciousResponse.getRunId());
				vo.setStatus("IN_PROGRESS");
				this.checkForKeysFromUiLicious(vo.getProjectName(),uiLiciousResponse.getRunId());
			}else{
				response.setData(vo);
				response.setSuccess("FAILED");
				MessageDescription msg = new MessageDescription();
				msg.setMessage("Failed while calling UiLicious to create Subscription. Please try again...");
				errors.add(msg);
				response.setErrors(errors);
				return response;
			}
			PromptCraftSubscriptionsNsql entity = promptCraftSubscriptionsAssembler.toEntity(vo);
			PromptCraftSubscriptionsNsql savedSubscription = jpaRepo.save(entity);
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

	// @Async
	// public void checkForKeysFromUiLicious(String runId){

	// 	JsonNode jsonResponse = uiLiciousClient.getSubscriptionKeys(runId);
	// 	if(jsonResponse != null){
	// 		log.info("Subscription keys are available for the run id {}",runId);
	// 	}
	// }

	@Async
	@Override
	public void checkForKeysFromUiLicious(String projectName, String runId) {
        int retries = 0;
        boolean stepsSizeSufficient = false;

		SubscriptionkeysVO keys = null;

		PromptCraftSubscriptionsNsql entity = null;

		int privateKeyStepNum = Integer.parseInt(privateKeyStepNumber);
        int publicKeyStepNum = Integer.parseInt(publicKeyStepNumber);

		PromptCraftSubscriptionsVO vo = getByUniqueliteral("projectName",projectName);
		if("COMPLETED".equalsIgnoreCase(vo.getStatus())){

		
			while (retries < MAX_RETRIES && !stepsSizeSufficient) {
				JsonNode jsonResponse = uiLiciousClient.getSubscriptionRunDetails(runId);
				if (jsonResponse != null) {
					log.debug("Subscription keys are available for the run id {}", runId);

					JsonNode stepsNode = jsonResponse.path("result").path("result").path("steps");
					if (stepsNode.isArray() && stepsNode.size() >= 25) {
						stepsSizeSufficient = true;
						log.debug("Steps size is sufficient: {}", stepsNode.size());

						for (JsonNode step : stepsNode) {
							int stepNum = step.path("stepNum").asInt();
							String status = step.path("status").asText();
							String description = step.path("description").asText();

							if (stepNum == privateKeyStepNum && "success".equalsIgnoreCase(status)) {
								keys.setPrivateKey(stepsNode.get(stepNum).path("return").asText());
								log.debug("PA Key: {}", keys.getPrivateKey());
							}

							if (stepNum == publicKeyStepNum && "success".equalsIgnoreCase(status)) {
								keys.setPublicKey(stepsNode.get(stepNum).path("retun").asText());
								log.debug("PU Key: {}", keys.getPublicKey());
							}
							if((stepNum == publicKeyStepNum || stepNum == privateKeyStepNum)  && "failed".equalsIgnoreCase(status)){
								vo.setStatus("FAILED");
								entity = promptCraftSubscriptionsAssembler.toEntity(vo);
								jpaRepo.save(entity);
							}
						}
						if(keys.getPrivateKey() != null && keys.getPublicKey() != null){

							// // Convert the object to a JSON string
							// String keyString = null;
							// ObjectMapper objectMapper = new ObjectMapper();
							// try {
							// 	 keyString = objectMapper.writeValueAsString(keys);
							// } catch (JsonProcessingException e) {
							// 	log.error("Error occured whie process json");
							// 	vo.setStatus("FAILED");
							// 	entity = promptCraftSubscriptionsAssembler.toEntity(vo);
							// 	jpaRepo.save(entity);
							// }
							// if(keyString != null){
							GenericMessage vaultResponse = vaultAuthClient.createSubscriptionKeys(projectName,keys);
							if(vaultResponse!=null && "SUCCESS".equalsIgnoreCase(vaultResponse.getSuccess())){
								log.error("Sucessfully added subscription keys to vault");
							vo.setStatus("COMPLETED");
							entity = promptCraftSubscriptionsAssembler.toEntity(vo);
							jpaRepo.save(entity);
								// }
							}
							
						}


					} else {
						log.info("Steps size is insufficient: {}. Retrying...", stepsNode.size());
						retries++;
						try {
							Thread.sleep(RETRY_INTERVAL_MS);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							log.error("Thread was interrupted", e);
							break;
						}
					}
				} else {
					log.warn("Received null response for run id {}", runId);
					retries++;
					try {
						Thread.sleep(RETRY_INTERVAL_MS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						log.error("Thread was interrupted", e);
						break;
					}
				}
			}
		}

        if (!stepsSizeSufficient) {
            log.error("Failed to get sufficient steps size within the timeout period for run id {}", runId);
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

}
