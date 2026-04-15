package com.daimler.data.db.repo.ddxDataProductsDetails;

import com.daimler.data.db.entities.DdxDataProductsDetailsNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class DdxDataProductsDetailsCustomRepositoryImpl extends CommonDataRepositoryImpl<DdxDataProductsDetailsNsql, String> implements DdxDataProductsDetailsCustomRepository {

}
