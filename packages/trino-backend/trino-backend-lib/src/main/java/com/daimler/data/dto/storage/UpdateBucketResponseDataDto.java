package com.daimler.data.dto.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateBucketResponseDataDto {

	private String id;
	private String bucketName;
	private String description;
	private String piiData;
	private String termsOfUse;
	private String classificationType;
	private List<CollaboratorsDto> collaborators;

}
