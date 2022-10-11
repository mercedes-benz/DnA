package com.daimler.data.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.PlanningITNsql;
import com.daimler.data.db.jsonb.PlanningIT;
import com.daimler.data.dto.planningit.PlanningITVO;

@Component
public class PlanningITAssembler implements GenericAssembler<PlanningITVO, PlanningITNsql>{
	
	@Override
	public PlanningITVO toVo(PlanningITNsql entity) {
		PlanningITVO vo = new PlanningITVO();
		if(entity!=null && entity.getData()!=null) {
			BeanUtils.copyProperties(entity.getData(), vo);
			vo.setId(entity.getId());
		}
		return vo;
	}

	@Override
	public PlanningITNsql toEntity(PlanningITVO vo) {
		PlanningITNsql entity = null;
		if(vo!=null) {
			entity = new PlanningITNsql();
			PlanningIT data = new PlanningIT();
			BeanUtils.copyProperties(vo, data);
			entity.setData(data);
			entity.setId(vo.getId());
		}
		return entity;
	}

}
