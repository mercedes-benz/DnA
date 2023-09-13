package com.daimler.data.auth.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.WorkbenchManageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuthenticatorClientImpl  implements AuthenticatorClient{
	
	private Logger LOGGER = LoggerFactory.getLogger(AuthenticatorClientImpl.class);

	@Value("${authenticator.uri}")
	private String authenticatorBaseUri;
	
	@Value("${codeServer.env.url}")
	private String codeServerEnvUrl;
	
	@Value("${kong.bearerOnly}")
	private String bearerOnly;
	
	@Value("${kong.clientId}")
	private String clientId;
	
	@Value("${kong.clientSecret}")
	private String clientSecret;
	
	@Value("${kong.discovery}")
	private String discovery;
	
	@Value("${kong.introspectionEndpoint}")
	private String introspectionEndpoint;
	
	@Value("${kong.introspectionEndpointAuthMethod}")
	private String introspectionEndpointAuthMethod;
	
	@Value("${kong.logoutPath}")
	private String logoutPath;
	
	@Value("${kong.realm}")
	private String realm;
	
	@Value("${kong.redirectAfterLogoutUri}")
	private String redirectAfterLogoutUri;
	
	@Value("${kong.redirectUriPath}")
	private String redirectUriPath;
	
	@Value("${kong.responseType}")
	private String responseType;
	
	@Value("${kong.scope}")
	private String scope;
	
	@Value("${kong.sslVerify}")
	private String sslVerify;
	
	@Value("${kong.tokenEndpointAuthMethod}")
	private String tokenEndpointAuthMethod;
	
	@Value("${kong.algorithm}")
	private String jwtAlgorithm;
	
	@Value("${kong.secret}")
	private String jwtSecret;
	
	@Value("${kong.clientHomeUrl}")
	private String jwtClientHomeUrl;
	
	@Value("${kong.privateKeyFilePath}")
	private String jwtPrivateKeyFilePath;
	
	@Value("${kong.expiresIn}")
	private String jwtExpiresIn;
	
	@Value("${kong.jwtClientId}")
	private String jwtClientId;
	
	@Value("${kong.jwtClientSecret}")
	private String jwtClientSecret;


	@Autowired
	RestTemplate restTemplate;
	
	private static final String CREATE_SERVICE = "/api/kong/services";
	private static final String CREATE_ROUTE = "/routes";
	private static final String ATTACH_PLUGIN_TO_SERVICE = "/plugins";
	private static final String WORKSPACE_API = "API";
	private static final String OIDC_PLUGIN = "oidc";
	private static final String JWTISSUER_PLUGIN = "jwtissuer";
	private static final String ATTACH_JWT_PLUGIN_TO_SERVICE = "/jwtplugins";
	
	@Override
	public GenericMessage createService(CreateServiceRequestVO createServiceRequestVO) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		

			String createServiceUri = authenticatorBaseUri + CREATE_SERVICE;
			HttpEntity<CreateServiceRequestVO> entity = new HttpEntity<CreateServiceRequestVO>(createServiceRequestVO,headers);			
			ResponseEntity<String> createServiceResponse = restTemplate.exchange(createServiceUri, HttpMethod.POST, entity, String.class);
			if (createServiceResponse != null && createServiceResponse.getStatusCode()!=null) {
				if(createServiceResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while calling Kong create service API for workspace: {} ",createServiceRequestVO.getData().getName());
				}
				else {
					LOGGER.info("Warnings while calling Kong create service API for workspace: {}, httpstatuscode is {}", createServiceRequestVO.getData().getName(),  createServiceResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from kong create service : " + createServiceResponse.getBody() + " Response Code is : " + createServiceResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("Kong service:{} already exists", createServiceRequestVO.getData().getName());
				MessageDescription error = new MessageDescription();				
				error.setMessage("Kong service already exists");
				errors.add(error);				
			}
			LOGGER.error("Error occured while creating service: {} for kong :{}",createServiceRequestVO.getData().getName(), ex.getMessage());		
			MessageDescription error = new MessageDescription();
			error.setMessage(ex.getMessage());
			errors.add(error);
			
		} 
		catch(Exception e) {
			LOGGER.error("Error occured while calling Kong create service API for workspace: {} with exception {} ", createServiceRequestVO.getData().getName(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed occured while calling Kong create service API for workspace:  " + createServiceRequestVO.getData().getName()+ " with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

	@Override
	public GenericMessage createRoute(CreateRouteRequestVO createRouteRequestVO, String serviceName) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		

			String createRouteUri = authenticatorBaseUri + CREATE_SERVICE + "/" + serviceName + CREATE_ROUTE;
			HttpEntity<CreateRouteRequestVO> entity = new HttpEntity<CreateRouteRequestVO>(createRouteRequestVO,headers);			
			ResponseEntity<String> createRouteResponse = restTemplate.exchange(createRouteUri, HttpMethod.POST, entity, String.class);
			if (createRouteResponse != null && createRouteResponse.getStatusCode()!=null) {
				if(createRouteResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while calling Kong create route: {} API for workspace: {} ",createRouteRequestVO.getData().getName(), serviceName);
				}
				else {
					LOGGER.info("Warnings while calling Kong create route: {} API for workspace: {} , httpstatuscode is {}", createRouteRequestVO.getData().getName(), serviceName,  createRouteResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from kong create route : " + createRouteResponse.getBody() + " Response Code is : " + createRouteResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Error occured while calling Kong create route: {} API for workspace: {} with exception {} ", createRouteRequestVO.getData().getName(), serviceName,  e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed occured while calling Kong create route: " + createRouteRequestVO.getData().getName() + " API for workspace:  " +  serviceName + " with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}
	
	
	// BUG-339 public GenericMessage attachJWTPluginToService(new dto,String serviceName){

	@Override
	public GenericMessage attachPluginToService(AttachPluginRequestVO attachPluginRequestVO, String serviceName) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		

			String attachPluginUri = authenticatorBaseUri + CREATE_SERVICE + "/" + serviceName + ATTACH_PLUGIN_TO_SERVICE;
			HttpEntity<AttachPluginRequestVO> entity = new HttpEntity<AttachPluginRequestVO>(attachPluginRequestVO,headers);			
			ResponseEntity<String> attachPluginResponse = restTemplate.exchange(attachPluginUri, HttpMethod.POST, entity, String.class);
			if (attachPluginResponse != null && attachPluginResponse.getStatusCode()!=null) {
				if(attachPluginResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while calling Kong attach plugin: {} for the service {} ",attachPluginRequestVO.getData().getName(), serviceName);
				}
				else {
					LOGGER.info("Warnings while calling Kong attach plugin:{} API for workspace: {} , httpstatuscode is {}", attachPluginRequestVO.getData().getName(), serviceName,  attachPluginResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from kong attach plugin : " + attachPluginResponse.getBody() + " Response Code is : " + attachPluginResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Error occured while calling Kong attach plugin: {} API for workspace: {} with exception {} ", attachPluginRequestVO.getData().getName(), serviceName,  e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Error occured while calling Kong attach plugin: " + attachPluginRequestVO.getData().getName() + " API for workspace:  " +  serviceName + " with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}
	
	public void callingKongApis(String serviceName, String env, boolean apiRecipe) {
		
		boolean kongApiForDeploymentURL = false;
		String deploymentServiceName = "";
		String url = "";
		if(serviceName.contains(WORKSPACE_API) && Objects.nonNull(env)) {
			kongApiForDeploymentURL = true;
			String[] wsid = serviceName.split("-");
			deploymentServiceName = wsid[0];
		}
		
		// request for kong create service	
		if(kongApiForDeploymentURL) {					    		    
			url = deploymentServiceName + "-" + env + ".codespaces-apps";
		}
		else {
			url = "http://" + serviceName + ".code-server:8080";
		}
		CreateServiceRequestVO createServiceRequestVO = new CreateServiceRequestVO();
		CreateServiceVO createServiceVO = new CreateServiceVO();
		createServiceVO.setName(serviceName);
		createServiceVO.setUrl(url);
		createServiceRequestVO.setData(createServiceVO);
		
		//request for creating kong route
		List<String> hosts = new ArrayList();
		List<String> paths = new ArrayList();
		List<String> protocols = new ArrayList();
		if(kongApiForDeploymentURL) {
			paths.add("/" + serviceName + "/" + env + "/api");
		}
		else {
			paths.add("/" + serviceName + "/");
			paths.add("/");
		}
		CreateRouteRequestVO createRouteRequestVO = new CreateRouteRequestVO();
		CreateRouteVO createRouteVO = new CreateRouteVO();		
		protocols.add("http");
		protocols.add("https");
		hosts.add(codeServerEnvUrl);
		createRouteVO.setHosts(hosts);
		createRouteVO.setName(serviceName);
		createRouteVO.setPaths(paths);
		createRouteVO.setProtocols(protocols);
		createRouteVO.setStripPath(true);
		createRouteRequestVO.setData(createRouteVO);
		
		//request for attaching plugin to service
		AttachPluginRequestVO attachPluginRequestVO = new AttachPluginRequestVO();
		AttachPluginVO attachPluginVO = new AttachPluginVO();
		AttachPluginConfigVO attachPluginConfigVO = new AttachPluginConfigVO();
		attachPluginVO.setName(OIDC_PLUGIN);
		attachPluginConfigVO.setBearer_only(bearerOnly);
		attachPluginConfigVO.setClient_id(clientId);
		attachPluginConfigVO.setClient_secret(clientSecret);
		attachPluginConfigVO.setDiscovery(discovery);
		attachPluginConfigVO.setIntrospection_endpoint(introspectionEndpoint);
		attachPluginConfigVO.setIntrospection_endpoint_auth_method(introspectionEndpointAuthMethod);
		attachPluginConfigVO.setLogout_path(logoutPath);
		attachPluginConfigVO.setRealm(realm);
		attachPluginConfigVO.setRedirect_after_logout_uri(redirectAfterLogoutUri);
		attachPluginConfigVO.setRedirect_uri_path(redirectUriPath);
		attachPluginConfigVO.setResponse_type(responseType);
		attachPluginConfigVO.setScope(scope);
		attachPluginConfigVO.setSsl_verify(sslVerify);
		attachPluginConfigVO.setToken_endpoint_auth_method(tokenEndpointAuthMethod);
		attachPluginVO.setConfig(attachPluginConfigVO);
		attachPluginRequestVO.setData(attachPluginVO);
		
		//request for attaching JWT plugin to service
		AttachJwtPluginRequestVO attachJwtPluginRequestVO = new AttachJwtPluginRequestVO();
		AttachJwtPluginVO attachJwtPluginVO = new AttachJwtPluginVO();
		AttachJwtPluginConfigVO attachJwtPluginConfigVO = new AttachJwtPluginConfigVO();
		attachJwtPluginVO.setName(JWTISSUER_PLUGIN);
		attachJwtPluginConfigVO.setAlgorithm(jwtAlgorithm);
		attachJwtPluginConfigVO.setAuthurl(authenticatorBaseUri);
		attachJwtPluginConfigVO.setClientHomeUrl(jwtClientHomeUrl);
		attachJwtPluginConfigVO.setClientId(jwtClientId);
		attachJwtPluginConfigVO.setClientSecret(jwtClientSecret);
		attachJwtPluginConfigVO.setExpiresIn(jwtExpiresIn);
		attachJwtPluginConfigVO.setIntrospectionUri(introspectionEndpoint);
		attachJwtPluginConfigVO.setPrivateKeyFilePath(jwtPrivateKeyFilePath);
		attachJwtPluginConfigVO.setSecret(jwtSecret);
		attachJwtPluginVO.setConfig(attachJwtPluginConfigVO);
		attachJwtPluginRequestVO.setData(attachJwtPluginVO);
		
		GenericMessage createServiceResponse = new GenericMessage();
		GenericMessage createRouteResponse = new GenericMessage();
		GenericMessage attachPluginResponse = new GenericMessage();
		GenericMessage attachJwtPluginResponse = new GenericMessage();
		
		try {	
			boolean isServiceAlreadyCreated = false;
			boolean isRouteAlreadyCreated = false;
			createServiceResponse = createService(createServiceRequestVO);
			if(Objects.nonNull(createServiceResponse) && Objects.nonNull(createServiceResponse.getErrors())) {
				List<MessageDescription> responseErrors = createServiceResponse.getErrors();
				for(MessageDescription error : responseErrors) {
					if(error.getMessage().contains("Kong service already exists")) {
						isServiceAlreadyCreated = true;
					}
				}
			}
			if(createServiceResponse.getSuccess().equalsIgnoreCase("success") || isServiceAlreadyCreated ) {
				createRouteResponse = createRoute(createRouteRequestVO, serviceName);
				if(Objects.nonNull(createRouteResponse) && Objects.nonNull(createRouteResponse.getErrors())) {
					List<MessageDescription> responseErrors = createRouteResponse.getErrors();
					for(MessageDescription error : responseErrors) {
						if(error.getMessage().contains("Route already exist")) {
							isRouteAlreadyCreated = true;
						}
					}
				}
			}
			else {
				LOGGER.info("Failed while calling kong create service API with errors " + createServiceResponse.getErrors());
				return;
			}
			if((createServiceResponse.getSuccess().equalsIgnoreCase("success")  || isServiceAlreadyCreated )&& (createRouteResponse.getSuccess().equalsIgnoreCase("success") || isRouteAlreadyCreated)) {
				if(!kongApiForDeploymentURL) {
					LOGGER.info("kongApiForDeploymentURL is false, calling oidc plugin " );
					attachPluginResponse = attachPluginToService(attachPluginRequestVO,serviceName);
				}
				else {
					if(apiRecipe) {
						LOGGER.info("kongApiForDeploymentURL and apiRecipe is true, calling jwtissuer plugin " );
						attachJwtPluginResponse = attachJwtPluginToService(attachJwtPluginRequestVO,serviceName);
					}
					LOGGER.info("kongApiForDeploymentURL is true and apiRecipe is false, calling jwtissuer plugin " );
					attachPluginResponse = attachPluginToService(attachPluginRequestVO,serviceName);
				}
			}
			
			else {
				LOGGER.info("Failed while calling kong create route API with errors " + createRouteResponse.getErrors());
				return;
			}
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage());
		}
		
		if (!kongApiForDeploymentURL && createServiceResponse.getSuccess().equalsIgnoreCase("success")
				&& createRouteResponse.getSuccess().equalsIgnoreCase("success")
				&& attachPluginResponse.getSuccess().equalsIgnoreCase("success")) {
			LOGGER.info("Kong service, kong route and oidc plugin is attached to the service: {} " + serviceName);

		}
		if (kongApiForDeploymentURL && createServiceResponse.getSuccess().equalsIgnoreCase("success")
				&& createRouteResponse.getSuccess().equalsIgnoreCase("success")
				&& attachJwtPluginResponse.getSuccess().equalsIgnoreCase("success")) {
			LOGGER.info("Kong service, kong route and jwtissuer plugin is attached to the service: {} " + serviceName);

		}
		else {
			LOGGER.info("kong create service status is: {} and errors if any: {}, warnings if any:", createServiceResponse.getSuccess(),
					createServiceResponse.getErrors(), createServiceResponse.getWarnings());
			LOGGER.info("kong create route status is: {} and errors if any: {}, warnings if any:", createRouteResponse.getSuccess(), 
					createRouteResponse.getErrors(), createRouteResponse.getWarnings());
			LOGGER.info("kong attach plugin to service status is: {} and errors if any: {}, warnings if any:", attachPluginResponse.getSuccess(),
					attachPluginResponse.getErrors(), attachPluginResponse.getWarnings());
			LOGGER.info("kong attach jwtissuer plugin to service status is: {} and errors if any: {}, warnings if any:", attachJwtPluginResponse.getSuccess(),
					attachJwtPluginResponse.getErrors(), attachJwtPluginResponse.getWarnings());
		}

	}

	@Override
	public GenericMessage attachJwtPluginToService(AttachJwtPluginRequestVO attachJwtPluginRequestVO, String serviceName) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		

			String attachPluginUri = authenticatorBaseUri + CREATE_SERVICE + "/" + serviceName + ATTACH_JWT_PLUGIN_TO_SERVICE;
			HttpEntity<AttachJwtPluginRequestVO> entity = new HttpEntity<AttachJwtPluginRequestVO>(attachJwtPluginRequestVO,headers);			
			ResponseEntity<String> attachJwtPluginResponse = restTemplate.exchange(attachPluginUri, HttpMethod.POST, entity, String.class);
			if (attachJwtPluginResponse != null && attachJwtPluginResponse.getStatusCode()!=null) {
				if(attachJwtPluginResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while calling Kong attach plugin: {} for the service {} ",attachJwtPluginRequestVO.getData().getName(), serviceName);
				}
				else {
					LOGGER.info("Warnings while calling Kong attach plugin:{} API for workspace: {} , httpstatuscode is {}", attachJwtPluginRequestVO.getData().getName(), serviceName,  attachJwtPluginResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from kong attach plugin : " + attachJwtPluginResponse.getBody() + " Response Code is : " + attachJwtPluginResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Failed to secure apis with IAM for workspace: {} with exception {} . Please contact admin for resolving. ", serviceName,  e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Error occured while calling Kong attach plugin: " + attachJwtPluginRequestVO.getData().getName() + " API for workspace:  " +  serviceName + " with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

}
