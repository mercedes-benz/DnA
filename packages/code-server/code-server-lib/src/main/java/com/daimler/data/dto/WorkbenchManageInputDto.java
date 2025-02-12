package com.daimler.data.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class WorkbenchManageInputDto {
	
	private String environment;
	private String wsid;
	private String shortid;
	private String action;
	private String type;
	private String repo;
	private String resource;
	private String pat;
	private String isCollaborator;
	private String profile;
	private String storage_capacity;
	private String mem_guarantee;
	private String mem_limit;
	private double cpu_limit;
	private double cpu_guarantee;
	private String pathCheckout;
	private String cloudServiceProvider;
	private List<String> extraContainers;
	
}
