package com.mb.dna.datalakehouse.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.application.auth.CreatedByVO;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.UserInfoVO;
import com.mb.dna.datalakehouse.dto.DataLakeTableCollabDetailsVO;
import com.mb.dna.datalakehouse.dto.DataProductDetailsVO;
import com.mb.dna.datalakehouse.dto.TrinoConnectorsCollectionVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeConnectDetails;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeConnectVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeConnectWrapperVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeDBConnectDetails;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeDBUserDetails;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectCollectionVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectDetails;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectResponseVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectUpdateRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeTechUserWrapperDto;
import com.mb.dna.datalakehouse.service.TrinoDatalakeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Trino Connectors Details API", tags = { "datalakes" })
@RequestMapping("/api")
@Slf4j
public class TrinoDatalakeController {

	@Autowired
	private UserStore userStore;
	
	private static final String BUCKETS_PREFIX = "dna-datalake-";
	private static final String SCHEMA_PREFIX = "dna_datalake_";
	private static final String ICEBERG_CONNECTOR = "Iceberg";
	private static final String DELTALAKE_CONNECTOR = "Delta Lake";
	
	@Value("${trino.catalog.iceberg}")
	private String icebergCatalogName;
	
	@Value("${trino.catalog.delta}")
	private String deltaCatalogName;
	
	@Value("${trino.host}")
	private String trinoHost;
	
	@Value("${trino.port}")
	private String trinoPort;
	
	@Autowired
	private TrinoDatalakeService trinoDatalakeService;
	
	@ApiOperation(value = "Get all available trino datalake projects.", nickname = "getAll", notes = "Get all trino datalake projects. This endpoints will be used to Get all valid available datalake projects", response = TrinoConnectorsCollectionVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all datalake projects", response = TrinoDataLakeProjectCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeProjectCollectionVO> getAll(
			@ApiParam(value = "page number from which listing of projects should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of projects, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		int defaultLimit = 10;
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		final List<TrinoDataLakeProjectVO> projects = trinoDatalakeService.getAll(limit, offset, user);
		Long count = trinoDatalakeService.getCount(user);
		TrinoDataLakeProjectCollectionVO collectionVO = new TrinoDataLakeProjectCollectionVO();
		log.debug("Sending all trino datalake projects and their details");
		if (projects != null && projects.size() > 0) {
			collectionVO.setData(projects);
			collectionVO.setTotalCount(count);
			return new ResponseEntity<>(collectionVO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(collectionVO, HttpStatus.NO_CONTENT);
		}
	}
	
	@ApiOperation(value = "Delete datalake project details for a given Id.", nickname = "deleteById", notes = "Delete datalake project details for a given Id.", response = TrinoDataLakeProjectVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteById(
			@ApiParam(value = "Data Lake project ID to be deleted", required = true) @PathVariable("id") String id){
		try {
			TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getUpdatedById(id);
			if(existingProject==null || !id.equalsIgnoreCase(existingProject.getId())) {
				log.warn("No datalake project found with id {}, failed to fetch saved inputs for given id", id);
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}else {
				CreatedByVO requestUser = this.userStore.getVO();
				String user = requestUser.getId();
				if(!((existingProject.getCreatedBy()!=null && existingProject.getCreatedBy().getId()!=null && existingProject.getCreatedBy().getId().equalsIgnoreCase(user)))){
					log.error("Only Owner or collaborators with write permissions can edit project details. Access denied");
					MessageDescription invalidMsg = new MessageDescription("Only Owner or collaborators with write permissions can edit project details. Access denied.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess("FAILED");
					errorMessage.addErrors(invalidMsg);
					return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
				}
			}
			if(!((existingProject.getDataProductDetails()!=null && existingProject.getDataProductDetails().getId()!=null ))){
				log.error("Cannot delete given Datalake project {} as it is linked to Dataproduct {}. Delete denied.",existingProject.getId(),existingProject.getDataProductDetails().getId());
				MessageDescription invalidMsg = new MessageDescription("Cannot delete given Datalake project as it is linked to Dataproduct. Please unlink and retry deleting.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess("FAILED");
				errorMessage.addErrors(invalidMsg);
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			}
			GenericMessage deleteResponse = trinoDatalakeService.deleteProjectById(id);
			if(deleteResponse!=null && "SUCCESS".equalsIgnoreCase(deleteResponse.getSuccess())) {
				return new ResponseEntity<>(deleteResponse, HttpStatus.OK); 
			}else {
				return new ResponseEntity<>(deleteResponse, HttpStatus.INTERNAL_SERVER_ERROR); 
			}
		}catch(Exception e) {
			log.error("Failed to delete Datalake project with id {} with exception {} ", id,e.getMessage());
			MessageDescription invalidMsg = new MessageDescription("Failed to delete Datalake Project with internal exception. Please retry later.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get datalake project details for a given Id.", nickname = "getById", notes = "Get datalake project details for a given Id.", response = TrinoDataLakeProjectVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeProjectVO> getById(
			@ApiParam(value = "Data Lake project ID to be fetched", required = true) @PathVariable("id") String id){
		try {
			TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getUpdatedById(id);
			if(existingProject==null || !id.equalsIgnoreCase(existingProject.getId())) {
				log.warn("No datalake project found with id {}, failed to fetch saved inputs for given id", id);
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}else {
				CreatedByVO requestUser = this.userStore.getVO();
				String user = requestUser.getId();
				Long count = trinoDatalakeService.getCountForUserAndProject(user,id); 
				if(count<=0) {
					log.warn("User {} , not part of datalake project with id {}, access denied",user, id);
					return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
				}
			}
			return new ResponseEntity<>(existingProject, HttpStatus.OK);
		}catch(Exception e) {
			log.error("Failed to fetch record with id {} with exception {}",id,e.getMessage());
			return null;
		}
	}
	
	@ApiOperation(value = "Get datalake project connect details for a given Id.", nickname = "getConnectDetailsById", notes = "Get datalake project how-to-connect details for a given Id.", response = TrinoDataLakeConnectWrapperVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeConnectWrapperVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}/connect", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeConnectWrapperVO> getConnectDetailsById(
			@ApiParam(value = "Data Lake project ID to be fetched", required = true) @PathVariable("id") String id){
		TrinoDataLakeConnectWrapperVO response = new TrinoDataLakeConnectWrapperVO();
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		try {
			TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
			if(existingProject==null || !id.equalsIgnoreCase(existingProject.getId())) {
				log.warn("No datalake project found with id {}, failed to fetch saved inputs for given id", id);
				response.setStatus("FAILED");
				response.setData(null);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}else {
				Long count = trinoDatalakeService.getCountForUserAndProject(user,id);
				if(count<=0) {
					log.warn("User {} , not part of datalake project with id {}, access denied",user, id);
					response.setStatus("FAILED");
					response.setData(null);
					return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
				}
			}
			response.setStatus("SUCCESS");
			//set connect data
			TrinoDataLakeConnectVO data = new TrinoDataLakeConnectVO();
			TrinoDataLakeProjectDetails project = new TrinoDataLakeProjectDetails();
			TrinoDataLakeConnectDetails howToConnect = new TrinoDataLakeConnectDetails();
			project.setCatalogName(existingProject.getCatalogName());
			project.setProjectName(existingProject.getProjectName());
			project.setSchemaName(existingProject.getSchemaName());
			TrinoDataLakeDBConnectDetails trinoDetails = new TrinoDataLakeDBConnectDetails();
			TrinoDataLakeDBUserDetails userVO = new TrinoDataLakeDBUserDetails();
			TrinoDataLakeDBUserDetails techUserVO = new TrinoDataLakeDBUserDetails();
			
			techUserVO.setAccesskey(existingProject.getTechUserClientId()!=null ? existingProject.getTechUserClientId() : "YOUR_TECHUSER_CLIENTID");
			techUserVO.setExternalAuthentication(null);
			techUserVO.setHostName(trinoHost);
			techUserVO.setPort(trinoPort);
			techUserVO.setSecretKey("YOUR_CLIENT_SECRET");
			trinoDetails.setTechUserVO(techUserVO);
			
			userVO.setAccesskey(user);
			userVO.setExternalAuthentication(true);
			userVO.setHostName(trinoHost);
			userVO.setPort(trinoPort);
			userVO.setSecretKey(id);
			trinoDetails.setUserVO(userVO);
			
			howToConnect.setTrino(trinoDetails);
			data.setHowToConnect(howToConnect);
			data.setProject(project);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}catch(Exception e) {
			log.error("Failed to fetch datalake project with id {}", id);
			response.setStatus("FAILED");
			response.setData(null);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@ApiOperation(value = "Create Data Lake project for user.", nickname = "createDataLakeProject", notes = "Create Data Lake project for user ", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> createDataLakeProject(
			@ApiParam(value = "Request Body that contains data required for create Data Lake project for user", required = true) @Valid @RequestBody TrinoDataLakeProjectRequestVO requestVO) {
		TrinoDataLakeProjectResponseVO serviceCreateResponse = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectVO request = requestVO.getData();
		String name = request.getProjectName();
		
		if(name ==null && "".equalsIgnoreCase(name)) {
			log.error("Datalake project request has no name, bad request. Failed to create datalake project");
			MessageDescription invalidMsg = new MessageDescription("Datalake project request does not contain valid name, failed to create.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}
		String connectorType = request.getConnectorType();
		String catalogName = connectorType!=null && connectorType.equalsIgnoreCase(DELTALAKE_CONNECTOR) ? deltaCatalogName : icebergCatalogName ;
		String schemaName = SCHEMA_PREFIX+name;
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getByUniqueliteral("projectName", name);
		if(existingProject!=null && existingProject.getId()!=null) {
			log.error("Datalake project with this name {} already exists , failed to create datalake project", name);
			MessageDescription invalidMsg = new MessageDescription("Datalake project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		List<String> existingSchemas = trinoDatalakeService.showSchemas(catalogName, schemaName);
		if(existingSchemas!=null && !existingSchemas.isEmpty()) {
			log.error("Datalake project with this name {} already exists , failed to create datalake project", name);
			MessageDescription invalidMsg = new MessageDescription("Datalake project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		String bucketName = BUCKETS_PREFIX + name;
		Boolean isBucketExists = trinoDatalakeService.isBucketExists(bucketName);
		if(isBucketExists) {
			log.error("Datalake project with this name {} already exists , failed to create forecast project", name);
			MessageDescription invalidMsg = new MessageDescription("Datalake project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		request.setBucketName(bucketName);
		UserInfoVO createdBy = new UserInfoVO();
		BeanUtils.copyProperties(requestUser, createdBy);
		request.setCreatedBy(createdBy);
		request.setSchemaName(schemaName);
		request.setCatalogName(catalogName);
		request.setBucketName(bucketName);
		request.setBucketId(null);
		request.setId(null);
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		Date createdOn = new Date();
		try {
			createdOn = isoFormat.parse(isoFormat.format(createdOn));
		}catch(Exception e) {
			log.warn("Failed to format createdOn date to ISO format");
		}
		request.setCreatedOn(createdOn);
		TrinoDataLakeProjectVO data = new TrinoDataLakeProjectVO();
		try {
			serviceCreateResponse = trinoDatalakeService.createDatalake(request);
			data = serviceCreateResponse.getData();
			if(data!=null && data.getId()!=null) {
				responseVO.setData(data);
				responseVO.setResponse(serviceCreateResponse.getResponse());
				log.info("Datalake {} created successfully", name);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			}else {
				GenericMessage failedResponse = new GenericMessage();
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				failedResponse.addErrors(message);
				failedResponse.setSuccess("FAILED");
				responseVO.setData(request);
				responseVO.setResponse(failedResponse);
				log.error("Datalake project {} , failed to create", name);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to save due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(request);
			responseVO.setResponse(failedResponse);
			log.error("Exception occurred:{} while creating datalake project {} ", e.getMessage(), name);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "update datalake project details for a given Id.", nickname = "updateDataProductDetails", notes = "update datalake project dataproduct details for a given Id.", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}/dataproduct", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PATCH)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> updateDataProductDetails(
			@ApiParam(value = "Data Lake project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating of datalake project details of dataproduct", required = true) @Valid @RequestBody DataProductDetailsVO datalakeDataProductUpdateRequestVO) {
		TrinoDataLakeProjectResponseVO responseAggregateVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		DataProductDetailsVO isExists = new DataProductDetailsVO();
		DataProductDetailsVO request = datalakeDataProductUpdateRequestVO;
		if(existingProject==null || existingProject.getId()==null) {
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Datalake project does not exist with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseAggregateVO.setResponse(responseMessage);
			return new ResponseEntity<>(responseAggregateVO, HttpStatus.NOT_FOUND);
		}
		//check if user is project owner with write permissions
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		if(!((existingProject.getCreatedBy()!=null && existingProject.getCreatedBy().getId()!=null && existingProject.getCreatedBy().getId().equalsIgnoreCase(user)))){
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Only Owner can edit project details. Access denied.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseAggregateVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseAggregateVO, HttpStatus.FORBIDDEN);
		}
		try{
			if(request == null || request.getId()==null) {
				log.info("Dataproduct id sent as null, unlinking dataproduct");
				existingProject.setDataProductDetails(new DataProductDetailsVO());
			}else {
				isExists =	trinoDatalakeService.isValidDataProduct(request.getId());
				if(isExists==null || isExists.getId()==null) {
					log.error("Given Data Product {} is invalid",id);
					MessageDescription invalidMsg = new MessageDescription("Given Dataproduct is invalid, please make sure that Dataproduct provided exists and you are either Product Owner or Creator of it.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess("FAILED");
					errorMessage.addErrors(invalidMsg);
					responseAggregateVO.setResponse(errorMessage);
					return new ResponseEntity<>(responseAggregateVO, HttpStatus.BAD_REQUEST);
				}else {
					existingProject.setDataProductDetails(isExists);
				}
			}
		}catch(Exception e) {
			log.error("Failed to check if dataproduct is valid with internal server exception {} . Update request for project {} failed",e.getMessage(),id);
			MessageDescription invalidMsg = new MessageDescription("Failed to validate given dataproduct, Please retry after a while.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseAggregateVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseAggregateVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String name = existingProject.getProjectName();
		TrinoDataLakeProjectVO data = new TrinoDataLakeProjectVO();
		try {
			TrinoDataLakeProjectVO updatedDataLakeProjectDetails = trinoDatalakeService.create(existingProject);
			GenericMessage successMessage = new GenericMessage();
			successMessage.setSuccess("SUCCESS");
			responseAggregateVO.setResponse(successMessage);
			responseAggregateVO.setData(updatedDataLakeProjectDetails);
			return new ResponseEntity<>(responseAggregateVO, HttpStatus.OK);
		}catch(Exception e) {
			log.error("Failed with exception {} while updating dataproduct details for Datalake project {}",e.getMessage(),id );
			GenericMessage errorMessage = new GenericMessage();
			MessageDescription invalidMsg = new MessageDescription("Failed with internal server error, while updating dataproduct details for Datalake project.");
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseAggregateVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseAggregateVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@ApiOperation(value = "update datalake project details for a given Id.", nickname = "updateTechUserDetails", notes = "update datalake project tech user details for a given Id.", response = GenericMessage.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeTechUserWrapperDto.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}/techuser", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PATCH)
	public ResponseEntity<GenericMessage> updateTechUserDetails(
			@ApiParam(value = "Data Lake project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating of datalake project details of tech user", required = true) @Valid @RequestBody TrinoDataLakeTechUserWrapperDto datalakeTechUserUpdateRequestVO) {
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		TrinoDataLakeTechUserWrapperDto request = datalakeTechUserUpdateRequestVO;
		if(existingProject==null || existingProject.getId()==null) {
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Datalake project does not exist with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		}
		//check if user is project owner or collab with write permissions
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		if(!((existingProject.getCreatedBy()!=null && existingProject.getCreatedBy().getId()!=null && existingProject.getCreatedBy().getId().equalsIgnoreCase(user)))){
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Only Owner can edit project details. Access denied.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}
		try{
				Boolean isExists = trinoDatalakeService.isKeyExists(request.getData().getClientId(), existingProject.getProjectName());
				if(isExists) {
					log.error("Given clientId already in use by another Datalake project, Please retry with different clientId. Update request for project {} failed",id);
					MessageDescription invalidMsg = new MessageDescription("Given clientId already in use by another Datalake project, Please retry with different clientId.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess("FAILED");
					errorMessage.addErrors(invalidMsg);
					return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
				}
		}catch(Exception e) {
			log.error("Failed to check if clientId already exists with internal server exception {} . Update request for project {} failed",e.getMessage(),id);
			MessageDescription invalidMsg = new MessageDescription("Failed to check if clientId already exists, Please retry after a while.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String name = existingProject.getProjectName();
		TrinoDataLakeProjectVO data = new TrinoDataLakeProjectVO();
		try {
			if(request.getData()!=null && request.getData().getClientId()!=null) {
				GenericMessage serviceResponse = trinoDatalakeService.updateTechUserDetails(existingProject,request.getData().getClientId(),request.getData().getClientSecret());
				return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
			}
			GenericMessage errorMessage = new GenericMessage();
			MessageDescription invalidMsg = new MessageDescription("Bad request, data should not be null.");
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		}catch(Exception e) {
			log.error("Failed with exception {} while updating tech user details for Datalake project {}",e.getMessage(),id );
			GenericMessage errorMessage = new GenericMessage();
			MessageDescription invalidMsg = new MessageDescription("Failed with internal server error, while updating tech user details for Datalake project.");
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	
	@ApiOperation(value = "update datalake project details for a given Id.", nickname = "updateById", notes = "update datalake project details for a given Id.", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> updateById(
			@ApiParam(value = "Data Lake project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating of datalake project details like add/remove tables and manage collaborators of tables", required = true) @Valid @RequestBody TrinoDataLakeProjectUpdateRequestVO datalakeUpdateRequestVO) {
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectResponseVO serviceUpdateResponse = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectUpdateRequestVO request =datalakeUpdateRequestVO;
		if(existingProject==null || existingProject.getId()==null) {
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Datalake project does not exist with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(new TrinoDataLakeProjectVO());
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		//check if user is project owner
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		List<String> writeCollabs = new ArrayList<>();
		List<DataLakeTableCollabDetailsVO> collabs = existingProject.getCollabs();
		if(collabs!=null && !collabs.isEmpty()) {
			for(DataLakeTableCollabDetailsVO collab : collabs) {
				if(collab.getHasWritePermission()!=null && collab.getHasWritePermission()) {
					writeCollabs.add(collab.getCollaborator().getId());
				}
			}
		}
		if(!((existingProject.getCreatedBy()!=null && existingProject.getCreatedBy().getId()!=null && existingProject.getCreatedBy().getId().equalsIgnoreCase(user))) || (writeCollabs.contains(user))){
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Only Owner can edit project details. Access denied.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(new TrinoDataLakeProjectVO());
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		if(existingProject.getDataProductDetails()!=null && existingProject.getDataProductDetails().getId()!=null){
			List<String> existingTablesInDna = new ArrayList<>();
			if(existingProject.getTables()!=null && !existingProject.getTables().isEmpty()) {
				existingTablesInDna = existingProject.getTables().stream().map(x->x.getTableName()).collect(Collectors.toList()); 
			}
			List<String> updateTablesList = new ArrayList<>();
			if(datalakeUpdateRequestVO!=null && datalakeUpdateRequestVO.getTables()!=null && !datalakeUpdateRequestVO.getTables().isEmpty()) {
				updateTablesList = datalakeUpdateRequestVO.getTables().stream().map(n->n.getTableName()).collect(Collectors.toList());
			}
			Boolean tryingToInValidateDataProduct = false;
			if(existingTablesInDna!=null && !existingTablesInDna.isEmpty()) {
				if(updateTablesList==null || updateTablesList.isEmpty()) {
					tryingToInValidateDataProduct = true;
				}else {
					for(String existingTable : existingTablesInDna) {
						if(!updateTablesList.contains(existingTable)) {
							tryingToInValidateDataProduct = true;
							break;
						}
					}
				}
			}
			if(tryingToInValidateDataProduct) {
			log.error("Datalake project with id {} is linked to DataProduct {}, cannot remove tables. Bad Request.", id,existingProject.getDataProductDetails().getId());
			MessageDescription invalidMsg = new MessageDescription("Datalake project is linked with DataProduct. Cannot remove tables when linked. Bad Request.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addErrors(invalidMsg);
			responseVO.setData(new TrinoDataLakeProjectVO());
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
		}
		String name = existingProject.getProjectName();
		TrinoDataLakeProjectVO data = new TrinoDataLakeProjectVO();
		try {
			serviceUpdateResponse = trinoDatalakeService.updateDatalake(existingProject, request);           
			data = serviceUpdateResponse.getData();
			if(data!=null && data.getId()!=null) {
				responseVO.setData(data);
				responseVO.setResponse(serviceUpdateResponse.getResponse());
				log.info("Datalake {} updated successfully", name);
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			}else {
				GenericMessage failedResponse = new GenericMessage();
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				failedResponse.addErrors(message);
				failedResponse.setSuccess("FAILED");
				responseVO.setData(existingProject);
				responseVO.setResponse(failedResponse);
				log.error("Datalake project {} , failed to update", name);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to save due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(existingProject);
			responseVO.setResponse(failedResponse);
			log.error("Exception occurred:{} while creating datalake project {} ", e.getMessage(), name);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
}
