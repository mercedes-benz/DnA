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
public class Capacity implements Serializable{

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String sku;
	private String region;
	private String state;

	private Date createdOn;
	private Date modifiedOn;
	
}
