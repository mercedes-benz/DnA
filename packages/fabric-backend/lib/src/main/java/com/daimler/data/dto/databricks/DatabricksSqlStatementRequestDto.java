package com.daimler.data.dto.databricks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabricksSqlStatementRequestDto {

    @JsonProperty("warehouse_id")
    private String warehouseId;

    private String catalog;

    private String statement;

    @JsonProperty("wait_timeout")
    private String waitTimeout;
}
