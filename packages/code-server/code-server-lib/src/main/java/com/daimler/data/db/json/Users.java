package com.daimler.data.db.json;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users implements Serializable{

	private String id;
	private String firstName;
	private String lastName;
	private String department;
	private String email;
	private String mobileNumber;
}
