package com.daimler.data.db.repo.workspace;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomBuildDeployRepoImpl extends CommonDataRepositoryImpl<CodeServerBuildDeployNsql,String> implements WorkspaceCustomBuildDeployRepo{

    @Override
    public CodeServerBuildDeployNsql findByProjectName(String projectName) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeServerBuildDeployNsql> cq = cb.createQuery(CodeServerBuildDeployNsql.class);
        Root<CodeServerBuildDeployNsql> root = cq.from(entityClass);
        CriteriaQuery<CodeServerBuildDeployNsql> getAll = cq.select(root);
        Predicate con1 = cb.equal(cb.lower(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectName"))),
                projectName.toLowerCase());
        Predicate con2 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());        
        Predicate pMain = cb.and(con1,con2);
        cq.where(pMain);
        TypedQuery<CodeServerBuildDeployNsql> getAllQuery = em.createQuery(getAll);
        List<CodeServerBuildDeployNsql> entities = getAllQuery.getResultList();
        if (entities != null && entities.size() > 0)
            return entities.get(0);
        else
            return null;
    }

}
