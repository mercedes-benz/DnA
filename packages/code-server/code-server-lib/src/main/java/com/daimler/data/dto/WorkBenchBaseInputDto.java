package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class WorkBenchBaseInputDto {

	private String environment;
	private String wsid;
	private String shortid;
	private String action;
	private String type;
	
}
