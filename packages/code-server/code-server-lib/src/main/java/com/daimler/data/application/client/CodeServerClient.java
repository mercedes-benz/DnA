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
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.dto.WorkBenchActionRequestDto;
import com.daimler.data.dto.WorkBenchBaseInputDto;
import com.daimler.data.dto.WorkBenchInputDto;

@Component
public class CodeServerClient {
	
	private static Logger LOGGER = LoggerFactory.getLogger(CodeServerClient.class);

	@Value("${codeServer.gitjob.deployuri}")
	private String codeServerGitJobDeployUri;
	
	@Value("${codeServer.gitjob.manageuri}")
	private String codeServerGitJobManageUri;
	
	@Value("${codeServer.git.pat}")
	private String personalAccessToken;
	
	@Value("${codeServer.base.uri}")
	private String codeServerBaseUri;
	
	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;
	
	@Autowired
	RestTemplate restTemplate;
	
	public HttpStatus pollWorkBenchStatus(String userId, String wsid) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			
			HttpEntity entity = new HttpEntity<>(headers);
			String url = codeServerBaseUri+"/"+userId+"/"+wsid+"/default/login";
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
	
	public GenericMessage performWorkBenchActions(String action,CodeServerWorkspace workspaceDetails) {
		GenericMessage respone = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		String userId = "";
		String password = "";
		String type = "";
		String environment = "";
		String wsid = "";
		String codeServerGitJobUri = codeServerGitJobDeployUri;
		if(workspaceDetails!=null) {
			userId = workspaceDetails.getOwner().toLowerCase();
			password = workspaceDetails.getPassword();
			if(password==null)
				password = "";
			type = workspaceDetails.getRecipeId();
			environment = workspaceDetails.getEnvironment();
			wsid = workspaceDetails.getName();
		}
		
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);

			WorkBenchActionRequestDto requestDto = null;
			if(!action.equalsIgnoreCase("deploy") && !action.equalsIgnoreCase("undeploy")) {
				codeServerGitJobUri = codeServerGitJobManageUri;
				requestDto = new WorkBenchActionRequestDto<WorkBenchInputDto>();
				WorkBenchInputDto inputDto = new WorkBenchInputDto();
				inputDto.setAction(action);
				inputDto.setShortid(userId);
				inputDto.setEnvironment(environment);
				inputDto.setWsid(wsid);
				inputDto.setPassword(password);
				inputDto.setType(type);
				requestDto.setInputs(inputDto);
			}else {
				requestDto = new WorkBenchActionRequestDto<WorkBenchBaseInputDto>();
				WorkBenchBaseInputDto baseInputDto = new WorkBenchBaseInputDto();
				baseInputDto.setAction(action);
				baseInputDto.setShortid(userId);
				baseInputDto.setEnvironment(environment);
				baseInputDto.setWsid(wsid);
				requestDto.setInputs(baseInputDto);
			}
			requestDto.setRef(codeServerEnvRef);
			HttpEntity<WorkBenchActionRequestDto> entity = new HttpEntity<WorkBenchActionRequestDto>(requestDto,headers);
			ResponseEntity<String> response = restTemplate.exchange(codeServerGitJobUri, HttpMethod.POST, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				if(response.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while performing {} action for codeServer workbench for user {} ", action, userId);
				}
				else {
					LOGGER.info("Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}", action, userId,response.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from codeServer Initialize : " + response.getBody() + " Response Code is : " + response.getStatusCodeValue());
					warnings.add(warning);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Error occured while calling codeServer manage workbench for user {} and action {} with exception {} ", userId, action, e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception " + e.getMessage());
			errors.add(error);
		}
		respone.setSuccess(status);
		respone.setWarnings(warnings);
		respone.setErrors(errors);
		
		return respone;
	}
	
	
}
