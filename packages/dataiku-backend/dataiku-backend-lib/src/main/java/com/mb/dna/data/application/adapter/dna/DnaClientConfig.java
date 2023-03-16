package com.mb.dna.data.application.adapter.dna;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(DnaClientConfig.PREFIX)
@Requires(property = DnaClientConfig.PREFIX)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DnaClientConfig {
	
	public static final String PREFIX = "dna";

	private String uri;
	private String dnaAuthEnable;
	private String verifyLoginUri;
	private String getUsersUri;
	private String jwt;

}
