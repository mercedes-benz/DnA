package com.daimler.data.dto.fabric;

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
public class LakehouseCollectionDto implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<LakehouseDto> value;
	
}
