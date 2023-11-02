package com.mb.dna.datalakehouse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mb.dna.datalakehouse.dto.TrinoAccessResponseVO;
import com.mb.dna.datalakehouse.dto.TrinoAccessVO;
import com.mb.dna.datalakehouse.service.TrinoAccessService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Trino access control details API", tags = { "trino" })
@RequestMapping("/api")
@Slf4j
public class TrinoAccessConfigController {

	@Autowired
	private TrinoAccessService service;
	
	@ApiOperation(value = "Get all trino access configuration details.", nickname = "getAll", notes = "Get all ttrino access configuration details. This endpoints will be used to get trino access configuration details like catalog rules, schema rules and table rules", response = TrinoAccessResponseVO.class, tags = {
			"trino", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all access details", response = TrinoAccessResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/access", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoAccessResponseVO> getAll() {
		final List<TrinoAccessVO> accessRulesRecords = service.getAll();
		TrinoAccessResponseVO accessDetails = new TrinoAccessResponseVO();
		log.debug("Sending all trino access details");
		if (accessRulesRecords != null && accessRulesRecords.size() > 0) {
			accessDetails.setData(accessRulesRecords.get(0));
		}
		return new ResponseEntity<>(accessDetails, HttpStatus.OK);
	}
	
}
