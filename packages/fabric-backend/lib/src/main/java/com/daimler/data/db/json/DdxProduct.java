package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DdxProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String workspaceName;
    private String workspaceId;
    private String lakehouseName;
    private String lakeHouseId;
    private Boolean isLakeHousesPublishedToDdx;
    private String productName;
    private String productId;
    private UserDetails createdBy;
    private Date createdOn;
    private Date modifiedOn;
    private DdxUnityDetails unityDetails;
    private List<Fabric2FabricDetail> fabric2fabricDetails;
    private List<MirroredCatalogDetail> mirroredCatalogDetails;
}
