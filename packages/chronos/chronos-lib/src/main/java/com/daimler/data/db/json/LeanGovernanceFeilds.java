package com.daimler.data.db.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeanGovernanceFeilds {

    private String typeOfProject;
    private String decription;
    private String division;
    private String subDivision;
    private String department;
    private List<String> tags;
    private String dataClassification;
    private Boolean piiData;
    private String archerId;
    private String producerId;
    private String termsOfUse;

}
