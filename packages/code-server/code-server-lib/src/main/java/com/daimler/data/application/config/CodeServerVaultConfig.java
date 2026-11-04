package com.daimler.data.application.config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.KubernetesAuthentication;
import org.springframework.vault.authentication.KubernetesAuthenticationOptions;
import org.springframework.vault.authentication.SimpleSessionManager;
import org.springframework.vault.client.RestTemplateBuilder;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.client.VaultHttpHeaders;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;
import org.springframework.web.client.RestOperations;

import com.daimler.data.dto.pat.PatVaultEntry;
import com.daimler.data.dto.pat.PatVaultListItem;
@Configuration
public class CodeServerVaultConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(CodeServerVaultConfig.class);
	private static final Pattern VERSION_PATTERN = Pattern.compile("^v([1-9][0-9]*)$");

	@Value("${spring.cloud.vault.patpath}")
	private String patPath;
	@Value("${spring.cloud.vault.mountpath}")
	private String mountPath;
	@Value("${spring.cloud.vault.uri}")
	private String vaultUri;
	@Value("${spring.cloud.vault.kubernetes.kubernetes-path}")
	private String kubernetesMountPath;
	@Value("${spring.cloud.vault.kubernetes.role}")
	private String kubernetesLoginRole;
	@Value("${spring.cloud.vault.kubernetes.service-account-token-file}")
	private String kubernetesServiceAccountTokenPath;
	@Value("${spring.cloud.vault.namespace}")
	private String namespace;

	public int createPatSecret(String userId, PatVaultEntry entry) {
		return createPatSecret(userId, version -> entry);
	}

	public synchronized int createPatSecret(String userId, IntFunction<PatVaultEntry> entryFactory) {
		VaultTemplate vaultTemplate = createVaultTemplate();
		String basePath = userPatPath(userId);
		for (int attempt = 0; attempt < 3; attempt++) {
			int version = nextVersion(vaultTemplate, basePath);
			PatVaultEntry entry = entryFactory.apply(version);
			Map<String, Object> secret = new HashMap<>();
			secret.put("secret", entry.getSecret());
			secret.put("displaySuffix", entry.getDisplaySuffix());
			secret.put("comment", entry.getComment());
			secret.put("createdAt", entry.getCreatedAt());
			secret.put("codeSpaceProjectNames", entry.getCodeSpaceProjectNames());
			secret.put("permissions", entry.getPermissions());
			try {
				vaultTemplate.opsForVersionedKeyValue(mountPath).put(basePath + "/v" + version, secret);
				return version;
			} catch (Exception e) {
				LOGGER.warn("Unable to write PAT metadata for user {}", userId);
			}
		}
		throw new IllegalStateException("Unable to create PAT metadata");
	}

	public List<PatVaultListItem> listPatSecrets(String userId) {
		try {
			VaultTemplate vaultTemplate = createVaultTemplate();
			List<String> keys = vaultTemplate.opsForVersionedKeyValue(mountPath).list(userPatPath(userId));
			List<PatVaultListItem> result = new ArrayList<>();
			if (keys == null) {
				return result;
			}
			for (String key : keys) {
				Matcher matcher = VERSION_PATTERN.matcher(key);
				if (!matcher.matches()) {
					continue;
				}
				int version = Integer.parseInt(matcher.group(1));
				PatVaultListItem item = readListItem(vaultTemplate, userId, version);
				if (item != null) {
					result.add(item);
				}
			}
			return result;
		} catch (Exception e) {
			LOGGER.warn("Unable to list PAT metadata for user {}", userId);
			throw new IllegalStateException("Unable to list PAT metadata");
		}
	}

	public boolean validatePatSecret(String userId, int version, String secret) {
		try {
			Versioned<Map<String, Object>> response = createVaultTemplate().opsForVersionedKeyValue(mountPath)
					.get(versionPath(userId, version));
			if (response == null || !response.hasData() || response.getData().get("secret") == null) {
				return false;
			}
			String storedSecret = String.valueOf(response.getData().get("secret"));
			return MessageDigest.isEqual(storedSecret.getBytes(StandardCharsets.UTF_8),
					secret.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOGGER.warn("Unable to validate PAT metadata for user {}", userId);
			return false;
		}
	}

	public boolean deletePatSecret(String userId, int version) {
		try {
			VaultTemplate vaultTemplate = createVaultTemplate();
			String path = versionPath(userId, version);
			VaultVersionedKeyValueOperations operations = vaultTemplate.opsForVersionedKeyValue(mountPath);
			Versioned<Map<String, Object>> response = operations.get(path);
			if (response == null || !response.hasData()) {
				return false;
			}
			operations.delete(path, response.getVersion());
			return true;
		} catch (Exception e) {
			LOGGER.warn("Unable to revoke PAT metadata for user {}", userId);
			throw new IllegalStateException("Unable to revoke PAT");
		}
	}

	private PatVaultListItem readListItem(VaultTemplate vaultTemplate, String userId, int version) {
		PatVaultListItem item = new PatVaultListItem();
		item.setVersion(version);
		try {
			Versioned<Map<String, Object>> response = vaultTemplate.opsForVersionedKeyValue(mountPath)
					.get(versionPath(userId, version));
			if (response == null || !response.hasData()) {
				item.setStatus("REVOKED");
				return item;
			}
			Map<String, Object> data = response.getData();
			item.setDisplaySuffix(stringValue(data.get("displaySuffix")));
			item.setComment(stringValue(data.get("comment")));
			item.setCreatedAt(stringValue(data.get("createdAt")));
			item.setCodeSpaceProjectCount(asStringList(data.get("codeSpaceProjectNames")).size());
			item.setPermissions(asStringList(data.get("permissions")));
			item.setStatus("ACTIVE");
			return item;
		} catch (Exception e) {
			LOGGER.warn("Unable to read PAT metadata for user {}", userId);
			throw new IllegalStateException("Unable to read PAT metadata");
		}
	}

	private int nextVersion(VaultTemplate vaultTemplate, String basePath) {
		List<String> keys = vaultTemplate.opsForVersionedKeyValue(mountPath).list(basePath);
		int maxVersion = 0;
		if (keys != null) {
			for (String key : keys) {
				Matcher matcher = VERSION_PATTERN.matcher(key);
				if (matcher.matches()) {
					maxVersion = Math.max(maxVersion, Integer.parseInt(matcher.group(1)));
				}
			}
		}
		return maxVersion + 1;
	}

	private String userPatPath(String userId) {
		if (userId == null || !userId.matches("[A-Za-z0-9._@-]+")) {
			throw new IllegalArgumentException("Invalid PAT owner");
		}
		return patPath + "/" + userId + "/codespaces_pat";
	}

	private String versionPath(String userId, int version) {
		if (version < 1) {
			throw new IllegalArgumentException("Invalid PAT version");
		}
		return userPatPath(userId) + "/v" + version;
	}

	private List<String> asStringList(Object value) {
		if (!(value instanceof List<?>)) {
			return new ArrayList<>();
		}
		List<String> result = new ArrayList<>();
		for (Object item : (List<?>) value) {
			if (item != null) {
				result.add(String.valueOf(item));
			}
		}
		return result;
	}

	private String stringValue(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private VaultTemplate createVaultTemplate() {
		VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));
		RestOperations restOperations = restOperations(endpoint);
		KubernetesAuthentication authentication = new KubernetesAuthentication(kubernetesOptions(),
				restOperations);
		return new VaultTemplate(restTemplateBuilder(endpoint), new SimpleSessionManager(authentication));
	}

	private KubernetesAuthenticationOptions kubernetesOptions() {
		try {
			String serviceAccountToken = Files.readString(new File(kubernetesServiceAccountTokenPath).toPath());
			return KubernetesAuthenticationOptions.builder().jwtSupplier(() -> serviceAccountToken)
					.role(kubernetesLoginRole).path(kubernetesMountPath).build();
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read Kubernetes service account token");
		}
	}

	private RestOperations restOperations(VaultEndpoint endpoint) {
		return restTemplateBuilder(endpoint).build();
	}

	private RestTemplateBuilder restTemplateBuilder(VaultEndpoint endpoint) {
		return RestTemplateBuilder.builder().endpoint(endpoint)
				.defaultHeader(VaultHttpHeaders.VAULT_NAMESPACE, namespace);
	}
}
