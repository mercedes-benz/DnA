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
public class TrinoQueryResponseCollection implements Serializable{

	
	
//	private List<TrinoQueryResponse>
	private String state;
	private String query;
	private String updateType;
	private String queryType;
	
}
