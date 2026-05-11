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
public class DdxPublishedLakeHouseDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean isLakeHousesPublishedToDdx;
    private List<String> publishedLakeHouseNames;
    private String productName;
    private String productId;
    private UserDetails createdBy;
    private Date createdOn;
    private Date modifiedOn;
    private Fabric2FabricDetails fabric2FabricDetails;
    private DdxUnityDetails unityDetails;
}
