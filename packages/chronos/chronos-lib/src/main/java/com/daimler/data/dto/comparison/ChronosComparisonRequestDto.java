package com.daimler.data.dto.comparison;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChronosComparisonRequestDto  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String minio_endpoint;
	private List<String> runs_list;
	private String actuals_file;
	private String target_folder;
	
}
