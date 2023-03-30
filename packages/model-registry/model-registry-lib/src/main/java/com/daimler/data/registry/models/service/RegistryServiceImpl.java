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
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
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
import com.daimler.data.registry.util.AppIdGenerator;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ServiceList;
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
				List<String> results = minioConfig.getModels(minioClient, userId);
				if (!ObjectUtils.isEmpty(results)) {
					String[] modelPath = results.get(0).split("/");
					String namespace = modelPath[0];
					List<String> finalModels = getAvailableModelList(namespace, results);
					if (!ObjectUtils.isEmpty(finalModels)) {
						modelCollection.setData(finalModels);
						LOGGER.info("returning successfully with models");
						return new ResponseEntity<>(modelCollection, HttpStatus.OK);
					}
				}
				LOGGER.info("returning empty no models found");
				return new ResponseEntity<>(modelCollection, HttpStatus.NO_CONTENT);

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

	private List<String> getAvailableModelList(String namespace, List<String> models) throws ApiException {
		List<String> availableModelList = new ArrayList<>();
		V1ServiceList v1ServiceList = kubeClient.getModelService(namespace);
		if (v1ServiceList != null && !ObjectUtils.isEmpty(v1ServiceList.getItems())) {
			List<String> serviceNameList = v1ServiceList.getItems().stream().map(item -> item.getMetadata().getName())
					.collect(Collectors.toList());
			Map<String, String> modelNameMap = models.stream()
					.collect(Collectors.toMap(Function.identity(), item -> getServiceName(item.split("/"))));
			modelNameMap.values().retainAll(serviceNameList);
			availableModelList = modelNameMap.keySet().stream().collect(Collectors.toList());
		}
		return availableModelList;
	}

	private String getServiceName(String[] modelPath) {
		String serviceName = "";
		for (int i = 0; i < modelPath.length - 1; i++) {
			serviceName += modelPath[i] + "-";
		}
		if (StringUtils.hasText(serviceName)) {
			serviceName = serviceName.substring(0, serviceName.length() - 1);
		}
		return serviceName;
	}

	@Override
	public ResponseEntity<ModelResponseVO> generateExternalUrl(ModelRequestVO modelRequestVO) {
		ModelResponseVO modelResponseVO = new ModelResponseVO();
		ModelExternalUrlVO modelExternalUrlVO = new ModelExternalUrlVO();
		String modelName = modelRequestVO.getData().getModelName();
		modelExternalUrlVO.setModelName(modelName);

		try {
			String[] modelPath = modelName.split("/");
			String namespace = modelPath[0];
			UserInfo currentUser = userStore.getUserInfo();
			String shortId = currentUser.getId();
			if (modelPath.length < 2 || namespace.split("-").length != 2) {
				LOGGER.debug("ModelName :  {} is invalid ", modelName);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Given modelName is invalid");
				messages.add(message);
				modelResponseVO.setData(modelExternalUrlVO);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.BAD_REQUEST);
			}

			String userId = namespace.split("-")[1];
			String serviceName = getServiceName(modelPath);
			String kongServiceName = userId + "-" + serviceName;
			String kongServiceUrl = "http://" + serviceName + "." + namespace + ".svc.cluster.local/v1/models/"
					+ serviceName + ":predict";
			String kongRoutePaths = "/model-serving/v1/models/" + shortId +"/"+ serviceName + ":predict";
			String url = "https://" + host + kongRoutePaths;
			String appId = AppIdGenerator.encrypt(kongServiceName);
			String appKey = UUID.randomUUID().toString();

			// create service , route and attachJwtPluginToService
			boolean serviceStatus = kongClient.createService(kongServiceName, kongServiceUrl);
			boolean routeStatus = kongClient.createRoute(kongRoutePaths, kongServiceName);
			boolean attachStatus = kongClient.attachJwtPluginToService(kongServiceName);

			if (serviceStatus && routeStatus && attachStatus) {
				String key = vaultConfig.createAppKey(appId, appKey);
				if (StringUtils.hasText(key)) {
					modelExternalUrlVO.setAppId(appId);
					modelExternalUrlVO.setAppKey(key);
					modelExternalUrlVO.setExternalUrl(url);
					modelResponseVO.setData(modelExternalUrlVO);
					LOGGER.info("Model {} exposed successfully", modelName);
					return new ResponseEntity<>(modelResponseVO, HttpStatus.OK);
				}
			}
			LOGGER.error("Error while exposing model {} ", modelName);
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to expose model due to internal error");
			messages.add(message);
			modelResponseVO.setData(modelExternalUrlVO);
			modelResponseVO.setErrors(messages);
			return new ResponseEntity<>(modelResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
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
