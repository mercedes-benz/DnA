package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureKeyVaultCollaborator implements Serializable {

	private static final long serialVersionUID = 1L;

	private String identifier;
	private String objectId;
	private String kind;
	private String displayName;
	private String accessLevel;
	/** Legacy records hold a single assignment id, newer ones hold one per granted role. */
	private String roleAssignmentId;
	private List<String> roleAssignmentIds;
}
