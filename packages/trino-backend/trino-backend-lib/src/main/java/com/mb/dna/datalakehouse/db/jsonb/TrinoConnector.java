package com.mb.dna.datalakehouse.db.jsonb;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoConnector implements Serializable {
	
	private static final long serialVersionUID = 8943578183642928886L;
	private List<String> formats;
	private List<String> dataTypes;
	
}
