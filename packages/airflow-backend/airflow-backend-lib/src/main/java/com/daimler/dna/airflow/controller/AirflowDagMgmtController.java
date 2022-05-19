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

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.dna.airflow.api.DagsApi;
import com.daimler.dna.airflow.app.main.auth.UserStore;
import com.daimler.dna.airflow.client.AirflowGitClient;
import com.daimler.dna.airflow.dto.AirflowDagRequestVo;
import com.daimler.dna.airflow.dto.AirflowDagResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagRetryRequestVO;
import com.daimler.dna.airflow.dto.AirflowDagRetryResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagUpdateRequestVO;
import com.daimler.dna.airflow.dto.AirflowDagUpdateResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagVo;
import com.daimler.dna.airflow.dto.AirflowProjectRequestVO;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowRetryDagVo;
import com.daimler.dna.airflow.dto.CreatedByVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.exceptions.MessageDescription;
import com.daimler.dna.airflow.service.DagMgmtService;
import com.daimler.dna.airflow.service.DnaProjectServiceImpl;
import com.daimler.dna.airflow.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1")
@Api(value = "Dags", tags = { "dags" })
public class AirflowDagMgmtController implements DagsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(AirflowDagMgmtController.class);

	@Autowired
	private AirflowGitClient gitClient;

	@Autowired
	private UserStore userStore;

	@Autowired
	private DnaProjectServiceImpl dnaService;

	@Autowired
	private DagMgmtService dagMgmtService;

//	@Override
//	public ResponseEntity<AirflowDagResponseVO> createDag(@Valid AirflowProjectRequestVO airflowProjectRequestVO) {
//		AirflowDagResponseVO response=null;		
//		AirflowProjectUserVO currentUser = this.userStore.getVO();		
//		List<MessageDescription> errors = gitClient.createAirflowDags(airflowProjectRequestVO.getData(),currentUser);
//		//List<MessageDescription> errors = gitClient.updateAirflowDags(airflowProjectRequestVO.getData(),currentUser);
//		if(ObjectUtils.isEmpty(errors)) {
//			response = new AirflowDagResponseVO();
//			response.setErrors(errors);
//			return new ResponseEntity<AirflowDagResponseVO>(response, HttpStatus.OK);
//		}else {
//			response = new AirflowDagResponseVO();
//			response.setErrors(errors);
//			return new ResponseEntity<AirflowDagResponseVO>(response, HttpStatus.BAD_REQUEST);
//		}
//	}

	@Override
	public ResponseEntity<AirflowDagVo> getDagByName(@NotNull @Valid String dagName) {
		AirflowDagVo airflowDagVo = null;
		HttpStatus httpStatus = null;
		if (StringUtils.hasText(gitClient.getDagById(dagName))) {
			airflowDagVo = new AirflowDagVo();
			airflowDagVo.setDagContent(gitClient.getDagById(dagName));
			airflowDagVo.setDagName(dagName);
			httpStatus = HttpStatus.OK;
		} else {
			httpStatus = HttpStatus.NO_CONTENT;
		}
		return new ResponseEntity<AirflowDagVo>(airflowDagVo, httpStatus);

	}

	@Override
	@ApiOperation(value = "Update an existing DAG file", nickname = "updateDag", notes = "Airflow Dag will be updated api", response = AirflowDagResponseVO.class, tags = {
			"dags", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = AirflowDagResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = com.daimler.dna.airflow.exceptions.GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dags", method = RequestMethod.PUT)
	public ResponseEntity<AirflowDagUpdateResponseVO> updateDag(
			@ApiParam(value = "Request Body that contains data to create dag in airflow", required = true) @Valid @RequestBody AirflowDagUpdateRequestVO airflowDagUpdateRequestVO) {
		LOGGER.trace("Entering updateDag.");
		return dagMgmtService.updateAirflowDag(airflowDagUpdateRequestVO.getData());
	}

	@ApiOperation(value = "Delete DAG for a given dagName.", nickname = "delete", notes = "Delete DAG for a given identifier.", response = com.daimler.dna.airflow.exceptions.GenericMessage.class, tags = {
			"dags", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = com.daimler.dna.airflow.exceptions.GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dags/{dagName}/{projectId}", method = RequestMethod.DELETE)
	@Override
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "DAG name to be deleted.", required = true) @PathVariable("dagName") String dagName,
			@ApiParam(value = "Project Id for associated DAG.", required = true) @PathVariable("projectId") String projectId) {
		LOGGER.trace("Entering delete DAG.");
		return dagMgmtService.deleteDag(dagName, projectId);
	}

	@Override
	public ResponseEntity<AirflowDagRetryResponseVO> permissionDetails(String dagName, String projectId) {
		AirflowRetryDagVo data = dagMgmtService.getPermission(dagName, projectId);
		AirflowDagRetryResponseVO res = new AirflowDagRetryResponseVO();
		res.setStatus("SUCCESS");
		res.setData(data);
		return new ResponseEntity<AirflowDagRetryResponseVO>(res, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AirflowDagRetryResponseVO> updatePermission(String dagName, String projectId,
			@Valid AirflowDagRetryRequestVO airflowRetryDagVo) {
		return dagMgmtService.updatePermission(dagName, projectId, airflowRetryDagVo.getData());
	}
}
