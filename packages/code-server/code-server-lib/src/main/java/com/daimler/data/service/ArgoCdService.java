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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ArgoCdService {

    @Value("${argocd.argocdTokenUrl}")
    private String argocdTokenUrl;

    @Value("${argocd.argocdCreateUrl}")
    private String argocdCreateUrl;

    @Value("${argocd.tokenUserName}")
    private String tokenUserName;

    @Value("${argocd.tokenPassword}")
    private String tokenPassword;

    @Value("${argocd.imageRegistry}")
    private String imageRegistry;

    @Value("${argocd.vaultKvPath}")
    private String vaultKvPath;

    @Value("${codeServer.env.ref}")
    private String codeServerEnvRef;

    @Autowired
    private RestTemplate restTemplate;

    public String getArgoToken() throws Exception {
        String url = argocdTokenUrl;
        log.info("Attempting to get ArgoCD token from: {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> request = Map.of("username", tokenUserName, "password", tokenPassword);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            log.info("Successfully obtained ArgoCD token");
            return (String) body.get("token");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = "ArgoCD authentication failed (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
            log.error("Failed to get ArgoCD token from URL: {}. Error: {}", url, errorMsg);
            throw new Exception(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Failed to connect to ArgoCD server at " + url + ": " + e.getMessage();
            log.error("ArgoCD connection error: {}", errorMsg, e);
            throw new Exception(errorMsg);
        }
    }

    public String createArgoApp(String token, String projectName, String userId, String environment,
                                String gitRepoUrl, String imageTag, boolean vaultInjectorEnable) throws Exception {
        try {
            log.info("createArgoApp - projectName: {}, gitRepoUrl: {}, imageTag: {}, environment: {}", 
                     projectName, gitRepoUrl, imageTag, environment);
            
            String appName = projectName + "-" + environment;
            String url = argocdCreateUrl + "?upsert=true";
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            String payload = this.buildPayload(appName, projectName, codeServerEnvRef, environment, gitRepoUrl, imageTag, vaultInjectorEnable);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("ArgoCD application created/updated successfully: {}", appName);
                return "success";
            } else {
                String errorMsg = "ArgoCD application creation failed: " + (response != null ? response.getBody() : "No response");
                log.error(errorMsg);
                throw new Exception(errorMsg);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = "ArgoCD API error (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
            log.error("Failed to create ArgoCD application: {}", errorMsg, e);
            throw new Exception(errorMsg);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ArgoCD")) {
                throw e; // Re-throw our custom exceptions
            }
            String errorMsg = "Failed to create ArgoCD application: " + e.getMessage();
            log.error(errorMsg, e);
            throw new Exception(errorMsg);
        }
    }

    public String deleteArgoApp(String token, String workspaceName, String environment) {
        try {
            String appName = workspaceName + "-" + environment;
            String url = argocdCreateUrl + "/" + appName;
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            HttpEntity<Object> entity = new HttpEntity<>(headers);
        
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("ArgoCD application deleted successfully: {}", appName);
                return "success";
            } else {
                log.info("Failed: " + response.getBody());
                return "failed";
            }
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.info("ArgoCD application not found (already deleted): {}", workspaceName + "-" + environment);
            return "not_found";
        } catch (Exception e) {
            log.error("Failed to delete ArgoCD application", e);            
            return "failed";
        }
    }

    public String restartArgoApp(String token, String workspaceName, String environment) {
        try {
            String appName = workspaceName + "-" + environment;
            String url = argocdCreateUrl + "/" + appName + "/resources/actions";
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            String payload = "{\"resourceName\":\"*\",\"kind\":\"Pod\",\"namespace\":\"*\",\"action\":\"restart\"}";
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("ArgoCD application restarted successfully: {}", appName);
                return "success";
            } else {
                log.info("Failed to restart: " + (response != null ? response.getBody() : ""));
                return "failed";
            }
        } catch (Exception e) {
            log.error("Failed to restart ArgoCD application", e);            
            return "failed";
        }
    }

    @SuppressWarnings("unchecked")
    public String buildPayload(String appName, String projectName, String clusterEnv, String targetEnv, String gitRepoUrl, 
                               String imageTag, boolean vaultInjectorEnable) throws IOException {
        String fileName = "argocd-workspace-create-template.json";
        ClassPathResource resource = new ClassPathResource(fileName);
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        
        String namespace = getNamespaceForEnvironment(clusterEnv, targetEnv);
        String vaultAuthPath = getVaultAuthPath(clusterEnv);
        String imageRepository = imageRegistry + "-" + projectName;
        
        String vaultStage = "dev".equals(clusterEnv) ? "staging" : "production";
        String vaultInjectorPath = vaultKvPath + "/" + vaultStage + "/" + projectName + "/" + targetEnv;
        String vaultInjectorRootPath = "/" + projectName + "/" + targetEnv + "/api";
        String vaultInjectorRootPathNonApi = "/" + projectName + "/" + targetEnv + "/";
        
        template = template.replace("{appName}", appName);
        template = template.replace("{env}", clusterEnv);
        template = template.replace("{repoURL}", gitRepoUrl);
        template = template.replace("{namespace}", namespace);
        template = template.replace("{imageRepository}", imageRepository);
        template = template.replace("{imageTag}", imageTag);
        template = template.replace("{vaultInjectorEnable}", String.valueOf(vaultInjectorEnable));
        template = template.replace("{vaultInjectorPath}", vaultInjectorPath);
        template = template.replace("{vaultInjectorRootPath}", vaultInjectorRootPath);
        template = template.replace("{vaultInjectorRootPathNonApi}", vaultInjectorRootPathNonApi);
        template = template.replace("{vaultInjectorAuthPath}", vaultAuthPath);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(template, Map.class);
        String finalJson = mapper.writeValueAsString(map); 
        return finalJson;
    }
    
    public String getArgoAppStatus(String token, String appName) {
        try {
            String url = argocdCreateUrl + "/" + appName;
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("ArgoCD app status retrieved successfully for: {}", appName);
                return response.getBody();
            } else {
                log.warn("Failed to get ArgoCD app status for {}: {}", appName, response.getStatusCode());
                return null;
            }
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.info("ArgoCD application not found: {}", appName);
            return null;
        } catch (Exception e) {
            log.error("Failed to get ArgoCD app status for {}: {}", appName, e.getMessage());            
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String checkArgoAppDeploymentStatus(String token, String appName) {
        try {
            String statusJson = getArgoAppStatus(token, appName);
            if (statusJson == null) {
                return "NOT_FOUND";
            }
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> appData = mapper.readValue(statusJson, Map.class);
            Map<String, Object> status = (Map<String, Object>) appData.get("status");
            
            if (status == null) {
                return "UNKNOWN";
            }
            
            Map<String, Object> health = (Map<String, Object>) status.get("health");
            Map<String, Object> sync = (Map<String, Object>) status.get("sync");
            
            String healthStatus = health != null ? (String) health.get("status") : "Unknown";
            String syncStatus = sync != null ? (String) sync.get("status") : "Unknown";
            
            log.info("ArgoCD app {} - Health: {}, Sync: {}", appName, healthStatus, syncStatus);
            
            if ("Healthy".equalsIgnoreCase(healthStatus) && "Synced".equalsIgnoreCase(syncStatus)) {
                return "DEPLOY_SUCCESS";
            } else if ("Progressing".equalsIgnoreCase(healthStatus) || "OutOfSync".equalsIgnoreCase(syncStatus)) {
                return "DEPLOYING";
            } else if ("Degraded".equalsIgnoreCase(healthStatus) || "Missing".equalsIgnoreCase(healthStatus)) {
                return "DEPLOY_FAILED";
            } else {
                return "DEPLOYING";
            }
            
        } catch (Exception e) {
            log.error("Failed to check ArgoCD deployment status for {}: {}", appName, e.getMessage());
            return "UNKNOWN";
        }
    }
    
    private String getNamespaceForEnvironment(String clusterEnv, String targetEnv) {
       
        if ("int".equalsIgnoreCase(targetEnv)) {
            return clusterEnv + "-dna-cs-apps-int";
        }
        return clusterEnv + "-dna-cs-apps";
    }
    
    private String getVaultAuthPath(String clusterEnv) {
        return "auth/k8_auth_dna_aws_" + clusterEnv;
    }
}
