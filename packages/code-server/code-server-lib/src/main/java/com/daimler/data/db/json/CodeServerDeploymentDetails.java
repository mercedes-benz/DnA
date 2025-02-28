package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;
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
	// private String technicalUserDetailsForIAMLogin;
	private String gitjobRunID;
	private String oneApiVersionShortName;
	private Boolean isSecuredWithCookie;
	private String deploymentType;
	private String clientId;
	private String redirectUri;
	private String ignorePaths;
	private String scope;
	private List<DeploymentAudit> deploymentAuditLogs;
	
}
