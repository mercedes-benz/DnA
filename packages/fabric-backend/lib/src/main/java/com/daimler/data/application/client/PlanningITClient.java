package com.daimler.data.application.client;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

import com.daimler.data.dto.planningit.PlanningITApiItemVO;
import com.daimler.data.dto.planningit.PlanningITApiResponseVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlanningITClient {

    @Value("${dataProduct.planningit.baseUrl}")
    private String planningItBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest httpRequest;

    public List<PlanningITApiItemVO> searchPlanningIT(String searchTerm) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String userinfo = httpRequest.getHeader("dna-request-userdetails");
            if (userinfo != null && !userinfo.isBlank()) {
                headers.set("dna-request-userdetails", userinfo);
            }
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromHttpUrl(planningItBaseUrl)
                    .queryParam("searchTerm", searchTerm)
                    .toUriString();

            ResponseEntity<PlanningITApiResponseVO> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, PlanningITApiResponseVO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()
                    && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch PlanningIT data for searchTerm {}: {}", searchTerm, e.getMessage());
        }
        return Collections.emptyList();
    }
}
