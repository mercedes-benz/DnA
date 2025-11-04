package com.daimler.data.client.uiLicious;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.client.uiLicious.UiliciousStartCreationResponseDTO;
import com.daimler.data.dto.promptCraftSubscriptions.MemberInfoVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UsersCollection;
import com.daimler.data.application.auth.UserStore;
//import com.daimler.data.util.JWTGenerator;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.uilicious.CreateUiliciousWorkspaceRequestVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.*;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UiLiciousClient {
    

    @Autowired
    private UserStore userStore;

    @Autowired
    RestTemplate restTemplate;

    @Value("${promptsraftsubscriptions.uiLicious.startCreationUri}")
    private String startCreationUri;

    @Value("${promptsraftsubscriptions.uiLicious.getRunDetailsUri}")
    private String getRunDetailsUri;

    @Value("${promptsraftsubscriptions.uiLicious.pidUser}")
    private String pidUser;

    @Value("${promptsraftsubscriptions.uiLicious.pidPassword}")
    private String pidPassword;

    @Value("${promptsraftsubscriptions.uiLicious.accessKey}")
    private String accessKey;

    @Value("${promptsraftsubscriptions.uiLicious.browser}")
    private String browser;

    @Value("${promptsraftsubscriptions.uiLicious.width}")
    private String width;

    @Value("${promptsraftsubscriptions.uiLicious.height}")
    private String height;

    @Value("${promptsraftsubscriptions.uiLicious.filePath}")
    private String filePath;

    @Value("${promptsraftsubscriptions.uiLicious.projectID}")
    private String projectID;

    @Value("${promptsraftsubscriptions.uiLicious.oauthKey}")
    private String oauthKey;

    @Value("${promptsraftsubscriptions.uiLicious.baseURL}")
    private String baseURL;

   
    private final ObjectMapper objectMapper;

    public UiLiciousClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public UiliciousStartCreationResponseDTO startCreation (String orgName, String projectName, List<MemberInfoVO> memberDetails){
        UiliciousStartCreationResponseDTO response = new UiliciousStartCreationResponseDTO();

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("AccessKey",accessKey );

            UiLiciousStartCreationRequestBody requestBody = new UiLiciousStartCreationRequestBody();
            UiLiciousStartCreationBodyDataDTO bodyData = new UiLiciousStartCreationBodyDataDTO();

            MemberInfoVO memberDetail = memberDetails.get(0);

            bodyData.setUserid(pidUser);
            bodyData.setPassword(pidPassword);
            bodyData.setOrgName(orgName);
            bodyData.setProjectName(projectName);
            bodyData.setMemberMailId(memberDetail.getEmail());
            bodyData.setOath_Key(oauthKey);

            requestBody.setData(bodyData);
            requestBody.setBrowser(browser);
            requestBody.setWidth(width);
            requestBody.setHeight(height);
            requestBody.setFilePath(filePath);
            requestBody.setProjectID(projectID);

            HttpEntity<UiLiciousStartCreationRequestBody> entity = new HttpEntity<UiLiciousStartCreationRequestBody>(requestBody, headers);
            ResponseEntity<String> httpResponse = restTemplate.exchange(startCreationUri, HttpMethod.POST, entity, String.class);
            if (httpResponse != null && httpResponse.getStatusCode() != null) {
                if (httpResponse.getStatusCode().equals(HttpStatus.OK)) {
                    String runId = null;
                    String httpResponseBody = httpResponse.getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonResponse = objectMapper.readTree(httpResponseBody);

                    if (jsonResponse != null) {
                        JsonNode resultNode = jsonResponse.path("result");
                        JsonNode testRunIDsNode = resultNode.path("testRunIDs");
                        if (testRunIDsNode.isArray() && testRunIDsNode.size() > 0) {
                            runId = testRunIDsNode.get(0).asText();
                            response.setResponseStatus(httpResponse.getStatusCode());
                            response.setRunId(runId);
                            log.info("called uilicious for create subscription and successfully got the run id");
                        }
                    }
                }else{
                    response.setResponseStatus(httpResponse.getStatusCode());
                    response.setRunId(null);
                    log.info(" failed while calling uilicious for create subscription with status {} and body {}",httpResponse.getStatusCode(),httpResponse.getBody());
                }
            }
        }catch( JsonProcessingException e){
            response.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setRunId(null);
            response.setErrorMessage(e.getMessage());
            log.error(" Exception occured while calling uilicious for create subscription with message{}",e.getMessage());
        }catch(Exception e){
            response.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setRunId(null);
            response.setErrorMessage(e.getMessage());
            log.error(" Exception occured while calling uilicious for create subscription with message{}",e.getMessage());
        }

        return response;
    }

    public JsonNode getSubscriptionRunDetails(String runId){
        JsonNode response = null;
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("AccessKey",accessKey );

            HttpEntity<String> entity = new HttpEntity<String>(headers);
            ResponseEntity<String> httpResponse = restTemplate.exchange(getRunDetailsUri+"?id="+runId, HttpMethod.GET, entity, String.class);
            if (httpResponse != null && httpResponse.getStatusCode() != null) {
                if (httpResponse.getStatusCode().equals(HttpStatus.OK)) {
                    String httpResponseBody = httpResponse.getBody();
                    ObjectMapper objectMapper = new ObjectMapper();
                    response = objectMapper.readTree(httpResponseBody);
                    log.info("called uilicious for get subscription run details and successfully got the response");
                    return response;
                }else{
                    log.info(" failed while calling uilicious for get subscription run details with status{}",httpResponse.getStatusCode());
                    return response;
                }
            }
        }catch( JsonProcessingException e){
            log.error(" Exception occured while calling uilicious for get subscription run details with message{}",e.getMessage());
        }catch(Exception e){
            log.error(" Exception occured while calling uilicious for get subscription run details with message{}",e.getMessage());
        }
        return response;
    }

    public String getUserAccountId(String loginName, int start, int length) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("loginName", loginName);
            requestBody.put("start", start);
            requestBody.put("length", length);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accessKey", accessKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            // Make API call

            String url = baseURL + "/api/v3.0/admin/account/list";
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseBody = response.getBody();
                JsonNode resultArray = responseBody.path("result");

                if (resultArray.isArray() && resultArray.size() > 0) {
                    JsonNode firstUser = resultArray.get(0);
                    String accountId = firstUser.path("_oid").asText();

                    log.info("Successfully retrieved account ID: {} for loginName: {}", accountId, loginName);
                    return accountId;
                } else {
                    log.warn("No user account found for loginName: {}", loginName);
                    return null;
                }
            } else {
                log.warn("Received non-OK response from Uilicious user account API: {}", response.getStatusCode());
                return null;
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Uilicious server is not reachable or connection timeout for loginName: {}, error: {}", loginName,
                    e.getMessage());
            throw new RuntimeException("Uilicious server is unavailable", e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Uilicious server returned error response for loginName: {}, status: {}, error: {}", loginName,
                    e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Something went wrong with Uilicious server", e);
        } catch (Exception e) {
            log.error("Error occurred while calling Uilicious user account API for loginName: {}, error: {}", loginName,
                    e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with Uilicious server", e);
        }
    }

    /**
     * Get workspaces by account ID (Second API call)
     */
    public List<UiliciousWorkspaceVO> getWorkspacesByAccountId(String accountId) {
        List<UiliciousWorkspaceVO> workspaces = new ArrayList<>();

        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("accessKey", accessKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Make API call

            String url = baseURL + "/api/v3.0/admin/account/" + accountId + "/space/list";
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    JsonNode.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseBody = response.getBody();
                JsonNode resultArray = responseBody.path("result");

                if (resultArray.isArray()) {
                    for (JsonNode workspaceNode : resultArray) {
                        UiliciousWorkspaceVO workspace = mapToWorkspaceVO(workspaceNode, accountId);
                        if (workspace != null) {
                            workspaces.add(workspace);
                        }
                    }
                }

                log.info("Successfully retrieved {} workspaces for account ID: {}", workspaces.size(), accountId);
            } else {
                log.warn("Received non-OK response from Uilicious workspace API: {}", response.getStatusCode());
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Uilicious server is not reachable or connection timeout for account ID: {}, error: {}", accountId,
                    e.getMessage());
            throw new RuntimeException("Uilicious server is unavailable", e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Uilicious server returned error response for account ID: {}, status: {}, error: {}", accountId,
                    e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Something went wrong with Uilicious server", e);
        } catch (Exception e) {
            log.error("Error occurred while calling Uilicious workspace API for account ID: {}, error: {}", accountId,
                    e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with Uilicious server", e);
        }

        return workspaces;
    }

    /**
     * Main method to get workspaces by login name (combines both API calls)
     */
    public List<UiliciousWorkspaceVO> getWorkspaces(String loginName, int start, int length) {
        log.info("Starting to fetch workspaces for loginName: {}", loginName);

        // Step 1: Get account ID by email
        String accountId = getUserAccountId(loginName, start, length);
        log.info("Account id: " + accountId);

        if (accountId == null || accountId.trim().isEmpty()) {
            log.warn("Could not retrieve account ID for loginName: {}", loginName);
            return new ArrayList<>();
        }

        // Step 2: Get workspaces by account ID
        List<UiliciousWorkspaceVO> workspaces = getWorkspacesByAccountId(accountId);

        log.info("Completed fetching {} workspaces for loginName: {}", workspaces.size(), loginName);
        return workspaces;

    }

    private UiliciousWorkspaceVO mapToWorkspaceVO(JsonNode workspaceNode, String accountId) {
        try {
            UiliciousWorkspaceVO workspace = new UiliciousWorkspaceVO();

            // Map fields based on the API response structure

            workspace.setSpaceName(workspaceNode.path("name").asText());
            workspace.setSpaceId(workspaceNode.path("_oid").asText());
            workspace.setUserRole(workspaceNode.path("userRole").asText());
            return workspace;
        } catch (Exception e) {
            log.error("Error mapping workspace node to VO: {}", e.getMessage());
            return null;
        }
    }

    public String createUiliciousWorkspace(String email, String userID, String loginName) {
        try{
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", userID);
            requestBody.put("name", loginName);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("AccessKey",accessKey );
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            String url = baseURL + "/api/v3.0/admin/account/new";

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseBody = response.getBody();
                JsonNode resultNode = responseBody.path("result");
                String accountId = resultNode.path("_oid").asText();
                log.info("Successfully created workspace with account ID: {}", accountId);
                return accountId;
            } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.warn("this user id already have existing uilicious workspace {}", loginName);
                return "FAILURE";
            } else {
                log.warn("Received non-OK response from Uilicious create workspace API: {}", response.getStatusCode());
                return null;
            }
        }catch(Exception e){
            log.error(" Exception occured while calling uilicious for create workspace with message{}",e.getMessage());
            return null;
        }     
    }
      
}
