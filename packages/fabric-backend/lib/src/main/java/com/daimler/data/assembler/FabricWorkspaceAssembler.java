package com.daimler.data.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.Capacity;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;

@Component
public class FabricWorkspaceAssembler implements GenericAssembler<FabricWorkspaceVO, FabricWorkspaceNsql> {

	@Override
	public FabricWorkspaceVO toVo(FabricWorkspaceNsql entity) {
		FabricWorkspaceVO vo = null;
		if(entity!=null) {
			vo = new FabricWorkspaceVO();
			vo.setId(entity.getId());
			FabricWorkspace data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				Capacity capacity = data.getCapacity();
				CapacityVO capacityVO = new CapacityVO();
				if(capacity != null) {
					BeanUtils.copyProperties(capacity, capacityVO);
				}
				vo.setCapacity(capacityVO);
				
				UserDetails creator = data.getCreatedBy();
				CreatedByVO createdByVO = new CreatedByVO();
				if(creator!=null) {
					BeanUtils.copyProperties(creator, createdByVO);
				}
				vo.setCreatedBy(createdByVO);
				vo.setHasPii(data.getHasPii());
			}
		}
		return vo;
	}

	@Override
	public FabricWorkspaceNsql toEntity(FabricWorkspaceVO vo) {
		FabricWorkspaceNsql entity = null;
		if(vo!=null) {
			entity = new FabricWorkspaceNsql();
			entity.setId(vo.getId());
			FabricWorkspace data = new FabricWorkspace();
			BeanUtils.copyProperties(vo, data);
			CapacityVO capacityVO = vo.getCapacity();
			Capacity capacity = new Capacity();
			if(capacityVO!=null) {
				BeanUtils.copyProperties(capacityVO, capacity);
			}
			data.setCapacity(capacity);
			UserDetails createdBy = new UserDetails();
			CreatedByVO createdByVO = vo.getCreatedBy();
			if(createdByVO!=null) {
				BeanUtils.copyProperties(createdByVO, createdBy);
			}
			data.setCreatedBy(createdBy);
			data.setHasPii(vo.isHasPii());
			entity.setData(data);
		}
		return entity;
	}

}
