package com.daimler.data.dna.trino.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.stereotype.Component;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KubernetesClient {

	private CoreV1Api api;
	private ApiClient client;
	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCryptVersion.$2Y,10);
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
    
	
	public Boolean isKeyExists(String key) throws ApiException {
		Boolean isExists = false;
		V1ConfigMap configMap = api.readNamespacedConfigMap(trinoConfigMapName, trinoNamespace, "true");
		String currentValue = configMap.getData().get("password.db");
		if(currentValue==null) {
			currentValue="";
			Stream<String> lines = currentValue.lines();
			if(lines!=null) {
				isExists = lines.anyMatch(x-> x.contains(key+":"));
			}
		}
		return isExists;
	}
	
	//operation values - add, replace, remove
	public void operateRecordToConfigMap(String operation, String prevKey, String key, String value) throws ApiException {
		String jsonRequest = "[{ \"op\" : \"" + operation + "\", \"path\":\"/data/password.db/"+ key + "\", \"value\": \""+ value + "\"}]";
		V1ConfigMap configMap = api.readNamespacedConfigMap(trinoConfigMapName, trinoNamespace, "true");
		String currentValue = configMap.getData().get("password.db");
		if(currentValue==null) {
			currentValue="";
		}
		String updatedValue = "";
		if("add".equalsIgnoreCase(operation)) {
			updatedValue = currentValue + key + ":" + passwordEncoder.encode(value) + "\n";
		}
		if("replace".equalsIgnoreCase(operation)) {
			Stream<String> lines = currentValue.lines();
			List<String> linesAsList = lines.filter(x -> !x.contains(prevKey+":")).collect(Collectors.toList());
			String updatedLines = String.join("\n",linesAsList);
			updatedValue = updatedLines + "\n" + key + ":" + passwordEncoder.encode(value) + "\n";
		}
		if("remove".equalsIgnoreCase(operation)) {
			Stream<String> lines = currentValue.lines();
			List<String> linesAsList = lines.filter(x -> !x.contains(prevKey+":")).collect(Collectors.toList());
			String updatedLines = String.join("\n",linesAsList);
			updatedValue = updatedLines + "\n" ;
		}
		configMap.getData().put("password.db", updatedValue);
		api.replaceNamespacedConfigMap(trinoConfigMapName, trinoNamespace, configMap, null, null, null, null);
	}
	
	public void getConfigMaps() {
		GenericKubernetesApi configMapClient = new GenericKubernetesApi<>(V1ConfigMap.class, V1ConfigMapList.class, "", "v1", "configmaps", client);
		KubernetesApiResponse response = configMapClient.get(trinoNamespace, trinoConfigMapName);
		Object configMapData = response.getObject();
	}
	
}
