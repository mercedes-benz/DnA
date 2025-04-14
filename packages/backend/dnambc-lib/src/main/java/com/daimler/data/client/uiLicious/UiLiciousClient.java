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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
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

    public UiliciousStartCreationResponseDTO startCreation (String orgName, String projectName, List<MemberInfoVO> memberDetails){
        UiliciousStartCreationResponseDTO response = new UiliciousStartCreationResponseDTO();

         String httpResponseBody = "";

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
                    httpResponseBody = httpResponse.getBody();
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
            log.error("Exception occured while calling uilicious for create subscription with message {} and body {}",e.getMessage(),httpResponseBody);
        }catch(Exception e){
            response.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setRunId(null);
            log.error("Exception occured while calling uilicious for create subscription with message {} and body {}",e.getMessage(),httpResponseBody);
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
}
