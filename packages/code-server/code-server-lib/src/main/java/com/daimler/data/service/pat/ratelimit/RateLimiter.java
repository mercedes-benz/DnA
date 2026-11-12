package com.daimler.data.service.pat.ratelimit;

public interface RateLimiter {
	RateLimitDecision tryAcquire(String key);
}
