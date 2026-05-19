package com.daimler.data.application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class VaultAuthorizationServiceClient {

    @Value("${vaultIntegration.service.baseUrl}")
    private String baseUrl;

    @Value("${vaultIntegration.service.apiKey}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> getSecret(String codeSpaceName, String env) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/secret/" + codeSpaceName + "/" + env)
                    .toUriString();

            
            ResponseEntity<String> response =
             restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
             
             return response;

        } catch (Exception e) {
            log.error("Error calling Vault service getSecret: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error fetching secret: " + e.getMessage());
        }
    }

    public ResponseEntity<String> createSecret(String path, String secretJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            String url = baseUrl + "/secret/" + path;
            HttpEntity<String> entity = new HttpEntity<>(secretJson, headers);

            log.info("Creating secret at path {}", path);
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            log.error("Error creating secret: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error creating secret: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updateSecret(String path, String env, String secretJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            String url = baseUrl + "/secret/" + path + "/" + env;
            HttpEntity<String> entity = new HttpEntity<>(secretJson, headers);

            log.info("Updating secret at {}", url);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        } catch (Exception e) {
            log.error("Error updating secret: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating secret: " + e.getMessage());
        }
    }

    public ResponseEntity<String> deleteSecret(String path, String secretName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            String url = baseUrl + "/secret/" + path + "/" + secretName;
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Deleting secret at {}", url);
            return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        } catch (Exception e) {
            log.error("Error deleting secret: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting secret: " + e.getMessage());
        }
    }
}
