package com.daimler.data.dto;

import java.io.Serializable;

import javax.sound.sampled.Port;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class VolumeMount implements Serializable { 
    private String name;
    private String mountPath;
}
