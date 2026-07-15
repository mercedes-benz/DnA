package com.daimler.data.application.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	@Value("${git.cache.branches.ttlSeconds:60}")
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
