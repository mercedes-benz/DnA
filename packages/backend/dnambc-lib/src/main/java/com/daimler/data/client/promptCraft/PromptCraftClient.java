package com.daimler.data.client.promptCraft;


import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.daimler.data.client.promptCraft.PromptCraftRegisterUserBodyDataDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PromptCraftClient {
    
    @Autowired
    RestTemplate restTemplate;

    @Value("${promptsraftsubscriptions.promotCraftBaseUrl}")
    private String promptCraftBaseUrl;

    @Value("${promptsraftsubscriptions.xApiKey}")
    private String xApiKeyString;

    public JsonNode promptCraftRegisterUser( String publicKey, String privateKey){
        JsonNode jsonNode = null;
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("x-api-key",xApiKeyString);
            headers.set("promptcraft-tenant-key",publicKey);
            headers.set("promptcraft-secret-key",privateKey);

            HttpEntity<String> entity = new HttpEntity<String>(headers);
            ResponseEntity<String> httpResponse = restTemplate.exchange(promptCraftBaseUrl+"/register_user", HttpMethod.GET, entity, String.class);
            if (httpResponse != null && httpResponse.getStatusCode() != null) {
                if (httpResponse.getStatusCode().equals(HttpStatus.OK)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    jsonNode = objectMapper.readTree( httpResponse.getBody());
                    log.info("Successfully called prompt craft register user API");
                    return jsonNode;
                }else{
                    log.error("Called prompt craft register user API, Failed with status {}", httpResponse.getStatusCode());
                    return jsonNode;
                }
            } 
            
        }catch(Exception e){
            log.error("error occured while calling prompt craft register user API {}",e.getMessage());
        }
        return jsonNode;
    }
}
