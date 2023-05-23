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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.LoginController;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.JWTGenerator;

import io.jsonwebtoken.Claims;

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
		log.debug("Intercepting Request to validate JWT:" + requestUri);
		if (!byPassUrl.contains(requestUri)) {
			String jwt = httpRequest.getHeader("Authorization");
			if (!StringUtils.hasText(jwt)) {
				log.error("Request UnAuthorized,No JWT available");
				forbidResponse(servletResponse);
				return;
			} else {
				Claims claims = JWTGenerator.decodeJWT(jwt);
				log.trace("Claims:" + claims.toString());
				String userId = (String) claims.get("id");
				if (claims == null) {
					log.error("Invalid  JWT!");
					HttpServletResponse response = (HttpServletResponse) servletResponse;
					response.reset();
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				} else {
					if (!oidcDisabled) {
						if (!StringUtils.hasText(userId)) {
							forbidResponse(servletResponse);
							return;
						} else {
							try {
								boolean tokenMappedToUser = userinfoService.validateUserToken(userId, jwt);
								if (!tokenMappedToUser) {
									forbidResponse(servletResponse);
									this.userStore.clear();
									return;
								}
							} catch (Exception e) {
								log.error("OIDC disabled, failed while authentication {} ", e.getMessage());
								forbidResponse(servletResponse);
								this.userStore.clear();
								return;
							}

						}

					}
					try {
						log.debug(
								"Request validation successful, set request user details in the store for further access");
						setUserDetailsToStore(claims);
						servletRequest.setAttribute("userDetails", this.userStore.getUserInfo());
						filterChain.doFilter(servletRequest, servletResponse);
					} finally {
						// Otherwise when a previously used container thread is used, it will have the
						// old user id set and
						// if for some reason this filter is skipped, userStore will hold an unreliable
						// value
						this.userStore.clear();
					}
				}
			}
		} else {
			log.debug("Request is exempted from validation");
			filterChain.doFilter(servletRequest, servletResponse);
		}

	}

	private void setUserDetailsToStore(Claims claims) {

		LoginController.UserInfo user = LoginController.UserInfo.builder().id((String) claims.get("id"))
				.firstName((String) claims.get("firstName")).lastName((String) claims.get("lastName"))
				.email((String) claims.get("email")).department((String) claims.get("department"))
				.mobileNumber((String) claims.get("mobileNumber")).build();
		List<LinkedHashMap> claimedRoles = (ArrayList) claims.get("digiRole");
		List<LoginController.UserRole> roles = new ArrayList<>();
		claimedRoles.forEach(roleMapEntity -> {
			roles.add(new LoginController.UserRole().builder().id((String) roleMapEntity.get("id"))
					.name((String) roleMapEntity.get("name")).build());
		});
		user.setDigiRole(roles);
		this.userStore.setUserInfo(user);

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
