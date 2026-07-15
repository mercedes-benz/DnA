package com.daimler.data.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestPayloadDto implements Serializable{

    @JsonProperty("action")
    private String action;      

    @JsonProperty("number")
    private int number;

    @JsonProperty("pull_request")
    private PullRequest pullRequest;

    @JsonProperty("repository")
    private Repository repository;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequest {
        @JsonProperty("title") public String title;
        @JsonProperty("html_url") public String htmlUrl;
        @JsonProperty("state") public String state;
        @JsonProperty("merged") public boolean merged;
        @JsonProperty("user") public User user;
        @JsonProperty("head") public Branch head;
        @JsonProperty("base") public Branch base;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("login") public String login;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Branch {
        @JsonProperty("ref") public String ref;
        @JsonProperty("sha") public String sha;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name") public String fullName;
    }
}
