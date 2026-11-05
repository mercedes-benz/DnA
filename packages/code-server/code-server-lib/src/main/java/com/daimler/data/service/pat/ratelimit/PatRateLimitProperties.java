package com.daimler.data.service.pat.ratelimit;

import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "code-server.pat.ratelimit")
public class PatRateLimitProperties {
	private boolean enabled = true;
	@Min(1)
	private int maxRequests = 5;
	@Min(1)
	private long windowSeconds = 7;
}
