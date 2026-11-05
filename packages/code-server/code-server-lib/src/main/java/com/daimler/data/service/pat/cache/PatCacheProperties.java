package com.daimler.data.service.pat.cache;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "code-server.pat.cache")
public class PatCacheProperties {
	private boolean enabled = true;
	@Min(1)
	private long ttlSeconds = 300;
	@NotBlank
	private String keyPrefix = "cs:pat:";
}
