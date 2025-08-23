package com.daimler.data.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.daimler.data.db.entities.ADAProjectsNsql;
import com.daimler.data.db.json.ADAProjectDetails;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVOService;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVOStakeholders;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVOTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

            // Handle Service object
            if (Objects.nonNull(vo.getService())) {
                ADAProjectDetails.Service service = details.new Service();
                service.setServiceName(vo.getService().getServiceName());
                service.setServiceQuantity(vo.getService().getServiceQuantity());
                details.setService(service);
            }
            
            // Handle Stakeholders list
            if (!CollectionUtils.isEmpty(vo.getStakeholders())) {
                details.setStakeholders(vo.getStakeholders().stream()
                    .filter(Objects::nonNull)
                    .map(stakeholderVO -> {
                        ADAProjectDetails.Stakeholder stakeholder = details.new Stakeholder();
                        stakeholder.setPosition(stakeholderVO.getPosition());
                        stakeholder.setUserID(stakeholderVO.getUserID());
                        return stakeholder;
                    })
                    .collect(Collectors.toList()));
            }
            
            // Handle Tags list
            if (!CollectionUtils.isEmpty(vo.getTags())) {
                details.setTags(vo.getTags().stream()
                    .filter(Objects::nonNull)
                    .map(tagVO -> {
                        ADAProjectDetails.Tag tag = details.new Tag();
                        tag.setDescription(tagVO.getDescription());
                        tag.setValue(tagVO.getValue());
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

            BeanUtils.copyProperties(details, vo);

            // Handle Service object
            if (Objects.nonNull(details.getService())) {
                ADAProjectDetailsVOService serviceVO = new ADAProjectDetailsVOService();
                serviceVO.setServiceName(details.getService().getServiceName());
                serviceVO.setServiceQuantity(details.getService().getServiceQuantity());
                vo.setService(serviceVO);
            }
            
            // Handle Stakeholders list
            if (!CollectionUtils.isEmpty(details.getStakeholders())) {
                vo.setStakeholders(details.getStakeholders().stream()
                    .filter(Objects::nonNull)
                    .map(stakeholder -> {
                        ADAProjectDetailsVOStakeholders stakeholderVO = new ADAProjectDetailsVOStakeholders();
                        stakeholderVO.setPosition(stakeholder.getPosition());
                        stakeholderVO.setUserID(stakeholder.getUserID());
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
                        tagVO.setDescription(tag.getDescription());
                        tagVO.setValue(tag.getValue());
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
