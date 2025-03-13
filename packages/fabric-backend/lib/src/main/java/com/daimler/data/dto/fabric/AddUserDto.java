package com.daimler.data.dto.fabric;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddUserDto  implements Serializable{

	private static final long serialVersionUID = 1L;

	private String emailAddress;
	private String groupUserAccessRight;
	
//	private String identifier;
//	private String principalType;// App,Group,None,User
//	private String displayName;
//	private String graphId;
//	private ServicePrincipalProfile profile;
//	private String userType;
	
}
