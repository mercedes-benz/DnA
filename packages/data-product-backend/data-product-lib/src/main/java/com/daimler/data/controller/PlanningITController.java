package com.daimler.data.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.planningit.PlanningitApi;
import com.daimler.data.dto.planningit.PlanningITVO;
import com.daimler.data.dto.planningit.PlanningITVOCollection;
import com.daimler.data.service.planningit.PlanningITService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Planningit", tags = { "planningit" })
@RequestMapping("/api")
public class PlanningITController implements PlanningitApi {
	
	@Autowired
	private PlanningITService service;
	
	@ApiOperation(value = "Get all planningit systems.", nickname = "getAll", notes = "This endpoints will be used to Get all valid available planningit systems.", response = PlanningITVOCollection.class, tags={ "planningit", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of succes or failure", response = PlanningITVOCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/planningit",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PlanningITVOCollection> getAll(){
		PlanningITVOCollection collection = new PlanningITVOCollection();
		List<PlanningITVO> records = service.getAll();
		HttpStatus responseCode = HttpStatus.NO_CONTENT;
		if(records!=null && !records.isEmpty()) {
			collection.setData(records);
			responseCode = HttpStatus.OK;
		}
		return new ResponseEntity<>(collection, responseCode);
	}

}
