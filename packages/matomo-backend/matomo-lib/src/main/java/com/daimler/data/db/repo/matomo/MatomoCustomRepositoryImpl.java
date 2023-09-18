package com.daimler.data.db.repo.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.db.json.Matomo;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class MatomoCustomRepositoryImpl extends CommonDataRepositoryImpl<MatomoNsql, String> implements MatomoCustomRepository {
    @Override
    public MatomoNsql findUserById(String siteId) {
        MatomoNsql existingRecord = null;
        List<MatomoNsql> results = new ArrayList<>();
        try {
            String getAllStmt = " select cast(id as text), cast(data as text) from matomo_nsql where  ((lower(jsonb_extract_path_text(data,'siteId')) = '" + siteId +
                    "'))";

            Query query = em.createNativeQuery(getAllStmt, MatomoNsql.class);
            List<MatomoNsql> fetchedResults = query.getResultList();
            if(fetchedResults!=null && !fetchedResults.isEmpty()) {
                existingRecord = fetchedResults.get(0);
            }
        }catch(Exception e) {
            log.error("Failed to fetch user with searchTerm {} with exception {}", siteId, e.getMessage());
        }
        return existingRecord;
    }


}
