// /* LICENSE START
//  *
//  * MIT License
//  *
//  * Copyright (c) 2019 Daimler TSS GmbH
//  *
//  * Permission is hereby granted, free of charge, to any person obtaining a copy
//  * of this software and associated documentation files (the "Software"), to deal
//  * in the Software without restriction, including without limitation the rights
//  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  * copies of the Software, and to permit persons to whom the Software is
//  * furnished to do so, subject to the following conditions:
//  *
//  * The above copyright notice and this permission notice shall be included in all
//  * copies or substantial portions of the Software.
//  *
//  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//  * SOFTWARE.
//  *
//  * LICENSE END
//  */

//  package com.daimler.data.auth.vault;

//  import com.daimler.data.application.config.VaultConfig;
//  import com.daimler.data.controller.exceptions.GenericMessage;
//  import com.daimler.data.controller.exceptions.MessageDescription;
//  import com.daimler.data.dto.auth.ApiKeyValidationResponseVO;
//  import com.daimler.data.dto.auth.ApiKeyValidationVO;
//  import com.daimler.data.dto.forecast.ForecastVO;
//  import com.daimler.data.dto.vault.VaultGenericResponse;
//  import com.daimler.data.service.forecast.ForecastService;

// import lombok.extern.slf4j.Slf4j;

// import org.json.JSONException;
//  import org.json.JSONObject;
//  import org.slf4j.Logger;
//  import org.slf4j.LoggerFactory;
//  import org.springframework.beans.factory.annotation.Autowired;
//  import org.springframework.http.HttpEntity;
//  import org.springframework.http.HttpHeaders;
//  import org.springframework.http.HttpMethod;
//  import org.springframework.http.ResponseEntity;
//  import org.springframework.stereotype.Component;
 
//  import java.util.ArrayList;
//  import java.util.List;
 
//  @Component
//  @Slf4j
//  public class VaultAuthClientImpl implements VaultAuthClient {

 
//      @Autowired
//      private ForecastService service;
 
//      @Autowired
//      private VaultConfig vaultConfig;
 
//      @Override
//      public ApiKeyValidationResponseVO validateApiKey(ApiKeyValidationVO apiKeyValidationVO) {
//          ApiKeyValidationResponseVO validationResponseVO = new ApiKeyValidationResponseVO();
//          List<MessageDescription> messages = new ArrayList<>();
//          MessageDescription message = null;
//          String uniqueAppId = apiKeyValidationVO.getAppId();
//          if (uniqueAppId != null) {
//              LOGGER.info("Entering validateApiKey for appId {}", uniqueAppId);
//              VaultGenericResponse vaultResponse = vaultConfig.validateApiKey(apiKeyValidationVO.getAppId(),
//                      apiKeyValidationVO.getApiKey());
//              if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
//                  LOGGER.info("Valid apikey for appId {}", uniqueAppId);
//                  validationResponseVO.setValidApiKey(true);
//              } else {
//                  message = new MessageDescription();
//                  LOGGER.error("Failed to validate apikey for appId {}", uniqueAppId);
//                  message.setMessage("Failed to validate");
//                  messages.add(message);
//                  validationResponseVO.setValidApiKey(false);
//              }
//          }
//          return validationResponseVO;
//      }
 
//      @Override
//      public GenericMessage createApiKey(String appId, String appKey) {
//          GenericMessage responseMessage = new GenericMessage();
//          List<MessageDescription> errors = new ArrayList<>();
 
//          if (appId != null) {
//              VaultGenericResponse vaultResponse = vaultConfig.createApiKey(appId,
//                      appKey);
//              if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
//                  LOGGER.info("create apikey for appId {} is success", appId);
//                  responseMessage.setSuccess("SUCCESS");
//              } else {
//                  MessageDescription msg = new MessageDescription("Failed to create apikey for appId " + appId);
//                  LOGGER.error("Failed to create apikey for appId {}", appId);
//                  responseMessage.setSuccess("FAILED");
//                  errors.add(msg);
//                  responseMessage.setErrors(errors);
//              }
//          }
//          return responseMessage;
//      }
 
//      @Override
//      public GenericMessage updateApiKey(String appId, String appKey) {
//          GenericMessage responseMessage = new GenericMessage();
//          List<MessageDescription> errors = new ArrayList<>();
 
//          if (appId != null) {
//              VaultGenericResponse vaultResponse = vaultConfig.updateApiKey(appId,
//                      appKey);
//              if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
//                  LOGGER.info("update apikey for appId {} is success", appId);
//                  responseMessage.setSuccess("SUCCESS");
//              } else {
//                  MessageDescription msg = new MessageDescription("Failed to update apikey for appId " + appId);
//                  LOGGER.error("Failed to update apikey for appId {}", appId);
//                  responseMessage.setSuccess("FAILED");
//                  errors.add(msg);
//                  responseMessage.setErrors(errors);
//              }
//          }
//          return responseMessage;
//      }
 
//      @Override
//      public String getApiKeys(String appId) {
//          String apiKey = null;
//          if (appId != null) {
//              String response = vaultConfig.getApiKey(appId);
//              if (response != null) {
//                  apiKey = response;
//              } else {
//                  LOGGER.error("failed to get api key for appId {}", appId);
//              }
//          }
//          return apiKey;
//      }
 
//      @Override
//      public JSONObject getUserDetails(String appId) {
//          JSONObject res = null;
 
//          try {
//              ForecastVO existingForecast = service.getById(appId);
//              if(existingForecast!=null) {
//                  res = (JSONObject) new JSONObject(existingForecast.getCreatedBy());
 
//                  LOGGER.info("existingForecast" + res);
//              }
//          } catch (Exception e) {
//              LOGGER.error("Error occurred while getting user detailse: {}", e.getMessage());
//              throw e;
//          }
 
//          return res;
//      }
//  }
 