package com.daimler.data.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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

    @Value("${argocd.namespacePrefix}")
    private String argocdNamespacePrefix;

    @Value("${codeServer.git.ghe.pat}")
    private String ghePat;

    @Value("${codeServer.git.pat}")
    private String gitPat;

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
                                String gitRepoUrl, String imageTag, boolean vaultInjectorEnable, String branch) throws Exception {
        try {
            log.info("createArgoApp - projectName: {}, gitRepoUrl: {}, imageTag: {}, environment: {}, branch: {}", 
                     projectName, gitRepoUrl, imageTag, environment, branch);
    
            String appName = projectName + "-" + environment;
            String url = argocdCreateUrl + "?upsert=true";
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            Map<String, String> resources = calculateResources(gitRepoUrl);
            String targetRevision = (branch != null && !branch.isEmpty()) ? branch : "main";
            
            String payload = this.buildPayload(appName, projectName, codeServerEnvRef, environment, gitRepoUrl, imageTag, vaultInjectorEnable, resources, targetRevision);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("ArgoCD application created/updated successfully: {}", appName);
                return "success";
            } else {
                String errorBody = response != null ? response.getBody() : "no response";
                log.error("ArgoCD create/update failed for {}: HTTP {} - {}", appName, 
                         response != null ? response.getStatusCode() : "null", errorBody);
                throw new Exception("ArgoCD deployment failed for " + appName + ": " + errorBody);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("ArgoCD HTTP error for {}-{}: {} - {}", projectName, environment, 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("ArgoCD error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("ArgoCD connection error for {}-{}: {}", projectName, environment, e.getMessage());
            throw new Exception("Cannot connect to ArgoCD server: " + e.getMessage());
        } catch (Exception e) {
            log.error("ArgoCD deployment exception for {}-{}: {}", projectName, environment, e.getMessage());
            throw e;
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
            String namespace = getNamespaceForEnvironment(codeServerEnvRef, environment);
            
            String url = argocdCreateUrl + "/" + appName + "/resource/actions" +
                        "?namespace=" + namespace +
                        "&resourceName=" + appName +
                        "&version=v1" +
                        "&kind=Deployment" +
                        "&group=apps";
            
            log.info("[Restart] Calling ArgoCD restart: appName={}, namespace={}, url={}", appName, namespace, url);
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            HttpEntity<String> entity = new HttpEntity<>("\"restart\"", headers);
        
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("[Restart] ArgoCD application restarted successfully: {}", appName);
                return "success";
            } else {
                log.error("[Restart] Failed to restart ArgoCD app {}: status={}, body={}", 
                    appName, (response != null ? response.getStatusCode() : "null"), (response != null ? response.getBody() : "no response"));
                return "failed";
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("[Restart] ArgoCD HTTP error for {}: status={}, body={}", 
                workspaceName + "-" + environment, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "failed";
        } catch (Exception e) {
            log.error("[Restart] Failed to restart ArgoCD application {}: {}", workspaceName + "-" + environment, e.getMessage(), e);            
            return "failed";
        }
    }

    @SuppressWarnings("unchecked")
    public String buildPayload(String appName, String projectName, String clusterEnv, String targetEnv, String gitRepoUrl, 
                               String imageTag, boolean vaultInjectorEnable, Map<String, String> resources, String targetRevision) throws IOException {
        
        String namespace = getNamespaceForEnvironment(clusterEnv, targetEnv);
        String vaultAuthPath = getVaultAuthPath(clusterEnv);
        String imageRepository = imageRegistry + "-" + projectName;
        
        String vaultStage = "dev".equals(clusterEnv) ? "staging" : "production";
        String vaultInjectorPath = vaultKvPath + "/" + vaultStage + "/" + projectName + "/" + targetEnv;
        String vaultInjectorRootPath = "/" + projectName + "/" + targetEnv + "/api";
        String vaultInjectorRootPathNonApi = "/" + projectName + "/" + targetEnv + "/";
        
        List<Map<String, Object>> helmParameters = new ArrayList<>();
        
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
        helmParameters.add(createHelmParamForceString("podAnnotations.prometheus\\.io/scrape", "true"));
        
        if (resources != null && resources.isEmpty()) {
            // resources: {} in values.yaml — no override needed, chart default applies as-is
            log.info("[Resources] resources is empty in values.yaml, skipping override (chart default resources: {} will be used)");
        } else if (resources != null && !resources.isEmpty()) {
            String cpu = resources.get("cpu");
            String memory = resources.get("memory");
            if (cpu != null) {
                helmParameters.add(createHelmParam("resources.requests.cpu", cpu + "m"));
                log.info("[Resources] Sending -> resources.requests.cpu: {}", cpu + "m");
            }
            if (memory != null) {
                helmParameters.add(createHelmParam("resources.requests.memory", memory + "Mi"));
                helmParameters.add(createHelmParam("resources.limits.memory", memory + "Mi"));
                log.info("[Resources] Sending -> resources.requests.memory: {}, resources.limits.memory: {}", memory + "Mi", memory + "Mi");
            }
            // Explicitly remove limits.cpu by setting to "null" string
            helmParameters.add(createHelmParam("resources.limits.cpu", "null"));
            log.info("[Resources] Sending -> resources.limits.cpu: null (override to suppress chart default)");
            log.info("[Resources] Final resources being sent to ArgoCD: " +
                "requests.cpu={}, requests.memory={}, limits.memory={} (same as requests.memory), limits.cpu=null (removed)",
                cpu != null ? cpu + "m" : "not set",
                memory != null ? memory + "Mi" : "not set",
                memory != null ? memory + "Mi" : "not set");
        } else {
            log.info("[Resources] No resource overrides to apply, chart values.yaml will be used as-is");
        }
        
        Map<String, Object> payload = new HashMap<>();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", appName);
        metadata.put("namespace", "argocd");
        Map<String, String> labels = new HashMap<>();
        String envLabel = (argocdNamespacePrefix != null && !argocdNamespacePrefix.isEmpty()) 
            ? argocdNamespacePrefix : clusterEnv;
        labels.put("env", envLabel);
        labels.put("project", "cs-apps");
        labels.put("cs-env", targetEnv);
        metadata.put("labels", labels);
        payload.put("metadata", metadata);
        
        Map<String, Object> spec = new HashMap<>();
        spec.put("project", "cs-apps");
        
        Map<String, Object> source = new HashMap<>();
        source.put("repoURL", gitRepoUrl);
        source.put("path", "deploy/helm");
        source.put("targetRevision", targetRevision);
        
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
        
        List<Map<String, String>> infoList = new ArrayList<>();
        Map<String, String> csEnvInfo = new HashMap<>();
        csEnvInfo.put("name", "cs-env");
        csEnvInfo.put("value", targetEnv);
        infoList.add(csEnvInfo);
        spec.put("info", infoList);
        
        payload.put("spec", spec);
        
        ObjectMapper mapper = new ObjectMapper();
        String finalJson = mapper.writeValueAsString(payload); 
        log.info("Built Helm-based ArgoCD payload for app: {}", appName);
        return finalJson;
    }
    
    private Map<String, Object> createHelmParam(String name, String value) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", name);
        param.put("value", value);
        return param;
    }

    private Map<String, Object> createHelmParamForceString(String name, String value) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", name);
        param.put("value", value);
        param.put("forceString", true);
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
        log.info("[Resources] Starting resource calculation for repo: {}", gitRepoUrl);
        String valuesYamlContent = fetchValuesYaml(gitRepoUrl);
        if (valuesYamlContent == null || valuesYamlContent.trim().isEmpty()) {
            log.info("[Resources] values.yaml content is null or empty, skipping resource overrides");
            return null;
        }
        log.info("[Resources] Received values.yaml content (length={})", valuesYamlContent.length());
        log.info("[Resources] Full values.yaml content:\n{}", valuesYamlContent);
        
        // Parse YAML with explicit error handling
        Object yamlRoot;
        try {
            Yaml yaml = new Yaml();
            yamlRoot = yaml.load(valuesYamlContent);
        } catch (Exception yamlEx) {
            log.error("[Resources] Failed to parse values.yaml as YAML: {}. First 500 chars: {}", 
                yamlEx.getMessage(), valuesYamlContent.substring(0, Math.min(500, valuesYamlContent.length())));
            return null;
        }
        
        if (yamlRoot == null) {
            log.info("[Resources] YAML parsed to null, skipping resource overrides");
            return null;
        }
        if (!(yamlRoot instanceof Map)) {
            log.warn("[Resources] YAML root is not a Map but a {}. First 500 chars: {}", 
                yamlRoot.getClass().getSimpleName(), valuesYamlContent.substring(0, Math.min(500, valuesYamlContent.length())));
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) yamlRoot;
        
        if (!values.containsKey("resources")) {
            log.info("[Resources] No 'resources' section found in values.yaml. Available top-level keys: {}", values.keySet());
            return null;
        }
        
        Object resourcesObj = values.get("resources");
        if (resourcesObj == null || !(resourcesObj instanceof Map)) {
            log.warn("[Resources] 'resources' is not a Map but: {} (value={})", 
                resourcesObj != null ? resourcesObj.getClass().getSimpleName() : "null", resourcesObj);
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resourcesSection = (Map<String, Object>) resourcesObj;
        log.info("[Resources] Raw resources from values.yaml: {}", resourcesSection);
        
        // If resources is explicitly empty {}, return empty map to signal "resources: {}" override
        if (resourcesSection.isEmpty()) {
            log.info("[Resources] resources is explicitly empty {}, will send resources={} override");
            return new HashMap<>();
        }
        
        if (!resourcesSection.containsKey("requests")) {
            log.info("[Resources] No 'requests' section found in resources. Available keys: {}", resourcesSection.keySet());
            return null;
        }
        
        Object requestsObj = resourcesSection.get("requests");
        if (requestsObj == null || !(requestsObj instanceof Map)) {
            log.warn("[Resources] 'requests' is not a Map but: {} (value={})", 
                requestsObj != null ? requestsObj.getClass().getSimpleName() : "null", requestsObj);
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> requests = (Map<String, Object>) requestsObj;
        log.info("[Resources] Raw requests from values.yaml: {}", requests);
        Map<String, String> convertedResources = new HashMap<>();
        if (requests.containsKey("cpu")) {
            String cpuValue = String.valueOf(requests.get("cpu"));
            String convertedCpu = convertCpu(cpuValue);
            log.info("[Resources] CPU: raw='{}' -> converted='{}'", cpuValue, convertedCpu);
            if (convertedCpu != null) {
                convertedResources.put("cpu", convertedCpu);
            }
        }
        if (requests.containsKey("memory")) {
            String memoryValue = String.valueOf(requests.get("memory"));
            String convertedMemory = convertMemory(memoryValue);
            log.info("[Resources] Memory: raw='{}' -> converted='{}'", memoryValue, convertedMemory);
            if (convertedMemory != null) {
                convertedResources.put("memory", convertedMemory);
            }
        }
        
        log.info("[Resources] Final calculated resources: {}", convertedResources);
        return convertedResources.isEmpty() ? null : convertedResources;
    } catch (Exception e) {
        log.error("[Resources] Failed to calculate resources: {}", e.getMessage(), e);
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
            
            // Use GHE/GitHub API endpoint instead of web UI raw URL
            String baseUrl = gitRepoUrl.substring(0, gitRepoUrl.lastIndexOf("/"));
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            String apiUrl = baseUrl + "/api/v3/repos/" + owner + "/" + repo + "/contents/deploy/helm/values.yaml?ref=main";
            
            log.info("[Resources] Attempting to fetch values.yaml from API: {}", apiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            // Use GHE PAT for GHE repos, standard PAT otherwise
            boolean isGheRepo = gitRepoUrl.contains(".ghe.");
            String pat = isGheRepo ? ghePat : gitPat;
            if (pat != null && !pat.isEmpty()) {
                headers.set("Authorization", "token " + pat);
            } else {
                log.warn("[Resources] No PAT configured for {} repo, request will be unauthenticated", isGheRepo ? "GHE" : "Git");
            }
            headers.set("Accept", "application/vnd.github.v3.raw");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String body = response.getBody();
                String preview = body.substring(0, Math.min(300, body.length()));
                log.info("[Resources] Successfully fetched values.yaml (HTTP {}, length={}). Preview: {}", 
                    response.getStatusCode().value(), body.length(), preview);
                
                // Detect HTML response (GHE returns login page with HTTP 200 when auth fails)
                String trimmed = body.trim();
                if (trimmed.startsWith("<") || trimmed.startsWith("<!DOCTYPE")) {
                    log.warn("[Resources] Response is HTML, not YAML — likely GHE login page (auth issue). PAT may be missing/expired/invalid. First 300 chars: {}", preview);
                    return null;
                }
                
                // Detect JSON API response (Accept header was ignored, got JSON wrapper with base64 content)
                if (trimmed.startsWith("{")) {
                    log.info("[Resources] Response is JSON instead of raw YAML — Accept header may have been ignored. Attempting to extract base64 content.");
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(body);
                        String encoding = jsonNode.path("encoding").asText("");
                        String content = jsonNode.path("content").asText("");
                        if ("base64".equals(encoding) && !content.isEmpty()) {
                            // GitHub API returns base64 with newlines, strip them before decoding
                            String cleanContent = content.replaceAll("\\s", "");
                            String decoded = new String(Base64.getDecoder().decode(cleanContent), StandardCharsets.UTF_8);
                            log.info("[Resources] Successfully decoded base64 content from JSON response (decoded length={})", decoded.length());
                            return decoded;
                        } else {
                            log.warn("[Resources] JSON response has no base64 content field. Keys: {}", jsonNode.fieldNames());
                            return null;
                        }
                    } catch (Exception jsonEx) {
                        log.warn("[Resources] Failed to parse JSON response: {}", jsonEx.getMessage());
                        return null;
                    }
                }
                
                // Content looks like raw YAML, return as-is
                return body;
            } else {
                log.info("[Resources] values.yaml fetch returned HTTP {}, will deploy without custom resources", 
                    response != null ? response.getStatusCode().value() : "null");
                return null;
            }
        } catch (Exception e) {
            log.info("[Resources] Could not fetch values.yaml ({}), continuing without custom resources", e.getMessage());
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
            String lastSyncPhase = rootNode.path("status").path("operationState").path("phase").asText("");
            log.info("ArgoCD app {} - Health: {}, Sync: {}, LastSyncPhase: {}", appName, healthStatus, syncStatus, lastSyncPhase);
            switch (healthStatus.toLowerCase()) {
                case "healthy":
                    // Healthy but last sync failed means the new changes didn't apply
                    if ("Failed".equalsIgnoreCase(lastSyncPhase) || "Error".equalsIgnoreCase(lastSyncPhase)) {
                        log.info("Application {} is healthy but last sync {} - FAILED", appName, lastSyncPhase);
                        return "FAILED";
                    }
                    // Only mark as DEPLOYED when both Healthy AND last sync Succeeded
                    if ("Succeeded".equalsIgnoreCase(lastSyncPhase)) {
                        log.info("Application {} is healthy and last sync succeeded - DEPLOYED", appName);
                        return "DEPLOYED";
                    }
                    // Healthy but sync not yet succeeded (Running, empty, etc.) — still deploying
                    log.info("Application {} is healthy but last sync phase is {} - DEPLOYING", appName, lastSyncPhase);
                    return "DEPLOYING";
                case "degraded":
                    log.info("Application {} is degraded - FAILED", appName);
                    return "FAILED";
                case "progressing":
                    log.info("Application {} is progressing - DEPLOYING", appName);
                    return "DEPLOYING";
                case "missing":
                    log.info("Application {} is missing - DEPLOYING (resources not yet created)", appName);
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
        String prefix = (argocdNamespacePrefix != null && !argocdNamespacePrefix.isEmpty()) 
            ? argocdNamespacePrefix : clusterEnv;
        if ("int".equalsIgnoreCase(targetEnv)) {
            return prefix + "-dna-cs-apps-int";
        }
        return prefix + "-dna-cs-apps";
    }
    
    private String getVaultAuthPath(String clusterEnv) {
        return "auth/k8_auth_dna_aws_" + clusterEnv;
    }

    /**
     * Fetches the resource tree for an ArgoCD application, extracting pod info.
     * Returns a list of maps with keys: name, status, namespace, kind.
     */
    public List<Map<String, String>> getAppPods(String token, String appName) {
        List<Map<String, String>> pods = new ArrayList<>();
        try {
            String url = argocdCreateUrl + "/" + appName + "/resource-tree";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode nodes = root.path("nodes");
                if (nodes.isArray()) {
                    for (JsonNode node : nodes) {
                        String kind = node.path("kind").asText("");
                        if ("Pod".equalsIgnoreCase(kind)) {
                            Map<String, String> podInfo = new HashMap<>();
                            podInfo.put("name", node.path("name").asText(""));
                            podInfo.put("namespace", node.path("namespace").asText(""));
                            podInfo.put("status", node.path("health").path("status").asText("Unknown"));
                            podInfo.put("kind", kind);
                            pods.add(podInfo);
                        }
                    }
                }
            }
            log.info("Found {} pods for ArgoCD app: {}", pods.size(), appName);
        } catch (Exception e) {
            log.error("Failed to get resource tree for ArgoCD app {}: {}", appName, e.getMessage());
        }
        return pods;
    }

    /**
     * Fetches pod logs from the ArgoCD logs API.
     * Returns a list of log lines for the given pod.
     */
    public List<String> getPodLogs(String token, String appName, String podName, String namespace, int sinceSeconds) {
        List<String> logLines = new ArrayList<>();
        try {
            String url = argocdCreateUrl + "/" + appName + "/logs"
                    + "?podName=" + podName
                    + "&namespace=" + namespace
                    + "&sinceSeconds=" + sinceSeconds
                    + "&follow=false";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            log.debug("Fetching pod logs from: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                String body = response.getBody();
                // ArgoCD logs API returns newline-delimited JSON objects: {"result":{"content":"...","podName":"..."}}
                String[] lines = body.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    try {
                        JsonNode logEntry = mapper.readTree(line);
                        String content = logEntry.path("result").path("content").asText("");
                        if (!content.isEmpty()) {
                            logLines.add(content);
                        }
                    } catch (Exception parseEx) {
                        // If not JSON, treat as raw log line
                        logLines.add(line);
                    }
                }
            }
            log.debug("Fetched {} log lines for pod {} in app {}", logLines.size(), podName, appName);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("Pod {} not found in ArgoCD app {}", podName, appName);
        } catch (Exception e) {
            log.error("Failed to fetch pod logs for {} in app {}: {}", podName, appName, e.getMessage());
        }
        return logLines;
    }
}
