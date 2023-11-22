package com.daimler.data.dto.storage;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.application.auth.CreatedByVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateBucketRequestDto  implements Serializable{

	private static final long serialVersionUID = 1L;

	private String bucketName;
	private String id;
	private List<CollaboratorsDto> collaborators;
	private String classificationType;
	private Boolean piiData;
	private CreatedByVO createdBy;
	private Boolean termsOfUse;
	
}
