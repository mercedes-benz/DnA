package com.daimler.data.controller;

import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.marketingRole.MarketingRolesApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.marketingRole.MarketingRoleCollection;
import com.daimler.data.dto.marketingRole.MarketingRoleRequestVO;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.marketingRoles.MarketingRoleService;
import com.daimler.data.service.userinfo.UserInfoService;

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
	
	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;


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

	@Override
	 @ApiOperation(value = "Deletes the marketingRole identified by given ID.", nickname = "delete", notes = "Deletes the marketingRole identified by given ID", response = GenericMessage.class, tags={ "marketingRoles", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/marketingRoles/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the marketingRole",required=true) @PathVariable("id") String id) {

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
									"Not authorized to delete roles. User does not have admin privileges.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			marketingRoleService.deleteRole(id);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			LOGGER.error("No Role found with the given id: {}", e.getMessage());
			MessageDescription invalidMsg = new MessageDescription("No Role found with the given id.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			LOGGER.error("Failed to delete due to internal error: {}", e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	
	}

}
