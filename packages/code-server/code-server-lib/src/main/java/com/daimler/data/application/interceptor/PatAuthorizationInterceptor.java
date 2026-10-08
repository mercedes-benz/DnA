package com.daimler.data.application.interceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.daimler.data.application.annotation.RequiresPatAuthorization;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.application.config.CodeServerVaultConfig;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.pat.PatPayload;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.service.pat.PatCryptoService;
import com.daimler.data.service.workspace.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PatAuthorizationInterceptor implements HandlerInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatAuthorizationInterceptor.class);
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	public static final String PAT_CONTEXT = "PAT_CONTEXT";

	private final PatCryptoService patCryptoService;
	private final CodeServerVaultConfig vaultConfig;
	private final WorkspaceService workspaceService;
	private final UserStore userStore;
	private final ObjectMapper objectMapper;

	public PatAuthorizationInterceptor(PatCryptoService patCryptoService, CodeServerVaultConfig vaultConfig,
			WorkspaceService workspaceService, UserStore userStore, ObjectMapper objectMapper) {
		this.patCryptoService = patCryptoService;
		this.vaultConfig = vaultConfig;
		this.workspaceService = workspaceService;
		this.userStore = userStore;
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		userStore.clear();
		String userId = null;
		int tokenVersion = 0;
		boolean authorized = false;
		try {
			if (!(handler instanceof HandlerMethod)) {
				return deny(request, response, userId, tokenVersion, "404_HANDLER_NOT_FOUND",
						HttpServletResponse.SC_NOT_FOUND, "Not found");
			}

			Method method = ((HandlerMethod) handler).getMethod();
			RequiresPatAuthorization authorization = method.getAnnotation(RequiresPatAuthorization.class);
			if (authorization == null) {
				return deny(request, response, userId, tokenVersion, "403_HANDLER_NOT_ALLOWED",
						HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			}

			String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
			if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)
					|| authorizationHeader.substring(BEARER_PREFIX.length()).isBlank()) {
				return deny(request, response, userId, tokenVersion, "401_INVALID_AUTHORIZATION",
						HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			}

			Optional<PatPayload> decrypted = patCryptoService
					.decrypt(authorizationHeader.substring(BEARER_PREFIX.length()).trim());
			if (decrypted.isEmpty()) {
				return deny(request, response, userId, tokenVersion, "401_INVALID_TOKEN",
						HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			}
			PatPayload payload = decrypted.get();
			userId = payload.getUserId();
			tokenVersion = payload.getTokenVersion();
			if (!vaultConfig.validatePatSecret(userId, tokenVersion, payload.getSecret())) {
				return deny(request, response, userId, tokenVersion, "401_REVOKED_OR_INVALID_TOKEN",
						HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			}

			List<String> permissions = payload.getPermissions() == null
					? Collections.emptyList() : payload.getPermissions();
			boolean hasRequiredPermission = false;
			for (String requiredPermission : authorization.anyOf()) {
				if (permissions.contains(requiredPermission)) {
					hasRequiredPermission = true;
					break;
				}
			}
			if (!hasRequiredPermission) {
				return deny(request, response, userId, tokenVersion, "403_INSUFFICIENT_PERMISSION",
						HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			}

			if (!hasWorkspaceAccess(request, payload)) {
				return deny(request, response, userId, tokenVersion, "403_WORKSPACE_ACCESS_DENIED",
						HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			}

			UserInfo userInfo = UserInfo.builder().id(userId).userRole(Collections.emptyList()).build();
			userStore.setUserInfo(userInfo);
			userStore.setAuthType("PAT");
			userStore.setPatTokenVersion(tokenVersion);
			userStore.setPatPermissions(permissions);
			request.setAttribute(PAT_CONTEXT, payload);
			logOutcome(request, userId, tokenVersion, "ALLOWED");
			authorized = true;
			return true;
		} catch (Exception e) {
			return deny(request, response, userId, tokenVersion, "403_AUTHORIZATION_ERROR",
					HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} finally {
			if (!authorized) {
				userStore.clear();
			}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception) {
		userStore.clear();
	}

	private boolean hasWorkspaceAccess(HttpServletRequest request, PatPayload payload) {
		Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		if (!(pathVariables instanceof Map<?, ?>)) {
			return true;
		}
		Object workspaceId = ((Map<?, ?>) pathVariables).get("id");
		if (workspaceId == null) {
			return true;
		}
		try {
			CodeServerWorkspaceVO workspace = workspaceService.getById(payload.getUserId(), String.valueOf(workspaceId));
			if (workspace == null || workspace.getWorkspaceId() == null || workspace.getProjectDetails() == null) {
				return false;
			}
			String projectName = workspace.getProjectDetails().getProjectName();
			if (projectName == null || payload.getCodeSpaceProjectNames() == null
					|| !payload.getCodeSpaceProjectNames().contains(projectName)) {
				return false;
			}
			String creator = payload.getUserId();
			boolean owner = workspace.getProjectDetails().getProjectOwner() != null
					&& Objects.equals(creator, workspace.getProjectDetails().getProjectOwner().getId());
			boolean collaborator = workspace.getProjectDetails().getProjectCollaborators() != null
					&& workspace.getProjectDetails().getProjectCollaborators().stream()
							.anyMatch(user -> Objects.equals(creator, user.getId()));
			return owner || collaborator;
		} catch (Exception e) {
			return false;
		}
	}

	private void writeError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		GenericMessage errorMessage = new GenericMessage();
		errorMessage.addErrors(new MessageDescription(message));
		objectMapper.writeValue(response.getWriter(), errorMessage);
	}

	private boolean deny(HttpServletRequest request, HttpServletResponse response, String userId, int tokenVersion,
			String outcome, int status, String message) throws IOException {
		logOutcome(request, userId, tokenVersion, outcome);
		writeError(response, status, message);
		return false;
	}

	private void logOutcome(HttpServletRequest request, String userId, int tokenVersion, String outcome) {
		LOGGER.info("PAT request userId={} tokenVersion={} method={} uri={} outcome={}", userId, tokenVersion,
				request.getMethod(), request.getRequestURI(), outcome);
	}
}
