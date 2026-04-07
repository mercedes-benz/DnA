package com.daimler.data.dto.azureKeyVault;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyVaultResponseDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private String type;
	private String location;
	private KeyVaultPropertiesDto properties;
	private String errorCode;
	private String message;
}
