package com.daimler.data.service.fabric;

import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.service.common.CommonService;

public interface FabricWorkspaceAdminService extends CommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String>  {

    FabricWorkspacesCollectionVO getAllForFabricAdmin(int limit, int offset); 
}
