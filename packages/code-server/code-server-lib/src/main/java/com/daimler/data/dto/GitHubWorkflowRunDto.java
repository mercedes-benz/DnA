package com.daimler.data.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWorkflowRunDto implements Serializable {

    private Long id;
    private String name;
    private String status;
    private String conclusion;

    @JsonProperty("run_number")
    private Long runNumber;
 
    @JsonProperty("head_branch")
    private String headBranch;
 
    @JsonProperty("head_sha")
    private String headSha;
 
    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("run_started_at")
    private Date runStartedAt;
}
