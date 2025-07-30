package com.daimler.data.application.client;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class DnAAliceAuthDataServiceClient {

    @Value("${authoriser.dnA.authService.uri}")
    private String authUrl;

    @Value("${authoriser.dnA.authService.secret}")
    private String secret;

    @Autowired
    private RestTemplate restTemplate;

    public List<String> getAllUserEntitlements(String userId) {
        List<String> response = new ArrayList<>();
        String url = authUrl + "/user/" + userId + "/entitlements";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", secret);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

           if (apiResponse.getStatusCode().is2xxSuccessful()) {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				JsonNode jsonData = objectMapper.readTree(apiResponse.getBody());
                JsonNode entitlements = jsonData.get("entitlements");
				if (entitlements.isArray()) {
					for (JsonNode entitlementNode : entitlements) {
						String id = entitlementNode.get("id").asText();
						response.add(id);
					}
				}
			}
            return response;
        } catch (Exception e) {
            log.error("Error fetching user entitlements for userId {}: {}", userId, e.getMessage());
            return response;
        }
        
    }

}
