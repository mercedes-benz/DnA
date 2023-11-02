package com.mb.dna.datalakehouse.db.jsonb;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoSchemaRules implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String catalog;
	private Boolean owner;
	private String schema;
	private String user;
	
}
