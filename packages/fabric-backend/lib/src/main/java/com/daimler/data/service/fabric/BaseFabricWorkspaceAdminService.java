package com.daimler.data.service.fabric;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspacesCollectionVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;

@Service
public class BaseFabricWorkspaceAdminService extends BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricWorkspaceAdminService{

    @Autowired
	private FabricWorkspaceCustomRepository customRepo;

    @Autowired
	private FabricWorkspaceAssembler assembler;
    
    @Transactional
	public FabricWorkspacesCollectionVO getAllForFabricAdmin(int limit, int offset) {
		FabricWorkspacesCollectionVO collectionVO = new FabricWorkspacesCollectionVO();
		List<FabricWorkspaceVO> vos = new ArrayList<>();

		List<FabricWorkspaceNsql> allEntities = customRepo.findAll(0, 0);

		if (allEntities != null && !allEntities.isEmpty()) {
			for (FabricWorkspaceNsql entity : allEntities) {
				if (entity != null && !ConstantsUtility.DELETED_STATE.equalsIgnoreCase(entity.getData().getStatus().getState())) {
					FabricWorkspaceVO vo = assembler.toVo(entity);
					vo.setUserRole(ConstantsUtility.PERMISSION_OWNER);
					vos.add(vo);
				}
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
