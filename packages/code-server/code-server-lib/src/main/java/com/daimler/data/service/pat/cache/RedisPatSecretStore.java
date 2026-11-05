package com.daimler.data.service.pat.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisPatSecretStore implements PatSecretStore {
	private final StringRedisTemplate redis;
	private final PatCacheProperties properties;

	public RedisPatSecretStore(StringRedisTemplate redis, PatCacheProperties properties) {
		this.redis = redis;
		this.properties = properties;
	}

	@Override
	public Optional<String> getSecretHash(String userId, int version) {
		return Optional.ofNullable(redis.opsForValue().get(key(userId, version)));
	}

	@Override
	public void put(String userId, int version, String secretHash, Duration ttl) {
		redis.opsForValue().set(key(userId, version), secretHash, ttl);
	}

	@Override
	public void evict(String userId, int version) {
		redis.delete(key(userId, version));
	}

	private String key(String userId, int version) {
		return properties.getKeyPrefix() + "secret:" + userId + ":v" + version;
	}
}
