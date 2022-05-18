package com.mb.dna.kube.client.main;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "KubeClient API", tags = { "kubeClientServices" })
@RequestMapping("/api")
@Slf4j
public class SampleController {

	@Autowired
	public KubeClientService kubeClient;
	
	@Autowired
	public NodePrintingReconciler reconciler;
	
	private static String KUBEFLOW_NAMESPACE = "kubeflow";
	
	public void doNothing() {
		try {
			ApiClient client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);
	        CoreV1Api api = new CoreV1Api();
	        V1PodList items = api.listNamespacedPod(KUBEFLOW_NAMESPACE,null, null, null, null, null, null, null, null, 10, false);
	        V1Pod minioPod = items.getItems().stream().filter(pod -> pod.getMetadata().getName().contains("minio")).findFirst().get();
	        V1PodSpec minioPodSpec = minioPod.getSpec();
	        minioPodSpec.getContainers().get(0).getEnv().forEach(x -> System.out.println("Environment name: " + x.getName() + " , value: " + x.getValue()));
	        
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> watch() {
		List<String> responses= new ArrayList<>();
		try {
			ApiClient client = Config.defaultClient();
			io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

	        CoreV1Api api = new CoreV1Api();

	        Watch<V1Namespace> watch = Watch.createWatch(
	                client,
	                api.listNamespaceCall(null, null, null, null, null, 5, null, null, null,  Boolean.TRUE, null),
	                new TypeToken<Watch.Response<V1Namespace>>(){}.getType());

	        for (Watch.Response<V1Namespace> item : watch) {
	            System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
	            responses.add("Item Type: "+ item.type + " Item Name: " + item.object.getMetadata().getName());
	        }
	        return responses;
		}catch(Exception e) {
			e.printStackTrace();
			return responses;
		}
	}
	
	@ApiOperation(value = " description", nickname = "method", notes = " description", response = NamespaceMetadataDto.class, tags = {
			"kubeClientServices", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = NamespaceMetadataDto.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/metadata/{namespace}", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<NamespaceMetadataDto> delete(
			@ApiParam(value = "Namespace for which metadata info is requested", required = true) @PathVariable("namespace") String namespace) {
		
		return new ResponseEntity<>(reconciler.reconcileResult(), HttpStatus.OK);
	}
}
