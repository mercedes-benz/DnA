package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.daimler.data.api.kongGateway.KongApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.kongGateway.AttachAppAuthoriserPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachAppAuthoriserPluginVO;
import com.daimler.data.dto.kongGateway.AttachApiAuthoriserPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachApiAuthoriserPluginVO;
import com.daimler.data.dto.kongGateway.AttachFunctionPluginVO;
import com.daimler.data.dto.kongGateway.AttachFunctionPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachJwtPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachOneApiPluginVO;
import com.daimler.data.dto.kongGateway.AttachOneApiPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachJwtPluginVO;
import com.daimler.data.dto.kongGateway.AttachPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachPluginVO;
import com.daimler.data.dto.kongGateway.AttachRequestTransformerPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachRequestTransformerPluginVO;
import com.daimler.data.dto.kongGateway.CreateRouteRequestVO;
import com.daimler.data.dto.kongGateway.CreateRouteResponseVO;
import com.daimler.data.dto.kongGateway.PluginStatusResponseVO;
import com.daimler.data.dto.kongGateway.CreateRouteVO;
import com.daimler.data.dto.kongGateway.CreateServiceRequestVO;
import com.daimler.data.dto.kongGateway.CreateServiceVO;
import com.daimler.data.kong.client.KongClient;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Kong API", tags = { "kong" })
@RequestMapping(value = "/api")
public class KongGatewayController implements KongApi{
	
	@Autowired
	private KongClient kongClient;
	
	private static Logger LOGGER = LoggerFactory.getLogger(KongGatewayController.class);

	@Override
	@ApiOperation(value = "Attach a plugin to service.", nickname = "attachPlugin", notes = "Attach a plugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/plugins",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> attachPlugin(@Valid AttachPluginRequestVO attachPluginRequestVO,
			String serviceName) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachPluginVO attachPluginVO = attachPluginRequestVO.getData();
		if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("jwt")) {
			if(Objects.nonNull(attachPluginVO.getConfig())) {
				AttachPluginConfigVO configVO = attachPluginVO.getConfig();
				if(Objects.isNull(configVO.getClaimsToVerify()) || Objects.isNull(configVO.getKeyClaimName())) {
					MessageDescription msg = new MessageDescription();
					msg.setMessage("Properties claims_to_verify and key_claim_name should not be null for attaching the JWT plugin to service ");
					errors.add(msg);
					response.setErrors(errors);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}				
			}
		}
		if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("oidc")) {
			if(Objects.nonNull(attachPluginVO.getConfig())) {
				AttachPluginConfigVO configVO = attachPluginVO.getConfig();
				if(Objects.isNull(configVO.getBearerOnly()) || Objects.isNull(configVO.getClientId()) ||
					Objects.isNull(configVO.getClientSecret()) || Objects.isNull(configVO.getDiscovery()) ||
					Objects.isNull(configVO.getIntrospectionEndpoint()) || Objects.isNull(configVO.getIntrospectionEndpointAuthMethod()) ||
					Objects.isNull(configVO.getLogoutPath()) || Objects.isNull(configVO.getRealm()) || 
					Objects.isNull(configVO.getRedirectAfterLogoutUri()) || Objects.isNull(configVO.getResponseType()) ||
					Objects.isNull(configVO.getScope()) || Objects.isNull(configVO.getSslVerify()) ||
					Objects.isNull(configVO.getTokenEndpointAuthMethod()) || Objects.isNull(configVO.getRedirectUri())) {
					MessageDescription msg = new MessageDescription();
					msg.setMessage("Properties should not be null for attaching the OIDC plugin to service ");
					errors.add(msg);
					response.setErrors(errors);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}				
			}
		}
		try {
			if(Objects.nonNull(attachPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachPluginToService(attachPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "create a route.", nickname = "createRoute", notes = "create a route", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/routes",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> createRoute(@Valid CreateRouteRequestVO createRouteRequestVO,
			String serviceName) {
		GenericMessage response = new GenericMessage();
		CreateRouteVO createRouteVO = createRouteRequestVO.getData();	
		try {
			if(Objects.nonNull(createRouteVO) && Objects.nonNull(serviceName)) {
				response = kongClient.createRoute(createRouteVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong route {} created successfully", createRouteVO.getName());
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Kong route {} creation failed", createRouteVO.getName());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
							
		}catch(Exception e) {
			LOGGER.error("Failed to create Kong route {} with exception {} ", createRouteVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}

	@Override
	@ApiOperation(value = "Create new service ", nickname = "createService", notes = "Create new service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> createService(@ApiParam(value = "Request Body that contains data required for creating a service" ,required=true ) 
		@Valid @RequestBody CreateServiceRequestVO createServiceRequestVO) {
		CreateServiceVO createServiceVO = createServiceRequestVO.getData();
		GenericMessage response = new GenericMessage();
		try {	
			if(Objects.nonNull(createServiceVO)) {
				String serviceName = createServiceVO.getName();//ws342
				String url = createServiceVO.getUrl();//http://ws342.code-server:8080
				response = kongClient.createService(serviceName, url);		
				//return new ResponseEntity<>(response, HttpStatus.CREATED);
			}			
			if (Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong service {} created successfully", createServiceVO.getName());
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				LOGGER.info("Kong service {} creation failed", createServiceVO.getName());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create Kong service {} with exception {} ", createServiceVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}			
	}	

	@Override
	 @ApiOperation(value = "Get all the existing services ", nickname = "getAllServices", notes = "Get all the existing kong services.", response = String.class, responseContainer = "List", tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<List<String>> getAllServices() {
		List<String> serviceNames = kongClient.getAllServices();
		return new ResponseEntity<>(serviceNames,HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Attach jwtissuer plugin to service.", nickname = "attachJwtIssuerPlugin", notes = "Attach jwtissuer plugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/jwtplugins",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> attachJwtIssuerPlugin(
			@Valid AttachJwtPluginRequestVO attachJwtPluginRequestVO, String serviceName) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachJwtPluginVO attachJwtPluginVO = attachJwtPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachJwtPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachJwtPluginToService(attachJwtPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachJwtPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachJwtPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachJwtPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	
	}

	@Override
	@ApiOperation(value = "Attach appAuthoriserPlugin to service.", nickname = "attachAppAuthoriserPlugin", notes = "Attach appAuthoriserPlugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/appAuthoriserPlugin",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> attachAppAuthoriserPlugin(
			@Valid AttachAppAuthoriserPluginRequestVO attachAppAuthoriserPluginRequestVO, String serviceName) {

		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachAppAuthoriserPluginVO attachAppAuthoriserPluginVO = attachAppAuthoriserPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachAppAuthoriserPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachAppAuthoriserPluginToService(attachAppAuthoriserPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachAppAuthoriserPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachAppAuthoriserPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachAppAuthoriserPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	
	
	}

	@Override
	@ApiOperation(value = "Delete a route.", nickname = "deleteRoute", notes = "Delete a route", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/routes/{routeName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteRoute(String serviceName, String routeName) {
		GenericMessage response = new GenericMessage();		
		try {
			if(Objects.nonNull(serviceName) && Objects.nonNull(routeName)) {
				response = kongClient.deleteRoute(serviceName, routeName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong route {} deleted successfully", routeName);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else {
				LOGGER.info("Kong route {} deletion failed", routeName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
							
		}catch(Exception e) {
			LOGGER.error("Failed to delete Kong route {} with exception {} ", routeName,e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	@Override
	@ApiOperation(value = "Delete the existing service ", nickname = "deleteService", notes = "Delete the existing service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteService(String serviceName) {
		GenericMessage response = new GenericMessage();		
		try {
			if(Objects.nonNull(serviceName)) {
				response = kongClient.deleteService(serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong service {} deleted successfully", serviceName);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else {
				LOGGER.info("Kong service {} deletion failed", serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
							
		}catch(Exception e) {
			LOGGER.error("Failed to delete Kong service {} with exception {} ", serviceName,e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Attach apiAuthoriserPlugin to service.", nickname = "attachApiAuthoriserPlugin", notes = "Attach apiAuthoriserPlugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/apiAuthoriserPlugin",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> attachApiAuthoriserPlugin(
			@Valid AttachApiAuthoriserPluginRequestVO attachApiAuthoriserPluginRequestVO, String serviceName) {

		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachApiAuthoriserPluginVO attachApiAuthoriserPluginVO = attachApiAuthoriserPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachApiAuthoriserPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachApiAuthoriserPluginToService(attachApiAuthoriserPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachApiAuthoriserPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachApiAuthoriserPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachApiAuthoriserPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	
	
	}

	@Override
	@ApiOperation(value = "Delete a plugin.", nickname = "deletePlugin", notes = "Delete a plugin", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/plugins/{pluginName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deletePlugin(String serviceName, String pluginName) {
		GenericMessage response = new GenericMessage();		
		try {
			if(Objects.nonNull(serviceName) && Objects.nonNull(pluginName)) {
				response = kongClient.deletePlugin(serviceName, pluginName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong plugin {} deleted successfully", pluginName);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else {
				LOGGER.info("Kong plugin {} deletion failed", pluginName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
							
		}catch(Exception e) {
			LOGGER.error("Failed to delete Kong plugin {} with exception {} ", pluginName,e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

@Override
	@ApiOperation(value = "Attach functionPlugin to service.", nickname = "attachFunctionPlugin", notes = "Attach functionPlugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/functionPlugin",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> attachFunctionPlugin(@Valid AttachFunctionPluginRequestVO attachFunctionPluginRequestVO, String serviceName){

		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachFunctionPluginVO attachFunctionPluginVO = attachFunctionPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachFunctionPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachFunctionPluginToService(attachFunctionPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachFunctionPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachFunctionPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachFunctionPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
  
  @Override
	@ApiOperation(value = "update status of a  plugin.", nickname = "upatePluginStatus", notes = "update status plugin", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/plugins/{pluginName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PATCH)
    public ResponseEntity<GenericMessage> upatePluginStatus(String serviceName, String pluginName, Boolean enable){
		GenericMessage response = new GenericMessage();		
		try {
			if(Objects.nonNull(serviceName) && Objects.nonNull(pluginName) && Objects.nonNull(enable)) {
				response = kongClient.updatePluginStatus(serviceName, pluginName,enable);
			}
			else {
				LOGGER.info("Kong plugin {} update status  failed", pluginName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong plugin {} updated the status successfully", pluginName);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getErrors()) && response.getSuccess().equalsIgnoreCase("NOT_FOUND")) {
				LOGGER.info("Kong plugin {} not exsits", pluginName);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			LOGGER.info("Kong plugin {} update status  failed with error", pluginName);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}catch(Exception e) {
			LOGGER.error("Failed to update status of Kong plugin {} with exception {} ", pluginName,e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Attach requesttransformerplugin to service.", nickname = "attachRequestTransformnerPlugin", notes = "Attach requesttransformerplugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/requestTransformerPlugin",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> attachRequestTransformerPlugin(@Valid  AttachRequestTransformerPluginRequestVO attachRequestTransformerPluginRequestVO, String serviceName){
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();

		AttachRequestTransformerPluginVO attachRequestTransformerPluginVO = attachRequestTransformerPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachRequestTransformerPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachRequestTransformerPluginToService(attachRequestTransformerPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachRequestTransformerPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching request transformer plugin {} to the service {} failed with error {}", attachRequestTransformerPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		catch(Exception e) {
			LOGGER.error("Failed to attach request transformer plugin {} with exception {} ", attachRequestTransformerPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Attach one api Plugin to service.", nickname = "attachOneApiPlugin", notes = "Attach one api Plugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/oneApiPlugin",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> attachOneApiPlugin( AttachOneApiPluginRequestVO attachOneApiPluginRequestVO,String serviceName){
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();

		AttachOneApiPluginVO attachOneApiPluginVO = attachOneApiPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachOneApiPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachOneApiPluginToService(attachOneApiPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachOneApiPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching request transformer plugin {} to the service {} failed with error {}", attachOneApiPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		catch(Exception e) {
			LOGGER.error("Failed to attach request transformer plugin {} with exception {} ", attachOneApiPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@Override
	public ResponseEntity<CreateRouteResponseVO> getRouteByName(String serviceName, String routeName) {
		try{
			CreateRouteResponseVO createRouteResponseVO = kongClient.getRouteByName(serviceName,routeName);
			if(Objects.nonNull(createRouteResponseVO)) {
				return new ResponseEntity<>(createRouteResponseVO, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception e) {
			LOGGER.error("Failed to get Kong route details {} with exception {} ", routeName,e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get status of the plugin.", nickname = "getPluginStatus", notes = "Get status of the plugin.", response = PluginStatusResponseVO.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = PluginStatusResponseVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/plugin/{pluginName}/status",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PluginStatusResponseVO> getPluginStatus(@ApiParam(value = "Name of the service for which plugin status has to be fetched",required=true) @PathVariable("serviceName") String serviceName,@ApiParam(value = "Name of the plugin for which the status need to be fetched",required=true) @PathVariable("pluginName") String pluginName){
		try{
			Map<String, Boolean> statusMap = kongClient.getPluginStatus(serviceName, pluginName);
			if (statusMap.isEmpty()) {
				LOGGER.info("Error while finding status of plugin {} for service {}",pluginName,serviceName);
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            PluginStatusResponseVO pluginStatusResponse = new PluginStatusResponseVO();
			if(statusMap.get(pluginName) == null){
				LOGGER.info("Status of plugin {} for service {} is null.",pluginName,serviceName);
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
            pluginStatusResponse.setEnabled(statusMap.get(pluginName));
            return new ResponseEntity<>(pluginStatusResponse, HttpStatus.OK);
		}catch(Exception e){
			LOGGER.error("Failed to get plugin status for plugin {} and service {} with error {}", pluginName, serviceName, e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//	@Override
//	public ResponseEntity<CreateServiceResponseVO> getServiceByName(String serviceName) {
//		CreateServiceResponseVO response = kongClient.getServiceByName(serviceName);
//		if(Objects.nonNull(response.getData())) {
//			return new ResponseEntity<>(response, HttpStatus.OK);
//		}
//		else {
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		
//	}
	

}
