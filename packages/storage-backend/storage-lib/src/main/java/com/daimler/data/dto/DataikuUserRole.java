package com.daimler.data.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DataikuUserRole
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataikuUserRole {

	private String login;
	private String sourceType;
	private String displayName;
	private List<String> groups;
	private String email;
	private String userProfile;

}
