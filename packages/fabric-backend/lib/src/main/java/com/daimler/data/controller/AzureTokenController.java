package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import com.daimler.data.dto.azure.AzureTokenRequestDto;
import com.daimler.data.dto.azure.AzureTokenResponseDto;
import com.daimler.data.dto.databricks.ClustersListResponseDto;
import com.daimler.data.dto.databricks.CreateCatalogRequestDto;
import com.daimler.data.dto.databricks.CreateCatalogResponseDto;
import com.daimler.data.dto.databricks.CreateCatalogWithAuthRequestDto;
import com.daimler.data.dto.databricks.CreateConnectionRequestDto;
import com.daimler.data.dto.databricks.CreateConnectionResponseDto;
import com.daimler.data.dto.databricks.CreateConnectionWithAuthRequestDto;
import com.daimler.data.service.azure.AzureTokenService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Azure Token APIs")
@RequestMapping("/api/azure")
@Slf4j
public class AzureTokenController {

	@Autowired
	private AzureTokenService azureTokenService;

	@Value("${databricks.host:https://adb-2518131549527911.11.azuredatabricks.net}")
	private String databricksHost;

	@PostMapping("/get-access-token")
	@ApiOperation(value = "Get Azure Access Token", 
		nickname = "getAccessToken", 
		notes = "Fetch access token from Azure using client credentials flow", 
		response = AzureTokenResponseDto.class, 
		tags = { "azure-authentication" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Access token retrieved successfully", response = AzureTokenResponseDto.class),
		@ApiResponse(code = 400, message = "Bad Request - Missing or invalid parameters"),
		@ApiResponse(code = 401, message = "Unauthorized - Invalid credentials"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<AzureTokenResponseDto> getAccessToken(
		@ApiParam(value = "Azure token request details", required = true) @Valid @RequestBody AzureTokenRequestDto tokenRequest) {

		log.info("📝 Received request to get access token for tenant: {}", tokenRequest.getTenantId());

		try {
			// Validate input
			if (tokenRequest.getTenantId() == null || tokenRequest.getTenantId().isEmpty()) {
				log.error("❌ Tenant ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientId() == null || tokenRequest.getClientId().isEmpty()) {
				log.error("❌ Client ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientSecret() == null || tokenRequest.getClientSecret().isEmpty()) {
				log.error("❌ Client Secret is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getScope() == null || tokenRequest.getScope().isEmpty()) {
				log.error("❌ Scope is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Call service to get access token
			AzureTokenResponseDto response = azureTokenService.getAccessToken(tokenRequest);

			if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getAccessToken() != null) {
				log.info("✅ Access token retrieved successfully");
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				log.error("❌ Failed to retrieve access token: {}", response != null ? response.getErrorMessage() : "Unknown error");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			log.error("❌ Exception occurred while getting access token: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/list-clusters")
	@ApiOperation(value = "List Databricks Clusters", 
		nickname = "listClusters", 
		notes = "Fetch access token from Azure and list all Databricks clusters", 
		response = ClustersListResponseDto.class, 
		tags = { "databricks-clusters" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Clusters list retrieved successfully", response = ClustersListResponseDto.class),
		@ApiResponse(code = 400, message = "Bad Request - Missing or invalid parameters"),
		@ApiResponse(code = 401, message = "Unauthorized - Invalid credentials"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<ClustersListResponseDto> listClusters(
		@ApiParam(value = "Azure token request details", required = true) @Valid @RequestBody AzureTokenRequestDto tokenRequest) {

		log.info("📝 Received request to list Databricks clusters for tenant: {}", tokenRequest.getTenantId());

		try {
			// Validate input
			if (tokenRequest.getTenantId() == null || tokenRequest.getTenantId().isEmpty()) {
				log.error("❌ Tenant ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientId() == null || tokenRequest.getClientId().isEmpty()) {
				log.error("❌ Client ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientSecret() == null || tokenRequest.getClientSecret().isEmpty()) {
				log.error("❌ Client Secret is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getScope() == null || tokenRequest.getScope().isEmpty()) {
				log.error("❌ Scope is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Call service to list clusters
			ClustersListResponseDto response = azureTokenService.listClusters(tokenRequest, databricksHost);

			if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getClusters() != null) {
				log.info("✅ Successfully fetched clusters list. Total clusters: {}", response.getClusters().size());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				log.error("❌ Failed to retrieve clusters list: {}", response != null ? response.getErrorMessage() : "Unknown error");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			log.error("❌ Exception occurred while listing clusters: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/create-connection")
	@ApiOperation(value = "Create Connection in Databricks", 
		nickname = "createConnection", 
		notes = "Fetch access token from Azure and create a Connection in Databricks Unity Catalog", 
		response = CreateConnectionResponseDto.class, 
		tags = { "databricks-connections" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Connection created successfully", response = CreateConnectionResponseDto.class),
		@ApiResponse(code = 400, message = "Bad Request - Missing or invalid parameters"),
		@ApiResponse(code = 401, message = "Unauthorized - Invalid credentials"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<CreateConnectionResponseDto> createConnection(
		@ApiParam(value = "Connection creation request with Azure authentication details", required = true) @Valid @RequestBody CreateConnectionWithAuthRequestDto request) {

		log.info("📝 Received request to create Connection for lakehouseId: {} for tenant: {}", request.getLakehouseId(), request.getAzureTokenRequest().getTenantId());

		try {
			// Validate token request input
			AzureTokenRequestDto tokenRequest = request.getAzureTokenRequest();
			if (tokenRequest.getTenantId() == null || tokenRequest.getTenantId().isEmpty()) {
				log.error("❌ Tenant ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientId() == null || tokenRequest.getClientId().isEmpty()) {
				log.error("❌ Client ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientSecret() == null || tokenRequest.getClientSecret().isEmpty()) {
				log.error("❌ Client Secret is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getScope() == null || tokenRequest.getScope().isEmpty()) {
				log.error("❌ Scope is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Validate connection request input
			if (request.getLakehouseId() == null || request.getLakehouseId().isEmpty()) {
				log.error("❌ Lakehouse ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (request.getConnectionType() == null || request.getConnectionType().isEmpty()) {
				log.error("❌ Connection type is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Create connection request DTO from the combined request
			String connectionName = "oneFabric_" + request.getLakehouseId();
			CreateConnectionRequestDto connectionRequest = new CreateConnectionRequestDto();
			connectionRequest.setName(connectionName);
			connectionRequest.setConnectionType(request.getConnectionType());
			connectionRequest.setOptions(request.getOptions());
			connectionRequest.setComment(request.getComment());

			// Call service to create connection
			CreateConnectionResponseDto response = azureTokenService.createConnection(tokenRequest, connectionRequest);

			if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getConnectionId() != null) {
				log.info("✅ Successfully created Connection. Connection ID: {}, Connection Name: {}", response.getConnectionId(), response.getName());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				log.error("❌ Failed to create Connection: {}", response != null ? response.getErrorMessage() : "Unknown error");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			log.error("❌ Exception occurred while creating connection: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/create-catalog")
	@ApiOperation(value = "Create Foreign Catalog in Databricks", 
		nickname = "createCatalog", 
		notes = "Fetch access token from Azure and create a Foreign Catalog in Databricks", 
		response = CreateCatalogResponseDto.class, 
		tags = { "databricks-catalogs" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Foreign Catalog created successfully", response = CreateCatalogResponseDto.class),
		@ApiResponse(code = 400, message = "Bad Request - Missing or invalid parameters"),
		@ApiResponse(code = 401, message = "Unauthorized - Invalid credentials"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<CreateCatalogResponseDto> createCatalog(
		@ApiParam(value = "Catalog creation request with Azure authentication details", required = true) @Valid @RequestBody CreateCatalogWithAuthRequestDto request) {

		log.info("📝 Received request to create Foreign Catalog: {} for tenant: {}", request.getName(), request.getAzureTokenRequest().getTenantId());

		try {
			// Validate token request input
			AzureTokenRequestDto tokenRequest = request.getAzureTokenRequest();
			if (tokenRequest.getTenantId() == null || tokenRequest.getTenantId().isEmpty()) {
				log.error("❌ Tenant ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientId() == null || tokenRequest.getClientId().isEmpty()) {
				log.error("❌ Client ID is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getClientSecret() == null || tokenRequest.getClientSecret().isEmpty()) {
				log.error("❌ Client Secret is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (tokenRequest.getScope() == null || tokenRequest.getScope().isEmpty()) {
				log.error("❌ Scope is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Validate catalog request input
			if (request.getName() == null || request.getName().isEmpty()) {
				log.error("❌ Catalog name is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			if (request.getConnectionName() == null || request.getConnectionName().isEmpty()) {
				log.error("❌ Connection name is missing");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Create catalog request DTO from the combined request
			CreateCatalogRequestDto catalogRequest = new CreateCatalogRequestDto();
			catalogRequest.setName(request.getName());
			catalogRequest.setConnectionName(request.getConnectionName());
			catalogRequest.setComment(request.getComment());
			catalogRequest.setOptions(request.getOptions());

			// Call service to create catalog
			CreateCatalogResponseDto response = azureTokenService.createCatalog(tokenRequest, catalogRequest);

			if (response != null && Boolean.TRUE.equals(response.getSuccess()) && response.getId() != null) {
				log.info("✅ Successfully created Foreign Catalog. Catalog ID: {}, Catalog Name: {}", response.getId(), response.getName());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				log.error("❌ Failed to create Foreign Catalog: {}", response != null ? response.getErrorMessage() : "Unknown error");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			log.error("❌ Exception occurred while creating catalog: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
