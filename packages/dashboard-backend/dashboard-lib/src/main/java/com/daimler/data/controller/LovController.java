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

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.lov.LovApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.lov.AgileReleaseTrainSql;
import com.daimler.data.db.entities.lov.CommonFunctionSql;
import com.daimler.data.db.entities.lov.ConnectionTypeSql;
import com.daimler.data.db.entities.lov.CustomerDepartmentSql;
import com.daimler.data.db.entities.lov.DataClassificationSql;
import com.daimler.data.db.entities.lov.DataWarehouseSql;
import com.daimler.data.db.entities.lov.FrontendTechnologySql;
import com.daimler.data.db.entities.lov.IntegratedPortalSql;
import com.daimler.data.db.entities.lov.KpiNameSql;
import com.daimler.data.db.entities.lov.LegalEntitySql;
import com.daimler.data.db.entities.lov.LevelSql;
import com.daimler.data.db.entities.lov.ReportingCauseSql;
import com.daimler.data.db.entities.lov.StatusSql;
import com.daimler.data.dto.lov.LovRequestVO;
import com.daimler.data.dto.lov.LovResponseVO;
import com.daimler.data.dto.lov.LovUpdateRequestVO;
import com.daimler.data.dto.lov.LovVOCollection;
import com.daimler.data.service.lov.AgileReleaseTrainService;
import com.daimler.data.service.lov.CommonFunctionService;
import com.daimler.data.service.lov.ConnectionTypeService;
import com.daimler.data.service.lov.CustomerDepartmentService;
import com.daimler.data.service.lov.DataClassificationService;
import com.daimler.data.service.lov.DataWarehouseService;
import com.daimler.data.service.lov.FrontendTechnologyService;
import com.daimler.data.service.lov.IntegratedPortalService;
import com.daimler.data.service.lov.KpiNameService;
import com.daimler.data.service.lov.LegalEntityService;
import com.daimler.data.service.lov.LevelService;
import com.daimler.data.service.lov.ReportingCauseService;
import com.daimler.data.service.lov.StatusService;
import com.daimler.data.service.report.ReportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Lov API", tags = { "lov" })
@RequestMapping("/api/lov")
public class LovController implements LovApi {

	@Autowired
	private CustomerDepartmentService customerDepartmentService;

	@Autowired
	private FrontendTechnologyService frontendTechnologyService;

	@Autowired
	private LevelService levelService;

	@Autowired
	private IntegratedPortalService integratedPortalService;

	@Autowired
	private KpiNameService kpiNameService;

	@Autowired
	private DataWarehouseService dataWarehouseService;

	@Autowired
	private ReportingCauseService reportingCauseService;

	@Autowired
	private LegalEntityService legalEntityService;

	@Autowired
	private StatusService statusService;

	@Autowired
	private CommonFunctionService commonFunctionService;

	@Autowired
	private AgileReleaseTrainService agileReleaseTrainService;

	@Autowired
	private ConnectionTypeService connectionTypeService;

	@Autowired
	private DataClassificationService dataClassificationService;

	@Override
	@ApiOperation(value = "Add a new agile release train.", nickname = "createAgileReleaseTrainLov", notes = "Add a new non existing agile release train.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/agilereleasetrains", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createAgileReleaseTrainLov(
			@ApiParam(value = "Request Body that contains data required for creating a new agile release train.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		AgileReleaseTrainSql entity = new AgileReleaseTrainSql();
		entity.setName(lovRequestVO.getData().getName());
		return agileReleaseTrainService.createLov(lovRequestVO, entity);

	}

	@Override
	@ApiOperation(value = "Add a new customer department.", nickname = "createCustomerDepartmentLov", notes = "Add a new non existing customer department.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/customer/departments", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createCustomerDepartmentLov(
			@ApiParam(value = "Request Body that contains data required for creating a new customer department.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		CustomerDepartmentSql entity = new CustomerDepartmentSql();
		entity.setName(lovRequestVO.getData().getName());
		return customerDepartmentService.createLov(lovRequestVO, entity);

	}

	@Override
	@ApiOperation(value = "Add a new common function.", nickname = "createCommonFunctionLov", notes = "Add a new non existing common function.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/commonfunctions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createCommonFunctionLov(
			@ApiParam(value = "Request Body that contains data required for creating a new common function.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		CommonFunctionSql entity = new CommonFunctionSql();
		entity.setName(lovRequestVO.getData().getName());
		return commonFunctionService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new frontend technology.", nickname = "createFrontendTechnologyLov", notes = "Add a new non existing frontend technology.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/frontendtechnologies", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createFrontendTechnologyLov(
			@ApiParam(value = "Request Body that contains data required for creating a new frontend technology.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		FrontendTechnologySql entity = new FrontendTechnologySql();
		entity.setName(lovRequestVO.getData().getName());
		return frontendTechnologyService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new level.", nickname = "createLevelLov", notes = "Add a new non existing level.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/levels", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createLevelLov(
			@ApiParam(value = "Request Body that contains data required for creating a new level.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		LevelSql entity = new LevelSql();
		entity.setName(lovRequestVO.getData().getName());
		return levelService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new integrated portal.", nickname = "createIntegratedPortalLov", notes = "Add a new non existing integrated portal.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/integratedportals", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createIntegratedPortalLov(
			@ApiParam(value = "Request Body that contains data required for creating a new integrated portal.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		IntegratedPortalSql entity = new IntegratedPortalSql();
		entity.setName(lovRequestVO.getData().getName());
		return integratedPortalService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new kpi name.", nickname = "createKpiNameLov", notes = "Add a new non existing kpi name.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/kpinames", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createKpiNameLov(
			@ApiParam(value = "Request Body that contains data required for creating a new kpi name.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		KpiNameSql entity = new KpiNameSql();
		entity.setName(lovRequestVO.getData().getName());
		return kpiNameService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new dataWarehouse.", nickname = "createDataWarehouseLov", notes = "Add a new non existing dataWarehouse.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createDataWarehouseLov(
			@ApiParam(value = "Request Body that contains data required for creating a new dataWarehouse.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		DataWarehouseSql entity = new DataWarehouseSql();
		entity.setName(lovRequestVO.getData().getName());
		return dataWarehouseService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new reporting cause.", nickname = "createReportingCauseLov", notes = "Add a new non existing reporting cause.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reportingcauses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createReportingCauseLov(
			@ApiParam(value = "Request Body that contains data required for creating a new reporting cause.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		ReportingCauseSql entity = new ReportingCauseSql();
		entity.setName(lovRequestVO.getData().getName());
		return reportingCauseService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new legal entity.", nickname = "createLegalEntityLov", notes = "Add a new non existing legal entity.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/legalentities", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createLegalEntityLov(
			@ApiParam(value = "Request Body that contains data required for creating a new legal entity.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		LegalEntitySql entity = new LegalEntitySql();
		entity.setName(lovRequestVO.getData().getName());
		return legalEntityService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new status.", nickname = "createStatusLov", notes = "Add a new non existing status.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/statuses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createStatusLov(
			@ApiParam(value = "Request Body that contains data required for creating a new status.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		StatusSql entity = new StatusSql();
		entity.setName(lovRequestVO.getData().getName());
		return statusService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new data classification.", nickname = "createDataClassificationLov", notes = "Add a new non existing data classification.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataclassifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createDataClassificationLov(
			@ApiParam(value = "Request Body that contains data required for creating a new data classification.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		DataClassificationSql entity = new DataClassificationSql();
		entity.setName(lovRequestVO.getData().getName());
		return dataClassificationService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Add a new connection type.", nickname = "createConnectionTypeLov", notes = "Add a new non existing connection type.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/connectiontypes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createConnectionTypeLov(
			@ApiParam(value = "Request Body that contains data required for creating a new connection type.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		ConnectionTypeSql entity = new ConnectionTypeSql();
		entity.setName(lovRequestVO.getData().getName());
		return connectionTypeService.createLov(lovRequestVO, entity);
	}

	@Override
	@ApiOperation(value = "Delete the agile release train identified by given ID.", nickname = "deleteAgileReleaseTrainLov", notes = "Delete the agile release train identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/agilereleasetrains/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteAgileReleaseTrainLov(
			@ApiParam(value = "Id of the agile release train", required = true) @PathVariable("id") Long id) {
		AgileReleaseTrainSql entity = new AgileReleaseTrainSql();
		return agileReleaseTrainService.deleteLov(id, ReportService.CATEGORY.ART, entity);
	}

	@Override
	@ApiOperation(value = "Delete the customer department identified by given ID.", nickname = "deleteCustomerDepartmentLov", notes = "Delete the customer department identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/customer/departments/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteCustomerDepartmentLov(
			@ApiParam(value = "Id of the customer department", required = true) @PathVariable("id") Long id) {
		CustomerDepartmentSql entity = new CustomerDepartmentSql();
		return customerDepartmentService.deleteLov(id, ReportService.CATEGORY.CUST_DEPARTMENT, entity);
	}

	@Override
	@ApiOperation(value = "Delete the common function identified by given ID.", nickname = "deleteCommonFunctionLov", notes = "Delete the common function identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/commonfunctions/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteCommonFunctionLov(
			@ApiParam(value = "Id of the common function", required = true) @PathVariable("id") Long id) {
		CommonFunctionSql entity = new CommonFunctionSql();
		return commonFunctionService.deleteLov(id, ReportService.CATEGORY.COMMON_FUNCTION, entity);
	}

	@Override
	@ApiOperation(value = "Delete the frontend technology identified by given ID.", nickname = "deleteFrontendTechnologyLov", notes = "Delete the frontend technology identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/frontendtechnologies/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteFrontendTechnologyLov(
			@ApiParam(value = "Id of the frontend technology", required = true) @PathVariable("id") Long id) {
		FrontendTechnologySql entity = new FrontendTechnologySql();
		return frontendTechnologyService.deleteLov(id, ReportService.CATEGORY.FRONTEND_TECH, entity);
	}

	@Override
	@ApiOperation(value = "Delete the level identified by given ID.", nickname = "deleteLevelLov", notes = "Delete the level identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/levels/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteLevelLov(
			@ApiParam(value = "Id of the level", required = true) @PathVariable("id") Long id) {
		LevelSql entity = new LevelSql();
		return levelService.deleteLov(id, ReportService.CATEGORY.LEVEL, entity);
	}

	@Override
	@ApiOperation(value = "Delete the integrated portal identified by given ID.", nickname = "deleteIntegratedPortal", notes = "Delete the integrated portal identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/integratedportals/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteIntegratedPortal(
			@ApiParam(value = "Id of the integrated portal", required = true) @PathVariable("id") Long id) {
		IntegratedPortalSql entity = new IntegratedPortalSql();
		return integratedPortalService.deleteLov(id, ReportService.CATEGORY.INTEGRATED_PORTAL, entity);
	}

	@Override
	@ApiOperation(value = "Delete the kpi name identified by given ID.", nickname = "deleteKpiNameLov", notes = "Delete the kpi name identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/kpinames/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteKpiNameLov(
			@ApiParam(value = "Id of the kpi name", required = true) @PathVariable("id") Long id) {
		KpiNameSql entity = new KpiNameSql();
		return kpiNameService.deleteLov(id, ReportService.CATEGORY.KPI_NAME, entity);
	}

	@Override
	@ApiOperation(value = "Delete the dataWarehouse identified by given ID.", nickname = "deleteDataWarehouseLov", notes = "Delete the dataWarehouse identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteDataWarehouseLov(
			@ApiParam(value = "Id of the dataWarehouse", required = true) @PathVariable("id") Long id) {
		DataWarehouseSql entity = new DataWarehouseSql();
		return dataWarehouseService.deleteLov(id, ReportService.CATEGORY.DATA_WAREHOUSE, entity);
	}

	@Override
	@ApiOperation(value = "Delete the reporting cause identified by given ID.", nickname = "deleteReportingCauseLov", notes = "Delete the reporting cause identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reportingcauses/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteReportingCauseLov(
			@ApiParam(value = "Id of the reporting cause", required = true) @PathVariable("id") Long id) {
		ReportingCauseSql entity = new ReportingCauseSql();
		return reportingCauseService.deleteLov(id, ReportService.CATEGORY.REPORTING_CAUSE, entity);
	}

	@Override
	@ApiOperation(value = "Delete the legal entity identified by given ID.", nickname = "deleteLegalEntityLov", notes = "Delete the legal entity identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/legalentities/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteLegalEntityLov(
			@ApiParam(value = "Id of the legal entity", required = true) @PathVariable("id") Long id) {
		LegalEntitySql entity = new LegalEntitySql();
		return legalEntityService.deleteLov(id, ReportService.CATEGORY.LEGAL_ENTITY, entity);
	}

	@Override
	@ApiOperation(value = "Delete the status identified by given ID.", nickname = "deleteStatusLov", notes = "Delete the status identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/statuses/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteStatusLov(
			@ApiParam(value = "Id of the status", required = true) @PathVariable("id") Long id) {
		StatusSql entity = new StatusSql();
		return statusService.deleteLov(id, ReportService.CATEGORY.STATUS, entity);
	}

	@Override
	@ApiOperation(value = "Delete the data classification identified by given ID.", nickname = "deleteDataClassificationLov", notes = "Delete the data classification identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataclassifications/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteDataClassificationLov(
			@ApiParam(value = "Id of the data classification", required = true) @PathVariable("id") Long id) {
		DataClassificationSql entity = new DataClassificationSql();
		return dataClassificationService.deleteLov(id, ReportService.CATEGORY.DATA_CLASSIFICATION, entity);
	}

	@Override
	@ApiOperation(value = "Delete the connection type identified by given ID.", nickname = "deleteConnectionTypeLov", notes = "Delete the connection type identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/connectiontypes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteConnectionTypeLov(
			@ApiParam(value = "Id of the connection type", required = true) @PathVariable("id") Long id) {
		ConnectionTypeSql entity = new ConnectionTypeSql();
		return connectionTypeService.deleteLov(id, ReportService.CATEGORY.CONNECTION_TYPE, entity);
	}

	@Override
	@ApiOperation(value = "Get all agile release train.", nickname = "getAllAgileReleaseTrainLov", notes = "Get all agile release train. This endpoints will be used to get all valid available agile release train.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/agilereleasetrains", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllAgileReleaseTrainLov(
			@ApiParam(value = "Sort agilereleasetrains based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return agileReleaseTrainService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all customer department.", nickname = "getAllCustomerDepartmentLov", notes = "Get all customer department. This endpoints will be used to get all valid available customer department.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/customer/departments", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllCustomerDepartmentLov(
			@ApiParam(value = "Sort customer departments based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return customerDepartmentService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all common function.", nickname = "getAllCommonFunctionLov", notes = "Get all common function. This endpoints will be used to Get all valid available common function.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/commonfunctions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllCommonFunctionLov(
			@ApiParam(value = "Sort commonfunctions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return commonFunctionService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all frontend technology.", nickname = "getAllFrontEndTechnologyLov", notes = "Get all frontend technology. This endpoints will be used to get all valid available frontend technology.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/frontendtechnologies", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllFrontendTechnologyLov(
			@ApiParam(value = "Sort frontendtechnologies based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return frontendTechnologyService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all level.", nickname = "getAllLevelLov", notes = "Get all level. This endpoints will be used to Get all valid available level.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/levels", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllLevelLov(
			@ApiParam(value = "Sort levels based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return levelService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all integrated portal.", nickname = "getAllIntegratedPortalLov", notes = "Get all integrated portal. This endpoints will be used to get all valid available integrated portal.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/integratedportals", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllIntegratedPortalLov(
			@ApiParam(value = "Sort integratedportals based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return integratedPortalService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all kpi name.", nickname = "getAllKpiNameLov", notes = "Get all kpi name. This endpoints will be used to Get all valid available kpi name.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/kpinames", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllKpiNameLov(
			@ApiParam(value = "Sort kpinames based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return kpiNameService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all dataWarehouse.", nickname = "getAllDataWarehouseLov", notes = "Get all dataWarehouse. This endpoints will be used to get all valid available dataWarehouse.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllDataWarehouseLov(
			@ApiParam(value = "Sort datawarehouses based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return dataWarehouseService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all reporting cause.", nickname = "getAllReportingCauseLov", notes = "Get all reporting cause. This endpoints will be used to Get all valid available reporting cause.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reportingcauses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllReportingCauseLov(
			@ApiParam(value = "Sort reportingcauses based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return reportingCauseService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all legal entity.", nickname = "getAllLegalEntityLov", notes = "Get all legal entity. This endpoints will be used to Get all valid available legal entity.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/legalentities", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllLegalEntityLov(
			@ApiParam(value = "Sort legalentities based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return legalEntityService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all status.", nickname = "getAllStatusLov", notes = "Get all status. This endpoints will be used to get all valid available status.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/statuses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllStatusLov(
			@ApiParam(value = "Sort statuses based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return statusService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all connection type.", nickname = "getAllConnectionTypeLov", notes = "Get all connection type. This endpoints will be used to get all valid available connection type.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/connectiontypes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllConnectionTypeLov(
			@ApiParam(value = "Sort connectiontypes based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return connectionTypeService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Get all data classification.", nickname = "getAllDataClassificationLov", notes = "Get all data classification. This endpoints will be used to get all valid available data classification.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataclassifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllDataClassificationLov(
			@ApiParam(value = "Sort dataclassifications based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		return dataClassificationService.getAllLov(sortOrder);
	}

	@Override
	@ApiOperation(value = "Update the agile release train identified by given ID.", nickname = "updateAgileReleaseTrainLov", notes = "Update the agile release train identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/agilereleasetrains", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateAgileReleaseTrainLov(
			@ApiParam(value = "Request Body that contains data required for updating agile release train.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		AgileReleaseTrainSql entity = new AgileReleaseTrainSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return agileReleaseTrainService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.ART);
	}

	@Override
	@ApiOperation(value = "Update the customer department identified by given ID.", nickname = "updateCustomerDepartmentLov", notes = "Update the customer department identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/customer/departments", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateCustomerDepartmentLov(
			@ApiParam(value = "Request Body that contains data required for updating customer department.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		CustomerDepartmentSql entity = new CustomerDepartmentSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return customerDepartmentService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.CUST_DEPARTMENT);
	}

	@Override
	@ApiOperation(value = "Update the common function identified by given ID.", nickname = "updateCommonFunctionLov", notes = "Update the common function identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/commonfunctions", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateCommonFunctionLov(
			@ApiParam(value = "Request Body that contains data required for updating common function.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		CommonFunctionSql entity = new CommonFunctionSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return commonFunctionService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.COMMON_FUNCTION);
	}

	@Override
	@ApiOperation(value = "Update the frontend technology identified by given ID.", nickname = "updateFrontendTechnologyLov", notes = "Update the frontend technology identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/frontendtechnologies", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateFrontendTechnologyLov(
			@ApiParam(value = "Request Body that contains data required for updating frontend technology.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		FrontendTechnologySql entity = new FrontendTechnologySql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return frontendTechnologyService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.FRONTEND_TECH);
	}

	@Override
	@ApiOperation(value = "Update the level identified by given ID.", nickname = "updateLevelLov", notes = "Update the level identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/levels", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateLevelLov(
			@ApiParam(value = "Request Body that contains data required for updating level.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		LevelSql entity = new LevelSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return levelService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.LEVEL);
	}

	@Override
	@ApiOperation(value = "Update the integrated portal identified by given ID.", nickname = "updateIntegratedPortalLov", notes = "Update the integrated portal identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/integratedportals", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateIntegratedPortalLov(
			@ApiParam(value = "Request Body that contains data required for updating integrated portal.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		IntegratedPortalSql entity = new IntegratedPortalSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return integratedPortalService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.INTEGRATED_PORTAL);
	}

	@Override
	@ApiOperation(value = "Update the kpi name identified by given ID.", nickname = "updateKpiNameLov", notes = "Update the kpi name identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/kpinames", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateKpiNameLov(
			@ApiParam(value = "Request Body that contains data required for updating kpi name.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		KpiNameSql entity = new KpiNameSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return kpiNameService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.KPI_NAME);
	}

	@Override
	@ApiOperation(value = "Update the dataWarehouse identified by given ID.", nickname = "updateDataWarehouseLov", notes = "Update the dataWarehouse identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datawarehouses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateDataWarehouseLov(
			@ApiParam(value = "Request Body that contains data required for updating dataWarehouse.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		DataWarehouseSql entity = new DataWarehouseSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return dataWarehouseService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.DATA_WAREHOUSE);
	}

	@Override
	@ApiOperation(value = "Update the reporting cause identified by given ID.", nickname = "updateReportingCauseLov", notes = "Update the reporting cause identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/reportingcauses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateReportingCauseLov(
			@ApiParam(value = "Request Body that contains data required for updating reporting cause.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		ReportingCauseSql entity = new ReportingCauseSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return reportingCauseService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.REPORTING_CAUSE);
	}

	@Override
	@ApiOperation(value = "Update the legal entity identified by given ID.", nickname = "updateLegalEntityLov", notes = "Update the legal entity identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/legalentities", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateLegalEntityLov(
			@ApiParam(value = "Request Body that contains data required for updating legal entity.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		LegalEntitySql entity = new LegalEntitySql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return legalEntityService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.LEGAL_ENTITY);
	}

	@Override
	@ApiOperation(value = "Update the status identified by given ID.", nickname = "updateStatusLov", notes = "Update the status identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/statuses", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateStatusLov(
			@ApiParam(value = "Request Body that contains data required for updating status.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		StatusSql entity = new StatusSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return statusService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.STATUS);

	}

	@Override
	@ApiOperation(value = "Update the connection type identified by given ID.", nickname = "updateConnectionTypeLov", notes = "Update the connection type identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/connectiontypes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateConnectionTypeLov(
			@ApiParam(value = "Request Body that contains data required for updating connection type.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		ConnectionTypeSql entity = new ConnectionTypeSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return connectionTypeService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.CONNECTION_TYPE);
	}

	@Override
	@ApiOperation(value = "Update the data classification identified by given ID.", nickname = "updateDataClassificationLov", notes = "Update the data classification identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataclassifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateDataClassificationLov(
			@ApiParam(value = "Request Body that contains data required for updating data classification.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		DataClassificationSql entity = new DataClassificationSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return dataClassificationService.updateLov(lovUpdateRequestVO, entity,
				ReportService.CATEGORY.DATA_CLASSIFICATION);
	}
}
