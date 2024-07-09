package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoConnectorsCollectionVO implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<TrinoConnectorVO> connectors;
	private List<String> reserveWords;

}