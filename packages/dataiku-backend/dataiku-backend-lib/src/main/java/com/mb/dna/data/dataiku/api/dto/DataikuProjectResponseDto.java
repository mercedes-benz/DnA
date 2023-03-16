package com.mb.dna.data.dataiku.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mb.dna.data.api.controller.exceptions.GenericMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectResponseDto implements Serializable{

	private DataikuProjectDto data;
	private GenericMessage response;
	
}
