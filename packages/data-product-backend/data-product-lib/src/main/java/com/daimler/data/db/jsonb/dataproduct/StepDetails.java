package com.daimler.data.db.jsonb.dataproduct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StepDetails {

	private Long stepNumber;
	private String stepIconType;
	private String stepText;
}
