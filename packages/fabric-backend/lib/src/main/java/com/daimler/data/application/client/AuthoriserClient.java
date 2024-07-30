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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.EntiltlemetGroupDto;
import com.daimler.data.dto.fabric.EntitlementsDto;
import com.daimler.data.dto.fabric.FabricOAuthResponse;
import com.daimler.data.dto.fabric.GlobalRoleAssignerPrivilegesDto;
import com.daimler.data.dto.fabric.RoleApproverPrivilegesDto;
import com.daimler.data.dto.fabric.RoleOwnerPrivilegesDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Value("${authoriser.ssoUri}")
	private String ssoUri;
	
	@Value("${authoriser.identityRoleUrl}")
	private String entitlementGroupUri;

	@Value("${authoriser.clientId}")
	private String authoriserClientID;

	@Value("${authoriser.clientSecret}")
	private String authoriserClientSecret;

	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate proxyRestTemplate;
	
	public String getToken() {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            String basicAuthenticationHeader = Base64.getEncoder()
                    .encodeToString(new StringBuffer(authoriserClientID).append(":").append(authoriserClientSecret).toString().getBytes());

            map.add("grant_type", "client_credentials");
            map.add("scope", "openid authorization_group entitlement_group scoped_entitlement email profile");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Basic " + basicAuthenticationHeader);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            try {
                ResponseEntity<String> response = proxyRestTemplate.postForEntity(ssoUri, request, String.class);
                ObjectMapper objectMapper = new ObjectMapper();
                FabricOAuthResponse introspectionResponse = objectMapper.readValue(response.getBody(),
				FabricOAuthResponse.class);
                log.info("Introspection Response:" + introspectionResponse);
                return introspectionResponse.getAccess_token();
            } catch (Exception e) {
                log.error("Failed to fetch OIDC token with error {} ",e.getMessage());
                return null;
            }
        }

    public EntitlementsDto getAllEntitlements(){
        EntitlementsDto entitlementsDto = new EntitlementsDto();

        try {
			
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return entitlementsDto;
			}

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

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return entiltlemetDetailsDto;
			}

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

			String token = getToken();
			System.out.println(token);
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return roleResponseDto;
			}
			
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

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return HttpStatus.INTERNAL_SERVER_ERROR;
			}

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

	public EntiltlemetGroupDto getEntitlementGroup(){
		EntiltlemetGroupDto entitlementGroup = new EntiltlemetGroupDto();
		try{

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return entitlementGroup;
			}

			HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", "Bearer "+token);
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity requestEntity = new HttpEntity<>(headers);
				ResponseEntity<EntiltlemetGroupDto> response = proxyRestTemplate.exchange(entitlementGroupUri, HttpMethod.POST,
						requestEntity, EntiltlemetGroupDto.class);
				if (response!=null && response.hasBody()) {
					entitlementGroup = response.getBody();
				}
			}catch(Exception e) {
				log.error("Failed to get Entitlement Group  with error {} ", e.getMessage());
			}
		return entitlementGroup;
	}

	public HttpStatus AssignRoleOwnerPrivilegesToCreator(String userId, RoleOwnerPrivilegesDto roleOwnerPrivileges ){
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return HttpStatus.INTERNAL_SERVER_ERROR;
			}

			String uri = authoriserBaseUrl+"/users/"+userId+"/privileges/roleowner";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(roleOwnerPrivileges,headers);
			ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.PUT,
			requestEntity, String.class);
			if (response != null && response.getStatusCode() != null) {
				if(response.getStatusCode().is2xxSuccessful()){
					log.info("Role Owner Privilege Assigned to User {} Successfully",userId);
				}
				return response.getStatusCode();
			}
		}catch(Exception e) {
			log.error("Failed to Assign Role Owner Privilege to user :{} with error {} ",userId ,e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	public HttpStatus AssignGlobalRoleAssignerPrivilegesToCreator(String userId,GlobalRoleAssignerPrivilegesDto globalRoleAssignerPrivileges ){
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return HttpStatus.INTERNAL_SERVER_ERROR;
			}

			String uri = authoriserBaseUrl+"/users/"+userId+"/privileges/globalroleassigner";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(globalRoleAssignerPrivileges,headers);
			ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.PUT,
			requestEntity, String.class);
			if (response != null && response.getStatusCode() != null) {
				if(response.getStatusCode().is2xxSuccessful()){
					log.info("Global Role Assigner Privilege Assigned to User {} Successfully",userId);
				}
				return response.getStatusCode();
			}
		}catch(Exception e) {
			log.error("Failed to Assign Global Role Assigner Privilege to user :{} with error {} ",userId ,e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	public HttpStatus AssignRoleApproverPrivilegesToCreator(String userId, RoleApproverPrivilegesDto roleApproverPrivileges){
		try {

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return HttpStatus.INTERNAL_SERVER_ERROR;
			}

			String uri = authoriserBaseUrl+"/users/"+userId+"/privileges/roleapprover";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(roleApproverPrivileges,headers);
			ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.PUT,
			requestEntity, String.class);
			if (response != null && response.getStatusCode() != null) {
				if(response.getStatusCode().is2xxSuccessful()){
					log.info("Role Approver Privilage Assigned to User {} Successfully",userId);
				}
				return response.getStatusCode();
			}
		}catch(Exception e) {
			log.error("Failed to Assign Role Approver Privileges to user :{} with error {} ",userId ,e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}


}

