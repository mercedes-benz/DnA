package com.daimler.data.db.json;

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
public class CodespaceSecurityConfigDetailCollection implements Serializable{

	private CodespaceSecurityConfigDetails draft;
	private CodespaceSecurityConfigDetails published;
	
}
