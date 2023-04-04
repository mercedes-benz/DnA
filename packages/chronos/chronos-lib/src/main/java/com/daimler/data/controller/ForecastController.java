package com.daimler.data.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.daimler.data.auth.vault.VaultAuthClientImpl;
import com.daimler.data.dto.forecast.*;
import com.daimler.data.dto.storage.BucketObjectDetailsDto;
import com.daimler.data.dto.storage.BucketObjectsCollectionDto;
import com.daimler.data.service.forecast.ForecastService;
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

import com.daimler.data.api.forecast.ForecastInputsApi;
import com.daimler.data.api.forecast.ForecastProjectsApi;
import com.daimler.data.api.forecast.ForecastRunsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.storage.BucketObjectsCollectionWrapperDto;
import com.daimler.data.dto.storage.FileUploadResponseDto;

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
public class ForecastController implements ForecastRunsApi, ForecastProjectsApi, ForecastInputsApi {

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
	private static final String BUCKETS_PREFIX = "chronos-";
	private static final String INPUT_FILE_PREFIX = "/inputs/";
	private static final String CONFIG_PATH = "/objects?prefix=configs/";

	private static final List<String> contentTypes = Arrays.asList("xlsx", "csv");

	private boolean isValidAttachment(String fileName) {
		boolean isValid = false;
		String extension = FilenameUtils.getExtension(fileName);
		if (contentTypes.contains(extension.toLowerCase())) {
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
		BucketObjectsCollectionWrapperDto projectSpecificBucketCollection = new BucketObjectsCollectionWrapperDto();
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
		collection = service.getBucketObjects(defaultConfigFolderPath);
		if (isValidId && id != null) {
			projectSpecificBucketCollection = service.getBucketObjects(existingForecast.getBucketName() + CONFIG_PATH);
			if (projectSpecificBucketCollection.getData() != null) {
				collection.getData().getBucketObjects().addAll(projectSpecificBucketCollection.getData().getBucketObjects());
			}
		}
		return new ResponseEntity<>(collection, HttpStatus.OK);
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
			"forecast-inputs", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/inputs/{savedinputid}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteSavedInputFile(
			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@ApiParam(value = "saved Input file ID", required = true) @PathVariable("savedinputid") String sid) {

		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		ForecastVO existingForecast = service.getById(id);
		if(existingForecast==null || !id.equalsIgnoreCase(existingForecast.getId())) {
			log.warn("No forecast found with id {}, failed to fetch saved inputs for given forecast id", id);
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
		GenericMessage responseMessage = service.deletInputFileByID(id,sid);
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
		CreatedByVO requestUser = this.userStore.getVO();
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
			 MessageDescription errMsg = new MessageDescription("Only user who triggered the run can delete. Unauthorized");
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
			@ApiParam(value = "page size to limit the number of forecasts, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {

		ForecastRunCollectionVO collection = new ForecastRunCollectionVO();
		int defaultLimit = 10;
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
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
				List<RunVO> records = service.getAllRunsForProject(limit, offset, existingForecast.getId());
				Long count = service.getRunsCount(id);
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


//	@Override
//	@ApiOperation(value = "Get all forecast project run visualization for the user.", nickname = "getRunVisualizationData", notes = "Get all forecasts projects for the user.", response = RunVisualizationVO.class, tags = {
//			"forecast-runs", })
//	@ApiResponses(value = {
//			@ApiResponse(code = 201, message = "Returns message of success or failure", response = RunVisualizationVO.class),
//			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
//			@ApiResponse(code = 400, message = "Bad request."),
//			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
//			@ApiResponse(code = 403, message = "Request is not authorized."),
//			@ApiResponse(code = 405, message = "Method not allowed"),
//			@ApiResponse(code = 500, message = "Internal error") })
//	@RequestMapping(value = "/forecasts/{id}/runs/{correlationid}", produces = { "application/json" }, consumes = {
//			"application/json" }, method = RequestMethod.GET)
//	public ResponseEntity<RunVisualizationVO> getRunVisualizationData(
//			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
//			@ApiParam(value = "DNA correlation Id for the run", required = true) @PathVariable("correlationid") String rid) {
//		return null;
//	}

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
    		@ApiParam(value = "If true, then run on Powerful Machines") @RequestParam(value="runOnPowerfulMachines", required=false)  Boolean runOnPowerfulMachines){
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
							frequency, forecastHorizon, hierarchy, comment, runOnPowerfulMachines, existingForecast,requestUser.getId(),createdOn);
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

}
