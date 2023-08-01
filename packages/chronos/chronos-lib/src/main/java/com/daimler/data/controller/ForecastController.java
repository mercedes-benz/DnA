package com.daimler.data.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.daimler.data.api.forecast.*;
import com.daimler.data.dto.forecast.*;
import com.daimler.data.dto.storage.BucketObjectDetailsDto;
import com.daimler.data.dto.storage.DeleteBucketResponseWrapperDto;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.auth.vault.VaultAuthClientImpl;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.storage.BucketObjectsCollectionWrapperDto;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.service.forecast.ForecastService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Forecast APIs")
@RequestMapping("/api")
@Slf4j
public class ForecastController implements ForecastRunsApi, ForecastProjectsApi, ForecastInputsApi, ForecastComparisonsApi, ForecastConfigFilesApi {

	@Autowired
	private ForecastService service;

	@Autowired
	private UserStore userStore;

	@Autowired
	private StorageServicesClient storageClient;

	@Autowired
	private VaultAuthClientImpl vaultAuthClient;

	@Value("${databricks.defaultConfigYml}")
	private String defaultConfigFolderPath;
	@Value("${databricks.runsDefaultPageSize}")
	private String runsDefaultPageSize;
	private static final String BUCKETS_PREFIX = "chronos-";
	private static final String INPUT_FILE_PREFIX = "/inputs/";
	private static final String COMPARISON_FOLDER_PREFIX = "/comparisons/";
	private static final String ACTUALS_FILENAME_PREFIX = "actuals";
	private static final String CONFIG_PATH = "/objects?prefix=configs/";
	private static final String BUCKET_TYPE = "chronos-core/";
	private static final String CONFIGS_FILE_PREFIX = "/configs/";

	private static final List<String> contentTypes = Arrays.asList("xlsx", "csv");
	private static final List<String> configContentTypes = Arrays.asList("yml", "yaml");

	private boolean isValidAttachment(String fileName) {
		boolean isValid = false;
		String extension = FilenameUtils.getExtension(fileName);
		if (contentTypes.contains(extension.toLowerCase())) {
			isValid = true;
		}
		return isValid;
	}

	private boolean isValidConfigFileAttachment(String fileName) {
		boolean isValid = false;
		String extension = FilenameUtils.getExtension(fileName);
		if (configContentTypes.contains(extension.toLowerCase())) {
			isValid = true;
		}
		return isValid;
	}

	@Override
	@ApiOperation(value = "Get forecasts config files", nickname = "getConfigFiles", notes = "Get forecasts config files", response = BucketObjectsCollectionWrapperDto.class, tags = {"forecast-projects",})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = BucketObjectsCollectionWrapperDto.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/forecasts/default-config/files",
			produces = {"application/json"},
			consumes = {"application/json"},
			method = RequestMethod.GET)
	public ResponseEntity<BucketObjectsCollectionWrapperDto> getConfigFiles(@ApiParam(value = "forecast project ID ") @Valid @RequestParam(value = "id", required = false) String id) {
		BucketObjectsCollectionWrapperDto collection = new BucketObjectsCollectionWrapperDto();
		List<BucketObjectDetailsDto> projectSpecificBucketCollection = new ArrayList<>();
		BucketObjectsCollectionWrapperDto chronosCoreSpecificcollection = new BucketObjectsCollectionWrapperDto();
		ForecastVO existingForecast = new ForecastVO();
		boolean isValidId = true;
		if (id != null) {
			existingForecast = service.getById(id);
			if (existingForecast == null || !id.equalsIgnoreCase(existingForecast.getId())) {
				log.warn("No forecast found with id {}, failed to fetch saved inputs for given forecast id", id);
				isValidId = false;
			}
			CreatedByVO requestUser = this.userStore.getVO();
			List<String> forecastProjectUsers = new ArrayList<>();
			forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
			List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
			if (collaborators != null && !collaborators.isEmpty()) {
				collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
			}
			if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
				if (!forecastProjectUsers.contains(requestUser.getId())) {
					log.warn("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs", id, existingForecast.getName());
					return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
				}
			}
		}
		collection = service.getBucketObjects(defaultConfigFolderPath,BUCKET_TYPE);
		if (isValidId && id != null) {
			projectSpecificBucketCollection = service.getProjectSpecificObjects(existingForecast.getConfigFiles());
			if (projectSpecificBucketCollection != null) {
				collection.getData().getBucketObjects().addAll(projectSpecificBucketCollection);
			}
		}
		return new ResponseEntity<>(collection, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Number of forecast-projects user.", nickname = "getNumberOfForecastProjects", notes = "Get number of forecast-projects. This endpoints will be used to get all valid available forecast-projects records.", response = TransparencyVO.class, tags={ "forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/transparency",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfForecastProjects() {
		try {
			Integer projectCount = service.getTotalCountOfForecastProjects();
			Integer userCount = service.getTotalCountOfForecastUsers();
			TransparencyVO transparencyVO = new TransparencyVO();
			transparencyVO.setUserCount(userCount);
			transparencyVO.setProjectCount(projectCount);
			log.info("Forecast users and project count fetched successfully");
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e){
			log.error("Failed to fetch Forecast count of users and project with exception {}", e.getMessage());
			return  new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get list of saved input files", nickname = "getInputFiles", notes = "Get list of saved input files", response = InputFilesCollectionVO.class, tags={ "forecast-inputs", })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = InputFilesCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/forecasts/{id}/inputs",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<InputFilesCollectionVO> getInputFiles(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id){
		InputFilesCollectionVO collection = new InputFilesCollectionVO();
		ForecastVO existingForecast = service.getById(id);
		if(existingForecast==null || !id.equalsIgnoreCase(existingForecast.getId())) {
			log.warn("No forecast found with id {}, failed to fetch saved inputs for given forecast id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if(collaborators!=null && !collaborators.isEmpty()) {
			collaborators.forEach(n-> forecastProjectUsers.add(n.getId()));
		}
		if(forecastProjectUsers!=null && !forecastProjectUsers.isEmpty()) {
			if(!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs",id,existingForecast.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
		}
		List<InputFileVO> files = existingForecast.getSavedInputs();
		HttpStatus responseStatus = HttpStatus.OK;
		if(files== null || files.isEmpty()) {
			responseStatus = HttpStatus.NO_CONTENT;
		}
		collection.setFiles(files);
		return new ResponseEntity<>(collection, responseStatus);
	}


	@Override
	@ApiOperation(value = "delete uploaded input file", nickname = "deleteSavedInputFile", notes = "delete uploaded input file by id", response = GenericMessage.class, tags = {
			"forecast-inputs",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/forecasts/{id}/inputs/{savedinputid}", produces = {"application/json"}, consumes = {
			"application/json"}, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteSavedInputFile(
			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@ApiParam(value = "saved Input file ID", required = true) @PathVariable("savedinputid") String sid) {

		GenericMessage responseMessage = new GenericMessage();
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		ForecastVO existingForecast = service.getById(id);
		if (existingForecast == null || !id.equalsIgnoreCase(existingForecast.getId())) {
			log.warn("No forecast found with id {}, failed to fetch saved inputs for given forecast id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs", id, existingForecast.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
		}
		boolean notFound = false;
		String fileName= "";
		List<InputFileVO> savedInputs = existingForecast.getSavedInputs();
		if (savedInputs != null && !savedInputs.isEmpty()) {
			List<String> fileIdList = savedInputs.stream().map(InputFileVO::getId).collect(Collectors.toList());
			if (fileIdList.contains(sid)) {
				notFound = true;
				Optional<InputFileVO> fileObject = savedInputs.stream().
						filter(x -> x.getId().equals(sid)).
						findFirst();
				InputFileVO file = fileObject.get();
				fileName = file.getName();
				savedInputs.remove(file);
			} else
				notFound = false;
		}
		if (!notFound) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		existingForecast.setSavedInputs(savedInputs);

		try {
			String prefix= "inputs/" + fileName;
			DeleteBucketResponseWrapperDto deleteFileResponse = storageClient.deleteFilePresent(existingForecast.getBucketName(),prefix);
			if(deleteFileResponse==null || (deleteFileResponse!=null && (deleteFileResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(deleteFileResponse.getStatus())))) {
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.setErrors(deleteFileResponse.getErrors());
				errorMessage.setWarnings(deleteFileResponse.getWarnings());
				return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
			}else if("SUCCESS".equalsIgnoreCase(deleteFileResponse.getStatus())) {
				log.info("Successfully deleted config file {} from storage bucket",fileName);
				ForecastVO updatedVO = service.create(existingForecast);
			}
		}
		catch (Exception e){
			List<MessageDescription> errors = new ArrayList<>();
			log.error("Failed while saving input files for project name {} project id {}",existingForecast.getName(), existingForecast.getId());
			MessageDescription msg = new MessageDescription("Failed while saving input files for project id" +existingForecast.getId());
			errors.add(msg);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		responseMessage.setSuccess("SUCCESS");
		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}



	@Override
	@ApiOperation(value = "Initialize/Create forecast project for user.", nickname = "createForecastProject", notes = "Create forecast project for user ", response = ForecastProjectResponseVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = ForecastProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ForecastProjectResponseVO> createForecastProject(
			@ApiParam(value = "Request Body that contains data required for intialize chronos project for user", required = true) @Valid @RequestBody ForecastProjectCreateRequestWrapperVO forecastRequestWrapperVO) {

		ForecastProjectResponseVO responseVO = new ForecastProjectResponseVO();
		ForecastProjectCreateRequestVO forecastProjectCreateVO = forecastRequestWrapperVO.getData();
		String name = forecastProjectCreateVO.getName();
		ForecastVO existingForecast = service.getByUniqueliteral("name", name);
		if(existingForecast!=null && existingForecast.getId()!=null) {
			log.error("Forecast project with this name {} already exists , failed to create forecast project", name);
			MessageDescription invalidMsg = new MessageDescription("Forecast project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(existingForecast);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		List<MessageDescription> errors = new ArrayList<>();
		if (forecastProjectCreateVO !=null && forecastProjectCreateVO.getCollaborators() != null && forecastProjectCreateVO.getCollaborators().size() > 0) {
			// To check if user is collaborator in the getAddCollaborators list.
			CollaboratorVO exstingcollaboratorisCreator = forecastProjectCreateVO.getCollaborators().stream().filter(x -> requestUser.getId().equalsIgnoreCase(x.getId())).findAny().orElse(null);
			if (exstingcollaboratorisCreator != null && exstingcollaboratorisCreator.getId() != null) {
				GenericMessage responseMessg = new GenericMessage();
				responseMessg.setSuccess("FAILED");
				MessageDescription errMsg = new MessageDescription(requestUser.getId() + " is already a Creator and can not be added as a collaborator");
				errors.add(errMsg);
				responseMessg.setErrors(errors);
				log.error(errMsg.getMessage());
				responseVO.setResponse(responseMessg);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}

			if (forecastProjectCreateVO.getCollaborators() != null) {
				// To check if user is already present in the existingForecast Collaborators list.
				Set<String> seenIds = new HashSet<>();
				GenericMessage responseMessg = new GenericMessage();
				for (CollaboratorVO collaborator : forecastProjectCreateVO.getCollaborators()) {
					if (seenIds.contains(collaborator.getId())) {
						// duplicate id found.
						responseMessg.setSuccess("FAILED");
						MessageDescription errMsg = new MessageDescription( "Duplicate entry for collaborator " + collaborator.getId());
						errors.add(errMsg);
						responseMessg.setErrors(errors);
						log.error(errMsg.getMessage());
						responseVO.setResponse(responseMessg);
						return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
					} else {
						seenIds.add(collaborator.getId());
					}
				}
			}
		}
		if (existingForecast == null) {
			boolean isBucketExists = service.isBucketExists(BUCKETS_PREFIX + name);
			if(isBucketExists) {
				log.error("Forecast project with this name {} already exists , failed to create forecast project", name);
				MessageDescription invalidMsg = new MessageDescription("Forecast project already exists with given name");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.CONFLICT.name());
				errorMessage.addWarnings(invalidMsg);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
		}
		ForecastVO forecastVO = new ForecastVO();
		forecastVO.setBucketName(BUCKETS_PREFIX + name);
		forecastVO.setCollaborators(forecastProjectCreateVO.getCollaborators());
		forecastVO.setCreatedBy(requestUser);
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		Date createdOn = new Date();
		try {
			createdOn = isoFormat.parse(isoFormat.format(createdOn));
		}catch(Exception e) {
			log.warn("Failed to format createdOn date to ISO format");
		}
		forecastVO.setCreatedOn(createdOn);
		forecastVO.setId(null);
		forecastVO.setName(forecastProjectCreateVO.getName());
		forecastVO.setRuns(null);
		forecastVO.setSavedInputs(null);
		try {
			ForecastVO createdVO = new ForecastVO();
			createdVO = service.createForecast(forecastVO);
			if (createdVO != null && createdVO.getId() != null) {
				GenericMessage successResponse = new GenericMessage();
				successResponse.setSuccess("SUCCESS");
				successResponse.setErrors(null);
				successResponse.setWarnings(null);
				responseVO.setData(createdVO);
				responseVO.setResponse(successResponse);
				log.info("ForecastProject {} created successfully", name);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else {
				GenericMessage failedResponse = new GenericMessage();
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				failedResponse.addErrors(message);
				failedResponse.setSuccess("FAILED");
				responseVO.setData(forecastVO);
				responseVO.setResponse(failedResponse);
				log.error("Forecast project {} , failed to create", name);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to save due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(forecastVO);
			responseVO.setResponse(failedResponse);
			log.error("Exception occurred:{} while creating forecast project {} ", e.getMessage(), name);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "API Key Generation for Forecast project.", nickname = "createForecastProject", notes = "API Key Generation for Forecast project.", response = ForecastProjectResponseVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = ForecastProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/apikey", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Override
	public ResponseEntity<ApiKeyResponseVO> getApikey(@ApiParam(value = "forecast project ID", required = true) @PathVariable("id") String id) {
		ApiKeyResponseVO responseVO = new ApiKeyResponseVO();
		ApiKeyVO response = new ApiKeyVO();
		ForecastVO existingForecast = service.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();

		// if existingForecast is null return not found.
		if (existingForecast == null) {
			responseMessage.setSuccess("FAILED");
			MessageDescription errMsg = new MessageDescription("forecast ID Not found!");
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			log.error("forecast ID Not found!");
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}

		response = service.getApiKey(id);
		responseMessage.setSuccess("SUCCESS");
		responseVO.setResponse(responseMessage);
		responseVO.setData(response);
		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "API Key Generation for Forecast project.", nickname = "createForecastProject", notes = "API Key Generation for Forecast project.", response = ForecastProjectResponseVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = ForecastProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/apikey", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<ApiKeyResponseVO> generateApikey(@ApiParam(value = "forecast project ID", required = true) @PathVariable("id") String id) {

		ApiKeyResponseVO responseVO = new ApiKeyResponseVO();
		ApiKeyVO response = new ApiKeyVO();
		ForecastVO existingForecast = service.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();

		// if existingForecast is null return not found.
		if (existingForecast == null) {
			responseMessage.setSuccess("FAILED");
			MessageDescription errMsg = new MessageDescription("forecast ID Not found!");
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			log.error("forecast ID Not found!");
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}

		responseMessage = service.generateApiKey(id);

		if (responseMessage != null && "FAILED".equalsIgnoreCase(responseMessage.getSuccess())) {
			log.error("Failed to generate an api key for " + id);
			responseMessage.setSuccess("FAILED");
			MessageDescription errMsg = new MessageDescription("Failed to generate an api key for " + id);
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// To get the API key after it's successfully gsenerated.
		response = service.getApiKey(id);
		responseMessage.setSuccess("SUCCESS");
		responseVO.setResponse(responseMessage);
		responseVO.setData(response);

		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "update forecasts details for a given Id.", nickname = "updateById", notes = "update forecasts details for a given Id.", response = ForecastVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = ForecastVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<ForecastProjectResponseVO> updateById(
			@ApiParam(value = "forecast project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating of collab details", required = true) @Valid @RequestBody ForecastProjectUpdateRequestVO forecastUpdateRequestVO) {
		ForecastVO existingForecast = service.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		ForecastProjectResponseVO responseVO = new ForecastProjectResponseVO();

		// if existingForecast is null return not found.
		if (existingForecast == null) {
			responseMessage.setSuccess("FAILED");
			MessageDescription errMsg = new MessageDescription("forecast ID Not found!");
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			log.error("forecast ID Not found!");
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}

		ForecastVO forecastVO = new ForecastVO();

		// if both AddCollaborators and RemoveCollaborators are empty
		// then return as Bad Request.
		if (forecastUpdateRequestVO.getAddCollaborators().size() == 0
				&& forecastUpdateRequestVO.getRemoveCollaborators().size() == 0) {
			responseMessage.setSuccess("FAILED");
			MessageDescription errMsg = new MessageDescription("Add and Remove Collaborators are list is empty!");
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			log.error("Add and Remove Collaborators are list is empty!");
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}

		if (forecastUpdateRequestVO.getApiKey() != null) {
			String apiKey = vaultAuthClient.getApiKeys(id);
			if (apiKey != null && apiKey != forecastUpdateRequestVO.getApiKey()) {
				GenericMessage updateApiKeyResponseMessage = vaultAuthClient.updateApiKey(id, forecastUpdateRequestVO.getApiKey());
				if (updateApiKeyResponseMessage != null && "FAILED".equalsIgnoreCase(updateApiKeyResponseMessage.getSuccess())) {
				 	responseVO.setResponse(updateApiKeyResponseMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		if (forecastUpdateRequestVO.getAddCollaborators() != null && forecastUpdateRequestVO.getAddCollaborators().size() > 0) {
			// To check if user is collaborator in the getAddCollaborators list.
			CollaboratorVO exstingcollaboratorisCreator = forecastUpdateRequestVO.getAddCollaborators().stream().filter(x -> existingForecast.getCreatedBy().getId().equalsIgnoreCase(x.getId())).findAny().orElse(null);
			if (exstingcollaboratorisCreator != null && exstingcollaboratorisCreator.getId() != null) {
				GenericMessage responseMessg = new GenericMessage();
				responseMessg.setSuccess("FAILED");
				MessageDescription errMsg = new MessageDescription(existingForecast.getCreatedBy().getId() + " is already a Creator and can not be added as a collaborator");
				errors.add(errMsg);
				responseMessg.setErrors(errors);
				log.error(errMsg.getMessage());
				responseVO.setResponse(responseMessg);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}

			if (existingForecast.getCollaborators() != null) {
				// To check if user is already present in the existingForecast Collaborators list.
				AtomicBoolean isCollabExits = new AtomicBoolean(false);
				forecastUpdateRequestVO.getAddCollaborators().stream().forEach(collab -> {
					CollaboratorVO exstingcollaborators = existingForecast.getCollaborators().stream().filter(x -> collab.getId().equalsIgnoreCase(x.getId())).findAny().orElse(null);
					GenericMessage responseMessg = new GenericMessage();
					if (exstingcollaborators != null && exstingcollaborators.getId() != null) {
						isCollabExits.set(true);
						responseMessg.setSuccess("FAILED");
						MessageDescription errMsg = new MessageDescription(exstingcollaborators.getId() + " collaborator is already present");
						errors.add(errMsg);
						responseMessg.setErrors(errors);
						log.error(errMsg.getMessage());
						responseVO.setResponse(responseMessg);
					}
				});
				if (isCollabExits.get()) {
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}
		}

		responseMessage = service.updateForecastByID(id, forecastUpdateRequestVO, existingForecast);
		 responseVO.setResponse(responseMessage);

		if (responseMessage != null && "FAILED".equalsIgnoreCase(responseMessage.getSuccess())) {
			if (responseMessage.getErrors()!=null) {
				if ( responseMessage.getErrors().get(0).getMessage().contains("User ID not found for deleting")) {
					return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
				}
			}
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// To get updated forecast after adding collaborator.
		ForecastVO updatedForecast = service.getById(id);
		responseVO.setData(updatedForecast);
		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "delete forecasts details for a given Id.", nickname = "deleteById", notes = "delete forecasts details for a given Id.", response = ForecastCollectionVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = ForecastCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteById(
			@ApiParam(value = "forecast project ID to be delete", required = true) @PathVariable("id") String id) {
		ForecastVO existingForecast = service.getById(id);
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();

		// if existingForecast is null return not found.
		if (existingForecast == null) {
			responseMessage.setSuccess("FAILED");
			MessageDescription errMsg = new MessageDescription("forecast ID Not found!");
			errors.add(errMsg);
			responseMessage.setErrors(errors);
			log.error("forecast ID Not found!");
			return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
		}

		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		responseMessage = service.deleteForecastByID(id);

		if (responseMessage != null && "SUCCESS".equalsIgnoreCase(responseMessage.getSuccess())) {
			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
		}

		return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	@ApiOperation(value = "Get all forecast projects for the user.", nickname = "getAll", notes = "Get all forecasts projects for the user.", response = ForecastCollectionVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ForecastCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ForecastCollectionVO> getAll(
			@ApiParam(value = "page number from which listing of forecasts should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of forecasts, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {

			ForecastCollectionVO collection = new ForecastCollectionVO();
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			CreatedByVO requestUser = this.userStore.getVO();
			String user = requestUser.getId();
			List<ForecastVO> records = service.getAll(limit, offset, user);
			Long count = service.getCount(user);
			HttpStatus responseCode = HttpStatus.NO_CONTENT;
			if(records!=null && !records.isEmpty()) {
				collection.setRecords(records);
				collection.setTotalCount(count.intValue());
				responseCode = HttpStatus.OK;
			}
		return new ResponseEntity<>(collection, responseCode);
	}

	@Override
	@ApiOperation(value = "Get forecasts details for a given Id.", nickname = "getById", notes = "Get forecasts details for a given Id.", response = ForecastVO.class, tags = {
			"forecast-projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = ForecastVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ForecastVO> getById(
			@ApiParam(value = "forecast project ID to be fetched", required = true) @PathVariable("id") String id){
		ForecastVO existingForecast = service.getById(id);
		if(existingForecast==null || !id.equalsIgnoreCase(existingForecast.getId())) {
			log.warn("No forecast found with id {}, failed to fetch saved inputs for given forecast id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		//remove deleted runs before returning
		List<RunVO> existingRuns = existingForecast.getRuns();
		if(existingRuns!=null && !existingRuns.isEmpty()) {
			List<RunVO> tempExistingRuns = new ArrayList<>(existingRuns);
			for (int i = 0; i < existingRuns.size(); i++) {
				RunVO details = existingRuns.get(i);
				if (details.isIsDeleted() != null) {
					boolean isDelete = details.isIsDeleted();
					if (isDelete) {
						tempExistingRuns.remove(details);
					}
				}

			}
			existingForecast.setRuns(tempExistingRuns);
		}
		List<ForecastComparisonVO> existingComparisons = existingForecast.getComparisons();
		if(existingComparisons!=null && !existingComparisons.isEmpty()) {
			List<ForecastComparisonVO> tempExistingComparisons = new ArrayList<>(existingComparisons);
			for(int i=0; i<tempExistingComparisons.size(); i++) {
				ForecastComparisonVO details= tempExistingComparisons.get(i);
				if(details.isIsDeleted()!= null) {
					boolean isDelete = details.isIsDeleted();
					if(isDelete) {
						tempExistingComparisons.remove(details);
					}
				}
			}
			existingForecast.setComparisons(tempExistingComparisons);
		}
    
		CreatedByVO requestUser = this.userStore.getVO();
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if(collaborators!=null && !collaborators.isEmpty()) {
			collaborators.forEach(n-> forecastProjectUsers.add(n.getId()));
		}
		if(forecastProjectUsers!=null && !forecastProjectUsers.isEmpty()) {
			if(!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs",id,existingForecast.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}else {
				return new ResponseEntity<>(existingForecast, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	@Override
	@ApiOperation(value = "delete particular run", nickname = "deleteRun", notes = "delete particular run based on id", response = GenericMessage.class, tags = {
			"forecast-runs", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/runs/{correlationid}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteRun(
			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@ApiParam(value = "DNA correlation Id for the run", required = true) @PathVariable("correlationid") String rid) {

		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		ForecastVO existingForecast = service.getById(id);
		boolean notFound = false;
		boolean notAuthorized = true;
		List<RunVO> runVOList = existingForecast.getRuns();
		if(runVOList!= null && !runVOList.isEmpty()) {
			for (RunVO run : runVOList) {
				if (rid.equalsIgnoreCase(run.getId())) {
					if(run.isIsDeleted() != null && run.isIsDeleted()==true){
						return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
					}
					notFound = true;
					List<String> forecastProjectUsers = new ArrayList<>();
					forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
					List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
					if (collaborators != null && !collaborators.isEmpty()) {
						collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
					}
					if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
						if (!forecastProjectUsers.contains(requestUser.getId())) {
							log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
							notAuthorized=false;

						}
					}
				}

			}
		}else
			notFound = false;
		if(!notFound) {
			 return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		if(!notAuthorized) {
			 GenericMessage responseMessage = new GenericMessage();
			 List<MessageDescription> errMsgs = new ArrayList<>();
			 MessageDescription errMsg = new MessageDescription("Only user of this project can delete the run. Unauthorized");
			 responseMessage.setSuccess("FAILED");
			 errMsgs.add(errMsg);
			 responseMessage.setErrors(errMsgs);
			 return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
		}
		GenericMessage responseMessage = service.deletRunByUUID(id,rid);
		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}

	@Override
	 @ApiOperation(value = "Get visualization data for specific run.", nickname = "getRunVisualizationData", notes = "Get visualization data for specific run.", response = RunVisualizationVO.class, tags={ "forecast-runs", })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = RunVisualizationVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/forecasts/{id}/runs/{correlationid}",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<RunVisualizationVO> getRunVisualizationData(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,@ApiParam(value = "DNA correlation Id for the run",required=true) @PathVariable("correlationid") String rid){
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		ForecastVO existingForecast = service.getById(id);
		boolean notFound = false;
		boolean notAuthorized = true;
		List<RunVO> runVOList = existingForecast.getRuns();
		if(runVOList!= null && !runVOList.isEmpty()) {
			for(RunVO run: runVOList) {
				if(rid.equalsIgnoreCase(run.getId())) {
					if(run.isIsDeleted() != null && run.isIsDeleted()==true){
						return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
					}
					notFound = true;
					if(!user.equalsIgnoreCase(run.getTriggeredBy())) {
						notAuthorized = false;
					}
				}
			}
		}else
			notFound = false;
		if(!notFound) {
			 return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if(collaborators!=null && !collaborators.isEmpty()) {
			collaborators.forEach(n-> forecastProjectUsers.add(n.getId()));
		}
		if(forecastProjectUsers!=null && !forecastProjectUsers.isEmpty()) {
			if(!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs",id,existingForecast.getName());
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
		}
		RunVisualizationVO visualizationData = service.getRunVisualizationsByUUID(id, rid);
		return new ResponseEntity<>(visualizationData, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Get all forecast projects for the user.", nickname = "getAllRunsForProject", notes = "Get all forecasts projects for the user.", response = ForecastRunCollectionVO.class, tags = {
			"forecast-runs", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ForecastRunCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/runs", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ForecastRunCollectionVO> getAllRunsForProject(
			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@ApiParam(value = "page number from which listing of forecasts should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of forecasts, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort runs by a given variable like runName, createdby, createdon, or status", allowableValues = "runName,createdOn,status,createdBy,inputFile") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort runs based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		ForecastRunCollectionVO collection = new ForecastRunCollectionVO();
		int defaultLimit = Integer.parseInt(runsDefaultPageSize);
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
		}
		if (sortBy == null || sortBy.trim().isEmpty())
			sortBy = "createdOn";
		if (sortOrder == null || sortOrder.trim().isEmpty()) {
			sortOrder = "desc";
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		ForecastVO existingForecast = service.getById(id);
		//validate user
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if(collaborators!=null && !collaborators.isEmpty()) {
			collaborators.forEach(n-> forecastProjectUsers.add(n.getId()));
		}
		if(forecastProjectUsers!=null && !forecastProjectUsers.isEmpty()) {
			if(!forecastProjectUsers.contains(requestUser.getId())) {
				log.error("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs",id,existingForecast.getName());
				return new ResponseEntity<>(collection, HttpStatus.FORBIDDEN);
			}else {
				Object[] runCollectionWrapper = service.getAllRunsForProject(limit, offset, existingForecast.getId(),sortBy,sortOrder);
				List<RunVO> records = (List<RunVO>) runCollectionWrapper[0];
				Long count = (Long) runCollectionWrapper[1];
				HttpStatus responseCode = HttpStatus.NO_CONTENT;
				if(records!=null && !records.isEmpty()) {
					collection.setRecords(records);
					collection.setTotalCount(count.intValue());
					responseCode = HttpStatus.OK;
				}
				return new ResponseEntity<>(collection, responseCode);
			}
		}
		return new ResponseEntity<>(collection, HttpStatus.NO_CONTENT);
	}



	@Override
	@ApiOperation(value = "Create new run for forecast project.", nickname = "createForecastRun", notes = "Create run for forecast project", response = ForecastRunResponseVO.class, tags={ "forecast-runs", })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = ForecastRunResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/forecasts/{id}/runs",
        produces = { "application/json" },
        consumes = { "multipart/form-data" },
        method = RequestMethod.POST)
    public ResponseEntity<ForecastRunResponseVO> createForecastRun(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Chronos default config yml", required=true, allowableValues="Default-Settings") @RequestParam(value="configurationFile", required=true)  String configurationFile,
    		@ApiParam(value = "frequency parameter.", required=true, allowableValues="Daily, Weekly, Monthly, Yearly, No_Frequency") @RequestParam(value="frequency", required=true)  String frequency,
    		@ApiParam(value = "Any number greater than 1", required=true) @RequestParam(value="forecastHorizon", required=true)  BigDecimal forecastHorizon,
    		@ApiParam(value = "The file to upload.") @Valid @RequestPart(value="file", required=false) MultipartFile file,
    		@ApiParam(value = "path of file in minio system, if not giving file in request part") @RequestParam(value="savedInputPath", required=false)  String savedInputPath,
    		@ApiParam(value = "flag whether to save file in request part to storage bucket for further runs") @RequestParam(value="saveRequestPart", required=false)  Boolean saveRequestPart,
    		@ApiParam(value = "name of the run sample. Example YYYY-MM-DD_run_topic") @RequestParam(value="runName", required=false)  String runName,
    		@ApiParam(value = "Levels Of Hierarchy number between 2 to 20 Or null") @RequestParam(value="hierarchy", required=false)  String hierarchy,
    		@ApiParam(value = "Comments for the run") @RequestParam(value="comment", required=false)  String comment,
    		@ApiParam(value = "If true, then run on Powerful Machines") @RequestParam(value="runOnPowerfulMachines", required=false)  Boolean runOnPowerfulMachines,
            @ApiParam(value = "Text field to denote Chronos Version") @RequestParam(value="infotext", required=false)  String infotext){
			ForecastRunResponseVO responseVO = new ForecastRunResponseVO();
			GenericMessage responseMessage = new GenericMessage();
			ForecastVO existingForecast = service.getById(id);
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date createdOn = new Date();
			try {
				createdOn = isoFormat.parse(isoFormat.format(createdOn));
			}catch(Exception e) {
				log.warn("Failed to format createdOn date to ISO format");
			}
			if(existingForecast==null || existingForecast.getId()==null) {
				log.error("Forecast project with this id {} doesnt exists , failed to create run", id);
				MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.addErrors(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}

			CreatedByVO requestUser = this.userStore.getVO();
			List<String> forecastProjectUsers = new ArrayList<>();
			forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
			List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
			if(collaborators!=null && !collaborators.isEmpty()) {
				collaborators.forEach(n-> forecastProjectUsers.add(n.getId()));
			}
			if(forecastProjectUsers!=null && !forecastProjectUsers.isEmpty()) {
				if(!forecastProjectUsers.contains(requestUser.getId())) {
					log.error("User not part of forecast project with id {} and name {}, Not authorized to user other project inputs",id,existingForecast.getName());
					MessageDescription invalidMsg = new MessageDescription("User not part of given Forecast project. Cannot initate run");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess("FAILED");
					errorMessage.addErrors(invalidMsg);
					responseVO.setData(null);
					responseVO.setResponse(errorMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}else {
					if(file!=null) {
						String fileName = file.getOriginalFilename();
						if (!isValidAttachment(fileName)) {
							log.error("Invalid file type {} attached for project name {} and id {} ", fileName, existingForecast.getName(), id);
							MessageDescription invalidMsg = new MessageDescription("Invalid File type attached. Supported only xlxs and csv extensions");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.setSuccess("FAILED");
							errorMessage.addErrors(invalidMsg);
							responseVO.setData(null);
							responseVO.setResponse(errorMessage);
							return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
						}else {
							List<InputFileVO> savedInputs = existingForecast.getSavedInputs();
							if(saveRequestPart) {
								if(savedInputs!=null && !savedInputs.isEmpty()) {
									List<String> fileNames = savedInputs.stream().map(InputFileVO::getName).collect(Collectors.toList());
									if(fileNames.contains(file.getOriginalFilename())) {
										log.error("File with name already exists in saved input files list. Project {} and file {}", existingForecast.getName(),fileName);
										MessageDescription invalidMsg = new MessageDescription("File with name already exists in saved input files list. Please rename and upload again");
										GenericMessage errorMessage = new GenericMessage();
										errorMessage.setSuccess("FAILED");
										errorMessage.addErrors(invalidMsg);
										responseVO.setData(null);
										responseVO.setResponse(errorMessage);
										return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
									}
								}else
									savedInputs = new ArrayList<>();
							}
							FileUploadResponseDto fileUploadResponse = storageClient.uploadFile(INPUT_FILE_PREFIX,file, existingForecast.getBucketName());
							if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
								GenericMessage errorMessage = new GenericMessage();
								errorMessage.setSuccess("FAILED");
								errorMessage.setErrors(fileUploadResponse.getErrors());
								errorMessage.setWarnings(fileUploadResponse.getWarnings());
								responseVO.setData(null);
								responseVO.setResponse(errorMessage);
								return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
							}else if("SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())){
									if(saveRequestPart) {
										InputFileVO currentInput = new InputFileVO();
										currentInput.setName(file.getOriginalFilename());
										currentInput.setPath(existingForecast.getBucketName()+"/inputs/"+file.getOriginalFilename());
										currentInput.setId(UUID.randomUUID().toString());
										currentInput.setCreatedOn(createdOn);
										currentInput.setCreatedBy(requestUser.getId());
										savedInputs.add(currentInput);
										existingForecast.setSavedInputs(savedInputs);
									}
								savedInputPath = existingForecast.getBucketName() + "/inputs/"+ file.getOriginalFilename();
							}
						}
				}

					if (runName == null || runName.trim().isEmpty()) {
						SimpleDateFormat runNameDate = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
						Date date = new Date();
						runName = "run-" + runNameDate.format(date);
					}
					log.info("Passed all validations for create run in controller, calling service for project {} ", id);
					ForecastRunResponseVO createRunResponse = service.createJobRun(file,savedInputPath, saveRequestPart, runName, configurationFile,
							frequency, forecastHorizon, hierarchy, comment, runOnPowerfulMachines, existingForecast,requestUser.getId(),createdOn,infotext);
					if(createRunResponse!= null && "SUCCESS".equalsIgnoreCase(createRunResponse.getResponse().getSuccess())
								&& createRunResponse.getData().getRunId()!=null) {
						return new ResponseEntity<>(createRunResponse, HttpStatus.CREATED);
					}else {
						return new ResponseEntity<>(createRunResponse, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				}
			}
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	@Override
	@ApiOperation(value = "Cancel particular run based on id.", nickname = "cancelRun", notes = "Cancel particular run based on id.", response = CancelRunResponseVO.class, tags={ "forecast-runs", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = CancelRunResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/runs/{correlationid}",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.PUT)
	public ResponseEntity<CancelRunResponseVO> cancelRun(@ApiParam(value = "forecast project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "DNA correlation Id for the run", required = true) @PathVariable("correlationid") String correlationid) {
		CancelRunResponseVO responseVO = new CancelRunResponseVO();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		Boolean notAuthorized = false;
		boolean notFound = true;
		if (existingForecast == null || existingForecast.getId() == null) {
			log.error("Forecast project with this id {} doesnt exists , failed to create comparison", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(null);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized = true;

			}
		}
		if (notAuthorized) {
			GenericMessage responseMessage = new GenericMessage();
			List<MessageDescription> errMsgs = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Only user of this project can cancel the run. Unauthorized");
			responseMessage.setSuccess("FAILED");
			errMsgs.add(errMsg);
			responseMessage.setErrors(errMsgs);
			responseVO.setData(null);
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		List<RunVO> runVOList = existingForecast.getRuns();
			if(runVOList!= null && !runVOList.isEmpty()) {
				for(RunVO run: runVOList) {
					if(correlationid.equalsIgnoreCase(run.getId()) && (run.isIsDeleted() == null || !run.isIsDeleted()) ) {
						notFound = false;
					}
				}
			}else
				notFound = true;
			if (notFound) {
				log.error("Invalid runCorrelationId {} sent for cancelling run {}  project name {} and id {}, by user {}", correlationid, existingForecast.getName(), id, requestUser);
				MessageDescription invalidMsg = new MessageDescription("Invalid runCorrelationIds " + correlationid + " sent for cancelling run. Please correct and retry.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.addErrors(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		CancelRunResponseVO cancelRunResponse = service.cancelRunById(existingForecast, correlationid);
		if (cancelRunResponse != null && "SUCCESS".equalsIgnoreCase(cancelRunResponse.getResponse().getSuccess())) {
			return new ResponseEntity<>(cancelRunResponse, HttpStatus.OK);
		}

		return new ResponseEntity<>(cancelRunResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@Override
	@ApiOperation(value = "Create new comparison for forecast project.", nickname = "createForecastComparison", notes = "Create comparison for forecast project", response = ForecastComparisonCreateResponseVO.class, tags = {"forecast-comparisons",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = ForecastRunResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/forecasts/{id}/comparisons",
			produces = {"application/json"},
			consumes = {"multipart/form-data"},
			method = RequestMethod.POST)
	public ResponseEntity<ForecastComparisonCreateResponseVO> createForecastComparison(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,
			@ApiParam(value = "Comma separated forecast run corelation Ids. Maximum 12 Ids can be sent. Please avoid sending duplicates.", required=true) @RequestParam(value="runCorelationIds", required=true)  String runCorelationIds,
			@ApiParam(value = "The input file for the comparison of forecast runs.") @Valid @RequestPart(value="actualsFile", required=false) MultipartFile actualsFile,
			@ApiParam(value = "Comparison name") @RequestParam(value="comparisonName", required=false)  String comparisonName)
	{
		ForecastComparisonCreateResponseVO responseVO = new ForecastComparisonCreateResponseVO();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		Date createdOn = new Date();
		try {
			createdOn = isoFormat.parse(isoFormat.format(createdOn));
		}catch(Exception e) {
			log.warn("Failed to format createdOn date to ISO format");
		}
		SimpleDateFormat comparisonNameDate = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
		Date date = new Date();
		comparisonName = comparisonName!= null && !"".equalsIgnoreCase(comparisonName) ? comparisonName :  "comparison - " + comparisonNameDate.format(date); //ISOdate as comparisonname from createdOn
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists , failed to create comparison", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(null);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;

			}
		}
		if(notAuthorized) {
			 GenericMessage responseMessage = new GenericMessage();
			 List<MessageDescription> errMsgs = new ArrayList<>();
			 MessageDescription errMsg = new MessageDescription("Only user of this project can delete the run. Unauthorized");
			 responseMessage.setSuccess("FAILED");
			 errMsgs.add(errMsg);
			 responseMessage.setErrors(errMsgs);
			 responseVO.setData(null);
			 responseVO.setResponse(responseMessage);
			 return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		List<String> invalidRunIds = new ArrayList<>();
		List<String> validRunsPath = new ArrayList<>();
		List<RunVO> existingRuns = existingForecast.getRuns();
		if(runCorelationIds!=null) {
			try {
				String[] runCorrIds = runCorelationIds.split(",");
				List<String> runIdsList = Arrays.asList(runCorrIds);
				List<String> distinctRunIds = runIdsList.stream().distinct().collect(Collectors.toList());
				if(runIdsList!=null && !runIdsList.isEmpty()) {
					for(String runId : distinctRunIds) {
						Optional<RunVO> any = existingRuns.stream().filter(x -> runId.equalsIgnoreCase(x.getId()) && (x.isIsDeleted()==null || !x.isIsDeleted())).findAny();
						if(any!=null && any.isPresent()) {
							validRunsPath.add(any.get().getResultFolderPath());
						}else {
							invalidRunIds.add(runId);
						}
					}
					if((invalidRunIds!=null && !invalidRunIds.isEmpty() ) || validRunsPath.isEmpty() ) {
						String invalidIdsString = String.join(",", invalidRunIds);
						log.error("Invalid runCorrelationIds {} sent for comparison {} of project name {} and id {}, by user {} ", 
								invalidIdsString, comparisonName, existingForecast.getName(), id, requestUser);
						MessageDescription invalidMsg = new MessageDescription("Invalid runCorrelationIds " +  invalidIdsString + " sent for comparison. Please correct and retry.");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.setSuccess("FAILED");
						errorMessage.addErrors(invalidMsg);
						responseVO.setData(null);
						responseVO.setResponse(errorMessage);
						return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
					}
				}
			}catch(Exception e) {
				log.error("Exception occurred:{} while creating comparison", e.getMessage());
			}
		}
		String comparisionId = UUID.randomUUID().toString();
		String targetFolder = COMPARISON_FOLDER_PREFIX+comparisionId;
		String actualsFilePath = "";
		if(actualsFile!=null) {
			String fileName = actualsFile.getOriginalFilename();
			String actuals_file_extension = FilenameUtils.getExtension(fileName);
			String actuals_filename = ACTUALS_FILENAME_PREFIX + "." + actuals_file_extension;
			if (!isValidAttachment(fileName)) {
				log.error("Invalid file type {} attached for project name {} and id {} ", fileName, existingForecast.getName(), id);
				MessageDescription invalidMsg = new MessageDescription("Invalid File type attached. Supported only xlxs and csv extensions");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.addErrors(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
			FileUploadResponseDto fileUploadResponse = storageClient.uploadFile(targetFolder+"/",actualsFile,actuals_filename, existingForecast.getBucketName());
			if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.setErrors(fileUploadResponse.getErrors());
				errorMessage.setWarnings(fileUploadResponse.getWarnings());
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			actualsFilePath = existingForecast.getBucketName() + targetFolder + "/" + actuals_filename;
		}
		targetFolder = existingForecast.getBucketName() +  targetFolder;
		ForecastComparisonCreateResponseVO createComparisonResponse = service.createComparison(id,existingForecast,validRunsPath,comparisionId,comparisonName,actualsFilePath,targetFolder
				,createdOn, requestUser.getId());
		if(createComparisonResponse!= null && "SUCCESS".equalsIgnoreCase(createComparisonResponse.getResponse().getSuccess())
				&& createComparisonResponse.getData().getComparisonId()!=null) {
			return new ResponseEntity<>(createComparisonResponse, HttpStatus.CREATED);
		}else {
			return new ResponseEntity<>(createComparisonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "delete one or more comparisons", nickname = "deleteComparison", notes = "delete one or more comparisons", response = GenericMessage.class, tags = {"forecast-comparisons",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/forecasts/{id}/comparisons",
			produces = {"application/json"},
			consumes = {"application/json"},
			method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteComparison(@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@NotNull @ApiParam(value = "Comma separated forecast comparison IDs that are to be deleted. Ex: ?comparisonIds=\"ComparisionX01,ComparisionX02\" ", required = true) @Valid @RequestParam(value = "comparisonIds", required = true) String comparisonIds) {
		GenericMessage responseMessage = new GenericMessage();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists , failed to create comparison", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			List<MessageDescription> errorMessage = new ArrayList<>();

			responseMessage.setSuccess("FAILED");
			errorMessage.add(invalidMsg);
			responseMessage.setErrors(errorMessage);
			return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;

			}
		}
		if(notAuthorized) {

			List<MessageDescription> errMsgs = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Only user of this project can delete the run. Unauthorized");
			responseMessage.setSuccess("FAILED");
			errMsgs.add(errMsg);
			responseMessage.setErrors(errMsgs);
			return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
		}
		List<String> invalidComparisonIds = new ArrayList<>();
		List<String> validComparisonIds = new ArrayList<>();
		List<ForecastComparisonVO> existingComparisons = existingForecast.getComparisons();

		if(comparisonIds!=null) {
			try {
				String[] comparisonsIds = comparisonIds.split(",");
				List<String> comparisonIdsList = Arrays.asList(comparisonsIds);
				List<String> distinctComparisonIds = comparisonIdsList.stream().distinct().collect(Collectors.toList());
				if(comparisonIdsList!=null && !comparisonIdsList.isEmpty()) {
					for(String comparisonId : distinctComparisonIds) {
						Optional<ForecastComparisonVO> any = existingComparisons.stream().filter(x -> comparisonId.equalsIgnoreCase(x.getComparisonId()) && !x.isIsDeleted()).findAny();
						if(any!=null && any.isPresent()) {
							validComparisonIds.add(comparisonId);
						}else {
							invalidComparisonIds.add(comparisonId);
						}
					}
					if(invalidComparisonIds!=null && !invalidComparisonIds.isEmpty()) {
												String invalidIdsString = String.join(",", invalidComparisonIds);
						log.error("Invalid ComparisonIds {} sent for comparison  of project name {} and id {}, by user {} ",
								invalidIdsString, existingForecast.getName(), id, requestUser);
						MessageDescription invalidMsg = new MessageDescription("Invalid ComparisonIds " +  invalidIdsString + " sent for comparison. Please correct and retry.");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.setSuccess("FAILED");
						errorMessage.addErrors(invalidMsg);
						return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
					}
				}
			}catch(Exception e) {
				log.error("Exception occurred:{} while deleting comparison", e.getMessage());
			}
			responseMessage = service.deleteComparison(id,validComparisonIds);
		}
		if (responseMessage != null && "SUCCESS".equalsIgnoreCase(responseMessage.getSuccess())) {
			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
		}

		return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@Override
	@ApiOperation(value = "Get forecast comparison html for specific comparison.", nickname = "getForecastComparisonById", notes = "Get forecast comparison html for specific comparison.", response = ForecastComparisonResultVO.class, tags = {"forecast-comparisons",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ForecastComparisonResultVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/forecasts/{id}/comparisons/{comparisonId}/comparisonData",
			produces = {"application/json"},
			consumes = {"application/json"},
			method = RequestMethod.GET)
	public ResponseEntity<ForecastComparisonResultVO> getForecastComparisonById(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,
			@ApiParam(value = "Comparison Id for the run",required=true) @PathVariable("comparisonId") String comparisonId){
		ForecastComparisonResultVO responseVO = new ForecastComparisonResultVO();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		Date createdOn = new Date();
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists , failed to create comparison", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;

			}
		}
		if(notAuthorized) {
			GenericMessage responseMessage = new GenericMessage();
			List<MessageDescription> errMsgs = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Only user of this project can delete the run. Unauthorized");
			responseMessage.setSuccess("FAILED");
			errMsgs.add(errMsg);
			responseMessage.setErrors(errMsgs);
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		boolean notFound = true;
		List<ForecastComparisonVO> comparisonVOList = existingForecast.getComparisons();
		if(comparisonVOList!= null && !comparisonVOList.isEmpty()) {
			for(ForecastComparisonVO comparison: comparisonVOList) {
				if(comparisonId.equalsIgnoreCase(comparison.getComparisonId())
				     && !comparison.isIsDeleted()) {
					notFound = false;
				}
			}
		}else
			notFound = true;
		if(notFound) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		ForecastComparisonResultVO comparisonHTMLData = service.getForecastComparisonById(id,comparisonId);
		return new ResponseEntity<>(comparisonHTMLData, HttpStatus.OK);
	}


	@Override
	@ApiOperation(value = "Get all forecast comparison runs for the project", nickname = "getForecastComparisons", notes = "Get all forecast comparison runs for the project", response = ForecastComparisonsCollectionDto.class, tags={ "forecast-comparisons", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ForecastComparisonsCollectionDto.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/comparisons",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)

	public ResponseEntity<ForecastComparisonsCollectionDto> getForecastComparisons(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,
			@ApiParam(value = "forecast comparisons page number ") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "forecast comparisons page size") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort comparisons by a given variable like comparisonName, createdby, createdon or status", allowableValues = "comparisonName, createdOn, status, createdBy, actualsFile") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort comparisons based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder)
	{
		int defaultLimit = Integer.parseInt(runsDefaultPageSize);
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
		}
		if (sortBy == null || sortBy.trim().isEmpty())
			sortBy = "createdOn";
		if (sortOrder == null || sortOrder.trim().isEmpty()) {
			sortOrder = "desc";
		}
		ForecastComparisonsCollectionDto collection = new ForecastComparisonsCollectionDto();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		Date createdOn = new Date();
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;
			}
		}
		if(notAuthorized) {
			log.error("Only user of this project can view the details. Unauthorized");
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
//		service.getAllForecastComparisons(id);
		Object[] getComparisonsResultsArr = service.getAllForecastComparisons(limit,offset,id, sortBy,sortOrder);
		List<ForecastComparisonVO> records = (List<ForecastComparisonVO>) getComparisonsResultsArr[0];
		Integer totalCount = (Integer) getComparisonsResultsArr[1];
		HttpStatus responseCode = HttpStatus.NO_CONTENT;
		if(records!=null && !records.isEmpty()) {
			collection.setRecords(records);
			collection.setTotalCount(totalCount);
			responseCode = HttpStatus.OK;
		}
		return new ResponseEntity<>(collection, responseCode);
	}

	@Override
	@ApiOperation(value = "Upload a new config file for forecast project.", nickname = "uploadConfigFiles", notes = "Upload a new config file for forecast project", response = ForecastConfigFileUploadResponseVO.class, tags = {"forecast-config-files",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = ForecastConfigFileUploadResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/forecasts/{id}/config-files",
			produces = {"application/json"},
			consumes = {"multipart/form-data"},
			method = RequestMethod.POST)
	public ResponseEntity<ForecastConfigFileUploadResponseVO> uploadConfigFiles(@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			  @ApiParam(value = "The config file to upload for the forecast project.") @Valid @RequestPart(value = "configFile", required = false) MultipartFile configFile) {
		ForecastConfigFileUploadResponseVO responseVO = new ForecastConfigFileUploadResponseVO();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		Date createdOn = new Date();
		try {
			createdOn = isoFormat.parse(isoFormat.format(createdOn));
		} catch (Exception e) {
			log.warn("Failed to format createdOn date to ISO format");
		}
		Boolean notAuthorized = false;
		if (existingForecast == null || existingForecast.getId() == null) {
			log.error("Forecast project with this id {} doesnt exists , failed to upload config file", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(null);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized = true;
			}
		}
		if (notAuthorized) {
			GenericMessage responseMessage = new GenericMessage();
			List<MessageDescription> errMsgs = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Only user of this project can upload the config file. Unauthorized");
			responseMessage.setSuccess("FAILED");
			errMsgs.add(errMsg);
			responseMessage.setErrors(errMsgs);
			responseVO.setData(null);
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		String configFilePath = "";
		String configFileIdId = UUID.randomUUID().toString();
		if (configFile != null) {
			String fileName = configFile.getOriginalFilename();
			String config_file_extension = FilenameUtils.getExtension(fileName);
			String configs_filename = fileName + "." + config_file_extension;
			if (!isValidConfigFileAttachment(fileName)) {
				log.error("Invalid file type {} attached for project name {} and id {} ", fileName, existingForecast.getName(), id);
				MessageDescription invalidMsg = new MessageDescription("Invalid File type attached. Supported only yaml and yml extensions");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.addErrors(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			} else {
				List<InputFileVO> configFiles = existingForecast.getConfigFiles();

				if (configFiles != null && !configFiles.isEmpty()) {
					List<String> fileNames = configFiles.stream().map(InputFileVO::getName).collect(Collectors.toList());
					if (fileNames.contains(configFile.getOriginalFilename())) {
						log.error("File with name already exists in uploaded config files list. Project {} and file {}", existingForecast.getName(), fileName);
						MessageDescription invalidMsg = new MessageDescription("File with name already exists in uploaded config files list. Please rename and upload again");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.setSuccess("FAILED");
						errorMessage.addErrors(invalidMsg);
						responseVO.setData(null);
						responseVO.setResponse(errorMessage);
						return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
					}
				} else
					configFiles = new ArrayList<>();
				FileUploadResponseDto fileUploadResponse = storageClient.uploadFile(CONFIGS_FILE_PREFIX, configFile, existingForecast.getBucketName());
				if (fileUploadResponse == null || (fileUploadResponse != null && (fileUploadResponse.getErrors() != null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
					log.error("Failed to upload config file {} to storage bucket",fileName);
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess("FAILED");
					errorMessage.setErrors(fileUploadResponse.getErrors());
					errorMessage.setWarnings(fileUploadResponse.getWarnings());
					responseVO.setData(null);
					responseVO.setResponse(errorMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				} else if ("SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())) {
					log.info("Successfully to uploaded config file {} to storage bucket",fileName);
					InputFileVO currentConfigInput = new InputFileVO();
					currentConfigInput.setName(configFile.getOriginalFilename());
					currentConfigInput.setPath(existingForecast.getBucketName() + "/configs/" + configFile.getOriginalFilename());
					currentConfigInput.setId(configFileIdId);
					currentConfigInput.setCreatedOn(createdOn);
					currentConfigInput.setCreatedBy(requestUser.getId());
					configFiles.add(currentConfigInput);
					existingForecast.setConfigFiles(configFiles);
					configFilePath = existingForecast.getBucketName() + "/configs/" + configFile.getOriginalFilename();
				}
			}
			ForecastConfigFileUploadResponseVO uploadConfigResponse = service.uploadConfigFile(existingForecast,configFileIdId,requestUser.getId(),createdOn,configFilePath,configFile.getOriginalFilename());
			if(uploadConfigResponse!= null && "SUCCESS".equalsIgnoreCase(uploadConfigResponse.getResponse().getSuccess())
					&& uploadConfigResponse.getData().getId()!=null) {
				return new ResponseEntity<>(uploadConfigResponse, HttpStatus.CREATED);
			}else {
				return new ResponseEntity<>(uploadConfigResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@Override
	@ApiOperation(value = "Get all uploaded config files for the project", nickname = "getForecastConfigFiles", notes = "Get all uploaded config files for the project", response = ForecastConfigFilesCollectionDto.class, tags={ "forecast-config-files", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ForecastConfigFilesCollectionDto.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/config-files",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<ForecastConfigFilesCollectionDto> getForecastConfigFiles(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id) {
		ForecastConfigFilesCollectionDto collection = new ForecastConfigFilesCollectionDto();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;
			}
		}
		if(notAuthorized) {
			log.error("Only user of this project can view the details. Unauthorized");
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		Object[] getConfigsFilesResultsArr = service.getForecastConfigFiles(id);
		List<InputFileVO> records = (List<InputFileVO>) getConfigsFilesResultsArr[0];
		Integer totalCount = (Integer) getConfigsFilesResultsArr[1];
		HttpStatus responseCode = HttpStatus.NO_CONTENT;
		if(records!=null && !records.isEmpty()) {
			collection.setRecords(records);
			collection.setTotalCount(totalCount);
			responseCode = HttpStatus.OK;
		}
		return new ResponseEntity<>(collection, responseCode);
	}
	@Override
	@ApiOperation(value = "delete uploaded config file by id.", nickname = "deleteConfigFile", notes = "delete uploaded config file by id.", response = GenericMessage.class, tags={ "forecast-config-files", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/config-files/{configFileId}",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteConfigFile(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,
			@ApiParam(value = "config file ID",required=true) @PathVariable("configFileId") String configFileId) {

		GenericMessage responseMessage = new GenericMessage();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists , failed to delete config file", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			List<MessageDescription> errorMessage = new ArrayList<>();

			responseMessage.setSuccess("FAILED");
			errorMessage.add(invalidMsg);
			responseMessage.setErrors(errorMessage);
			return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;

			}
		}
		if(notAuthorized) {

			List<MessageDescription> errMsgs = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Only user of this project can delete the config file. Unauthorized");
			responseMessage.setSuccess("FAILED");
			errMsgs.add(errMsg);
			responseMessage.setErrors(errMsgs);
			return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
		}
		boolean notFound = false;
		String fileName= "";
		List<InputFileVO> configFiles = existingForecast.getConfigFiles();
		if (configFiles != null && !configFiles.isEmpty()) {
			List<String> configFileIdList = configFiles.stream().map(InputFileVO::getId).collect(Collectors.toList());
			if (configFileIdList.contains(configFileId)) {
				notFound = true;
				Optional<InputFileVO> configFileObject = configFiles.stream().
						filter(x -> x.getId().equals(configFileId)).
						findFirst();
				InputFileVO file = configFileObject.get();
				fileName = file.getName();
				configFiles.remove(file);
			} else
				notFound = false;
		}
		if (!notFound) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		existingForecast.setConfigFiles(configFiles);
		try {
			String prefix= "configs/" + fileName;
			DeleteBucketResponseWrapperDto deleteFileResponse = storageClient.deleteFilePresent(existingForecast.getBucketName(),prefix);
			if(deleteFileResponse==null || (deleteFileResponse!=null && (deleteFileResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(deleteFileResponse.getStatus())))) {
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.setErrors(deleteFileResponse.getErrors());
				errorMessage.setWarnings(deleteFileResponse.getWarnings());
				return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
			}else if("SUCCESS".equalsIgnoreCase(deleteFileResponse.getStatus())) {
				log.info("Successfully deleted config file {} from storage bucket",fileName);
				ForecastVO updatedVO = service.create(existingForecast);
			}
		}
		catch (Exception e){
			List<MessageDescription> errors = new ArrayList<>();
			log.error("Failed while saving config files for project name {} project id {}",existingForecast.getName(), existingForecast.getId());
			MessageDescription msg = new MessageDescription("Failed while saving config files for project id" +existingForecast.getId());
			errors.add(msg);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		responseMessage.setSuccess("SUCCESS");
		return new ResponseEntity<>(responseMessage, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Get specific forecast config yaml for a forecast project.", nickname = "getForecastConfigFileById", notes = "Get specific forecast config yaml for a forecast project.", response = ForecastConfigFileResultVO.class, tags={ "forecast-config-files", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = ForecastConfigFileResultVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/config-files/{configFileId}/configFileData",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<ForecastConfigFileResultVO> getForecastConfigFileById(@ApiParam(value = "forecast project ID ",required=true) @PathVariable("id") String id,
			@ApiParam(value = "Specific config file  Id for the forecast project",required=true) @PathVariable("configFileId") String configFileId) {
		ForecastConfigFileResultVO responseVO = new ForecastConfigFileResultVO();
		ForecastVO existingForecast = service.getById(id);
		CreatedByVO requestUser = this.userStore.getVO();
		Boolean notAuthorized = false;
		if(existingForecast==null || existingForecast.getId()==null) {
			log.error("Forecast project with this id {} doesnt exists , failed to get config file", id);
			MessageDescription invalidMsg = new MessageDescription("Forecast project doesnt exists with given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		List<String> forecastProjectUsers = new ArrayList<>();
		forecastProjectUsers.add(existingForecast.getCreatedBy().getId());
		List<CollaboratorVO> collaborators = existingForecast.getCollaborators();
		if (collaborators != null && !collaborators.isEmpty()) {
			collaborators.forEach(n -> forecastProjectUsers.add(n.getId()));
		}
		if (forecastProjectUsers != null && !forecastProjectUsers.isEmpty()) {
			if (!forecastProjectUsers.contains(requestUser.getId())) {
				log.warn("User not part of forecast project with id {} and name {}, Not authorized", id, existingForecast.getName());
				notAuthorized=true;

			}
		}
		if(notAuthorized) {
			GenericMessage responseMessage = new GenericMessage();
			List<MessageDescription> errMsgs = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Only user of this project can get the config file. Unauthorized");
			responseMessage.setSuccess("FAILED");
			errMsgs.add(errMsg);
			responseMessage.setErrors(errMsgs);
			responseVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		boolean notFound = true;
		List<InputFileVO> configFilesVOList = existingForecast.getConfigFiles();
		if(configFilesVOList!= null && !configFilesVOList.isEmpty()) {
			for(InputFileVO configFile: configFilesVOList) {
				if(configFileId.equalsIgnoreCase(configFile.getId())) {
					notFound = false;
				}
			}
		}else
			notFound = true;
		if(notFound) {
			log.error("Config file id {} doesnt exists. Invalid configFileId",configFileId);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		ForecastConfigFileResultVO configFileData = service.getForecastConfigFileById(id,configFileId);
		return new ResponseEntity<>(configFileData, HttpStatus.OK);
	}

}
