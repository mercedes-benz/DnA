package com.daimler.data.dto.cdc;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableSchemaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("columnName")
    private String columnName;

    @JsonProperty("colType")
    private String colType;

    @JsonProperty("colConstraint")
    private String colConstraint;
}
