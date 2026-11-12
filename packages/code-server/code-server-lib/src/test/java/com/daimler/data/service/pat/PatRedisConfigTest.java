package com.daimler.data.service.pat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.daimler.data.application.config.CodeServerVaultConfig;
import com.daimler.data.application.config.PatRedisConfig;
import com.daimler.data.service.pat.cache.CachingPatSecretValidator;
import com.daimler.data.service.pat.cache.PatSecretValidator;
import com.daimler.data.service.pat.cache.RedisPatSecretStore;
import com.daimler.data.service.pat.cache.VaultPatSecretValidator;
import com.daimler.data.service.pat.ratelimit.NoOpRateLimiter;
import com.daimler.data.service.pat.ratelimit.RateLimiter;
import com.daimler.data.service.pat.ratelimit.RedisSlidingWindowRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class PatRedisConfigTest {
	private final ApplicationContextRunner context = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
			.withUserConfiguration(PatRedisConfig.class, RedisPatSecretStore.class,
					VaultPatSecretValidator.class, CachingPatSecretValidator.class,
					RedisSlidingWindowRateLimiter.class, NoOpRateLimiter.class)
			.withBean(CodeServerVaultConfig.class, () -> mock(CodeServerVaultConfig.class))
			.withPropertyValues("codeServer.redis.host=127.0.0.1", "codeServer.redis.port=1");

	@Test
	void startsWithUnreachableRedisAndSingleFactory() {
		context.run(application -> {
			assertThat(application).hasNotFailed();
			assertThat(application).hasSingleBean(RedisConnectionFactory.class);
			assertThat(application).hasSingleBean(StringRedisTemplate.class);
			assertThat(application.getBean(PatSecretValidator.class)).isInstanceOf(CachingPatSecretValidator.class);
			assertThat(application.getBean(RateLimiter.class)).isInstanceOf(RedisSlidingWindowRateLimiter.class);
		});
	}

	@Test
	void disabledFeaturesUseVaultAndNoOp() {
		context.withPropertyValues("codeServer.pat.cache.enabled=false", "codeServer.pat.ratelimit.enabled=false")
				.run(application -> {
					assertThat(application).hasNotFailed();
					assertThat(application.getBean(PatSecretValidator.class)).isInstanceOf(VaultPatSecretValidator.class);
					assertThat(application.getBean(RateLimiter.class)).isInstanceOf(NoOpRateLimiter.class);
				});
	}

	@Test
	void rejectsNonPositiveSettings() {
		context.withPropertyValues("codeServer.pat.cache.ttlSeconds=0",
				"codeServer.pat.ratelimit.maxRequests=0", "codeServer.pat.ratelimit.windowSeconds=0")
				.run(application -> assertThat(application).hasFailed());
	}
}
