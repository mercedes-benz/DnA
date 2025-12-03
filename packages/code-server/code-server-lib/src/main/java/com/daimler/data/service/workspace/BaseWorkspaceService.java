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

 package com.daimler.data.service.workspace;

 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 import java.util.Optional;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.stream.Collector;
 import java.util.stream.Collectors;
 import java.util.Collections;

 import org.json.JSONObject;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.ObjectUtils;
 
 import com.daimler.data.application.auth.UserStore;
 import com.daimler.data.application.client.CodeServerClient;
 import com.daimler.data.application.client.GitClient;
 import com.daimler.data.application.client.VaultClient;
 import com.daimler.data.assembler.CodeServerUserGroupassembler;
 import com.daimler.data.assembler.WorkspaceAssembler;
 import com.daimler.data.auth.client.AuthenticatorClient;
 import com.daimler.data.auth.client.DnaAuthClient;
 import com.daimler.data.controller.exceptions.GenericMessage;
 import com.daimler.data.controller.exceptions.MessageDescription;
 import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
 import com.daimler.data.db.entities.CodeServerUserGroupNsql;
 import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
 import com.daimler.data.db.json.BuildAudit;
 import com.daimler.data.db.json.CodeServerBuildDeploy;
 import com.daimler.data.db.json.CodeServerBuildDetails;
 import com.daimler.data.db.json.CodeServerDeploymentDetails;
 import com.daimler.data.db.json.CodeServerLeanGovernanceFeilds;
 import com.daimler.data.db.json.CodeServerUserGroup;
 import com.daimler.data.db.json.CodeServerUserGroupList;
 import com.daimler.data.db.json.CodeServerUserGroupWsDetails;
 import com.daimler.data.db.json.CodeServerWorkspace;
 import com.daimler.data.db.json.CodespaceSecurityConfig;
 import com.daimler.data.db.json.UserInfo;
 import com.daimler.data.db.json.CodeServerProjectDetails;
 import com.daimler.data.db.repo.workspace.WorkSpaceCodeServerBuildDeployRepository;
 import com.daimler.data.db.repo.workspace.WorkspaceCustomAdditionalServiceRepo;
 import com.daimler.data.db.repo.workspace.WorkspaceCustomBuildDeployRepo;
 import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
 import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
 import com.daimler.data.db.repo.workspace.WorkspaceCustomUserGroupRepo;
 import com.daimler.data.db.repo.workspace.WorkspaceRepository;
 import com.daimler.data.db.repo.workspace.WorkspaceUserGroupRepository;
 import com.daimler.data.dto.AdditionalPropertiesDto;
 import com.daimler.data.dto.CodespaceSecurityConfigDto;
 import com.daimler.data.dto.DeploymentManageDto;
 import com.daimler.data.dto.DeploymentManageInputDto;
 import com.daimler.data.dto.GitLatestCommitIdDto;
 import com.daimler.data.dto.WorkbenchManageDto;
 import com.daimler.data.dto.WorkbenchManageInputDto;
 import com.daimler.data.dto.solution.ChangeLogVO;
 import com.daimler.data.dto.userinfo.UsersCollection;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.CloudServiceProviderEnum;
 import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
 import com.daimler.data.dto.workspace.CodeServerUserGroupByIdVO;
 import com.daimler.data.dto.workspace.CodeServerUserGroupCollectionVO;
 import com.daimler.data.dto.workspace.CodeServerUserGroupVO;
 import com.daimler.data.dto.workspace.CodeServerUserGroupWsDetailsVO;
 import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
 import com.daimler.data.dto.workspace.CodeServerWorkspaceValidateVO;
 import com.daimler.data.dto.workspace.CodeSpaceReadmeVo;
 import com.daimler.data.dto.workspace.CreatedByVO;
 import com.daimler.data.dto.workspace.DataGovernanceRequestInfo;
 import com.daimler.data.dto.workspace.DeployedAppConfigDto;
 import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;
 import com.daimler.data.dto.workspace.ResourceVO;
 import com.daimler.data.dto.workspace.UpdateUserGroupRequestVO;
 import com.daimler.data.dto.workspace.UserInfoVO;
 import com.daimler.data.dto.workspace.WorkspacePluginStatusVO;
 import com.daimler.data.dto.workspace.admin.CodespaceSecurityConfigDetailsVO;
 import com.daimler.data.dto.workspace.recipe.RecipeVO.RecipeTypeEnum;
 import com.daimler.data.dto.workspace.buildDeploy.*;
 import com.daimler.data.util.CommonUtils;
 import com.daimler.data.util.ConstantsUtility;
 import com.daimler.dna.notifications.common.producer.KafkaProducerService;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.daimler.data.db.json.DeploymentAudit;
 import lombok.extern.slf4j.Slf4j;
 
 @Service
 @Slf4j
 @SuppressWarnings(value = "unused")
 public class BaseWorkspaceService implements WorkspaceService {
 
	 @Value("${codeServer.env.ref}")
	 private String codeServerEnvRef;
  
	 @Value("${codeServer.base.uri}")
	 private String codeServerBaseUri;
 
	 @Value("${codeServer.base.uri.aws}")
	 private String codeServerBaseUriAws;
  
	 @Value("${codeServer.git.orgname}")
	 private String gitOrgName;
  
	 @Value("${codeServer.env.value}")
	 private String codeServerEnvValue;
  
	 @Value("${codeServer.env.value.aws}")
	 private String codeServerEnvValueAws;
 
	 @Value("${codeServer.git.orguri}")
	 private String gitOrgUri;
  
	 @Value("${codeServer.git.orgname}")
	 private String orgName;
  
	 @Value("${codeServer.jupyter.url}")
	 private String jupyterUrl;
  
	 @Value("${codeServer.workspace.url}")
	 private String codespaceUrl;
 
	 @Value("${codeServer.workspace.url.aws}")
	 private String codespaceUrlAWS;
 
	 @Value("${codeServer.collab.pid}")
	 private String collabPid;
 
	 @Value("${codeServer.codespace.filename}")
	 private String codespaceFileName;

	 @Value("${codeServer.technical.id}")
	 private String technicalId;

	 @Value("${kong.dnaClientSecret}")
	 private String dnaClientSecret;

	 @Value("${kong.dnaAppId}")
	 private String dnaAppId;
 
	 @Autowired
	 private WorkspaceAssembler workspaceAssembler;
	 @Autowired
	 private WorkspaceCustomRepository workspaceCustomRepository;
	 @Autowired
	 private WorkspaceRepository jpaRepo;
  
	 @Autowired
	 private CodeServerClient client;
  
	 @Autowired
	 private GitClient gitClient;
  
	 @Autowired
	 private AuthenticatorClient authenticatorClient;
  
	 @Autowired
	 private KafkaProducerService kafkaProducer;
  
	 @Autowired
	  private UserStore userStore;
	 
	 @Autowired
	 private WorkspaceCustomAdditionalServiceRepo additionalServiceRepo;
  
	 @Autowired
	 private WorkspaceCustomRecipeRepo workspaceCustomRecipeRepo;
  
	 @Autowired
	 private DnaAuthClient dnaAuthClient;
	
	 @Autowired
	 private VaultClient VaultClient;

	 @Autowired
	 private WorkSpaceCodeServerBuildDeployRepository buildDeployRepo;

	 @Autowired
	 private WorkspaceRepository workSpaceRepo;
 
	 @Autowired
	 private CodeServerUserGroupassembler groupassembler;
 
	 @Autowired
	 private WorkspaceUserGroupRepository userGroupRepository;

	 @Autowired
	 private WorkspaceCustomUserGroupRepo workspaceCustomUserGroupRepo;

	 @Autowired
	 private WorkspaceCustomBuildDeployRepo buildDeployCustomRepo;

	 @Value("${codeServer.build.retainedlimit}")
     private String retainedBuildLimitValue;
 
  
	 public BaseWorkspaceService() {
		 super();
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage deleteById(String userId, String id) {
		 // 1. undeploy if deployed and id is project owner id
		 // 4. update all workspaces under this project
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 CodeServerWorkspaceNsql entity = new CodeServerWorkspaceNsql();

		 if(technicalId.equalsIgnoreCase(userId)){
			 entity = workspaceCustomRepository.findByWorkspaceId(id);
			}
		 else{
		  entity = workspaceCustomRepository.findById(userId, id);
		 }
		 String cloudServiceProvider = entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider();
		 boolean isProjectOwner = false;
		 boolean isCodespaceDeployed = false;
		 String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		 if (projectOwnerId.equalsIgnoreCase(userId)|| technicalId.equalsIgnoreCase(userId)) {
			 isProjectOwner = true;
		 }
  
		 if (isProjectOwner) {
			 log.info("Delete requested by project owner {} ", userId);
			 // undeploy int if present
			 // dont do this undeploy step for public, bat, private, default 
			 if (entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl() != null
					 && entity.getData().getProjectDetails().getIntDeploymentDetails().getLastDeployedBranch() != null
					 && entity.getData().getProjectDetails().getIntDeploymentDetails()
							 .getLastDeploymentStatus() != null) {
				 isCodespaceDeployed = true;
				 String branch = entity.getData().getProjectDetails().getIntDeploymentDetails().getLastDeployedBranch();
				 DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				 DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				 deployJobInputDto.setAction("undeploy");
				 deployJobInputDto.setBranch(branch);
				 if(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
					deployJobInputDto.setEnvironment(codeServerEnvValue);
				} else {
					deployJobInputDto.setEnvironment(codeServerEnvValueAws);
				}
				 deployJobInputDto
						 .setRepo(gitOrgUri + gitOrgName + "/" + entity.getData().getProjectDetails().getGitRepoName());
				 String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				 deployJobInputDto.setShortid(projectOwner);
				 deployJobInputDto.setTarget_env("int");
 //				deployJobInputDto.setSecure_iam("false");
				//  if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				// 	 deployJobInputDto.setType(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
				//  } else {
				// 	 deployJobInputDto.setType("default");
				//  }
				 String projectName = entity.getData().getProjectDetails().getProjectName();
				 String projectOwnerWsId = entity.getData().getWorkspaceId();
				 deployJobInputDto.setWsid(projectOwnerWsId);
				 deployJobInputDto.setProjectName(projectName);
				 deploymentJobDto.setInputs(deployJobInputDto);
				 deploymentJobDto.setRef(codeServerEnvRef);
				 GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				 if (jobResponse != null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					 log.info(
							 "Found deployment of branch {} on Staging environment, undeploy triggered successfully by user {}",
							 branch, userId);
				 } else {
					 log.warn("Found deployment of branch {} on Staging environment, undeploy trigger failed by user {}",
							 branch, userId);
					 MessageDescription intUndeployTriggerFailed = new MessageDescription("Undeploy of branch " + branch
							 + " on Staging environment failed. Please retry deleting Project.");
					 errors.add(intUndeployTriggerFailed);
					 responseMessage.setSuccess("FAILED");
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
			 }
			 // undeploy prod if present
			 if (entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl() != null
					 && entity.getData().getProjectDetails().getProdDeploymentDetails().getLastDeployedBranch() != null
					 && entity.getData().getProjectDetails().getProdDeploymentDetails()
							 .getLastDeploymentStatus() != null) {
				 isCodespaceDeployed = true;
				 String branch = entity.getData().getProjectDetails().getProdDeploymentDetails().getLastDeployedBranch();
				 DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				 DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				 deployJobInputDto.setAction("undeploy");
				 deployJobInputDto.setBranch(branch);
				 if(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
					deployJobInputDto.setEnvironment(codeServerEnvValue);
				} else {
					deployJobInputDto.setEnvironment(codeServerEnvValueAws);
				}
				 deployJobInputDto
						 .setRepo(gitOrgUri + gitOrgName + "/" + entity.getData().getProjectDetails().getGitRepoName());
				 String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				 deployJobInputDto.setShortid(projectOwner);
				 deployJobInputDto.setTarget_env("prod");
  //				deployJobInputDto.setSecure_iam("false");
				 deployJobInputDto.setProjectName(projectOwnerId);
				//  if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				// 	 deployJobInputDto.setType(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
				//  } else {
				// 	 deployJobInputDto.setType("default");
				//  }
				 String projectName = entity.getData().getProjectDetails().getProjectName();
				 String projectOwnerWsId = entity.getData().getWorkspaceId();
				 deployJobInputDto.setWsid(projectOwnerWsId);
				 deployJobInputDto.setProjectName(projectName);
				 deploymentJobDto.setInputs(deployJobInputDto);
				 deploymentJobDto.setRef(codeServerEnvRef);
				 GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				 if (jobResponse != null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					 log.info(
							 "Found deployment of branch {} on Production environment, undeploy triggered successfully by user {}",
							 branch, userId);
				 } else {
					 log.warn(
							 "Found deployment of branch {} on Production environment, undeploy trigger failed by user {}",
							 branch, userId);
					 MessageDescription prodUndeployTriggerFailed = new MessageDescription("Undeploy of branch " + branch
							 + " on Production environment failed. Please retry deleting Project.");
					 errors.add(prodUndeployTriggerFailed);
					 responseMessage.setSuccess("FAILED");
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
			 }
		 }
  
		 String repoName = entity.getData().getProjectDetails().getGitRepoName();
		 /*
		  * if(isProjectOwner) {
		  * //deleting repo
		  * HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
		  * if(!deleteRepoStatus.is2xxSuccessful()) {
		  * MessageDescription gitRepoDeleteWarning = new
		  * MessageDescription("Failed while deleting git repository " +repoName +
		  * " for project. Please delete manually.");
		  * warnings.add(gitRepoDeleteWarning);
		  * }else {
		  * log.info("Repository {} deleted for the project by owner {} ", repoName,
		  * userId);
		  * }
		  * }else {
		  * //removing collab user from repo
		  * HttpStatus deleteUserFromRepoStatus = gitClient.deleteUserFromRepo(userId,
		  * repoName);
		  * if(!deleteUserFromRepoStatus.is2xxSuccessful()) {
		  * MessageDescription gitRepoDeleteWarning = new
		  * MessageDescription("Failed to remove user from git repository " +repoName +
		  * " for project. Please remove manually.");
		  * warnings.add(gitRepoDeleteWarning);
		  * }else {
		  * log.info("User {} removed for the project repo {} successfully",userId,
		  * repoName);
		  * }
		  * 
		  * }
		  */
		 // trigger delete of all project members workspaces if user is owner otherwise
		 // trigger just for user individual workspace
		 String projectName = entity.getData().getProjectDetails().getProjectName();
		 String recipeType = entity.getData().getProjectDetails().getRecipeDetails().getToDeployType();
		 if(recipeType == null) {
			 recipeType = "default";
		 }
		 String environment = null;
		 if(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
			environment = codeServerEnvValue;
		 } else {
			environment = codeServerEnvValueAws;
		 }
		 
		 // List<Object[]> records = new ArrayList<>();
		 // if(isProjectOwner) {
		 // records =
		 // workspaceCustomRepository.getWorkspaceIdsForProjectMembers(projectName);
		 // }else {
		 // Object[] collabRecord =
		 // {entity.getData().getWorkspaceId(),entity.getData().getWorkspaceOwner().getId()};
		 // records.add(collabRecord);
		 // }
		 // for(Object[] record: records) {
		 WorkbenchManageDto ownerWorkbenchDeleteDto = new WorkbenchManageDto();
		 ownerWorkbenchDeleteDto.setRef(codeServerEnvRef);
		 WorkbenchManageInputDto ownerWorkbenchDeleteInputsDto = new WorkbenchManageInputDto();
		 ownerWorkbenchDeleteInputsDto.setAction(ConstantsUtility.DELETEACTION);
		 ownerWorkbenchDeleteInputsDto.setEnvironment(environment);
		 ownerWorkbenchDeleteInputsDto.setIsCollaborator("false");
		 ownerWorkbenchDeleteInputsDto.setPat("");
		 ownerWorkbenchDeleteInputsDto.setResource("");
		 String repoNameWithOrg = gitOrgUri + gitOrgName + "/" + repoName;
		 ownerWorkbenchDeleteInputsDto.setRepo(repoNameWithOrg);
		 String workspaceUserId = entity.getData().getWorkspaceOwner().getId();
		 ownerWorkbenchDeleteInputsDto.setShortid(workspaceUserId);
		 ownerWorkbenchDeleteInputsDto.setType(recipeType);
		 ownerWorkbenchDeleteInputsDto.setCloudServiceProvider(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider());
		 ownerWorkbenchDeleteInputsDto.setWsid(entity.getData().getWorkspaceId());
		 ownerWorkbenchDeleteDto.setInputs(ownerWorkbenchDeleteInputsDto);
		 if(entity.getData().getStatus().equalsIgnoreCase("CREATED"))
		 {
			 boolean deleteAction = client.deleteServer(ownerWorkbenchDeleteDto);
			 if(!deleteAction)
			 {
				 log.warn("Deleting is failed for {} for user {}",entity.getData().getWorkspaceId(), workspaceUserId);
				 MessageDescription prodUndeployTriggerFailed = new MessageDescription("Failed while deleting codespace for user.");
				 errors.add(prodUndeployTriggerFailed);
				 responseMessage.setSuccess("FAILED");
				 responseMessage.setErrors(errors);
				 return responseMessage;
  
			 }
		 }
		 // }
		 // update all workspaces for the project to deleted state in db if user is
		 // projectOwner otherwise change state to deleted only for individual workspace
		 // if(isProjectOwner) {
		 // workspaceCustomRepository.updateDeletedStatusForProject(projectName);
		 // }else {
		 if(technicalId.equalsIgnoreCase(userId) && entity.getData().getProjectDetails().getDataGovernance().getTypeOfProject().equalsIgnoreCase("Playground")){
			entity.getData().setStatus("DELETED");
			jpaRepo.save(entity);
		 }


		 else {
		 entity.getData().setStatus("DELETED");
		 entity.getData().setActiveInGroup(Boolean.FALSE);

		 //update status as deleted in logs

		 CodeServerBuildDeployNsql buildDeployNsql = buildDeployCustomRepo.findByProjectName(projectName);
		 if(buildDeployNsql != null){
			buildDeployNsql.getData().setStatus("DELETED");
			buildDeployRepo.save(buildDeployNsql);
		 }



		 //remove from group
		 // List<String> groupEntity = workspaceCustomUserGroupRepo.findByWsid(entity.getData().getWorkspaceId(), userId);
		 // log.info("groupEntity {}",groupEntity.toString());
		 String wsId = entity.getData().getWorkspaceId();
		 // groupEntity.forEach( i ->{
			 Optional<CodeServerUserGroupNsql> groupOptional = userGroupRepository.findById(userId);
			 if(groupOptional.isPresent()){
				CodeServerUserGroupNsql group = groupOptional.get();
			 group.getData().getGroups().forEach( g -> {
				 g.getWorkspaces().removeIf( w -> w.getWorkSpaceId().equalsIgnoreCase(wsId));
			 });
			 userGroupRepository.save(group);
			}
		 // });
  
		 UserInfo removeUser = new UserInfo();
		 if (entity.getData().getProjectDetails().getProjectCollaborators() != null) {
			 for (UserInfo collaborator : entity.getData().getProjectDetails().getProjectCollaborators()) {
				 if (userId != null) {
					 if (collaborator.getId().equalsIgnoreCase(userId)) {
						 removeUser = collaborator;
						 break;
					 }
				 }
			 }
		 }
		 jpaRepo.save(entity);
		 workspaceCustomRepository.updateCollaboratorDetails(projectName, removeUser, true);
		}
		 // }
		 // Deleting Kong route
		 if((entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl() != null
		 && entity.getData().getProjectDetails().getIntDeploymentDetails().getLastDeployedBranch() != null
		 && entity.getData().getProjectDetails().getIntDeploymentDetails()
				 .getLastDeploymentStatus() != null) ||(entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl() != null
				 && entity.getData().getProjectDetails().getProdDeploymentDetails().getLastDeployedBranch() != null
				 && entity.getData().getProjectDetails().getProdDeploymentDetails()
						 .getLastDeploymentStatus() != null) ){
		 GenericMessage deleteRouteResponse = authenticatorClient.deleteRoute(entity.getData().getWorkspaceId(),
				 entity.getData().getWorkspaceId(), cloudServiceProvider);
		 if (deleteRouteResponse != null && deleteRouteResponse.getSuccess()!= null && deleteRouteResponse.getSuccess().equalsIgnoreCase("Success"))
			 log.info("Kong route: {} deleted successfully", entity.getData().getWorkspaceId());
		 else {
			 if (deleteRouteResponse.getErrors() != null && deleteRouteResponse.getErrors().get(0) != null) {
				 log.info("Failed to delete the Kong route: {} with exception : {}", entity.getData().getWorkspaceId(),
						 deleteRouteResponse.getErrors().get(0).getMessage());
			 }
		 }
  
		 // Deleting Kong service
		 GenericMessage deleteServiceResponse = authenticatorClient.deleteService(entity.getData().getWorkspaceId(), cloudServiceProvider);
		 if (deleteServiceResponse != null && deleteServiceResponse.getSuccess() != null && deleteServiceResponse.getSuccess().equalsIgnoreCase("Success"))
			 log.info("Kong service: {} deleted successfully", entity.getData().getWorkspaceId());
		 else {
			 if (deleteServiceResponse.getErrors() != null && deleteServiceResponse.getErrors().get(0) != null) {
				 log.info("Failed to delete the Kong service: {} with exception : {}", entity.getData().getWorkspaceId(),
						 deleteServiceResponse.getErrors().get(0).getMessage());
			 }
		 }
	 }
		 // deleting kong route and service if codespace is deployed to staging/production
  //		if (isCodespaceDeployed) {
  //			String serviceName = entity.getData().getWorkspaceId() + "-api";
  //			// Deleting Kong route
  //			GenericMessage deployDeleteRouteResponse = authenticatorClient.deleteRoute(serviceName, serviceName);
  //			if (deployDeleteRouteResponse != null && deployDeleteRouteResponse.getSuccess().equalsIgnoreCase("Success"))
  //				log.info("Kong route: {} deleted successfully", serviceName);
  //			else {
  //				if (deployDeleteRouteResponse.getErrors() != null
  //						&& deployDeleteRouteResponse.getErrors().get(0) != null) {
  //					log.info("Failed to delete the Kong route: {} with exception : {}", serviceName,
  //							deployDeleteRouteResponse.getErrors().get(0).getMessage());
  //				}
  //			}
  //
  //			// Deleting Kong service
  //			GenericMessage deployDeleteServiceResponse = authenticatorClient.deleteService(serviceName);
  //			if (deployDeleteServiceResponse != null
  //					&& deployDeleteServiceResponse.getSuccess().equalsIgnoreCase("Success"))
  //				log.info("Kong service: {} deleted successfully", serviceName);
  //			else {
  //				if (deployDeleteServiceResponse.getErrors() != null
  //						&& deployDeleteServiceResponse.getErrors().get(0) != null) {
  //					log.info("Failed to delete the Kong service: {} with exception : {}", serviceName,
  //							deployDeleteServiceResponse.getErrors().get(0).getMessage());
  //				}
  //			}
  //
  //		}
		 responseMessage.setSuccess("SUCCESS");
		 responseMessage.setErrors(errors);
		 responseMessage.setWarnings(warnings);
		 return responseMessage;
	 }
  
	 @Override
	 @Transactional
	 public InitializeWorkspaceResponseVO initiateWorkspacewithAdminPat(CodeServerWorkspaceVO vo, String pat){
		InitializeWorkspaceResponseVO responseVO = new InitializeWorkspaceResponseVO();
		responseVO.setData(null);
		responseVO.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
 
			CodeServerWorkspaceNsql entity = workspaceAssembler.toEntity(vo);
			
			String repoName = "";
			String repoNameWithOrg = "";
			List<UserInfoVO> collabs = vo.getProjectDetails().getProjectCollaborators();
			boolean isOwner = false;
			List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
			String projectName = vo.getProjectDetails().getProjectName();
			repoName = vo.getProjectDetails().getGitRepoName();
			if (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public") || vo
					.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private")) {
				repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
			}
			String pathCheckout = "";
			if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")
					&& !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
							.startsWith("private")
			   ) {
				repoNameWithOrg = gitOrgUri + gitOrgName + "/" + repoName;
			} else {
				repoNameWithOrg = vo.getProjectDetails().getRecipeDetails().getRepodetails();
				if(repoNameWithOrg.contains(",")) {
				   String url[] = repoNameWithOrg.split(",");
				   repoNameWithOrg = url[0];
				   pathCheckout = url[1];
			   } else {
				   pathCheckout = "";
			   }
			}
			if(repoNameWithOrg.isEmpty()) {
			   pathCheckout = "";
			   repoNameWithOrg = vo.getProjectDetails().getGitRepoName().replace("https://", "");
		   }
			UserInfoVO projectOwner = vo.getProjectDetails().getProjectOwner();
			UserInfoVO workspaceOwner = vo.getWorkspaceOwner();
			String projectOwnerId = "";
			// validate user pat
			String RecipeId = null;
		   if(vo.getProjectDetails().getRecipeDetails().getRecipeId()!=null){
			   vo.getProjectDetails().getRecipeDetails().setRecipeId(RecipeIdEnum.fromValue(vo.getProjectDetails().getRecipeDetails().getRecipeId().toString()));
		   } else if(vo.getProjectDetails().getRecipeDetails().getRecipeType().equals(ConstantsUtility.GENERIC)) {
			   vo.getProjectDetails().getRecipeDetails().setRecipeId(RecipeIdEnum.TEMPLATE);
		   } else {
			   vo.getProjectDetails().getRecipeDetails().setRecipeId(RecipeIdEnum.PRIVATE_USER_DEFINED);
		   }
		   if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default") && 
				!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				HttpStatus validateUserPatstatus = gitClient.validateGitPat(collabPid, pat);
				if (!validateUserPatstatus.is2xxSuccessful()) {
					MessageDescription errMsg = new MessageDescription(
							"Invalid Git Personal Access Token provided. Please verify and retry.");
					errors.add(errMsg);
					responseVO.setErrors(errors);
					return responseVO;
				}
			}
			else {
				if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default")) {
					HttpStatus publicGitPatStatus = gitClient.validatePublicGitPat(entity.getData().getGitUserName(), pat, repoName);
					if(!publicGitPatStatus.is2xxSuccessful()) {
						MessageDescription errMsg = new MessageDescription("Invalid GitHub Personal Access Token. Please verify and retry");
						errors.add(errMsg);
						responseVO.setErrors(errors);
						return responseVO;
					}
				}
			}
 
			WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
			WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
			// ownerWorkbenchCreateInputsDto.setAction(ConstantsUtility.CREATEACTION);
			String resource = entity.getData().getProjectDetails().getRecipeDetails().getResource() ;
			String[] parts = resource.split(",");
			ownerWorkbenchCreateInputsDto.setStorage_capacity(parts[0]);
			ownerWorkbenchCreateInputsDto.setMem_guarantee(parts[1]);
			ownerWorkbenchCreateInputsDto.setMem_limit(parts[3]);
			double cpuLimit = Double.parseDouble(parts[4].replaceAll("[^0-9.]", ""));
			double cpuGuarantee = Double.parseDouble(parts[2].replaceAll("[^0-9.]", ""));
			ownerWorkbenchCreateInputsDto.setCpu_limit(cpuLimit);
			ownerWorkbenchCreateInputsDto.setCpu_guarantee(cpuGuarantee);
			if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				ownerWorkbenchCreateInputsDto.setProfile(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
			} else {
				ownerWorkbenchCreateInputsDto.setProfile("default");
			}
			if(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
				ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValue);
			} else {
				ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValueAws);
			}
			ownerWorkbenchCreateInputsDto.setPathCheckout(pathCheckout);
			if(Objects.nonNull(projectOwner) && Objects.nonNull(workspaceOwner) && projectOwner.getId().equalsIgnoreCase(workspaceOwner.getId())) {
				 ownerWorkbenchCreateInputsDto.setIsCollaborator("false");
				 isOwner = true;
				 projectOwnerId = projectOwner.getId();
			 }
			 else {
				 ownerWorkbenchCreateInputsDto.setIsCollaborator("true");
			 }
			ownerWorkbenchCreateInputsDto.setPat(pat);
			ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg.replace("https://", ""));
			ownerWorkbenchCreateInputsDto.setShortid(entity.getData().getWorkspaceOwner().getId());
			if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				ownerWorkbenchCreateInputsDto.setType(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
			} else {
				ownerWorkbenchCreateInputsDto.setType("default");
			}
			List<String> extraContainers = new ArrayList<>();
			List<String> additionalServices =  vo.getProjectDetails().getRecipeDetails().getAdditionalServices();
			if (additionalServices != null) {
				for (String additionalService : additionalServices) {
					String additionalServiceEnv = additionalServiceRepo.findByServiceName(additionalService);
					if(!additionalServiceEnv.isEmpty()) {
						StringBuffer addStringBuffer =  new StringBuffer();
						addStringBuffer.append(additionalServiceEnv);
						addStringBuffer.deleteCharAt(0);
						addStringBuffer.deleteCharAt(addStringBuffer.length()-1);
						extraContainers.add(addStringBuffer.toString());
					}
				}
			}
			ownerWorkbenchCreateInputsDto.setExtraContainers(extraContainers);
			ownerWorkbenchCreateInputsDto.setWsid(entity.getData().getWorkspaceId());
			ownerWorkbenchCreateInputsDto.setResource(vo.getProjectDetails().getRecipeDetails().getResource());
			ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
			String codespaceName = vo.getProjectDetails().getProjectName();
			String ownerwsid = vo.getWorkspaceId();
			GenericMessage createOwnerWSResponse = client.doCreateCodeServer(ownerWorkbenchCreateDto,codespaceName);
			if (createOwnerWSResponse != null) {
				if (!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) ||
						(createOwnerWSResponse.getErrors() != null && !createOwnerWSResponse.getErrors().isEmpty()) ||
						(createOwnerWSResponse.getWarnings() != null
								&& !createOwnerWSResponse.getWarnings().isEmpty())) {
					if(vo.getGitUserName()!=null) {
						log.info("intiate workbench failed for user "+vo.getGitUserName());
					}
					MessageDescription errMsg = new MessageDescription(
							"Failed to initialize workbench while creating individual codespaces, please retry.");
					errors.add(errMsg);
					errors.addAll(createOwnerWSResponse.getErrors());
					warnings.addAll(createOwnerWSResponse.getWarnings());
					responseVO.setErrors(errors);
					responseVO.setWarnings(warnings);
					return responseVO;
 
				}
			}
			if(vo.getGitUserName()!=null) {
				log.info("intiate workbench successful for user "+vo.getGitUserName());
			}
			Date initatedOn = new Date();
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			entity.getData().setIntiatedOn(isoFormat.parse(isoFormat.format(new Date())));
			// entity.getData().setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
			entity.getData().setStatus(ConstantsUtility.CREATEDSTATE);//added
			String recipeId = vo.getProjectDetails().getRecipeDetails().getRecipeId().toString();
			String workspaceUrl = this.getWorkspaceUrl(recipeId,ownerwsid,workspaceOwner.getId(),vo.getProjectDetails().getRecipeDetails().getCloudServiceProvider().toString());
			entity.getData().setWorkspaceUrl(workspaceUrl);
			jpaRepo.save(entity);
			responseVO.setData(workspaceAssembler.toVo(entity));
			responseVO.setErrors(new ArrayList<>());
			responseVO.setWarnings(new ArrayList<>());
			responseVO.setSuccess("SUCCESS");
			return responseVO;
		} catch (Exception e) {
			MessageDescription errMsg = new MessageDescription(
					"Failed to initialize workbench with exception." + e.getMessage() + " Please retry.");
			errors.add(errMsg);
			responseVO.setErrors(errors);
			responseVO.setWarnings(warnings);
			return responseVO;
		}	 
	}
 
	 @Override
	 @Transactional
	 public InitializeWorkspaceResponseVO initiateWorkspace(CodeServerWorkspaceVO vo, String pat) {
		 InitializeWorkspaceResponseVO responseVO = new InitializeWorkspaceResponseVO();
		 responseVO.setData(null);
		 responseVO.setSuccess("FAILED");
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 try {
  
			 CodeServerWorkspaceNsql entity = workspaceAssembler.toEntity(vo);
			 
			 String repoName = "";
			 String repoNameWithOrg = "";
			 List<UserInfoVO> collabs = vo.getProjectDetails().getProjectCollaborators();
			 boolean isOwner = false;
			 List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
			 String projectName = vo.getProjectDetails().getProjectName();
			 repoName = vo.getProjectDetails().getGitRepoName();
			 if (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public") || vo
					 .getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private")) {
				 repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
			 }
			 String pathCheckout = "";
			 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")
					 && !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
							 .startsWith("private")
				) {
				 repoNameWithOrg = gitOrgUri + gitOrgName + "/" + repoName;
			 } else {
				 repoNameWithOrg = vo.getProjectDetails().getRecipeDetails().getRepodetails();
				 if(repoNameWithOrg.contains(",")) {
					String url[] = repoNameWithOrg.split(",");
					repoNameWithOrg = url[0];
					pathCheckout = url[1];
				} else {
					pathCheckout = "";
				}
			 }
			 if(repoNameWithOrg.isEmpty()) {
				pathCheckout = "";
				repoNameWithOrg = vo.getProjectDetails().getGitRepoName().replace("https://", "");
			}
			 UserInfoVO projectOwner = vo.getProjectDetails().getProjectOwner();
			 UserInfoVO workspaceOwner = vo.getWorkspaceOwner();
			 String projectOwnerId = "";
			 // validate user pat
			 String RecipeId = null;
			if(vo.getProjectDetails().getRecipeDetails().getRecipeId()!=null){
				vo.getProjectDetails().getRecipeDetails().setRecipeId(RecipeIdEnum.fromValue(vo.getProjectDetails().getRecipeDetails().getRecipeId().toString()));
			} else if(vo.getProjectDetails().getRecipeDetails().getRecipeType().equals(ConstantsUtility.GENERIC)) {
				vo.getProjectDetails().getRecipeDetails().setRecipeId(RecipeIdEnum.TEMPLATE);
			} else {
				vo.getProjectDetails().getRecipeDetails().setRecipeId(RecipeIdEnum.PRIVATE_USER_DEFINED);
			}
			if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default") && 
				 !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				 HttpStatus validateUserPatstatus = gitClient.validateGitPat(entity.getData().getGitUserName(), pat);
				 if (!validateUserPatstatus.is2xxSuccessful()) {
					 MessageDescription errMsg = new MessageDescription(
							 "Invalid Git Personal Access Token provided. Please verify and retry.");
					 errors.add(errMsg);
					 responseVO.setErrors(errors);
					 return responseVO;
				 }
			 }
			 else {
				 if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default")) {
					 HttpStatus publicGitPatStatus = gitClient.validatePublicGitPat(entity.getData().getGitUserName(), pat, repoName);
					 if(!publicGitPatStatus.is2xxSuccessful()) {
						 MessageDescription errMsg = new MessageDescription("Invalid GitHub Personal Access Token. Please verify and retry");
						 errors.add(errMsg);
						 responseVO.setErrors(errors);
						 return responseVO;
					 }
				 }
			 }
  
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
			 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
			 // ownerWorkbenchCreateInputsDto.setAction(ConstantsUtility.CREATEACTION);
			 String resource = entity.getData().getProjectDetails().getRecipeDetails().getResource() ;
			 String[] parts = resource.split(",");
			 ownerWorkbenchCreateInputsDto.setStorage_capacity(parts[0]);
			 ownerWorkbenchCreateInputsDto.setMem_guarantee(parts[1]);
			 ownerWorkbenchCreateInputsDto.setCloudServiceProvider(ConstantsUtility.DHC_CAAS_AWS);
			 ownerWorkbenchCreateInputsDto.setMem_limit(parts[3]);
			 double cpuLimit = Double.parseDouble(parts[4].replaceAll("[^0-9.]", ""));
			 double cpuGuarantee = Double.parseDouble(parts[2].replaceAll("[^0-9.]", ""));
			 ownerWorkbenchCreateInputsDto.setCpu_limit(cpuLimit);
			 ownerWorkbenchCreateInputsDto.setCpu_guarantee(cpuGuarantee);
			 if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				 ownerWorkbenchCreateInputsDto.setProfile(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
			 } else {
				 ownerWorkbenchCreateInputsDto.setProfile("default");
			 }
			 
			 ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValueAws);
			 ownerWorkbenchCreateInputsDto.setPathCheckout(pathCheckout);
			 if(Objects.nonNull(projectOwner) && Objects.nonNull(workspaceOwner) && projectOwner.getId().equalsIgnoreCase(workspaceOwner.getId())) {
				  ownerWorkbenchCreateInputsDto.setIsCollaborator("false");
				  isOwner = true;
				  projectOwnerId = projectOwner.getId();
			  }
			  else {
				  ownerWorkbenchCreateInputsDto.setIsCollaborator("true");
			  }
			 ownerWorkbenchCreateInputsDto.setPat(pat);
			 if(repoNameWithOrg.endsWith("/")){
				 StringBuffer fixRepoSuffix = new StringBuffer();
				 fixRepoSuffix.append(repoNameWithOrg);
				 fixRepoSuffix.deleteCharAt(repoNameWithOrg.length()-1);
				 repoNameWithOrg = fixRepoSuffix.toString();
			  }
			 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg.replace("https://", ""));
			 ownerWorkbenchCreateInputsDto.setShortid(entity.getData().getWorkspaceOwner().getId());
			 if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				 ownerWorkbenchCreateInputsDto.setType(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
			 } else {
				 ownerWorkbenchCreateInputsDto.setType("default");
			 }
			 List<String> extraContainers = new ArrayList<>();
			 List<String> additionalServices =  vo.getProjectDetails().getRecipeDetails().getAdditionalServices();
			 if (additionalServices != null) {
				 for (String additionalService : additionalServices) {
					 String additionalServiceEnv = additionalServiceRepo.findByServiceName(additionalService);
					 if(!additionalServiceEnv.isEmpty()) {
						 StringBuffer addStringBuffer =  new StringBuffer();
						 addStringBuffer.append(additionalServiceEnv);
						 addStringBuffer.deleteCharAt(0);
						 addStringBuffer.deleteCharAt(addStringBuffer.length()-1);
						 extraContainers.add(addStringBuffer.toString());
					 }
				 }
			 }
			 ownerWorkbenchCreateInputsDto.setExtraContainers(extraContainers);
			 ownerWorkbenchCreateInputsDto.setWsid(entity.getData().getWorkspaceId());
			 ownerWorkbenchCreateInputsDto.setResource(vo.getProjectDetails().getRecipeDetails().getResource());
			 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
			 String codespaceName = vo.getProjectDetails().getProjectName();
			 String ownerwsid = vo.getWorkspaceId();
			 GenericMessage createOwnerWSResponse = client.doCreateCodeServer(ownerWorkbenchCreateDto,codespaceName);
			 if (createOwnerWSResponse != null) {
				 if (!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) ||
						 (createOwnerWSResponse.getErrors() != null && !createOwnerWSResponse.getErrors().isEmpty()) ||
						 (createOwnerWSResponse.getWarnings() != null
								 && !createOwnerWSResponse.getWarnings().isEmpty())) {
  
					 MessageDescription errMsg = new MessageDescription(
							 "Failed to initialize workbench while creating individual codespaces, please retry.");
					 errors.add(errMsg);
					 errors.addAll(createOwnerWSResponse.getErrors());
					 warnings.addAll(createOwnerWSResponse.getWarnings());
					 responseVO.setErrors(errors);
					 responseVO.setWarnings(warnings);
					 return responseVO;
  
				 }
			 }
			 Date initatedOn = new Date();
			 
			 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			 entity.getData().setIntiatedOn(isoFormat.parse(isoFormat.format(new Date())));
			 // entity.getData().setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
			 entity.getData().setStatus(ConstantsUtility.CREATEDSTATE);//added
			 String recipeId = vo.getProjectDetails().getRecipeDetails().getRecipeId().toString();
			 String workspaceUrl = this.getWorkspaceUrl(recipeId,ownerwsid,workspaceOwner.getId(),ConstantsUtility.DHC_CAAS_AWS);
			 entity.getData().setWorkspaceUrl(workspaceUrl);
			 jpaRepo.save(entity);
			 responseVO.setData(workspaceAssembler.toVo(entity));
			 responseVO.setErrors(new ArrayList<>());
			 responseVO.setWarnings(new ArrayList<>());
			 responseVO.setSuccess("SUCCESS");
			 return responseVO;
		 } catch (Exception e) {
			 MessageDescription errMsg = new MessageDescription(
					 "Failed to initialize workbench with exception." + e.getMessage() + " Please retry.");
			 errors.add(errMsg);
			 responseVO.setErrors(errors);
			 responseVO.setWarnings(warnings);
			 return responseVO;
		 }
	 }
  
	 @Override
	 @Transactional
	 public InitializeWorkspaceResponseVO createWorkspace(CodeServerWorkspaceVO vo, String pat) {
		CreatedByVO currentUser = this.userStore.getVO();
		InitializeWorkspaceResponseVO responseVO = new InitializeWorkspaceResponseVO();
		 responseVO.setData(vo);
		 responseVO.setSuccess("FAILED");
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 try {
			 RecipeIdEnum recipe = vo.getProjectDetails().getRecipeDetails().getRecipeId();
			 String recipeIdType = vo.getProjectDetails().getRecipeDetails().getToDeployType();
			 if(recipeIdType==null){
				 recipeIdType = "default";
			 }
			 //map to store git username and is admin permission to repo
			 Map<String,Boolean> gitUsers = new HashMap<>();
			 UserInfoVO owner = vo.getProjectDetails().getProjectOwner();
  
			 String repoName = vo.getProjectDetails().getGitRepoName();
			 if (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public") || vo
					 .getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private")) {
				 repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
			 }
			 List<UserInfoVO> collabs = new ArrayList<>();
			 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				 // validate user pat
				 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
						 .equalsIgnoreCase("default")) {
					 HttpStatus validateUserPatstatus = gitClient.validateGitPat(owner.getGitUserName(), pat);
					 if (!validateUserPatstatus.is2xxSuccessful()) {
						 MessageDescription errMsg = new MessageDescription(
								 "Invalid GitHub Personal Access Token provided. Please verify and retry.");
						 errors.add(errMsg);
						 responseVO.setErrors(errors);
						 return responseVO;
					 }
				 }
 
				 // initialize repo
				 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
						 .startsWith("private") &&
						 !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
						 .equalsIgnoreCase("default")) {
					 repoName = vo.getProjectDetails().getGitRepoName();
					 String recipeName = vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase();
					 if(null != RecipeIdEnum.fromValue(recipeName)){
						recipeName = recipeName+"-template";
					 }
						String gitUrl = vo.getProjectDetails().getRecipeDetails().getRepodetails();
						List<String> repoDetails = null;
						repoDetails = CommonUtils.getRepoNameFromGitUrl(gitUrl);
						if(null!=gitUrl && !gitUrl.isBlank()) {
							recipeName = repoDetails.get(1);
						}
						String repoOwner = repoDetails.get(0);
						HttpStatus createRepoStatus = gitClient.createRepo(repoOwner,repoName,recipeName);
						if (!createRepoStatus.is2xxSuccessful()) {
							 MessageDescription errMsg = new MessageDescription(
									 "Failed while initializing git repository " + repoName
											 + " for codespace  with status " + createRepoStatus.name()
											 + " . Please verify inputs/permissions/existing repositories and retry.");
							 errors.add(errMsg);
							 responseVO.setErrors(errors);
							 return responseVO;
						 }
  //					}
  
					 // create repo success, adding collabs
  
					 gitUsers.put(owner.getGitUserName(),false);
  //					if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
  //							.equalsIgnoreCase("default")
  //							&& !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
  //							.startsWith("bat")) {
						 collabs = vo.getProjectDetails().getProjectCollaborators();
						 if (collabs != null && !collabs.isEmpty()) {
							 // List<String> collabsGitUserNames = collabs.stream().map(n -> n.getGitUserName())
							 // 		.collect(Collectors.toList());
							 // gitUsers.addAll(collabsGitUserNames);
							 for(UserInfoVO user : collabs){
								 gitUsers.put(user.getGitUserName(),user.isIsAdmin());
							 }
						 }
							for (Map.Entry<String, Boolean> gitUser : gitUsers.entrySet()) {
								HttpStatus addGitUser = gitClient.addUserToRepo(gitUser.getKey(), repoName);
								if (addGitUser == HttpStatus.UNPROCESSABLE_ENTITY) {
									log.info("Failed while adding {} as collaborator with status {}",gitUser.getKey(), addGitUser.name());
									MessageDescription errMsg = new MessageDescription(
											"Failed while adding " + gitUser.getKey()
													+ " as collaborator, Because"
													+ " the Git user account Suspended, please ask the user to Login again and add this user manually in the git repo.");
									errors.add(errMsg);
									responseVO.setSuccess("FAILED");
									responseVO.setErrors(errors);
									return responseVO;
								}
								if (!addGitUser.is2xxSuccessful()) {
									MessageDescription warnMsg = new MessageDescription("Failed while adding " + gitUser
											+ " as collaborator to repository. Please add manually and try again.");
									log.info(
											"Failed while adding {} as collaborator to repository. Please add manually",
											gitUser);
									warnings.add(warnMsg);
									responseVO.setWarnings(warnings);
									/*
									 * HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
									 * log.
									 * info("Created git repository {} successfully. Failed while adding {} as collaborator with status {} and delete repo status as {} "
									 * ,repoName,gitUser,addGitUser.name(),deleteRepoStatus.name());
									 * if(deleteRepoStatus.is2xxSuccessful()) {
									 * MessageDescription errMsg = new MessageDescription("Created git repository "
									 * +repoName + " successfully. Failed while adding " + gitUser +
									 * " as collaborator . Please make " + gitUser +
									 * " is valid git user. Deleted repository successfully, please retry");
									 * errors.add(errMsg);
									 * responseVO.setErrors(errors);
									 * return responseVO;
									 * }else {
									 * MessageDescription errMsg = new MessageDescription("Created git repository "
									 * +repoName + " successfully. Failed while adding " + gitUser +
									 * " as collaborator . Please make " + gitUser +
									 * " is valid git user. Unable to delete repository because of " +
									 * deleteRepoStatus.name() + ", please delete repository manually and retry");
									 * errors.add(errMsg);
									 * responseVO.setErrors(errors);
									 * return responseVO;
									 * }
									 */
								}
								if (gitUser.getValue() != null) {
									if (gitUser.getValue()) {
										HttpStatus addAdminAccessToGitUser = gitClient
												.addAdminAccessToRepo(gitUser.getKey(), repoName);
										if (!addAdminAccessToGitUser.is2xxSuccessful()) {
											MessageDescription warnMsg = new MessageDescription(
													"Failed while adding " + gitUser.getKey()
															+ " as admin to repository");
											log.info(
													"Failed while adding {} as collaborator to repository. Please add manually",
													gitUser.getKey());
											warnings.add(warnMsg);
											responseVO.setWarnings(warnings);
										}
									}
								}
							}
  //					}
				 }
			 } else {
				 // repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
				 HttpStatus publicGitPatStatus = gitClient.validatePublicGitPat(vo.getGitUserName(), pat, repoName);
				 if (!publicGitPatStatus.is2xxSuccessful()) {
					 MessageDescription errMsg = new MessageDescription(
							 "Invalid Personal Access Token. Please verify and retry");
					 errors.add(errMsg);
					 responseVO.setErrors(errors);
					 return responseVO;
				 }				 
				 repoName = vo.getProjectDetails().getRecipeDetails().getGitPath()+","+vo.getProjectDetails().getRecipeDetails().getGitRepoLoc();
				}
				if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private")) {
					HttpStatus addAdminAccessToGitUser = gitClient.addAdminAccessToRepo(owner.getGitUserName(),
							repoName);
					if (!addAdminAccessToGitUser.is2xxSuccessful()) {
						MessageDescription warnMsg = new MessageDescription(
								"Failed while adding " + owner.getGitUserName()
										+ " as admin to repository");
						log.info("Failed while adding {} as collaborator to repository. Please add manually",
								owner.getGitUserName());
						warnings.add(warnMsg);
						responseVO.setWarnings(warnings);
					}
				}
			 if(repoName.isEmpty()){
				  repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
  
			 }
			 vo.getProjectDetails().setGitRepoName(repoName);
			 // add records to db
			 CodeServerWorkspaceNsql ownerEntity = workspaceAssembler.toEntity(vo);
			 List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
			 Long ownerwsseqid = jpaRepo.getNextWorkspaceSeqId();
			 String ownerwsid = ConstantsUtility.WORKSPACEPREFIX + String.valueOf(ownerwsseqid);
			 ownerEntity.getData().setWorkspaceId(ownerwsid);
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
			 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
			 ownerWorkbenchCreateInputsDto.setAction(ConstantsUtility.CREATEACTION);
			 String resource = ownerEntity.getData().getProjectDetails().getRecipeDetails().getResource() ;
			 String[] parts = resource.split(",");
			 ownerWorkbenchCreateInputsDto.setStorage_capacity(parts[0]);
			 ownerWorkbenchCreateInputsDto.setMem_guarantee(parts[1]);
			 ownerWorkbenchCreateInputsDto.setMem_limit(parts[3]);
			 double cpuLimit = Double.parseDouble(parts[4].replaceAll("[^0-9.]", ""));
			 double cpuGuarantee = Double.parseDouble(parts[2].replaceAll("[^0-9.]", ""));
			 ownerWorkbenchCreateInputsDto.setCpu_limit(cpuLimit);
			 ownerWorkbenchCreateInputsDto.setCpu_guarantee(cpuGuarantee);
			 ownerWorkbenchCreateInputsDto.setProfile(recipeIdType);
			 if(vo.getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
				ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValue);
			} else {
				ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValueAws);
			}
			 ownerWorkbenchCreateInputsDto.setIsCollaborator("false");
			 ownerWorkbenchCreateInputsDto.setPat(pat);
			 String repoNameWithOrg = "";
			 String pathCheckout = "";
			 if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")
					 && !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
							 .startsWith("private")) {
								 
								 repoNameWithOrg = gitOrgUri + gitOrgName + "/" + repoName;
			 } else {
				 repoNameWithOrg = vo.getProjectDetails().getRecipeDetails().getGitPath();
				 pathCheckout = vo.getProjectDetails().getRecipeDetails().getGitRepoLoc();
				 if(pathCheckout.isEmpty() && repoNameWithOrg.isEmpty()) {
					 pathCheckout = "";
					 repoNameWithOrg = vo.getProjectDetails().getGitRepoName().replace("https://", "");
				 }
				 // repoNameWithOrg = vo.getProjectDetails().getRecipeDetails().getRepodetails();
				 // String url[] = repoNameWithOrg.split(",");
				 // repoNameWithOrg = url[0];
				 // pathCheckout = url[1];
			 }
			 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg.replace("https://", ""));
			 String projectOwnerId = ownerEntity.getData().getWorkspaceOwner().getId();
			 ownerWorkbenchCreateInputsDto.setShortid(projectOwnerId);
			 if(vo.getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				 ownerWorkbenchCreateInputsDto.setType(vo.getProjectDetails().getRecipeDetails().getToDeployType());
			 } else {
				 ownerWorkbenchCreateInputsDto.setType("default");
			 }
			 ownerWorkbenchCreateInputsDto.setWsid(ownerwsid);
			 ownerWorkbenchCreateInputsDto.setResource(vo.getProjectDetails().getRecipeDetails().getResource());
			 ownerWorkbenchCreateInputsDto.setPathCheckout(pathCheckout);
			 List<String> extraContainers = new ArrayList<>();
			 List<String> additionalServices =  vo.getProjectDetails().getRecipeDetails().getAdditionalServices();
			 if (additionalServices != null) {
				 for (String additionalService : additionalServices) {
					 String additionalServiceEnv = additionalServiceRepo.findByServiceName(additionalService);
					 if(!additionalServiceEnv.isEmpty()) {
						 StringBuffer addStringBuffer =  new StringBuffer();
						 addStringBuffer.append(additionalServiceEnv);
						 addStringBuffer.deleteCharAt(0);
						 addStringBuffer.deleteCharAt(addStringBuffer.length()-1);
						 extraContainers.add(addStringBuffer.toString());
					 }
				 }
			 }
			 ownerWorkbenchCreateInputsDto.setExtraContainers(extraContainers);
			 ownerWorkbenchCreateInputsDto.setCloudServiceProvider(vo.getProjectDetails().getRecipeDetails().getCloudServiceProvider().toString());
			 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
			 String codespaceName = vo.getProjectDetails().getProjectName();
			 
			 GenericMessage createOwnerWSResponse = client.doCreateCodeServer(ownerWorkbenchCreateDto,codespaceName);
			 if (createOwnerWSResponse != null) {
				 if (!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) ||
						 (createOwnerWSResponse.getErrors() != null && !createOwnerWSResponse.getErrors().isEmpty()) ||
						 (createOwnerWSResponse.getWarnings() != null
								 && !createOwnerWSResponse.getWarnings().isEmpty())) {
  //					if (!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
  //							.startsWith("public")
  //							&& !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
  //							.startsWith("private")
  //							&& !vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
  //							.startsWith("bat")) {
  //						HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
  //						if (!deleteRepoStatus.is2xxSuccessful()) {
  //							MessageDescription errMsg = new MessageDescription("Created git repository " + repoName
  //									+ " successfully and added collaborator(s). Failed to initialize workbench, Unable to delete repository, please delete repository manually and retry");
  //							errors.add(errMsg);
  //							errors.addAll(createOwnerWSResponse.getErrors());
  //							warnings.addAll(createOwnerWSResponse.getWarnings());
  //							responseVO.setErrors(errors);
  //							responseVO.setWarnings(warnings);
  //							return responseVO;
  //						} else {
  //							MessageDescription errMsg = new MessageDescription("Created git repository " + repoName
  //									+ " successfully and added collaborator(s). Failed to initialize workbench. Deleted repository, please retry creating codespace again");
  //							errors.add(errMsg);
  //							errors.addAll(createOwnerWSResponse.getErrors());
  //							warnings.addAll(createOwnerWSResponse.getWarnings());
  //							responseVO.setErrors(errors);
  //							responseVO.setWarnings(warnings);
  //							return responseVO;
  //						}
  //					}
					 
					 MessageDescription errMsg = new MessageDescription("Failed to initialize workbench. Please retry creating codespace again");
					 errors.add(errMsg);
					 errors.addAll(createOwnerWSResponse.getErrors());
					 warnings.addAll(createOwnerWSResponse.getWarnings());
					 responseVO.setErrors(errors);
					 responseVO.setWarnings(warnings);
					 return responseVO;
				 }
			 }
  
			 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			 Date now = isoFormat.parse(isoFormat.format(new Date()));
			 String projectName = ownerEntity.getData().getProjectDetails().getProjectName();
			 ownerEntity.getData().setIntiatedOn(now);
			 //  ownerEntity.getData().setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
			 ownerEntity.getData().setStatus(ConstantsUtility.CREATEDSTATE);//added
			 String recipeId = vo.getProjectDetails().getRecipeDetails().getRecipeId().toString();
			 String workspaceUrl = this.getWorkspaceUrl(recipeId,ownerwsid,projectOwnerId, vo.getProjectDetails().getRecipeDetails().getCloudServiceProvider().toString());
			 ownerEntity.getData().setWorkspaceUrl(workspaceUrl);
			 ownerEntity.getData().getProjectDetails().setProjectCreatedOn(now);
			 if (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public") ||
					 vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default")) {
				 ownerEntity.getData().getProjectDetails().setProjectCollaborators(new ArrayList<>());
				 collabs = new ArrayList<>();
			 } else {
					collabs = vo.getProjectDetails().getProjectCollaborators();
			 }
			 List<UserInfo> ownerCollab = new ArrayList<>();
			 ownerEntity.getData().getProjectDetails().setProjectCollaborators(new ArrayList<>());
			 if (collabs != null && !collabs.isEmpty()) {
				 for (UserInfoVO collaborator : collabs) {
					if(vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
					.startsWith("private")) {
						List<String> repoDetails = CommonUtils.getRepoNameFromGitUrl(vo.getProjectDetails().getRecipeDetails().getRepodetails());
						String orgName = repoDetails.get(0);
						repoName = repoDetails.get(1);
						HttpStatus status = gitClient.isUserCollaborator(orgName, collaborator.getId(), repoName);
						if(!status.is2xxSuccessful()) {
							log.info("Collaborator {} Addition failed for recipe {}  ",collaborator.getId(),vo.getProjectDetails().getRecipeDetails().getRecipeId());
							errors.add(new MessageDescription("Cannot add User "+collaborator.getId()+" as collaborator because the user is  not a collaborator to the private repo "+repoName+" add the user to the repo and try again"));
							responseVO.setErrors(errors);
							responseVO.setWarnings(new ArrayList<>());
							responseVO.setSuccess("FAILED");
							responseVO.setData(null);
							return responseVO;
						}
						ownerCollab.add(workspaceAssembler.toUserInfo(collaborator));
					}else{
						ownerCollab.add(workspaceAssembler.toUserInfo(collaborator));
					}
					 CodeServerWorkspaceNsql collabEntity = new CodeServerWorkspaceNsql();
					 CodeServerWorkspace collabData = new CodeServerWorkspace();
					 collabData.setDescription(ownerEntity.getData().getDescription());
					 collabData.setGitUserName(collaborator.getGitUserName());
					 collabData.setIntiatedOn(null);
					 collabData.setProjectDetails(ownerEntity.getData().getProjectDetails());
					 collabData.setStatus(ConstantsUtility.COLLABREQUESTEDSTATE);
					 collabData.setActiveInGroup(Boolean.FALSE);
					 Long collabWsSeqId = jpaRepo.getNextWorkspaceSeqId();
					 String collabWsId = ConstantsUtility.WORKSPACEPREFIX + String.valueOf(collabWsSeqId);
					 collabData.setWorkspaceId(collabWsId);
					 UserInfo collabUser = workspaceAssembler.toUserInfo(collaborator);
					 collabData.setWorkspaceOwner(collabUser);
					 collabData.setWorkspaceUrl("");
					 collabEntity.setId(null);
					 collabEntity.setData(collabData);
					 entities.add(collabEntity);
				 }
			 }
			 ownerEntity.getData().getProjectDetails().setProjectCollaborators(ownerCollab);
			 entities.add(ownerEntity);
			 jpaRepo.saveAllAndFlush(entities);
			 CodeServerWorkspaceNsql savedOwnerEntity = workspaceCustomRepository.findbyProjectName(projectOwnerId, projectName);
			 CodeServerWorkspaceVO savedOwnerVO = workspaceAssembler.toVo(savedOwnerEntity);
			 responseVO.setErrors(new ArrayList<>());
			 responseVO.setWarnings(errors);
			 responseVO.setSuccess("SUCCESS");
			 responseVO.setData(savedOwnerVO);
			 return responseVO;
		 } catch (Exception e) {
			 MessageDescription errMsg = new MessageDescription(
					 "Failed with exception. Please delete repository manually if created and retry create workspaces "+e.getMessage());
			 errors.add(errMsg);
			 responseVO.setErrors(errors);
			 return responseVO;
		 }
	 }
  
	 private String getWorkspaceUrl(String recipeId,String wsId, String shortId, String cloudServiceProvider)
	 {
		 String defaultRecipeId = RecipeIdEnum.DEFAULT.toString();
		 String workspaceUrl = null;
		 if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)){
			workspaceUrl = codespaceUrlAWS +"/"+shortId.toLowerCase()+"/"+wsId+"/?folder=/home/coder";
		 } else {
			workspaceUrl = codespaceUrl+"/"+shortId.toLowerCase()+"/"+wsId+"/?folder=/home/coder";
		 }
		 if (!defaultRecipeId.equalsIgnoreCase(recipeId))
			 workspaceUrl += "/app";
		 if (recipeId.toLowerCase().startsWith("public")) {
			 switch (recipeId) {
				 case "public-dna-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/backend";
					 break;
				 case "public-dna-frontend":
					 workspaceUrl = workspaceUrl + "/" + "packages/frontend";
					 break;
				 case "public-dna-report-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/dashboard-backend";
					 break;
				 case "public-dna-codespace-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/code-server";
					 break;
				 case "public-dna-malware-scanner":
					 workspaceUrl = workspaceUrl + "/" + "packages/malware-scanner";
					 break;
				 case "public-dna-storage-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/storage-mfe";
					 break;
				 case "public-dna-storage-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/storage-backend";
					 break;
				 case "public-dna-chronos-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/chronos-mfe";
					 break;
				 case "public-dna-chronos-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/chronos";
					 break;
				 case "public-dna-data-product-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/data-product-mfe";
					 break;
				 case "public-dna-data-product-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/data-product-backend";
					 break;
				 case "public-dna-dss-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/dss-mfe";
					 break;
				 case "public-dna-dataiku-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/dataiku-backend";
					 break;
				 case "public-dna-airflow-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/airflow-backend";
					 break;
				 case "public-dna-modal-registry-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/model-registry";
					 break;
				 case "public-dna-trino-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/trino-backend";
					 break;
				 case "public-dna-nass":
					 workspaceUrl = workspaceUrl + "/" + "packages/naas";
					 break;
				 case "public-dna-authenticator-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/authenticator-service";
					 break;
				 case "public-dna-matomo-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/matomo-mfe";
					 break;
				 case "public-dna-matomo-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/matomo-backend";
					 break;
				 case "public-dna-datalake-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/datalake-mfe";
					 break;
					 case "public-dna-fabric-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/fabric-mfe";
					 break;
				 case "public-dna-dataentry-mfe":
					 workspaceUrl = workspaceUrl + "/" + "packages/dataentry-mfe";
					 break;
				 case "public-dna-fabric-backend":
					 workspaceUrl = workspaceUrl + "/" + "packages/fabric-backend";
					 break;
			 }
		 }
		 return workspaceUrl;
	 }
  
  
	 @Override
	 public CodeServerWorkspaceVO getById(String userId, String id) {
		CodeServerWorkspaceNsql entity = new CodeServerWorkspaceNsql();
		 if(technicalId.equalsIgnoreCase(userId)){
			 entity = workspaceCustomRepository.findByWorkspaceId(id);
			}
		 else{
		  entity = workspaceCustomRepository.findById(userId, id);
		 }
		 return workspaceAssembler.toVo(entity);
	 }
  
	 @Override
	 public CodeSpaceReadmeVo getCodeSpaceReadmeFile(String id) throws Exception {
		String gitUrl = null;
		String repoName = null;
		String readmeFileContent = null;
		String repoOwner = null;
		byte[] file = null;
		CodeSpaceReadmeVo codeSpaceReadmeVo =  new CodeSpaceReadmeVo();
		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(id);
		String gitHubUrl= entity.getData().getProjectDetails().getRecipeDetails().getRepodetails();
		String projectName = entity.getData().getProjectDetails().getProjectName();
		if(gitHubUrl == null || gitHubUrl.isEmpty()) {
			gitHubUrl = "https://" + gitOrgUri + orgName + "/"+projectName+"/";
		}
		if(gitHubUrl.contains(".git")) {
			gitHubUrl = gitHubUrl.replaceAll("\\.git$", "/");
		}
		String[] codespaceSplitValues = gitHubUrl.split("/");
		int length = codespaceSplitValues.length;
		repoName = codespaceSplitValues[length-1];
		repoOwner = codespaceSplitValues[length-2];
		gitUrl = gitHubUrl.replace("/"+repoOwner, "");
		gitUrl = gitUrl.replace("/"+repoName, "");
		JSONObject jsonResponse = gitClient.readFileFromGit(repoName, repoOwner, gitUrl, codespaceFileName);
		if(jsonResponse !=null && jsonResponse.has("name") && jsonResponse.has("content")) {
			readmeFileContent =  jsonResponse.getString("content");
			log.info("Retrieving a software's SHA was successfull from Git.");
			if(readmeFileContent!=null){
				file = readmeFileContent.getBytes();
				codeSpaceReadmeVo.setFile(file);
			}
			
		}
		return codeSpaceReadmeVo;
	 }
 
 
	 @Override
	 public List<CodeServerWorkspaceVO> getAll(String userId, int offset, int limit) {
		 List<CodeServerWorkspaceNsql> entities = workspaceCustomRepository.findAll(userId, limit, offset);
		 entities.forEach(entity -> {
			 CodeServerWorkspaceVO vo = workspaceAssembler.toVo(entity);
			 String serverStatus = getServerStatus(vo); // Update server status
			 if(serverStatus.equalsIgnoreCase("true"))
			 {
				 vo.setServerStatus("SERVER_STARTED");
			 }
			 else
			 {
				 vo.setServerStatus("SERVER_STOPPED");
			 }
		 });
  
		 return entities.stream().map(workspaceAssembler::toVo).collect(Collectors.toList());
	 }
  
	 @Override
	 public Integer getCount(String userId) {
		 return workspaceCustomRepository.getCount(userId);
	 }
  
	 @Override
	 public CodeServerWorkspaceVO getByUniqueliteral(String userId, String uniqueLiteral, String value) {
		 if (value != null) {
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyUniqueLiteral(userId, uniqueLiteral,
					 value);
			 return workspaceAssembler.toVo(entity);
		 } else
			 return null;
	 }

	 @Override
     @Transactional
     public GenericMessage approveRequestWorkspace(String userId, String id, String environment, String branch,
	 		 boolean isprivateRecipe,String version) {
         GenericMessage responseMessage = new GenericMessage();
         String status = "FAILED";
         List<MessageDescription> warnings = new ArrayList<>();
         List<MessageDescription> errors = new ArrayList<>();
         try {
             CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
             if (entity != null) {
				SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
				 Date now = isoFormat.parse(isoFormat.format(new Date()));
                 String projectName = entity.getData().getProjectDetails().getProjectName();
                //  String environmentJsonbName = "intDeploymentDetails";
                //  CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails()
                //       .getIntDeploymentDetails();
                //  if (!"int".equalsIgnoreCase(environment)) {
                //since this is only meant for prod deployments
                String environmentJsonbName = "prod";
                CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
                //  }
                 deploymentDetails.setLastDeploymentStatus("APPROVAL_PENDING");
                 // deploymentDetails.setTechnicalUserDetailsForIAMLogin(technicalUserDetailsForIAMLogin);
				 
				workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName,
							 deploymentDetails,"APPROVAL_PENDING");
				 List<DeploymentAudit> auditLogs = new ArrayList<>();
					CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){						
							auditLogs = optionalBuildDeployentity.getData().getProdDeploymentAuditLogs();
					}
					// DeploymentAudit auditLog = new DeploymentAudit();

                //  List<DeploymentAudit> auditLogs = deploymentDetails.getDeploymentAuditLogs();
                 if (auditLogs == null) {
                     auditLogs = new ArrayList<>();
                 }
                 
                 
                 DeploymentAudit auditLog = new DeploymentAudit();
                 auditLog.setTriggeredOn(now);
                 auditLog.setTriggeredBy(entity.getData().getWorkspaceOwner().getGitUserName());
                 auditLog.setBranch(branch);
                 auditLog.setDeploymentStatus("APPROVAL_PENDING");
				 auditLog.setVersion(version);
                 auditLogs.add(auditLog);

				 CodeServerBuildDeploy buildDeployLogs = null;
				 CodeServerBuildDeployNsql auditLogEntity = null;
				 if(optionalBuildDeployentity != null){
					auditLogEntity = optionalBuildDeployentity;
					buildDeployLogs =  auditLogEntity.getData();						
				 }else{
					 buildDeployLogs = new CodeServerBuildDeploy();
					 auditLogEntity = new CodeServerBuildDeployNsql();
					 String deployLogId = UUID.randomUUID().toString();
					 auditLogEntity.setId(deployLogId);
					 buildDeployLogs.setIntBuildAuditLogs(new ArrayList<>());
					 buildDeployLogs.setProdBuildAuditLogs(new ArrayList<>());	
					 buildDeployLogs.setIntDeploymentAuditLogs(new ArrayList<>());					 	
					 buildDeployLogs.setProjectName(projectName.toLowerCase());	
					 buildDeployLogs.setStatus("CREATED");

				 }
					buildDeployLogs.setProdDeploymentAuditLogs(auditLogs);
				 
				 auditLogEntity.setData(buildDeployLogs);
				 buildDeployRepo.save(auditLogEntity);
                //  deploymentDetails.setDeploymentAuditLogs(auditLogs);
                //  workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName,
                //          deploymentDetails);
						 status = "SUCCESS";
             }
 
         } catch (Exception e) {
             MessageDescription error = new MessageDescription();
             error.setMessage(
                     "Failed while deploying codeserver workspace project with exception " + e.getMessage());
             errors.add(error);
         }
         responseMessage.setErrors(errors);
         responseMessage.setWarnings(warnings);
         responseMessage.setSuccess(status);
         return responseMessage;
     }
 
	 @Override
	 @Transactional
	 	public GenericMessage deployWorkspace(String userId, String id, String environment, String branch,
				 boolean isprivateRecipe,String version,String deployType) {
		 GenericMessage responseMessage = new GenericMessage();
		 String status = "FAILED";
		 List<MessageDescription> warnings = new ArrayList<>();
		 List<MessageDescription> errors = new ArrayList<>();
		 String cloudServiceProvider = null;
		 boolean workspaceMigrated = false;
		 boolean hasProdUrl = false;
		 boolean hasIntUrl = false;
		 try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			 String repoName = null;
			 String repoUrl = null;
			 String gitOrg = null;
		
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			 if (entity != null ) {
				 DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				 DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				 deployJobInputDto.setAction("deploy");
				 deployJobInputDto.setBranch(branch);
				 deployJobInputDto.setEnvironment(codeServerEnvValue);
				 deployJobInputDto.setAppVersion(version);
				 String workspaceOwner = entity.getData().getWorkspaceOwner().getId();
				 String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				 String projectName = entity.getData().getProjectDetails().getProjectName();
				 CodeServerWorkspaceNsql ownerEntity = workspaceCustomRepository.findbyProjectName(projectOwner,
						 projectName);
				 cloudServiceProvider = ownerEntity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider();
				 if(Objects.nonNull(ownerEntity.getData().getIsWorkspaceMigrated())) {
					workspaceMigrated = ownerEntity.getData().getIsWorkspaceMigrated();
				 }
				
				 if (ownerEntity == null || ownerEntity.getData() == null
						 || ownerEntity.getData().getWorkspaceId() == null) {
					 MessageDescription error = new MessageDescription();
					 error.setMessage(
							 "Failed while deploying codeserver workspace project, couldnt fetch project owner details");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
			hasProdUrl = Objects.nonNull(
					ownerEntity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl());
			hasIntUrl = Objects.nonNull(
					ownerEntity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl());
			// if (!workspaceMigrated) {
			// 	if (cloudServiceProvider.equals(ConstantsUtility.DHC_CAAS) && (hasIntUrl || hasProdUrl)) {
			// 		cloudServiceProvider = ConstantsUtility.DHC_CAAS;
			// 	} else {
			// 		cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
			// 	}
			if ((hasProdUrl && ownerEntity.getData().getProjectDetails().getProdDeploymentDetails()
					.getDeploymentUrl().contains(codeServerBaseUriAws)) ||
					(hasIntUrl && ownerEntity.getData().getProjectDetails().getIntDeploymentDetails()
							.getDeploymentUrl().contains(codeServerBaseUriAws))) {
				cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
			} else if (hasProdUrl || hasIntUrl) {
				cloudServiceProvider = ConstantsUtility.DHC_CAAS;
			} else {
				cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
			}
			 if(cloudServiceProvider.equals(ConstantsUtility.DHC_CAAS)){
				deployJobInputDto.setEnvironment(codeServerEnvValue);
			 } else {
				deployJobInputDto.setEnvironment(codeServerEnvValueAws);
			 }	
			 String serviceName = projectName;
			 String workspaceId = entity.getData().getWorkspaceId();

			 
			 CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					 if (!"int".equalsIgnoreCase(environment)) {
						 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					 }
			 String lastBuildOrDeployStatus = "";
			 boolean isApiRecipe = deploymentDetails.getDeploymentType() == ConstantsUtility.UI ? false : true;
			 boolean secureWithIAMRequired = (deploymentDetails.getSecureWithIAMRequired() != null) ? deploymentDetails.getSecureWithIAMRequired() : false;
			 boolean secureWithDnaRequired = (deploymentDetails.getSecureWithDnaRequired() != null) ? deploymentDetails.getSecureWithDnaRequired() : false;
			 boolean isSecuredWithCookie = false;

			 //buildAndDeploy flow
				CodeServerBuildDeployNsql buildDeployEntity = null;
				List<BuildAudit> builds = new ArrayList<>();
				if (version == null || version.isEmpty() || version.isBlank()) {
					String lastBuildType = "buildAndDeploy";
					buildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
					int retainedBuildLimit;
					try {
						retainedBuildLimit = Integer.parseInt(retainedBuildLimitValue.trim());
					} catch (NumberFormatException ex) {
						log.error("Invalid retained build limit value '{}'. Please correct it in Vault.",
								retainedBuildLimitValue);
						throw new IllegalStateException(
								"Invalid retained build limit value: " + retainedBuildLimitValue);
					}

					if (buildDeployEntity != null && buildDeployEntity.getData() != null) {
						CodeServerBuildDeploy buildDeployData = buildDeployEntity.getData();

						builds = "int".equalsIgnoreCase(environment)
								? buildDeployData.getIntBuildAuditLogs()
								: buildDeployData.getProdBuildAuditLogs();

						if (builds == null)
							builds = new ArrayList<>();

						long retainedCount = builds.stream()
								.filter(b -> "BUILD_SUCCESS".equalsIgnoreCase(b.getBuildStatus()))
								.filter(b -> !b.isImageDeleted())
								.count();

						BuildAudit latestBuild = builds.isEmpty() ? null : builds.get(builds.size() - 1);

						boolean applyLimit = latestBuild != null && latestBuild.isKeepBuildImage();

						if (!applyLimit) {
							log.info("KeepBuildImage unchecked, skipping retained build limit check");
						} else {

							if (retainedCount >= retainedBuildLimit) {
								MessageDescription invalidMsg = new MessageDescription();
								invalidMsg.setMessage("Build not allowed: There are already " + retainedCount +
										" successful builds with images retained. Please delete older images before triggering a new build.");
								GenericMessage errorMessage = new GenericMessage();
								errorMessage.addErrors(invalidMsg);

								log.info(
										"User {} attempted buildAndDeploy for project {} but retained image limit ({}) reached.",
										userId, projectName, retainedBuildLimit);

								return errorMessage;
							}
						}
					}
				ManageBuildRequestDto buildRequestDto = new ManageBuildRequestDto();
				buildRequestDto.setBranch(branch);
				buildRequestDto.setEnvironment(environment);
				buildRequestDto.setComments("Build and Deploy");
				log.info("build triggered for workspaceId {} and branch {} and environment {} and lastBuildType {}",workspaceId,branch,environment,lastBuildType);
				responseMessage = this.buildWorkSpace(userId, id, branch, buildRequestDto, isprivateRecipe, environment,lastBuildType);
				if(responseMessage.getSuccess().equalsIgnoreCase("SUCCESS")){
					if(deploymentDetails.getDeploymentUrl() == null || deploymentDetails.getDeploymentUrl().isEmpty()){
						authenticatorClient.callingKongApis(workspaceId, projectName, environment, isApiRecipe, deploymentDetails.getClientId(), "", deploymentDetails.getRedirectUri(), deploymentDetails.getIgnorePaths(), deploymentDetails.getScope(), deploymentDetails.getOneApiVersionShortName(), isSecuredWithCookie, secureWithIAMRequired, deploymentDetails.getSsoType(), secureWithDnaRequired, deploymentDetails.getAliceRoleEnabled(), deploymentDetails.getEntitlementPrefixEnabled(), deploymentDetails.getSelectedAliceRoles(), cloudServiceProvider);
					}
					status = "SUCCESS";
					lastBuildOrDeployStatus = "BUILD_REQUESTED";
				}else{
					status = "FAILED";
		 			return responseMessage;
				}
			}else{
				//deploy flow
				if (isprivateRecipe) {
					repoUrl = entity.getData().getProjectDetails().getRecipeDetails().getRepodetails();
					if(Objects.nonNull(repoUrl) && repoUrl.contains(".git")){
						repoUrl = repoUrl.replaceAll("\\.git$", "/");
					} else {
						repoUrl.concat("/");
					}
					List<String> repoDetails = CommonUtils.getDetailsFromUrl(repoUrl);
					if (repoDetails.size() > 0 && repoDetails != null) {
						repoName = repoDetails.get(2);
						gitOrg = repoDetails.get(1);
					}
					deployJobInputDto.setRepo(gitOrg + "/" + repoName);		
				} else {
					repoName = entity.getData().getProjectDetails().getGitRepoName();
					deployJobInputDto.setRepo(gitOrgName + "/" + repoName);		
 
				}
				 
				 deployJobInputDto.setShortid(workspaceOwner);
				 deployJobInputDto.setTarget_env(environment);
 
				Boolean isValutInjectorEnable = false;
				 try{
					isValutInjectorEnable = VaultClient.enableVaultInjector(projectName.toLowerCase(), environment);
				 }catch(Exception e){
					MessageDescription error = new MessageDescription();
					error.setMessage("Some error occured during deployment, with exception " + e.getMessage());
					errors.add(error);
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseMessage.setSuccess(status);
					return responseMessage;
				 }
				 String workspaceOwnerWsId = entity.getData().getWorkspaceId();
				 //String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				 deployJobInputDto.setWsid(workspaceOwnerWsId);
				 deployJobInputDto.setProjectName(projectName.toLowerCase());
				 deployJobInputDto.setValutInjectorEnable(isValutInjectorEnable);
				 deploymentJobDto.setInputs(deployJobInputDto);
				 deploymentJobDto.setRef(codeServerEnvRef);
				 GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				 if (jobResponse != null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					 
					
					 List<DeploymentAudit> auditLogs = new ArrayList<>();
					CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){
						if("int".equalsIgnoreCase(environment)){
							auditLogs = optionalBuildDeployentity.getData().getIntDeploymentAuditLogs();
						}else{
							auditLogs = optionalBuildDeployentity.getData().getProdDeploymentAuditLogs();
						}
					}
					 if(null == auditLogs){
						auditLogs = new ArrayList<>();
					}
					DeploymentAudit auditLog = new DeploymentAudit();
					 if("APPROVAL_PENDING".equalsIgnoreCase(deploymentDetails.getLastDeploymentStatus())){						
						if (!auditLogs.isEmpty()){
							auditLog = auditLogs.get(auditLogs.size() - 1);
						}						
						auditLog.setApprovedBy(entity.getData().getWorkspaceOwner().getGitUserName());																
					 }
					 else{
												
						auditLog.setTriggeredOn(now);
						auditLog.setTriggeredBy(entity.getData().getWorkspaceOwner().getGitUserName());
						auditLog.setBranch(branch);					
					 }

					 GitLatestCommitIdDto commitId =null;
						if(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
						.startsWith("private")){
							List<String> repoDetails = CommonUtils.getRepoNameFromGitUrl(entity.getData().getProjectDetails().getGitRepoName());
							commitId = gitClient.getLatestCommitId(repoDetails.get(0),branch,repoDetails.get(1));
						}else{
							commitId = gitClient.getLatestCommitId(gitOrgName,branch,entity.getData().getProjectDetails().getGitRepoName());
							
						}
						if(commitId == null){
							MessageDescription warning = new MessageDescription();
							warning.setMessage("Error while adding commit id to deployment audit log");
						}
						auditLog.setCommitId(commitId.getSha());
						auditLog.setDeploymentStatus("DEPLOY_REQUESTED");
						auditLog.setVersion(version);
						if("APPROVAL_PENDING".equalsIgnoreCase(deploymentDetails.getLastDeploymentStatus())){
							if (!auditLogs.isEmpty()){
								auditLogs.set(auditLogs.size() - 1, auditLog);
							}
							else{
								auditLogs.add(auditLog);
							}
						}else{
							auditLogs.add(auditLog);
						}

					 CodeServerBuildDeploy buildDeployLogs = null;
					 CodeServerBuildDeployNsql auditLogEntity = null;
					 if(optionalBuildDeployentity != null){
						auditLogEntity = optionalBuildDeployentity;
						buildDeployLogs =  auditLogEntity.getData();						
					 }else{
						 buildDeployLogs = new CodeServerBuildDeploy();
						 auditLogEntity = new CodeServerBuildDeployNsql();
						 String deployLogId = UUID.randomUUID().toString();
						 auditLogEntity.setId(deployLogId);
						 buildDeployLogs.setIntBuildAuditLogs(new ArrayList<>());
						 buildDeployLogs.setProdBuildAuditLogs(new ArrayList<>());							 	
						 buildDeployLogs.setProjectName(projectName.toLowerCase());	
					 	 buildDeployLogs.setStatus("CREATED");				
					 }
					 if("int".equalsIgnoreCase(environment)){
						buildDeployLogs.setIntDeploymentAuditLogs(auditLogs);
					 }else{
						buildDeployLogs.setProdDeploymentAuditLogs(auditLogs);
					 }
					 auditLogEntity.setData(buildDeployLogs);
					 buildDeployRepo.save(auditLogEntity);

					 if(deployType.equalsIgnoreCase("deploy") && (deploymentDetails.getDeploymentUrl() == null || deploymentDetails.getDeploymentUrl().isEmpty())){
						 authenticatorClient.callingKongApis(workspaceId, projectName, environment, isApiRecipe, deploymentDetails.getClientId(), "", deploymentDetails.getRedirectUri(), deploymentDetails.getIgnorePaths(), deploymentDetails.getScope(), deploymentDetails.getOneApiVersionShortName(), isSecuredWithCookie, secureWithIAMRequired, deploymentDetails.getSsoType(), secureWithDnaRequired, deploymentDetails.getAliceRoleEnabled(), deploymentDetails.getEntitlementPrefixEnabled(), deploymentDetails.getSelectedAliceRoles(), cloudServiceProvider);
					 }
					
					// deploymentDetails.setLastDeployedBranch(branch);
					// deploymentDetails.setLastDeployedVersion(version);	
					lastBuildOrDeployStatus = "DEPLOY_REQUESTED";				
					deploymentDetails.setLastDeploymentStatus("DEPLOY_REQUESTED");
					
					status = "SUCCESS";
				 } else {
					 status = "FAILED";
					 errors.addAll(jobResponse.getErrors());
				 }
			 }
			 log.info("project name {} updating deployment details to db",serviceName);	
			 workspaceCustomRepository.updateDeploymentDetails(projectName, environment,deploymentDetails,lastBuildOrDeployStatus);
			 }
		 } catch (Exception e) {
			log.error("Failed while deploying codeserver workspace project with exception : {} ", e.getMessage());
			 MessageDescription error = new MessageDescription();
			 error.setMessage("Failed while deploying codeserver workspace project with exception " + e.getMessage());
			 errors.add(error);
		 }
		 responseMessage.setErrors(errors);
		 responseMessage.setWarnings(warnings);
		 responseMessage.setSuccess(status);
		 return responseMessage;
	 }
 
	 @Override
	 @Transactional
	 public GenericMessage deployedAppConfig(String userId, String id, String environment, DeployedAppConfigDto deployedAppConfigDto){
		 GenericMessage responseMessage = new GenericMessage();
		 String status = "FAILED";
         List<MessageDescription> warnings = new ArrayList<>();
         List<MessageDescription> errors = new ArrayList<>();
		 String cloudServiceProvider = null;
		 String environmentJsonbName = null;
		 CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
		 try{
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			 if (entity != null) {
				 String projectName = entity.getData().getProjectDetails().getProjectName();
			 	 String workspaceId = entity.getData().getWorkspaceId();
				 if("int".equalsIgnoreCase(environment)){
					 environmentJsonbName = "intDeploymentDetails";
					 deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
				 } else{
					 environmentJsonbName = "prodDeploymentDetails";
					 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
				 }
				 if(Objects.nonNull(deploymentDetails) && Objects.nonNull(deploymentDetails.getDeploymentUrl())){
					 if(deploymentDetails.getDeploymentUrl().contains(codeServerBaseUriAws)){
					 cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
				 	 } else{
						 cloudServiceProvider = ConstantsUtility.DHC_CAAS;
				 	 }
				 } else{
					 MessageDescription error = new MessageDescription();
					 error.setMessage(
							 "Failed while updating deployed app config for workspace project, no deployemnts found.");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }

				 boolean isApiRecipe = deployedAppConfigDto.isIsApiRecipe();
                 boolean oneApiVersionShortNameNotBlank = !deployedAppConfigDto.getOneApiVersionShortName().isBlank();
                 boolean secureWithIAMRequired = deployedAppConfigDto.isSecureWithIAMRequired();
                 boolean secureWithDnaRequired = deployedAppConfigDto.isSecureWithDnaRequired();

                 if ((!isApiRecipe && oneApiVersionShortNameNotBlank) ||
                		 (secureWithIAMRequired && (oneApiVersionShortNameNotBlank || secureWithDnaRequired)) ||
    					 (secureWithDnaRequired && (secureWithIAMRequired || oneApiVersionShortNameNotBlank)) ||
                         (oneApiVersionShortNameNotBlank && (secureWithIAMRequired || secureWithDnaRequired))) {
				     MessageDescription error = new MessageDescription();
				     error.setMessage("Failed while deploying codeserver workspace project, couldn't deploy for this combination. BAD REQUEST. ");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 responseMessage.setWarnings(warnings);
					 responseMessage.setSuccess(status);
					 return responseMessage;
                 }

				 String clientSecret = null;
				 if(secureWithDnaRequired){
					clientSecret = dnaClientSecret;
				 } else{
					clientSecret = deployedAppConfigDto.getClientSecret();
				 }
				 boolean isSecuredWithCookie = false; //disable for now 
				 String deploymentType = deployedAppConfigDto.isIsApiRecipe() ? ConstantsUtility.API : ConstantsUtility.UI;
				 authenticatorClient.callingKongApis(workspaceId, projectName, environment, deployedAppConfigDto.isIsApiRecipe(), deployedAppConfigDto.getClientID(), clientSecret, deployedAppConfigDto.getRedirectUri(), deployedAppConfigDto.getIgnorePaths(), deployedAppConfigDto.getScope(), deployedAppConfigDto.getOneApiVersionShortName(), isSecuredWithCookie, secureWithIAMRequired, deployedAppConfigDto.getSsoType().toString(), secureWithDnaRequired, deployedAppConfigDto.isAliceRoleEnabled(), deployedAppConfigDto.isEntitlementPrefixEnabled(), deployedAppConfigDto.getSelectedAliceRoles(), cloudServiceProvider);
				 workspaceCustomRepository.updateDeployedAppConfig(projectName, environmentJsonbName,
						 secureWithIAMRequired, deployedAppConfigDto.getOneApiVersionShortName(),
						 isSecuredWithCookie, deploymentType, deployedAppConfigDto.getClientID(),
						 deployedAppConfigDto.getRedirectUri(), deployedAppConfigDto.getIgnorePaths(),
						 deployedAppConfigDto.getScope(), deployedAppConfigDto.getSsoType().toString(),
						 secureWithDnaRequired, deployedAppConfigDto.isAliceRoleEnabled(),
						 deployedAppConfigDto.isEntitlementPrefixEnabled(),
						 deployedAppConfigDto.getSelectedAliceRoles());
					 status = "SUCCESS";
			 }
			 
		 } catch (Exception e) {
			 MessageDescription error = new MessageDescription();
			 error.setMessage("Failed while updating deployed app config of workspace project with exception " + e.getMessage());
			 errors.add(error);
		 }
		 responseMessage.setErrors(errors);
		 responseMessage.setWarnings(warnings);
		 responseMessage.setSuccess(status);
		 return responseMessage;
	 }

	 @Override
	 @Transactional
	 public WorkspacePluginStatusVO pluginStatus(String userId, String id, String environment, String pluginName){
		 CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
		 WorkspacePluginStatusVO workspacePluginStatusVo = new WorkspacePluginStatusVO();
		 String cloudServiceProvider = null;
		 try{
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
		     if (entity != null) {
			 	 String projectName = entity.getData().getProjectDetails().getProjectName();
			 	 String workspaceId = entity.getData().getWorkspaceId();
			 	 if("int".equalsIgnoreCase(environment)){
			     	 deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
			     } else{
				  	 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
			     }
			     if(Objects.nonNull(deploymentDetails) && Objects.nonNull(deploymentDetails.getDeploymentUrl())){
			         if(deploymentDetails.getDeploymentUrl().contains(codeServerBaseUriAws)){
				    	 cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
				     } else{
				 		 cloudServiceProvider = ConstantsUtility.DHC_CAAS;
				     }
			     } else{
					log.info("No deployment details found for workspace {}",id);
				 }
				 workspacePluginStatusVo = authenticatorClient.getPluginStatus(projectName.toLowerCase()+"-"+environment, pluginName, cloudServiceProvider);
		     } else{
				log.info("No workspace details found for workpspace {}",id);
			 }
		 } catch (Exception e) {
			 log.info("Error while fetching {} plugin status for workspace {} with exception {}.",pluginName,id,e.getMessage());
		 }
		 
		 return workspacePluginStatusVo;
	 }

	 @Override
	 @Transactional
	 public GenericMessage togglePlugin(String userId, String id, String environment, String pluginName, boolean enable){
		 GenericMessage responseMessage = new GenericMessage();
		 String status = "FAILED";
         List<MessageDescription> warnings = new ArrayList<>();
         List<MessageDescription> errors = new ArrayList<>();
		 CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
		 String configPublishedAppId = null;
		 try{
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			 if (entity != null) {
				 String projectName = entity.getData().getProjectDetails().getProjectName();
				 if("int".equalsIgnoreCase(environment)){
					 deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					 configPublishedAppId = entity.getData().getProjectDetails().getSecurityConfig().getStaging().getPublished().getAppID();
				 }
				 else{
					 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					 configPublishedAppId = entity.getData().getProjectDetails().getSecurityConfig().getProduction().getPublished().getAppID();
				 }
				 if("oidc".equalsIgnoreCase(pluginName) && !Boolean.TRUE.equals(deploymentDetails.getSecureWithIAMRequired() || deploymentDetails.getSecureWithDnaRequired())){
					 MessageDescription error = new MessageDescription();
					 error.setMessage("No OIDC plugin found. Please secure with IAM first.");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 responseMessage.setWarnings(warnings);
					 responseMessage.setSuccess(status);
					 return responseMessage;
				 }
				 if("apiauthoriser".equalsIgnoreCase(pluginName) && Objects.isNull(configPublishedAppId)){
					 MessageDescription error = new MessageDescription();
					 error.setMessage("No AUTHORISER plugin found. Please publish your authorization config.");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 responseMessage.setWarnings(warnings);
					 responseMessage.setSuccess(status);
					 return responseMessage;
				 }
				 if("oidc".equalsIgnoreCase(pluginName)){
					if(!enable && Objects.nonNull(configPublishedAppId)){
						authenticatorClient.changePluginStatus(projectName.toLowerCase()+"-"+environment, "oidc", enable);
						authenticatorClient.changePluginStatus(projectName.toLowerCase()+"-"+environment, "apiauthoriser", enable);
				 	} else {
						authenticatorClient.changePluginStatus(projectName.toLowerCase()+"-"+environment, "oidc", enable);
					}
				 }
				 if("apiauthoriser".equalsIgnoreCase(pluginName)){
					 authenticatorClient.changePluginStatus(projectName.toLowerCase()+"-"+environment, "apiauthoriser", enable);
				 }
				 status = "SUCCESS";
			 }
		 } catch (Exception e) {
			 MessageDescription error = new MessageDescription();
			 error.setMessage("Failed while updating plugins of workspace project with exception " + e.getMessage());
			 errors.add(error);
		 }
		 responseMessage.setErrors(errors);
		 responseMessage.setWarnings(warnings);
		 responseMessage.setSuccess(status);
		 return responseMessage;
	 }
	 
	 @Override
	 @Transactional
	public GenericMessage reassignOwner(CreatedByVO currentUser, CodeServerWorkspaceVO vo,
			UserInfoVO newOwnerDeatils) {
		GenericMessage responseVO = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(currentUser.getId(), vo.getId());
		boolean isProjectOwner = false;
		String projectName = entity.getData().getProjectDetails().getProjectName();
 
		String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		if (projectOwnerId.equalsIgnoreCase(currentUser.getId())) {
			isProjectOwner = true;
		}
 
		try {
 
			if (entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
					.startsWith("private")) {
				List<String> repoDetails = CommonUtils
						.getRepoNameFromGitUrl(vo.getProjectDetails().getRecipeDetails().getRepodetails());
				Boolean isUserAdmin = gitClient.isUserAdmin(repoDetails.get(0), newOwnerDeatils.getId(),
						repoDetails.get(1));
				if (!isUserAdmin) {
					log.error("collab user is not an admin for the private repo, cannot transfer ownership");
					MessageDescription msg = new MessageDescription(
							"Collab user not an admin of the repo, cannot transfer ownership.");
					errors.add(msg);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					return responseMessage;
				}
			}
 
			UserInfo currentOwnerAsCollab = entity.getData().getProjectDetails().getProjectOwner();
			UserInfo newOwner = new UserInfo();
			BeanUtils.copyProperties(newOwnerDeatils, newOwner);
 
			// To update project owner.
			GenericMessage updateProjectOwnerDetails = workspaceCustomRepository
					.updateProjectOwnerDetails(projectName, newOwner);
			String repoName = vo.getProjectDetails().getGitRepoName();
			if (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")
					|| vo
							.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase()
							.startsWith("private")) {
				repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
			}
			// adding new owner as repo admin
			if (!entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
					.startsWith("private")) {
				HttpStatus addAdminAccessToGitUser = gitClient.addAdminAccessToRepo(newOwner.getGitUserName(),
						repoName);
				if (!addAdminAccessToGitUser.is2xxSuccessful()) {
					MessageDescription warnMsg = new MessageDescription(
							"Failed while adding " + newOwner.getGitUserName()
									+ " as admin to repository");
					log.info("Failed while adding {} as admin to repository. Please add manually",
							newOwner.getGitUserName());
					warnings.add(warnMsg);
					responseVO.setWarnings(warnings);
				}
			}
			// To add current owner as collaborator.
			GenericMessage updateCollaboratorAsOwner = workspaceCustomRepository
					.updateCollaboratorDetails(projectName, currentOwnerAsCollab, false);
 
			// To remove new owner from collaborator.
			GenericMessage removeNewOwnerFromCollab = workspaceCustomRepository
					.updateCollaboratorDetails(projectName, newOwner, true);
			// removing old user as repo admin
			if (!entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
					.startsWith("private")) {
				HttpStatus removeAdminAccessToGitUser = gitClient.removeAdminAccessFromRepo(projectOwnerId, repoName);
				if (!removeAdminAccessToGitUser.is2xxSuccessful()) {
					MessageDescription warnMsg = new MessageDescription("Failed while adding " + projectOwnerId
							+ " as admin to repository");
					log.info("Failed while removing {} as admin to repository. Please add manually",
							projectOwnerId);
					warnings.add(warnMsg);
					responseVO.setWarnings(warnings);
				}
			}
 
			if ("FAILED".equalsIgnoreCase(updateProjectOwnerDetails.getSuccess())
					|| "FAILED".equalsIgnoreCase(updateCollaboratorAsOwner.getSuccess())
					|| "FAILED".equalsIgnoreCase(removeNewOwnerFromCollab.getSuccess())) {
				// TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				log.error("Failed to update project owner details");
				MessageDescription msg = new MessageDescription("Failed to update project owner details");
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}
			//Notifying transfered user  
			String eventType = "Codespace-Transfer Ownership Status Update";
			String resourceID = vo.getProjectDetails().getProjectName();
			List <String> transferedUserId = new ArrayList<>();
			List <String> transferedUserEmail = new ArrayList<>();
			transferedUserId.add(newOwnerDeatils.getId());
			transferedUserEmail.add(newOwnerDeatils.getEmail());
			String message = "Ownership of the Codespace \"" + vo.getProjectDetails().getProjectName() + "\" has been transferred to you by user " + currentUser.getId() + ".";
			kafkaProducer.send(eventType, resourceID, "", currentUser.getId(), message, true, transferedUserId, transferedUserEmail, null);
			
			responseMessage.setSuccess("SUCCESS");
		} catch (Exception e) {
			log.error("Failed to add collaborator details as requested with Exception: {} ", e.getMessage());
			MessageDescription msg = new MessageDescription("Failed to add collaborator details");
			errors.add(msg);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			return responseMessage;
		}
 
		return responseMessage;
	}
 
	 @Override
	 public Integer getTotalCountOfWorkSpace() {
		 return workspaceCustomRepository.getTotalCountOfWorkSpace();
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage removeCollabById(String currentUserUserId, CodeServerWorkspaceVO vo, String removeUserId) {
		 GenericMessage responseVO = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 GenericMessage responseMessage = new GenericMessage();
		 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(currentUserUserId, vo.getId());
		 boolean isProjectOwner = false;
		 boolean isAdmin = false;
		 String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		 if (projectOwnerId.equalsIgnoreCase(currentUserUserId)) {
			 isProjectOwner = true;
		 }
		 List<UserInfo>collabList =entity.getData().getProjectDetails().getProjectCollaborators();
		 if(collabList!=null){
			 for(UserInfo user : collabList){
				 if(currentUserUserId.equalsIgnoreCase(user.getId())){
					 if(user.getIsAdmin()){
						 isAdmin =true;
					 }
				 }
			 }
		 }
  
		 if (isProjectOwner || isAdmin) {
			 String projectName = entity.getData().getProjectDetails().getProjectName();
			 String technincalId = workspaceCustomRepository.getWorkspaceTechnicalId(removeUserId, projectName);
			 if (technincalId=="" || technincalId.isEmpty() || technincalId == null) {
				 log.error("No collaborator details found.");
				 MessageDescription msg = new MessageDescription("No collaborator details found.");
				 errors.add(msg);
				 responseMessage.setErrors(errors);
				 return responseMessage;
			 }
			 responseMessage = deleteById(removeUserId, technincalId);
		 } else {
			 log.error("Failed to remove collaborator details as requested user is not a project owner "
					 + entity.getData().getWorkspaceId());
			 MessageDescription msg = new MessageDescription(
					 "Failed to remove collaborator details as requested user is not a project owner");
			 errors.add(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
  
		 return responseMessage;
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage addCollabById(String userId, CodeServerWorkspaceVO vo, UserInfoVO userRequestDto) {
		 GenericMessage responseVO = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 GenericMessage responseMessage = new GenericMessage();
		 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, vo.getId());
		 boolean isProjectOwner = false;
		 boolean isAdmin = false;
		 if (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public") 
				 || vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default")) {
			 log.error("Cannot add collaborator for this project {} of recipe type - {} "
					 + entity.getData().getWorkspaceId(), vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase());
			 MessageDescription msg = new MessageDescription(
					 "Cannot add collaborator for projects of recipe type -" + vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase() + " .");
			 errors.add(msg);
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
		 String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		 if (projectOwnerId.equalsIgnoreCase(userId)) {
			 isProjectOwner = true;
		 }
		 
		 List<UserInfo>collabList =entity.getData().getProjectDetails().getProjectCollaborators();
		 if(collabList!=null){
			 for(UserInfo user : collabList){
				 if(userId.equalsIgnoreCase(user.getId())){
					 if(user.getIsAdmin()){
						 isAdmin =true;
					 }
				 }
			 }
		 }
  
		 if (isProjectOwner || isAdmin) {
			 try {
				 String repoName = entity.getData().getProjectDetails().getGitRepoName();
				 String projectName = entity.getData().getProjectDetails().getProjectName();
  
				 UserInfo collaborator = new UserInfo();
				 BeanUtils.copyProperties(userRequestDto, collaborator);
				 if(!userRequestDto.isIsAdmin()){
					 collaborator.setIsAdmin(false);
				 }else{
					 collaborator.setIsAdmin(true);
				 }
				 if(!userRequestDto.isIsApprover()){
					collaborator.setIsApprover(false);
				}else{
					collaborator.setIsApprover(true);
				}
 
				 String gitUser = userRequestDto.getGitUserName();
 
				 if(vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private") ){
					String gitUrl = vo.getProjectDetails().getRecipeDetails().getRepodetails();
					List<String> repoDetails = CommonUtils.getRepoNameFromGitUrl(vo.getProjectDetails().getRecipeDetails().getRepodetails());
					String repoOwner = repoDetails.get(0);
					repoName = repoDetails.get(1);
					HttpStatus status = gitClient.isUserCollaborator(repoOwner,gitUser, repoName);
					if(!status.is2xxSuccessful()){
						log.info("Cannot add User {} as collaborator because the user is  not a collaborator to the private repo {}",userRequestDto.getGitUserName(),repoName);
						MessageDescription msg = new MessageDescription("Cannot add User "+userRequestDto.getGitUserName()+" as collaborator because the user is  not a collaborator to the private repo "+repoName+" add the user to the repo and try again");
						errors.add(msg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors); 
						return responseMessage;
					}
				}
 
				 if(! (vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")
						 || vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private") 
						 || vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().equalsIgnoreCase("default"))) {
					 HttpStatus addGitUser = gitClient.addUserToRepo(gitUser, repoName);
					if(addGitUser == HttpStatus.UNPROCESSABLE_ENTITY){
						log.info("Failed while adding {} as collaborator with status {}", repoName,
								userRequestDto.getGitUserName(), addGitUser.name());
						MessageDescription errMsg = new MessageDescription(
								"Failed while adding " + userRequestDto.getGitUserName() + " as collaborator, Because"
										+ " the Git user account Suspended, please ask the user to Login again and add this user manually in the git repo.");
						errors.add(errMsg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors); 
						return responseMessage;
					}
					 if (!addGitUser.is2xxSuccessful()) {
						 log.info("Failed while adding {} as collaborator with status {}", repoName,
								 userRequestDto.getGitUserName(), addGitUser.name());
						 MessageDescription errMsg = new MessageDescription(
								 "Failed while adding " + userRequestDto.getGitUserName() + " as collaborator . Please make "
										 + userRequestDto.getGitUserName()
										 + " is valid git user and add this user manually in the git repo.");
						 warnings.add(errMsg);
					 }
				 }
				 HttpStatus addAdminAccessToGitUser = gitClient.addAdminAccessToRepo(projectOwnerId, repoName);
				 if(!addAdminAccessToGitUser.is2xxSuccessful())
				 {
					 MessageDescription warnMsg = new MessageDescription("Failed while adding " +projectOwnerId
					 + " as admin to repository");
					 log.info("Failed while adding {} as collaborator to repository. Please add manually",
					 projectOwnerId);
					 warnings.add(warnMsg);
					 responseVO.setWarnings(warnings);
				 }
					 responseMessage.setWarnings(warnings);
					 CodeServerWorkspaceNsql collabEntity = new CodeServerWorkspaceNsql();
					 CodeServerWorkspace collabData = new CodeServerWorkspace();
					 collabData.setDescription(entity.getData().getDescription());
					 collabData.setGitUserName(collaborator.getGitUserName());
					 collabData.setIntiatedOn(null);
					 collabData.setProjectDetails(entity.getData().getProjectDetails());
					 collabData.setStatus(ConstantsUtility.COLLABREQUESTEDSTATE);
					 collabData.setActiveInGroup(Boolean.FALSE);
					 Long collabWsSeqId = jpaRepo.getNextWorkspaceSeqId();
					 String collabWsId = ConstantsUtility.WORKSPACEPREFIX + String.valueOf(collabWsSeqId);
					 collabData.setWorkspaceId(collabWsId);
					 UserInfo collabUser = workspaceAssembler.toUserInfo(userRequestDto);
					 collabData.setWorkspaceOwner(collabUser);
					 collabData.setWorkspaceUrl("");
					 collabEntity.setId(null);
					 collabEntity.setData(collabData);
  
					 jpaRepo.save(collabEntity);
					 workspaceCustomRepository.updateCollaboratorDetails(projectName, collaborator, false);
					 responseMessage.setSuccess("SUCCESS");
				 
			 } catch (Exception e) {
				 log.error("Failed to add collaborator details as requested with Exception: {} ", e.getMessage());
				 MessageDescription msg = new MessageDescription("Failed to add collaborator details");
				 errors.add(msg);
				 responseMessage.setSuccess("FAILED");
				 responseMessage.setErrors(errors);
				 return responseMessage;
			 }
		 } else {
			 log.error("Failed to add collaborator details as requested user is not a project owner "
					 + entity.getData().getWorkspaceId());
			 MessageDescription msg = new MessageDescription(
					 "Failed to add collaborator details as requested user is not a project owner");
			 errors.add(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
  
		 return responseMessage;
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage undeployWorkspace(String userId, String id, String environment, String branch) {
		 GenericMessage responseMessage = new GenericMessage();
		 String status = "FAILED";
		 List<MessageDescription> warnings = new ArrayList<>();
		 List<MessageDescription> errors = new ArrayList<>();
		 try {
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			 if (entity != null) {
				 DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				 DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				 deployJobInputDto.setAction("undeploy");
				 deployJobInputDto.setBranch(branch);
				 if(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
					deployJobInputDto.setEnvironment(codeServerEnvValue);
				} else {
					deployJobInputDto.setEnvironment(codeServerEnvValueAws);
				}
				 deployJobInputDto.setRepo(gitOrgName + "/" + entity.getData().getProjectDetails().getGitRepoName());
				 String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				 deployJobInputDto.setShortid(projectOwner);
				 deployJobInputDto.setTarget_env(environment);
				//  if(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType()!=null){
				// 	 deployJobInputDto.setType(entity.getData().getProjectDetails().getRecipeDetails().getToDeployType());
				//  } else {
				// 	 deployJobInputDto.setType("default");
				//  }
				 String projectName = entity.getData().getProjectDetails().getProjectName();
				 CodeServerWorkspaceNsql ownerEntity = workspaceCustomRepository.findbyProjectName(projectOwner,
						 projectName);
				 if (ownerEntity == null || ownerEntity.getData() == null
						 || ownerEntity.getData().getWorkspaceId() == null) {
					 MessageDescription error = new MessageDescription();
					 error.setMessage(
							 "Failed while deploying codeserver workspace project, couldnt fetch project owner details");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
				 String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
  //				deployJobInputDto.setWsid(projectOwnerWsId);
				 deployJobInputDto.setWsid(projectName);
				 deployJobInputDto.setProjectName(projectName);
				 deploymentJobDto.setInputs(deployJobInputDto);
				 deploymentJobDto.setRef(codeServerEnvRef);
				 GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				 if (jobResponse != null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					 String environmentJsonbName = "intDeploymentDetails";
					 CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails()
							 .getIntDeploymentDetails();
					 if (!"int".equalsIgnoreCase(environment)) {
						 environmentJsonbName = "prodDeploymentDetails";
						 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					 }
					 deploymentDetails.setLastDeploymentStatus("UNDEPLOY_REQUESTED");
					workspaceCustomRepository.updateDeploymentDetails(projectName, environment,
							 deploymentDetails,deploymentDetails.getLastDeploymentStatus());

					 List<DeploymentAudit> auditLogs = new ArrayList<>();
					CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){
						if("int".equalsIgnoreCase(environment)){
							auditLogs = optionalBuildDeployentity.getData().getIntDeploymentAuditLogs();
						}else{
							auditLogs = optionalBuildDeployentity.getData().getProdDeploymentAuditLogs();
						}
					}
					 if (auditLogs == null) {
						 auditLogs = new ArrayList<>();
					 }
					 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
					 Date now = isoFormat.parse(isoFormat.format(new Date()));
					 DeploymentAudit auditLog = new DeploymentAudit();
					 auditLog.setTriggeredOn(now);
					 auditLog.setTriggeredBy(entity.getData().getWorkspaceOwner().getGitUserName());
					 auditLog.setBranch(branch);					
					 auditLog.setDeploymentStatus("UNDEPLOY_REQUESTED");
					 auditLogs.add(auditLog);
					 
					 CodeServerBuildDeploy buildDeployLogs = null;
					 CodeServerBuildDeployNsql auditLogEntity = null;
					 if(optionalBuildDeployentity != null){
						auditLogEntity = optionalBuildDeployentity;
						buildDeployLogs =  auditLogEntity.getData();						
					 }else{
						 buildDeployLogs = new CodeServerBuildDeploy();
						 auditLogEntity = new CodeServerBuildDeployNsql();
						 String deployLogId = UUID.randomUUID().toString();
						 auditLogEntity.setId(deployLogId);
						 buildDeployLogs.setIntBuildAuditLogs(new ArrayList<>());
						 buildDeployLogs.setProdBuildAuditLogs(new ArrayList<>());							 							 
						 buildDeployLogs.setProjectName(projectName.toLowerCase());	
					 	 buildDeployLogs.setStatus("CREATED");				
					 }
					 if("int".equalsIgnoreCase(environment)){
						buildDeployLogs.setIntDeploymentAuditLogs(auditLogs);
					 }else{
						buildDeployLogs.setProdDeploymentAuditLogs(auditLogs);
					 }
					 auditLogEntity.setData(buildDeployLogs);
					 buildDeployRepo.save(auditLogEntity);
					 status = "SUCCESS";
				 } else {
					 status = "FAILED";
					 errors.addAll(jobResponse.getErrors());
				 }
			 }
		 } catch (Exception e) {
			log.error("Failed while deploying codeserver workspace project with exception : {} ", e.getMessage());
			 MessageDescription error = new MessageDescription();
			 error.setMessage("Failed while deploying codeserver workspace project with exception " + e.getMessage());
			 errors.add(error);
		 }
		 responseMessage.setErrors(errors);
		 responseMessage.setWarnings(warnings);
		 responseMessage.setSuccess(status);
		 return responseMessage;
	 }
  
	 @Override
	 public CodeServerWorkspaceVO getByProjectName(String userId, String projectName) {
		 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyProjectName(userId, projectName);
		 return workspaceAssembler.toVo(entity);
	 }
	 
	 @Override
	 public CodeServerWorkspaceVO getByProjectName(String projectName) {
		 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyProjectName(projectName);
		 return workspaceAssembler.toVo(entity);
	 }
	 
	 
  
	 @Override
	 @Transactional
	 public GenericMessage update(String userId, String wsId, String projectName, String existingStatus,
			 String latestStatus, String targetEnv, String branch, String gitJobRunId,String version) {
		 GenericMessage responseMessage = new GenericMessage();
		 String status = "FAILED";
		 List<MessageDescription> warnings = new ArrayList<>();
		 List<MessageDescription> errors = new ArrayList<>();
		 String cloudServiceProvider = null;
		 boolean hasProdUrl = false;
		 boolean hasIntUrl = false;
		 try {
			 String[] createDeleteStatuses = { "CREATED", "CREATE_FAILED", "DELETED", "DELETE_REQUESTED" };
			 boolean isCreateDeleteStatuses = Arrays.stream(createDeleteStatuses).anyMatch(latestStatus::equals);
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(wsId);
			 String workspaceOwner = entity.getData().getWorkspaceOwner().getId();
			 String workspaceName = entity.getData().getWorkspaceId();
			 String defaultRecipeId = RecipeIdEnum.DEFAULT.toString();
			 String pythonRecipeId = RecipeIdEnum.PY_FASTAPI.toString();
			 String reactRecipeId = RecipeIdEnum.REACT.toString();
			 String angularRecipeId = RecipeIdEnum.ANGULAR.toString();
			 String quarkusRecipeId = RecipeIdEnum.QUARKUS.toString();
			 String micronautRecipeId = RecipeIdEnum.MICRONAUT.toString();
			 String vueRecipeId = RecipeIdEnum.VUEJS.toString();
			 String dashRecipeId = RecipeIdEnum.DASH.toString();
			 String expressjsRecipeId = RecipeIdEnum.EXPRESSJS.toString();
			 String streamlitRecipeId = RecipeIdEnum.STREAMLIT.toString();
			 String nestjsRecipeId = RecipeIdEnum.NESTJS.toString();
  //			String publicDnABackendRecipeId = RecipeIdEnum.PUBLIC_DNA_BACKEND.toString();
  //			String publicDnaFrontendRecipeId = RecipeIdEnum.PUBLIC_DNA_FRONTEND.toString();
  //			String publicDnaAirflowBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_AIRFLOW_BACKEND.toString();
  //			String publicDnaAuthenticatorBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_AUTHENTICATOR_BACKEND.toString();
  //			String publicDnaChronosBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_CHRONOS_BACKEND.toString();
  //			String publicDnaChronosMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_CHRONOS_MFE.toString();
  //			String publicDnaCodespaceBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_CODESPACE_BACKEND.toString();
  //			String publicDnaDataProductBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_DATA_PRODUCT_BACKEND.toString();
  //			String publicDnaDnaDataProductMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_DATA_PRODUCT_MFE.toString();
  //			String publicDnaDataikuBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_DATAIKU_BACKEND.toString();
  //			String publicDnaDssMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_DSS_MFE.toString();
  //			String publicDnaMalwareScannerRecipeId = RecipeIdEnum.PUBLIC_DNA_MALWARE_SCANNER.toString();
  //			String publicDnaModalRegistryBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_MODAL_REGISTRY_BACKEND.toString();
  //			String publicDnaNassRecipeId = RecipeIdEnum.PUBLIC_DNA_NASS.toString();
  //			String publicDnaReportBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_REPORT_BACKEND.toString();
  //			String publicDnaStorageBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_STORAGE_BACKEND.toString();
  //			String publicDnaStorageMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_STORAGE_MFE.toString();
  //			String publicDnaTrinoBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_TRINO_BACKEND.toString();
  
			 String projectRecipe = entity.getData().getProjectDetails().getRecipeDetails().getRecipeId();
			 String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
			 log.info("projectRecipe: {}", projectRecipe);
			 if (isCreateDeleteStatuses) {
				 if ("CREATED".equalsIgnoreCase(latestStatus)) {
					 String workspaceUrl = codeServerBaseUri + "/" + workspaceName + "/?folder=/home/coder";
					 cloudServiceProvider = entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider();
					 if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)){
						workspaceUrl = codespaceUrlAWS + "/" + workspaceName + "/?folder=/home/coder";
					 } else {
						workspaceUrl = codespaceUrl + "/" + workspaceName + "/?folder=/home/coder";
					 }
					 if (!defaultRecipeId.equalsIgnoreCase(projectRecipe))
						 workspaceUrl += "/app";
					 if (projectRecipe.toLowerCase().startsWith("public")) {
						 switch (projectRecipe) {
							 case "public-dna-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/backend";
								 break;
							 case "public-dna-frontend":
								 workspaceUrl = workspaceUrl + "/" + "packages/frontend";
								 break;
							 case "public-dna-report-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/dashboard-backend";
								 break;
							 case "public-dna-codespace-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/code-server";
								 break;
							 case "public-dna-malware-scanner":
								 workspaceUrl = workspaceUrl + "/" + "packages/malware-scanner";
								 break;
							 case "public-dna-storage-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/storage-mfe";
								 break;
							 case "public-dna-storage-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/storage-backend";
								 break;
							 case "public-dna-chronos-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/chronos-mfe";
								 break;
							 case "public-dna-chronos-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/chronos";
								 break;
							 case "public-dna-data-product-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/data-product-mfe";
								 break;
							 case "public-dna-data-product-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/data-product-backend";
								 break;
							 case "public-dna-dss-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/dss-mfe";
								 break;
							 case "public-dna-dataiku-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/dataiku-backend";
								 break;
							 case "public-dna-airflow-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/airflow-backend";
								 break;
							 case "public-dna-modal-registry-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/model-registry";
								 break;
							 case "public-dna-trino-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/trino-backend";
								 break;
							 case "public-dna-nass":
								 workspaceUrl = workspaceUrl + "/" + "packages/naas";
								 break;
							 case "public-dna-authenticator-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/authenticator-service";
								 break;
							 case "public-dna-matomo-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/matomo-mfe";
								 break;
							 case "public-dna-matomo-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/matomo-backend";
								 break;
							 case "public-dna-datalake-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/datalake-mfe";
								 break;
							 case "public-dna-fabric-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/fabric-mfe";
								 break;
							 case "public-dna-dataentry-mfe":
								 workspaceUrl = workspaceUrl + "/" + "packages/dataentry-mfe";
								 break;
							 case "public-dna-fabric-backend":
								 workspaceUrl = workspaceUrl + "/" + "packages/fabric-backend";
								 break;
  
						 }
					 }
				 }
				 entity.getData().setStatus(latestStatus);
				 workspaceCustomRepository.update(entity);
				 log.info("updated status for user {} , workspace name {}, existingStatus {}, latestStatus {}",
						 userId, wsId, existingStatus, latestStatus);
				 status = "SUCCESS";
				 responseMessage.setSuccess(status);
				 responseMessage.setErrors(errors);
				 responseMessage.setWarnings(warnings);
				 return responseMessage;
			 } else {
				 if (projectRecipe.toLowerCase().startsWith("public")
						 || projectRecipe.equalsIgnoreCase("default")) {
					 log.error("Cannot update public/private recipe types, deploy n undeploy is disabled");
					 MessageDescription msg = new MessageDescription(
							 "Cannot update public/private recipe types, deploy n undeploy is disabled.");
					 errors.add(msg);
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
				 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
				 Date now = isoFormat.parse(isoFormat.format(new Date()));
 //				CodeServerWorkspaceNsql ownerEntity = workspaceCustomRepository.findbyProjectName(projectOwner,
 //						projectName);
 //				if (ownerEntity == null || ownerEntity.getData() == null
 //						|| ownerEntity.getData().getWorkspaceId() == null) {
 //					MessageDescription error = new MessageDescription();
 //					error.setMessage(
 //							"Failed while deploying codeserver workspace project, couldnt fetch project owner details");
 //					errors.add(error);
 //					responseMessage.setErrors(errors);
 //					return responseMessage;
 //				}
 //				String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				String deploymentUrl = "";
				if (("int".equalsIgnoreCase(targetEnv)
						&& entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl() != null)
						|| ("prod".equalsIgnoreCase(targetEnv)
								&& entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl() != null)) {
					deploymentUrl = "int".equalsIgnoreCase(targetEnv)
							? entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl()
							: entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl();
				} else {
					deploymentUrl = codeServerBaseUri + "/" + projectName.toLowerCase() + "/" + targetEnv + "/";
					if (pythonRecipeId.equalsIgnoreCase(projectRecipe)) {
						deploymentUrl = codeServerBaseUri + "/" + projectName.toLowerCase() + "/" + targetEnv + "/docs";
					}
					if (reactRecipeId.equalsIgnoreCase(projectRecipe) || angularRecipeId.equalsIgnoreCase(projectRecipe)
							|| vueRecipeId.equalsIgnoreCase(projectRecipe) || dashRecipeId.equalsIgnoreCase(projectRecipe)
							|| streamlitRecipeId.equalsIgnoreCase(projectRecipe) || nestjsRecipeId.equalsIgnoreCase(projectRecipe)
							||
							expressjsRecipeId.equalsIgnoreCase(projectRecipe)) {
						deploymentUrl = codeServerBaseUri + "/" + projectName.toLowerCase() + "/" + targetEnv + "/";
					}
					if (quarkusRecipeId.equalsIgnoreCase(projectRecipe)) {
						deploymentUrl = codeServerBaseUri + "/" + projectName.toLowerCase() + "/" + targetEnv + "/q/swagger-ui";
					}
					if (micronautRecipeId.equalsIgnoreCase(projectRecipe)) {
						deploymentUrl = codeServerBaseUri + "/" + projectName.toLowerCase() + "/" + targetEnv
								+ "/swagger-ui/index.html";
					}
				}
				 String environmentJsonbName = "intDeploymentDetails";
				 CodeServerDeploymentDetails deploymentDetails = null;
				 CodeServerBuildDetails buildDetails = null;
				 if ("int".equalsIgnoreCase(targetEnv)) {
					 deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					 buildDetails = entity.getData().getProjectDetails().getIntBuildDetails();
				 } else {
					 environmentJsonbName = "prodDeploymentDetails";
					 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					 buildDetails = entity.getData().getProjectDetails().getProdBuildDetails();
				 }
				 cloudServiceProvider = entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider();
				 
				hasProdUrl = Objects.nonNull(
					entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl());
				hasIntUrl = Objects.nonNull(
					entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl());
					if ((hasProdUrl && entity.getData().getProjectDetails().getProdDeploymentDetails()
							.getDeploymentUrl().contains(codeServerBaseUriAws)) ||
							(hasIntUrl && entity.getData().getProjectDetails().getIntDeploymentDetails()
									.getDeploymentUrl().contains(codeServerBaseUriAws))) {
						cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
					} else if (hasProdUrl || hasIntUrl) {
						cloudServiceProvider = ConstantsUtility.DHC_CAAS;
					} else {
						cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
					}
				 if(cloudServiceProvider.equals(ConstantsUtility.DHC_CAAS_AWS)){
					deploymentUrl = deploymentUrl.replaceAll(codeServerBaseUri, codeServerBaseUriAws);
				 }
				 CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					 CodeServerBuildDeployNsql buildDeployentity = null;
					 CodeServerBuildDeploy buildDeployData = null;
				 if ("DEPLOYED".equalsIgnoreCase(latestStatus)) {
					 String existingDeploymentUrl = deploymentDetails.getDeploymentUrl();
					 deploymentDetails.setDeploymentUrl(deploymentUrl);
					 deploymentDetails.setLastDeployedBranch(branch);
					 deploymentDetails.setLastDeployedBy(entity.getData().getWorkspaceOwner());
					 deploymentDetails.setLastDeployedOn(now);
					 deploymentDetails.setLastDeploymentStatus(latestStatus);
					 deploymentDetails.setGitjobRunID(gitJobRunId);	
					 deploymentDetails.setLastDeployedVersion(version);	
						 workspaceCustomRepository.updateDeploymentDetails(projectName, targetEnv,
							 deploymentDetails,latestStatus);	 
						 
						 //setting audit log details
					 if(optionalBuildDeployentity != null){
						 buildDeployentity = optionalBuildDeployentity;
						 buildDeployData = buildDeployentity.getData();
						 List<DeploymentAudit>  auditLogs = new ArrayList<>();
						 List<BuildAudit> buildAuditLogs = new ArrayList<>();
						 if("int".equalsIgnoreCase(targetEnv)){	
							auditLogs = buildDeployData.getIntDeploymentAuditLogs();
							buildAuditLogs = buildDeployData.getIntBuildAuditLogs();						
							 int lastIndex = buildDeployData.getIntDeploymentAuditLogs().size() - 1;
							 buildDeployData.getIntDeploymentAuditLogs().get(lastIndex).setDeploymentStatus(latestStatus);
							 buildDeployData.getIntDeploymentAuditLogs().get(lastIndex).setDeployedOn(now);							 
							 
						 }else{
							auditLogs = buildDeployData.getProdDeploymentAuditLogs();
							buildAuditLogs = buildDeployData.getProdBuildAuditLogs();
							 int lastIndex = buildDeployData.getProdDeploymentAuditLogs().size() - 1;
							 buildDeployData.getProdDeploymentAuditLogs().get(lastIndex).setDeploymentStatus(latestStatus);
							 buildDeployData.getProdDeploymentAuditLogs().get(lastIndex).setDeployedOn(now);	
						 }

					// 	 List<DeploymentAudit> sortedList = auditLogs.stream().filter( i -> i.getDeploymentStatus().equalsIgnoreCase("DEPLOYED")).collect(Collectors.toList());
					// String lastDeployedVersion = (sortedList.size() > 0 && sortedList.size() != 1) ? sortedList.get(sortedList.size() - 1).getVersion():null;
					
					// if(lastDeployedVersion != null &&  buildAuditLogs.stream().anyMatch( i -> (i.getVersion().equalsIgnoreCase(lastDeployedVersion) && !i.isImageDeleted()))){
							buildAuditLogs.stream().forEach(i ->{
								if(!i.getVersion().equalsIgnoreCase(version) && !i.isKeepBuildImage() && !i.isImageDeleted()){
									GenericMessage deleteApiResonse = client.deleteBuild(projectName, i.getVersion());
									if(deleteApiResonse.getSuccess().equalsIgnoreCase("SUCCESS")){
										i.setImageDeleted(true);
									}									
								}
							});
						// }
						if("int".equalsIgnoreCase(targetEnv)){	
							buildDeployData.setIntBuildAuditLogs(buildAuditLogs);
						}else{
							buildDeployData.setProdBuildAuditLogs(buildAuditLogs);
						}
						 buildDeployentity.setData(buildDeployData);
						 buildDeployRepo.save(buildDeployentity);
					 }
					 status = "SUCCESS";
					 log.info(
							 "updated deployment details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							 projectName, branch, targetEnv, latestStatus);
					 // boolean apiRecipe = false;
					 // String serviceName = projectName;
					 // if (projectRecipe.equalsIgnoreCase(reactRecipeId)
					 // 		|| projectRecipe.equalsIgnoreCase(angularRecipeId)) {
					 // 	log.info("projectRecipe: {} and service name is : {}", projectRecipe, serviceName);
					 // 	authenticatorClient.callingKongApis(name, serviceName, targetEnv, apiRecipe,null,null);
					 // } else {
					 // 	apiRecipe = true;
					 // 	log.info("projectRecipe: {} and service name is : {}", projectRecipe, serviceName);
					 // 	authenticatorClient.callingKongApis(name, serviceName, targetEnv, apiRecipe,null,null);
					 // }
				} else if ("UNDEPLOYED".equalsIgnoreCase(latestStatus) || "RESTART_FAILED".equalsIgnoreCase(latestStatus) || "RESTARTED".equalsIgnoreCase(latestStatus) ) {
					if("UNDEPLOYED".equalsIgnoreCase(latestStatus)){
					 deploymentDetails.setDeploymentUrl(null);
					 deploymentDetails.setLastDeploymentStatus(latestStatus);
					}
					 
						 workspaceCustomRepository.updateDeploymentDetails(projectName, targetEnv,
						 deploymentDetails,latestStatus);
					 if(optionalBuildDeployentity != null){
						 buildDeployentity = optionalBuildDeployentity;
						 buildDeployData = buildDeployentity.getData();
						 if("int".equalsIgnoreCase(targetEnv)){							
							 int lastIndex = buildDeployData.getIntDeploymentAuditLogs().size() - 1;
							 buildDeployData.getIntDeploymentAuditLogs().get(lastIndex).setDeploymentStatus(latestStatus);							 
							 
						 }else{
							 int lastIndex = buildDeployData.getProdDeploymentAuditLogs().size() - 1;
							 buildDeployData.getProdDeploymentAuditLogs().get(lastIndex).setDeploymentStatus(latestStatus);
						 }
						 buildDeployentity.setData(buildDeployData);
						 buildDeployRepo.save(buildDeployentity);
					 }
					 status = "SUCCESS";
					 log.info(
							 "updated deployment details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							 projectName, branch, targetEnv, latestStatus);
				 }else if("BUILD_SUCCESS".equalsIgnoreCase(latestStatus) || "BUILD_FAILED".equalsIgnoreCase(latestStatus)){
					buildDetails.setLastBuildStatus(latestStatus);
					buildDetails.setLastBuildOn(now);
					buildDetails.setLastBuildBy(entity.getData().getWorkspaceOwner());
					buildDetails.setGitjobRunID(gitJobRunId);
					buildDetails.setLastBuildBranch(branch);

						workspaceCustomRepository.updateBuildDetails(projectName, targetEnv,
						buildDetails);	
				   
				   if(optionalBuildDeployentity != null){
					   buildDeployentity = optionalBuildDeployentity;
					   buildDeployData = buildDeployentity.getData();
					   Boolean keepBuildImage = false;
					   Boolean buildImageDeleted = false;
					   if("int".equalsIgnoreCase(targetEnv)){							
						   int lastIndex = buildDeployData.getIntBuildAuditLogs().size() - 1;
						   buildDeployData.getIntBuildAuditLogs().get(lastIndex).setBuildOn(now);
						   buildDeployData.getIntBuildAuditLogs().get(lastIndex).setBuildStatus(latestStatus);
						   keepBuildImage = buildDeployData.getIntBuildAuditLogs().get(lastIndex).isKeepBuildImage();
					   }else{
						   int lastIndex = buildDeployData.getProdBuildAuditLogs().size() - 1;
						   buildDeployData.getProdBuildAuditLogs().get(lastIndex).setBuildOn(now);
						   buildDeployData.getProdBuildAuditLogs().get(lastIndex).setBuildStatus(latestStatus);
						   keepBuildImage = buildDeployData.getProdBuildAuditLogs().get(lastIndex).isKeepBuildImage();
					   }
					   if("BUILD_SUCCESS".equalsIgnoreCase(latestStatus) && buildDetails.getLastBuildType().equalsIgnoreCase("build")){
					if(!keepBuildImage){
							GenericMessage deleteApiResonse = client.deleteBuild(projectName, version);
									if(deleteApiResonse.getSuccess().equalsIgnoreCase("SUCCESS")){
										buildImageDeleted = true;
									}
					}
					}
					if(buildImageDeleted){
						if("int".equalsIgnoreCase(targetEnv)){
							int lastIndex = buildDeployData.getIntBuildAuditLogs().size() - 1;
							buildDeployData.getIntBuildAuditLogs().get(lastIndex).setImageDeleted(true);
						}else{
							int lastIndex = buildDeployData.getProdBuildAuditLogs().size() - 1;
							buildDeployData.getProdBuildAuditLogs().get(lastIndex).setImageDeleted(true);
						}
					}

					   buildDeployentity.setData(buildDeployData);
					   buildDeployRepo.save(buildDeployentity);
				   }
				   status = "SUCCESS";
				   boolean isPrivateRecipe = false;
				   if(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("private")){
					isPrivateRecipe = true;
				   }
					log.info(
							"updated build details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							projectName, branch, targetEnv, latestStatus);
							if("BUILD_SUCCESS".equalsIgnoreCase(latestStatus) && buildDetails.getLastBuildType().equalsIgnoreCase("buildAndDeploy")){
								this.deployWorkspace(userId, entity.getId(), targetEnv, branch,
								isPrivateRecipe,version,"buildAndDeploy");
				   log.info("User {} deployed workspace {} project {}", userId, wsId,
						   entity.getData().getProjectDetails().getRecipeDetails().getRecipeId());
							   
					}
					// else{
					// 	log.info("User {} deployed workspace failed because of build failure {} project {}", userId, wsId,
					// 	   entity.getData().getProjectDetails().getRecipeDetails().getRecipeId());
					// }
				} else {
					 deploymentDetails.setDeploymentUrl(deploymentUrl);
					 deploymentDetails.setLastDeploymentStatus(latestStatus);
					 deploymentDetails.setGitjobRunID(gitJobRunId);
					
						 workspaceCustomRepository.updateDeploymentDetails(projectName, targetEnv,
						 deploymentDetails,latestStatus);
					 if(optionalBuildDeployentity != null){
						 buildDeployentity = optionalBuildDeployentity;
						 buildDeployData = buildDeployentity.getData();
						 if("int".equalsIgnoreCase(targetEnv)){							
							 int lastIndex = buildDeployData.getIntDeploymentAuditLogs().size() - 1;
							 buildDeployData.getIntDeploymentAuditLogs().get(lastIndex).setDeploymentStatus(latestStatus);							 
							 
						 }else{
							 int lastIndex = buildDeployData.getProdDeploymentAuditLogs().size() - 1;
							 buildDeployData.getProdDeploymentAuditLogs().get(lastIndex).setDeploymentStatus(latestStatus);
						 }
						 buildDeployentity.setData(buildDeployData);
						 buildDeployRepo.save(buildDeployentity);
						 status = "SUCCESS";
					 }
					 log.info(
							 "updated deployment details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							 projectName, branch, targetEnv, latestStatus);
				 }

				 
				 responseMessage.setSuccess(status);
				 responseMessage.setErrors(errors);
				 responseMessage.setWarnings(warnings);
				 return responseMessage;
			 }
		 } catch (Exception e) {
			 log.error("caught exception while updating status {}", e.getMessage());
			 MessageDescription error = new MessageDescription();
			 error.setMessage(
					 "Failed while deploying codeserver workspace project, couldnt fetch project owner details");
			 errors.add(error);
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
	 }
  
	 @Override
	 public List<String> getAllWorkspaceIds() {
		 return workspaceCustomRepository.getAllWorkspaceIds();
	 }
  
	 @Override
	 public CodeServerWorkspaceValidateVO validateCodespace(String id, String userId) {
		 return workspaceCustomRepository.validateCodespace(id, userId);
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage saveSecurityConfig(CodeServerWorkspaceVO vo , Boolean isPublished, String env) {
		 GenericMessage responseMessage = new GenericMessage();
		 try {
  
			 List<String> workspaceIds = workspaceCustomRepository
					 .getWorkspaceIdsByProjectName(vo.getProjectDetails().getProjectName());
			 if (!workspaceIds.isEmpty()) {
				 List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
				 boolean authorizerPluginCalled = false;
				 for (String id : workspaceIds) {
					 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(id);
					 CodespaceSecurityConfig config = workspaceAssembler.toSecurityConfig(vo.getProjectDetails().getSecurityConfig());
					 if(entity != null){
						 boolean isStagingPublishedBefore = entity.getData().getProjectDetails().getSecurityConfig().getStaging().getPublished().getAppID() != null && entity.getData().getProjectDetails().getSecurityConfig().getStaging().getPublished().getEntitlements() != null;
						 boolean isProductionPublishedBefore = entity.getData().getProjectDetails().getSecurityConfig().getProduction().getPublished().getAppID() != null && entity.getData().getProjectDetails().getSecurityConfig().getProduction().getPublished().getEntitlements() != null;
						 if(("int".equalsIgnoreCase(env) && entity.getData().getProjectDetails().getIntDeploymentDetails().getSecureWithDnaRequired() != null && entity.getData().getProjectDetails().getIntDeploymentDetails().getSecureWithDnaRequired() && !Objects.equals(config.getStaging().getDraft().getAppID(), dnaAppId)) ||
						 		 ("prod".equalsIgnoreCase(env) && entity.getData().getProjectDetails().getProdDeploymentDetails().getSecureWithDnaRequired() != null && entity.getData().getProjectDetails().getProdDeploymentDetails().getSecureWithDnaRequired() && !Objects.equals(config.getProduction().getDraft().getAppID(), dnaAppId))){
							 log.info("use our app id to secure your application with DnA credentials.");
							 MessageDescription msg = new MessageDescription();
							 List<MessageDescription> errorMessage = new ArrayList<>();
							 msg.setMessage("use " + dnaAppId + " app id to secure your application with DnA credentials.");
							 errorMessage.add(msg);
							 responseMessage.addErrors(msg);
							 responseMessage.setSuccess("FAILED");
							 responseMessage.setErrors(errorMessage);
							 return responseMessage;
						 }
						 entity.getData().getProjectDetails().setSecurityConfig(config);
  
						 if(isPublished){
							 if("int".equalsIgnoreCase(env)){
								 if(!((entity.getData().getProjectDetails().getIntDeploymentDetails().getSecureWithIAMRequired() != null && entity.getData().getProjectDetails().getIntDeploymentDetails().getSecureWithIAMRequired()) || (entity.getData().getProjectDetails().getIntDeploymentDetails().getSecureWithDnaRequired() != null && entity.getData().getProjectDetails().getIntDeploymentDetails().getSecureWithDnaRequired()))){
									 log.info("Secure your application before publishing your config.");
									 MessageDescription msg = new MessageDescription();
									 List<MessageDescription> errorMessage = new ArrayList<>();
									 msg.setMessage("Secure your application before publishing your config.");
									 errorMessage.add(msg);
									 responseMessage.addErrors(msg);
									 responseMessage.setSuccess("FAILED");
									 responseMessage.setErrors(errorMessage);
									 return responseMessage;
								 }
								 if(config.getStaging().getDraft().getAppID()!=null && config.getStaging().getDraft().getEntitlements()!=null && !config.getStaging().getDraft().getAppID().isEmpty() && !config.getStaging().getDraft().getEntitlements().isEmpty() ){
									 entity.getData().getProjectDetails().getSecurityConfig().getStaging().setPublished(config.getStaging().getDraft());
									 if(!isStagingPublishedBefore && !authorizerPluginCalled){
										String workspaceId = entity.getData().getWorkspaceId();
										String projectName = entity.getData().getProjectDetails().getProjectName();
										boolean isApiRecipe = entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentType().equalsIgnoreCase(ConstantsUtility.API);
										String ssoType = entity.getData().getProjectDetails().getIntDeploymentDetails().getSsoType();
										CodespaceSecurityConfig securityConfig = entity.getData().getProjectDetails().getSecurityConfig();
										authenticatorClient.callingApiAuthorizerPlugin(workspaceId, projectName, env, isApiRecipe, securityConfig, ssoType, ConstantsUtility.DHC_CAAS_AWS);
										authorizerPluginCalled = true;
									 }
								 }else{
									 log.info("APPID and Entitlement should not be empty while publishing");
									 MessageDescription msg = new MessageDescription();
									 List<MessageDescription> errorMessage = new ArrayList<>();
									 msg.setMessage("APPID and Entitlement should not be empty while publishing");
									 errorMessage.add(msg);
									 responseMessage.addErrors(msg);
									 responseMessage.setSuccess("FAILED");
									 responseMessage.setErrors(errorMessage);
									 return responseMessage;
								 }
							 }
							 if("prod".equalsIgnoreCase(env)){
								 if(!((entity.getData().getProjectDetails().getProdDeploymentDetails().getSecureWithIAMRequired() != null && entity.getData().getProjectDetails().getProdDeploymentDetails().getSecureWithIAMRequired()) || (entity.getData().getProjectDetails().getProdDeploymentDetails().getSecureWithDnaRequired() != null && entity.getData().getProjectDetails().getProdDeploymentDetails().getSecureWithDnaRequired()))){
									log.info("Secure your application before publishing your config.");
									 MessageDescription msg = new MessageDescription();
									 List<MessageDescription> errorMessage = new ArrayList<>();
									 msg.setMessage("Secure your application before publishing your config.");
									 errorMessage.add(msg);
									 responseMessage.addErrors(msg);
									 responseMessage.setSuccess("FAILED");
									 responseMessage.setErrors(errorMessage);
									 return responseMessage;
								 }
								 if(config.getProduction().getDraft().getAppID()!=null && config.getProduction().getDraft().getEntitlements()!=null && !config.getProduction().getDraft().getAppID().isEmpty() && !config.getProduction().getDraft().getEntitlements().isEmpty()){
									 entity.getData().getProjectDetails().getSecurityConfig().getProduction().setPublished(config.getProduction().getDraft());
									 if(!isProductionPublishedBefore && !authorizerPluginCalled){
										String workspaceId = entity.getData().getWorkspaceId();
										String projectName = entity.getData().getProjectDetails().getProjectName();
										boolean isApiRecipe = entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentType().equalsIgnoreCase(ConstantsUtility.API);
										String ssoType = entity.getData().getProjectDetails().getProdDeploymentDetails().getSsoType();
										CodespaceSecurityConfig securityConfig = entity.getData().getProjectDetails().getSecurityConfig();
										authenticatorClient.callingApiAuthorizerPlugin(workspaceId, projectName, env, isApiRecipe, securityConfig, ssoType, ConstantsUtility.DHC_CAAS_AWS);
										authorizerPluginCalled = true;
									 }
								 }else{
									 log.info("APPID and Entitlement should not be empty while publishing");
									 MessageDescription msg = new MessageDescription();
									 List<MessageDescription> errorMessage = new ArrayList<>();
									 msg.setMessage("APPID and Entitlement should not be empty while publishing");
									 errorMessage.add(msg);
									 responseMessage.addErrors(msg);
									 responseMessage.setSuccess("FAILED");
									 responseMessage.setErrors(errorMessage);
									 return responseMessage;
								 }
							 }
						 }
						 entities.add(entity);
					 }
				 }
				 jpaRepo.saveAllAndFlush(entities);
			 }
			 responseMessage.setSuccess("SUCCESS");
  
		 } catch (Exception e) {
			 log.error("caught exception while saving security config {}", e.getMessage());
			 MessageDescription msg = new MessageDescription();
			 List<MessageDescription> errorMessage = new ArrayList<>();
			 msg.setMessage("caught exception while saving security config");
			 errorMessage.add(msg);
			 responseMessage.addErrors(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errorMessage);
		 }
  
		 // CreatedByVO currentUser = this.userStore.getVO();
		 // String userId = currentUser != null ? currentUser.getId() : null;
  
		 // String resourceID = vo.getWorkspaceId();
		 // List<String> teamMembers = new ArrayList<>();
		 // List<String> teamMembersEmails = new ArrayList<>();
		 // List<ChangeLogVO> changeLogs = new ArrayList<>();
		 // UserInfoVO projectOwner = vo.getProjectDetails().getProjectOwner();
		 // teamMembers.add(projectOwner.getId());
		 // teamMembersEmails.add(projectOwner.getEmail());
		 // List<UserInfoVO> projectCollaborators = vo.getProjectDetails().getProjectCollaborators();
		 // if (Objects.nonNull(projectCollaborators)) {
		 // 	if (projectCollaborators.size() > 0) {
		 // 		for (UserInfoVO collab : projectCollaborators) {
		 // 			teamMembers.add(collab.getId());
		 // 			teamMembersEmails.add(collab.getEmail());
		 // 		}
		 // 	}
		 // }
		 // String eventType = "Codespace-SecurityConfig Status Update";
		 // String message = ""; 
  
		 // if (vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("ACCEPTED") ||vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("PUBLISHED")) {
		 // 	message = "Codespace " + vo.getProjectDetails().getProjectName() + " is accepted / published by Codespace Admin.";
		 // 	kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
		 // } 
		 // if ( vo.getProjectDetails().getSecurityConfig().getStatus().equalsIgnoreCase("REQUESTED") ) {
		 // 	message = "Codespace " + vo.getProjectDetails().getProjectName() + " is requesting to publish security config. ";
		 // 	//kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
		 // 	notifyAllCodespaceAdminUsers(eventType,resourceID,message,userId,changeLogs);
		 // }
  
  
		 return responseMessage;
  
	 }
  
	 // @Override
	 // @Transactional
	 // public GenericMessage updateSecurityConfigStatus(String projectName, String status, String userId,
	 // 		CodeServerWorkspaceVO vo) {
	 // 	GenericMessage responseMessage = new GenericMessage();
  
	 // 	try {
  
	 // 		responseMessage = workspaceCustomRepository.updateSecurityConfigStatus(projectName, status);
  
	 // 		// CodeServerWorkspaceNsql entity = workspaceAssembler.toEntity(vo);
	 // 		// jpaRepo.save(entity);
	 // 		// MessageDescription msg = new MessageDescription();
	 // 		// List<MessageDescription> errorMessage = new ArrayList<>();
  
	 // 		// responseMessage.setSuccess("SUCCESS");
  
	 // 	} catch (Exception e) {
	 // 		log.error("caught exception while saving security config {}", e.getMessage());
	 // 		MessageDescription msg = new MessageDescription();
	 // 		List<MessageDescription> errorMessage = new ArrayList<>();
	 // 		msg.setMessage("No workspace found for given id and the user");
	 // 		errorMessage.add(msg);
	 // 		responseMessage.addErrors(msg);
	 // 		responseMessage.setSuccess("FAILED");
	 // 		responseMessage.setErrors(errorMessage);
	 // 	}
  
	 // 	String resourceID = vo.getWorkspaceId();
	 // 	List<String> teamMembers = new ArrayList<>();
	 // 	List<String> teamMembersEmails = new ArrayList<>();
	 // 	List<ChangeLogVO> changeLogs = new ArrayList<>();
	 // 	UserInfoVO projectOwner = vo.getProjectDetails().getProjectOwner();
	 // 	teamMembers.add(projectOwner.getId());
	 // 	teamMembersEmails.add(projectOwner.getEmail());
	 // 	List<UserInfoVO> projectCollaborators = vo.getProjectDetails().getProjectCollaborators();
	 // 	if (Objects.nonNull(projectCollaborators)) {
	 // 		if (projectCollaborators.size() > 0) {
	 // 			for (UserInfoVO collab : projectCollaborators) {
	 // 				teamMembers.add(collab.getId());
	 // 				teamMembersEmails.add(collab.getEmail());
	 // 			}
	 // 		}
	 // 	}
	 // 	String eventType = "Codespace-SecurityConfig Status Update";
	 // 	String message = ""; 
  
	 // 	if (status.equalsIgnoreCase("ACCEPTED") ||status.equalsIgnoreCase("PUBLISHED")) {
	 // 		message = "Codespace " + projectName + " is accepted / published by Codespace Admin.";
	 // 		kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
	 // 	}
	 // 	if ( status.equalsIgnoreCase("REQUESTED") ) {
	 // 		message = "Codespace " + projectName + " is requesting to publish security config. ";
	 // 		//kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
	 // 		notifyAllCodespaceAdminUsers(eventType,resourceID,message,userId,changeLogs);
	 // 	}
  
	 // 	return responseMessage;
  
	 // }
  
	 @Override
	 public List<CodespaceSecurityConfigDetailsVO> getAllSecurityConfigs(Integer offset, Integer limit, String projectName) {
  
		 List<CodespaceSecurityConfigDto> collectionDtos = workspaceCustomRepository.getAllSecurityConfigs(offset,limit,projectName);
		 CodespaceSecurityConfigDetailsVO vo = new CodespaceSecurityConfigDetailsVO();
		 if(collectionDtos != null){
			 List<CodespaceSecurityConfigDetailsVO> finalConfigData = collectionDtos.stream()
					 .map(n -> workspaceAssembler.dtoToVo(n)).collect(Collectors.toList());
			 return finalConfigData;
		 }else{
			 return new ArrayList<>();
		 }
	 }
  
	 // public void notifyAllCodespaceAdminUsers(String eventType, String resourceId, String message, String triggeringUser,
	 // 		List<ChangeLogVO> changeLogs) {
	 // 	log.info("Notifying all Codespace Admin users on " + eventType + " for " + message);
	 // 	UsersCollection usersCollection = dnaAuthClient.getAll();
	 // 	List<com.daimler.data.dto.userinfo.UserInfoVO> allUsers = usersCollection.getRecords();
	 // 	List<String> codespaceAdminUsersIds = new ArrayList<>();
	 // 	List<String> codespaceAdminUsersEmails = new ArrayList<>();
	 // 	for (com.daimler.data.dto.userinfo.UserInfoVO user : allUsers) {
	 // 		boolean isCodespaceAdmin = false;
	 // 		if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getRoles())) {
	 // 			isCodespaceAdmin = user.getRoles().stream().anyMatch(role -> "CodespaceAdmin".equalsIgnoreCase(role.getName()));
	 // 		}
	 // 		if (isCodespaceAdmin) {
	 // 			codespaceAdminUsersIds.add(user.getId());
	 // 			codespaceAdminUsersEmails.add(user.getEmail());
	 // 		}
	 // 	}
	 // 	try {
	 // 		String id = resourceId.toString();
	 // 		kafkaProducer.send(eventType, id, "", triggeringUser, message, true, codespaceAdminUsersIds,
	 // 				codespaceAdminUsersEmails, changeLogs);
	 // 		log.info("Successfully notified all codespace admin users for event {} for {} ", eventType, message);
	 // 	} catch (Exception e) {
	 // 		log.error("Exception occurred while notifying all codespace Admin users on {}  for {} . Failed with exception {}",
	 // 				eventType, message, e.getMessage());
	 // 	}
	 // }
  
	 @Override
	 @Transactional
	 public GenericMessage updateGovernancenceValues(String userId, String id,
			 DataGovernanceRequestInfo dataGovernanceInfo) {
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
		 boolean isProjectOwner = false;
		 boolean isAdmin = false;
  
		 String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		 String projectName = entity.getData().getProjectDetails().getProjectName();
		 if (projectOwnerId.equalsIgnoreCase(userId)) {
			 isProjectOwner = true;
		 }
		 List<UserInfo>collabList =entity.getData().getProjectDetails().getProjectCollaborators();
		 if(collabList!=null){
			 for(UserInfo user : collabList){
				 if(userId.equalsIgnoreCase(user.getId())){
					 if(user.getIsAdmin()){
						 isAdmin =true;
					 }
				 }
			 }
		 }
  
		 if (isProjectOwner || isAdmin) {
			 try {
				 CodeServerLeanGovernanceFeilds newGovFeilds = new CodeServerLeanGovernanceFeilds();
				 BeanUtils.copyProperties(dataGovernanceInfo.getData(), newGovFeilds);
				 if (dataGovernanceInfo.getData().isPiiData() != null) {
					 newGovFeilds.setPiiData(dataGovernanceInfo.getData().isPiiData());
				 }
				 if (dataGovernanceInfo.getData().isEnableDeployApproval() != null) {
					newGovFeilds.setEnableDeployApproval(dataGovernanceInfo.getData().isEnableDeployApproval());
				}
				 GenericMessage updateLeanGovernanceFeilds = workspaceCustomRepository
						 .updateGovernanceDetails(projectName, newGovFeilds);
				 if ("SUCCESS".equalsIgnoreCase(updateLeanGovernanceFeilds.getSuccess())) {
					 responseMessage.setSuccess("SUCCESS");
				 } else {
					 log.error("Failed to update Lean Governance details");
					 MessageDescription msg = new MessageDescription("Failed to update lean governane details");
					 errors.add(msg);
					 responseMessage.setSuccess("FAILED");
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
			 } catch (Exception e) {
				 log.error("Failed to update governance details as requested with Exception: {} ", e.getMessage());
				 MessageDescription msg = new MessageDescription("Failed to update governance details");
				 errors.add(msg);
				 responseMessage.setSuccess("FAILED");
				 responseMessage.setErrors(errors);
				 return responseMessage;
			 }
  
		 } else {
			 log.error("Failed to update governance details as requested user is not a project owner "
					 + entity.getData().getWorkspaceId());
			 MessageDescription msg = new MessageDescription(
					 "Failed to update governance details as requested user is not a project owner");
			 errors.add(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
		 return responseMessage;
  
	 }
	 @Override
	 @Transactional
	 public String getServerStatus(CodeServerWorkspaceVO vo)
	 {
		 String userName = vo.getWorkspaceOwner().getId().toLowerCase();
		 String id = vo.getWorkspaceId(); 
		 CodeServerWorkspaceNsql savedOwnerEntity = workspaceCustomRepository.findbyProjectName(userName,vo.getProjectDetails().getProjectName());
			 String statusValue = "false";
			 GenericMessage responseMessage = new GenericMessage();
			 try {
  
				 boolean response = client.serverStatus(userName.toLowerCase(),id, vo.getProjectDetails().getRecipeDetails().getCloudServiceProvider().toString());
				 if (response) {
					 statusValue = "true";
					 savedOwnerEntity.getData().setServerStatus("SERVER_STARTED");
					 log.debug("Server started sucessfully for {} user of workspace {}",userName,id);
				 } else {
					 log.debug("Server is not started for {} user of workspace {}",userName,id);
					 statusValue = "false";
					 savedOwnerEntity.getData().setServerStatus("SERVER_STOPPED");
				 }
			 } catch (Exception e) {
				 log.error("caught exception while getting server status", e.getMessage());
			 }
			 jpaRepo.save(savedOwnerEntity);
			 return statusValue;
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage startServer(String userId,String wsId, String cloudServiceProvider)
	 {
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 try {
  
			 GenericMessage startServer = client.doStartServer(userId.toLowerCase(),wsId,cloudServiceProvider);
				 if (startServer != null) {
					 if (!"SUCCESS".equalsIgnoreCase(startServer.getSuccess()) ||
							 (startServer.getErrors() != null && !startServer.getErrors().isEmpty()) ||
							 (startServer.getWarnings() != null
									 && !startServer.getWarnings().isEmpty())) {
					 MessageDescription msg = new MessageDescription();
					 List<MessageDescription> errorMessage = new ArrayList<>(); 
						 msg.setMessage("Failed to start workbench.");
						 responseMessage.setSuccess("FAILED");
					 }
					 responseMessage.setSuccess("SUCCESS");
				 }
  
		 } catch (Exception e) {
			 log.error("caught exception while saving security config {}", e.getMessage());
			 MessageDescription msg = new MessageDescription();
			 List<MessageDescription> errorMessage = new ArrayList<>();
			 msg.setMessage("caught exception while saving security config");
			 errorMessage.add(msg);
			 responseMessage.addErrors(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errorMessage);
		 }
		 return responseMessage;
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage stopServer(CodeServerWorkspaceVO vo, String cloudServiceProvider) {
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 String wsid = vo.getWorkspaceId();
		 String userName = vo.getWorkspaceOwner().getId().toLowerCase();
		 CodeServerWorkspaceNsql savedOwnerEntity = workspaceCustomRepository.findbyProjectName(userName,vo.getProjectDetails().getProjectName());
  
		 try {
			 boolean stopServerResponse = client.stopServer(wsid, userName, cloudServiceProvider);
  
			 if (stopServerResponse) {
				 responseMessage.setSuccess("SUCCESS");
				 savedOwnerEntity.getData().setServerStatus("SERVER_STOPPED");
				 jpaRepo.save(savedOwnerEntity);
			 } else {
				 MessageDescription errorMsg = new MessageDescription();
				 errorMsg.setMessage("Failed to stop server.");
				 errors.add(errorMsg);
				 responseMessage.setSuccess("FAILED");
				 responseMessage.setErrors(errors);
			 }
		 } catch (Exception e) {
			 log.error("Caught exception while stopping server: {}", e.getMessage());
			 MessageDescription errorMsg = new MessageDescription();
			 errorMsg.setMessage("Exception occurred while stopping server: " + e.getMessage());
			 errors.add(errorMsg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errors);
		 }
		 return responseMessage;
	 }
  
	 @Override
	 public GenericMessage updateResourceValue(CodeServerWorkspaceNsql entity,ResourceVO updatedResourceValue)
	 {
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 try
		 {
			 CodeServerWorkspace workspace = entity.getData();
			 String repoName = "";
			 String repoNameWithOrg = "";
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 repoName = workspace.getProjectDetails().getGitRepoName();
			 if (workspace.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public") || workspace
					 .getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("private")) {
				 repoName = workspace.getProjectDetails().getRecipeDetails().getRepodetails();
			 }
			 String pathCheckout = "";
			 if (!workspace.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")
					 && !workspace.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
							 .startsWith("private")) {
				 repoNameWithOrg = gitOrgUri + gitOrgName + "/" + repoName;
			 } else {
				 repoNameWithOrg = workspace.getProjectDetails().getRecipeDetails().getRepodetails();
				 if(repoNameWithOrg==null || repoNameWithOrg.isEmpty() || repoNameWithOrg.isBlank()){
					 repoNameWithOrg = workspace.getProjectDetails().getGitRepoName();
					 String url[] = repoNameWithOrg.split(",");
					 repoNameWithOrg = url[0];
					 pathCheckout = url[1];
				 }else{
					pathCheckout="";
					if(repoNameWithOrg.contains(",")) {
						String url[] = repoNameWithOrg.split(",");
						repoNameWithOrg = url[0];
						pathCheckout = url[1];
					}
				 }
			 }
				 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
				 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
				 ownerWorkbenchCreateInputsDto.setCloudServiceProvider(workspace.getProjectDetails().getRecipeDetails().getCloudServiceProvider());
				 ownerWorkbenchCreateInputsDto.setStorage_capacity(updatedResourceValue.getDiskSpace()+"Gi");
				 ownerWorkbenchCreateInputsDto.setMem_guarantee(updatedResourceValue.getMinRam()+"M");
				 ownerWorkbenchCreateInputsDto.setMem_limit(updatedResourceValue.getMaxRam()+"M");
				 ownerWorkbenchCreateInputsDto.setCpu_limit(Double.parseDouble(updatedResourceValue.getMaxCpu()));
				 ownerWorkbenchCreateInputsDto.setCpu_guarantee(Double.parseDouble(updatedResourceValue.getMinCpu()));
				 if(workspace.getProjectDetails().getRecipeDetails().getToDeployType()!=null){
					 ownerWorkbenchCreateInputsDto.setProfile(workspace.getProjectDetails().getRecipeDetails().getToDeployType());
				 } else {
					 ownerWorkbenchCreateInputsDto.setProfile("default");
				 }
				 if(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
					ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValue);
				} else {
					ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValueAws);
				}
				 ownerWorkbenchCreateInputsDto.setIsCollaborator("false");
				 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg);
				 ownerWorkbenchCreateInputsDto.setShortid(workspace.getWorkspaceOwner().getId());
				 if(workspace.getProjectDetails().getRecipeDetails().getToDeployType()!=null){
					 ownerWorkbenchCreateInputsDto.setType(workspace.getProjectDetails().getRecipeDetails().getToDeployType());
				 } else {
					 ownerWorkbenchCreateInputsDto.setType("default");
				 }
				 List<String> extraContainers = new ArrayList<>();
				 List<String> additionalServices =  workspace.getProjectDetails().getRecipeDetails().getAdditionalServices();
				 if (additionalServices != null) {
					for (String additionalService : additionalServices) {
						String additionalServiceEnv = additionalServiceRepo.findByServiceName(additionalService);
						if(!additionalServiceEnv.isEmpty()) {
							StringBuffer addStringBuffer =  new StringBuffer();
							addStringBuffer.append(additionalServiceEnv);
							addStringBuffer.deleteCharAt(0);
							addStringBuffer.deleteCharAt(addStringBuffer.length()-1);
							extraContainers.add(addStringBuffer.toString());
						}
					}
				 }
				 ownerWorkbenchCreateInputsDto.setExtraContainers(extraContainers);
				 ownerWorkbenchCreateInputsDto.setWsid(workspace.getWorkspaceId());
				 ownerWorkbenchCreateInputsDto.setPathCheckout(pathCheckout);
				 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
				 String codespaceName = workspace.getProjectDetails().getProjectName();
				 String ownerwsid = workspace.getWorkspaceId();
				 boolean status = client.serverStatus(workspace.getWorkspaceOwner().getId().toLowerCase(),workspace.getWorkspaceId(),workspace.getProjectDetails().getRecipeDetails().getCloudServiceProvider());
				 if(status)
				 {
					 boolean stopserver = client.stopServer(workspace.getWorkspaceId(), workspace.getWorkspaceOwner().getId().toLowerCase(),workspace.getProjectDetails().getRecipeDetails().getCloudServiceProvider());
					 if(!stopserver)
					 {
						 responseMessage.setSuccess("FAILED");
						 MessageDescription errMsg = new MessageDescription("Failed while stoping server. stop before update");
						 errors.add(errMsg);
						 responseMessage.setErrors(errors);
						 responseMessage.setWarnings(warnings);
						 return responseMessage;
					 }
				 }
				  boolean createOwnerWSResponse = client.createServer(ownerWorkbenchCreateDto,codespaceName);
				 if (!createOwnerWSResponse) {
				 responseMessage.setSuccess("FAILED");
				 MessageDescription errMsg = new MessageDescription("Failed to update resource workspace.");
				 errors.add(errMsg);
				 responseMessage.setErrors(errors);
				 responseMessage.setWarnings(warnings);
				 return responseMessage;
				 }
				 log.info("resource value is updated scucessfully");
				 String resource = updatedResourceValue.getDiskSpace()+"Gi,"+updatedResourceValue.getMinRam()+"M,";
				 resource+= updatedResourceValue.getMinCpu()+","+updatedResourceValue.getMaxRam()+"M,"+updatedResourceValue.getMaxCpu();
				 workspace.getProjectDetails().getRecipeDetails().setResource(resource);
				 entity.setData(workspace);
				 jpaRepo.save(entity);
				 log.info("successfully saved resource value {}",resource);
				 
			 MessageDescription errMsg = new MessageDescription("Sucessfully created workspace");
			 errors.add(errMsg);
			 responseMessage.setSuccess("SUCCESS");
			 responseMessage.setErrors(errors);
			 responseMessage.setWarnings(warnings);
			 return responseMessage;
		 }
		 catch(Exception e)
		 {
			log.error(e.getMessage(),e);
			 MessageDescription errMsg = new MessageDescription(
					 "Failed with updating resource value");
			 errors.add(errMsg);
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
	 }
  
	 @Override
	 public GenericMessage moveExistingWorkspace(CodeServerWorkspaceNsql vo)
	 {
		 GenericMessage responseMessage = new GenericMessage();
		 List<MessageDescription> errors = new ArrayList<>();
		 List<MessageDescription> warnings = new ArrayList<>();
		 try
		 {
			 CodeServerWorkspace workspace = vo.getData();
			 String cloudServiceProvider = workspace.getProjectDetails().getRecipeDetails().getCloudServiceProvider();
			 String repoName = "";
			 String repoNameWithOrg = "";
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 repoName = workspace.getProjectDetails().getGitRepoName();
			 if (workspace.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public") || workspace
					 .getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("private")) {
				 repoName = workspace.getProjectDetails().getRecipeDetails().getRepodetails();
			 }
			 String pathCheckout = "";
			 if (!workspace.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")
					 && !workspace.getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
							 .startsWith("private")) {
				 repoNameWithOrg = gitOrgUri + gitOrgName + "/" + repoName;
			 } else {
				 repoNameWithOrg = workspace.getProjectDetails().getRecipeDetails().getRepodetails();
				 String url[] = repoNameWithOrg.split(",");
				 repoNameWithOrg = url[0];
				 pathCheckout = url[1];
			 }
				 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
				 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
				 ownerWorkbenchCreateInputsDto.setStorage_capacity("4Gi");
				 ownerWorkbenchCreateInputsDto.setMem_guarantee("200M");
				 ownerWorkbenchCreateInputsDto.setMem_limit("4000M");
				 ownerWorkbenchCreateInputsDto.setCpu_limit(2);
				 ownerWorkbenchCreateInputsDto.setCpu_guarantee(0.3);
				 if(workspace.getProjectDetails().getRecipeDetails().getToDeployType()!=null){
					 ownerWorkbenchCreateInputsDto.setProfile(workspace.getProjectDetails().getRecipeDetails().getToDeployType());
				 } else {
					 ownerWorkbenchCreateInputsDto.setProfile("default");
				 }
				 if(vo.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().equals(ConstantsUtility.DHC_CAAS)){
					ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValue);
				} else {
					ownerWorkbenchCreateInputsDto.setEnvironment(codeServerEnvValueAws);
				}
				 ownerWorkbenchCreateInputsDto.setIsCollaborator("false");
				 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg);
				 ownerWorkbenchCreateInputsDto.setShortid(workspace.getWorkspaceOwner().getId());
				 if(workspace.getProjectDetails().getRecipeDetails().getToDeployType()!=null){
					 ownerWorkbenchCreateInputsDto.setType(workspace.getProjectDetails().getRecipeDetails().getToDeployType());
				 } else {
					 ownerWorkbenchCreateInputsDto.setType("default");
				 }
				 ownerWorkbenchCreateInputsDto.setWsid(workspace.getWorkspaceId());
				 ownerWorkbenchCreateInputsDto.setResource(workspace.getProjectDetails().getRecipeDetails().getResource());
				 ownerWorkbenchCreateInputsDto.setPathCheckout(pathCheckout);
				 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
				 String codespaceName = workspace.getProjectDetails().getProjectName();
				 String ownerwsid = workspace.getWorkspaceId();
				 GenericMessage createOwnerWSResponse = client.toMoveExistingtoJupyterhub(ownerWorkbenchCreateDto,codespaceName);
				 if (!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) ||
				 (createOwnerWSResponse.getErrors() != null && !createOwnerWSResponse.getErrors().isEmpty()) ||
				 (createOwnerWSResponse.getWarnings() != null
						 && !createOwnerWSResponse.getWarnings().isEmpty())) {
				 responseMessage.setSuccess("FAILED");
				 MessageDescription errMsg = new MessageDescription("Failed to create workspace.");
				 errors.add(errMsg);
				 errors.addAll(createOwnerWSResponse.getErrors());
				 warnings.addAll(createOwnerWSResponse.getWarnings());
				 responseMessage.setErrors(errors);
				 responseMessage.setWarnings(warnings);
				 return responseMessage;
				 }
				 workspace.setStatus(ConstantsUtility.CREATEDSTATE);//added
				 String recipeId = workspace.getProjectDetails().getRecipeDetails().getRecipeId().toString();
				 String projectOwnerId = workspace.getWorkspaceOwner().getId();
				 String workspaceUrl = this.getWorkspaceUrl(recipeId,ownerwsid,projectOwnerId,ConstantsUtility.DHC_CAAS_AWS);
				 workspace.setWorkspaceUrl(workspaceUrl);
				 String resource = workspace.getProjectDetails().getRecipeDetails().getResource();
				 workspace.getProjectDetails().getRecipeDetails().setResource(resource);
				 workspace.setServerStatus("SERVER_STOPPED");
				 vo.setData(workspace);
				 jpaRepo.save(vo);
				 GenericMessage deleteRouteResponse = authenticatorClient.deleteRoute(vo.getData().getWorkspaceId(),
				 vo.getData().getWorkspaceId(), cloudServiceProvider);
				 if (deleteRouteResponse != null && deleteRouteResponse.getSuccess()!= null && deleteRouteResponse.getSuccess().equalsIgnoreCase("Success"))
					 log.info("Kong route: {} deleted successfully", vo.getData().getWorkspaceId());
				 else {
					 if (deleteRouteResponse.getErrors() != null && deleteRouteResponse.getErrors().get(0) != null) {
						 log.info("Failed to delete the Kong route: {} with exception : {}", vo.getData().getWorkspaceId(),
								 deleteRouteResponse.getErrors().get(0).getMessage());
					 }
				 }
				 // Deleting Kong service
				 GenericMessage deleteServiceResponse = authenticatorClient.deleteService(vo.getData().getWorkspaceId(), cloudServiceProvider);
				 if (deleteServiceResponse != null && deleteServiceResponse.getSuccess() != null && deleteServiceResponse.getSuccess().equalsIgnoreCase("Success"))
					 log.info("Kong service: {} deleted successfully", vo.getData().getWorkspaceId());
				 else {
					 if (deleteServiceResponse.getErrors() != null && deleteServiceResponse.getErrors().get(0) != null) {
						 log.info("Failed to delete the Kong service: {} with exception : {}", vo.getData().getWorkspaceId(),
								 deleteServiceResponse.getErrors().get(0).getMessage());
					 }
				 }
			 MessageDescription errMsg = new MessageDescription("Sucessfully created workspace");
			 errors.add(errMsg);
			 errors.addAll(createOwnerWSResponse.getErrors());
			 warnings.addAll(createOwnerWSResponse.getWarnings());
			 responseMessage.setSuccess("SUCCESS");
			 responseMessage.setErrors(errors);
			 responseMessage.setWarnings(warnings);
			 return responseMessage;
		 }
		 catch(Exception e)
		 {
			 MessageDescription errMsg = new MessageDescription(
					 "Failed with exception {}. Please delete repository manually if created and retry create workspaces");
			 errors.add(errMsg);
			 responseMessage.setErrors(errors);
			 return responseMessage;
		 }
	 }
  
	 @Override
	 @Transactional
	 public GenericMessage makeAdmin(CodeServerWorkspaceVO vo){
		 GenericMessage responseMessage = new GenericMessage();
		 try {
  
			 List<String> workspaceIds = workspaceCustomRepository
					 .getWorkspaceIdsByProjectName(vo.getProjectDetails().getProjectName());
			 if (!workspaceIds.isEmpty()) {
				 List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
				 for (String id : workspaceIds) {
					 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(id);
					 List<UserInfoVO> projectCollabsVO = vo.getProjectDetails().getProjectCollaborators();
					 List<UserInfo> projectCollabs = new ArrayList<UserInfo>();
					 if (projectCollabsVO != null && !projectCollabsVO.isEmpty()) {
						  projectCollabs = projectCollabsVO.stream().map(n -> workspaceAssembler.toUserInfo(n))
							  .collect(Collectors.toList());
					 }
					 if(entity != null){
						 if(vo.getProjectDetails().getProjectCollaborators()!=null){
							 entity.getData().getProjectDetails().setProjectCollaborators(projectCollabs);
							 entities.add(entity);
						 }
					 }
				 }
				 jpaRepo.saveAllAndFlush(entities);
			 }
			 responseMessage.setSuccess("SUCCESS");
  
		 } catch (Exception e) {
			 log.error("caught exception while making collaborator as admin :{}", e.getMessage());
			 MessageDescription msg = new MessageDescription();
			 List<MessageDescription> errorMessage = new ArrayList<>();
			 msg.setMessage("caught exception while making collaborator as admin");
			 errorMessage.add(msg);
			 responseMessage.addErrors(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errorMessage);
		 }
		 return responseMessage;
	 }

	 @Override
	 @Transactional
	 public GenericMessage makeApprover(CodeServerWorkspaceVO vo){
		 GenericMessage responseMessage = new GenericMessage();
		 try {
 
			 List<String> workspaceIds = workspaceCustomRepository
					 .getWorkspaceIdsByProjectName(vo.getProjectDetails().getProjectName());
			 if (!workspaceIds.isEmpty()) {
				 List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
				 for (String id : workspaceIds) {
					 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(id);
					 List<UserInfoVO> projectCollabsVO = vo.getProjectDetails().getProjectCollaborators();
					 List<UserInfo> projectCollabs = new ArrayList<UserInfo>();
					 if (projectCollabsVO != null && !projectCollabsVO.isEmpty()) {
						  projectCollabs = projectCollabsVO.stream().map(n -> workspaceAssembler.toUserInfo(n))
							  .collect(Collectors.toList());
					 }
					 if(entity != null){
						 if(vo.getProjectDetails().getProjectCollaborators()!=null){
							 entity.getData().getProjectDetails().setProjectCollaborators(projectCollabs);
							 entities.add(entity);
						 }
					 }
				 }
				 jpaRepo.saveAllAndFlush(entities);
			 }
			 responseMessage.setSuccess("SUCCESS");
 
		 } catch (Exception e) {
			 log.error("caught exception while making collaborator as approver :{}", e.getMessage());
			 MessageDescription msg = new MessageDescription();
			 List<MessageDescription> errorMessage = new ArrayList<>();
			 msg.setMessage("caught exception while making collaborator as approver");
			 errorMessage.add(msg);
			 responseMessage.addErrors(msg);
			 responseMessage.setSuccess("FAILED");
			 responseMessage.setErrors(errorMessage);
		 }
		 return responseMessage;
	 }
 
	@Override
	@Transactional
	public GenericMessage restartWorkspace(String userId, String id, String env){
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		String cloudServiceProvider = null;
		boolean hasProdUrl = false;
		boolean hasIntUrl = false;
		try {
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			if (entity != null) {
				DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				deployJobInputDto.setAction("restart");
				deployJobInputDto.setBranch("main");
				cloudServiceProvider = entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider();
				hasProdUrl = Objects.nonNull(
					entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl());
				hasIntUrl = Objects.nonNull(
					entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl());
				if ((hasProdUrl && entity.getData().getProjectDetails().getProdDeploymentDetails()
						.getDeploymentUrl().contains(codeServerBaseUriAws)) ||
						(hasIntUrl && entity.getData().getProjectDetails().getIntDeploymentDetails()
								.getDeploymentUrl().contains(codeServerBaseUriAws))) {
					cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
				} else if (hasProdUrl || hasIntUrl) {
					cloudServiceProvider = ConstantsUtility.DHC_CAAS;
				} else {
					cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
				}
				if(cloudServiceProvider.equals(ConstantsUtility.DHC_CAAS)){
					deployJobInputDto.setEnvironment(codeServerEnvValue);
				} else {
					deployJobInputDto.setEnvironment(codeServerEnvValueAws);
				}
				deployJobInputDto.setRepo(gitOrgName + "/" + entity.getData().getProjectDetails().getGitRepoName());
				String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				deployJobInputDto.setShortid(projectOwner);
				deployJobInputDto.setTarget_env(env);
				// deployJobInputDto.setType("RESTART");
				String projectName = entity.getData().getProjectDetails().getProjectName();
				CodeServerWorkspaceNsql ownerEntity = workspaceCustomRepository.findbyProjectName(projectOwner,
						projectName);
				if (ownerEntity == null || ownerEntity.getData() == null
						|| ownerEntity.getData().getWorkspaceId() == null) {
					MessageDescription error = new MessageDescription();
					error.setMessage(
							"Failed while restarting  codeserver workspace project, couldnt fetch project owner details");
					errors.add(error);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					return responseMessage;
				}
				if(("int".equalsIgnoreCase(env)&& !"DEPLOYED".equalsIgnoreCase(entity.getData().getProjectDetails()
				.getIntDeploymentDetails().getLastDeploymentStatus())) || "prod".equalsIgnoreCase(env)&& !"DEPLOYED".equalsIgnoreCase(entity.getData().getProjectDetails()
				.getProdDeploymentDetails().getLastDeploymentStatus())){
					MessageDescription error = new MessageDescription();
					error.setMessage(
							"Failed while restarting  codeserver workspace project, couldnt restart project Which is not in deployed state");
					errors.add(error);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					return responseMessage;
				}
				String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				Boolean isValutInjectorEnable = false;
				try{
					isValutInjectorEnable = VaultClient.enableVaultInjector(projectName.toLowerCase(), env);
				}catch(Exception e){
					MessageDescription error = new MessageDescription();
					error.setMessage("Some error occured during restart, with exception " + e.getMessage());
					errors.add(error);
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseMessage.setSuccess(status);
					return responseMessage;
				}
				deployJobInputDto.setWsid(projectOwnerWsId);
				deployJobInputDto.setProjectName(projectName.toLowerCase());
				deployJobInputDto.setValutInjectorEnable(isValutInjectorEnable);
				deploymentJobDto.setInputs(deployJobInputDto);
				deploymentJobDto.setRef(codeServerEnvRef);
				GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				if (jobResponse != null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					// String environmentJsonbName = "intDeploymentDetails";
					// CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails()
					// 		.getIntDeploymentDetails();
					// if (!"int".equalsIgnoreCase(env)) {
					// 	environmentJsonbName = "prodDeploymentDetails";
					// 	deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					// }
					
					List<DeploymentAudit> auditLogs = new ArrayList<>();
					CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){
						if("int".equalsIgnoreCase(env)){
							auditLogs = optionalBuildDeployentity.getData().getIntDeploymentAuditLogs();
						}else{
							auditLogs = optionalBuildDeployentity.getData().getProdDeploymentAuditLogs();
						}
					}
					if (auditLogs == null) {
						auditLogs = new ArrayList<>();
					}
					SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
					Date now = isoFormat.parse(isoFormat.format(new Date()));
					DeploymentAudit auditLog = new DeploymentAudit();
					auditLog.setTriggeredOn(now);
					auditLog.setTriggeredBy(entity.getData().getWorkspaceOwner().getGitUserName());				
					auditLog.setDeploymentStatus("RESTART_REQUESTED");
					auditLogs.add(auditLog);

					CodeServerBuildDeploy buildDeployLogs = null;
					CodeServerBuildDeployNsql auditLogEntity = null;
					if(optionalBuildDeployentity != null){
					   auditLogEntity = optionalBuildDeployentity;
					   buildDeployLogs =  auditLogEntity.getData();						
					}else{
						buildDeployLogs = new CodeServerBuildDeploy();
						auditLogEntity = new CodeServerBuildDeployNsql();
						String deployLogId = UUID.randomUUID().toString();
						auditLogEntity.setId(deployLogId);
						buildDeployLogs.setIntBuildAuditLogs(new ArrayList<>());
						buildDeployLogs.setProdBuildAuditLogs(new ArrayList<>());	
						buildDeployLogs.setProjectName(projectName.toLowerCase());
						buildDeployLogs.setStatus("CREATED");	
								
					}
					if("int".equalsIgnoreCase(env)){
					   buildDeployLogs.setIntDeploymentAuditLogs(auditLogs);
					}else{
					   buildDeployLogs.setProdDeploymentAuditLogs(auditLogs);
					}
					auditLogEntity.setData(buildDeployLogs);
					buildDeployRepo.save(auditLogEntity);
					status = "SUCCESS";
					
				} else {
					status = "FAILED";
					errors.addAll(jobResponse.getErrors());
				}
			}
		} catch (Exception e) {
			log.error("Failed while restarting codeserver workspace project with exception : {} ", e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while restarting codeserver workspace project with exception " + e.getMessage());
			errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
	}
 
	@Override
	@Transactional
	public GenericMessage migrateWorkspace(CodeServerWorkspaceNsql entity){
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try{
			String recipeId= null;
			String ownersWsid = null;
			String workspaceUrl = null;
			String shortId=null;
			if(ConstantsUtility.DHC_CAAS.equalsIgnoreCase(entity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider().toString())){
				recipeId = entity.getData().getProjectDetails().getRecipeDetails().getRecipeId();
				ownersWsid = entity.getData().getWorkspaceId();
				shortId = entity.getData().getWorkspaceOwner().getId();
				workspaceUrl = this.getWorkspaceUrl(recipeId,ownersWsid,shortId,ConstantsUtility.DHC_CAAS_AWS);
				entity.getData().getProjectDetails().getRecipeDetails().setCloudServiceProvider(ConstantsUtility.DHC_CAAS_AWS);
				entity.getData().setIsWorkspaceMigrated(true);
				entity.getData().setWorkspaceUrl(workspaceUrl);
				jpaRepo.save(entity);
				status = "SUCCESS";
			} else {
				MessageDescription error = new MessageDescription();
				log.info("workspace already migrated "+ entity.getData().getWorkspaceId());
				error.setMessage("workspace already migrated , Bad Request ");
				errors.add(error);
			}
		} catch (Exception e) {
			MessageDescription error = new MessageDescription();
			log.info("Failed while Migrating codeserver workspace project with exception " + e.getMessage());
			error.setMessage("Failed while Migrating codeserver workspace project with exception " + e.getMessage());
			errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
	}
 
	@Override
	public CodeServerUserGroupCollectionVO createWorkSpaceGroup(CodeServerUserGroupVO vo){
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		CreatedByVO currentUser = this.userStore.getVO();
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));			
			CodeServerUserGroupNsql entity = null;
			CodeServerUserGroupList data = null;
			Optional<CodeServerUserGroupNsql> entityOptional = userGroupRepository.findById(currentUser.getId());
			if(entityOptional.isPresent()){
				entity = entityOptional.get();
				data = entity.getData();
			}else{
				entity = new CodeServerUserGroupNsql();
				data = new CodeServerUserGroupList();
				List<CodeServerUserGroup> groupList = new ArrayList<>();
				data.setGroups(groupList);
			}
			CodeServerUserGroup userGroup = new CodeServerUserGroup();
			String id = UUID.randomUUID().toString();
				userGroup.setCreatedBy(currentUser.getId());
				userGroup.setCreatedDate(now);
				userGroup.setName(vo.getName());
				userGroup.setOrder(vo.getOrder());
				userGroup.setGroupId(id);
				userGroup.setUpdatedBy(currentUser.getId());
				userGroup.setUpdatedDate(now);

				//remove workspace from other groups
				if(data.getGroups() != null || !data.getGroups().isEmpty()){
				data.getGroups().forEach(group ->{
					if(group.getWorkspaces() != null || !group.getWorkspaces().isEmpty()){
					vo.getWorkspaces().forEach(workSpaceInReq ->{
						group.getWorkspaces().removeIf(i -> i.getWorkSpaceId().equals(workSpaceInReq.getWorkspaceId()));
					});
					}
				});
				}
				List<CodeServerUserGroupWsDetails> workspaceList = new ArrayList<>();
				vo.getWorkspaces().forEach(workSpaceInReq ->{
					CodeServerUserGroupWsDetails workSpace = new CodeServerUserGroupWsDetails();
					workSpace.setWorkSpaceId(workSpaceInReq.getWorkspaceId());
					workSpace.setOrder(0);
					workspaceList.add(workSpace);
					CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpaceInReq.getWorkspaceId() );                            
						if (workspaceVo != null) {
							workspaceVo.setActiveInGroup(Boolean.TRUE);
							CodeServerWorkspaceNsql workSpaceEntity = workspaceAssembler.toEntity(workspaceVo);
							jpaRepo.save(workSpaceEntity);

						}
					
				});
				userGroup.setWorkspaces(workspaceList);

				data.getGroups().add(userGroup);
				entity.setData(data);
				entity.setId(currentUser.getId());
				CodeServerUserGroupNsql savedEntity = userGroupRepository.save(entity);
				CodeServerUserGroupCollectionVO responseData = groupassembler.toVo(savedEntity);
				responseData.getData().forEach(group ->{
					List<CodeServerWorkspaceVO> workspaceListt = new ArrayList<>(); 
					group.getWorkspaces().forEach(workSpace ->{                           
						CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpace.getWorkspaceId() );                           
						if(null != workspaceVo && null != workspaceVo.getId())
							workspaceListt.add(workspaceVo);
					});
					group.setWorkspaces(workspaceListt);
				});

				return responseData;
		} catch (Exception e) {
			log.info("Failed while creating codeserver workspace group with exception " + e.getMessage());
			return  null;
		}


	}

	@Override
	public CodeServerUserGroupCollectionVO updateWorkSpaceGroup(UpdateUserGroupRequestVO vo){
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		CreatedByVO currentUser = this.userStore.getVO();
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			String id = "";
			CodeServerUserGroupNsql entity = null;
			CodeServerUserGroupList data = null;
			Optional<CodeServerUserGroupNsql> entityOptional = userGroupRepository.findById(currentUser.getId());
			if(entityOptional.isPresent()){
				entity = entityOptional.get();
				data = entity.getData();
			}else{
				return null;
			}
			CodeServerUserGroup userGroup = data.getGroups().stream().filter(i -> i.getGroupId().equals(vo.getGroupId())).findFirst().orElse(null);
				if(userGroup != null){
				userGroup.setName(vo.getName());
				userGroup.setUpdatedBy(currentUser.getId());
				userGroup.setUpdatedDate(now);

				//remove newly added workspace from other groups
				if(data.getGroups() != null || !data.getGroups().isEmpty()){
				data.getGroups().forEach(group ->{
					vo.getWsAdded().forEach(workSpaceInReq ->{
						group.getWorkspaces().removeIf(i -> i.getWorkSpaceId().equals(workSpaceInReq.getWsId()));
					});
				});
				}
				List<CodeServerUserGroupWsDetails> workspaceList = userGroup.getWorkspaces();
				vo.getWsAdded().forEach(workSpaceInReq ->{
					CodeServerUserGroupWsDetails workSpace = new CodeServerUserGroupWsDetails();
					workSpace.setWorkSpaceId(workSpaceInReq.getWsId());
					workSpace.setOrder(0);
					workspaceList.add(workSpace);
					CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpaceInReq.getWsId() );                            
						if (workspaceVo != null ) {
							workspaceVo.setActiveInGroup(Boolean.TRUE);
							CodeServerWorkspaceNsql workSpaceEntity = workspaceAssembler.toEntity(workspaceVo);
							jpaRepo.save(workSpaceEntity);

						}
				});

				//remove workspace from exisitng group
				vo.getWsRemoved().forEach(workSpaceInReq ->{
					workspaceList.removeIf(i -> i.getWorkSpaceId().equals(workSpaceInReq.getWsId()));
					CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpaceInReq.getWsId() );                            
						if (workspaceVo != null) {
							workspaceVo.setActiveInGroup(Boolean.FALSE);
							CodeServerWorkspaceNsql workSpaceEntity = workspaceAssembler.toEntity(workspaceVo);
							jpaRepo.save(workSpaceEntity);

						}
				});
				userGroup.setWorkspaces(workspaceList);
				data.getGroups().removeIf(i -> i.getGroupId().equals(vo.getGroupId()));
				data.getGroups().add(userGroup);
				entity.setData(data);
				CodeServerUserGroupNsql savedEntity = userGroupRepository.save(entity);
				CodeServerUserGroupCollectionVO responseData = groupassembler.toVo(savedEntity);
				responseData.getData().forEach(group ->{
					List<CodeServerWorkspaceVO> workspaceListt = new ArrayList<>(); 
					group.getWorkspaces().forEach(workSpace ->{                           
						CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpace.getWorkspaceId() );                           
						if(null != workspaceVo && null != workspaceVo.getId())
							workspaceListt.add(workspaceVo);
					});
					group.setWorkspaces(workspaceListt);
				});
				return responseData;
			}else
				return null;
		} catch (Exception e) {
			log.info("Failed while updating codeserver workspace group with exception " + e.getMessage());
			return  null;
		}
	}

	@Override
	public CodeServerUserGroupCollectionVO getAllWorkSpaceGroup(){
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			CodeServerUserGroupCollectionVO responseData = null;
			Optional<CodeServerUserGroupNsql> entity = userGroupRepository.findById(currentUser.getId());
			if(entity.isPresent()){
				responseData = groupassembler.toVo(entity.get());
				responseData.getData().forEach(group ->{
					List<CodeServerWorkspaceVO> workspaceListt = new ArrayList<>(); 
					group.getWorkspaces().forEach(workSpace ->{                           
						CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpace.getWorkspaceId() );                           
						if(null != workspaceVo && null != workspaceVo.getId()){
							if(workspaceVo.getProjectDetails().getRecipeDetails().isIsDeployEnabled() == null || !workspaceVo.getProjectDetails().getRecipeDetails().isIsDeployEnabled()) {
								if(workspaceVo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("private")||workspaceVo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")||workspaceVo.getProjectDetails().getRecipeDetails().getRecipeId().name().equalsIgnoreCase("template")){
									workspaceVo.getProjectDetails().getRecipeDetails().setIsDeployEnabled(false);
								}else{
									workspaceVo.getProjectDetails().getRecipeDetails().setIsDeployEnabled(true);
								}
							}
							String serverStatus = getServerStatus(workspaceVo); // Update server status
			 				if(serverStatus.equalsIgnoreCase("true"))
			 				{
								workspaceVo.setServerStatus("SERVER_STARTED");
			 				}
			 				else
			 				{
								workspaceVo.setServerStatus("SERVER_STOPPED");
			 				}
							workspaceListt.add(workspaceVo);
						}
					});
					group.setWorkspaces(workspaceListt);
				});
			}else{
				responseData = new CodeServerUserGroupCollectionVO();
				responseData.setUserId(currentUser.getId());
				responseData.setData(new ArrayList<>());
			}
			return responseData;
		} catch (Exception e) {
			log.info("Failed while getAllWorkSpaceGroup codeserver workspace group with exception " + e.getMessage());
			return  null;
		}
	}

	@Override
	public CodeServerUserGroupByIdVO getWorkSpaceGroupById(String id){
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			CodeServerUserGroupByIdVO responseData = null;
			List<CodeServerWorkspaceVO> workspaceList = new ArrayList<>();
			Optional<CodeServerUserGroupNsql> optionalEntity = userGroupRepository.findById(currentUser.getId());
			if(optionalEntity.isPresent()){
				responseData = new CodeServerUserGroupByIdVO();
				CodeServerUserGroupNsql entity = optionalEntity.get();
				    Optional<CodeServerUserGroup> optionalGroup = entity.getData().getGroups().stream().filter(i -> i.getGroupId().equalsIgnoreCase(id)).findFirst();
					if(optionalGroup.isPresent()){
						CodeServerUserGroup group = optionalGroup.get();
						responseData.setGroupId(group.getGroupId());
                        responseData.setName(group.getName());
                        responseData.setOrder(group.getOrder());						
						group.getWorkspaces().forEach(workSpace ->{                           
                            CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpace.getWorkSpaceId() );                             
                            if(null != workspaceVo && null != workspaceVo.getId())
								workspaceList.add(workspaceVo);
                        });
					}
					responseData.setWorkspaces(workspaceList);        
			}
			return responseData;
		} catch (Exception e) {
			log.info("Failed while getWorkSpaceGroupById codeserver workspace group with exception " + e.getMessage());
			return  null;
		}
	}

	@Override
	public CodeServerUserGroupCollectionVO deleteWorkSpaceGroup(String id){
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		CreatedByVO currentUser = this.userStore.getVO();
		try {
			CodeServerUserGroupNsql entity = null;
			CodeServerUserGroupList data = null;
			Optional<CodeServerUserGroupNsql> entityOptional = userGroupRepository.findById(currentUser.getId());
			if(entityOptional.isPresent()){
				entity = entityOptional.get();
				data = entity.getData();
			}
			data.getGroups().forEach(group ->{
				if(group.getGroupId().equals(id)){
					 group.getWorkspaces().forEach(workSpace ->{
						CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpace.getWorkSpaceId() );                            
						if (workspaceVo != null) {
							workspaceVo.setActiveInGroup(Boolean.FALSE);
							CodeServerWorkspaceNsql workSpaceEntity = workspaceAssembler.toEntity(workspaceVo);
							jpaRepo.save(workSpaceEntity);

						}
					 });	
				}
			} );
			data.getGroups().removeIf(i -> i.getGroupId().equals(id));
			entity.setData(data);
			CodeServerUserGroupNsql savedEntity = userGroupRepository.save(entity);
			CodeServerUserGroupCollectionVO responseData = groupassembler.toVo(savedEntity);
			responseData.getData().forEach(group ->{
				List<CodeServerWorkspaceVO> workspaceListt = new ArrayList<>(); 
					group.getWorkspaces().forEach(workSpace ->{                           
						CodeServerWorkspaceVO workspaceVo = this.getByUniqueliteral(currentUser.getId(), "workspaceId", workSpace.getWorkspaceId() );                           
						if(null != workspaceVo && null != workspaceVo.getId())
							workspaceListt.add(workspaceVo);
					});
					group.setWorkspaces(workspaceListt);
			});
			return responseData;
		} catch (Exception e) {
			log.info("Failed while deleting codeserver workspace group with exception " + e.getMessage());
			return  null;
		}
	}

	@Override
	@Transactional
	public GenericMessage buildWorkSpace(String userId,String id,String branch,ManageBuildRequestDto buildRequestDto,boolean isPrivateRecipe,String environment,String lastBuildType){
		GenericMessage responseMessage = new GenericMessage();
		 String status = "FAILED";
		 List<MessageDescription> warnings = new ArrayList<>();
		 List<MessageDescription> errors = new ArrayList<>();
		 String cloudServiceProvider = null;
		 boolean workspaceMigrated = false;
		 boolean hasProdUrl = false;
		boolean hasIntUrl = false;
		 try {
			 String repoName = null;
			 String repoUrl = null;
			 String gitOrg = null;
			 CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			 if (entity != null ) {
				 DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				 DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				 deployJobInputDto.setAction("build");
				 deployJobInputDto.setBranch(branch);
				 deployJobInputDto.setEnvironment(codeServerEnvValue);
				 deployJobInputDto.setTarget_env(environment);

  			if (isPrivateRecipe) {
					repoUrl = entity.getData().getProjectDetails().getRecipeDetails().getRepodetails();
					if(Objects.nonNull(repoUrl) && repoUrl.contains(".git")){
						repoUrl = repoUrl.replaceAll("\\.git$", "/");
					} else {
						repoUrl.concat("/");
					}
					List<String> repoDetails = CommonUtils.getDetailsFromUrl(repoUrl);
					if (repoDetails.size() > 0 && repoDetails != null) {
						repoName = repoDetails.get(2);
						gitOrg = repoDetails.get(1);
					}
					deployJobInputDto.setRepo(gitOrg + "/" + repoName);		
				} else {
					repoName = entity.getData().getProjectDetails().getGitRepoName();
					deployJobInputDto.setRepo(gitOrgName + "/" + repoName);		

				}
				 String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				 String workspaceOwner = entity.getData().getWorkspaceOwner().getId();
				 deployJobInputDto.setShortid(workspaceOwner);
 
				 String projectName = entity.getData().getProjectDetails().getProjectName();
				 CodeServerWorkspaceNsql ownerEntity = workspaceCustomRepository.findbyProjectName(projectOwner,projectName);
				 cloudServiceProvider = ownerEntity.getData().getProjectDetails().getRecipeDetails().getCloudServiceProvider();
				 if(Objects.nonNull(ownerEntity.getData().getIsWorkspaceMigrated())) {
					workspaceMigrated = ownerEntity.getData().getIsWorkspaceMigrated();
				 }
				hasProdUrl = Objects.nonNull(
						ownerEntity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl());
				hasIntUrl = Objects.nonNull(
						ownerEntity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl());				
					if (cloudServiceProvider.equals(ConstantsUtility.DHC_CAAS) && (hasIntUrl || hasProdUrl)) {
						cloudServiceProvider = ConstantsUtility.DHC_CAAS;
					} else {
						cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
					}
				if ((hasProdUrl && ownerEntity.getData().getProjectDetails().getProdDeploymentDetails()
						.getDeploymentUrl().contains(codeServerBaseUriAws)) ||
						(hasIntUrl && ownerEntity.getData().getProjectDetails().getIntDeploymentDetails()
								.getDeploymentUrl().contains(codeServerBaseUriAws))) {
					cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
				} else if (hasProdUrl || hasIntUrl) {
					cloudServiceProvider = ConstantsUtility.DHC_CAAS;
				} else {
					cloudServiceProvider = ConstantsUtility.DHC_CAAS_AWS;
				}
				 if(cloudServiceProvider.equals(ConstantsUtility.DHC_CAAS)){
					deployJobInputDto.setEnvironment(codeServerEnvValue);
				 } else {
					deployJobInputDto.setEnvironment(codeServerEnvValueAws);
				 }
				 if (ownerEntity == null || ownerEntity.getData() == null
						 || ownerEntity.getData().getWorkspaceId() == null) {
					 MessageDescription error = new MessageDescription();
					 error.setMessage(
							 "Failed while deploying codeserver workspace project, couldnt fetch project owner details");
					 errors.add(error);
					 responseMessage.setErrors(errors);
					 return responseMessage;
				 }
				 Boolean isValutInjectorEnable = false;
				 try{
					isValutInjectorEnable = VaultClient.enableVaultInjector(projectName.toLowerCase(), environment);
				 }catch(Exception e){
					MessageDescription error = new MessageDescription();
					error.setMessage("Some error occured during deployment, with exception " + e.getMessage());
					errors.add(error);
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseMessage.setSuccess(status);
					return responseMessage;
				 }
				 int versionNumber = 0;
					CodeServerBuildDetails buildDetails = entity.getData().getProjectDetails().getIntBuildDetails();
					 if (!"int".equalsIgnoreCase(environment)) {
						 buildDetails = entity.getData().getProjectDetails().getProdBuildDetails();
					 }
					 if(buildDetails == null){
						 buildDetails = new CodeServerBuildDetails();
						 versionNumber = 1;
					 }else if(buildDetails.getVersion() == null){
						versionNumber = 1;
					 }else{
						 String num[] = buildDetails.getVersion().split("-");
						 versionNumber = Integer.parseInt(num[1].substring(1));
						 versionNumber++;	
					 }
					 String appVersion = environment+"-v"+versionNumber;
					 deployJobInputDto.setAppVersion(appVersion);
				String workspaceOwnerWsId = entity.getData().getWorkspaceId();
				 //String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				 deployJobInputDto.setWsid(workspaceOwnerWsId);
				 deployJobInputDto.setProjectName(projectName.toLowerCase());
				 deployJobInputDto.setValutInjectorEnable(isValutInjectorEnable);
				 deploymentJobDto.setInputs(deployJobInputDto);
				 deploymentJobDto.setRef(codeServerEnvRef);
				 log.info("deploymentJobDto {}",deploymentJobDto);
				 GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				 if (jobResponse != null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
					Date now = isoFormat.parse(isoFormat.format(new Date()));
					buildDetails.setLastBuildBranch(branch);
					buildDetails.setLastBuildBy(entity.getData().getWorkspaceOwner());
					buildDetails.setLastBuildOn(now);
					buildDetails.setLastBuildStatus("BUILD_REQUESTED");
					buildDetails.setVersion(appVersion);
					buildDetails.setLastBuildType(lastBuildType);
					buildDetails.setGitjobRunID("");
					
					workspaceCustomRepository.updateBuildDetails(projectName,environment,buildDetails);
					List<BuildAudit> auditLogs = new ArrayList<>();
					CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){
						if("int".equalsIgnoreCase(environment)){
							auditLogs = optionalBuildDeployentity.getData().getIntBuildAuditLogs();
						}else{
							auditLogs = optionalBuildDeployentity.getData().getProdBuildAuditLogs();
						}
					}	
					if(null == auditLogs){
						auditLogs = new ArrayList<>();
					}
					 BuildAudit auditLog = new BuildAudit();
					 GitLatestCommitIdDto commitId =null;
					 if(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()
					 .startsWith("private")){
						List<String> repoDetails = CommonUtils.getRepoNameFromGitUrl(entity.getData().getProjectDetails().getGitRepoName());
						commitId = gitClient.getLatestCommitId(repoDetails.get(0),branch,repoDetails.get(1));
					}else{
						commitId = gitClient.getLatestCommitId(gitOrgName,branch,entity.getData().getProjectDetails().getGitRepoName());
						
					}
					if(commitId == null){
						MessageDescription warning = new MessageDescription();
						warning.setMessage("Error while adding commit id to deployment audit log");
					}else{
						auditLog.setCommitId(commitId.getSha());
					}
					 auditLog.setImageDeleted(Boolean.FALSE);
					 auditLog.setTriggeredOn(now);
					 auditLog.setTriggeredBy(entity.getData().getWorkspaceOwner().getGitUserName());
					 auditLog.setBranch(branch);
					 auditLog.setBuildStatus("BUILD_REQUESTED");
					 auditLog.setComments(buildRequestDto.getComments());
					 auditLog.setVersion(appVersion);
					 auditLog.setKeepBuildImage(buildRequestDto.isKeepBuildImage() != null && buildRequestDto.isKeepBuildImage());
					 auditLogs.add(auditLog);
					 CodeServerBuildDeploy buildDeployLogs = null;
					 CodeServerBuildDeployNsql auditLogEntity = null;
					 if(optionalBuildDeployentity != null){
						auditLogEntity = optionalBuildDeployentity;
						buildDeployLogs =  auditLogEntity.getData();						
					 }else{
						 buildDeployLogs = new CodeServerBuildDeploy();
						 auditLogEntity = new CodeServerBuildDeployNsql();
						 String deployLogId = UUID.randomUUID().toString();
						 auditLogEntity.setId(deployLogId);
						 buildDeployLogs.setIntDeploymentAuditLogs(new ArrayList<>());
						 buildDeployLogs.setProdDeploymentAuditLogs(new ArrayList<>());	
						 buildDeployLogs.setProjectName(projectName.toLowerCase());	
						 buildDeployLogs.setStatus("CREATED");			
					 }
					 if("int".equalsIgnoreCase(environment)){
						buildDeployLogs.setIntBuildAuditLogs(auditLogs);
					 }else{
						buildDeployLogs.setProdBuildAuditLogs(auditLogs);
					 }
					 auditLogEntity.setData(buildDeployLogs);

					 buildDeployRepo.save(auditLogEntity);
					status = "SUCCESS";
				 } else {
					 status = "FAILED";
					 errors.addAll(jobResponse.getErrors());
				 }
			 }
		
		 } catch (Exception e) {
			log.error("Failed while build codeserver workspace project with exception : {} ", e.getMessage());
			 MessageDescription error = new MessageDescription();
			 error.setMessage("Failed while build codeserver workspace project with exception " + e.getMessage());
			 errors.add(error);
		 }
		 responseMessage.setErrors(errors);
		 responseMessage.setWarnings(warnings);
		 responseMessage.setSuccess(status);
		 return responseMessage;
		
	}

	@Override
	public CodeServerWorkspaceVO findByWorkspaceId(String wsId){
		try {
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(wsId);
			if (entity != null) {
				return workspaceAssembler.toVo(entity);
			}
			return null;
		} catch (Exception e) {
			log.error("Caught exception while fetching workspace by id: {}", e.getMessage());
			return null;
		}
		
	}

	@Override
	public VersionListResponseVO getBuildVersion(String projectName){
		VersionListResponseVO response = null;
		try {
			CodeServerBuildDeployNsql entity =  buildDeployCustomRepo.findByProjectName(projectName);
			if (entity != null) {
				response = new VersionListResponseVO();
				CodeServerBuildDeploy buildDeploy = entity.getData();
				if (buildDeploy != null) {
					List<BuildAudit> intBuildDetails = buildDeploy.getIntBuildAuditLogs();
					List<BuildAudit> prodBuildDetails = buildDeploy.getProdBuildAuditLogs();
					VersionCollectionVO intVersion = new VersionCollectionVO();
					VersionCollectionVO prodVersion = new VersionCollectionVO();
					intVersion.setEnvironment("int");
					prodVersion.setEnvironment("prod");
					List<VersioVO> intVersions = new ArrayList<>();
					List<VersioVO> prodVersions = new ArrayList<>();

					if (intBuildDetails != null) {
						intBuildDetails.stream().forEach(i ->{
							if(i.getBuildStatus().equalsIgnoreCase("BUILD_SUCCESS") && !i.isImageDeleted()){
								VersioVO version = new VersioVO();
								version.setVersion(i.getVersion());
								intVersions.add(version);
							}
						});
					}
					if (prodBuildDetails != null) {
						prodBuildDetails.stream().forEach(i ->{
							if(i.getBuildStatus().equalsIgnoreCase("BUILD_SUCCESS") && !i.isImageDeleted()){
								VersioVO version = new VersioVO();
								version.setVersion(i.getVersion());
								prodVersions.add(version);
							}
						});
					}
					if(intVersions.size() > 0){
						intVersion.setVersions(intVersions);
					}else{
						intVersion.setVersions(new ArrayList<>());
					}
					if(prodVersions.size() > 0){
						prodVersion.setVersions(prodVersions);
					}else{
						prodVersion.setVersions(new ArrayList<>());
					}
					response.setIntBuildVersions(intVersion);
					response.setProdBuildVersions(prodVersion);
				}
			}
			return response;
		} catch (Exception e) {
			log.error("Caught exception while fetching build versions: {}", e.getMessage());
			return null;
		}
	}

	@Override
	@Transactional
	public GenericMessage rejectDeployApproval(String userId, String id) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();		
		try{
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		Date now = isoFormat.parse(isoFormat.format(new Date()));
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId, id);
			if (entity != null) {
				String projectName = entity.getData().getProjectDetails().getProjectName();
				String environmentJsonbName = "prodDeploymentDetails";
				CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
				List<DeploymentAudit> auditLogs = new ArrayList<>();
					CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){						
							auditLogs = optionalBuildDeployentity.getData().getProdDeploymentAuditLogs();						
					}
				if (auditLogs == null) {
				 auditLogs = new ArrayList<>();
				}
				DeploymentAudit auditLog = new DeploymentAudit();
				if (!auditLogs.isEmpty()){
					 auditLog = auditLogs.get(auditLogs.size() - 1);
				}
				auditLog.setApprovedBy(entity.getData().getWorkspaceOwner().getGitUserName());				
				auditLog.setDeploymentStatus("APPROVAL_REJECTED");
				if (!auditLogs.isEmpty()){
					auditLogs.set(auditLogs.size() - 1, auditLog);
				}
				else{
					auditLogs.add(auditLog);
				}

				CodeServerBuildDeploy buildDeployLogs = null;
				 CodeServerBuildDeployNsql auditLogEntity = null;
				 if(optionalBuildDeployentity != null){
					auditLogEntity = optionalBuildDeployentity;
					buildDeployLogs =  auditLogEntity.getData();						
				 }else{
					 buildDeployLogs = new CodeServerBuildDeploy();
					 auditLogEntity = new CodeServerBuildDeployNsql();
					 String deployLogId = UUID.randomUUID().toString();	
					 auditLogEntity.setId(deployLogId);
					 buildDeployLogs.setIntBuildAuditLogs(new ArrayList<>());
					 buildDeployLogs.setProdBuildAuditLogs(new ArrayList<>());	
					 buildDeployLogs.setIntDeploymentAuditLogs(new ArrayList<>());
					 buildDeployLogs.setProjectName(projectName.toLowerCase());	
					 buildDeployLogs.setStatus("CREATED");	
						 				
				 }
					buildDeployLogs.setProdDeploymentAuditLogs(auditLogs);
				 
				 auditLogEntity.setData(buildDeployLogs);
				 buildDeployRepo.save(auditLogEntity);
				 deploymentDetails.setLastDeploymentStatus("APPROVAL_REJECTED");
					workspaceCustomRepository.updateDeploymentDetails(projectName, "prod",
					deploymentDetails,"APPROVAL_REJECTED");
					status = "SUCCESS";
			}
		} catch (Exception e) {
			log.error("Failed while rejecting codeserver workspace project with exception : {} ", e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while rejecting codeserver workspace project deployment with exception " + e.getMessage());
				errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage; 
	}

	@Override
	@Transactional
	public GenericMessage migrateWorkspaceLogs(CodeServerWorkspaceNsql entity){
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		Boolean deployingInProgress = false;
			Boolean intDeployingInProgress = false;
			Boolean prodDeployingInProgress = false;
			Boolean intDeployed = false;
			Boolean intCodeDeployFailed = false;
			Date intLastDeployedTime = new Date(0);
			Boolean prodDeployed = false;
			Boolean prodCodeDeployFailed = false;
			Date prodLastDeployedTime = new Date(0);
			Boolean deployed = false;
			Date lastBuildOrDeployedOn = null;
			String lastBuildOrDeployedStatus = null;
			String lastBuildOrDeployedEnv = null;
		try{
			List<DeploymentAudit> intAuditLogs =  new ArrayList<>();
			List<DeploymentAudit> prodAuditLogs =  new ArrayList<>();
			String projectName = entity.getData().getProjectDetails().getProjectName();
			if(entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentAuditLogs() != null){
				intAuditLogs.addAll(entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentAuditLogs());
			 }
			 if(entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentAuditLogs() != null){
				prodAuditLogs.addAll(entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentAuditLogs());
			 }
			 CodeServerProjectDetails projectDetails = entity.getData().getProjectDetails();
			 if(null == intAuditLogs){
				 intAuditLogs = new ArrayList<>();
			 }
			 if(null == prodAuditLogs){
				 prodAuditLogs = new ArrayList<>();
			 }
					CodeServerBuildDeploy buildDeployLogs = new CodeServerBuildDeploy();
					CodeServerBuildDeployNsql auditLogEntity = new CodeServerBuildDeployNsql();
					String deployLogId = UUID.randomUUID().toString();	
					 auditLogEntity.setId(deployLogId);
					 buildDeployLogs.setIntBuildAuditLogs(new ArrayList<>());
					 buildDeployLogs.setProdBuildAuditLogs(new ArrayList<>());
					 buildDeployLogs.setIntDeploymentAuditLogs(new ArrayList<>());
					 buildDeployLogs.setProdDeploymentAuditLogs(new ArrayList<>());					 
					 buildDeployLogs.setProjectName(projectName.toLowerCase());	
					 buildDeployLogs.setStatus("CREATED");				
				 	
					 buildDeployLogs.getIntDeploymentAuditLogs().addAll(intAuditLogs);
					 buildDeployLogs.getProdDeploymentAuditLogs().addAll(prodAuditLogs);
					
					 auditLogEntity.setData(buildDeployLogs);
					 buildDeployRepo.save(auditLogEntity);	
					

			  if(projectDetails.getIntDeploymentDetails().getLastDeploymentStatus() != null &&
			  !projectDetails.getIntDeploymentDetails().getLastDeploymentStatus().isBlank()){
				String lastStatus = projectDetails.getIntDeploymentDetails().getLastDeploymentStatus();
					if(lastStatus.equalsIgnoreCase("DEPLOY_REQUESTED")){
						intDeployingInProgress = true;
					}else if(lastStatus.equalsIgnoreCase("DEPLOYED")){
						intDeployed = true;						
					}else if(lastStatus.equalsIgnoreCase("DEPLOYMENT_FAILED")){
						intCodeDeployFailed = true;
					}else if(projectDetails.getIntDeploymentDetails().getDeploymentUrl() != null && projectDetails.getIntDeploymentDetails().getDeploymentUrl() != "null"){
						intDeployed = true;
					}
			  }
				
			  if(projectDetails.getIntDeploymentDetails().getLastDeploymentStatus() != null && 
			  projectDetails.getIntDeploymentDetails().getLastDeploymentStatus().equalsIgnoreCase("DEPLOYED")){
				intLastDeployedTime = projectDetails.getIntDeploymentDetails().getLastDeployedOn();
			  }else if(!projectDetails.getIntDeploymentDetails().getDeploymentAuditLogs().isEmpty()){
				int size = projectDetails.getIntDeploymentDetails().getDeploymentAuditLogs().size();
				intLastDeployedTime = 	projectDetails.getIntDeploymentDetails().getDeploymentAuditLogs().get(size -1).getTriggeredOn();
			  }

			  if(projectDetails.getProdDeploymentDetails().getLastDeploymentStatus() != null &&
			  !projectDetails.getProdDeploymentDetails().getLastDeploymentStatus().isBlank()){
				String lastStatus = projectDetails.getProdDeploymentDetails().getLastDeploymentStatus();
					if(lastStatus.equalsIgnoreCase("DEPLOY_REQUESTED")){
						prodDeployingInProgress = true;
					}else if(lastStatus.equalsIgnoreCase("DEPLOYED")){						
						prodDeployed = true;
					}else if(lastStatus.equalsIgnoreCase("DEPLOYMENT_FAILED")){
						prodCodeDeployFailed = true;
					}else if(projectDetails.getProdDeploymentDetails().getDeploymentUrl() != null && projectDetails.getProdDeploymentDetails().getDeploymentUrl() != "null"){
						prodDeployed = true;
					}
			  }

			  if(projectDetails.getProdDeploymentDetails().getLastDeploymentStatus() != null &&
			   projectDetails.getProdDeploymentDetails().getLastDeploymentStatus().equalsIgnoreCase("DEPLOYED")){
				prodLastDeployedTime = projectDetails.getProdDeploymentDetails().getLastDeployedOn();
			  }else if(!projectDetails.getProdDeploymentDetails().getDeploymentAuditLogs().isEmpty()){
				int size = projectDetails.getProdDeploymentDetails().getDeploymentAuditLogs().size();
				prodLastDeployedTime = 	projectDetails.getProdDeploymentDetails().getDeploymentAuditLogs().get(size -1).getTriggeredOn();
			  }

			  if(intDeployingInProgress || prodDeployingInProgress){
				deployingInProgress = true;
			  }

			  if(intDeployed || prodDeployed || intCodeDeployFailed || prodCodeDeployFailed){
				deployed = true;
			  }
			  String wsStatus = entity.getData().getStatus();
			  if(wsStatus != "CREATE_REQUESTED" && wsStatus != "CREATE_FAILED"){
					if(deployingInProgress){
					if(intDeployingInProgress){
						lastBuildOrDeployedEnv = "int";
						lastBuildOrDeployedOn = intLastDeployedTime;
					}else{
						lastBuildOrDeployedEnv = "prod";
						lastBuildOrDeployedOn = prodLastDeployedTime;
					}
					lastBuildOrDeployedStatus = "DEPLOY_REQUESTED";
					}else if(deployed){
						if(intLastDeployedTime.compareTo(prodLastDeployedTime) > 0){
							if(intCodeDeployFailed){
								lastBuildOrDeployedStatus = "DEPLOYMENT_FAILED";
							}else{
								lastBuildOrDeployedStatus = "DEPLOYED";
							}
							lastBuildOrDeployedEnv = "int";
							lastBuildOrDeployedOn = intLastDeployedTime;

						}else{
							if(prodCodeDeployFailed){
								lastBuildOrDeployedStatus = "DEPLOYMENT_FAILED";
							}else{
								lastBuildOrDeployedStatus = "DEPLOYED";
							}
							lastBuildOrDeployedEnv = "prod";
							lastBuildOrDeployedOn = prodLastDeployedTime;

						}
					}
			  }

			  workspaceCustomRepository.updateLatestBuildOrDeployStatus(lastBuildOrDeployedStatus,lastBuildOrDeployedEnv,
			  					lastBuildOrDeployedOn,projectDetails.getProjectName());



				 
				 status = "SUCCESS";

		} catch (Exception e) {
			MessageDescription error = new MessageDescription();
			log.info("Failed while Migrating codeserver workspace logs  with exception " + e.getMessage());
			error.setMessage("Failed while Migrating codeserver workspace logs  with exception " + e.getMessage());
			errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
	}

	@Override
	public GenericMessage deleteBuild(String projectName,String version){
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);
			CodeServerBuildDeploy data = optionalBuildDeployentity.getData();
			 List<BuildAudit> builds = new ArrayList<>();
			//   List<BuildAudit> newBuilds = new ArrayList<>();
			  String env = "";
                        if(version.startsWith("int")){
							env = "int";
                            builds = data.getIntBuildAuditLogs();
                        }else if(version.startsWith("prod")){
							env = "prod";
                             builds = data.getProdBuildAuditLogs();
                        }
                        if(builds.stream().anyMatch( i -> (i.getVersion().equalsIgnoreCase(version) && !i.isImageDeleted()))){
							builds.stream().forEach(i ->{
								if(i.getVersion().equalsIgnoreCase(version)){
									GenericMessage deleteApiResonse = client.deleteBuild(projectName, version);
									if(deleteApiResonse.getSuccess().equalsIgnoreCase("SUCCESS")){
										i.setImageDeleted(true);
									}									
								}
							});
						}
						if(env.equals("int")){
							data.setIntBuildAuditLogs(builds);
						}else{
							data.setProdBuildAuditLogs(builds);
						}
						optionalBuildDeployentity.setData(data);
						 buildDeployRepo.save(optionalBuildDeployentity);
						 status = "SUCCESS";
						
		} catch (Exception e) {
			MessageDescription error = new MessageDescription();
			log.info("Failed while Migrating codeserver workspace logs  with exception " + e.getMessage());
			error.setMessage("Failed while Migrating codeserver workspace logs  with exception " + e.getMessage());
			errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
	}


	public GenericMessage getStatusByJobRunId(CodeServerWorkspaceNsql entity){
		GenericMessage responseMessage = new GenericMessage();
		String status = "SUCCESS";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		String gitJobRunId = "";
		String branch = "";
		String appVersion = "";
		String message = "";
		String eventType = "";
		String environment = "Staging";
		try {
			CodeServerWorkspace data = entity.getData();
			String resourceID = data.getWorkspaceId();
			List<String> teamMembers = new ArrayList<>();
			List<String> teamMembersEmails = new ArrayList<>();
			UserInfo projectOwner = data.getProjectDetails().getProjectOwner();
			teamMembers.add(projectOwner.getId());
			teamMembersEmails.add(projectOwner.getEmail());
			if(data.getProjectDetails().getLastBuildOrDeployedStatus().equalsIgnoreCase("BUILD_REQUESTED")){
				CodeServerBuildDetails buildDetails = entity.getData().getProjectDetails().getIntBuildDetails();
					 if (!"int".equalsIgnoreCase(data.getProjectDetails().getLastBuildOrDeployedEnv())) {
						 buildDetails = entity.getData().getProjectDetails().getProdBuildDetails();
					 }
				gitJobRunId = buildDetails.getGitjobRunID();
				branch = buildDetails.getLastBuildBranch();
				appVersion = buildDetails.getVersion();	 
			}else if(data.getProjectDetails().getLastBuildOrDeployedStatus().equalsIgnoreCase("DEPLOY_REQUESTED")){
				CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					 if (!"int".equalsIgnoreCase(data.getProjectDetails().getLastBuildOrDeployedEnv())) {
						 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					 }
				gitJobRunId = deploymentDetails.getGitjobRunID();
				branch = deploymentDetails.getLastDeployedBranch();
				appVersion = deploymentDetails.getLastDeployedVersion();		 
			}
			if(data.getProjectDetails().getLastBuildOrDeployedEnv().equalsIgnoreCase("prod")) {
				environment = "Production";
			}
			if(gitJobRunId != null && !gitJobRunId.isBlank() && !gitJobRunId.isEmpty()){
			ResponseEntity<String> gitJobResponse = client.getStatusByJobRunId(gitJobRunId);

					ObjectMapper objectMapper = new ObjectMapper();
            		JsonNode rootNode = objectMapper.readTree(gitJobResponse.getBody());
            		String responseStatus = rootNode.path("status").asText();
            		String conclusion = rootNode.path("conclusion").asText();
					String latestStatus = null;
					String updateStatus = "";
					if(responseStatus.equalsIgnoreCase("completed") && conclusion.equalsIgnoreCase("success") ){
						latestStatus = "success";
					}else if(responseStatus.equalsIgnoreCase("completed") && conclusion.equalsIgnoreCase("failure")){
						latestStatus = "failure";
					}
			if(latestStatus != null){
			if(data.getProjectDetails().getLastBuildOrDeployedStatus().equalsIgnoreCase("BUILD_REQUESTED")){
				if(latestStatus.equalsIgnoreCase("success"))
					updateStatus = 	"BUILD_SUCCESS";
				else
					updateStatus = 	"BUILD_FAILED";	 
			}else if(data.getProjectDetails().getLastBuildOrDeployedStatus().equalsIgnoreCase("DEPLOY_REQUESTED")){
				if(latestStatus.equalsIgnoreCase("success"))
					updateStatus = 	"DEPLOYED";
				else
					updateStatus = 	"DEPLOYMENT_FAILED";	 
			}	
			String projectName = data.getProjectDetails().getProjectName();
			String  userId = data.getProjectDetails().getProjectOwner().getId();
				if(updateStatus.equalsIgnoreCase("BUILD_SUCCESS")) {
					eventType = "Codespace-build";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Successfully build Codespace "+ projectName + " with branch " + data.getProjectDetails() +" on " + environment + " triggered by " +userId;
				}
				if(updateStatus.equalsIgnoreCase("BUILD_FAILED")) {
					eventType = "Codespace-build Failed";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Failed to build Codespace " + projectName + " with branch " + branch +" on " +  environment + " triggered by " +userId;
				}
				if(updateStatus.equalsIgnoreCase("DEPLOYED")) {
					eventType = "Codespace-Deploy";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Successfully deployed Codespace "+ projectName + " with branch " + branch +" on " + environment + " triggered by " +userId;
				}													
				if(updateStatus.equalsIgnoreCase("DEPLOYMENT_FAILED")) {
					eventType = "Codespace-Deploy Failed";
					log.info("Latest status is {}, and eventType is {}",latestStatus,eventType);
					message = "Failed to deploy Codespace " + projectName + " with branch " + branch +" on " +  environment + " triggered by " +userId;
				}
				GenericMessage updateResponse = this.update(userId,data.getWorkspaceId(),projectName,data.getStatus(),updateStatus,data.getProjectDetails().getLastBuildOrDeployedEnv(),branch,gitJobRunId,appVersion);
				if(updateResponse != null && updateResponse.getErrors() != null && !updateResponse.getErrors().isEmpty()){
					errors.addAll(updateResponse.getErrors());
					status = "FAILED";
				}else{
					kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
					status = "SUCCESS";
				}
			}	
		}		 
		} catch (Exception e) {
			MessageDescription error = new MessageDescription();
			log.info("Failed while getStatusByJobRunId  with exception " + e.getMessage());
			error.setMessage("Failed while getStatusByJobRunId  with exception " + e.getMessage());
			errors.add(error);
			status = "FAILED";
		}

		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
	}

	public String updateGitJobRunId(GitJobRunIdRequestVO requestVo){
		String response = "FAILED";
		try {
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findByWorkspaceId(requestVo.getWsId());
			CodeServerWorkspace data = entity.getData();
			CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(requestVo.getProjectName());	
			CodeServerBuildDeployNsql buildDeployentity = null;
			CodeServerBuildDeploy buildDeployData = null;
			if(data.getProjectDetails().getLastBuildOrDeployedStatus().equalsIgnoreCase("BUILD_REQUESTED")){
				CodeServerBuildDetails buildDetails = entity.getData().getProjectDetails().getIntBuildDetails();
					 if (!"int".equalsIgnoreCase(data.getProjectDetails().getLastBuildOrDeployedEnv())) {
						 buildDetails = entity.getData().getProjectDetails().getProdBuildDetails();
					 }
				buildDetails.setGitjobRunID(requestVo.getGitJobRunId());
				workspaceCustomRepository.updateBuildDetails(requestVo.getProjectName(),data.getProjectDetails().getLastBuildOrDeployedEnv(),buildDetails);	 
				if(optionalBuildDeployentity != null){
					   buildDeployentity = optionalBuildDeployentity;
					   buildDeployData = buildDeployentity.getData();
					   if("int".equalsIgnoreCase(data.getProjectDetails().getLastBuildOrDeployedEnv())){							
						   int lastIndex = buildDeployData.getIntBuildAuditLogs().size() - 1;
						   buildDeployData.getIntBuildAuditLogs().get(lastIndex).setGitjobRunID(requestVo.getGitJobRunId());						   
					   }else{
						   int lastIndex = buildDeployData.getProdBuildAuditLogs().size() - 1;
						   buildDeployData.getProdBuildAuditLogs().get(lastIndex).setGitjobRunID(requestVo.getGitJobRunId());
					   }
					   buildDeployentity.setData(buildDeployData);
					   buildDeployRepo.save(buildDeployentity);
				   }
				   response = "SUCCESS";
				
			}else if(data.getProjectDetails().getLastBuildOrDeployedStatus().equalsIgnoreCase("DEPLOY_REQUESTED")){
				 CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					 if (!"int".equalsIgnoreCase(data.getProjectDetails().getLastBuildOrDeployedEnv())) {
						 deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					 }
				deploymentDetails.setGitjobRunID(requestVo.getGitJobRunId());	
				workspaceCustomRepository.updateDeploymentDetails(requestVo.getProjectName(), data.getProjectDetails().getLastBuildOrDeployedEnv(),deploymentDetails,data.getProjectDetails().getLastBuildOrDeployedStatus()); 				
				 //setting audit log details
					 if(optionalBuildDeployentity != null){
						 buildDeployentity = optionalBuildDeployentity;
						 buildDeployData = buildDeployentity.getData();
						 if("int".equalsIgnoreCase(data.getProjectDetails().getLastBuildOrDeployedEnv())){							
							 int lastIndex = buildDeployData.getIntDeploymentAuditLogs().size() - 1;
							 buildDeployData.getIntDeploymentAuditLogs().get(lastIndex).setGitjobRunID(requestVo.getGitJobRunId());							 
							 
						 }else{
							 int lastIndex = buildDeployData.getProdDeploymentAuditLogs().size() - 1;
							 buildDeployData.getProdDeploymentAuditLogs().get(lastIndex).setGitjobRunID(requestVo.getGitJobRunId());		
						 }
						 buildDeployentity.setData(buildDeployData);
						 buildDeployRepo.save(buildDeployentity);
					 }
					  response = "SUCCESS";
			}
			return response;
		} catch (Exception e) {
			log.info("Failed to get gitJobRunId with exception " + e.getMessage());
			return response;
		}
	}

	

	
}
