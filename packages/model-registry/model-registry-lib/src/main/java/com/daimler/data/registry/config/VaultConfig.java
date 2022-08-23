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

package com.daimler.data.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Configuration
public class VaultConfig {

	Logger LOGGER = LoggerFactory.getLogger(VaultConfig.class);

	@Value("${spring.cloud.vault.token}")
	private String vaultToken;

	@Value("${spring.cloud.vault.scheme}")
	private String vaultScheme;

	@Value("${spring.cloud.vault.host}")
	private String vaultHost;

	@Value("${spring.cloud.vault.port}")
	private String vaultPort;

	@Value("${spring.cloud.vault.vaultpath}")
	private String vaultPath;

	@Value("${spring.cloud.vault.mountpath}")
	private String mountPath;

	/**
	 * Write the secret at {@code path}
	 * 
	 * @param appId
	 * @param appKey
	 * @return String
	 */
	public String createAppKey(String appId, String appKey) {
		try {
			VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(),
					new TokenAuthentication(vaultToken));
			VaultResponse response = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).get(vaultPath);
			if (response != null && response.getData() != null && response.getData().get(appId) != null) {
				LOGGER.info("AppKey already exists for appID {} ,", appId);
				return response.getData().get(appId).toString();
			}
			Map<String, String> secMap = new HashMap<String, String>();
			secMap.put(appId, appKey);
			vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).patch(vaultPath, secMap);
			LOGGER.info("In createAppKey, App Key created successfully for appId {} ", appId);
			return appKey;
		} catch (Exception e) {
			LOGGER.error("Error occured {} while creating appkey for appId {} ", e.getMessage(), appId);
			throw e;
		}
	}

	/**
	 * validateAppKey
	 * <p>
	 * If given appKey is valid
	 * 
	 * @param appId
	 * @param appKey
	 * @return VaultResponse
	 */
	public VaultResponse validateAppKey(String appId, String appKey) {
		try {
			VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(),
					new TokenAuthentication(vaultToken));
			return vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).get(vaultPath);
		} catch (Exception e) {
			LOGGER.error("In validateAppKey, Error occured {} while validating appKey for appId {} ", e.getMessage(),
					appId);
			throw e;
		}
	}

	/**
	 * push host,port,scheme in VaultEndpoint
	 * 
	 * @return VaultEndpoint
	 */
	private VaultEndpoint getVaultEndpoint() {
		VaultEndpoint vaultEndpoint = new VaultEndpoint();
		vaultEndpoint.setScheme(vaultScheme);
		vaultEndpoint.setHost(vaultHost);
		vaultEndpoint.setPort(Integer.parseInt(vaultPort));
		return vaultEndpoint;
	}

}
