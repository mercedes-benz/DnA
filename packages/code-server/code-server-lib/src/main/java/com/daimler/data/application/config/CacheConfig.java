package com.daimler.data.application.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * The Caffeine cache and GitClient ETag stores are in-memory per pod, so each replica bounds
 * staleness independently by the TTL. Server-validated ETag requests returning 304 do not consume
 * the shared PAT budget and provide the primary rate-limit relief; Redis or another shared cache
 * would be needed to deduplicate fresh fetches across the Kubernetes deployment.
 */
@Configuration
@EnableCaching
public class CacheConfig {

	@Value("${git.cache.branches.ttlSeconds:30}")
	private long ttlSeconds;

	@Value("${git.cache.branches.maxSize:500}")
	private long maxSize;

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("git-branches");
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
				.maximumSize(maxSize));
		return cacheManager;
	}
}
