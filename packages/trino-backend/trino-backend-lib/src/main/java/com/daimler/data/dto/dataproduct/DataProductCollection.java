package com.daimler.data.dto.dataproduct;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mb.dna.datalakehouse.dto.DataProductDetailsVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataProductCollection implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<DataProductDetailsVO> records;
	
}
