package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoConnectorVO implements Serializable {

	private static final long serialVersionUID = 2233728617186747810L;
	private String name;
	private List<String> formats;
	private List<String> dataTypes;
	  
}
