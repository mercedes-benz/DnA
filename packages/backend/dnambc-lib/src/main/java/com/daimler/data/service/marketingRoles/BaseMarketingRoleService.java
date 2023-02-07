package com.daimler.data.service.marketingRoles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.db.entities.MarketingRoleNsql;
import com.daimler.data.db.repo.marketingRoles.MarketingRoleRepository;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;

@Service
public class BaseMarketingRoleService extends BaseCommonService<MarketingRoleVO, MarketingRoleNsql, String> implements MarketingRoleService{

	private static Logger LOGGER = LoggerFactory.getLogger(BaseMarketingRoleService.class);
	
	@Autowired
	private SolutionService solutionService;
	
	@Autowired
	private MarketingRoleRepository jpaRepo;
	
	@Override
	public boolean deleteRole(String roleIdToDelete) {
		LOGGER.debug("Fetching Role from db by id: {}",roleIdToDelete);
		MarketingRoleNsql roleEntity = jpaRepo.getOne(roleIdToDelete);
		String roleName = roleEntity.getData().getName();
		LOGGER.debug("Deleting Role:{} from solutions",roleName);
		solutionService.deleteTagForEachSolution(roleName, null, SolutionService.TAG_CATEGORY.MARKETINGROLE);
		return deleteById(roleIdToDelete);
	}
	

}
