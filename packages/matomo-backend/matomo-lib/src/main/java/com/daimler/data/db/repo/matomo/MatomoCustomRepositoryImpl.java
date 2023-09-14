package com.daimler.data.db.repo.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.db.json.Matomo;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class MatomoCustomRepositoryImpl extends CommonDataRepositoryImpl<MatomoNsql, String> implements MatomoCustomRepository {
    @Override
    public List<MatomoNsql> getAll(String userId, int offset, int limit){
        String user = userId.toLowerCase();
        String getAllStmt = " select cast(id as text), cast(data as text) from matomo_nsql where  ((lower(jsonb_extract_path_text(data,'createdBy','id')) = '" + user +
                "'))";

        Query q = em.createNativeQuery(getAllStmt);
        ObjectMapper mapper = new ObjectMapper();
        List<Object[]> results = q.getResultList();
        List<MatomoNsql> convertedResults = results.stream().map(temp -> {
            MatomoNsql entity = new MatomoNsql();
            try {
                String jsonData = temp[1] != null ? temp[1].toString() : "";
                Matomo Matomo = mapper.readValue(jsonData, Matomo.class);
                entity.setData(Matomo);
            } catch (Exception e) {
                log.error("Failed while fetching all matomo sites using native query with exception {} ", e.getMessage());
            }
            String id = temp[0] != null ? temp[0].toString() : "";
            entity.setId(id);
            return entity;
        }).collect(Collectors.toList());
        return convertedResults;
    }

    @Override
    public long getTotalCount(String userId) {
        String user = userId.toLowerCase();
        String getCountStmt = " select count(*) from matomo_nsql where  ((lower(jsonb_extract_path_text(data,'createdBy','id')) = '" + user +
                "'))";
        Query q = em.createNativeQuery(getCountStmt);
        BigInteger results = (BigInteger) q.getSingleResult();
        return results.longValue();
    }
}
