package com.daimler.data.db.repo.workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import com.daimler.data.db.entities.CodeServerUserGroupNsql;
import com.daimler.data.db.json.CodeServerUserGroupList;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomUserGroupRepoImpl extends CommonDataRepositoryImpl<CodeServerUserGroupNsql, String> implements WorkspaceCustomUserGroupRepo {

    @Override
    public  List<String>  findByWsid(String wsid,String userId){

        String query = "SELECT id FROM public.user_wsgroup_nsql,jsonb_array_elements(data -> 'groups') as groupss,jsonb_array_elements(groupss -> 'workspaces') as workspace where jsonb_extract_path_text(workspace,'workSpaceId') = '"+wsid+"' and id = '"+userId+"'";
        Query q = em.createNativeQuery(query);
     
     @SuppressWarnings("unchecked")
    List<Object[]> results = q.getResultList();
    List<String> convertedResults = results.stream().map(temp -> {
        String tempUser = null;

        try {
            String jsonId = temp[0] != null ? temp[0].toString() : "";
            tempUser = jsonId;
            
        } catch (Exception e) {
            log.error("Failed while fetching user View details using native {} ", e.getMessage());
        }
        return tempUser;
    }).collect(Collectors.toList());
    log.info("users {}",convertedResults.toString());
    return convertedResults;
    }


}
