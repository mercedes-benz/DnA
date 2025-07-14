package com.daimler.data.db.repo.workspace;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface WorkspaceCustomBuildDeployRepo extends CommonDataRepository<CodeServerBuildDeployNsql,String> {

    CodeServerBuildDeployNsql findByProjectName(String projectName);

}
