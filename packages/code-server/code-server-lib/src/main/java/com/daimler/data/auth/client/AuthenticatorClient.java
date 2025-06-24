package com.daimler.data.auth.client;
 
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.json.CodespaceSecurityConfig;
import com.daimler.data.dto.workspace.WorkspacePluginStatusVO;
 
public interface AuthenticatorClient {
	public GenericMessage createService(CreateServiceRequestVO createServiceRequestVO, String cloudServiceProvider);
	public GenericMessage createRoute(CreateRouteRequestVO createRouteRequestVO, String serviceName, String cloudServiceProvider);
	public GenericMessage attachPluginToService(AttachPluginRequestVO attachPluginRequestVO, String serviceName, String cloudServiceProvider);
	public GenericMessage attachJwtPluginToService(AttachJwtPluginRequestVO attachJwtPluginRequestVO, String serviceName, String cloudServiceProvider);
	public GenericMessage attachAppAuthoriserPluginToService(AttachAppAuthoriserPluginRequestVO attachAppAuthoriserPluginRequestVO, String serviceName, String cloudServiceProvider);
 
	public GenericMessage attachApiAuthoriserPluginToService(AttachApiAuthoriserPluginRequestVO attachApiAuthoriserPluginRequestVO, String serviceName, String cloudServiceProvider);
	public void callingKongApis(String wsid, String serviceName, String env, boolean apiRecipe, String clientID, String clientSecret, String redirectUriFromUser, String ignorePaths, String scope, String oneApiVersionShortName, boolean isSecuredWithCookie, boolean secureWithIAM, String ssoType, boolean secureWithDna, String cloudServiceProvider);
	public void callingApiAuthorizerPlugin(String wsid, String serviceName, String env, boolean apiRecipe, CodespaceSecurityConfig securityConfig, String ssoType, String cloudServiceProvider);
	public GenericMessage deleteService(String serviceName, String cloudServiceProvider);
	public GenericMessage deleteRoute(String serviceName, String routeName, String cloudServiceProvider);
 
	public GenericMessage deletePlugin(String serviceName, String pluginName, String cloudServiceProvider);
 
	public RouteResponseVO getRouteByName(String serviceName, String routeName, String cloudServiceProvider);
 
	public GenericMessage attachFunctionPluginToService(AttachFunctionPluginRequestVO attachFunctionPluginRequestVO, String serviceName);
 
	public GenericMessage changePluginStatus(String serviceName, String pluginName, Boolean isEnabled);

	public WorkspacePluginStatusVO getPluginStatus(String serviceName, String pluginName, String cloudServiceProvider);
 
	public GenericMessage attachRequestTransformerPluginToService(AttachRequestTransformerPluginRequestVO attachRequestTransformerPluginRequestVO, String serviceName);
 
	public GenericMessage attachOneApiPluginToService(AttachOneApiPluginRequestVO attachOneApiPluginRequestVO, String serviceName);
}
