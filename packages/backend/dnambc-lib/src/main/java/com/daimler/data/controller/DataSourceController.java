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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.datasource.DatasourcesApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.datasource.DataSourceBulkRequestVO;
import com.daimler.data.dto.datasource.DataSourceCollection;
import com.daimler.data.dto.datasource.DataSourceRequestVO;
import com.daimler.data.dto.datasource.DataSourceVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.datasource.DataSourceService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "DataSource API", tags = { "datasources" })
@RequestMapping("/api")
@Slf4j
public class DataSourceController implements DatasourcesApi {

	private static Logger logger = LoggerFactory.getLogger(DataSourceController.class);
	
	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private DataSourceService datasourceService;

	@Override
	@ApiOperation(value = "Adds a new datasources.", nickname = "create", notes = "Adds a new non existing datasource which is used in providing solution.", response = DataSourceVO.class, tags = {
			"datasources", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "DataSource added successfully", response = DataSourceVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<DataSourceVO> create(@Valid DataSourceRequestVO requestVO) {
		DataSourceVO requestDatasourceVO = requestVO.getData();
		try {
			DataSourceVO existingdatasourceVo = datasourceService.getByUniqueliteral("name",
					requestDatasourceVO.getName());
			if (existingdatasourceVo != null && existingdatasourceVo.getName() != null)
				return new ResponseEntity<>(existingdatasourceVo, HttpStatus.CONFLICT);
			requestDatasourceVO.setId(null);
			DataSourceVO datasourceVo = datasourceService.create(requestDatasourceVO);
			if (datasourceVo != null && datasourceVo.getId() != null) {
				log.info("Datasource {} created successfully", requestDatasourceVO.getName());
				return new ResponseEntity<>(datasourceVo, HttpStatus.CREATED);
			} else {
				log.info("Failed to created datasource {} with unknown error", requestDatasourceVO.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Failed with exception {} while creating datasource {}", e.getLocalizedMessage(),
					requestDatasourceVO.getName());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the datasource identified by given ID.", nickname = "delete", notes = "Deletes the datasource identified by given ID", response = GenericMessage.class, tags = {
			"datasources", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the datasource", required = true) @PathVariable("id") String id) {
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						if (userId == null || !isAdmin) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to delete Data Sources. User does not have admin privileges.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							log.info("User not authorized to delete datasource");
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			DataSourceVO datasource = datasourceService.getById(id);
			String datasourceName = datasource != null ? datasource.getName() : "";
			String userName = datasourceService.currentUserName(currentUser);
			String eventMessage = "DataSource  " + datasourceName + " has been deleted by Admin " + userName;
			datasourceService.deleteDataSource(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			log.info("Datasource {} deleted successfully", id);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);

		} catch (EntityNotFoundException e) {
			log.error("Failed to delete datasource {}, no entity found", id);
			MessageDescription invalidMsg = new MessageDescription("No tag with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed with exception {} while deleting datasource {} ", e.getLocalizedMessage(), id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available datasources.", nickname = "getAll", notes = "Get all datasources. This endpoints will be used to Get all valid available datasources maintenance records.", response = DataSourceCollection.class, tags = {
			"datasources", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all datasources", response = DataSourceCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataSourceCollection> getAll(
			@ApiParam(value = "Sort datasources based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
			@ApiParam(value = "Filter datasources based on the given source like sourceName, example cdc", allowableValues = "cdc") @Valid @RequestParam(value = "source", required = false) String source) {
		
		List<DataSourceVO> datasources = new ArrayList<>();		
		DataSourceCollection datasourceCollection = new DataSourceCollection();
		if(source != null && source.equalsIgnoreCase("cdc")) {
			datasources = datasourceService.getAllDataCatalogs(source,sortOrder);	
		}
		else {
			datasources = datasourceService.getAll();
			if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				datasources.sort(Comparator.comparing(DataSourceVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				datasources.sort(Comparator.comparing(DataSourceVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
		}
		if (!ObjectUtils.isEmpty(datasources)) {
			datasourceCollection.addAll(datasources);
			log.debug("Returning all available datasources");
			return new ResponseEntity<>(datasourceCollection, HttpStatus.OK);
		} else {
			log.debug("No datasources found, returning empty");
			return new ResponseEntity<>(datasourceCollection, HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Add datasources.", nickname = "bulkCreate", notes = "Add datasources will add non-existing datasources.", response = GenericMessage.class, tags = {
			"datasources", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure ", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@PostMapping(path = "/datasources/bulk", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<GenericMessage> bulkCreate(
			@ApiParam(value = "Api access token to access api.", required = true) @RequestHeader(value = "accessToken", required = true) String accessToken,
			@ApiParam(value = "Request Body that contains data required for adding new datasources", required = true) @Valid @RequestBody DataSourceBulkRequestVO dataSourceBulkRequestVO) {
		if(datasourceService.accessTokenIntrospection(accessToken)) {
			logger.info("Valid access token.");
			return datasourceService.bulkCreate(dataSourceBulkRequestVO.getData());
		}else {
			GenericMessage genericMessage = new GenericMessage();
			logger.info("Access Token must not be empty or invalid.");
			genericMessage.setErrors(Arrays.asList(new MessageDescription("Invalid Access Token.")));
			return new ResponseEntity<>(genericMessage, HttpStatus.FORBIDDEN);
		}
	}

}
