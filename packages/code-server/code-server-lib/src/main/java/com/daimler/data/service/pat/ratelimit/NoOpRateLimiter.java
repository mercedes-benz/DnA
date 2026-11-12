package com.daimler.data.service.pat.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "code-server.pat.ratelimit", name = "enabled", havingValue = "false")
public class NoOpRateLimiter implements RateLimiter {
	private final PatRateLimitProperties properties;

	public NoOpRateLimiter(PatRateLimitProperties properties) {
		this.properties = properties;
	}

	@Override
	public RateLimitDecision tryAcquire(String key) {
		return RateLimitDecision.allowed(properties.getMaxRequests(), properties.getMaxRequests());
	}
}
