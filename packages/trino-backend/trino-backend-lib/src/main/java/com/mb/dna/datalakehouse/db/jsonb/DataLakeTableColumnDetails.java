package com.mb.dna.datalakehouse.db.jsonb;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataLakeTableColumnDetails implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String columnName;
	private String comment;
	private String dataType;
	private Boolean notNullConstraintEnabled;

}
