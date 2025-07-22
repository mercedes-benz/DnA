package com.daimler.data.service.catalogManagement;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class BaseFabricCatalogManagementService extends BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricCatalogManagementService{
	
	@Autowired
	private FabricWorkspaceCustomRepository customRepo;
	
	@Autowired
	private FabricWorkspaceRepository jpaRepo;

	private FabricWorkspaceAssembler assembler;

	public BaseFabricCatalogManagementService() {
		super();
	}


	public GenericMessage publishCatalogMetaData(PublishCatalogRequestVO request){
		GenericMessage response = new GenericMessage();
		
		return response;
	}


}
