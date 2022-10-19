package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class WorkBenchInputDto extends WorkBenchBaseInputDto{

	private String password;
	
}
