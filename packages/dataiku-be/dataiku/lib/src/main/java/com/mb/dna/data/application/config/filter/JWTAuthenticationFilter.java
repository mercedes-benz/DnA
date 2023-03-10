package com.mb.dna.data.application.config.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mb.dna.data.application.adapter.dna.DnaClient;
import com.mb.dna.data.application.config.filter.UserStore.UserRole;

import io.jsonwebtoken.Claims;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Filter("/api/**") 
@Singleton
public class JWTAuthenticationFilter implements HttpServerFilter{

	private Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

	@Value("${dna.dnaAuthEnable}")
	private boolean dnaAuthEnable;
	
	@Property(name = "${dna.jwt}")
	private static String secret_key;
	
	@Inject
	private DnaClient dnaAuthClient;
	
	@Inject
	private UserStore userStore;
	
	@Inject
	ApplicationContext applicationContext;
	
	@SuppressWarnings("unused")
	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain filterChain) {
		System.out.println("intercepted");
		//injectMicronautApplicationDependecies(request);
		String jwt = request.getHeaders().get("Authorization");
		System.out.println("jwt is " + jwt);
		System.out.println("secret is " + secret_key);
		if (jwt==null || jwt.isBlank() || jwt.isEmpty()) {
			log.error("Request UnAuthorized,No JWT available");
			Optional<MutableHttpResponse<?>> response = Optional.of(HttpResponse.status(HttpStatus.FORBIDDEN));
			return Publishers.just(response.get());
		} else {
			System.out.println("got jwt, going for decode");
			Claims claims = JWTGenerator.decodeJWT(jwt);
			log.info("Claims:" + claims.toString());
			String userId = (String) claims.get("id");
			if (claims == null) {
				log.error("Invalid  JWT!");
				Optional<MutableHttpResponse<?>> response = Optional.of(HttpResponse.status(HttpStatus.UNAUTHORIZED));
				return Publishers.just(response.get());
			} else {
				System.out.println("dnaauthenabled is " + dnaAuthEnable);
				if (dnaAuthEnable) {
					JSONObject res = dnaAuthClient.verifyLogin(jwt);
					if (res != null) {
						try {
							setUserDetailsToStore(res);
							filterChain.proceed(request);
						} finally {
							this.userStore.clear();
						}

					} else {
						log.error("Request UnAuthorized,No JWT available");
						Optional<MutableHttpResponse<?>> response = Optional.of(HttpResponse.status(HttpStatus.FORBIDDEN));
						return Publishers.just(response.get());
					}

				} else {
					try {
						log.info(
								"Request validation successful, set request user details in the store for further access");
						setUserDetailsToStore(claims);
						filterChain.proceed(request);
					} finally {
						this.userStore.clear();
					}
				}

			}
		}
		return Publishers.map(filterChain.proceed(request), mutableHttpResponse -> {
	          return mutableHttpResponse;
	        });
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
		if (jSONArrayRole!= null && !jSONArrayRole.isEmpty()) {
			List<UserRole> roles = new ArrayList<>();
			for (int i = 0; i < jSONArrayRole.length(); i++) {
				UserRole userRole = new UserRole();
				userRole.setId(jSONArrayRole.getJSONObject(i).get("id").toString());
				userRole.setName(jSONArrayRole.getJSONObject(i).get("name").toString());
				roles.add(userRole);
			}
			user.setUserRole(roles);
		}
		System.out.println("user id is " + user.getId());
		this.userStore.setUserInfo(user);
	}

	private void injectMicronautApplicationDependecies(HttpRequest<?> servletRequest) {
		ApplicationContext applicationContext = this.applicationContext;
		userStore = applicationContext.getBean(UserStore.class);
		System.out.println("got userstorebean");
	}

}
