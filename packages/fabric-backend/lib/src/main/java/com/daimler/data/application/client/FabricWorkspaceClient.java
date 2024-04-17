package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.fabric.AddUserDto;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.FabricOAuthResponse;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspaceUpdateDto;
import com.daimler.data.dto.fabric.WorkspacesCollectionDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FabricWorkspaceClient {

	@Value("${fabricWorkspaces.clientId}")
	private String clientId;
	
	@Value("${fabricWorkspaces.clientSecret}")
	private String clientSecret;
	
	@Value("${fabricWorkspaces.scope}")
	private String scope;
	
	@Value("${fabricWorkspaces.grantType}")
	private String grantType;
	
	@Value("${fabricWorkspaces.accessToken}")
	private String accessToken;
	
	@Value("${fabricWorkspaces.tokenTypeHint}")
	private String tokenTypeHint;
	
	@Value("${fabricWorkspaces.capacityId}")
	private String capacityId;
	
	@Value("${fabricWorkspaces.uri.login}")
	private String loginUrl;
	
	@Value("${fabricWorkspaces.uri.workspacesBase}")
	private String workspacesBaseUrl;
	
	@Value("${fabricWorkspaces.uri.capacitiesBase}")
	private String capacitiesBaseUrl;
	
	@Value("${fabricWorkspaces.uri.addUserUrl}")
	private String addUserUrl;
	
	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate proxyRestTemplate;
	
	@Autowired
	private RestTemplate restTemplate;

	public String getToken() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		String basicAuthenticationHeader = Base64.getEncoder()
				.encodeToString(new StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes());
		map.add("token", accessToken);
		map.add("token_type_hint", tokenTypeHint);
		//map.add("client_id", clientId);
		//map.add("client_secret", "clientSecret");
		map.add("grant_type", grantType);
		map.add("scope", scope);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic " + basicAuthenticationHeader);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<String> response = proxyRestTemplate.postForEntity(loginUrl, request, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			FabricOAuthResponse introspectionResponse = objectMapper.readValue(response.getBody(),
					FabricOAuthResponse.class);
			log.debug("Introspection Response:" + introspectionResponse);
			return introspectionResponse.getAccess_token();
		} catch (Exception e) {
			log.error("Failed to fetch OIDC token with error {} ",e.getMessage());
			return null;
		}
	}
	
	public WorkspaceDetailDto createWorkspace(CreateWorkspaceDto createRequest) {
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return workspaceDetailDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CreateWorkspaceDto> requestEntity = new HttpEntity<>(createRequest,headers);
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspacesBaseUrl, HttpMethod.POST,
					requestEntity, WorkspaceDetailDto.class);
			if (response!=null && response.hasBody()) {
				workspaceDetailDto = response.getBody();
			}
		}catch(HttpClientErrorException.Conflict e) {
			workspaceDetailDto.setErrorCode("409");
			workspaceDetailDto.setMessage("Workspace name already exists");
			log.error("Failed to create workspace with displayName {} with conflict error {} ", createRequest.getDisplayName(), e.getMessage());
		}catch(Exception e) {
			workspaceDetailDto.setErrorCode("500");
			workspaceDetailDto.setMessage(e.getMessage());
			log.error("Failed to create workspace with displayName {} with error {} ", createRequest.getDisplayName(), e.getMessage());
		}
		return workspaceDetailDto;
	}
	
	public WorkspaceDetailDto getWorkspaceDetails(String workspaceId) {
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return workspaceDetailDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String workspaceUrl = workspacesBaseUrl + "/" + workspaceId;
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspaceUrl , HttpMethod.GET,
					requestEntity, WorkspaceDetailDto.class);
			if (response !=null && response.hasBody()) {
				workspaceDetailDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to get workspace details for id {} with {} exception ", workspaceId, e.getMessage());
		}
		return workspaceDetailDto;
	}
	
	public WorkspacesCollectionDto listWorkspaces() {
		WorkspacesCollectionDto collection = new WorkspacesCollectionDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return collection;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<WorkspacesCollectionDto> response = proxyRestTemplate.exchange(workspacesBaseUrl , HttpMethod.GET,
					requestEntity, WorkspacesCollectionDto.class);
			if (response !=null && response.hasBody()) {
				collection = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to get workspaces with {} exception ", e.getMessage());
		}
		return collection;
	}
	
	public GenericMessage addUser(String groupId, String emailAddress) {
		GenericMessage response = new GenericMessage();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errorMessage = new MessageDescription("Failed to login using service principal, please try later.");
				errors.add(errorMessage);
				response.setErrors(errors);
				response.setWarnings(new ArrayList<>());
				log.error("Failed to fetch token to invoke fabric Apis");
				return response;
			}
			AddUserDto addUserDto = new AddUserDto();
			addUserDto.setEmailAddress(emailAddress);
			addUserDto.setGroupUserAccessRight("Admin");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<AddUserDto> requestEntity = new HttpEntity<>(addUserDto,headers);
			String addUserToGroupUrl = addUserUrl + "/" + groupId + "/users";
			ResponseEntity<String> addUserResponse = proxyRestTemplate.exchange(addUserToGroupUrl, HttpMethod.POST,
					requestEntity, String.class);
			if (addUserResponse!=null && addUserResponse.getStatusCode().is2xxSuccessful()) {
				response.setSuccess("SUCCESS");
				response.setErrors(new ArrayList<>());
				response.setWarnings(new ArrayList<>());
				return response;
			}
		}catch(Exception e) {
			response.setSuccess("FAILED");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errorMessage = new MessageDescription("Failed to add user to workspace with exception, please try again or contact Admin.");
			errors.add(errorMessage);
			response.setErrors(errors);
			response.setWarnings(new ArrayList<>());
			log.error("Failed to add user {} to workspace {} with exception ", emailAddress, groupId, e.getMessage());
			return response;
		}
		return null;
	}
	
	public WorkspaceDetailDto updateWorkspace(String workspaceId, WorkspaceUpdateDto updateRequest) {
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return workspaceDetailDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<WorkspaceUpdateDto> requestEntity = new HttpEntity<>(updateRequest,headers);
			String workspaceUrl = workspacesBaseUrl + "/" + workspaceId;
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspaceUrl, HttpMethod.POST,
					requestEntity, WorkspaceDetailDto.class);
			if (response!=null && response.hasBody()) {
				workspaceDetailDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to update workspace {} with displayName {} with error {} ", workspaceId, updateRequest.getDisplayName(), e.getMessage());
		}
		return workspaceDetailDto;
	}
	
	public ErrorResponseDto assignCapacity(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to fetch token while assigning capacity to workspace. Please reassign or update workspace.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String request = "{\"capacityId\": \"" + capacityId +"\"}";
			HttpEntity<String> requestEntity = new HttpEntity<>(request,headers);
			String assignCapacityUrl = workspacesBaseUrl + "/" + workspaceId + "/assignToCapacity";
			ResponseEntity<ErrorResponseDto> response = proxyRestTemplate.exchange(assignCapacityUrl, HttpMethod.POST,
					requestEntity, ErrorResponseDto.class);
			if (response!=null && response.hasBody()) {
				errorResponse = response.getBody();
			}
		}catch(Exception e) {
			errorResponse.setErrorCode("Failed");
			errorResponse.setMessage("Failed to assign capacity to workspace, with error " + e.getMessage());
			log.error("Failed to assign capacity {} to workspace with displayName {} with error {} ", capacityId, workspaceId , e.getMessage());
		}
		return errorResponse;
	}
	
	public ErrorResponseDto unassignCapacity(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to login using service principal, please try later.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String request = "{\"capacityId\": \"" + capacityId +"\"}";
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String assignCapacityUrl = workspacesBaseUrl + "/" + workspaceId + "/unassignFromCapacity";
			ResponseEntity<ErrorResponseDto> response = proxyRestTemplate.exchange(assignCapacityUrl, HttpMethod.POST,
					requestEntity, ErrorResponseDto.class);
			if (response!=null && response.hasBody()) {
				errorResponse = response.getBody();
			}
		}catch(Exception e) {
			errorResponse.setErrorCode("Failed to unassign capacity, with error " + e.getMessage());
			log.error("Failed to unassign capacity {} of workspace with displayName {} with error {} ", capacityId, workspaceId , e.getMessage());
		}
		return errorResponse;
	}
	
	public ErrorResponseDto deleteWorkspace(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to login using service principal, please try later.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String workspaceUrl = workspacesBaseUrl + "/" + workspaceId;
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspaceUrl , HttpMethod.DELETE,
					requestEntity, WorkspaceDetailDto.class);
			if (response !=null && response.getStatusCode().is2xxSuccessful()) {
				errorResponse = null;
			}
		}catch(Exception e) {
			errorResponse.setMessage("Failed to delete workspace with error : " + e.getMessage());
			log.error("Failed to delete workspace details for id {} with {} exception ", workspaceId, e.getMessage());
		}
		return errorResponse;
	}
	
	
}
