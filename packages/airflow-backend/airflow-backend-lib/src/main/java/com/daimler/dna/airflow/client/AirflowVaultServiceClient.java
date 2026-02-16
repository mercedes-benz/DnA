package com.daimler.dna.airflow.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AirflowVaultServiceClient {

    @Value("${vaultIntegration.service.baseUrl}")
    private String baseUrl;

    @Value("${vaultIntegration.service.apiKey}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> getSecret(String dagName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));

            String url = baseUrl + "/airflow/secret/" + dagName;
            log.info("GET Vault Secret URL: {}", url);

            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        } catch (Exception e) {
            log.error("Error while GET secret: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching secret: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updateSecret(String dagName, String secretJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = baseUrl + "/airflow/secret/" + dagName;
            log.info("PUT Vault Secret URL: {}", url);

            HttpEntity<String> entity = new HttpEntity<>(secretJson, headers);

            return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        } catch (Exception e) {
            log.error("Error while PUT secret: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating secret: " + e.getMessage());
        }
    }
}
