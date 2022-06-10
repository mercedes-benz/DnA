package com.daimler.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

	private String user;
	private String group;
	private Boolean admin;
	private Boolean readProjectContent;
	private Boolean writeProjectContent;
	private Boolean exportDatasetsData;
	private Boolean readDashboards;
	private Boolean writeDashboards;
	private Boolean moderateDashboards;
	private Boolean runScenarios;
	private Boolean manageDashboardAuthorizations;
	private Boolean manageExposedElements;
	private Boolean manageAdditionalDashboardUsers;
	private Boolean executeApp;

}
