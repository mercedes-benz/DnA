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
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.PowerAppsAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.powerapps.CreatedByVO;
import com.daimler.data.dto.powerapps.DeveloperVO;
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
@RequestMapping("/service")
@Slf4j
public class PowerAppApproverController
{
	@Autowired
	private PowerAppService service;

	@Autowired
	private PowerAppsAssembler assembler;
	
	@Autowired
	private UserStore userStore;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private KafkaProducerService kafkaProducer;
	
	@Value("${powerapps.defaults.environment}")
	private String defaultEnvironment;
	
	@Value("${powerapps.defaults.productionAvailability}")
	private String prodAvailabilityDefault;
	
	@Value("${powerapps.defaults.developerlicense}")
	private String developerlicenseDefault;
	
	@Value("${powerapps.defaults.powerBiApproverToken}")
	private String powerBiApproverToken;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
    @ApiOperation(value = "Update existing power app subscription details .", nickname = "update", notes = "Updates an existing power app subscription.", response = PowerAppResponseVO.class, tags={ "powerapps", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Updated successfully", response = PowerAppResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "No workspace found to update", response = GenericMessage.class),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/powerapps/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<PowerAppResponseVO> update(@ApiParam(value = "Power app subscription ID to be updated",required=true) @PathVariable("id") String id,@ApiParam(value = "Request Body that contains data required for updating an existing workspace" ,required=true )  @Valid @RequestBody PowerAppUpdateRequestVO powerappUpdateRequestVO,
    		@ApiParam(value = "x-api-key" ) @RequestHeader(value="x-api-key", required=true) String authKey){
    	PowerAppResponseVO responseVO = new PowerAppResponseVO();
    	PowerAppVO existingApp = service.getById(id);
		if(existingApp==null || !id.equalsIgnoreCase(existingApp.getId())) {
			log.warn("No app found with id {}, failed to fetch saved inputs for given power app request id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		String powerBiApproverKey = httpRequest.getHeader("x-api-key");
		if(powerBiApproverKey!=null && powerBiApproverKey.equals(powerBiApproverToken)) {
			if(!(ConstantsUtility.APPROVED_STATE.equalsIgnoreCase(powerappUpdateRequestVO.getState().name()) || ConstantsUtility.REJECTED_STATE.equalsIgnoreCase(powerappUpdateRequestVO.getState().name()))){
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
					kafkaProducer.send("Power platform request approval update", id, "Power App Request state updated to "+state, "PowerAppApprover", "Power App Request state updated to "+state,
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
			log.error("Invalid token {} for updating the request for app {} ",powerBiApproverKey, existingApp.getId()+"-"+existingApp.getName());
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}
	}
    
}
