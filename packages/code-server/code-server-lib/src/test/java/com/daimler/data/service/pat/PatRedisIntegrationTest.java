package com.daimler.data.service.pat;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.daimler.data.application.config.PatRedisConfig;
import com.daimler.data.application.config.PatRedisProperties;
import com.daimler.data.service.pat.cache.PatCacheProperties;
import com.daimler.data.service.pat.cache.RedisPatSecretStore;
import com.daimler.data.service.pat.ratelimit.PatRateLimitProperties;
import com.daimler.data.service.pat.ratelimit.RateLimitDecision;
import com.daimler.data.service.pat.ratelimit.RedisSlidingWindowRateLimiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@EnabledIfEnvironmentVariable(named = "PAT_REDIS_IT", matches = "true")
class PatRedisIntegrationTest {
	private static final String BURST = "cs:pat:rl:FAKE-IT-BURST:v1";
	private static final String SLIDING = "cs:pat:rl:FAKE-IT-SLIDING:v1";
	private static final String SECRET_KEY = "cs:pat:secret:FAKE-IT-CACHE:v1";
	private LettuceConnectionFactory factory;
	private StringRedisTemplate redis;
	private RedisSlidingWindowRateLimiter limiter;
	private RedisPatSecretStore store;

	@BeforeEach
	void setUp() {
		PatRedisProperties properties = new PatRedisProperties();
		properties.setHost("127.0.0.1");
		PatRedisConfig config = new PatRedisConfig();
		PatRateLimitProperties rateLimit = new PatRateLimitProperties();
		factory = config.patRedisConnectionFactory(properties, new PatCacheProperties(), rateLimit);
		factory.afterPropertiesSet();
		redis = config.patRedisTemplate(factory);
		redis.afterPropertiesSet();
		limiter = new RedisSlidingWindowRateLimiter(redis, config.patSlidingWindowScript(), rateLimit, properties);
		store = new RedisPatSecretStore(redis, properties);
		redis.delete(List.of(BURST, SLIDING, SECRET_KEY));
		assertEquals(1, factory.getDatabase());
	}

	@AfterEach
	void tearDown() {
		try {
			if (redis != null) {
				redis.delete(List.of(BURST, SLIDING, SECRET_KEY));
			}
		} finally {
			if (factory != null) {
				factory.destroy();
			}
		}
	}

	@Test
	void burstLimitAndWindowRecovery() throws InterruptedException {
		for (int remaining = 4; remaining >= 0; remaining--) {
			RateLimitDecision decision = limiter.tryAcquire("FAKE-IT-BURST:v1");
			assertTrue(decision.isAllowed());
			assertEquals(remaining, decision.getRemaining());
			System.out.println("LIVE burst allowed=true remaining=" + decision.getRemaining());
		}
		RateLimitDecision denied = limiter.tryAcquire("FAKE-IT-BURST:v1");
		assertFalse(denied.isAllowed());
		assertTrue(denied.getRetryAfterMillis() >= 1 && denied.getRetryAfterMillis() <= 7000);
		assertTrue(denied.getRetryAfterSeconds() >= 1 && denied.getRetryAfterSeconds() <= 7);
		System.out.println("LIVE sixth allowed=false retryAfterMillis=" + denied.getRetryAfterMillis());
		assertTtl(BURST, 7000);
		Thread.sleep(denied.getRetryAfterMillis() + 200);
		RateLimitDecision recovered = limiter.tryAcquire("FAKE-IT-BURST:v1");
		assertTrue(recovered.isAllowed());
		assertEquals(4, recovered.getRemaining());
		System.out.println("LIVE after window allowed=true remaining=4");
	}

	@Test
	void slidingWindowExpiresEntriesOneByOne() throws InterruptedException {
		long start = System.nanoTime();
		assertTrue(limiter.tryAcquire("FAKE-IT-SLIDING:v1").isAllowed());
		waitUntil(start, 2000);
		assertTrue(limiter.tryAcquire("FAKE-IT-SLIDING:v1").isAllowed());
		waitUntil(start, 4000);
		for (int i = 0; i < 3; i++) {
			assertTrue(limiter.tryAcquire("FAKE-IT-SLIDING:v1").isAllowed());
		}
		assertFalse(limiter.tryAcquire("FAKE-IT-SLIDING:v1").isAllowed());
		waitUntil(start, 7300);
		assertSingleSlotRecovered();
		System.out.println("LIVE sliding t=7.3s expired=1 allowed=true remaining=0 cardinality=5");
		waitUntil(start, 9300);
		assertSingleSlotRecovered();
		System.out.println("LIVE sliding t=9.3s expired=1 allowed=true remaining=0 cardinality=5");
	}

	@Test
	void cacheUsesDatabaseOneNamesTtlAndEviction() throws InterruptedException {
		assertTrue(store.getSecretHash("FAKE-IT-CACHE", 1).isEmpty());
		store.put("FAKE-IT-CACHE", 1, "FAKE-HASH-FOR-TESTING", Duration.ofSeconds(1));
		assertEquals("FAKE-HASH-FOR-TESTING", store.getSecretHash("FAKE-IT-CACHE", 1).orElseThrow());
		assertTtl(SECRET_KEY, 1000);
		Thread.sleep(1200);
		assertTrue(store.getSecretHash("FAKE-IT-CACHE", 1).isEmpty());
		store.put("FAKE-IT-CACHE", 1, "FAKE-HASH-FOR-TESTING", Duration.ofSeconds(30));
		store.evict("FAKE-IT-CACHE", 1);
		assertTrue(store.getSecretHash("FAKE-IT-CACHE", 1).isEmpty());
		System.out.println("LIVE cache database=1 put/get/TTL-expiry/evict verified");
	}

	private void assertSingleSlotRecovered() {
		RateLimitDecision decision = limiter.tryAcquire("FAKE-IT-SLIDING:v1");
		assertTrue(decision.isAllowed());
		assertEquals(0, decision.getRemaining());
		assertEquals(5L, redis.opsForZSet().zCard(SLIDING));
		assertFalse(limiter.tryAcquire("FAKE-IT-SLIDING:v1").isAllowed());
	}

	private void assertTtl(String key, long maximumMillis) {
		Long ttl = redis.getExpire(key, TimeUnit.MILLISECONDS);
		assertNotNull(ttl);
		assertTrue(ttl > 0 && ttl <= maximumMillis);
		System.out.println("LIVE key=" + key + " database=1 ttlMillis=" + ttl);
	}

	private void waitUntil(long start, long elapsedMillis) throws InterruptedException {
		long remaining = elapsedMillis - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		if (remaining > 0) {
			Thread.sleep(remaining);
		}
	}
}
