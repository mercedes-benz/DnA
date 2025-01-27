package com.daimler.data.assembler;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerUserGroupNsql;
import com.daimler.data.db.json.CodeServerUserGroup;
import com.daimler.data.dto.workspace.CodeServerUserGroupCollectionVO;
import com.daimler.data.dto.workspace.CodeServerUserGroupVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CodeServerUserGroupassembler implements GenericAssembler<CodeServerUserGroupCollectionVO, CodeServerUserGroupNsql>{@Override
    public CodeServerUserGroupCollectionVO toVo(CodeServerUserGroupNsql entity) {
        try {
            CodeServerUserGroupCollectionVO voList = null;
            if(entity != null) {
                voList = new CodeServerUserGroupCollectionVO();
                voList.setUserId(entity.getId());
                if(entity.getData() != null){
                    List<CodeServerUserGroup> groupList = entity.getData().getGroups();
                    groupList.forEach( group -> {
                        CodeServerUserGroupVO vo = new CodeServerUserGroupVO();
                        BeanUtils.copyProperties(group, vo);
                    });

                }
                    
            }
            return voList;
        } catch (Exception e) {
            log.error("Error in CodeServerUserGroupassembler toVo", e);
            return null;
        }
        
    }

    @Override
    public CodeServerUserGroupNsql toEntity(CodeServerUserGroupCollectionVO vo) {
        return new CodeServerUserGroupNsql();
    }

}
