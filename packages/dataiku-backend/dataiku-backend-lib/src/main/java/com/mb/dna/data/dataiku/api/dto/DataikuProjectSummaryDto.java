package com.mb.dna.data.dataiku.api.dto;

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
public class DataikuProjectSummaryDto implements Serializable{
    
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private String projectKey;
	private String shortDesc;
	private Boolean isProjectAdmin;
	private String role;
	private DataikuProjectCheckListDto checklists;
	private String cloudProfile;
	private List<CollaboratorDetailsDto> collaborators;
	private String status;
	private String classificationType;
	private String solutionId;
	private List<String> tags;
	private DataikuProjectTimeStampDetailsDto creationTag;
}
