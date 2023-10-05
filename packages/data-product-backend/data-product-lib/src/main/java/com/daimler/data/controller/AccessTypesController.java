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

import com.daimler.data.api.dataproductlov.AccesstypesApi;
import com.daimler.data.dto.dataproductlov.AccessTypesVO;
import com.daimler.data.dto.dataproductlov.AccessTypesCollection;
import com.daimler.data.service.dataproductlov.AccessTypesService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "accesstypes",tags = { "accesstypes" })
@RequestMapping(value = "/api")
@RestController
public class AccessTypesController implements AccesstypesApi {
	
	@Autowired
	private AccessTypesService accessTypesService;
	
	private static Logger log = LoggerFactory.getLogger(AccessTypesController.class);
	
	@ApiOperation(value = "Get all available accesstypes", nickname = "getAll", notes = "Get all accesstypes. This endpoints will be used to Get all valid available accesstypes maintenance records.", response = AccessTypesCollection.class, tags={ "accesstypes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = AccessTypesCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/accesstypes",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	
	public ResponseEntity<AccessTypesCollection> getAll(
    		@ApiParam(value = "Sort accesstypes by a given variable like name", allowableValues = "name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort accesstypes based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
    		@ApiParam(value = "page number from which listing of accesstypes should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "size to limit the number of accesstypes, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			Long count = accessTypesService.getCount();
			List<AccessTypesVO> accesstypes = accessTypesService.getAccessTypes(offset,limit,sortOrder);
			AccessTypesCollection accessTypesCollection = new AccessTypesCollection();
			accessTypesCollection.setTotalCount(count.intValue());
			if (accesstypes != null && accesstypes.size() > 0) {
				accessTypesCollection.setData(accesstypes);
				log.debug("Returning available accesstypes");
				return new ResponseEntity<>(accessTypesCollection, HttpStatus.OK);
			} else {
				log.debug("No accesstypes available, returning empty");
				return new ResponseEntity<>(accessTypesCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {
			log.error("Exception Occured: {}", e.getMessage());
			throw e;
		}
				
	
	}
	
	// @ApiOperation(value = "get the accesstypes  by given ID.", nickname = "getByID", notes = "get the accesstypes  by given ID", response = AccessTypesCollection.class, tags={ "accesstypes", })
    // @ApiResponses(value = { 
    //     @ApiResponse(code = 200, message = "Returns message of success or failure", response = AccessTypesCollection.class),
    //     @ApiResponse(code = 400, message = "Bad request"),
    //     @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
    //     @ApiResponse(code = 403, message = "Request is not authorized."),
    //     @ApiResponse(code = 404, message = "Invalid id, record not found."),
    //     @ApiResponse(code = 500, message = "Internal error") })
    // @RequestMapping(value = "/accesstypes/{id}",
    //     produces = { "application/json" }, 
    //     consumes = { "application/json" },
    //     method = RequestMethod.GET)
	// public ResponseEntity<AccessTypesVO> getByID(
    // 		@ApiParam(value = "Id of the accesstype",required=true) @PathVariable("id") String id){
	
	// 	AccessTypesVO accessTypesVO = null;
	// 	if (StringUtils.hasText(id)) {
	// 		AccessTypesVO existingVO = accessTypesService.getById(id);
	// 		if (existingVO != null && existingVO.getId() != null) {
	// 			accessTypesVO = existingVO;
	// 		} 
	// 	}
	// 	if(accessTypesVO != null) {
	// 		log.info("accesstype {} fetched successfully", id);
	// 		return new ResponseEntity<>(accessTypesVO, HttpStatus.OK);
	// 	}else {
	// 		log.info("No accesstype {} found", id);
	// 		return new ResponseEntity<>(new AccessTypesVO(), HttpStatus.NO_CONTENT);
	// 	}
	// }
}
