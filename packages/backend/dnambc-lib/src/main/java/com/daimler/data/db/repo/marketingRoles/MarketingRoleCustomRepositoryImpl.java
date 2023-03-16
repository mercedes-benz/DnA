package com.daimler.data.db.repo.marketingRoles;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.MarketingRoleNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

@Repository
public class MarketingRoleCustomRepositoryImpl extends CommonDataRepositoryImpl<MarketingRoleNsql, String> 
		implements MarketingRoleCustomRepository {

}
