package com.daimler.data.application.config;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.planningit.PlanningITVO;
import com.daimler.data.dto.planningit.PlanningITVOCollection;
import com.daimler.data.service.planningit.PlanningITService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Planningit", tags = { "planningit" })
@RequestMapping("/service")
public class PlanningITServiceController {

	@Autowired
	private PlanningITService service;
	
	@ApiOperation(value = "Bulk add planning IT systems", nickname = "insertAll", notes = "Bulk add planning IT systems", response = GenericMessage.class, tags={ "planningit", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/planningit",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> insertAll(@ApiParam(value = "Request Body that contains data required for creating a new dataproduct provider form" ,
    	required=true )  @Valid @RequestBody PlanningITVOCollection planningITCollectionRequestVO){
		HttpStatus responseCode = HttpStatus.CREATED;
		GenericMessage responseMessage = new GenericMessage();
		if(planningITCollectionRequestVO!=null) {
			List<PlanningITVO> volist = planningITCollectionRequestVO.getData();
			try {
				service.bulkInsert(volist);
				responseMessage.setSuccess("SUCCESS");
			}catch(Exception e) {
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errMsg = new MessageDescription("Failed while inserting records with exception " + e.getMessage());
				errors.add(errMsg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}
		return new ResponseEntity<>(responseMessage,responseCode);
	}
    
}
