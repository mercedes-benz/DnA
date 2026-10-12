package com.daimler.data.dto;

import java.io.Serializable;

import com.daimler.data.db.json.UserInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class CodespaceResourceExemptionDto implements Serializable {

    private String projectName;
    private UserInfo projectOwner;
    private Integer workspaceCount;
    private Boolean exemptInt;
    private Boolean exemptProd;

}