package com.daimler.data.db.jsonb.report;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KPIName implements Serializable{
	 
	private static final long serialVersionUID = 2790979409985146122L;
	
	private String kpiName;
	private String kpiClassification;

}
