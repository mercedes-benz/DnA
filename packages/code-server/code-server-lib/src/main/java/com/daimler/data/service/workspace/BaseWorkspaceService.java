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
import java.util.List;
import java.util.stream.Collectors;

import com.daimler.data.dto.workspace.CreatedByVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.CodeServerClient;
import com.daimler.data.application.client.GitClient;
import com.daimler.data.assembler.WorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.db.repo.workspace.WorkspaceRepository;
import com.daimler.data.dto.DeploymentManageDto;
import com.daimler.data.dto.DeploymentManageInputDto;
import com.daimler.data.dto.WorkbenchManageDto;
import com.daimler.data.dto.WorkbenchManageInputDto;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseWorkspaceService implements WorkspaceService {
	
	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;
	
	@Value("${codeServer.base.uri}")
	private String codeServerBaseUri;
	
	@Value("${codeServer.git.orgname}")
	private String gitOrgName;
	
	@Value("${codeServer.git.orguri}")
	private String gitOrgUri;
	
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
	
	public BaseWorkspaceService() {
		super();
	}

	@Override
	@Transactional
	public GenericMessage deleteById(String userId,String id) {
		//1. undeploy if deployed and id is project owner id
		//2. delete repo 
		//3. for all workspaces under this project, trigger delete job
		//4. update all workspaces under this project, mark as deleted
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		CodeServerWorkspaceNsql entity =  workspaceCustomRepository.findById(userId,id);
		
		boolean isProjectOwner = false;
		String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		if(projectOwnerId.equalsIgnoreCase(userId)) {
			isProjectOwner = true;
		}
		
		if(isProjectOwner) {
			log.info("Delete requested by project owner {} " , userId);
			//undeploy int if present
			if(entity.getData().getProjectDetails().getIntDeploymentDetails().getDeploymentUrl()!=null 
					&& entity.getData().getProjectDetails().getIntDeploymentDetails().getLastDeployedBranch()!=null
					&& entity.getData().getProjectDetails().getIntDeploymentDetails().getLastDeploymentStatus()!=null){
				String branch = entity.getData().getProjectDetails().getIntDeploymentDetails().getLastDeployedBranch();
				DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				deployJobInputDto.setAction("undeploy");
				deployJobInputDto.setBranch(branch);
				deployJobInputDto.setEnvironment(entity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
				deployJobInputDto.setRepo(gitOrgUri+gitOrgName+"/"+entity.getData().getProjectDetails().getGitRepoName());
				String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				deployJobInputDto.setShortid(projectOwner);
				deployJobInputDto.setTarget_env("int");
				deployJobInputDto.setType(client.toDeployType(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()));
				String projectName = entity.getData().getProjectDetails().getProjectName();
				String projectOwnerWsId = entity.getData().getWorkspaceId();
				deployJobInputDto.setWsid(projectOwnerWsId);
				deploymentJobDto.setInputs(deployJobInputDto);
				deploymentJobDto.setRef(codeServerEnvRef);
				GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					log.info("Found deployment of branch {} on Staging environment, undeploy triggered successfully by user {}", branch, userId);
				}else {
					log.warn("Found deployment of branch {} on Staging environment, undeploy trigger failed by user {}", branch, userId);
					MessageDescription intUndeployTriggerFailed = new MessageDescription("Undeploy of branch "+branch+ " on Staging environment failed. Please retry deleting Project.");
					errors.add(intUndeployTriggerFailed);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					return responseMessage;
				}
			}
			//undeploy prod if present
			if(entity.getData().getProjectDetails().getProdDeploymentDetails().getDeploymentUrl()!=null 
					&& entity.getData().getProjectDetails().getProdDeploymentDetails().getLastDeployedBranch()!=null 
					&& entity.getData().getProjectDetails().getProdDeploymentDetails().getLastDeploymentStatus()!=null){
				String branch = entity.getData().getProjectDetails().getProdDeploymentDetails().getLastDeployedBranch();
				DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				deployJobInputDto.setAction("undeploy");
				deployJobInputDto.setBranch(branch);
				deployJobInputDto.setEnvironment(entity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
				deployJobInputDto.setRepo(gitOrgUri+gitOrgName+"/"+entity.getData().getProjectDetails().getGitRepoName());
				String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				deployJobInputDto.setShortid(projectOwner);
				deployJobInputDto.setTarget_env("prod");
				deployJobInputDto.setType(client.toDeployType(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()));
				String projectName = entity.getData().getProjectDetails().getProjectName();
				String projectOwnerWsId = entity.getData().getWorkspaceId();
				deployJobInputDto.setWsid(projectOwnerWsId);
				deploymentJobDto.setInputs(deployJobInputDto);
				deploymentJobDto.setRef(codeServerEnvRef);
				GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					log.info("Found deployment of branch {} on Production environment, undeploy triggered successfully by user {}", branch, userId);
				}else {
					log.warn("Found deployment of branch {} on Production environment, undeploy trigger failed by user {}", branch, userId);
					MessageDescription prodUndeployTriggerFailed = new MessageDescription("Undeploy of branch "+branch+ " on Production environment failed. Please retry deleting Project.");
					errors.add(prodUndeployTriggerFailed);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					return responseMessage;
				}
			}
		}
			
		String repoName = entity.getData().getProjectDetails().getGitRepoName();
		if(isProjectOwner) {
			//deleting repo
			HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
			if(!deleteRepoStatus.is2xxSuccessful()) {
				MessageDescription gitRepoDeleteWarning = new MessageDescription("Failed while deleting git repository " +repoName + " for project. Please delete manually.");
				warnings.add(gitRepoDeleteWarning);
			}else {
			 log.info("Repository {} deleted for the project by owner {} ", repoName, userId);
			}
		}else {
			//removing collab user from repo
			HttpStatus deleteUserFromRepoStatus = gitClient.deleteUserFromRepo(userId, repoName);
			if(!deleteUserFromRepoStatus.is2xxSuccessful()) {
				MessageDescription gitRepoDeleteWarning = new MessageDescription("Failed to remove user from git repository " +repoName + " for project. Please remove manually.");
				warnings.add(gitRepoDeleteWarning);
			}else {
			 log.info("User {} removed for the project repo {} successfully",userId, repoName);
			}
			
		}
			//trigger delete of all project members workspaces if user is owner otherwise trigger just for user individual workspace
			String projectName = entity.getData().getProjectDetails().getProjectName();
			String recipeType = client.toDeployType(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId());
			String environment = entity.getData().getProjectDetails().getRecipeDetails().getEnvironment();
			List<Object[]> records = new ArrayList<>();
			if(isProjectOwner) {
			records = workspaceCustomRepository.getWorkspaceIdsForProjectMembers(projectName);
			}else {
				Object[] collabRecord = {entity.getData().getWorkspaceId(),entity.getData().getWorkspaceOwner().getId()};
				records.add(collabRecord);
			}
			for(Object[] record: records) {
				 WorkbenchManageDto ownerWorkbenchDeleteDto = new WorkbenchManageDto();
				 ownerWorkbenchDeleteDto.setRef(codeServerEnvRef);
				 WorkbenchManageInputDto ownerWorkbenchDeleteInputsDto = new WorkbenchManageInputDto();
				 ownerWorkbenchDeleteInputsDto.setAction(ConstantsUtility.DELETEACTION);
				 ownerWorkbenchDeleteInputsDto.setEnvironment(environment);
				 ownerWorkbenchDeleteInputsDto.setIsCollaborator("false");
				 ownerWorkbenchDeleteInputsDto.setPassword("");
				 ownerWorkbenchDeleteInputsDto.setPat("");
				 String repoNameWithOrg =  gitOrgUri + gitOrgName + "/" + repoName;
				 ownerWorkbenchDeleteInputsDto.setRepo(repoNameWithOrg);
				 String workspaceUserId = record[1].toString();
				 ownerWorkbenchDeleteInputsDto.setShortid(workspaceUserId);
				 ownerWorkbenchDeleteInputsDto.setType(recipeType);
				 ownerWorkbenchDeleteInputsDto.setWsid(record[0].toString());
				 ownerWorkbenchDeleteDto.setInputs(ownerWorkbenchDeleteInputsDto);
				 GenericMessage deleteOwnerWSResponse = client.manageWorkBench(ownerWorkbenchDeleteDto);
				 warnings.addAll(deleteOwnerWSResponse.getErrors());
				 warnings.addAll(deleteOwnerWSResponse.getWarnings());
			}
			//update all workspaces for the project to deleted state in db if user is projectOwner otherwise change state to deleted only for individual workspace
			if(isProjectOwner) {
				workspaceCustomRepository.updateDeletedStatusForProject(projectName);
			}else {
				entity.getData().setStatus("DELETED");

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
				workspaceCustomRepository.updateCollaboratorDetails(projectName, removeUser, true);
				jpaRepo.save(entity);
			}
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
	}
	
	@Override
	@Transactional
	public InitializeWorkspaceResponseVO initiateWorkspace(CodeServerWorkspaceVO vo, String pat, String password) {
		InitializeWorkspaceResponseVO responseVO = new InitializeWorkspaceResponseVO();
		responseVO.setData(null);
		responseVO.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			
			CodeServerWorkspaceNsql entity = workspaceAssembler.toEntity(vo);
			
			//validate user pat 
			HttpStatus validateUserPatstatus = gitClient.validateGitPat(entity.getData().getGitUserName(),pat);
			if(!validateUserPatstatus.is2xxSuccessful()) {
				MessageDescription errMsg = new MessageDescription("Invalid GitHub Personal Access Token provided. Please verify and retry.");
				errors.add(errMsg);
				responseVO.setErrors(errors);
				return responseVO;
			}
			
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
			 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
			 ownerWorkbenchCreateInputsDto.setAction(ConstantsUtility.CREATEACTION);
			 ownerWorkbenchCreateInputsDto.setEnvironment(entity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
			 ownerWorkbenchCreateInputsDto.setIsCollaborator("true");
			 ownerWorkbenchCreateInputsDto.setPassword(password);
			 ownerWorkbenchCreateInputsDto.setPat(pat);
			 String repoName = entity.getData().getProjectDetails().getGitRepoName();
			 String repoNameWithOrg =  gitOrgUri + gitOrgName + "/" + repoName;
			 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg);
			 ownerWorkbenchCreateInputsDto.setShortid(entity.getData().getWorkspaceOwner().getId());
			 ownerWorkbenchCreateInputsDto.setType(client.toDeployType(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId()));
			 ownerWorkbenchCreateInputsDto.setWsid(entity.getData().getWorkspaceId());
			 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
			 
			 GenericMessage createOwnerWSResponse = client.manageWorkBench(ownerWorkbenchCreateDto);
			 if(createOwnerWSResponse!=null) {
				 if(!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) || 
						 	(createOwnerWSResponse.getErrors()!=null && !createOwnerWSResponse.getErrors().isEmpty()) ||
						 	(createOwnerWSResponse.getWarnings()!=null && !createOwnerWSResponse.getWarnings().isEmpty())) {
					 	HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
					 	if(!deleteRepoStatus.is2xxSuccessful()) {
					 		MessageDescription errMsg = new MessageDescription("Created git repository " +repoName + " successfully and added collaborator(s). Failed to initialize workbench. Deleted repository successfully, please retry");
							errors.add(errMsg);
							errors.addAll(createOwnerWSResponse.getErrors());
							warnings.addAll(createOwnerWSResponse.getWarnings());
							responseVO.setErrors(errors);
							responseVO.setWarnings(warnings);
							return responseVO;
					 	}else {
							MessageDescription errMsg = new MessageDescription("Created git repository " + repoName + " successfully and added collaborator(s). Failed to initialize workbench. Unable to delete repository, please delete repository manually and retry");
							errors.add(errMsg);
							errors.addAll(createOwnerWSResponse.getErrors());
							warnings.addAll(createOwnerWSResponse.getWarnings());
							responseVO.setErrors(errors);
							responseVO.setWarnings(warnings);
							return responseVO;
					 	}
				 }
			 }
			 Date initatedOn = new Date();
			 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			 entity.getData().setIntiatedOn(isoFormat.parse(isoFormat.format(new Date())));
			 entity.getData().setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
			 jpaRepo.save(entity);
			 responseVO.setData(workspaceAssembler.toVo(entity));
			 responseVO.setErrors(new ArrayList<>());
			 responseVO.setWarnings(new ArrayList<>());
			 responseVO.setSuccess("SUCCESS");
			 return responseVO;
		}catch(Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed to initialize workbench with exception." + e.getMessage() + " Please retry.");
			errors.add(errMsg);
			responseVO.setErrors(errors);
			responseVO.setWarnings(warnings);
			return responseVO;
		}
	}
	
	

	@Override
	@Transactional
	public InitializeWorkspaceResponseVO createWorkspace(CodeServerWorkspaceVO vo, String pat, String password) {
		InitializeWorkspaceResponseVO responseVO = new InitializeWorkspaceResponseVO();
		responseVO.setData(vo);
		responseVO.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			RecipeIdEnum recipe = vo.getProjectDetails().getRecipeDetails().getRecipeId();
			String recipeIdType =  client.toDeployType(recipe.toString());
			
			List<String> gitUsers = new ArrayList<>();
			UserInfoVO owner = vo.getProjectDetails().getProjectOwner();
			
			String repoName = vo.getProjectDetails().getGitRepoName();
			List<UserInfoVO> collabs = new ArrayList<>();
			if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
			//validate user pat 
			HttpStatus validateUserPatstatus = gitClient.validateGitPat(owner.getGitUserName(),pat);
			if(!validateUserPatstatus.is2xxSuccessful()) {
				MessageDescription errMsg = new MessageDescription("Invalid GitHub Personal Access Token provided. Please verify and retry.");
				errors.add(errMsg);
				responseVO.setErrors(errors);
				return responseVO;
			}
			
			//initialize repo
			repoName = vo.getProjectDetails().getGitRepoName();
			HttpStatus createRepoStatus = gitClient.createRepo(repoName);
			if(!createRepoStatus.is2xxSuccessful()) {
				MessageDescription errMsg = new MessageDescription("Failed while initializing git repository " +repoName + " for codespace  with status " + createRepoStatus.name()  + " . Please verify inputs/permissions/existing repositories and retry.");
				errors.add(errMsg);
				responseVO.setErrors(errors);
				return responseVO;
			}
			// create repo success, adding collabs
			 
			 gitUsers.add(owner.getGitUserName());
			 collabs = vo.getProjectDetails().getProjectCollaborators();
			 if(collabs!=null && !collabs.isEmpty()) {
				 List<String> collabsGitUserNames = collabs.stream().map(n->n.getGitUserName()).collect(Collectors.toList());
				 gitUsers.addAll(collabsGitUserNames);
			 }
			 for(String gitUser: gitUsers) {
				 HttpStatus addGitUser = gitClient.addUserToRepo(gitUser, repoName);
				 if(!addGitUser.is2xxSuccessful()) {
					 	HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
					 	log.info("Created git repository {} successfully. Failed while adding {} as collaborator with status {} and delete repo status as {} ",repoName,gitUser,addGitUser.name(),deleteRepoStatus.name());
					 	if(deleteRepoStatus.is2xxSuccessful()) {
					 		MessageDescription errMsg = new MessageDescription("Created git repository " +repoName + " successfully. Failed while adding " + gitUser  + " as collaborator . Please make " + gitUser + " is valid git user. Deleted repository successfully, please retry");
							errors.add(errMsg);
							responseVO.setErrors(errors);
							return responseVO;
					 	}else {
							MessageDescription errMsg = new MessageDescription("Created git repository " +repoName + " successfully. Failed while adding " +  gitUser + " as collaborator . Please make " + gitUser + " is valid git user. Unable to delete repository because of " + deleteRepoStatus.name() + ", please delete repository manually and retry");
							errors.add(errMsg);
							responseVO.setErrors(errors);
							return responseVO;
					 	}
				 }
			 }
		}else {
			repoName = vo.getProjectDetails().getRecipeDetails().getRepodetails();
			HttpStatus publicGitPatStatus = gitClient.validatePublicGitPat(vo.getGitUserName(), pat, repoName);
			if(!publicGitPatStatus.is2xxSuccessful()) {
				MessageDescription errMsg = new MessageDescription("Invalid Personal Access Token. Please verify and retry");
				errors.add(errMsg);
				responseVO.setErrors(errors);
				return responseVO;
			}
		}
			
			 vo.getProjectDetails().setGitRepoName(repoName);
			 //add records to db
			 CodeServerWorkspaceNsql ownerEntity = workspaceAssembler.toEntity(vo);
			 List<CodeServerWorkspaceNsql> entities = new ArrayList<>();
			 Long ownerwsseqid = jpaRepo.getNextWorkspaceSeqId();
			 String ownerwsid = ConstantsUtility.WORKSPACEPREFIX + String.valueOf(ownerwsseqid);
			 ownerEntity.getData().setWorkspaceId(ownerwsid);
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
			 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
			 ownerWorkbenchCreateInputsDto.setAction(ConstantsUtility.CREATEACTION);
			 ownerWorkbenchCreateInputsDto.setEnvironment(ownerEntity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
			 ownerWorkbenchCreateInputsDto.setIsCollaborator("false");
			 ownerWorkbenchCreateInputsDto.setPassword(password);
			 ownerWorkbenchCreateInputsDto.setPat(pat);
			 String repoNameWithOrg = "";
			 if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				 repoNameWithOrg =  gitOrgUri + gitOrgName + "/" + repoName;
			 }
			 else {
				 repoNameWithOrg =  vo.getProjectDetails().getRecipeDetails().getRepodetails();
			 }
			 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg);
			 String projectOwnerId = ownerEntity.getData().getWorkspaceOwner().getId();
			 ownerWorkbenchCreateInputsDto.setShortid(projectOwnerId);
			 ownerWorkbenchCreateInputsDto.setType(client.toDeployType(ownerEntity.getData().getProjectDetails().getRecipeDetails().getRecipeId()));
			 ownerWorkbenchCreateInputsDto.setWsid(ownerwsid);
			 ownerWorkbenchCreateInputsDto.setResource(vo.getProjectDetails().getRecipeDetails().getResource());
			 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
			 
			 GenericMessage createOwnerWSResponse = client.manageWorkBench(ownerWorkbenchCreateDto);
			 if(createOwnerWSResponse!=null) {
				 if(!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) || 
						 	(createOwnerWSResponse.getErrors()!=null && !createOwnerWSResponse.getErrors().isEmpty()) ||
						 	(createOwnerWSResponse.getWarnings()!=null && !createOwnerWSResponse.getWarnings().isEmpty())) {
					 if(!vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
					 	HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
					 	if(!deleteRepoStatus.is2xxSuccessful()) {
					 		MessageDescription errMsg = new MessageDescription("Created git repository " + repoName + " successfully and added collaborator(s). Failed to initialize workbench. Deleted repository successfully, please retry");
							errors.add(errMsg);
							errors.addAll(createOwnerWSResponse.getErrors());
							warnings.addAll(createOwnerWSResponse.getWarnings());
							responseVO.setErrors(errors);
							responseVO.setWarnings(warnings);
							return responseVO;
					 	}else {
							MessageDescription errMsg = new MessageDescription("Created git repository " + repoName + " successfully and added collaborator(s). Failed to initialize workbench. Unable to delete repository, please delete repository manually and retry");
							errors.add(errMsg);
							errors.addAll(createOwnerWSResponse.getErrors());
							warnings.addAll(createOwnerWSResponse.getWarnings());
							responseVO.setErrors(errors);
							responseVO.setWarnings(warnings);
							return responseVO;
					 	}
					 }	
				 }
			 }
			 
			 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			 Date now = isoFormat.parse(isoFormat.format(new Date()));
			 String projectName = ownerEntity.getData().getProjectDetails().getProjectName();
			 ownerEntity.getData().setIntiatedOn(now);
			 ownerEntity.getData().setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
			 ownerEntity.getData().setPassword(password);
			 ownerEntity.getData().setWorkspaceUrl("");//set url
			 ownerEntity.getData().getProjectDetails().setProjectCreatedOn(now);
			 if(vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
				 ownerEntity.getData().getProjectDetails().setProjectCollaborators(new ArrayList<>());
				 collabs = new ArrayList<>();
			 }
			 entities.add(ownerEntity);
			 if(collabs!=null && !collabs.isEmpty()) {
				 for(UserInfoVO collaborator : collabs) {
					 CodeServerWorkspaceNsql collabEntity = new CodeServerWorkspaceNsql();
					 CodeServerWorkspace collabData = new CodeServerWorkspace();
					 collabData.setDescription(ownerEntity.getData().getDescription());
					 collabData.setGitUserName(collaborator.getGitUserName());
					 collabData.setIntiatedOn(null);
					 collabData.setPassword("");
					 collabData.setProjectDetails(ownerEntity.getData().getProjectDetails());
					 collabData.setStatus(ConstantsUtility.COLLABREQUESTEDSTATE);
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
			 jpaRepo.saveAllAndFlush(entities);
			 CodeServerWorkspaceNsql savedOwnerEntity = workspaceCustomRepository.findbyProjectName(projectOwnerId, projectName);
			 CodeServerWorkspaceVO savedOwnerVO = workspaceAssembler.toVo(savedOwnerEntity);
			 responseVO.setErrors(new ArrayList<>());
			 responseVO.setWarnings(errors);
			 responseVO.setSuccess("SUCCESS");
			 responseVO.setData(savedOwnerVO);
			 return responseVO;
		}catch(Exception e) {
			e.printStackTrace();
			MessageDescription errMsg = new MessageDescription("Failed with exception {}. Please delete repository manually if created and retry create workspaces");
			errors.add(errMsg);
			responseVO.setErrors(errors);
			return responseVO;
		}
	}


	@Override
	public CodeServerWorkspaceVO getById(String userId,String id) {
		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findById(userId,id);
		return workspaceAssembler.toVo(entity);
	}


	@Override
	public List<CodeServerWorkspaceVO> getAll(String userId, int offset, int limit) {
		List<CodeServerWorkspaceNsql> entities = workspaceCustomRepository.findAll(userId,limit, offset);
		return entities.stream().map(n -> workspaceAssembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	public Integer getCount(String userId) {
		return workspaceCustomRepository.getCount(userId);
	}


	@Override
	public CodeServerWorkspaceVO getByUniqueliteral(String userId, String uniqueLiteral, String value) {
		if (value != null) {
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyUniqueLiteral(userId,uniqueLiteral, value);
			return workspaceAssembler.toVo(entity);
		} else
			return null;
	}

	
	@Override
	@Transactional
	public GenericMessage deployWorkspace(String userId,String id,String environment, String branch) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			CodeServerWorkspaceNsql entity =  workspaceCustomRepository.findById(userId,id);
			if(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")){
				log.error("Cannot deploy workspace for this project with id {} of recipe type - Public " + id);
				MessageDescription msg = new MessageDescription("Cannot deploy workspace for this project of recipe type - Public.");
				errors.add(msg);
			}
			if(entity!=null && !entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")) {
				DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				deployJobInputDto.setAction("deploy");
				deployJobInputDto.setBranch(branch);
				deployJobInputDto.setEnvironment(entity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
				deployJobInputDto.setRepo(gitOrgName+"/"+entity.getData().getProjectDetails().getGitRepoName());
				String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				deployJobInputDto.setShortid(projectOwner);
				deployJobInputDto.setTarget_env(environment);
				deployJobInputDto.setType(client.toDeployType(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()));
				String projectName = entity.getData().getProjectDetails().getProjectName();
				CodeServerWorkspaceNsql ownerEntity =  workspaceCustomRepository.findbyProjectName(projectOwner, projectName);
				if(ownerEntity==null || ownerEntity.getData()==null || ownerEntity.getData().getWorkspaceId()==null) {
					MessageDescription error = new MessageDescription();
					error.setMessage("Failed while deploying codeserver workspace project, couldnt fetch project owner details");
					errors.add(error);
					responseMessage.setErrors(errors);
					return responseMessage;
				}
				String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				deployJobInputDto.setWsid(projectOwnerWsId);
				deploymentJobDto.setInputs(deployJobInputDto);
				deploymentJobDto.setRef(codeServerEnvRef);
				GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					String environmentJsonbName = "intDeploymentDetails";
					CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					if(!"int".equalsIgnoreCase(environment)) {
						environmentJsonbName = "prodDeploymentDetails";
						deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					}
					deploymentDetails.setLastDeploymentStatus("DEPLOY_REQUESTED");;
					workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName, deploymentDetails);
					status = "SUCCESS";
				}else {
					status = "FAILED";
					errors.addAll(jobResponse.getErrors());
				}
			}
		}catch(Exception e) {
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
	public GenericMessage reassignOwner(CreatedByVO currentUser, CodeServerWorkspaceVO vo, UserInfoVO newOwnerDeatils) {
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

		if (isProjectOwner && !entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")) {
			UserInfo currentOwnerAsCollab = entity.getData().getProjectDetails().getProjectOwner();
			UserInfo newOwner = new UserInfo();
			BeanUtils.copyProperties(newOwnerDeatils, newOwner);

			try {
				// To update project owner.
				GenericMessage updateProjectOwnerDetails = workspaceCustomRepository.updateProjectOwnerDetails(projectName, newOwner);

				// To add current owner as collaborator.
				GenericMessage updateCollaboratorAsOwner = workspaceCustomRepository.updateCollaboratorDetails(projectName, currentOwnerAsCollab, false);

				// To remove new owner from collaborator.
				GenericMessage removeNewOwnerFromCollab = workspaceCustomRepository.updateCollaboratorDetails(projectName, newOwner, true);

				if ("FAILED".equalsIgnoreCase(updateProjectOwnerDetails.getSuccess())
						|| "FAILED".equalsIgnoreCase(updateCollaboratorAsOwner.getSuccess())
						|| "FAILED".equalsIgnoreCase(removeNewOwnerFromCollab.getSuccess())) {
					log.error("Failed to update project owner details");
					MessageDescription msg = new MessageDescription("Failed to update project owner details");
					errors.add(msg);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					return responseMessage;
				}
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
			MessageDescription msg = null;
			if(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")){
				log.error("Cannot reassign owners for this project {} of recipe type - Public " + projectName);
				 msg = new MessageDescription("Cannot reassign owners for this project of recipe type - Public");

			}else {
				log.error("Failed to remove collaborator details as requested user is not a project owner " + entity.getData().getWorkspaceId());
				 msg = new MessageDescription("Failed to remove collaborator details as requested user is not a project owner");
			}
			errors.add(msg);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			return responseMessage;
		}

		return responseMessage;
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

		String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		if (projectOwnerId.equalsIgnoreCase(currentUserUserId)) {
			isProjectOwner = true;
		}

		if (isProjectOwner) {
			String projectName = entity.getData().getProjectDetails().getProjectName();
			String technincalId = workspaceCustomRepository.getWorkspaceTechnicalId(removeUserId, projectName);
			if(technincalId.isEmpty() || technincalId == null) {
				log.error("No collaborator details found.");
				MessageDescription msg = new MessageDescription("No collaborator details found.");
				errors.add(msg);
				responseMessage.setErrors(errors);
				return responseMessage;
			}
			responseMessage = deleteById(removeUserId, technincalId);
		} else {
			log.error("Failed to remove collaborator details as requested user is not a project owner " + entity.getData().getWorkspaceId());
			MessageDescription msg = new MessageDescription("Failed to remove collaborator details as requested user is not a project owner");
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

		if(vo.getProjectDetails().getRecipeDetails().getRecipeId().name().toLowerCase().startsWith("public")) {
			log.error("Cannot add collaborator for this project {} of recipe type - Public " + entity.getData().getWorkspaceId());
			MessageDescription msg = new MessageDescription("Cannot add collaborator for projects of recipe type - Public.");
			errors.add(msg);
			responseMessage.setErrors(errors);
			return responseMessage;
		}
		String projectOwnerId = entity.getData().getProjectDetails().getProjectOwner().getId();
		if (projectOwnerId.equalsIgnoreCase(userId)) {
			isProjectOwner = true;
		}

		if (isProjectOwner) {
			try {
				String repoName = entity.getData().getProjectDetails().getGitRepoName();
				CodeServerWorkspaceNsql ownerEntity = workspaceAssembler.toEntity(vo);
				String projectName = entity.getData().getProjectDetails().getProjectName();

				UserInfo collaborator = new UserInfo();
				BeanUtils.copyProperties(userRequestDto, collaborator);

				String gitUser = userRequestDto.getGitUserName();
				HttpStatus addGitUser = gitClient.addUserToRepo(gitUser, repoName);
				if (addGitUser.is2xxSuccessful()) {
					CodeServerWorkspaceNsql collabEntity = new CodeServerWorkspaceNsql();
					CodeServerWorkspace collabData = new CodeServerWorkspace();
					collabData.setDescription(ownerEntity.getData().getDescription());
					collabData.setGitUserName(collaborator.getGitUserName());
					collabData.setIntiatedOn(null);
					collabData.setPassword("");
					collabData.setProjectDetails(ownerEntity.getData().getProjectDetails());
					collabData.setStatus(ConstantsUtility.COLLABREQUESTEDSTATE);
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
				} else {
					log.info("Failed while adding {} as collaborator with status {}", repoName, userRequestDto.getGitUserName(), addGitUser.name());
					MessageDescription errMsg = new MessageDescription("Failed while adding " + userRequestDto.getGitUserName() + " as collaborator . Please make " + userRequestDto.getGitUserName() + " is valid git user. Unable to delete repository because of " + ", please delete repository manually and retry");
					errors.add(errMsg);
					responseVO.setErrors(errors);
					return responseVO;
				}
			} catch (Exception e) {
				log.error("Failed to add collaborator details as requested with Exception: {} ", e.getMessage());
				MessageDescription msg = new MessageDescription("Failed to add collaborator details");
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}
		} else {
			log.error("Failed to add collaborator details as requested user is not a project owner " + entity.getData().getWorkspaceId());
			MessageDescription msg = new MessageDescription("Failed to add collaborator details as requested user is not a project owner");
			errors.add(msg);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			return responseMessage;
		}

		return responseMessage;
	}

	@Override
	@Transactional
	public GenericMessage undeployWorkspace(String userId,String id,String environment, String branch) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			CodeServerWorkspaceNsql entity =  workspaceCustomRepository.findById(userId,id);
			if(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase().startsWith("public")) {
				log.error("Cannot undeploy workspace for this project {} of recipe type - Public " + entity.getData().getWorkspaceId());
				MessageDescription msg = new MessageDescription("Cannot undeploy workspace for projects of recipe type - Public.");
				errors.add(msg);
				responseMessage.setErrors(errors);
				return responseMessage;
			}
			if(entity!=null) {
				DeploymentManageDto deploymentJobDto = new DeploymentManageDto();
				DeploymentManageInputDto deployJobInputDto = new DeploymentManageInputDto();
				deployJobInputDto.setAction("undeploy");
				deployJobInputDto.setBranch(branch);
				deployJobInputDto.setEnvironment(entity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
				deployJobInputDto.setRepo(gitOrgName+"/"+entity.getData().getProjectDetails().getGitRepoName());
				String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
				deployJobInputDto.setShortid(projectOwner);
				deployJobInputDto.setTarget_env(environment);
				deployJobInputDto.setType(client.toDeployType(entity.getData().getProjectDetails().getRecipeDetails().getRecipeId().toLowerCase()));
				String projectName = entity.getData().getProjectDetails().getProjectName();
				CodeServerWorkspaceNsql ownerEntity =  workspaceCustomRepository.findbyProjectName(projectOwner, projectName);
				if(ownerEntity==null || ownerEntity.getData()==null || ownerEntity.getData().getWorkspaceId()==null) {
					MessageDescription error = new MessageDescription();
					error.setMessage("Failed while deploying codeserver workspace project, couldnt fetch project owner details");
					errors.add(error);
					responseMessage.setErrors(errors);
					return responseMessage;
				}
				String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				deployJobInputDto.setWsid(projectOwnerWsId);
				deploymentJobDto.setInputs(deployJobInputDto);
				deploymentJobDto.setRef(codeServerEnvRef);
				GenericMessage jobResponse = client.manageDeployment(deploymentJobDto);
				if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					String environmentJsonbName = "intDeploymentDetails";
					CodeServerDeploymentDetails deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
					if(!"int".equalsIgnoreCase(environment)) {
						environmentJsonbName = "prodDeploymentDetails";
						deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
					}
					deploymentDetails.setLastDeploymentStatus("UNDEPLOY_REQUESTED");
					workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName, deploymentDetails);
					status = "SUCCESS";
				}else {
					status = "FAILED";
					errors.addAll(jobResponse.getErrors());
				}
			}
		}catch(Exception e) {
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
	public CodeServerWorkspaceVO getByProjectName(String userId,String projectName) {
		CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyProjectName(userId, projectName);
		return workspaceAssembler.toVo(entity);
	}

	@Override
	@Transactional
	public GenericMessage update(String userId, String name, String projectName, String existingStatus,
			String latestStatus, String targetEnv, String branch) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			String[] createDeleteStatuses = {"CREATED","CREATE_FAILED","DELETED","DELETE_REQUESTED"};
			boolean isCreateDeleteStatuses = Arrays.stream(createDeleteStatuses).anyMatch(latestStatus::equals);
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyUniqueLiteral(userId, "workspaceId", name);
			String workspaceOwner = entity.getData().getWorkspaceOwner().getId();
			String workspaceName = entity.getData().getWorkspaceId();
			String defaultRecipeId = RecipeIdEnum.DEFAULT.toString();
			String pythonRecipeId =  RecipeIdEnum.PY_FASTAPI.toString();
			String reactRecipeId = RecipeIdEnum.REACT.toString();
			String angularRecipeId =  RecipeIdEnum.ANGULAR.toString();
			String publicDnABackendRecipeId = RecipeIdEnum.PUBLIC_DNA_BACKEND.toString();
			String publicDnaFrontendRecipeId = RecipeIdEnum.PUBLIC_DNA_FRONTEND.toString();
			String publicDnaAirflowBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_AIRFLOW_BACKEND.toString();
			String publicDnaAuthenticatorBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_AUTHENTICATOR_BACKEND.toString();
			String publicDnaChronosBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_CHRONOS_BACKEND.toString();
			String publicDnaChronosMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_CHRONOS_MFE.toString();
			String publicDnaCodespaceBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_CODESPACE_BACKEND.toString();
			String publicDnaDataProductBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_DATA_PRODUCT_BACKEND.toString();
			String publicDnaDnaDataProductMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_DATA_PRODUCT_MFE.toString();
			String publicDnaDataikuBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_DATAIKU_BACKEND.toString();
			String publicDnaDssMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_DSS_MFE.toString();
			String publicDnaMalwareScannerRecipeId = RecipeIdEnum.PUBLIC_DNA_MALWARE_SCANNER.toString();
			String publicDnaModalRegistryBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_MODAL_REGISTRY_BACKEND.toString();
			String publicDnaNassRecipeId = RecipeIdEnum.PUBLIC_DNA_NASS.toString();
			String publicDnaReportBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_REPORT_BACKEND.toString();
			String publicDnaStorageBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_STORAGE_BACKEND.toString();
			String publicDnaStorageMfeRecipeId = RecipeIdEnum.PUBLIC_DNA_STORAGE_MFE.toString();
			String publicDnaTrinoBackendRecipeId = RecipeIdEnum.PUBLIC_DNA_TRINO_BACKEND.toString();
			
			String projectRecipe = entity.getData().getProjectDetails().getRecipeDetails().getRecipeId();
			String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
			log.info("projectRecipe: {}",projectRecipe);
			if(isCreateDeleteStatuses) {
				if("CREATED".equalsIgnoreCase(latestStatus)) {
					String workspaceUrl = codeServerBaseUri+"/"+workspaceName+"/?folder=/home/coder";
					if(!defaultRecipeId.equalsIgnoreCase(projectRecipe))
						workspaceUrl += "/app";
					if(projectRecipe.toLowerCase().startsWith("public")) {						
						switch(projectRecipe) {						
						case "public-dna-backend" : workspaceUrl = workspaceUrl + "/" + "packages/backend"; break;
						case "public-dna-frontend" : workspaceUrl = workspaceUrl + "/" + "packages/frontend"; break;			
						case "public-dna-report-backend" : workspaceUrl = workspaceUrl + "/" + "packages/dashboard-backend"; break;
						case "public-dna-codespace-backend" : workspaceUrl = workspaceUrl + "/" + "packages/code-server"; break;
						case "public-dna-malware-scanner" : workspaceUrl = workspaceUrl + "/" + "packages/malware-scanner"; break;
						case "public-dna-storage-mfe" : workspaceUrl = workspaceUrl + "/" + "packages/storage-mfe"; break;
						case "public-dna-storage-backend" : workspaceUrl = workspaceUrl + "/" + "packages/storage-backend"; break;
						case "public-dna-chronos-mfe" : workspaceUrl = workspaceUrl + "/" + "packages/chronos-mfe"; break;
						case "public-dna-chronos-backend" : workspaceUrl = workspaceUrl + "/" + "packages/chronos"; break;
						case "public-dna-data-product-mfe" : workspaceUrl = workspaceUrl + "/" + "packages/data-product-mfe"; break;
						case "public-dna-data-product-backend" : workspaceUrl = workspaceUrl + "/" + "packages/data-product-backend"; break;
						case "public-dna-dss-mfe" : workspaceUrl = workspaceUrl + "/" + "packages/dss-mfe"; break;
						case "public-dna-dataiku-backend" : workspaceUrl = workspaceUrl + "/" + "packages/dataiku-backend"; break;
						case "public-dna-airflow-backend" : workspaceUrl = workspaceUrl + "/" + "packages/airflow-backend"; break;
						case "public-dna-modal-registry-backend" : workspaceUrl = workspaceUrl + "/" + "packages/model-registry"; break;
						case "public-dna-trino-backend" : workspaceUrl = workspaceUrl + "/" + "packages/trino-backend"; break;
						case "public-dna-nass" : workspaceUrl = workspaceUrl + "/" + "packages/naas"; break;			
						case "public-dna-authenticator-backend" : workspaceUrl = workspaceUrl + "/" + "packages/authenticator-service"; break;
						
						}
					}					
					entity.getData().setWorkspaceUrl(workspaceUrl);
					entity.getData().setStatus(latestStatus);
				}
				workspaceCustomRepository.update(entity);
				log.info("updated status for user {} , workspace name {}, existingStatus {}, latestStatus {}",
						userId,name,existingStatus,latestStatus);
				status = "SUCCESS";
				responseMessage.setSuccess(status);
				responseMessage.setErrors(errors);
				responseMessage.setWarnings(warnings);
				return responseMessage;
			}else {
				if(projectRecipe.toLowerCase().startsWith("public")) {
					log.error("Cannot update public recipe types, deploy n undeploy is disabled");
					MessageDescription msg = new MessageDescription("Cannot update public recipe types, deploy n undeploy is disabled.");
					errors.add(msg);
					responseMessage.setErrors(errors);
					return responseMessage;
				}
				 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
				 Date now = isoFormat.parse(isoFormat.format(new Date()));
				 CodeServerWorkspaceNsql ownerEntity =  workspaceCustomRepository.findbyProjectName(projectOwner, projectName);
				 if(ownerEntity==null || ownerEntity.getData()==null || ownerEntity.getData().getWorkspaceId()==null) {
						MessageDescription error = new MessageDescription();
						error.setMessage("Failed while deploying codeserver workspace project, couldnt fetch project owner details");
						errors.add(error);
						responseMessage.setErrors(errors);
						return responseMessage;
				  }
				 String projectOwnerWsId = ownerEntity.getData().getWorkspaceId();
				 String deploymentUrl = "";
				 deploymentUrl = codeServerBaseUri+"/"+projectOwnerWsId+"/"+ targetEnv +"/api/swagger-ui.html";
				 if(pythonRecipeId.equalsIgnoreCase(projectRecipe)) {
					 deploymentUrl = codeServerBaseUri+"/"+projectOwnerWsId+"/"+ targetEnv +"/api/docs";
				 }
				 if(reactRecipeId.equalsIgnoreCase(projectRecipe) || angularRecipeId.equalsIgnoreCase(projectRecipe)) {
					 deploymentUrl = codeServerBaseUri+"/"+projectOwnerWsId+"/"+ targetEnv + "/";
				 }				 
				 String environmentJsonbName = "intDeploymentDetails";
				 CodeServerDeploymentDetails deploymentDetails = new CodeServerDeploymentDetails();
				 if("int".equalsIgnoreCase(targetEnv)) {
				 	deploymentDetails = entity.getData().getProjectDetails().getIntDeploymentDetails();
				 }else {
				 	environmentJsonbName = "prodDeploymentDetails";
					deploymentDetails = entity.getData().getProjectDetails().getProdDeploymentDetails();
				 }
				if("DEPLOYED".equalsIgnoreCase(latestStatus)) {
					String existingDeploymentUrl = deploymentDetails.getDeploymentUrl();
					if(existingDeploymentUrl==null || "".equalsIgnoreCase(existingDeploymentUrl) || "null".equalsIgnoreCase(existingDeploymentUrl)) {
						deploymentDetails.setDeploymentUrl(deploymentUrl);
					}
					deploymentDetails.setLastDeployedBranch(branch);
					deploymentDetails.setLastDeployedBy(entity.getData().getWorkspaceOwner());
					deploymentDetails.setLastDeployedOn(now);
					deploymentDetails.setLastDeploymentStatus(latestStatus);
					workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName, deploymentDetails);
					log.info("updated deployment details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							projectName,branch,targetEnv,latestStatus);
				}
				else if("UNDEPLOYED".equalsIgnoreCase(latestStatus)) {
					deploymentDetails.setDeploymentUrl(null);
					deploymentDetails.setLastDeploymentStatus(latestStatus);
					workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName, deploymentDetails);
					log.info("updated deployment details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							projectName,branch,targetEnv,latestStatus);
				} else {
					deploymentDetails.setLastDeploymentStatus(latestStatus);
					workspaceCustomRepository.updateDeploymentDetails(projectName, environmentJsonbName, deploymentDetails);
					log.info("updated deployment details successfully for projectName {} , branch {} , targetEnv {} and status {}",
							projectName,branch,targetEnv,latestStatus);
				}
			}
		}catch(Exception e) {
			log.error("caught exception while updating status {}",e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while deploying codeserver workspace project, couldnt fetch project owner details");
			errors.add(error);
			responseMessage.setErrors(errors);
			return responseMessage;
		}
		return null;
	}


	
}
