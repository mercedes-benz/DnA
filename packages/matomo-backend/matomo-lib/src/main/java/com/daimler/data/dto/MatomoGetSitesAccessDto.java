package com.daimler.data.dto;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatomoGetSitesAccessDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String site;
    private String access;

}
