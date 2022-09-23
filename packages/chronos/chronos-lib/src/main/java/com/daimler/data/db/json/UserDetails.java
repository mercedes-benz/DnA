package com.daimler.data.db.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {

	private String id;
	private String firstName;
	private String lastName;
	private String department;
	private String email;
	private String mobileNumber;
	
}
