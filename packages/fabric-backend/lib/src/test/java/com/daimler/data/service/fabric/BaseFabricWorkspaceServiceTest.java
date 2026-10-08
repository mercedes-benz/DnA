package com.daimler.data.service.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.daimler.data.application.client.AuthoriserClient;
import com.daimler.data.application.client.CodeServerClient;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.db.entities.AuthoriserRolesNsql;
import com.daimler.data.db.json.AuthoriserRoleDeatils;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.roles.AuthoriserRolesRepository;
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabric.EntiltlemetDetailsDto;
import com.daimler.data.dto.fabricWorkspace.AuthoriserRoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.CreateRoleRequestVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.MembersVO;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class BaseFabricWorkspaceServiceTest {

	@Mock
	private FabricWorkspaceCustomRepository customRepo;

	@Mock
	private AuthoriserClient identityClient;

	@Mock
	private CodeServerClient codeServerClient;

	@Mock
	private FabricWorkspaceAssembler assembler;

	@Mock
	private AuthoriserRolesRepository rolesJpaRepo;

	@InjectMocks
	private BaseFabricWorkspaceService service;

	@BeforeEach
	void setPolicyConfiguration() throws ReflectiveOperationException {
		setField("applicationId", "dna");
		setField("subgroupPrefix", "fabric_workspace_");
		setField("aliceRetryMaxAttempts", 3);
		setField("aliceRetryBackoffMillis", 0L);
		setField("fabricTechUserId", "TECHUSER");
		setField("communityAvailability", "COMMUNITY1,COMMUNITY2");
		setField("workflowDefinitionId", "1");
	}

	@Test
	void codespaceAdminIsAllowedWithoutRepositoryOrAliceCalls() {
		assertTrue(service.canCreateAliceRole("alice", true));

		verifyNoInteractions(customRepo, identityClient, codeServerClient);
	}

	@Test
	void workspaceCreatorIsAllowedWithoutCallingAlice() {
		when(customRepo.existsByCreator("alice")).thenReturn(true);

		assertTrue(service.canCreateAliceRole("alice", false));

		verify(identityClient, never()).getAllUserEntitlements("alice");
		verify(codeServerClient, never()).isCodespaceProjectOwnerOrAdmin();
	}

	@Test
	void codespacesProjectOwnerOrAdminIsAllowedWithoutCallingAlice() {
		when(customRepo.existsByCreator("alice")).thenReturn(false);
		when(codeServerClient.isCodespaceProjectOwnerOrAdmin()).thenReturn(true);

		assertTrue(service.canCreateAliceRole("alice", false));

		verify(identityClient, never()).getAllUserEntitlements("alice");
	}

	@Test
	void workspaceAdminEntitlementIsAllowed() {
		when(customRepo.existsByCreator("alice")).thenReturn(false);
		when(codeServerClient.isCodespaceProjectOwnerOrAdmin()).thenReturn(false);
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

	@Test
	void createsNewRoleAndCompletesAllSetupSteps() {
		String roleName = "dna_test";
		String roleId = "DNA_TEST";
		CreatedByVO requestUser = requestUser();
		prepareNewRole(roleName, HttpStatus.OK);

		GenericMessage response = service.createGenericRole(roleRequest(roleName), requestUser);

		assertEquals("SUCCESS", response.getSuccess());
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().isEmpty());
		assertNotNull(response.getWarnings());
		assertTrue(response.getWarnings().isEmpty());
		verify(identityClient, times(1)).createRole(any(CreateRoleRequestDto.class), eq("alice"));
		verify(identityClient).AssignEntitlementToRole(roleId, roleId);
		verify(rolesJpaRepo).save(any(AuthoriserRolesNsql.class));
		verify(identityClient).AssignGlobalRoleAssignerPrivilegesToCreator("TECHUSER", roleId);
	}

	@Test
	void continuesSetupAndReportsDnASaveFailure() {
		String roleName = "dna_test";
		String roleId = "DNA_TEST";
		prepareNewRole(roleName, HttpStatus.OK);
		when(rolesJpaRepo.save(any(AuthoriserRolesNsql.class))).thenThrow(new RuntimeException("database unavailable"));

		GenericMessage response = service.createGenericRole(roleRequest(roleName), requestUser());

		assertEquals("FAILED", response.getSuccess());
		assertTrue(response.getErrors().get(0).getMessage().contains("saving the role in DnA"));
		assertTrue(response.getErrors().get(0).getMessage().contains("Click Create Role again"));
		verify(identityClient).createEntitlement(any(CreateEntitlementRequestDto.class));
		verify(identityClient).AssignEntitlementToRole(roleId, roleId);
	}

	@Test
	void retriesEntitlementAssignmentUntilItSucceeds() {
		String roleName = "dna_test";
		String roleId = "DNA_TEST";
		prepareNewRole(roleName, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.OK);

		GenericMessage response = service.createGenericRole(roleRequest(roleName), requestUser());

		assertEquals("SUCCESS", response.getSuccess());
		verify(identityClient, times(3)).AssignEntitlementToRole(roleId, roleId);
	}

	@Test
	void reportsFailedEntitlementAssignmentAfterAllRetries() {
		String roleName = "dna_test";
		String roleId = "DNA_TEST";
		prepareNewRole(roleName, HttpStatus.INTERNAL_SERVER_ERROR);

		GenericMessage response = service.createGenericRole(roleRequest(roleName), requestUser());

		assertEquals("FAILED", response.getSuccess());
		assertNotNull(response.getSuccess());
		assertTrue(response.getErrors().get(0).getMessage().contains("entitlement assignment"));
		verify(identityClient, times(3)).AssignEntitlementToRole(roleId, roleId);
	}

	@Test
	void treatsEntitlementAssignmentConflictAsSuccess() {
		prepareNewRole("dna_test", HttpStatus.CONFLICT);

		GenericMessage response = service.createGenericRole(roleRequest("dna_test"), requestUser());

		assertEquals("SUCCESS", response.getSuccess());
		verify(identityClient).AssignEntitlementToRole("DNA_TEST", "DNA_TEST");
	}

	@Test
	void recoversEntitlementAfterAnEmptyCreateResponse() {
		String roleName = "dna_test";
		String roleId = "DNA_TEST";
		stubRoleCreation(roleName);
		prepareNewRoleSteps(roleId, HttpStatus.OK, true);
		EntiltlemetDetailsDto foundById = new EntiltlemetDetailsDto();
		foundById.setUuid("entitlement-uuid");
		when(identityClient.getEntitlement(roleId)).thenReturn(new EntiltlemetDetailsDto(), foundById);
		when(identityClient.getEntitlement(roleName)).thenReturn(new EntiltlemetDetailsDto());
		when(identityClient.createEntitlement(any(CreateEntitlementRequestDto.class)))
				.thenReturn(new EntiltlemetDetailsDto());
		when(identityClient.AssignEntitlementToRole(roleId, roleId)).thenReturn(HttpStatus.OK);

		GenericMessage response = service.createGenericRole(roleRequest(roleName), requestUser());

		assertEquals("SUCCESS", response.getSuccess());
		verify(identityClient).AssignEntitlementToRole(roleId, roleId);
	}

	@Test
	void resumesRoleOwnedByRequesterInDna() {
		String roleId = "DNA_TEST";
		when(identityClient.getRole(roleId)).thenReturn(existingAliceRole(roleId));
		when(rolesJpaRepo.findById(roleId)).thenReturn(Optional.of(roleWithOwner(roleId, "alice")));
		prepareNewRoleSteps(roleId, HttpStatus.OK, true);

		GenericMessage response = service.createGenericRole(roleRequest("dna_test"), requestUser());

		assertEquals("SUCCESS", response.getSuccess());
		verify(identityClient, never()).createRole(any(CreateRoleRequestDto.class), any(String.class));
		verify(identityClient).AssignEntitlementToRole(roleId, roleId);
	}

	@Test
	void resumesRoleOwnedByRequesterInAlice() {
		String roleId = "DNA_TEST";
		when(identityClient.getRole(roleId)).thenReturn(existingAliceRole(roleId));
		when(rolesJpaRepo.findById(roleId)).thenReturn(Optional.empty());
		AuthoriserRoleDetailsVO details = new AuthoriserRoleDetailsVO();
		MembersVO owner = new MembersVO();
		owner.setId("alice");
		details.setRoleOwners(List.of(owner));
		when(identityClient.getRoleDetails(roleId)).thenReturn(details);
		prepareNewRoleSteps(roleId, HttpStatus.OK, true);

		GenericMessage response = service.createGenericRole(roleRequest("dna_test"), requestUser());

		assertEquals("SUCCESS", response.getSuccess());
		verify(identityClient, never()).createRole(any(CreateRoleRequestDto.class), any(String.class));
		verify(identityClient).AssignEntitlementToRole(roleId, roleId);
	}

	@Test
	void doesNotResumeRoleOwnedBySomeoneElse() {
		String roleId = "DNA_TEST";
		when(identityClient.getRole(roleId)).thenReturn(existingAliceRole(roleId));
		when(rolesJpaRepo.findById(roleId)).thenReturn(Optional.of(roleWithOwner(roleId, "another-user")));
		when(identityClient.getRoleDetails(roleId)).thenReturn(new AuthoriserRoleDetailsVO());

		GenericMessage response = service.createGenericRole(roleRequest("dna_test"), requestUser());

		assertEquals("CONFLICT", response.getSuccess());
		assertEquals("Failed to create role : Role Already Exists.", response.getErrors().get(0).getMessage());
		verify(identityClient, never()).createRole(any(CreateRoleRequestDto.class), any(String.class));
		verify(identityClient, never()).AssignRoleOwnerPrivilegesToCreator(any(String.class), any(String.class));
		verify(identityClient, never()).AssignGlobalRoleAssignerPrivilegesToCreator(any(String.class), any(String.class));
		verify(identityClient, never()).AssignRoleApproverPrivilegesToCreator(any(String.class), any(String.class));
		verify(identityClient, never()).createEntitlement(any(CreateEntitlementRequestDto.class));
		verify(identityClient, never()).AssignEntitlementToRole(any(String.class), any(String.class));
	}

	@Test
	void doesNotAssignApproverWhenTechnicalUserAssignerFails() {
		String roleName = "dna_test";
		String roleId = "DNA_TEST";
		stubRoleCreation(roleName);
		prepareNewRoleSteps(roleId, HttpStatus.INTERNAL_SERVER_ERROR, false);
		stubEntitlement(roleName, HttpStatus.OK);

		GenericMessage response = service.createGenericRole(roleRequest(roleName), requestUser());

		assertEquals("FAILED", response.getSuccess());
		assertTrue(response.getErrors().get(0).getMessage().contains("role approver privilege"));
		verify(identityClient, times(3)).AssignGlobalRoleAssignerPrivilegesToCreator("TECHUSER", roleId);
		verify(identityClient, never()).AssignRoleApproverPrivilegesToCreator(any(String.class), any(String.class));
	}

	@Test
	void hidesIdentityClientExceptionDetails() {
		when(identityClient.getRole("DNA_TEST")).thenThrow(new RuntimeException("secret-internal-detail"));

		GenericMessage response = service.createGenericRole(roleRequest("dna_test"), requestUser());

		assertEquals("FAILED", response.getSuccess());
		assertEquals("Failed to create role, please try again.", response.getErrors().get(0).getMessage());
		assertFalse(response.getErrors().get(0).getMessage().contains("secret-internal-detail"));
		assertNotNull(response.getWarnings());
	}

	@Test
	void saveCreatedRoleDetailsRecordsAgreementAcceptance() throws Exception {
		CreatedByVO requestUser = new CreatedByVO();
		when(assembler.toUserDetails(requestUser)).thenReturn(new UserDetails());

		service.saveCreatedRoleDetails("role-id", requestUser, false, "DRAFT-1.0");

		ArgumentCaptor<AuthoriserRolesNsql> roleCaptor = ArgumentCaptor.forClass(AuthoriserRolesNsql.class);
		verify(rolesJpaRepo).save(roleCaptor.capture());
		assertEquals(Boolean.TRUE, roleCaptor.getValue().getData().getAgreementAccepted());
		assertEquals("DRAFT-1.0", roleCaptor.getValue().getData().getAgreementVersion());
		assertNotNull(roleCaptor.getValue().getData().getAgreementAcceptedOn());
	}

	private void prepareNewRole(String roleName, HttpStatus... assignmentStatuses) {
		String roleId = roleName.toUpperCase();
		stubRoleCreation(roleName);
		prepareNewRoleSteps(roleId, HttpStatus.OK, true);
		stubEntitlement(roleName, assignmentStatuses);
	}

	private void stubRoleCreation(String roleName) {
		CreateRoleResponseDto createdRole = new CreateRoleResponseDto();
		createdRole.setId(roleName.toUpperCase());
		when(identityClient.createRole(any(CreateRoleRequestDto.class), eq("alice"))).thenReturn(createdRole);
	}

	private void prepareNewRoleSteps(String roleId, HttpStatus technicalAssignerStatus, boolean stubApprover) {
		when(identityClient.AssignRoleOwnerPrivilegesToCreator("alice", roleId)).thenReturn(HttpStatus.OK);
		when(identityClient.AssignGlobalRoleAssignerPrivilegesToCreator("alice", roleId)).thenReturn(HttpStatus.OK);
		when(identityClient.AssignGlobalRoleAssignerPrivilegesToCreator("TECHUSER", roleId))
				.thenReturn(technicalAssignerStatus);
		if (stubApprover) {
			when(identityClient.AssignRoleApproverPrivilegesToCreator("alice", roleId)).thenReturn(HttpStatus.OK);
		}
	}

	private void stubEntitlement(String roleName, HttpStatus... assignmentStatuses) {
		String roleId = roleName.toUpperCase();
		when(identityClient.getEntitlement(roleId)).thenReturn(null);
		when(identityClient.getEntitlement(roleName)).thenReturn(null);
		EntiltlemetDetailsDto entitlement = new EntiltlemetDetailsDto();
		entitlement.setEntitlementId(roleId);
		when(identityClient.createEntitlement(any(CreateEntitlementRequestDto.class))).thenReturn(entitlement);
		when(identityClient.AssignEntitlementToRole(roleId, roleId))
				.thenReturn(assignmentStatuses[0], Arrays.copyOfRange(assignmentStatuses, 1, assignmentStatuses.length));
	}

	private CreateRoleRequestVO roleRequest(String roleName) {
		Map<String, Object> data = Map.of("roleName", roleName, "isDynamic", false, "agreementVersion", "DRAFT-1.0");
		return new ObjectMapper().convertValue(Map.of("data", data), CreateRoleRequestVO.class);
	}

	private CreatedByVO requestUser() {
		CreatedByVO requestUser = new CreatedByVO();
		requestUser.setId("alice");
		return requestUser;
	}

	private CreateRoleResponseDto existingAliceRole(String roleId) {
		CreateRoleResponseDto existingRole = new CreateRoleResponseDto();
		existingRole.setId(roleId);
		return existingRole;
	}

	private AuthoriserRolesNsql roleWithOwner(String roleId, String ownerId) {
		UserDetails owner = new UserDetails();
		owner.setId(ownerId);
		AuthoriserRoleDeatils roleDetails = new AuthoriserRoleDeatils();
		roleDetails.setOwnerDetails(List.of(owner));
		return new AuthoriserRolesNsql(roleId, roleDetails);
	}

	private void setField(String name, Object value) throws ReflectiveOperationException {
		Field field = BaseFabricWorkspaceService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(service, value);
	}
}
