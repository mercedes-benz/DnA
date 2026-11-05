package com.daimler.data.service.pat.cache;

import java.time.Duration;
import java.util.Optional;

public interface PatSecretStore {
	Optional<String> getSecretHash(String userId, int version);
	void put(String userId, int version, String secretHash, Duration ttl);
	void evict(String userId, int version);
}
