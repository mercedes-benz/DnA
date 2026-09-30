package com.daimler.data.dto.planningit;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningITApiItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String appReferenceStr;
    private String name;
    private String shortName;
    @JsonProperty("ObjectState")
    private String objectState;
    private String providerOrgRefstr;
    private String providerOrgId;
    private String providerOrgShortname;
    private String providerOrgDeptid;
}
