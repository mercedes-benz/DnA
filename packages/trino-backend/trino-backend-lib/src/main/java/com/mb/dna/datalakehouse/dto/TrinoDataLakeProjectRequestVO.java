package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoDataLakeProjectRequestVO implements Serializable {

	private static final long serialVersionUID = 1L;
	private TrinoDataLakeProjectVO data;
}
