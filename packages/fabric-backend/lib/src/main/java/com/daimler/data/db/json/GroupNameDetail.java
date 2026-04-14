package com.daimler.data.db.json;

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
public class GroupNameDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String testRunId;
    private List<GroupNameList> groupNameList;
    private String runStatus;



    //private GroupMessage message;  //GroupMessage contain 1-status, 2-groupName and 3-message
}
