package com.daimler.data.assembler;

import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CapacityNsql;
import com.daimler.data.db.json.Capacity;
import com.daimler.data.dto.adaProjects.CapacityVO;

@Component
public class CapacityAssembler implements GenericAssembler<CapacityVO, CapacityNsql> {

    @Override
    public CapacityVO toVo(CapacityNsql capacity) {
        if (capacity == null) return null;
        CapacityVO vo = new CapacityVO();
        vo.setId(capacity.getData().getId());
        vo.setName(capacity.getData().getName());
        vo.setRegion(capacity.getData().getRegion());
        vo.setSku(capacity.getData().getSku());
        vo.setState(capacity.getData().getState());
        vo.setCreatedOn(capacity.getData().getCreatedOn());
        vo.setModifiedOn(capacity.getData().getModifiedOn());
        return vo;
    }

    @Override
    public CapacityNsql toEntity(CapacityVO vo) {
        if (vo == null) return null;
        CapacityNsql capacity = new CapacityNsql();
        Capacity capacityData = new Capacity();
        capacityData.setId(vo.getId());
        capacityData.setName(vo.getName());
        capacityData.setRegion(vo.getRegion());
        capacityData.setSku(vo.getSku());
        capacityData.setState(vo.getState());
        capacity.setId(vo.getRegion().toLowerCase());
        capacity.setData(capacityData);
        return capacity;
    }
}
