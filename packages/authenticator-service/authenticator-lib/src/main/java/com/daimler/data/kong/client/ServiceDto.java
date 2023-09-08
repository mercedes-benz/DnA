package com.daimler.data.kong.client;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceDto implements Serializable{


	private static final long serialVersionUID = 1L;
	
	  private int port;	  
	  private String client_certificate;	  
	  private int write_timeout;	 
	  private String name;
	  private int connect_timeout;
	  private int read_timeout;
	  private String tls_verify;
	  private List<String> tags;
	  private String tls_verify_depth;
	  private String id;
	  private String host;
	  private String protocol;
	  private Boolean enabled;
	  private int retries;
	  private String path;
	  private String ca_certificates;

}
