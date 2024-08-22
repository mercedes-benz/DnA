package com.daimler.data.application.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VaultClient {

    private static Logger LOGGER = LoggerFactory.getLogger(VaultClient.class);

    @Value("${codeServer.vault.baseuri}")
	private String vaultBaseUri;

    @Autowired
	RestTemplate restTemplate;


    public Boolean isValutInjectorEnable(String projectName, String environment) throws Exception{
        Boolean responseBoolean = false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            String url = vaultBaseUri+"/" + projectName + "/"+ environment;
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity,String.class);
            if (response != null && response.getStatusCode()!=null && response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> secMap = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, String>>() {});
                if(!secMap.isEmpty()){
                    responseBoolean = true;
                }
                LOGGER.info("Sucessfully fetched the secrets from vault, value = {} for projectname {} and evironment {}", responseBoolean,projectName,environment);
            }else{
                throw new Exception(" Failed at vault with the status: "+response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.error("Error occured while fetching secrets for project {} and environment {} with exception {} ", projectName,environment, e.getMessage());
            throw e;
        }
        return responseBoolean;
        }
	
}
