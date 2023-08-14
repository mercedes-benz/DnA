package com.daimler.data.controller;

import java.util.Objects;

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

import com.daimler.data.api.kongGateway.KongApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.kongGateway.AttachPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachPluginVO;
import com.daimler.data.dto.kongGateway.CreateRouteRequestVO;
import com.daimler.data.dto.kongGateway.CreateRouteResponseVO;
import com.daimler.data.dto.kongGateway.CreateRouteVO;
import com.daimler.data.dto.kongGateway.CreateServiceRequestVO;
import com.daimler.data.dto.kongGateway.CreateServiceResponseVO;
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
	public ResponseEntity<GenericMessage> attachPlugin(@Valid AttachPluginRequestVO attachPluginRequestVO,
			String serviceName) {
		GenericMessage response = new GenericMessage();
		AttachPluginVO attachPluginVO = attachPluginRequestVO.getData();
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

//	@Override
//	public ResponseEntity<CreateRouteResponseVO> getRouteByName(String serviceName, String routeName) {
//		CreateRouteResponseVO createRouteResponseVO = kongClient.getRouteByName(serviceName,routeName);
//		if(Objects.nonNull(createRouteResponseVO)) {
//			return new ResponseEntity<>(createRouteResponseVO, HttpStatus.OK);
//		}
//		else {
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

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
