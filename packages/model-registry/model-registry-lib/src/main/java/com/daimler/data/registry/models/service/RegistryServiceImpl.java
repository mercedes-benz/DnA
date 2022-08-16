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

package com.daimler.data.registry.models.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.model.ModelCollection;
import com.daimler.data.dto.model.ModelExternalUrlVO;
import com.daimler.data.dto.model.ModelRequestVO;
import com.daimler.data.dto.model.ModelResponseVO;
import com.daimler.data.kong.client.KongClient;
import com.daimler.data.registry.config.KubernetesClient;
import com.daimler.data.registry.config.MinioConfig;
import com.daimler.data.registry.config.VaultConfig;
import com.daimler.data.registry.dto.VaultGenericResponse;
import com.daimler.data.registry.util.AppIdGenerator;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Service;
import io.minio.MinioClient;

@Service
public class RegistryServiceImpl implements RegistryService {

	private static Logger LOGGER = LoggerFactory.getLogger(RegistryServiceImpl.class);

	@Autowired
	private KubernetesClient kubeClient;

	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	private VaultConfig vaultConfig;

	@Autowired
	private UserStore userStore;

	@Autowired
	private KongClient kongClient;

	@Value("${model.host}")
	private String host;

	@Value("${model.backendServiceSuffix}")
	private String backendServiceSuffix;

	@Value("${minio.endpoint}")
	private String endpoint;

	@Value("${minio.accessKey}")
	private String accessKey;

	@Value("${minio.secretKey}")
	private String secretKey;

	@Override
	public ResponseEntity<ModelCollection> getAllModels() {
		ModelCollection modelCollection = new ModelCollection();
		try {
			UserInfo currentUser = this.userStore.getUserInfo();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (StringUtils.hasText(userId)) {
				MinioClient minioClient = minioConfig.getMinioClient(endpoint, accessKey, secretKey);
				modelCollection = minioConfig.getModels(minioClient, userId);
				if (modelCollection != null && modelCollection.getData() != null
						&& !modelCollection.getData().isEmpty()) {
					LOGGER.info("returning successfully with models");
					return new ResponseEntity<>(modelCollection, HttpStatus.OK);
				} else {
					LOGGER.info("returning empty no models found");
					return new ResponseEntity<>(modelCollection, HttpStatus.NO_CONTENT);
				}
			} else {
				LOGGER.error("UserId  is null or empty");
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("UserId is null or empty");
				messages.add(message);
				modelCollection.setErrors(messages);
				return new ResponseEntity<>(modelCollection, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get buckets objects for given user, exception occured is : {}", e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			modelCollection.setErrors(messages);
			return new ResponseEntity<>(modelCollection, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<ModelResponseVO> generateExternalUrl(ModelRequestVO modelRequestVO) {
		ModelResponseVO modelResponseVO = new ModelResponseVO();
		ModelExternalUrlVO modelExternalUrlVO = new ModelExternalUrlVO();
		String modelName = modelRequestVO.getData().getModelName();
		modelExternalUrlVO.setModelName(modelName);
		String url = "";
		String appId = "";
		String appKey = "";
		String dataToEncrypt = "";
		try {
			String[] modelPath = modelName.split("/");
			String[] userId = modelPath[0].split("-");
			if (modelPath.length < 2 || userId.length != 2) {
				LOGGER.debug("ModelName :  {} is invalid ", modelName);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Given modelName is invalid");
				messages.add(message);
				modelResponseVO.setData(modelExternalUrlVO);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.BAD_REQUEST);
			}
			String metaDataNamespace = modelPath[0];
			String metaDataName = userId[1];
			String backendServiceName = "";
			for (int i = 1; i < modelPath.length - 1; i++) {
				metaDataName += "-" + modelPath[i];
				backendServiceName += modelPath[i] + "-";
			}
			if (StringUtils.hasText(backendServiceName)) {
				backendServiceName = backendServiceName.substring(0, backendServiceName.length() - 1);
			}

			String kongPath = "/v1/models/" + backendServiceName + ":predict";
			String path = "/model-serving/v1/models/" + backendServiceName + ":predict";
			dataToEncrypt = path;

			backendServiceName += "-" + backendServiceSuffix;

			url = "https://" + host +  path;
			V1Service service = kubeClient.getModelService(metaDataNamespace, backendServiceName);
			if (service == null) {
				modelResponseVO.setData(modelExternalUrlVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Error while getting service details for given model");
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.NOT_FOUND);
			}
			appId = AppIdGenerator.encrypt(dataToEncrypt);
			appKey = UUID.randomUUID().toString();

			// create service , route and attachJwtPluginToService
			String kongServiceName = "kfp-service-" + metaDataName;
			String kongServiceUrl = "http://" + backendServiceName + "." + metaDataNamespace + ".svc.cluster.local"
					+ kongPath;
			String kongRoutePaths = path;

			boolean serviceStatus = kongClient.createService(kongServiceName, kongServiceUrl);
			boolean routeStatus = kongClient.createRoute(kongRoutePaths, kongServiceName);
			boolean attachStatus = kongClient.attachJwtPluginToService(kongServiceName);

			if (serviceStatus && routeStatus && attachStatus) {
				VaultGenericResponse vaultResponse = vaultConfig.createAppKey(appId, appKey);
				if (vaultResponse != null && "200".equals(vaultResponse.getStatus())) {
					LOGGER.info("AppId and AppKey created successfully");
					modelExternalUrlVO.setAppId(appId);
					modelExternalUrlVO.setAppKey(appKey);
				} else {
					LOGGER.error("Failed to create appId while exposing model {} ", modelName);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to create appId due to internal error");
					messages.add(message);
					modelResponseVO.setData(modelExternalUrlVO);
					modelResponseVO.setErrors(messages);
					return new ResponseEntity<>(modelResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				kubeClient.getUrl(metaDataNamespace, metaDataName, backendServiceName, path);
			}

			modelExternalUrlVO.setExternalUrl(url);
			modelResponseVO.setData(modelExternalUrlVO);
			LOGGER.info("Model {} exposed successfully", modelName);
			return new ResponseEntity<>(modelResponseVO, HttpStatus.OK);
		} catch (ApiException ex) {
			LOGGER.error("ApiException occurred while exposing model. Exception is {} ", ex.getResponseBody());
			if (ex.getCode() == HttpStatus.CONFLICT.value()) {
				modelExternalUrlVO.setAppId(appId);
				modelExternalUrlVO.setAppKey(appKey);
				modelExternalUrlVO.setExternalUrl(url);
				modelResponseVO.setData(modelExternalUrlVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage(ex.getResponseBody());
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.OK);
			} else if (ex.getCode() == HttpStatus.FORBIDDEN.value()) {
				modelResponseVO.setData(modelExternalUrlVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage(ex.getResponseBody());
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.FORBIDDEN);
			} else {
				modelResponseVO.setData(modelExternalUrlVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage(ex.getMessage());
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while exposing model {} ", e.getMessage(), modelName);
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			modelResponseVO.setData(modelExternalUrlVO);
			modelResponseVO.setErrors(messages);
			return new ResponseEntity<>(modelResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
