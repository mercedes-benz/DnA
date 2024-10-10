package com.daimler.data.kong.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class AttachFunctionPluginConfigRequestDto implements Serializable{
    private static final long serialVersionUID = 1L;
    private List<String> access;
}
