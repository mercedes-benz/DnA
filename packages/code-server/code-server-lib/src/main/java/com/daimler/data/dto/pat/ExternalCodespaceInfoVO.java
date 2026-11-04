package com.daimler.data.dto.pat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalCodespaceInfoVO {
	private String workspaceId;
	private String projectName;
	private String recipeId;
	private String cloudServiceProvider;
	private String serverStatus;
	private ExternalDeploymentInfo intDeployment;
	private ExternalDeploymentInfo prodDeployment;
}
