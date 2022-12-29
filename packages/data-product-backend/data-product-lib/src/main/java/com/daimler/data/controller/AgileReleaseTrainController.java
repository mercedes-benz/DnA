package com.daimler.data.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.dataproductlov.AgileReleaseTrainsApi;
import com.daimler.data.dto.dataproductlov.AgileReleaseTrainCollection;
import com.daimler.data.dto.dataproductlov.AgileReleaseTrainVO;
import com.daimler.data.dto.dataproductlov.CarlafunctionCollection;
import com.daimler.data.service.dataproductlov.AgileReleaseTrainService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "agileReleaseTrains",tags = { "agileReleaseTrains" })
@RequestMapping(value = "/api")
@RestController
public class AgileReleaseTrainController implements AgileReleaseTrainsApi{
	
	@Autowired
	private AgileReleaseTrainService agileReleaseTrainService;
	
	private static Logger log = LoggerFactory.getLogger(AgileReleaseTrainController.class);
	
	 @ApiOperation(value = "Get all available agileReleaseTrains.", nickname = "getAll", notes = "Get all agileReleaseTrains. This endpoints will be used to Get all valid available agileReleaseTrains maintenance records.", response = AgileReleaseTrainCollection.class, tags={ "agileReleaseTrains", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = CarlafunctionCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
	 @RequestMapping(value = "/agileReleaseTrains",
     produces = { "application/json" }, 
     consumes = { "application/json" },
     method = RequestMethod.GET)
	
	public ResponseEntity<AgileReleaseTrainCollection> getAll(
			@ApiParam(value = "Sort agileReleaseTrains by a given variable like name", allowableValues = "name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
			@ApiParam(value = "Sort agileReleaseTrains based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
			@ApiParam(value = "page number from which listing of agileReleaseTrains should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "size to limit the number of agileReleaseTrains, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			Long count = agileReleaseTrainService.getCount();				
			List<AgileReleaseTrainVO> agileReleaseTrains = agileReleaseTrainService.getAgileReleaseTrains(offset,limit,sortOrder);
			AgileReleaseTrainCollection agileReleaseTrainCollection = new AgileReleaseTrainCollection();
			agileReleaseTrainCollection.setTotalCount(count.intValue());
			if (agileReleaseTrains != null && agileReleaseTrains.size() > 0) {
				agileReleaseTrainCollection.setData(agileReleaseTrains);
				log.debug("Returning available AgileReleaseTrainCollection");
				return new ResponseEntity<>(agileReleaseTrainCollection, HttpStatus.OK);
			} else {
				log.debug("No AgileReleaseTrains available, returning empty");
				return new ResponseEntity<>(agileReleaseTrainCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {
			log.error("Exception Occured: {}", e.getMessage());
			throw e;
		}
				
	
	}
	
	 @ApiOperation(value = "get the agileReleaseTrains  by given ID.", nickname = "getByID", notes = "get the agileReleaseTrains  by given ID", response = AgileReleaseTrainVO.class, tags={ "agileReleaseTrains", })
	    @ApiResponses(value = { 
	        @ApiResponse(code = 200, message = "Returns message of success or failure", response = AgileReleaseTrainVO.class),
	        @ApiResponse(code = 400, message = "Bad request"),
	        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
	        @ApiResponse(code = 403, message = "Request is not authorized."),
	        @ApiResponse(code = 404, message = "Invalid id, record not found."),
	        @ApiResponse(code = 500, message = "Internal error") })
	    @RequestMapping(value = "/agileReleaseTrains/{id}",
	        produces = { "application/json" }, 
	        consumes = { "application/json" },
	        method = RequestMethod.GET)
	public ResponseEntity<AgileReleaseTrainVO> getByID(
			@ApiParam(value = "Id of the agileReleaseTrains",required=true) @PathVariable("id") String id){
	
		AgileReleaseTrainVO agileReleaseTrainVO = null;
		if (StringUtils.hasText(id)) {
			AgileReleaseTrainVO existingVO = agileReleaseTrainService.getById(id);
			if (existingVO != null && existingVO.getId() != null) {
				agileReleaseTrainVO = existingVO;
			} 
		}
		if(agileReleaseTrainVO != null) {
			log.info("AgileReleaseTrain {} fetched successfully", id);
			return new ResponseEntity<>(agileReleaseTrainVO, HttpStatus.OK);
		}else {
			log.info("No AgileReleaseTrain {} found", id);
			return new ResponseEntity<>(new AgileReleaseTrainVO(), HttpStatus.NO_CONTENT);
		}
	}

}
