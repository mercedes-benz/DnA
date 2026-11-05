package com.daimler.data.application.config;

import java.time.Duration;
import java.util.List;

import com.daimler.data.service.pat.cache.PatCacheProperties;
import com.daimler.data.service.pat.ratelimit.PatRateLimitProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
@EnableConfigurationProperties({PatRedisProperties.class, PatCacheProperties.class, PatRateLimitProperties.class})
public class PatRedisConfig {
	@Bean
	public LettuceConnectionFactory patRedisConnectionFactory(PatRedisProperties properties) {
		RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
		server.setDatabase(properties.getDatabase());
		server.setUsername(properties.getUser());
		server.setPassword(properties.getPassword());
		Duration timeout = Duration.ofMillis(properties.getTimeout());
		LettuceClientConfiguration client = LettuceClientConfiguration.builder()
				.commandTimeout(timeout)
				.clientOptions(ClientOptions.builder()
						.socketOptions(SocketOptions.builder().connectTimeout(timeout).build()).build())
				.build();
		LettuceConnectionFactory factory = new LettuceConnectionFactory(server, client);
		factory.setEagerInitialization(false);
		return factory;
	}

	@Bean
	public StringRedisTemplate patRedisTemplate(LettuceConnectionFactory factory) {
		return new StringRedisTemplate(factory);
	}

	@Bean
	public DefaultRedisScript<List> patSlidingWindowScript() {
		// TIME followed by writes requires effects replication (default on Redis 5+).
		String script = "local time = redis.call('TIME') "
				+ "local now = time[1] * 1000 + math.floor(time[2] / 1000) "
				+ "local window = tonumber(ARGV[1]) local limit = tonumber(ARGV[2]) "
				+ "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - window) "
				+ "local count = redis.call('ZCARD', KEYS[1]) "
				+ "if count < limit then "
				+ "redis.call('ZADD', KEYS[1], now, now .. ':' .. ARGV[3]) "
				+ "redis.call('PEXPIRE', KEYS[1], window) "
				+ "return {1, limit - count - 1, 0} end "
				+ "local oldest = redis.call('ZRANGE', KEYS[1], 0, 0, 'WITHSCORES') "
				+ "return {0, 0, math.max(1, tonumber(oldest[2]) + window - now)}";
		return new DefaultRedisScript<>(script, List.class);
	}
}
