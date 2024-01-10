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
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.dto.workspace.recipe.RecipeVO;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomRecipeRepoImpl extends CommonDataRepositoryImpl<CodeServerRecipeNsql, String>
        implements WorkspaceCustomRecipeRepo {

    @Override
    public List<CodeServerRecipeNsql> findAllRecipe(int offset, int limit) {
        // TODO Auto-generated method stub

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeServerRecipeNsql> cq = cb.createQuery(CodeServerRecipeNsql.class);
        Root<CodeServerRecipeNsql> root = cq.from(entityClass);
        CriteriaQuery<CodeServerRecipeNsql> getAll = cq.select(root);
        TypedQuery<CodeServerRecipeNsql> getAllQuery = em.createQuery(getAll);
        if (offset >= 0)
            getAllQuery.setFirstResult(offset);
        if (limit > 0)
            getAllQuery.setMaxResults(limit);
        return getAllQuery.getResultList();

    }

    @Override
    public CodeServerRecipeNsql findByRecipeName(String recipeName) {
        // TODO Auto-generated method stub

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeServerRecipeNsql> cq = cb.createQuery(CodeServerRecipeNsql.class);
        Root<CodeServerRecipeNsql> root = cq.from(entityClass);
        CriteriaQuery<CodeServerRecipeNsql> getAll = cq.select(root);
        Predicate con = cb.equal(cb.lower(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("recipeName"))),
                recipeName.toLowerCase());
        Predicate pMain = cb.and(con);
        cq.where(pMain);
        TypedQuery<CodeServerRecipeNsql> getAllQuery = em.createQuery(getAll);
        List<CodeServerRecipeNsql> entities = getAllQuery.getResultList();
        if (entities != null && entities.size() > 0)
            return entities.get(0);
        else
            return null;
    }
}
