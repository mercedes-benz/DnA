package com.mb.dna.datalakehouse.db.repo;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.jsonb.TrinoDataLakeProject;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class TrinoDataLakeCustomRepoImpl extends CommonDataRepositoryImpl<TrinoDataLakeNsql, String>
		implements TrinoDataLakeCustomRepo {

	@Override
	public List<TrinoDataLakeNsql> getAll(String userId, int offset, int limit){
		String getAllStmt = " select cast(id as text), cast(data as text) from trino_datalake_nsql where  jsonb_extract_path_text(data,'tables') ~* '" + userId + "'";
		if (limit > 0)
			getAllStmt = getAllStmt + " limit " + limit;
		if (offset >= 0)
			getAllStmt = getAllStmt + " offset " + offset;
		Query q = em.createNativeQuery(getAllStmt);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<TrinoDataLakeNsql> convertedResults = results.stream().map(temp -> {
			TrinoDataLakeNsql entity = new TrinoDataLakeNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				TrinoDataLakeProject tempDatalake = mapper.readValue(jsonData, TrinoDataLakeProject.class);
				entity.setData(tempDatalake);
			} catch (Exception e) {
				log.error("Failed while fetching all forecast projects using native query with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}
	
	@Override
	public Long getCount(String userId) {
		String query = "select count(*) from trino_datalake_nsql where jsonb_extract_path_text(data,'tables') ~* '" + userId + "'";
		Query q = em.createNativeQuery(query);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}
	
	@Override
	public Long getCountByUserAndProject(String userId, String id) {
		String query = "select count(*) from trino_datalake_nsql where id = '"+ id + "' and jsonb_extract_path_text(data,'tables') ~* '" + userId + "'";
		Query q = em.createNativeQuery(query);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}
}