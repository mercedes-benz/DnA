package com.daimler.data.controller;

import java.util.Comparator;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.marketingRole.MarketingRolesApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.marketingRole.MarketingRoleCollection;
import com.daimler.data.dto.marketingRole.MarketingRoleRequestVO;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;
import com.daimler.data.service.marketingRoles.MarketingRoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "MarketingRoles API", tags = { "marketingRoles" })
@RequestMapping("/api")
@Slf4j
public class MarketingRoleController implements MarketingRolesApi {
	private static Logger LOGGER = LoggerFactory.getLogger(MarketingRoleController.class);

	@Autowired
	private MarketingRoleService marketingRoleService;

	@Override
	@ApiOperation(value = "Adds a new marketingRole.", nickname = "create", notes = "Adds a new non existing marketingRoles which is used in providing solution.", response = MarketingRoleVO.class, tags = {
			"marketingRoles", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = MarketingRoleVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/marketingRoles", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<MarketingRoleVO> create(@Valid MarketingRoleRequestVO marketingRoleRequestVO) {
		MarketingRoleVO marketingRoleVO = marketingRoleRequestVO.getData();
		try {
			MarketingRoleVO existingMarketingRoleVO = marketingRoleService.getByUniqueliteral("name", marketingRoleVO.getName());
			if(existingMarketingRoleVO != null && existingMarketingRoleVO.getId() != null) {
				LOGGER.debug("MarketingRole already exists");
				return new ResponseEntity<>(existingMarketingRoleVO,HttpStatus.CONFLICT);
			}								
			marketingRoleVO.setId(null);
			MarketingRoleVO newMarketingRoleVO = marketingRoleService.create(marketingRoleVO);
			
			if (newMarketingRoleVO != null && StringUtils.hasText(newMarketingRoleVO.getId())) {
				LOGGER.debug("MarketingRole creation successfull.");
				return new ResponseEntity<>(newMarketingRoleVO, HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Error occured while creating new MarketingRole: {}", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available marketingRoles.", nickname = "getAll", notes = "Get all marketingRoles. This endpoints will be used to Get all valid available marketingRoles maintenance records.", response = MarketingRoleCollection.class, tags = {
			"marketingRoles", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = MarketingRoleCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/marketingRoles", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<MarketingRoleCollection> getAll(
			@ApiParam(value = "Sort marketingRoles based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		// TODO Auto-generated method stub
		MarketingRoleCollection marketingRoleCollection = new MarketingRoleCollection();
		final List<MarketingRoleVO> marketingRoles = marketingRoleService.getAll();
		if (marketingRoles != null && marketingRoles.size() > 0) {
			if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				marketingRoles.sort(Comparator.comparing(MarketingRoleVO::getName, String.CASE_INSENSITIVE_ORDER));
			}
			if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				marketingRoles
						.sort(Comparator.comparing(MarketingRoleVO::getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			marketingRoleCollection.addAll(marketingRoles);
			log.debug("Returning all available marketingRoles");
			return new ResponseEntity<>(marketingRoleCollection, HttpStatus.OK);
		} else {
			log.debug("No marketingRoles found");
			return new ResponseEntity<>(marketingRoleCollection, HttpStatus.NO_CONTENT);
		}
	}

}
