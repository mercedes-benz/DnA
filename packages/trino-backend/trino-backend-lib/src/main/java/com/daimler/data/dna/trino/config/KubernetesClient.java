package com.daimler.data.dna.trino.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.PatchUtils;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KubernetesClient {

	private CoreV1Api api;
	private ApiClient client;
	
	@Value("${trino.kubernetes.namespace}")
	private String trinoNamespace;
	
	@Value("${trino.kubernetes.configmap}")
	private String trinoConfigMapName;

	public KubernetesClient() {
		try {
			this.client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);
			this.api = new CoreV1Api();
			log.info("Got kubernetes java client and core api successfully");
		} catch (Exception e) {
			log.error("Error while getting kubernetes java client and core api successfully");
			e.printStackTrace();
		}
	}
    
	//operation values - add, replace, remove
	public void operateRecordToConfigMap(String operation, String key, String value) throws ApiException {
		String jsonRequest = "[{ \"op\" : \"" + operation + "\", \"path\":\"/data/password.db/"+ key + "\", \"value\": \""+ value + "\"}]";
		V1Patch body = new V1Patch(jsonRequest);
		PatchUtils.patch(V1ConfigMap.class,
						() -> api.patchNamespacedConfigMapCall(trinoConfigMapName, trinoNamespace, body, null, null, null, null, null, null),
						V1Patch.PATCH_FORMAT_JSON_PATCH,
						api.getApiClient());
	}
	
	public void getConfigMaps() {
		GenericKubernetesApi configMapClient = new GenericKubernetesApi<>(V1ConfigMap.class, V1ConfigMapList.class, "", "v1", "configmaps", client);
		KubernetesApiResponse response = configMapClient.get(trinoNamespace, trinoConfigMapName);
		Object configMapData = response.getObject();
	}
	
}
