package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LakehouseColumnDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String columnName;
    private String colType;
    private Boolean enabled;
}
