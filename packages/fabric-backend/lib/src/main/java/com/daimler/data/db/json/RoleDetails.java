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
public class RoleDetails implements Serializable{

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String state;
	private String link;
	private List<EntitlementDetails> entitlements;
	private String assignEntitlementsState;
	private String roleOwner;
	private String globalRoleAssigner;
	private String roleApprover;
	private String secondaryRoleApprover;
}
