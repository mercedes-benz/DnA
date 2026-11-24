package com.daimler.data.service.pat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.daimler.data.application.auth.PatPermissions;
import com.daimler.data.controller.WorkspaceController;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.pat.ExternalCodespaceInfoVO;
import com.daimler.data.dto.pat.ExternalDeploymentInfo;
import com.daimler.data.dto.workspace.CodeServerDeploymentDetailsVO;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;
import com.daimler.data.service.workspace.WorkspaceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExternalCodespaceServiceImpl implements ExternalCodespaceService {

	private final WorkspaceService workspaceService;
	private final WorkspaceController workspaceController;

	public ExternalCodespaceServiceImpl(WorkspaceService workspaceService, WorkspaceController workspaceController) {
		this.workspaceService = workspaceService;
		this.workspaceController = workspaceController;
	}

	@Override
	public ExternalCodespaceResult getCodespace(String userId, List<String> patCodeSpaceProjectNames,
			String workspaceId) {
		CodeServerWorkspaceVO existing = workspaceService.findByWorkspaceId(workspaceId);
		if (existing == null) {
			log.warn("External PAT getCodespace rejected, userId={} workspaceId={} reason=not_found", userId,
					workspaceId);
			return ExternalCodespaceResult.notFound();
		}
		CodeServerWorkspaceVO workspace = workspaceService.getById(userId, workspaceId);
		if (workspace == null || workspace.getProjectDetails() == null) {
			log.warn("External PAT getCodespace denied, userId={} workspaceId={} reason=pat_not_authorized_for_codespace",
					userId, workspaceId);
			return ExternalCodespaceResult.forbidden();
		}
		if (!isInPatScope(workspace, patCodeSpaceProjectNames)) {
			log.warn("External PAT getCodespace denied, userId={} workspaceId={} reason=project_not_in_pat_scope",
					userId, workspaceId);
			return ExternalCodespaceResult.forbidden();
		}
		return ExternalCodespaceResult.found(toInfo(workspace));
	}

	@Override
	public List<ExternalCodespaceInfoVO> getAllCodespaces(String userId, List<String> patCodeSpaceProjectNames) {
		List<CodeServerWorkspaceVO> workspaces = workspaceService.getAll(userId, 0, Integer.MAX_VALUE);
		return workspaces.stream()
				.filter(workspace -> workspace.getProjectDetails() != null)
				.filter(workspace -> isInPatScope(workspace, patCodeSpaceProjectNames))
				.map(this::toInfo)
				.collect(Collectors.toList());
	}

	private boolean isInPatScope(CodeServerWorkspaceVO workspace, List<String> patCodeSpaceProjectNames) {
		String projectName = workspace.getProjectDetails().getProjectName();
		return projectName != null && patCodeSpaceProjectNames != null
				&& patCodeSpaceProjectNames.contains(projectName);
	}

	@Override
	public ResponseEntity<?> deployCodespace(String userId, List<String> patPermissions,
			List<String> patCodeSpaceProjectNames, String workspaceId, ManageDeployRequestDto deployRequestDto) {
		if (deployRequestDto == null || deployRequestDto.getTargetEnvironment() == null) {
			log.warn("External PAT deployCodespace rejected, userId={} workspaceId={} reason=invalid_request",
					userId, workspaceId);
			return forbidden("Forbidden");
		}
		String targetEnvironment = deployRequestDto.getTargetEnvironment().name();
		String requiredPermission;
		if ("int".equalsIgnoreCase(targetEnvironment)) {
			requiredPermission = PatPermissions.DEPLOY_STAGING;
		} else if ("prod".equalsIgnoreCase(targetEnvironment)) {
			requiredPermission = PatPermissions.DEPLOY_PRODUCTION;
		} else {
			log.warn(
					"External PAT deployCodespace rejected, userId={} workspaceId={} reason=unknown_target_environment",
					userId, workspaceId);
			return forbidden("Forbidden");
		}
		if (patPermissions == null || !patPermissions.contains(requiredPermission)) {
			log.warn(
					"External PAT deployCodespace denied, userId={} workspaceId={} targetEnvironment={} reason=insufficient_permission",
					userId, workspaceId, targetEnvironment);
			return forbidden("Forbidden");
		}

		ExternalCodespaceResult accessResult = getCodespace(userId, patCodeSpaceProjectNames, workspaceId);
		if (accessResult.getStatus() == ExternalCodespaceResult.Status.NOT_FOUND) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage("Codespace not found"));
		}
		if (accessResult.getStatus() == ExternalCodespaceResult.Status.FORBIDDEN) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(errorMessage("Forbidden: PAT does not have access to this codespace"));
		}

		log.info("External PAT deployCodespace triggered, userId={} workspaceId={} targetEnvironment={}", userId,
				workspaceId, targetEnvironment);
		return workspaceController.deployWorkspaceProject(workspaceId, deployRequestDto);
	}

	private ExternalCodespaceInfoVO toInfo(CodeServerWorkspaceVO workspace) {
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
		return info;
	}

	private ExternalDeploymentInfo deploymentInfo(CodeServerDeploymentDetailsVO deployment) {
		if (deployment == null) {
			return null;
		}
		ExternalDeploymentInfo info = new ExternalDeploymentInfo();
		info.setLastDeploymentStatus(deployment.getLastDeploymentStatus());
		info.setDeploymentUrl(deployment.getDeploymentUrl());
		info.setLastDeployedBranch(deployment.getLastDeployedBranch());
		return info;
	}

	private ResponseEntity<GenericMessage> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage(message));
	}

	private GenericMessage errorMessage(String message) {
		GenericMessage response = new GenericMessage();
		response.addErrors(new MessageDescription(message));
		return response;
	}
}
