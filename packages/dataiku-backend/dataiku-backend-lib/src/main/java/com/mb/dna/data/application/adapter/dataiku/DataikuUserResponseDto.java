package com.mb.dna.data.application.adapter.dataiku;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuUserResponseDto extends DataikuUserDto{

		private String errorType;
		private String message;
		private String detailedMessage;
		
}
