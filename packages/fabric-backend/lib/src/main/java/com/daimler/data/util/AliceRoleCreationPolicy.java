package com.daimler.data.util;

import java.util.List;
import java.util.Locale;

public final class AliceRoleCreationPolicy {

	private AliceRoleCreationPolicy() {
	}

	public static boolean hasWorkspaceAdminEntitlement(List<String> entitlements, String applicationId,
			String subgroupPrefix) {
		if (entitlements == null || entitlements.isEmpty() || applicationId == null || subgroupPrefix == null) {
			return false;
		}

		String requiredPrefix = (applicationId + "." + subgroupPrefix).toLowerCase(Locale.ROOT);
		return entitlements.stream()
				.filter(entitlement -> entitlement != null)
				.map(entitlement -> entitlement.toLowerCase(Locale.ROOT))
				.anyMatch(entitlement -> entitlement.startsWith(requiredPrefix) && entitlement.endsWith("_admin"));
	}
}
