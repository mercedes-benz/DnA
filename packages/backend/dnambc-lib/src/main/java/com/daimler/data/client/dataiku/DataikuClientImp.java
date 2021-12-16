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

package com.daimler.data.client.dataiku;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.dataiku.DataikuPermission;
import com.daimler.data.dto.dataiku.DataikuProjectVO;
import com.daimler.data.dto.dataiku.DataikuUserRole;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class DataikuClientImp implements DataikuClient {

	private Logger LOGGER = LoggerFactory.getLogger(DataikuClientImp.class);

	@Value("${dataiku.production.uri}")
	private String productionUri;

	@Value("${dataiku.production.apiKey}")
	private String productionApiKey;

	@Value("${dataiku.training.uri}")
	private String trainingUri;

	@Value("${dataiku.training.apiKey}")
	private String trainingApiKey;

	@Autowired
	RestTemplate restTemplate;

	private static final String PROJECTS = "/projects/";
	private static final String USER_ROLES = "/admin/users/";
	private static final String PERMISSIONS = "/permissions";
	private static final String EMEA_CORPDIR = "@emea.corpdir.net";
	private static final String APAC_CORPDIR = "@apac.corpdir.net";

	/**
	 * <p>
	 * To get all projects of dataiku PRODUCTION/TRAINING
	 * </p>
	 * 
	 * @param isTrainingData
	 * @return DataikuProjectVOCollection
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Optional<List<DataikuProjectVO>> getAllDataikuProjects(Boolean live) {
		LOGGER.trace("Entering getAllDataikuProjects");
		List<DataikuProjectVO> projects = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			String dataikuUri = setDataikuUri(live, headers, PROJECTS);
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(dataikuUri, HttpMethod.GET, entity, String.class);
			if (response != null && response.hasBody()) {
				LOGGER.info("Success from dataiku");
				projects = new ArrayList<>();
				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				projects = mapper.readValue(response.getBody(), new TypeReference<List<DataikuProjectVO>>() {
				});
			}
		} catch (JsonParseException e) {
			LOGGER.error("JsonParseException occured:{}", e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.error("JsonMappingException occured:{}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Error occured while calling dataiku service:{}", e.getMessage());
		}
		LOGGER.trace("returning from getAllDataikuProjects");
		return Optional.ofNullable(projects);
	}

	/**
	 * <p> To get user role </p>
	 * 
	 * @param userId
	 * @return DataikuUserRole
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Optional<DataikuUserRole> getDataikuUserRole(String userId, Boolean live) {
		LOGGER.trace("Entering getDataikuUserRole");
		DataikuUserRole userRole = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			String dataikuUri = setDataikuUri(live, headers, USER_ROLES);
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = null;
			try {
				LOGGER.info("Fetching details from @emea.corpdir.net");
				response = restTemplate.exchange(dataikuUri + userId.toLowerCase() + EMEA_CORPDIR, HttpMethod.GET,
						entity, String.class);
			} catch (Exception e) {
				LOGGER.error("error:{}", e.getMessage());
				LOGGER.info("Fetching details from @apac.corpdir.net");
				response = restTemplate.exchange(dataikuUri + userId.toLowerCase() + APAC_CORPDIR, HttpMethod.GET,
						entity, String.class);
			}
			if (response != null && response.hasBody()) {
				LOGGER.info("Success from dataiku");
				userRole = new DataikuUserRole();
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				userRole = mapper.readValue(response.getBody(), new TypeReference<DataikuUserRole>() {
				});
			}

		} catch (JsonParseException e) {
			LOGGER.error("JsonParseException occured:{}", e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.error("JsonMappingException occured:{}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Error occured while calling dataiku user role service:{}", e.getMessage());
		}
		LOGGER.trace("returning from getDataikuUserRole");
		return Optional.ofNullable(userRole);
	}

	/**
	 * <p> To get dataiku project permission </p>
	 * 
	 * @param projectKey
	 * @return DataikuPermission
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Optional<DataikuPermission> getDataikuProjectPermission(String projectKey, Boolean live) {
		LOGGER.trace("Entering getDataikuProjectPermission");
		DataikuPermission permission = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			String dataikuUri = setDataikuUri(live, headers, PROJECTS);
			dataikuUri = dataikuUri + projectKey + PERMISSIONS;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(dataikuUri, HttpMethod.GET, entity, String.class);
			if (response != null && response.hasBody()) {
				LOGGER.debug("Success from dataiku");
				permission = new DataikuPermission();
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				permission = mapper.readValue(response.getBody(), new TypeReference<DataikuPermission>() {
				});
			}

		} catch (JsonParseException e) {
			LOGGER.error("JsonParseException occured:{}", e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.error("JsonMappingException occured:{}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Error occured while calling dataiku user role service:{}", e.getMessage());
		}
		LOGGER.trace("returning from getDataikuProjectPermission");
		return Optional.ofNullable(permission);
	}

	@Override
	public Optional<DataikuProjectVO> getDataikuProject(String projectKey, Boolean live) {
		LOGGER.trace("Entering getDataikuProject");
		DataikuProjectVO project = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			String dataikuUri = setDataikuUri(live, headers, PROJECTS+projectKey);
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(dataikuUri, HttpMethod.GET, entity, String.class);
			if (response != null && response.hasBody()) {
				LOGGER.info("Success from dataiku");
				project = new DataikuProjectVO();
				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				project = mapper.readValue(response.getBody(), new TypeReference<DataikuProjectVO>() {
				});
			}
		} catch (JsonParseException e) {
			LOGGER.error("JsonParseException occured:{}", e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.error("JsonMappingException occured:{}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Error occured while calling dataiku service:{}", e.getMessage());
		}
		LOGGER.trace("returning from getDataikuProject");
		return Optional.ofNullable(project);
		
		
	}
	
	/**
	 * Setting dataiku uri and basic auth
	 * 
	 * @param live
	 * @param headers
	 * @param dataikuUri
	 * @param uriExtension
	 */
	private String setDataikuUri(Boolean live, HttpHeaders headers, String uriExtension) {
		LOGGER.debug("Entering setDataikuUri.");
		String dataikuUri = "";
		if (live) {
			LOGGER.debug("Forming uri for production environment");
			headers.setBasicAuth(productionApiKey, "");
			dataikuUri = productionUri + uriExtension;
		} else {
			LOGGER.debug("Forming uri for training environment");
			headers.setBasicAuth(trainingApiKey, "");
			dataikuUri = trainingUri + uriExtension;
		}
		LOGGER.debug("Returning from setDataikuUri.");
		return dataikuUri;
	}

}
