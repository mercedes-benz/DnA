package com.daimler.data.db.jsonb;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserNotificationPreference implements Serializable{

	private static final long serialVersionUID = -5702986518030330715L;
	
	private String userId;
	private Boolean termsOfUse;
	private NotificationPreference solutionNotificationPref;
	private NotificationPreference notebookNotificationPref;
	private NotificationPreference persistenceNotificationPref;
	private NotificationPreference dashboardNotificationPref;
	private NotificationPreference dataComplianceNotificationPref;
	private NotificationPreference dataProductNotificationPref;
	private NotificationPreference chronosNotificationPref;
	private NotificationPreference codespaceNotificationPref;
	
}
