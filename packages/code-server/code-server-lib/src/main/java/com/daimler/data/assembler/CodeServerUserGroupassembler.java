package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerUserGroupNsql;
import com.daimler.data.db.json.CodeServerUserGroup;
import com.daimler.data.db.json.CodeServerUserGroupList;
import com.daimler.data.db.json.CodeServerUserGroupWsDetails;
import com.daimler.data.dto.workspace.CodeServerUserGroupCollectionVO;
import com.daimler.data.dto.workspace.CodeServerUserGroupVO;
import com.daimler.data.dto.workspace.CodeServerUserGroupWsDetailsVO;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.service.workspace.WorkspaceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CodeServerUserGroupassembler implements GenericAssembler<CodeServerUserGroupCollectionVO, CodeServerUserGroupNsql>{
    
    
    @Override
    public CodeServerUserGroupCollectionVO toVo(CodeServerUserGroupNsql entity) {
        try {
            CodeServerUserGroupCollectionVO groupCollection = null;
            if(entity != null) {
                groupCollection = new CodeServerUserGroupCollectionVO();
                groupCollection.setUserId(entity.getId());
                List<CodeServerUserGroupVO> voList = new ArrayList<>();
                if(entity.getData() != null){
                    List<CodeServerUserGroup> groupList = entity.getData().getGroups();
                    groupList.forEach( group -> {
                        CodeServerUserGroupVO vo = new CodeServerUserGroupVO();
                        vo.setGroupId(group.getGroupId());
                        vo.setName(group.getName());
                        vo.setOrder(group.getOrder());
                        List<CodeServerUserGroupWsDetailsVO> workspaceList = new ArrayList<>();
                        group.getWorkspaces().forEach(workSpace ->{
                            CodeServerUserGroupWsDetailsVO wsvo = new CodeServerUserGroupWsDetailsVO();
                            wsvo.setWsId(workSpace.getWorkSpaceId());
                            wsvo.setOrder(workSpace.getOrder());
                            // CodeServerWorkspaceVO workspaceVo = workSpaceService.getById(entity.getId(),workSpace.getWorkSpaceId());
                            // wsvo.setName(workspaceVo.getProjectDetails().getProjectName());
                            workspaceList.add(wsvo);
                        });
                        vo.setWorkspaces(workspaceList);
                        voList.add(vo);
                        
                    });

                }
                groupCollection.setData(voList);
                    
            }
            return groupCollection;
        } catch (Exception e) {
            log.error("Error in CodeServerUserGroupassembler toVo", e);
            return null;
        }
        
    }

    @Override
    public CodeServerUserGroupNsql toEntity(CodeServerUserGroupCollectionVO voCollection) {
        try {
            CodeServerUserGroupNsql entity = null;
            if(voCollection != null){
                entity = new CodeServerUserGroupNsql();
                CodeServerUserGroupList data = new CodeServerUserGroupList();
                List<CodeServerUserGroup> groupList = new ArrayList<>();
                voCollection.getData().forEach(vo ->{
                    CodeServerUserGroup group = new CodeServerUserGroup();
                    group.setName(vo.getName());
                    group.setOrder(vo.getOrder());
                    group.setGroupId(vo.getGroupId());
                    List<CodeServerUserGroupWsDetails> workSpaceList = new ArrayList<>();
                    vo.getWorkspaces().forEach(wsvo ->{
                        CodeServerUserGroupWsDetails workSpace = new CodeServerUserGroupWsDetails();
                        workSpace.setOrder(wsvo.getOrder());
                        workSpace.setWorkSpaceId(wsvo.getWsId());
                        workSpaceList.add(workSpace);
                    });
                    group.setWorkspaces(workSpaceList);
                    groupList.add(group);
                });
                data.setGroups(groupList);
                entity.setData(data);
                entity.setId(voCollection.getUserId());
            }
            return entity;
        } catch (Exception e) {
            log.error("Error in CodeServerUserGroupassembler toEntity", e);
            return null;
        }
    }

}
