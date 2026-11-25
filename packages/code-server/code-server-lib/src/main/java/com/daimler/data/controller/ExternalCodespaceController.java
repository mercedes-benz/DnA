package com.daimler.data.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.application.annotation.RequiresPatAuthorization;
import com.daimler.data.application.auth.PatPermissions;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.pat.ExternalCodespaceInfoVO;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;
import com.daimler.data.service.pat.ExternalCodespaceResult;
import com.daimler.data.service.pat.ExternalCodespaceService;

@RestController
@RequestMapping("/external/v1")
@Slf4j
public class ExternalCodespaceController {
	private final ExternalCodespaceService externalCodespaceService;
	private final UserStore userStore;

	public ExternalCodespaceController(ExternalCodespaceService externalCodespaceService, UserStore userStore) {
		this.externalCodespaceService = externalCodespaceService;
		this.userStore = userStore;
	}

	@GetMapping("/codespaces/{id}")
	@RequiresPatAuthorization(anyOf = PatPermissions.READ)
	public ResponseEntity<?> getCodespace(@PathVariable("id") String id) {
		String userId = userStore.getUserInfo().getId();
		try {
			ExternalCodespaceResult result = externalCodespaceService.getCodespace(userId,
					userStore.getPatCodeSpaceProjectNames(), id);
			switch (result.getStatus()) {
			case NOT_FOUND:
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(errorMessage("Codespace not found: no codespace exists with id '" + id + "'"));
			case FORBIDDEN:
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(errorMessage("Forbidden: this token is not authorized to access codespace '" + id
								+ "'. The token is scoped to specific projects and does not cover this codespace"));
			default:
				log.info("External PAT getCodespace succeeded, userId={} workspaceId={}", userId, id);
				return ResponseEntity.ok(result.getData());
			}
		} catch (Exception e) {
			log.error("External PAT getCodespace failed, userId={} workspaceId={}", userId, id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(errorMessage("Unable to retrieve codespace"));
		}
	}

	@GetMapping("/codespaces")
	@RequiresPatAuthorization(anyOf = PatPermissions.READ)
	public ResponseEntity<?> getAllCodespaces() {
		String userId = userStore.getUserInfo().getId();
		try {
			List<ExternalCodespaceInfoVO> codespaces = externalCodespaceService.getAllCodespaces(userId,
					userStore.getPatCodeSpaceProjectNames());
			log.info("External PAT getAllCodespaces succeeded, userId={} count={}", userId, codespaces.size());
			return ResponseEntity.ok(codespaces);
		} catch (Exception e) {
			log.error("External PAT getAllCodespaces failed, userId={}", userId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(errorMessage("Unable to retrieve codespaces"));
		}
	}

	@PostMapping("/codespaces/{id}/deploy")
	@RequiresPatAuthorization(anyOf = { PatPermissions.DEPLOY_STAGING, PatPermissions.DEPLOY_PRODUCTION })
	public ResponseEntity<?> deployCodespace(@PathVariable("id") String id,
			@RequestBody ManageDeployRequestDto deployRequestDto) {
		String userId = userStore.getUserInfo() == null ? null : userStore.getUserInfo().getId();
		return externalCodespaceService.deployCodespace(userId, userStore.getPatPermissions(),
				userStore.getPatCodeSpaceProjectNames(), id, deployRequestDto);
	}

	private GenericMessage errorMessage(String message) {
		GenericMessage response = new GenericMessage();
		response.addErrors(new MessageDescription(message));
		return response;
	}
}
