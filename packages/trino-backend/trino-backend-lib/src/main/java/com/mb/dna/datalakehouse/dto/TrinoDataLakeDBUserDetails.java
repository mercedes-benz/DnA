package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoDataLakeDBUserDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private String accesskey;
	private String secretKey;
	private String hostName;
	private String port;
	@JsonInclude(Include.NON_NULL)
    private Boolean externalAuthentication;
    
}
