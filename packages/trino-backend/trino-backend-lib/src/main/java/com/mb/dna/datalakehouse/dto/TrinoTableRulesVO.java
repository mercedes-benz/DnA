package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TrinoTableRulesVO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String catalog;
	private String schema;
	private String table;
	private List<String> privileges;
	private String user;
	
}
