package com.daimler.data.dto.azureKeyVault;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyVaultPermissionsDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<String> keys;
	private List<String> secrets;
	private List<String> certificates;
}
