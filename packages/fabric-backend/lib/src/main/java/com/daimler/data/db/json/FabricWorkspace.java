package com.daimler.data.db.json;

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
public class FabricWorkspace {

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
	private String role;
	
}
