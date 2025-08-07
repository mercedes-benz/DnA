package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.daimler.data.db.json.catalogManangement.Schemas;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Databases implements Serializable {
    private static final long serialVersionUID = 1L;

    private String databaseName;
    private List<Schemas> schemas;
}
