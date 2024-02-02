package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) 
public class CodeServerDeploymentDetails implements Serializable {

	private Date lastDeployedOn;
	private UserInfo lastDeployedBy;
	private String lastDeployedBranch;
	private String deploymentUrl;
	private String lastDeploymentStatus;
	private Boolean secureWithIAMRequired;
	private String technicalUserDetailsForIAMLogin;
	private String gitjobRunID;
	private List<DeploymentAudit> deploymentAuditLogs;
	
	
}
