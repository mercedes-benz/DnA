package com.daimler.data.dto.databricks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClustersListResponseDto {

	@JsonProperty("clusters")
	private List<ClusterDto> clusters;

	/**
	 * Indicates whether the operation was successful
	 */
	private Boolean success = true;

	/**
	 * Error message if operation failed
	 */
	private String errorMessage;

}
