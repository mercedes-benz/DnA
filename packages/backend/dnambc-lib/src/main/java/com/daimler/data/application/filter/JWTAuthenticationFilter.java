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

package com.daimler.data.application.filter;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.LoginController;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.JWTGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Claims;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.piwik.java.tracking.PiwikRequest;
import org.piwik.java.tracking.PiwikTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTAuthenticationFilter implements Filter {

	@Value("${oidc.disabled}")
	private boolean oidcDisabled;

	@Value("#{'${jwt.secret.byPassUrl}'.split(';')}")
	private List<String> byPassUrl;

	private UserInfoService userinfoService;

	private UserStore userStore;

	private Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		injectSpringDependecies(servletRequest);
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		String requestUri = httpRequest.getRequestURI();
		log.debug("Intercepting Request to store userinfo:" + requestUri);
		if (!byPassUrl.contains(requestUri)) {
			String userinfo = httpRequest.getHeader("dna-request-userdetails");
			if (!StringUtils.hasText(userinfo)) {
				log.error("Request UnAuthorized,No userinfo available");
				forbidResponse(servletResponse);
				return;
			} else {

				try {
					log.debug(
							"Request validation successful, set request user details in the store for further access");
					setUserDetailsToStore(userinfo);
					servletRequest.setAttribute("userDetails", this.userStore.getUserInfo());
					filterChain.doFilter(servletRequest, servletResponse);
				} catch (Exception e) {
					log.error("Error while storing userDetails {} ", e.getMessage());
					forbidResponse(servletResponse);
					this.userStore.clear();
					return;
				} finally {
					// Otherwise when a previously used container thread is used, it will have the
					// old user id set and
					// if for some reason this filter is skipped, userStore will hold an unreliable
					// value
					this.userStore.clear();
				}
			}
		} else {
			log.debug("Request is exempted from validation");
			filterChain.doFilter(servletRequest, servletResponse);
		}

	}

	private void setUserDetailsToStore(String userinfo) throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		LoginController.UserInfo userInfo = objectMapper.readValue(userinfo, new TypeReference<LoginController.UserInfo>() {});
		List<LoginController.UserRole> userRoles = new ArrayList<>();
    
		try{
			JsonNode rootNode = objectMapper.readTree(userinfo);
			JsonNode digiRoleList = rootNode.get("digiRole");
			if (digiRoleList != null && digiRoleList.isArray()) {
				for (JsonNode role :  digiRoleList) {
					LoginController.UserRole userRole = new LoginController.UserRole();
					userRole.setId(role.get("id").asText());
					userRole.setName(role.get("name").asText());
					userRoles.add(userRole);
				}
			}
        }catch(Exception e){
			log.debug("Exception occured during saving user role");
		}
		userInfo.setDigiRole(digiRoleList);
		this.userStore.setUserInfo(userInfo);
		

	}

	private void forbidResponse(ServletResponse servletResponse) {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		response.reset();
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}

	private void injectSpringDependecies(ServletRequest servletRequest) {
		if (userinfoService == null) {
			ServletContext servletContext = servletRequest.getServletContext();
			WebApplicationContext webApplicationContext = WebApplicationContextUtils
					.getWebApplicationContext(servletContext);
			userinfoService = webApplicationContext.getBean(UserInfoService.class);
			userStore = webApplicationContext.getBean(UserStore.class);
			oidcDisabled = webApplicationContext.getEnvironment().getProperty("oidc.disabled", Boolean.class);
		}
	}
}
