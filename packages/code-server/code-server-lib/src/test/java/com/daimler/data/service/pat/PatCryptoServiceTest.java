package com.daimler.data.service.pat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.daimler.data.dto.pat.PatPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

class PatCryptoServiceTest {
	private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

	@Test
	void encryptsAndDecryptsPayload() {
		PatCryptoService crypto = new PatCryptoService(KEY, new ObjectMapper());
		PatPayload payload = payload();

		Optional<PatPayload> decrypted = crypto.decrypt(crypto.encrypt(payload));

		assertTrue(decrypted.isPresent());
		assertEquals(payload, decrypted.get());
	}

	@Test
	void usesDifferentNonceForEachToken() {
		PatCryptoService crypto = new PatCryptoService(KEY, new ObjectMapper());
		PatPayload payload = payload();

		assertNotEquals(crypto.encrypt(payload), crypto.encrypt(payload));
	}

	@Test
	void rejectsTamperedCiphertextAndWrongPrefix() {
		PatCryptoService crypto = new PatCryptoService(KEY, new ObjectMapper());
		String token = crypto.encrypt(payload());
		byte[] encrypted = Base64.getUrlDecoder().decode(token.substring("cs_pat_v1_".length()));
		encrypted[encrypted.length - 1] ^= 1;
		String tampered = "cs_pat_v1_" + Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);

		assertTrue(crypto.decrypt(tampered).isEmpty());
		assertTrue(crypto.decrypt("cs_pat_v2_" + token.substring("cs_pat_v1_".length())).isEmpty());
	}

	@Test
	void rejectsTokenEncryptedWithAnotherKey() {
		PatCryptoService creator = new PatCryptoService(KEY, new ObjectMapper());
		PatCryptoService other = new PatCryptoService(
				Base64.getEncoder().encodeToString(new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
				new ObjectMapper());

		assertTrue(other.decrypt(creator.encrypt(payload())).isEmpty());
	}

	@Test
	void rejectsPlaceholderKeyWithoutThrowingDuringDecrypt() {
		PatCryptoService crypto = new PatCryptoService("XXXX", new ObjectMapper());
		PatCryptoService validCrypto = new PatCryptoService(KEY, new ObjectMapper());
		String validToken = validCrypto.encrypt(payload());

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> crypto.encrypt(payload()));

		assertEquals("PAT encryption not configured", exception.getMessage());
		assertTrue(crypto.decrypt(validToken).isEmpty());
	}

	private PatPayload payload() {
		PatPayload payload = new PatPayload();
		payload.setUserId("creator");
		payload.setTokenVersion(4);
		payload.setSecret("a-random-secret");
		payload.setCodeSpaceProjectNames(List.of("project"));
		payload.setPermissions(List.of("workspace:read"));
		payload.setComment("test");
		payload.setCreatedAt("2024-01-01T00:00:00Z");
		payload.setExpiresAt(null);
		return payload;
	}
}
