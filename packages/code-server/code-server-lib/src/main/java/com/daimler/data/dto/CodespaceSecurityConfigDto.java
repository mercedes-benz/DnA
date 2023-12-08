package com.daimler.data.dto;

import java.io.Serializable;

import com.daimler.data.db.json.UserInfo;
import com.daimler.data.db.json.CodespaceSecurityConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class CodespaceSecurityConfigDto implements Serializable{
    
    private String id;
    private String projectName;
    private UserInfo projectOwner;
    private CodespaceSecurityConfig securityConfig;
    
}
