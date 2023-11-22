package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;

import com.daimler.data.controller.exceptions.GenericMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTableStmtResponseVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String tableStmt;
	private GenericMessage responseMsg;
	
}
