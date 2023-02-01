package com.daimler.data.service.marketingRoles;

import org.springframework.stereotype.Service;

import com.daimler.data.db.entities.MarketingRoleNsql;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
public class BaseMarketingRoleService extends BaseCommonService<MarketingRoleVO, MarketingRoleNsql, String> implements MarketingRoleService{
	

}
