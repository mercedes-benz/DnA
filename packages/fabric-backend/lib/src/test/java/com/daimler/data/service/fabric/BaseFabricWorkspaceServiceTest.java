package com.daimler.data.service.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.daimler.data.application.client.AuthoriserClient;
import com.daimler.data.application.client.AzureManagementClient;
import com.daimler.data.application.client.CodeServerClient;
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.assembler.GenericAssembler;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.db.entities.AuthoriserRolesNsql;
import com.daimler.data.db.json.AuthoriserRoleDeatils;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.roles.AuthoriserRolesRepository;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.BaseCommonService;
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

	@Mock
	private FabricWorkspaceClient fabricWorkspaceClient;

	@Mock
	private AzureManagementClient azureManagementClient;

	@Mock
	private GenericAssembler<Object, Object> baseAssembler;

	@Mock
	private JpaRepository<Object, String> baseJpaRepo;

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
		setField("defaultFolders", new String[0]);
		setField("fabricCapacityId", "default-capacity");
		setField("fabricCapacityName", "Default Capacity");
		setField("powerbiCapacityId", "powerbi-capacity");
		setField("powerbiCapacityName", "PowerBI Capacity");
		setField("capacityRegion", "West Europe");
		setField("capacitySku", "F64");
		setField("capacityState", "Active");
		setBaseField("assembler", baseAssembler);
		setBaseField("jpaRepo", baseJpaRepo);
		setBaseField("customRepo", customRepo);
	}

	@Test
	void technicalUserCreationContinuesAfterAddUserFailureAndAssignsDefaultCapacity() {
		FabricWorkspaceVO vo = workspaceRequest("TE12345");
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(createdWorkspace(null));
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(failedMessage());
		when(fabricWorkspaceClient.assignCapacity("ws-1", false)).thenReturn(new ErrorResponseDto());

		ResponseEntity<FabricWorkspaceResponseVO> response = service.createWorkspace(vo);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		verify(fabricWorkspaceClient, never()).deleteWorkspace(any());
		verify(fabricWorkspaceClient).assignCapacity("ws-1", false);
		CapacityVO capacity = savedWorkspace().getCapacity();
		assertEquals("default-capacity", capacity.getId());
		assertEquals("Default Capacity", capacity.getName());
		assertEquals("West Europe", capacity.getRegion());
		assertEquals("F64", capacity.getSku());
		assertEquals("Active", capacity.getState());
		assertTrue(response.getBody().getResponses().getWarnings().stream()
				.anyMatch(w -> w.getMessage().contains("Failed to add owner")));
	}

	@Test
	void technicalUserCreatorIdIsTreatedAsTechnicalRequest() {
		FabricWorkspaceVO vo = workspaceRequest(null);
		vo.getCreatedBy().setId("TECHUSER");
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(createdWorkspace(null));
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(null);
		when(fabricWorkspaceClient.assignCapacity("ws-1", false)).thenReturn(new ErrorResponseDto());

		ResponseEntity<FabricWorkspaceResponseVO> response = service.createWorkspace(vo);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		verify(fabricWorkspaceClient).assignCapacity("ws-1", false);
		assertEquals("default-capacity", savedWorkspace().getCapacity().getId());
	}

	@Test
	void nonTechnicalUserAddUserFailureStillRollsBack() {
		FabricWorkspaceVO vo = workspaceRequest(null);
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(createdWorkspace(null));
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(failedMessage());
		when(fabricWorkspaceClient.deleteWorkspace("ws-1")).thenReturn(null);

		ResponseEntity<FabricWorkspaceResponseVO> response = service.createWorkspace(vo);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		verify(fabricWorkspaceClient).deleteWorkspace("ws-1");
		verify(fabricWorkspaceClient, never()).assignCapacity(any(), eq(false));
	}

	@Test
	void anyAssignCapacityErrorStoresNoCapacityAndWarns() {
		FabricWorkspaceVO vo = workspaceRequest(null);
		ErrorResponseDto assignError = new ErrorResponseDto();
		assignError.setErrorCode("Failed");
		assignError.setMessage("Forbidden");
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(createdWorkspace(null));
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(successMessage());
		when(fabricWorkspaceClient.assignCapacity("ws-1", false)).thenReturn(assignError);

		ResponseEntity<FabricWorkspaceResponseVO> response = service.createWorkspace(vo);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNull(savedWorkspace().getCapacity());
		assertTrue(response.getBody().getResponses().getWarnings().stream()
				.anyMatch(w -> w.getMessage().startsWith("Failed to assign capacity")));
	}

	@Test
	void technicalUserRequestedCapacityIsUsedAndDetailsFetched() {
		FabricWorkspaceVO vo = workspaceRequest("TE12345");
		CapacityVO requested = new CapacityVO();
		requested.setId("spire-capacity");
		vo.setCapacity(requested);
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(createdWorkspace("spire-capacity"));
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(successMessage());
		when(fabricWorkspaceClient.getCapacityDetails("spire-capacity")).thenReturn(capacityDetails("spire-capacity"));

		service.createWorkspace(vo);

		ArgumentCaptor<CreateWorkspaceDto> createCaptor = ArgumentCaptor.forClass(CreateWorkspaceDto.class);
		verify(fabricWorkspaceClient).createWorkspace(createCaptor.capture());
		assertEquals("spire-capacity", createCaptor.getValue().getCapacityId());
		verify(fabricWorkspaceClient, never()).assignCapacity(any(), eq(false));
		CapacityVO capacity = savedWorkspace().getCapacity();
		assertEquals("spire-capacity", capacity.getId());
		assertEquals("Spire Capacity", capacity.getName());
		assertEquals("North Europe", capacity.getRegion());
		assertEquals("F8", capacity.getSku());
		assertEquals("Active", capacity.getState());
	}

	@Test
	void technicalUserAdoptingExistingWorkspaceKeepsItsCapacity() {
		FabricWorkspaceVO vo = workspaceRequest("TE12345");
		WorkspaceDetailDto conflict = new WorkspaceDetailDto();
		conflict.setErrorCode("409");
		conflict.setMessage("WorkspaceNameAlreadyExists");
		WorkspaceDetailDto existing = new WorkspaceDetailDto();
		existing.setId("ws-1");
		existing.setDisplayName("ws-name");
		existing.setCapacityId("existing-capacity");
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(conflict);
		when(fabricWorkspaceClient.getWorkspaceDetailsByName("ws-name")).thenReturn(existing);
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(successMessage());
		when(fabricWorkspaceClient.getCapacityDetails("existing-capacity")).thenReturn(capacityDetails("existing-capacity"));

		ResponseEntity<FabricWorkspaceResponseVO> response = service.createWorkspace(vo);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		verify(fabricWorkspaceClient, never()).assignCapacity(any(), eq(false));
		assertEquals("existing-capacity", savedWorkspace().getCapacity().getId());
		assertEquals("Spire Capacity", savedWorkspace().getCapacity().getName());
	}

	@Test
	void technicalUserAdoptingWorkspaceWithoutCapacityGetsDefaultCapacity() {
		FabricWorkspaceVO vo = workspaceRequest("TE12345");
		WorkspaceDetailDto conflict = new WorkspaceDetailDto();
		conflict.setErrorCode("409");
		WorkspaceDetailDto existing = new WorkspaceDetailDto();
		existing.setId("ws-1");
		existing.setDisplayName("ws-name");
		when(fabricWorkspaceClient.createWorkspace(any(CreateWorkspaceDto.class))).thenReturn(conflict);
		when(fabricWorkspaceClient.getWorkspaceDetailsByName("ws-name")).thenReturn(existing);
		when(fabricWorkspaceClient.addUser("ws-1", "owner@example.com")).thenReturn(successMessage());
		when(fabricWorkspaceClient.assignCapacity("ws-1", false)).thenReturn(new ErrorResponseDto());

		service.createWorkspace(vo);

		verify(fabricWorkspaceClient).assignCapacity("ws-1", false);
		assertEquals("default-capacity", savedWorkspace().getCapacity().getId());
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
		stubEntitlement("dna_test", HttpStatus.OK);

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
		stubEntitlement("dna_test", HttpStatus.OK);

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
		setField("aliceRoleAgreementUrl", "https://alice.example.com/agreement");
		when(assembler.toUserDetails(requestUser)).thenReturn(new UserDetails());

		service.saveCreatedRoleDetails("role-id", requestUser, false);

		ArgumentCaptor<AuthoriserRolesNsql> roleCaptor = ArgumentCaptor.forClass(AuthoriserRolesNsql.class);
		verify(rolesJpaRepo).save(roleCaptor.capture());
		assertEquals(Boolean.TRUE, roleCaptor.getValue().getData().getAgreementAccepted());
		assertEquals("https://alice.example.com/agreement", roleCaptor.getValue().getData().getAgreementUrl());
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
		Map<String, Object> data = Map.of("roleName", roleName, "isDynamic", false);
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

	private FabricWorkspaceVO workspaceRequest(String initiatedBy) {
		CreatedByVO owner = new CreatedByVO();
		owner.setId("owner1");
		owner.setEmail("owner@example.com");
		FabricWorkspaceVO vo = new FabricWorkspaceVO();
		vo.setName("ws-name");
		vo.setCreatedBy(owner);
		vo.setInitiatedBy(initiatedBy);
		return vo;
	}

	private WorkspaceDetailDto createdWorkspace(String capacityId) {
		WorkspaceDetailDto created = new WorkspaceDetailDto();
		created.setId("ws-1");
		created.setDisplayName("ws-name");
		created.setCapacityId(capacityId);
		return created;
	}

	private CapacityVO capacityDetails(String id) {
		CapacityVO details = new CapacityVO();
		details.setId(id);
		details.setName("Spire Capacity");
		details.setRegion("North Europe");
		details.setSku("F8");
		details.setState("Active");
		return details;
	}

	private GenericMessage successMessage() {
		GenericMessage message = new GenericMessage();
		message.setSuccess("SUCCESS");
		return message;
	}

	private GenericMessage failedMessage() {
		GenericMessage message = new GenericMessage();
		message.setSuccess("FAILED");
		return message;
	}

	private FabricWorkspaceVO savedWorkspace() {
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(baseAssembler).toEntity(captor.capture());
		return (FabricWorkspaceVO) captor.getValue();
	}

	private void setBaseField(String name, Object value) throws ReflectiveOperationException {
		Field field = BaseCommonService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(service, value);
	}

	private void setField(String name, Object value) throws ReflectiveOperationException {
		Field field = BaseFabricWorkspaceService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(service, value);
	}
}
