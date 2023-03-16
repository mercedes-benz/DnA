package com.daimler.data.service.marketingRoles;

import com.daimler.data.db.entities.MarketingRoleNsql;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;
import com.daimler.data.service.common.CommonService;

public interface MarketingRoleService extends CommonService<MarketingRoleVO, MarketingRoleNsql, String>{	

	boolean deleteRole(String id);

}
