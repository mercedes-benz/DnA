package com.daimler.data.dto.databricks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.daimler.data.dto.azure.AzureTokenRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Combined DTO for creating a Foreign Catalog with Azure authentication
 * Contains both Azure token request and catalog creation details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCatalogWithAuthRequestDto {

    /**
     * Azure token request details (tenantId, clientId, clientSecret, scope)
     */
    private AzureTokenRequestDto azureTokenRequest;

    /**
     * Name of the catalog to create
     */
    private String name;

    /**
     * Name of the connection to use for the catalog
     */
    @JsonProperty("connection_name")
    private String connectionName;

    /**
     * Optional comment/description for the catalog
     */
    private String comment;

    /**
     * Options for the catalog (e.g., database name for federated catalog)
     */
    private Map<String, String> options;
}
