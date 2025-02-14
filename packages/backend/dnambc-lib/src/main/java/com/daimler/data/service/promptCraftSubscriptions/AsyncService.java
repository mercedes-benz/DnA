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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.daimler.data.application.auth.vault.VaultAuthClientImpl;
import com.daimler.data.assembler.PromptCraftSubscriptionsAssembler;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.PromptCraftSubscriptionsNsql;
import com.daimler.data.db.repo.promptCraftSubscriptions.PromptCraftSubscriptionsCustomRepository;
import com.daimler.data.db.repo.promptCraftSubscriptions.PromptCraftSubscriptionsRepository;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysVO;
import com.daimler.data.service.promptCraftSubscriptions.PromptCraftSubscriptionsService;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AsyncService {

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
	private PromptCraftSubscriptionsService service;

	private static final int MAX_RETRIES = 12; // 2 minutes with 10 seconds interval
    private static final int RETRY_INTERVAL_MS = 10000; // 10 seconds


	@Async
	public void checkForKeysFromUiLicious(String projectName, String runId) {
        int retries = 0;
        boolean stepsSizeSufficient = false;

		SubscriptionkeysVO keys = new SubscriptionkeysVO();

		PromptCraftSubscriptionsNsql entity = null;


		PromptCraftSubscriptionsVO vo = service.getByUniqueliteral("projectName",projectName);
		if(!"COMPLETED".equalsIgnoreCase(vo.getStatus())){

		
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
							String cmd = step.path("cmd").asText();
						
							// Normalize for case-insensitive checks
							String lowerDescription = description.toLowerCase();
							String lowerStatus = status.toLowerCase();
						
							// Check for Private Key condition
							if (lowerDescription.startsWith("i get text") 
									&& lowerDescription.contains("secret key") 
									&& "grabText".equalsIgnoreCase(cmd) 
									&& "success".equalsIgnoreCase(status)) {
								keys.setPrivateKey(step.path("return").asText());
								log.debug("PA Key: {}", keys.getPrivateKey());
							}
				
							// Check for Public Key condition
							if (lowerDescription.startsWith("i get text") 
									&& lowerDescription.contains("public key") 
									&& "grabText".equalsIgnoreCase(cmd) 
									&& "success".equalsIgnoreCase(status)) {
								keys.setPublicKey(step.path("return").asText());
								log.debug("PU Key: {}", keys.getPublicKey());
							}

							if (lowerDescription.startsWith("i get text") 
									&& (lowerDescription.contains("secret key") 
									||lowerDescription.contains("public key"))
									&& "grabText".equalsIgnoreCase(cmd) 
									&& "failed".equalsIgnoreCase(status)) {

										vo.setStatus("FAILED");
										entity = promptCraftSubscriptionsAssembler.toEntity(vo);
										jpaRepo.save(entity);
								}
						
						}
		
						if(keys.getPrivateKey() != null && keys.getPublicKey() != null){

							GenericMessage vaultResponse = vaultAuthClient.createSubscriptionKeys(projectName,keys);
							if(vaultResponse!=null && "SUCCESS".equalsIgnoreCase(vaultResponse.getSuccess())){
								log.info("Sucessfully added subscription keys to vault");
								vo.setStatus("COMPLETED");
								entity = promptCraftSubscriptionsAssembler.toEntity(vo);
								jpaRepo.save(entity);
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
    
}


