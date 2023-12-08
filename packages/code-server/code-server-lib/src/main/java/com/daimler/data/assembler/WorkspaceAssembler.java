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
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.daimler.data.dto.workspace.RoleCollectionVO;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.CodeServerProjectDetails;
import com.daimler.data.db.json.CodeServerRecipeDetails;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.json.CodespaceSecurityConfig;
import com.daimler.data.db.json.CodespaceSecurityConfigDetailsLOV;
import com.daimler.data.db.json.CodespaceSecurityEntitlement;
import com.daimler.data.db.json.CodespaceSecurityApiList;
import com.daimler.data.db.json.CodespaceSecurityRole;
import com.daimler.data.db.json.CodespaceSecurityUserRoleMap;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.CodespaceSecurityConfigDto;
import com.daimler.data.dto.workspace.CodeServerDeploymentDetailsVO;
import com.daimler.data.dto.workspace.CodeServerProjectDetailsVO;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO;
import com.daimler.data.dto.workspace.CodespaceSecurityConfigLOV;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CloudServiceProviderEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CpuCapacityEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.EnvironmentEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.OperatingSystemEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RamSizeEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
import com.daimler.data.dto.workspace.CodespaceSecurityApiListVO.HttpMethodEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.CodespaceSecurityConfigVO;
import com.daimler.data.dto.workspace.CodespaceSecurityEntitlementVO;
import com.daimler.data.dto.workspace.CodespaceSecurityApiListVO;

import com.daimler.data.dto.workspace.CodespaceSecurityRoleVO;
import com.daimler.data.dto.workspace.CodespaceSecurityUserRoleMapResponseVO;
import com.daimler.data.dto.workspace.CodespaceSecurityUserRoleMapVO;
import com.daimler.data.dto.workspace.UserInfoVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkspaceAssembler implements GenericAssembler<CodeServerWorkspaceVO, CodeServerWorkspaceNsql> {

	@Value("${codeServer.env.value}")
	private String codeServerEnvValue;

	private UserInfoVO toUserInfoVO(UserInfo userInfo) {
		UserInfoVO vo = new UserInfoVO();
		if (userInfo != null) {
			BeanUtils.copyProperties(userInfo, vo);
		}
		return vo;
	}

	public UserInfo toUserInfo(UserInfoVO userInfo) {
		UserInfo entity = new UserInfo();
		if (userInfo != null) {
			BeanUtils.copyProperties(userInfo, entity);
		}
		return entity;
	}

	public CodespaceSecurityRole toRole(CodespaceSecurityRoleVO roleVO) {
		CodespaceSecurityRole entity = new CodespaceSecurityRole();
		if (roleVO != null) {
			BeanUtils.copyProperties(roleVO, entity);
			List<CodespaceSecurityConfigLOV> listOfEntitlements = roleVO.getRoleEntitlements();
			if (listOfEntitlements != null) {
				List<CodespaceSecurityConfigDetailsLOV> listOfroleEntitlements = listOfEntitlements.stream()
						.map(n -> toCodespaceSecurityConfigDetailsLOV(n))
						.collect(Collectors.toList());
				entity.setRoleEntitlements(listOfroleEntitlements);
			}
		}
		return entity;
	}

	public CodespaceSecurityConfigDetailsLOV toCodespaceSecurityConfigDetailsLOV(
			CodespaceSecurityConfigLOV securityConfigLOV) {
		CodespaceSecurityConfigDetailsLOV securityConfigDetails = new CodespaceSecurityConfigDetailsLOV();
		if (securityConfigLOV != null) {
			BeanUtils.copyProperties(securityConfigLOV, securityConfigDetails);
		}
		return securityConfigDetails;
	}

	public CodespaceSecurityEntitlement toEntitlement(CodespaceSecurityEntitlementVO entitlementVO) {
		CodespaceSecurityEntitlement entity = new CodespaceSecurityEntitlement();
		if (entitlementVO != null) {
			BeanUtils.copyProperties(entitlementVO, entity);
			List<CodespaceSecurityApiListVO> apiListVO = entitlementVO.getApiList();
			if (apiListVO != null) {
				List<CodespaceSecurityApiList> apiLists = apiListVO.stream().map(n -> toApiList(n))
						.collect(Collectors.toList());
				entity.setApiList(apiLists);
			}
		}
		return entity;
	}

	public CodespaceSecurityApiList toApiList(CodespaceSecurityApiListVO apiListVO) {
		CodespaceSecurityApiList apiList = new CodespaceSecurityApiList();
		if (apiListVO != null) {
			apiList.setApiPattern(apiListVO.getApiPattern());
			apiList.setHttpMethod(apiListVO.getHttpMethod().toString());
		}
		return apiList;
	}

	public CodespaceSecurityEntitlementVO toEntitlementVO(CodespaceSecurityEntitlement entitlement) {
		CodespaceSecurityEntitlementVO entitlementVO = new CodespaceSecurityEntitlementVO();
		if (entitlementVO != null) {
			BeanUtils.copyProperties(entitlement, entitlementVO);
			List<CodespaceSecurityApiList> apiLists = entitlement.getApiList();
			if (apiLists != null) {
				List<CodespaceSecurityApiListVO> apiListVO = apiLists.stream().map(n -> toApiListVO(n))
						.collect(Collectors.toList());
				entitlementVO.setApiList(apiListVO);
			} else {
				entitlementVO.setApiList(new ArrayList<>());
			}
		}
		return entitlementVO;
	}

	public CodespaceSecurityRoleVO toRoleVO(CodespaceSecurityRole role) {
		CodespaceSecurityRoleVO roleVO = new CodespaceSecurityRoleVO();
		if (role != null) {
			BeanUtils.copyProperties(role, roleVO);
			List<CodespaceSecurityConfigDetailsLOV> roleEntitlementList = role.getRoleEntitlements();
			if (roleEntitlementList != null) {
				List<CodespaceSecurityConfigLOV> listOfRoleEntitlements = roleEntitlementList.stream()
						.map(n -> toCodespaceSecurityConfigLOV(n))
						.collect(Collectors.toList());
				roleVO.setRoleEntitlements(listOfRoleEntitlements);
			} else {
				roleVO.setRoleEntitlements(new ArrayList<>());
			}
		}
		return roleVO;
	}

	public CodespaceSecurityConfigLOV toCodespaceSecurityConfigLOV(
			CodespaceSecurityConfigDetailsLOV securityConfigDetailsLOV) {
		CodespaceSecurityConfigLOV securityConfigLOV = new CodespaceSecurityConfigLOV();
		if (securityConfigDetailsLOV != null) {
			BeanUtils.copyProperties(securityConfigDetailsLOV, securityConfigLOV);
		}
		return securityConfigLOV;
	}

	public CodespaceSecurityApiListVO toApiListVO(CodespaceSecurityApiList apiList) {
		CodespaceSecurityApiListVO apiListVO = new CodespaceSecurityApiListVO();
		if (apiList != null) {
			apiListVO.setApiPattern(apiList.getApiPattern());
			apiListVO.setHttpMethod(HttpMethodEnum.fromValue(apiList.getHttpMethod()));
		}
		return apiListVO;
	}

	public CodespaceSecurityUserRoleMap toUserRoleMap(CodespaceSecurityUserRoleMapVO userRoleMapVO) {
		CodespaceSecurityUserRoleMap entity = new CodespaceSecurityUserRoleMap();
		if (userRoleMapVO != null) {
			BeanUtils.copyProperties(userRoleMapVO, entity);
			List<CodespaceSecurityConfigLOV> roleListVO = userRoleMapVO.getRoles();
			if (roleListVO != null) {
				List<CodespaceSecurityConfigDetailsLOV> listOfRoles = roleListVO.stream()
						.map(n -> toCodespaceSecurityConfigDetailsLOV(n))
						.collect(Collectors.toList());
				entity.setRoles(listOfRoles);
				;
			}
		}
		return entity;
	}

	public CodespaceSecurityUserRoleMapVO toUserRoleMapVO(CodespaceSecurityUserRoleMap userRoleMap) {
		CodespaceSecurityUserRoleMapVO ueserRoleMapVO = new CodespaceSecurityUserRoleMapVO();
		if (userRoleMap != null) {
			BeanUtils.copyProperties(userRoleMap, ueserRoleMapVO);
			List<CodespaceSecurityConfigDetailsLOV> listOfRoles = userRoleMap.getRoles();
			if (listOfRoles != null) {
				List<CodespaceSecurityConfigLOV> listOfRolesVO = listOfRoles.stream()
						.map(n -> toCodespaceSecurityConfigLOV(n)).collect(Collectors.toList());
				ueserRoleMapVO.setRoles(listOfRolesVO);
			} else {
				ueserRoleMapVO.setRoles(new ArrayList<>());
			}
		}
		return ueserRoleMapVO;
	}

	private CodeServerDeploymentDetails toDeploymentDetails(CodeServerDeploymentDetailsVO vo) {
		CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
		if (vo != null) {
			BeanUtils.copyProperties(vo, deploymentDetails);
			deploymentDetails.setLastDeployedBy(toUserInfo(vo.getLastDeployedBy()));
		}
		return deploymentDetails;
	}

	private CodeServerDeploymentDetailsVO toDeploymentDetailsVO(CodeServerDeploymentDetails deploymentDetails)
			throws ParseException {
		CodeServerDeploymentDetailsVO deploymentDetailsVO = new CodeServerDeploymentDetailsVO();
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		if (deploymentDetails != null) {
			BeanUtils.copyProperties(deploymentDetails, deploymentDetailsVO);
			deploymentDetailsVO.setLastDeployedBy(toUserInfoVO(deploymentDetails.getLastDeployedBy()));
			if (Objects.isNull(deploymentDetails.getSecureWithIAMRequired())) {
				deploymentDetailsVO.setSecureWithIAMRequired(false);
			}
			if (deploymentDetails.getLastDeployedOn() != null)
				deploymentDetailsVO
						.setLastDeployedOn(isoFormat.parse(isoFormat.format(deploymentDetails.getLastDeployedOn())));
		}
		return deploymentDetailsVO;
	}

	private CodespaceSecurityConfigVO tosecurityConfigVO(CodespaceSecurityConfig CodespaceSecurityConfig) {
		CodespaceSecurityConfigVO codespaceSecurityConfigVO = new CodespaceSecurityConfigVO();
		if (CodespaceSecurityConfig != null) {
			BeanUtils.copyProperties(CodespaceSecurityConfig, codespaceSecurityConfigVO);

		}
		List<CodespaceSecurityRole> roles = CodespaceSecurityConfig.getRoles();
		if (roles != null && !roles.isEmpty()) {
			List<CodespaceSecurityRoleVO> rolesVO = roles.stream().map(n -> toRoleVO(n)).collect(Collectors.toList());
			codespaceSecurityConfigVO.setRoles(rolesVO);
		} else {
			codespaceSecurityConfigVO.setRoles(new ArrayList<>());
		}
		List<CodespaceSecurityEntitlement> entitlements = CodespaceSecurityConfig.getEntitlements();
		if (entitlements != null && !entitlements.isEmpty()) {
			List<CodespaceSecurityEntitlementVO> entitlementsVO = entitlements.stream().map(n -> toEntitlementVO(n))
					.collect(Collectors.toList());
			codespaceSecurityConfigVO.setEntitlements(entitlementsVO);
		} else {
			codespaceSecurityConfigVO.setEntitlements(new ArrayList<>());
		}
		List<CodespaceSecurityUserRoleMap> userRoleMaps = CodespaceSecurityConfig.getUserRoleMappings();
		if (userRoleMaps != null && !userRoleMaps.isEmpty()) {
			List<CodespaceSecurityUserRoleMapVO> userRoleMapsVO = userRoleMaps.stream().map(n -> toUserRoleMapVO(n))
					.collect(Collectors.toList());
			codespaceSecurityConfigVO.setUserRoleMappings(userRoleMapsVO);
		} else {
			codespaceSecurityConfigVO.setUserRoleMappings(new ArrayList<>());
		}
		return codespaceSecurityConfigVO;
	}

	private CodespaceSecurityConfig toSecurityConfig(CodespaceSecurityConfigVO CodespaceSecurityConfigVO) {
		CodespaceSecurityConfig entity = new CodespaceSecurityConfig();

		if (CodespaceSecurityConfigVO != null) {
			BeanUtils.copyProperties(CodespaceSecurityConfigVO, entity);
			if (CodespaceSecurityConfigVO.isIsProtectedByDna() != null) {
				entity.setIsProtectedByDna(CodespaceSecurityConfigVO.isIsProtectedByDna());
			}
		}
		List<CodespaceSecurityRoleVO> rolesVO = CodespaceSecurityConfigVO.getRoles();
		if (rolesVO != null && !rolesVO.isEmpty()) {
			List<CodespaceSecurityRole> roles = rolesVO.stream().map(n -> toRole(n)).collect(Collectors.toList());
			entity.setRoles(roles);
		}
		List<CodespaceSecurityEntitlementVO> entitlementsVO = CodespaceSecurityConfigVO.getEntitlements();
		if (entitlementsVO != null && !entitlementsVO.isEmpty()) {
			List<CodespaceSecurityEntitlement> entitlements = entitlementsVO.stream().map(n -> toEntitlement(n))
					.collect(Collectors.toList());
			entity.setEntitlements(entitlements);
		}
		List<CodespaceSecurityUserRoleMapVO> userRoleMapVO = CodespaceSecurityConfigVO.getUserRoleMappings();
		if (userRoleMapVO != null && !userRoleMapVO.isEmpty()) {
			List<CodespaceSecurityUserRoleMap> userRoleMap = userRoleMapVO.stream().map(n -> toUserRoleMap(n))
					.collect(Collectors.toList());
			entity.setUserRoleMappings(userRoleMap);
		}
		return entity;
	}

	private CodeServerRecipeDetails toRecipeDetails(CodeServerRecipeDetailsVO vo) {
		CodeServerRecipeDetails recipeDetails = new CodeServerRecipeDetails();
		if (vo != null) {
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
		if (recipe != null) {
			recipeDetailsVO
					.setCloudServiceProvider(CloudServiceProviderEnum.fromValue(recipe.getCloudServiceProvider()));
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
			if (entity != null) {
				vo.setId(entity.getId());
				CodeServerWorkspace data = entity.getData();
				if (data != null) {
					BeanUtils.copyProperties(data, vo);
					if (data.getIntiatedOn() != null)
						vo.setIntiatedOn(isoFormat.parse(isoFormat.format(data.getIntiatedOn())));
					UserInfo codespaceUserDetails = data.getWorkspaceOwner();
					if (codespaceUserDetails != null) {
						UserInfoVO codespaceUser = this.toUserInfoVO(codespaceUserDetails);
						vo.setWorkspaceOwner(codespaceUser);
					}
					CodeServerProjectDetails projectDetails = data.getProjectDetails();
					CodeServerProjectDetailsVO projectDetailsVO = new CodeServerProjectDetailsVO();
					if (projectDetails != null) {
						CodeServerDeploymentDetailsVO intDeployDetailsVO = toDeploymentDetailsVO(
								projectDetails.getIntDeploymentDetails());
						CodeServerDeploymentDetailsVO prodDeployDetailsVO = toDeploymentDetailsVO(
								projectDetails.getProdDeploymentDetails());
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
						if (projectDetails.getSecurityConfig() != null) {
							CodespaceSecurityConfigVO securityConfigVO = this
									.tosecurityConfigVO(projectDetails.getSecurityConfig());
							projectDetailsVO.setSecurityConfig(securityConfigVO);
						}
						if (projectDetails.getProjectCreatedOn() != null)
							projectDetailsVO.setProjectCreatedOn(
									isoFormat.parse(isoFormat.format(projectDetails.getProjectCreatedOn())));
					}
					vo.setProjectDetails(projectDetailsVO);

				}
			}
		} catch (Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}
		return vo;
	}

	@Override
	public CodeServerWorkspaceNsql toEntity(CodeServerWorkspaceVO vo) {
		CodeServerWorkspaceNsql entity = null;
		if (vo != null) {
			entity = new CodeServerWorkspaceNsql();
			CodeServerWorkspace data = new CodeServerWorkspace();
			entity.setId(vo.getId());
			BeanUtils.copyProperties(vo, data);
			UserInfoVO ownerVO = vo.getWorkspaceOwner();
			if (ownerVO != null) {
				UserInfo owner = this.toUserInfo(ownerVO);
				data.setWorkspaceOwner(owner);
			}
			CodeServerProjectDetailsVO projectDetailsVO = vo.getProjectDetails();
			if (projectDetailsVO != null) {
				CodeServerProjectDetails projectDetails = new CodeServerProjectDetails();
				BeanUtils.copyProperties(projectDetailsVO, projectDetails);
				UserInfoVO projectOwnerVO = projectDetailsVO.getProjectOwner();
				if (projectOwnerVO != null) {
					UserInfo projectOwner = this.toUserInfo(projectOwnerVO);
					projectDetails.setProjectOwner(projectOwner);
				}
				List<UserInfoVO> projectCollabsVO = projectDetailsVO.getProjectCollaborators();
				if (projectCollabsVO != null && !projectCollabsVO.isEmpty()) {
					List<UserInfo> projectCollabs = projectCollabsVO.stream().map(n -> toUserInfo(n))
							.collect(Collectors.toList());
					projectDetails.setProjectCollaborators(projectCollabs);
				}
				CodeServerRecipeDetailsVO recipeDetailsVO = projectDetailsVO.getRecipeDetails();
				if (recipeDetailsVO != null) {
					CodeServerRecipeDetails recipeDetails = this.toRecipeDetails(recipeDetailsVO);
					projectDetails.setRecipeDetails(recipeDetails);
				}
				CodeServerDeploymentDetailsVO intDeploymentDetailsVO = projectDetailsVO.getIntDeploymentDetails();
				if (intDeploymentDetailsVO != null) {
					CodeServerDeploymentDetails intDetails = this.toDeploymentDetails(intDeploymentDetailsVO);
					projectDetails.setIntDeploymentDetails(intDetails);
				}
				CodeServerDeploymentDetailsVO prodDeploymentDetailsVO = projectDetailsVO.getProdDeploymentDetails();
				if (prodDeploymentDetailsVO != null) {
					CodeServerDeploymentDetails prodDetails = this.toDeploymentDetails(prodDeploymentDetailsVO);
					projectDetails.setProdDeploymentDetails(prodDetails);
				}
				CodespaceSecurityConfigVO codespaceSecurityConfigVO = projectDetailsVO.getSecurityConfig();
				if (codespaceSecurityConfigVO != null) {
					CodespaceSecurityConfig securityConfig = this.toSecurityConfig(codespaceSecurityConfigVO);
					projectDetails.setSecurityConfig(securityConfig);
				}

				data.setProjectDetails(projectDetails);
				entity.setData(data);
			}
		}
		return entity;
	}

	public CodespaceSecurityConfigResponseVO toSaveConfigResponse(CodeServerWorkspaceVO vo) {
		CodespaceSecurityConfigResponseVO responseVO = new CodespaceSecurityConfigResponseVO();
		try {
			responseVO.setEntitlements(vo.getProjectDetails().getSecurityConfig().getEntitlements());
			responseVO.setStatus(vo.getProjectDetails().getSecurityConfig().getStatus());
			responseVO.setOpenSegments(vo.getProjectDetails().getSecurityConfig().getOpenSegments());
			responseVO.setIsProtectedByDna(vo.getProjectDetails().getSecurityConfig().isIsProtectedByDna());
			List<CodespaceSecurityEntitlementVO> entitlements = vo.getProjectDetails().getSecurityConfig()
					.getEntitlements();
			List<CodespaceSecurityRoleResponseVO> roles = new ArrayList();
			List<CodespaceSecurityUserRoleMapVO> userRoleMapVO = vo.getProjectDetails().getSecurityConfig()
					.getUserRoleMappings();
			List<CodespaceSecurityUserRoleMapResponseVO> userRoleMap = new ArrayList();
			List<CodespaceSecurityRoleVO> rolesVO = vo.getProjectDetails().getSecurityConfig().getRoles();
			if (rolesVO != null) {
				for (CodespaceSecurityRoleVO role : rolesVO) {
					CodespaceSecurityRoleResponseVO roleResponseVO = new CodespaceSecurityRoleResponseVO();
					List<CodespaceSecurityEntitlementLOV> entitlementsList = new ArrayList();
					roleResponseVO.setId(role.getName());
					roleResponseVO.setName(role.getName());
					List<String> entitlementIds = role.getRoleEntitlementIds();
					for (String id : entitlementIds) {
						for (CodespaceSecurityEntitlementVO entitlement : entitlements) {
							CodespaceSecurityEntitlementLOV CodespaceSecurityEntitlementLOV = new CodespaceSecurityEntitlementLOV();
							if (entitlement.getId().equalsIgnoreCase(id)) {
								CodespaceSecurityEntitlementLOV.setId(entitlement.getId());
								CodespaceSecurityEntitlementLOV.setName(entitlement.getName());
								entitlementsList.add(CodespaceSecurityEntitlementLOV);
							}
						}
					}
					roleResponseVO.setRoleEntitlements(entitlementsList);
					roles.add(roleResponseVO);
				}
				responseVO.setRoles(roles);
			}
			if (userRoleMapVO != null) {
				for (CodespaceSecurityUserRoleMapVO mapping : userRoleMapVO) {
					CodespaceSecurityUserRoleMapResponseVO userRoleMapResponseVO = new CodespaceSecurityUserRoleMapResponseVO();
					List<CodespaceSecurityRoleLOV> userRoleList = new ArrayList<>();
					List<String> roleIds = mapping.getRoleIds();
					BeanUtils.copyProperties(mapping, userRoleMapResponseVO);
					for (String id : roleIds) {
						for (CodespaceSecurityRoleVO userRole : rolesVO) {
							CodespaceSecurityRoleLOV userRoleMappingsLOV = new CodespaceSecurityRoleLOV();
							if (userRole.getId().equalsIgnoreCase(id)) {
								userRoleMappingsLOV.setId(userRole.getId());
								userRoleMappingsLOV.setName(userRole.getName());
								userRoleList.add(userRoleMappingsLOV);
							}
						}
					}
					userRoleMapResponseVO.setRoles(userRoleList);
					userRoleMap.add(userRoleMapResponseVO);
				}
				responseVO.setUserRoleMappings(userRoleMap);
			}
		} catch (Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}
		return responseVO;
	}

	
	public CodespaceSecurityConfigDetailsVO dtoToVo(CodespaceSecurityConfigDto dto) {
		CodespaceSecurityConfigDetailsVO vo = new CodespaceSecurityConfigDetailsVO();
		try {
			if (vo != null) {
				vo.setId(dto.getId());
				vo.setProjectName(dto.getProjectName());
				vo.setProjectOwner(toUserInfoVO(dto.getProjectOwner()));
				vo.setSecurityConfig(tosecurityConfigVO(dto.getSecurityConfig()));
			}
		} catch (Exception e) {
			log.error("Failed in assembler", e.getMessage());
		}
		return vo;
	public CodespaceSecurityConfigVO assembleSecurityConfig(CodeServerWorkspaceVO vo, CodespaceSecurityConfigVO data) {
		CodespaceSecurityConfigVO assembledSecurityConfig = new CodespaceSecurityConfigVO();
		if (data != null) {
			try {

				assembledSecurityConfig.setStatus(data.getStatus());
				if (data.getEntitlements() != null) {
					assembledSecurityConfig.setEntitlements(data.getEntitlements());
				} else {
					assembledSecurityConfig.setEntitlements(new ArrayList<>());
				}
				if (data.getOpenSegments() != null) {
					assembledSecurityConfig.setOpenSegments(data.getOpenSegments());
				} else {
					assembledSecurityConfig.setOpenSegments(new ArrayList<>());
				}
				if (data.isIsProtectedByDna() != null) {
					assembledSecurityConfig.setIsProtectedByDna(data.isIsProtectedByDna());
				}
				List<CodespaceSecurityEntitlementVO> oldEntitlementList = new ArrayList<>();
				if (vo.getProjectDetails().getSecurityConfig().getEntitlements() != null) {
					oldEntitlementList = vo.getProjectDetails().getSecurityConfig()
							.getEntitlements();
				}
				List<CodespaceSecurityRoleVO> oldRolesList = new ArrayList<>();
				if (vo.getProjectDetails().getSecurityConfig().getRoles() != null) {
					oldRolesList = vo.getProjectDetails().getSecurityConfig().getRoles();
				}
				List<CodespaceSecurityUserRoleMapVO> olduserRolesMapList = new ArrayList<>();
				if (vo.getProjectDetails().getSecurityConfig()
						.getUserRoleMappings() != null) {
					olduserRolesMapList = vo.getProjectDetails().getSecurityConfig()
							.getUserRoleMappings();
				}
				List<CodespaceSecurityRoleVO> newRoleList = new ArrayList<>();
				List<CodespaceSecurityUserRoleMapVO> newUserRoleMapList = new ArrayList<>();

				if (oldEntitlementList != null && data.getEntitlements() != null
						&& oldEntitlementList.size() > data.getEntitlements().size()) {

					List<CodespaceSecurityEntitlementVO> entitlementListToDelete = new ArrayList<>(oldEntitlementList);
					entitlementListToDelete.removeAll(data.getEntitlements());
					for (CodespaceSecurityRoleVO role : oldRolesList) {
						List<CodespaceSecurityConfigLOV> oldRoleEntitlementList = role.getRoleEntitlements();
						List<CodespaceSecurityConfigLOV> newRoleEntitlementList = new ArrayList<>();
						for (CodespaceSecurityConfigLOV roleEntitlement : oldRoleEntitlementList) {
							for (CodespaceSecurityEntitlementVO entitlementToDelete : entitlementListToDelete) {
								if (!roleEntitlement.getId().equalsIgnoreCase(entitlementToDelete.getId())) {
									newRoleEntitlementList.add(roleEntitlement);
								}
							}
						}
						role.getRoleEntitlements().clear();
						role.setRoleEntitlements(newRoleEntitlementList);
						newRoleList.add(role);
					}
					assembledSecurityConfig.setRoles(newRoleList);
				} else {
					if (data.getRoles() != null) {
						assembledSecurityConfig.setRoles(data.getRoles());
					} else {
						assembledSecurityConfig.setRoles(new ArrayList<>());
					}

				}

				if (oldRolesList != null && data.getRoles() != null && oldRolesList.size() > data.getRoles().size()) {
					List<CodespaceSecurityRoleVO> roleListToDelete = new ArrayList<>(oldRolesList);
					roleListToDelete.removeAll(data.getRoles());

					for (CodespaceSecurityUserRoleMapVO userRoleMap : olduserRolesMapList) {
						List<CodespaceSecurityConfigLOV> oldUserRoleList = userRoleMap.getRoles();
						List<CodespaceSecurityConfigLOV> newUserRoleList = new ArrayList<>();
						for (CodespaceSecurityConfigLOV userRole : oldUserRoleList) {
							for (CodespaceSecurityRoleVO roleToDelete : roleListToDelete) {
								if (!userRole.getId().equalsIgnoreCase(roleToDelete.getId())) {
									newUserRoleList.add(userRole);
								}
							}
						}
						userRoleMap.getRoles().clear();
						userRoleMap.setRoles(newUserRoleList);
						if (!userRoleMap.getRoles().isEmpty()) {
							newUserRoleMapList.add(userRoleMap);
						}

					}
					assembledSecurityConfig.setUserRoleMappings(newUserRoleMapList);

				} else {
					if (data.getUserRoleMappings() != null) {
						assembledSecurityConfig.setUserRoleMappings(data.getUserRoleMappings());
					} else {
						assembledSecurityConfig.setUserRoleMappings(new ArrayList<>());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
			}
		}
		return assembledSecurityConfig;
	}

	public CodespaceSecurityConfigVO generateSecurityConfigIds(CodespaceSecurityConfigVO data) {
		CodespaceSecurityConfigVO securityConfigWithIds = new CodespaceSecurityConfigVO();
		if (data != null) {

			securityConfigWithIds.setStatus(data.getStatus());
			if (data.getEntitlements() != null) {
				securityConfigWithIds.setEntitlements(data.getEntitlements());
			} else {
				securityConfigWithIds.setEntitlements(new ArrayList<>());
			}
			if (data.getOpenSegments() != null) {
				securityConfigWithIds.setOpenSegments(data.getOpenSegments());
			} else {
				securityConfigWithIds.setOpenSegments(new ArrayList<>());
			}
			if (data.isIsProtectedByDna() != null) {
				securityConfigWithIds.setIsProtectedByDna(data.isIsProtectedByDna());
			}

			List<CodespaceSecurityEntitlementVO> entitlementList = new ArrayList<>();
			if (data.getEntitlements() != null) {
				entitlementList = data.getEntitlements();
			}
			List<CodespaceSecurityRoleVO> roleList = new ArrayList<>();
			if (data.getRoles() != null) {
				roleList = data.getRoles();
			}
			List<CodespaceSecurityUserRoleMapVO> roleMapList = new ArrayList<>();
			if (data.getUserRoleMappings() != null) {
				roleMapList = data.getUserRoleMappings();
			}

			for (CodespaceSecurityEntitlementVO entitlement : entitlementList) {
				if(entitlement.getId()==null){
					entitlement.setId(UUID.randomUUID().toString());
				}
			}
			securityConfigWithIds.setEntitlements(entitlementList);
			for(CodespaceSecurityRoleVO role : roleList){
				if(role.getId()==null){
					role.setId(UUID.randomUUID().toString());
				}
			}
			securityConfigWithIds.setRoles(roleList);
			for(CodespaceSecurityUserRoleMapVO userRoleMap : roleMapList){
				if(userRoleMap.getId()==null){
					userRoleMap.setId(UUID.randomUUID().toString());
				}
			}
			securityConfigWithIds.setUserRoleMappings(roleMapList);
			
		}
		return securityConfigWithIds;
	}

}
