package com.mb.dna.datalakehouse.db.jsonb;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoAccess  implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<TrinoCatalogRules> catalogs;
	private List<TrinoSchemaRules> schemas;
	private List<TrinoTableRules> tables;
	
}
