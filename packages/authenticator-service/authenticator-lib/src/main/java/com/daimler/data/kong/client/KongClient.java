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

package com.daimler.data.kong.client;

import java.util.List;
import java.util.Map;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.kongGateway.AttachAppAuthoriserPluginVO;
import com.daimler.data.dto.kongGateway.AttachApiAuthoriserPluginVO;
import com.daimler.data.dto.kongGateway.AttachJwtPluginVO;
import com.daimler.data.dto.kongGateway.AttachPluginVO;
import com.daimler.data.dto.kongGateway.CreateRouteResponseVO;
import com.daimler.data.dto.kongGateway.CreateRouteVO;

public interface KongClient {

	public GenericMessage createService(String name, String url);
	
//	public CreateServiceResponseVO getServiceByName(String serviceName);

	public GenericMessage createRoute(CreateRouteVO createRouteVO, String serviceName);
	
	public CreateRouteResponseVO getRouteByName(String serviceName, String routeName);

	public GenericMessage attachPluginToService(AttachPluginVO attachPluginVO, String serviceName);
	
	public GenericMessage attachJwtPluginToService(AttachJwtPluginVO attachJwtPluginVO, String serviceName);
	
	public GenericMessage attachAppAuthoriserPluginToService(AttachAppAuthoriserPluginVO attachAppAuthoriserPluginVO, String serviceName);

	public GenericMessage attachApiAuthoriserPluginToService(AttachApiAuthoriserPluginVO attachAppAuthoriserPluginVO, String serviceName);
	
	public List<String> getAllServices();
	
	public GenericMessage deleteRoute(String serviceName, String routeName);

	public GenericMessage deletePlugin(String serviceName, String pluginName);

	public Map<String,String> getPluginIds(String serviceName, String pluginName);
	
	public GenericMessage deleteService(String serviceName);
	
}
