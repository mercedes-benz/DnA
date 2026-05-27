package com.daimler.data.db.repo.ddxMirroredCatalogProduct;

import java.util.List;
import java.util.Optional;

import com.daimler.data.db.entities.DdxMirroredCatalogProductNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface DdxMirroredCatalogProductCustomRepository extends CommonDataRepository<DdxMirroredCatalogProductNsql, String> {

    Optional<DdxMirroredCatalogProductNsql> findByCatalogName(String dataProductName);

    Optional<DdxMirroredCatalogProductNsql> findByCorrelationId(String ddxCorrelationId);

    List<DdxMirroredCatalogProductNsql> findByMirroredCatalogId(String mirroredCatalogId);
}
