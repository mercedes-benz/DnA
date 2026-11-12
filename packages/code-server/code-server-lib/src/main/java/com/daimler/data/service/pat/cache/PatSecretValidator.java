package com.daimler.data.service.pat.cache;

public interface PatSecretValidator {
	boolean validate(String userId, int version, String secret);
}
