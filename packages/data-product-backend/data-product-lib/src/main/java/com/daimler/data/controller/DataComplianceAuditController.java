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

import com.daimler.data.api.datacomplianceAudit.DatacomplianceApi;
import com.daimler.data.dto.datacomplianceAudit.DataComplianceAuditCollectionVO;
import com.daimler.data.dto.datacomplianceAudit.DataComplianceAuditVO;
import com.daimler.data.service.datacomplianceAudit.DataComplianceAuditService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "datacomplianceAudit",tags = { "datacomplianceAudit" })
@RequestMapping(value = "/api")
@RestController
public class DataComplianceAuditController implements DatacomplianceApi{
	
	private static Logger LOGGER = LoggerFactory.getLogger(DataComplianceAuditController.class);
	
	@Autowired
	private DataComplianceAuditService dataComplianceAuditService;

	
	@Override
	 @ApiOperation(value = "Get all available DataCompliance NetworkList auditLogs.", nickname = "getAll", notes = "Get all available DataCompliance NetworkList auditLogs. This endpoint will be used to Get all valid available auditLogs maintenance records.", response = DataComplianceAuditCollectionVO.class, tags={ "datacomplianceAudit", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = DataComplianceAuditCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datacompliance/audit",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<DataComplianceAuditCollectionVO> getAll(
			@ApiParam(value = "page number from which listing of audits should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "size to limit the number of audits, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			Long count = dataComplianceAuditService.getAuditCount(null);
			DataComplianceAuditCollectionVO dataComplianceAuditCollection = new DataComplianceAuditCollectionVO();						
			dataComplianceAuditCollection.setTotalCount(count.intValue());
			LOGGER.debug("Fetching Data Compliance Network List Audits");			
			List<DataComplianceAuditVO> dataComplianceAudits = dataComplianceAuditService.getDataComplianceAudits(offset,limit);
			if (!ObjectUtils.isEmpty(dataComplianceAudits)) {				
				dataComplianceAuditCollection.setRecords(dataComplianceAudits);
				return new ResponseEntity<>(dataComplianceAuditCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(dataComplianceAuditCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {
			LOGGER.error("Exception Occured: {}", e.getMessage());
			throw e;
		}
		
	}

	@Override
	@ApiOperation(value = "Get all available DataCompliance NetworkList auditLogs for a selected entity ID.", nickname = "getByEntityId", notes = "Get all available DataCompliance NetworkList auditLogs. This endpoint will be used to get all valid available auditLogs for a selected entity ID maintenance records.", response = DataComplianceAuditCollectionVO.class, tags={ "datacomplianceAudit", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = DataComplianceAuditCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datacompliance/audit/{entityId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<DataComplianceAuditCollectionVO> getByEntityId(
			@ApiParam(value = "Audit logs of entityId to be fetched",required=true) @PathVariable("entityId") String entityId,
			@ApiParam(value = "page number from which listing of audits should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "size to limit the number of audits, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		
		try {
			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			
			Long count = dataComplianceAuditService.getAuditCount(entityId);			
			DataComplianceAuditCollectionVO dataComplianceAuditCollection = new DataComplianceAuditCollectionVO();						
			dataComplianceAuditCollection.setTotalCount(count.intValue());
			LOGGER.debug("Fetching Data Compliance Network List Audits");
			List<DataComplianceAuditVO> dataComplianceAudits = dataComplianceAuditService.getAuditsByEntityId(entityId, offset, limit);
			if (!ObjectUtils.isEmpty(dataComplianceAudits)) {				
				dataComplianceAuditCollection.setRecords(dataComplianceAudits);
				return new ResponseEntity<>(dataComplianceAuditCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(dataComplianceAuditCollection, HttpStatus.NO_CONTENT);
			}
		}
		catch(Exception e) {			
			LOGGER.error("Exception Occured: {}", e.getMessage());
			throw e;		
		}		
		
	}


	
}
