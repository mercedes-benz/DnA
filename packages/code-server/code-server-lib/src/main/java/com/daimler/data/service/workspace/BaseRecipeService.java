package com.daimler.data.service.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.RecipeAssembler;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
import com.daimler.data.db.repo.workspace.WorkspaceRecipeRepository;
import com.daimler.data.dto.workspace.recipe.RecipeVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseRecipeService implements RecipeService{

	@Autowired
	private WorkspaceRecipeRepository recipeJpaRepo;

	@Autowired
	private RecipeAssembler recipeAssembler;

	@Autowired
	private WorkspaceCustomRecipeRepo workspaceCustomRecipeRepo;
    
	@Override
	@Transactional
	public List<RecipeVO> getAllRecipes(int offset, int limit) {
		List<CodeServerRecipeNsql> entities = workspaceCustomRecipeRepo.findAllRecipe(offset, limit);
		return entities.stream().map(n -> recipeAssembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	public RecipeVO createRecipe(RecipeVO recipeRequestVO) {
		CodeServerRecipeNsql entity = recipeAssembler.toEntity(recipeRequestVO);
		CodeServerRecipeNsql savedEntity = recipeJpaRepo.save(entity);
		return recipeAssembler.toVo(savedEntity);
	}

	@Override
	public RecipeVO getByRecipeName(String recipeName) {
		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(recipeName);
		return recipeAssembler.toVo(entity);
	}
    
}
