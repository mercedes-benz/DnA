package com.daimler.data.service.fabric;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.dto.fabric.CreateEntitlementRequestDto;
import com.daimler.data.dto.fabric.CreateRoleRequestDto;
import com.daimler.data.dto.fabricWorkspace.EntitlementDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricLakehouseCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricShortcutsCollectionVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.DnaRoleCollectionVO;
import com.daimler.data.dto.fabricWorkspace.ShortcutCreateRequestVO;
import com.daimler.data.dto.fabricWorkspace.*;
import com.daimler.data.service.common.CommonService;

public interface FabricWorkspaceService extends CommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> {

	Long getCount(String user);

	ResponseEntity<FabricWorkspaceResponseVO>  createWorkspace(FabricWorkspaceVO vo, String secondaryRoleApproverId, String customEntitlementName);
	
	GenericMessage delete(String id, boolean skipDeleteFabricWorkspace);

	FabricWorkspaceVO updateFabricProject(FabricWorkspaceVO existingFabricWorkspace);

	CreateEntitlementRequestDto prepareEntitlementCreateRequestDto(String workspaceName,  String workspaceId, String permissionName);

	EntitlementDetailsVO callEntitlementCreate(String workspaceName,  String workspaceId, String permissionName);

	CreateRoleRequestDto prepareRoleCreateRequestDto(String workspaceName, String permissionName);

	RoleDetailsVO callRoleCreate(String workspaceName, String permissionName);

	GroupDetailsVO callGroupAssign(GroupDetailsVO existingGroupDetailsVO, String workspaceId, String permissionName);

	FabricWorkspaceStatusVO processWorkspaceUserManagement(FabricWorkspaceStatusVO currentStatus, String workspaceName,
			String creatorId, String workspaceId, String secondaryRoleApproverId, String customEntitlementName);

	FabricWorkspacesCollectionVO getAll(int limit, int offset, String user, List<String> allEntitlementsList, Boolean isTechnicalUser);
	
	GenericMessage requestRoles(FabricWorkspaceRoleRequestVO roleRequestVO, String userId, String authToken);
	
	FabricWorkspaceStatusVO fixBugsInWorkspaceUserManagement(FabricWorkspaceStatusVO currentStatus, String workspaceName,
			String creatorId, String workspaceId);

	List<GroupDetailsVO> autoProcessGroupsUsers(List<GroupDetailsVO> existingGroupsDetails, String workspaceName,
			String creatorId, String workspaceId);

	FabricWorkspacesCollectionVO getAllLov(int limit, int offset);

	GenericMessage deleteLakehouse(String id, String lakehouseId);

	GenericMessage createLakehouse(String id, @Valid FabricLakehouseCreateRequestVO createRequestVO);

	GenericMessage createLakehouseS3Shortcut(String id, String lakehouseId,
			@Valid ShortcutCreateRequestVO createRequestVO, String email);

	FabricShortcutsCollectionVO getLakehouseS3Shortcuts(String id, String lakehouseId);

	GenericMessage deleteLakehouseS3Shortcut(String id, String lakehouseId, String shortcutName);

	GenericMessage createGenericRole(CreateRoleRequestVO roleRequestVO, String userId);

	DnaRoleCollectionVO getAllUserDnaRoles(String id,String authToken);

}
