package com.daimler.data.assembler;

import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.jsonb.TrinoDataLakeProject;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;

@Component
public class TrinoDataLakeAssembler  implements GenericAssembler<TrinoDataLakeProjectVO, TrinoDataLakeNsql>{

	
	@Override
	public TrinoDataLakeProjectVO toVo(TrinoDataLakeNsql entity) {
		TrinoDataLakeProjectVO datalakeProjectVO = null;
		if (Objects.nonNull(entity)) {
			datalakeProjectVO = new TrinoDataLakeProjectVO();
			datalakeProjectVO.setId(entity.getId());
			TrinoDataLakeProject projectDetails = entity.getData();
			if(projectDetails!=null) {
				BeanUtils.copyProperties(projectDetails, datalakeProjectVO);
			}
		}
		return datalakeProjectVO;
	}

	@Override
	public TrinoDataLakeNsql toEntity(TrinoDataLakeProjectVO vo) {
		TrinoDataLakeNsql entity = null;
		if(vo!=null) {
			entity = new TrinoDataLakeNsql();
			TrinoDataLakeProject projectDetails = new TrinoDataLakeProject();
			BeanUtils.copyProperties(vo,projectDetails);
			entity.setId(vo.getId());
			entity.setData(projectDetails);
		}
		return entity;
	}
	
	
}
