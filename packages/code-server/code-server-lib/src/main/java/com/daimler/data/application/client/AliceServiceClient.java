package com.daimler.data.application.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.daimler.data.dto.EntitlementsDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AliceServiceClient {

    @Value("${alice.aliceBaseUrl}")
    private String baseUrl;

    @Value("${alice.aliceAuthToken}")
    private String authToken;

    @Autowired
    private RestTemplate restTemplate;

    public EntitlementsDto getEntitlements(String roleId) {
        EntitlementsDto vo = new EntitlementsDto();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("x-api-key", authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/roles" + roleId + "/entitlements").toUriString();

            ResponseEntity<EntitlementsDto> response = restTemplate.exchange(url, HttpMethod.GET,
                    requestEntity, EntitlementsDto.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                if (response != null && response.hasBody()) {
                    vo = response.getBody();
                } else {
                    log.warn("Empty response received for roleId: {}", roleId);
                }
            }

        } catch (Exception e) {
            log.error("Exception occurred while fetching entitlements: {}", e.getMessage());
        }

        return vo;
    }

}
