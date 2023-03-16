package com.daimler.data.dto.databricks;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.forecast.RunDetailsVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataBricksJobRunOutputResponseWrapperDto implements Serializable{

    private static final long serialVersionUID = 1L;
    private String status;
    private RunDetailsVO metadata;
    private String error;
    private List<MessageDescription> errors;
    private List<MessageDescription> warnings;

}