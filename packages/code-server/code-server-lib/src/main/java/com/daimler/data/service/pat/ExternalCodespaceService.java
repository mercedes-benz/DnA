package com.daimler.data.service.pat;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.daimler.data.dto.pat.ExternalCodespaceInfoVO;
import com.daimler.data.dto.workspace.ManageDeployRequestDto;

/**
 * Business logic for the external (PAT-scoped) codespace endpoints.
 * Encapsulates access validation (does the calling PAT's user have access to
 * the requested codespace) and permission validation (does the PAT have the
 * required scope for the requested operation).
 */
public interface ExternalCodespaceService {

	ExternalCodespaceResult getCodespace(String userId, List<String> patCodeSpaceProjectNames, String workspaceId);

	List<ExternalCodespaceInfoVO> getAllCodespaces(String userId, List<String> patCodeSpaceProjectNames);

	ResponseEntity<?> deployCodespace(String userId, List<String> patPermissions,
			List<String> patCodeSpaceProjectNames, String workspaceId, ManageDeployRequestDto deployRequestDto);
}
