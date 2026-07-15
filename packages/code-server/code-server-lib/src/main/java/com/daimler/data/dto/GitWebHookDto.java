package com.daimler.data.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitWebHookDto implements Serializable{

    private String repoName;
    private String intRepoName;
    private String prodRepoName;
    private boolean webHookEnabled;

}
