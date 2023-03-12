package com.mb.dna.data.application.adapter.dna;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//import io.micronaut.serde.annotation;
import io.micronaut.context.annotation.Bean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRole implements Serializable{
	
	private String id;
	private String name;
}
