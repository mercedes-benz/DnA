package com.daimler.data.assembler;

import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;
import com.mb.dna.datalakehouse.db.jsonb.TrinoAccess;
import com.mb.dna.datalakehouse.dto.TrinoAccessVO;

@Component
public class TrinoAccessAssembler  implements GenericAssembler<TrinoAccessVO, TrinoAccessNsql>{

	
	@Override
	public TrinoAccessVO toVo(TrinoAccessNsql entity) {
		TrinoAccessVO accessVO = null;
		if (Objects.nonNull(entity)) {
			accessVO = new TrinoAccessVO();
			TrinoAccess accessDetails = entity.getData();
			if(accessDetails!=null) {
				BeanUtils.copyProperties(accessDetails, accessVO);
			}
		}
		return accessVO;
	}

	@Override
	public TrinoAccessNsql toEntity(TrinoAccessVO vo) {
		TrinoAccessNsql entity = null;
		if(vo!=null) {
			entity = new TrinoAccessNsql();
			TrinoAccess accessDetails = new TrinoAccess();
			BeanUtils.copyProperties(vo,accessDetails);
			entity.setData(accessDetails);
		}
		return entity;
	}
	
	
}
