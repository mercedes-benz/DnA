package com.daimler.data.application.config;

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
import com.daimler.data.dto.CodeServerWorkBenchCreateDto;
import com.daimler.data.dto.CodeServerWorkBenchInputDto;

@Component
public class CodeServerClient {
	
	private static Logger LOGGER = LoggerFactory.getLogger(AVScannerClient.class);

	@Value("${codeServer.gitjob.uri}")
	private String codeServerGitJobUri;
	
	@Value("${codeServer.git.pat}")
	private String personalAccessToken;
	
	@Value("${codeServer.base.uri}")
	private String codeServerBaseUri;
	
	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;
	
	private static String workBenchCreateAction = "create";
	
	private static String workBenchDefaultType = "default";
	private static String workBenchMsType = "microservice";
	
	@Autowired
	RestTemplate restTemplate;
	
	private String mapRecipe(String recipeId) {
		String type= workBenchDefaultType;
		switch (recipeId) {
		case "ms-springboot":
			type = workBenchMsType;
			break;
		default:
			type= workBenchDefaultType;
			break;
		}
		return type;
	}
	
	public HttpStatus pollWorkBenchStatus(String userId) {
		
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			
			HttpEntity entity = new HttpEntity<>(headers);
			String url = codeServerBaseUri+"/"+userId+"/default/login";
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				LOGGER.info("Success while polling codeServer workbench status for user {} ", userId);
				return response.getStatusCode();
			}
		} catch (Exception e) {
			LOGGER.error("Error occured while polling codeServer workbench status for user {} with exception {} ", userId, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	public GenericMessage createWorkbench(String userId, String password, String type) {
		GenericMessage createRespone = new GenericMessage();
		String status = "Failed";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);

			CodeServerWorkBenchCreateDto createDto = new CodeServerWorkBenchCreateDto();
			CodeServerWorkBenchInputDto inputDto = new CodeServerWorkBenchInputDto();
			inputDto.setAction(workBenchCreateAction);
			inputDto.setPassword(password);
			inputDto.setShortid(userId);
			if(type==null) {
				type = workBenchDefaultType;
			}
			inputDto.setType(this.mapRecipe(type));
			createDto.setInputs(inputDto);
			createDto.setRef(codeServerEnvRef);
			
			HttpEntity<CodeServerWorkBenchCreateDto> entity = new HttpEntity<CodeServerWorkBenchCreateDto>(createDto,headers);
			ResponseEntity<String> response = restTemplate.exchange(codeServerGitJobUri, HttpMethod.POST, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				if(response.getStatusCode().is2xxSuccessful()) {
					status = "Success";
					LOGGER.info("Success while creating codeServer workbench status for user {} ", userId);
				}
				else {
					LOGGER.info("Warnings while polling codeServer workbench status for user {} with status code {}", userId,response.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from codeServer Initialize : " + response.getBody() + " Response Code is : " + response.getStatusCodeValue());
					warnings.add(warning);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Error occured while calling codeServer Intialize workbench for user {} with exception {} ", userId, e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while intializing codeserver workbench with exception " + e.getMessage());
			errors.add(error);
		}
		
		createRespone.setSuccess(status);
		createRespone.setWarnings(warnings);
		createRespone.setErrors(errors);
		
		return createRespone;
	}
	
}
