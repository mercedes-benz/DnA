package com.daimler.data.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.application.auth.PatPermissions;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.config.CodeServerVaultConfig;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.pat.CreatePatRequest;
import com.daimler.data.dto.pat.CreatePatResponse;
import com.daimler.data.dto.pat.PatListResponse;
import com.daimler.data.dto.pat.PatPayload;
import com.daimler.data.dto.pat.PatVaultEntry;
import com.daimler.data.dto.pat.PatVaultListItem;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.service.pat.PatCryptoService;
import com.daimler.data.service.workspace.WorkspaceService;

@RestController
@RequestMapping("/api")
public class PersonalAccessTokenController {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonalAccessTokenController.class);
	private final UserStore userStore;
	private final WorkspaceService workspaceService;
	private final PatCryptoService patCryptoService;
	private final CodeServerVaultConfig vaultConfig;

	public PersonalAccessTokenController(UserStore userStore, WorkspaceService workspaceService,
			PatCryptoService patCryptoService, CodeServerVaultConfig vaultConfig) {
		this.userStore = userStore;
		this.workspaceService = workspaceService;
		this.patCryptoService = patCryptoService;
		this.vaultConfig = vaultConfig;
	}

	@PostMapping("/personal-access-tokens")
	public ResponseEntity<?> createPat(@RequestBody CreatePatRequest request) {
		String userId = currentUserId();
		if (userId == null || request == null || !validPermissions(request.getPermissions())
				|| request.getComment() != null && request.getComment().length() > 200) {
			return error(HttpStatus.BAD_REQUEST, "Invalid PAT request");
		}
		List<String> codeSpaceProjectNames;
		try {
			codeSpaceProjectNames = accessibleProjectNames(userId);
		} catch (Exception e) {
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create PAT");
		}
		if (codeSpaceProjectNames.isEmpty()) {
			return error(HttpStatus.BAD_REQUEST, "No accessible codespaces");
		}

		String createdAt = Instant.now().toString();
		String[] token = new String[1];
		try {
			PatVaultEntry entryTemplate = new PatVaultEntry();
			entryTemplate.setComment(request.getComment());
			entryTemplate.setCreatedAt(createdAt);
			entryTemplate.setCodeSpaceProjectNames(codeSpaceProjectNames);
			entryTemplate.setPermissions(new ArrayList<>(request.getPermissions()));
			int version = vaultConfig.createPatSecret(userId, nextVersion -> {
				String secret = patCryptoService.generateSecret();
				PatPayload payload = new PatPayload();
				payload.setUserId(userId);
				payload.setTokenVersion(nextVersion);
				payload.setSecret(secret);
				payload.setCodeSpaceProjectNames(codeSpaceProjectNames);
				payload.setPermissions(new ArrayList<>(request.getPermissions()));
				payload.setComment(request.getComment());
				payload.setCreatedAt(createdAt);
				payload.setExpiresAt(null);
				token[0] = patCryptoService.encrypt(payload);
				PatVaultEntry entry = new PatVaultEntry();
				entry.setSecret(secret);
				entry.setDisplaySuffix(token[0].substring(token[0].length() - 4));
				entry.setComment(entryTemplate.getComment());
				entry.setCreatedAt(entryTemplate.getCreatedAt());
				entry.setCodeSpaceProjectNames(entryTemplate.getCodeSpaceProjectNames());
				entry.setPermissions(entryTemplate.getPermissions());
				return entry;
			});
			LOGGER.info("Created PAT for userId={} version={}", userId, version);
			CreatePatResponse response = new CreatePatResponse(version, token[0],
					token[0].substring(token[0].length() - 4), new ArrayList<>(request.getPermissions()),
					request.getComment(), createdAt, codeSpaceProjectNames.size());
			return ResponseEntity.status(HttpStatus.CREATED).header("Cache-Control", "no-store").body(response);
		} catch (Exception e) {
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create PAT");
		}
	}

	@GetMapping("/personal-access-tokens")
	public ResponseEntity<?> listPats() {
		String userId = currentUserId();
		if (userId == null) {
			return error(HttpStatus.UNAUTHORIZED, "Unauthorized");
		}
		try {
			List<PatVaultListItem> items = vaultConfig.listPatSecrets(userId).stream()
					.sorted(Comparator.comparingInt(PatVaultListItem::getVersion).reversed())
					.collect(Collectors.toList());
			return ResponseEntity.ok(new PatListResponse(items, items.size()));
		} catch (Exception e) {
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to list PATs");
		}
	}

	@DeleteMapping("/personal-access-tokens/{version}")
	public ResponseEntity<?> revokePat(@PathVariable("version") int version) {
		String userId = currentUserId();
		if (userId == null) {
			return error(HttpStatus.UNAUTHORIZED, "Unauthorized");
		}
		try {
			if (!vaultConfig.deletePatSecret(userId, version)) {
				return error(HttpStatus.NOT_FOUND, "PAT not found");
			}
			LOGGER.info("Revoked PAT for userId={} version={}", userId, version);
			return ResponseEntity.ok(new GenericMessage("Revoked"));
		} catch (Exception e) {
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to revoke PAT");
		}
	}

	private String currentUserId() {
		return userStore.getUserInfo() == null ? null : userStore.getUserInfo().getId();
	}

	private boolean validPermissions(List<String> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return false;
		}
		Set<String> uniquePermissions = new LinkedHashSet<>(permissions);
		return uniquePermissions.size() == permissions.size()
				&& uniquePermissions.stream().allMatch(permission -> permission != null
						&& PatPermissions.ALL.contains(permission));
	}

	private List<String> accessibleProjectNames(String userId) {
		Set<String> projectNames = new LinkedHashSet<>();
		List<CodeServerWorkspaceVO> workspaces = workspaceService.getAll(userId, 0, 0);
		if (workspaces == null) {
			return new ArrayList<>();
		}
		for (CodeServerWorkspaceVO workspace : workspaces) {
			if (workspace == null || workspace.getProjectDetails() == null
					|| workspace.getProjectDetails().getProjectName() == null) {
				continue;
			}
			boolean owner = workspace.getProjectDetails().getProjectOwner() != null
					&& userId.equals(workspace.getProjectDetails().getProjectOwner().getId());
			boolean collaborator = workspace.getProjectDetails().getProjectCollaborators() != null
					&& workspace.getProjectDetails().getProjectCollaborators().stream()
							.anyMatch(user -> userId.equals(user.getId()));
			if (owner || collaborator) {
				projectNames.add(workspace.getProjectDetails().getProjectName());
			}
		}
		return new ArrayList<>(projectNames);
	}

	private ResponseEntity<GenericMessage> error(HttpStatus status, String message) {
		GenericMessage response = new GenericMessage();
		response.addErrors(new MessageDescription(message));
		return ResponseEntity.status(status).body(response);
	}
}
