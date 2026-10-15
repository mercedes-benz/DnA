package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodespaceProjectAdminEligibilityDto {

	@JsonProperty("isProjectOwnerOrAdmin")
	private Boolean isProjectOwnerOrAdmin;
}
