package com.daimler.data.db.repo.DbService;


import java.util.List;

import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface DbServiceCustomRepository extends CommonDataRepository<DbServiceNsql,String> {

    List<DbServiceNsql> findAllDbService(int offset, int limit,String id);

    String updateDeleteStatus(DbServiceNsql entity);

}
