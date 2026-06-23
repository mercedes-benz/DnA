package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.daimler.data.db.json.catalogManangement.Databases;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FabricCatalogMetadata implements Serializable{
    private static final long serialVersionUID = 1L;
    private FabricCatalogMetadata metadata;
    private String serviceName;
    private List<Databases> databases;
}
