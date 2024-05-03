package com.daimler.data.service.forecast;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.CommonService;

public interface FabricWorkspaceService extends CommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> {

	Long getCount(String user);

	List<FabricWorkspaceVO> getAll(int limit, int offset, String user);

	ResponseEntity<FabricWorkspaceResponseVO>  createWorkspace(FabricWorkspaceVO vo);
	
	GenericMessage delete(String id);

	FabricWorkspaceVO updateFabricProject(FabricWorkspaceVO existingFabricWorkspace);

}
