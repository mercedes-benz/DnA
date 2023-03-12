package com.mb.dna.data.db.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.dna.data.db.entities.UserPrivilege;
import com.mb.dna.data.db.entities.UserPrivilegeNsql;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UserPrivilegeRepositoryImpl implements UserPrivilegeRepository{

	private final EntityManager entityManager;

	public UserPrivilegeRepositoryImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	@Override
	public Optional<UserPrivilegeNsql> findById(long id) {
		return Optional.ofNullable(entityManager.find(UserPrivilegeNsql.class, id));
	}

	@Override
	@Transactional
	public UserPrivilegeNsql save(UserPrivilege userinfo) {
		UserPrivilegeNsql user = new UserPrivilegeNsql(userinfo);
		entityManager.persist(user);
		return user;
	}

	@Override
	@Transactional
	public void deleteById(long id) {
		findById(id).ifPresent(entityManager::remove);
	}

	@Override
	public List<UserPrivilegeNsql> findAll(int limit, int offset, String sortBy, String sortOrder, String userId) {
		List<UserPrivilegeNsql> convertedResults = new ArrayList<>();
		String queryString = "SELECT * FROM userprivilege_nsql ";
		if(userId!=null) {
			queryString += " where lower(jsonb_extract_path_text(data,'userID')) = '" + userId.toLowerCase() + "' ";
		}
        if (sortBy==null || "userId".equalsIgnoreCase(sortBy)) {
        	queryString += " ORDER BY order by lower(jsonb_extract_path_text(data,'userId')) ";
        }else {
        	queryString += " ORDER BY order by lower(jsonb_extract_path_text(data,'profile')) ";
        }
        if(sortOrder==null || "asc".equalsIgnoreCase(sortOrder)) {
        	queryString += " asc";
        }else {
        	queryString += " desc";
        }
        Query query = entityManager.createNativeQuery(queryString, UserPrivilegeNsql.class);
        if(limit>0)
        	query.setMaxResults(limit);
        if(offset>=0)
        query.setFirstResult(offset);
        ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = query.getResultList();
		if(results!=null && !results.isEmpty()) {
				convertedResults = results.stream().map(temp -> {
				UserPrivilegeNsql entity = new UserPrivilegeNsql();
				try {
					String jsonData = temp[1] != null ? temp[1].toString() : "";
					UserPrivilege tempData = mapper.readValue(jsonData, UserPrivilege.class);
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
	
	@Override
	public Integer findCount() {
		String queryString = "select count(*) from userprivilege_nsql";
		Query q = entityManager.createNativeQuery(queryString);
		Integer results = (Integer) q.getSingleResult();
		return results;
	}

	@Override
	@Transactional
	public int update(long id, String profile) {
		return entityManager.createQuery("UPDATE userprivilege_nsql SET data = jsonb_set(data, '{profile}', :profile , FALSE) where id = :id")
                .setParameter("profile", profile)
                .setParameter("id", id)
                .executeUpdate();
	}
	
	@Override
	@Transactional
	public void update(String userId, String profile) {
		UserPrivilegeNsql existingEntity = this.findAll(0, 0, null, null, userId).get(0);
		if(existingEntity!=null && existingEntity.getId()!=null && userId.equalsIgnoreCase(existingEntity.getData().getUserId())) {
			existingEntity.getData().setProfile(profile);
			entityManager.merge(existingEntity);
		}else {
			UserPrivilegeNsql newEntity = new UserPrivilegeNsql();
			UserPrivilege userRecord = new UserPrivilege(userId.toLowerCase(),profile.toLowerCase());
			newEntity.setData(userRecord);
			entityManager.persist(newEntity);
		}
	}
	
}
