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

import com.daimler.data.api.dataproductlov.CarlafunctionsApi;
import com.daimler.data.dto.dataproductlov.CarlaFunctionVO;
import com.daimler.data.dto.dataproductlov.CarlafunctionCollection;
import com.daimler.data.service.dataproductlov.CarlaFunctionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "carlafunctions",tags = { "carlafunctions" })
@RequestMapping(value = "/api")
@RestController
public class CarlaFunctionController implements CarlafunctionsApi{
	
	@Autowired
	private CarlaFunctionService carlaFunctionService;
	
	private static Logger log = LoggerFactory.getLogger(CarlaFunctionController.class);
	
	@ApiOperation(value = "Get all available carlafunctions.", nickname = "getAll", notes = "Get all carlafunctions. This endpoints will be used to Get all valid available carlafunctions maintenance records.", response = CarlafunctionCollection.class, tags={ "carlafunctions", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = CarlafunctionCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/carlafunctions",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	
	public ResponseEntity<CarlafunctionCollection> getAll(
    		@ApiParam(value = "Sort carlafunctions by a given variable like name", allowableValues = "name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort carlafunctions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
    		@ApiParam(value = "page number from which listing of carlafunctions should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "size to limit the number of carlafunctions, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			Long count = carlaFunctionService.getCount();
			List<CarlaFunctionVO> carlafunctions = carlaFunctionService.getCarlafunctions(offset,limit,sortOrder);
			CarlafunctionCollection carlafunctionCollection = new CarlafunctionCollection();
			carlafunctionCollection.setTotalCount(count.intValue());
			if (carlafunctions != null && carlafunctions.size() > 0) {
				carlafunctionCollection.setData(carlafunctions);
				log.debug("Returning available carlafunctions");
				return new ResponseEntity<>(carlafunctionCollection, HttpStatus.OK);
			} else {
				log.debug("No carlafunctions available, returning empty");
				return new ResponseEntity<>(carlafunctionCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {
			log.error("Exception Occured: {}", e.getMessage());
			throw e;
		}
				
	
	}
	
	@ApiOperation(value = "get the carlafunctions  by given ID.", nickname = "getByID", notes = "get the carlafunctions  by given ID", response = CarlafunctionCollection.class, tags={ "carlafunctions", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CarlafunctionCollection.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/carlafunctions/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<CarlaFunctionVO> getByID(
    		@ApiParam(value = "Id of the carlafunction",required=true) @PathVariable("id") String id){
	
		CarlaFunctionVO carlaFunctionVO = null;
		if (StringUtils.hasText(id)) {
			CarlaFunctionVO existingVO = carlaFunctionService.getById(id);
			if (existingVO != null && existingVO.getId() != null) {
				carlaFunctionVO = existingVO;
			} 
		}
		if(carlaFunctionVO != null) {
			log.info("CarlaFunction {} fetched successfully", id);
			return new ResponseEntity<>(carlaFunctionVO, HttpStatus.OK);
		}else {
			log.info("No CarlaFunction {} found", id);
			return new ResponseEntity<>(new CarlaFunctionVO(), HttpStatus.NO_CONTENT);
		}
	}

}
