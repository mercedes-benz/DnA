package com.daimler.data.dto.azure;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureTokenResponseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("expires_in")
	private Long expiresIn;

	@JsonProperty("ext_expires_in")
	private Long extExpiresIn;

	@JsonProperty("access_token")
	private String accessToken;

	/**
	 * Indicates whether the operation was successful
	 */
	private Boolean success = true;

	/**
	 * Error message if operation failed
	 */
	private String errorMessage;
}
