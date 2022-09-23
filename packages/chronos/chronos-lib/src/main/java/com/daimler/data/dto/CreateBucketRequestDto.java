package com.daimler.data.dto;

import java.util.List;

import com.daimler.data.dto.forecast.CreatedByVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBucketRequestDto {

	private String bucketName;
	private List<CollaboratorsDto> collaborators;
	private String classificationType;
	private Boolean piiData;
	private CreatedByVO creator;
	private Boolean termsOfUse;
	
}
