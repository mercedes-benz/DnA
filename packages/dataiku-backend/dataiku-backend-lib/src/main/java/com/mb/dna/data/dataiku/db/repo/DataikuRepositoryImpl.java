package com.mb.dna.data.dataiku.db.repo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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
	public DataikuSql findByProjectName(String projectName, String cloudProfile) {
		DataikuSql existingRecord = null;
		
		List<DataikuSql> results = new ArrayList<>();
		try {
			String queryString = "SELECT id,project_name,description,cloud_profile,created_by,created_on, "
					+ "status,classification_type,has_pii,division_id,division_name,subdivision_id,subdivision_name,department "
					+ "FROM dataiku_sql ";
			if(projectName!=null && !projectName.isBlank() && !projectName.isEmpty()) {
				queryString += " where LOWER(project_name) = '" + projectName.toLowerCase() + "' and LOWER(cloud_profile) = '" + cloudProfile.toLowerCase() + "'";
			}else {
				return existingRecord;
			}
			Query query = entityManager.createNativeQuery(queryString, DataikuSql.class);
			List<DataikuSql> fetchedResults = query.getResultList();
			if(fetchedResults!=null && !fetchedResults.isEmpty()) {
				existingRecord = fetchedResults.get(0);
			}
		}catch(Exception e) {
			log.error("Failed to fetch dataiku project with name {} with exception {}", projectName, e.getMessage());
		}
        return existingRecord;
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
		if(sortBy ==null || "".equalsIgnoreCase(sortBy) || "projectName".equalsIgnoreCase(sortBy)) {
			if(sortOrder==null || "".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder)) {
				Collections.sort(filteredResults, (DataikuSql a, DataikuSql b) -> a.getProjectName().compareTo(b.getProjectName()));
			}else {
				Collections.sort(filteredResults, (DataikuSql a, DataikuSql b) -> b.getProjectName().compareTo(a.getProjectName()));
			}
		}else {
			if(sortOrder==null || "".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder)) {
				Collections.sort(filteredResults, (DataikuSql a, DataikuSql b) -> a.getCreatedOn().compareTo(b.getCreatedOn()));
			}else {
				Collections.sort(filteredResults, (DataikuSql a, DataikuSql b) -> b.getCreatedOn().compareTo(a.getCreatedOn()));
			}
		}
		if(limit>1) {
			int count = filteredResults.size();
			if(limit>count)
				limit = count - offset;
			limitedFilteredResults = filteredResults.subList(offset, limit);
		}
		else{
			limitedFilteredResults = filteredResults;
		}
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
		String insertStmt = "insert into  dataiku_sql(id,cloud_profile,created_by,created_on,description"
				+ ",project_name,status,classification_type,has_pii,division_id,division_name,subdivision_id,subdivision_name,department) "
				+ "values (:id, :cloudProfile, :createdBy, :createdOn, :description, :projectName"
				+ ",:status,:classification_type,:has_pii,:division_id,:division_name,:subdivision_id,:subdivision_name,:department)";
		Query q = entityManager.createNativeQuery(insertStmt);
		q.setParameter("id", dataikuProject.getId());
		q.setParameter("cloudProfile", dataikuProject.getCloudProfile());
		q.setParameter("createdBy", dataikuProject.getCreatedBy());
		q.setParameter("createdOn", dataikuProject.getCreatedOn());
		q.setParameter("description", dataikuProject.getDescription());
		q.setParameter("projectName", dataikuProject.getProjectName());
		
		q.setParameter("status", dataikuProject.getStatus());
		q.setParameter("classification_type", dataikuProject.getClassificationType());
		q.setParameter("has_pii", dataikuProject.getHasPii());
		q.setParameter("division_id", dataikuProject.getDivisionId());
		q.setParameter("division_name", dataikuProject.getDivisionName());
		q.setParameter("subdivision_id", dataikuProject.getSubdivisionId());
		q.setParameter("subdivision_name", dataikuProject.getSubdivisionName());
		q.setParameter("department", dataikuProject.getDepartment());
		
		q.executeUpdate();
		log.info("successfully ran insert statement for dataiku {}",dataikuProject.getProjectName());
	}
	
	public void insertCollab(CollaboratorSql collaborator) {
		String insertStmt = "insert into  collaborator_sql(id,userid,dataiku_id,permission,givenname,surname) values (:id, :userId, :dataikuId,:permission, :givenname, :surname)";
		Query q = entityManager.createNativeQuery(insertStmt);
		q.setParameter("id", collaborator.getId());
		q.setParameter("userId", collaborator.getUserId());
		q.setParameter("dataikuId", collaborator.getDataikuId());
		q.setParameter("permission", collaborator.getPermission());
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
	
	public void updateDataiku(String id,String description, String classificationType, String department, String divisionId, String divisionName, Boolean hasPii, String status, String subDivisionId, String subDivisionName) {
		String updateStmt = "update dataiku_sql set  description = :updatedDescription, "
				+ "status = :status, classificationType = :classificationType, hasPii = :hasPii, divisionId = :divisionId, divisionName = :divisionName,  "
				+ "subdivisionId = :subdivisionId, subdivisionName = :subdivisionName, department = :department "
				+ " where id = :id";
		Query q = entityManager.createNativeQuery(updateStmt);
		q.setParameter("updatedDescription", description);
		q.setParameter("status", status);
		q.setParameter("classificationType", classificationType);
		q.setParameter("hasPii", hasPii);
		q.setParameter("divisionId", divisionId);
		q.setParameter("divisionName", divisionName);
		q.setParameter("subdivisionId", subDivisionId);
		q.setParameter("subdivisionName", subDivisionName);
		q.setParameter("department", department);
		q.setParameter("id", id);
		q.executeUpdate();
		log.info("successfully updated description for dataikuprojectid {} ",id);
	}
	
	@Override
	public void update(DataikuSql dataikuProject) {
		//entityManager.merge(dataikuProject);
		if(dataikuProject!=null) {
			updateDataiku(dataikuProject.getId(),dataikuProject.getDescription(),dataikuProject.getClassificationType(),dataikuProject.getDepartment(),
					dataikuProject.getDivisionId(),dataikuProject.getDivisionName(),dataikuProject.getHasPii(),dataikuProject.getStatus(),dataikuProject.getSubdivisionId(),dataikuProject.getSubdivisionName());
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
