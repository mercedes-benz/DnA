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

package com.daimler.data.kong.client;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class KongClientImpl implements KongClient {

	private Logger LOGGER = LoggerFactory.getLogger(KongClientImpl.class);

	@Value("${kong.uri}")
	private String kongBaseUri;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public boolean createService(String serviceName, String url) {
		try {
			String kongUri = kongBaseUri + "/services/";

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");

			MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
			requestBody.add("name", serviceName);
			requestBody.add("url", url);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
					requestBody, headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.POST, request, String.class);

			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("Kong service:{} created successfully", serviceName);
					return true;
				}
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("Kong service:{} already exists", serviceName);
				return true;
			}
			throw ex;
		} catch (Exception e) {
			LOGGER.error("Error while creating service for kong :{}", e.getMessage());
			throw e;
		}
		return false;
	}

	@Override
	public boolean createRoute(String path, String serviceName) {
		try {
			String kongUri = kongBaseUri + "/services/" + serviceName + "/routes";

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");

			// get available routes
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> routes = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
			if (routes != null && routes.hasBody() && routes.getStatusCode() == HttpStatus.OK) {
				JSONArray array = (JSONArray) new JSONObject(routes.getBody()).getJSONArray("data");
				if (array != null && !array.isEmpty()) {
					LOGGER.info("Route already exist for service:{}", serviceName);
					return true;
				}
			}

			// create new route
			MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
			requestBody.add("paths", path);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
					requestBody, headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.POST, request, String.class);

			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("Route created successfully for service:{}", serviceName);
					return true;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while creating routes for service :{}", e.getMessage());
			throw e;
		}
		return false;
	}

	@Override
	public boolean attachJwtPluginToService(String serviceName) {

		try {
			String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");

			MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
			requestBody.add("name", "jwt");
			requestBody.add("config.claims_to_verify", "exp");
			requestBody.add("config.key_claim_name", "kid");

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
					requestBody, headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.POST, request, String.class);

			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("Jwt plugin attached successfully to service: {}", serviceName);
					return true;
				}
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("Jwt plugin already attached to service: {}", serviceName);
				return true;
			}
			throw ex;
		} catch (Exception e) {
			LOGGER.error("Error while attaching jwt plugin to service: {}", e.getMessage());
			throw e;
		}
		return false;
	}

}
