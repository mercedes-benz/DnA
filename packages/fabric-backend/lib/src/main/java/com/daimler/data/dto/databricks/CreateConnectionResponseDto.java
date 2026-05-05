package com.daimler.data.dto.databricks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Connection response from Databricks Unity Catalog
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateConnectionResponseDto {

    /**
     * Name of the connection
     */
    private String name;

    /**
     * Type of connection (e.g., SQLSERVER)
     */
    @JsonProperty("connection_type")
    private String connectionType;

    /**
     * Connection options
     */
    private Map<String, String> options;

    /**
     * Owner of the connection
     */
    private String owner;

    /**
     * Whether the connection is read-only
     */
    @JsonProperty("read_only")
    private Boolean readOnly;

    /**
     * Comment/description of the connection
     */
    private String comment;

    /**
     * Full name of the connection
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * JDBC URL for the connection
     */
    private String url;

    /**
     * Credential type (e.g., OAUTH_M2M)
     */
    @JsonProperty("credential_type")
    private String credentialType;

    /**
     * Unique connection ID
     */
    @JsonProperty("connection_id")
    private String connectionId;

    /**
     * Metastore ID where this connection resides
     */
    @JsonProperty("metastore_id")
    private String metastoreId;

    /**
     * Timestamp when the connection was created (in milliseconds)
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * User ID or name who created the connection
     */
    @JsonProperty("created_by")
    private String createdBy;

    /**
     * Timestamp when the connection was last updated
     */
    @JsonProperty("updated_at")
    private Long updatedAt;

    /**
     * User ID or name who last updated the connection
     */
    @JsonProperty("updated_by")
    private String updatedBy;

    /**
     * Securable type of the connection
     */
    @JsonProperty("securable_type")
    private String securableType;

    /**
     * Securable kind of the connection
     */
    @JsonProperty("securable_kind")
    private String securableKind;

    /**
     * Provisioning info
     */
    @JsonProperty("provisioning_info")
    private ProvisioningInfoDto provisioningInfo;

    /**
     * Indicates whether the operation was successful
     */
    private Boolean success = true;

    /**
     * Error message if operation failed
     */
    private String errorMessage;

    /**
     * Inner DTO for provisioning info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProvisioningInfoDto {
        private String state;
    }
}
