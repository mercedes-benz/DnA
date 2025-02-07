package com.daimler.data.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.powerapps.PowerappsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.PowerAppsAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.powerapps.CreatedByVO;
import com.daimler.data.dto.powerapps.DeveloperVO;
import com.daimler.data.dto.powerapps.PowerAppCollectionVO;
import com.daimler.data.dto.powerapps.PowerAppCreateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppCreateRequestWrapperVO;
import com.daimler.data.dto.powerapps.PowerAppResponseVO;
import com.daimler.data.dto.powerapps.PowerAppUpdateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.powerapp.PowerAppService;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Power Apps APIs")
@RequestMapping("/api")
@Slf4j
public class PowerAppController implements PowerappsApi
{
	@Autowired
	private PowerAppService service;

	@Autowired
	private PowerAppsAssembler assembler;
	
	@Autowired
	private UserStore userStore;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Value("${powerapps.defaults.environment}")
	private String defaultEnvironment;
	
	@Value("${powerapps.defaults.productionAvailability}")
	private String prodAvailabilityDefault;
	
	@Value("${powerapps.defaults.developerlicense}")
	private String developerlicenseDefault;
	
	@Value("${powerapps.defaults.powerBiApproverKey}")
	private String powerBiApproverKey;
	
	@Value("${powerapps.defaults.powerBiApproverId}")
	private String powerBiApproverId;
	
	@Autowired
	private KafkaProducerService kafkaProducer;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
	@Override
	@ApiOperation(value = "Creates a new power app subscription request.", nickname = "create", notes = "Creates a new power app subscription request", response = PowerAppResponseVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = PowerAppResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<PowerAppResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new workspace" ,required=true )  @Valid @RequestBody PowerAppCreateRequestWrapperVO powerAppCreateVO,
    		@ApiParam(value = "Authorization" ) @RequestHeader(value="Authorization", required=false) String authorization){
		PowerAppResponseVO responseVO = new PowerAppResponseVO();
		if(this.userStore.getUserInfo() ==null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null || "".equalsIgnoreCase(this.userStore.getVO().getId().trim())) {
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		if(powerAppCreateVO!= null) {
			PowerAppCreateRequestVO projectCreateVO = powerAppCreateVO.getData();
			String name = projectCreateVO.getName();
			PowerAppVO existingApp = service.findbyUniqueLiteral(name);
			if(existingApp!=null && existingApp.getId()!=null) {
				log.error("Power App request with this name {} already exists , failed to create new request", name);
				MessageDescription invalidMsg = new MessageDescription("Power app request already exists with given name. Please retry with unique name");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.CONFLICT.name());
				errorMessage.addWarnings(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			if(projectCreateVO.getName() == null || "".equalsIgnoreCase(projectCreateVO.getName())
					|| projectCreateVO.getEnvOwnerId() == null || "".equalsIgnoreCase(projectCreateVO.getEnvOwnerId())
					|| projectCreateVO.getDepartment()  == null || "".equalsIgnoreCase(projectCreateVO.getDepartment())
					|| projectCreateVO.getBillingPlant()  == null || "".equalsIgnoreCase(projectCreateVO.getBillingPlant())
					|| projectCreateVO.getBillingCostCentre() == null || "".equalsIgnoreCase(projectCreateVO.getBillingCostCentre())) {
				log.error("Power App request has bad input , failed to create new request", name);
				MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, Please fill all mandatory fields.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
				errorMessage.addErrors(mandatoryFieldsError);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
			projectCreateVO.setName(name.trim());
			if(projectCreateVO.getName().matches(".*[^a-zA-Z0-9- ].*")) {
				log.error("Power App request name is invalid , failed to create new request", name.trim());
				MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, name is invalid. Please retry again with valid name.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
				errorMessage.addErrors(mandatoryFieldsError);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
			List<DeveloperVO>  validatedDevelopersList = new ArrayList<>();
			if(projectCreateVO.getDevelopers()!=null && !projectCreateVO.getDevelopers().isEmpty()) {
				List<String> tempDeveloperIds  = new ArrayList<String>();
				for(DeveloperVO tempDeveloper : projectCreateVO.getDevelopers()) {
					if(tempDeveloper.getUserDetails().getId()==null || tempDeveloper.getUserDetails().getId().equalsIgnoreCase("")) {
						log.error("Invalid developers details passed in collab list for power app {} create request.", projectCreateVO.getName());
						MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, Please recheck developers details.");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
						errorMessage.addErrors(mandatoryFieldsError);
						responseVO.setData(null);
						responseVO.setResponse(errorMessage);
						return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
					}else {
						if(tempDeveloper.getUserDetails().getId().equalsIgnoreCase(requestUser.getId())) {
							log.info("Requestor {} details passed in developers collab list for power app {} create request . Ignoring record and proceeding", requestUser.getId(), projectCreateVO.getName());
						}else {
							if(tempDeveloperIds.contains(tempDeveloper.getUserDetails().getId())) {
								log.info("Developer {} record already added in collab list for power app {} create request . Ignoring duplicate record and proceeding", tempDeveloper.getUserDetails().getId(), projectCreateVO.getName());
							}else {
								DeveloperVO tempDeveloperVO = new DeveloperVO();
								tempDeveloperVO.setUserDetails(tempDeveloper.getUserDetails());
								if(tempDeveloper.getLicense()==null || "".equalsIgnoreCase(tempDeveloper.getLicense())) {
									tempDeveloperVO.setLicense(developerlicenseDefault);
								}else {
									if(!(tempDeveloper.getLicense().equalsIgnoreCase(ConstantsUtility.POWERAPPLICENSE_VIRTUALAGENT)
											|| tempDeveloper.getLicense().equalsIgnoreCase(ConstantsUtility.POWERAPPLICENSE_AUTOPREMIUM)
											|| tempDeveloper.getLicense().equalsIgnoreCase(ConstantsUtility.POWERAPPLICENSE_PREMIUMUSER))) {
										log.error("Invalid Developer license details {} for developer {} during power app {} create request.",tempDeveloper.getLicense(),tempDeveloper.getUserDetails().getId(), projectCreateVO.getName());
										MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, Invalid developer licenses details.");
										GenericMessage errorMessage = new GenericMessage();
										errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
										errorMessage.addErrors(mandatoryFieldsError);
										responseVO.setData(null);
										responseVO.setResponse(errorMessage);
										return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
									}
									else {
										tempDeveloperVO.setLicense(tempDeveloper.getLicense());
									}
								}
								validatedDevelopersList.add(tempDeveloperVO);
								tempDeveloperIds.add(tempDeveloper.getUserDetails().getId());
							}
						}
					}
				}
				projectCreateVO.setDevelopers(validatedDevelopersList);
			}
		    
			if(projectCreateVO.getEnvironment() == null || "".equalsIgnoreCase(projectCreateVO.getEnvironment())){
				projectCreateVO.setEnvironment(defaultEnvironment);
			}else {
				if(!(projectCreateVO.getEnvironment().toString().equalsIgnoreCase(ConstantsUtility.ENV_DEDICATED_PRODCONFIDENTIAL)
						|| projectCreateVO.getEnvironment().toString().equalsIgnoreCase(ConstantsUtility.ENV_SHARED_DEV)
						|| projectCreateVO.getEnvironment().toString().equalsIgnoreCase(ConstantsUtility.ENV_SHARED_INT))) {
					log.error("Invalid Environment details {} passed power app {} create request.",projectCreateVO.getEnvironment().toString(), projectCreateVO.getName());
					MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, Invalid Environment details.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
					errorMessage.addErrors(mandatoryFieldsError);
					responseVO.setData(null);
					responseVO.setResponse(errorMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}
			if(projectCreateVO.getProdEnvAvailability()== null || "".equalsIgnoreCase(projectCreateVO.getProdEnvAvailability())){
				projectCreateVO.setProdEnvAvailability(prodAvailabilityDefault);
			}else {
				if(!(projectCreateVO.getProdEnvAvailability().toString().equalsIgnoreCase(ConstantsUtility.IMMEDIATE_PROD_ENV_AVAIL)
						|| projectCreateVO.getProdEnvAvailability().toString().equalsIgnoreCase(ConstantsUtility.LATER_PROD_ENV_AVAIL))) {
					log.error("Invalid Production environment availability details {} passed power app {} create request.",projectCreateVO.getProdEnvAvailability().toString(), projectCreateVO.getName());
					MessageDescription mandatoryFieldsError = new MessageDescription("Bad Request, Invalid Production availability details.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
					errorMessage.addErrors(mandatoryFieldsError);
					responseVO.setData(null);
					responseVO.setResponse(errorMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}
			List<MessageDescription> errors = new ArrayList<>();
			PowerAppVO vo = new PowerAppVO();
			vo = assembler.toVo(projectCreateVO);
			vo.setRequestedBy(requestUser);
			vo.setRequestedOn(new Date());
			vo.setState(ConstantsUtility.REQUESTED_STATE);
			vo.setSubscriptionType(ConstantsUtility.SHARED_DEVELOPMENT_ACCOUNT);			try {
				PowerAppVO createdVO = service.create(vo);
				if (createdVO != null && createdVO.getId() != null) {
					GenericMessage successResponse = new GenericMessage();
					successResponse.setSuccess("SUCCESS");
					successResponse.setErrors(null);
					successResponse.setWarnings(null);
					responseVO.setData(createdVO);
					responseVO.setResponse(successResponse);
					log.info("Power app Project {} created successfully by requestor {} ", name, requestUser.getId());
					return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
				}
			}catch(Exception e) {
				log.error("Failed to create powerapp {} requestedBy {} with exception {}",projectCreateVO.getName(),requestUser.getId(),e.getMessage());
				MessageDescription invalidMsg = new MessageDescription("Failed to create power app request with unknown error. Please try again.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.INTERNAL_SERVER_ERROR.name());
				errorMessage.addWarnings(invalidMsg);
				responseVO.setData(vo);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}else {
			MessageDescription invalidMsg = new MessageDescription("Bad request, please fill all required fields and retry.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(null);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
		}
		MessageDescription invalidMsg = new MessageDescription("Failed to create power app request with unknown error. Please try again.");
		GenericMessage errorMessage = new GenericMessage();
		errorMessage.setSuccess(HttpStatus.INTERNAL_SERVER_ERROR.name());
		errorMessage.addWarnings(invalidMsg);
		responseVO.setData(null);
		responseVO.setResponse(errorMessage);
		return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

	
	@Override
    @ApiOperation(value = "Get all power platform subscriptions for the user.", nickname = "getAll", notes = "Get all platform subscriptions. This endpoints will be used to get all valid available platform subscription records.", response = PowerAppCollectionVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = PowerAppCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PowerAppCollectionVO> getAll(@ApiParam(value = "Authorization" ) @RequestHeader(value="Authorization", required=false) String authorization,
    		@ApiParam(value = "apikey" ) @RequestHeader(value="apikey", required=false) String apikey,@ApiParam(value = "appid" ) @RequestHeader(value="appid", required=false) String appid,
    		@ApiParam(value = "query filter to search similar power apps with the given searchTerm name") @Valid @RequestParam(value = "name", required = false) String name,
    		@ApiParam(value = "filter power apps based on the state - allowed values requested, approved, rejected") @Valid @RequestParam(value = "state", required = false) String state,
    		@ApiParam(value = "page number from which listing of workspaces should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "page size to limit the number of workspaces, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
    		@ApiParam(value = "Sort workspaces by a given variable like name, requestedOn, state", defaultValue = "requestedOn") @Valid @RequestParam(value = "sortBy", required = false, defaultValue="requestedOn") String sortBy,
    		@ApiParam(value = "Sort solutions based on the given order, example asc,desc", defaultValue = "desc") @Valid @RequestParam(value = "sortOrder", required = false, defaultValue="desc") String sortOrder){
		PowerAppCollectionVO collection = new PowerAppCollectionVO();
		int defaultLimit = 10;
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit == 0 || limit<0) {
			limit = 0;
		}
		List<PowerAppVO> records =  new ArrayList<>();
		String user = "";
		if (apikey != null && powerBiApproverKey.equals(apikey) && appid!=null && powerBiApproverId.equals(appid)) {
			log.info("System admin approver requesting all power apps");
			user = "";
		}else {
			if(this.userStore.getUserInfo() ==null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null || "".equalsIgnoreCase(this.userStore.getVO().getId().trim())) {
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
			CreatedByVO requestUser = this.userStore.getVO();
			user = requestUser.getId();
		}
		if(sortBy==null || sortBy.equalsIgnoreCase("") || (!("name".equalsIgnoreCase(sortBy) || "requestedOn".equalsIgnoreCase(sortBy) || "state".equalsIgnoreCase(sortBy)))) {
			sortBy = "requestedOn";
		}
		if(sortOrder == null || !"asc".equalsIgnoreCase(sortOrder)) {
			sortOrder = "desc";
		}else {
			sortOrder = "asc";
		}
		if(state!=null && "".equalsIgnoreCase(state.trim())
				&& !(ConstantsUtility.APPROVED_STATE.equalsIgnoreCase(state) || ConstantsUtility.REJECTED_STATE.equalsIgnoreCase(state) || ConstantsUtility.REQUESTED_STATE.equalsIgnoreCase(state))){
			state = "";
		}
			records = service.getAll(limit, offset, name, state, user, sortBy, sortOrder);
		Long count = service.getCount(name, state, user);
		HttpStatus responseCode = HttpStatus.NO_CONTENT;
		if(records!=null && !records.isEmpty()) {
			collection.setRecords(records);
			collection.setTotalCount(count.intValue());
			responseCode = HttpStatus.OK;
		}
		log.info("sending power app details for response {} ",responseCode.name());
		return new ResponseEntity<>(collection, responseCode);
    }

	@Override
    @ApiOperation(value = "Get power app subscription details for a given Id.", nickname = "getById", notes = " This endpoints will get power app subscription details for a given identifier.", response = PowerAppVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = PowerAppVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PowerAppVO> getById(@ApiParam(value = "Power platform subscription ID to be fetched",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Authorization" ) @RequestHeader(value="Authorization", required=false) String authorization,
    		@ApiParam(value = "apikey" ) @RequestHeader(value="apikey", required=false) String apikey,@ApiParam(value = "appid" ) @RequestHeader(value="appid", required=false) String appid){
		PowerAppVO existingApp = service.getById(id);
		if(existingApp==null || !id.equalsIgnoreCase(existingApp.getId())) {
			log.warn("No app found with id {}, failed to fetch saved inputs for given power app request id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		if (apikey != null && powerBiApproverKey.equals(apikey) && appid!=null && powerBiApproverId.equals(appid)) {
			log.info("sending power app details for id {} and name {}, fetched by system admin",id,existingApp.getName());
			return new ResponseEntity<>(existingApp, HttpStatus.OK);
		}
		if(this.userStore.getUserInfo() ==null || this.userStore.getVO() == null || this.userStore.getVO().getId() == null) {
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
			CreatedByVO requestUser = this.userStore.getVO();
			List<String> appUsers = new ArrayList<>();
			appUsers.add(existingApp.getRequestedBy().getId());
			List<DeveloperVO> developers = existingApp.getDevelopers();
			if(developers!=null && !developers.isEmpty()) {
				developers.forEach(n-> appUsers.add(n.getUserDetails().getId()));
			}
			if(appUsers!=null && !appUsers.isEmpty()) {
				if(!appUsers.contains(requestUser.getId())) {
					log.warn("User not part of requested power platform application with id {} and name {}, Not authorized to use other projects",id,existingApp.getName());
					return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
				}else {
					log.info("sending power app details for id {} and name {}",id,existingApp.getName());
					return new ResponseEntity<>(existingApp, HttpStatus.OK);
				}
			}
		
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	@Override
	@ApiOperation(value = "Update existing power app subscription details .", nickname = "update", notes = "Updates an existing power app subscription.", response = PowerAppResponseVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Updated successfully", response = PowerAppResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "No workspace found to update", response = GenericMessage.class),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps/{id}/state",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<PowerAppResponseVO> update(
    		@ApiParam(value = "apikey" ,required=true) @RequestHeader(value="apikey", required=true) String apikey,@ApiParam(value = "appid" ,required=true) @RequestHeader(value="appid", required=true) String appid,
    		@ApiParam(value = "Power app subscription ID to be updated",required=true) @PathVariable("id") String id,
    		@ApiParam(value = "Request Body that contains data required for updating an existing workspace" ,required=true )  @Valid @RequestBody PowerAppUpdateRequestVO powerappUpdateRequestVO){
		PowerAppResponseVO responseVO = new PowerAppResponseVO();
		if(apikey != null && powerBiApproverKey.equals(apikey) && appid!=null && powerBiApproverId.equals(appid)) {
			PowerAppVO existingApp = service.getById(id);
			if(existingApp==null || !id.equalsIgnoreCase(existingApp.getId())) {
				log.warn("No app found with id {}, failed to fetch saved inputs for given power app request id", id);
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
			if(!(ConstantsUtility.APPROVED_STATE.equalsIgnoreCase(powerappUpdateRequestVO.getState()) || ConstantsUtility.REJECTED_STATE.equalsIgnoreCase(powerappUpdateRequestVO.getState()))){
				MessageDescription invalidMsg = new MessageDescription("Invalid state, cannot updated Power App request. Bad Request");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
				errorMessage.addWarnings(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
			if(!ConstantsUtility.REQUESTED_STATE.equalsIgnoreCase(existingApp.getState())){
				MessageDescription invalidMsg = new MessageDescription("Power App request already updated. Cannot override. Operation not allowed.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setSuccess(HttpStatus.BAD_REQUEST.name());
				errorMessage.addWarnings(invalidMsg);
				responseVO.setData(null);
				responseVO.setResponse(errorMessage);
				return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
			}
				try {
					existingApp.setComments(powerappUpdateRequestVO.getComments());
					existingApp.setUpdatedOn(new Date());
					existingApp.setUrl(powerappUpdateRequestVO.getUrl());
					String state = powerappUpdateRequestVO.getState().toString().toUpperCase();
					existingApp.setState(state);
					PowerAppVO updatedVO = service.create(existingApp);
					List<String> appUserIds = new ArrayList<>();
					List<String> appUserEmails = new ArrayList<>();
					appUserIds.add(existingApp.getRequestedBy().getId());
					appUserEmails.add(existingApp.getRequestedBy().getEmail());
					List<DeveloperVO> developers = existingApp.getDevelopers();
					if(developers!=null && !developers.isEmpty()) {
						developers.forEach(n-> {appUserIds.add(n.getUserDetails().getId()); appUserEmails.add(n.getUserDetails().getEmail());});
					}
					kafkaProducer.send("Power platform request approval update", id, "Power App " + existingApp.getName() + " request state has been updated to "+state.toLowerCase(), "PowerAppApprover", "Power App " + existingApp.getName() + " request state updated to "+state,
							true, appUserIds, appUserEmails, null);
					GenericMessage successResponse = new GenericMessage();
					successResponse.setSuccess("SUCCESS");
					successResponse.setErrors(null);
					successResponse.setWarnings(null);
					responseVO.setData(updatedVO);
					responseVO.setResponse(successResponse);
					log.info("Power app Project {} updated successfully by approver with state {} ", existingApp.getId()+"-"+existingApp.getName(), powerappUpdateRequestVO.getState());
					return new ResponseEntity<>(responseVO, HttpStatus.OK);
				}catch(Exception e) {
					log.error("Failed to update power app {} request with error {}",existingApp.getId()+"-"+existingApp.getName(), e.getMessage());
					MessageDescription invalidMsg = new MessageDescription("Failed to update power app request with unknown error. Please try again.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.setSuccess(HttpStatus.INTERNAL_SERVER_ERROR.name());
					errorMessage.addWarnings(invalidMsg);
					responseVO.setData(null);
					responseVO.setResponse(errorMessage);
					return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			
		}else {
			log.error("Invalid token {} for updating the request for app {} ",powerBiApproverKey, id);
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
	}

    
}
