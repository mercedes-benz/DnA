package com.daimler.data.service.pat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import com.daimler.data.service.pat.cache.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CachingPatSecretValidatorTest {
	private static final String SECRET = "FAKE-SECRET-FOR-TESTING";
	private PatSecretStore store;
	private VaultPatSecretValidator vault;
	private CachingPatSecretValidator validator;

	@BeforeEach
	void setUp() {
		store = mock(PatSecretStore.class);
		vault = mock(VaultPatSecretValidator.class);
		validator = new CachingPatSecretValidator(vault, store, new PatCacheProperties());
		when(store.getSecretHash("user", 1)).thenReturn(Optional.empty());
	}

	@Test
	void validHitDoesNotReadVault() throws Exception {
		String hash = Base64.getEncoder().encodeToString(
				MessageDigest.getInstance("SHA-256").digest(SECRET.getBytes(StandardCharsets.UTF_8)));
		when(store.getSecretHash("user", 1)).thenReturn(Optional.of(hash));
		assertTrue(validator.validate("user", 1, SECRET));
		verifyNoInteractions(vault);
	}

	@Test
	void mismatchedHitDoesNotReadVault() {
		when(store.getSecretHash("user", 1)).thenReturn(Optional.of("FAKE-HASH"));
		assertFalse(validator.validate("user", 1, SECRET));
		verifyNoInteractions(vault);
	}

	@Test
	void successfulMissCachesHashWithTtl() {
		when(vault.validate("user", 1, SECRET)).thenReturn(true);
		assertTrue(validator.validate("user", 1, SECRET));
		verify(store).put(eq("user"), eq(1), argThat(hash -> !hash.equals(SECRET)), eq(Duration.ofSeconds(300)));
	}

	@Test
	void failedMissIsNotCached() {
		assertFalse(validator.validate("user", 1, SECRET));
		verify(store, never()).put(anyString(), anyInt(), anyString(), any());
	}

	@Test
	void redisReadFailureUsesVault() {
		when(store.getSecretHash("user", 1)).thenThrow(new IllegalStateException("FAKE Redis failure"));
		when(vault.validate("user", 1, SECRET)).thenReturn(true);
		assertTrue(validator.validate("user", 1, SECRET));
		verify(vault).validate("user", 1, SECRET);
	}

	@Test
	void redisReadFailureDoesNotAuthorizeInvalidSecret() {
		when(store.getSecretHash("user", 1)).thenThrow(new IllegalStateException("FAKE Redis failure"));
		assertFalse(validator.validate("user", 1, SECRET));
	}

	@Test
	void redisWriteFailureStillReturnsTrue() {
		when(vault.validate("user", 1, SECRET)).thenReturn(true);
		doThrow(new IllegalStateException("FAKE Redis failure"))
				.when(store).put(anyString(), anyInt(), anyString(), any());
		assertTrue(validator.validate("user", 1, SECRET));
	}
}
