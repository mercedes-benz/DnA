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
public class FabricWorkspaceStatus implements Serializable{

	private static final long serialVersionUID = 1L;
	private String state;
	private List<EntitlementDetails> entitlements;
	private List<RoleDetails> roles;
	private List<GroupDetails> microsoftGroups;
	
}