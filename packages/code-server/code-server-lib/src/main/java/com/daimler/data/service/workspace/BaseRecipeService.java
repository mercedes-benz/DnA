package com.daimler.data.service.workspace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.daimler.data.db.repo.workspace.WorkspaceCustomSoftwareRepo;
import com.daimler.data.assembler.RecipeAssembler;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
import com.daimler.data.db.repo.workspace.WorkspaceRecipeRepository;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.db.json.CodeServerSoftware;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.assembler.SoftwareAssembler;
import java.util.stream.Collectors;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import java.util.UUID;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import com.daimler.data.db.json.CodeServerRecipeLov;
import com.fasterxml.jackson.databind.ObjectMapper;




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

	@Autowired
	private WorkspaceCustomSoftwareRepo workspaceCustomSoftwareRepo;

	@Autowired
	private SoftwareAssembler softwareAssembler;
    
	@Override
	@Transactional
	public List<RecipeVO> getAllRecipes(int offset, int limit) {
		List<CodeServerRecipeNsql> entities = workspaceCustomRecipeRepo.findAllRecipe(offset, limit);
		return entities.stream().map(n -> recipeAssembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	public RecipeVO createRecipe(RecipeVO recipeRequestVO) {
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		String id= UUID.randomUUID().toString();
		recipeRequestVO.setId(id);
		CodeServerRecipeNsql entity = recipeAssembler.toEntity(recipeRequestVO);
		CodeServerRecipeNsql savedEntity = new CodeServerRecipeNsql();
		try {
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			entity.getData().setCreatedOn(now);
			savedEntity = recipeJpaRepo.save(entity);
		} catch (Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}
		return recipeAssembler.toVo(savedEntity);
	}

	@Override
	public RecipeVO getByRecipeName(String recipeName) {
		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(recipeName);
		return recipeAssembler.toVo(entity);
	}

	@Override
	public List<SoftwareCollection> getAllsoftwareLov()
	{
		List<CodeServerSoftwareNsql> allSoftwares = workspaceCustomSoftwareRepo.findAllSoftwareDetails();
		if(!allSoftwares.isEmpty() || allSoftwares.size()>0)
		{
			return allSoftwares.stream().map(n-> softwareAssembler.toVo(n)).collect(Collectors.toList());
		}
		else
		{
			log.info("there are no records of software ");
		}
		return null;
		

	}

	@Override
	public List<RecipeLovVO> getAllRecipeLov(String id)
	{
		List<CodeServerRecipeLov> publiclovDeatils = workspaceCustomRecipeRepo.getAllPublicRecipeLov();
		List<CodeServerRecipeLov> privatelovDetails =  workspaceCustomRecipeRepo.getAllPrivateRecipeLov(id);
		if(privatelovDetails!=null)
		{
			publiclovDeatils.addAll(privatelovDetails);
		}
		if(publiclovDeatils!=null)
		{
			return publiclovDeatils.stream().map(n-> recipeAssembler.toRecipeLovVO(n)).collect(Collectors.toList());
		}
		else
		{
			log.info("there are no recipe lov details ");
		}
		return null;

	}

}
