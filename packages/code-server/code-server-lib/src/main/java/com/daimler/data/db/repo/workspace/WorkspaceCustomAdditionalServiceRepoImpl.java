package com.daimler.data.db.repo.workspace;

import java.util.List;

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
}
