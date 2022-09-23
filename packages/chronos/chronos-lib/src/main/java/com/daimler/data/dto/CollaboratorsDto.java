package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollaboratorsDto {

	private String firstName;
	private String lastName;
	private String accesskey;
	private String department;
	private String email;
	private String mobileNumber;
	private PermissionsDto permission;
	
}
