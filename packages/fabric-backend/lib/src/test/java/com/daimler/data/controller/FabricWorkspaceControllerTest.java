package com.daimler.data.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.fabricWorkspace.AuthoriserRoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.EntraGroupResponseVO;
import com.daimler.data.dto.fabricWorkspace.CreateRoleRequestVO;
import com.daimler.data.service.fabric.FabricWorkspaceService;

@ExtendWith(MockitoExtension.class)
class FabricWorkspaceControllerTest {

	@Mock
	private UserStore userStore;

	@Mock
	private FabricWorkspaceService service;

	@Mock
	private UserInfo userInfo;

	@InjectMocks
	private FabricWorkspaceController controller;

	@BeforeEach
	void setCurrentUser() {
		CreatedByVO user = new CreatedByVO();
		user.setId("alice");
		when(userStore.getUserInfo()).thenReturn(userInfo);
		when(userStore.getVO()).thenReturn(user);
	}

	@Test
	void createRoleReturnsForbiddenAndDoesNotCreateWhenUserIsIneligible() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(false);

		ResponseEntity<GenericMessage> response = controller.createRole(new CreateRoleRequestVO());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("FAILED", response.getBody().getSuccess());
		assertEquals("Only Fabric workspace owners, Fabric workspace admins and Codespaces admins can create Alice roles.",
				response.getBody().getErrors().get(0).getMessage());
		verify(service, never()).createGenericRole(any(), any());
	}

	@Test
	void createRoleCallsServiceWhenUserIsEligible() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);
		when(service.createGenericRole(any(), any())).thenReturn(new GenericMessage("SUCCESS"));

		ResponseEntity<GenericMessage> response = controller.createRole(new CreateRoleRequestVO());

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(service).createGenericRole(any(), any());
	}

	@Test
	void getRoleDetailsReturnsForbiddenForNonOwnerNonAdmin() {
		when(service.isRoleOwner("role-id", "alice")).thenReturn(false);

		ResponseEntity<?> response = controller.getRoleDetails("role-id");

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		verify(service, never()).getRoleDetails(anyString());
	}

	@Test
	void getRoleDetailsAllowsOwner() {
		when(service.isRoleOwner("role-id", "alice")).thenReturn(true);
		when(service.getRoleDetails("role-id")).thenReturn(new AuthoriserRoleDetailsVO());

		ResponseEntity<?> response = controller.getRoleDetails("role-id");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void getGroupMemberDetailsReturnsForbiddenForNonOwnerNonAdmin() {
		when(service.isRoleOwner("role-id", "alice")).thenReturn(false);

		ResponseEntity<EntraGroupResponseVO> response = controller.getGroupMemberDetails("role-id");

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		verify(service, never()).getEntraGroupMembers(anyString());
	}

	@Test
	void getGroupMemberDetailsAllowsOwner() {
		when(service.isRoleOwner("role-id", "alice")).thenReturn(true);
		when(service.getEntraGroupMembers("role-id")).thenReturn(new EntraGroupResponseVO());

		ResponseEntity<EntraGroupResponseVO> response = controller.getGroupMemberDetails("role-id");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}
}
