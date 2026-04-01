package com.daimler.data.dto.fabric;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class DdxResponseDto {

    private String status;
    private Integer statusCode;
    private String message;
    private String system;

    private Integer dataProductId;
    private String dataProductName;
    private String dofUrl;

}
