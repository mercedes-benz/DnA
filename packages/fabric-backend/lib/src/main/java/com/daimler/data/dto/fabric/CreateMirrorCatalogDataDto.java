package com.daimler.data.dto.fabric;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMirrorCatalogDataDto {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("dataProduct")
    private String dataProduct;

    @JsonProperty("catalogName")
    private String catalogName;

    @JsonProperty("Groups")
    private List<String> groups;

    @JsonProperty("connectionName")
    private String connectionName;

    @JsonProperty("WorkspaceID_MirrorCreation")
    private String workspaceIdMirrorCreation;

    @JsonProperty("WorkspaceName")
    private String workspaceName;

    @JsonProperty("network_connectionName")
    private String networkConnectionName;

    @JsonProperty("selection")
    private List<Selection> selection;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Selection {

        @JsonProperty("schema")
        private String schema;

        @JsonProperty("tables")
        private List<String> tables;
    }
}
