package com.daimler.data.db.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shortcut implements Serializable{

	private static final long serialVersionUID = 1L;

	private String name;
	private String path;
	private String bucketName;
	private String bucketPath;
	private String connectionName;
	private String connectionId;
	
}
