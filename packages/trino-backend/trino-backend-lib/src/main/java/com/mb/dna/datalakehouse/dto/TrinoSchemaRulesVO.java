package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TrinoSchemaRulesVO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String catalog;
	private Boolean owner;
	private String schema;
	private String user;
	
}
