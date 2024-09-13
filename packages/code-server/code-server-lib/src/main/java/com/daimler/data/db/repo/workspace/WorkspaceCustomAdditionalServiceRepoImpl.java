package com.daimler.data.db.repo.workspace;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomAdditionalServiceRepoImpl extends CommonDataRepositoryImpl<CodeServerAdditionalServiceNsql, String>
        implements WorkspaceCustomAdditionalServiceRepo {

        @Override
        public List<CodeServerAdditionalServiceNsql> findAllServices(int offset, int limit) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<CodeServerAdditionalServiceNsql> cq = cb.createQuery(CodeServerAdditionalServiceNsql.class);
                Root<CodeServerAdditionalServiceNsql> root = cq.from(entityClass);
                CriteriaQuery<CodeServerAdditionalServiceNsql> getAll = cq.select(root);
                TypedQuery<CodeServerAdditionalServiceNsql> getAllQuery = em.createQuery(getAll);
                return getAllQuery.getResultList();
        }

        @Override
        public String findByServiceName(String serviceName) {
                List<Object[]> results = new ArrayList<>();
                String getQuery = "SELECT CAST(jsonb_extract_path_text(data, 'additionalProperties') AS text) AS additionalProperties "
                                + "FROM public.additional_service_nsql " +
                                 "WHERE jsonb_extract_path_text(data, 'serviceName') = '" + serviceName + "'";
                try {
                        Query query = em.createNativeQuery(getQuery);
                        results = query.getResultList();
                        return  results.toString();
                } catch (Exception e) {
                        log.error("Failed to query additional services while creating server.");
                }
                return null;
        }
}
