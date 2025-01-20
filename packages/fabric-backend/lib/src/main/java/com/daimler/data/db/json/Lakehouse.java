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
public class Lakehouse implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String  name;
	private String description;
	private String sensitivityLabel;  
	private List<Shortcut> shortcuts;
	
}
