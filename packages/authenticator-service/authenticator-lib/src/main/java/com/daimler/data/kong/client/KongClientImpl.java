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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.kongGateway.AttachPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachPluginVO;
import com.daimler.data.dto.kongGateway.CreateRouteVO;

@Component
public class KongClientImpl implements KongClient {

	private Logger LOGGER = LoggerFactory.getLogger(KongClientImpl.class);

	@Value("${kong.uri}")
	private String kongBaseUri;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public GenericMessage createService(String serviceName, String url) {	
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
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
			ResponseEntity<String> response  = restTemplate.exchange(kongUri, HttpMethod.POST, request, String.class);
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					message.setSuccess("Success");		
					message.setErrors(errors);
					message.setWarnings(warnings);
					LOGGER.info("Kong service:{} created successfully", serviceName);
					return message;
				}
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("Kong service:{} already exists", serviceName);
				message.setSuccess("Failure");	
				messageDescription.setMessage("Kong service already exists");
				errors.add(messageDescription);
				message.setErrors(errors);
				message.setWarnings(warnings);
				return message;
			}
			LOGGER.error("Error occured while creating service: {} for kong :{}",serviceName, ex.getMessage());		
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			message.setWarnings(warnings);
			return message;
			
		} catch (Exception e) {
			LOGGER.error("Error while creating service for kong :{}", e.getMessage());		
			message.setSuccess("Failure");
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			message.setWarnings(warnings);
			return message;
		}
		return message;
	}

	@Override
	public GenericMessage createRoute(CreateRouteVO createRouteVO, String serviceName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		String routeName = createRouteVO.getName();
		String existingRouteName = "";
		List<String> routeNames = new ArrayList<>();
		String getKongUri = kongBaseUri + "/services/" + serviceName + "/routes";		
		String postKongUri = kongBaseUri + "/services/" + serviceName + "/routes";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		try {
			// get available routes
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> routes = restTemplate.exchange(getKongUri, HttpMethod.GET, entity, String.class);
			if (routes != null && routes.hasBody() && routes.getStatusCode() == HttpStatus.OK) {
				JSONArray array = (JSONArray) new JSONObject(routes.getBody()).getJSONArray("data");
				if (array != null && !array.isEmpty()) {
					for(int i=0; i<array.length(); i++) {
						JSONObject jsonObject = (JSONObject) array.get(i);
						if(Objects.nonNull(jsonObject.get("name"))) {
							existingRouteName = (String) jsonObject.get("name");
						}
								
						routeNames.add(existingRouteName);
					}														
				}
			}
			if(routeNames.contains(routeName)) {
				LOGGER.info("Route: {} already exist for service:{}", createRouteVO.getName(), serviceName);
				message.setSuccess("Failure");
				messageDescription.setMessage("Route already exist");
				errors.add(messageDescription);
				message.setErrors(errors);
				return message;
			}
			else {
				// create new route
				JSONObject requestBody = new JSONObject(); 
				requestBody.put("paths", new JSONArray(createRouteVO.getPaths()));
				requestBody.put("protocols", new JSONArray(createRouteVO.getProtocols())); 
				requestBody.put("hosts", new JSONArray(createRouteVO.getHosts())); 
				requestBody.put("name", routeName);   
				requestBody.put("strip_path", createRouteVO.isStripPath()); // Added strip_path 				
				HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
				ResponseEntity<String> response = restTemplate.exchange(postKongUri, HttpMethod.POST, request, String.class);
				if (response != null && response.hasBody()) {
					HttpStatus statusCode = response.getStatusCode();
					if (statusCode == HttpStatus.CREATED) {
						LOGGER.info("Route: {} created successfully for service:{}", createRouteVO.getName(), serviceName);
						message.setSuccess("Success");		
						message.setErrors(errors);
						message.setWarnings(warnings);
						return message;
					}
				}
			}
			
		} catch (Exception e) {	
			LOGGER.error("Exception while creating a route {} with error message {}",routeName, e.getMessage());	
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			message.setWarnings(warnings);
			return message;
		}
		return message;
		
	}

	@Override
	public GenericMessage attachPluginToService(AttachPluginVO attachPluginVO, String serviceName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			AttachPluginConfigVO attachPluginConfigVO = attachPluginVO.getConfig();
			String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			AttachPluginWrapperDto requestWrapper = new AttachPluginWrapperDto();			
			AttachPluginConfigRequestDto attachPluginConfigRequestDto = new AttachPluginConfigRequestDto();	
			ResponseEntity<String> response = null;
			if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("oidc")) {
				requestWrapper.setName(attachPluginVO.getName().name().toLowerCase());
				attachPluginConfigRequestDto.setBearer_only(attachPluginConfigVO.getBearerOnly());
				attachPluginConfigRequestDto.setClient_id(attachPluginConfigVO.getClientId());
				attachPluginConfigRequestDto.setClient_secret(attachPluginConfigVO.getClientSecret());
				attachPluginConfigRequestDto.setDiscovery(attachPluginConfigVO.getDiscovery());
				attachPluginConfigRequestDto.setIntrospection_endpoint(attachPluginConfigVO.getIntrospectionEndpoint());
				attachPluginConfigRequestDto.setIntrospection_endpoint_auth_method(attachPluginConfigVO.getIntrospectionEndpointAuthMethod());
				attachPluginConfigRequestDto.setLogout_path(attachPluginConfigVO.getLogoutPath());
				attachPluginConfigRequestDto.setRealm(attachPluginConfigVO.getRealm());
				attachPluginConfigRequestDto.setRedirect_after_logout_uri(attachPluginConfigVO.getRedirectAfterLogoutUri());
				attachPluginConfigRequestDto.setResponse_type(attachPluginConfigVO.getResponseType());
				attachPluginConfigRequestDto.setScope(attachPluginConfigVO.getScope());
				attachPluginConfigRequestDto.setSsl_verify(attachPluginConfigVO.getSslVerify());
				attachPluginConfigRequestDto.setToken_endpoint_auth_method(attachPluginConfigVO.getTokenEndpointAuthMethod());
				attachPluginConfigRequestDto.setRedirect_uri_path(attachPluginConfigVO.getRedirectUriPath());
				requestWrapper.setConfig(attachPluginConfigRequestDto);
				HttpEntity<AttachPluginWrapperDto> oidcRequest = new HttpEntity<AttachPluginWrapperDto>(
						requestWrapper, headers);
				response = restTemplate.exchange(kongUri, HttpMethod.POST, oidcRequest, String.class);
			}
			if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("jwt")) {
				MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
				requestBody.add("name", "jwt");
				requestBody.add("config.claims_to_verify", "exp");
				requestBody.add("config.key_claim_name", "kid");
				HttpEntity<MultiValueMap<String, String>> jwtRequest = new HttpEntity<MultiValueMap<String, String>>(
						requestBody, headers);
				response = restTemplate.exchange(kongUri, HttpMethod.POST, jwtRequest, String.class);
				
			}
			
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("Plugin: {} attached successfully to service: {}", attachPluginVO.getName(), serviceName);					
					message.setSuccess("Success");
					message.setErrors(errors);
					message.setWarnings(warnings);
					return message;
				}
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("OIDC plugin: {} already attached to service: {}",attachPluginVO.getName(), serviceName);
				message.setSuccess("Failure");
				messageDescription.setMessage("Plugin already attached to service");
				errors.add(messageDescription);
				message.setErrors(errors);
				return message;
			}	
			LOGGER.error("Error occured while attaching plugin: {} to service: {}",attachPluginVO.getName(), ex.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		} catch (Exception e) {
			LOGGER.error("Error while attaching plugin: {} to service: {}",attachPluginVO.getName(), e.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		return message;
	}	
	
//	@Override
//	public CreateServiceResponseVO getServiceByName(String serviceName) {
//		
//		CreateServiceResponseVO createServiceResponseVO = new CreateServiceResponseVO();
//		ServiceResponse serviceResponse = null;
//		MessageDescription messageDescription = new MessageDescription();
//		List<MessageDescription> errors = new ArrayList<>();		
//		JSONObject res = null;
//		try {
//			String kongUri = kongBaseUri + "/services/" + serviceName;
//			HttpHeaders headers = new HttpHeaders();
//			headers.set("Accept", "application/json");
//			headers.set("Content-Type", "application/x-www-form-urlencoded");
//			HttpEntity entity = new HttpEntity<>(headers);
//			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
//			if (response != null && response.hasBody()) {
//				HttpStatus statusCode = response.getStatusCode();
//				if (statusCode == HttpStatus.OK) {
//					JSONObject jsonResponse = new JSONObject(response.getBody());
//					String jsonString = response.getBody();
//					ObjectMapper mapper = new ObjectMapper();
//					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//					try {
//						serviceResponse = mapper.readValue(jsonString, ServiceResponse.class);
//					} catch (JsonMappingException e) {
//						LOGGER.error("JsonMappingException for get service {}", e.getMessage());
//						e.printStackTrace();
//					} catch (JsonProcessingException e) {
//						LOGGER.error("JsonProcessingException for get service {}", e.getMessage());
//						e.printStackTrace();
//					}
//					createServiceResponseVO.setData(serviceResponse);					
//					return createServiceResponseVO;
//				}
//			}
//		} catch (HttpClientErrorException ex) {
//			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
//			LOGGER.error("Service {} does not exist", serviceName);
//			messageDescription.setMessage("Service does not exist");
//			errors.add(messageDescription);
//			createServiceResponseVO.setErrors(errors);
//			return createServiceResponseVO;
//			}
//			LOGGER.error("Exception occured while getting service: {} details", serviceName);			
//			messageDescription.setMessage(ex.getMessage());
//			errors.add(messageDescription);
//			createServiceResponseVO.setErrors(errors);
//			return createServiceResponseVO;
//		} catch (Exception e) {
//			LOGGER.error("Error while getting service: {} details", serviceName);			
//			messageDescription.setMessage(e.getMessage());
//			errors.add(messageDescription);
//			createServiceResponseVO.setErrors(errors);
//			return createServiceResponseVO;
//		}
//		return createServiceResponseVO;
//	
//	}
//
//	@Override
//	public CreateRouteResponseVO getRouteByName(String serviceName, String routeName) {
//
//		
//		CreateRouteResponseVO createRouteResponseVO = new CreateRouteResponseVO();
//		RouteResponse routeResponse = null;
//		MessageDescription messageDescription = new MessageDescription();
//		List<MessageDescription> errors = new ArrayList<>();				
//		try {
//			String kongUri = kongBaseUri + "/services/" + serviceName + "/routes/" + routeName;
//			HttpHeaders headers = new HttpHeaders();
//			headers.set("Accept", "application/json");
//			headers.set("Content-Type", "application/x-www-form-urlencoded");
//			HttpEntity entity = new HttpEntity<>(headers);
//			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
//			if (response != null && response.hasBody()) {
//				HttpStatus statusCode = response.getStatusCode();
//				if (statusCode == HttpStatus.OK) {					
//					String jsonString = response.getBody();
//					ObjectMapper mapper = new ObjectMapper();
//					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//					try {
//						routeResponse = mapper.readValue(jsonString, RouteResponse.class);
//					} catch (JsonMappingException e) {
//						LOGGER.error("JsonMappingException for get route {}", e.getMessage());
//						e.printStackTrace();
//					} catch (JsonProcessingException e) {
//						LOGGER.error("JsonProcessingException for get route{}", e.getMessage());
//						e.printStackTrace();
//					}
//					createRouteResponseVO.setData(routeResponse);					
//					return createRouteResponseVO;
//				}
//			}
//		} catch (HttpClientErrorException ex) {
//			messageDescription.setMessage(ex.getMessage());
//			LOGGER.error("Error while getting service  {}", serviceName);
//			messageDescription.setMessage("Error while getting service details");
//			errors.add(messageDescription);
//			createRouteResponseVO.setErrors(errors);
//			return createRouteResponseVO;
//			
//		} 
//		catch (Exception e) {
//			LOGGER.error("Exception occured while getting service: {} details", serviceName);			
//			messageDescription.setMessage(e.getMessage());
//			errors.add(messageDescription);
//			createRouteResponseVO.setErrors(errors);
//			return createRouteResponseVO;
//		}
//		return createRouteResponseVO;
//	}

}
