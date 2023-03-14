package com.mb.dna.data.application.adapter.dataiku;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuUserDto {

	private String login;
	private String sourceType;
	private String displayName;
	private List<String> groups;
	private String email;
	private String userProfile;
	private Boolean enabled;
	
}
