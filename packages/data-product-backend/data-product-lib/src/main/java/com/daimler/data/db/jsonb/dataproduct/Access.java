package com.daimler.data.db.jsonb.dataproduct;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Access {
	
	private List<String> accessType;
	private String kafka;
	private String oneApi;
	private String confidentiality;
	private boolean personalRelatedData;
	private boolean deletionRequirements;
	private boolean restrictDataAccess;
	private boolean minimumInformationCheck;
	
}
