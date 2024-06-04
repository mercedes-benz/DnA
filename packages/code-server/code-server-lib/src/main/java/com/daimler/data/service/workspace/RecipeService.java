package com.daimler.data.service.workspace;

import java.util.List;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import com.daimler.data.dto.CodeServerRecipeDto;

public interface RecipeService {
    
    List<RecipeVO> getAllRecipes(int offset, int limit);

	RecipeVO createRecipe(RecipeVO recipeRequestVO);

	RecipeVO getByRecipeName(String recipeName);
	
	List<SoftwareCollection> getAllsoftwareLov();

	List<RecipeLovVO> getAllRecipeLov(String Id);

	// List<RecipeVO> getAllRecipesWhichAreInRequestedAndAcceptedState(int offset, int limit);

	// GenericMessage saveRecipeInfo(String name);

	// GenericMessage publishRecipeInfo(String name);
	
    GenericMessage validateGitHubUrl(String gitHubUrl);

    GenericMessage deleteRecipe(String recipeName);
}