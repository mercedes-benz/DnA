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
public class AzurePrincipalDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String displayName;
	private String mail;
	private String appId;
	private String servicePrincipalType;
	private String principalType;
	private String kind;
	private String identifier;
}
