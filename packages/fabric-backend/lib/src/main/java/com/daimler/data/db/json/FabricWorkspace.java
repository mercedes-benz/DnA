package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FabricWorkspace implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;
	private String typeOfProject;
	private String description;
	private String divisionId;
	private String division;
	private String subDivisionId;
	private String subDivision;
	private String department;
	private List<String> tags;
	private String dataClassification;
	private String archerId;
	private String procedureId;
	private Boolean termsOfUse;
	private Boolean hasPii;
	private String costCenter;
	private String internalOrder;
	private String capacityAssignmentProgress;
	private Capacity capacity;
	private UserDetails createdBy;
	private Date createdOn;
	private Date lastModifiedOn;
	private String role;
	private FabricWorkspaceStatus status;
	private List<ProjectDetails> relatedSolutions;
	private List<ProjectDetails> relatedReports;
	private List<Lakehouse> lakehouses;
	private String initiatedBy;
	// private String secondaryRoleApproverId;
	// private String customEntitlementName;
	private String customGroupName;
}
