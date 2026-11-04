package com.daimler.data.dto.pat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalDeploymentInfo {
	private String lastDeploymentStatus;
	private String deploymentUrl;
	private String lastDeployedBranch;
}
