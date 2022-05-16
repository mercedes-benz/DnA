package com.daimler.dna.notifications.common.dna.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DnaNotificationPreferenceClientImpl implements DnaNotificationPreferenceClient{

	@Value("${dna.uri}")
	private String dnaBaseUri;

	@Value("${dna.user.notificationPreferences.get.api}")
	private String notificationPreferencesApiUri;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public UserNotificationPrefVO getUserNotificationPreferences(String userId) {
		
		UserNotificationPrefVO res = new UserNotificationPrefVO();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			//headers.set("Authorization", authToken);
			
			String getUserNotificationPrefUri = dnaBaseUri + notificationPreferencesApiUri + "?userId=" + userId;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<UserNotificationPrefVO> response = restTemplate.exchange(getUserNotificationPrefUri, HttpMethod.GET, entity, UserNotificationPrefVO.class);
			if (response != null && response.hasBody()) {
				log.info("Success from dna getUserNotificationPreferences");
				res = response.getBody();
			}
		}catch (Exception e) {
			log.error("Error occured while calling dna getUserNotificationPreferences {}, returning default preferences", e.getMessage());
		}
		return res;
	}
	
}
