package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class DeploymentManageInputDto {

	private String environment;
	private String action;
	private String repo;
	private String branch;
	private String target_env;
	private String projectName;
	private String appVersion;
	private String wsid;
	private Boolean valutInjectorEnable;
	
	// private String shortid;
	
	// private String type;                                   
	
	
	
	// private String secure_iam;
	
	
}
