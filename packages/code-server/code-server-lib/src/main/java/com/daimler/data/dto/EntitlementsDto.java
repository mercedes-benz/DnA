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
public class EntitlementsDto implements Serializable{
    private static final long serialVersionUID = 1L;
    @JsonProperty("entitlements")
    private List<EntitlementDetailsDto> entitlementList;  
}