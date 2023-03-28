package com.mb.dna.data.userprivilege.api.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrivilegeDto implements Serializable{

	private String id;
	private String userId;
	private String profile;
	private String givenName;
	private String surName;
	
}
