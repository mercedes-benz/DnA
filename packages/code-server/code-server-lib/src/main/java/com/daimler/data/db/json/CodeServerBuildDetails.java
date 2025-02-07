package com.daimler.data.db.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) 
public class CodeServerBuildDetails implements Serializable {

	private Date lastBuildOn;
	private UserInfo lastBuildBy;
	private String lastBuildBranch;
	private String lastBuildStatus;
    private String artifactId;
	private String version;
	
}
