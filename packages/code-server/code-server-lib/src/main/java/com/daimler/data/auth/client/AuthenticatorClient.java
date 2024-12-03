package com.daimler.data.auth.client;

import com.daimler.data.controller.exceptions.GenericMessage;

public interface AuthenticatorClient {
	
	public GenericMessage createService(CreateServiceRequestVO createServiceRequestVO);
	
	public GenericMessage createRoute(CreateRouteRequestVO createRouteRequestVO, String serviceName);
	
	public GenericMessage attachPluginToService(AttachPluginRequestVO attachPluginRequestVO, String serviceName);
	
	public GenericMessage attachJwtPluginToService(AttachJwtPluginRequestVO attachJwtPluginRequestVO, String serviceName);
	
	public GenericMessage attachAppAuthoriserPluginToService(AttachAppAuthoriserPluginRequestVO attachAppAuthoriserPluginRequestVO, String serviceName);

	public GenericMessage attachApiAuthoriserPluginToService(AttachApiAuthoriserPluginRequestVO attachApiAuthoriserPluginRequestVO, String serviceName);
	
	public void callingKongApis(String userId, String serviceName, String env, boolean apiRecipe, String clientID, String clientSecret);
	
	public GenericMessage deleteService(String serviceName);
	
	public GenericMessage deleteRoute(String serviceName, String routeName);

	public GenericMessage deletePlugin(String serviceName, String pluginName);

	public RouteResponseVO getRouteByName(String serviceName, String routeName);

}
