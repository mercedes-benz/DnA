package com.daimler.data.db.repo.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Root;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;

public class CommonDataRepositoryImpl<T, ID> implements CommonDataRepository<T, ID> {

	public enum SORT_TYPE {
		ASC, DESC;
	}

	@PersistenceContext
	protected EntityManager em;

	protected Class<T> entityClass;

	private static final Logger LOG = LoggerFactory.getLogger(CommonDataRepositoryImpl.class);

	@SuppressWarnings("unchecked")
	public CommonDataRepositoryImpl() {
		this.entityClass = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0]);
	}

	@Override
	public void deleteAll() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaDelete<T> bulkDelete = cb.createCriteriaDelete(entityClass);
		Root<T> root = bulkDelete.from(entityClass);
		int rowsDeleted = em.createQuery(bulkDelete).executeUpdate();
		LOG.info("Number of deleted rows " + rowsDeleted);
	}

	@Override
	public void insertAll(List<T> tNsqlList) {
		if (Objects.nonNull(tNsqlList) && !tNsqlList.isEmpty()) {
			tNsqlList.forEach(t -> em.persist(t));
		}
	}

	@Override
	public void update(T entity) {
		em.merge(entity);
	}

	@Override
	public T findbyUniqueLiteral(String uniqueliteralName, String value) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<T> byName = cq.select(root);
		Predicate con1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal(uniqueliteralName))),
				value.toLowerCase());
		cq.where(con1);
		TypedQuery<T> byNameQuery = em.createQuery(byName);
		List<T> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities.get(0);
		else
			return null;
	}

	@Override
	public List<T> findAllSortyByUniqueLiteral(String uniqueLiteralName, SORT_TYPE sortOrder) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root);
		if (sortOrder == SORT_TYPE.ASC) {
			cq.orderBy(cb.asc(cb.function("jsonb_extract_path_text", String.class, root.get("data"),
					cb.literal(uniqueLiteralName))));
		}
		if (sortOrder == SORT_TYPE.DESC) {
			cq.orderBy(cb.desc(cb.function("jsonb_extract_path_text", String.class, root.get("data"),
					cb.literal(uniqueLiteralName))));
		}
		TypedQuery<T> byNameQuery = em.createQuery(cq);
		List<T> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities;
		else
			return null;
	}

	@Override
	public List<T> findAllSortyByUniqueLiteral(int limit, int offset, String uniqueLiteralName, SORT_TYPE sortOrder) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root);
		if (sortOrder == SORT_TYPE.ASC) {
			if (uniqueLiteralName.equals("id")) {
				cq.orderBy(cb.asc(root.get("id")));
			} else {
				cq.orderBy(cb.asc(cb.function("jsonb_extract_path_text", String.class, root.get("data"),
						cb.literal(uniqueLiteralName))));
			}
		}
		if (sortOrder == SORT_TYPE.DESC) {
			if (uniqueLiteralName.equals("id")) {
				cq.orderBy(cb.desc(root.get("id")));
			} else {
				cq.orderBy(cb.desc(cb.function("jsonb_extract_path_text", String.class, root.get("data"),
						cb.literal(uniqueLiteralName))));

			}
		}
		TypedQuery<T> byNameQuery = em.createQuery(cq);
		if (offset >= 0)
			byNameQuery.setFirstResult(offset);
		if (limit > 0)
			byNameQuery.setMaxResults(limit);
		List<T> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities;
		else
			return null;
	}

	@Override
	public List<T> findAll(int limit, int offset) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<T> getAll = cq.select(root);
		TypedQuery<T> getAllQuery = em.createQuery(getAll);
		if (offset >= 0)
			getAllQuery.setFirstResult(offset);
		if (limit > 0)
			getAllQuery.setMaxResults(limit);
		return getAllQuery.getResultList();
	}

	@Override
	public Long getCount(int limit, int offset) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<Long> getAll = cq.select(cb.count(root));
		TypedQuery<Long> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getSingleResult();
	}
}

