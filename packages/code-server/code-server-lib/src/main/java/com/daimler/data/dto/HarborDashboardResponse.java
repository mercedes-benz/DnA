package com.daimler.data.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HarborDashboardResponse {

    @JsonProperty("dashboard_id")
    private Integer dashboardId;

    @JsonProperty("dashboard_uid")
    private String dashboardUid;

    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    @JsonProperty("dashboard_created")
    private Boolean dashboardCreated;

    @JsonProperty("permissions_updated_for")
    private List<String> permissionsUpdatedFor;

    @JsonProperty("not_onboarded_users")
    private List<String> notOnboardedUsers;
}
