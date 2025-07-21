package com.daimler.data.db.repo.DbService;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DbServiceCustomRepositoryImpl extends CommonDataRepositoryImpl<DbServiceNsql,String> implements DbServiceCustomRepository {

     @Override
    public List<DbServiceNsql> findAllDbService(int offset, int limit,String id) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DbServiceNsql> cq = cb.createQuery(DbServiceNsql.class);
        Root<DbServiceNsql> root = cq.from(entityClass);
        CriteriaQuery<DbServiceNsql> getAll = cq.select(root);
        Predicate con1 = cb.equal(cb.lower(cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectOwner"), cb.literal("id"))), id.toLowerCase());
        Predicate con2 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
        Predicate pMain = cb.and(con1, con2);
		cq.where(pMain);
        TypedQuery<DbServiceNsql> getAllQuery = em.createQuery(getAll);
        if (offset >= 0)
            getAllQuery.setFirstResult(offset);
        if (limit > 0)
            getAllQuery.setMaxResults(limit);
        return getAllQuery.getResultList();

    }

}
