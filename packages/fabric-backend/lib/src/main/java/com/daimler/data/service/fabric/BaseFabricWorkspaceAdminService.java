package com.daimler.data.service.fabric;

import java.util.ArrayList;
import java.util.List;

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
		List<FabricWorkspaceVO> vos = new ArrayList<>();

		List<FabricWorkspaceNsql> allEntities = customRepo.findAll(0, 0);

		if (allEntities != null && !allEntities.isEmpty()) {
			for (FabricWorkspaceNsql entity : allEntities) {
				if (entity != null
						&& !ConstantsUtility.DELETED_STATE.equalsIgnoreCase(entity.getData().getStatus().getState())) {
					FabricWorkspaceVO vo = assembler.toVo(entity);
					vo.setUserRole(ConstantsUtility.PERMISSION_OWNER);
					vos.add(vo);
				}
			}
		}
		if (search != null && !search.trim().isEmpty()) {
			try {
				String queryStr = "SELECT cast(id AS text), cast(data AS text) FROM fabric_workspace_nsql " +
						"WHERE lower(jsonb_extract_path_text(data, 'name')) LIKE :search";

				Query query = em.createNativeQuery(queryStr);
				query.setParameter("search", "%" + search.trim().toLowerCase() + "%");

				ObjectMapper mapper = new ObjectMapper();
				List<Object[]> results = query.getResultList();

				vos = results.stream().map(temp -> {
					FabricWorkspaceNsql entity = new FabricWorkspaceNsql();
					try {
						String jsonData = temp[1] != null ? temp[1].toString() : "";
						FabricWorkspace workspace = mapper.readValue(jsonData, FabricWorkspace.class);
						entity.setData(workspace);
					} catch (Exception e) {
						log.error("Failed while parsing workspace JSON: {}", e.getMessage());
					}
					String id = temp[0] != null ? temp[0].toString() : "";
					entity.setId(id);

					FabricWorkspaceVO vo = assembler.toVo(entity);
					vo.setUserRole(ConstantsUtility.PERMISSION_OWNER);
					return vo;
				}).collect(Collectors.toList());

			} catch (Exception e) {
				log.error("Error searching Fabric workspaces with search '{}': {}", search, e.getMessage());
			}
		}

		List<FabricWorkspaceVO> paginatedVOs = new ArrayList<>();
		int totalCount = 0;
		if (vos != null && !vos.isEmpty()) {
			totalCount = vos.size();
			int newOffset = offset > vos.size() ? 0 : offset;
			if (limit == 0) {
				limit = totalCount;
			}
			int newLimit = offset + limit > vos.size() ? vos.size() : offset + limit;
			paginatedVOs = vos.subList(newOffset, newLimit);
		}

		collectionVO.setRecords(paginatedVOs);
		collectionVO.setTotalCount(totalCount);
		return collectionVO;
	}
}
