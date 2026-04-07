package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureKeyVault implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String keyVaultName;
	private String description;
	private String dataClassification;
	private String divisionId;
	private String division;
	private String subDivisionId;
	private String subDivision;
	private String department;
	private Boolean hasPii;
	private String location;
	
	private UserDetails createdBy;
	private Date createdOn;
}
