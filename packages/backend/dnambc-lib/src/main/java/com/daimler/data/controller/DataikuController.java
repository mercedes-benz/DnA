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

package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.dataiku.DataikuApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.dto.dataiku.DataikuProjectVO;
import com.daimler.data.dto.dataiku.DataikuProjectVOCollection;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.dataiku.DataikuService;
import com.daimler.data.service.userinfo.UserInfoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Dataiku API", tags = { "dataiku" })
@RequestMapping("/api")
@ConditionalOnExpression("${dna.feature.dataiku}")
@Slf4j
public class DataikuController implements DataikuApi {
	private static Logger LOGGER = LoggerFactory.getLogger(DataikuController.class);

	@Autowired
	DataikuService vService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Override
	@ApiOperation(value = "Get all available dataiku projects.", nickname = "getAll", notes = "Get all dataiku projects. This endpoints will be used to Get all valid available dataiku projects.", response = DataikuProjectVOCollection.class, tags = {
			"dataiku", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = DataikuProjectVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataiku/projects", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataikuProjectVOCollection> getAll(
			@NotNull @ApiParam(value = "If requested data from live(Production) or training environment", required = true, defaultValue = "true") @Valid @RequestParam(value = "live", required = true, defaultValue = "true") Boolean live) {
		DataikuProjectVOCollection col = null;
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		List<DataikuProjectVO> extolloprojects = vService.getAllDataikuProjects(userId, live, "eXtollo");
		List<DataikuProjectVO> onPremprojects = live ? vService.getAllDataikuProjects(userId, live, "onPremise") : null;
		List<DataikuProjectVO> consolidateList = new ArrayList<>();
		if(extolloprojects != null && !extolloprojects.isEmpty()) consolidateList.addAll(extolloprojects);
		if(onPremprojects != null && !onPremprojects.isEmpty()) consolidateList.addAll(onPremprojects);
		if (!ObjectUtils.isEmpty(consolidateList)) {
			col = new DataikuProjectVOCollection();
			col.setData(consolidateList);
			col.setTotalCount(consolidateList.size());
		}
		if (col != null) {
			log.debug("Returning projects found for user {} ", userId);
			return new ResponseEntity<>(col, HttpStatus.OK);
		} else {
			log.debug("No dataiku projects found for user {} ", userId);
			return new ResponseEntity<>(col, HttpStatus.NO_CONTENT);
		}
	}

	@ApiOperation(value = "Get dataiku project by given identifier.", nickname = "getByProjectKey", notes = "Get all dataiku project. This endpoints will be used to Get available dataiku project based on given identifier.", response = DataikuProjectVO.class, tags = {
			"dataiku", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = DataikuProjectVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataiku/projects/{projectKey}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Override
	public ResponseEntity<DataikuProjectVO> getByProjectKey(
			@ApiParam(value = "ProjectKey for which project to be fetched", required = true) @PathVariable("projectKey") String projectKey,
			@NotNull @ApiParam(value = "If requested data from live(Production) or training environment", required = true, defaultValue = "true") @Valid @RequestParam(value = "live", required = true, defaultValue = "true") Boolean live) {
		LOGGER.trace("Entering getByProjectKey");
		DataikuProjectVO extolloVo = null;
		DataikuProjectVO onpremVo = null;
		try {
			 extolloVo = vService.getByProjectKey(projectKey, live, "extollo");
		} catch (Exception e) {
			log.error("Failed to fetch extollo by project key with an exception {} ", e.getMessage());
		}
		try {
			onpremVo = vService.getByProjectKey(projectKey, live, "onprem");
		} catch (Exception e) {
			log.error("Failed to fetch onprem by project key with an exception {} ", e.getMessage());
		}
		DataikuProjectVO projectVO = null;
		if (extolloVo != null) {
			projectVO = extolloVo;
		} else if (onpremVo != null) {
			projectVO = onpremVo;
		}
		if (projectVO != null) {
			log.debug("Returning project found {} ", projectKey);
			return new ResponseEntity<>(projectVO, HttpStatus.OK);
		} else {
			log.debug("No project found {} ", projectKey);
			return new ResponseEntity<>(projectVO, HttpStatus.NO_CONTENT);
		}
	}

}
