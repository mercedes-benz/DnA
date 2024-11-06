package com.daimler.data.application.client;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RSAEncryptionUtil {

	private static byte KeyLength32 = 0;
	private static byte KeyLength64 = 1;

	private static int KEY_LENGTHS_PREFIX = 2;
	private static int MIN_HMAC_KEY_SIZE_BYTES = 32;
	private static int HMAC_KEY_SIZE_BYTES = 64;
	private static int AES_KEY_SIZE_BYTES = 32;

	private static final String TRANSFORMATION = "RSA/ECB/OAEPPadding";
	private static final String ALGORITHM = "RSA";
	private static final String MESSAGE_DIGEST = "SHA-256";
	private static final String MASK_GENERATION_FUNCTION = "MGF1";
	
	private static byte Aes256CbcPkcs7 = (byte) 0;
	private static byte HMACSHA256 = (byte) 0;

	private static byte[] algorithmChoices = { Aes256CbcPkcs7, HMACSHA256 };
	
	@Value("${fabricWorkspaces.gateway.encryption.publickey.modulus}")
	private String modulus;
	
	@Value("${fabricWorkspaces.gateway.encryption.publickey.exponent}")
	private String exponent;
	
	public String encryptCredentialDetails(String accessKey, String secretKey){
		try {
			String connectionDetailsString = "{\"credentialData\":[{\"name\":\"username\",\"value\":\"" + accessKey + "\"},{\"name\":\"password\",\"value\":\"" + secretKey + "\"}]}";
			byte[] plainTextBytes = connectionDetailsString.getBytes("UTF-8");
			byte[] modulusBytes = Base64.getDecoder().decode(modulus);
			byte[] exponentBytes = Base64.getDecoder().decode(exponent);
			return Encrypt(plainTextBytes, modulusBytes, exponentBytes);
		}catch(Exception e) {
			log.error("Failed to encrypt credentials data with exception : {}",e.getMessage());
			return null;
		}
	}
	
	
	private static byte[] authenticatedEncrypt(byte[] keyEnc, byte[] keyMac, byte[] message) throws Exception {
		if (keyEnc.length < 32)
			throw new Exception("Encryption Key must be at least 256 bits (32 bytes)");
		if (keyMac.length < 32)
			throw new Exception("Mac Key must be at least 256 bits (32 bytes)");
		if (message == null)
			throw new Exception("Credentials cannot be null");

		byte[] iv;
		byte[] cipherText;
		byte[] tagData;
		byte[] tag;
		byte[] output;

		SecretKeySpec key = new SecretKeySpec(keyEnc, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		iv = cipher.getIV();
		cipherText = cipher.doFinal(message);

		// The IV and ciphertest both need to be included in the MAC to prevent
		// tampering.
		//
		// By including the algorithm identifiers, we have technically moved from
		// simple Authenticated Encryption (AE) to Authenticated Encryption with
		// Additional Data (AEAD). By including the algorithm identifiers in the
		// MAC, it becomes harder for an attacker to change them as an attempt to
		// perform a downgrade attack.

		tagData = new byte[algorithmChoices.length + iv.length + cipherText.length];
		int tagDataOffset = 0;

		System.arraycopy(algorithmChoices, 0, tagData, tagDataOffset, algorithmChoices.length);
		tagDataOffset = algorithmChoices.length + tagDataOffset;

		System.arraycopy(iv, 0, tagData, tagDataOffset, iv.length);
		tagDataOffset = iv.length + tagDataOffset;

		System.arraycopy(cipherText, 0, tagData, tagDataOffset, cipherText.length);
		tagDataOffset = cipherText.length + tagDataOffset;

		SecretKeySpec secretKeySpec = new SecretKeySpec(keyMac, "HmacSHA256");
		Mac tagGenerator = Mac.getInstance("HmacSHA256");
		tagGenerator.init(secretKeySpec);

		tagGenerator.update(tagData, 0, tagData.length);
		tag = tagGenerator.doFinal();
		tagGenerator.reset();

		// Build the final result as the concatenation of everything except the keys.
		output = new byte[algorithmChoices.length + tag.length + iv.length + cipherText.length];
		int outputOffset = 0;

		System.arraycopy(algorithmChoices, 0, output, outputOffset, algorithmChoices.length);
		outputOffset = algorithmChoices.length + outputOffset;

		System.arraycopy(tag, 0, output, outputOffset, tag.length);
		outputOffset = tag.length + outputOffset;

		System.arraycopy(iv, 0, output, outputOffset, iv.length);
		outputOffset = iv.length + outputOffset;

		System.arraycopy(cipherText, 0, output, outputOffset, cipherText.length);
		outputOffset = cipherText.length + outputOffset;

		return output;
	}
	
	public static String Encrypt(byte[] plainTextBytes, byte[] modulusBytes, byte[] exponentBytes) throws Exception {

		// Generate ephemeral keys for encryption (32 bytes), hmac (64 bytes)
		byte[] keyEnc = GetRandomBytes(AES_KEY_SIZE_BYTES);
		byte[] keyMac = GetRandomBytes(HMAC_KEY_SIZE_BYTES);

		// Encrypt message using ephemeral keys and Authenticated Encryption
		// Symmetric algorithm and encryptor
		byte[] cipherText = authenticatedEncrypt(keyEnc, keyMac, plainTextBytes);

		// Encrypt ephemeral keys using RSA
		byte[] keys = new byte[KEY_LENGTHS_PREFIX + keyEnc.length + keyMac.length];

		// Prefixing length of Keys. Symmetric Key length followed by HMAC key length
		keys[0] = (byte) KeyLength32;
		keys[1] = (byte) KeyLength64;

		System.arraycopy(keyEnc, 0, keys, 2, keyEnc.length);
		System.arraycopy(keyMac, 0, keys, keyEnc.length + 2, keyMac.length);

		BigInteger modulus = new BigInteger(1, modulusBytes);
		BigInteger exponent = new BigInteger(1, exponentBytes);

		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory fact = KeyFactory.getInstance(ALGORITHM);
		PublicKey pubKey = fact.generatePublic(keySpec);

		Cipher cipherKey = Cipher.getInstance(TRANSFORMATION);

		OAEPParameterSpec oaepParams = new OAEPParameterSpec(MESSAGE_DIGEST, MASK_GENERATION_FUNCTION, new MGF1ParameterSpec(MESSAGE_DIGEST),
				PSource.PSpecified.DEFAULT);
		cipherKey.init(Cipher.ENCRYPT_MODE, pubKey, oaepParams);

		byte[] encryptedKeys = cipherKey.doFinal(keys);

		// Prepare final payload
		return Base64.getEncoder().encodeToString(encryptedKeys) + Base64.getEncoder().encodeToString(cipherText);
	}

	private static byte[] GetRandomBytes(int size) {

		byte[] data = new byte[size];
		SecureRandom random = new SecureRandom();
		random.nextBytes(data);
		return data;
	}
	
}
