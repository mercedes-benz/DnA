package com.daimler.data.service.catalogManagement;

import java.util.List;

import java.util.List;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.dto.fabricCatalogManagement.LakehouseObjectsResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.TableMismatchResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataDetailsVO;
import com.daimler.data.dto.fabricCatalogManagement.LegalEntitiesResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.GroupStatusResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.CommonService;


public interface FabricCatalogManagementService extends CommonService<FabricCatalogMetadataDetailsVO, FabricCatalogMetadataNsql, String> {

	PublishCatalogResponseVO publishCatalogMetaData(PublishCatalogRequestVO request, FabricWorkspaceVO existingFabricWorkspace, boolean hasExistingPublish);
	PublishCatalogResponseVO getCatalogMetadata(String serviceName);
	PublishCatalogResponseVO updateCatalogMetaData(PublishCatalogRequestVO request, FabricWorkspaceVO existingFabricWorkspace);
	LakehouseObjectsResponseVO getLakehouseObjects(String workspaceId, String lakehouseId, String schemaName);
	TableMismatchResponseVO checkTableMismatch(String workspaceId, String lakehouseId, String serviceName);

	/**
	 * This method is fetech and retrun all the legal enteties from the genesis 
	 * @return List<LegalEntitiesResponseVO>
	 */
	List<LegalEntitiesResponseVO> getAllFabricLegalEntities(String queryString);

	/**
	 * method to trigger the ui-licious to add the workspace and groups to the lakehouse group in order to 
	 * provide the access to the lakehouse for the workspace users
	 */
	void addWorkspaceGropusToLakehouse(String workspaceId, String lakehouseId, String workspaceName, String lakehouseName, List<String> groupName, String ddxId);

	/**
	 * method to get the status of groups added to lakehouse through ui-licious
	 */
	List<GroupStatusResponseVO> getGroupsAssignmentStatus(String workspaceName, String workspaceId, String lakehouseName, String lakehouseId, List<String> groupName, String ddxId);
}
