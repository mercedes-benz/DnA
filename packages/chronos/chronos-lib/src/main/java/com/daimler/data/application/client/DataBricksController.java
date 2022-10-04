package com.daimler.data.application.client;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.forecast.RunUpdateRequestWrapperVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Forecast Run update API")
@RequestMapping("/client/databricks}")
@Slf4j
public class DataBricksController {

	@ApiOperation(value = "api for databricks to update the run details upon finishing the run", nickname = "updateRun", notes = "", response = GenericMessage.class, tags={ "forecast-runs", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/forecasts/runs/{correlationId",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<GenericMessage> updateRun(@ApiParam(value = "Request Body that contains updated details of run" ,required=true )  @Valid @RequestBody RunUpdateRequestWrapperVO forecastRunUpdateRequestVO){
		return null;
	}
    
}
