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
 import java.util.HashMap;
 import java.util.Map;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
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

	/*
	 * To add user in vault
	 * 
	 */
	public String addUserVault(String userId, String userSecretKey) {
		// Adding user in vault
		VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
				 new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));
		Map<String, String> secMap = new HashMap<String, String>();
		secMap.put(userId, userSecretKey);
		vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).put(vaultPathUtility(userId), secMap);
		return userSecretKey;
	}

	
	public String validateUserInVault(String userId) {
		String userSecretKey = "";

		LOGGER.debug("Validating user:{} in Vault.", userId);
		VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), 
				 new KubernetesAuthentication(this.getK8sOptions(), this.getrestOperations(this.getVaultEndpoint())));

		LOGGER.debug("Fetching details for user:{} from vault.",userId);
		VaultResponse vaultResponse = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2)
				.get(vaultPathUtility(userId));
		if (vaultResponse != null && vaultResponse.getData().get(userId) != null) {
			LOGGER.debug("Got credential for user:{} from vault",userId);
			userSecretKey = vaultResponse.getData().get(userId).toString();
		}
		return userSecretKey;
	}

	/*
	 * Return vault Path where value will be written.
	 * 
	 */
	private String vaultPathUtility(String userId) {
		LOGGER.debug("Processing vaultPathUtility");
		return vaultPath + "/" + userId;
	}

	 /*
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
