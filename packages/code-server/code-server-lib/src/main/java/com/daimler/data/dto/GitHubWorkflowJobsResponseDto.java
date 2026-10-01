package com.daimler.data.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWorkflowJobsResponseDto implements Serializable {

    @JsonProperty("total_count")
    private int totalCount;

    private List<Job> jobs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Job implements Serializable {
        private Long id;
        private String name;
        private String status;
        private String conclusion;
    }
}
