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
public class CodeServerLeanGovernanceFeilds implements Serializable {

	private static final long serialVersionUID = 1L;
	private String description;
	private String classificationType;
	private String divisionId;
	private String division;
	private String subDivisionId;
	private String subDivision;
	private String department;
	private String archerId;
	private String procedureID;
	private List<String> tags;
	private String typeOfProject;
	private Boolean piiData;
	private Boolean enableDeployApproval;
    
}
