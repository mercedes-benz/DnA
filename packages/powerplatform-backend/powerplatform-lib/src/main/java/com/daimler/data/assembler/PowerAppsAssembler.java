package com.daimler.data.assembler;

import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.dto.powerapps.PowerAppVO;

@Component
public class PowerAppsAssembler implements GenericAssembler<PowerAppVO, PowerAppNsql> {

	@Override
	public PowerAppVO toVo(PowerAppNsql entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PowerAppNsql toEntity(PowerAppVO vo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
