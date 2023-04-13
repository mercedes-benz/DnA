package com.mb.dna.data.userprivilege.db.repo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeDto;
import com.mb.dna.data.userprivilege.db.entities.UserPrivilegeSql;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UserPrivilegeRepositoryImpl implements UserPrivilegeRepository{

	@Inject
	private final EntityManager entityManager;

	public UserPrivilegeRepositoryImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	@Override
	public Optional<UserPrivilegeSql> findById(String id) {
		return Optional.ofNullable(entityManager.find(UserPrivilegeSql.class, id));
	}
	
	@Override
	public UserPrivilegeSql findByUser(String searchTerm) {
		UserPrivilegeSql existingRecord = null;
		List<UserPrivilegeSql> results = new ArrayList<>();
		try {
			String queryString = "SELECT id,userId,profile,givenName,surName FROM userprivilege_sql ";
			if(searchTerm!=null && !searchTerm.isBlank() && !searchTerm.isEmpty()) {
				queryString += " where LOWER(userId) = '" + searchTerm.toLowerCase() + 
						"' or LOWER(givenName) = '" + searchTerm.toLowerCase() + 
						"' or LOWER(surName) = '"+ searchTerm.toLowerCase() + 
						"'";
			}else {
				return existingRecord;
			}
			Query query = entityManager.createNativeQuery(queryString, UserPrivilegeSql.class);
			List<UserPrivilegeSql> fetchedResults = query.getResultList();
			if(fetchedResults!=null && !fetchedResults.isEmpty()) {
				existingRecord = fetchedResults.get(0);
			}
		}catch(Exception e) {
			log.error("Failed to fetch user with searchTerm {} with exception {}", searchTerm, e.getMessage());
		}
        return existingRecord;
	}
	
	@Override
	public UserPrivilegeSql save(UserPrivilegeSql userinfo) {
		entityManager.persist(userinfo);
		return userinfo;
	}

	@Override
	public void deleteById(String id) {
		findById(id).ifPresent(entityManager::remove);
	}

	@Override
	public List<UserPrivilegeSql> findAll(int limit, int offset, String sortBy, String sortOrder, String userId) {
		List<UserPrivilegeSql> results = new ArrayList<>();
		String queryString = "SELECT id,userId,profile,givenName,surName FROM userprivilege_sql ";
		if(userId!=null && !userId.isBlank() && !userId.isEmpty()) {
			queryString += " where LOWER(userId) like '%" + userId.toLowerCase() + "%' ";
		}
        if (sortBy==null || "userId".equalsIgnoreCase(sortBy)) {
        	queryString += " order by userId ";
        }else {
        	queryString += " order by profile";
        }
        if(sortOrder==null || "asc".equalsIgnoreCase(sortOrder) || "".equalsIgnoreCase(sortOrder)) {
        	queryString += " asc";
        }else {
        	queryString += " desc";
        }
        Query query = entityManager.createNativeQuery(queryString, UserPrivilegeSql.class);
        if(limit>0)
        	query.setMaxResults(limit);
        if(offset>=0)
        query.setFirstResult(offset);
		List<UserPrivilegeSql> fetchedResults = query.getResultList();
		if(fetchedResults!=null && !fetchedResults.isEmpty()) {
			results = fetchedResults;
		}
        return results;
	}
	
	@Override
	public BigInteger findCount(String userId) {
		String queryString = "select count(*) from userprivilege_sql";
		if(userId!=null && !userId.isBlank() && !userId.isEmpty()) {
			queryString += " where LOWER(userId) like '%" + userId.toLowerCase() + "%' ";
		}
		Query q = entityManager.createNativeQuery(queryString);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results;
	}
	
	@Override
	public void update(UserPrivilegeDto record) {
		List<UserPrivilegeSql> existingRecords = this.findAll(0, 0, null, null, record.getUserId());
		UserPrivilegeSql existingEntity = existingRecords!=null && !existingRecords.isEmpty() ? existingRecords.get(0) : null;
		if(existingEntity!=null && existingEntity.getId()!=null && record.getUserId().equalsIgnoreCase(existingEntity.getUserId())) {
			existingEntity.setProfile(record.getProfile());
			entityManager.merge(existingEntity);
			log.info("user {} updated with profile {}",record.getUserId(),record.getProfile());
		}else {
			UserPrivilegeSql newEntity = new UserPrivilegeSql();
			newEntity.setGivenName(record.getGivenName());
			newEntity.setSurName(record.getSurName());
			newEntity.setProfile(record.getProfile());
			newEntity.setUserId(record.getUserId());
			entityManager.persist(newEntity);
			log.info("user {} added with profile {}",record.getUserId(),record.getProfile());
		}
	}

	@Override
	public void deleteAll() {
		String queryString = "delete from userprivilege_sql";
		Query q = entityManager.createNativeQuery(queryString);
		q.executeUpdate();
	}
	
}
