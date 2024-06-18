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
public class CodeServerRecipeDto implements Serializable{
    
    private String id;
	private String recipeName;
    private String recipeId;
    private String osName;
    private String maxCpu;
    private String maxRam;
    
}
