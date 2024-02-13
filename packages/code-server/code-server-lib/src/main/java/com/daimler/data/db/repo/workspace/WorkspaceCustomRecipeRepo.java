package com.daimler.data.db.repo.workspace;

import java.util.List;
import com.daimler.data.db.json.CodeServerRecipeLov;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;
import com.daimler.data.dto.workspace.recipe.RecipeVO;

public interface WorkspaceCustomRecipeRepo extends CommonDataRepository<CodeServerRecipeNsql, String> {

    List<CodeServerRecipeNsql> findAllRecipe(int offset, int limit);

    CodeServerRecipeNsql findByRecipeName(String recipeName);

    List<CodeServerRecipeLov> getAllPublicRecipeLov();
    List<CodeServerRecipeLov> getAllPrivateRecipeLov(String id);
    List<CodeServerRecipeNsql> findAllRecipesWithRequestedAndAcceptedState(int offset, int limit);

}
