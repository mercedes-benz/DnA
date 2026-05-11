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
public class MirroredCatalogDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ddxCorrelationId;
    private String catalogName;
    private String schemaName;
    private String region;
    private Boolean fullSchema;
    private List<MirroredObjectDetail> objects;
    private String storageAccountUrl;
    private String ddxGroup;
    private String centralWorkspaceId;
    private String centralWorkspaceName;
    private String mirroredCatalogId;
    private String status;
    private String grantPermissionStatus;
    private String testRunId;
    private Date initiatedOn;
    private Date completedOn;
}
