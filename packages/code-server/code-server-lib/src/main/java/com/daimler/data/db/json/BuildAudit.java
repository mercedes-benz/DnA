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
public class BuildAudit implements Serializable{

    private String triggeredBy;
    private Date triggeredOn;
    private Date buildOn;
    private String buildStatus;
    private String branch;
    private String version;
    private String comments;
    private String commitId;

}
