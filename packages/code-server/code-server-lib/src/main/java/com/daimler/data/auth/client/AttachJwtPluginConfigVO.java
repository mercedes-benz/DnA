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
public class AttachJwtPluginConfigVO implements Serializable{
	

	private static final long serialVersionUID = 1L;
	private String algorithm;
	private String secret;
	private String authurl;
	private String clientHomeUrl;
	private String privateKeyFilePath;
	private String expiresIn;
	private String clientId;
	private String clientSecret;
	private String introspectionUri;

}
