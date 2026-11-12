package com.daimler.data.service.pat.ratelimit;

import lombok.Value;

@Value
public class RateLimitDecision {
	boolean allowed;
	int limit;
	int remaining;
	long retryAfterMillis;

	public static RateLimitDecision allowed(int limit, int remaining) {
		return new RateLimitDecision(true, limit, remaining, 0);
	}

	public static RateLimitDecision denied(int limit, long retryAfterMillis) {
		return new RateLimitDecision(false, limit, 0, Math.max(1, retryAfterMillis));
	}

	public long getRetryAfterSeconds() {
		return Math.max(1, (retryAfterMillis + 999) / 1000);
	}
}
