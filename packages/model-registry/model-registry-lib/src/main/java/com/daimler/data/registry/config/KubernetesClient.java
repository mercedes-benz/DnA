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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.data.dto.MinioSecretMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1HTTPIngressPath;
import io.kubernetes.client.openapi.models.V1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressBackend;
import io.kubernetes.client.openapi.models.V1IngressRule;
import io.kubernetes.client.openapi.models.V1IngressServiceBackend;
import io.kubernetes.client.openapi.models.V1IngressSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceBackendPort;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KubernetesClient {

	@Value("${kubeflow.namespace}")
	private String kubeflowNamespace;

	@Value("${kubeflow.secret.name}")
	private String kubeflowSecret;

	@Value("${model.host}")
	private String host;

	@Value("${model.certManager}")
	private String certManager;

	private CoreV1Api api;
	private ApiClient client;

	private NetworkingV1Api networkingV1beta1Api;

	public KubernetesClient() {
		try {
			this.client = Config.defaultClient();

			Configuration.setDefaultApiClient(client);
			this.api = new CoreV1Api();
			this.networkingV1beta1Api = new NetworkingV1Api();
			log.info("Got kubernetes java client and core api successfully");
		} catch (Exception e) {
			log.error("Error while getting kubernetes java client and core api successfully");
			e.printStackTrace();
		}
	}

	public V1Secret getSecret(String secretName, String namespace) {
		try {
			return api.readNamespacedSecret(secretName, namespace, "true");
		} catch (ApiException ex) {
			log.error("Exception occurred while getting secret list. Exception is {} ", ex.getResponseBody());
			return null;
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
		Map<String, String> annotations = meta.getAnnotations();
		String jsonData = annotations.getOrDefault("kubectl.kubernetes.io/last-applied-configuration", "no value found")
				.toString();
		MinioSecretMetadata jsonMetadata = new MinioSecretMetadata();
		try {
			jsonMetadata = mapper.readValue(jsonData, MinioSecretMetadata.class);
		} catch (Exception e) {
			log.error("Got error while fetching or parsing json data from secret meta. Exception is {} ",
					e.getMessage());
		}
		return jsonMetadata;
	}

	public MinioSecretMetadata getKubeflowMinioSpec() {
		V1Secret minioSecrets = this.getSecret(kubeflowSecret, kubeflowNamespace);
		MinioSecretMetadata secretDetails = new MinioSecretMetadata();
		try {
			if (minioSecrets != null) {
				V1ObjectMeta minioKubeflowSecretMetadata = this.getSecretMetaData(minioSecrets);
				if (minioKubeflowSecretMetadata != null) {
					secretDetails = this.getJsonDataFromSecretMeta(minioKubeflowSecretMetadata);
					log.info("Successfully fetched secretDetails");
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
			String port = ports.get(0).getContainerPort().toString();
			secretDetails.setPort(port);
			String host = minioPod.getStatus().getPodIP();
			secretDetails.setHost(host);
			log.info("Successfully fetched secretDetails of minio running at host {} and port {}", host, port);
		} catch (Exception e) {
			log.error("Exception occurred while getting kubeflow specific minio details. Exception is {} ",
					e.getMessage());
		}
		return secretDetails;
	}

	public V1Service getModelService(String metaDataNamespace, String backendServiceName) throws ApiException {
		V1Service service = null;
		V1ServiceList serviceList = api.listNamespacedService(metaDataNamespace, "true", null, null, null, null, null,
				null, null, 10, false);
		if (serviceList != null && !ObjectUtils.isEmpty(serviceList.getItems())) {
			service = serviceList.getItems().stream()
					.filter(item -> item.getMetadata().getName().equals(backendServiceName)).findFirst().get();
		}
		return service;
	}

	public void getUri(String metaDataNamespace, String metaDataName, String backendServiceName, String path)
			throws ApiException {
		V1Ingress ingress = new V1Ingress();
		V1ObjectMeta metaData = new V1ObjectMeta();
		Map<String, String> annotations = new HashMap<>();
		annotations.put("traefik.frontend.rule.type", "PathPrefix");
		annotations.put("kubernetes.io/ingress.class", "traefik");
		annotations.put("traefik.ingress.kubernetes.io/router.tls", "true");
		annotations.put("traefik.ingress.kubernetes.io/router.entrypoints", "websecure");
		annotations.put("cert-manager.io/cluster-issuer", certManager);
		metaData.setAnnotations(annotations);
		metaData.setName(metaDataName);
		metaData.setNamespace(metaDataNamespace);

		V1ServiceBackendPort port = new V1ServiceBackendPort();
		port.setNumber(80);
		V1IngressServiceBackend v1IngressServiceBackend = new V1IngressServiceBackend();
		v1IngressServiceBackend.setName(backendServiceName);
		v1IngressServiceBackend.setPort(port);
		V1IngressBackend v1IngressBackend = new V1IngressBackend();
		v1IngressBackend.setService(v1IngressServiceBackend);

		V1HTTPIngressPath v1HTTPIngressPath = new V1HTTPIngressPath();
		v1HTTPIngressPath.setPath(path);
		v1HTTPIngressPath.setPathType("Prefix");
		v1HTTPIngressPath.setBackend(v1IngressBackend);

		List<V1HTTPIngressPath> paths = new ArrayList<>();
		paths.add(v1HTTPIngressPath);
		V1HTTPIngressRuleValue v1HTTPIngressRuleValue = new V1HTTPIngressRuleValue();
		v1HTTPIngressRuleValue.setPaths(paths);

		V1IngressRule ingressRule = new V1IngressRule();
		ingressRule.setHost(host);
		ingressRule.setHttp(v1HTTPIngressRuleValue);

		List<V1IngressRule> rules = new ArrayList<>();
		rules.add(ingressRule);
		V1IngressSpec ingressSpec = new V1IngressSpec();
		ingressSpec.setRules(rules);

		ingress.setApiVersion("networking.k8s.io/v1");
		ingress.setKind("Ingress");
		ingress.setMetadata(metaData);
		ingress.setSpec(ingressSpec);
		log.info("Ingress yaml created: {} ", ingress.toString());
		networkingV1beta1Api.createNamespacedIngress(metaDataNamespace, ingress, null, null, null, null);
	}
}
