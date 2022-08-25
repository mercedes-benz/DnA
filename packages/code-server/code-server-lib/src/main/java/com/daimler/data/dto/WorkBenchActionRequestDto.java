package com.daimler.data.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class WorkBenchActionRequestDto<T> implements Serializable{
	
	private static final long serialVersionUID = -5040393756376282259L;
	
	private String ref;
	private T inputs;

}
