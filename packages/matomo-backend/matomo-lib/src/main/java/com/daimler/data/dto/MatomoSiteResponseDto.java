package com.daimler.data.dto;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatomoSiteResponseDto {
    private static final long serialVersionUID = 1L;
    private String value;
    private String result;
    private String  message;
    private String status;
    private List<MessageDescription> errors;
    private List<MessageDescription> warnings;
}
