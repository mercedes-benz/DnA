package com.daimler.data.db.repo.workspace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
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

    @Override
    public CodeServerSoftwareNsql findBySoftwareName(String softwareName)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeServerSoftwareNsql> cq = cb.createQuery(CodeServerSoftwareNsql.class);
        Root<CodeServerSoftwareNsql> root = cq.from(entityClass);
        CriteriaQuery<CodeServerSoftwareNsql> getAll = cq.select(root);
        Predicate con = cb.equal(cb.lower(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("softwareName"))),
                softwareName.toLowerCase());
        Predicate pMain = cb.and(con);
        cq.where(pMain);
        TypedQuery<CodeServerSoftwareNsql> getAllQuery = em.createQuery(getAll);
        List<CodeServerSoftwareNsql> entities = getAllQuery.getResultList();
        if (entities != null && entities.size() > 0)
            return entities.get(0);
        else
            return null;
    }

    @Override
    public CodeServerSoftwareNsql findBySoftwareId(String id)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeServerSoftwareNsql> cq = cb.createQuery(CodeServerSoftwareNsql.class);
        Root<CodeServerSoftwareNsql> root = cq.from(entityClass);
        CriteriaQuery<CodeServerSoftwareNsql> getAll = cq.select(root);
        Predicate con = cb.equal(root.get("id"),id);
        Predicate pMain = cb.and(con);
        cq.where(pMain);
        TypedQuery<CodeServerSoftwareNsql> getAllQuery = em.createQuery(getAll);
        List<CodeServerSoftwareNsql> entities = getAllQuery.getResultList();
        if (entities != null && entities.size() > 0)
            return entities.get(0);
        else
            return null;

    }

    @Override
    @Transactional
    public GenericMessage deleteSoftware(CodeServerSoftwareNsql software) {
        try {
            if (software != null) {
                //CodeServerSoftwareNsql existingSoftware = findBySoftwareName(software.getData().getSoftwareName());
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaDelete<CodeServerSoftwareNsql> cd = cb.createCriteriaDelete(CodeServerSoftwareNsql.class);
                Root<CodeServerSoftwareNsql> root = cd.from(CodeServerSoftwareNsql.class);
                cd.where(cb.equal(root.get("id"), software.getId()));
                em.createQuery(cd).executeUpdate();
                return new GenericMessage("Software deleted successfully");
            } else {
                return new GenericMessage("Software not found");
            }
        } catch (Exception e) {
            return new GenericMessage("Failed to delete software: " + e.getMessage());
        }
    }
}
