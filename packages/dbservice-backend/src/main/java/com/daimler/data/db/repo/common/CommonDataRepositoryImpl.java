package com.daimler.data.db.repo.common;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class CommonDataRepositoryImpl<E, K> implements CommonDataRepository<E, K> {

    public enum SORT_TYPE {
        ASC, DESC;
    }

    @PersistenceContext
    protected EntityManager em;

    protected Class<E> entityClass;

    private static final Logger LOG = LoggerFactory.getLogger(CommonDataRepositoryImpl.class);

    @SuppressWarnings("unchecked")
    public CommonDataRepositoryImpl() {
        this.entityClass = ((Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]);
    }

    @Override
    public void deleteAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<E> deleteQuery = cb.createCriteriaDelete(entityClass);
        deleteQuery.from(entityClass);
        int rowsDeleted = em.createQuery(deleteQuery).executeUpdate();
        LOG.info("Number of rows deleted: {}", rowsDeleted);
    }

    @Override
    public void insertAll(List<E> entities) {
        if (Objects.nonNull(entities) && !entities.isEmpty()) {
            entities.forEach(em::persist);
        }
    }

    @Override
    public E findByUniqueColumn(String columnName, String value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        cq.select(root).where(cb.equal(root.get(columnName), value));
        TypedQuery<E> query = em.createQuery(cq);
        List<E> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<E> findAllSortedByColumn(String columnName, SORT_TYPE sortOrder) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        cq.select(root);

        if (SORT_TYPE.ASC.equals(sortOrder)) {
            cq.orderBy(cb.asc(root.get(columnName)));
        } else {
            cq.orderBy(cb.desc(root.get(columnName)));
        }

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<E> findAllSortedByColumn(int limit, int offset, String columnName, SORT_TYPE sortOrder) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        cq.select(root);

        if (SORT_TYPE.ASC.equals(sortOrder)) {
            cq.orderBy(cb.asc(root.get(columnName)));
        } else {
            cq.orderBy(cb.desc(root.get(columnName)));
        }

        TypedQuery<E> query = em.createQuery(cq);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public void update(E entity) {
        em.merge(entity);
    }

    @Override
    public List<E> findAll(int limit, int offset) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        cq.select(root);

        TypedQuery<E> query = em.createQuery(cq);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public Long getCount() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<E> root = cq.from(entityClass);
        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public E findById(K id) {
        return em.find(entityClass, id);
    }
}
