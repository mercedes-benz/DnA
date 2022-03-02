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

package com.daimler.dna.airflow.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/*@RestController
@RequestMapping("/api/v1")
@Api(value = "Users", tags = { "users" })
//@Slf4j
*/public class LoginController {

	// private Logger log = LoggerFactory.getLogger(LoginController.class);

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

	/*
	 * @Value("${drd.request-url}") private String drdRequestUrl;
	 */

	@Autowired
	private RestTemplate restTemplate;

	/*
	 * @ApiOperation(value = "update users in airflow", nickname = "updateUser",
	 * notes = "Existing Airflow user will be updated with this api", response =
	 * AirflowResponseWrapperVO.class, tags = { "users", })
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Returns message of succes or failure ",
	 * response = AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request", response =
	 * AirflowResponseWrapperVO.class),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Method not allowed"),
	 * 
	 * @ApiResponse(code = 500, message = "Internal error") })
	 * 
	 * @RequestMapping(value = "/authorization/{userId}", method =
	 * RequestMethod.PUT) public ResponseEntity<AirflowResponseWrapperVO>
	 * updateUser(
	 * 
	 * @ApiParam(value = "UserId in path", required = true) @PathVariable("userId")
	 * String userId,
	 * 
	 * @ApiParam(value =
	 * "Request Body that contains data to create user in airflow", required =
	 * true) @Valid @RequestBody AirflowUserVO airflowUserVo) { return null; }
	 */

	/*
	 * @Autowired private RestTemplate drdRestTemplate;
	 */

	/*
	 * @Autowired private UserInfoService userInfoService;
	 * 
	 * @Autowired private UserRoleService userRoleService;
	 * 
	 * @Autowired private UserInfoAssembler userInfoAssembler;
	 * 
	 * @Autowired private UserRoleAssembler userRoleAssembler;
	 * 
	 * @ApiOperation(value =
	 * "Authenticates and generates a JWT on successful authentication.", nickname =
	 * "login", notes = "ApplicationLogin", response = String.class, tags =
	 * {"authentication",})
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "User Authenticated Successfully"),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request"),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500,
	 * message = "Internal error")})
	 * 
	 * @RequestMapping(value = "/login", produces = {"application/json"}, consumes =
	 * { "application/json"}, method = RequestMethod.GET) public
	 * ResponseEntity<String> login(@RequestHeader("Authorization") String
	 * oauthToken) {
	 * 
	 * // If one wants to access the backend service with a dummy token then this
	 * login method generates one based on // the OIDC_DISBALED flag, else it would
	 * look into the body of the request for a token and would introspect with //
	 * the OIDC if (oidcDisabled == true) {
	 * log.info("OIDC is disabled, generating a dummy token"); // String jwt =
	 * JWTGenerator.createJWT(getMockUser()); //
	 * userinfoService.updateUserToken(getMockUser().getId(), jwt); return new
	 * ResponseEntity<>("{\"token\": \"" + JWTGenerator.createJWT(getMockUser()) +
	 * "\"}", HttpStatus.OK); } else {
	 * log.info("OIDC is enabled, introspecting the token"); if
	 * (!StringUtils.isEmpty(oauthToken)) { IntrospectionResponse response =
	 * doTokenIntrospection(oauthToken); if (response.getSub() != null) { UserInfo
	 * userInfo = fetchUserInfo(oauthToken, response.getSub()); if (userInfo ==
	 * null) { return new ResponseEntity<>
	 * ("{\"errmsg\": \"Error fetching userinfo or No user exists!\"}",
	 * HttpStatus.INTERNAL_SERVER_ERROR); } else { String jwt =
	 * JWTGenerator.createJWT(userInfo);
	 * userInfoService.updateUserToken(userInfo.getId(), jwt); return new
	 * ResponseEntity<String>("{\"token\": \"" + jwt + "\"}", HttpStatus.OK); } }
	 * else { return new
	 * ResponseEntity<>("{\"errmsg\": \"Token Introspection Failed!\"}",
	 * HttpStatus.NOT_ACCEPTABLE); }
	 * 
	 * } else { return new ResponseEntity<>("{\"errmsg\": \"Invalid Token!\"}",
	 * HttpStatus.BAD_REQUEST); } } }
	 * 
	 * @ApiOperation(value = "Verifies the JWT and returns user details.", nickname
	 * = "verifyLogin", notes = "ApplicationLogin", response = String.class, tags =
	 * {"authentication",})
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "User Verified Successfully"),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request"),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500,
	 * message = "Internal error")})
	 * 
	 * @RequestMapping(value = "/verifyLogin", produces = {"application/json"},
	 * consumes = { "application/json"}, method = RequestMethod.POST) public
	 * ResponseEntity<String> verifyLogin(@RequestHeader("Authorization") String
	 * jwt) { if (StringUtils.isEmpty(jwt)) { return new
	 * ResponseEntity<>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST); }
	 * else { Claims claims = JWTGenerator.decodeJWT(jwt); if (claims == null) {
	 * return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}",
	 * HttpStatus.BAD_REQUEST); } String userId = (String) claims.get("id"); if
	 * (StringUtils.isEmpty(userId)) { return new
	 * ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}",
	 * HttpStatus.BAD_REQUEST); } else { boolean tokenMappedToUser = false; if
	 * (oidcDisabled) { tokenMappedToUser = true; } else { tokenMappedToUser =
	 * userInfoService.validateUserToken(userId, jwt); } if (tokenMappedToUser) {
	 * ObjectMapper mapper = new ObjectMapper(); List roles = (List)
	 * claims.get("digiRole"); String role = null; try { role =
	 * mapper.writerWithDefaultPrettyPrinter().writeValueAsString(roles.toArray()).
	 * replaceAll("\n", ""); } catch (JsonProcessingException e) { return new
	 * ResponseEntity<String>("{\"errmsg\": \"Error Parsing JWT!\"}",
	 * HttpStatus.INTERNAL_SERVER_ERROR); } return new
	 * ResponseEntity<String>("{\"data\":{\"roles\":" + role + ",\"department\":\""
	 * + claims.get("department") + "\",\"eMail\":\"" + claims.get("email") +
	 * "\",\"firstName\":\"" + claims.get("firstName") + "\",\"lastName\":\"" +
	 * claims.get("lastName") + "\",\"id\":\"" + claims.get("id") +
	 * "\",\"mobileNumber\":\"" + claims.get("mobileNumber") + "\"}}",
	 * HttpStatus.OK); } else { return new
	 * ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}",
	 * HttpStatus.BAD_REQUEST); } } }
	 * 
	 * }
	 * 
	 * @ApiOperation(value = "Logs the user out.", nickname = "logout", notes =
	 * "ApplicationLogout", response = String.class, tags = {"authentication",})
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "User Logged ut Successfully"),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request"),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500,
	 * message = "Internal error")})
	 * 
	 * @RequestMapping(value = "/logout", produces = {"application/json"}, consumes
	 * = { "application/json"}, method = RequestMethod.POST) public
	 * ResponseEntity<String> logout(@RequestHeader("AccessToken") String
	 * oauthToken, @RequestHeader("Authorization") String jwt) { if
	 * (StringUtils.isEmpty(jwt)) { return new
	 * ResponseEntity<>("{\"errmsg\": \"Invalid JWT!\"}", HttpStatus.BAD_REQUEST); }
	 * else { Claims claims = JWTGenerator.decodeJWT(jwt); if (claims == null) {
	 * return new ResponseEntity<String>("{\"errmsg\": \"Invalid JWT!\"}",
	 * HttpStatus.BAD_REQUEST); } if (!oidcDisabled) { revokeToken(oauthToken); }
	 * String userId = (String) claims.get("id");
	 * userInfoService.updateUserToken(userId, null); return new
	 * ResponseEntity<String>("{\"msg\": \"User Logged out Successfully!\"}",
	 * HttpStatus.OK);
	 * 
	 * 
	 * }
	 * 
	 * }
	 * 
	 * @ApiOperation(value = "Retrieves user details from the DRD system.", nickname
	 * = "userinfo", notes = "UserInfo", response = String.class, tags =
	 * {"authentication",})
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "User Details Retrieved Successfully"),
	 * 
	 * @ApiResponse(code = 400, message = "Bad Request"),
	 * 
	 * @ApiResponse(code = 401, message =
	 * "Request does not have sufficient credentials."),
	 * 
	 * @ApiResponse(code = 403, message = "Request is not authorized."),
	 * 
	 * @ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500,
	 * message = "Internal error")})
	 * 
	 * @RequestMapping(value = "/userinfo/{id}", produces = {"application/json"},
	 * consumes = { "application/json"}, method = RequestMethod.GET) public
	 * ResponseEntity<?> getDrdUserInfo(@ApiParam(value =
	 * "UserID of the user for which the details have to be retrieved", required =
	 * true) @PathVariable("id") String id) { HttpHeaders headers = new
	 * HttpHeaders(); headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	 * HttpEntity<String> request = new HttpEntity<String>(headers);
	 * 
	 * ResponseEntity<String> response = drdRestTemplate.exchange(drdRequestUrl +
	 * id, HttpMethod.GET, request, String.class); ObjectMapper mapper = new
	 * ObjectMapper(); try { DRDResponse userInfo =
	 * mapper.readValue(response.getBody(), DRDResponse.class); return new
	 * ResponseEntity<DRDResponse>(userInfo, HttpStatus.OK); } catch (IOException e)
	 * { log.error(e.getMessage()); e.printStackTrace(); return new
	 * ResponseEntity<String>("{\"errmsg\": \"" + e.getMessage() + "\"}",
	 * HttpStatus.INTERNAL_SERVER_ERROR); } }
	 * 
	 * private UserInfo fetchUserInfo(String accessToken, String userId) {
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	 * headers.set("Authorization", "Bearer " + accessToken); HttpEntity<String>
	 * request = new HttpEntity<String>(headers);
	 * 
	 * ResponseEntity<String> response = restTemplate.exchange(userInfoUrl,
	 * HttpMethod.GET, request, String.class); ObjectMapper mapper = new
	 * ObjectMapper(); try { UserInfo userInfo =
	 * mapper.readValue(response.getBody(), UserInfo.class); UserInfoVO userVO =
	 * null; try { userVO = userInfoService.getById(userInfo.getId()); } catch
	 * (NoSuchElementException e) { log.info("User not found, adding the user " +
	 * userInfo.getId()); UserRoleNsql roleEntity = userRoleService.getRoleUser();
	 * UserInfoRole userRole = new UserInfoRole();
	 * userRole.setId(roleEntity.getId());
	 * userRole.setName(roleEntity.getData().getName()); List<UserInfoRole>
	 * userRoleList = new ArrayList<>(); userRoleList.add(userRole); UserInfoNsql
	 * userEntity = userInfoAssembler.toEntity(userInfo, userRoleList);
	 * userInfoService.addUser(userEntity); userVO =
	 * userInfoAssembler.toVo(userEntity); } List<UserRoleVO> rolesVO =
	 * userVO.getRoles(); List<UserRole> userRoles =
	 * userInfoAssembler.toUserRoles(rolesVO); List<UserRole> existingRoles =
	 * userInfo.getDigiRole(); if (userRoles != null && !userRoles.isEmpty() &&
	 * existingRoles != null) { existingRoles.addAll(userRoles);
	 * userInfo.setDigiRole(existingRoles); } return userInfo; } catch (IOException
	 * e) { log.error(e.getMessage()); return null; } }
	 * 
	 * private boolean revokeToken(String accessToken) { String
	 * basicAuthenticationHeader = Base64.getEncoder().encodeToString(new
	 * StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes()
	 * ); MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
	 * map.add("token", accessToken); map.add("token_type_hint", "access_token");
	 * 
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	 * headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	 * headers.set("Authorization", "Basic " + basicAuthenticationHeader);
	 * HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map,
	 * headers);
	 * 
	 * restTemplate.postForEntity(oidcTokenRevocationUrl, request, String.class);
	 * return true; }
	 * 
	 * private IntrospectionResponse doTokenIntrospection(String accessToken) {
	 * String basicAuthenticationHeader = Base64.getEncoder().encodeToString(new
	 * StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes()
	 * ); MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
	 * map.add("token", accessToken);
	 * 
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	 * headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	 * headers.set("Authorization", "Basic " + basicAuthenticationHeader);
	 * HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map,
	 * headers);
	 * 
	 * ResponseEntity<String> response =
	 * restTemplate.postForEntity(oidcTokenIntrospectionUrl, request, String.class);
	 * ObjectMapper objectMapper = new ObjectMapper(); try { IntrospectionResponse
	 * introspectionResponse = objectMapper.readValue(response.getBody(),
	 * IntrospectionResponse.class); log.debug("Introspection Response:" +
	 * introspectionResponse); return introspectionResponse; } catch (IOException e)
	 * { log.error(e.getMessage()); return null; } }
	 * 
	 * private UserInfo getMockUser() { UserInfoVO demoUser =
	 * userInfoService.getById("DEMOUSER"); List<UserRole> roles = new
	 * ArrayList<>(); demoUser.getRoles().forEach(userRoleVO -> { roles.add(new
	 * UserRole(userRoleVO.getId(),userRoleVO.getName())); }); //roles.add(new
	 * UserRole("3", "Admin")); return new
	 * UserInfo.UserInfoBuilder().id(demoUser.getId()).firstName(demoUser.
	 * getFirstName()).lastName(demoUser.getLastName()).email(demoUser.getEmail()).
	 * mobileNumber(demoUser.getMobileNumber()).department(demoUser.getDepartment())
	 * .digiRole(roles).build(); }
	 */

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
		// private List<UserRole> digiRole;

		private String sub;
		private boolean email_verified;
		private String name;
		private String given_name;
		private String family_name;
		private String personal_data_hint;
		private String updated_at;

		public UserInfo() {
			// Always add USER role
			// this.digiRole = new ArrayList<>();
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

	/*
	 * @Data
	 * 
	 * @AllArgsConstructor
	 * 
	 * @NoArgsConstructor
	 * 
	 * @Builder public static class UserRole implements Serializable { private
	 * String id; private String name; }
	 * 
	 * @Data
	 * 
	 * @AllArgsConstructor
	 * 
	 * @NoArgsConstructor
	 * 
	 * @ToString
	 * 
	 * @JsonIgnoreProperties(ignoreUnknown = true) public static class
	 * IntrospectionResponse implements Serializable { private String sub; private
	 * String aud; private String scope; private String active; private String
	 * token_type; private String exp; private String client_id; private String
	 * auth_time; }
	 * 
	 * @Data
	 * 
	 * @NoArgsConstructor
	 * 
	 * @JsonIgnoreProperties(ignoreUnknown = true) public static class DRDResponse
	 * implements Serializable { private String id; private String firstName;
	 * private String lastName; private String email; private String mobileNumber;
	 * private String department;
	 * 
	 * private Attrs attrs;
	 * 
	 * private void setAttrs(Attrs attrs) { this.id = attrs.getUid() != null ?
	 * attrs.getUid().get(0) : ""; this.firstName = attrs.getGivenName() != null ?
	 * attrs.getGivenName().get(0) : ""; this.lastName = attrs.getSn() != null ?
	 * attrs.getSn().get(0) : ""; this.email = attrs.getMail() != null ?
	 * attrs.getMail().get(0) : ""; this.mobileNumber = attrs.getMobile() != null ?
	 * attrs.getMobile().get(0) : ""; this.department = attrs.getDepartmentNumber()
	 * != null ? attrs.getDepartmentNumber().get(0) : ""; }
	 * 
	 * @NoArgsConstructor
	 * 
	 * @Data
	 * 
	 * @JsonIgnoreProperties(ignoreUnknown = true) private class Attrs { private
	 * List<String> dcxCompanyID; private List<String> objectClass; private
	 * List<String> c; private List<String> dcxUserSuspended; private List<String>
	 * cn;
	 * 
	 * private List<String> uid; private List<String> departmentNumber; private
	 * List<String> mobile; private List<String> mail; private List<String>
	 * givenName; private List<String> sn; } }
	 */
}
