package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.json.catalogManangement.CdcTableDetail;
import com.daimler.data.db.json.catalogManangement.CdcTableDetailListDeserializer;
import com.daimler.data.db.json.catalogManangement.Databases;
import com.daimler.data.db.json.catalogManangement.LakehouseTableDetail;
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
    @JsonDeserialize(using = CdcTableDetailListDeserializer.class)
    private List<CdcTableDetail> publishedCdcTables;
    private List<String> publishedLakehouseTables;
    private List<LakehouseTableDetail> publishedLakehouseTableDetails;
}
