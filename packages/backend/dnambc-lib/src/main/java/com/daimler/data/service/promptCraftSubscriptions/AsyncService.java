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

	private static final int MAX_RETRIES = 8; // 2 minutes with 10 seconds interval
    private static final int RETRY_INTERVAL_MS = 15000; // 10 seconds


	@Async
	public void checkForKeysFromUiLicious(String projectName, String runId) {
        int retries = 0;
        boolean stepsSizeSufficient = false;

		SubscriptionkeysVO keys = new SubscriptionkeysVO();

		PromptCraftSubscriptionsNsql entity = null;

		log.info("checkForKeysFromUiLicious started for projectName={}, runId={}", projectName, runId);

		PromptCraftSubscriptionsVO vo = service.getByUniqueliteral("projectName",projectName);
		if(!"COMPLETED".equalsIgnoreCase(vo.getStatus())){

		
			while (retries < MAX_RETRIES && !stepsSizeSufficient) {
				log.info("checkForKeysFromUiLicious retry={}/{} for projectName={}, runId={}", retries, MAX_RETRIES, projectName, runId);
				JsonNode jsonResponse = uiLiciousClient.getSubscriptionRunDetails(runId);
				if (jsonResponse != null) {
					log.info("Received response from UiLicious for runId={}", runId);

					JsonNode stepsNode = jsonResponse.path("result").path("result").path("steps");
					log.info("Steps count from UiLicious for runId={} : {}", runId, stepsNode.isArray() ? stepsNode.size() : "not an array");
					if (stepsNode.isArray() && stepsNode.size() >= 10) {
						stepsSizeSufficient = true;
						log.info("Steps size is sufficient: {} for runId={}", stepsNode.size(), runId);

						for (JsonNode step : stepsNode) {
							int stepNum = step.path("stepNum").asInt();
							String status = step.path("status").asText();
							String description = step.path("description").asText();
							String cmd = step.path("cmd").asText();
							String error = step.path("error").asText();
							String returnVal = step.path("return").asText();
						
							log.info("Step[{}] cmd={} status={} description='{}' return='{}' error='{}'",
									stepNum, cmd, status, description, returnVal, error);

							// Normalize for case-insensitive checks
							String lowerDescription = description.toLowerCase();
							String lowerStatus = status.toLowerCase();
							String lowerError = error.toLowerCase();
						
							// Check for Private Key - supports both UiLicious patterns:
							// Pattern 1: "//div[text()='Secret Key']//..//code"
							// Pattern 2: "(//div[@data-sentry-component='CodeView']//code)[1]"
							if (lowerDescription.startsWith("i get text") 
									&& (lowerDescription.contains("'secret key'") || lowerDescription.contains("code)[1]"))
									&& "grabText".equalsIgnoreCase(cmd) 
									&& "success".equalsIgnoreCase(status)) {
								keys.setPrivateKey(step.path("return").asText());
								log.info("Private Key extracted at step {} with value starting: {}...", stepNum, 
									returnVal.length() > 10 ? returnVal.substring(0, 10) : returnVal);
							}
				
							// Check for Public Key - supports both UiLicious patterns:
							// Pattern 1: "//div[text()='Public Key']//..//code"
							// Pattern 2: "(//div[@data-sentry-component='CodeView']//code)[2]"
							if (lowerDescription.startsWith("i get text") 
									&& (lowerDescription.contains("'public key'") || lowerDescription.contains("code)[2]"))
									&& "grabText".equalsIgnoreCase(cmd) 
									&& "success".equalsIgnoreCase(status)) {
								keys.setPublicKey(step.path("return").asText());
								log.info("Public Key extracted at step {} with value starting: {}...", stepNum,
									returnVal.length() > 10 ? returnVal.substring(0, 10) : returnVal);
							}

							// Check for failures when trying to extract keys
							if (lowerDescription.startsWith("i get text") 
									&& (lowerDescription.contains("'secret key'") || lowerDescription.contains("'public key'")
									    || lowerDescription.contains("code)[1]") || lowerDescription.contains("code)[2]"))
									&& "grabText".equalsIgnoreCase(cmd) 
									&& "failure".equalsIgnoreCase(status)) {

										vo.setStatus("FAILED");
										entity = promptCraftSubscriptionsAssembler.toEntity(vo);
										jpaRepo.save(entity);
										log.info("Failed extracting key at step {}: {}", stepNum, error);
								}

								if (lowerError.startsWith("i don't see") 
								&& (lowerDescription.contains("codeview") || lowerDescription.contains("secret") || lowerDescription.contains("public"))
								&& "grabText".equalsIgnoreCase(cmd) 
								&& "failure".equalsIgnoreCase(status)) {

									vo.setStatus("FAILED");
									entity = promptCraftSubscriptionsAssembler.toEntity(vo);
									jpaRepo.save(entity);
									log.info("Failed finding key element at step {}: {}", stepNum, error);
							}
						
						}

						log.info("After processing all steps for runId={}: privateKeyFound={}, publicKeyFound={}",
								runId, keys.getPrivateKey() != null, keys.getPublicKey() != null);
		
						if(keys.getPrivateKey() != null && keys.getPublicKey() != null){

							String userID = service.getPromptCraftSubscriptionUserID( keys.getPublicKey(), keys.getPrivateKey());
							if( userID != null) {
								keys.setUserID(userID);
							log.info("PromptCraft userID obtained successfully for projectName={}", projectName);
							
							GenericMessage vaultResponse = vaultAuthClient.createSubscriptionKeys(projectName,keys);
							if(vaultResponse!=null && "SUCCESS".equalsIgnoreCase(vaultResponse.getSuccess())){
								log.info("Successfully added subscription keys to vault for projectName={}", projectName);
								vo.setStatus("COMPLETED");
								entity = promptCraftSubscriptionsAssembler.toEntity(vo);
								jpaRepo.save(entity);
							} else {
								vo.setStatus("FAILED");
								entity = promptCraftSubscriptionsAssembler.toEntity(vo);
								jpaRepo.save(entity);
								log.error("Failed to store keys in Vault for projectName={}. VaultResponse: {}", 
									projectName, vaultResponse != null ? vaultResponse.getSuccess() : "null");
							}
						}
						else {
							vo.setStatus("FAILED");
							entity = promptCraftSubscriptionsAssembler.toEntity(vo);
							jpaRepo.save(entity);
							log.error("Failed to get PromptCraft userID for projectName={}", projectName);
						}
					} else {
						log.warn("Keys not found after processing all steps for projectName={}. privateKey={}, publicKey={}", 
							projectName, keys.getPrivateKey() != null, keys.getPublicKey() != null);
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
