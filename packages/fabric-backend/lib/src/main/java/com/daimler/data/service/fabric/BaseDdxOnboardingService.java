package com.daimler.data.service.fabric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.databricks.sdk.core.DatabricksConfig;
// import com.databricks.sdk.core.http.CommonsHttpClient;
import com.databricks.sdk.service.catalog.CreateCatalog;
import com.databricks.sdk.service.sql.ExecuteStatementRequest;
import com.databricks.sdk.service.sql.StatementResponse;
import com.databricks.sdk.WorkspaceClient;
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.fabric.FabricSqlEndpointResponseDto;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.DdxPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricWorkspace.DdxUnityDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.util.ProxyConfig;
import com.daimler.data.util.CommonsHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpHost;
import com.daimler.data.dto.fabric.DdxOnboardingRequestDto;
import com.daimler.data.dto.fabric.DdxResponseDto;
import com.daimler.data.service.azure.AzureTokenService;
import com.daimler.data.dto.azure.AzureTokenRequestDto;
import com.daimler.data.dto.azure.AzureTokenResponseDto;
import com.daimler.data.dto.databricks.CreateCatalogRequestDto;
import com.daimler.data.dto.databricks.CreateCatalogResponseDto;
import com.daimler.data.dto.fabric.DataProductConnectionStringDto;

// import com.databricks.sdk.core.http.ProxyConfig;
// import com.databricks.sdk.core.http.impl.CommonsHttpClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseDdxOnboardingService implements DdxOnboardingService {

    @Autowired
    private FabricWorkspaceClient fabricWorkspaceClient;

    @Autowired
    private AzureTokenService azureTokenService;

    @Autowired
    private FabricWorkspaceService fabricWorkspaceService;

    @Autowired
    private FabricWorkspaceRepository jpaRepo;

    @Autowired
    private FabricWorkspaceAssembler assembler;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private String proxyPort;

    @Value("${ddxIntegration.client.host}")
    private String databricksHost;

    @Value("${ddxIntegration.client.id}")
    private String databricksSpClientId;

    @Value("${ddxIntegration.client.secret}")
    private String databricksSpClientSecret;

    @Value("${ddxIntegration.client.tenantId}")
    private String databricksSpTenantId;

    @Value("${ddxIntegration.client.scope}")
    private String databricksSpScope;

    @Override
    public GenericMessage onboardToDdx(DdxOnboardingRequestDto publishDdxRequest, String workspaceId, String workspaceName, String lakehouseId, String userId, CreatedByVO createdBy) {

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        MessageDescription message = new MessageDescription();

        try {
            // --- Input Validation ---
            if (publishDdxRequest == null) {
                throw new IllegalArgumentException("DdxOnboardingRequestDto cannot be null");
            }
            if (workspaceId == null || workspaceId.trim().isEmpty()) {
                throw new IllegalArgumentException("workspaceId cannot be null or empty");
            }
            if (lakehouseId == null || lakehouseId.trim().isEmpty()) {
                throw new IllegalArgumentException("lakehouseId cannot be null or empty");
            }
            if (createdBy == null) {
                throw new IllegalArgumentException("createdBy cannot be null");
            }

            // --- Fabric Lakehouse & Connection Details ---
            String connectionName = "oneFabric_" + lakehouseId;
            String catalogName = "westeurope_" + lakehouseId;

            // 1. Fetch SQL Endpoint Details
            log.info("Fetching SQL endpoint details for workspace: {} and lakehouse: {}", workspaceId, lakehouseId);
            FabricSqlEndpointResponseDto sqlEndpoint;
            try {
                sqlEndpoint = fabricWorkspaceClient.getSqlEndpoint(workspaceId, lakehouseId);
            } catch (Exception e) {
                log.error("Failed to fetch SQL endpoint for workspace: {} and lakehouse: {}", workspaceId, lakehouseId, e);
                throw new RuntimeException("Failed to retrieve SQL endpoint details: " + e.getMessage(), e);
            }

            if (sqlEndpoint == null) {
                throw new RuntimeException("SQL endpoint response is null for workspace: " + workspaceId + " and lakehouse: " + lakehouseId);
            }

            // 2. Extract and Validate SQL Endpoint Properties
            String fabricSqlEndpoint;
            String fabricDatabaseName;
            try {
                if (sqlEndpoint.getProperties() == null || sqlEndpoint.getProperties().getSqlEndpointProperties() == null) {
                    throw new RuntimeException("SQL endpoint properties are missing or null");
                }
                fabricSqlEndpoint = sqlEndpoint.getProperties().getSqlEndpointProperties().getConnectionString();
                fabricDatabaseName = sqlEndpoint.getDisplayName();

                if (fabricSqlEndpoint == null || fabricSqlEndpoint.trim().isEmpty()) {
                    throw new RuntimeException("Connection string from SQL endpoint is null or empty");
                }
                if (fabricDatabaseName == null || fabricDatabaseName.trim().isEmpty()) {
                    throw new RuntimeException("Database name from SQL endpoint is null or empty");
                }

                log.info("SQL endpoint details retrieved - Database: {}", fabricDatabaseName);
            } catch (RuntimeException e) {
                log.error("Failed to extract SQL endpoint properties for lakehouse: {}", lakehouseId, e);
                throw e;
            }

            // 3. Prepare and Validate Azure Token Request
            log.info("🔐 Requesting Azure access token for Databricks authentication");
            if (databricksSpTenantId == null || databricksSpClientId == null || databricksSpClientSecret == null) {
                throw new RuntimeException("Databricks service principal configuration is incomplete");
            }

            AzureTokenRequestDto tokenRequest = new AzureTokenRequestDto();
            tokenRequest.setTenantId(databricksSpTenantId);
            tokenRequest.setClientId(databricksSpClientId);
            tokenRequest.setClientSecret(databricksSpClientSecret);
            tokenRequest.setScope(databricksSpScope);
            tokenRequest.setGrantType("client_credentials");

            // 4. Create Catalog Request
            CreateCatalogRequestDto createCatalogRequest = new CreateCatalogRequestDto();
            createCatalogRequest.setName(catalogName);
            createCatalogRequest.setConnectionName(connectionName);
            createCatalogRequest.setComment("Catalog for fabric lakehouse: " + lakehouseId + " in workspace: " + workspaceId);
            createCatalogRequest.setOptions(new HashMap<String, String>(){{
                put("database", "DnA_dataiku");
            }});

            // 5. Create Catalog via Azure Token Service
            log.info("Creating Databricks catalog: {} with connection: {}", catalogName, connectionName);
            CreateCatalogResponseDto catalogResponse;
            try {
                catalogResponse = azureTokenService.createCatalog(tokenRequest, createCatalogRequest);
            } catch (Exception e) {
                log.error("Failed to create catalog: {} for workspace: {}", catalogName, workspaceId, e);
                throw new RuntimeException("Databricks catalog creation failed: " + e.getMessage(), e);
            }

            if (catalogResponse == null) {
                throw new RuntimeException("Catalog creation response is null");
            }

            log.info("✅ Catalog creation response: {}", catalogResponse);
            log.info("🎉 --- Databricks Fabric Setup Completed Successfully ---");

            // 6. Prepare DDX Onboarding Request
            log.info("Preparing DDX onboarding request with {} data product connections", publishDdxRequest.getDataProductConnections().size());
            try {
                if (publishDdxRequest.getDataProductConnections() == null || publishDdxRequest.getDataProductConnections().isEmpty()) {
                    throw new RuntimeException("No data product connections found in the request");
                }

                publishDdxRequest.getDataProductConnections().forEach(connection -> {
                    connection.setCloudRegion("westeurope");
                    connection.setFormatType("SQLSERVER_FORMAT");
                    connection.setCloudProvider("Azure");
                    connection.getStoringCountries().add("Germany");
                    
                    DataProductConnectionStringDto connectionString = connection.getDataProductConnectionString();
                    if (connectionString == null) {
                        throw new RuntimeException("DataProductConnectionString is null for a connection");
                    }
                    
                    connectionString.setCatalogName(catalogName);
                    connectionString.setSchemaName("dbo");
                    connectionString.setFullSchema(true);
                });
                log.info("Prepared DDX onboarding request: {}", publishDdxRequest.getDataProductName());
            } catch (RuntimeException e) {
                log.error("Failed to prepare DDX onboarding request: {}", e.getMessage());
                throw e;
            }

            // 7. Onboard to DDX
            log.info("Onboarding product: {} to DDX for workspace: {}", publishDdxRequest.getDataProductName(), workspaceId);
            DdxResponseDto onboardingResponse;
            try {
                onboardingResponse = fabricWorkspaceClient.ddxProductOnboarding(publishDdxRequest);
            } catch (Exception e) {
                log.error("Failed to call DDX product onboarding API for product: {}", publishDdxRequest.getDataProductName(), e);
                throw new RuntimeException("DDX onboarding API call failed: " + e.getMessage(), e);
            }

            if (onboardingResponse == null) {
                throw new RuntimeException("DDX onboarding response is null");
            }

            // 8. Validate DDX Onboarding Response
            if (onboardingResponse.getStatusCode() != 201) {
                String errorMsg = onboardingResponse.getMessage() != null 
                    ? onboardingResponse.getMessage() 
                    : "Unknown error from DDX service";
                log.warn("DDX onboarding failed with status code: {} and message: {}", onboardingResponse.getStatusCode(), errorMsg);
                message.setMessage("Failed to onboard to DDX with status code: " + onboardingResponse.getStatusCode() + ", error: " + errorMsg);
                errors.add(message);
                responseMessage.setErrors(errors);
                responseMessage.setSuccess("FAILED");
                return responseMessage;
            }

            // 9. Update DDX Lakehouse Details
            log.info("Updating DDX lakehouse details for workspace: {} and lakehouse: {}", workspaceId, lakehouseId);
            try {
                updateDdxLakeHouseDetails(workspaceId, lakehouseId, fabricDatabaseName, catalogName, onboardingResponse, createdBy);
            } catch (Exception e) {
                log.error("Failed to update DDX lakehouse details for workspace: {} and lakehouse: {}", workspaceId, lakehouseId, e);
                // Log the error but continue - the onboarding was successful
                message.setMessage("Product onboarded to DDX successfully, but failed to update lakehouse details: " + e.getMessage());
                errors.add(message);
                responseMessage.setErrors(errors);
                responseMessage.setSuccess("PARTIAL_SUCCESS");
                return responseMessage;
            }

            // 10. Success Response
            message.setMessage("Product onboarded to DDX successfully for product: " + publishDdxRequest.getDataProductName());
            responseMessage.setSuccess("SUCCESS");
            log.info("✅ Successfully onboarded product: {} to DDX for workspace: {}", publishDdxRequest.getDataProductName(), workspaceId);

        } catch (IllegalArgumentException e) {
            message.setMessage("Invalid input parameter: " + e.getMessage());
            errors.add(message);
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("FAILED");
            log.error("Invalid input provided for onboarding to DDX - workspace: {}, lakehouse: {}, userId: {}", workspaceId, lakehouseId, userId, e);
            return responseMessage;
        } catch (RuntimeException e) {
            message.setMessage("Failed to onboard product to DDX: " + e.getMessage());
            errors.add(message);
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("FAILED");
            log.error("Runtime exception occurred while onboarding to DDX for userId: {}, workspace: {}, lakehouse: {}", userId, workspaceId, lakehouseId, e);
            return responseMessage;
        } catch (Exception e) {
            message.setMessage("Unexpected error occurred during DDX onboarding: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            errors.add(message);
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("FAILED");
            log.error("Unexpected exception occurred for userId: {} during onboarding to DDX for workspace: {} and lakehouse: {}", userId, workspaceId, lakehouseId, e);
            return responseMessage;
        }

        return responseMessage;
    }

    private void updateDdxLakeHouseDetails(String workspaceId, String lakehouseId, String lakehouseName, String catalogName, DdxResponseDto onboardingResponse, CreatedByVO createdBy) {
        try {
            FabricWorkspaceVO workspace = fabricWorkspaceService.getById(workspaceId);
            DdxPublishedLakeHouseDetailsVO details = Optional.ofNullable(workspace.getDdxPublishedLakeHouseDetails())
                .orElse(new DdxPublishedLakeHouseDetailsVO());

            details.setIsLakeHousesPublishedToDdx(true);

            List<String> publishedNames = Optional.ofNullable(details.getPublishedLakeHouseNames())
                .orElse(new ArrayList<>());
            if (!publishedNames.contains(lakehouseName)) {
                publishedNames.add(lakehouseName);
            }
            details.setPublishedLakeHouseNames(publishedNames);

            details.setProductName(onboardingResponse.getDataProductName());
            details.setProductId(String.valueOf(onboardingResponse.getDataProductId()));
            details.setCreatedBy(createdBy);
            Date now = new Date();
            if (details.getCreatedOn() == null) {
                details.setCreatedOn(now);
            }
            details.setModifiedOn(now);

            DdxUnityDetailsVO unityDetails = Optional.ofNullable(details.getUnityDetails())
                .orElse(new DdxUnityDetailsVO());
            unityDetails.setCatalogName(catalogName);
            details.setUnityDetails(unityDetails);

            workspace.setDdxPublishedLakeHouseDetails(details);
            jpaRepo.save(assembler.toEntity(workspace));

            log.info("Successfully updated DDX published lakehouse details for workspace: {}", workspaceId);
        } catch (Exception e) {
            log.error("Failed to update DDX lake house details for workspace {}: {}", workspaceId, e.getMessage(), e);
        }
    }
    
}