package com.daimler.data.notifications.common.event.config;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.dto.dataproduct.ChangeLogVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericEventRecord implements Serializable {

	private static final long serialVersionUID = -646154079735583154L;

	private Boolean mailRequired;
	private String uuid;
	private String resourceId;
	private String publishingAppName;
	private String eventType;
	private String publishingUser;
	private String message;
	private String messageDetails;
	private String time;
	private List<String> subscribedUsers;
	private List<String> subscribedUsersEmail;
	private List<ChangeLogVO> changeLogs;

}
