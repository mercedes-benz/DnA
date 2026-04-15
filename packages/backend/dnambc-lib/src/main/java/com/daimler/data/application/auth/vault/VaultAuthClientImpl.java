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

package com.daimler.data.application.auth.vault;

import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysVO;
import com.daimler.data.dto.vault.VaultGenericResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VaultAuthClientImpl implements VaultAuthClient {

    private Logger LOGGER = LoggerFactory.getLogger(VaultAuthClientImpl.class);

    @Autowired
    private VaultConfig vaultConfig;

    // @Override
    // public ApiKeyValidationResponseVO validateApiKey(ApiKeyValidationVO apiKeyValidationVO) {
    //     ApiKeyValidationResponseVO validationResponseVO = new ApiKeyValidationResponseVO();
    //     List<MessageDescription> messages = new ArrayList<>();
    //     MessageDescription message = null;
    //     String uniqueprojectName = apiKeyValidationVO.getprojectName();
    //     if (uniqueprojectName != null) {
    //         LOGGER.info("Entering validateApiKey for projectName {}", uniqueprojectName);
    //         VaultGenericResponse vaultResponse = vaultConfig.validateApiKey(apiKeyValidationVO.getprojectName(),
    //                 apiKeyValidationVO.getApiKey());
    //         if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
    //             LOGGER.info("Valid apikey for projectName {}", uniqueprojectName);
    //             validationResponseVO.setValidApiKey(true);
    //         } else {
    //             message = new MessageDescription();
    //             LOGGER.error("Failed to validate apikey for projectName {}", uniqueprojectName);
    //             message.setMessage("Failed to validate");
    //             messages.add(message);
    //             validationResponseVO.setValidApiKey(false);
    //         }
    //     }
    //     return validationResponseVO;
    // }

    @Override
    public GenericMessage createSubscriptionKeys(String projectName, SubscriptionkeysVO subscriptionKeys) {
        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();

        if (projectName != null) {
            VaultGenericResponse vaultResponse = vaultConfig.createSubscriptionKeys(projectName,
            subscriptionKeys);
            if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
                LOGGER.info("create apikey for projectName {} is success", projectName);
                responseMessage.setSuccess("SUCCESS");
            } else {
                MessageDescription msg = new MessageDescription("Failed to create apikey for projectName " + projectName);
                LOGGER.error("Failed to create apikey for projectName {}", projectName);
                responseMessage.setSuccess("FAILED");
                errors.add(msg);
                responseMessage.setErrors(errors);
            }
        }
        return responseMessage;
    }

    @Override
    public GenericMessage updateSubscriptionKeys(String projectName, SubscriptionkeysVO subscriptionKeys) {
        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();

        if (projectName != null) {
            VaultGenericResponse vaultResponse = vaultConfig.updateSubscriptionKeys(projectName,
            subscriptionKeys);
            if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
                LOGGER.info("update subscriptionKeys for projectName {} is success", projectName);
                responseMessage.setSuccess("SUCCESS");
            } else {
                MessageDescription msg = new MessageDescription("Failed to update apikey for projectName " + projectName);
                LOGGER.error("Failed to update subscriptionKeys for projectName {}", projectName);
                responseMessage.setSuccess("FAILED");
                errors.add(msg);
                responseMessage.setErrors(errors);
            }
        }
        return responseMessage;
    }

    @Override
    public SubscriptionkeysVO getSubscriptionKeys(String projectName) {
        SubscriptionkeysVO subscriptionKeys = null;
        if (projectName != null) {
            SubscriptionkeysVO response = vaultConfig.getSubscriptionKeys(projectName);
            if (response != null) {
                subscriptionKeys = response;
            } else {
                LOGGER.error("failed to get api key for projectName {}", projectName);
            }
        }
        return subscriptionKeys;
    }

    // @Override
    // public JSONObject getUserDetails(String projectName) {
    //     JSONObject res = null;

    //     try {
    //         ForecastVO existingForecast = service.getById(projectName);
    //         if(existingForecast!=null) {
    //             res = (JSONObject) new JSONObject(existingForecast.getCreatedBy());

    //             LOGGER.info("existingForecast" + res);
    //         }
    //     } catch (Exception e) {
    //         LOGGER.error("Error occurred while getting user detailse: {}", e.getMessage());
    //         throw e;
    //     }

    //     return res;
    // }
}
