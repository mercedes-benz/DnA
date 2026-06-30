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

    @Value("${argocd.vaultAuthPath}")
    private String vaultAuthPath;
 
    @Value("${argocd.vaultMountPath}")
    private String vaultMountPath;

    @Value("${argocd.resourceCap.cpuRequestMax}")
    private int resourceCapCpuRequestMax;

    @Value("${argocd.resourceCap.memoryRequestMax}")
    private int resourceCapMemoryRequestMax;

    @Value("${argocd.resourceCap.memoryLimitMax}")
    private int resourceCapMemoryLimitMax;

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

    public String getGhePat() {
        return ghePat;
    }

    public String getGitPat() {
        return gitPat;
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
                                String gitRepoUrl, String imageTag, boolean vaultInjectorEnable, String branch,
                                boolean resourceExceptionEnabled) throws Exception {
        try {
            log.info("createArgoApp - projectName: {}, gitRepoUrl: {}, imageTag: {}, environment: {}, branch: {}, resourceExceptionEnabled: {}", 
                     projectName, gitRepoUrl, imageTag, environment, branch, resourceExceptionEnabled);
    
            String appName = projectName + "-" + environment;
            String url = argocdCreateUrl + "?upsert=true";
    
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
        
            Map<String, String> resources = calculateResources(gitRepoUrl, branch);
            log.info("[Resources] calculateResources returned: {} for app {}", resources, appName);
            log.info("[Resources] resourceExceptionEnabled={} for app {}", resourceExceptionEnabled, appName);

            // Always apply defaults to fill missing keys (regardless of exception flag)
            resources = applyResourceDefaults(resources);
            log.info("[Resources] After applyResourceDefaults: {} for app {}", resources, appName);

            // Apply caps only when exception flag is NOT enabled
            if (!resourceExceptionEnabled) {
                resources = applyResourceCaps(resources);
                log.info("[Resources] After applyResourceCaps: {} for app {}", resources, appName);
            } else {
                log.info("[Resources] Resource exception enabled for {}, skipping resource caps. Final resources: {}", appName, resources);
            }
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
        
        log.info("[Resources][buildPayload] Input resources map for {}: {}", appName, resources);
        if (resources != null && resources.isEmpty()) {
            // Scenario 1: resources: {} — apply all base defaults
            helmParameters.add(createHelmParam("resources.requests.cpu", "250m"));
            helmParameters.add(createHelmParam("resources.requests.memory", "256Mi"));
            helmParameters.add(createHelmParam("resources.limits.memory", "1Gi"));
            log.info("[Resources][buildPayload] Scenario 1: Empty resources in values.yaml, sending defaults: cpu=250m, memory=256Mi, limits.memory=1Gi");
        } else if (resources != null && !resources.isEmpty()) {
            // Scenario 2: resources provided — defaults already filled by applyResourceDefaults
            String cpu = resources.getOrDefault("cpu", "250");
            String memory = resources.getOrDefault("memory", "256");
            String limitsMemory = resources.getOrDefault("limitsMemory", "1024");
            boolean hasLimitsCpu = "true".equals(resources.get("hasLimitsCpu"));

            log.info("[Resources][buildPayload] Scenario 2: Using resolved values - cpu={}m, memory={}Mi, limitsMemory={}Mi, hasLimitsCpu={}",
                cpu, memory, limitsMemory, hasLimitsCpu);

            helmParameters.add(createHelmParam("resources.requests.cpu", cpu + "m"));
            helmParameters.add(createHelmParam("resources.requests.memory", memory + "Mi"));
            helmParameters.add(createHelmParam("resources.limits.memory", limitsMemory + "Mi"));
            // CPU limit: only if key existed in YAML, set to null (no limit)
            if (hasLimitsCpu) {
                helmParameters.add(createHelmParam("resources.limits.cpu", "null"));
            }

            log.info("[Resources][buildPayload] Final Helm params: requests.cpu={}, requests.memory={}, limits.memory={}, limits.cpu={}",
                cpu + "m", memory + "Mi", limitsMemory + "Mi",
                hasLimitsCpu ? "null (no limit)" : "not set (key absent)");
        } else {
            log.warn("[Resources][buildPayload] Scenario 3: resources is NULL — no resource helm params sent. Chart defaults will be used. " +
                     "This likely means values.yaml fetch failed for app: {}", appName);
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

            Map<String, String> convertedResources = new HashMap<>();

            // Parse requests section (may be missing or partial)
            Object requestsObj = resourcesSection.get("requests");
            if (requestsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> requests = (Map<String, Object>) requestsObj;
                log.info("[Resources] Raw requests from values.yaml: {}", requests);

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
            } else {
                log.info("[Resources] No 'requests' section or not a Map. Defaults will be applied later.");
            }

            // Parse limits section (may be missing or partial)
            Object limitsObj = resourcesSection.get("limits");
            log.info("[Resources] limits section: type={}, value={}", 
                limitsObj != null ? limitsObj.getClass().getSimpleName() : "null", limitsObj);
            if (limitsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> limits = (Map<String, Object>) limitsObj;
                log.info("[Resources] Parsed limits map: {}", limits);

                if (limits.containsKey("cpu")) {
                    convertedResources.put("hasLimitsCpu", "true");
                    log.info("[Resources] Found limits.cpu, will set to null (no limit)");
                }

                if (limits.containsKey("memory")) {
                    String limitsMemoryValue = String.valueOf(limits.get("memory"));
                    String convertedLimitsMemory = convertMemory(limitsMemoryValue);
                    log.info("[Resources] Limits.Memory: raw='{}' -> converted='{}'", limitsMemoryValue, convertedLimitsMemory);
                    if (convertedLimitsMemory != null) {
                        convertedResources.put("limitsMemory", convertedLimitsMemory);
                    } else {
                        log.warn("[Resources] Limits.Memory conversion returned null for '{}'", limitsMemoryValue);
                    }
                } else {
                    log.info("[Resources] No 'memory' key in limits section. Default will be applied later.");
                }
            } else {
                log.info("[Resources] No 'limits' section found or not a Map. Defaults will be applied later.");
            }

            log.info("[Resources] Final calculated resources (before defaults/caps): {}", convertedResources);
            // Return map even if some keys are missing — defaults will be filled by applyResourceCaps
            return convertedResources;
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
            double value = Double.parseDouble(cpuValue);
            // If value is >= 1 and looks like millicores already (e.g. "325" from YAML Integer parsing), keep as-is
            // If value is < 1, treat as cores (e.g. "0.5" = 500m)
            if (value < 1) {
                return String.valueOf((int) (value * 1000));
            }
            // Bare number >= 1: treat as millicores directly (SnakeYAML may strip 'm' suffix)
            return String.valueOf((int) value);
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
        
        // Handle bare numbers (SnakeYAML may parse "7000Mi" as Integer 7000 if suffix is stripped,
        // or user may specify memory without unit) - treat as Mi
        try {
            int numericValue = Integer.parseInt(memoryValue);
            log.info("[Resources] Memory value '{}' has no unit suffix, treating as Mi", memoryValue);
            return String.valueOf(numericValue);
        } catch (NumberFormatException e) {
            // Not a number either
        }
        
        log.warn("Unsupported memory unit (must be 'Mi' or 'Gi'): {}", memoryValue);
        return null;
    }

    /**
     * Applies defaults for missing keys in the resource map.
     * This should ALWAYS be called regardless of the exception flag,
     * to ensure buildPayload never receives null values for required keys.
     *
     * Defaults:
     *   CPU Request: 250m
     *   Memory Request: 256Mi
     *   Memory Limit: 1024Mi (1Gi)
     */
    Map<String, String> applyResourceDefaults(Map<String, String> resources) {
        if (resources == null) {
            log.info("[ResourceDefaults] Resources is null (values.yaml fetch likely failed), no defaults to apply");
            return null;
        }
        if (resources.isEmpty()) {
            log.info("[ResourceDefaults] Resources is empty {{}}, defaults will be applied by buildPayload (Scenario 1)");
            return resources;
        }

        Map<String, String> result = new HashMap<>(resources);

        if (!result.containsKey("cpu")) {
            result.put("cpu", "250");
            log.info("[ResourceDefaults] CPU request missing, defaulting to 250m");
        }
        if (!result.containsKey("memory")) {
            result.put("memory", "256");
            log.info("[ResourceDefaults] Memory request missing, defaulting to 256Mi");
        }
        if (!result.containsKey("limitsMemory")) {
            result.put("limitsMemory", "1024");
            log.info("[ResourceDefaults] Memory limit missing, defaulting to 1024Mi (1Gi)");
        }

        log.info("[ResourceDefaults] After defaults: {}", result);
        return result;
    }

    /**
     * Applies caps to resource values. Only called when resourceExceptionEnabled is false.
     *
     * Caps:
     *   CPU Request: >= 300m → cap to 300m
     *   Memory Request: >= 4Gi (4096Mi) → cap to 4Gi
     *   Memory Limit: >= 6Gi (6144Mi) → cap to 6Gi
     */
    Map<String, String> applyResourceCaps(Map<String, String> resources) {
        if (resources == null) {
            log.info("[ResourceCaps] Resources is null, no caps to apply");
            return null;
        }
        if (resources.isEmpty()) {
            log.info("[ResourceCaps] Resources is empty {{}}, no caps to apply");
            return resources;
        }

        Map<String, String> capped = new HashMap<>(resources);

        // --- Cap CPU request: >= 300m → 300m ---
        try {
            int cpuMillis = Integer.parseInt(capped.get("cpu"));
            if (cpuMillis >= resourceCapCpuRequestMax) {
                log.info("[ResourceCaps] CPU request {}m >= cap {}m, capping to {}m", cpuMillis, resourceCapCpuRequestMax, resourceCapCpuRequestMax);
                capped.put("cpu", String.valueOf(resourceCapCpuRequestMax));
            } else {
                log.info("[ResourceCaps] CPU request {}m < cap {}m, keeping value", cpuMillis, resourceCapCpuRequestMax);
            }
        } catch (NumberFormatException e) {
            log.warn("[ResourceCaps] Invalid CPU value '{}', using default 250m", capped.get("cpu"));
            capped.put("cpu", "250");
        }

        // --- CPU Limit: if key exists in YAML, buildPayload sets it to null (no limit) ---
        if ("true".equals(capped.get("hasLimitsCpu"))) {
            log.info("[ResourceCaps] CPU limit key exists in YAML, will be set to null (no limit) by buildPayload");
        }

        // --- Cap Memory request: >= 4096Mi → 4096Mi ---
        try {
            int memoryMi = Integer.parseInt(capped.get("memory"));
            if (memoryMi >= resourceCapMemoryRequestMax) {
                log.info("[ResourceCaps] Memory request {}Mi >= cap {}Mi, capping to {}Mi", memoryMi, resourceCapMemoryRequestMax, resourceCapMemoryRequestMax);
                capped.put("memory", String.valueOf(resourceCapMemoryRequestMax));
            } else {
                log.info("[ResourceCaps] Memory request {}Mi < cap {}Mi, keeping value", memoryMi, resourceCapMemoryRequestMax);
            }
        } catch (NumberFormatException e) {
            log.warn("[ResourceCaps] Invalid memory value '{}', using default 256Mi", capped.get("memory"));
            capped.put("memory", "256");
        }

        // --- Cap Memory limit: >= 6144Mi → 6144Mi ---
        try {
            int limitsMemoryMi = Integer.parseInt(capped.get("limitsMemory"));
            if (limitsMemoryMi >= resourceCapMemoryLimitMax) {
                log.info("[ResourceCaps] Memory limit {}Mi >= cap {}Mi, capping to {}Mi", limitsMemoryMi, resourceCapMemoryLimitMax, resourceCapMemoryLimitMax);
                capped.put("limitsMemory", String.valueOf(resourceCapMemoryLimitMax));
            } else {
                log.info("[ResourceCaps] Memory limit {}Mi < cap {}Mi, keeping value", limitsMemoryMi, resourceCapMemoryLimitMax);
            }
        } catch (NumberFormatException e) {
            log.warn("[ResourceCaps] Invalid limits memory value '{}', using default 1024Mi", capped.get("limitsMemory"));
            capped.put("limitsMemory", "1024");
        }

        log.info("[ResourceCaps] Final capped resources: {}", capped);
        return capped;
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
                    log.warn("ArgoCD app {} - permission denied, marking as DEPLOYMENT_FAILED", appName);
                    return "DEPLOYMENT_FAILED";
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
                log.info("Application {} is degraded - DEPLOYMENT_FAILED", appName);
                return "DEPLOYMENT_FAILED";
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
                log.info("Application {} is healthy - DEPLOYED", appName);
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
                if (statusCode == 403 || statusCode == 404) {
                    result.put("status", "DEPLOYMENT_FAILED");
                    result.put("errorMessage", "ArgoCD app " + appName + " - HTTP " + statusCode);
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
                result.put("status", "DEPLOYMENT_FAILED");
                result.put("errorMessage", operationMessage != null && !operationMessage.isEmpty()
                    ? operationMessage : "Application health is Degraded. Check pod logs for details.");
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
                result.put("status", "DEPLOYED");
                return result;
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to check ArgoCD deployment status for {}", appName, e);
            return result;
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

}