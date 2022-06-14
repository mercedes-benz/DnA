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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.MinioKeyDetails;
import com.daimler.data.dto.MinioSecretMetadata;
import com.daimler.data.dto.model.ModelCollection;
import com.daimler.data.dto.model.ModelExternalUriVO;
import com.daimler.data.dto.model.ModelRequestVO;
import com.daimler.data.dto.model.ModelResponseVO;
import com.daimler.data.registry.config.KubernetesClient;
import com.daimler.data.registry.config.MinioConfig;

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

	@Value("${model.host}")
	private String host;

	@Value("${model.backendServiceSuffix}")
	private String backendServiceSuffix;

	@Override
	public ModelCollection getAllModels(String userId) {
		if (userId != null && !"".equalsIgnoreCase(userId)) {
			MinioSecretMetadata minioDetails = kubeClient.getKubeflowMinioSpec();
			String endpoint = "http://" + minioDetails.getHost() + ":" + minioDetails.getPort();
			if (minioDetails != null) {
				MinioKeyDetails keyDetails = minioDetails.getStringData();
				MinioClient minioClient = minioConfig.getMinioClient(endpoint, keyDetails.getAccesskey(),
						keyDetails.getSecretkey());
				return minioConfig.getModels(minioClient, userId);
			}
		}
		return null;
	}

	@Override
	public ResponseEntity<ModelResponseVO> generateExternalUri(ModelRequestVO modelRequestVO) {
		ModelResponseVO modelResponseVO = new ModelResponseVO();
		ModelExternalUriVO modelExternalUriVO = new ModelExternalUriVO();
		String modelName = modelRequestVO.getData().getModelName();
		modelExternalUriVO.setModelName(modelName);
		String uri = "";
		try {
			String[] modelPath = modelName.split("/");
			String[] userId = modelPath[0].split("-");
			if (modelPath.length < 2 || userId.length != 2) {
				LOGGER.debug("ModelName :  {} is invalid ", modelName);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Given modelName is invalid");
				messages.add(message);
				modelResponseVO.setData(modelExternalUriVO);
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

			String path = "/v1/models/" + backendServiceName + ":predict";

			backendServiceName += "-" + backendServiceSuffix;

			uri = "https://" + host + path;
			V1Service service = kubeClient.getModelService(metaDataNamespace, backendServiceName);
			if (service == null) {
				modelResponseVO.setData(modelExternalUriVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Error while getting service details for given model");
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.NOT_FOUND);
			}
			kubeClient.getUri(metaDataNamespace, metaDataName, backendServiceName, path);
			modelExternalUriVO.setExternalUri(uri);
			modelResponseVO.setData(modelExternalUriVO);
			LOGGER.info("Model {} exposed successfully", modelName);
			return new ResponseEntity<>(modelResponseVO, HttpStatus.OK);
		} catch (ApiException ex) {
			LOGGER.error("ApiException occurred while exposing model. Exception is {} ", ex.getResponseBody());
			if (ex.getCode() == HttpStatus.CONFLICT.value()) {
				modelExternalUriVO.setExternalUri(uri);
				modelResponseVO.setData(modelExternalUriVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage(ex.getResponseBody());
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.CONFLICT);
			} else if (ex.getCode() == HttpStatus.FORBIDDEN.value()) {
				modelResponseVO.setData(modelExternalUriVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage(ex.getResponseBody());
				messages.add(message);
				modelResponseVO.setErrors(messages);
				return new ResponseEntity<>(modelResponseVO, HttpStatus.FORBIDDEN);
			} else {
				modelResponseVO.setData(modelExternalUriVO);
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
			modelResponseVO.setData(modelExternalUriVO);
			modelResponseVO.setErrors(messages);
			return new ResponseEntity<>(modelResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
