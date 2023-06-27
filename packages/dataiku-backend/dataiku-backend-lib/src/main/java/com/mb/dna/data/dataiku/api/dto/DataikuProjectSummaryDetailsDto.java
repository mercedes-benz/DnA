package com.mb.dna.data.dataiku.api.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectSummaryDetailsDto implements Serializable{

	private String id;
	private String name;
	private String projectKey;
	private String shortDesc;
	private String cloudProfile;
	private List<CollaboratorDetailsDto> collaborators;
	private String status;
	private String classificationType;
	private Boolean hasPii;
	private String divisionId;
	private String divisionName;
	private String subdivisionId;
	private String subdivisionName;
	private String department;
	private String solutionId;
	private DataikuProjectTimeStampDetailsDto creationTag;
	
}
