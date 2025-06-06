package com.daimler.data.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.db.json.HelmValues;
import com.daimler.data.db.json.HelmValues.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ArgoCdService {

    @Value("${argocd.argocdTokenUrl}")
    private String argocdTokenUrl;

    @Value("${argocd.argocdCreateUrl}")
    private String argocdCreateUrl;

    @Value("${argocd.argocdCreateRequest}")
    private String argocdCreateRequest;

    @Value("${argocd.tokenUserName}")
    private String tokenUserName;

    @Value("${argocd.tokenPassword}")
    private String tokenPassword;

    @Autowired
    private RestTemplate restTemplate;

    public String getArgoToken() {
        try {
            String url = argocdTokenUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> request = Map.of("username", tokenUserName, "password", tokenPassword);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
    
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return (String) response.getBody().get("token");
        } catch (Exception e) {
            log.error("exception {}",e.getMessage());            
			return null;
        }
    }

    public String createArgoApp(String token,String serviceName,String dbName) {
        try {
            String url = argocdCreateUrl;
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            Map<String, Object> payload = this.buildPayload(serviceName,dbName);
            log.info("payload {}",payload.toString());
        
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Application created successfully!");
                return "success";
            } else {
                log.info("Failed: " + response.getBody());
                return "failed";
            }
        } catch (Exception e) {
            log.error("exception {}",e.getMessage());            
			return "failed";
        }
    }

    public String deleteArgoApp(String token,String serviceName) {
        try {
            String url = argocdCreateUrl+"/"+serviceName;
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
        
            HttpEntity<Object> entity = new HttpEntity<>(headers);
        
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Application deleted successfully!");
                return "success";
            } else {
                log.info("Failed: " + response.getBody());
                return "failed";
            }
        } catch (Exception e) {
            log.error("exception {}",e.getMessage());            
			return "failed";
        }
    }

    public Map<String, Object> buildPayload(String serviceName,String dbName) throws JsonMappingException, JsonProcessingException {

        String filledJson = argocdCreateRequest
        .replace("{serviceName}", serviceName)
        .replace("{dbname}", dbName);
       

// Convert JSON string to Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(filledJson, Map.class);
        map.forEach((key, value) -> System.out.println(key + " : " + value));
        return map;
    }


}
