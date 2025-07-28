package com.daimler.data.service.catalogManagement;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.CommonService;


public interface FabricCatalogManagementService extends CommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> {

	GenericMessage publishCatalogMetaData(PublishCatalogRequestVO request, FabricWorkspaceVO existingFabricWorkspace);
	FabricCatalogMetadataVO getCatalogMetadata(String serviceName);
}
