package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.DeploymentManageDto;
import com.daimler.data.dto.WorkbenchManageDto;
import com.daimler.data.util.ConstantsUtility;

@Component
public class CodeServerClient {
	
	private static Logger LOGGER = LoggerFactory.getLogger(CodeServerClient.class);

	@Value("${codeServer.gitjob.deployuri}")
	private String codeServerGitJobDeployUri;
	
	@Value("${codeServer.gitjob.manageuri}")
	private String codeServerGitJobManageUri;
	
	@Value("${codeServer.gitjob.pat}")
	private String personalAccessToken;
	
	@Value("${codeServer.base.uri}")
	private String codeServerBaseUri;
	
	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;
	
	@Autowired
	RestTemplate restTemplate;
	

	
	public String toDeployType(String recipeId) {
		String recipeType = "";
		String deployType = "";
		if(recipeId!=null)
			recipeType = recipeId.toLowerCase();
		switch(recipeType) {
			case "springboot":  deployType = ConstantsUtility.SPRINGBOOT; break;
			case "py-fastapi" : deployType = ConstantsUtility.PYFASTAPI; break;
			case "react":  deployType = ConstantsUtility.REACT; break;
			case "angular" : deployType = ConstantsUtility.ANGULAR; break;
			case "quarkus" : deployType = ConstantsUtility.QUARKUS; break;
			case "micronaut" : deployType = ConstantsUtility.MICRONAUT; break;
			case "public-dna-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-frontend" : deployType = ConstantsUtility.PUBLIC; break;			
			case "public-dna-report-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-codespace-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-malware-scanner" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-storage-mfe" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-storage-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-chronos-mfe" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-chronos-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-data-product-mfe" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-data-product-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-dss-mfe" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-dataiku-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-airflow-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-modal-registry-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-trino-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-nass" : deployType = ConstantsUtility.PUBLIC; break;			
			case "public-dna-authenticator-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-matomo-mfe" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-matomo-backend" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-dna-datalake-mfe" : deployType = ConstantsUtility.PUBLIC; break;
			case "public-user-defined" : deployType = ConstantsUtility.PUBLIC; break;
			case "private-user-defined" : deployType = ConstantsUtility.PUBLIC; break;
			case "bat-frontend" : deployType = ConstantsUtility.BAT_FRONTEND; break;
			case "bat-backend" : deployType = ConstantsUtility.BAT_BACKEND; break;
			
			default: deployType = ConstantsUtility.DEFAULT; break;
		}
		return deployType;
	}
	
	public GenericMessage manageWorkBench(WorkbenchManageDto manageDto) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);
			HttpEntity<WorkbenchManageDto> entity = new HttpEntity<WorkbenchManageDto>(manageDto,headers);
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(codeServerGitJobManageUri, HttpMethod.POST, entity, String.class);
			if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode()!=null) {
				if(manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while performing {} action for codeServer workbench for user {} ", manageDto.getInputs().getAction(), manageDto.getInputs().getShortid());
				}
				else {
					LOGGER.info("Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}", manageDto.getInputs().getAction(), manageDto.getInputs().getShortid(), manageWorkbenchResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from codeServer Initialize : " + manageWorkbenchResponse.getBody() + " Response Code is : " + manageWorkbenchResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Error occured while calling codeServer manage workbench for user {} and action {} with exception {} ", manageDto.getInputs().getAction(), manageDto.getInputs().getShortid(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}
	
	public GenericMessage manageDeployment(DeploymentManageDto deployDto) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);
			HttpEntity<DeploymentManageDto> entity = new HttpEntity<DeploymentManageDto>(deployDto,headers);
			ResponseEntity<String> manageDeploymentResponse = restTemplate.exchange(codeServerGitJobDeployUri, HttpMethod.POST, entity, String.class);
			if (manageDeploymentResponse != null && manageDeploymentResponse.getStatusCode()!=null) {
				if(manageDeploymentResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while performing {} action for codeServer workbench for user {} ", deployDto.getInputs().getAction(), deployDto.getInputs().getShortid());
				}
				else {
					LOGGER.info("Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}", deployDto.getInputs().getAction(), deployDto.getInputs().getShortid(),manageDeploymentResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from codeServer Initialize : " + manageDeploymentResponse.getBody() + " Response Code is : " + manageDeploymentResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Error occured while calling codeServer manage workbench for user {} and action {} with exception {} ", deployDto.getInputs().getAction(), deployDto.getInputs().getShortid(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception " + e.getMessage());
			errors.add(error);
			e.printStackTrace();
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}
	
	
	
}
