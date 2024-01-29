package com.daimler.data.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class McListBucketCollectionDto implements Serializable{

	private HttpStatus httpStatus;
	private String status;	
	private List<ErrorDTO> errors;
	private List<String> policies;
	private List<McListBucketDto> data;
}
