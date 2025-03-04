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
	private String wsid;
	private String projectName;
	private String shortid;
	private String action;
	private String type;                                   
	private String repo;
	private String branch;
	private String target_env;
	private Boolean valutInjectorEnable;
	private String secure_iam;
	private String appVersion;
	
}
