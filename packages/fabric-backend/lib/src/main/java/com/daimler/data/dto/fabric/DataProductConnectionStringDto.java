package com.daimler.data.dto.fabric;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataProductConnectionStringDto {

    private String workspaceName;
    private String lakehouseName;
    private Boolean fullLakehouse;
    private String workspaceId;
    private String lakehouseId;

    private String catalogName;
    private String schemaName;
    private Boolean fullSchema;
}