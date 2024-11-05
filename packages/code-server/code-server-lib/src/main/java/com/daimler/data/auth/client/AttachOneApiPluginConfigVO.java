package com.daimler.data.auth.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachOneApiPluginConfigVO implements Serializable{
    private static final long serialVersionUID = 1L;
	private String api_version_shortname;
    private String environment;

    
}
