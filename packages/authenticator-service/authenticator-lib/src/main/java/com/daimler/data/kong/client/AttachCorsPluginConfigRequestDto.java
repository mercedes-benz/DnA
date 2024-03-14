package com.daimler.data.kong.client;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachCorsPluginConfigRequestDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private List<String> origins;
    private long max_age;
	private Boolean credentials;
}
