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

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.daimler.data.dto.kongGateway.AttachAppAuthoriserPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachAppAuthoriserPluginVO;
import com.daimler.data.dto.kongGateway.AttachApiAuthoriserPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachApiAuthoriserPluginVO;
import com.daimler.data.dto.kongGateway.AttachJwtPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachJwtPluginVO;
import com.daimler.data.dto.kongGateway.AttachPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachPluginVO;
import com.daimler.data.dto.kongGateway.CreateRouteResponseVO;
import com.daimler.data.dto.kongGateway.CreateRouteVO;
import com.daimler.data.dto.kongGateway.RouteResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class KongClientImpl implements KongClient {

	private Logger LOGGER = LoggerFactory.getLogger(KongClientImpl.class);

	@Value("${kong.uri}")
	private String kongBaseUri;

	@Value("${corsPlugin.allowedUrls}")
	private String allowedUrls;

	@Value("${corsPlugin.maxAge}")
	private long maxAge;

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
		String patchKongUri = kongBaseUri + "/services/" + serviceName + "/routes/" + createRouteVO.getName();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		try {
//			// get available routes
//			HttpEntity entity = new HttpEntity<>(headers);
//			ResponseEntity<String> routes = restTemplate.exchange(getKongUri, HttpMethod.GET, entity, String.class);
//			if (routes != null && routes.hasBody() && routes.getStatusCode() == HttpStatus.OK) {
//				JSONArray array = (JSONArray) new JSONObject(routes.getBody()).getJSONArray("data");
//				if (array != null && !array.isEmpty()) {
//					for(int i=0; i<array.length(); i++) {
//						JSONObject jsonObject = (JSONObject) array.get(i);
//						if(Objects.nonNull(jsonObject.get("name"))) {
//							existingRouteName = (String) jsonObject.get("name");
//						}
//						routeNames.add(existingRouteName);
//					}														
//				}
//			}
//			if(routeNames.contains(routeName)) {
//				LOGGER.info("Route: {} already exist for service:{}", createRouteVO.getName(), serviceName);
//				message.setSuccess("Failure");
//				messageDescription.setMessage("Route already exist");
//				errors.add(messageDescription);
//				message.setErrors(errors);
//				return message;
//			}
//			else {
				// create new route
				JSONObject requestBody = new JSONObject(); 
				requestBody.put("paths", new JSONArray(createRouteVO.getPaths()));
				requestBody.put("protocols", new JSONArray(createRouteVO.getProtocols())); 
				requestBody.put("hosts", new JSONArray(createRouteVO.getHosts())); 
				requestBody.put("name", routeName);   
				requestBody.put("strip_path", createRouteVO.isStripPath()); // Added strip_path 
				requestBody.put("preserve_host", true);
				HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
				ResponseEntity<String> response = restTemplate.exchange(patchKongUri, HttpMethod.PUT, request, String.class);
				if (response != null && response.hasBody()) {
					HttpStatus statusCode = response.getStatusCode();
					LOGGER.info("statuscode is:{}",statusCode);
					if (statusCode == HttpStatus.CREATED || statusCode == HttpStatus.OK) {
						LOGGER.info("Route: {} created successfully for service:{}", createRouteVO.getName(), serviceName);
						message.setSuccess("Success");		
						message.setErrors(errors);
						message.setWarnings(warnings);
						return message;
					}
				}
			//}
			
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
				attachPluginConfigRequestDto.setRedirect_uri(attachPluginConfigVO.getRedirectUri());
				attachPluginConfigRequestDto.setRevoke_tokens_on_logout(attachPluginConfigVO.getRevokeTokensOnLogout());
				attachPluginConfigRequestDto.setRecovery_page_path(attachPluginConfigVO.getRecoveryPagePath());
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
			if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("cors")){

				AttachCorsPluginWrapperDto corsRequestWrapper = new AttachCorsPluginWrapperDto();			
				AttachCorsPluginConfigRequestDto attachCorsPluginConfigRequestDto = new AttachCorsPluginConfigRequestDto();

				corsRequestWrapper.setName(attachPluginVO.getName().name().toLowerCase());
				attachCorsPluginConfigRequestDto.setMax_age(maxAge);
				String [] urls = allowedUrls.split(",");
				List<String> origins = Arrays.asList(urls);
				attachCorsPluginConfigRequestDto.setOrigins(origins);
				attachCorsPluginConfigRequestDto.setCredentials(true);
				corsRequestWrapper.setConfig(attachCorsPluginConfigRequestDto);
					HttpEntity<AttachCorsPluginWrapperDto> corsRequest = new HttpEntity<AttachCorsPluginWrapperDto>(
						corsRequestWrapper, headers);
					response = restTemplate.exchange(kongUri, HttpMethod.POST, corsRequest, String.class);
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

	@Override
	public List<String> getAllServices() {
		
		GetAllServicesDto allServicesCollection = new GetAllServicesDto();
		List<String> serviceNames = new ArrayList<>();
		List<ServiceDto> kongServices = new ArrayList<>();
		String serviceName =  "";
		try {
			String kongUri = kongBaseUri + "/services/";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				LOGGER.info("completed fetching all services from kong ");
				ObjectMapper mapper = new ObjectMapper();
				allServicesCollection = mapper.readValue(response.getBody(), GetAllServicesDto.class);
				//return response.getBody();
				kongServices = allServicesCollection.getData();
				JSONArray array = (JSONArray) new JSONObject(response.getBody()).getJSONArray("data");
				if (array != null && !array.isEmpty()) {
					for(int i=0; i<array.length(); i++) {
						JSONObject jsonObject = (JSONObject) array.get(i);
						String host = jsonObject.getString("host");
						String name = jsonObject.getString("name");
						if(Objects.nonNull(jsonObject.get("name")) && Objects.nonNull(jsonObject.get("host"))) {
							if(host.contains("code-server") && name.startsWith("ws")) {
								serviceName = (String) jsonObject.get("name");
								serviceNames.add(serviceName);
							}							
						}
					}														
				}				
			}
			return serviceNames;
		}
		catch(Exception e) {
			LOGGER.error("Error occured while fetching services from kong with exception {}", e.getMessage());
		}
		return serviceNames;
	}

	@Override
	public GenericMessage attachJwtPluginToService(AttachJwtPluginVO attachJwtPluginVO, String serviceName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			AttachJwtPluginConfigVO attachJwtPluginConfigVO = attachJwtPluginVO.getConfig();
			String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			AttachJwtPluginWrapperDto requestWrapper = new AttachJwtPluginWrapperDto();			
			AttachJwtPluginConfigRequestDto attachJwtPluginConfigRequestDto = new AttachJwtPluginConfigRequestDto();	
			ResponseEntity<String> response = null;
			requestWrapper.setName(attachJwtPluginVO.getName());
			attachJwtPluginConfigRequestDto.setAlgorithm(attachJwtPluginConfigVO.getAlgorithm());
			attachJwtPluginConfigRequestDto.setAuthurl(attachJwtPluginConfigVO.getAuthurl());
			attachJwtPluginConfigRequestDto.setClientHomeUrl(attachJwtPluginConfigVO.getClientHomeUrl());
			attachJwtPluginConfigRequestDto.setClient_id(attachJwtPluginConfigVO.getClientId());
			attachJwtPluginConfigRequestDto.setClient_secret(attachJwtPluginConfigVO.getClientSecret());
			attachJwtPluginConfigRequestDto.setExpiresIn(attachJwtPluginConfigVO.getExpiresIn());
			attachJwtPluginConfigRequestDto.setIntrospection_uri(attachJwtPluginConfigVO.getIntrospectionUri());
			attachJwtPluginConfigRequestDto.setEnableAuthTokenIntrospection(attachJwtPluginConfigVO.isEnableAuthTokenIntrospection());
			attachJwtPluginConfigRequestDto.setPrivateKeyFilePath(attachJwtPluginConfigVO.getPrivateKeyFilePath());
			attachJwtPluginConfigRequestDto.setSecret(attachJwtPluginConfigVO.getSecret());
			requestWrapper.setConfig(attachJwtPluginConfigRequestDto);
			HttpEntity<AttachJwtPluginWrapperDto> jwtIssuerRequest = new HttpEntity<AttachJwtPluginWrapperDto>(requestWrapper, headers);
			response = restTemplate.exchange(kongUri, HttpMethod.POST, jwtIssuerRequest, String.class);
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("Plugin: {} attached successfully to API: {}", attachJwtPluginVO.getName(), serviceName);					
					message.setSuccess("Success");
					message.setErrors(errors);
					message.setWarnings(warnings);
					return message;
				}
			}
			
		}catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info(" plugin: {} already attached to service: {}",attachJwtPluginVO.getName(), serviceName);
				message.setSuccess("Failure");
				messageDescription.setMessage("Plugin already attached to service");
				errors.add(messageDescription);
				message.setErrors(errors);
				return message;
			}	
			LOGGER.error("Error occured while attaching plugin: {} to service: {}",attachJwtPluginVO.getName(), ex.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		} catch (Exception e) {
			LOGGER.error("Error while attaching plugin: {} to service: {}",attachJwtPluginVO.getName(), e.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		return message;
	}

	@Override
	public GenericMessage attachAppAuthoriserPluginToService(AttachAppAuthoriserPluginVO attachAppAuthoriserPluginVO, String serviceName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {			
			String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			AttachAppAuthoriserPluginWrapperDto requestWrapper = new AttachAppAuthoriserPluginWrapperDto();
			AttachAppAuthoriserPluginConfigVO appAuthoriserPluginConfigVO = attachAppAuthoriserPluginVO.getConfig();
			AttachAppAuthoriserPluginConfigRequestDto appAuthoriserPluginConfigRequestDto = new AttachAppAuthoriserPluginConfigRequestDto();
			appAuthoriserPluginConfigRequestDto.setCsvalidateurl(appAuthoriserPluginConfigVO.getCsvalidateurl());
			requestWrapper.setName(attachAppAuthoriserPluginVO.getName());
			requestWrapper.setConfig(appAuthoriserPluginConfigRequestDto);
						
			HttpEntity<AttachAppAuthoriserPluginWrapperDto> appAuthoriserRequest = new HttpEntity<AttachAppAuthoriserPluginWrapperDto>(requestWrapper, headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.POST, appAuthoriserRequest, String.class);
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("App Authoriser plugin attached successfully to service: {}", serviceName);					
					message.setSuccess("Success");
					message.setErrors(errors);
					message.setWarnings(warnings);
					return message;
				}
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("App Authoriser plugin already attached to service: {}", serviceName);
				message.setSuccess("Failure");
				messageDescription.setMessage("App Authoriser Plugin already attached to service");
				errors.add(messageDescription);
				message.setErrors(errors);
				return message;
			}	
			LOGGER.error("Error occured while attaching App Authoriser plugin to service: {}", ex.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		} catch (Exception e) {
			LOGGER.error("Error while attaching App Authoriser plugin to service: {}", e.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		return message;
	}

	@Override
	public GenericMessage deleteRoute(String serviceName, String routeName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			String kongUri = kongBaseUri + "/services/" + serviceName + "/routes/" + routeName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.DELETE, entity, String.class);
			if (response != null) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					message.setSuccess("Success");		
					message.setErrors(errors);
					message.setWarnings(warnings);
					LOGGER.info("Kong route:{} for the service {} deleted successfully", routeName, serviceName);
					return message;
				}
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
			LOGGER.error("Route {} does not exist", routeName);
			messageDescription.setMessage("Route does not exist");
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
			}
			LOGGER.error("Exception: {} occured while deleting route: {} details", ex.getMessage(), routeName);			
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		catch(Exception e) {
			LOGGER.error("Error: {} while deleting route: {} details", e.getMessage(), routeName);			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			errors.add(messageDescription);
			message.setErrors(errors);
		}
		return message;
	}

	@Override
	public GenericMessage deleteService(String serviceName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			String kongUri = kongBaseUri + "/services/" + serviceName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.DELETE, entity, String.class);
			if (response != null) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					message.setSuccess("Success");		
					message.setErrors(errors);
					message.setWarnings(warnings);
					LOGGER.info("Kong service:{} deleted successfully", serviceName);
					return message;
				}
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
			LOGGER.error("Service {} does not exist", serviceName);
			messageDescription.setMessage("Service does not exist");
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
			}
			LOGGER.error("Exception: {} occured while deleting service: {} details", ex.getMessage(), serviceName);			
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		catch(Exception e) {
			LOGGER.error("Error: {} while deleting service: {} details", e.getMessage(), serviceName);			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			errors.add(messageDescription);
			message.setErrors(errors);
		}
		return message;
	}	

	@Override
	public GenericMessage attachApiAuthoriserPluginToService(AttachApiAuthoriserPluginVO attachApiAuthoriserPluginVO, String serviceName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {			
			String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			AttachApiAuthoriserPluginWrapperDto requestWrapper = new AttachApiAuthoriserPluginWrapperDto();
			AttachApiAuthoriserPluginConfigVO apiAuthoriserPluginConfigVO = attachApiAuthoriserPluginVO.getConfig();
			AttachApiAuthoriserPluginConfigRequestDto apiAuthoriserPluginConfigRequestDto = new AttachApiAuthoriserPluginConfigRequestDto();

			apiAuthoriserPluginConfigRequestDto.setApplicationName(apiAuthoriserPluginConfigVO.getApplicationName());
			apiAuthoriserPluginConfigRequestDto.setEnableUserinfoIntrospection(apiAuthoriserPluginConfigVO.isEnableUserinfoIntrospection());
			apiAuthoriserPluginConfigRequestDto.setWsconfigurl(apiAuthoriserPluginConfigVO.getWsconfigurl());
			apiAuthoriserPluginConfigRequestDto.setPoolID(apiAuthoriserPluginConfigVO.getPoolID());
			apiAuthoriserPluginConfigRequestDto.setUserinfoIntrospectionUri(apiAuthoriserPluginConfigVO.getUserinfoIntrospectionUri());
			apiAuthoriserPluginConfigRequestDto.setLogType(apiAuthoriserPluginConfigVO.getLogType());
			apiAuthoriserPluginConfigRequestDto.setEnv(apiAuthoriserPluginConfigVO.getEnv());
			apiAuthoriserPluginConfigRequestDto.setProjectName(apiAuthoriserPluginConfigVO.getProjectName());
			requestWrapper.setName(attachApiAuthoriserPluginVO.getName());
			requestWrapper.setConfig(apiAuthoriserPluginConfigRequestDto);
						
			HttpEntity<AttachApiAuthoriserPluginWrapperDto> apiAuthoriserRequest = new HttpEntity<AttachApiAuthoriserPluginWrapperDto>(requestWrapper, headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.POST, apiAuthoriserRequest, String.class);
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.CREATED) {
					LOGGER.info("Api Authoriser plugin attached successfully to service: {}", serviceName);					
					message.setSuccess("Success");
					message.setErrors(errors);
					message.setWarnings(warnings);
					return message;
				}
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("Api Authoriser plugin already attached to service: {}", serviceName);
				message.setSuccess("Failure");
				messageDescription.setMessage("Api Authoriser Plugin already attached to service");
				errors.add(messageDescription);
				message.setErrors(errors);
				return message;
			}	
			LOGGER.error("Error occured while attaching Api Authoriser plugin to service: {}", ex.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		} catch (Exception e) {
			LOGGER.error("Error while attaching Api Authoriser plugin to service: {}", e.getMessage());
			message.setSuccess("Failure");
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		return message;
	}
	
	@Override
	public GenericMessage deletePlugin(String serviceName, String pluginName) {
		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		
		try {
			Map<String,String>pluginIdMap = getPluginIds(serviceName,pluginName);
			if(!pluginIdMap.isEmpty()){
				String PluginIdToDelete = pluginIdMap.get(pluginName);
				if(PluginIdToDelete!=null){
					String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins/" + PluginIdToDelete;
					HttpHeaders headers = new HttpHeaders();
					headers.set("Accept", "application/json");
					headers.set("Content-Type", "application/x-www-form-urlencoded");
					HttpEntity entity = new HttpEntity<>(headers);
					ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.DELETE, entity, String.class);
					if (response != null) {
						HttpStatus statusCode = response.getStatusCode();
						if (statusCode.is2xxSuccessful()) {
							message.setSuccess("Success");		
							message.setErrors(errors);
							message.setWarnings(warnings);
							LOGGER.info("Kong plugin:{} for the service {} deleted successfully", pluginName, serviceName);
							return message;
						}
						
					}
				}
				LOGGER.error("plugin {} does not exist", pluginName);
					messageDescription.setMessage("plugin does not exist");
					errors.add(messageDescription);
					message.setErrors(errors);
					return message;
			}
			else{
				LOGGER.error("plugin {} does not exist", pluginName);
				messageDescription.setMessage("plugin does not exist");
				errors.add(messageDescription);
				message.setErrors(errors);
				return message;
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
			LOGGER.error("plugin {} does not exist", pluginName);
			messageDescription.setMessage("plugin does not exist");
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
			}
			LOGGER.error("Exception: {} occured while deleting plugin: {} details", ex.getMessage(), pluginName);			
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		catch(Exception e) {
			LOGGER.error("Error: {} while deleting plugin: {} details", e.getMessage(), pluginName);			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			errors.add(messageDescription);
			message.setErrors(errors);
		}
	
		return message;
	}

	public Map<String,String> getPluginIds(String serviceName, String pluginName){
		Map<String,String>pluginIdMap = new HashMap<>();
		try {
			String kongUri = kongBaseUri + "/services/" + serviceName + "/plugins";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
			response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
			if (response != null) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					ObjectMapper objectMapper = new ObjectMapper();
					 objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					try{
						JsonNode rootNode = objectMapper.readTree(response.getBody());
						JsonNode data = rootNode.get("data");
						if (data != null && data.isArray()) {
							for (JsonNode plugin : (ArrayNode) data) 
							{
								pluginIdMap.put(plugin.get("name").asText(),plugin.get("id").asText());
							}
						}
					}catch(Exception e){
						LOGGER.debug("Exception occured during fetching plugin list");
					}
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Error: {} while fetching plugin: {} details", e.getMessage(), pluginName);			
		}
		return pluginIdMap;
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
	@Override
	public CreateRouteResponseVO getRouteByName(String serviceName, String routeName) {

		
		CreateRouteResponseVO createRouteResponseVO = new CreateRouteResponseVO();
		RouteResponse routeResponse = null;
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();				
		try {
			String kongUri = kongBaseUri + "/services/" + serviceName + "/routes/" + routeName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/x-www-form-urlencoded");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.OK) {					
					String jsonString = response.getBody();
					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					try {
						routeResponse = mapper.readValue(jsonString, RouteResponse.class);
					} catch (JsonMappingException e) {
						LOGGER.error("JsonMappingException for get route {}", e.getMessage());
					} catch (JsonProcessingException e) {
						LOGGER.error("JsonProcessingException for get route{}", e.getMessage());
					}
					createRouteResponseVO.setData(routeResponse);					
					return createRouteResponseVO;
				}
			}
		} catch (HttpClientErrorException ex) {
			messageDescription.setMessage(ex.getMessage());
			LOGGER.error("Error while getting service  {}", serviceName);
			messageDescription.setMessage("Error while getting service details");
			errors.add(messageDescription);
			createRouteResponseVO.setErrors(errors);
			return createRouteResponseVO;
			
		} 
		catch (Exception e) {
			LOGGER.error("Exception occured while getting route: {} details {}.", serviceName, e.getMessage());			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			createRouteResponseVO.setErrors(errors);
			return createRouteResponseVO;
		}
		return createRouteResponseVO;
	}


}
