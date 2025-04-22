/* LICENSE START
 *
 * MIT License
 *
 * Copyright (c) 2019 Daimler TSS GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * LICENSE END
 */

 package com.daimler.data.application.config;

 import com.daimler.data.dto.vault.VaultDTO;
 import com.daimler.data.dto.vault.VaultGenericResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.vault.authentication.KubernetesAuthentication;
 import org.springframework.vault.authentication.KubernetesAuthenticationOptions;
 import org.springframework.vault.client.VaultEndpoint;
 import org.springframework.vault.client.VaultHttpHeaders;
 import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
 import org.springframework.vault.core.VaultTemplate;
 import org.springframework.vault.support.VaultResponse;
 import org.springframework.vault.client.RestTemplateBuilder;
 import org.springframework.web.client.RestOperations;
 import org.springframework.web.client.RestTemplate;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.nio.file.Files;
 import java.util.HashMap;
 import java.util.Map;
 
 @Configuration
 public class VaultConfig {
 
     Logger LOGGER = LoggerFactory.getLogger(VaultConfig.class);
 
     @Value("${spring.cloud.vault.vaultpath}")
     private String vaultPath;
 
     @Value("${spring.cloud.vault.mountpath}")
     private String mountPath;
 
     @Value("${spring.cloud.vault.uri}")
      private String vaultUri;
     
      @Value("${spring.cloud.vault.authentication}")
      private String authType;
 
     @Value("${spring.cloud.vault.kubernetes.kubernetes-path}")
      private String kubernetesMountPath;
  
      @Value("${spring.cloud.vault.kubernetes.role}")
      private String kubernetesLoginRole;
  
      @Value("${spring.cloud.vault.kubernetes.service-account-token-file}")
      private String kubernetesSATokenPath;
  
      @Value("${spring.cloud.vault.namespace}")
      private String namespace;
 
     /**
      * Write the apikey at {@code path}
      *
      * @param appId
      * @param apiKey
      * @return VaultAdapterGenericResponse
      */
     public VaultGenericResponse createApiKey(String appId, String apiKey) {
         try {
              VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
                  new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));
 
             VaultResponse response = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).get(vaultPath);
             if (response != null && response.getData() != null && response.getData().get(appId) != null) {
                 LOGGER.info("AppKey already exists for appID {} ,", appId);
                 return new VaultGenericResponse("500", "AppKey already exists for appID", new VaultDTO(appId, apiKey));
             }
 
             Map<String, String> secMap = new HashMap<String, String>();
             secMap.put(appId, apiKey);
             vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).patch(vaultPath, secMap);
             LOGGER.info("In createAppKey, App Key created successfully for appId {} ", appId);
             return new VaultGenericResponse("200", "Api key created successfully", new VaultDTO(appId, apiKey));
         } catch (Exception e) {
             LOGGER.error("Error occurred {} while creating apikey for appId {} ", e.getMessage(), appId);
             return new VaultGenericResponse("500", e.getMessage(), null);
         }
     }
 
     /**
      * To update the secret at {@code path}
      *
      * @param appId
      * @param apiKey
      * @return VaultAdapterGenericResponse
      */
     public VaultGenericResponse updateApiKey(String appId, String apiKey) {
         try {
             VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
             new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));
 
             Map<String, String> secMap = new HashMap<String, String>();
             secMap.put(appId, apiKey);
             vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).patch(vaultPath, secMap);
             LOGGER.info("In createAppKey, App Key updated successfully for appId {} ", appId);
             return new VaultGenericResponse("200", "Api key updated successfully", new VaultDTO(appId, apiKey));
         } catch (Exception e) {
             LOGGER.error("Error occurred {} while updating apikey for appId {} ", e.getMessage(), appId);
             return new VaultGenericResponse("500", e.getMessage(), null);
         }
     }
 
     /**
      * validateApiKey
      * <p>
      * If given apikey is valid
      *
      * @param appId
      * @param apiKey
      * @return VaultAdapterGenericResponse
      */
     public VaultGenericResponse validateApiKey(String appId, String apiKey) {
         try {
            VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
            new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));
             VaultResponse vaultresponse = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).get(vaultPath);
             if (vaultresponse != null && vaultresponse.getData() != null && vaultresponse.getData().get(appId) != null && vaultresponse.getData().get(appId).equals(apiKey)) {
                 LOGGER.debug("In validateApiKey, Api key is valid for appID {} , returning", appId);
                 return new VaultGenericResponse("200", "Valid api key", new VaultDTO(appId, apiKey));
             } else {
                 LOGGER.debug("In validateApiKey, Invalid API key for appId {} , returning", appId);
                 return new VaultGenericResponse("404", "Invalid api key", null);
             }
 
         } catch (Exception e) {
             LOGGER.error("In validateApiKey, Error occurred {} while validating apiKey for appId {} ", e.getMessage(), appId);
             return new VaultGenericResponse("500", e.getMessage(), null);
         }
     }
 
     /**
      * Return data<k,v> from {@code path}
      *
      * @param appId
      * @return VaultResponse
      */
     public String getApiKey(String appId) {
         try {
             String apiKey = null;
             VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
             new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));
 
             VaultResponse response = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).get(vaultPath);
             if (response != null) {
                 apiKey = (String) response.getData().get(appId);
             }
             return apiKey;
         } catch (Exception e) {
             LOGGER.error("In getApiKeys, Error occurred while fetching API Key for appID {} Error is {}", appId, e.getMessage());
             return null;
         }
     }
 
     /**
      * Return vault Path where value will be written
      *
      * @param appId
      * @return
      */
     private String vaultPathUtility(String appId) {
         return vaultPath + "/" + appId;
     }
 
     /**
      * push host,port,scheme in VaultEndpoint
      *
      * @return VaultEndpoint
      */
     private VaultEndpoint getVaultEndpoint() {
         LOGGER.debug("Processing getVaultEndpoint");
         VaultEndpoint vaultEndpoint = VaultEndpoint.from(URI.create(vaultUri));
         return vaultEndpoint;
     }
 
     private KubernetesAuthenticationOptions getK8sOptions() {
         String serviceTokenCandidate;
         try {
             serviceTokenCandidate = new String(Files.readAllBytes(new File(kubernetesSATokenPath).toPath()));
             KubernetesAuthenticationOptions options = KubernetesAuthenticationOptions.builder()
                         .jwtSupplier(() -> serviceTokenCandidate)
                         .role(kubernetesLoginRole)
                         .path(kubernetesMountPath)
                         .build();
                         return options;
         } catch (IOException e) {
             throw new RuntimeException("Failed to read the Kubernetes service account token", e);
         }
     }
     
 /**
      * Return RestOperations
      * 
      * @return RestOperations
      */
     private RestOperations getrestOperations(VaultEndpoint vaultEndpoint){
         RestTemplate a = RestTemplateBuilder.builder().endpoint(vaultEndpoint)
         .defaultHeader(VaultHttpHeaders.VAULT_NAMESPACE,namespace)
         .build();
         return a;
         
     }
 }
 
 