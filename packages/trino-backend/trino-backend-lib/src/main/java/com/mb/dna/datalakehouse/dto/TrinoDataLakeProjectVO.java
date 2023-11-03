package com.mb.dna.datalakehouse.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.daimler.data.dto.UserInfoVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinoDataLakeProjectVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String projectName;
	private String connectorType;
	private String schemaName;
	private String bucketName;
	private String bucketId;
	private List<DatalakeTableVO> tables;
	
	private String classificationType;
	private Boolean hasPii;
	private String divisionId;
	private String divisionName;
	private String subdivisionId;
	private String subdivisionName;
	private String department;
	
	//private String status;
	
	private Date createdOn;
	private UserInfoVO createdBy;
	
	
}
