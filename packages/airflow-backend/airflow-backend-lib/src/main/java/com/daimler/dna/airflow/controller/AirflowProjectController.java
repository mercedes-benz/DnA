/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.dna.airflow.controller;

import java.util.Objects;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.dna.airflow.api.ProjectsApi;
import com.daimler.dna.airflow.dto.AirflowProjectIdVO;
import com.daimler.dna.airflow.dto.AirflowProjectRequestVO;
import com.daimler.dna.airflow.dto.AirflowProjectResponseVO;
import com.daimler.dna.airflow.dto.AirflowProjectResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.service.DnaProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1")
@Api(value = "Projects", tags = { "projects" })
public class AirflowProjectController implements ProjectsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(AirflowProjectController.class);

	@Autowired
	private DnaProjectService dnaProjectService;

	@ApiOperation(value = "Create airflow projects", nickname = "createProject", notes = "Airflow project will be onboarded with this api", response = AirflowProjectResponseWrapperVO.class, tags = {
			"projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = AirflowProjectResponseWrapperVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = AirflowProjectResponseWrapperVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/projects", method = RequestMethod.POST)
	public ResponseEntity<AirflowProjectResponseWrapperVO> createProject(
			@ApiParam(value = "Request Body that contains data to create user in airflow", required = true) @Valid @RequestBody AirflowProjectRequestVO airflowProjectRequestVO) {
		ResponseEntity<AirflowProjectResponseWrapperVO> res = dnaProjectService
				.createAirflowProject(airflowProjectRequestVO.getData());
		// res.setData(airflowProjectRequestVO.getData());
		return res;
	}

	@ApiOperation(value = "get all airflow projects", nickname = "getAllAirflowProject", notes = "get all airflow projects", response = AirflowProjectResponseVO.class, tags = {
			"projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = AirflowProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/projects", method = RequestMethod.GET)
	public ResponseEntity<AirflowProjectResponseVO> getAllAirflowProject() {
		AirflowProjectResponseVO res = new AirflowProjectResponseVO();
		res.setData(dnaProjectService.getAllProjects(0, 0));
		return new ResponseEntity<AirflowProjectResponseVO>(res, HttpStatus.OK);
	}

	@ApiOperation(value = "get by project id airflow projects", nickname = "getByAirflowProject", notes = "get by project id airflow projects", response = AirflowProjectVO.class, tags = {
			"projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = AirflowProjectVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/projects/{projectId}", method = RequestMethod.GET)
	public ResponseEntity<AirflowProjectVO> getByAirflowProject(
			@ApiParam(value = "project id for which project to be fetched.", required = true) @PathVariable("projectId") String projectId) {
		AirflowProjectVO res = dnaProjectService.getByProjectId(projectId);
		HttpStatus status = HttpStatus.NO_CONTENT;
		if (Objects.nonNull(res)) {
			status = HttpStatus.OK;
		}
		return new ResponseEntity<AirflowProjectVO>(res, status);
	}

	@Override
	public ResponseEntity<AirflowProjectResponseWrapperVO> updatedProject(String projectId,
			@Valid AirflowProjectRequestVO airflowProjectRequestVO) {
		ResponseEntity<AirflowProjectResponseWrapperVO> res = dnaProjectService
				.updateAirflowProject(airflowProjectRequestVO.getData(), projectId);
		return res;
	}

	@Override
	@ApiOperation(value = "get airflow project ID", nickname = "getAirflowProjectId", notes = "get airflow project ID", response = AirflowProjectIdVO.class, tags = {
			"projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = AirflowProjectIdVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/projects/projectid", method = RequestMethod.GET)
	public ResponseEntity<AirflowProjectIdVO> getAirflowProjectId() {
		AirflowProjectIdVO airflowProjectIdVO = new AirflowProjectIdVO();
		airflowProjectIdVO.setProjectId(dnaProjectService.getProjectId());
		return new ResponseEntity<AirflowProjectIdVO>(airflowProjectIdVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Get pipeline status for a projectId.", nickname = "getSatusByProjectId", notes = "Get pipeline status for a projectId.", response = AirflowProjectResponseWrapperVO.class, tags = {
			"projects", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = AirflowProjectResponseWrapperVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = com.daimler.dna.airflow.exceptions.GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/projects/status/{projectId}", method = RequestMethod.GET)
	public ResponseEntity<AirflowProjectResponseWrapperVO> getStatusByProjectId(@ApiParam(value = "Project Id for associated DAG.", required = true) @PathVariable("projectId") String projectId) {
		LOGGER.trace("Entering status of projectId.");
		return dnaProjectService.getAirflowDagStatus(projectId);
	}

}
