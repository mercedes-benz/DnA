package com.daimler.data.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import com.daimler.data.application.annotation.RequiresPatAuthorization;
import com.daimler.data.application.auth.PatPermissions;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.application.config.CodeServerVaultConfig;
import com.daimler.data.dto.pat.PatPayload;
import com.daimler.data.dto.workspace.CodeServerProjectDetailsVO;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.service.pat.PatCryptoService;
import com.daimler.data.service.workspace.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;

class PatAuthorizationInterceptorTest {
	private static final String KEY = java.util.Base64.getEncoder().encodeToString(new byte[32]);
	private PatCryptoService crypto;
	private CodeServerVaultConfig vaultConfig;
	private WorkspaceService workspaceService;
	private UserStore userStore;
	private PatAuthorizationInterceptor interceptor;

	@BeforeEach
	void setUp() {
		crypto = new PatCryptoService(KEY, new ObjectMapper());
		vaultConfig = mock(CodeServerVaultConfig.class);
		workspaceService = mock(WorkspaceService.class);
		userStore = new UserStore();
		interceptor = new PatAuthorizationInterceptor(crypto, vaultConfig, workspaceService, userStore,
				new ObjectMapper());
	}

	@Test
	void missingHeaderReturns401AndClearsIdentity() throws Exception {
		userStore.setUserInfo(UserInfo.builder().id("old").build());
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertFalse(interceptor.preHandle(request(), response, handler("read")));

		assertEquals(401, response.getStatus());
		assertNull(userStore.getUserInfo());
	}

	@Test
	void invalidTokenReturns401() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = request();
		request.addHeader("Authorization", "Bearer not-a-pat");

		assertFalse(interceptor.preHandle(request, response, handler("read")));

		assertEquals(401, response.getStatus());
		assertNull(userStore.getUserInfo());
	}

	@Test
	void vaultMismatchReturns401() throws Exception {
		when(vaultConfig.validatePatSecret("creator", 2, "secret")).thenReturn(false);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = authorizedRequest(payload("creator", 2, List.of(PatPermissions.READ)));

		assertFalse(interceptor.preHandle(request, response, handler("read")));

		assertEquals(401, response.getStatus());
		assertNull(userStore.getUserInfo());
	}

	@Test
	void readOnlyTokenCannotDeploy() throws Exception {
		when(vaultConfig.validatePatSecret("creator", 2, "secret")).thenReturn(true);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = authorizedRequest(payload("creator", 2, List.of(PatPermissions.READ)));

		assertFalse(interceptor.preHandle(request, response, handler("deploy")));

		assertEquals(403, response.getStatus());
		assertNull(userStore.getUserInfo());
	}

	@Test
	void projectOutsideTokenScopeReturns403() throws Exception {
		when(vaultConfig.validatePatSecret("creator", 2, "secret")).thenReturn(true);
		stubWorkspace("workspace-1", "another-project", true);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = authorizedRequest(
				payload("creator", 2, List.of(PatPermissions.READ), List.of("project")));
		request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "workspace-1"));

		assertFalse(interceptor.preHandle(request, response, handler("read")));

		assertEquals(403, response.getStatus());
		assertNull(userStore.getUserInfo());
	}

	@Test
	void creatorNoLongerCollaboratorReturns403() throws Exception {
		when(vaultConfig.validatePatSecret("creator", 2, "secret")).thenReturn(true);
		stubWorkspace("workspace-1", "project", false);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = authorizedRequest(payload("creator", 2, List.of(PatPermissions.READ)));
		request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "workspace-1"));

		assertFalse(interceptor.preHandle(request, response, handler("read")));

		assertEquals(403, response.getStatus());
		assertNull(userStore.getUserInfo());
	}

	@Test
	void successSetsCreatorIdentityWithoutRolesAndCompletionClearsIt() throws Exception {
		when(vaultConfig.validatePatSecret("creator", 2, "secret")).thenReturn(true);
		stubWorkspace("workspace-1", "project", true);
		MockHttpServletRequest request = authorizedRequest(payload("creator", 2, List.of(PatPermissions.READ)));
		request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "workspace-1"));

		assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler("read")));
		assertEquals("creator", userStore.getUserInfo().getId());
		assertTrue(userStore.getUserInfo().getUserRole().isEmpty());
		assertTrue(userStore.isPatRequest());
		assertInstanceOf(PatPayload.class, request.getAttribute(PatAuthorizationInterceptor.PAT_CONTEXT));

		interceptor.afterCompletion(request, new MockHttpServletResponse(), handler("read"), null);

		assertNull(userStore.getUserInfo());
		assertFalse(userStore.isPatRequest());
		assertEquals(0, userStore.getPatTokenVersion());
	}

	@Test
	void separateRequestStoresKeepConcurrentIdentitiesIsolated() throws Exception {
		when(vaultConfig.validatePatSecret("creator-a", 1, "secret")).thenReturn(true);
		when(vaultConfig.validatePatSecret("creator-b", 1, "secret")).thenReturn(true);
		CountDownLatch authorized = new CountDownLatch(2);
		CountDownLatch finish = new CountDownLatch(1);
		String[] identities = new String[2];
		Thread first = authThread("creator-a", 0, authorized, finish, identities);
		Thread second = authThread("creator-b", 1, authorized, finish, identities);

		first.start();
		second.start();
		assertTrue(authorized.await(5, TimeUnit.SECONDS));
		finish.countDown();
		first.join(5000);
		second.join(5000);

		assertEquals("creator-a", identities[0]);
		assertEquals("creator-b", identities[1]);
	}

	private Thread authThread(String userId, int index, CountDownLatch authorized, CountDownLatch finish,
			String[] identities) {
		return new Thread(() -> {
			try {
				UserStore requestStore = new UserStore();
				PatAuthorizationInterceptor requestInterceptor = new PatAuthorizationInterceptor(crypto, vaultConfig,
						workspaceService, requestStore, new ObjectMapper());
				MockHttpServletRequest request = authorizedRequest(payload(userId, 1, List.of(PatPermissions.READ)));
				HandlerMethod handler = handler("read");
				if (requestInterceptor.preHandle(request, new MockHttpServletResponse(), handler)) {
					identities[index] = requestStore.getUserInfo().getId();
				}
				authorized.countDown();
				finish.await(5, TimeUnit.SECONDS);
				requestInterceptor.afterCompletion(request, new MockHttpServletResponse(), handler, null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void stubWorkspace(String workspaceId, String projectName, boolean creatorIsMember) {
		CodeServerWorkspaceVO workspace = mock(CodeServerWorkspaceVO.class);
		CodeServerProjectDetailsVO details = mock(CodeServerProjectDetailsVO.class);
		UserInfoVO owner = mock(UserInfoVO.class);
		when(workspace.getWorkspaceId()).thenReturn(workspaceId);
		when(workspace.getProjectDetails()).thenReturn(details);
		when(details.getProjectName()).thenReturn(projectName);
		when(details.getProjectOwner()).thenReturn(creatorIsMember ? owner : null);
		when(owner.getId()).thenReturn(creatorIsMember ? "creator" : "someone-else");
		when(details.getProjectCollaborators()).thenReturn(Collections.emptyList());
		when(workspaceService.getById("creator", workspaceId)).thenReturn(workspace);
	}

	private PatPayload payload(String userId, int version, List<String> permissions) {
		return payload(userId, version, permissions, List.of("project"));
	}

	private PatPayload payload(String userId, int version, List<String> permissions, List<String> projectNames) {
		PatPayload payload = new PatPayload();
		payload.setUserId(userId);
		payload.setTokenVersion(version);
		payload.setSecret("secret");
		payload.setPermissions(permissions);
		payload.setCodeSpaceProjectNames(projectNames);
		return payload;
	}

	private MockHttpServletRequest authorizedRequest(PatPayload payload) {
		MockHttpServletRequest request = request();
		request.addHeader("Authorization", "Bearer " + crypto.encrypt(payload));
		return request;
	}

	private MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/external/v1/codespaces/workspace-1");
		return request;
	}

	private HandlerMethod handler(String method) throws NoSuchMethodException {
		return new HandlerMethod(new TestHandlers(), TestHandlers.class.getMethod(method));
	}

	private static class TestHandlers {
		@RequiresPatAuthorization(anyOf = PatPermissions.READ)
		public void read() {
		}

		@RequiresPatAuthorization(anyOf = { PatPermissions.DEPLOY_STAGING, PatPermissions.DEPLOY_PRODUCTION })
		public void deploy() {
		}
	}
}
