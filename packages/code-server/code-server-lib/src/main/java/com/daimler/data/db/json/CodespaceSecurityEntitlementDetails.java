package com.daimler.data.db.json;

import java.io.Serializable;

import com.daimler.data.dto.workspace.CodespaceSecurityentitlementDetailsVO.HttpMethodEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodespaceSecurityEntitlementDetails implements Serializable {

    private static final long serialVersionUID = 1L;

	private String apiPattern;
	private String httpMethod;
}
