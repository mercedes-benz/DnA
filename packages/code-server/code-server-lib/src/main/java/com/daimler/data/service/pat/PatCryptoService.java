package com.daimler.data.service.pat;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.data.dto.pat.PatPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PatCryptoService {
	private static final String TOKEN_PREFIX = "cs_pat_v1_";
	private static final byte[] AAD = "cs_pat_v1".getBytes(StandardCharsets.UTF_8);
	private static final int NONCE_LENGTH = 12;
	private static final int TAG_LENGTH_BITS = 128;
	private static final Pattern TOKEN_PATTERN = Pattern.compile("^cs_pat_v1_([A-Za-z0-9_-]+)$");
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final String encodedEncryptionKey;
	private final ObjectMapper objectMapper;

	public PatCryptoService(@Value("${codeServer.pat.encryptionkey:XXXX}") String encodedEncryptionKey,
			ObjectMapper objectMapper) {
		this.encodedEncryptionKey = encodedEncryptionKey;
		this.objectMapper = objectMapper;
	}

	public String generateSecret() {
		byte[] secret = new byte[32];
		SECURE_RANDOM.nextBytes(secret);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
	}

	public String encrypt(PatPayload payload) {
		if (payload == null || payload.getTokenVersion() < 1) {
			throw new IllegalArgumentException("A positive PAT version is required");
		}
		SecretKeySpec key = encryptionKey();
		try {
			byte[] nonce = new byte[NONCE_LENGTH];
			SECURE_RANDOM.nextBytes(nonce);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			cipher.updateAAD(AAD);
			byte[] encryptedPayload = cipher.doFinal(objectMapper.writeValueAsBytes(payload));
			byte[] encryptedToken = new byte[nonce.length + encryptedPayload.length];
			System.arraycopy(nonce, 0, encryptedToken, 0, nonce.length);
			System.arraycopy(encryptedPayload, 0, encryptedToken, nonce.length, encryptedPayload.length);
			return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedToken);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to encrypt PAT");
		}
	}

	public Optional<PatPayload> decrypt(String token) {
		if (token == null) {
			return Optional.empty();
		}
		Matcher matcher = TOKEN_PATTERN.matcher(token);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		try {
			byte[] encryptedToken = Base64.getUrlDecoder().decode(matcher.group(1));
			if (encryptedToken.length <= NONCE_LENGTH + TAG_LENGTH_BITS / 8) {
				return Optional.empty();
			}
			byte[] nonce = new byte[NONCE_LENGTH];
			byte[] ciphertext = new byte[encryptedToken.length - NONCE_LENGTH];
			System.arraycopy(encryptedToken, 0, nonce, 0, NONCE_LENGTH);
			System.arraycopy(encryptedToken, NONCE_LENGTH, ciphertext, 0, ciphertext.length);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			cipher.updateAAD(AAD);
			PatPayload payload = objectMapper.readValue(cipher.doFinal(ciphertext), PatPayload.class);
			if (payload.getTokenVersion() < 1 || payload.getUserId() == null || payload.getUserId().isBlank()
					|| payload.getSecret() == null || payload.getSecret().isBlank()) {
				return Optional.empty();
			}
			return Optional.of(payload);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private SecretKeySpec encryptionKey() {
		try {
			byte[] key = Base64.getDecoder().decode(encodedEncryptionKey);
			if (key.length != 32) {
				throw new IllegalArgumentException("Invalid key length");
			}
			return new SecretKeySpec(key, "AES");
		} catch (RuntimeException e) {
			throw new IllegalStateException("PAT encryption not configured");
		}
	}
}
