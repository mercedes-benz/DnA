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
package com.daimler.data.application.client;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

import com.daimler.data.dto.azure.CmkKeyResponseDto;
import com.daimler.data.dto.azureKeyVault.AzureUserDto;
import com.daimler.data.dto.azureKeyVault.AzureUserSearchResponseDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultAccessPolicyDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultCreateRequestDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultNameAvailabilityRequestDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultNameAvailabilityResponseDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultPermissionsDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultPropertiesDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultResponseDto;
import com.daimler.data.dto.azureKeyVault.KeyVaultSkuDto;
import com.daimler.data.dto.azureKeyVault.RoleAssignmentPropertiesDto;
import com.daimler.data.dto.azureKeyVault.RoleAssignmentRequestDto;
import com.daimler.data.dto.azureKeyVault.RoleAssignmentResponseDto;
import com.daimler.data.dto.fabric.FabricOAuthResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AzureManagementClient {

    @Value("${fabricWorkspaces.group.clientId}")
    private String groupSearchclientId;
    
    @Value("${fabricWorkspaces.group.clientSecret}")
    private String groupSearchclientSecret;
    
    @Value("${fabricWorkspaces.group.scope}")
    private String groupSearchscope;
    
    @Value("${fabricWorkspaces.group.grantType}")
    private String groupSearchgrantType;
    
    @Value("${fabricWorkspaces.group.login}")
    private String groupSearchloginUrl;
    
    @Value("${fabricWorkspaces.accessToken}")
    private String accessToken;
    
    @Value("${fabricWorkspaces.tokenTypeHint}")
    private String tokenTypeHint;
    
    @Value("${fabricWorkspaces.grantType}")
    private String grantType;
    
    @Value("${fabricWorkspaces.uri.login}")
    private String loginUrl;
    
    @Value("${fabricWorkspaces.azure.subscriptionId}")
    private String azureSubscriptionId;

    @Value("${fabricWorkspaces.azure.resourceGroup}")
    private String azureResourceGroup;

    @Value("${fabricWorkspaces.azure.tenantId}")
    private String azureTenantId;

    @Value("${fabricWorkspaces.azure.objectId}")
    private String azureObjectId;

    @Value("${fabricWorkspaces.azure.management.clientId}")
    private String azureManagementClientId;

    @Value("${fabricWorkspaces.azure.management.clientSecret}")
    private String azureManagementClientSecret;

    @Value("${fabricWorkspaces.azure.management.scope}")
    private String azureManagementScope;

    @Value("${fabricWorkspaces.azure.keyvault.location}")
    private String keyVaultLocation;

    @Value("${fabricWorkspaces.azure.keyvault.publicNetworkAccess}")
    private String keyVaultPublicNetworkAccess;

    @Value("${fabricWorkspaces.azure.keyvault.baseUrl}")
    private String azureKeyVaultBaseUrl;

    @Value("${fabricWorkspaces.azure.keyvault.createUrl}")
    private String azureKeyVaultCreateUrl;

    @Value("${fabricWorkspaces.azure.keyvault.checkNameUrl}")
    private String azureKeyVaultCheckNameUrl;

    @Value("${fabricWorkspaces.azure.keyvault.userSearchUrl}")
    private String azureUserSearchUrl;

    @Value("${fabricWorkspaces.azure.keyvault.roleAssignmentUrl}")
    private String azureRoleAssignmentUrl;

    @Value("${fabricWorkspaces.azure.keyvault.roles.cryptoOfficer}")
    private String keyVaultCryptoOfficerRole;

    @Value("${fabricWorkspaces.azure.keyvault.roles.cryptoUser}")
    private String keyVaultCryptoUserRole;

    @Value("${fabricWorkspaces.azure.keyvault.roles.keyVaultAdminRole}")
    private String keyVaultAdminRole;

    @Value("${fabricWorkspaces.azure.cmk.createUrl}")
    private String CreateCmkUrl;

    @Autowired
    private RestTemplate proxyRestTemplate;

    public String getTokenForGroupSearch() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        String basicAuthenticationHeader = Base64.getEncoder()
                .encodeToString(new StringBuffer(groupSearchclientId).append(":").append(groupSearchclientSecret).toString().getBytes());
        map.add("grant_type", groupSearchgrantType);
        map.add("scope", groupSearchscope);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + basicAuthenticationHeader);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<String> response = proxyRestTemplate.postForEntity(groupSearchloginUrl, request, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            FabricOAuthResponse introspectionResponse = objectMapper.readValue(response.getBody(),
                    FabricOAuthResponse.class);
            log.debug("Introspection Response:" + introspectionResponse);
            log.info("Successfully fetch oidc token post login for group search");
            return introspectionResponse.getAccess_token();
        } catch (Exception e) {
            log.error("Failed to fetch OIDC token for group search with error {} ",e.getMessage());
            return null;
        }
    }
    
    public String getTokenForAzureManagement() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        String basicAuthenticationHeader = Base64.getEncoder()
                .encodeToString(new StringBuffer(azureManagementClientId).append(":").append(azureManagementClientSecret).toString().getBytes());
        map.add("token", accessToken);
        map.add("token_type_hint", tokenTypeHint);
        map.add("grant_type", grantType);
        map.add("scope", azureManagementScope);
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
            log.info("Successfully fetch oidc token post login for Azure Management API");
            return introspectionResponse.getAccess_token();
        } catch (Exception e) {
            log.error("Failed to fetch OIDC token for Azure Management API with error {} ",e.getMessage());
            return null;
        }
    }
    
    public KeyVaultNameAvailabilityResponseDto checkKeyVaultNameAvailability(String keyVaultName) {
        KeyVaultNameAvailabilityResponseDto responseDto = new KeyVaultNameAvailabilityResponseDto();
        try {
            String token = getTokenForAzureManagement();
            if(!Objects.nonNull(token)) {
                log.error("Failed to fetch token to invoke Azure Management APIs");
                responseDto.setNameAvailable(false);
                responseDto.setMessage("Failed to login using service principal, please try later.");
                return responseDto;
            }
            
            KeyVaultNameAvailabilityRequestDto requestDto = new KeyVaultNameAvailabilityRequestDto(keyVaultName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<KeyVaultNameAvailabilityRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

            String url = azureKeyVaultCheckNameUrl;
            url = url.replace("{subscriptionId}", azureSubscriptionId);
            
            log.info("Checking Key Vault name availability for: {}", keyVaultName);
            ResponseEntity<KeyVaultNameAvailabilityResponseDto> response = proxyRestTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, KeyVaultNameAvailabilityResponseDto.class);
            
            responseDto = response.getBody();
            log.info("Name availability check for {} result: {}", keyVaultName, responseDto.getNameAvailable());
            return responseDto;
        } catch (HttpClientErrorException e) {
            log.error("Azure API error checking Key Vault name availability: {}", e.getMessage());
            responseDto.setNameAvailable(false);
            responseDto.setReason("ClientError");
            responseDto.setMessage("Failed to check name availability: " + e.getMessage());
            return responseDto;
        } catch (Exception e) {
            log.error("Error checking Key Vault name availability: {}", e.getMessage());
            responseDto.setNameAvailable(false);
            responseDto.setMessage("Failed to check name availability: " + e.getMessage());
            return responseDto;
        }
    }
    
    public String getUserPrincipalId(String userEmail) {
        try {
            String token = getTokenForGroupSearch();
            if(!Objects.nonNull(token)) {
                log.error("Failed to fetch token to invoke Microsoft Graph API");
                return null;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = azureUserSearchUrl + "?$filter=mail eq '" + userEmail + "'";
            log.info("User search URL: {}", url);
            
            log.info("Searching for user with email: {}", userEmail);
            ResponseEntity<AzureUserSearchResponseDto> response = proxyRestTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, AzureUserSearchResponseDto.class);
            log.info("res body: {}", response.getBody());
            AzureUserSearchResponseDto searchResponse = response.getBody();
            if (searchResponse != null && searchResponse.getValue() != null && !searchResponse.getValue().isEmpty()) {
                AzureUserDto user = searchResponse.getValue().get(0);
                log.info("Found user {} with principal ID: {}", userEmail, user.getId());
                return user.getId();
            }
            
            log.warn("No user found with email: {}", userEmail);
            return null;
        } catch (Exception e) {
            log.error("Error searching for user {}: {}", userEmail, e.getMessage());
            return null;
        }
    }
    
	public KeyVaultResponseDto createOrUpdateKeyVault(String keyVaultName) {
		KeyVaultResponseDto responseDto = new KeyVaultResponseDto();
		try {
			String token = getTokenForAzureManagement();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke Azure Management APIs");
				responseDto.setErrorCode("AUTH_ERROR");
				responseDto.setMessage("Failed to login using service principal, please try later.");
				return responseDto;
			}

			KeyVaultCreateRequestDto createRequest = new KeyVaultCreateRequestDto();
			createRequest.setLocation(keyVaultLocation);
			
			KeyVaultPropertiesDto properties = new KeyVaultPropertiesDto();
			properties.setTenantId(azureTenantId);
			
			KeyVaultSkuDto sku = new KeyVaultSkuDto();
			sku.setFamily("A");
			sku.setName("standard");
			properties.setSku(sku);

            //Not added access policies as RBAC is enabled, so permissions will be managed via role assignments
			// KeyVaultAccessPolicyDto accessPolicy = new KeyVaultAccessPolicyDto();
			// accessPolicy.setTenantId(azureTenantId);
			// accessPolicy.setObjectId(azureObjectId);
			
			// KeyVaultPermissionsDto permissions = new KeyVaultPermissionsDto();
			// permissions.setKeys(Arrays.asList(
			// 	"encrypt", "decrypt", "wrapKey", "unwrapKey", "sign", "verify",
			// 	"get", "list", "create", "update", "import", "delete",
			// 	"backup", "restore", "recover", "purge"
			// ));
			// permissions.setSecrets(Arrays.asList(
			// 	"get", "list", "set", "delete", "backup", "restore", "recover", "purge"
			// ));
			// permissions.setCertificates(Arrays.asList(
			// 	"get", "list", "delete", "create", "import", "update",
			// 	"managecontacts", "getissuers", "listissuers", "setissuers",
			// 	"deleteissuers", "manageissuers", "recover", "purge"
			// ));
			// accessPolicy.setPermissions(permissions);
			
            properties.setEnableRbacAuthorization(true);
			// properties.setAccessPolicies(Arrays.asList(accessPolicy));
			properties.setEnabledForDeployment(true);
			properties.setEnabledForDiskEncryption(true);
			properties.setEnabledForTemplateDeployment(true);
			properties.setEnablePurgeProtection(true);
			properties.setPublicNetworkAccess(keyVaultPublicNetworkAccess);
			
			createRequest.setProperties(properties);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer " + token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			HttpEntity<KeyVaultCreateRequestDto> requestEntity = new HttpEntity<>(createRequest, headers);

			String url = azureKeyVaultCreateUrl;
			url = url.replace("{subscriptionId}", azureSubscriptionId)
					.replace("{resourceGroupName}", azureResourceGroup)
					.replace("{keyVaultName}", keyVaultName);
			
			log.info("Creating Key Vault: {}", keyVaultName);
			ResponseEntity<KeyVaultResponseDto> response = proxyRestTemplate.exchange(
					url, HttpMethod.PUT, requestEntity, KeyVaultResponseDto.class);
			
			if (response != null && response.hasBody()) {
				responseDto = response.getBody();
				log.info("Successfully created Key Vault: {}", keyVaultName);
			}
			return responseDto;
		} catch (HttpClientErrorException.Conflict e) {
			log.error("Failed to create Key Vault with name {} with conflict error {} ", keyVaultName, e.getMessage());
			responseDto.setErrorCode("409");
			responseDto.setMessage("Key Vault already exists");
			return responseDto;
		} catch (HttpClientErrorException e) {
			log.error("Azure API error creating Key Vault {}: {}", keyVaultName, e.getMessage());
			responseDto.setErrorCode(String.valueOf(e.getStatusCode().value()));
			responseDto.setMessage("Failed to create Key Vault: " + e.getMessage());
			return responseDto;
		} catch (Exception e) {
			log.error("Error creating Key Vault {}: {}", keyVaultName, e.getMessage());
			responseDto.setErrorCode("500");
			responseDto.setMessage("Failed to create Key Vault: " + e.getMessage());
			return responseDto;
		}
	}
    
    public RoleAssignmentResponseDto assignRoleToUser(String keyVaultName, String userPrincipalId, String roleType) {
        RoleAssignmentResponseDto responseDto = new RoleAssignmentResponseDto();
        try {
            String token = getTokenForAzureManagement();
            if(!Objects.nonNull(token)) {
                log.error("Failed to fetch token to invoke Azure Management APIs");
                responseDto.setErrorCode("AUTH_ERROR");
                responseDto.setMessage("Failed to login using service principal, please try later.");
                return responseDto;
            }

            String roleDefinitionId = keyVaultAdminRole;
            if ("user".equalsIgnoreCase(roleType)) {
                roleDefinitionId = keyVaultCryptoUserRole;
            }
            
            String fullRoleDefinitionId = "/subscriptions/" + azureSubscriptionId + roleDefinitionId;

            log.info("Role assignment details - Type: {}, Definition ID: {}, Full Role Definition: {}", 
                roleType, roleDefinitionId, fullRoleDefinitionId);
            
            RoleAssignmentPropertiesDto properties = new RoleAssignmentPropertiesDto();
            properties.setRoleDefinitionId(fullRoleDefinitionId);
            properties.setPrincipalId(userPrincipalId);
            properties.setPrincipalType("User");
            
            RoleAssignmentRequestDto requestDto = new RoleAssignmentRequestDto();
            requestDto.setProperties(properties);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<RoleAssignmentRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
            
            String roleAssignmentId = java.util.UUID.randomUUID().toString();

            String url = azureRoleAssignmentUrl;
            url = url.replace("{subscriptionId}", azureSubscriptionId)
                    .replace("{resourceGroupName}", azureResourceGroup)
                    .replace("{keyVaultName}", keyVaultName)
                    .replace("{roleAssignmentId}", roleAssignmentId);
            
            // log.info("Assigning role {} to user {} for Key Vault {}", roleType, userPrincipalId, keyVaultName);
            log.info("Assigning role '{}' to user {} for Key Vault '{}'. Role Assignment ID: {}", 
                roleType, userPrincipalId, keyVaultName, roleAssignmentId);
            log.info("Role assignment URL: {}", url);

            ResponseEntity<RoleAssignmentResponseDto> response = proxyRestTemplate.exchange(
                    url, HttpMethod.PUT, requestEntity, RoleAssignmentResponseDto.class);
            
            responseDto = response.getBody();
            // log.info("Successfully assigned role to user for Key Vault: {}", keyVaultName);
            log.info("Successfully assigned role '{}' to user {} for Key Vault: {}", roleType, userPrincipalId, keyVaultName);
            return responseDto;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                log.info("Role assignment already exists for user {} on Key Vault {}", userPrincipalId, keyVaultName);
                responseDto.setErrorCode("409");
                responseDto.setMessage("Role assignment already exists");
                return responseDto;
            }
            log.error("Azure API error assigning role to user: {}", e.getMessage());
            responseDto.setErrorCode(String.valueOf(e.getStatusCode().value()));
            responseDto.setMessage("Failed to assign role: " + e.getMessage());
            return responseDto;
        } catch (Exception e) {
            log.error("Error assigning role to user: {}", e.getMessage());
            responseDto.setErrorCode("INTERNAL_ERROR");
            responseDto.setMessage("Failed to assign role: " + e.getMessage());
            return responseDto;
        }
    }

    public Map<String, Object> createWorkSpaceCmkKey(String workspaceId){
    
        Map<String, Object> responseMap = new LinkedHashMap<>();

        boolean cmkFlag = false;
        try {
            String token = getTokenForAzureManagement();
            if(!Objects.nonNull(token)) {
                log.error("Failed to fetch token to invoke Azure Management APIs");
                responseMap.put("cmkFlag", cmkFlag);
                responseMap.put("message", "Failed to obtain token using service principal, please try later.");
                return responseMap;
            }

            // create a body to send
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("kty", "RSA");
            requestBody.put("key_size", 3072);
            requestBody.put("key_ops", Arrays.asList(
                "encrypt",
                "decrypt",
                "sign",
                "verify",
                "wrapKey",
                "unwrapKey"
            ));
            
            HttpHeaders headers = new HttpHeaders();
            // headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // String url = azureKeyVaultCheckNameUrl;
            String url = "https://KV-ADA-CMKFabric.vault.azure.net/keys/cmk-ada-{workspaceId}/create?api-version=7.4";
            url = url.replace("{workspaceId}", workspaceId);

            // log.info("Checking Key Vault name availability for: {}", keyVaultName);
            ResponseEntity<CmkKeyResponseDto> response = proxyRestTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, CmkKeyResponseDto.class);
            
            CmkKeyResponseDto responseDto = response.getBody();
            cmkFlag = true;
            log.info("Successfully created CMK key for workspace : {}", workspaceId);
            responseMap.put("cmkFlag", cmkFlag);
            responseMap.put("message", "Successfully created CMK key for workspace.");
            responseMap.put("keyId", responseDto.getKey().getKid());
            return responseMap;
        } catch (HttpClientErrorException e) {
            log.error("CMK key creation failed for workspace {}: {}", workspaceId, e.getMessage());
            responseMap.put("cmkFlag", cmkFlag);
            responseMap.put("message", e.getMessage());
            return responseMap;
        } catch (Exception e) {
            log.error("CMK key creation failed for workspace {}: {}", workspaceId, e.getMessage());
            responseMap.put("cmkFlag", cmkFlag);
            responseMap.put("message", e.getMessage());
            return responseMap;
        }
    }

    public boolean assignCmkKeyToWorkspace(String workspaceId,String kid){

        Map<String, Object> responseMap = new LinkedHashMap<>();

        boolean cmkAssignFlag = false;
        try {
            String token = getTokenForAzureManagement();
            if(!Objects.nonNull(token)) {
                log.error("Failed to fetch token to invoke Azure Management APIs");
                responseMap.put("cmkAssignFlag", cmkAssignFlag);
                responseMap.put("message", "Failed to obtain token using service principal, please try later.");
                return cmkAssignFlag;
            }

            // create a body to send
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("keyIdentifier", kid);
            
            HttpHeaders headers = new HttpHeaders();
            // headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // String url = azureKeyVaultCheckNameUrl;
            String url = "https://api.fabric.microsoft.com/v1/workspaces/{workspaceId}/encryption/assign";
            url = url.replace("{workspaceId}", workspaceId);

            // log.info("Checking Key Vault name availability for: {}", keyVaultName);
            ResponseEntity<JsonNode> response = proxyRestTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, JsonNode.class);
            
            
            if(response.getStatusCode().is2xxSuccessful()) {
                cmkAssignFlag = true;
                log.info("Successfully assigned CMK key to workspace : {}", workspaceId);
                responseMap.put("cmkAssignFlag", cmkAssignFlag);
                responseMap.put("message", "Successfully assigned CMK key to workspace.");
            } else {
                log.error("Failed to assign CMK key to workspace {}: HTTP status {}", workspaceId, response.getStatusCode());
                responseMap.put("cmkAssignFlag", cmkAssignFlag);
                responseMap.put("message", "Failed to assign CMK key to workspace: HTTP status " + response.getStatusCode());
            }
            return cmkAssignFlag;
        } catch (HttpClientErrorException e) {
            log.error("CMK key assignment failed for workspace {}: {}", workspaceId, e.getMessage());
            responseMap.put("cmkAssignFlag", cmkAssignFlag);
            responseMap.put("message", e.getMessage());
            return cmkAssignFlag;
        } catch (Exception e) {
            log.error("CMK key assignment failed for workspace {}: {}", workspaceId, e.getMessage());
            responseMap.put("cmkAssignFlag", cmkAssignFlag);
            responseMap.put("message", e.getMessage());
            return cmkAssignFlag;
        }
    }
}