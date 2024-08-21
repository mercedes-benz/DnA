package com.daimler.data.service.fabric;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabricWorkspace.EntitlementDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceRoleRequestVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RoleDetailsVO;
import com.daimler.data.service.common.CommonService;

public interface FabricWorkspaceService extends CommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> {

	Long getCount(String user);

	ResponseEntity<FabricWorkspaceResponseVO>  createWorkspace(FabricWorkspaceVO vo);
	
	GenericMessage delete(String id);

	FabricWorkspaceVO updateFabricProject(FabricWorkspaceVO existingFabricWorkspace);

	CreateEntitlementRequestDto prepareEntitlementCreateRequestDto(String workspaceName,  String workspaceId, String permissionName);

	EntitlementDetailsVO callEntitlementCreate(String workspaceName,  String workspaceId, String permissionName);

	CreateRoleRequestDto prepareRoleCreateRequestDto(String workspaceName, String permissionName);

	RoleDetailsVO callRoleCreate(String workspaceName, String permissionName);

	GroupDetailsVO callGroupAssign(GroupDetailsVO existingGroupDetailsVO, String workspaceId, String permissionName);

	FabricWorkspaceStatusVO processWorkspaceUserManagement(FabricWorkspaceStatusVO currentStatus, String workspaceName,
			String creatorId, String workspaceId);

	FabricWorkspacesCollectionVO getAll(int limit, int offset, String user, List<String> allEntitlementsList);

	GenericMessage requestRoles(FabricWorkspaceRoleRequestVO roleRequestVO, String userId);

}
