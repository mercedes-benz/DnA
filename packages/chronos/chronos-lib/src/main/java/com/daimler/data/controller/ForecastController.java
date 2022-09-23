package com.daimler.data.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.forecast.CollaboratorVO;
import com.daimler.data.dto.forecast.CreatedByVO;
import com.daimler.data.dto.forecast.ForecastCollectionVO;
import com.daimler.data.dto.forecast.ForecastProjectCreateRequestVO;
import com.daimler.data.dto.forecast.ForecastProjectCreateRequestWrapperVO;
import com.daimler.data.dto.forecast.ForecastProjectResponseVO;
import com.daimler.data.dto.forecast.ForecastRunCollectionVO;
import com.daimler.data.dto.forecast.ForecastRunRequestVO;
import com.daimler.data.dto.forecast.ForecastRunResponseVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.dto.forecast.InputFilesCollectionVO;
import com.daimler.data.dto.forecast.RunUpdateRequestWrapperVO;
import com.daimler.data.dto.forecast.RunVisualizationVO;
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
public class ForecastController implements ForecastRunsApi, ForecastProjectsApi, ForecastInputsApi {

	@Autowired
	private ForecastService service;
	
	@Autowired
	private UserStore userStore;
	
	private static final String BUCKETS_PREFIX = "chronos-";
	
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
				return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
			}
		}
		List<InputFileVO> files = service.getSavedFiles(id);
		HttpStatus responseStatus = HttpStatus.OK;
		if(files== null || files.isEmpty()) {
			responseStatus = HttpStatus.NO_CONTENT;
		}
		collection.setFiles(files);
		return new ResponseEntity<>(collection, responseStatus);
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
		ForecastVO forecastVO = new ForecastVO();
		forecastVO.setApiKey(forecastProjectCreateVO.getApiKey());
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
				return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
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
	@RequestMapping(value = "/forecasts/{id}/runs/{rid}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteRun(
			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@ApiParam(value = "run id ", required = true) @PathVariable("rid") String rid) {
		return null;
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
		return null;
	}

	@Override
	@ApiOperation(value = "Get all forecast projects for the user.", nickname = "getRunVisualizationData", notes = "Get all forecasts projects for the user.", response = RunVisualizationVO.class, tags = {
			"forecast-runs", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = RunVisualizationVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/forecasts/{id}/runs/{rid}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<RunVisualizationVO> getRunVisualizationData(
			@ApiParam(value = "forecast project ID ", required = true) @PathVariable("id") String id,
			@ApiParam(value = "run id ", required = true) @PathVariable("rid") String rid) {
		return null;
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
    @RequestMapping(value = "/forecasts/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<ForecastRunResponseVO> createForecastRun(@ApiParam(value = "Request Body that contains data required for intialize chronos project for user" ,required=true )  @Valid @RequestBody ForecastRunRequestVO forecastRunRequestVO,
    		@ApiParam(value = "The file to upload.") @Valid @RequestPart(value="file", required=false) MultipartFile file){
		
		return null;
	}


	@Override
	@ApiOperation(value = "api for databricks to update the run details upon finishing the run", nickname = "updateRun", notes = "", response = GenericMessage.class, tags={ "forecast-runs", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/forecasts/{id}/runs/{rid}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<GenericMessage> updateRun(@ApiParam(value = "Request Body that contains updated details of run" ,required=true )  @Valid @RequestBody RunUpdateRequestWrapperVO forecastRunUpdateRequestVO){
		return null;
	}

}
