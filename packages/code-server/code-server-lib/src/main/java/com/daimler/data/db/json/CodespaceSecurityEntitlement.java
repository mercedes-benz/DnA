package com.daimler.data.db.json;

import java.io.Serializable;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodespaceSecurityEntitlement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<String> name;
	//private List<CodespaceSecurityApiList> apiList;
	private String id;
	private String apiPattern;
	private String httpMethod;
	
}
