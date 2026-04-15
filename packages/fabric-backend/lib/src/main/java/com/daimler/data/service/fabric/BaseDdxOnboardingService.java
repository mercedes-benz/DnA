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
import com.daimler.data.assembler.DdxDataProductsDetailsAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.entities.DdxDataProductsDetailsNsql;
import com.daimler.data.db.json.DdxDataProductsDetail;
import com.daimler.data.db.json.DdxProduct;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.db.repo.ddxDataProductsDetails.DdxDataProductsDetailsRepository;
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
import com.daimler.data.dto.fabric.DdxOnboardingResultDto;
import com.daimler.data.dto.fabric.DdxResponseDto;
import com.daimler.data.service.azure.AzureTokenService;
import com.daimler.data.dto.azure.AzureTokenRequestDto;
import com.daimler.data.dto.azure.AzureTokenResponseDto;
import com.daimler.data.dto.databricks.CreateCatalogRequestDto;
import com.daimler.data.dto.databricks.CreateCatalogResponseDto;
import com.daimler.data.dto.databricks.CreateConnectionRequestDto;
import com.daimler.data.dto.databricks.CreateConnectionResponseDto;
import com.daimler.data.dto.fabric.DataProductConnectionStringDto;
import com.daimler.data.dto.fabric.DataProductConStringUnityDto;
import com.daimler.data.dto.fabric.DataProductConStringFabricDto;
import com.daimler.data.dto.databricks.DatabricksSqlStatementResponseDto;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private DdxDataProductsDetailsAssembler ddxDataProductsDetailsAssembler;

    @Autowired
    private DdxDataProductsDetailsRepository ddxDataProductsDetailsRepo;

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

    @Value("${fabricWorkspaces.scope}")
    private String scope;

    @Override
    @Transactional
    public DdxOnboardingResultDto onboardToDdx(DdxOnboardingRequestDto publishDdxRequest, String workspaceId, String workspaceName, String lakehouseId, String userId, CreatedByVO createdBy) {

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        MessageDescription message = new MessageDescription();
        DdxResponseDto ddxResponse = null;

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
            // String catalogName = "westeurope_fcos_dna_testddxlakehouseschema_catalog";

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
            if (databricksSpTenantId == null || databricksSpClientId == null || databricksSpClientSecret == null || databricksSpScope == null) {
                throw new RuntimeException("Databricks service principal configuration is incomplete");
            }

            AzureTokenRequestDto tokenRequest = new AzureTokenRequestDto();
            tokenRequest.setTenantId(databricksSpTenantId);
            tokenRequest.setClientId(databricksSpClientId);
            tokenRequest.setClientSecret(databricksSpClientSecret);
            tokenRequest.setScope(databricksSpScope);
            tokenRequest.setGrantType("client_credentials");

            // 4. Create Connection in Databricks Unity Catalog
            log.info("Creating Databricks connection: {} for workspace: {}", connectionName, workspaceId);
            CreateConnectionRequestDto createConnectionRequest = new CreateConnectionRequestDto();
            createConnectionRequest.setName(connectionName);
            createConnectionRequest.setConnectionType("SQLSERVER");
            Map<String, String> connectionOptions = new HashMap<>();
            connectionOptions.put("host", fabricSqlEndpoint);
            connectionOptions.put("port", "1433");
            connectionOptions.put("client_id", databricksSpClientId);
            connectionOptions.put("client_secret", databricksSpClientSecret);
            connectionOptions.put("token_endpoint", "https://login.microsoftonline.com/" + databricksSpTenantId + "/oauth2/v2.0/token");
            createConnectionRequest.setOptions(connectionOptions);
            createConnectionRequest.setComment("Connection for fabric lakehouse: " + lakehouseId + " in workspace: " + workspaceId);

            CreateConnectionResponseDto connectionResponse;
            try {
                connectionResponse = azureTokenService.createConnection(tokenRequest, createConnectionRequest);
            } catch (Exception e) {
                log.error("Failed to create connection: {} for workspace: {}", connectionName, workspaceId, e);
                throw new RuntimeException("Databricks connection creation failed: " + e.getMessage(), e);
            }

            if (connectionResponse == null) {
                throw new RuntimeException("Connection creation response is null");
            }

            if (Boolean.TRUE.equals(connectionResponse.getSuccess())) {
                log.info("Connection created successfully: {}", connectionResponse.getName());
                connectionName = connectionResponse.getName();
            } else {
                String connError = connectionResponse.getErrorMessage() != null ? connectionResponse.getErrorMessage() : "";
                if (connError.toLowerCase().contains("already exists")) {
                    log.info("Connection {} already exists, proceeding with existing connection", connectionName);
                } else {
                    log.error("Failed to create connection: {}, error: {}", connectionName, connError);
                    throw new RuntimeException("Databricks connection creation failed: " + connError);
                }
            }

            // 5. Create Catalog Request
            CreateCatalogRequestDto createCatalogRequest = new CreateCatalogRequestDto();
            createCatalogRequest.setName(catalogName);
            createCatalogRequest.setConnectionName(connectionName);
            createCatalogRequest.setComment("Federated catalog for fabric lakehouse: " + lakehouseId + " in workspace: " + workspaceId);
            createCatalogRequest.setOptions(new HashMap<String, String>(){{
                put("database", fabricDatabaseName);
            }});

            // 6. Create Catalog via Azure Token Service
            log.info("Creating Databricks catalog: {} with connection: {}", catalogName, connectionName);
            CreateCatalogResponseDto catalogResponse;
            try {
                catalogResponse = azureTokenService.createCatalog(tokenRequest, createCatalogRequest);
                log.info("catalogResponse :=================== {} ========================", catalogResponse);
            } catch (Exception e) {
                log.error("Failed to create catalog: {} for workspace: {}", catalogName, workspaceId, e);
                throw new RuntimeException("Databricks catalog creation failed: " + e.getMessage(), e);
            }

            if (catalogResponse == null) {
                throw new RuntimeException("Catalog creation response is null");
            }

            if (Boolean.TRUE.equals(catalogResponse.getSuccess())) {
                log.info("✅ Catalog created successfully: {}", catalogResponse.getName());
            } else {
                String errorMsg = catalogResponse.getErrorMessage() != null ? catalogResponse.getErrorMessage() : "";
                if (errorMsg.toLowerCase().contains("already exists")) {
                    log.info("Catalog {} already exists, proceeding with existing catalog for onboarding", catalogName);
                } else {
                    log.error("Failed to create catalog: {}, error: {}", catalogName, errorMsg);
                    throw new RuntimeException("Databricks catalog creation failed: " + errorMsg);
                }
            }

            //6.2 compute process
            log.info("Starting catalog compute process for catalog: {}", catalogName);
            DatabricksSqlStatementResponseDto computeResponse;
            try {
                computeResponse = fabricWorkspaceClient.catalogComputeProcess(catalogName);
            } catch (Exception e) {
                log.error("Catalog compute process failed for catalog: {}", catalogName, e);
                throw new RuntimeException("Catalog compute process failed: " + e.getMessage(), e);
            }

            if (computeResponse == null || computeResponse.getStatus() == null) {
                throw new RuntimeException("Catalog compute process returned null response for catalog: " + catalogName);
            }

            if ("FAILED".equals(computeResponse.getStatus().getState())) {
                String errorDetail = "";
                if (computeResponse.getStatus().getError() != null) {
                    errorDetail = " [" + computeResponse.getStatus().getError().getErrorCode() + "]: "
                            + computeResponse.getStatus().getError().getMessage();
                }
                throw new RuntimeException("Catalog compute process failed for catalog: " + catalogName + errorDetail);
            }

            if (!"SUCCEEDED".equals(computeResponse.getStatus().getState())) {
                throw new RuntimeException("Catalog compute process returned unexpected state: " + computeResponse.getStatus().getState() + " for catalog: " + catalogName);
            }

            log.info("Catalog compute process completed successfully for catalog: {}. Schemas found: {}",
                catalogName, computeResponse.getResult() != null ? computeResponse.getResult().getRowCount() : 0);

            log.info("🎉 --- Databricks Fabric Setup Completed Successfully ---");
            log.info("DDX Onboarding Request=========={}===========", publishDdxRequest);

            // 7. Prepare DDX Onboarding Request
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
                    
                    // DataProductConnectionStringDto connectionString = connection.getDataProductConnectionString();
                    // if (connectionString == null) {
                    //     throw new RuntimeException("DataProductConnectionString is null for a connection");
                    // }
                    
                    if ("UnityCatalog".equals(connection.getTechnology())) {
                        DataProductConStringUnityDto unityDto = new DataProductConStringUnityDto();

                        unityDto.setCatalogName(catalogName);
                        unityDto.setSchemaName("dbo");
                        unityDto.setFullSchema(true);

                        connection.setDataProductConnectionString(unityDto);
                    }
                    else if ("Fabric".equals(connection.getTechnology())) {
                        DataProductConStringFabricDto fabricDto = new DataProductConStringFabricDto();

                        fabricDto.setLakehouseId(lakehouseId);
                        fabricDto.setLakehouseName("TestLakehouse");
                        fabricDto.setWorkspaceId(workspaceId);
                        fabricDto.setWorkspaceName(workspaceName);
                        fabricDto.setFullLakehouse(true);

                        connection.setDataProductConnectionString(fabricDto);
                    } else {
                        throw new RuntimeException("Unsupported technology type: " + connection.getTechnology());
                    }

                });
                log.info(" DDX request: {}", publishDdxRequest);
                log.info("Prepared DDX onboarding request: {}", publishDdxRequest.getDataProductName());
            } catch (RuntimeException e) {
                log.error("Failed to prepare DDX onboarding request: {}", e.getMessage());
                throw e;
            }

            // 8. Onboard to DDX
            log.info("Onboarding product: {} to DDX for workspace: {}", publishDdxRequest.getDataProductName(), workspaceId);

            log.info("Calling DDX onboarding API with request: {}", publishDdxRequest);

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

            // 9. Validate DDX Onboarding Response
            if (!"200".equals(onboardingResponse.getStatus())) {
                String errorMsg = onboardingResponse.getMessage() != null 
                    ? onboardingResponse.getMessage() 
                    : "Unknown error from DDX service";
                log.warn("DDX onboarding failed with status: {} and message: {}", onboardingResponse.getStatus(), errorMsg);
                message.setMessage(errorMsg);
                errors.add(message);
                responseMessage.setErrors(errors);
                responseMessage.setSuccess("FAILED");
                return DdxOnboardingResultDto.builder().responseMessage(responseMessage).ddxResponse(onboardingResponse).build();
            }
            onboardingResponse.setStatusCode(201);
            // Clear internal fields so only data-relevant fields appear in the response
            onboardingResponse.setStatus(null);
            onboardingResponse.setStatusCode(null);
            onboardingResponse.setSystem(null);
            ddxResponse = onboardingResponse;

            // 10. Update DDX Lakehouse Details
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
                return DdxOnboardingResultDto.builder().responseMessage(responseMessage).ddxResponse(ddxResponse).build();
            }

            // 11. Success Response
            responseMessage.setSuccess("SUCCESS");
            log.info("✅ Successfully onboarded product: {} to DDX for workspace: {}. DataProductId: {}, DofUrl: {}",
                publishDdxRequest.getDataProductName(), workspaceId, onboardingResponse.getDataProductId(), onboardingResponse.getDofUrl());

        } catch (IllegalArgumentException e) {
            message.setMessage("Invalid input parameter: " + e.getMessage());
            errors.add(message);
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("FAILED");
            log.error("Invalid input provided for onboarding to DDX - workspace: {}, lakehouse: {}, userId: {}", workspaceId, lakehouseId, userId, e);
            return DdxOnboardingResultDto.builder().responseMessage(responseMessage).build();
        } catch (RuntimeException e) {
            message.setMessage("Failed to onboard product to DDX: " + e.getMessage());
            errors.add(message);
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("FAILED");
            log.error("Runtime exception occurred while onboarding to DDX for userId: {}, workspace: {}, lakehouse: {}", userId, workspaceId, lakehouseId, e);
            return DdxOnboardingResultDto.builder().responseMessage(responseMessage).build();
        } catch (Exception e) {
            message.setMessage("Unexpected error occurred during DDX onboarding: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            errors.add(message);
            responseMessage.setErrors(errors);
            responseMessage.setSuccess("FAILED");
            log.error("Unexpected exception occurred for userId: {} during onboarding to DDX for workspace: {} and lakehouse: {}", userId, workspaceId, lakehouseId, e);
            return DdxOnboardingResultDto.builder().responseMessage(responseMessage).build();
        }

        return DdxOnboardingResultDto.builder().responseMessage(responseMessage).ddxResponse(ddxResponse).build();
    }

    private void updateDdxLakeHouseDetails(String workspaceId, String lakehouseId, String lakehouseName, String catalogName, DdxResponseDto onboardingResponse, CreatedByVO createdBy) {
        try {
            // Load the workspace entity directly from the repository to avoid the lossy
            // getById round-trip (VO <-> Entity conversion drops fields like
            // capacityAssignmentProgress and can overwrite JSONB data).
            Optional<FabricWorkspaceNsql> entityOpt = jpaRepo.findById(workspaceId);
            if (entityOpt.isEmpty()) {
                log.error("Workspace not found with id {} while updating DDX lakehouse details", workspaceId);
                return;
            }
            FabricWorkspaceNsql workspaceEntity = entityOpt.get();
            FabricWorkspace workspaceData = workspaceEntity.getData();

            // Build the DDX product object
            DdxPublishedLakeHouseDetailsVO details = new DdxPublishedLakeHouseDetailsVO();
            details.setWorkspaceId(workspaceId);
            details.setWorkspaceName(workspaceData != null ? workspaceData.getName() : null);
            details.setLakehouseName(lakehouseName);
            details.setLakeHouseId(lakehouseId);
            details.setIsLakeHousesPublishedToDdx(true);
            details.setProductName(onboardingResponse.getDataProductName());
            details.setProductId(String.valueOf(onboardingResponse.getDataProductId()));
            details.setCreatedBy(createdBy);
            Date now = new Date();
            details.setCreatedOn(now);
            details.setModifiedOn(now);

            DdxUnityDetailsVO unityDetails = new DdxUnityDetailsVO();
            unityDetails.setCatalogName(catalogName);
            details.setUnityDetails(unityDetails);

            DdxProduct product = ddxDataProductsDetailsAssembler.toProduct(details);

            // Save to ddx_dataProducts_details_nsql table.
            // Each row is keyed by workspaceId; ddxProducts is a list of all
            // published products for that workspace.
            Optional<DdxDataProductsDetailsNsql> existingOpt = ddxDataProductsDetailsRepo.findById(workspaceId);
            DdxDataProductsDetailsNsql ddxEntity;
            if (existingOpt.isPresent()) {
                ddxEntity = existingOpt.get();
                DdxDataProductsDetail data = ddxEntity.getData();
                if (data == null) {
                    data = new DdxDataProductsDetail();
                    data.setDdxProducts(new ArrayList<>());
                }
                List<DdxProduct> products = data.getDdxProducts();
                if (products == null) {
                    products = new ArrayList<>();
                }
                // Avoid duplicates by productName (unique identifier for DDX products)
                String productName = onboardingResponse.getDataProductName();
                boolean exists = products.stream()
                    .anyMatch(p -> productName != null && productName.equals(p.getProductName()));
                if (!exists) {
                    products.add(product);
                }
                data.setDdxProducts(products);
                ddxEntity.setData(data);
            } else {
                ddxEntity = new DdxDataProductsDetailsNsql();
                ddxEntity.setId(workspaceId);
                DdxDataProductsDetail data = new DdxDataProductsDetail();
                List<DdxProduct> products = new ArrayList<>();
                products.add(product);
                data.setDdxProducts(products);
                ddxEntity.setData(data);
            }
            ddxDataProductsDetailsRepo.save(ddxEntity);

            // Update the workspace's ddxPublishedLakeHouseDetails list directly
            // on the managed entity — no VO round-trip needed.
            if (workspaceData != null) {
                List<String> detailIds = workspaceData.getDdxPublishedLakeHouseDetails();
                if (detailIds == null) {
                    detailIds = new ArrayList<>();
                }
                if (!detailIds.contains(lakehouseId)) {
                    detailIds.add(lakehouseId);
                }
                workspaceData.setDdxPublishedLakeHouseDetails(detailIds);
                workspaceEntity.setData(workspaceData);
                jpaRepo.save(workspaceEntity);
            }

            log.info("Successfully updated DDX published lakehouse details for workspace: {}, lakehouseId: {}", workspaceId, lakehouseId);
        } catch (Exception e) {
            log.error("Failed to update DDX lake house details for workspace {}: {}", workspaceId, e.getMessage(), e);
        }
    }
    
}
