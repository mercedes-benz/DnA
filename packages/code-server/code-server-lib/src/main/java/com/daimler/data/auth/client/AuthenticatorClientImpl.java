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
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;

@Component
public class AuthenticatorClientImpl  implements AuthenticatorClient{
	
	private Logger LOGGER = LoggerFactory.getLogger(AuthenticatorClientImpl.class);

	@Autowired
	private WorkspaceCustomRepository customRepository;
	
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

	@Value("${kong.uiRecipesToUseOidc}")
	private boolean uiRecipesToUseOidc;
	
	@Value("${kong.revokeTokensOnLogout}")
	private String revokeTokensOnLogout;
	
	@Value("${kong.enableAuthTokenIntrospection}")
	private boolean enableAuthTokenIntrospection;
	
	@Value("${kong.csvalidateurl}")
	private String csvalidateurl;
	

	@Autowired
	RestTemplate restTemplate;
	
	private static final String CREATE_SERVICE = "/api/kong/services";
	private static final String CREATE_ROUTE = "/routes";
	private static final String ATTACH_PLUGIN_TO_SERVICE = "/plugins";
	private static final String WORKSPACE_API = "api";
	private static final String OIDC_PLUGIN = "oidc";
	private static final String JWTISSUER_PLUGIN = "jwtissuer";
	private static final String APP_AUTHORISER_PLUGIN = "appauthoriser";
	private static final String ATTACH_JWT_PLUGIN_TO_SERVICE = "/jwtplugins";
	private static final String ATTACH_APP_AUTHORISER_PLUGIN_TO_SERVICE = "/appAuthoriserPlugin";
	
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
	
	public void callingKongApis(String wsid,String serviceName, String env, boolean apiRecipe) {
		boolean kongApiForDeploymentURL = !wsid.equalsIgnoreCase(serviceName) && Objects.nonNull(env);
		CodeServerWorkspaceNsql workspaceNsql = customRepository.findByWorkspaceId(wsid);
		CodeServerDeploymentDetails intDeploymentDetails = workspaceNsql.getData().getProjectDetails().getIntDeploymentDetails();
		CodeServerDeploymentDetails prodDeploymentDetails = workspaceNsql.getData().getProjectDetails().getProdDeploymentDetails();
		Boolean intSecureIAM = false;
		Boolean prodSecureIAM = false;
		if(Objects.nonNull(prodDeploymentDetails)) {
			prodSecureIAM = prodDeploymentDetails.getSecureWithIAMRequired(); 
		}
		if(Objects.nonNull(intDeploymentDetails)) {
			intSecureIAM = intDeploymentDetails.getSecureWithIAMRequired(); 
		}
		LOGGER.info("Codespace deployed to production with enabling secureIAM is :{}",prodSecureIAM);
		LOGGER.info("Codespace deployed to staging with enabling secureIAM is :{}",intSecureIAM);
		String url = "";		
		
		// request for kong create service	
		CreateServiceRequestVO createServiceRequestVO = new CreateServiceRequestVO();
		CreateServiceVO createServiceVO = new CreateServiceVO();
		if(kongApiForDeploymentURL) {					    		    
			url = "http://" + serviceName.toLowerCase() + "-" + env + ".codespaces-apps:80";
		}
		else {
			url = "http://" + serviceName.toLowerCase() + ".code-server:8080";
		}		
		createServiceVO.setName(serviceName);
		createServiceVO.setUrl(url);
		createServiceRequestVO.setData(createServiceVO);
		
		//request for creating kong route
		List<String> hosts = new ArrayList();
		List<String> paths = new ArrayList();
		List<String> protocols = new ArrayList();
		String currentPath = "";
		CreateRouteRequestVO createRouteRequestVO = new CreateRouteRequestVO();
		CreateRouteVO createRouteVO = new CreateRouteVO();
		if(kongApiForDeploymentURL) {
			if(apiRecipe) {
				currentPath = "/" + serviceName.toLowerCase() + "/" + env + "/api";
				if(env.equalsIgnoreCase("int"))
					paths.add("/" + serviceName.toLowerCase() + "/" + "int" + "/api");
				if(env.equalsIgnoreCase("prod"))
					paths.add("/" + serviceName.toLowerCase() + "/" + "prod" + "/api");
			}
			else {
				currentPath = "/" + serviceName.toLowerCase() + "/" + env + "/";
				if(env.equalsIgnoreCase("int"))
					paths.add("/" + serviceName.toLowerCase() + "/" + "int/");
				if(env.equalsIgnoreCase("prod"))
					paths.add("/" + serviceName.toLowerCase() + "/" + "prod/");
			}
//			if(Objects.nonNull(intSecureIAM) && intSecureIAM) {
//				paths.add("/" + serviceName + "/" + "int" + "/api");
//			}
//			if(Objects.nonNull(prodSecureIAM) && prodSecureIAM) {
//				paths.add("/" + serviceName + "/" + "prod" + "/api");
//			}
			if(!(paths.contains(currentPath))) {
				paths.add(currentPath);
			}			
		}
		else {
			paths.add("/" + serviceName);
		}
		protocols.add("http");
		protocols.add("https");
		hosts.add(codeServerEnvUrl);
		createRouteVO.setName(serviceName);
		createRouteVO.setHosts(hosts);		
		createRouteVO.setPaths(paths);
		createRouteVO.setProtocols(protocols);
		createRouteVO.setStripPath(true);
		createRouteRequestVO.setData(createRouteVO);
		
		//request for attaching plugin to service
		AttachPluginRequestVO attachPluginRequestVO = new AttachPluginRequestVO();
		AttachPluginVO attachPluginVO = new AttachPluginVO();
		AttachPluginConfigVO attachPluginConfigVO = new AttachPluginConfigVO();

		attachPluginVO.setName(OIDC_PLUGIN);

		String recovery_page_path = "https://" + codeServerEnvUrl + "/" + serviceName + "/";	
		String redirectUri = "/" + serviceName;

		attachPluginConfigVO.setBearer_only(bearerOnly);
		attachPluginConfigVO.setClient_id(clientId);
		attachPluginConfigVO.setClient_secret(clientSecret);
		attachPluginConfigVO.setDiscovery(discovery);
		attachPluginConfigVO.setIntrospection_endpoint(introspectionEndpoint);
		attachPluginConfigVO.setIntrospection_endpoint_auth_method(introspectionEndpointAuthMethod);
		attachPluginConfigVO.setLogout_path(logoutPath);
		attachPluginConfigVO.setRealm(realm);
		attachPluginConfigVO.setRedirect_after_logout_uri(redirectAfterLogoutUri);
		attachPluginConfigVO.setRedirect_uri(redirectUri);
		attachPluginConfigVO.setRevoke_tokens_on_logout(revokeTokensOnLogout);
		attachPluginConfigVO.setResponse_type(responseType);
		attachPluginConfigVO.setScope(scope);
		attachPluginConfigVO.setSsl_verify(sslVerify);
		attachPluginConfigVO.setToken_endpoint_auth_method(tokenEndpointAuthMethod);
		attachPluginConfigVO.setRecovery_page_path(recovery_page_path);
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
		attachJwtPluginConfigVO.setClient_id(jwtClientId);
		attachJwtPluginConfigVO.setClient_secret(jwtClientSecret);
		attachJwtPluginConfigVO.setExpiresIn(jwtExpiresIn);
		attachJwtPluginConfigVO.setIntrospection_uri(introspectionEndpoint);
		attachJwtPluginConfigVO.setEnableAuthTokenIntrospection(enableAuthTokenIntrospection);
		attachJwtPluginConfigVO.setPrivateKeyFilePath(jwtPrivateKeyFilePath);
		attachJwtPluginConfigVO.setSecret(jwtSecret);
		attachJwtPluginVO.setConfig(attachJwtPluginConfigVO);
		attachJwtPluginRequestVO.setData(attachJwtPluginVO);
		
		//request for attaching APPAUTHORISER plugin to service
		AttachAppAuthoriserPluginRequestVO appAuthoriserPluginRequestVO = new AttachAppAuthoriserPluginRequestVO();
		AttachAppAuthoriserPluginVO appAuthoriserPluginVO = new AttachAppAuthoriserPluginVO();
		AttachAppAuthoriserPluginConfigVO appAuthoriserPluginConfigVO = new AttachAppAuthoriserPluginConfigVO();
		appAuthoriserPluginConfigVO.setCsvalidateurl(csvalidateurl);
		appAuthoriserPluginVO.setName(APP_AUTHORISER_PLUGIN);
		appAuthoriserPluginVO.setConfig(appAuthoriserPluginConfigVO);
		appAuthoriserPluginRequestVO.setData(appAuthoriserPluginVO);
		
		GenericMessage createServiceResponse = new GenericMessage();
		GenericMessage createRouteResponse = new GenericMessage();
		GenericMessage attachPluginResponse = new GenericMessage();
		GenericMessage attachJwtPluginResponse = new GenericMessage();
		GenericMessage attachAppAuthoriserPluginResponse = new GenericMessage();
		
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
					LOGGER.info("kongApiForDeploymentURL is false, calling oidc and appauthoriser plugin " );
					attachPluginResponse = attachPluginToService(attachPluginRequestVO,serviceName);
					attachAppAuthoriserPluginResponse = attachAppAuthoriserPluginToService(appAuthoriserPluginRequestVO, serviceName);
				}
				else {
					if(!apiRecipe && uiRecipesToUseOidc) {
						LOGGER.info("kongApiForDeploymentURL is {} and apiRecipe is {} and uiRecipesToUseOidc is : {}, calling oidc plugin ",kongApiForDeploymentURL, apiRecipe, uiRecipesToUseOidc );
						attachPluginResponse = attachPluginToService(attachPluginRequestVO,serviceName);
					}
					else {
						if(intSecureIAM || prodSecureIAM) {
							attachJwtPluginResponse = attachJwtPluginToService(attachJwtPluginRequestVO,serviceName);
							LOGGER.info("kongApiForDeploymentURL is {} and apiRecipe is {} and uiRecipesToUseOidc is : {}, calling jwtissuer plugin ",kongApiForDeploymentURL, apiRecipe, uiRecipesToUseOidc );
						}else {
							LOGGER.info("Secure with IAM false, hence didnt add jwt plugin to service. kongApiForDeploymentURL is {} and apiRecipe is {} and uiRecipesToUseOidc is : {}, calling jwtissuer plugin ",kongApiForDeploymentURL, apiRecipe, uiRecipesToUseOidc );
						}
					}
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
			String errors = createServiceResponse.getErrors()!= null && !createServiceResponse.getErrors().isEmpty() ? createServiceResponse.getErrors().get(0).getMessage() : "";
			String warnings =  createServiceResponse.getWarnings()!= null && !createServiceResponse.getWarnings().isEmpty() ? createServiceResponse.getWarnings().get(0).getMessage() : "";
			LOGGER.info("kong create service status is: {} and errors if any: {}, warnings if any:", createServiceResponse.getSuccess(),
					errors, warnings);
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
	
	


	@Override
	public GenericMessage deleteService(String serviceName) {

		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			String deleteServiceUri = authenticatorBaseUri + CREATE_SERVICE  +"/" + serviceName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(deleteServiceUri, HttpMethod.DELETE, entity, String.class);
			if (response != null && response.hasBody()) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					message.setSuccess("Success");		
					message.setErrors(errors);
					message.setWarnings(warnings);
					LOGGER.info("Kong service:{} deleted successfully", serviceName);
					return message;
				}
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
			LOGGER.error("Service {} does not exist", serviceName);
			messageDescription.setMessage("Service does not exist");
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
			}
			LOGGER.error("Exception: {} occured while deleting service: {} details",ex.getMessage(), serviceName);			
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		catch(Exception e) {
			LOGGER.error("Error: {} while deleting service: {} details",e.getMessage(), serviceName);			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			errors.add(messageDescription);
			message.setErrors(errors);
		}
		return message;
	
	}

	@Override
	public GenericMessage deleteRoute(String serviceName, String routeName) {

		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			String deleteRouteUri = authenticatorBaseUri + CREATE_SERVICE + "/" + serviceName + CREATE_ROUTE + "/" + routeName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(deleteRouteUri, HttpMethod.DELETE, entity, String.class);
			if (response != null) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					message.setSuccess("Success");		
					message.setErrors(errors);
					message.setWarnings(warnings);
					LOGGER.info("Kong route:{} for the service {} deleted successfully", routeName, serviceName);
					return message;
				}
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
			LOGGER.error("Route {} does not exist", routeName);
			messageDescription.setMessage("Route does not exist");
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
			}
			LOGGER.error("Exception occured: {} while deleting route: {} details", ex.getMessage(),routeName);			
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		catch(Exception e) {
			LOGGER.error("Error occured: {} while deleting route: {} details", e.getMessage(),routeName);			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			errors.add(messageDescription);
			message.setErrors(errors);
		}
		return message;
	
	}

	@Override
	public GenericMessage attachAppAuthoriserPluginToService(AttachAppAuthoriserPluginRequestVO attachAppAuthoriserPluginRequestVO, String serviceName) {

		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		

			String attachPluginUri = authenticatorBaseUri + CREATE_SERVICE + "/" + serviceName + ATTACH_APP_AUTHORISER_PLUGIN_TO_SERVICE;
			if(attachAppAuthoriserPluginRequestVO==null) {
				AttachAppAuthoriserPluginRequestVO appAuthoriserPluginRequestVO = new AttachAppAuthoriserPluginRequestVO();
				AttachAppAuthoriserPluginVO appAuthoriserPluginVO = new AttachAppAuthoriserPluginVO();
				AttachAppAuthoriserPluginConfigVO appAuthoriserPluginConfigVO = new AttachAppAuthoriserPluginConfigVO();
				appAuthoriserPluginConfigVO.setCsvalidateurl(csvalidateurl);
				appAuthoriserPluginVO.setName(APP_AUTHORISER_PLUGIN);
				appAuthoriserPluginVO.setConfig(appAuthoriserPluginConfigVO);
				appAuthoriserPluginRequestVO.setData(appAuthoriserPluginVO);
				attachAppAuthoriserPluginRequestVO = appAuthoriserPluginRequestVO;
			}
			HttpEntity<AttachAppAuthoriserPluginRequestVO> entity = new HttpEntity<AttachAppAuthoriserPluginRequestVO>(attachAppAuthoriserPluginRequestVO,headers);			
			ResponseEntity<String> attachAppAuthoriserPluginResponse = restTemplate.exchange(attachPluginUri, HttpMethod.POST, entity, String.class);
			if (attachAppAuthoriserPluginResponse != null && attachAppAuthoriserPluginResponse.getStatusCode()!=null) {
				if(attachAppAuthoriserPluginResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while calling Kong attach plugin: {} for the service {} ",attachAppAuthoriserPluginRequestVO.getData().getName(), serviceName);
				}
				else {
					LOGGER.info("Warnings while calling Kong attach plugin:{} API for workspace: {} , httpstatuscode is {}", attachAppAuthoriserPluginRequestVO.getData().getName(), serviceName,  attachAppAuthoriserPluginResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from kong attach plugin : " + attachAppAuthoriserPluginResponse.getBody() + " Response Code is : " + attachAppAuthoriserPluginResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Failed to secure apis with IAM for workspace: {} with exception {} . Please contact admin for resolving. ", serviceName,  e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Error occured while calling Kong attach plugin: " + attachAppAuthoriserPluginRequestVO.getData().getName() + " API for workspace:  " +  serviceName + " with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	
	}

}
