package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.controller.exceptions.GenericMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablesCollectionResponseDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<DatalakeTableVO> tables;
	private GenericMessage response;
}
