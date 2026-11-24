package com.daimler.data.service.pat;

import com.daimler.data.dto.pat.ExternalCodespaceInfoVO;

/**
 * Result wrapper for external (PAT-scoped) codespace lookups, used to
 * distinguish between a codespace that does not exist at all and a
 * codespace that exists but the requesting PAT does not have access to.
 */
public final class ExternalCodespaceResult {

	public enum Status {
		FOUND, NOT_FOUND, FORBIDDEN
	}

	private final Status status;
	private final ExternalCodespaceInfoVO data;

	private ExternalCodespaceResult(Status status, ExternalCodespaceInfoVO data) {
		this.status = status;
		this.data = data;
	}

	public static ExternalCodespaceResult found(ExternalCodespaceInfoVO data) {
		return new ExternalCodespaceResult(Status.FOUND, data);
	}

	public static ExternalCodespaceResult notFound() {
		return new ExternalCodespaceResult(Status.NOT_FOUND, null);
	}

	public static ExternalCodespaceResult forbidden() {
		return new ExternalCodespaceResult(Status.FORBIDDEN, null);
	}

	public Status getStatus() {
		return status;
	}

	public ExternalCodespaceInfoVO getData() {
		return data;
	}
}
