package com.mb.dna.data.db.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.dna.data.db.entities.DataikuNsql;
import com.mb.dna.data.db.entities.DataikuProjectDetails;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DataikuRepositoryImpl implements DataikuRepository{

	@Inject
	private final EntityManager entityManager;
	
	public DataikuRepositoryImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public List<DataikuNsql> getAllDataikuProject(int limit, int offset, String sortBy, String sortOrder, String projectName){
		List<DataikuNsql> convertedResults = new ArrayList<>();
		String queryString = "SELECT * FROM dataiku_nsql ";
		if(projectName!=null && !projectName.isBlank() && !projectName.isEmpty()) {
			queryString += " where lower(jsonb_extract_path_text(data,'projectName')) like '%" + projectName.toLowerCase() + "%' ";
		}
        if (sortBy==null || "projectName".equalsIgnoreCase(sortBy)) {
        	queryString += " order by lower(jsonb_extract_path_text(data,'projectName')) ";
        }
        if(sortOrder==null || "asc".equalsIgnoreCase(sortOrder)) {
        	queryString += " asc";
        }else {
        	queryString += " desc";
        }
        Query query = entityManager.createNativeQuery(queryString, DataikuNsql.class);
        if(limit>0)
        	query.setMaxResults(limit);
        if(offset>=0)
        query.setFirstResult(offset);
        ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = query.getResultList();
		if(results!=null && !results.isEmpty()) {
				convertedResults = results.stream().map(temp -> {
					DataikuNsql entity = new DataikuNsql();
				try {
					String jsonData = temp[1] != null ? temp[1].toString() : "";
					DataikuProjectDetails tempData = mapper.readValue(jsonData, DataikuProjectDetails.class);
					entity.setData(tempData);
				} catch (Exception e) {
					log.error("Failed while fetching all user records with exception {} ", e.getMessage());
				}
				String id = temp[0] != null ? temp[0].toString() : "";
				entity.setId(id);
				return entity;
			}).collect(Collectors.toList());
		}
        return convertedResults;
	}
}
