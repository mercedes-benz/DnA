package com.mb.dna.datalakehouse.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mb.dna.datalakehouse.dto.TrinoConnectorVO;
import com.mb.dna.datalakehouse.dto.TrinoConnectorsCollectionVO;
import com.mb.dna.datalakehouse.service.TrinoConnectorService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Trino Connectors Details API", tags = { "trino" })
@RequestMapping("/api")
@Slf4j
public class TrinoConnectorsDetailsController {

	@Autowired
	private TrinoConnectorService trinoConnectorsService;
	
	private static String[] TRINO_RESERVE_WORDS = {"ALTER","AND","AS","BETWEEN","BY","CASE","CAST","CONSTRAINT","CREATE","CROSS","CUBE","CURRENT_CATALOG",
	                                               "CURRENT_DATE","CURRENT_PATH","CURRENT_ROLE","CURRENT_SCHEMA","CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_USER",
	                                               "DEALLOCATE","DELETE","DESCRIBE","DISTINCT","DROP","ELSE","END","ESCAPE","EXCEPT","EXECUTE","EXISTS","EXTRACT",
	                                               "FALSE","FOR","FROM","FULL","GROUP","GROUPING","HAVING","IN","INNER","INSERT","INTERSECT","INTO","IS","JOIN",
	                                               "JSON_ARRAY","JSON_EXISTS","JSON_OBJECT","JSON_QUERY","JSON_TABLE","JSON_VALUE","LEFT","LIKE","LISTAGG",
	                                               "LOCALTIME","LOCALTIMESTAMP","NATURAL","NORMALIZE","NOT","NULL","ON","OR","ORDER","OUTER","PREPARE","RECURSIVE",
	                                               "RIGHT","ROLLUP","SELECT","SKIP","TABLE","THEN","TRIM","TRUE","UESCAPE","UNION","UNNEST","USING","VALUES","WHEN","WHERE","WITH"};
	
	@ApiOperation(value = "Get all available trino connectors details.", nickname = "getAll", notes = "Get all trino connectors details. This endpoints will be used to Get all valid available trino connectors and their details like formats supported and dataTypes.", response = TrinoConnectorsCollectionVO.class, tags = {
			"trino", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all connector details", response = TrinoConnectorsCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/connectors", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoConnectorsCollectionVO> getAll() {
		final List<TrinoConnectorVO> connectors = trinoConnectorsService.getAll();		
		TrinoConnectorsCollectionVO connectorsCollection = new TrinoConnectorsCollectionVO();
		connectorsCollection.setReserveWords(Arrays.asList(TRINO_RESERVE_WORDS));
		log.info("Sending all trino connectors and their details");
		if (connectors != null && connectors.size() > 0) {
			connectorsCollection.setConnectors(connectors);
			return new ResponseEntity<>(connectorsCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(connectorsCollection, HttpStatus.NO_CONTENT);
		}
	}
	
}
