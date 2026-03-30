package com.daimler.data.service.azure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.daimler.data.dto.azure.AzureTokenRequestDto;
import com.daimler.data.dto.azure.AzureTokenResponseDto;
import com.daimler.data.dto.databricks.ClustersListResponseDto;
import com.daimler.data.dto.databricks.CreateCatalogRequestDto;
import com.daimler.data.dto.databricks.CreateCatalogResponseDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseAzureTokenService implements AzureTokenService {

	@Autowired(required = false)
	private RestTemplate proxyRestTemplate;

	@Value("${fabricWorkspaces.uri.login}")
	private String tokenEndpoint;

	@Value("${databricksIntegration.url.base}")
	private String databricksBaseUrl;

	@Override
	public AzureTokenResponseDto getAccessToken(AzureTokenRequestDto tokenRequest) {

		AzureTokenResponseDto responseDto = new AzureTokenResponseDto();

		try {
			// Build the token endpoint URL
			

			log.info("Fetching Azure access token from endpoint: {}", tokenEndpoint);

			// Create request body as form data (x-www-form-urlencoded)
			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("grant_type", tokenRequest.getGrantType());
			body.add("client_id", tokenRequest.getClientId());
			body.add("client_secret", tokenRequest.getClientSecret());
			body.add("scope", tokenRequest.getScope());

			// Set headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			// Create HTTP entity
			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

			// Make the API call using proxyRestTemplate
			ResponseEntity<AzureTokenResponseDto> response = proxyRestTemplate.postForEntity(
					tokenEndpoint,
					requestEntity,
					AzureTokenResponseDto.class);

			if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
				responseDto = response.getBody();
				responseDto.setSuccess(true);
				responseDto.setErrorMessage(null);
				log.info("✅ Successfully fetched Azure access token. Token Type: {}, Expires In: {}",
						responseDto.getTokenType(), responseDto.getExpiresIn());
				// log.info("Access Token: {}", responseDto.getAccessToken());
			} else {
				String errorMsg = "❌ Failed to fetch access token. Status Code: " + response.getStatusCode();
				log.error(errorMsg);
				responseDto.setSuccess(false);
				responseDto.setErrorMessage("Failed to fetch access token. HTTP Status: " + response.getStatusCode());
				responseDto.setAccessToken(null);
			}

		} catch (Exception e) {
			String errorMsg = "❌ Exception occurred while fetching Azure access token: " + e.getMessage();
			log.error(errorMsg, e);
			responseDto.setSuccess(false);
			responseDto.setErrorMessage(e.getMessage());
			responseDto.setAccessToken(null);
		}

		return responseDto;
	}

	@Override
	public ClustersListResponseDto listClusters(AzureTokenRequestDto tokenRequest, String databricksHost) {

		ClustersListResponseDto responseDto = new ClustersListResponseDto();

		try {
			log.info("📝 Fetching access token for cluster listing...");

			// Step 1: Get access token
			AzureTokenResponseDto tokenResponse = getAccessToken(tokenRequest);

			if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
				String errorMsg = "❌ Failed to retrieve access token";
				log.error(errorMsg);
				responseDto.setSuccess(false);
				responseDto.setErrorMessage(tokenResponse != null && tokenResponse.getErrorMessage() != null ? tokenResponse.getErrorMessage() : "Failed to retrieve access token from Azure");
				return responseDto;
			}

			log.info("✅ Access token obtained successfully. Token Type: {}", tokenResponse.getTokenType());

			// Step 2: Call Databricks clusters API
			String clustersEndpoint = databricksBaseUrl + "/api/2.0/clusters/list";

			log.info("📝 Calling Databricks Clusters API: {}", clustersEndpoint);

			// Set headers with Bearer token
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + tokenResponse.getAccessToken());

			// Create HTTP entity with no body for GET request
			HttpEntity<String> apiRequest = new HttpEntity<>(headers);

			// Make the API call using proxyRestTemplate
			ResponseEntity<ClustersListResponseDto> response = proxyRestTemplate.exchange(
					clustersEndpoint,
					HttpMethod.GET,
					apiRequest,
					ClustersListResponseDto.class);


			if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
				responseDto = response.getBody();
				responseDto.setSuccess(true);
				responseDto.setErrorMessage(null);
				log.info("✅ Successfully fetched clusters list. Total clusters: {}",
						responseDto.getClusters() != null ? responseDto.getClusters().size() : 0);
			} else {
				String errorMsg = "❌ Failed to list clusters. Status Code: " + response.getStatusCode();
				log.error(errorMsg);
				responseDto.setSuccess(false);
				responseDto.setErrorMessage("Failed to list clusters from Databricks. HTTP Status: " + response.getStatusCode());
			}

		} catch (Exception e) {
			String errorMsg = "❌ Exception occurred while listing clusters: " + e.getMessage();
			log.error(errorMsg, e);
			responseDto.setSuccess(false);
			responseDto.setErrorMessage(e.getMessage());
		}

		return responseDto;
	}

	@Override
	public CreateCatalogResponseDto createCatalog(AzureTokenRequestDto tokenRequest, CreateCatalogRequestDto catalogRequest) {

		CreateCatalogResponseDto responseDto = new CreateCatalogResponseDto();

		try {
			log.info("📝 Creating Foreign CatalogRequest: {} ", catalogRequest.toString());

			// Step 1: Get access token
			AzureTokenResponseDto tokenResponse = getAccessToken(tokenRequest);

			if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
				String errorMsg = "❌ Failed to retrieve access token for catalog creation";
				log.error(errorMsg);
				responseDto.setSuccess(false);
				responseDto.setErrorMessage(tokenResponse != null && tokenResponse.getErrorMessage() != null ? tokenResponse.getErrorMessage() : "Failed to retrieve access token from Azure");
				return responseDto;
			}

			log.info("✅ Access token obtained successfully for catalog creation");

			// Step 2: Call Databricks create catalog API
			String catalogsEndpoint = databricksBaseUrl + "/api/2.1/unity-catalog/catalogs";

			log.info("📝 Calling Databricks Create Catalog API: {}", catalogsEndpoint);

			// Set headers with Bearer token
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer " + tokenResponse.getAccessToken());

			// Create HTTP entity with catalog request body
			HttpEntity<CreateCatalogRequestDto> apiRequest = new HttpEntity<>(catalogRequest, headers);

			// Make the API call using proxyRestTemplate
			ResponseEntity<CreateCatalogResponseDto> response = proxyRestTemplate.exchange(
					catalogsEndpoint,
					HttpMethod.POST,
					apiRequest,
					CreateCatalogResponseDto.class);

			if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
				responseDto = response.getBody();
				responseDto.setSuccess(true);
				responseDto.setErrorMessage(null);
				log.info("✅ Successfully created Foreign Catalog. Catalog Name: {}, Catalog ID: {}, Catalog Type: {}",
						responseDto.getName(), responseDto.getId(), responseDto.getCatalogType());
			} else {
				String errorMsg = "❌ Failed to create catalog. Status Code: " + response.getStatusCode();
				log.error(errorMsg);
				responseDto.setSuccess(false);
				responseDto.setErrorMessage("Failed to create catalog in Databricks. HTTP Status: " + response.getStatusCode());
			}

		} catch (Exception e) {
			String errorMsg = "❌ Exception occurred while creating catalog: " + e.getMessage();
			log.error(errorMsg, e);
			responseDto.setSuccess(false);
			responseDto.setErrorMessage(e.getMessage());
		}

		return responseDto;
	}

}
