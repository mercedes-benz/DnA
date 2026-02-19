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
public class KeyVaultPropertiesDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String tenantId;
	private KeyVaultSkuDto sku;
	private List<KeyVaultAccessPolicyDto> accessPolicies;
	private Boolean enabledForDeployment;
	private Boolean enabledForDiskEncryption;
	private Boolean enabledForTemplateDeployment;
	private Boolean enablePurgeProtection;
	private String publicNetworkAccess;
	private Boolean enableRbacAuthorization;
}
