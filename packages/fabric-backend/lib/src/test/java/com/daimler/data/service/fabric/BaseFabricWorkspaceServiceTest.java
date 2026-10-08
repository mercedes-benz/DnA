package com.daimler.data.service.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimler.data.application.client.AuthoriserClient;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;

@ExtendWith(MockitoExtension.class)
class BaseFabricWorkspaceServiceTest {

	@Mock
	private FabricWorkspaceCustomRepository customRepo;

	@Mock
	private AuthoriserClient identityClient;

	@InjectMocks
	private BaseFabricWorkspaceService service;

	@BeforeEach
	void setPolicyConfiguration() throws ReflectiveOperationException {
		setField("applicationId", "dna");
		setField("subgroupPrefix", "fabric_workspace_");
	}

	@Test
	void codespaceAdminIsAllowedWithoutRepositoryOrAliceCalls() {
		assertTrue(service.canCreateAliceRole("alice", true));

		verifyNoInteractions(customRepo, identityClient);
	}

	@Test
	void workspaceCreatorIsAllowedWithoutCallingAlice() {
		when(customRepo.existsByCreator("alice")).thenReturn(true);

		assertTrue(service.canCreateAliceRole("alice", false));

		verify(identityClient, never()).getAllUserEntitlements("alice");
	}

	@Test
	void workspaceAdminEntitlementIsAllowed() {
		when(customRepo.existsByCreator("alice")).thenReturn(false);
		when(identityClient.getAllUserEntitlements("alice"))
				.thenReturn(List.of("dna.fabric_workspace_123_Admin"));

		assertTrue(service.canCreateAliceRole("alice", false));
	}

	@Test
	void nonAdminEntitlementIsDenied() {
		when(customRepo.existsByCreator("alice")).thenReturn(false);
		when(identityClient.getAllUserEntitlements("alice"))
				.thenReturn(List.of("dna.fabric_workspace_123_Member"));

		assertFalse(service.canCreateAliceRole("alice", false));
	}

	@Test
	void aliceFailureIsDenied() {
		when(customRepo.existsByCreator("alice")).thenReturn(false);
		when(identityClient.getAllUserEntitlements("alice")).thenThrow(new RuntimeException("unavailable"));

		assertFalse(service.canCreateAliceRole("alice", false));
	}

	private void setField(String name, Object value) throws ReflectiveOperationException {
		Field field = BaseFabricWorkspaceService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(service, value);
	}
}
