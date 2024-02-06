package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatalakeTableVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String tableName;
	private String dataFormat;
	private String description;
	private BigDecimal xCoOrdinate ;
	private BigDecimal yCoOrdinate;
	private List<DataLakeTableCollabDetailsVO> collabs;
	private List<DataLakeTableColumnDetailsVO> columns;
	private String externalLocation;
}
