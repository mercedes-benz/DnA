package com.daimler.data.dto.databricks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabricksSqlStatementResponseDto {

    @JsonProperty("statement_id")
    private String statementId;

    private Status status;

    private Manifest manifest;

    private Result result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String state;
        private Error error;
        @JsonProperty("sql_state")
        private String sqlState;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
        @JsonProperty("error_code")
        private String errorCode;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Manifest {
        private String format;

        @JsonProperty("total_row_count")
        private Integer totalRowCount;

        private Boolean truncated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("row_count")
        private Integer rowCount;

        @JsonProperty("data_array")
        private List<List<String>> dataArray;
    }
}
