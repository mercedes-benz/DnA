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

        

        // --- Fabric Lakehouse & Connection Details ---
        String connectionName = "oneFabric_"+ lakehouseId;
        String catalogName = "westeurope_"+workspaceName; // need attention westeurope_ need to add as the prefix to CDC service name
        // String userToGrantAccess = userId;
        // String userToGrantAccess = "guumang@apac.corpdir.net";

        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        MessageDescription message = new MessageDescription();

        try {
            //fetch sql endpoint details for the lakehouse
            log.info("Fetching SQL endpoint details for workspace: {} and lakehouse: {}", workspaceId, lakehouseId);
            FabricSqlEndpointResponseDto sqlEndpoint = fabricWorkspaceClient.getSqlEndpoint(workspaceId, lakehouseId);
            log.info("sqlEndPoint details: {}", sqlEndpoint.toString());
            String fabricSqlEndpoint = sqlEndpoint.getProperties().getSqlEndpointProperties().getConnectionString();
            log.info("fabricSqlEndpoint details: {}", fabricSqlEndpoint);
            String fabricDatabaseName = sqlEndpoint.getDisplayName();
            log.info("fabricDatabaseName details: {}", fabricDatabaseName);// use same db name in calling create catalog API.

            // 0. Get Azure Access Token
            log.info("🔐 Requesting Azure access token for Databricks authentication");
            AzureTokenRequestDto tokenRequest = new AzureTokenRequestDto();
            tokenRequest.setTenantId(databricksSpTenantId);
            tokenRequest.setClientId(databricksSpClientId);
            tokenRequest.setClientSecret(databricksSpClientSecret);
            tokenRequest.setScope(databricksSpScope);
            tokenRequest.setGrantType("client_credentials");
            

            CreateCatalogRequestDto createCatalogRequest = new CreateCatalogRequestDto();
            createCatalogRequest.setName(catalogName);
            createCatalogRequest.setConnectionName(connectionName);
            createCatalogRequest.setOptions(new HashMap<String, String>(){{
                put("database", fabricDatabaseName);
            }});

            // -------------- call the create catalog API using the token --------------
            CreateCatalogResponseDto catalogResponse = azureTokenService.createCatalog(tokenRequest, createCatalogRequest);
            
            log.info("🎉 --- Databricks Fabric Setup Completed Successfully ---");

            publishDdxRequest.getDataProductConnections().forEach(connection -> {
                if(connection.getTechnology().equalsIgnoreCase("UnityCatalog")){
                    connection.setCatalogName(catalogName);
                }
            });

            publishDdxRequest.setDataProductConnections();
            //onboard to DDX

            DdxResponseDto onboardingResponse = fabricWorkspaceClient.ddxProductOnboarding(publishDdxRequest);
            if(onboardingResponse.getStatusCode()!=201){
                message.setMessage("Failed to onboard to DDX with error : " + onboardingResponse.getMessage());
			    errors.add(message);
			    responseMessage.setErrors(errors);
			    responseMessage.setSuccess("FAILED");
                return responseMessage;
            }

            updateDdxLakeHouseDetails(workspaceId, lakehouseId, fabricDatabaseName, catalogName, onboardingResponse, createdBy);

            message.setMessage("Product onboard to ddx successfully for product: " + publishDdxRequest.getDataProductName());
			responseMessage.setSuccess("SUCCESS");

            
        } catch (Exception e) {
            // TODO: handle exception
			message.setMessage("Failed before onboarding product to DDX with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred: for {} while onboarding to DDX for workspace {} lakehouse {} ", userId, workspaceId, lakehouseId, e);
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