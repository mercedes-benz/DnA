package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.fabric.UiLicioueStepsDto;
import com.daimler.data.dto.fabricCatalogManagement.GroupStatusResponseVO;
import com.daimler.data.util.ConstantsUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.Json;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UiLiciousClient {

    private final RestTemplate restTemplate;

    private static final int POLL_MAX_ATTEMPTS = 30;
    private static final long POLL_INTERVAL_MS = 2000L;

    private final ObjectMapper objectMapper;

    @Value("${uilicious.email}")
    private String email;

    @Value("${uilicious.password}")
    private String password;

    @Value("${uilicious.accessKey}")
    private String accessKey;

    @Value("${uilicious.browser:firefox}")
    private String browser;

    @Value("${uilicious.width:1280}")
    private String width;

    @Value("${uilicious.height:800}")
    private String height;

    @Value("${uilicious.filePath}")
    private String filePath;

    @Value("${uilicious.projectID}")
    private String projectID;

    @Value("${uilicious.baseURL}")
    private String baseURL;

    @Value("${uilicious.userAgent}")
    private String userAgent;

    @Value("${uilicious.servicePrincipalName}")
    private String defaultServicePrincipalName;

    @Value("${uilicious.contentType}")
    private String contentType;

    @Autowired
    public UiLiciousClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String addWorkspaceGroupsToLakehouse(String workspaceId, String lakehouseId, String workspaceName, String lakehouseName, List<String> groupName) {
 
        Map<String, Object> requestBody = new HashMap<>();
 
        StringBuilder groupNamesStringBuilder = new StringBuilder();
        groupNamesStringBuilder.append("[");
        for(String group : groupName){
            groupNamesStringBuilder.append("\"").append(group).append("\"").append(",");
        }
        groupNamesStringBuilder.setLength(groupNamesStringBuilder.length() - 1); // Remove the trailing comma
        groupNamesStringBuilder.append("]");
 
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("email", email);
        dataMap.put("password", password);
        dataMap.put("WorkspaceName", workspaceName);
        dataMap.put("WorkspaceID", workspaceId);
        dataMap.put("Lakehouse", lakehouseName);
        dataMap.put("LakehouseID", lakehouseId);
        dataMap.put("Groups", groupName);
 
        String data;
        try {
            data = objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            log.error("Error serializing data to JSON: {}", e.getMessage());
            return null;
        }
 
        requestBody.put("projectID", projectID);
        requestBody.put("filePath", filePath);
        requestBody.put("data", data);
        requestBody.put("browser", browser);
        requestBody.put("width", width);
        requestBody.put("height", height);
 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accessKey", accessKey);
        headers.set("User-Agent", userAgent);
        headers.set("Content-Type", contentType);
       
        // licious API call to trigger the test suite which adds the workspace groups to lakehouse and returns the response containing the details of the triggered test run
        JsonNode response = callUiLiciousApi(baseURL + "start", JsonNode.class, requestBody, HttpMethod.POST, headers);
 
        JsonNode testRunIDsJson = response.get("result").get("testRunIDs");
 
        List<String> testRunIDsList = objectMapper.convertValue(
            testRunIDsJson,
            new TypeReference<List<String>>() {}
        );
 
        if(testRunIDsList.isEmpty()){
            log.warn("No test run ID returned from UiLicious API for workspaceId: {} and lakehouseId: {}", workspaceId, lakehouseId);
            return null;
        }
 
        return testRunIDsList.get(0);
    }
 
    public List<GroupStatusResponseVO> getStatusOfGroupsAdditionToLakehouse(String workspaceName, String workspaceId, String lakehouseName, String lakehouseId, List<String> groupName, String testRunID) {
 
        Map<String, Object> requestBody = new HashMap<>();
       
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accessKey", accessKey);
        headers.set("User-Agent", userAgent);
        headers.set("Content-Type", contentType);
       
        JsonNode response = null;
        requestBody.put("id", testRunID);
        response = callUiLiciousApi(baseURL + "get", JsonNode.class, requestBody, HttpMethod.POST, headers); // Adjust the response type as needed
 
        String testRunLakehouseId = response.get("result").get("data").get("LakehouseID").asText();
        JsonNode groupsNode = response.get("result").get("data").get("Groups");
        List<String> groups = objectMapper.convertValue(groupsNode, new TypeReference<List<String>>() {});
       
        JsonNode stepsNode = response.get("result").get("result").get("steps");
        // fetching out the steps node from the API response
 
        List<UiLicioueStepsDto> steps = objectMapper.convertValue(
            stepsNode,
            new TypeReference<List<UiLicioueStepsDto>>() {}
        );
        List<UiLicioueStepsDto> groupsStatusStep = steps
            .stream()
            .filter(step -> step != null && step.getDescription() != null && step.getDescription().contains((ConstantsUtility.UILICIOUS_GROUP_STATUS_CONSTANT)) &&
                       step.getDescription().contains(ConstantsUtility.UILICIOUS_GROUP_CONSTANT))
            .toList();
        if(groupsStatusStep.isEmpty()){
            log.warn("No group status found in the API response for lakehouseId: {}", lakehouseId);
            return new ArrayList<>();
        }
        List<Map<String, String>> responseGroupsStatusList = new ArrayList<>();
        try {
            responseGroupsStatusList = objectMapper.readValue(
                groupsStatusStep.get(0).getDescription(),
                new TypeReference<List<Map<String, String>>>() {}
            );  
        } catch (Exception e) {
            log.error("Error parsing group status from API response for lakehouseId: {}: {}", lakehouseId, e.getMessage());
            return Collections.emptyList();
        }
 
        List<GroupStatusResponseVO> groupStatusList = new ArrayList<>();
 
        for(Map<String, String> groupStatus : responseGroupsStatusList){
            GroupStatusResponseVO groupStatusResponseVO = new GroupStatusResponseVO();
            groupStatusResponseVO.setGroupName(groupStatus.get(ConstantsUtility.UILICIOUS_GROUP_CONSTANT));
            groupStatusResponseVO.setStatus((groupStatus.get(ConstantsUtility.UILICIOUS_GROUP_STATUS_CONSTANT)));
            groupStatusResponseVO.setMessage(ConstantsUtility
                .GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(groupStatus.get(ConstantsUtility.UILICIOUS_GROUP_STATUS_CONSTANT)));
            groupStatusList.add(groupStatusResponseVO);
        }
 
        return groupStatusList;
    }

    public String addServicePrincipalToLakehouse(String workspaceId, String lakehouseId,
            String workspaceName, String lakehouseName, String servicePrincipalName) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("email", email);
        dataMap.put("password", password);
        dataMap.put("WorkspaceName", workspaceName);
        dataMap.put("WorkspaceID", workspaceId);
        dataMap.put("Lakehouse", lakehouseName);
        dataMap.put("LakehouseID", lakehouseId);

        String effectiveServicePrincipalName = (servicePrincipalName != null && !servicePrincipalName.trim().isEmpty())
                ? servicePrincipalName
                : this.defaultServicePrincipalName;
        dataMap.put("servicePrincipalName", effectiveServicePrincipalName);

        String data;
        try {
            data = objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            log.error("Error serializing data to JSON: {}", e.getMessage());
            return null;
        }

        log.info("UiLicious request for workspaceId: {}, lakehouseId: {}, servicePrincipalName: {}",
                workspaceId, lakehouseId, effectiveServicePrincipalName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("projectID", projectID);
        requestBody.put("filePath", filePath);
        requestBody.put("data", data);
        requestBody.put("browser", browser);
        requestBody.put("width", width);
        requestBody.put("height", height);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accessKey", accessKey);

        JsonNode response = callUiLiciousApi(baseURL + "start", JsonNode.class, requestBody, HttpMethod.POST, headers);
        System.out.println("response.toString() = " + response.toString());
        if (response == null || response.get("result") == null || response.get("result").get("testRunIDs") == null) {
            log.warn("UiLicious start response did not contain testRunIDs for workspaceId: {} and lakehouseId: {}",
                    workspaceId, lakehouseId);
            return null;
        }
        JsonNode testRunIDsJson = response.get("result").get("testRunIDs");
        List<String> testRunIDsList = objectMapper.convertValue(
                testRunIDsJson,
                new TypeReference<List<String>>() {
                });

        if (testRunIDsList.isEmpty()) {
            log.warn("No test run ID returned from UiLicious API for workspaceId: {} and lakehouseId: {}",
                    workspaceId, lakehouseId);
            return null;
        }

        return testRunIDsList.get(0);
    }

    public void triggerTestRun(String projectID, String filePath, String data, String browser,
            String width, String height, String accessKey) {
        if (projectID == null || projectID.trim().isEmpty()) {
            throw new IllegalArgumentException("projectID is required");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath is required");
        }
        if (data == null) {
            throw new IllegalArgumentException("data is required");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("projectID", projectID);
        requestBody.put("filePath", filePath);
        requestBody.put("data", data);
        requestBody.put("browser", browser != null ? browser : this.browser);
        requestBody.put("width", width != null ? width : this.width);
        requestBody.put("height", height != null ? height : this.height);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (accessKey != null && !accessKey.trim().isEmpty()) {
            headers.set("accessKey", accessKey);
        } else {
            headers.set("accessKey", this.accessKey);
        }

        try {
            callUiLiciousApi(baseURL + "start", JsonNode.class, requestBody, HttpMethod.POST, headers);
            log.info("UiLicious test run triggered successfully for projectID: {}, filePath: {}", projectID, filePath);
        } catch (Exception e) {
            log.error("Error triggering UiLicious test run for projectID {}, filePath {}: {}",
                    projectID, filePath, e.getMessage());
            throw new RuntimeException("Error triggering UiLicious test run", e);
        }
    }

    private JsonNode pollUntilFinalStatus(String testRunId, HttpHeaders headers) {
        Map<String, Object> statusRequest = new HashMap<>();
        statusRequest.put("id", testRunId);
        JsonNode lastResponse = null;

        for (int attempt = 1; attempt <= POLL_MAX_ATTEMPTS; attempt++) {
            lastResponse = callUiLiciousApi(baseURL + "get", JsonNode.class, statusRequest, HttpMethod.POST, headers);
            String status = extractRunStatus(lastResponse);
            log.info("UiLicious poll attempt {} for testRunId {} returned status {}", attempt, testRunId, status);

            if (isTerminalStatus(status)) {
                return lastResponse;
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("UiLicious polling interrupted for testRunId {}", testRunId);
            }
        }

        return lastResponse;
    }

    private String extractRunStatus(JsonNode response) {
        if (response == null) {
            return "";
        }
        String nestedStatus = response.path("result").path("result").path("status").asText("");
        if (nestedStatus != null && !nestedStatus.isEmpty()) {
            return nestedStatus.toLowerCase();
        }
        String topLevelStatus = response.path("result").path("status").asText("");
        return topLevelStatus == null ? "" : topLevelStatus.toLowerCase();
    }

    private boolean isTerminalStatus(String status) {
        if (status == null) {
            return false;
        }
        return "success".equals(status) || "failed".equals(status) || "error".equals(status)
                || "aborted".equals(status) || "stopped".equals(status) || "cancelled".equals(status);
    }

    private <T> T callUiLiciousApi(String url, Class<T> responseType, Map<String, Object> requestBody,
            HttpMethod method, HttpHeaders headers) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    responseType);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Received non-OK response from UiLicious API: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error calling UiLicious API at {}: {}", url, e.getMessage());
            throw new RuntimeException("Error calling UiLicious API", e);
        }
    }

}
