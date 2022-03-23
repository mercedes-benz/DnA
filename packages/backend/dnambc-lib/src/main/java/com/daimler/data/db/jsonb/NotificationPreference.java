package com.daimler.data.db.jsonb;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference implements Serializable{

	private boolean enableAppNotifications;
	private boolean enableEmailNotifications;
	
}
