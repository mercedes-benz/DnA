package com.daimler.data.assembler;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;

import com.daimler.data.dto.workspace.recipe.RecipeVO.OSNameEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.json.CodeServerRecipe;
import com.daimler.data.db.json.RecipeSoftware;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.dto.workspace.recipe.RecipeSoftwareVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RecipeAssembler implements GenericAssembler<RecipeVO, CodeServerRecipeNsql> {

	@Override
	public RecipeVO toVo(CodeServerRecipeNsql entity) {
		// TODO Auto-generated method stub
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		RecipeVO recipeVo = new RecipeVO();
			if (Objects.nonNull(entity) && Objects.nonNull(entity.getData())) {
				CodeServerRecipe recipe = entity.getData();
				BeanUtils.copyProperties(recipe, recipeVo);
				List<RecipeSoftware> softwares = recipe.getSoftware();
				List<RecipeSoftwareVO> softwareVos = new ArrayList<>();
				if (Objects.nonNull(softwares)) {
					for (RecipeSoftware software : softwares) {
						RecipeSoftwareVO softwareVo = new RecipeSoftwareVO();
						BeanUtils.copyProperties(software, softwareVo);
						softwareVos.add(softwareVo);
					}
					recipeVo.setSoftware(softwareVos);
				}
				UserInfoVO userInfoVo = new UserInfoVO();
				UserInfo userInfo = recipe.getCreatedBy();
				if (Objects.nonNull(userInfo)) {
					BeanUtils.copyProperties(userInfo, userInfoVo);
					recipeVo.setCreatedBy(userInfoVo);
				}
				if (recipe.getOSName() != null) {
					recipeVo.setOSName(OSNameEnum.fromValue(recipe.getOSName()));
				}
				if (recipe.getPlugins() != null) {
					recipeVo.setPlugins(recipe.getPlugins());
				}
		return recipeVo;
	}

	@Override
	public CodeServerRecipeNsql toEntity(RecipeVO vo) {
		// TODO Auto-generated method stub
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		CodeServerRecipeNsql entity = new CodeServerRecipeNsql();
		CodeServerRecipe recipeData = new CodeServerRecipe();
		if (Objects.nonNull(vo)) {
			BeanUtils.copyProperties(vo, recipeData);
			List<RecipeSoftwareVO> softwares = vo.getSoftware();
			List<RecipeSoftware> softwareData = new ArrayList<>();
			if (Objects.nonNull(softwares)) {
				for (RecipeSoftwareVO software : softwares) {
					RecipeSoftware softwareVo = new RecipeSoftware();
					BeanUtils.copyProperties(software, softwareVo);
					softwareData.add(softwareVo);
				}
				recipeData.setSoftware(softwareData);
			}
			UserInfo userInfo = new UserInfo();
			UserInfoVO userInfoVo = vo.getCreatedBy();
			if (Objects.nonNull(userInfoVo)) {
				BeanUtils.copyProperties(userInfoVo, userInfo);
				recipeData.setCreatedBy(userInfo);
			}
			if (vo.getPlugins() != null) {
				recipeData.setPlugins(vo.getPlugins());
			}
			recipeData.setOSName(vo.getOSName().toString());
			entity.setData(recipeData);
		}
		return entity;
	}
}
