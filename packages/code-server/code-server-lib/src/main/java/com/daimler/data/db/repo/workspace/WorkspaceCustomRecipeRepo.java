package com.daimler.data.db.repo.workspace;

import java.util.List;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.workspace.recipe.RecipeVO;

public interface WorkspaceCustomRecipeRepo extends CommonDataRepository<CodeServerRecipeNsql, String> {

    List<CodeServerRecipeNsql> findAllRecipe(int offset, int limit);

    CodeServerRecipeNsql findByRecipeName(String recipeName);

    List<CodeServerRecipeDto> getAllPublicRecipeLov();
    List<CodeServerRecipeDto> getAllPrivateRecipeLov(String id);
    // List<CodeServerRecipeNsql> findAllRecipesWithRequestedAndAcceptedState(int offset, int limit);
    // GenericMessage updateRecipeInfo(String name,String status);

    GenericMessage deleteRecipe(CodeServerRecipeNsql recipe);

    String findBySoftwareName(String addInfo);

}
