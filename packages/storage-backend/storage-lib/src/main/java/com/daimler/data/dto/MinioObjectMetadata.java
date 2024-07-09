package com.daimler.data.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinioObjectMetadata implements Serializable {

	private static final long serialVersionUID = 1L;
	private String status;
	private String type;
	private String lastModified;
	private long size;
	private String key;
	private String etag;
	private String url;
	private String versionOrdinal;
	private String storageClass;
	
}
