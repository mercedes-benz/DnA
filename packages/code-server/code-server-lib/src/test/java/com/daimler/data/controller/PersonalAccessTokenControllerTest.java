package com.daimler.data.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.List;
import java.util.function.IntFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.config.CodeServerVaultConfig;
import com.daimler.data.dto.pat.CreatePatRequest;
import com.daimler.data.dto.pat.CreatePatResponse;
import com.daimler.data.dto.pat.PatVaultEntry;
import com.daimler.data.dto.workspace.CodeServerProjectDetailsVO;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.service.pat.PatCryptoService;
import com.daimler.data.service.workspace.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;

class PersonalAccessTokenControllerTest {
	private UserStore userStore;
	private WorkspaceService workspaceService;
	private CodeServerVaultConfig vaultConfig;
	private PersonalAccessTokenController controller;

	@BeforeEach
	void setUp() {
		userStore = new UserStore();
		userStore.setUserInfo(UserStore.UserInfo.builder().id("creator").build());
		workspaceService = mock(WorkspaceService.class);
		vaultConfig = mock(CodeServerVaultConfig.class);
		PatCryptoService crypto = new PatCryptoService(
				Base64.getEncoder().encodeToString(new byte[32]), new ObjectMapper());
		controller = new PersonalAccessTokenController(userStore, workspaceService, crypto, vaultConfig);
	}

	@Test
	void rejectsUnknownPermissionAndEmptyPermissionList() {
		CreatePatRequest unknown = new CreatePatRequest();
		unknown.setPermissions(List.of("workspace:admin"));
		CreatePatRequest empty = new CreatePatRequest();
		empty.setPermissions(List.of());

		assertEquals(400, controller.createPat(unknown).getStatusCodeValue());
		assertEquals(400, controller.createPat(empty).getStatusCodeValue());
		verify(workspaceService, never()).getAll(any(), any(Integer.class), any(Integer.class));
	}

	@Test
	void successfulCreationReturnsNoStoreHeader() {
		stubAccessibleWorkspace();
		when(vaultConfig.createPatSecret(eq("creator"), anyIntFunction())).thenAnswer(invocation -> {
			IntFunction<PatVaultEntry> entryFactory = invocation.getArgument(1);
			PatVaultEntry entry = entryFactory.apply(1);
			assertNotNull(entry.getSecret());
			return 1;
		});
		CreatePatRequest request = new CreatePatRequest();
		request.setPermissions(List.of("workspace:read"));
		request.setComment("cli");

		ResponseEntity<?> response = controller.createPat(request);

		assertEquals(201, response.getStatusCodeValue());
		assertEquals("no-store", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
		CreatePatResponse body = (CreatePatResponse) response.getBody();
		assertNotNull(body);
		assertTrue(body.getToken().startsWith("cs_pat_v1_"));
		assertEquals(1, body.getCodeSpaceProjectCount());
	}

	@Test
	void listResponseDoesNotIncludeSecretField() throws Exception {
		com.daimler.data.dto.pat.PatVaultListItem item = new com.daimler.data.dto.pat.PatVaultListItem();
		item.setVersion(2);
		item.setStatus("ACTIVE");
		when(vaultConfig.listPatSecrets("creator")).thenReturn(List.of(item));

		ResponseEntity<?> response = controller.listPats();
		String json = new ObjectMapper().writeValueAsString(response.getBody());

		assertEquals(200, response.getStatusCodeValue());
		assertTrue(!json.contains("\"secret\""));
		assertTrue(!json.contains("\"token\""));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IntFunction<PatVaultEntry> anyIntFunction() {
		return (IntFunction) any(IntFunction.class);
	}

	private void stubAccessibleWorkspace() {
		CodeServerWorkspaceVO workspace = mock(CodeServerWorkspaceVO.class);
		CodeServerProjectDetailsVO details = mock(CodeServerProjectDetailsVO.class);
		UserInfoVO owner = mock(UserInfoVO.class);
		when(workspace.getProjectDetails()).thenReturn(details);
		when(details.getProjectName()).thenReturn("project");
		when(details.getProjectOwner()).thenReturn(owner);
		when(owner.getId()).thenReturn("creator");
		when(workspaceService.getAll("creator", 0, 0)).thenReturn(List.of(workspace));
	}
}
