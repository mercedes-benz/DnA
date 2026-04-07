package com.daimler.data.db.repo.workspace;

import java.util.List;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface WorkspaceCustomSoftwareRepo extends CommonDataRepository<CodeServerSoftwareNsql, String> {

    List<CodeServerSoftwareNsql> findAllSoftwareDetails();
    CodeServerSoftwareNsql findBySoftwareName(String softwareName);
    CodeServerSoftwareNsql findBySoftwareId(String id);
    GenericMessage deleteSoftware(CodeServerSoftwareNsql software);
}
