package com.daimler.data.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.dto.workspace.CodespaceProjectAdminEligibilityVO;
import com.daimler.data.dto.workspace.CreatedByVO;
import com.daimler.data.service.workspace.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class WorkspaceControllerTest {

	@Mock
	private WorkspaceService service;

	@Mock
	private UserStore userStore;

	@InjectMocks
	private WorkspaceController controller;

	@Test
	void missingUserIsForbiddenWithoutCheckingEligibility() {
		ResponseEntity<CodespaceProjectAdminEligibilityVO> response =
				controller.getCodespaceProjectAdminEligibility();

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertNull(response.getBody());
		verifyNoInteractions(service);
	}

	@Test
	void projectOwnerOrAdminReceivesTrueEligibility() {
		CreatedByVO user = new CreatedByVO();
		user.setId("alice");
		when(userStore.getUserInfo()).thenReturn(new UserStore.UserInfo());
		when(userStore.getVO()).thenReturn(user);
		when(service.isProjectOwnerOrAdmin("alice")).thenReturn(true);

		ResponseEntity<CodespaceProjectAdminEligibilityVO> response =
				controller.getCodespaceProjectAdminEligibility();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().isIsProjectOwnerOrAdmin());
		verify(service).isProjectOwnerOrAdmin("alice");
	}

	@Test
	void nonOwnerAndAdminReceivesFalseEligibility() {
		CreatedByVO user = new CreatedByVO();
		user.setId("alice");
		when(userStore.getUserInfo()).thenReturn(new UserStore.UserInfo());
		when(userStore.getVO()).thenReturn(user);
		when(service.isProjectOwnerOrAdmin("alice")).thenReturn(false);

		ResponseEntity<CodespaceProjectAdminEligibilityVO> response =
				controller.getCodespaceProjectAdminEligibility();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse(response.getBody().isIsProjectOwnerOrAdmin());
		verify(service).isProjectOwnerOrAdmin("alice");
	}
}
