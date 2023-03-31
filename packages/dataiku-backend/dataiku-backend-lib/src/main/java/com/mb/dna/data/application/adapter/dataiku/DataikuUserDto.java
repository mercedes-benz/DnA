package com.mb.dna.data.application.adapter.dataiku;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuUserDto extends DataikuErrorResponseDto implements Serializable{

	private String login;
	private String sourceType;
	private String displayName;
	private List<String> groups;
	private String email;
	private String userProfile;
	private Boolean enabled;
	
}
