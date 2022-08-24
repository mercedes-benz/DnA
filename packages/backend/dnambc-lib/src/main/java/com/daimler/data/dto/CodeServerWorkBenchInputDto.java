package com.daimler.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeServerWorkBenchInputDto {

	private String shortid;
	private String password;
	private String action;
	private String type;
	
}
