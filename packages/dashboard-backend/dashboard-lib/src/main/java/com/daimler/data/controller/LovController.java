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
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.lov.LovApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.lov.AgileReleaseTrainSql;
import com.daimler.data.db.entities.lov.ConnectionTypeSql;
import com.daimler.data.db.entities.lov.CustomerDepartmentSql;
import com.daimler.data.db.entities.lov.DataSourceSql;
import com.daimler.data.db.entities.lov.DesignGuideSql;
import com.daimler.data.db.entities.lov.FrontendTechnologySql;
import com.daimler.data.db.entities.lov.HierarchySql;
import com.daimler.data.db.entities.lov.IntegratedPortalSql;
import com.daimler.data.db.entities.lov.KpiNameSql;
import com.daimler.data.db.entities.lov.ProductPhaseSql;
import com.daimler.data.db.entities.lov.ReportingCauseSql;
import com.daimler.data.db.entities.lov.RessortSql;
import com.daimler.data.db.entities.lov.StatusSql;
import com.daimler.data.db.entities.lov.SubsystemSql;
import com.daimler.data.dto.lov.LovRequestVO;
import com.daimler.data.dto.lov.LovResponseVO;
import com.daimler.data.dto.lov.LovUpdateRequestVO;
import com.daimler.data.dto.lov.LovVOCollection;
import com.daimler.data.service.lov.AgileReleaseTrainService;
import com.daimler.data.service.lov.ConnectionTypeService;
import com.daimler.data.service.lov.CustomerDepartmentService;
import com.daimler.data.service.lov.DataSourceService;
import com.daimler.data.service.lov.DesignGuideService;
import com.daimler.data.service.lov.FrontendTechnologyService;
import com.daimler.data.service.lov.HierarchyService;
import com.daimler.data.service.lov.IntegratedPortalService;
import com.daimler.data.service.lov.KpiNameService;
import com.daimler.data.service.lov.ProductPhaseService;
import com.daimler.data.service.lov.ReportingCauseService;
import com.daimler.data.service.lov.RessortService;
import com.daimler.data.service.lov.StatusService;
import com.daimler.data.service.lov.SubsystemService;
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
	private DataSourceService dataSourceService;

	@Autowired
	private CustomerDepartmentService customerDepartmentService;

	@Autowired
	private FrontendTechnologyService frontendTechnologyService;

	@Autowired
	private HierarchyService hierarchyService;

	@Autowired
	private IntegratedPortalService integratedPortalService;

	@Autowired
	private KpiNameService kpiNameService;

	@Autowired
	private ProductPhaseService productPhaseService;

	@Autowired
	private ReportingCauseService reportingCauseService;

	@Autowired
	private RessortService ressortService;

	@Autowired
	private StatusService statusService;

	@Autowired
	private DesignGuideService designGuideService;

	@Autowired
	private AgileReleaseTrainService agileReleaseTrainService;

	@Autowired
	private ConnectionTypeService connectionTypeService;

	@Autowired
	private SubsystemService subsystemService;

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
	@ApiOperation(value = "Add a new data source.", nickname = "createDataSourceLov", notes = "Add a new non existing data source.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createDataSourceLov(
			@ApiParam(value = "Request Body that contains data required for creating a new data source.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		DataSourceSql entity = new DataSourceSql();
		entity.setName(lovRequestVO.getData().getName());
		return dataSourceService.createLov(lovRequestVO, entity);

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
	@ApiOperation(value = "Add a new design guide.", nickname = "createDesignGuideLov", notes = "Add a new non existing design guide.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/designguides", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createDesignGuideLov(
			@ApiParam(value = "Request Body that contains data required for creating a new design guide.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		DesignGuideSql entity = new DesignGuideSql();
		entity.setName(lovRequestVO.getData().getName());
		return designGuideService.createLov(lovRequestVO, entity);
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
	@ApiOperation(value = "Add a new hierarchy.", nickname = "createHierarchyLov", notes = "Add a new non existing hierarchy.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/hierarchies", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createHierarchyLov(
			@ApiParam(value = "Request Body that contains data required for creating a new hierarchy.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		HierarchySql entity = new HierarchySql();
		entity.setName(lovRequestVO.getData().getName());
		return hierarchyService.createLov(lovRequestVO, entity);
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
	@ApiOperation(value = "Add a new product phase.", nickname = "createProductPhaseLov", notes = "Add a new non existing product phase.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/productphases", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createProductPhaseLov(
			@ApiParam(value = "Request Body that contains data required for creating a new product phase.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		ProductPhaseSql entity = new ProductPhaseSql();
		entity.setName(lovRequestVO.getData().getName());
		return productPhaseService.createLov(lovRequestVO, entity);
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
	@ApiOperation(value = "Add a new ressort.", nickname = "createRessortLov", notes = "Add a new non existing ressort.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/ressort", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createRessortLov(
			@ApiParam(value = "Request Body that contains data required for creating a new ressort.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		RessortSql entity = new RessortSql();
		entity.setName(lovRequestVO.getData().getName());
		return ressortService.createLov(lovRequestVO, entity);
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
	@ApiOperation(value = "Add a new subsystem.", nickname = "createSubsystemLov", notes = "Add a new non existing subsystem.", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subsystems", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<LovResponseVO> createSubsystemLov(
			@ApiParam(value = "Request Body that contains data required for creating a new subsystem.", required = true) @Valid @RequestBody LovRequestVO lovRequestVO) {
		SubsystemSql entity = new SubsystemSql();
		entity.setName(lovRequestVO.getData().getName());
		return subsystemService.createLov(lovRequestVO, entity);
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
	@ApiOperation(value = "Delete the data source identified by given ID.", nickname = "deleteDataSourceLov", notes = "Delete the data source identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteDataSourceLov(
			@ApiParam(value = "Id of the data source", required = true) @PathVariable("id") Long id) {
		DataSourceSql entity = new DataSourceSql();
		return dataSourceService.deleteLov(id, ReportService.CATEGORY.DATASOURCE, entity);
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
	@ApiOperation(value = "Delete the design guide identified by given ID.", nickname = "deleteDesignGuideLov", notes = "Delete the design guide identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/designguides/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteDesignGuideLov(
			@ApiParam(value = "Id of the design guide", required = true) @PathVariable("id") Long id) {
		DesignGuideSql entity = new DesignGuideSql();
		return designGuideService.deleteLov(id, ReportService.CATEGORY.DESIGN_GUIDE, entity);
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
	@ApiOperation(value = "Delete the hierarchy identified by given ID.", nickname = "deleteHierarchyLov", notes = "Delete the hierarchy identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/hierarchies/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteHierarchyLov(
			@ApiParam(value = "Id of the hierarchy", required = true) @PathVariable("id") Long id) {
		HierarchySql entity = new HierarchySql();
		return hierarchyService.deleteLov(id, ReportService.CATEGORY.HIERARCHIES, entity);
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
	@ApiOperation(value = "Delete the product phase identified by given ID.", nickname = "deleteProductPhaseLov", notes = "Delete the product phase identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/productphases/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteProductPhaseLov(
			@ApiParam(value = "Id of the product phase", required = true) @PathVariable("id") Long id) {
		ProductPhaseSql entity = new ProductPhaseSql();
		return productPhaseService.deleteLov(id, ReportService.CATEGORY.PRODUCT_PHASE, entity);
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
	@ApiOperation(value = "Delete the ressort identified by given ID.", nickname = "deleteRessortLov", notes = "Delete the ressort identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/ressort/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteRessortLov(
			@ApiParam(value = "Id of the ressort", required = true) @PathVariable("id") Long id) {
		RessortSql entity = new RessortSql();
		return ressortService.deleteLov(id, ReportService.CATEGORY.RESSORT, entity);
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
	@ApiOperation(value = "Delete the subsystem identified by given ID.", nickname = "deleteSubsystemLov", notes = "Delete the subsystem identified by given ID", response = GenericMessage.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subsystems/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteSubsystemLov(
			@ApiParam(value = "Id of the subsystem", required = true) @PathVariable("id") Long id) {
		SubsystemSql entity = new SubsystemSql();
		return subsystemService.deleteLov(id, ReportService.CATEGORY.SUBSYSTEM, entity);
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
	public ResponseEntity<LovVOCollection> getAllAgileReleaseTrainLov() {
		return agileReleaseTrainService.getAllLov();
	}

	@Override
	@ApiOperation(value = "Get all data source.", nickname = "getAllDataSourceLov", notes = "Get all data source. This endpoints will be used to Get all valid available data source.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllDataSourceLov() {
		return dataSourceService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllCustomerDepartmentLov() {
		return customerDepartmentService.getAllLov();
	}

	@Override
	@ApiOperation(value = "Get all design guide.", nickname = "getAllDesignGuideLov", notes = "Get all design guide. This endpoints will be used to Get all valid available design guide.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/designguides", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllDesignGuideLov() {
		return designGuideService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllFrontendTechnologyLov() {
		return frontendTechnologyService.getAllLov();
	}

	@Override
	@ApiOperation(value = "Get all hierarchies.", nickname = "getAllHierarchyLov", notes = "Get all hierarchies. This endpoints will be used to Get all valid available hierarchies.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/hierarchies", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllHierarchyLov() {
		return hierarchyService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllIntegratedPortalLov() {
		return integratedPortalService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllKpiNameLov() {
		return kpiNameService.getAllLov();
	}

	@Override
	@ApiOperation(value = "Get all product phase.", nickname = "getAllProductPhaseLov", notes = "Get all product phases. This endpoints will be used to get all valid available product phases.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/productphases", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllProductPhaseLov() {
		return productPhaseService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllReportingCauseLov() {
		return reportingCauseService.getAllLov();
	}

	@Override
	@ApiOperation(value = "Get all ressort.", nickname = "getAllRessortLov", notes = "Get all ressort. This endpoints will be used to Get all valid available ressort.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/ressort", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllRessortLov() {
		return ressortService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllStatusLov() {
		return statusService.getAllLov();
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
	public ResponseEntity<LovVOCollection> getAllConnectionTypeLov() {
		return connectionTypeService.getAllLov();
	}

	@Override
	@ApiOperation(value = "Get all subsystem.", nickname = "getAllSubsystemLov", notes = "Get all subsystem. This endpoints will be used to get all valid available subsystem.", response = LovVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LovVOCollection.class),
			@ApiResponse(code = 204, message = "No content found."), @ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subsystems", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LovVOCollection> getAllSubsystemLov() {
		return subsystemService.getAllLov();
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
	@ApiOperation(value = "Update the data source identified by given ID.", nickname = "updateDataSourceLov", notes = "Update the data source identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datasources", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateDataSourceLov(
			@ApiParam(value = "Request Body that contains data required for updating data source.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		DataSourceSql entity = new DataSourceSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return dataSourceService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.DATASOURCE);
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
	@ApiOperation(value = "Update the design guide identified by given ID.", nickname = "updateDesignGuideLov", notes = "Update the design guide identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/designguides", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateDesignGuideLov(
			@ApiParam(value = "Request Body that contains data required for updating design guide.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		DesignGuideSql entity = new DesignGuideSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return designGuideService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.DESIGN_GUIDE);
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
	@ApiOperation(value = "Update the hierarchy identified by given ID.", nickname = "updateHierarchyLov", notes = "Update the hierarchy identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/hierarchies", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateHierarchyLov(
			@ApiParam(value = "Request Body that contains data required for updating hierarchy.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		HierarchySql entity = new HierarchySql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return hierarchyService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.HIERARCHIES);
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
	@ApiOperation(value = "Update the product phase identified by given ID.", nickname = "updateProductPhaseLov", notes = "Update the product phase identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/productphases", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateProductPhaseLov(
			@ApiParam(value = "Request Body that contains data required for updating product phase.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		ProductPhaseSql entity = new ProductPhaseSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return productPhaseService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.PRODUCT_PHASE);
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
	@ApiOperation(value = "Update the ressort identified by given ID.", nickname = "updateRessortLov", notes = "Update the ressort identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/ressort", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateRessortLov(
			@ApiParam(value = "Request Body that contains data required for updating ressort.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		RessortSql entity = new RessortSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return ressortService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.RESSORT);
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
	@ApiOperation(value = "Update the subsystem identified by given ID.", nickname = "updateSubsystemLov", notes = "Update the subsystem identified by given ID", response = LovResponseVO.class, tags = {
			"lov", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated.", response = LovResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/subsystems", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<LovResponseVO> updateSubsystemLov(
			@ApiParam(value = "Request Body that contains data required for updating subsystem.", required = true) @Valid @RequestBody LovUpdateRequestVO lovUpdateRequestVO) {
		SubsystemSql entity = new SubsystemSql();
		BeanUtils.copyProperties(lovUpdateRequestVO.getData(), entity);
		return subsystemService.updateLov(lovUpdateRequestVO, entity, ReportService.CATEGORY.SUBSYSTEM);
	}
}
