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
public class KeyVaultNameAvailabilityRequestDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String type;
	
	public KeyVaultNameAvailabilityRequestDto(String name) {
		this.name = name;
		this.type = "Microsoft.KeyVault/vaults";
	}
}
