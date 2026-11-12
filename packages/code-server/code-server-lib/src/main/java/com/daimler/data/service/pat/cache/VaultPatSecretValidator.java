package com.daimler.data.service.pat.cache;

import org.springframework.stereotype.Service;
import com.daimler.data.application.config.CodeServerVaultConfig;

@Service
public class VaultPatSecretValidator implements PatSecretValidator {
	private final CodeServerVaultConfig vaultConfig;

	public VaultPatSecretValidator(CodeServerVaultConfig vaultConfig) {
		this.vaultConfig = vaultConfig;
	}

	@Override
	public boolean validate(String userId, int version, String secret) {
		return vaultConfig.validatePatSecret(userId, version, secret);
	}
}
