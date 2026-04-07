package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.json.catalogManangement.Databases;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FabricCatalogMetadataDetails implements Serializable{
    private static final long serialVersionUID = 1L;

    private FabricCatalogMetadata metadata;
    private List<UserDetails> owners;
    private MandatoryFields mandatoryFields;
}
