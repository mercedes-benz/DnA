package com.daimler.data.service.pat.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "code-server.pat.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CachingPatSecretValidator implements PatSecretValidator {
	private final VaultPatSecretValidator delegate;
	private final PatSecretStore store;
	private final PatCacheProperties properties;

	public CachingPatSecretValidator(VaultPatSecretValidator delegate, PatSecretStore store,
			PatCacheProperties properties) {
		this.delegate = delegate;
		this.store = store;
		this.properties = properties;
	}

	@Override
	public boolean validate(String userId, int version, String secret) {
		String hash = hash(secret);
		Optional<String> cached;
		try {
			cached = store.getSecretHash(userId, version);
		} catch (RuntimeException e) {
			log.warn("PAT cache read failed, userId={} version={} exceptionClass={} message={}",
					userId, version, e.getClass().getSimpleName(), e.getMessage());
			return delegate.validate(userId, version, secret);
		}
		if (cached.isPresent()) {
			log.debug("PAT cache hit, userId={} version={}", userId, version);
			return MessageDigest.isEqual(cached.get().getBytes(StandardCharsets.UTF_8),
					hash.getBytes(StandardCharsets.UTF_8));
		}
		log.debug("PAT cache miss, userId={} version={}", userId, version);
		if (!delegate.validate(userId, version, secret)) {
			return false;
		}
		try {
			store.put(userId, version, hash, Duration.ofSeconds(properties.getTtlSeconds()));
		} catch (RuntimeException e) {
			log.warn("PAT cache write failed, userId={} version={} exceptionClass={} message={}",
					userId, version, e.getClass().getSimpleName(), e.getMessage());
		}
		return true;
	}

	private String hash(String secret) {
		try {
			return Base64.getEncoder().encodeToString(
					MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}
}
