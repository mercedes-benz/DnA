package com.daimler.data.db.repo.workspace;

import java.util.List;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceLovVo;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface WorkspaceCustomAdditionalServiceRepo extends CommonDataRepository<CodeServerAdditionalServiceNsql, String>  {
    
    List<CodeServerAdditionalServiceNsql> findAllServices(int offset, int limit);
    String findByServiceName(String serviceName);
    CodeServerAdditionalServiceNsql findByAddServiceName(String serviceName);    
    CodeServerAdditionalServiceNsql findByAddServiceId(String id);
    GenericMessage deleteAddService(CodeServerAdditionalServiceNsql addService);
}
