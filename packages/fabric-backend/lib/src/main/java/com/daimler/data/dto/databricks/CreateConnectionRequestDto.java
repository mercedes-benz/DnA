package com.daimler.data.dto.databricks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for creating a Connection in Databricks Unity Catalog
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConnectionRequestDto {

    /**
     * Name of the connection to create
     */
    private String name;

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
