package com.daimler.data.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.daimler.data.application.auth.PatPermissions;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;

class ExternalCodespaceControllerTest {
	private UserStore userStore;
	private WorkspaceController workspaceController;
	private ExternalCodespaceController controller;

	@BeforeEach
	void setUp() {
		userStore = new UserStore();
		userStore.setUserInfo(UserStore.UserInfo.builder().id("creator").build());
		workspaceController = mock(WorkspaceController.class);
		controller = new ExternalCodespaceController(null, workspaceController, userStore);
	}

	@Test
	void stagingTokenCannotDeployToProduction() throws Exception {
		userStore.setPatPermissions(List.of(PatPermissions.DEPLOY_STAGING));
		ManageDeployRequestDto request = requestWithTarget("prod");

		ResponseEntity<?> response = controller.deployCodespace("workspace-1", request);

		assertEquals(403, response.getStatusCodeValue());
		verify(workspaceController, never()).deployWorkspaceProject("workspace-1", request);
	}

	@Test
	void stagingTokenCanDelegateStagingDeployment() throws Exception {
		userStore.setPatPermissions(List.of(PatPermissions.DEPLOY_STAGING));
		ManageDeployRequestDto request = requestWithTarget("int");
		ResponseEntity<GenericMessage> expected = ResponseEntity.ok(new GenericMessage("ok"));
		doReturn(expected).when(workspaceController).deployWorkspaceProject("workspace-1", request);

		ResponseEntity<?> response = controller.deployCodespace("workspace-1", request);

		assertEquals(expected, response);
		verify(workspaceController).deployWorkspaceProject("workspace-1", request);
	}

	private ManageDeployRequestDto requestWithTarget(String target) throws Exception {
		ManageDeployRequestDto request = mock(ManageDeployRequestDto.class);
		Method getter = ManageDeployRequestDto.class.getMethod("getTargetEnvironment");
		Object value = Arrays.stream(getter.getReturnType().getEnumConstants())
				.filter(candidate -> ((Enum<?>) candidate).name().equalsIgnoreCase(target))
				.findFirst().orElseThrow();
		doReturn(value).when(request).getTargetEnvironment();
		return request;
	}
}
