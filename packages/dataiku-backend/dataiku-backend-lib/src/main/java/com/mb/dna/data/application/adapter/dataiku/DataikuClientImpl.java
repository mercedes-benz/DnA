package com.mb.dna.data.application.adapter.dataiku;

import java.util.Optional;

import org.json.JSONObject;

import com.mb.dna.data.api.controller.exceptions.MessageDescription;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DataikuClientImpl implements DataikuClient {

	@Inject
	HttpClient client;
	
	@Inject
	DataikuClientConfig dataikuClientConfig;
	
	@Override
	public MessageDescription addUser(DataikuUserDto user) {
		try {
			String url =  dataikuClientConfig.getBaseuri() + dataikuClientConfig.getUsersUri() ;
			HttpRequest<DataikuUserDto> req = HttpRequest.POST(url,user).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
			HttpResponse<DataikuResponseDto> response = client.toBlocking().exchange(req,DataikuResponseDto.class);
			if(response!=null && response.getBody()!=null) {
				Optional<DataikuResponseDto> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("User {} onboarded on dataiku system with status {}",user.getLogin() , response.getStatus().toString());
				}
			}
			return null;
		}catch(Exception e) {
			log.error("Failed while onboarding user {} with exception {} ",user.getLogin(), e.getMessage());
			return new MessageDescription("Failed while on boarding " + user.getLogin() + " with exception " + e.getMessage());
		}
	}
	
	@Override
	public MessageDescription updateUser(DataikuUserDto user) {
		try {
			String url =  dataikuClientConfig.getBaseuri() + dataikuClientConfig.getUsersUri() + "/" + user.getLogin().toUpperCase();
			HttpRequest<DataikuUserDto> req = HttpRequest.PUT(url,user).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
			HttpResponse<DataikuResponseDto> response = client.toBlocking().exchange(req,DataikuResponseDto.class);
			if(response!=null && response.getBody()!=null) {
				Optional<DataikuResponseDto> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("User {} updated groups on dataiku system with status {}",user.getLogin() , response.getStatus().toString());
				}
			}
			return null;
		}catch(Exception e) {
			log.error("Failed while updating groups for user {} with exception {} ",user.getLogin(), e.getMessage());
			return new MessageDescription("Failed while on updating " + user.getLogin() + " groups with exception " + e.getMessage());
		}
	}
	
	@Override
	public DataikuUserDto getDataikuUser(String loginName){
		DataikuUserDto responseBody = new DataikuUserDto();
		String url = dataikuClientConfig.getBaseuri() + dataikuClientConfig.getUsersUri() + "/" + loginName;
		HttpRequest<?> req = HttpRequest.GET(url).header("Accept", "application/json")
		.header("Content-Type", "application/json")
		.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
		try {
			HttpResponse<DataikuUserDto> response = client.toBlocking().exchange(req,DataikuUserDto.class);
			if(response!=null && response.getBody()!=null) {
				responseBody = response.getBody().get();
			}
		}catch(Exception e) {
			log.error("Failed to fetch dataiku user for {} with exception {}", loginName, e.getMessage());
		}
		return responseBody;
	}

	@Override
	public MessageDescription updateScenario(String projectName) {
		try {
			String requestJson = dataikuClientConfig.getScenarioUpdateRequest();
			String updatedRequestJson = requestJson.replaceFirst("XXXXdefaultProjectNameXXXX", projectName);
			String url =  dataikuClientConfig.getBaseuri() + "/projects/" + dataikuClientConfig.getScenarioProjectKey() + "/scenarios/" + dataikuClientConfig.getScenarioId();
			log.info("url is {} and updatescenario json is {} " ,url,updatedRequestJson);
			HttpRequest<String> req = HttpRequest.PUT(url,updatedRequestJson)
			.header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic " + dataikuClientConfig.getAuth())
			;
			HttpResponse<DataikuResponseDto> response = client.toBlocking().exchange(req,DataikuResponseDto.class);
			if(response!=null && response.getBody()!=null) {
				Optional<DataikuResponseDto> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("Updated scenario for projectName {} with response {} ",projectName, responseBody.get().getMsg());
				}
			}
			return null;
		}catch(Exception e) {
			log.error("Failed while updating scenario for project {} with exception {} ",projectName, e.getMessage());
			return new MessageDescription("Failed while updating scenario for project " + projectName + " with exception " + e.getMessage());
		}
	}

	@Override
	public MessageDescription runScenario(String projectName) {
		try {
			String url =  dataikuClientConfig.getBaseuri() + "/projects/" + dataikuClientConfig.getScenarioProjectKey() + "/scenarios/" + dataikuClientConfig.getScenarioId() + "/run";
			HttpRequest<?> req = HttpRequest.POST(url,null).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
			HttpResponse<JSONObject> response = client.toBlocking().exchange(req,JSONObject.class);
			if(response!=null && response.getBody()!=null) {
				Optional<JSONObject> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("Ran updated scenario for projectName with response status {} ",projectName, response.getStatus().toString());
				}
			}
			return null;
		}catch(Exception e) {
			log.error("Failed while calling run scenario after update for project {} with exception {} ",projectName, e.getMessage());
			return new MessageDescription("Failed while calling run scenario for project " + projectName + " with exception " + e.getMessage());
		}
	}
	
	@Override
	public MessageDescription deleteProject(String projectName) {
		try {
			String url =  dataikuClientConfig.getBaseuri() + "/projects/" + projectName.toUpperCase();
			HttpRequest<?> req = HttpRequest.DELETE(url,null).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
			HttpResponse<DataikuResponseDto> response = client.toBlocking().exchange(req,DataikuResponseDto.class);
			if(response!=null && response.getBody()!=null) {
				Optional<DataikuResponseDto> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("Deleted project at dataiku successfully for key {} with response status {} ",projectName, response.getStatus().toString());
				}
			}
			return null;
		}catch(Exception e) {
			log.error("Failed while deleting dataiku project {} with exception {} ",projectName, e.getMessage());
			return new MessageDescription("Failed while deleting dataiku project " + projectName + " with exception " + e.getMessage());
		}
	}
	
}
