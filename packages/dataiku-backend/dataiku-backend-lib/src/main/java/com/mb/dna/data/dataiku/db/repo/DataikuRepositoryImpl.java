package com.mb.dna.data.dataiku.db.repo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.mb.dna.data.dataiku.db.entities.CollaboratorSql;
import com.mb.dna.data.dataiku.db.entities.DataikuSql;

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
	
	@Override
	public BigInteger getTotalCount(String userId, String projectName) {
		String user = userId.toLowerCase();
		EntityManager em = this.entityManager;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<DataikuSql> cq = cb.createQuery(DataikuSql.class);
		Root<DataikuSql> root = cq.from(DataikuSql.class);
		TypedQuery<DataikuSql> typedQuery = em.createQuery(cq);
		List<DataikuSql> results = typedQuery.getResultList();
		List<DataikuSql> filteredResults = new ArrayList<>();
		if(results!=null && !results.isEmpty()) {
			for(DataikuSql record: results) {
				if(userId!=null) {
					CollaboratorSql collabUser = record.getCollaborators().stream().filter(collab -> userId.equalsIgnoreCase(collab.getUserId()))
						  .findAny().orElse(null);
					if(userId.equalsIgnoreCase(record.getCreatedBy()) || collabUser!=null) {
						if(projectName==null || projectName.isBlank() || projectName.isEmpty() || projectName.equalsIgnoreCase(record.getProjectName()))
							filteredResults.add(record);
					}
				}else {
					if(projectName==null || projectName.isBlank() || projectName.isEmpty() || projectName.equalsIgnoreCase(record.getProjectName()))
						filteredResults.add(record);
				}
			}
		}
		int count = filteredResults.size();
		return BigInteger.valueOf(count);
	}
	
	@Override
	public List<DataikuSql> getAll(String userId, int offset, int limit, String sortBy, String sortOrder, String projectName){
		EntityManager em = this.entityManager;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<DataikuSql> cq = cb.createQuery(DataikuSql.class);
		Root<DataikuSql> root = cq.from(DataikuSql.class);
		TypedQuery<DataikuSql> typedQuery = em.createQuery(cq);
		List<DataikuSql> results = typedQuery.getResultList();
		List<DataikuSql> filteredResults = new ArrayList<>();
		List<DataikuSql> limitedFilteredResults = new ArrayList<>();
		if(results!=null && !results.isEmpty()) {
			for(DataikuSql record: results) {
				if(userId!=null) {
					CollaboratorSql collabUser = record.getCollaborators().stream().filter(collab -> userId.equalsIgnoreCase(collab.getUserId()))
						  .findAny().orElse(null);
					if(userId.equalsIgnoreCase(record.getCreatedBy()) || collabUser!=null) {
						if(projectName==null || projectName.isBlank() || projectName.isEmpty() || projectName.equalsIgnoreCase(record.getProjectName()))
							filteredResults.add(record);
					}
				}else {
					if(projectName==null || projectName.isBlank() || projectName.isEmpty() || projectName.equalsIgnoreCase(record.getProjectName()))
						filteredResults.add(record);
				}
			}
		}
		if(limit>0)
			limitedFilteredResults = filteredResults.subList(offset, limit);
		else
			limitedFilteredResults = filteredResults;
		return limitedFilteredResults;
	}
	
	@Override
	public void save(DataikuSql dataikuProject) {
		entityManager.persist(dataikuProject);
	}
	
	@Override
	public void update(DataikuSql dataikuProject) {
		entityManager.merge(dataikuProject);
	}

	@Override
	public void deleteById(String id) {
		findById(id).ifPresent(entityManager::remove);
	}
	
	@Override
	public Optional<DataikuSql> findById(String id) {
		return Optional.ofNullable(entityManager.find(DataikuSql.class, id));
	}
	
}
