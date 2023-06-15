package com.daimler.data.dto.userinfo.dashboard;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetDashboardServiceLovResponseWrapperDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String status;
    private List<DashboardServiceLovDto> data;
    private List<MessageDescription> errors;
    private List<MessageDescription> warnings;
}

