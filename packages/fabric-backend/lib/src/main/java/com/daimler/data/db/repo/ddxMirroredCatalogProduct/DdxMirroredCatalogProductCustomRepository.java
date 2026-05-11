package com.daimler.data.db.repo.ddxMirroredCatalogProduct;

import java.util.List;

import com.daimler.data.db.entities.DdxMirroredCatalogProductNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface DdxMirroredCatalogProductCustomRepository extends CommonDataRepository<DdxMirroredCatalogProductNsql, String> {

    DdxMirroredCatalogProductNsql findByCatalogName(String catalogName);

    DdxMirroredCatalogProductNsql findByCorrelationId(String ddxCorrelationId);

    List<DdxMirroredCatalogProductNsql> findByDdxIdAndLakehouseId(String ddxId, String lakehouseId);
}
