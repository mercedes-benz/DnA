package com.daimler.data.service.pat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.List;

import com.daimler.data.application.config.PatRedisProperties;
import com.daimler.data.service.pat.ratelimit.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

class RedisSlidingWindowRateLimiterTest {
	private StringRedisTemplate redis;
	private DefaultRedisScript<List> script;
	private RedisSlidingWindowRateLimiter limiter;

	@BeforeEach
	void setUp() {
		redis = mock(StringRedisTemplate.class);
		script = new DefaultRedisScript<>("return {1, 4, 0}", List.class);
		limiter = new RedisSlidingWindowRateLimiter(redis, script, new PatRateLimitProperties(),
				new PatRedisProperties());
	}

	@Test
	void mapsAllowedResult() {
		when(redis.execute(eq(script), eq(List.of("cs:pat:rl:FAKE-KEY")), eq("7000"), eq("5"), anyString()))
				.thenReturn(List.of(1L, 4L, 0L));
		RateLimitDecision result = limiter.tryAcquire("FAKE-KEY");
		assertTrue(result.isAllowed());
		assertEquals(5, result.getLimit());
		assertEquals(4, result.getRemaining());
	}

	@Test
	void mapsDeniedResultAndRoundsRetryUp() {
		when(redis.execute(eq(script), eq(List.of("cs:pat:rl:FAKE-KEY")), eq("7000"), eq("5"), anyString()))
				.thenReturn(List.of(0L, 0L, 1001L));
		RateLimitDecision result = limiter.tryAcquire("FAKE-KEY");
		assertFalse(result.isAllowed());
		assertEquals(0, result.getRemaining());
		assertEquals(1001, result.getRetryAfterMillis());
		assertEquals(2, result.getRetryAfterSeconds());
	}

	@Test
	void redisFailureAllowsRequest() {
		when(redis.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
				.thenThrow(new IllegalStateException("FAKE Redis failure"));
		assertTrue(limiter.tryAcquire("FAKE-KEY").isAllowed());
	}

	@Test
	void emptyResultAllowsRequest() {
		when(redis.execute(eq(script), anyList(), anyString(), anyString(), anyString())).thenReturn(List.of());
		assertTrue(limiter.tryAcquire("FAKE-KEY").isAllowed());
	}
}
