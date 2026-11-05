package com.daimler.data.service.pat.cache;

import java.time.Duration;
import java.util.Optional;

import com.daimler.data.application.config.PatRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RedisPatSecretStore implements PatSecretStore {
	private final StringRedisTemplate redis;
	private final PatRedisProperties properties;

	public RedisPatSecretStore(StringRedisTemplate redis, PatRedisProperties properties) {
		this.redis = redis;
		this.properties = properties;
	}

	@Override
	public Optional<String> getSecretHash(String userId, int version) {
		log.debug("Reading PAT cache entry, userId={} version={}", userId, version);
		return Optional.ofNullable(redis.opsForValue().get(key(userId, version)));
	}

	@Override
	public void put(String userId, int version, String secretHash, Duration ttl) {
		redis.opsForValue().set(key(userId, version), secretHash, ttl);
		log.debug("Stored PAT cache entry, userId={} version={} ttlMillis={}", userId, version, ttl.toMillis());
	}

	@Override
	public void evict(String userId, int version) {
		redis.delete(key(userId, version));
		log.debug("Evicted PAT cache entry, userId={} version={}", userId, version);
	}

	private String key(String userId, int version) {
		return properties.getKeyPrefix() + "secret:" + userId + ":v" + version;
	}
}
