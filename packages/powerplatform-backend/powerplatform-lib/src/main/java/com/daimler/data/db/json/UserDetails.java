package com.daimler.data.db.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails implements Serializable{

	private static final long serialVersionUID = 1L;
	private String id;
	private String firstName;
	private String lastName;
	private String department;
	private String email;
	private String mobileNumber;
	
}
