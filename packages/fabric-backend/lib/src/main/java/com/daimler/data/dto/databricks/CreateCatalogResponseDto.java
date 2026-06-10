package com.daimler.data.dto.databricks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Foreign Catalog response from Databricks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCatalogResponseDto {

    /**
     * Name of the catalog
     */
    private String name;

    /**
     * Owner of the catalog
     */
    private String owner;

    /**
     * Comment/description of the catalog
     */
    private String comment;

    /**
     * Options for the catalog
     */
    private Map<String, String> options;

    /**
     * Type of the catalog
     */
    @JsonProperty("catalog_type")
    private String catalogType;

    /**
     * Name of the connection associated with this catalog
     */
    @JsonProperty("connection_name")
    private String connectionName;

    /**
     * Metastore ID where this catalog resides
     */
    @JsonProperty("metastore_id")
    private String metastoreId;

    /**
     * Timestamp when the catalog was created (in milliseconds)
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * User ID or name who created the catalog
     */
    @JsonProperty("created_by")
    private String createdBy;

    /**
     * Timestamp when the catalog was last updated
     */
    @JsonProperty("updated_at")
    private Long updatedAt;

    /**
     * User ID or name who last updated the catalog
     */
    @JsonProperty("updated_by")
    private String updatedBy;

    /**
     * Isolation mode of the catalog
     */
    @JsonProperty("isolation_mode")
    private String isolationMode;

    /**
     * Whether the catalog is browse-only
     */
    @JsonProperty("browse_only")
    private Boolean browseOnly;

    /**
     * Query federation attributes if any
     */
    @JsonProperty("query_federation_attributes")
    private QueryFederationAttributesDto queryFederationAttributes;

    /**
     * Unique ID of the catalog
     */
    private String id;

    /**
     * Full name of the catalog
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * Securable type of the catalog
     */
    @JsonProperty("securable_type")
    private String securableType;

    /**
     * Securable kind of the catalog
     */
    @JsonProperty("securable_kind")
    private String securableKind;

    /**
     * Resource name/path of the catalog
     */
    @JsonProperty("resource_name")
    private String resourceName;

    /**
     * Metastore version
     */
    @JsonProperty("metastore_version")
    private Long metastoreVersion;

    /**
     * Indicates whether the operation was successful
     */
    private Boolean success = true;

    /**
     * Error message if operation failed
     */
    private String errorMessage;

    /**
     * Inner DTO for query federation attributes
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryFederationAttributesDto {
        /**
         * ID of the connection used for federation
         */
        @JsonProperty("connection_id")
        private String connectionId;
    }
}
