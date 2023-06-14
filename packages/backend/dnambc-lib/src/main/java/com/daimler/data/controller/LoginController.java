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

package com.daimler.data.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.assembler.UserRoleAssembler;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.entities.UserRoleNsql;
import com.daimler.data.db.jsonb.UserInfoRole;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.service.userrole.UserRoleService;
import com.daimler.data.util.JWTGenerator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Login API", tags = { "authentication" })
@RequestMapping("/api")
@Slf4j
public class LoginController {

	private static Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	@Value("${oidc.provider}")
	private String oidcProvider;

	@Value("${oidc.token.issuer}")
	private String issuerUrl;

	@Value("${oidc.disabled}")
	private boolean oidcDisabled;

	@Value("${oidc.token.introspection.url}")
	private String oidcTokenIntrospectionUrl;

	@Value("${oidc.token.revocation.url}")
	private String oidcTokenRevocationUrl;

	@Value("${oidc.userinfo.url}")
	private String userInfoUrl;

	@Value("${oidc.clientId}")
	private String clientId;

	@Value("${oidc.clientSecret}")
	private String clientSecret;

	@Value("${drd.request-url}")
	private String drdRequestUrl;

	@Value("${dna.feature.internal-user-enabled}")
	private boolean internalUserEnabled;

	@Value("${dna.user.role}")
	private String USER_ROLE;

	@Autowired
	private RestTemplate restTemplate;

	@Lazy
	@Autowired
	private RestTemplate drdRestTemplate;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private UserInfoAssembler userInfoAssembler;

	@Autowired
	private UserRoleAssembler userRoleAssembler;

	@ApiOperation(value = "Authenticates and generates a JWT on successful authentication.", nickname = "login", notes = "ApplicationLogin", response = String.class, tags = {
			"authentication", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User Authenticated Successfully"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/login", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<String> login(@RequestHeader("Authorization") String oauthToken) {

		if (StringUtils.isEmpty(oauthToken)) {
			return new ResponseEntity<>("{\"errmsg\": \"Invalid Token!\"}", HttpStatus.BAD_REQUEST);
		}
		if (oidcDisabled) {
			log.debug("OIDC is disabled, generating a dummy token");
//            String jwt = JWTGenerator.createJWT(getMockUser());
//            userinfoService.updateUserToken(getMockUser().getId(), jwt);
			userInfoService.updateNewUserToken("DEMOUSER", true);
			return new ResponseEntity<>(
					"{\"token\": \"" + JWTGenerator.generateJWT(getMockUser(), "00014JWuWlHW0ajrhQRXvOCiSFj1")
							+ "\",\"loggedIn\":\"Y\"}",
					HttpStatus.OK);
		} else if ("OKTA".equalsIgnoreCase(oidcProvider) || "GOOGLE".equalsIgnoreCase(oidcProvider)) {
			log.debug("Verifying access token with {}", oidcProvider);
			IntrospectionResponse response = doOKTATokenIntrospection(oauthToken);
			if (response.getSub() != null && response.getActive().equalsIgnoreCase("true")) {
				UserInfo userInfo = fetchOKTAUserInfo(oauthToken, response.getSub());
				if (userInfo == null) {
					return new ResponseEntity<>("{\"errmsg\": \"Error fetching userinfo or No user exists!\"}",
							HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					userInfo.setId(userInfo.getEmail());
					String jwt = JWTGenerator.generateJWT(userInfo, oauthToken);
					userInfoService.updateNewUserToken(userInfo.getEmail(), true);
					return new ResponseEntity<String>("{\"token\": \"" + jwt + "\",\"loggedIn\":\"Y\"}", HttpStatus.OK);
				}

			} else {
				return new ResponseEntity<>("{\"errmsg\": \"Token Introspection Failed!\"}", HttpStatus.NOT_ACCEPTABLE);
			}

		} else {
			log.debug("OIDC is enabled, introspecting the token");

			IntrospectionResponse response = doTokenIntrospection(oauthToken);
			if (response.getSub() != null) {
				UserInfo userInfo = fetchUserInfo(oauthToken, response.getSub());
				if (userInfo == null) {
					return new ResponseEntity<>("{\"errmsg\": \"Error fetching userinfo or No user exists!\"}",
							HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					String jwt = JWTGenerator.generateJWT(userInfo, oauthToken);
					userInfoService.updateNewUserToken(userInfo.getId(), true);
					return new ResponseEntity<String>("{\"token\": \"" + jwt + "\",\"loggedIn\":\"Y\"}", HttpStatus.OK);
				}

			} else {
				return new ResponseEntity<>("{\"errmsg\": \"Token Introspection Failed!\"}", HttpStatus.NOT_ACCEPTABLE);
			}

		}
	}

	@ApiOperation(value = "Verifies the JWT and returns user details.", nickname = "verifyLogin", notes = "ApplicationLogin", response = String.class, tags = {
			"authentication", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User Verified Successfully"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/verifyLogin", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<String> verifyLogin(@RequestHeader("Authorization") String jwt) {
		log.trace("Verify login ");
		if (StringUtils.isEmpty(jwt)) {
			return new ResponseEntity<>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
		} else {
			boolean tokenMappedToUser = false;
			String userId = "";
			Claims claims = JWTGenerator.decodeJWT(jwt);
			log.debug("Verify login claim {}", claims);
			if (claims == null) {
				return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
			}
			userId = (String) claims.get("id");
			log.debug("Verify login {}", userId);
			String oauthToken = (String) claims.get("authToken");
			if (StringUtils.isEmpty(userId)) {
				return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
			} else if (!userInfoService.isLoggedIn(userId)) {
				return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
			} else {
				if (oidcDisabled) {
					tokenMappedToUser = true;
				} else {
					/*
					 * IntrospectionResponse response = doTokenIntrospection(oauthToken);
					 * tokenMappedToUser = response.getActive()!=null?
					 * response.getActive().equalsIgnoreCase("Y"): false; if(tokenMappedToUser) {
					 * UserInfo userInfo = fetchUserInfo(oauthToken, response.getSub()); jwt =
					 * JWTGenerator.generateJWT(userInfo, oauthToken); }
					 */
					jwt = JWTGenerator.refreshJWT(claims, oauthToken);
					tokenMappedToUser = true;
				}
				if (tokenMappedToUser) {
					ObjectMapper mapper = new ObjectMapper();
					List roles = (List) claims.get("digiRole");
					String role = null;
					List<String> divisions = (List<String>) claims.get("divisionAdmins");
					String divisionAdmins = null;
					try {
						role = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(roles.toArray())
								.replaceAll("\n", "");
						divisionAdmins = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(divisions)
								.replaceAll("\n", "");
					} catch (JsonProcessingException e) {
						return new ResponseEntity<String>("{\"errmsg\": \"Error Parsing JWT!\"}",
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
					return new ResponseEntity<String>("{\"token\": \"" + jwt
							+ "\",\"loggedIn\":\"Y\",\"data\":{\"roles\":" + role + ",\"department\":\""
							+ claims.get("department") + "\",\"eMail\":\"" + claims.get("email") + "\",\"firstName\":\""
							+ claims.get("firstName") + "\",\"lastName\":\"" + claims.get("lastName") + "\",\"id\":\""
							+ claims.get("id") + "\",\"mobileNumber\":\"" + claims.get("mobileNumber")
							+ "\",\"divisionAdmins\":" + divisionAdmins + "}}", HttpStatus.OK);
				} else {
					return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
				}
			}
		}

	}

	@ApiOperation(value = "Logs the user out.", nickname = "logout", notes = "ApplicationLogout", response = String.class, tags = {
			"authentication", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User Logged ut Successfully"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/logout", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<String> logout(@RequestHeader("AccessToken") String oauthToken,
			@RequestHeader("Authorization") String jwt) {
		if (StringUtils.isEmpty(jwt)) {
			return new ResponseEntity<>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
		} else {
			String userId = "";
			Claims claims = JWTGenerator.decodeJWT(jwt);
			if (claims == null) {
				return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST);
			}
			if (!oidcDisabled) {
				revokeToken(oauthToken);
			}
			userId = (String) claims.get("id");

			userInfoService.updateNewUserToken(userId, false);
			return new ResponseEntity<String>("{\"msg\": \"User Logged out Successfully!\"}", HttpStatus.OK);

		}

	}

	@ApiOperation(value = "Retrieves user details from the DRD system.", nickname = "userinfo", notes = "UserInfo", response = String.class, tags = {
			"authentication", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User Details Retrieved Successfully"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/userinfo/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<?> getDrdUserInfo(
			@ApiParam(value = "UserID of the user for which the details have to be retrieved", required = true) @PathVariable("id") String id) {
		LOGGER.trace("Entering getDrdUserInfo.");
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> request = new HttpEntity<String>(headers);
		if (!internalUserEnabled) {
			return new ResponseEntity<String>("{\"errmsg\": \"This Feature is not allowed.\"}",
					HttpStatus.METHOD_NOT_ALLOWED);
		} else {
			ResponseEntity<String> response = drdRestTemplate.exchange(drdRequestUrl + id, HttpMethod.GET, request,
					String.class);
			ObjectMapper mapper = new ObjectMapper();
			try {
				DRDResponse userInfo = mapper.readValue(response.getBody(), DRDResponse.class);
				return new ResponseEntity<DRDResponse>(userInfo, HttpStatus.OK);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				return new ResponseEntity<String>("{\"errmsg\": \"" + e.getMessage() + "\"}",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	private UserInfo fetchUserInfo(String accessToken, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Bearer " + accessToken);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		String id = "";
		UserInfo userInfo = new UserInfo();

		try {
			ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
			ObjectMapper mapper = new ObjectMapper();
			userInfo = mapper.readValue(response.getBody(), UserInfo.class);
			LOGGER.info("Fetching user:{} from database.", userId);
			id = userInfo.getId();
		} catch (Exception e) {
			if (userId != null && userId.toLowerCase().startsWith("TE".toLowerCase())) {
				log.debug("Technical user {} , bypassed OIDC userinfo fetch", userId);
				id = userId;
			} else {
				log.error("Failed to fetch OIDC User info", e.getMessage());
			}
		}
		if (Objects.isNull(userInfo.getFirstName()) && Objects.isNull(userInfo.getLastName())) {
			LOGGER.info("Null values provided, cannot add user:{}", userId);
			return null;
		}
		UserInfoVO userVO = userInfoService.getById(id);		
		if (Objects.isNull(userVO)) {
			LOGGER.info("User not found, adding the user:{}", id);
			LOGGER.debug("Setting default role as 'User' for: {}", id);
			UserRoleNsql roleEntity = userRoleService.getRoleUser();
			UserInfoRole userRole = new UserInfoRole();
			userRole.setId(roleEntity.getId());
			userRole.setName(roleEntity.getData().getName());
			List<UserInfoRole> userRoleList = new ArrayList<>();
			userRoleList.add(userRole);
			// Setting entity to add new user
			UserInfoNsql userEntity = userInfoAssembler.toEntity(userInfo, userRoleList);
			userEntity.setIsLoggedIn("Y");
			LOGGER.info("Onboarding new user:{}", userId);
			userInfoService.addUser(userEntity);
			userVO = userInfoAssembler.toVo(userEntity);
		}

		List<UserRoleVO> rolesVO = userVO.getRoles();
		List<UserRole> userRoles = userInfoAssembler.toUserRoles(rolesVO);
		List<UserRole> existingRoles = userInfo.getDigiRole();
		if (userRoles != null && !userRoles.isEmpty() && existingRoles != null) {
			existingRoles.addAll(userRoles);
			userInfo.setDigiRole(existingRoles);
		}
		userInfo.setDivisionAdmins(userVO.getDivisionAdmins());
		userInfo.setDepartment(userVO.getDepartment());
		userInfo.setEmail(userVO.getEmail());
		userInfo.setFirstName(userVO.getFirstName());
		userInfo.setId(userVO.getId());
		userInfo.setLastName(userVO.getLastName());
		userInfo.setMobileNumber(userVO.getMobileNumber());
		userInfo.setDigiRole(userRoles);
		return userInfo;
	} 
	
	
	private UserInfo fetchOKTAUserInfo(String accessToken, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Bearer " + accessToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		LOGGER.info("Fetching user:{} from database.",userId);
		ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			UserInfo userInfo = mapper.readValue(response.getBody(), UserInfo.class);
			UserInfoVO userVO = null;
			LOGGER.info("Fetching user information from db.");
			userVO = userInfoService.getById(userInfo.getEmail());
			if(Objects.isNull(userVO)) {
				LOGGER.info("User not found, adding the user:{} ", userInfo.getEmail());
				UserRoleNsql roleEntity = userRoleService.getRoleUser();
				UserInfoRole userRole = new UserInfoRole();
				userRole.setId(roleEntity.getId());
				userRole.setName(roleEntity.getData().getName());
				LOGGER.debug("Setting default role as 'Admin' for: {}",userInfo.getId());
				if ("Admin".equalsIgnoreCase(USER_ROLE)) {
					userRole.setId("3");
					userRole.setName("Admin");
				}
				List<UserInfoRole> userRoleList = new ArrayList<>();
				userRoleList.add(userRole);
				UserInfoNsql userEntity = userInfoAssembler.toEntity(userInfo, userRoleList);
				userEntity.setId(userInfo.getEmail());
				userEntity.setIsLoggedIn("Y");
				LOGGER.debug("Onboarding new user:{}",userId);
				userInfoService.addUser(userEntity);
				userVO = userInfoAssembler.toVo(userEntity);
			}
			List<UserRoleVO> rolesVO = userVO.getRoles();
			List<UserRole> userRoles = userInfoAssembler.toUserRoles(rolesVO);
			List<UserRole> existingRoles = userInfo.getDigiRole();
			if (userRoles != null && !userRoles.isEmpty() && existingRoles != null) {
				existingRoles.addAll(userRoles);
				userInfo.setDigiRole(existingRoles);
			}
			userInfo.setDivisionAdmins(userVO.getDivisionAdmins());
			return userInfo;
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private boolean revokeToken(String accessToken) {
		String basicAuthenticationHeader = Base64.getEncoder()
				.encodeToString(new StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes());
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("token", accessToken);
		map.add("token_type_hint", "access_token");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic " + basicAuthenticationHeader);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		restTemplate.postForEntity(oidcTokenRevocationUrl, request, String.class);
		return true;
	}

	private IntrospectionResponse doOKTATokenIntrospection(String accessToken) {
		String basicAuthenticationHeader = Base64.getEncoder()
				.encodeToString(new StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes());
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("token", accessToken);
		map.add("token_type_hint", "access_token");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic " + basicAuthenticationHeader);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(oidcTokenIntrospectionUrl, request, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			IntrospectionResponse introspectionResponse = objectMapper.readValue(response.getBody(),
					IntrospectionResponse.class);
			log.debug("Introspection Response:" + introspectionResponse);
			return introspectionResponse;
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private IntrospectionResponse doTokenIntrospection(String accessToken) {
		String basicAuthenticationHeader = Base64.getEncoder()
				.encodeToString(new StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes());
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("token", accessToken);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic " + basicAuthenticationHeader);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(oidcTokenIntrospectionUrl, request, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			IntrospectionResponse introspectionResponse = objectMapper.readValue(response.getBody(),
					IntrospectionResponse.class);			
			log.debug("Introspection Response:" + introspectionResponse);
			return introspectionResponse;
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private UserInfo getMockUser() {
		UserInfoVO demoUser = userInfoService.getById("DEMOUSER");
		List<UserRole> roles = new ArrayList<>();
		demoUser.getRoles().forEach(userRoleVO -> {
			roles.add(new UserRole(userRoleVO.getId(), userRoleVO.getName()));
		});
		// roles.add(new UserRole("3", "Admin"));
		return new UserInfo.UserInfoBuilder().id(demoUser.getId()).firstName(demoUser.getFirstName())
				.lastName(demoUser.getLastName()).email(demoUser.getEmail()).mobileNumber(demoUser.getMobileNumber())
				.department(demoUser.getDepartment()).digiRole(roles).build();
	}

	@Data
	@AllArgsConstructor
	@Builder
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class UserInfo {
		private String id;
		private String firstName;
		private String lastName;
		private String email;
		private String mobileNumber;
		private String department;
		private List<UserRole> digiRole;
		private List<String> divisionAdmins;

		private String sub;
		private boolean email_verified;
		private String name;
		private String given_name;
		private String family_name;
		private String personal_data_hint;
		private String updated_at;

		public UserInfo() {
			// Always add USER role
			this.digiRole = new ArrayList<>();
			// digiRole.add(new UserRole("1", "USER"));
			this.department = "";
		}

		public void setSub(String sub) {
			this.sub = this.id = sub;
		}

		public void setGiven_name(String given_name) {
			this.firstName = this.given_name = given_name;
		}

		public void setFamily_name(String family_name) {
			this.lastName = this.family_name = family_name;
		}

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class UserRole implements Serializable {
		private String id;
		private String name;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IntrospectionResponse implements Serializable {
		private String sub;
		private String aud;
		private String scope;
		private String active;
		private String token_type;
		private String exp;
		private String client_id;
		private String auth_time;
	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DRDResponse implements Serializable {
		private String id;
		private String firstName;
		private String lastName;
		private String email;
		private String mobileNumber;
		private String department;

		private Attrs attrs;

		private void setAttrs(Attrs attrs) {
			this.id = attrs.getUid() != null ? attrs.getUid().get(0) : "";
			this.firstName = attrs.getGivenName() != null ? attrs.getGivenName().get(0) : "";
			this.lastName = attrs.getSn() != null ? attrs.getSn().get(0) : "";
			this.email = attrs.getMail() != null ? attrs.getMail().get(0) : "";
			this.mobileNumber = attrs.getMobile() != null ? attrs.getMobile().get(0) : "";
			this.department = attrs.getDepartmentNumber() != null ? attrs.getDepartmentNumber().get(0) : "";
		}

		@NoArgsConstructor
		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		private class Attrs {
			private List<String> dcxCompanyID;
			private List<String> objectClass;
			private List<String> c;
			private List<String> dcxUserSuspended;
			private List<String> cn;

			private List<String> uid;
			private List<String> departmentNumber;
			private List<String> mobile;
			private List<String> mail;
			private List<String> givenName;
			private List<String> sn;
		}
	}
}
