package com.daimler.data.db.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeanIXDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appReferenceStr;
    private String name;
    private String shortName;
    private String objectState;
    private String providerOrgRefstr;
    private String providerOrgId;
    private String providerOrgShortname;
    private String providerOrgDeptid;
}