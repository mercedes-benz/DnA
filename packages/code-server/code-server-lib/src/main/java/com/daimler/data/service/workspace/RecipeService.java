package com.daimler.data.service.workspace;

import java.util.List;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import com.daimler.data.dto.CodeServerRecipeDto;

public interface RecipeService {
    
    List<RecipeVO> getAllRecipes(int offset, int limit,String id);

	RecipeVO createRecipe(RecipeVO recipeRequestVO);
	
	RecipeVO updateRecipe(RecipeVO recipeRequestVO);

	RecipeVO getRecipeById(String id);
	
	List<SoftwareCollection> getAllsoftwareLov();

	List<RecipeLovVO> getAllRecipeLov(String Id);

	GenericMessage createOrValidateSoftwareTemplate(String gitUrl, List<String> softwares);

	// List<RecipeVO> getAllRecipesWhichAreInRequestedAndAcceptedState(int offset, int limit);

	// GenericMessage saveRecipeInfo(String name);

	// GenericMessage publishRecipeInfo(String name);
	
    GenericMessage validateGitHubUrl(String gitHubUrl);

    GenericMessage deleteRecipe(String id);

    List<AdditionalServiceLovVo> getAllAdditionalServiceLov();
}