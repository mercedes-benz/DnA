package com.mb.dna.datalakehouse.db.jsonb;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoTableRules implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String catalog;
	private String schema;
	private String table;
	private List<String> privileges;
	private String user;
	
}
