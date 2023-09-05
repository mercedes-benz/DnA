package com.daimler.data.dto.userinfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attrs {
	
	private List<String> dcxCompanyID;
	private List<String> objectClass;
	private List<String> c;
	private List<String> dcxUserSuspended;
	private List<String> cn;

	private List<String> uid;
	private List<String> departmentNumber;
	private List<String> mobile;
	private List<String> mail;
	private List<String> givenName;
	private List<String> sn;

}
