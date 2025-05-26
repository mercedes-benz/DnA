package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import com.daimler.data.dto.workspace.recipe.InitializeSoftwareLovVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.daimler.data.api.workspace.recipe.CodeServerRecipeApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.service.workspace.RecipeService;
import com.daimler.data.service.workspace.WorkspaceService;
import com.daimler.data.dto.workspace.recipe.InitializeRecipeVo;
import com.daimler.data.dto.workspace.recipe.RecipeCollectionVO;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.dto.workspace.recipe.InitializeRecipeLovVo;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import com.daimler.data.dto.workspace.CreatedByVO;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.dto.workspace.recipe.AdditionalPropertiesVO;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceCollectionVo;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.GitHubVo;
import com.daimler.data.dto.workspace.recipe.InitializeAdditionalServiceLovVo;

@RestController
@Api(value = "Recipe API", tags = { "code-server-recipe" })
@RequestMapping("/api")
@Slf4j
public class RecipeController implements CodeServerRecipeApi {

     @Autowired
	 private RecipeService service;

	 @Autowired
	 private UserStore userStore;

	 @Autowired
	private WorkspaceCustomRecipeRepo workspaceCustomRecipeRepo;

	@Override
	@ApiOperation(value = "Initialize/Create recipe for user in code-server-recipe.", nickname = "createRecipe", notes = "Create recipe for user in code-server with given password", response = RecipeVO.class, tags = {
			"code-server-recipe", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = RecipeVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<InitializeRecipeVo> createRecipe(
			@ApiParam(value = "Request Body that contains data required for intialize code server workbench for user", required = true) @Valid @RequestBody RecipeVO recipeRequestVO) {
		CreatedByVO currentUser = this.userStore.getVO();
		UserInfoVO currentUserVO = new UserInfoVO();
		BeanUtils.copyProperties(currentUser, currentUserVO);
		recipeRequestVO.setCreatedBy(currentUserVO);
		String id = recipeRequestVO.getId() != null ? recipeRequestVO.getId() : null;
		String recipeName = recipeRequestVO.getRecipeName() != null ? recipeRequestVO.getRecipeName() : null;
		//RecipeVO vo = service.getByRecipeName(recipeName);
		if(recipeRequestVO.isIsDeployEnabled() == null){
			recipeRequestVO.setIsDeployEnabled(false);
		}
		InitializeRecipeVo responseMessage = new InitializeRecipeVo();
		String name = service.getRecipeById(id)!= null ? service.getRecipeById(id).getRecipeName() : null;
		if (name == null) {
			// recipeRequestVO.setStatus("REQUESTED");
			GenericMessage softwareMessage = service.createOrValidateSoftwareTemplate(recipeRequestVO.getRepodetails(), recipeRequestVO.getSoftware());
			if(softwareMessage.getSuccess().equals("SUCCESS")) {
				RecipeVO recipeVO = service.createRecipe(recipeRequestVO);
				if (Objects.nonNull(recipeVO)) {
					responseMessage.setData(recipeVO);
					responseMessage.setSuccess("SUCCESS");
					log.info("The recipe has been created successfully with the name: "+recipeName);
					return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
				}
				responseMessage.setData(null);
				responseMessage.setSuccess("FAILED");
				log.info("The creation of a recipe failed due to an unspecified recipe name. "+recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			} else {
				responseMessage.setData(softwareMessage.getErrors());
				responseMessage.setSuccess("FAILED");
				log.info("The software creation process failed for create in the Git repository for the recipe."+recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
			}
		} else {
			responseMessage.setData("Recipe name already exist. Kindly give a unique name");
			responseMessage.setSuccess("CONFLICT");
			log.info("workspace {} already exists for User {} with name: {} ", recipeName);
			return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
		}
	}

	@Override
	@ApiOperation(value = "Update recipe details for a given id.", nickname = "updateRecipeById", notes = "Update recipe details for a recipe id.", response = InitializeRecipeVo.class, tags={ "code-server-recipe", })
	@ApiResponses(value = { 
		@ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeRecipeVo.class),
		@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
		@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
		@ApiResponse(code = 403, message = "Request is not authorized."),
		@ApiResponse(code = 405, message = "Method not allowed"),
		@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails/{id}",
		produces = { "application/json" }, 
		consumes = { "application/json" },
		method = RequestMethod.PUT)
	public ResponseEntity<InitializeRecipeVo> updateRecipeById(@ApiParam(value = "Request Body that contains data required for intialize code server workbench for user" ,required=true )  @Valid @RequestBody RecipeVO recipeRequestVO){
		CreatedByVO currentUser = this.userStore.getVO();
		UserInfoVO currentUserVO = new UserInfoVO();
		
		BeanUtils.copyProperties(currentUser, currentUserVO);
		recipeRequestVO.setCreatedBy(currentUserVO);
		String id = recipeRequestVO.getId() != null ? recipeRequestVO.getId() : null;
		String recipeName = recipeRequestVO.getRecipeName() != null ? recipeRequestVO.getRecipeName() : null;
		//RecipeVO vo = service.getByRecipeName(recipeName);
		if(recipeRequestVO.isIsDeployEnabled() == null){
		recipeRequestVO.setIsDeployEnabled(false);
		}
		InitializeRecipeVo responseMessage = new InitializeRecipeVo();
		String name = service.getRecipeById(id)!= null ? service.getRecipeById(id).getRecipeName() : null;
		if(name!=null) {
			GenericMessage softwareMessage = service.createOrValidateSoftwareTemplate(recipeRequestVO.getRepodetails(), recipeRequestVO.getSoftware());
			if(softwareMessage.getSuccess().equals("SUCCESS")) {
				RecipeVO recipeVO = service.updateRecipe(recipeRequestVO);
				if (Objects.nonNull(recipeVO)) {
					responseMessage.setData(recipeVO);
					responseMessage.setSuccess("SUCCESS");
					log.info("The recipe has been updated successfully with the name: "+recipeName);
					return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
				}
				responseMessage.setData(null);
				responseMessage.setSuccess("FAILED");
				log.info("The update of a recipe failed for recipe name. "+recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			} else {
				responseMessage.setData(softwareMessage.getErrors());
				responseMessage.setSuccess("FAILED");
				log.info("The software creation process failed for update in the Git repository for the recipe."+recipeName);
				return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	@ApiOperation(value = "Get all recipe managment workspaces for the user.", nickname = "getAllRecipes", notes = "Get all codeServer recipe managment workspaces for the user.", response = RecipeCollectionVO.class, tags = {
			"code-server-recipe", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = RecipeCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<RecipeCollectionVO> getAllRecipes(
			@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		RecipeCollectionVO recipeCollectionVO = new RecipeCollectionVO();

		if (offset == null) {
			offset = 0;
		}
		if (limit == null) {
			limit = 0;
		}
		CreatedByVO currentUser = this.userStore.getVO();
		String id = currentUser.getId();
		// if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
			List<RecipeVO> allRecipes = service.getAllRecipes(offset, limit,id);
			if (Objects.nonNull(allRecipes)) {
				for (RecipeVO recipe : allRecipes) {
					recipeCollectionVO.addDataItem(recipe);
				}
				recipeCollectionVO.setCount(allRecipes.size());
				// recipeCollectionVO.setSuccess("SUCCESS");
				return new ResponseEntity<>(recipeCollectionVO, HttpStatus.OK);
			} else {
				recipeCollectionVO.setData(null);
				recipeCollectionVO.setCount(null);
				recipeCollectionVO.setSuccess("FAILED");
				log.info("Failed to fetch all the recipe details for use "+userStore.getUserInfo().getId());
				return new ResponseEntity<>(recipeCollectionVO, HttpStatus.NO_CONTENT);
			}

		// } else {
		// 	recipeCollectionVO.setData(null);
		// 	recipeCollectionVO.setCount(null);
		// 	recipeCollectionVO.setSuccess("CONFLICT");
		// 	log.info(" user is unauthorized to access codespace" + userStore.getUserInfo().getId());
		// 	return new ResponseEntity<>(recipeCollectionVO, HttpStatus.UNAUTHORIZED);
		// }
	}

	@Override
	@ApiOperation(value = "Get workspace recipe details for a given Id.", nickname = "getById", notes = "Get workspace recipe details for a given Id.", response = RecipeVO.class, tags = {
			"code-server-recipe", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = RecipeVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/recipeDetails/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<InitializeRecipeVo> getRecipeById(
			@ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("id") String id) {
		
			InitializeRecipeVo responseMessage = new InitializeRecipeVo();
		// if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
			RecipeVO recipeVO = service.getRecipeById(id);
			if (Objects.nonNull(recipeVO) && Objects.nonNull(recipeVO.getRecipeName())) {
				responseMessage.setSuccess("SUCCESS");
				responseMessage.setData(recipeVO);
				return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			} else {
				responseMessage.setSuccess("FAILED");
				responseMessage.setData(null);
				log.info("No recipe found for given id: {} ", id);
				return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
			}
		// } else {

		// 	responseMessage.setData(null);
		// 	responseMessage.setSuccess("UNAUTHORIZED");
		// 	log.info(" user {} is unauthorized to access codespace" + userStore.getUserInfo().getId());
		// 	return new ResponseEntity<>(responseMessage, HttpStatus.UNAUTHORIZED);

		// }
	}

	 @ApiOperation(value = "Get all software details in recipe", nickname = "getAllsoftwareLov", notes = "Get all softwares details for recipe in codespace", response = InitializeSoftwareLovVo.class, tags={ "code-server-recipe", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = InitializeSoftwareLovVo.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request.", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/recipeDetails/softwareLov",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<InitializeSoftwareLovVo> getAllsoftwareLov()
	{
		InitializeSoftwareLovVo vo = new InitializeSoftwareLovVo();
		List<SoftwareCollection> allSoftwares = service.getAllsoftwareLov();
		if(Objects.nonNull(allSoftwares))
		{
			vo.setData(allSoftwares);
			vo.setSuccess("SUCCESS");
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} else {
				vo.setData(null);
				vo.setSuccess("FAILED");
				log.info("Failed to fetch all software details ");
				return new ResponseEntity<>(vo, HttpStatus.NO_CONTENT);
		}
	}

	@ApiOperation(value = "Get all Additional Services details in recipe", nickname = "getAllAdditionalServiceLov", notes = "Get all Additional Service details for recipe in codespace", response = InitializeAdditionalServiceLovVo.class, tags={ "code-server-recipe", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = InitializeAdditionalServiceLovVo.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request.", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/recipeDetails/additionalServiceLov",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<InitializeAdditionalServiceLovVo> getAllAdditionalServiceLov() {
		InitializeAdditionalServiceLovVo vo = new InitializeAdditionalServiceLovVo();
		List<AdditionalServiceLovVo> allServices = service.getAllAdditionalServiceLov();

		if(Objects.nonNull(allServices))
		{
			vo.setData(allServices);
			vo.setSuccess("SUCCESS");
			return new ResponseEntity<>(vo, HttpStatus.OK);
		} else {
			vo.setData(null);
			vo.setSuccess("FAILED");
			log.info("Failed to fetch Additional Service details ");
			return new ResponseEntity<>(vo, HttpStatus.NO_CONTENT);
		}
	}
	
	@ApiOperation(value = "Get all lov of recipes ", nickname = "getAllrecipeLov", notes = "Get all recipes in codespace", response = InitializeRecipeLovVo.class, tags={ "code-server-recipe", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = InitializeRecipeLovVo.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request.", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/recipeDetails/recipelov",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<InitializeRecipeLovVo> getAllrecipeLov()
	{
		CreatedByVO currentUser = this.userStore.getVO();
		String id = currentUser.getId();
		InitializeRecipeLovVo lov = new InitializeRecipeLovVo();
		List<RecipeLovVO> recipeDetails = service.getAllRecipeLov(id);
		if(Objects.nonNull(recipeDetails))
		{
			lov.setData(recipeDetails);
			lov.setSuccess("SUCCESS");
			return new ResponseEntity<>(lov, HttpStatus.OK);
		} else {
				lov.setData(null);
				lov.setSuccess("FAILED");
				log.info("Failed to fetch all software deatils ");
				return new ResponseEntity<>(lov, HttpStatus.NO_CONTENT);
		}
	}

	// @Override
    // @ApiOperation(value = "Get all recipes which are in requested and accepted state, waiting for processing.", nickname = "getAllRecipesWhichAreInRequestedAndAcceptedState", notes = "Get all recipes which are in requested and accepted state, waiting for processing", response = RecipeCollectionVO.class, tags={ "code-server-recipe", })
    // @ApiResponses(value = {
    //     @ApiResponse(code = 201, message = "Returns message of success or failure", response = RecipeCollectionVO.class),
    //     @ApiResponse(code = 204, message = "Fetch complete, no content found."),
    //     @ApiResponse(code = 400, message = "Bad request."),
    //     @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
    //     @ApiResponse(code = 403, message = "Request is not authorized."),
    //     @ApiResponse(code = 405, message = "Method not allowed"),
    //     @ApiResponse(code = 500, message = "Internal error") })
    // @RequestMapping(value = "/recipeDetails/recipesByStatus",
    //     produces = { "application/json" },
    //     consumes = { "application/json" },
    //     method = RequestMethod.GET)
    // public ResponseEntity<RecipeCollectionVO> getAllRecipesWhichAreInRequestedAndAcceptedState(@ApiParam(value = "page number from which listing of SecurityConfigs should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    // @ApiParam(value = "page size to limit the number of SecurityConfigs, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit){
    //     RecipeCollectionVO recipeCollectionVO = new RecipeCollectionVO();
    //     if (offset == null) {
    //         offset = 0;
    //     }
    //     if (limit == null) {
    //         limit = 0;
    //     }
    //     if (userStore.getUserInfo().hasCodespaceAdminAccess()) {
    //         List<RecipeVO> allRecipes = service.getAllRecipesWhichAreInRequestedAndAcceptedState(offset, limit);
    //         if (Objects.nonNull(allRecipes)) {
    //             recipeCollectionVO.data(allRecipes);
    //             recipeCollectionVO.setCount(allRecipes.size());
    //             recipeCollectionVO.setSuccess("SUCCESS");
    //             return new ResponseEntity<>(recipeCollectionVO, HttpStatus.OK);
    //         } else {
    //             recipeCollectionVO.setData(null);
    //             recipeCollectionVO.setCount(null);
    //             recipeCollectionVO.setSuccess("FAILED");
    //             log.info("Failed to fetch all the recipe details for user "+userStore.getUserInfo().getId());
    //             return new ResponseEntity<>(recipeCollectionVO, HttpStatus.NO_CONTENT);
    //         }
 
    //     } else {
    //         recipeCollectionVO.setData(null);
    //         recipeCollectionVO.setCount(null);
    //         recipeCollectionVO.setSuccess("FAILED");
    //         log.info(" user is unauthorized to access codespace" + userStore.getUserInfo().getId());
    //         return new ResponseEntity<>(recipeCollectionVO, HttpStatus.UNAUTHORIZED);
    //     }
 
       
    // }

	// @ApiOperation(value = "Accepting the changes to be added in access management , marking status as Accepted if success", nickname = "acceptRecipeInfo", notes = "Accepting the changes to be added in access management system", response = GenericMessage.class, tags={ "code-server-recipe", })
    // @ApiResponses(value = { 
    //     @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
    //     @ApiResponse(code = 204, message = "Fetch complete, no content found."),
    //     @ApiResponse(code = 400, message = "Bad request."),
    //     @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
    //     @ApiResponse(code = 403, message = "Request is not authorized."),
    //     @ApiResponse(code = 405, message = "Method not allowed"),
    //     @ApiResponse(code = 500, message = "Internal error") })
    // @RequestMapping(value = "/recipeDetails/{name}/accept",
    //     produces = { "application/json" }, 
    //     consumes = { "application/json" },
    //     method = RequestMethod.POST)
    // public ResponseEntity<GenericMessage> acceptRecipeInfo(@ApiParam(value = "Recipe name to be fetched",required=true) @PathVariable("name") String name)
	// {
	// 	GenericMessage responseMessage = new GenericMessage();
	// 	List<MessageDescription> errorMessage = new ArrayList<>();
	// 	if(!userStore.getUserInfo().hasCodespaceAdminAccess()) 
	// 	{
	// 		log.info(
	// 				"recipe details for workspace can be view/edit only by Owners, insufficient privileges.");
	// 		MessageDescription msg = new MessageDescription();
	// 		msg.setMessage("recipe details for workspace can be view/edit only by Owners");
	// 		errorMessage.add(msg);
	// 		responseMessage.setErrors(errorMessage);
	// 		return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
	// 	}
	// 	RecipeVO recipeVO = service.getByRecipeName(name);
	// 	if (Objects.nonNull(recipeVO) && Objects.nonNull(recipeVO.getRecipeName())) {
	// 		recipeVO.setStatus("ACCEPTED");
	// 		responseMessage = service.saveRecipeInfo(name);
	// 	}
	// 	else
	// 	{
	// 		log.info(
	// 				"recipe details for workspace can be view/edit only by Owners, insufficient privileges.");
	// 		MessageDescription msg = new MessageDescription();
	// 		msg.setMessage("recipe details for workspace can be view/edit only by Owners");
	// 		errorMessage.add(msg);
	// 		responseMessage.setErrors(errorMessage);
	// 		return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);

	// 	}
	// 	return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	// }

	// @ApiOperation(value = "Marking status after Publishing the changes added in access management system", nickname = "publishRecipeInfo", notes = "Marking status after Publishing the changes added in access management system", response = GenericMessage.class, tags={ "code-server-recipe", })
    // @ApiResponses(value = { 
    //     @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
    //     @ApiResponse(code = 204, message = "Fetch complete, no content found."),
    //     @ApiResponse(code = 400, message = "Bad request."),
    //     @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
    //     @ApiResponse(code = 403, message = "Request is not authorized."),
    //     @ApiResponse(code = 405, message = "Method not allowed"),
    //     @ApiResponse(code = 500, message = "Internal error") })
    // @RequestMapping(value = "/recipeDetails/{name}/publish",
    //     produces = { "application/json" }, 
    //     consumes = { "application/json" },
    //     method = RequestMethod.POST)
    // public ResponseEntity<GenericMessage> publishRecipeInfo(@ApiParam(value = "recipe name to be fetched",required=true) @PathVariable("name") String name)
	// {
	// 	GenericMessage responseMessage = new GenericMessage();
	// 	List<MessageDescription> errorMessage = new ArrayList<>();
	// 	if(!userStore.getUserInfo().hasCodespaceAdminAccess()) 
	// 	{
	// 		log.info(
	// 				"recipe details for workspace can be view/edit only by Owners, insufficient privileges.");
	// 		MessageDescription msg = new MessageDescription();
	// 		msg.setMessage("recipe details for workspace can be view/edit only by Owners");
	// 		errorMessage.add(msg);
	// 		responseMessage.setErrors(errorMessage);
	// 		return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
	// 	}
	// 	RecipeVO recipeVO = service.getByRecipeName(name);
	// 	if (Objects.nonNull(recipeVO) && Objects.nonNull(recipeVO.getRecipeName())) {
	// 		recipeVO.setStatus("PUBLISHED");
	// 		responseMessage = service.publishRecipeInfo(name);
	// 	}
	// 	else
	// 	{
	// 		log.info(
	// 				"recipe details for workspace can be view/edit only by Owners, insufficient privileges.");
	// 		MessageDescription msg = new MessageDescription();
	// 		msg.setMessage("recipe details for workspace can be view/edit only by Owners");
	// 		errorMessage.add(msg);
	// 		responseMessage.setErrors(errorMessage);
	// 		return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);

	// 	}
	// 	return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	// }
    
	@Override
	@ApiOperation(value = "To validate GitHub Url and to check if user is collaborator", nickname = "validateGitHub", notes = "To validate GitHub Url and to check if user is collaborator", response = InitializeRecipeVo.class, tags={ "code-server-recipe", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeRecipeVo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/recipeDetails/validate",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
   	public ResponseEntity<GenericMessage> validateGitHub(@ApiParam(value = "Request Body that contains data required for intialize code server workbench for user" ,required=true )  @Valid @RequestBody GitHubVo gitHubUrl) {
			String gitUrl = gitHubUrl.getGitHubUrl();
			GenericMessage  genericMessage = service.validateGitHubUrl(gitUrl);
			if("FAILED".equals(genericMessage.getSuccess()))
			{
				return new ResponseEntity<>(genericMessage,HttpStatus.FORBIDDEN);
			}
		return new ResponseEntity<>(genericMessage,HttpStatus.OK);
	}

	@Override
    @ApiOperation(value = "Delete recipe with given Id.", nickname = "deleteRecipe", notes = "Delete recipe for a given identifier.", response = GenericMessage.class, tags={ "code-server-recipe", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/recipeDetails/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
		public ResponseEntity<GenericMessage> deleteRecipe(@ApiParam(value = "recipe to be deleted", required = true) @PathVariable("id") String id) {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser.getId();
			CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findById(id);
		
			if (entity != null && Objects.nonNull(entity)) {
				if (userId.equalsIgnoreCase(entity.getData().getCreatedBy().getId())) {
					GenericMessage genericMessage = service.deleteRecipe(id);
					return ResponseEntity.ok(genericMessage);
				} else {
					// not authorized
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GenericMessage("User not authorized to delete this recipe."));
				}
			} else {
				// not found
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericMessage("Recipe not found."));
			}
		}
}
