package com.daimler.data.db.repo.DbService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import jakarta.persistence.Query;
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

	private String addQuotes(String value) {
		if(value!=null && !"null".equalsIgnoreCase(value)){
			String escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")  
            .replace("'", "''"); 
			return "\"" + escaped + "\"";
		}
		else
			return null;
	}


    @Override
	public String updateDeleteStatus(DbServiceNsql entity) {
		String response = "FAILED";
		Date deployedOn = entity.getData().getModifiedOn();
		String	longdate = String.valueOf(deployedOn.getTime()) ;
		String updateQuery = "update db_service_nsql\r\n"
				+ "set data = jsonb_set(jsonb_set(jsonb_set(data, '{status}', '\"DELETED\"'), '{modifiedBy}', '{"+
				" \"id\": " + addQuotes(entity.getData().getModifiedBy().getId()) + "," +
				" \"email\": " + addQuotes(entity.getData().getModifiedBy().getEmail()) + "," +
				" \"lastName\": " + addQuotes(entity.getData().getModifiedBy().getLastName()) + "," +
				" \"firstName\": " + addQuotes(entity.getData().getModifiedBy().getFirstName()) + "," +
				" \"department\": " + addQuotes(entity.getData().getModifiedBy().getDepartment()) + "," +
				" \"gitUserName\": " + addQuotes(entity.getData().getModifiedBy().getGitUserName()) + "," +
				" \"mobileNumber\": " + addQuotes(entity.getData().getModifiedBy().getMobileNumber()) + "," +				
				" \"isRead\": " + entity.getData().getModifiedBy().getIsRead() + "," +
				" \"isAdmin\": " + entity.getData().getModifiedBy().getIsAdmin() + "," +
				" \"isWrite\": " + entity.getData().getModifiedBy().getIsWrite() + "}'"+
				"), '{modifiedOn}', '"+longdate+"') "+
				"WHERE id = '"+entity.getId()+"'";

				log.info("updateQuery {}",updateQuery);
				

		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			log.info("delete status updated successfully for dbService {} ", entity.getData().getServiceName());
			response = "SUCCESS";
		} catch (Exception e) {
			log.error("Failed while updating the delete status with Exception {} ", e.getMessage());
		}
		return response;
	}

}
