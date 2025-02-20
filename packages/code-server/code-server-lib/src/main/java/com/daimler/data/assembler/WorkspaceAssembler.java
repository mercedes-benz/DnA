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
 import com.daimler.data.db.json.DeploymentAudit;
 import com.daimler.data.dto.workspace.RoleCollectionVO;
 import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
 import com.daimler.data.db.json.CodeServerDeploymentDetails;
 import com.daimler.data.db.json.CodeServerLeanGovernanceFeilds;
 import com.daimler.data.db.json.CodeServerProjectDetails;
 import com.daimler.data.db.json.CodeServerRecipeDetails;
 import com.daimler.data.db.json.CodeServerWorkspace;
 import com.daimler.data.db.json.CodespaceSecurityConfig;
 import com.daimler.data.db.json.CodespaceSecurityConfigDetailCollection;
 import com.daimler.data.db.json.CodespaceSecurityConfigDetails;
 import com.daimler.data.db.json.CodespaceSecurityConfigDetailsLOV;
 import com.daimler.data.db.json.CodespaceSecurityEntitlement;
 import com.daimler.data.db.json.CodespaceSecurityApiList;
 import com.daimler.data.db.json.CodespaceSecurityRole;
 import com.daimler.data.db.json.CodespaceSecurityUserRoleMap;
 import com.daimler.data.db.json.UserInfo;
 import com.daimler.data.dto.CodespaceSecurityConfigDto;
 import com.daimler.data.dto.workspace.CodeServerDeploymentDetailsVO;
 import com.daimler.data.dto.workspace.CodeServerGovernanceVO;
 import com.daimler.data.dto.workspace.CodeServerProjectDetailsVO;
 import com.daimler.data.util.ConstantsUtility;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityConfigLOV;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CloudServiceProviderEnum;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CpuCapacityEnum;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.EnvironmentEnum;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.OperatingSystemEnum;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RamSizeEnum;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
 import com.daimler.data.dto.workspace.CodespaceSecurityEntitlementVO.HttpMethodEnum;
 import com.daimler.data.dto.workspace.CodespaceSecurityConfigDetailVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityConfigDetailCollectionVO;
 import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityConfigVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityEntitlementVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityApiListVO;
 import com.daimler.data.dto.workspace.admin.CodespacePublishedSecurityConfigVO;
 import com.daimler.data.dto.workspace.admin.CodespaceSecurityConfigDetailsVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityRoleVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityUserRoleMapResponseVO;
 import com.daimler.data.dto.workspace.CodespaceSecurityUserRoleMapVO;
 import com.daimler.data.dto.workspace.DeploymentAuditVO;
 import com.daimler.data.dto.workspace.UserInfoVO;
 import com.daimler.data.dto.workspace.DeploymentAuditVO;
 import lombok.extern.slf4j.Slf4j;
 
 @Slf4j
 @Component
 public class WorkspaceAssembler implements GenericAssembler<CodeServerWorkspaceVO, CodeServerWorkspaceNsql> {
 
	 @Value("${codeServer.env.value}")
	 private String codeServerEnvValue;

	 @Value("${codeServer.env.value.aws}")
	 private String codeServerEnvValueAws;
 
	 private UserInfoVO toUserInfoVO(UserInfo userInfo) {
		 UserInfoVO vo = new UserInfoVO();
		 if (userInfo != null) {
			 BeanUtils.copyProperties(userInfo, vo);
			 if(userInfo.getIsAdmin()!=null){
				vo.setIsAdmin(userInfo.getIsAdmin());
			 }
			 else{
				vo.setIsAdmin(false);
			 }
			 if(userInfo.getIsApprover()!=null){
				vo.setIsApprover(userInfo.getIsApprover());
			 }
			 else{
				vo.setIsApprover(false);
			 }
		 }
		 return vo;
	 }
 
	 public UserInfo toUserInfo(UserInfoVO userInfo) {
		 UserInfo entity = new UserInfo();
		 if (userInfo != null) {
			 BeanUtils.copyProperties(userInfo, entity);
			 if(userInfo.isIsAdmin()!=null){
				entity.setIsAdmin(userInfo.isIsAdmin());
			 }
			 else{
				entity.setIsAdmin(false);
			 }
			 if(userInfo.isIsApprover()!=null){
				entity.setIsApprover(userInfo.isIsApprover());
			 }
			 else{
				entity.setIsApprover(false);
			 }
		 }
		
		 return entity;
	 }
 
	 // public CodespaceSecurityRole toRole(CodespaceSecurityRoleVO roleVO) {
	 // 	CodespaceSecurityRole entity = new CodespaceSecurityRole();
	 // 	if (roleVO != null) {
	 // 		BeanUtils.copyProperties(roleVO, entity);
	 // 		List<CodespaceSecurityConfigLOV> listOfEntitlements = roleVO.getRoleEntitlements();
	 // 		if (listOfEntitlements != null) {
	 // 			List<CodespaceSecurityConfigDetailsLOV> listOfroleEntitlements = listOfEntitlements.stream()
	 // 					.map(n -> toCodespaceSecurityConfigDetailsLOV(n))
	 // 					.collect(Collectors.toList());
	 // 			entity.setRoleEntitlements(listOfroleEntitlements);
	 // 		}
	 // 	}
	 // 	return entity;
	 // }
 
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
			 if(entitlementVO.getHttpMethod()!=null){
			 	entity.setHttpMethod(entitlementVO.getHttpMethod().toString());
			 }
			 // List<CodespaceSecurityApiListVO> apiListVO = entitlementVO.getApiList();
			 // if (apiListVO != null) {
			 // 	List<CodespaceSecurityApiList> apiLists = apiListVO.stream().map(n -> toApiList(n))
			 // 			.collect(Collectors.toList());
			 // 	entity.setApiList(apiLists);
			 // }
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
			 if(entitlement.getHttpMethod()!=null){
			 	entitlementVO.setHttpMethod(HttpMethodEnum.fromValue(entitlement.getHttpMethod()));
			 }
			 // List<CodespaceSecurityApiList> apiLists = entitlement.getApiList();
			 // if (apiLists != null) {
			 // 	List<CodespaceSecurityApiListVO> apiListVO = apiLists.stream().map(n -> toApiListVO(n))
			 // 			.collect(Collectors.toList());
			 // 	entitlementVO.setApiList(apiListVO);
			 // } else {
			 // 	entitlementVO.setApiList(new ArrayList<>());
			 // }
		 }
		 return entitlementVO;
	 }
 
	 // public CodespaceSecurityRoleVO toRoleVO(CodespaceSecurityRole role) {
	 // 	CodespaceSecurityRoleVO roleVO = new CodespaceSecurityRoleVO();
	 // 	if (role != null) {
	 // 		BeanUtils.copyProperties(role, roleVO);
	 // 		List<CodespaceSecurityConfigDetailsLOV> roleEntitlementList = role.getRoleEntitlements();
	 // 		if (roleEntitlementList != null) {
	 // 			List<CodespaceSecurityConfigLOV> listOfRoleEntitlements = roleEntitlementList.stream()
	 // 					.map(n -> toCodespaceSecurityConfigLOV(n))
	 // 					.collect(Collectors.toList());
	 // 			roleVO.setRoleEntitlements(listOfRoleEntitlements);
	 // 		} else {
	 // 			roleVO.setRoleEntitlements(new ArrayList<>());
	 // 		}
	 // 	}
	 // 	return roleVO;
	 // }
 
	 public CodespaceSecurityConfigLOV toCodespaceSecurityConfigLOV(
			 CodespaceSecurityConfigDetailsLOV securityConfigDetailsLOV) {
		 CodespaceSecurityConfigLOV securityConfigLOV = new CodespaceSecurityConfigLOV();
		 if (securityConfigDetailsLOV != null) {
			 BeanUtils.copyProperties(securityConfigDetailsLOV, securityConfigLOV);
		 }
		 return securityConfigLOV;
	 }
 
	//  public CodespaceSecurityApiListVO toApiListVO(CodespaceSecurityApiList apiList) {
	// 	 CodespaceSecurityApiListVO apiListVO = new CodespaceSecurityApiListVO();
	// 	 if (apiList != null) {
	// 		 apiListVO.setApiPattern(apiList.getApiPattern());
	// 		 apiListVO.setHttpMethod(HttpMethodEnum.fromValue(apiList.getHttpMethod()));
	// 	 }
	// 	 return apiListVO;
	//  }
 
	 // public CodespaceSecurityUserRoleMap toUserRoleMap(CodespaceSecurityUserRoleMapVO userRoleMapVO) {
	 // 	CodespaceSecurityUserRoleMap entity = new CodespaceSecurityUserRoleMap();
	 // 	if (userRoleMapVO != null) {
	 // 		BeanUtils.copyProperties(userRoleMapVO, entity);
	 // 		List<CodespaceSecurityConfigLOV> roleListVO = userRoleMapVO.getRoles();
	 // 		if (roleListVO != null) {
	 // 			List<CodespaceSecurityConfigDetailsLOV> listOfRoles = roleListVO.stream()
	 // 					.map(n -> toCodespaceSecurityConfigDetailsLOV(n))
	 // 					.collect(Collectors.toList());
	 // 			entity.setRoles(listOfRoles);
	 // 			;
	 // 		}
	 // 	}
	 // 	return entity;
	 // }
 
	 // public CodespaceSecurityUserRoleMapVO toUserRoleMapVO(CodespaceSecurityUserRoleMap userRoleMap) {
	 // 	CodespaceSecurityUserRoleMapVO ueserRoleMapVO = new CodespaceSecurityUserRoleMapVO();
	 // 	if (userRoleMap != null) {
	 // 		BeanUtils.copyProperties(userRoleMap, ueserRoleMapVO);
	 // 		List<CodespaceSecurityConfigDetailsLOV> listOfRoles = userRoleMap.getRoles();
	 // 		if (listOfRoles != null) {
	 // 			List<CodespaceSecurityConfigLOV> listOfRolesVO = listOfRoles.stream()
	 // 					.map(n -> toCodespaceSecurityConfigLOV(n)).collect(Collectors.toList());
	 // 			ueserRoleMapVO.setRoles(listOfRolesVO);
	 // 		} else {
	 // 			ueserRoleMapVO.setRoles(new ArrayList<>());
	 // 		}
	 // 	}
	 // 	return ueserRoleMapVO;
	 // }
 
	 private CodeServerDeploymentDetails toDeploymentDetails(CodeServerDeploymentDetailsVO vo) {
		 CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
		 if (vo != null) {
			 BeanUtils.copyProperties(vo, deploymentDetails);
			 if(vo.isSecureWithIAMRequired()!=null)
			 {
				deploymentDetails.setSecureWithIAMRequired(vo.isSecureWithIAMRequired());
			 }
			 else
			 {
				deploymentDetails.setSecureWithIAMRequired(false);
			 }
			 deploymentDetails.setLastDeployedBy(toUserInfo(vo.getLastDeployedBy()));
			 List<DeploymentAudit> auditDetails = this.toDeploymentAuditDetails(vo.getDeploymentAuditLogs());
			 deploymentDetails.setDeploymentAuditLogs(auditDetails);
		 }
		 return deploymentDetails;
	 }
 
	 private List<DeploymentAudit> toDeploymentAuditDetails(List<DeploymentAuditVO> auditdetails)
	 {
		 List<DeploymentAudit> deployedAuditLogDetails = new ArrayList<>();
		 try
		 {
			 if(auditdetails != null && !auditdetails.isEmpty())
			 {
				 for(DeploymentAuditVO audit: auditdetails)
				 { 
					 DeploymentAudit auditDetails = new DeploymentAudit();
					 auditDetails.setDeploymentStatus(audit.getDeploymentStatus());
					 if(Objects.nonNull(audit.getDeployedOn())){
						 auditDetails.setDeployedOn(audit.getDeployedOn());
					 }
					 auditDetails.setTriggeredBy(audit.getTriggeredBy());
					 if(Objects.nonNull(audit.getTriggeredOn())){
						 auditDetails.setTriggeredOn(audit.getTriggeredOn());
					 }
					 auditDetails.setBranch(audit.getBranch());
					 auditDetails.setCommitId(audit.getCommitId());
					 deployedAuditLogDetails.add(auditDetails);
				 }
			 }
		 }
		 catch(Exception e)
		 {
			 log.error("Failed while parsing in assembler");
		 }
		 return deployedAuditLogDetails;
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
			 else
			 {
				deploymentDetailsVO.setSecureWithIAMRequired(deploymentDetails.getSecureWithIAMRequired());
			 }
			 if (deploymentDetails.getLastDeployedOn() != null){
				 deploymentDetailsVO
						 .setLastDeployedOn(isoFormat.parse(isoFormat.format(deploymentDetails.getLastDeployedOn())));
			 }
			 if(deploymentDetails.getDeploymentAuditLogs()!=null && !deploymentDetails.getDeploymentAuditLogs().isEmpty())
			 {
				 List<DeploymentAuditVO> auditDetails = this.toDeploymentAuditDetailsVO(deploymentDetails.getDeploymentAuditLogs());
				 deploymentDetailsVO.setDeploymentAuditLogs(auditDetails);
			 }
		 }
		 return deploymentDetailsVO;
	 }
 
	 private List<DeploymentAuditVO> toDeploymentAuditDetailsVO(List<DeploymentAudit> deploymentAuditLogs) {
		 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		 List<DeploymentAuditVO> auditDetailsVO = new ArrayList<>();
		 try
		 {
			 if(deploymentAuditLogs!=null && !deploymentAuditLogs.isEmpty())
			 {
				 for(DeploymentAudit audit: deploymentAuditLogs)
				 {
						 DeploymentAuditVO auditDetails = new DeploymentAuditVO();
						 auditDetails.setDeploymentStatus(audit.getDeploymentStatus());
						 if(Objects.nonNull(audit.getDeployedOn()))
							 auditDetails.setDeployedOn(isoFormat.parse(isoFormat.format(audit.getDeployedOn())));
						 auditDetails.setTriggeredBy(audit.getTriggeredBy());
						 if(Objects.nonNull(audit.getTriggeredOn()))
							 auditDetails.setTriggeredOn(isoFormat.parse(isoFormat.format(audit.getTriggeredOn())));
						 auditDetails.setBranch(audit.getBranch());
						 auditDetails.setCommitId(audit.getCommitId());
						 auditDetailsVO.add(auditDetails);
				 }
			 }
		 }
		 catch(ParseException e)
		 {
			 log.error("Failed in assembler  while parsing date into iso format with exception {} in auditDeatils");
		 }
		 return auditDetailsVO;
	 }
 
	 private CodespaceSecurityConfigVO tosecurityConfigVO(CodespaceSecurityConfig CodespaceSecurityConfig) {
		 CodespaceSecurityConfigVO codespaceSecurityConfigVO = new CodespaceSecurityConfigVO();
		 if (CodespaceSecurityConfig != null) {
			 BeanUtils.copyProperties(CodespaceSecurityConfig, codespaceSecurityConfigVO);
 
			 CodespaceSecurityConfigDetailVO stagingDraftConfigVO = new CodespaceSecurityConfigDetailVO();
			 CodespaceSecurityConfigDetailVO stagingPublishedConfigVO = new CodespaceSecurityConfigDetailVO();
			 CodespaceSecurityConfigDetailVO productionPublishedConfigVO = new CodespaceSecurityConfigDetailVO();
			 CodespaceSecurityConfigDetailVO productionDraftConfigVO = new CodespaceSecurityConfigDetailVO();

			 if(CodespaceSecurityConfig.getStaging()!=null){
				
				if(CodespaceSecurityConfig.getStaging().getDraft()!=null){
					CodespaceSecurityConfigDetails stagingDraftConfig = CodespaceSecurityConfig.getStaging ().getDraft();
					stagingDraftConfigVO.setAppID(stagingDraftConfig.getAppID());
					List<CodespaceSecurityEntitlement> entitlements = stagingDraftConfig.getEntitlements();
					if (entitlements != null && !entitlements.isEmpty()) {
						List<CodespaceSecurityEntitlementVO> entitlementsVO = entitlements.stream().map(n -> toEntitlementVO(n))
								.collect(Collectors.toList());
						stagingDraftConfigVO.setEntitlements(entitlementsVO);
					} 
				}else {
					stagingDraftConfigVO.setEntitlements(new ArrayList<>());
				}

				if(CodespaceSecurityConfig.getStaging().getPublished() !=null){
					CodespaceSecurityConfigDetails stagingPublishedConfig = CodespaceSecurityConfig.getStaging().getPublished();
					stagingPublishedConfigVO.setAppID(stagingPublishedConfig.getAppID());
					List<CodespaceSecurityEntitlement> entitlements = stagingPublishedConfig.getEntitlements();
					if (entitlements != null && !entitlements.isEmpty()) {
						List<CodespaceSecurityEntitlementVO> entitlementsVO = entitlements.stream().map(n -> toEntitlementVO(n))
								.collect(Collectors.toList());
						stagingPublishedConfigVO.setEntitlements(entitlementsVO);
					} 
				}else {
					stagingPublishedConfigVO.setEntitlements(new ArrayList<>());
				}
			}
			if(CodespaceSecurityConfig.getProduction()!=null){
				
				if(CodespaceSecurityConfig.getProduction().getPublished() !=null){
					CodespaceSecurityConfigDetails productionPublishedConfig = CodespaceSecurityConfig.getProduction().getPublished();
					productionPublishedConfigVO.setAppID(productionPublishedConfig.getAppID());
					List<CodespaceSecurityEntitlement> entitlements = productionPublishedConfig.getEntitlements();
					if (entitlements != null && !entitlements.isEmpty()) {
						List<CodespaceSecurityEntitlementVO> entitlementsVO = entitlements.stream().map(n -> toEntitlementVO(n))
								.collect(Collectors.toList());
								productionPublishedConfigVO.setEntitlements(entitlementsVO);
					} 
				}else {
					productionPublishedConfigVO.setEntitlements(new ArrayList<>());
				}

				if(CodespaceSecurityConfig.getProduction().getDraft()!=null){
					CodespaceSecurityConfigDetails productionDraftConfig = CodespaceSecurityConfig.getProduction().getDraft();
					productionDraftConfigVO.setAppID(productionDraftConfig.getAppID());
					List<CodespaceSecurityEntitlement> entitlements = productionDraftConfig.getEntitlements();
					if (entitlements != null && !entitlements.isEmpty()) {
						List<CodespaceSecurityEntitlementVO> entitlementsVO = entitlements.stream().map(n -> toEntitlementVO(n))
								.collect(Collectors.toList());
								productionDraftConfigVO.setEntitlements(entitlementsVO);
					} 
				}else {
					productionDraftConfigVO.setEntitlements(new ArrayList<>());
				} 
			}
 
			 CodespaceSecurityConfigDetailCollectionVO stagingCollection = new CodespaceSecurityConfigDetailCollectionVO();
			 stagingCollection.setDraft(stagingDraftConfigVO);
			 stagingCollection.setPublished(stagingPublishedConfigVO);
			 CodespaceSecurityConfigDetailCollectionVO productionCollection = new CodespaceSecurityConfigDetailCollectionVO();
			 productionCollection.setDraft(productionDraftConfigVO);
			 productionCollection.setPublished(productionPublishedConfigVO);
 
			 codespaceSecurityConfigVO.setStaging(stagingCollection);
			 codespaceSecurityConfigVO.setProduction(productionCollection);
		 }
		 // List<CodespaceSecurityRole> roles = CodespaceSecurityConfig.getRoles();
		 // if (roles != null && !roles.isEmpty()) {
		 // 	List<CodespaceSecurityRoleVO> rolesVO = roles.stream().map(n -> toRoleVO(n)).collect(Collectors.toList());
		 // 	codespaceSecurityConfigVO.setRoles(rolesVO);
		 // } else {
		 // 	codespaceSecurityConfigVO.setRoles(new ArrayList<>());
		 // }
		 
		 // List<CodespaceSecurityUserRoleMap> userRoleMaps = CodespaceSecurityConfig.getUserRoleMappings();
		 // if (userRoleMaps != null && !userRoleMaps.isEmpty()) {
		 // 	List<CodespaceSecurityUserRoleMapVO> userRoleMapsVO = userRoleMaps.stream().map(n -> toUserRoleMapVO(n))
		 // 			.collect(Collectors.toList());
		 // 	codespaceSecurityConfigVO.setUserRoleMappings(userRoleMapsVO);
		 // } else {
		 // 	codespaceSecurityConfigVO.setUserRoleMappings(new ArrayList<>());
		 // }
		 return codespaceSecurityConfigVO;
	 }
 
	 public CodespaceSecurityConfig toSecurityConfig(CodespaceSecurityConfigVO CodespaceSecurityConfigVO) {
		CodespaceSecurityConfig entity = new CodespaceSecurityConfig();
		if (CodespaceSecurityConfigVO != null) {
			 BeanUtils.copyProperties(CodespaceSecurityConfigVO, entity);
			//  if (CodespaceSecurityConfigVO.isIsProtectedByDna() != null) {
			// 	 entity.setIsProtectedByDna(CodespaceSecurityConfigVO.isIsProtectedByDna());
			//  }
 
			CodespaceSecurityConfigDetails stagingDraftConfig = new CodespaceSecurityConfigDetails();
			CodespaceSecurityConfigDetails stagingPublishedConfig = new CodespaceSecurityConfigDetails();
			CodespaceSecurityConfigDetails productionDraftConfig = new CodespaceSecurityConfigDetails();
			CodespaceSecurityConfigDetails productionPublishedConfig = new CodespaceSecurityConfigDetails();

			if(CodespaceSecurityConfigVO.getStaging()!=null){
				
				if(CodespaceSecurityConfigVO.getStaging().getDraft()!=null){
					CodespaceSecurityConfigDetailVO stagingDraftConfigVO = CodespaceSecurityConfigVO.getStaging().getDraft();
					stagingDraftConfig.setAppID(stagingDraftConfigVO.getAppID());
					List<CodespaceSecurityEntitlementVO> entitlementsVO = stagingDraftConfigVO.getEntitlements();
					if (entitlementsVO != null && !entitlementsVO.isEmpty()) {
						List<CodespaceSecurityEntitlement> entitlements = entitlementsVO.stream().map(n -> toEntitlement(n))
								.collect(Collectors.toList());
						stagingDraftConfig.setEntitlements(entitlements);
					}
				}

				if(CodespaceSecurityConfigVO.getStaging().getPublished()!=null){
					CodespaceSecurityConfigDetailVO stagingPublishedConfigVO = CodespaceSecurityConfigVO.getStaging().getPublished();
					stagingPublishedConfig.setAppID(stagingPublishedConfigVO.getAppID());
					List<CodespaceSecurityEntitlementVO> entitlementsVO = stagingPublishedConfigVO.getEntitlements();
					if (entitlementsVO != null && !entitlementsVO.isEmpty()) {
						List<CodespaceSecurityEntitlement> entitlements = entitlementsVO.stream().map(n -> toEntitlement(n))
								.collect(Collectors.toList());
						stagingPublishedConfig.setEntitlements(entitlements);
					}
				}
			}
			if(CodespaceSecurityConfigVO.getProduction()!=null){
				
				if(CodespaceSecurityConfigVO.getProduction().getPublished() !=null){
					CodespaceSecurityConfigDetailVO productionPublishedConfigVO = CodespaceSecurityConfigVO.getProduction().getPublished();
					productionPublishedConfig.setAppID(productionPublishedConfigVO.getAppID());
					List<CodespaceSecurityEntitlementVO> entitlementsVO = productionPublishedConfigVO.getEntitlements();
					if (entitlementsVO != null && !entitlementsVO.isEmpty()) {
						List<CodespaceSecurityEntitlement> entitlements = entitlementsVO.stream().map(n -> toEntitlement(n))
								.collect(Collectors.toList());
						productionPublishedConfig.setEntitlements(entitlements);
					}
				}

				if(CodespaceSecurityConfigVO.getProduction().getDraft()!=null){
					CodespaceSecurityConfigDetailVO productionDraftConfigVO = CodespaceSecurityConfigVO.getProduction().getDraft();
					productionDraftConfig.setAppID(productionDraftConfigVO.getAppID());
					List<CodespaceSecurityEntitlementVO> entitlementsVO = productionDraftConfigVO.getEntitlements();
					if (entitlementsVO != null && !entitlementsVO.isEmpty()) {
						List<CodespaceSecurityEntitlement> entitlements = entitlementsVO.stream().map(n -> toEntitlement(n))
								.collect(Collectors.toList());
						productionDraftConfig.setEntitlements(entitlements);
					}
				}
			}
			CodespaceSecurityConfigDetailCollection stagingCollection = new CodespaceSecurityConfigDetailCollection();
			stagingCollection.setDraft(stagingDraftConfig);
			stagingCollection.setPublished(stagingPublishedConfig);
			CodespaceSecurityConfigDetailCollection productionCollection = new CodespaceSecurityConfigDetailCollection();
			productionCollection.setDraft(productionDraftConfig);
			productionCollection.setPublished(productionPublishedConfig);

			entity.setStaging(stagingCollection);
			entity.setProduction(productionCollection);
		}
		 // List<CodespaceSecurityRoleVO> rolesVO = CodespaceSecurityConfigVO.getRoles();
		 // if (rolesVO != null && !rolesVO.isEmpty()) {
		 // 	List<CodespaceSecurityRole> roles = rolesVO.stream().map(n -> toRole(n)).collect(Collectors.toList());
		 // 	entity.setRoles(roles);
		 // }
		 
		 
		 // List<CodespaceSecurityUserRoleMapVO> userRoleMapVO = CodespaceSecurityConfigVO.getUserRoleMappings();
		 // if (userRoleMapVO != null && !userRoleMapVO.isEmpty()) {
		 // 	List<CodespaceSecurityUserRoleMap> userRoleMap = userRoleMapVO.stream().map(n -> toUserRoleMap(n))
		 // 			.collect(Collectors.toList());
		 // 	entity.setUserRoleMappings(userRoleMap);
		 // }
		 return entity;
	 }
 
	 // public CodespaceSecurityConfig toPublishedSecurityConfig(CodespaceSecurityConfigVO CodespaceSecurityConfigVO) {
	 // 	CodespaceSecurityConfig entity = new CodespaceSecurityConfig();
 
	 // 	if (CodespaceSecurityConfigVO != null) {
	 // 		BeanUtils.copyProperties(CodespaceSecurityConfigVO, entity);
	 // 		if (CodespaceSecurityConfigVO.isIsProtectedByDna() != null) {
	 // 			entity.setIsProtectedByDna(CodespaceSecurityConfigVO.isIsProtectedByDna());
	 // 		}
	 // 	}
	 // 	List<CodespaceSecurityRoleVO> rolesVO = CodespaceSecurityConfigVO.getRoles();
	 // 	if (rolesVO != null && !rolesVO.isEmpty()) {
	 // 		List<CodespaceSecurityRole> roles = rolesVO.stream().map(n -> toRole(n)).collect(Collectors.toList());
	 // 		entity.setRoles(roles);
	 // 	}
	 // 	List<CodespaceSecurityEntitlementVO> entitlementsVO = CodespaceSecurityConfigVO.getEntitlements();
	 // 	if (entitlementsVO != null && !entitlementsVO.isEmpty()) {
	 // 		List<CodespaceSecurityEntitlement> entitlements = entitlementsVO.stream().map(n -> toEntitlement(n))
	 // 				.collect(Collectors.toList());
	 // 		entity.setEntitlements(entitlements);
	 // 	}
	 // 	List<CodespaceSecurityUserRoleMapVO> userRoleMapVO = CodespaceSecurityConfigVO.getUserRoleMappings();
	 // 	if (userRoleMapVO != null && !userRoleMapVO.isEmpty()) {
	 // 		List<CodespaceSecurityUserRoleMap> userRoleMap = userRoleMapVO.stream().map(n -> toUserRoleMap(n))
	 // 				.collect(Collectors.toList());
	 // 		entity.setUserRoleMappings(userRoleMap);
	 // 	}
	 // 	return entity;
	 // }
 
 
	 private CodeServerRecipeDetails toRecipeDetails(CodeServerRecipeDetailsVO vo) {
		 CodeServerRecipeDetails recipeDetails = new CodeServerRecipeDetails();
		 if (vo != null) {
			BeanUtils.copyProperties(vo, recipeDetails);
			 recipeDetails.setCpuCapacity(vo.getCpuCapacity().toString());
			 recipeDetails.setCloudServiceProvider(vo.getCloudServiceProvider().toString());
			 if(vo.getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS_AWS)){
				recipeDetails.setEnvironment(codeServerEnvValueAws);
			} else {
				recipeDetails.setEnvironment(codeServerEnvValue);
			}
			 recipeDetails.setOperatingSystem(vo.getOperatingSystem().toString());
			 recipeDetails.setRamSize(vo.getRamSize().toString());
			 if(vo.getRecipeId()!=null){
				recipeDetails.setRecipeId(vo.getRecipeId().toString());
			 }
			 recipeDetails.setResource(vo.getResource());
			 recipeDetails.setRepodetails(vo.getRepodetails());
			 recipeDetails.setDeployEnabled(vo.isIsDeployEnabled());
			 if(vo.getSoftware()!=null)
			 {
				recipeDetails.setSoftware(vo.getSoftware());
			 }
			 if(vo.getAdditionalServices()!=null) {
				recipeDetails.setAdditionalServices(vo.getAdditionalServices());
			 }
		 }
		 return recipeDetails;
	 }
 
	 private CodeServerRecipeDetailsVO toRecipeDetailsVO(CodeServerRecipeDetails recipe) {
		 CodeServerRecipeDetailsVO recipeDetailsVO = new CodeServerRecipeDetailsVO();
		 if (recipe != null) {
			BeanUtils.copyProperties(recipe, recipeDetailsVO);
			 recipeDetailsVO
					 .setCloudServiceProvider(CloudServiceProviderEnum.fromValue(recipe.getCloudServiceProvider()));
			 recipeDetailsVO.setCpuCapacity(CpuCapacityEnum.fromValue(recipe.getCpuCapacity()));
			 recipeDetailsVO.setEnvironment(EnvironmentEnum.fromValue(recipe.getEnvironment().toUpperCase()));
			 recipeDetailsVO.setOperatingSystem(OperatingSystemEnum.fromValue(recipe.getOperatingSystem()));
			 recipeDetailsVO.setRamSize(RamSizeEnum.fromValue(recipe.getRamSize()));
			 recipeDetailsVO.setRecipeId(RecipeIdEnum.fromValue(recipe.getRecipeId()));
			 recipeDetailsVO.setResource(recipe.getResource());
			 recipeDetailsVO.setRepodetails(recipe.getRepodetails());
			 recipeDetailsVO.setIsDeployEnabled(recipe.isDeployEnabled());
			 if(recipe.getSoftware()!=null)
			 {
				recipeDetailsVO.setSoftware(recipe.getSoftware());
			 }
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
					 if(data.getIsWorkspaceMigrated()!= null){
						vo.setIsWorkspaceMigrated(data.getIsWorkspaceMigrated());
					 }else{
						vo.setIsWorkspaceMigrated(false);
					 }
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
						 CodeServerLeanGovernanceFeilds governance = projectDetails.getDataGovernance();
						 if (governance != null) {
							 CodeServerGovernanceVO governanceVO = this.toGovernanceVo(governance);
							 projectDetailsVO.setDataGovernance(governanceVO);
						 }
						 projectDetailsVO.setIntDeploymentDetails(intDeployDetailsVO);
						 projectDetailsVO.setProdDeploymentDetails(prodDeployDetailsVO);
						 List<UserInfo> collabs = projectDetails.getProjectCollaborators();
						 if(collabs!=null && !collabs.isEmpty()) {
							 List<UserInfoVO> collabsVO = collabs.stream().map
									 (n -> { UserInfoVO user = new UserInfoVO();
											 BeanUtils.copyProperties(n,user);
											 if(n.getIsAdmin()==null){
												user.setIsAdmin(false);
											 }
											 else{
												user.setIsAdmin(n.getIsAdmin());
											 }
											 if(n.getIsApprover()==null){
												user.setIsApprover(false);
											 }
											 else{
												user.setIsApprover(n.getIsApprover());
											 }
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
						 // if (projectDetails.getPublishedSecurityConfig() != null) {
						 // 	CodespaceSecurityConfigVO publishedSecuirtyConfigVO = this
						 // 			.tosecurityConfigVO(projectDetails.getPublishedSecurityConfig());
						 // 	projectDetailsVO.setPublishedSecuirtyConfig(publishedSecuirtyConfigVO);
 
						 // }
						 if (projectDetails.getProjectCreatedOn() != null)
							 projectDetailsVO.setProjectCreatedOn(
									 isoFormat.parse(isoFormat.format(projectDetails.getProjectCreatedOn())));

						projectDetailsVO.setRecipeName(projectDetails.getRecipeName());
					 }
					 vo.setProjectDetails(projectDetailsVO);
 
				 }
			 }
		 } catch (Exception e) {
			 log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		 }
		 return vo;
	 }
 
	 public CodeServerGovernanceVO toGovernanceVo(CodeServerLeanGovernanceFeilds governance) {
		 CodeServerGovernanceVO governanceVo = new CodeServerGovernanceVO();
		 if (governance != null) {
			 BeanUtils.copyProperties(governance, governanceVo);
			 if (governance.getPiiData() != null) {
				 governanceVo.setPiiData(governance.getPiiData());
			 }
			 else
			 {
				 governanceVo.setPiiData(false);
			 }
			 if (governance.getEnableDeployApproval() != null) {
				governanceVo.setEnableDeployApproval(governance.getEnableDeployApproval());
			}
			else
			{
				governanceVo.setEnableDeployApproval(false);
			}
		 }
		 return governanceVo;
	 }
 
	 @Override
	 public CodeServerWorkspaceNsql toEntity(CodeServerWorkspaceVO vo) {
		 CodeServerWorkspaceNsql entity = null;
		 if (vo != null) {
			 entity = new CodeServerWorkspaceNsql();
			 CodeServerWorkspace data = new CodeServerWorkspace();
			 entity.setId(vo.getId());
			 BeanUtils.copyProperties(vo, data);
			 if(vo.isIsWorkspaceMigrated()!=null){
				data.setIsWorkspaceMigrated(vo.isIsWorkspaceMigrated());
			 }else{
				data.setIsWorkspaceMigrated(false);
			 }
			 UserInfoVO ownerVO = vo.getWorkspaceOwner();
			 if (ownerVO != null) {
				 UserInfo owner = this.toUserInfo(ownerVO);
				 data.setWorkspaceOwner(owner);
			 }
			 CodeServerProjectDetailsVO projectDetailsVO = vo.getProjectDetails();
			 if (projectDetailsVO != null) {
				 CodeServerProjectDetails projectDetails = new CodeServerProjectDetails();
				 CodeServerGovernanceVO governanceVO = projectDetailsVO.getDataGovernance();
				 if (governanceVO != null) {
					 CodeServerLeanGovernanceFeilds governance = this.toGovernanceEntity(governanceVO);
					 projectDetails.setDataGovernance(governance);
				 }
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
				//  CodespaceSecurityConfigVO codespacePublishSecurityConfigVo = projectDetailsVO
				// 		 .getPublishedSecuirtyConfig();
				//  if (codespacePublishSecurityConfigVo != null) {
				// 	 projectDetails.setPublishedSecurityConfig(this.toSecurityConfig(codespacePublishSecurityConfigVo));
				//  }
				projectDetails.setRecipeName(projectDetailsVO.getRecipeName());
				 data.setProjectDetails(projectDetails);
				 entity.setData(data);
			 }
		 }
		 return entity;
	 }
 
	 public CodeServerLeanGovernanceFeilds toGovernanceEntity(CodeServerGovernanceVO governanceVO) {
		 CodeServerLeanGovernanceFeilds governanceFeilds = new CodeServerLeanGovernanceFeilds();
		 if (governanceVO != null) {
			 BeanUtils.copyProperties(governanceVO, governanceFeilds);
			 if (governanceVO.isPiiData()) {
				 governanceFeilds.setPiiData(governanceVO.isPiiData());
			 }
			 else
			 {
				 governanceFeilds.setPiiData(false);
			 }
			 if (governanceVO.isEnableDeployApproval()) {
				governanceFeilds.setEnableDeployApproval(governanceVO.isEnableDeployApproval());
			}
			else
			{
				governanceFeilds.setEnableDeployApproval(false);
			}
		 }
		 return governanceFeilds;
	 }
 
	 // public CodespaceSecurityConfigVO assembleSecurityConfig(CodeServerWorkspaceVO vo, CodespaceSecurityConfigVO data,String env) {
	 // 	CodespaceSecurityConfigVO assembledSecurityConfig = new CodespaceSecurityConfigVO();
	 // 	if (data != null) {
	 // 		try {
	 // 			assembledSecurityConfig.setAppId(data.getAppId());
	 // 			//assembledSecurityConfig.setStatus(data.getStatus());
	 // 			if (data.getEntitlements() != null) {
	 // 				assembledSecurityConfig.setEntitlements(data.getEntitlements());
	 // 			} else {
	 // 				assembledSecurityConfig.setEntitlements(new ArrayList<>());
	 // 			}
	 // 			if (data.getOpenSegments() != null) {
	 // 				assembledSecurityConfig.setOpenSegments(data.getOpenSegments());
	 // 			} else {
	 // 				assembledSecurityConfig.setOpenSegments(new ArrayList<>());
	 // 			}
	 // 			if (data.isIsProtectedByDna() != null) {
	 // 				assembledSecurityConfig.setIsProtectedByDna(data.isIsProtectedByDna());
	 // 			}
	 // 			List<CodespaceSecurityEntitlementVO> oldEntitlementList = new ArrayList<>();
	 // 			if ("staging".equalsIgnoreCase(env) && vo.getProjectDetails().getSecurityConfig().getStaging().getEntitlements() != null) {
	 // 				oldEntitlementList = vo.getProjectDetails().getSecurityConfig()
	 // 						.getEntitlements();
	 // 			}
	 // 			// List<CodespaceSecurityRoleVO> oldRolesList = new ArrayList<>();
	 // 			// if (vo.getProjectDetails().getSecurityConfig().getRoles() != null) {
	 // 			// 	oldRolesList = vo.getProjectDetails().getSecurityConfig().getRoles();
	 // 			// }
	 // 			// List<CodespaceSecurityUserRoleMapVO> olduserRolesMapList = new ArrayList<>();
	 // 			// if (vo.getProjectDetails().getSecurityConfig()
	 // 			// 		.getUserRoleMappings() != null) {
	 // 			// 	olduserRolesMapList = vo.getProjectDetails().getSecurityConfig()
	 // 			// 			.getUserRoleMappings();
	 // 			// }
	 // 			// List<CodespaceSecurityRoleVO> newRoleList = new ArrayList<>();
	 // 			// List<CodespaceSecurityUserRoleMapVO> newUserRoleMapList = new ArrayList<>();
 
	 // 			if (oldEntitlementList != null && data.getEntitlements() != null
	 // 					&& oldEntitlementList.size() > data.getEntitlements().size()) {
 
	 // 				List<CodespaceSecurityEntitlementVO> entitlementListToDelete = new ArrayList<>(oldEntitlementList);
	 // 				entitlementListToDelete.removeAll(data.getEntitlements());
	 // 				for (CodespaceSecurityRoleVO role : oldRolesList) {
	 // 					List<CodespaceSecurityConfigLOV> oldRoleEntitlementList = role.getRoleEntitlements();
	 // 					List<CodespaceSecurityConfigLOV> newRoleEntitlementList = new ArrayList<>();
	 // 					for (CodespaceSecurityConfigLOV roleEntitlement : oldRoleEntitlementList) {
	 // 						for (CodespaceSecurityEntitlementVO entitlementToDelete : entitlementListToDelete) {
	 // 							if (!roleEntitlement.getId().equalsIgnoreCase(entitlementToDelete.getId())) {
	 // 								newRoleEntitlementList.add(roleEntitlement);
	 // 							}
	 // 						}
	 // 					}
	 // 					role.getRoleEntitlements().clear();
	 // 					role.setRoleEntitlements(newRoleEntitlementList);
	 // 					newRoleList.add(role);
	 // 				}
	 // 				assembledSecurityConfig.setRoles(newRoleList);
	 // 			} else {
	 // 				if (data.getRoles() != null) {
	 // 					assembledSecurityConfig.setRoles(data.getRoles());
	 // 				} else {
	 // 					assembledSecurityConfig.setRoles(new ArrayList<>());
	 // 				}
 
	 // 			}
 
	 // 			if (oldRolesList != null && data.getRoles() != null && oldRolesList.size() > data.getRoles().size()) {
	 // 				List<CodespaceSecurityRoleVO> roleListToDelete = new ArrayList<>(oldRolesList);
	 // 				roleListToDelete.removeAll(data.getRoles());
 
	 // 				for (CodespaceSecurityUserRoleMapVO userRoleMap : olduserRolesMapList) {
	 // 					List<CodespaceSecurityConfigLOV> oldUserRoleList = userRoleMap.getRoles();
	 // 					List<CodespaceSecurityConfigLOV> newUserRoleList = new ArrayList<>();
	 // 					for (CodespaceSecurityConfigLOV userRole : oldUserRoleList) {
	 // 						for (CodespaceSecurityRoleVO roleToDelete : roleListToDelete) {
	 // 							if (!userRole.getId().equalsIgnoreCase(roleToDelete.getId())) {
	 // 								newUserRoleList.add(userRole);
	 // 							}
	 // 						}
	 // 					}
	 // 					userRoleMap.getRoles().clear();
	 // 					userRoleMap.setRoles(newUserRoleList);
	 // 					if (!userRoleMap.getRoles().isEmpty()) {
	 // 						newUserRoleMapList.add(userRoleMap);
	 // 					}
 
	 // 				}
	 // 				assembledSecurityConfig.setUserRoleMappings(newUserRoleMapList);
 
	 // 			} else {
	 // 				if (data.getUserRoleMappings() != null) {
	 // 					assembledSecurityConfig.setUserRoleMappings(data.getUserRoleMappings());
	 // 				} else {
	 // 					assembledSecurityConfig.setUserRoleMappings(new ArrayList<>());
	 // 				}
	 // 			}
	 // 		} catch (Exception e) {
	 // 			log.error("Failed in assembler while parsing entitlements/roles with exception {}", e.getMessage());
	 // 		}
	 // 	}
	 // 	return assembledSecurityConfig;
	 // }
 
	 public CodespaceSecurityConfigDetailVO generateSecurityConfigIds(CodespaceSecurityConfigDetailVO data, String projectName) {
		 CodespaceSecurityConfigDetailVO securityConfigWithIds = new CodespaceSecurityConfigDetailVO();
		 if (data != null) {
 
			 //securityConfigWithIds.setStatus(data.getStatus());
			 securityConfigWithIds.setAppID(data.getAppID());
			 if (data.getEntitlements() != null) {
				 securityConfigWithIds.setEntitlements(data.getEntitlements());
			 } else {
				 securityConfigWithIds.setEntitlements(new ArrayList<>());
			 }
			 // if (data.getOpenSegments() != null) {
			 // 	securityConfigWithIds.setOpenSegments(data.getOpenSegments());
			 // } else {
			 // 	securityConfigWithIds.setOpenSegments(new ArrayList<>());
			 // }
			 // if (data.isIsProtectedByDna() != null) {
			 // 	securityConfigWithIds.setIsProtectedByDna(data.isIsProtectedByDna());
			 // }
 
			 List<CodespaceSecurityEntitlementVO> entitlementList = new ArrayList<>();
			 if (data.getEntitlements() != null) {
				 entitlementList = data.getEntitlements();
			 }
			 // List<CodespaceSecurityRoleVO> roleList = new ArrayList<>();
			 // if (data.getRoles() != null) {
			 // 	roleList = data.getRoles();
			 // }
			 // List<CodespaceSecurityUserRoleMapVO> roleMapList = new ArrayList<>();
			 // if (data.getUserRoleMappings() != null) {
			 // 	roleMapList = data.getUserRoleMappings();
			 // }
			 for (CodespaceSecurityEntitlementVO entitlement : entitlementList) {
				 if(entitlement.getId()==null){
					 entitlement.setId(UUID.randomUUID().toString());
				 }
				 // if(projectName!=null){
				 // 	if(!entitlement.getName().startsWith(projectName)){
				 // 		entitlement.setName(projectName+"_"+entitlement.getName());
				 // 	}
				 // }
			 }
			 securityConfigWithIds.setEntitlements(entitlementList);
			 // for(CodespaceSecurityRoleVO role : roleList){
			 // 	if(role.getId()==null){
			 // 		role.setId(UUID.randomUUID().toString());
			 // 	}
				 // if(projectName!=null){
				 // 	if(!role.getName().startsWith(projectName)){
				 // 		role.setName(projectName+"_"+role.getName());
				 // 	}
				 // }
			 // }
			 // securityConfigWithIds.setRoles(roleList);
 //			for(CodespaceSecurityUserRoleMapVO userRoleMap : roleMapList){
 //				if(userRoleMap.getId()==null){
 //					userRoleMap.setId(UUID.randomUUID().toString());
 //				}
 //			}
			 //securityConfigWithIds.setUserRoleMappings(roleMapList);
			 
		 }
		 return securityConfigWithIds;
	 }
 
	 public CodespaceSecurityConfigDetailsVO dtoToVo(CodespaceSecurityConfigDto dto) {
		 CodespaceSecurityConfigDetailsVO vo = new CodespaceSecurityConfigDetailsVO();
		 CodespacePublishedSecurityConfigVO publishedSecuirtyConfigVO = new CodespacePublishedSecurityConfigVO();
 
		 try {
			 if (dto != null) {
				 vo.setId(dto.getId());
				 vo.setProjectName(dto.getProjectName());
				 vo.setProjectOwner(toUserInfoVO(dto.getProjectOwner()));
				 CodespaceSecurityConfigVO  securityConfigVO = tosecurityConfigVO(dto.getSecurityConfig());
				 publishedSecuirtyConfigVO.setStaging(securityConfigVO.getStaging().getPublished());
				 publishedSecuirtyConfigVO.setProduction(securityConfigVO.getProduction().getPublished());
				 vo.setSecurityConfig(publishedSecuirtyConfigVO);
			 }
		 } catch (Exception e) {
			 log.error("Failed in assembler", e.getMessage());
		 }
		 return vo;
	 }
 }
 