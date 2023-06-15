package com.daimler.data.dto.comparison;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.json.ComparisonState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateComparisonResponseWrapperDto implements Serializable{

	private static final long serialVersionUID = 1L;
	private ComparisonState data;
	private List<MessageDescription> errors;
	private List<MessageDescription> warnings;
	
}
