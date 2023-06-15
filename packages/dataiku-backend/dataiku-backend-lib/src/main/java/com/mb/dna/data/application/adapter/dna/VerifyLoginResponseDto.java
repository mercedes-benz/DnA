package com.mb.dna.data.application.adapter.dna;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
public class VerifyLoginResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private UserInfo data;
	private String loggedIn;
	private String token;
	
}
