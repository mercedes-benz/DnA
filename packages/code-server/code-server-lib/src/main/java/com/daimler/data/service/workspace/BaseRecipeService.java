package com.daimler.data.service.workspace;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.daimler.data.db.repo.workspace.WorkspaceCustomSoftwareRepo;
import com.daimler.data.assembler.AdditionalServiceAssembler;
import com.daimler.data.assembler.RecipeAssembler;
import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.workspace.WorkspaceAdditionalServiceRepo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomAdditionalServiceRepo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomAdditionalServiceRepoImpl;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
import com.daimler.data.db.repo.workspace.WorkspaceRecipeRepository;
import com.daimler.data.db.repo.workspace.WorkspaceSoftwareRepository;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.db.json.CodeServerAdditionalService;
import com.daimler.data.db.json.CodeServerSoftware;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.assembler.SoftwareAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import java.util.UUID;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.workspace.recipe.AdditionalPropertiesVO;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.InitializeAdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.auth.UserStore.UserInfo;
import com.daimler.data.application.client.GitClient;
import com.daimler.data.service.ArgoCdService;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.workspace.CreatedByVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import org.springframework.http.HttpStatus;


@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseRecipeService implements RecipeService{

	@Autowired
	private WorkspaceRecipeRepository recipeJpaRepo;

	@Autowired
	private WorkspaceSoftwareRepository softwareJpaRepo;

	@Autowired
	private WorkspaceAdditionalServiceRepo serviceJpaRepo;

	@Autowired
	private RecipeAssembler recipeAssembler;

	@Autowired
	private WorkspaceCustomRecipeRepo workspaceCustomRecipeRepo;

	@Autowired
	private WorkspaceCustomSoftwareRepo workspaceCustomSoftwareRepo;

	@Autowired
	private WorkspaceCustomAdditionalServiceRepo workspaceCustomAdditionalServiceRepo;

	@Autowired
	private SoftwareAssembler softwareAssembler;

	@Autowired
	private AdditionalServiceAssembler additionalServiceAssembler;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private WorkspaceCustomAdditionalServiceRepo additionalServiceRepo;

	@Autowired
	 private UserStore userStore;

	@Autowired
	private GitClient gitClient;

	@Autowired
	private ArgoCdService argoCdService;

	@Value("${codeserver.recipe.software.filename}")
	private String gitFileName;

	@Value("${codeServer.git.ghe.pid}")
	private String configuredPid;

	@Value("${codeServer.git.ghe.pat}")
	private String ghePat;
	
	@Value("${codeServer.git.pat}")
	private String gitIPat;
    
	@Override
	@Transactional
	public List<RecipeVO> getAllRecipes(int offset, int limit,String id) {
		List<CodeServerRecipeNsql> entities = workspaceCustomRecipeRepo.findAllRecipe(offset, limit,id);
		return entities.stream().map(n -> recipeAssembler.toVo(n)).collect(Collectors.toList());
	}

@Override
@Transactional
public RecipeVO createRecipe(RecipeVO recipeRequestVO) {

	SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
	String repoUrl = recipeRequestVO.getRepodetails();
	
	if (Boolean.TRUE.equals(recipeRequestVO.isIsPublic())
			&& repoUrl != null
			&& repoUrl.contains("ghe.com")) {
		throw new RuntimeException(
				"Public recipes are not allowed for GHE repositories. Please set recipe visibility to Private.");
	}

	if (Boolean.FALSE.equals(recipeRequestVO.isIsPublic())
			&& repoUrl != null
			&& repoUrl.contains("ghe.com")) {

		String cleanUrl = repoUrl.replaceAll("\\.git$", "");
		String[] parts = cleanUrl.split("/");

		if (parts.length < 2) {
			throw new RuntimeException("Invalid GHE repository URL");
		}

		String repoName = parts[parts.length - 1];
		String orgName = parts[parts.length - 2];
		String gitUrl = cleanUrl.substring(0, cleanUrl.lastIndexOf("/" + orgName));

		HttpStatus status = gitClient.validateGitUserWithPid(
				gitUrl,
				repoName,
				orgName,
				configuredPid,
				ghePat);

		if (!status.is2xxSuccessful()) {
			throw new RuntimeException(
					"Unable to access GHE repository. Please verify PID access.");
		}
	}

	// Register repository with ArgoCD when deployment is enabled
	if (Boolean.TRUE.equals(recipeRequestVO.isIsDeployEnabled()) && repoUrl != null) {
		registerRepoWithArgoCD(repoUrl);
	}

	CodeServerRecipeNsql entity = recipeAssembler.toEntity(recipeRequestVO);
	CodeServerRecipeNsql savedEntity = saveEntity(isoFormat, entity, new CodeServerRecipeNsql());
	return recipeAssembler.toVo(savedEntity);
}

@Override
@Transactional
public RecipeVO updateRecipe(RecipeVO recipeRequestVO) {

	SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");

	String repoUrl = recipeRequestVO.getRepodetails();
	
	if (Boolean.TRUE.equals(recipeRequestVO.isIsPublic())
			&& repoUrl != null
			&& repoUrl.contains("ghe.com")) {

		String cleanUrl = repoUrl.replaceAll("\\.git$", "");
		String[] parts = cleanUrl.split("/");

		if (parts.length < 2) {
            throw new RuntimeException("Invalid GHE repository URL");
        }

        String repoName = parts[parts.length - 1];
		String orgName = parts[parts.length - 2];
		String gitUrl = cleanUrl.substring(0, cleanUrl.lastIndexOf("/" + orgName));

		HttpStatus status = gitClient.validateGitUserWithPid(
				gitUrl,
				repoName,
				orgName,
				configuredPid,
				ghePat);

		if (!status.is2xxSuccessful()) {
			throw new RuntimeException(
					"Unable to access GHE repository. Please verify PID access.");
		}
	}

	// Register repository with ArgoCD when deployment is enabled
	if (Boolean.TRUE.equals(recipeRequestVO.isIsDeployEnabled()) && repoUrl != null) {
		registerRepoWithArgoCD(repoUrl);
	}

	CodeServerRecipeNsql existing = workspaceCustomRecipeRepo.findByRecipeName(
			recipeRequestVO.getRecipeName());

	if (existing == null) {
		throw new RuntimeException("Recipe not found");
	}
	CodeServerRecipeNsql updatedEntity = recipeAssembler.toEntity(recipeRequestVO);
	existing.setData(updatedEntity.getData());
	CodeServerRecipeNsql savedEntity = saveEntity(isoFormat, existing, new CodeServerRecipeNsql());
	return recipeAssembler.toVo(savedEntity);
}

	private CodeServerRecipeNsql saveEntity(SimpleDateFormat isoFormat, CodeServerRecipeNsql entity,
			CodeServerRecipeNsql savedEntity) {
		try {
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			entity.getData().setCreatedOn(now);
			savedEntity = recipeJpaRepo.save(entity);
		} catch (Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}
		return savedEntity;
	}

	@Override
	public GenericMessage createOrValidateSoftwareTemplate(String gitHubUrl, List<String> softwares) {
		GenericMessage responseMessage = new GenericMessage();
		HttpStatus status = null;
		try {
			String repoName = null;
			String softwareFileName = null;
			String gitUrl = null;
			String repoOwner = null;
			String SHA = null;
			String encodedFileContent = null;
			StringBuffer fileContent =  new StringBuffer();
			if(gitHubUrl.contains(".git")) {
				gitHubUrl = gitHubUrl.replaceAll("\\.git$", "/");
			}
			String[] codespaceSplitValues = gitHubUrl.split("/");
			int length = codespaceSplitValues.length;
			repoName = codespaceSplitValues[length-1];
			repoOwner = codespaceSplitValues[length-2];
			gitUrl = gitHubUrl.replace("/"+repoOwner, "");
		gitUrl = gitUrl.replace("/"+repoName, "");
		
		String patToUse;

		if (gitHubUrl.contains("ghe.com")) {
			patToUse = ghePat;
			log.info("Using GHE PAT, token present: {}", ghePat != null && !ghePat.isEmpty());
		} else if (gitHubUrl.contains("git.i")) {
			patToUse = gitIPat;
			log.info("Using git.i PAT, token present: {}, token prefix: {}", 
				gitIPat != null && !gitIPat.isEmpty(),
				gitIPat != null && gitIPat.length() > 8 ? gitIPat.substring(0, 8) + "..." : "N/A");
		} else {
			patToUse = null;
			log.info("No specific PAT matched, using default");
		}

		JSONObject jsonResponse = gitClient.readFileFromGit(repoName, repoOwner, gitUrl, gitFileName, patToUse);
			if(jsonResponse !=null && jsonResponse.has("name") && jsonResponse.has("content")) {
				softwareFileName  = jsonResponse.getString("name");
				SHA =  jsonResponse.has("sha")? jsonResponse.getString("sha") : null;
				log.info("Retrieving a software's SHA was successfull from Git.");
			}
			for(String software: softwares) {
				String additionalProperties = workspaceCustomRecipeRepo.findBySoftwareName(software);
				fileContent.append(additionalProperties);
			}
			if(fileContent.toString().contains("dotnet")){
				fileContent.append("\ncode-server --install-extension ms-dotnettools.vscode-dotnet-runtime\ncode-server --install-extension aliasadidev.nugetpackagemanagergui");
			}
			fileContent.append("\ncode-server --install-extension mtxr.sqltools-driver-pg\ncode-server --install-extension mtxr.sqltools\ncode-server --install-extension cweijan.vscode-database-client2\ncode-server --install-extension cweijan.vscode-redis-client\n");
		encodedFileContent = Base64.getEncoder().encodeToString(fileContent.toString().getBytes());
		if( encodedFileContent != null) {
			status = gitClient.createOrValidateSoftwareInGit(repoName, repoOwner, SHA, gitUrl, encodedFileContent, patToUse);
			if(status.is2xxSuccessful()) {
				log.info("Software creation was successfull in Git.");
				responseMessage.setSuccess("SUCCESS");
				return responseMessage;
				}
			}
			else {
				responseMessage = 	getMessageDescrption("An error occurred while encoding the software file into the Git repository.","FAILED");
				return responseMessage;
			}
			log.info("Software creation failed in Git.");
			responseMessage.setSuccess("FAILED");
			return responseMessage;
	} catch (Exception e) {
    log.error("Exception in createOrValidateSoftwareTemplate: {}", e.getMessage(), e);

    if (gitHubUrl != null
        && e.getMessage() != null
        && e.getMessage().contains("Branch protection")) {

        if (gitHubUrl.contains("ghe.com")) {

            responseMessage = getMessageDescrption(
                "Your GHE repository main branch is protected. "
              + "Please allow bypass in branch protection rules "
              + "or use a non-protected branch.",
                "FAILED");

        } else if (gitHubUrl.contains("git.i")) {

            responseMessage = getMessageDescrption(
                "Your git.i repository main branch is protected. "
              + "Please create a feature branch or update branch protection rules.",
                "FAILED");

        } else {
            responseMessage = getMessageDescrption(
                "Your repository branch is protected. "
              + "Please update branch protection settings.",
                "FAILED");
        }

    } else if (e.getMessage() != null && e.getMessage().contains("pull request")) {

        responseMessage = getMessageDescrption(
            "Conflict in Git while creating Software File",
            "FAILED");

    } else {

        responseMessage = getMessageDescrption(
            "An unexpected error occurred while uploading a software file to the Git repository.",
            "FAILED");
    }

    responseMessage.setSuccess("FAILED");
    return responseMessage;
}
	}

	
	private void registerRepoWithArgoCD(String repoUrl) {
		try {
			String token = argoCdService.getArgoToken();
			String repoGitUrl = repoUrl.endsWith(".git") ? repoUrl : repoUrl + ".git";
			argoCdService.registerRepository(token, repoGitUrl, configuredPid, ghePat);
			log.info("Repository registered with ArgoCD for deployment: {}", repoGitUrl);
		} catch (Exception e) {
			log.error("Failed to register repository with ArgoCD: {}", e.getMessage());
		}
	}

	private GenericMessage getMessageDescrption(String message, String statusMsg ) {
		GenericMessage responseMessage = new GenericMessage();
		MessageDescription msg = new MessageDescription();
		List<MessageDescription> errorMessage = new ArrayList<>();
		msg.setMessage(message);
		errorMessage.add(msg);
		responseMessage.addErrors(msg);
		responseMessage.setSuccess(statusMsg);
		return responseMessage;
	}

	@Override
public GenericMessage validateGitHubUrl(String gitHubUrl) {
    GenericMessage responseMessage = new GenericMessage();
    responseMessage.setSuccess("SUCCESS");

    try {
        if (gitHubUrl.endsWith(".git")) {
            gitHubUrl = gitHubUrl.substring(0, gitHubUrl.length() - 4);
        }

        String[] parts = gitHubUrl.split("/");
        int length = parts.length;

        String repoName = parts[length - 1];
        String applicationName = parts[length - 2];

        String gitUrl = gitHubUrl
                .replace("/" + repoName, "")
                .replace("/" + applicationName, "");

        HttpStatus status;
		boolean isGheRepo = gitHubUrl.contains("ghe.com");

		if (isGheRepo) {
			log.info("Validating PRIVATE GHE repo using PID");
			
			MessageDescription gheWarning = new MessageDescription();
			gheWarning.setMessage("GHE_REPO_DETECTED");
			responseMessage.addWarnings(gheWarning);
			
			status = gitClient.validateGitUserWithPid(
					gitUrl, repoName, applicationName, configuredPid, ghePat);
		} else {
			log.info("Non-GHE repo detected – skipping PID validation");
			return responseMessage;
		}
        if (!status.is2xxSuccessful()) {
            MessageDescription msg = new MessageDescription();
            msg.setMessage("Unexpected error occured while validating PID onboarding for the given git repo. Please try again.");
            responseMessage.setSuccess("FAILED");
            responseMessage.addErrors(msg);
            return responseMessage;
        }

    } catch (Exception e) {
        log.error("Validation failed", e);
        MessageDescription msg = new MessageDescription();
        msg.setMessage("Unexpected error occured while validating PID onboarding for the given git repo.");
        responseMessage.setSuccess("FAILED");
        responseMessage.addErrors(msg);
    }
    return responseMessage;
}

	@Override
	@Transactional
	public RecipeVO getRecipeById(String id) {
		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findById(id);
		return recipeAssembler.toVo(entity);
	}

	@Override
	@Transactional
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
	@Transactional
	public List<AdditionalServiceLovVo> getAllAdditionalServiceLov() {
		List<AdditionalServiceLovVo> additionalServiceLovVo = new ArrayList<>();
		try{
			List<CodeServerAdditionalServiceNsql> allServices = additionalServiceRepo.findAllServices(10, 0);
			if(!allServices.isEmpty() || allServices.size() >0)
			{
				additionalServiceLovVo = allServices.stream().map(n-> additionalServiceAssembler.toVo(n)).collect(Collectors.toList());
				log.info("Additional Services fetched successfully");
			}
			else
			{
				log.info("Additional Services not available");
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("failed while fetching additional properties",e);
		}
		return additionalServiceLovVo ;
	}

	@Override
	@Transactional
	public List<RecipeLovVO> getAllRecipeLov(String id)
	{
		List<CodeServerRecipeDto> publiclovDeatils = workspaceCustomRecipeRepo.getAllPublicRecipeLov();
		List<CodeServerRecipeDto> privatelovDetails =  workspaceCustomRecipeRepo.getAllPrivateRecipeLov(id);
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

	// @Override
	// @Transactional
	// public List<RecipeVO> getAllRecipesWhichAreInRequestedAndAcceptedState(int offset, int limit){
    //     List<CodeServerRecipeNsql> entities = workspaceCustomRecipeRepo.findAllRecipesWithRequestedAndAcceptedState(offset, limit);
    //     return entities.stream().map(n -> recipeAssembler.toVo(n)).collect(Collectors.toList());
    // }

	// @Override
	// @Transactional
	// public GenericMessage saveRecipeInfo(String name)
	// {
	// 	GenericMessage responseMessage = new GenericMessage();
	// 	if(name != null)
	// 	{
	// 		try
	// 		{
	// 			responseMessage = workspaceCustomRecipeRepo.updateRecipeInfo(name,"ACCEPTED");
	// 		}
	// 		catch(Exception e)
	// 		{
	// 			log.info("failed in recipe service while updating status to accept state",e.getMessage());
	// 			log.error("caught exception while changing status {}", e.getMessage());
	// 			MessageDescription msg = new MessageDescription();
	// 			List<MessageDescription> errorMessage = new ArrayList<>();
	// 			msg.setMessage("caught exception while saving recipe info");
	// 			errorMessage.add(msg);
	// 			responseMessage.addErrors(msg);
	// 			responseMessage.setSuccess("FAILED");
	// 			responseMessage.setErrors(errorMessage);
	// 		}
	
	// 		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(name);
	// 		RecipeVO data =  recipeAssembler.toVo(entity);
	// 		UserInfoVO vo = data.getCreatedBy();
	
	// 		CreatedByVO currentUser = this.userStore.getVO();
	// 		String userId = currentUser != null ? currentUser.getId() : null;
	
	// 		String resourceID = name;
	// 		List<String> teamMembers = new ArrayList<>();
	// 		List<String> teamMembersEmails = new ArrayList<>();
	// 		List<ChangeLogVO> changeLogs = new ArrayList<>();
	// 		UserInfoVO projectOwner = vo;
	// 		teamMembers.add(projectOwner.getId());
	// 		teamMembersEmails.add(projectOwner.getEmail());
	
	// 		String eventType = "Codespace-Recipe Status Update";
	// 		String message = ""; 
	
	// 		message = "Codespace Recipe " + data.getRecipeName() + " is accepted by Codespace Admin.";
	// 		kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
	// 	}
	// 	else
	// 	{
	// 		log.info("Failed in recipe service method during accept...");
	// 	}
	// 	return responseMessage;

	// }

	// @Override
	// @Transactional
	// public GenericMessage publishRecipeInfo(String name)
	// {
	// 	GenericMessage responseMessage = new GenericMessage();
	// 	if(name != null)
	// 	{
	// 		try
	// 		{
				
	// 			responseMessage = workspaceCustomRecipeRepo.updateRecipeInfo(name,"PUBLISHED");
	// 		}
	// 		catch(Exception e)
	// 		{
	// 			log.info("failed in recipe service while updating status to publish state",e.getMessage());
	// 			log.error("caught exception while changing status {}", e.getMessage());
	// 			MessageDescription msg = new MessageDescription();
	// 			List<MessageDescription> errorMessage = new ArrayList<>();
	// 			msg.setMessage("caught exception while saving recipe info");
	// 			errorMessage.add(msg);
	// 			responseMessage.addErrors(msg);
	// 			responseMessage.setSuccess("FAILED");
	// 			responseMessage.setErrors(errorMessage);
	// 		}
	
	// 		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(name);
	// 		RecipeVO data =  recipeAssembler.toVo(entity);
	// 		UserInfoVO vo = data.getCreatedBy();
	
	// 		CreatedByVO currentUser = this.userStore.getVO();
	// 		String userId = currentUser != null ? currentUser.getId() : null;
	
	// 		String resourceID = name;
	// 		List<String> teamMembers = new ArrayList<>();
	// 		List<String> teamMembersEmails = new ArrayList<>();
	// 		List<ChangeLogVO> changeLogs = new ArrayList<>();
	// 		UserInfoVO projectOwner = vo;
	// 		teamMembers.add(projectOwner.getId());
	// 		teamMembersEmails.add(projectOwner.getEmail());
	
	// 		String eventType = "Codespace-Recipe Status Update";
	// 		String message = ""; 
	
	// 		message = "Codespace Recipe " + data.getRecipeName() + " is published by Codespace Admin.";
	// 		kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
	// 	}
	// 	else
	// 	{
	// 		log.info("Failed in recipe service method during publish...");
	// 	}
	// 	return responseMessage;

	// }

	@Override
	public GenericMessage deleteRecipe(String id)
	{
		GenericMessage msg = new GenericMessage();
		CodeServerRecipeNsql recipe = workspaceCustomRecipeRepo.findById(id);
        if (recipe != null) {
			GenericMessage val =   workspaceCustomRecipeRepo.deleteRecipe(recipe);
            return new GenericMessage("Recipe deleted successfully");
        } else {
            return new GenericMessage("Recipe not found");
        }

	}

    @Override
	@Transactional
	public SoftwareCollection createSoftware(SoftwareCollection softwareRequestVO) {
		UserInfo currentUser = userStore.getUserInfo();
		Date currentTime = new Date();
		softwareRequestVO.setCreatedOn(currentTime);
    	CreatedByVO currentUserVO = userStore.getVO();
		UserInfoVO createdByUser = new UserInfoVO();
		BeanUtils.copyProperties(currentUserVO, createdByUser);
		softwareRequestVO.setCreatedBy(createdByUser);
		softwareRequestVO.setUpdatedOn(currentTime);
    	softwareRequestVO.setUpdatedBy(createdByUser);
		CodeServerSoftwareNsql softwareEntity = softwareAssembler.toEntity(softwareRequestVO);
		CodeServerSoftwareNsql savedSoftwareEntity = softwareJpaRepo.save(softwareEntity);
		return softwareAssembler.toVo(savedSoftwareEntity);
	}

	@Override
	@Transactional
	public SoftwareCollection getSoftwareByName(String softwareName) {
		CodeServerSoftwareNsql entity = workspaceCustomSoftwareRepo.findBySoftwareName(softwareName);
		if (entity!=null){
			return softwareAssembler.toVo(entity);
		}
		return null;
	}
    
	@Override
	@Transactional
	public SoftwareCollection getSoftwareById(String id) {
		CodeServerSoftwareNsql entity = workspaceCustomSoftwareRepo.findBySoftwareId(id);
		if (entity!=null){
			return softwareAssembler.toVo(entity);
		}
		return null;
	}

	@Override
	@Transactional
	public SoftwareCollection updateSoftware(SoftwareCollection softwareRequestVO) {
		UserInfo currentUser = userStore.getUserInfo();
		Date currentTime = new Date();
		CreatedByVO currentUserVO = userStore.getVO();
		UserInfoVO updatedByUser = new UserInfoVO();
		BeanUtils.copyProperties(currentUserVO, updatedByUser);
		softwareRequestVO.setUpdatedOn(currentTime);
    	softwareRequestVO.setUpdatedBy(updatedByUser);
		CodeServerSoftwareNsql softwareEntity = softwareAssembler.toEntity(softwareRequestVO);
		CodeServerSoftwareNsql savedSoftwareEntity = softwareJpaRepo.save(softwareEntity);
		return softwareAssembler.toVo(savedSoftwareEntity);
	}

	@Override
	public GenericMessage deleteSoftware(String id)
	{
		GenericMessage msg = new GenericMessage();
		CodeServerSoftwareNsql software = workspaceCustomSoftwareRepo.findBySoftwareId(id);
        if (software != null) {
			GenericMessage val =  workspaceCustomSoftwareRepo.deleteSoftware(software);
            return new GenericMessage("Software deleted successfully");
        } else {
            return new GenericMessage("Software not found");
        }

	}

	@Override
	@Transactional
	public AdditionalServiceLovVo createAdditionalService(AdditionalServiceLovVo addServiceRequestVO) {
		UserInfo currentUser = userStore.getUserInfo();
		Date currentTime = new Date();
		addServiceRequestVO.setCreatedOn(currentTime);
    	CreatedByVO currentUserVO = userStore.getVO();
		UserInfoVO createdByUser = new UserInfoVO();
		BeanUtils.copyProperties(currentUserVO, createdByUser);
		addServiceRequestVO.setCreatedBy(createdByUser);
    	addServiceRequestVO.setUpdatedOn(currentTime);
    	addServiceRequestVO.setUpdatedBy(createdByUser);
		CodeServerAdditionalServiceNsql serviceEntity = additionalServiceAssembler.toEntity(addServiceRequestVO);
		CodeServerAdditionalServiceNsql savedServiceEntity = serviceJpaRepo.save(serviceEntity);
		return additionalServiceAssembler.toVo(savedServiceEntity);
	}

	@Override
	@Transactional
	public AdditionalServiceLovVo updateAddService(AdditionalServiceLovVo addServiceRequestVO) {
		UserInfo currentUser = userStore.getUserInfo();
		Date currentTime = new Date();
		addServiceRequestVO.setUpdatedOn(currentTime);
    	CreatedByVO currentUserVO = userStore.getVO();
		UserInfoVO createdByUser = new UserInfoVO();
		BeanUtils.copyProperties(currentUserVO, createdByUser);
		addServiceRequestVO.setUpdatedBy(createdByUser);
		CodeServerAdditionalServiceNsql serviceEntity = additionalServiceAssembler.toEntity(addServiceRequestVO);
		CodeServerAdditionalServiceNsql savedServiceEntity = serviceJpaRepo.save(serviceEntity);
		return additionalServiceAssembler.toVo(savedServiceEntity);
	}

	@Override
	@Transactional
	public AdditionalServiceLovVo getServiceByName(String serviceName) {
		CodeServerAdditionalServiceNsql entity = additionalServiceRepo.findByAddServiceName(serviceName);
		if (entity!=null){
			return additionalServiceAssembler.toVo(entity);
		}
		return null;
	}

	@Override
	public GenericMessage deleteAddService(String id)
	{
		GenericMessage msg = new GenericMessage();
		CodeServerAdditionalServiceNsql addService = workspaceCustomAdditionalServiceRepo.findByAddServiceId(id);
        if (addService != null) {
			GenericMessage val =  workspaceCustomAdditionalServiceRepo.deleteAddService(addService);
            return new GenericMessage("Additional Services deleted successfully");
        } else {
            return new GenericMessage("Additional Services not found");
        }

	}
}
