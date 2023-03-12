package com.mb.dna.data.application.adapter.dataiku;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(DataikuClientConfig.PREFIX)
@Requires(property = DataikuClientConfig.PREFIX)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataikuClientConfig {
	
	public static final String PREFIX = "dataiku";
	
	private String auth;
	private String baseuri;
	private String scenariosUri;
	private String usersUri;
	private String groupNamePrefix;
	private String environmentProfile;

}
