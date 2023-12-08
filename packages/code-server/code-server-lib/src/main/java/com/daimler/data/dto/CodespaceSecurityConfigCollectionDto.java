package com.daimler.data.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class CodespaceSecurityConfigCollectionDto implements Serializable{

    private List<CodespaceSecurityConfigDto> securityConfigs;
}
