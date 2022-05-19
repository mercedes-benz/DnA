package com.daimler.data.db.repo.storage;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.StorageNsql;
import com.daimler.data.db.jsonb.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class StorageRepositoryImpl implements StorageRepository {

	private static Logger logger = LoggerFactory.getLogger(StorageRepositoryImpl.class);

	@PersistenceContext
	protected EntityManager em;
	
	private static final String storage_nsql = "storage_nsql";

	@Override
	public StorageNsql findbyUniqueLiteral(String uniqueliteralName, String value) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<StorageNsql> cq = cb.createQuery(StorageNsql.class);
		Root<StorageNsql> root = cq.from(StorageNsql.class);
		CriteriaQuery<StorageNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal(uniqueliteralName))),
				value.toLowerCase());
		cq.where(con1);
		TypedQuery<StorageNsql> byNameQuery = em.createQuery(byName);
		List<StorageNsql> entities = byNameQuery.getResultList();
		if (entities != null && !entities.isEmpty())
			return entities.get(0);
		else
			return null;
	}

	@Override
	public List<StorageNsql> getAllWithFilters(String userId) {
		Query q = getNativeQueryWithFilters("", userId);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();

		return results.stream().map(temp -> {
			StorageNsql entity = new StorageNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				Storage storage = mapper.readValue(jsonData, Storage.class);
				entity.setData(storage);
			} catch (Exception e) {
				logger.error("Failed while fetching all buckets using native {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).toList();
	}

	private Query getNativeQueryWithFilters(String selectFieldsString, String userId) {
		String prefix = StringUtils.hasText(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "from " + storage_nsql;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(userId);
		String query = prefix + basicpredicate + consolidatedPredicates;
		return em.createNativeQuery(query);
	}

	private String buildPredicateString(String userId) {
		String consolidatedQuery = "";
		if (StringUtils.hasLength(userId)) {
			String isCreator = " lower(jsonb_extract_path_text(data,'createdBy','id')) like " + "'%"
					+ userId.toLowerCase() + "%'";

			String isCollaborator = " lower(jsonb_extract_path_text(data,'collaborators')) similar to '"
					+ userId.toLowerCase() + "'";

			consolidatedQuery = " and " + isCreator + " or " + isCollaborator;
		}
		return consolidatedQuery;
	}

}
