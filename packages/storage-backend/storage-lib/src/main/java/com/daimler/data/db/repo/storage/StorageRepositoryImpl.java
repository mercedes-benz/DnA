package com.daimler.data.db.repo.storage;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.StorageNsql;

@Repository
public class StorageRepositoryImpl implements StorageRepository{

	@PersistenceContext
	protected EntityManager em;
	
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

}
