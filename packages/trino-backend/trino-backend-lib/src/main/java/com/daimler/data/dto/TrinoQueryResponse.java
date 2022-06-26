package com.daimler.data.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrinoQueryResponse implements Serializable{

	
	private static final long serialVersionUID = 637799177908445000L;
	
	private String queryId;
	private String state;
	private String query;
	private String updateType;
	private String queryType;
	
}
