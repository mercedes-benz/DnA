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
public class NetworkConnectionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String connectivityType;
	private String displayName;
	private ConnectionDetailsDto connectionDetails;
	private String privacyLevel;
	private NetworkCredentialDetailsDto credentialDetails;
}
