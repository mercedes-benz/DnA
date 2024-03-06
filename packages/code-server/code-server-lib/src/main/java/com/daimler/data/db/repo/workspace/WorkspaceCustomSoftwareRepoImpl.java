package com.daimler.data.db.repo.workspace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomSoftwareRepo;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomSoftwareRepoImpl extends CommonDataRepositoryImpl<CodeServerSoftwareNsql, String>
        implements WorkspaceCustomSoftwareRepo {


    @Override
    public List<CodeServerSoftwareNsql> findAllSoftwareDetails()
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeServerSoftwareNsql> cq = cb.createQuery(CodeServerSoftwareNsql.class);
        Root<CodeServerSoftwareNsql> root = cq.from(entityClass);
        CriteriaQuery<CodeServerSoftwareNsql> getAll = cq.select(root);
        TypedQuery<CodeServerSoftwareNsql> getAllQuery = em.createQuery(getAll);
        return getAllQuery.getResultList();

    }
}
