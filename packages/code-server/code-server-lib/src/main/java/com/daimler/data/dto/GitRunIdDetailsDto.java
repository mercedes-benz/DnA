package com.daimler.data.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class GitRunIdDetailsDto implements Serializable{

    private String gitjobRunId;
    private String environment;
    private String status;
    private String conclusion;
    private String projectName;
    private String owner;
    private Date lastBuildOrDeployedOn;
    private Boolean isWorkspaceMigratedToGHE;
    
}
