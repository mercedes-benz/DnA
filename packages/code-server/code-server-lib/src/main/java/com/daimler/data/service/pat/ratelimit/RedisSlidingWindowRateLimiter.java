package com.daimler.data.service.pat.ratelimit;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.daimler.data.application.config.PatRedisProperties;
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
	private final PatRedisProperties redisProperties;

	public RedisSlidingWindowRateLimiter(StringRedisTemplate redis, DefaultRedisScript<List> script,
			PatRateLimitProperties properties, PatRedisProperties redisProperties) {
		this.redis = redis;
		this.script = script;
		this.properties = properties;
		this.redisProperties = redisProperties;
	}

	@Override
	public RateLimitDecision tryAcquire(String key) {
		int limit = properties.getMaxRequests();
		try {
			List<?> result = redis.execute(script,
					Collections.singletonList(redisProperties.getKeyPrefix() + "rl:" + key),
					String.valueOf(properties.getWindowSeconds() * 1000), String.valueOf(limit),
					UUID.randomUUID().toString());
			if (result == null || result.size() != 3) {
				throw new IllegalStateException("Invalid rate limit result");
			}
			RateLimitDecision decision = ((Number) result.get(0)).longValue() == 1
					? RateLimitDecision.allowed(limit, ((Number) result.get(1)).intValue())
					: RateLimitDecision.denied(limit, ((Number) result.get(2)).longValue());
			if (decision.isAllowed()) {
				log.debug("PAT rate limiter allowed request, tokenIdentity={} remaining={}",
						key, decision.getRemaining());
			}
			return decision;
		} catch (RuntimeException e) {
			log.error("PAT Redis rate limiter unavailable; allowing request, exceptionClass={} message={}",
					e.getClass().getSimpleName(), e.getMessage());
			return RateLimitDecision.allowed(limit, limit);
		}
	}
}
