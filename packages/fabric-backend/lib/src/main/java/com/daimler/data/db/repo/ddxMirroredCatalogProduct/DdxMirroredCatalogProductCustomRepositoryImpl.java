package com.daimler.data.db.repo.ddxMirroredCatalogProduct;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.NoResultException;

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
    public Optional<DdxMirroredCatalogProductNsql> findByCatalogName(String dataProductName) {
        String sql = "SELECT * FROM ddx_mirrored_catalog_product_nsql" +
                     " WHERE data ->> 'dataProductName' = :dataProductName";
        try {
            DdxMirroredCatalogProductNsql result = (DdxMirroredCatalogProductNsql) em
                .createNativeQuery(sql, DdxMirroredCatalogProductNsql.class)
                .setParameter("dataProductName", dataProductName)
                .getSingleResult();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            if (e instanceof NoResultException) {
                log.debug("No result found for dataProductName: {}", dataProductName);
            } else {
                log.error("Error fetching by dataProductName: {}", dataProductName, e);
            }
            return Optional.empty();
        }
    }

    @Override
    public Optional<DdxMirroredCatalogProductNsql> findByCorrelationId(String ddxCorrelationId) {
        String sql = "SELECT * FROM ddx_mirrored_catalog_product_nsql " +
                     "WHERE data ->> 'ddxCorrelationId' = :ddxCorrelationId";
        try {
            DdxMirroredCatalogProductNsql result = (DdxMirroredCatalogProductNsql) em
                .createNativeQuery(sql, DdxMirroredCatalogProductNsql.class)
                .setParameter("ddxCorrelationId", ddxCorrelationId)
                .getSingleResult();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            if (e instanceof NoResultException) {
                log.debug("No result found for ddxCorrelationId: {}", ddxCorrelationId);
            } else {
                log.error("Error fetching by ddxCorrelationId: {}", ddxCorrelationId, e);
            }
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DdxMirroredCatalogProductNsql> findByMirroredCatalogId(String mirroredCatalogId) {
        String sql = "SELECT * FROM ddx_mirrored_catalog_product_nsql " +
                     "WHERE data -> 'mirrorCatalogDetails' ->> 'mirroredCatalogId' = :mirroredCatalogId";
        try {
            List<DdxMirroredCatalogProductNsql> results = em
                .createNativeQuery(sql, DdxMirroredCatalogProductNsql.class)
                .setParameter("mirroredCatalogId", mirroredCatalogId)
                .getResultList();
            return results != null ? results : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching by mirroredCatalogId: {}", mirroredCatalogId, e);
            return Collections.emptyList();
        }
    }
}
