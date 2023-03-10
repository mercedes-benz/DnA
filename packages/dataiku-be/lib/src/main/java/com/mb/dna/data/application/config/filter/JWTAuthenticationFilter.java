package com.mb.dna.data.application.config.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationFailed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JWTAuthenticationFilter implements HttpServerFilter{

	private Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

	@Value("${dna.dnaAuthEnable}")
	private boolean dnaAuthEnable;
	
	@Inject
	private DnaClient dnaAuthClient;
	
	private UserStore userStore;
	
	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
		String jwt = request.getHeaders().get("Authorization");
		if(jwt == null || !jwt.startsWith("Bearer ")) {
			throw new AuthenticationException(new AuthenticationFailed("JWT token is missing or invalid"));
		}
		Claims claims = JWTGenerator.decodeJWT(jwt);
		String userId = (String) claims.get("id");
		if (claims == null) {
			throw new AuthenticationException(new AuthenticationFailed("JWT token is missing or invalid"));
		} else {
			if (dnaAuthEnable) {
				JSONObject res = dnaAuthClient.verifyLogin(jwt);
				if (res != null) {
					try {
						setUserDetailsToStore(res);
						filterChain.doFilter(servletRequest, servletResponse);
					} finally {
						this.userStore.clear();
					}

				} else {
					log.error("Request UnAuthorized,No JWT available");
					forbidResponse(servletResponse);
					return;
				}

			} else {
				try {
					log.debug(
							"Request validation successful, set request user details in the store for further access");
					setUserDetailsToStore(claims);
					filterChain.doFilter(servletRequest, servletResponse);
				} finally {
					this.userStore.clear();
				}
			}

		}
		return null;
	}
	
	private void setUserDetailsToStore(Claims claims) {
		// To Set user details for local development
		UserStore.UserInfo user = UserStore.UserInfo.builder().id((String) claims.get("id"))
				.firstName((String) claims.get("firstName")).lastName((String) claims.get("lastName"))
				.email((String) claims.get("email")).department((String) claims.get("department"))
				.mobileNumber((String) claims.get("mobileNumber")).build();
		// To Set user Roles for local development
		List<LinkedHashMap> claimedRoles = (ArrayList) claims.get("digiRole");
		List<UserRole> roles = new ArrayList<>();
		claimedRoles.forEach(roleMapEntity -> {
			roles.add(UserRole.builder().id((String) roleMapEntity.get("id")).name((String) roleMapEntity.get("name"))
					.build());
		});
		user.setUserRole(roles);
		this.userStore.setUserInfo(user);
	}

	/**
	 * method to set user info from dnabmc client
	 * 
	 * @param claims
	 */
	private void setUserDetailsToStore(JSONObject claims) {
		UserStore.UserInfo user = UserStore.UserInfo.builder().id((String) claims.get("id"))
				.firstName((String) claims.get("firstName")).lastName((String) claims.get("lastName"))
				.email((String) claims.get("eMail")).department((String) claims.get("department"))
				.mobileNumber((String) claims.get("mobileNumber")).build();
		// Adding User Roles
		JSONArray jSONArrayRole = claims.getJSONArray("roles");
		if (!ObjectUtils.isEmpty(jSONArrayRole)) {
			List<UserRole> roles = new ArrayList<>();
			for (int i = 0; i < jSONArrayRole.length(); i++) {
				UserRole userRole = new UserRole();
				userRole.setId(jSONArrayRole.getJSONObject(i).get("id").toString());
				userRole.setName(jSONArrayRole.getJSONObject(i).get("name").toString());
				roles.add(userRole);
			}
			user.setUserRole(roles);
		}
		this.userStore.setUserInfo(user);
	}

}
