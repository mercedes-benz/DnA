package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodespaceSecurityConfig implements Serializable{

	private String status;
	private List<CodespaceSecurityRole> roles;
	private List<CodespaceSecurityEntitlement> entitlements;
	private List<CodespaceSecurityUserRoleMap> userRoleMappings;
	private List<String> openSegments;
	private Boolean isProtectedByDna;

	
}
