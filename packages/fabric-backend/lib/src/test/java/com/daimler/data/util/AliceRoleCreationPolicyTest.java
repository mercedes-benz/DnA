package com.daimler.data.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class AliceRoleCreationPolicyTest {

	@Test
	void allowsWorkspaceAdminEntitlement() {
		assertTrue(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("dna.fabric_workspace_123_Admin"), "dna", "fabric_workspace_"));
	}

	@Test
	void matchesApplicationAndEntitlementCaseInsensitively() {
		assertTrue(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("DNA.FABRIC_WORKSPACE_123_aDmIn"), "dna", "fabric_workspace_"));
	}

	@Test
	void rejectsNonAdminWorkspaceEntitlements() {
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("dna.fabric_workspace_123_Contributor"), "dna", "fabric_workspace_"));
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("dna.fabric_workspace_123_Member"), "dna", "fabric_workspace_"));
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("dna.fabric_workspace_123_Viewer"), "dna", "fabric_workspace_"));
	}

	@Test
	void rejectsEntitlementsForAnotherApplicationOrWithoutSubgroupPrefix() {
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("other.fabric_workspace_123_Admin"), "dna", "fabric_workspace_"));
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				List.of("dna.workspace_123_Admin"), "dna", "fabric_workspace_"));
	}

	@Test
	void rejectsNullAndEmptyEntitlementListsAndNullEntries() {
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(null, "dna", "fabric_workspace_"));
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(List.of(), "dna", "fabric_workspace_"));
		assertFalse(AliceRoleCreationPolicy.hasWorkspaceAdminEntitlement(
				Arrays.asList(null, null), "dna", "fabric_workspace_"));
	}
}
