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
//		GenericMessage responseMessage = new GenericMessage();
//		String status = "FAILED";
//		List<MessageDescription> warnings = new ArrayList<>();
//		List<MessageDescription> errors = new ArrayList<>();
//		try {
//			CodeServerWorkspaceNsql entity =  workspaceCustomRepository.findById(userId,id);
//			if(entity!=null && entity.getData()!=null && !"DELETED".equalsIgnoreCase(entity.getData().getStatus())) {
//				boolean undeployCheck = true;
//				GenericMessage undeployJobResponse = new GenericMessage();
//				if(entity.getData().getLastDeployedOn()!=null) {
//					undeployJobResponse = client.performWorkBenchActions("undeploy", entity.getData());
//					if(undeployJobResponse!=null && "SUCCESS".equalsIgnoreCase(undeployJobResponse.getSuccess()))
//						undeployCheck = true;
//					else
//						undeployCheck = false;
//				}
//					if(undeployCheck) {
//					GenericMessage deleteJobResponse = client.performWorkBenchActions("delete", entity.getData());
//					if(deleteJobResponse!=null && "SUCCESS".equalsIgnoreCase(deleteJobResponse.getSuccess())) {
//						entity.getData().setStatus("DELETE_REQUESTED");
//						jpaRepo.save(entity);
//						status = "SUCCESS";
//					}else {
//						errors.addAll(deleteJobResponse.getErrors());
//					}
//				}else {
//					errors.addAll(undeployJobResponse.getErrors());
//				}
//			}
//		}catch(Exception e) {
//			log.error("Error occured while deleting workspace {} in database with exception {} ", id, e.getMessage());
//			MessageDescription error = new MessageDescription();
//			error.setMessage("Failed while deleting workspace with exception " + e.getMessage());
//			errors.add(error);
//		}
//		responseMessage.setSuccess(status);
//		responseMessage.setWarnings(warnings);
//		responseMessage.setErrors(errors);
//		return responseMessage;
		return null;
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
			 WorkbenchManageDto ownerWorkbenchCreateDto = new WorkbenchManageDto();
			 ownerWorkbenchCreateDto.setRef(codeServerEnvRef);
			 WorkbenchManageInputDto ownerWorkbenchCreateInputsDto = new WorkbenchManageInputDto();
			 ownerWorkbenchCreateInputsDto.setAction(ConstantsUtility.CREATEACTION);
			 ownerWorkbenchCreateInputsDto.setEnvironment(entity.getData().getProjectDetails().getRecipeDetails().getEnvironment());
			 ownerWorkbenchCreateInputsDto.setIsCollaborator("true");
			 ownerWorkbenchCreateInputsDto.setPassword(password);
			 ownerWorkbenchCreateInputsDto.setPat(pat);
			 String repoName = entity.getData().getProjectDetails().getGitRepoName();
			 String repoNameWithOrg =  gitOrgName + "/" + repoName;
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
			//initialize repo
			String repoName = vo.getProjectDetails().getGitRepoName();
			HttpStatus createRepoStatus = gitClient.createRepo(repoName);
			if(!createRepoStatus.is2xxSuccessful()) {
				MessageDescription errMsg = new MessageDescription("Failed while initializing git repository " +repoName + " for codespace with status " + createRepoStatus.name() + " . Please verify inputs/permissions and retry.");
				errors.add(errMsg);
				responseVO.setErrors(errors);
				return responseVO;
			}
			// create repo success, adding collabs
			 List<String> gitUsers = new ArrayList<>();
			 UserInfoVO owner = vo.getProjectDetails().getProjectOwner();
			 gitUsers.add(owner.getGitUserName());
			 List<UserInfoVO> collabs = vo.getProjectDetails().getProjectCollaborators();
			 if(collabs!=null && !collabs.isEmpty()) {
				 List<String> collabsGitUserNames = collabs.stream().map(n->n.getGitUserName()).collect(Collectors.toList());
				 gitUsers.addAll(collabsGitUserNames);
			 }
			 for(String gitUser: gitUsers) {
				 HttpStatus addGitUser = gitClient.addUserToRepo(gitUser, repoName);
				 if(!addGitUser.is2xxSuccessful()) {
					 	HttpStatus deleteRepoStatus = gitClient.deleteRepo(repoName);
					 	if(!deleteRepoStatus.is2xxSuccessful()) {
					 		MessageDescription errMsg = new MessageDescription("Created git repository " +repoName + " successfully. Failed while adding " + gitUser  + " as collaborator with status " + addGitUser.name() + ". Deleted repository successfully, please retry");
							errors.add(errMsg);
							responseVO.setErrors(errors);
							return responseVO;
					 	}else {
							MessageDescription errMsg = new MessageDescription("Created git repository " +repoName + " successfully. Failed while adding " +  gitUser + " as collaborator with status " + addGitUser.name() + ". Unable to delete repository because of " + deleteRepoStatus.name() + ", please delete repository manually and retry");
							errors.add(errMsg);
							responseVO.setErrors(errors);
							return responseVO;
					 	}
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
			 String repoNameWithOrg =  gitOrgName + "/" + repoName;
			 ownerWorkbenchCreateInputsDto.setRepo(repoNameWithOrg);
			 String projectOwnerId = ownerEntity.getData().getWorkspaceOwner().getId();
			 ownerWorkbenchCreateInputsDto.setShortid(projectOwnerId);
			 ownerWorkbenchCreateInputsDto.setType(client.toDeployType(ownerEntity.getData().getProjectDetails().getRecipeDetails().getRecipeId()));
			 ownerWorkbenchCreateInputsDto.setWsid(ownerwsid);
			 ownerWorkbenchCreateDto.setInputs(ownerWorkbenchCreateInputsDto);
			 
			 GenericMessage createOwnerWSResponse = client.manageWorkBench(ownerWorkbenchCreateDto);
			 if(createOwnerWSResponse!=null) {
				 if(!"SUCCESS".equalsIgnoreCase(createOwnerWSResponse.getSuccess()) || 
						 	(createOwnerWSResponse.getErrors()!=null && !createOwnerWSResponse.getErrors().isEmpty()) ||
						 	(createOwnerWSResponse.getWarnings()!=null && !createOwnerWSResponse.getWarnings().isEmpty())) {
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
			 
			 SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			 Date now = isoFormat.parse(isoFormat.format(new Date()));
			 String projectName = ownerEntity.getData().getProjectDetails().getProjectName();
			 ownerEntity.getData().setIntiatedOn(now);
			 ownerEntity.getData().setStatus(ConstantsUtility.CREATEREQUESTEDSTATE);
			 ownerEntity.getData().setPassword(password);
			 ownerEntity.getData().setWorkspaceUrl("");//set url
			 ownerEntity.getData().getProjectDetails().setProjectCreatedOn(now);
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
			if(entity!=null) {
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
	public GenericMessage undeployWorkspace(String userId,String id,String environment, String branch) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			CodeServerWorkspaceNsql entity =  workspaceCustomRepository.findById(userId,id);
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
					workspaceCustomRepository.updateDeploymentDetails(status, environmentJsonbName, deploymentDetails);
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
			entity.getData().setStatus(latestStatus);
			String workspaceOwner = entity.getData().getWorkspaceOwner().getId();
			String workspaceName = entity.getData().getWorkspaceId();
			String defaultRecipeId = RecipeIdEnum.DEFAULT.name();
			String projectRecipe = entity.getData().getProjectDetails().getRecipeDetails().getRecipeId();
			String projectOwner = entity.getData().getProjectDetails().getProjectOwner().getId();
			if(isCreateDeleteStatuses) {
				if("CREATED".equalsIgnoreCase(latestStatus)) {
					String workspaceUrl = codeServerBaseUri+"/"+workspaceName+"/?folder=/home/coder";
					if(!defaultRecipeId.equalsIgnoreCase(projectRecipe))
						workspaceUrl += "/app";
					entity.getData().setWorkspaceUrl(workspaceUrl);
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
				 String deploymentUrl = codeServerBaseUri+"/"+projectOwnerWsId+"/"+ targetEnv +"/api/swagger-ui.html";
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
					deploymentDetails.setDeploymentUrl("");
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
