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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

 package com.daimler.data.application.config;

 import java.io.File;
  import java.io.IOException;
  import java.net.URI;
  import java.nio.file.Files;
  import java.util.Map;
 
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.context.annotation.Configuration;
 import org.springframework.vault.authentication.KubernetesAuthentication;
  import org.springframework.vault.authentication.KubernetesAuthenticationOptions;
  import org.springframework.vault.client.RestTemplateBuilder;
  import org.springframework.vault.client.VaultEndpoint;
  import org.springframework.vault.client.VaultHttpHeaders;
  import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
  import org.springframework.vault.core.VaultTemplate;
  import org.springframework.vault.support.VaultResponse;
  import org.springframework.web.client.RestOperations;
  import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
 
 @Configuration
 @Slf4j
 public class VaultConfig {
 
 
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
 
     /*
      * To add user in vault
      * 
      */
     public String addToVault(String dbName,Map<String, String> secMap) {
         // Adding user in vault
         try {
            VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
                  new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));

                  VaultResponse vaultResponse = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2)
                  .get(vaultPathUtility(dbName));
          if (vaultResponse == null) {
            vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).put(vaultPathUtility(dbName), secMap);
            return "success";
          }  
          return "Failed";    
         } catch (Exception e) {
            log.error("Error creating secret for dbName: {}", dbName, e);
            return "Failed";
         }
     }
 
     
     public VaultResponse getFromVault(String dbName) {         
 
         try {
            log.info("Validating dbName:{} in Vault.", dbName);
         VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
                  new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));
 
         log.info("Fetching details for user:{} from vault.",dbName);
         VaultResponse vaultResponse = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2)
                 .get(vaultPathUtility(dbName));
         if (vaultResponse != null && vaultResponse.getData() != null) {
             return vaultResponse;
         }
         return null;
         } catch (Exception e) {
            log.error("Error getting secret for dbName: {}", dbName, e);
            return null;
         }
     }


    public String deleteFromVault(String dbName) {
        log.info("Deleting secret for dbName:{} from Vault.", dbName);
    
        VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(),
            new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));

        try {
            vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2)
                     .delete(vaultPathUtility(dbName));
            log.info("Secret deleted successfully for dbName: {}", dbName);
            return "success";
        } catch (Exception e) {
            log.error("Error deleting secret for dbName: {}", dbName, e);
            return "Failed";
        }
    }

 
     /*
      * Return vault Path where value will be written.
      * 
      */
     private String vaultPathUtility(String dbName) {
         log.info("Processing vaultPathUtility");
         return vaultPath + "/" + dbName;
     }
 
      /*
       * push host,port,scheme in VaultEndpoint
       * 
       * @return VaultEndpoint
       */
      private VaultEndpoint getVaultEndpoint() {
          log.info("Processing getVaultEndpoint");
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
 