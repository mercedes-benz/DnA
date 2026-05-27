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
public class DdxMirroredCatalogProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataProductName;
    private String catalogName;
    private String schemaName;
    private String region;
    private Boolean fullSchema;
    private List<MirroredObjectDetail> objects;
    private String storageAccountUrl;
    private String ddxCorrelationId;
    private String ddxGroupPermission;
    private String workspaceId;
    private String workspaceName;
    private String status;
    private Date initiatedOn;
    private Date completedOn;
    private MirroredCatalogDetail mirrorCatalogDetails;
    private List<DdxGroupDetail> ddxGroupDetails;
}
