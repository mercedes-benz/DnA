package com.daimler.data.controller;

import java.util.List;

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
import com.daimler.data.dto.pat.ExternalDeploymentInfo;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;
import com.daimler.data.service.workspace.WorkspaceService;

@RestController
@RequestMapping("/external/v1")
public class ExternalCodespaceController {
	private final WorkspaceService workspaceService;
	private final WorkspaceController workspaceController;
	private final UserStore userStore;

	public ExternalCodespaceController(WorkspaceService workspaceService, WorkspaceController workspaceController,
			UserStore userStore) {
		this.workspaceService = workspaceService;
		this.workspaceController = workspaceController;
		this.userStore = userStore;
	}

	@GetMapping("/codespaces/{id}")
	@RequiresPatAuthorization(anyOf = PatPermissions.READ)
	public ResponseEntity<?> getCodespace(@PathVariable("id") String id) {
		try {
			CodeServerWorkspaceVO workspace = workspaceService.getById(userStore.getUserInfo().getId(), id);
			if (workspace == null || workspace.getProjectDetails() == null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage("Forbidden"));
			}
			ExternalCodespaceInfoVO info = new ExternalCodespaceInfoVO();
			info.setWorkspaceId(workspace.getWorkspaceId());
			info.setProjectName(workspace.getProjectDetails().getProjectName());
			info.setServerStatus(workspace.getServerStatus());
			if (workspace.getProjectDetails().getRecipeDetails() != null) {
				if (workspace.getProjectDetails().getRecipeDetails().getRecipeId() != null) {
					info.setRecipeId(workspace.getProjectDetails().getRecipeDetails().getRecipeId().toString());
				}
				if (workspace.getProjectDetails().getRecipeDetails().getCloudServiceProvider() != null) {
					info.setCloudServiceProvider(
							workspace.getProjectDetails().getRecipeDetails().getCloudServiceProvider().toString());
				}
			}
			info.setIntDeployment(deploymentInfo(workspace.getProjectDetails().getIntDeploymentDetails()));
			info.setProdDeployment(deploymentInfo(workspace.getProjectDetails().getProdDeploymentDetails()));
			return ResponseEntity.ok(info);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage("Unable to retrieve codespace"));
		}
	}

	@PostMapping("/codespaces/{id}/deploy")
	@RequiresPatAuthorization(anyOf = { PatPermissions.DEPLOY_STAGING, PatPermissions.DEPLOY_PRODUCTION })
	public ResponseEntity<?> deployCodespace(@PathVariable("id") String id,
			@RequestBody ManageDeployRequestDto deployRequestDto) {
		if (deployRequestDto == null || deployRequestDto.getTargetEnvironment() == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage("Forbidden"));
		}
		String targetEnvironment = deployRequestDto.getTargetEnvironment().name();
		String requiredPermission;
		if ("int".equalsIgnoreCase(targetEnvironment)) {
			requiredPermission = PatPermissions.DEPLOY_STAGING;
		} else if ("prod".equalsIgnoreCase(targetEnvironment)) {
			requiredPermission = PatPermissions.DEPLOY_PRODUCTION;
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage("Forbidden"));
		}
		List<String> permissions = userStore.getPatPermissions();
		if (permissions == null || !permissions.contains(requiredPermission)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage("Forbidden"));
		}
		return workspaceController.deployWorkspaceProject(id, deployRequestDto);
	}

	private ExternalDeploymentInfo deploymentInfo(
			com.daimler.data.dto.workspace.CodeServerDeploymentDetailsVO deployment) {
		if (deployment == null) {
			return null;
		}
		ExternalDeploymentInfo info = new ExternalDeploymentInfo();
		info.setLastDeploymentStatus(deployment.getLastDeploymentStatus());
		info.setDeploymentUrl(deployment.getDeploymentUrl());
		info.setLastDeployedBranch(deployment.getLastDeployedBranch());
		return info;
	}

	private GenericMessage errorMessage(String message) {
		GenericMessage response = new GenericMessage();
		response.addErrors(new MessageDescription(message));
		return response;
	}
}
