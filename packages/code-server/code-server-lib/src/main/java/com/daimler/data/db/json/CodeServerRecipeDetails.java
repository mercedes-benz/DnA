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
public class CodeServerRecipeDetails implements Serializable {

	private String id;
	private String recipeId;
	private String recipeName;
	private String ramSize;
	private String cpuCapacity;
	private String operatingSystem;
	private String environment;
	private String cloudServiceProvider;
	private String resource;
	private String repodetails;
	private List<String> software;
	private List<String> additionalServices;
	private String toDeployType;
	private String recipeType;
	private String gitPath;
	private String gitRepoLoc;
	
}
