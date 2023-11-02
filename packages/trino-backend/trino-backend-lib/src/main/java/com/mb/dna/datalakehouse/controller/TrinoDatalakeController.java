package com.mb.dna.datalakehouse.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.mb.dna.datalakehouse.dto.TrinoConnectorsCollectionVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectCollectionVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectResponseVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectUpdateRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;
import com.mb.dna.datalakehouse.service.TrinoDatalakeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Trino Connectors Details API", tags = { "datalakes" })
@RequestMapping("/api")
@Slf4j
public class TrinoDatalakeController {

	@Autowired
	private UserStore userStore;
	
	@Autowired
	private TrinoDatalakeService trinoDatalakeService;
	
	@ApiOperation(value = "Get all available trino datalake projects.", nickname = "getAll", notes = "Get all trino datalake projects. This endpoints will be used to Get all valid available datalake projects", response = TrinoConnectorsCollectionVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all datalake projects", response = TrinoDataLakeProjectCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeProjectCollectionVO> getAll() {
		final List<TrinoDataLakeProjectVO> projects = trinoDatalakeService.getAll();
		Long count = trinoDatalakeService.getCount(0, 0);
		TrinoDataLakeProjectCollectionVO collectionVO = new TrinoDataLakeProjectCollectionVO();
		log.debug("Sending all trino datalake projects and their details");
		if (projects != null && projects.size() > 0) {
			collectionVO.setData(projects);
			collectionVO.setTotalCount(count);
			return new ResponseEntity<>(collectionVO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(collectionVO, HttpStatus.NO_CONTENT);
		}
	}
	
	@ApiOperation(value = "Get datalake project details for a given Id.", nickname = "getById", notes = "Get datalake project details for a given Id.", response = TrinoDataLakeProjectVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeProjectVO> getById(
			@ApiParam(value = "Data Lake project ID to be fetched", required = true) @PathVariable("id") String id){
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
		if(existingProject==null || !id.equalsIgnoreCase(existingProject.getId())) {
			log.warn("No datalake project found with id {}, failed to fetch saved inputs for given id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(existingProject, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "Create Data Lake project for user.", nickname = "createDataLakeProject", notes = "Create Data Lake project for user ", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> createDataLakeProject(
			@ApiParam(value = "Request Body that contains data required for create Data Lake project for user", required = true) @Valid @RequestBody TrinoDataLakeProjectRequestVO requestVO) {
		
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectVO request = requestVO.getData();
		
		return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
	}
	
	@ApiOperation(value = "update datalake project details for a given Id.", nickname = "updateById", notes = "update datalake project details for a given Id.", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> updateById(
			@ApiParam(value = "Data Lake project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating of datalake project details like add/remove tables and manage collaborators of tables", required = true) @Valid @RequestBody TrinoDataLakeProjectUpdateRequestVO datalakeUpdateRequestVO) {
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();

		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}
	
	
}
