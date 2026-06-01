package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.daimler.data.db.json.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FabricCatalogMetadataDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<UserDetails> owners;
    private MandatoryFields mandatoryFields;
    private List<String> publishedLakehouseTables;
    private List<LakehouseTableDetail> publishedLakehouseTableDetails;
    @JsonDeserialize(using = CdcTableDetailListDeserializer.class)
    private List<CdcTableDetail> publishedCdcTables;
}
