package com.daimler.data.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
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

    public String getArgocdBaseUrl() {
        if (argocdCreateUrl != null && argocdCreateUrl.contains("/api/")) {
            return argocdCreateUrl.substring(0, argocdCreateUrl.indexOf("/api/"));
        }
        return argocdCreateUrl;
    }

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
        
            Map<String, String> resources = calculateResources(gitRepoUrl);
            
            String payload = this.buildPayload(appName, projectName, codeServerEnvRef, environment, gitRepoUrl, imageTag, vaultInjectorEnable, resources);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("ArgoCD application created/updated successfully: {}", appName);
                return "success";
            } else {
                log.info("Failed: " + (response != null ? response.getBody() : ""));
                return "failed";
            }
        } catch (Exception e) {
            log.error("exception {}", e.getMessage());
            return "failed";
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
            String namespace = getNamespaceForEnvironment(environment, environment);
            
            String resourceName = appName;
            String url = argocdCreateUrl + "/" + appName + "/resource/actions" +
                        "?namespace=" + namespace +
                        "&resourceName=" + resourceName +
                        "&version=v1" +
                        "&kind=Deployment" +
                        "&group=apps";
            
            log.info("Restarting ArgoCD application: {} in namespace: {}", appName, namespace);
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            HttpEntity<String> entity = new HttpEntity<>("restart", headers);
        
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
                               String imageTag, boolean vaultInjectorEnable, Map<String, String> resources) throws IOException {
        
        String namespace = getNamespaceForEnvironment(clusterEnv, targetEnv);
        String vaultAuthPath = getVaultAuthPath(clusterEnv);
        String imageRepository = imageRegistry + "-" + projectName;
        
        String vaultStage = "dev".equals(clusterEnv) ? "staging" : "production";
        String vaultInjectorPath = vaultKvPath + "/" + vaultStage + "/" + projectName + "/" + targetEnv;
        String vaultInjectorRootPath = "/" + projectName + "/" + targetEnv + "/api";
        String vaultInjectorRootPathNonApi = "/" + projectName + "/" + targetEnv + "/";
        
        List<Map<String, String>> helmParameters = new ArrayList<>();
        
        helmParameters.add(createHelmParam("namespace", namespace));
        helmParameters.add(createHelmParam("fullnameOverride", appName));
        helmParameters.add(createHelmParam("image.repository", imageRepository));
        helmParameters.add(createHelmParam("image.tag", imageTag));
        helmParameters.add(createHelmParam("imagePullSecrets[0].name", "imagepullsecret"));
        
        helmParameters.add(createHelmParam("vaultInjector.enable", String.valueOf(vaultInjectorEnable)));
        helmParameters.add(createHelmParam("vaultInjector.path", vaultInjectorPath));
        helmParameters.add(createHelmParam("vaultInjector.root_path", vaultInjectorRootPath));
        helmParameters.add(createHelmParam("vaultInjector.root_path_non_api", vaultInjectorRootPathNonApi));
        helmParameters.add(createHelmParam("vaultInjector.authpath", vaultAuthPath));
        helmParameters.add(createHelmParam("vaultInjector.namespace", "/"));
        
        if (resources != null && !resources.isEmpty()) {
            String cpu = resources.get("cpu");
            String memory = resources.get("memory");
            if (cpu != null) {
                helmParameters.add(createHelmParam("resources.requests.cpu", cpu + "m"));
            }
            if (memory != null) {
                helmParameters.add(createHelmParam("resources.requests.memory", memory + "Mi"));
                helmParameters.add(createHelmParam("resources.limits.memory", memory + "Mi"));
            }
            // Explicitly remove limits.cpu by setting to "0" - Kubernetes treats 0 as no limit
            helmParameters.add(createHelmParam("resources.limits.cpu", "0"));
        }
        
        Map<String, Object> payload = new HashMap<>();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", appName);
        metadata.put("namespace", "argocd");
        Map<String, String> labels = new HashMap<>();
        labels.put("env", clusterEnv);
        labels.put("project", "cs-apps");
        metadata.put("labels", labels);
        payload.put("metadata", metadata);
        
        Map<String, Object> spec = new HashMap<>();
        spec.put("project", "cs-apps");
        
        Map<String, Object> source = new HashMap<>();
        source.put("repoURL", gitRepoUrl);
        source.put("path", "deploy/helm");
        source.put("targetRevision", "main");
        
        Map<String, Object> helm = new HashMap<>();
        helm.put("parameters", helmParameters);
        source.put("helm", helm);
        
        spec.put("source", source);
        
        Map<String, Object> destination = new HashMap<>();
        destination.put("server", "https://kubernetes.default.svc");
        destination.put("namespace", namespace);
        spec.put("destination", destination);
        
        Map<String, Object> syncPolicy = new HashMap<>();
        Map<String, Boolean> automated = new HashMap<>();
        automated.put("prune", true);
        automated.put("selfHeal", true);
        syncPolicy.put("automated", automated);
        
        List<String> syncOptions = new ArrayList<>();
        syncOptions.add("ApplyOutOfSyncOnly=true");
        syncOptions.add("CreateNamespace=true");
        syncPolicy.put("syncOptions", syncOptions);
        
        spec.put("syncPolicy", syncPolicy);
        payload.put("spec", spec);
        
        ObjectMapper mapper = new ObjectMapper();
        String finalJson = mapper.writeValueAsString(payload); 
        log.info("Built Helm-based ArgoCD payload for app: {}", appName);
        return finalJson;
    }
    
    private Map<String, String> createHelmParam(String name, String value) {
        Map<String, String> param = new HashMap<>();
        param.put("name", name);
        param.put("value", value);
        return param;
    }
    
    public void registerRepository(String token, String repoUrl, String username, String password) throws Exception {
        try {
            String repoApiUrl = argocdCreateUrl.replace("/applications", "/repositories");
            log.info("Registering repository with ArgoCD: {}", repoUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> repoPayload = new HashMap<>();
            repoPayload.put("project", "cs-apps");
            repoPayload.put("repo", repoUrl);
            repoPayload.put("type", "git");
            repoPayload.put("username", username);
            repoPayload.put("password", password);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(repoPayload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(repoApiUrl, entity, String.class);
            
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("Repository registered successfully with ArgoCD: {}", repoUrl);
            } else {
                log.warn("Repository registration returned status: {}", response != null ? response.getStatusCode() : "null");
            }
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            log.info("Repository already registered in ArgoCD: {}", repoUrl);
        } catch (Exception e) {
            log.error("Failed to register repository: {}", e.getMessage());
        }
    }
    
    public Map<String, String> calculateResources(String gitRepoUrl) {
    try {
        String valuesYamlContent = fetchValuesYaml(gitRepoUrl);
        if (valuesYamlContent == null || valuesYamlContent.trim().isEmpty()) {
            return null;
        }
        Yaml yaml = new Yaml();
        Map<String, Object> values = yaml.load(valuesYamlContent);
        if (values == null || !values.containsKey("resources")) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> resourcesSection =
                (Map<String, Object>) values.get("resources");
        if (!resourcesSection.containsKey("requests")) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> requests =
                (Map<String, Object>) resourcesSection.get("requests");
        Map<String, String> convertedResources = new HashMap<>();
        if (requests.containsKey("cpu")) {
            String cpuValue = String.valueOf(requests.get("cpu"));
            String convertedCpu = convertCpu(cpuValue);
            if (convertedCpu != null) {
                convertedResources.put("cpu", convertedCpu);
            }
        }
        if (requests.containsKey("memory")) {
            String memoryValue = String.valueOf(requests.get("memory"));
            String convertedMemory = convertMemory(memoryValue);
            if (convertedMemory != null) {
                convertedResources.put("memory", convertedMemory);
            }
        }
        return convertedResources.isEmpty() ? null : convertedResources;
    } catch (Exception e) {
        log.error("Failed to calculate resources", e);
        return null;
    }
}
    private String convertCpu(String cpuValue) {
        cpuValue = cpuValue.trim();
        
        if (cpuValue.endsWith("m")) {
            return cpuValue.substring(0, cpuValue.length() - 1);
        }
        
        try {
            double cores = Double.parseDouble(cpuValue);
            return String.valueOf((int) (cores * 1000));
        } catch (NumberFormatException e) {
            log.warn("Invalid CPU value (must be 'm' or cores): {}", cpuValue);
            return null;
        }
    }
    
    private String convertMemory(String memoryValue) {
        memoryValue = memoryValue.trim();
        
        if (memoryValue.endsWith("Mi")) {
            return memoryValue.substring(0, memoryValue.length() - 2);
        }
        
        if (memoryValue.endsWith("Gi")) {
            try {
                double gigabytes = Double.parseDouble(memoryValue.substring(0, memoryValue.length() - 2));
                return String.valueOf((int) (gigabytes * 1024));
            } catch (NumberFormatException e) {
                log.warn("Invalid memory value: {}", memoryValue);
                return null;
            }
        }
        
        log.warn("Unsupported memory unit (must be 'Mi' or 'Gi'): {}", memoryValue);
        return null;
    }
    
    private String fetchValuesYaml(String gitRepoUrl) {
        try {
            String[] urlParts = gitRepoUrl.replace(".git", "").split("/");
            if (urlParts.length < 2) {
                log.info("Invalid Git URL format, skipping values.yaml fetch: {}", gitRepoUrl);
                return null;
            }
            
            String owner = urlParts[urlParts.length - 2];
            String repo = urlParts[urlParts.length - 1];
            
            String baseUrl = gitRepoUrl.substring(0, gitRepoUrl.lastIndexOf("/"));
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            String rawFileUrl = baseUrl + "/" + owner + "/" + repo + "/raw/main/deploy/helm/values.yaml";
            
            log.info("Attempting to fetch values.yaml from: {}", rawFileUrl);
            
            ResponseEntity<String> response = restTemplate.getForEntity(rawFileUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully fetched values.yaml");
                return response.getBody();
            } else {
                log.info("values.yaml not accessible, will deploy without custom resources");
                return null;
            }
        } catch (Exception e) {
            log.info("Could not fetch values.yaml ({}), continuing without custom resources", e.getMessage());
            return null;
        }
    }
    
    public ResponseEntity<String> getStatusOfArgoApp(String token, String appName) {
        try {
            String url = argocdCreateUrl + "/" + appName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.info("ArgoCD application not found: {}", appName);
            return ResponseEntity.status(404).build();
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            log.warn("Permission denied accessing ArgoCD application: {}", appName);
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Failed to get ArgoCD app status for {}", appName, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    public String checkArgoAppDeploymentStatus(String token, String appName) {
        try {
            ResponseEntity<String> argoResponse = getStatusOfArgoApp(token, appName);
            if (argoResponse == null || !argoResponse.getStatusCode().is2xxSuccessful()) {
                int statusCode = argoResponse != null ? argoResponse.getStatusCode().value() : 0;
                if (statusCode == 403) {
                    log.warn("ArgoCD app {} - permission denied, marking as FAILED", appName);
                    return "FAILED";
                }
                if (statusCode == 404) {
                    log.warn("ArgoCD app {} - not found, marking as FAILED", appName);
                    return "FAILED";
                }
                log.info("ArgoCD app {} not ready yet - DEPLOYING", appName);
                return "DEPLOYING";
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(argoResponse.getBody());
            String healthStatus = rootNode.path("status").path("health").path("status").asText("");
            String syncStatus = rootNode.path("status").path("sync").path("status").asText("");
            String operationPhase = rootNode.path("status").path("operationState").path("phase").asText("");
            log.info("ArgoCD app {} - Health: {}, Sync: {}, OperationPhase: {}", appName, healthStatus, syncStatus, operationPhase);
            switch (healthStatus.toLowerCase()) {
                case "healthy":
                    if ("synced".equalsIgnoreCase(syncStatus)) {
                        log.info("Application {} is healthy and synced - DEPLOYED", appName);
                        return "DEPLOYED";
                    } else if ("failed".equalsIgnoreCase(operationPhase) || "error".equalsIgnoreCase(operationPhase)) {
                        log.info("Application {} is healthy but sync failed (phase: {}) - FAILED", appName, operationPhase);
                        return "FAILED";
                    } else {
                        log.info("Application {} is healthy but sync status is {} (phase: {}) - DEPLOYING",
                                appName, syncStatus, operationPhase);
                        return "DEPLOYING";
                    }
                case "progressing":
                    log.info("Application {} is progressing - DEPLOYING", appName);
                    return "DEPLOYING";
                case "degraded":
                    log.info("Application {} is degraded - still DEPLOYING (may be transient during startup)", appName);
                    return "DEPLOYING";
                case "missing":
                    log.info("Application {} is missing - still DEPLOYING (resources not yet created)", appName);
                    return "DEPLOYING";
                case "suspended":
                    log.info("Application {} is suspended - FAILED", appName);
                    return "FAILED";
                case "unknown":
                    log.info("Application {} has unknown health status - DEPLOYING", appName);
                    return "DEPLOYING";
                default:
                    log.info("Unexpected health status {} for application {} - DEPLOYING",
                            healthStatus, appName);
                    return "DEPLOYING";
            }
        } catch (Exception e) {
            log.error("Failed to check ArgoCD deployment status for {}", appName, e);
            return "DEPLOYING";
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
