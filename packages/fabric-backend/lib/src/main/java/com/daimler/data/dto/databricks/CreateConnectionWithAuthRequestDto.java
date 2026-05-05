package com.daimler.data.dto.databricks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.daimler.data.dto.azure.AzureTokenRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Combined DTO for creating a Connection with Azure authentication
 * Contains both Azure token request and connection creation details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConnectionWithAuthRequestDto {

    /**
     * Azure token request details (tenantId, clientId, clientSecret, scope)
     */
    private AzureTokenRequestDto azureTokenRequest;

    /**
     * Lakehouse ID used to derive the connection name (oneFabric_<lakehouseId>)
     */
    @JsonProperty("lakehouse_id")
    private String lakehouseId;

    /**
     * Type of connection (e.g., SQLSERVER)
     */
    @JsonProperty("connection_type")
    private String connectionType;

    /**
     * Connection options (host, port, client_id, client_secret, token_endpoint, etc.)
     */
    private Map<String, String> options;

    /**
     * Optional comment/description for the connection
     */
    private String comment;
}
