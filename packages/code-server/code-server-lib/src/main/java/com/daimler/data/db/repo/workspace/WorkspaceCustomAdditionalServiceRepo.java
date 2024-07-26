package com.daimler.data.db.repo.workspace;

import java.util.List;

import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface WorkspaceCustomAdditionalServiceRepo extends CommonDataRepository<CodeServerAdditionalServiceNsql, String>  {
    
    List<CodeServerAdditionalServiceNsql> findAllServices(int offset, int limit);
    String findByServiceName(String serviceName);
}
