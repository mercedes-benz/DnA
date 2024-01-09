package com.daimler.data.service.workspace;

import java.util.List;
import com.daimler.data.dto.workspace.recipe.RecipeVO;

public interface RecipeService {
    
    List<RecipeVO> getAllRecipes(int offset, int limit);

	RecipeVO createRecipe(RecipeVO recipeRequestVO);

	RecipeVO getByRecipeName(String recipeName);
}