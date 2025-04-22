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
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.daimler.data.auth.vault.VaultAuthClientImpl;
 import com.daimler.data.dto.auth.ApiKeyValidationResponseVO;
 import com.daimler.data.dto.auth.ApiKeyValidationVO;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import org.springframework.util.ObjectUtils;
 import org.springframework.util.StringUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.util.UriTemplate;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.daimler.data.application.auth.UserStore;
 import com.daimler.data.application.auth.UserStore.UserRole;
 import com.daimler.data.auth.client.DnaAuthClient;
 
 import io.jsonwebtoken.Claims;
 
 @Component
 public class JWTAuthenticationFilter implements Filter {
 
     private Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
 
 
     private UserStore userStore;
 
     @Autowired
     private DnaAuthClient dnaAuthClient;
 
     @Autowired
     private VaultAuthClientImpl vaultAuthClient;
 
     @Override
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
             throws IOException, ServletException {
         // filterChain.doFilter(servletRequest, servletResponse);
         injectSpringDependecies(servletRequest);
         HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
         String requestUri = httpRequest.getRequestURI();
         log.debug("Intercepting Request to store userinfo:" + requestUri);
         String userinfo = httpRequest.getHeader("dna-request-userdetails");
         if (!StringUtils.hasText(userinfo)) {
             String uriPattern = "/api/forecasts/{id}";
             UriTemplate uriTemplate = new UriTemplate(uriPattern);
             Map<String, String> pathVariables = uriTemplate.match(requestUri);
             String appId = pathVariables.get("id");
             String appKey = httpRequest.getHeader("apiKey");
             if (appId != null && appKey != null) {
                 ApiKeyValidationVO apiKeyValidationVO = new ApiKeyValidationVO();
                 apiKeyValidationVO.setApiKey(appKey);
                 apiKeyValidationVO.setAppId(appId);
                 ApiKeyValidationResponseVO apiKeyValidationResponseVO = vaultAuthClient
                         .validateApiKey(apiKeyValidationVO);
                 if (apiKeyValidationResponseVO.isValidApiKey()) {
                     JSONObject userdetails = vaultAuthClient.getUserDetails(appId);
 
                     if (userdetails != null) {
                         userdetails.put("eMail", userdetails.get("email"));
                         userdetails.put("roles", new JSONArray());
                     }
                     setUserDetailsToStore(userdetails);
                     filterChain.doFilter(servletRequest, servletResponse);
                     return;
                 } else {
                     log.error("Request UnAuthorized, Validated appId and appKey failed");
                     forbidResponse(servletResponse);
                     return;
                 }
             }
             log.error("Request UnAuthorized,No appkey available");
             forbidResponse(servletResponse);
             return;
         } else if (StringUtils.hasText(userinfo)) {
             
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
         } else {
             log.debug("Request is exempted from validation");
             filterChain.doFilter(servletRequest, servletResponse);
         }
 
     }
 
     // private void setUserDetailsToStore(Claims claims) {
     // // To Set user details for local development
     // UserStore.UserInfo user = UserStore.UserInfo.builder().id((String)
     // claims.get("id"))
     // .firstName((String) claims.get("firstName")).lastName((String)
     // claims.get("lastName"))
     // .email((String) claims.get("email")).department((String)
     // claims.get("department"))
     // .mobileNumber((String) claims.get("mobileNumber")).build();
     // // To Set user Roles for local development
     // List<LinkedHashMap> claimedRoles = (ArrayList) claims.get("digiRole");
     // List<UserRole> roles = new ArrayList<>();
     // claimedRoles.forEach(roleMapEntity -> {
     // roles.add(UserRole.builder().id((String)
     // roleMapEntity.get("id")).name((String) roleMapEntity.get("name"))
     // .build());
     // });
     // user.setUserRole(roles);
     // this.userStore.setUserInfo(user);
     // }
 
     private void setUserDetailsToStore(String userinfo) throws JsonProcessingException {
 
         ObjectMapper objectMapper = new ObjectMapper();
         UserStore.UserInfo userInfo = objectMapper.readValue(userinfo, new TypeReference<UserStore.UserInfo>() {
         });
         this.userStore.setUserInfo(userInfo);
         List<UserRole> userRoles = new ArrayList<>();
         try{
             JsonNode rootNode = objectMapper.readTree(userinfo);
             JsonNode digiRoleList = rootNode.get("digiRole");
             if (digiRoleList != null && digiRoleList.isArray()) {
                 for (JsonNode role : (ArrayNode) digiRoleList) {
                     UserRole userRole = new UserRole();
                     userRole.setId(role.get("id").asText());
                     userRole.setName(role.get("name").asText());
                     userRoles.add(userRole);
                 }
             }
         }catch(Exception e){
             log.debug("Exception occured during saving user role");
         }
         this.userStore.getUserInfo().setUserRole(userRoles);
 
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
 
     private void forbidResponse(ServletResponse servletResponse) {
         HttpServletResponse response = (HttpServletResponse) servletResponse;
         response.reset();
         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
     }
 
     private void injectSpringDependecies(ServletRequest servletRequest) {
         ServletContext servletContext = servletRequest.getServletContext();
         WebApplicationContext webApplicationContext = WebApplicationContextUtils
                 .getWebApplicationContext(servletContext);
         userStore = webApplicationContext.getBean(UserStore.class);
     }
 }
 