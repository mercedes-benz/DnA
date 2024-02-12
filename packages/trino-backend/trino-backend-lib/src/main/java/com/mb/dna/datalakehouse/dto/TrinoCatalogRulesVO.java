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
public class TrinoCatalogRulesVO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String allow;
	private String catalog;
	private String user;
	
}
