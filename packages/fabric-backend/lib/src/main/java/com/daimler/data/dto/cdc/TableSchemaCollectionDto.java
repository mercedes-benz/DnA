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
public class TableSchemaCollectionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("data")
    private SchemaDataWrapper data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SchemaDataWrapper {

        @JsonProperty("columns")
        private List<TableSchemaDto> columns;
    }
}