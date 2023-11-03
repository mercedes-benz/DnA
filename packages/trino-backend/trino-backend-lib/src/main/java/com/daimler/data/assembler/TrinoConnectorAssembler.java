package com.daimler.data.assembler;

import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;
import com.mb.dna.datalakehouse.db.jsonb.TrinoConnector;
import com.mb.dna.datalakehouse.dto.TrinoConnectorVO;

@Component
public class TrinoConnectorAssembler  implements GenericAssembler<TrinoConnectorVO, TrinoConnectorNsql> {

	@Override
	public TrinoConnectorVO toVo(TrinoConnectorNsql entity) {
		TrinoConnectorVO trinoConnectorVO = null;
		if (Objects.nonNull(entity)) {
			trinoConnectorVO = new TrinoConnectorVO();
			trinoConnectorVO.setName(entity.getId());
			TrinoConnector trinoConnectors = entity.getData();
			if(trinoConnectors!=null) {
				BeanUtils.copyProperties(trinoConnectors, trinoConnectorVO);
			}
		}
		return trinoConnectorVO;
	}

	@Override
	public TrinoConnectorNsql toEntity(TrinoConnectorVO vo) {
		//will not be used, Not exposing APIs to change the data, data has to be changed through flyway update script
		return new TrinoConnectorNsql();
	}
}

