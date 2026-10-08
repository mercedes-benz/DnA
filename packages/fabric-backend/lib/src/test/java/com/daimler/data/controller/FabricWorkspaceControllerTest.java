package com.daimler.data.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.fabricWorkspace.AliceRoleEligibilityVO;
import com.daimler.data.dto.fabricWorkspace.AuthoriserRoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.EntraGroupResponseVO;
import com.daimler.data.dto.fabricWorkspace.CreateRoleRequestVO;
import com.daimler.data.service.fabric.FabricWorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		ReflectionTestUtils.setField(controller, "aliceRoleAgreementVersion", "DRAFT-1.0");
		when(userStore.getUserInfo()).thenReturn(userInfo);
		when(userStore.getVO()).thenReturn(user);
	}

	@Test
	void createRoleReturnsForbiddenAndDoesNotCreateWhenUserIsIneligible() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(false);

		ResponseEntity<GenericMessage> response = controller.createRole(new CreateRoleRequestVO());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("FAILED", response.getBody().getSuccess());
		assertEquals("Only Fabric workspace owners, Fabric workspace admins, Codespaces project owners or admins and Codespaces admins can create Alice roles.",
				response.getBody().getErrors().get(0).getMessage());
		verify(service, never()).createGenericRole(any(), any());
	}

	@Test
	void createRoleCallsServiceWhenUserIsEligible() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);
		when(service.createGenericRole(any(), any())).thenReturn(new GenericMessage("SUCCESS"));

		ResponseEntity<GenericMessage> response = controller.createRole(roleRequest(Boolean.TRUE, "DRAFT-1.0"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(service).createGenericRole(any(), any());
	}

	@Test
	void createRoleRejectsNullAgreementAcceptance() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);

		assertAgreementRequired(controller.createRole(roleRequest(null, "DRAFT-1.0")));

		verify(service, never()).createGenericRole(any(), any());
	}

	@Test
	void createRoleRejectsFalseAgreementAcceptance() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);

		assertAgreementRequired(controller.createRole(roleRequest(Boolean.FALSE, "DRAFT-1.0")));

		verify(service, never()).createGenericRole(any(), any());
	}

	@Test
	void createRoleRejectsOutdatedAgreementVersion() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);

		assertAgreementRequired(controller.createRole(roleRequest(Boolean.TRUE, "OLD")));

		verify(service, never()).createGenericRole(any(), any());
	}

	@Test
	void createRoleRejectsNullRequestData() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);

		assertAgreementRequired(controller.createRole(new CreateRoleRequestVO()));

		verify(service, never()).createGenericRole(any(), any());
	}

	@Test
	void eligibilityIncludesCurrentAgreementVersion() {
		when(service.canCreateAliceRole("alice", false)).thenReturn(true);

		ResponseEntity<AliceRoleEligibilityVO> response = controller.getAliceRoleCreationEligibility();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("DRAFT-1.0", response.getBody().getAgreementVersion());
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

	private CreateRoleRequestVO roleRequest(Boolean agreementAccepted, String agreementVersion) {
		Map<String, Object> data = new HashMap<>();
		data.put("roleName", "dna_test");
		data.put("isDynamic", false);
		data.put("agreementAccepted", agreementAccepted);
		data.put("agreementVersion", agreementVersion);
		return new ObjectMapper().convertValue(Map.of("data", data), CreateRoleRequestVO.class);
	}

	private void assertAgreementRequired(ResponseEntity<GenericMessage> response) {
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("FAILED", response.getBody().getSuccess());
		assertEquals("You must accept the current Alice role creation agreement before creating a role.",
				response.getBody().getErrors().get(0).getMessage());
	}
}
