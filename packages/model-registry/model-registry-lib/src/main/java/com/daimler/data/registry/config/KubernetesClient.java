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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.data.dto.MinioSecretMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1EnvFromSource;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KubernetesClient {
	
	@Value("${kubeflow.namespace}")
	private String kubeflowNamespace;
	
	@Value("${kubeflow.secret.name}")
	private String kubeflowSecret;
	
	private CoreV1Api api;
	private ApiClient client;
	
	public KubernetesClient() {
		try {
			client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);
	        CoreV1Api api = new CoreV1Api();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public V1PodList getPods(String namespace) {
		try {
			return api.listNamespacedPod(namespace,null, null, null, null, null, null, null, null, 10, false);
		} catch (Exception e) {
			log.error("Error occured while getting pods list using namespace. Exception is :  {}", e.getMessage());
			return null;
		}
		
	}
	
//	public void getKubeflowMinioSpec() throws Exception{
//		V1PodList items = api.listNamespacedPod("", null, null, null, null, null, null, null, null,
//				10, false);
//		V1Pod minioPod = items.getItems().stream().filter(pod -> pod.getMetadata().getName().contains("minio"))
//				.findFirst().get();
//		V1PodSpec minioPodSpec = minioPod.getSpec();
//		V1Container minioContainer = minioPodSpec.getContainers().stream()
//				.filter(container -> container.getName().contains("minio")).findFirst().get();
//		List<V1ContainerPort> ports = minioContainer.getPorts();
//		log.info(ports.get(0).getHostIP().toString());
//		log.info(ports.get(0).getHostPort().toString());
//		
//		minioContainer.getEnv()
//				.forEach(x -> log.info("Environment name: " + x.getName() + " , value: " + x.getValue()));
//		String podIp = minioPod.getStatus().getPodIP();
//		LOG.info(minioPod.toString());
//		LOG.info(minioPodSpec.toString());
//		LOG.info(minioContainer.get);
//	}
//	

//	V1PodList items = api.listNamespacedPod(KUBEFLOW_NAMESPACE, null, null, null, null, null, null, null, null,
//			10, false);
//	V1Pod minioPod = items.getItems().stream().filter(pod -> pod.getMetadata().getName().contains("minio"))
//			.findFirst().get();
//	V1PodSpec minioPodSpec = minioPod.getSpec();
//	V1Container minioContainer = minioPodSpec.getContainers().stream()
//			.filter(container -> container.getName().contains("minio")).findFirst().get();
//	minioContainer.getEnv()
//			.forEach(x -> LOG.info("Environment name: " + x.getName() + " , value: " + x.getValue()));
//	String podIp = minioPod.getStatus().getPodIP();
	
	
	public V1PodSpec getPodSpec(V1Pod pod) {
		try {
			return pod.getSpec();
		} catch (Exception e) {
			log.error("Error occured while getting spec for given pod. Exception is :  {}", e.getMessage());
			return null;
		}
	}

	public List<V1Container> getContainers(V1PodSpec podSpec){
		try {
			return podSpec.getContainers();
		} catch (Exception e) {
			log.error("Error occured while getting containers list for given podspec. Exception is :  {}", e.getMessage());
			return null;
		}
	}
	
	public List<V1EnvVar> getEnviromnetVariables(V1Container container){
    	try {
			return container.getEnv();
		} catch (Exception e) {
			log.error("Error occured while getting environment variables. Exception is :  {}", e.getMessage());
			return null;
		}
    }
	
	public List<V1EnvFromSource> getEnvironmentVariablesFromSource(V1Container container){
		try {
			return container.getEnvFrom();
		} catch (Exception e) {
			log.error("Error occured while getting environment variables from source. Exception is :  {}", e.getMessage());
			return null;
		}
	}
	
	public V1SecretList getSecretList(String namespace){
		try {
			return api.listNamespacedSecret(namespace, "true", null, null, null, null, null, null, null, null, false);
		} catch (Exception e) {
			log.error("Error occured while getting secret list. Exception is :  {}", e.getMessage());
			return null;
		}
	}
	
	public V1Secret getSecret(String secretName, String namespace){
		try {
			return api.readNamespacedSecret(secretName, namespace, "true" );
		} catch (Exception e) {
			log.error("Error occured while getting secret list. Exception is :  {}", e.getMessage());
			return null;
		}
	}
	
	public V1ObjectMeta getSecretMetaData(V1Secret secret) {
		try {
			return secret.getMetadata();
		} catch (Exception e) {
			log.error("Error occured while getting secret metadata V1ObjectMeta. Exception is :  {}", e.getMessage());
			return null;
		}
	}
	
	public MinioSecretMetadata getJsonDataFromSecretMeta(V1ObjectMeta meta) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> annotations = meta.getAnnotations();
		String jsonData = annotations.getOrDefault("kubectl.kubernetes.io/last-applied-configuration", "no value found").toString();
		MinioSecretMetadata jsonMetadata = new MinioSecretMetadata();
		try{
			jsonMetadata = mapper.readValue(jsonData, MinioSecretMetadata.class);
		}catch(Exception e) {
			log.error("Got error while fetching or parsing json data from secret meta. Exception is {} ",e.getMessage());
		}
		return jsonMetadata;
	}
	
	public MinioSecretMetadata getKubeflowMinioSpec() {
		V1Secret minioSecrets = this.getSecret(kubeflowSecret, kubeflowNamespace);
		MinioSecretMetadata secretDetails = new MinioSecretMetadata();
		try {
			if(minioSecrets!=null) {
				V1ObjectMeta minioKubeflowSecretMetadata = this.getSecretMetaData(minioSecrets);
				if(minioKubeflowSecretMetadata!=null) {
					secretDetails = this.getJsonDataFromSecretMeta(minioKubeflowSecretMetadata);
				}
			}
			V1PodList items = api.listNamespacedPod(kubeflowNamespace, null, null, null, null, null, null, null, null,
					10, false);
			V1Pod minioPod = items.getItems().stream().filter(pod -> pod.getMetadata().getName().contains("minio"))
					.findFirst().get();
			V1PodSpec minioPodSpec = minioPod.getSpec();
			V1Container minioContainer = minioPodSpec.getContainers().stream()
					.filter(container -> container.getName().contains("minio")).findFirst().get();
			List<V1ContainerPort> ports = minioContainer.getPorts();
			secretDetails.setPort(ports.get(0).getContainerPort().toString());
			secretDetails.setHost(minioPod.getStatus().getPodIP());
		}catch(Exception e) {
			log.error("Exception occurred while getting kubeflow specific minio details. Exception is {} ", e.getMessage());
		}
		return secretDetails;
	}
	
}
