package com.daimler.data.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.daimler.data.db.entities.ADAProjectsNsql;
import com.daimler.data.db.json.ADAProjectDetails;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVOServices;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVOStakeholders;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVOTags;

import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ADAProjectsAssembler implements GenericAssembler<ADAProjectDetailsVO, ADAProjectsNsql> {

    @Override
    public ADAProjectsNsql toEntity(ADAProjectDetailsVO vo) {
        ADAProjectsNsql adaProjectsNsql = null;
        if (Objects.nonNull(vo)) {
            adaProjectsNsql = new ADAProjectsNsql();
            ADAProjectDetails details = new ADAProjectDetails();

            BeanUtils.copyProperties(vo, details);
            details.setActive(vo.isActive());
            
            // Handle Service object
            if (!CollectionUtils.isEmpty(vo.getServices())) {
                details.setServices(vo.getServices().stream()
                        .filter(Objects::nonNull)
                        .map(s -> {
                            ADAProjectDetails.Service service = new ADAProjectDetails.Service();
                            BeanUtils.copyProperties(s, service);
                            return service;
                        })
                        .collect(Collectors.toList()));
            }
            // Handle Stakeholders list
            if (!CollectionUtils.isEmpty(vo.getStakeholders())) {
                details.setStakeholders(vo.getStakeholders().stream()
                    .filter(Objects::nonNull)
                    .map(stakeholderVO -> {
                        ADAProjectDetails.Stakeholder stakeholder = new ADAProjectDetails.Stakeholder();
                        BeanUtils.copyProperties(stakeholderVO, stakeholder);
                        return stakeholder;
                    })
                    .collect(Collectors.toList()));
            }
            
            // Handle Tags list
            if (!CollectionUtils.isEmpty(vo.getTags())) {
                details.setTags(vo.getTags().stream()
                    .filter(Objects::nonNull)
                    .map(tagVO -> {
                        ADAProjectDetails.Tag tag = new ADAProjectDetails.Tag();
                        BeanUtils.copyProperties(tagVO, tag);
                        return tag;
                    })
                    .collect(Collectors.toList()));
            }
            
            adaProjectsNsql.setData(details);
        }
        return adaProjectsNsql;
    }

    @Override
    public ADAProjectDetailsVO toVo(ADAProjectsNsql entity) {
        ADAProjectDetailsVO vo = null;
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getData())) {
            vo = new ADAProjectDetailsVO();
            ADAProjectDetails details = entity.getData();

            vo.setId(entity.getId());
            BeanUtils.copyProperties(details, vo);
          //  vo.setADAID(details.getAdaID());
            vo.setActive(details.isActive());

            // Handle Service object
            if (!CollectionUtils.isEmpty(details.getServices())) {
                vo.setServices(details.getServices().stream()
                        .filter(Objects::nonNull)
                        .map(s -> {
                            ADAProjectDetailsVOServices serviceVO = new ADAProjectDetailsVOServices();
                            BeanUtils.copyProperties(s, serviceVO);
                            return serviceVO;
                        })
                        .collect(Collectors.toList()));
            }
            // Handle Stakeholders list
            if (!CollectionUtils.isEmpty(details.getStakeholders())) {
                vo.setStakeholders(details.getStakeholders().stream()
                    .filter(Objects::nonNull)
                    .map(stakeholder -> {
                        ADAProjectDetailsVOStakeholders stakeholderVO = new ADAProjectDetailsVOStakeholders();
                        BeanUtils.copyProperties(stakeholder, stakeholderVO);
                        return stakeholderVO;
                    })
                    .collect(Collectors.toList()));
            } else {
                vo.setStakeholders(Collections.emptyList());
            }
            
            // Handle Tags list
            if (!CollectionUtils.isEmpty(details.getTags())) {
                vo.setTags(details.getTags().stream()
                    .filter(Objects::nonNull)
                    .map(tag -> {
                        ADAProjectDetailsVOTags tagVO = new ADAProjectDetailsVOTags();
                        BeanUtils.copyProperties(tag, tagVO);
                        return tagVO;
                    })
                    .collect(Collectors.toList()));
            } else {
                vo.setTags(Collections.emptyList());
            }
        }
        return vo;
    }
}
