package com.daimler.data.dto.databricks;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunNowNotebookParamsDto  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String y;
	private String X;
	private String X_pred;
	private String Config;
	private String fh;
	private String hierarchy;
	private String freq;
	private String results_folder;
	private String excel;
	private String correlationId;
	private String infotext;
}
