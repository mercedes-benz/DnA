package com.daimler.data.db.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunDetails {

	 private String id;
	 private String jobId;
	 private String runId;
	 private String numberInJob;
	 
	 private RunState runState;
	 private Boolean isDelete;
	 
	 private String triggeredBy;
	 private Date triggeredOn;
	 
	 private String comment;
	 private String runName;
	 private String forecastHorizon;
	 private String frequency;
	 private String inputFile;
	 private String configurationFile;
	 
	 private String creatorUserName;
	 private Long setupDuration;
	 private Long executionDuration;
	 private Long startTime;
	 private Long endTime;
	 
	 
}
