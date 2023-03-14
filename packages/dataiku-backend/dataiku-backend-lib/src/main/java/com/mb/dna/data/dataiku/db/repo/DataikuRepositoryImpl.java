package com.mb.dna.data.dataiku.db.repo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
		//entityManager.persist(dataikuProject);
		if(dataikuProject!=null) {
			insertDataiku(dataikuProject);
			List<CollaboratorSql> collabs = dataikuProject.getCollaborators();
			if(collabs!=null && !collabs.isEmpty()) {
				collabs.forEach(n->this.insertCollab(n));
			}
		}
	}
	
	public void insertDataiku(DataikuSql dataikuProject) {
		String insertStmt = "insert into  dataiku_sql(id,cloud_profile,created_by,created_on,description,project_name) "
				+ "values (:id, :cloudProfile, :createdBy, :createdOn, :description, :projectName)";
		Query q = entityManager.createNativeQuery(insertStmt);
		q.setParameter("id", dataikuProject.getId());
		q.setParameter("cloudProfile", dataikuProject.getCloudProfile());
		q.setParameter("createdBy", dataikuProject.getCreatedBy());
		q.setParameter("createdOn", dataikuProject.getCreatedOn());
		q.setParameter("description", dataikuProject.getDescription());
		q.setParameter("projectName", dataikuProject.getProjectName());
		q.executeUpdate();
		log.info("successfully ran insert statement for dataiku {}",dataikuProject.getProjectName());
	}
	
	public void insertCollab(CollaboratorSql collaborator) {
		String insertStmt = "insert into  collaborator_sql(id,userid,dataiku_id,givenname,surname) values (:id, :userId, :dataikuId, :givenname, :surname)";
		Query q = entityManager.createNativeQuery(insertStmt);
		q.setParameter("id", collaborator.getId());
		q.setParameter("userId", collaborator.getUserId());
		q.setParameter("dataikuId", collaborator.getDataikuId());
		q.setParameter("givenname", collaborator.getGivenName());
		q.setParameter("surname", collaborator.getSurName());
		q.executeUpdate();
		log.info("successfully ran insert statement for collaborator {} and dataiku id {}",collaborator.getUserId(),collaborator.getDataikuId());
	}
	
	public void deleteExistingCollabs(String dataikuid) {
		String deleteStmt = "delete from collaborator_sql where dataiku_id = :id";
		Query q = entityManager.createNativeQuery(deleteStmt);
		q.setParameter("id", dataikuid);
		q.executeUpdate();
		log.info("successfully deleted old collab records for dataiku id {}",dataikuid);
	}
	
	public void updateDataiku(String id,String description) {
		String updateStmt = "update dataiku_sql set  description = :updatedDescription where id = :id";
		Query q = entityManager.createNativeQuery(updateStmt);
		q.setParameter("updatedDescription", description);
		q.setParameter("id", id);
		q.executeUpdate();
		log.info("successfully updated description for dataikuprojectid {} ",id);
	}
	
	@Override
	public void update(DataikuSql dataikuProject) {
		//entityManager.merge(dataikuProject);
		if(dataikuProject!=null) {
			updateDataiku(dataikuProject.getId(),dataikuProject.getDescription());
			deleteExistingCollabs(dataikuProject.getId());
			List<CollaboratorSql> collabs = dataikuProject.getCollaborators();
			if(collabs!=null && !collabs.isEmpty()) {
				collabs.forEach(n->this.insertCollab(n));
			}
		}
	}

	@Override
	public void deleteById(String id) {
		findById(id).ifPresent(entityManager::remove);
	}
	
	@Override
	public Optional<DataikuSql> findById(String id) {
		try {
			return Optional.ofNullable(entityManager.find(DataikuSql.class, id));
		}catch(Exception e) {
			log.error("Dataiku project with id {} not found",id);
			return null;
		}
	}
	
}
