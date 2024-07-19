package com.daimler.data.application.client;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.EntitlementsDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthoriserClient {
    
    @Value("${fabricWorkspaces.clientId}")
	private String clientId;
	
	@Value("${fabricWorkspaces.clientSecret}")
	private String clientSecret;
	
	@Value("${fabricWorkspaces.scope}")
	private String scope;
	
	@Value("${fabricWorkspaces.grantType}")
	private String grantType;
	
	@Value("${fabricWorkspaces.uri.login}")
	private String loginUrl;

	@Value("${authoriser.uri}")
	private String authoriserBaseUrl;
	
	@Value("${authoriser.applicationId}")
	private String applicationId;

	@Value("${authoriser.token}")
	private String token;
	

	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate proxyRestTemplate;
	

    public EntitlementsDto getAllEntitlements(){
        EntitlementsDto entitlementsDto = new EntitlementsDto();

        try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String uri = authoriserBaseUrl+"/applications/"+applicationId+"/entitlements";
			ResponseEntity<EntitlementsDto> response = proxyRestTemplate.exchange(uri , HttpMethod.GET,
					requestEntity, EntitlementsDto.class);
			if (response !=null && response.hasBody()) {
				entitlementsDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to get Entilements details with {} exception ", e.getMessage());
		}
		return entitlementsDto;

    }

    public EntiltlemetDetailsDto  createEntitlement(CreateEntitlementRequestDto createRequest){
        EntiltlemetDetailsDto entiltlemetDetailsDto = new EntiltlemetDetailsDto();

        try {
			String uri = authoriserBaseUrl+"/applications/"+applicationId+"/entitlements";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CreateEntitlementRequestDto> requestEntity = new HttpEntity<>(createRequest,headers);
			ResponseEntity<EntiltlemetDetailsDto> response = proxyRestTemplate.exchange(uri, HttpMethod.POST,
					requestEntity, EntiltlemetDetailsDto.class);
			if (response!=null && response.hasBody()) {
                entiltlemetDetailsDto = response.getBody();
			}
		}catch(HttpClientErrorException.Conflict e) {
			log.error("Failed to create Entitlement with displayName {} with conflict error {} ", createRequest.getDisplayName(), e.getMessage());
		}catch(Exception e) {
			log.error("Failed to create Entitlement with displayName {} with error {} ", createRequest.getDisplayName(), e.getMessage());
		}
		return entiltlemetDetailsDto;
    }

    public CreateRoleResponseDto  createRole(CreateRoleRequestDto createRequest){
        CreateRoleResponseDto roleResponseDto = new CreateRoleResponseDto();

        try {
			String uri = authoriserBaseUrl+"/roles";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CreateRoleRequestDto> requestEntity = new HttpEntity<>(createRequest,headers);
			ResponseEntity<CreateRoleResponseDto> response = proxyRestTemplate.exchange(uri, HttpMethod.POST,
					requestEntity, CreateRoleResponseDto.class);
			if (response!=null && response.hasBody()) {
                roleResponseDto = response.getBody();
			}
		}catch(HttpClientErrorException.Conflict e) {
			log.error("Failed to create Role with Name {} with conflict error {} ", createRequest.getName(), e.getMessage());
		}catch(Exception e) {
			log.error("Failed to create Role with Name {} with error {} ", createRequest.getName(), e.getMessage());
		}
		return roleResponseDto;
    }

	public HttpStatus  AssignEntitlementToRole(String entitlementId, String roleId){
        
        try {
			String uri = authoriserBaseUrl+"/roles/"+roleId+"/applications/"+applicationId+"/entitlements/"+entitlementId;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.POST,
			requestEntity, String.class);
			if (response != null && response.getStatusCode() != null) {
				if(response.getStatusCode().is2xxSuccessful()){
					log.info("Entitlement :{} Assigned to Role {} Successfully",entitlementId,roleId);
				}
				return response.getStatusCode();
			}
		}catch(HttpClientErrorException.Conflict e) {
			log.error("Failed to Assign Entitlement to Role with conflict error {} ", e.getMessage());
		}catch(Exception e) {
			log.error("Failed to Assign Entitlement to Role with error {} ", e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
    }
	

}

