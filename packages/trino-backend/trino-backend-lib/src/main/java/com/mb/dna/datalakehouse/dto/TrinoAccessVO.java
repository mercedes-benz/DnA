package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoAccessVO  implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<TrinoCatalogRulesVO> catalogs;
	private List<TrinoSchemaRulesVO> schemas;
	private List<TrinoTableRulesVO> tables;
	
}
