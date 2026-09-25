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
public class AzureKeyVaultCollaborator implements Serializable {

	private static final long serialVersionUID = 1L;

	private String identifier;
	private String shortId;
	private String firstName;
	private String lastName;
	private String objectId;
	private String principalType;
	private String kind;
	private String displayName;
	private String role;
	private String accessLevel;
	private List<String> roles;
	private String roleAssignmentId;
	private List<String> roleAssignmentIds;
}
