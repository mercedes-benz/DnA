package com.daimler.data.db.repo.workspace;

import java.util.List;

public interface WorkspaceCustomUserGroupRepo {

     List<String> findByWsid(String wsid,String userId);

}
