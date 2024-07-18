package com.daimler.data.dto.fabric;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntiltlemetDetailsDto implements Serializable{

	private static final long serialVersionUID = 1L;

    private String uuid;
    private String entitlementId;
    private String type; 
    private String displayName;
    private String description;
    private String dataClassification;
    private Boolean dataClassificationInherited;
    private Boolean connectivity;
}
