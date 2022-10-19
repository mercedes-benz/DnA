package com.daimler.data.db.jsonb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningIT {
	
	private String appReferenceStr;
	private String shortName;
	private String name;
	private String objectState;
	private String providerOrgRefstr;
	private String providerOrgId;
	private String providerOrgShortname;
	private String providerOrgDeptid;
	
}
