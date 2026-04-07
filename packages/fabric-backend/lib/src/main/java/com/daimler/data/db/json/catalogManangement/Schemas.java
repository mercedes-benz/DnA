package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.daimler.data.db.json.catalogManangement.Tables;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schemas implements Serializable {
    private static final long serialVersionUID = 1L;

    private String schemaName;
    private List<Tables> tables;
}
