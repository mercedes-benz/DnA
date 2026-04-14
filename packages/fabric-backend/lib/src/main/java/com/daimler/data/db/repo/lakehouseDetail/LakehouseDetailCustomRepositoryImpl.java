package com.daimler.data.db.repo.lakehouseDetail;

import com.daimler.data.db.entities.LakehouseDetailNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class LakehouseDetailCustomRepositoryImpl extends CommonDataRepositoryImpl<LakehouseDetailNsql, String> implements LakehouseDetailCustomRepository {

}
