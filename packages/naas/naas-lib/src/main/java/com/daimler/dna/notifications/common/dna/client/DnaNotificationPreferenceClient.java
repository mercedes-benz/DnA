package com.daimler.dna.notifications.common.dna.client;

import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;

public interface DnaNotificationPreferenceClient {

	UserNotificationPrefVO getUserNotificationPreferences(String userId);
	
}
