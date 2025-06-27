package com.daimler.data.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ArgoCdService {

    @Value("${argocd.argocdTokenUrl}")
    private String argocdTokenUrl;

    @Value("${argocd.argocdCreateUrl}")
    private String argocdCreateUrl;

    @Value("${argocd.createProjectName}")
    private String argocdCreateProjectName;

    @Value("${argocd.tokenUserName}")
    private String tokenUserName;

    @Value("${argocd.tokenPassword}")
    private String tokenPassword;

    @Value("${argocd.createTargetRevision}")
    private String targetRevision;

    @Value("${argocd.createMemoryRequest}")
    private String memoryRequest;

    @Value("${argocd.createCpuRequest}")
    private String cpuRequest;

    @Value("${argocd.createMaxconnections}")
    private String maxconnections;

    @Value("${argocd.createReplicaCount}")
    private String replicaCount;

    @Value("${argocd.createNameSpace}")
    private String nameSpace;

    @Value("${argocd.createPDBminAvailable}")
    private String pdbMinAvailable;

    @Value("${argocd.createEnvironment}")
    private String environment;

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

    public String createArgoApp(String token,String serviceName,String dbName,String dbType) {
        try {
            String url = argocdCreateUrl;
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            String payload = this.buildPayload(serviceName,dbName,dbType);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("Application created successfully!");
                return "success";
            } else {
                log.info("Failed: " + response != null?response.getBody():"");
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

    @SuppressWarnings("unchecked")
    public  String buildPayload(String serviceName,String dbName,String dbType) throws IOException {
        String fileName = "argocd-"+dbType+"-create-template.json";
    ClassPathResource resource = new ClassPathResource(fileName);
    String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    template = template.replace("{serviceName}", serviceName);
    template = template.replace("{dbName}", dbName);
    template = template.replace("{dbname}-db-backup", dbName+"-db-backup");
    template = template.replace("{projectName}", argocdCreateProjectName);
    template = template.replace("{targetRevision}", targetRevision);
    template = template.replace("{cpuRequest}", cpuRequest);
    template = template.replace("{memoryRequest}", memoryRequest);
    template = template.replace("{maxconnections}", maxconnections);
    template = template.replace("{replicaCount}", replicaCount);
    template = template.replace("{nameSpace}", nameSpace);
    template = template.replace("{pdbMinAvailable}", pdbMinAvailable);
    template = template.replace("{environment}", environment);


    ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(template, Map.class);
        String finalJson = mapper.writeValueAsString(map); 
        return finalJson;
}
}
