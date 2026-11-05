package com.daimler.data.service.pat.ratelimit;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "code-server.pat.ratelimit", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class RedisSlidingWindowRateLimiter implements RateLimiter {
	private final StringRedisTemplate redis;
	private final DefaultRedisScript<List> script;
	private final PatRateLimitProperties properties;

	public RedisSlidingWindowRateLimiter(StringRedisTemplate redis, DefaultRedisScript<List> script,
			PatRateLimitProperties properties) {
		this.redis = redis;
		this.script = script;
		this.properties = properties;
	}

	@Override
	public RateLimitDecision tryAcquire(String key) {
		int limit = properties.getMaxRequests();
		try {
			List<?> result = redis.execute(script, Collections.singletonList(key),
					String.valueOf(properties.getWindowSeconds() * 1000), String.valueOf(limit),
					UUID.randomUUID().toString());
			if (result == null || result.size() != 3) {
				throw new IllegalStateException("Invalid rate limit result");
			}
			return ((Number) result.get(0)).longValue() == 1
					? RateLimitDecision.allowed(limit, ((Number) result.get(1)).intValue())
					: RateLimitDecision.denied(limit, ((Number) result.get(2)).longValue());
		} catch (RuntimeException e) {
			log.error("PAT Redis rate limiter unavailable; allowing request");
			return RateLimitDecision.allowed(limit, limit);
		}
	}
}
