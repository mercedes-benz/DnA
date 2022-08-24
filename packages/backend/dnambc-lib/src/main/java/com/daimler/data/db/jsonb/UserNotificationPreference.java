package com.daimler.data.db.jsonb;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference implements Serializable{

	private static final long serialVersionUID = -5702986518030330715L;
	
	private String userId;
	private NotificationPreference solutionNotificationPref;
	private NotificationPreference notebookNotificationPref;
	private NotificationPreference persistenceNotificationPref;
	private NotificationPreference dashboardNotificationPref;
	
}
