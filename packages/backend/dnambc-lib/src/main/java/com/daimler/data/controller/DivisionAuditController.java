package com.daimler.data.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.divisionAudit.DivisionApi;
import com.daimler.data.dto.divisionAudit.DivisionAuditCollectionVO;
import com.daimler.data.dto.divisionAudit.DivisionAuditVO;
import com.daimler.data.service.divisionAudit.DivisionAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "divisionAudit",tags = { "divisionAudit" })
@RequestMapping(value = "/api")
@RestController
public class DivisionAuditController implements DivisionApi{
	
	private static Logger LOGGER = LoggerFactory.getLogger(DivisionAuditController.class);
	
	@Autowired
	private DivisionAuditService divisionAuditService;

	
	@Override
	@ApiOperation(value = "Get all available auditLogs.", nickname = "getAll", notes = "Get all auditLogs. This endpoints will be used to Get all valid available auditLogs maintenance records.", response = DivisionAuditCollectionVO.class, tags={ "divisionAudit", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = DivisionAuditCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/division/audit",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<DivisionAuditCollectionVO> getAll(
			@ApiParam(value = "page number from which listing of audits should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "size to limit the number of audits, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			Long count = divisionAuditService.getAuditCount(null);
			DivisionAuditCollectionVO divisionAuditCollection = new DivisionAuditCollectionVO();			
			
			divisionAuditCollection.setTotalCount(count.intValue());
			LOGGER.debug("Fetching Division Audits");			
			List<DivisionAuditVO> divisionAudits = divisionAuditService.getDivisionAudits(offset,limit);
			if (!ObjectUtils.isEmpty(divisionAudits)) {				
				divisionAuditCollection.setRecords(divisionAudits);
				return new ResponseEntity<>(divisionAuditCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(divisionAuditCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {
			LOGGER.error("Exception Occured: {}", e.getMessage());
			throw e;
		}
		
	}

	@Override
	@ApiOperation(value = "Get all available DivisionAudit Logs for a selected division ID.", nickname = "getByDivisionId", notes = "Get all DivisionAudit Logs. This endpoints will be used to Get all valid available auditLogs for a given divisionId maintenance records.", response = DivisionAuditCollectionVO.class, tags={ "divisionAudit", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = DivisionAuditCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/division/audit/{divisionId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<DivisionAuditCollectionVO> getByDivisionId(
			@ApiParam(value = "Audit logs of DivisionID to be fetched",required=true) @PathVariable("divisionId") String divisionId,
			@ApiParam(value = "page number from which listing of audits should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "size to limit the number of audits, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			
			Long count = divisionAuditService.getAuditCount(divisionId);			
			DivisionAuditCollectionVO divisionAuditCollection = new DivisionAuditCollectionVO();						
			divisionAuditCollection.setTotalCount(count.intValue());
			LOGGER.debug("Fetching Division Audits");
			List<DivisionAuditVO> divisionAudits = divisionAuditService.getAuditsByDivisionId(divisionId, offset, limit);
			if (!ObjectUtils.isEmpty(divisionAudits)) {				
				divisionAuditCollection.setRecords(divisionAudits);
				return new ResponseEntity<>(divisionAuditCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(divisionAuditCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {			
			LOGGER.error("Exception Occured: {}", e.getMessage());
			throw e;		
		}		
		
	}


	
}
