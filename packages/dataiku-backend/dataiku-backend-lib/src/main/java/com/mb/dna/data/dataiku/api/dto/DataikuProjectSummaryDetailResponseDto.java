package com.mb.dna.data.dataiku.api.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mb.dna.data.api.controller.exceptions.GenericMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectSummaryDetailResponseDto implements Serializable{

	private DataikuProjectSummaryDetailsDto data;
	private GenericMessage response;
	
}
