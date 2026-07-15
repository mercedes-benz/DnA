package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushPayloadDto implements Serializable{

    @JsonProperty("ref")
    private String ref;          

    @JsonProperty("before")
    private String before;

    @JsonProperty("after")
    private String after;

    @JsonProperty("pusher")
    private Pusher pusher;

    @JsonProperty("repository")
    private Repository repository;

    @JsonProperty("commits")
    private List<Commit> commits;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pusher {
        @JsonProperty("name") public String name;
        @JsonProperty("email") public String email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name") public String fullName;
        @JsonProperty("html_url") public String htmlUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        @JsonProperty("id") public String id;
        @JsonProperty("message") public String message;
        @JsonProperty("author") public Author author;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        @JsonProperty("name") public String name;
        @JsonProperty("email") public String email;
    }

}
