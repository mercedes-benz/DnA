package com.mb.dna.data.db.entities;

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
public class DataikuProjectDetails implements Serializable {

	private String projectName;
	private String description;
	private String cloudProfile;
	private List<CollaboratorDetails> collaborators;
}
