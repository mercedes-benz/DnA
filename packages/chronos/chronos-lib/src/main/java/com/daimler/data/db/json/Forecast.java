package com.daimler.data.db.json;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Forecast {

	private String name;
	private String bucketName;
	private String bucketId;
	private UserDetails createdBy;
	//private String resultFolderPath;
	private Date createdOn;
	private List<UserDetails> collaborators;
	private List<File> savedInputs;
	private List<RunDetails> runs;
	private List<ComparisonDetails> comparisons;
	private List<ConfigFileDetails> configFiles;
	
}
