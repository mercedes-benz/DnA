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
import com.daimler.data.db.json.CodespaceSecurityConfig;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.util.ConstantsUtility;

@Component
public class AuthenticatorClientImpl  implements AuthenticatorClient{
	
	private Logger LOGGER = LoggerFactory.getLogger(AuthenticatorClientImpl.class);

	@Autowired
	private WorkspaceCustomRepository customRepository;
	
	@Value("${authenticator.uri}")
	private String authenticatorBaseUri;

	@Value("${authenticator.uri.aws}")
	private String authenticatorBaseUriAWS;

	@Value("${authenticator.api.aws.key}")
	private String awsApiKey;

	@Value("${authenticator.api.key}")
	private String apiKey;

	@Value("${codeServer.env.url}")
	private String codeServerEnvUrl;

	@Value("${codeServer.env.url.aws}")
	private String codeServerEnvUrlAWS;
	
	@Value("${kong.bearerOnly}")
	private String bearerOnly;
	
	@Value("${kong.clientId}")
	private String clientId;
	
	@Value("${kong.clientSecret}")
	private String clientSec;
	
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

	@Value("${kong.logType}")
	private String logType;

	@Value("${kong.wsconfigurl}")
	private String wsconfigurl;

	@Value("${kong.applicationName}")
	private String applicationName;

	@Value("${kong.poolID}")
	private String poolID;

	@Value("${kong.userinfoIntrospectionUri}")
	private String userinfoIntrospectionUri;

	@Value("${kong.enableUserinfoIntrospection}")
	private Boolean enableUserinfoIntrospection;

	@Value("${kong.authoriserBearerOnly}")
	private String authoriserBearerOnly;

	@Value("${kong.authoriserClientId}")
	private String authoriserClientId;

	@Value("${kong.authoriserClientSecret}")
	private String authoriserClientSecret;

	@Value("${kong.authoriserIntrospectionEndpointAuthMethod}")
	private String authoriserIntrospectionEndpointAuthMethod;

	@Value("${kong.authoriserScope}")
	private String authoriserScope;

	@Value("${kong.authoriserRedirectAfterLogoutUri}")
	private String authRedirectAfterLogoutUri;
 
	@Value("${kong.authoriserIntrospectionEndpoint}")
	private String authIntrospectionEndpoint;
 
	@Value("${kong.authoriserDiscovery}")
	private String authDiscovery;
	
	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;



	@Autowired
	RestTemplate restTemplate;
	
	private static final String CREATE_SERVICE = "/api/kong/services";
	private static final String CREATE_ROUTE = "/routes";
	private static final String ATTACH_PLUGIN_TO_SERVICE = "/plugins";
	private static final String WORKSPACE_API = "api";
	private static final String OIDC_PLUGIN = "oidc";
	private static final String CORS_PLUGIN = "cors";
	private static final String JWTISSUER_PLUGIN = "jwtissuer";
	private static final String APP_AUTHORISER_PLUGIN = "appauthoriser";
	private static final String API_AUTHORISER_PLUGIN = "apiauthoriser";
	private static final String ATTACH_JWT_PLUGIN_TO_SERVICE = "/jwtplugins";
	private static final String ATTACH_API_AUTHORISER_PLUGIN_TO_SERVICE = "/apiAuthoriserPlugin";
	private static final String ATTACH_APP_AUTHORISER_PLUGIN_TO_SERVICE = "/appAuthoriserPlugin";
	
	@Override
	public GenericMessage createService(CreateServiceRequestVO createServiceRequestVO, String cloudServiceProvider) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			String createServiceUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)? authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE;
			HttpEntity<CreateServiceRequestVO> entity = new HttpEntity<CreateServiceRequestVO>(createServiceRequestVO,headers);	
			LOGGER.info("CreateService deploy entity "+entity);
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
	public GenericMessage createRoute(CreateRouteRequestVO createRouteRequestVO, String serviceName, String cloudServiceProvider) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			String createRouteUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + CREATE_ROUTE;
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
	public GenericMessage attachPluginToService(AttachPluginRequestVO attachPluginRequestVO, String serviceName, String cloudServiceProvider) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			LOGGER.info("cloudServiceProvider "+cloudServiceProvider);	
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			String attachPluginUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + ATTACH_PLUGIN_TO_SERVICE;
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
	
	public void callingKongApis(String wsid,String serviceName, String env, boolean apiRecipe, String clientID, String clientSecret, String cloudServiceProvider) {
		boolean kongApiForDeploymentURL = !wsid.equalsIgnoreCase(serviceName) && Objects.nonNull(env);
		CodeServerWorkspaceNsql workspaceNsql = customRepository.findByWorkspaceId(wsid);
		CodeServerDeploymentDetails intDeploymentDetails = workspaceNsql.getData().getProjectDetails().getIntDeploymentDetails();
		CodeServerDeploymentDetails prodDeploymentDetails = workspaceNsql.getData().getProjectDetails().getProdDeploymentDetails();
		CodespaceSecurityConfig securityConfig  = workspaceNsql.getData().getProjectDetails().getSecurityConfig();
		String projectName = workspaceNsql.getData().getProjectDetails().getProjectName();
		Boolean intSecureIAM = false;
		Boolean prodSecureIAM = false;
		LOGGER.info("serviceProvider "+cloudServiceProvider);
		if("prod".equalsIgnoreCase(env)){
			if(Objects.nonNull(prodDeploymentDetails)) {
				prodSecureIAM = prodDeploymentDetails.getSecureWithIAMRequired(); 
			}
		}
		if("int".equalsIgnoreCase(env)){
			if(Objects.nonNull(intDeploymentDetails)) {
				intSecureIAM = intDeploymentDetails.getSecureWithIAMRequired(); 
			}
		}
		LOGGER.info("Codespace deployed to production with enabling secureIAM is :{}",prodSecureIAM);
		LOGGER.info("Codespace deployed to staging with enabling secureIAM is :{}",intSecureIAM);
		String url = "";		
		
		// request for kong create service	
		CreateServiceRequestVO createServiceRequestVO = new CreateServiceRequestVO();
		CreateServiceVO createServiceVO = new CreateServiceVO();
		if(kongApiForDeploymentURL) {	
			url = "http://" + serviceName.toLowerCase() + "-" + env;		
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)){
				if("dev".equalsIgnoreCase(codeServerEnvRef))
					url = url + ".dev-dna-cs-apps:80";
				else if("staging".equalsIgnoreCase(codeServerEnvRef))
					url = url + ".test-dna-cs-apps:80";
				else
					url = url + ".prod-dna-cs-apps:80";
			}else{	    		    
				url = url +  ".codespaces-apps:80";
			}
		}
		else {
			url = "http://" + serviceName.toLowerCase() + ".code-server:8080";
		}		
		createServiceVO.setName(env!=null?serviceName.toLowerCase()+"-"+env:serviceName.toLowerCase());
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
			paths.add("/" + serviceName.toLowerCase());
		}
		protocols.add("http");
		protocols.add("https");
		hosts.add(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?codeServerEnvUrlAWS:codeServerEnvUrl);
		createRouteVO.setName(env!=null?serviceName.toLowerCase()+"-"+env:serviceName.toLowerCase());
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

		String recovery_page_path = "https://" + (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?codeServerEnvUrlAWS:codeServerEnvUrl) + "/" + serviceName.toLowerCase() + "/";	
		String redirectUri = "/" + serviceName.toLowerCase();

		attachPluginConfigVO.setBearer_only(bearerOnly);
		attachPluginConfigVO.setClient_id(clientId);
		attachPluginConfigVO.setClient_secret(clientSec);
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
		attachJwtPluginConfigVO.setAuthurl(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri);
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

		//request for attaching CORS plugin to service
		AttachPluginVO attachCorsPluginVO = new AttachPluginVO();
		AttachPluginRequestVO attachCorsPluginRequestVO = new AttachPluginRequestVO();
		attachCorsPluginVO.setName(CORS_PLUGIN);
		attachCorsPluginRequestVO.setData(attachCorsPluginVO);
		
		GenericMessage createServiceResponse = new GenericMessage();
		GenericMessage createRouteResponse = new GenericMessage();
		GenericMessage attachPluginResponse = new GenericMessage();
		GenericMessage attachJwtPluginResponse = new GenericMessage();
		GenericMessage attachCorsPluginResponse = new GenericMessage();
		GenericMessage attachAppAuthoriserPluginResponse = new GenericMessage();
		GenericMessage attachApiAuthoriserPluginResponse = new GenericMessage();
		try {	
			boolean isServiceAlreadyCreated = false;
			boolean isRouteAlreadyCreated = false;
			createServiceResponse = createService(createServiceRequestVO, cloudServiceProvider);
			if(Objects.nonNull(createServiceResponse) && Objects.nonNull(createServiceResponse.getErrors())) {
				List<MessageDescription> responseErrors = createServiceResponse.getErrors();
				for(MessageDescription error : responseErrors) {
					if(error.getMessage().contains("Kong service already exists")) {
						isServiceAlreadyCreated = true;
					}
				}
			}
			LOGGER.info("calling kong to get route details for projectname : {}",env!=null ? serviceName.toLowerCase()+"-"+env:serviceName);
			RouteResponseVO routeResponse = getRouteByName( env!=null ? serviceName.toLowerCase()+"-"+env:serviceName,  env!=null ? serviceName.toLowerCase()+"-"+env:serviceName,cloudServiceProvider);
			if(routeResponse == null || routeResponse.getId()==null){
				if("success".equalsIgnoreCase(createServiceResponse.getSuccess()) || isServiceAlreadyCreated ) {
					createRouteResponse = createRoute(createRouteRequestVO, env!=null ? serviceName.toLowerCase()+"-"+env:serviceName);
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
			}else{
				LOGGER.info("Route name {} already exist. ",env!=null ? serviceName.toLowerCase()+"-"+env:serviceName);
				isRouteAlreadyCreated = true;
			}
			if(("success".equalsIgnoreCase(createServiceResponse.getSuccess())  || isServiceAlreadyCreated )&& ("success".equalsIgnoreCase(createRouteResponse.getSuccess()) || isRouteAlreadyCreated)) {
				if(!kongApiForDeploymentURL) {
					LOGGER.info("kongApiForDeploymentURL is false, calling oidc and appauthoriser plugin " );
					attachPluginResponse = attachPluginToService(attachPluginRequestVO,serviceName,cloudServiceProvider);
					attachAppAuthoriserPluginResponse = attachAppAuthoriserPluginToService(appAuthoriserPluginRequestVO, serviceName, cloudServiceProvider);
				}
				else {
					//attaching cors plugin to deployments
					LOGGER.info("kongApiForDeploymentURL is true, calling CORS plugin " );
					attachCorsPluginResponse = attachPluginToService(attachCorsPluginRequestVO,serviceName.toLowerCase()+"-"+env,cloudServiceProvider);
					LOGGER.info("kong attach CORS plugin to service status is: {} and errors if any: {}, warnings if any:", attachCorsPluginResponse.getSuccess(),
					attachCorsPluginResponse.getErrors(), attachCorsPluginResponse.getWarnings());

					// if(!apiRecipe && uiRecipesToUseOidc) {
					// 	LOGGER.info("kongApiForDeploymentURL is {} and apiRecipe is {} and uiRecipesToUseOidc is : {}, calling oidc plugin ",kongApiForDeploymentURL, apiRecipe, uiRecipesToUseOidc );
					// 	attachPluginResponse = attachPluginToService(attachPluginRequestVO,env!=null?serviceName.toLowerCase()+"-"+env:serviceName);
					// }else {
					
					if(apiRecipe){
						if(intSecureIAM  || prodSecureIAM) {
								if(Objects.nonNull(clientID) && Objects.nonNull(clientSecret)){
									if(!clientID.isEmpty() && !clientSecret.isEmpty()){
										//deleting OIDC  and Authorizer plugin if already available
										GenericMessage deletePluginResponse = new GenericMessage();
										deletePluginResponse = deletePlugin(serviceName.toLowerCase()+"-"+env,API_AUTHORISER_PLUGIN,cloudServiceProvider);
										LOGGER.info("kong deleting api authorizer plugin to service status is: {} and errors if any: {}, warnings if any:", deletePluginResponse.getSuccess(),
										deletePluginResponse.getErrors(), deletePluginResponse.getWarnings());
										deletePluginResponse = deletePlugin(serviceName.toLowerCase()+"-"+env,OIDC_PLUGIN,cloudServiceProvider);
										LOGGER.info("kong deleting OIDC plugin to service status is: {} and errors if any: {}, warnings if any:", deletePluginResponse.getSuccess(),
										deletePluginResponse.getErrors(), deletePluginResponse.getWarnings(),cloudServiceProvider);
										//deleteing jwy issuer plugin if any
										deletePluginResponse = deletePlugin(serviceName.toLowerCase()+"-"+env,JWTISSUER_PLUGIN,cloudServiceProvider);
										LOGGER.info("kong deleting api authorizer plugin to service status is: {} and errors if any: {}, warnings if any:", deletePluginResponse.getSuccess(),
										deletePluginResponse.getErrors(), deletePluginResponse.getWarnings());
										
										//request for attaching ODIC plugin to authorize service with new client id and secret
										AttachPluginRequestVO attachOIDCPluginRequestVO = new AttachPluginRequestVO();
										AttachPluginVO attachOIDCPluginVO = new AttachPluginVO();
										AttachPluginConfigVO attachOIDCPluginConfigVO = new AttachPluginConfigVO();

										attachOIDCPluginVO.setName(OIDC_PLUGIN);

										String authRecovery_page_path = "https://" + (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?codeServerEnvUrlAWS:codeServerEnvUrl) + "/" + serviceName.toLowerCase() + "/"+env+"/api";	
										String authRedirectUri = "/" + serviceName.toLowerCase()+"/"+env+"/api";

										if("int".equalsIgnoreCase(env)){
											attachOIDCPluginConfigVO.setDiscovery(authDiscovery);
											attachOIDCPluginConfigVO.setIntrospection_endpoint(authIntrospectionEndpoint);
											attachOIDCPluginConfigVO.setRedirect_after_logout_uri(authRedirectAfterLogoutUri);
										}
										if("prod".equalsIgnoreCase(env)){
											String prodDiscovery = authDiscovery.replace("-int","");
											String prodIntrospectionEndpoint = authIntrospectionEndpoint.replace("-int", "");
											String prodRedirectAfterLogoutUri =authRedirectAfterLogoutUri.replace("-int", "");

											attachOIDCPluginConfigVO.setDiscovery(prodDiscovery);
											attachOIDCPluginConfigVO.setIntrospection_endpoint(prodIntrospectionEndpoint);
											attachOIDCPluginConfigVO.setRedirect_after_logout_uri(prodRedirectAfterLogoutUri);
										}
										attachOIDCPluginConfigVO.setBearer_only(authoriserBearerOnly);
										attachOIDCPluginConfigVO.setClient_id(clientID);
										attachOIDCPluginConfigVO.setClient_secret(clientSecret);
										attachOIDCPluginConfigVO.setIntrospection_endpoint_auth_method(authoriserIntrospectionEndpointAuthMethod);
										attachOIDCPluginConfigVO.setLogout_path(logoutPath);
										attachOIDCPluginConfigVO.setRealm(realm);
										attachOIDCPluginConfigVO.setRedirect_uri(authRedirectUri);
										attachOIDCPluginConfigVO.setRevoke_tokens_on_logout(revokeTokensOnLogout);
										attachOIDCPluginConfigVO.setResponse_type(responseType);
										attachOIDCPluginConfigVO.setScope(authoriserScope);
										attachOIDCPluginConfigVO.setSsl_verify(sslVerify);
										attachOIDCPluginConfigVO.setToken_endpoint_auth_method(tokenEndpointAuthMethod);
										attachOIDCPluginConfigVO.setRecovery_page_path(authRecovery_page_path);
										attachOIDCPluginVO.setConfig(attachOIDCPluginConfigVO);
										attachOIDCPluginRequestVO.setData(attachOIDCPluginVO);

										LOGGER.info("kongApiForDeploymentURL is {} and apiRecipe is {}, calling oidc plugin ",kongApiForDeploymentURL, apiRecipe );
										attachPluginResponse = attachPluginToService(attachOIDCPluginRequestVO,serviceName.toLowerCase()+"-"+env,cloudServiceProvider);

										//request for attaching APIAUTHORISER plugin to service
										if("int".equalsIgnoreCase(env)&& securityConfig.getStaging().getPublished().getAppID()!=null || "prod".equalsIgnoreCase(env)&& securityConfig.getProduction().getPublished().getAppID()!=null){
											
											AttachApiAuthoriserPluginRequestVO apiAuthoriserPluginRequestVO = new AttachApiAuthoriserPluginRequestVO();
											AttachApiAuthoriserPluginVO apiAuthoriserPluginVO = new AttachApiAuthoriserPluginVO();
											AttachApiAuthoriserPluginConfigVO apiAuthoriserPluginConfigVO = new AttachApiAuthoriserPluginConfigVO();
											if("int".equalsIgnoreCase(env)){
												apiAuthoriserPluginConfigVO.setEnv("staging");
												apiAuthoriserPluginConfigVO.setUserinfoIntrospectionUri(userinfoIntrospectionUri);
												if(securityConfig.getStaging().getPublished().getAppID()!=null)
													apiAuthoriserPluginConfigVO.setApplicationName(securityConfig.getStaging().getPublished().getAppID());
											}
											if("prod".equalsIgnoreCase(env)){
												apiAuthoriserPluginConfigVO.setEnv("production");
												String prodUserinfoIntrospectionUri = userinfoIntrospectionUri.replace("-int","");
												apiAuthoriserPluginConfigVO.setUserinfoIntrospectionUri(prodUserinfoIntrospectionUri);
												if(securityConfig.getProduction().getPublished().getAppID()!=null)
													apiAuthoriserPluginConfigVO.setApplicationName(securityConfig.getProduction().getPublished().getAppID());
											}
											// apiAuthoriserPluginConfigVO.setApplicationName(applicationName);
											apiAuthoriserPluginConfigVO.setEnableUserinfoIntrospection(enableUserinfoIntrospection);
											apiAuthoriserPluginConfigVO.setLogType(logType);
											apiAuthoriserPluginConfigVO.setPoolID(poolID);
											apiAuthoriserPluginConfigVO.setWsconfigurl(wsconfigurl);
											apiAuthoriserPluginConfigVO.setProjectName(projectName.toLowerCase());
	
											apiAuthoriserPluginVO.setName(API_AUTHORISER_PLUGIN);
											apiAuthoriserPluginVO.setConfig(apiAuthoriserPluginConfigVO);
											apiAuthoriserPluginRequestVO.setData(apiAuthoriserPluginVO);
	
											LOGGER.info("kongApiForDeploymentURL is {} and apiRecipe is :{}, calling apiAuthoriser plugin ",kongApiForDeploymentURL, apiRecipe );
											attachApiAuthoriserPluginResponse = attachApiAuthoriserPluginToService(apiAuthoriserPluginRequestVO, serviceName.toLowerCase()+"-"+env,cloudServiceProvider);
										}
									}
								}
							
							// attachJwtPluginResponse = attachJwtPluginToService(attachJwtPluginRequestVO,env!=null?serviceName.toLowerCase()+"-"+env:serviceName);
							// LOGGER.info("kongApiForDeploymentURL is {} and apiRecipe is {} and uiRecipesToUseOidc is : {}, calling jwtissuer plugin ",kongApiForDeploymentURL, apiRecipe, uiRecipesToUseOidc );
						}else{
							GenericMessage deletePluginResponse = new GenericMessage();
							deletePluginResponse = deletePlugin(serviceName.toLowerCase()+"-"+env,API_AUTHORISER_PLUGIN,cloudServiceProvider);
							LOGGER.info("kong deleting api authorizer plugin to service status is: {} and errors if any: {}, warnings if any:", deletePluginResponse.getSuccess(),
							deletePluginResponse.getErrors(), deletePluginResponse.getWarnings());
							deletePluginResponse = deletePlugin(serviceName.toLowerCase()+"-"+env,OIDC_PLUGIN,cloudServiceProvider);
							LOGGER.info("kong deleting OIDC plugin to service status is: {} and errors if any: {}, warnings if any:", deletePluginResponse.getSuccess(),
							deletePluginResponse.getErrors(), deletePluginResponse.getWarnings());
							//deleteing jwy issuer plugin if any
							deletePluginResponse = deletePlugin(serviceName.toLowerCase()+"-"+env,JWTISSUER_PLUGIN,cloudServiceProvider);
							LOGGER.info("kong deleting api authorizer plugin to service status is: {} and errors if any: {}, warnings if any:", deletePluginResponse.getSuccess(),
							deletePluginResponse.getErrors(), deletePluginResponse.getWarnings());
						}
						// }
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
		
		if (!kongApiForDeploymentURL && "success".equalsIgnoreCase(createServiceResponse.getSuccess())
				&& "success".equalsIgnoreCase(createRouteResponse.getSuccess())
				&& "success".equalsIgnoreCase(attachPluginResponse.getSuccess())) {
			LOGGER.info("Kong service, kong route and oidc plugin is attached to the service: {} " + serviceName);

		}
		if (kongApiForDeploymentURL && "success".equalsIgnoreCase(createServiceResponse.getSuccess())
				&& "success".equalsIgnoreCase(createRouteResponse.getSuccess())
				//&& attachJwtPluginResponse.getSuccess().equalsIgnoreCase("success")
				) {
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
			// LOGGER.info("kong attach jwtissuer plugin to service status is: {} and errors if any: {}, warnings if any:", attachJwtPluginResponse.getSuccess(),
			// 		attachJwtPluginResponse.getErrors(), attachJwtPluginResponse.getWarnings());
		}

	}

	@Override
	public GenericMessage attachJwtPluginToService(AttachJwtPluginRequestVO attachJwtPluginRequestVO, String serviceName, String cloudServiceProvider) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			String attachPluginUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + ATTACH_JWT_PLUGIN_TO_SERVICE;
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
	public GenericMessage deleteService(String serviceName, String cloudServiceProvider) {

		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			
			String deleteServiceUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE  +"/" + serviceName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
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
	public GenericMessage deleteRoute(String serviceName, String routeName, String cloudServiceProvider) {

		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			String deleteRouteUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + CREATE_ROUTE + "/" + routeName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
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
	public GenericMessage deletePlugin(String serviceName, String pluginName, String cloudServiceProvider) {

		GenericMessage message = new GenericMessage();
		MessageDescription messageDescription = new MessageDescription();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			String deleteRouteUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + ATTACH_PLUGIN_TO_SERVICE + "/" + pluginName;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(deleteRouteUri, HttpMethod.DELETE, entity, String.class);
			if (response != null) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					message.setSuccess("Success");		
					message.setErrors(errors);
					message.setWarnings(warnings);
					LOGGER.info("Kong plugin:{} for the service {} deleted successfully", pluginName, serviceName);
					return message;
				}
			}
		}
		catch (HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {			
			LOGGER.error("plugin {} does not exist", pluginName);
			messageDescription.setMessage("Route does not exist");
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
			}
			LOGGER.error("Exception occured: {} while deleting plugin: {} details", ex.getMessage(),pluginName);			
			messageDescription.setMessage(ex.getMessage());
			errors.add(messageDescription);
			message.setErrors(errors);
			return message;
		}
		catch(Exception e) {
			LOGGER.error("Error occured: {} while deleting plugin: {} details", e.getMessage(),pluginName);			
			messageDescription.setMessage(e.getMessage());
			errors.add(messageDescription);
			errors.add(messageDescription);
			message.setErrors(errors);
		}
		return message;
	
	}

	@Override
	public GenericMessage attachAppAuthoriserPluginToService(AttachAppAuthoriserPluginRequestVO attachAppAuthoriserPluginRequestVO, String serviceName, String cloudServiceProvider) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			String attachPluginUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + ATTACH_APP_AUTHORISER_PLUGIN_TO_SERVICE;
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

	@Override
	public GenericMessage attachApiAuthoriserPluginToService(AttachApiAuthoriserPluginRequestVO attachApiAuthoriserPluginRequestVO, String serviceName, String cloudServiceProvider) {

		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");		
			if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
				if(awsApiKey!=null){
					headers.set("apikey", awsApiKey);
				}
			}
			String attachPluginUri = (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)?authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + ATTACH_API_AUTHORISER_PLUGIN_TO_SERVICE;
			HttpEntity<AttachApiAuthoriserPluginRequestVO> entity = new HttpEntity<AttachApiAuthoriserPluginRequestVO>(attachApiAuthoriserPluginRequestVO,headers);			
			ResponseEntity<String> attachApiAuthoriserPluginResponse = restTemplate.exchange(attachPluginUri, HttpMethod.POST, entity, String.class);
			if (attachApiAuthoriserPluginResponse != null && attachApiAuthoriserPluginResponse.getStatusCode()!=null) {
				if(attachApiAuthoriserPluginResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while calling Kong attach plugin: {} for the service {} ",attachApiAuthoriserPluginRequestVO.getData().getName(), serviceName);
				}
				else {
					LOGGER.info("Warnings while calling Kong attach plugin:{} API for workspace: {} , httpstatuscode is {}", attachApiAuthoriserPluginRequestVO.getData().getName(), serviceName,  attachApiAuthoriserPluginResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from kong attach plugin : " + attachApiAuthoriserPluginResponse.getBody() + " Response Code is : " + attachApiAuthoriserPluginResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Failed to secure apis with IAM for workspace: {} with exception {} . Please contact admin for resolving. ", serviceName,  e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Error occured while calling Kong attach plugin: " + attachApiAuthoriserPluginRequestVO.getData().getName() + " API for workspace:  " +  serviceName + " with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	
	}

	@Override
	public RouteResponseVO getRouteByName(String serviceName, String routeName, String cloudServiceProvider) {
    RouteResponseVO routeResponseVO = new RouteResponseVO();
    try {
        String kongUri =  (cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS)? authenticatorBaseUriAWS:authenticatorBaseUri) + CREATE_SERVICE + "/" + serviceName + "/routes/" + routeName;
      try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            if(cloudServiceProvider.equalsIgnoreCase(ConstantsUtility.DHC_CAAS_AWS) && apiKey.equals("NA")){
              if(awsApiKey!=null){
                headers.set("apikey", awsApiKey);
              }
            }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(kongUri, HttpMethod.GET, entity, String.class);
        if (response != null && response.hasBody()) {
            HttpStatus statusCode = response.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                String jsonString = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                try {
                    JsonNode rootNode = mapper.readTree(jsonString);
                    JsonNode dataNode = rootNode.path("data");
                    routeResponseVO = mapper.treeToValue(dataNode, RouteResponseVO.class);
                } catch (JsonMappingException e) {
                    LOGGER.error("JsonMappingException for get route {}", e.getMessage());
                } catch (JsonProcessingException e) {
                    LOGGER.error("JsonProcessingException for get route{}", e.getMessage());
                }
                return routeResponseVO;
            }
        }
    } catch (HttpClientErrorException ex) {
        LOGGER.error("Error while getting route details  {} error: {}", serviceName, ex.getMessage());
        return routeResponseVO;
    } catch (Exception e) {
        LOGGER.error("Exception occurred while getting route details: {} details {}.", serviceName, e.getMessage());
        return routeResponseVO;
    }
    return routeResponseVO;
}

}
