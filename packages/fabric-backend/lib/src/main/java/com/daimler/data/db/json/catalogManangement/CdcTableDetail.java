package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.daimler.data.db.json.UserDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CdcTableDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String workspaceName;
    private String workspaceId;
    private String lakehouseName;
    private String lakeHouseId;
    private Boolean isLakeHousesPublishedToCdc;
    private FabricCatalogMetadata metadata;
    private MandatoryFields mandatoryFields;
    private List<String> publishedLakehouseTables;
    private List<LakehouseTableDetail> publishedLakehouseTableDetails;
    private UserDetails createdBy;
    private Date createdOn;
    private Date modifiedOn;
}
