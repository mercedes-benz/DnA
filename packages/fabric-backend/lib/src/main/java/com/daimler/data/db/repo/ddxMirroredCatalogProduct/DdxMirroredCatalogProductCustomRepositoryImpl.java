package com.daimler.data.db.repo.ddxMirroredCatalogProduct;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.DdxMirroredCatalogProductNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class DdxMirroredCatalogProductCustomRepositoryImpl
        extends CommonDataRepositoryImpl<DdxMirroredCatalogProductNsql, String>
        implements DdxMirroredCatalogProductCustomRepository {

    @Override
    public DdxMirroredCatalogProductNsql findByCatalogName(String catalogName) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<DdxMirroredCatalogProductNsql> cq = cb.createQuery(DdxMirroredCatalogProductNsql.class);
            Root<DdxMirroredCatalogProductNsql> root = cq.from(DdxMirroredCatalogProductNsql.class);
            Predicate condition = cb.equal(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("catalogName")),
                catalogName);
            cq.where(condition);
            TypedQuery<DdxMirroredCatalogProductNsql> query = em.createQuery(cq);
            List<DdxMirroredCatalogProductNsql> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                return results.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching by catalogName: {}", catalogName, e);
            return null;
        }
    }

    @Override
    public DdxMirroredCatalogProductNsql findByCorrelationId(String ddxCorrelationId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<DdxMirroredCatalogProductNsql> cq = cb.createQuery(DdxMirroredCatalogProductNsql.class);
            Root<DdxMirroredCatalogProductNsql> root = cq.from(DdxMirroredCatalogProductNsql.class);
            Predicate condition = cb.equal(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("ddxCorrelationId")),
                ddxCorrelationId);
            cq.where(condition);
            TypedQuery<DdxMirroredCatalogProductNsql> query = em.createQuery(cq);
            List<DdxMirroredCatalogProductNsql> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                return results.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching by ddxCorrelationId: {}", ddxCorrelationId, e);
            return null;
        }
    }

    @Override
    public List<DdxMirroredCatalogProductNsql> findByDdxIdAndLakehouseId(String ddxId, String lakehouseId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<DdxMirroredCatalogProductNsql> cq = cb.createQuery(DdxMirroredCatalogProductNsql.class);
            Root<DdxMirroredCatalogProductNsql> root = cq.from(DdxMirroredCatalogProductNsql.class);
            Predicate ddxIdCondition = cb.equal(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("ddxId")),
                ddxId);
            Predicate lakehouseIdCondition = cb.equal(
                cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("lakehouseId")),
                lakehouseId);
            cq.where(cb.and(ddxIdCondition, lakehouseIdCondition));
            TypedQuery<DdxMirroredCatalogProductNsql> query = em.createQuery(cq);
            List<DdxMirroredCatalogProductNsql> results = query.getResultList();
            return results != null ? results : List.of();
        } catch (Exception e) {
            log.error("Error fetching by ddxId: {} and lakehouseId: {}", ddxId, lakehouseId, e);
            return List.of();
        }
    }
}
