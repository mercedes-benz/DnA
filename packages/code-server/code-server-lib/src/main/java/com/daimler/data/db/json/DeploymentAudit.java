package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) 
public class DeploymentAudit implements Serializable{

    private String triggeredBy;
    private Date triggeredOn;
    private String deploymentStatus;
    private Date deployedOn;
    private String branch;
    private String commitId;
    private String approvedBy;
    private String version;

}