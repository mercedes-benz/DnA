package com.daimler.data.db.repo.workspace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import com.daimler.data.db.json.CodeServerRecipeLov;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Override
    public List<CodeServerRecipeLov> getAllPublicRecipeLov()
    {
        List<CodeServerRecipeLov> lov = new ArrayList<>();
        List<Object[]> results = new ArrayList<>();
        // String getQuery = "SELECT DISTINCT ON (jsonb_extract_path_text(data, 'projectDetails', 'projectName'))"+
		// 			"cast(jsonb_extract_path_text(data,'id',) as text) as RECIPE_ID,  " +
        //           "cast(jsonb_extract_path_text(data,'recipeName') as text) as RECIPE_NAME, " +
        //           "FROM recipe_nsql";
                //    "FROM recipe_nsql WHERE lower(jsonb_extract_path_text(data,'status')) in('published') ";
        // String getQuery = "SELECT cast(jsonb_extract_path_text(data,'id') as text) as RECIPE_ID," + 
        //           "cast(jsonb_extract_path_text(data,'recipeName') as text) as RECIPE_NAME " +
        //           "FROM public.recipe_nsql WHERE lower(jsonb_extract_path_text(data,'status')) in('requested') and lower(jsonb_extract_path_text(data,'isPublic')::boolean=true";
        String getQuery = "SELECT cast(jsonb_extract_path_text(data, 'id') as text) as RECIPE_ID, " +
                        "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
                        "FROM public.recipe_nsql " +
                        "WHERE lower(jsonb_extract_path_text(data, 'status')) IN ('published') " +
                       "AND jsonb_extract_path_text(data, 'isPublic') = 'true'";
        try {
			Query q = em.createNativeQuery(getQuery);
			results = q.getResultList();
			ObjectMapper mapper = new ObjectMapper();
			for(Object[] rowData : results){
				CodeServerRecipeLov rowDetails = new CodeServerRecipeLov();
				if(rowData !=null){
					rowDetails.setId((String)rowData[0]);
					rowDetails.setRecipeName((String)rowData[1]);
				}
                lov.add(rowDetails);
			}
            return lov;
        }
        catch(Exception e) {
			e.printStackTrace();
			log.error("Failed to query workspaces under project , which are in requested and accepted state");
		}
        return null;
    }

    @Override
    public  List<CodeServerRecipeLov> getAllPrivateRecipeLov(String id)
    {
         List<CodeServerRecipeLov> lov = new ArrayList<>();
        List<Object[]> results = new ArrayList<>();
        // String getQuery = "SELECT cast(jsonb_extract_path_text(data, 'id') as text) as RECIPE_ID, " +
        //                 "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
        //                 "FROM public.recipe_nsql " +
        //                 "WHERE lower(jsonb_extract_path_text(data, 'status')) IN ('published') " +
        //                "AND jsonb_extract_path_text(data, 'isPublic') = 'false'"+
        //                 "AND jsonb_extract_path_text(data,'users','gitUserName') IN "+"'"+id+"'";
    String getQuery = "SELECT " +
                        "cast(jsonb_extract_path_text(data, 'id') as text) as RECIPE_ID, " +
                        "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
                    "FROM " +
                        "public.recipe_nsql " +
                    "WHERE " +
                        "lower(jsonb_extract_path_text(data, 'status')) IN ('published') " +
                        "AND jsonb_extract_path_text(data, 'isPublic') = 'false' " +
                        "AND EXISTS (" +
                            "SELECT 1 " +
                            "FROM jsonb_array_elements(data->'users') AS u(usr) " +
                            "WHERE jsonb_extract_path_text(u.usr, 'gitUserName') = '" + id + "'" +
                        ")";
        try {
			Query q = em.createNativeQuery(getQuery);
			results = q.getResultList();
			ObjectMapper mapper = new ObjectMapper();
			for(Object[] rowData : results){
				CodeServerRecipeLov rowDetails = new CodeServerRecipeLov();
				if(rowData !=null){
					rowDetails.setId((String)rowData[0]);
					rowDetails.setRecipeName((String)rowData[1]);
				}
                lov.add(rowDetails);
			}
            return lov;
        }
        catch(Exception e) {
			e.printStackTrace();
			log.error("Failed to query workspaces under project , which are in requested and accepted state");
		}
        return null;

    }

}
