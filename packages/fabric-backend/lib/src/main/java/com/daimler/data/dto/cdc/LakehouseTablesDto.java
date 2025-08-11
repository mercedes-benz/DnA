package com.daimler.data.dto.cdc;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LakehouseTablesDto implements Serializable{

	private static final long serialVersionUID = 1L;

    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("schemaName")
    private String schema;
}