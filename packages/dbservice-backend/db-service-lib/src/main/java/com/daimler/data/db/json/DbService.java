package com.daimler.data.db.json;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbService {

    private CodeServerLeanGovernanceFeilds dataGovernance;
    private String serviceName;
    private String status;
    private String dbName;
    private String dbType;
    private List<UserInfo> projectCollaborators;
    private String description;
    private String projectType;
    private UserInfo projectOwner;
    private Date createdOn;
    private Date modifiedOn;
    private UserInfo modifiedBy;
    private String argocdAppId;


}
