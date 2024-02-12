package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;

import com.daimler.data.dto.UserInfoVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataLakeTableCollabDetailsVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserInfoVO collaborator;
	private Boolean hasWritePermission;
}
