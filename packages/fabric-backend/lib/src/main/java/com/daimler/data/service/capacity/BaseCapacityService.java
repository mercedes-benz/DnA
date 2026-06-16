package com.daimler.data.service.capacity;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.CapacityAssembler;
import com.daimler.data.db.entities.CapacityNsql;
import com.daimler.data.db.repo.capacity.CapacityRepository;
import com.daimler.data.dto.adaProjects.CapacityVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class BaseCapacityService extends BaseCommonService<CapacityVO, CapacityNsql, String> implements CapacityService {

    @Autowired
    private CapacityRepository capacityRepository;

    @Autowired 
    private CapacityAssembler capacityAssembler;

    @Override
    public CapacityVO getCapacityByRegion(String region) {
        if(region == null || region.isEmpty()) {
            throw new IllegalArgumentException("Region must not be null or empty");
        }
        CapacityNsql capacityNsql = capacityRepository.findById(region.trim().toLowerCase()).orElse(null);
        if (capacityNsql == null) {
            return null;
        }
        return capacityAssembler.toVo(capacityNsql);
    }

    @Override
    public CapacityVO createOrUpdateCapacity(CapacityVO capacityVO, String region) {
        if (capacityVO == null) {
            throw new IllegalArgumentException("CapacityVO must not be null");
        }
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region must not be null or empty");
        }
        log.info("Creating or updating capacity for region: {}", region);
        CapacityNsql existing = capacityRepository.findById(region.trim().toLowerCase()).orElse(null);
        if (existing != null) {
            log.info("Existing capacity found for region: {}, updating it", region);
            existing.getData().setId(capacityVO.getId());
            existing.getData().setRegion(capacityVO.getRegion());
            existing.getData().setName(capacityVO.getName());
            existing.getData().setState(capacityVO.getState());
            existing.getData().setSku(capacityVO.getSku());
            existing.getData().setModifiedOn(new Date());
            CapacityNsql updated = capacityRepository.save(existing);
            log.info("Successfully updated capacity for region: {}", region);
            return capacityAssembler.toVo(updated);
        }
        CapacityNsql entity = capacityAssembler.toEntity(capacityVO);
        entity.getData().setModifiedOn(new Date());
        entity.getData().setCreatedOn(new Date());
        entity.setId(region.toLowerCase());
        CapacityNsql saved = capacityRepository.save(entity);
        log.info("Successfully saved capacity for region: {}", region);
        return capacityAssembler.toVo(saved);
    }

    @Override
    public CapacityVO deleteCapacityByRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region must not be null or empty");
        }
        log.info("Deleting capacity for region: {}", region);
        CapacityNsql existing = capacityRepository.findById(region.trim().toLowerCase()).orElse(null);
        if (existing == null) {
            log.warn("Capacity not found for region: {}", region);
            return null;
        }
        try {
            capacityRepository.delete(existing);
            log.info("Successfully deleted capacity for region: {}", region);
            return capacityAssembler.toVo(existing);
        } catch (Exception e) {
            log.error("Error deleting capacity for region: {}", region, e);
            return null;
        }
    }
    
}
