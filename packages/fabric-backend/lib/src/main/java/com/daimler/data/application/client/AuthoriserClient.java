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

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabric.EntiltlemetGroupDto;
import com.daimler.data.dto.fabric.EntitlementsDto;
import com.daimler.data.dto.fabric.FabricOAuthResponse;
import com.daimler.data.dto.fabric.GlobalRoleAssignerPrivilegesDto;
import com.daimler.data.dto.fabric.RoleApproverPrivilegesDto;
import com.daimler.data.dto.fabric.RoleIdDto;
import com.daimler.data.dto.fabric.RoleOwnerPrivilegesDto;
import com.daimler.data.dto.fabric.UserRoleRequestDto;
import com.daimler.data.dto.fabricWorkspace.AuthoriserRoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.MembersVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthoriserClient {

	@Value("${authoriser.uri}")
	private String authoriserBaseUrl;
	
	@Value("${authoriser.applicationId}")
	private String applicationId;

	@Value("${authoriser.ssoUri}")
	private String ssoUri;
	
	@Value("${authoriser.identityAuthLogin}")
	private String entitlementGroupUri;

	@Value("${authoriser.clientId}")
	private String authoriserClientID;

	@Value("${authoriser.clientSecret}")
	private String authoriserClientSecret;

	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate proxyRestTemplate;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UserStore userStore;
	
	public String getToken() {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            String basicAuthenticationHeader = Base64.getEncoder()
                    .encodeToString(new StringBuffer(authoriserClientID).append(":").append(authoriserClientSecret).toString().getBytes());
//            map.add("client_id", authoriserClientID);
//            map.add("client_secret", authoriserClientSecret);
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
    
    public EntiltlemetDetailsDto  getEntitlement(String entitlementName){
        EntiltlemetDetailsDto entiltlemetDetailsDto = new EntiltlemetDetailsDto();
        try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return entiltlemetDetailsDto;
			}
			String uri = authoriserBaseUrl+"/applications/"+applicationId+"/entitlements"+"/"+entitlementName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<EntiltlemetDetailsDto> response = proxyRestTemplate.exchange(uri, HttpMethod.GET,
					requestEntity, EntiltlemetDetailsDto.class);
			if (response!=null && response.hasBody()) {
                entiltlemetDetailsDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to fetch Entitlement with displayName {} with error {} ", entitlementName, e.getMessage());
		}
		return entiltlemetDetailsDto;
    }
    
    public GenericMessage deleteEntitlement(String entitlementId){
    	GenericMessage response = new GenericMessage();
    	List<MessageDescription> errors = new ArrayList<>();
    	List<MessageDescription> warnings = new ArrayList<>();
    	response.setSuccess("FAILED");
        try {

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				MessageDescription error = new MessageDescription("Failed to fetch token");
				response.setErrors(errors);
				response.setWarnings(warnings);
				return response;
			}

			String uri = authoriserBaseUrl+"/applications/"+applicationId+"/entitlements/"+entitlementId;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<Object> deleteEntitlementResponse = proxyRestTemplate.exchange(uri, HttpMethod.DELETE,
					requestEntity, Object.class);
			if (deleteEntitlementResponse!=null && deleteEntitlementResponse.getStatusCode().is2xxSuccessful()) {
				log.info("Entitlement with displayName {} deleted successfully", entitlementId);
				response.setSuccess("SUCCESS");
				response.setErrors(errors);
				response.setWarnings(warnings);
			}
		}catch(Exception e) {
			log.error("Failed to delete Entitlement with displayName {} with error {} ", entitlementId, e.getMessage());
		}
		return response;
    }
    
    public GenericMessage deleteRole(String roleId){
    	GenericMessage response = new GenericMessage();
    	List<MessageDescription> errors = new ArrayList<>();
    	List<MessageDescription> warnings = new ArrayList<>();
    	response.setSuccess("FAILED");
        try {

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				MessageDescription error = new MessageDescription("Failed to fetch token");
				response.setErrors(errors);
				response.setWarnings(warnings);
				return response;
			}

			String uri = authoriserBaseUrl+"/roles/"+roleId;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<Object> deleteRoleResponse = proxyRestTemplate.exchange(uri, HttpMethod.DELETE,
					requestEntity, Object.class);
			if (deleteRoleResponse!=null && deleteRoleResponse.getStatusCode().is2xxSuccessful()) {
				log.info("Role with displayName {} deleted successfully", roleId);
				response.setSuccess("SUCCESS");
				response.setErrors(errors);
				response.setWarnings(warnings);
			}
		}catch(Exception e) {
			log.error("Failed to delete Role with displayName {} with error {} ", roleId, e.getMessage());
		}
		return response;
    }

    public CreateRoleResponseDto  getRole(String roleId){
        CreateRoleResponseDto roleResponseDto = new CreateRoleResponseDto();
        try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return roleResponseDto;
			}
			String uri = authoriserBaseUrl+"/roles";
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<CreateRoleResponseDto> response = proxyRestTemplate.exchange(uri+"/"+roleId, HttpMethod.GET,
					requestEntity, CreateRoleResponseDto.class);
			if (response!=null && response.hasBody()) {
                roleResponseDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to get Role with Name {} with error {} ", roleId, e.getMessage());
		}
		return roleResponseDto;
    }
    
    public CreateRoleResponseDto  createRole(CreateRoleRequestDto createRequest){
        CreateRoleResponseDto roleResponseDto = new CreateRoleResponseDto();

        try {

			String token = getToken();
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
			return HttpStatus.CONFLICT;
		}catch(Exception e) {
			log.error("Failed to Assign Entitlement to Role with error {} ", e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
    }
	
	public HttpStatus removeEntitlementFromRole(String entitlementId, String roleId){
        
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
			ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.DELETE,
			requestEntity, String.class);
			if (response != null && response.getStatusCode() != null) {
				if(response.getStatusCode().is2xxSuccessful()){
					log.info("Entitlement :{} removed from Role {} Successfully",entitlementId,roleId);
				}
				return response.getStatusCode();
			}
		}catch(HttpClientErrorException.Conflict e) {
			log.error("Failed to remove Entitlement from Role with conflict error {} ", e.getMessage());
			return HttpStatus.CONFLICT;
		}catch(Exception e) {
			log.error("Failed to remove Entitlement from Role with error {} ", e.getMessage());
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

	public HttpStatus AssignRoleOwnerPrivilegesToCreator(String userId,String roleId){
		try {
			RoleOwnerPrivilegesDto roleOwnerPrivileges = new RoleOwnerPrivilegesDto();
			RoleIdDto roleIdDto = new RoleIdDto();
			roleIdDto.setRoleId(roleId);
			List<RoleIdDto> roleIdDtoList = new ArrayList<>();
			roleIdDtoList.add(roleIdDto);
			roleOwnerPrivileges.setRoleOwnerPrivileges(roleIdDtoList);
			
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
	public HttpStatus AssignGlobalRoleAssignerPrivilegesToCreator(String userId,String roleId){
		try {
			GlobalRoleAssignerPrivilegesDto roleAssignerPrivileges = new GlobalRoleAssignerPrivilegesDto();
			RoleIdDto roleIdDto = new RoleIdDto();
			roleIdDto.setRoleId(roleId);
			List<RoleIdDto> roleIdDtoList = new ArrayList<>();
			roleIdDtoList.add(roleIdDto);
			roleAssignerPrivileges.setGlobalRoleAssignerPrivileges(roleIdDtoList);
			
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
			HttpEntity requestEntity = new HttpEntity<>(roleAssignerPrivileges,headers);
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
	public HttpStatus AssignRoleApproverPrivilegesToCreator(String userId, String roleId){
		try {
			
			RoleApproverPrivilegesDto roleApproverPrivileges = new RoleApproverPrivilegesDto();
			RoleIdDto roleIdDto = new RoleIdDto();
			roleIdDto.setRoleId(roleId);
			List<RoleIdDto> roleIdDtoList = new ArrayList<>();
			roleIdDtoList.add(roleIdDto);
			roleApproverPrivileges.setRoleApproverPrivileges(roleIdDtoList);
			
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


	public HttpStatus RequestRoleForUser(UserRoleRequestDto requestDto,String userId, String roleId, String authToken){
		try {
			String token = "";
			if(authToken!=null && !authToken.trim().equalsIgnoreCase("")) {
				token = authToken;
			}else {
				token = getToken();
				if(!Objects.nonNull(token)) {
					log.error("Failed to fetch token to invoke fabric Apis");
					return HttpStatus.INTERNAL_SERVER_ERROR;
				}
			}
			String uri = authoriserBaseUrl+"/users/"+userId+"/roles/"+roleId;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(requestDto,headers);
			ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.POST,
			requestEntity, String.class);
			if (response != null && response.getStatusCode() != null) {
				if(response.getStatusCode().is2xxSuccessful()){
					log.info("Successfully requested role {} for user {}",roleId,userId);
				}
				return response.getStatusCode();
			}
		}catch(Exception e) {
			log.error("Failed to request role {} to user :{} with error {} ",roleId,userId ,e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public List<String> getAllUserManagableRoles(String id, String authToken){

		List<String> roles = new ArrayList<>();
		try {
			String token = "";
			if(authToken!=null && !authToken.trim().equalsIgnoreCase("")) {
				token = authToken;
			}else {
				token = getToken();
				if(!Objects.nonNull(token)) {
					log.error("Failed to fetch token to invoke fabric Apis");
					return roles;
				}
			}
            String uri = authoriserBaseUrl + "/roles";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
            if (response != null && response.getStatusCode() != null) {
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Successfully got roles for user {}", id);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.getBody());
                    JsonNode rolesNode = rootNode.path("roles");
                    if (rolesNode.isArray()) {
                        for (JsonNode roleNode : rolesNode) {
                            String roleId = roleNode.path("id").asText();
                            if (!roleId.isEmpty()) {
                                roles.add(roleId);
                            }
                        }
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            log.error("Failed to get roles for user :{} with status {}", id, e.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to get roles for user :{} with error {} ", id, e.getMessage());
        }
        return roles;
	}

	public CreatedByVO getUserDetails(String id){
		CreatedByVO userDetail = new CreatedByVO();
		try{

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return userDetail;
			}

			HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", "Bearer "+token);
				headers.setContentType(MediaType.APPLICATION_JSON);
				String uri = authoriserBaseUrl + "/users/"+ id;
				HttpEntity requestEntity = new HttpEntity<>(headers);
				ResponseEntity<String> response = proxyRestTemplate.exchange(uri, HttpMethod.GET,
						requestEntity, String.class);
				if (response != null && response.getStatusCode() != null) {
					if (response.getStatusCode().is2xxSuccessful()) {
						log.info("Successfully got user  details{}", id);
						ObjectMapper objectMapper = new ObjectMapper();
						JsonNode jsonData = objectMapper.readTree(response.getBody());
						
						userDetail.setId(jsonData.path("id").asText());
						userDetail.setFirstName(jsonData.path("givenname").asText());
						userDetail.setLastName(jsonData.path("surname").asText());
						userDetail.setDepartment(jsonData.path("departmentNumber").asText());
						userDetail.setEmail(jsonData.path("mailAddress").asText());

						return userDetail;
					}
				}
			}catch(Exception e) {
				log.error("Failed to get user detail   with error {} ", e.getMessage());
			}
		return userDetail;
	}

	public AuthoriserRoleDetailsVO getRoleDetails(String roleId){
		AuthoriserRoleDetailsVO response = new AuthoriserRoleDetailsVO();
		try{

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return response;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String uri = authoriserBaseUrl + "/roles/"+ roleId;
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<AuthoriserRoleDetailsVO> roleDetailsResponse = proxyRestTemplate.exchange(uri, HttpMethod.GET,
					requestEntity, AuthoriserRoleDetailsVO.class);
					if(roleDetailsResponse.getBody() != null){
						response = roleDetailsResponse.getBody();
					}
				
		}catch(Exception e) {
			log.error("Failed to get roles detail with error {} ", e.getMessage());
		}
		return response;
	}

	public List<MembersVO> getUsersForRole(String roleId){
		List<MembersVO> response = new ArrayList<>();
		try{

			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return response;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String uri = authoriserBaseUrl + "/roles/"+ roleId +"/users";
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<String> apiResponse = proxyRestTemplate.exchange(uri, HttpMethod.GET,
					requestEntity, String.class);
			if (apiResponse.getStatusCode().is2xxSuccessful()) {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				JsonNode jsonData = objectMapper.readTree(apiResponse.getBody());
				JsonNode usersNode = jsonData.get("users");

				if (usersNode.isArray()) {
					for (JsonNode userNode : usersNode) {
						JsonNode userObject = userNode.get("user");
						MembersVO member = objectMapper.treeToValue(userObject, MembersVO.class);
						response.add(member);
					}
				}
			}
				
		}catch(Exception e) {
			log.error("Failed to get users for role with error {} ", e.getMessage());
		}
		return response;
	}

}

