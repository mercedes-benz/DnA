package com.daimler.data.assembler;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.MarketingRoleNsql;
import com.daimler.data.db.jsonb.MarketingRole;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;

@Component
public class MarketingRoleAssembler implements GenericAssembler<MarketingRoleVO, MarketingRoleNsql>{

	@Override
	public MarketingRoleVO toVo(MarketingRoleNsql entity) {
		MarketingRoleVO marketingRoleVO = null;
		if(Objects.nonNull(entity)) {
			marketingRoleVO = new MarketingRoleVO();
			marketingRoleVO.setId(entity.getId());
			marketingRoleVO.setName(entity.getData().getName());
		}
		return marketingRoleVO;
	}

	@Override
	public MarketingRoleNsql toEntity(MarketingRoleVO vo) {
		MarketingRoleNsql marketingRoleNsql = null;
		if(Objects.nonNull(vo)) {
			marketingRoleNsql = new MarketingRoleNsql();
			MarketingRole marketingRole = new MarketingRole();
			marketingRole.setName(vo.getName());	
			if (vo.getId() != null)
				marketingRoleNsql.setId(vo.getId());
			marketingRoleNsql.setData(marketingRole);			
		}
		return marketingRoleNsql;
	}

}
