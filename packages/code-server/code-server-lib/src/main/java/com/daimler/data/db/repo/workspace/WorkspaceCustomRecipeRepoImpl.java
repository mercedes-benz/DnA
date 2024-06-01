package com.daimler.data.db.repo.workspace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.dto.CodeServerRecipeDto;

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
    @Transactional
    public GenericMessage deleteRecipe(CodeServerRecipeNsql recipe) {
        try {
            if (recipe != null) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaDelete<CodeServerRecipeNsql> cd = cb.createCriteriaDelete(CodeServerRecipeNsql.class);
                Root<CodeServerRecipeNsql> root = cd.from(CodeServerRecipeNsql.class);
                cd.where(cb.equal(root.get("id"), recipe.getId()));
                em.createQuery(cd).executeUpdate();
                return new GenericMessage("Recipe deleted successfully");
            } else {
                return new GenericMessage("Recipe not found");
            }
        } catch (Exception e) {
            return new GenericMessage("Failed to delete recipe: " + e.getMessage());
        }
    }

    @Override
    public List<CodeServerRecipeDto> getAllPublicRecipeLov()
    {
        List<CodeServerRecipeDto> lov = new ArrayList<>();
        List<Object[]> results = new ArrayList<>();
        // String getQuery = "SELECT id as RECIPE_ID, " +
        //                 "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
        //                 "FROM public.recipe_nsql " +
        //                 "WHERE lower(jsonb_extract_path_text(data, 'status')) IN ('published') " +
        //                "AND jsonb_extract_path_text(data, 'isPublic') = 'true'";
         String getQuery = "SELECT id as RECIPE_ID, " +
                        "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
                        "FROM public.recipe_nsql " +
                        "WHERE " +
                       "jsonb_extract_path_text(data, 'isPublic') = 'true'";
        try {
			Query q = em.createNativeQuery(getQuery);
			results = q.getResultList();
			ObjectMapper mapper = new ObjectMapper();
			for(Object[] rowData : results){
				CodeServerRecipeDto rowDetails = new CodeServerRecipeDto();
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
    public  List<CodeServerRecipeDto> getAllPrivateRecipeLov(String id)
    {
         List<CodeServerRecipeDto> lov = new ArrayList<>();
        List<Object[]> results = new ArrayList<>();
        // String getQuery = "SELECT " +
        //                 "id as RECIPE_ID, " +
        //                 "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
        //             "FROM " +
        //                 "public.recipe_nsql " +
        //             "WHERE " +
        //                 "lower(jsonb_extract_path_text(data, 'status')) IN ('published') " +
        //                 "AND jsonb_extract_path_text(data, 'isPublic') = 'false' " +
        //                 "AND EXISTS (" +
        //                     "SELECT 1 " +
        //                     "FROM jsonb_array_elements(data->'users') AS u(usr) " +
        //                     "WHERE jsonb_extract_path_text(u.usr, 'gitUserName') = '" + id + "'" +
        //                 ")";
        String getQuery = "SELECT " +
                "id as RECIPE_ID, " +
                "cast(jsonb_extract_path_text(data, 'recipeName') as text) as RECIPE_NAME " +
            "FROM " +
                "public.recipe_nsql " +
            "WHERE " +
                "jsonb_extract_path_text(data, 'isPublic') = 'false' " +
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
				CodeServerRecipeDto rowDetails = new CodeServerRecipeDto();
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

    // @Override
    // public List<CodeServerRecipeNsql> findAllRecipesWithRequestedAndAcceptedState(int offset, int limit) {
    //     CriteriaBuilder cb = em.getCriteriaBuilder();
    //     CriteriaQuery<CodeServerRecipeNsql> cq = cb.createQuery(CodeServerRecipeNsql.class);
    //     Root<CodeServerRecipeNsql> root = cq.from(entityClass);
    //     CriteriaQuery<CodeServerRecipeNsql> getAll = cq.select(root);
    //     Predicate con = cb.equal(cb.lower(
    //             cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
    //             "requested");
    //     Predicate con1 = cb.equal(cb.lower(
    //             cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
    //             "accepted");
    //     Predicate pMain = cb.or(con, con1);
    //     cq.where(pMain);
    //     TypedQuery<CodeServerRecipeNsql> getAllQuery = em.createQuery(getAll);
    //     if (offset >= 0)
    //         getAllQuery.setFirstResult(offset);
    //     if (limit > 0)
    //         getAllQuery.setMaxResults(limit);
    //     return getAllQuery.getResultList();
 
    // }

    // @Override
    // public GenericMessage updateRecipeInfo(String name,String status)
    // {
    //     GenericMessage updateResponse = new GenericMessage();
	// 	updateResponse.setSuccess("FAILED");
	// 	List<MessageDescription> errors = new ArrayList<>();
	// 	List<MessageDescription> warnings = new ArrayList<>();
    //     String updateQuery = "UPDATE recipe_nsql SET data = jsonb_set(data, '{status}', '\""+ status +"\"') "
    //     + " WHERE lower(jsonb_extract_path_text(data,'recipeName')) = '" + name.toLowerCase() + "'";

	// 	try {
	// 		Query q = em.createNativeQuery(updateQuery);
	// 		q.executeUpdate();
	// 		updateResponse.setSuccess("SUCCESS");
	// 		updateResponse.setErrors(new ArrayList<>());
	// 		updateResponse.setWarnings(new ArrayList<>());
	// 		log.info("updated status of recipe {} to ACCPETED state", name);
	// 	}catch(Exception e) {
	// 		e.printStackTrace();
	// 		MessageDescription errMsg = new MessageDescription("Failed while updating the recipe  status.");
	// 		errors.add(errMsg);
	// 		log.error("Failed to update status of recipe  {} to ACCPETED state with exception {}", name, e.getMessage());
	// 	}
    //     return updateResponse;
    // }
}
