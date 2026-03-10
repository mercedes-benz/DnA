package com.daimler.data.dto.fabric;

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
public class LegalEntityDto implements Serializable{

    private static final long serialVersionUID = 1L;

    @JsonProperty("COMPANY_CODE")
    private String companyCode;

    @JsonProperty("LEGAL_NAME")
    private String legalName;

}
