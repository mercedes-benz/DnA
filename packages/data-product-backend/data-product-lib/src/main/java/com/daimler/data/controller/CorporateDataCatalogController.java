package com.daimler.data.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.dataproductlov.CorporateDataCatalogsApi;
import com.daimler.data.dto.dataproductlov.CorporateDataCatalogVO;
import com.daimler.data.service.dataproductlov.CorporateDataCatalogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "corporateDataCatalogs",tags = { "corporateDataCatalogs" })
@RequestMapping(value = "/api")
@RestController
public class CorporateDataCatalogController implements CorporateDataCatalogsApi{
	
	@Autowired
	private CorporateDataCatalogService corporateDataCatalogService;
	
	private static Logger log = LoggerFactory.getLogger(CorporateDataCatalogController.class);
	
//	 @ApiOperation(value = "Get all available corporateDataCatalogs.", nickname = "getAll", notes = "Get all corporateDataCatalogs. This endpoints will be used to Get all valid available corporateDataCatalogs maintenance records.", response = CorporateDataCatalogCollection.class, tags={ "corporateDataCatalogs", })
//	    @ApiResponses(value = { 
//	        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = CorporateDataCatalogCollection.class),
//	        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
//	        @ApiResponse(code = 400, message = "Bad request."),
//	        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
//	        @ApiResponse(code = 403, message = "Request is not authorized."),
//	        @ApiResponse(code = 405, message = "Method not allowed"),
//	        @ApiResponse(code = 500, message = "Internal error") })
//	    @RequestMapping(value = "/corporateDataCatalogs",
//	        produces = { "application/json" }, 
//	        consumes = { "application/json" },
//	        method = RequestMethod.GET)
//	
//	public ResponseEntity<CorporateDataCatalogCollection> getAll(
//			@ApiParam(value = "Sort corporateDataCatalogs by a given variable like name", allowableValues = "name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
//			@ApiParam(value = "Sort corporateDataCatalogs based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
//			@ApiParam(value = "page number from which listing of corporateDataCatalogs should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
//			@ApiParam(value = "size to limit the number of corporateDataCatalogs, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
//		
//		try {
//			int defaultLimit = 10;
//			if (offset == null || offset < 0)
//				offset = 0;
//			if (limit == null || limit < 0) {
//				limit = defaultLimit;
//			}
//			Long count = corporateDataCatalogService.getCount();				
//			List<CorporateDataCatalogVO> corporateDataCatalogs = corporateDataCatalogService.getCorporateDataCatalogs(offset,limit,sortOrder);
//			CorporateDataCatalogCollection corporateDataCatalogCollection = new CorporateDataCatalogCollection();
//			corporateDataCatalogCollection.setTotalCount(count.intValue());
//			if (corporateDataCatalogs != null && corporateDataCatalogs.size() > 0) {
//				corporateDataCatalogCollection.setData(corporateDataCatalogs);
//				log.debug("Returning available corporateDataCatalogs");
//				return new ResponseEntity<>(corporateDataCatalogCollection, HttpStatus.OK);
//			} else {
//				log.debug("No corporateDataCatalogs available, returning empty");
//				return new ResponseEntity<>(corporateDataCatalogCollection, HttpStatus.NO_CONTENT);
//			}
//		}
//		catch(Exception e) {
//			log.error("Exception Occured: {}", e.getMessage());
//			throw e;
//		}					
//	}
	
	 @ApiOperation(value = "get the corporateDataCatalogs  by given ID.", nickname = "getByID", notes = "get the corporateDataCatalogs  by given ID", response = CorporateDataCatalogVO.class, tags={ "corporateDataCatalogs", })
	    @ApiResponses(value = { 
	        @ApiResponse(code = 200, message = "Returns message of success or failure", response = CorporateDataCatalogVO.class),
	        @ApiResponse(code = 400, message = "Bad request"),
	        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
	        @ApiResponse(code = 403, message = "Request is not authorized."),
	        @ApiResponse(code = 404, message = "Invalid id, record not found."),
	        @ApiResponse(code = 500, message = "Internal error") })
	    @RequestMapping(value = "/corporateDataCatalogs/{id}",
	        produces = { "application/json" }, 
	        consumes = { "application/json" },
	        method = RequestMethod.GET)
	public ResponseEntity<CorporateDataCatalogVO> getByID(
    		@ApiParam(value = "Id of the corporateDataCatalog",required=true) @PathVariable("id") String id){
	
		 CorporateDataCatalogVO corporateDataCatalogVO = null;
		if (StringUtils.hasText(id)) {
			CorporateDataCatalogVO existingVO = corporateDataCatalogService.getById(id);
			if (existingVO != null && existingVO.getId() != null) {
				corporateDataCatalogVO = existingVO;
			} 
		}
		if(corporateDataCatalogVO != null) {
			log.info("corporateDataCatalog {} fetched successfully", id);
			return new ResponseEntity<>(corporateDataCatalogVO, HttpStatus.OK);
		}else {
			log.info("No corporateDataCatalog {} found", id);
			return new ResponseEntity<>(new CorporateDataCatalogVO(), HttpStatus.NO_CONTENT);
		}
	}

}
