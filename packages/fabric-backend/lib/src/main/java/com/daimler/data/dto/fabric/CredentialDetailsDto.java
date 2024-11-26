package com.daimler.data.dto.fabric;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialDetailsDto implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String credentialType;
	private Boolean useEndUserOAuth2Credentials;
	private String encryptedConnection;
	private String encryptionAlgorithm;
	private String privacyLevel;
	private Boolean skipTestConnection;
	private String credentials;
}