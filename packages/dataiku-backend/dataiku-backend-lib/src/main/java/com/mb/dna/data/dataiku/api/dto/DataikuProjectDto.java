package com.mb.dna.data.dataiku.api.dto;

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
public class DataikuProjectDto implements Serializable {

	private String id;
	private String projectName;
	private String description;
	private String cloudProfile;
	private List<CollaboratorDetailsDto> collaborators;
	private String createdBy;
	private Date createdOn;
	private String status;
	private String classificationType;
	private Boolean hasPii;
	private String divisionId;
	private String divisionName;
	private String subdivisionId;
	private String subdivisionName;
	private String department;
	
}
