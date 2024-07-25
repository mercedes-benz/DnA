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
public class AdditionalPropertiesDto implements Serializable { 
    private List<EnvironmentVariable> env;
    private List<Port> ports;
    private List<String> args;
    private List<VolumeMount> volumeMounts;
    private String imagePullPolicy;
    private String image;
    private SecurityContext securityContext;
}
