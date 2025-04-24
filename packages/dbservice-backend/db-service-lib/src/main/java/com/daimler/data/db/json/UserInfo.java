package com.daimler.data.db.json;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable{

	private String id;
	private String firstName;
	private String lastName;
	private String department;
	private String email;
	private String mobileNumber;
	private String gitUserName;
	private Boolean isAdmin;
}

