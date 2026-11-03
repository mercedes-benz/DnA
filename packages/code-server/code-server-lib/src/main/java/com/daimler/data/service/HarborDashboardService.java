package com.daimler.data.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.HarborDashboardResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HarborDashboardService {

    @Value("${harbor.dashboard.api.url}")
    private String apiUrl;

    @Value("${harbor.dashboard.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    public HarborDashboardResponse fetchDashboardData(String projectName, List<String> userEmails, List<String> workspaceIds) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            if (projectName != null && !projectName.trim().isEmpty()) {
                requestBody.put("project_name", projectName);
            }
            if (userEmails != null && !userEmails.isEmpty()) {
                requestBody.put("user_emails", userEmails);
            }
            if (workspaceIds != null && !workspaceIds.isEmpty()) {
                requestBody.put("workspace_ids", workspaceIds);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.set("api-key", apiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling Harbor dashboard API: {} with payload {}", apiUrl, requestBody);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.getBody(), HarborDashboardResponse.class);
            }

            log.warn("Harbor dashboard API call failed with status: {} and body: {}",
                    response.getStatusCode(), response.getBody());
            return null;

        } catch (Exception e) {
            log.error("Exception while calling Harbor dashboard API: {}", e.getMessage(), e);
            return null;
        }
    }
}
