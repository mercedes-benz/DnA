package com.daimler.data.assembler;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.db.json.Matomo;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.matomo.CollaboratorVO;
import com.daimler.data.dto.matomo.CreatedByVO;
import com.daimler.data.dto.matomo.MatomoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatomoAssembler implements GenericAssembler<MatomoVO, MatomoNsql> {

    @Override
    public MatomoVO toVo(MatomoNsql entity) {
        MatomoVO vo = null;
        if(entity!=null) {
            vo = new MatomoVO();
            vo.setId(entity.getId());
            Matomo data = entity.getData();
            if(data!=null) {
                BeanUtils.copyProperties(data, vo);
                if(data.getCollaborators()!=null && !data.getCollaborators().isEmpty()) {
                    List<CollaboratorVO> collaborators = data.getCollaborators().stream().map
                            (n -> { CollaboratorVO user = new CollaboratorVO();
                                BeanUtils.copyProperties(n,user);
                                return user;
                            }).collect(Collectors.toList());
                    vo.setCollaborators(collaborators);
                }
                if(data.getCreatedBy()!=null) {
                    CreatedByVO creator = new CreatedByVO();
                    BeanUtils.copyProperties(data.getCreatedBy(),creator);
                    vo.setCreatedBy(creator);
                }
            }
        }
        return vo;
    }

    @Override
    public MatomoNsql toEntity(MatomoVO vo) {
        MatomoNsql entity = null;
        if(vo!=null) {
            entity = new MatomoNsql();
            entity.setId(vo.getId());
            Matomo data = new Matomo();
            BeanUtils.copyProperties(vo, data);
            if(vo.getCollaborators()!=null && !vo.getCollaborators().isEmpty()) {
                List<UserDetails> collaborators = vo.getCollaborators().stream().map
                        (n -> { UserDetails collaborator = new UserDetails();
                            BeanUtils.copyProperties(n,collaborator);
                            return collaborator;
                        }).collect(Collectors.toList());
                data.setCollaborators(collaborators);
            }
            if(vo.getCreatedBy()!=null) {
                UserDetails creator = new UserDetails();
                BeanUtils.copyProperties(vo.getCreatedBy(), creator);
                data.setCreatedBy(creator);
            }

        }
        return entity;
    }
}
