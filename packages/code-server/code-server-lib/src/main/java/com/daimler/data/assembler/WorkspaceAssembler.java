/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.assembler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.CodeServerProjectDetails;
import com.daimler.data.db.json.CodeServerRecipeDetails;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.workspace.CodeServerDeploymentDetailsVO;
import com.daimler.data.dto.workspace.CodeServerProjectDetailsVO;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CloudServiceProviderEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CpuCapacityEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.EnvironmentEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.OperatingSystemEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RamSizeEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.UserInfoVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkspaceAssembler implements GenericAssembler<CodeServerWorkspaceVO, CodeServerWorkspaceNsql> {


	@Value("${codeServer.env.value}")
	private String codeServerEnvValue;
	
	private UserInfoVO toUserInfoVO(UserInfo userInfo) {
		UserInfoVO vo = new UserInfoVO();
		if(userInfo!=null) {
			BeanUtils.copyProperties(userInfo, vo);
		}
		return vo;
	}
	
	public UserInfo  toUserInfo(UserInfoVO userInfo) {
		UserInfo entity = new UserInfo();
		if(userInfo!=null) {
			BeanUtils.copyProperties(userInfo, entity);
		}
		return entity;
	}
	
	private CodeServerDeploymentDetails toDeploymentDetails(CodeServerDeploymentDetailsVO vo) {
		CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
		if(vo!=null) {
			BeanUtils.copyProperties(vo, deploymentDetails);
			deploymentDetails.setLastDeployedBy(toUserInfo(vo.getLastDeployedBy()));
		}
		return deploymentDetails;
	}
	
	private CodeServerDeploymentDetailsVO toDeploymentDetailsVO(CodeServerDeploymentDetails deploymentDetails) throws ParseException {
		CodeServerDeploymentDetailsVO deploymentDetailsVO = new CodeServerDeploymentDetailsVO();
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		if(deploymentDetails!=null) {
			BeanUtils.copyProperties(deploymentDetails, deploymentDetailsVO);
			deploymentDetailsVO.setLastDeployedBy(toUserInfoVO(deploymentDetails.getLastDeployedBy()));
			if(deploymentDetails.getLastDeployedOn()!=null)
				deploymentDetailsVO.setLastDeployedOn(isoFormat.parse(isoFormat.format(deploymentDetails.getLastDeployedOn())));
		}
		return deploymentDetailsVO;
	}
	
	private CodeServerRecipeDetails toRecipeDetails(CodeServerRecipeDetailsVO vo) {
		CodeServerRecipeDetails recipeDetails = new CodeServerRecipeDetails();
		if(vo!=null) {
			recipeDetails.setCpuCapacity(vo.getCpuCapacity().toString());
			recipeDetails.setCloudServiceProvider(vo.getCloudServiceProvider().toString());
			recipeDetails.setEnvironment(codeServerEnvValue);
			recipeDetails.setOperatingSystem(vo.getOperatingSystem().toString());
			recipeDetails.setRamSize(vo.getRamSize().toString());
			recipeDetails.setRecipeId(vo.getRecipeId().toString());
			recipeDetails.setResource(vo.getResource());
		}
		return recipeDetails;
	}
	
	private CodeServerRecipeDetailsVO toRecipeDetailsVO(CodeServerRecipeDetails recipe) {
		CodeServerRecipeDetailsVO recipeDetailsVO = new CodeServerRecipeDetailsVO();
		if(recipe!=null) {
			recipeDetailsVO.setCloudServiceProvider(CloudServiceProviderEnum.fromValue(recipe.getCloudServiceProvider()));
			recipeDetailsVO.setCpuCapacity(CpuCapacityEnum.fromValue(recipe.getCpuCapacity()));
			recipeDetailsVO.setEnvironment(EnvironmentEnum.fromValue(recipe.getEnvironment().toUpperCase()));
			recipeDetailsVO.setOperatingSystem(OperatingSystemEnum.fromValue(recipe.getOperatingSystem()));
			recipeDetailsVO.setRamSize(RamSizeEnum.fromValue(recipe.getRamSize()));
			recipeDetailsVO.setRecipeId(RecipeIdEnum.fromValue(recipe.getRecipeId()));
			recipeDetailsVO.setResource(recipe.getResource());
		}
		return recipeDetailsVO;
	}
	
	@Override
	public CodeServerWorkspaceVO toVo(CodeServerWorkspaceNsql entity) {
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		CodeServerWorkspaceVO vo = new CodeServerWorkspaceVO();
		try {
		if(entity!=null) {
			vo.setId(entity.getId());
			CodeServerWorkspace data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				if(data.getIntiatedOn()!=null)
					vo.setIntiatedOn(isoFormat.parse(isoFormat.format(data.getIntiatedOn())));
				UserInfo codespaceUserDetails = data.getWorkspaceOwner();
				if(codespaceUserDetails!=null) {
					UserInfoVO codespaceUser = this.toUserInfoVO(codespaceUserDetails);
					vo.setWorkspaceOwner(codespaceUser);
				}
				CodeServerProjectDetails projectDetails = data.getProjectDetails();
				CodeServerProjectDetailsVO projectDetailsVO = new CodeServerProjectDetailsVO();
					if(projectDetails!=null) {
						CodeServerDeploymentDetailsVO intDeployDetailsVO = toDeploymentDetailsVO(projectDetails.getIntDeploymentDetails());
						CodeServerDeploymentDetailsVO prodDeployDetailsVO = toDeploymentDetailsVO(projectDetails.getProdDeploymentDetails());
						projectDetailsVO.setIntDeploymentDetails(intDeployDetailsVO);
						projectDetailsVO.setProdDeploymentDetails(prodDeployDetailsVO);
						List<UserInfo> collabs = projectDetails.getProjectCollaborators();
						if(collabs!=null && !collabs.isEmpty()) {
							List<UserInfoVO> collabsVO = collabs.stream().map
									(n -> { UserInfoVO user = new UserInfoVO();
											BeanUtils.copyProperties(n,user);
											return user;
									}).collect(Collectors.toList());
							projectDetailsVO.setProjectCollaborators(collabsVO);
							}
						UserInfoVO projectOwnerVO = this.toUserInfoVO(projectDetails.getProjectOwner());
						projectDetailsVO.setProjectOwner(projectOwnerVO);
						CodeServerRecipeDetails recipeDetails = projectDetails.getRecipeDetails();
						CodeServerRecipeDetailsVO recipeVO = this.toRecipeDetailsVO(recipeDetails);
						projectDetailsVO.setRecipeDetails(recipeVO);
						projectDetailsVO.setProjectName(projectDetails.getProjectName());
						projectDetailsVO.setGitRepoName(projectDetails.getGitRepoName());
						if(projectDetails.getProjectCreatedOn()!=null)
							projectDetailsVO.setProjectCreatedOn(isoFormat.parse(isoFormat.format(projectDetails.getProjectCreatedOn())));
					}
					vo.setProjectDetails(projectDetailsVO);
					
			}
		}
		}catch(Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}
		return vo;
	}

	@Override
	public CodeServerWorkspaceNsql toEntity(CodeServerWorkspaceVO vo) {
		CodeServerWorkspaceNsql entity = null;
		if(vo!=null) {
			entity = new CodeServerWorkspaceNsql();
			CodeServerWorkspace data = new CodeServerWorkspace();
			entity.setId(vo.getId());
			BeanUtils.copyProperties(vo,data);
			UserInfoVO ownerVO = vo.getWorkspaceOwner();
			if(ownerVO!=null) {
				UserInfo owner = this.toUserInfo(ownerVO);
				data.setWorkspaceOwner(owner);
			}
			CodeServerProjectDetailsVO projectDetailsVO = vo.getProjectDetails();
			if(projectDetailsVO!=null) {
				CodeServerProjectDetails projectDetails = new CodeServerProjectDetails();
				BeanUtils.copyProperties(projectDetailsVO,projectDetails);
				UserInfoVO projectOwnerVO = projectDetailsVO.getProjectOwner();
				if(projectOwnerVO!=null) {
					UserInfo projectOwner = this.toUserInfo(projectOwnerVO);
					projectDetails.setProjectOwner(projectOwner);
				}
				List<UserInfoVO> projectCollabsVO = projectDetailsVO.getProjectCollaborators();
				if(projectCollabsVO!=null && !projectCollabsVO.isEmpty()) {
					List<UserInfo> projectCollabs = projectCollabsVO.stream().map(n -> toUserInfo(n)).collect(Collectors.toList());
					projectDetails.setProjectCollaborators(projectCollabs);
				}
				CodeServerRecipeDetailsVO recipeDetailsVO = projectDetailsVO.getRecipeDetails();
				if(recipeDetailsVO!=null) {
					CodeServerRecipeDetails recipeDetails = this.toRecipeDetails(recipeDetailsVO);
					projectDetails.setRecipeDetails(recipeDetails);
				}
				CodeServerDeploymentDetailsVO intDeploymentDetailsVO = projectDetailsVO.getIntDeploymentDetails();
				if(intDeploymentDetailsVO!=null) {
					CodeServerDeploymentDetails intDetails = this.toDeploymentDetails(intDeploymentDetailsVO);
					projectDetails.setIntDeploymentDetails(intDetails);
				}
				CodeServerDeploymentDetailsVO prodDeploymentDetailsVO = projectDetailsVO.getProdDeploymentDetails();
				if(prodDeploymentDetailsVO!=null) {
					CodeServerDeploymentDetails prodDetails = this.toDeploymentDetails(prodDeploymentDetailsVO);
					projectDetails.setProdDeploymentDetails(prodDetails);
				}
				data.setProjectDetails(projectDetails);
				entity.setData(data);
			}
		}
		return entity;
	}

	
}
