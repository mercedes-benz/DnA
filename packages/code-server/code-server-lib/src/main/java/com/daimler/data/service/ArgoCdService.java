package com.daimler.data.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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

    @Value("${argocd.vaultAuthPath}")
    private String vaultAuthPath;
 
    @Value("${argocd.vaultMountPath}")
    private String vaultMountPath;

    @Value("${codeServer.git.ghe.pat}")
    private String ghePat;

    @Value("${codeServer.git.pat}")
    private String gitPat;

    @Value("${argocd.resources.defaults.requests.cpu}")
    private String defaultRequestCpu;

    @Value("${argocd.resources.defaults.requests.memory.mi}")
    private String defaultRequestMemoryMi;

    @Value("${argocd.resources.defaults.limits.memory.mi}")
    private String defaultLimitMemoryMi;

    @Autowired
    private RestTemplate restTemplate;

    private volatile String cachedArgoToken;
    private volatile long cachedArgoTokenExpiresAt;
    private static final long ARGO_TOKEN_REFRESH_BUFFER_MS = 5 * 60 * 1000L; // refresh 5 min before expiry
    private static final long ARGO_TOKEN_FALLBACK_TTL_MS = 23 * 60 * 60 * 1000L; // fallback if exp claim missing
    private final ReentrantLock tokenLock = new ReentrantLock();

    public String getArgocdBaseUrl() {
        if (argocdCreateUrl != null && argocdCreateUrl.contains("/api/")) {
            return argocdCreateUrl.substring(0, argocdCreateUrl.indexOf("/api/"));
        }
        return argocdCreateUrl;
    }

    public String getGhePat() {
        return ghePat;
    }

    public String getGitPat() {
        return gitPat;
    }

    private long extractTokenExpiry(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return System.currentTimeMillis() + ARGO_TOKEN_FALLBACK_TTL_MS;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode claims = mapper.readTree(payload);
            if (claims.has("exp")) {
                long expSeconds = claims.get("exp").asLong();
                return (expSeconds * 1000L) - ARGO_TOKEN_REFRESH_BUFFER_MS;
            }
        } catch (Exception e) {
            log.debug("Could not parse JWT exp claim, using fallback TTL: {}", e.getMessage());
        }
        return System.currentTimeMillis() + ARGO_TOKEN_FALLBACK_TTL_MS;
    }

    public String getArgoToken() throws Exception {
        String token = cachedArgoToken;
        if (token != null && System.currentTimeMillis() < cachedArgoTokenExpiresAt) {
            return token;
        }
        tokenLock.lock();
        try {
            // Double-check after acquiring lock
            if (cachedArgoToken != null && System.currentTimeMillis() < cachedArgoTokenExpiresAt) {
                return cachedArgoToken;
            }
            String url = argocdTokenUrl;
            log.info("Requesting new ArgoCD token (previous expired or absent)");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> request = Map.of("username", tokenUserName, "password", tokenPassword);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            String newToken = (String) body.get("token");
            cachedArgoToken = newToken;
            cachedArgoTokenExpiresAt = extractTokenExpiry(newToken);
            long remainingSec = (cachedArgoTokenExpiresAt - System.currentTimeMillis()) / 1000;
            log.info("Obtained ArgoCD token, cached until {}s before expiry ({}s from now)", ARGO_TOKEN_REFRESH_BUFFER_MS / 1000, remainingSec);
            return newToken;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            cachedArgoToken = null;
            String errorMsg = "ArgoCD authentication failed (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
            log.error("Failed to get ArgoCD token from URL: {}. Error: {}", argocdTokenUrl, errorMsg);
            throw new Exception(errorMsg);
        } catch (Exception e) {
            cachedArgoToken = null;
            String errorMsg = "Failed to connect to ArgoCD server at " + argocdTokenUrl + ": " + e.getMessage();
            log.error("ArgoCD connection error: {}", errorMsg, e);
            throw new Exception(errorMsg);
        } finally {
            tokenLock.unlock();
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
        
            Map<String, String> resources = calculateResources(gitRepoUrl, branch);
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
            
            try {
                ResponseEntity<String> statusResponse = getStatusOfArgoApp(token, appName);
                if (statusResponse == null || !statusResponse.getStatusCode().is2xxSuccessful()) {
                    log.warn("[Restart] ArgoCD application does not exist or is not accessible: {}", appName);
                    return "not_found";
                }
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                log.warn("[Restart] ArgoCD application not found (404): {}", appName);
                return "not_found";
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404 || e.getStatusCode().value() == 403) {
                    log.warn("[Restart] ArgoCD application not accessible ({}): {}", e.getStatusCode(), appName);
                    return "not_found";
                }
                throw e;
            }
            
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
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("[Restart] ArgoCD resource not found during restart: {}-{}", workspaceName, environment);
            return "not_found";
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("[Restart] ArgoCD HTTP error for {}: status={}, body={}", 
                workspaceName + "-" + environment, e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().value() == 404) {
                return "not_found";
            }
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
        String imageRepository = imageRegistry + "-" + projectName;
        
        String vaultInjectorPath = vaultKvPath + "/" + vaultMountPath + "/" + projectName + "/" + targetEnv;
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
        helmParameters.add(createHelmParam("vaultInjector.authpath", this.vaultAuthPath));
        helmParameters.add(createHelmParam("vaultInjector.namespace", "/"));
        helmParameters.add(createHelmParamForceString("podAnnotations.prometheus\\.io/scrape", "true"));
        
        if (resources != null && resources.isEmpty()) {
            helmParameters.add(createHelmParam("resources.requests.cpu", defaultRequestCpu));
            helmParameters.add(createHelmParam("resources.requests.memory", defaultRequestMemoryMi + "Mi"));
            helmParameters.add(createHelmParam("resources.limits.memory", defaultLimitMemoryMi + "Mi"));
            log.info("[Resources] Empty resources in values.yaml, sending defaults");
        } else if (resources != null && !resources.isEmpty()) {
            String cpu = resources.get("cpu");
            String memory = resources.get("memory");
            String limitsMemory = resources.get("limitsMemory");
            boolean hasLimitsCpu = "true".equals(resources.get("hasLimitsCpu"));

            // Missing keys inside a non-empty resources block should still use case-1 defaults.
            String requestCpu = (cpu != null) ? cpu + "m" : defaultRequestCpu;
            boolean requestMemoryDefaulted = (memory == null);
            String requestMemory = requestMemoryDefaulted ? defaultRequestMemoryMi : memory;
            String limitMemory = (limitsMemory != null) ? limitsMemory : defaultLimitMemoryMi;

            if (requestMemoryDefaulted && parseMemoryMi(limitMemory) < parseMemoryMi(defaultRequestMemoryMi)) {
                limitMemory = defaultRequestMemoryMi;
            }

            helmParameters.add(createHelmParam("resources.requests.cpu", requestCpu));
            helmParameters.add(createHelmParam("resources.requests.memory", requestMemory + "Mi"));
            helmParameters.add(createHelmParam("resources.limits.memory", limitMemory + "Mi"));
            if (hasLimitsCpu) {
                helmParameters.add(createHelmParam("resources.limits.cpu", "null"));
            }
            log.info("[Resources] Applying Helm params: requests.cpu={}, requests.memory={} (defaulted={}), limits.memory={} (source={}), limits.cpu={}",
                requestCpu,
                requestMemory + "Mi",
                requestMemoryDefaulted,
                limitMemory + "Mi",
                limitsMemory != null ? "values.yaml" : "default",
                hasLimitsCpu ? "null" : "not set");

        } else {
            log.info("[Resources] No resource overrides, using chart defaults");
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
    
    public Map<String, String> calculateResources(String gitRepoUrl, String branch) {
        try {
            log.info("[Resources] Starting resource calculation for repo: {}", gitRepoUrl);
            String valuesYamlContent = fetchValuesYaml(gitRepoUrl, branch);
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

            if (resourcesSection.isEmpty()) {
                log.info("[Resources] resources is explicitly empty {}, will use hardcoded defaults");
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

            Object limitsObj = resourcesSection.get("limits");
            if (limitsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> limits = (Map<String, Object>) limitsObj;

                if (limits.containsKey("cpu")) {
                    convertedResources.put("hasLimitsCpu", "true");
                }

                if (limits.containsKey("memory")) {
                    String limitsMemoryValue = String.valueOf(limits.get("memory"));
                    String convertedLimitsMemory = convertMemory(limitsMemoryValue);
                    log.info("[Resources] Limits.Memory: raw='{}' -> converted='{}'", limitsMemoryValue, convertedLimitsMemory);
                    if (convertedLimitsMemory != null) {
                        convertedResources.put("limitsMemory", convertedLimitsMemory);
                    }
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
    
    private double parseMemoryMi(String memoryMi) {
        try {
            return Double.parseDouble(memoryMi.trim());
        } catch (NumberFormatException e) {
            log.warn("[Resources] Unable to parse memory Mi value: {}", memoryMi);
            return Double.MAX_VALUE;
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
    
    private String fetchValuesYaml(String gitRepoUrl, String branch) {
        try {
            String[] urlParts = gitRepoUrl.replace(".git", "").split("/");
            if (urlParts.length < 2) {
                log.info("Invalid Git URL format, skipping values.yaml fetch: {}", gitRepoUrl);
                return null;
            }
            
            String owner = urlParts[urlParts.length - 2];
            String repo = urlParts[urlParts.length - 1];
            
            String ref = (branch != null && !branch.isEmpty()) ? branch : "main";
            // Use GHE/GitHub API endpoint instead of web UI raw URL
            String baseUrl = gitRepoUrl.substring(0, gitRepoUrl.lastIndexOf("/"));
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            String apiUrl = baseUrl + "/api/v3/repos/" + owner + "/" + repo + "/contents/deploy/helm/values.yaml?ref=" + ref;
            
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
            log.debug("Permission denied accessing ArgoCD application: {} (RBAC may still be propagating)", appName);
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
                    log.info("ArgoCD app {} - permission denied (RBAC propagating), treating as DEPLOYING", appName);
                    return "DEPLOYING";
                }
                if (statusCode == 404) {
                    log.warn("ArgoCD app {} - not found, marking as DEPLOYMENT_FAILED", appName);
                    return "DEPLOYMENT_FAILED";
                }
                log.info("ArgoCD app {} not ready yet - DEPLOY_REQUESTED", appName);
                return "DEPLOY_REQUESTED";
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(argoResponse.getBody());
            String healthStatus = rootNode.path("status").path("health").path("status").asText("");
            String lastSyncPhase = rootNode.path("status").path("operationState").path("phase").asText("");
            String operationMessage = rootNode.path("status").path("operationState").path("message").asText("");
            log.info("ArgoCD app {} - Health: {}, LastSyncPhase: {}, Message: {}", appName, healthStatus, lastSyncPhase, operationMessage);

            if (operationMessage != null && !operationMessage.isEmpty() &&
                (operationMessage.toLowerCase().contains("failed") || operationMessage.toLowerCase().contains("error"))) {
                log.info("Application {} has error message - DEPLOYMENT_FAILED", appName);
                return "DEPLOYMENT_FAILED";
            }

            if ("Failed".equalsIgnoreCase(lastSyncPhase) || "Error".equalsIgnoreCase(lastSyncPhase)) {
                log.info("Application {} sync failed (health={}, syncPhase={}) - DEPLOYMENT_FAILED", appName, healthStatus, lastSyncPhase);
                return "DEPLOYMENT_FAILED";
            }

            if ("Degraded".equalsIgnoreCase(healthStatus)) {
                // Degraded is only a definitive failure if the sync has already completed successfully.
                // During active deployment, Degraded is often transient (image pull, readiness probe warmup).
                if ("Succeeded".equalsIgnoreCase(lastSyncPhase)) {
                    log.info("Application {} is degraded after sync succeeded - DEPLOYMENT_FAILED", appName);
                    return "DEPLOYMENT_FAILED";
                }
                log.info("Application {} is degraded but sync not yet succeeded (syncPhase={}) - treating as DEPLOYING", appName, lastSyncPhase);
                return "DEPLOYING";
            }

            if ("Running".equalsIgnoreCase(lastSyncPhase)) {
                log.info("Application {} sync still running (health={}) - DEPLOYING", appName, healthStatus);
                return "DEPLOYING";
            }
 
            String syncStatus = rootNode.path("status").path("sync").path("status").asText("");
            if ("OutOfSync".equalsIgnoreCase(syncStatus)) {
                log.info("Application {} is OutOfSync (health={}, syncPhase={}) - DEPLOYING", appName, healthStatus, lastSyncPhase);
                return "DEPLOYING";
            }

            if ("Healthy".equalsIgnoreCase(healthStatus)) {
                if (!isDesiredImageDeployed(rootNode, appName)) {
                    log.info("Application {} is healthy but running stale image (new sync not started yet) - DEPLOYING", appName);
                    return "DEPLOYING";
                }
                log.info("Application {} is healthy with correct image - DEPLOYED", appName);
                return "DEPLOYED";
            }

            log.info("Application {} is in progress (health={}, syncPhase={}) - DEPLOY_REQUESTED", appName, healthStatus, lastSyncPhase);
            return "DEPLOY_REQUESTED";
        } catch (Exception e) {
            log.error("Failed to check ArgoCD deployment status for {}", appName, e);
            return "DEPLOY_REQUESTED";
        }
    }
    public Map<String, String> checkArgoAppDeploymentStatusWithError(String token, String appName) {
        Map<String, String> result = new HashMap<>();
        result.put("status", "DEPLOY_REQUESTED");
        result.put("errorMessage", "");
        try {
            ResponseEntity<String> argoResponse = getStatusOfArgoApp(token, appName);
            if (argoResponse == null || !argoResponse.getStatusCode().is2xxSuccessful()) {
                int statusCode = argoResponse != null ? argoResponse.getStatusCode().value() : 0;
                if (statusCode == 403) {
                    log.info("ArgoCD app {} - permission denied (RBAC propagating), treating as DEPLOYING", appName);
                    result.put("status", "DEPLOYING");
                    return result;
                }
                if (statusCode == 404) {
                    result.put("status", "DEPLOYMENT_FAILED");
                    result.put("errorMessage", "ArgoCD app " + appName + " - not found (HTTP 404)");
                    return result;
                }
                return result;
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(argoResponse.getBody());
            String healthStatus = rootNode.path("status").path("health").path("status").asText("");
            String lastSyncPhase = rootNode.path("status").path("operationState").path("phase").asText("");
            String operationMessage = rootNode.path("status").path("operationState").path("message").asText("");

            if (operationMessage != null && !operationMessage.isEmpty() &&
                (operationMessage.toLowerCase().contains("failed") || operationMessage.toLowerCase().contains("error"))) {
                result.put("status", "DEPLOYMENT_FAILED");
                result.put("errorMessage", operationMessage);
                return result;
            }
            if ("Failed".equalsIgnoreCase(lastSyncPhase) || "Error".equalsIgnoreCase(lastSyncPhase)) {
                result.put("status", "DEPLOYMENT_FAILED");
                result.put("errorMessage", operationMessage != null ? operationMessage : "Sync phase failed");
                return result;
            }
            if ("Degraded".equalsIgnoreCase(healthStatus)) {
                // Degraded is only a definitive failure if the sync has already completed successfully.
                // During active deployment, Degraded is often transient (image pull, readiness probe warmup).
                if ("Succeeded".equalsIgnoreCase(lastSyncPhase)) {
                    result.put("status", "DEPLOYMENT_FAILED");
                    result.put("errorMessage", operationMessage != null && !operationMessage.isEmpty()
                        ? operationMessage : "Application health is Degraded. Check pod logs for details.");
                    return result;
                }
                // Sync hasn't completed — treat as still deploying
                result.put("status", "DEPLOYING");
                return result;
            }
            if ("Running".equalsIgnoreCase(lastSyncPhase)) {
                return result; 
            }
 
            String syncStatus = rootNode.path("status").path("sync").path("status").asText("");
            if ("OutOfSync".equalsIgnoreCase(syncStatus)) {
                return result; 
            }
            if ("Healthy".equalsIgnoreCase(healthStatus)) {
                if (!isDesiredImageDeployed(rootNode, appName)) {
                    log.info("Application {} is healthy but running stale image (new sync not started yet) - DEPLOYING", appName);
                    result.put("status", "DEPLOYING");
                    return result;
                }
                result.put("status", "DEPLOYED");
                return result;
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to check ArgoCD deployment status for {}", appName, e);
            return result;
        }
    }

    /**
     * Checks whether the image tag currently running in ArgoCD matches the desired
     * image tag from the app spec. Returns false (stale) when ArgoCD still serves
     * a previously-deployed image because the new sync hasn't started yet.
     *
     * Extracts the desired tag from spec.source.helm.parameters (image.tag) and
     * compares it against the list of actually-running images in
     * status.summary.images. If either field is missing, returns true (optimistic)
     * to avoid blocking deployments when ArgoCD omits the data.
     */
    public boolean isDesiredImageDeployed(JsonNode rootNode, String appName) {
        try {
            String desiredTag = getDesiredImageTag(rootNode);
            if (desiredTag == null || desiredTag.isEmpty()) {
                log.debug("No desired image.tag found in spec for {} - skipping image validation", appName);
                return true;
            }

            List<String> runningImages = getRunningImages(rootNode);
            if (runningImages.isEmpty()) {
                log.debug("No running images found in status.summary for {} - skipping image validation", appName);
                return true;
            }

            boolean matched = runningImages.stream()
                    .anyMatch(img -> img.contains(":" + desiredTag));
            log.info("Image validation for {}: desired tag={}, running images={}, matched={}",
                    appName, desiredTag, runningImages, matched);
            return matched;
        } catch (Exception e) {
            log.warn("Image validation failed for {} - allowing status through: {}", appName, e.getMessage());
            return true;
        }
    }

    /**
     * Extracts the desired image.tag from the ArgoCD app spec's Helm parameters.
     */
    private String getDesiredImageTag(JsonNode rootNode) {
        JsonNode parameters = rootNode.path("spec").path("source").path("helm").path("parameters");
        if (parameters.isArray()) {
            for (JsonNode param : parameters) {
                if ("image.tag".equals(param.path("name").asText(""))) {
                    return param.path("value").asText("");
                }
            }
        }
        return null;
    }

    /**
     * Extracts the list of currently-running container images from ArgoCD status.
     */
    private List<String> getRunningImages(JsonNode rootNode) {
        List<String> images = new ArrayList<>();
        JsonNode imagesNode = rootNode.path("status").path("summary").path("images");
        if (imagesNode.isArray()) {
            for (JsonNode img : imagesNode) {
                images.add(img.asText(""));
            }
        }
        return images;
    }

    private String getNamespaceForEnvironment(String clusterEnv, String targetEnv) {
        String prefix = (argocdNamespacePrefix != null && !argocdNamespacePrefix.isEmpty())
            ? argocdNamespacePrefix : clusterEnv;
        if ("int".equalsIgnoreCase(targetEnv)) {
            return prefix + "-dna-cs-apps-int";
        }
        return prefix + "-dna-cs-apps";
    }

}