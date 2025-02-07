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
public class CodeServerProjectDetails implements Serializable {

	private String projectName;
	private UserInfo projectOwner;
	private Date projectCreatedOn;
	private String recipeName;
	private List<UserInfo> projectCollaborators;
	private CodeServerBuildDetails buildDetails;
	private CodeServerDeploymentDetails prodDeploymentDetails;
	private CodeServerDeploymentDetails intDeploymentDetails;
	private CodeServerRecipeDetails recipeDetails;
	private String gitRepoName;
	private CodespaceSecurityConfig securityConfig;	
	private CodeServerLeanGovernanceFeilds dataGovernance;
}
