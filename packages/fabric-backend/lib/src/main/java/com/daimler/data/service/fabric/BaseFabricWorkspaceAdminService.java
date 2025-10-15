package com.daimler.data.service.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.service.common.BaseCommonService;
import java.util.stream.Collectors;
import com.daimler.data.util.ConstantsUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseFabricWorkspaceAdminService extends BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricWorkspaceAdminService{

    @Autowired
	private FabricWorkspaceCustomRepository customRepo;

    @Autowired
	private FabricWorkspaceAssembler assembler;
    
	@PersistenceContext
	protected EntityManager em;

	@Transactional
	public FabricWorkspacesCollectionVO getAllForFabricAdmin(int limit, int offset, String search) {
		FabricWorkspacesCollectionVO collectionVO = new FabricWorkspacesCollectionVO();
		GenericMessage message = new GenericMessage();
		List<FabricWorkspaceVO> vos = new ArrayList<>();
		int totalCount = 0;

		try {

			List<FabricWorkspaceNsql> entities = customRepo.getAllForAdmin( limit, offset, search);
			totalCount = (int) customRepo.getTotalCountForAdmin(search);

			vos = entities.stream()
					.map(entity -> {
						FabricWorkspaceVO vo = assembler.toVo(entity);
						vo.setUserRole(ConstantsUtility.PERMISSION_OWNER); 
						return vo;
					})
					.collect(Collectors.toList());

			message.setSuccess("SUCCESS");

		} catch (Exception e) {
			log.error("Error fetching Fabric workspaces for admin: searchTerm={}", search, e);
			message.setSuccess("ERROR");
			message.setErrors(List.of(new MessageDescription("Failed to fetch workspaces: " + e.getMessage())));
			vos = Collections.emptyList();
			totalCount = 0;
		}

		collectionVO.setRecords(vos);
		collectionVO.setTotalCount(totalCount);
		return collectionVO;
	}
}
