package com.daimler.data.dto.cdc;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LakehouseTablesCollectionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("data")
    private DataWrapper data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataWrapper {
        @JsonProperty("tables")
        private List<LakehouseTablesDto> tables;
    }
}
