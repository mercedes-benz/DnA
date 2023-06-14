package com.mb.dna.data.application.adapter.dataiku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.reactivestreams.Publisher;

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
	public MessageDescription addUser(DataikuUserDto user, String cloudProfile) {
		try {
			String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
			String url =  baseUri + dataikuClientConfig.getUsersUri() ;
			String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
			HttpRequest<DataikuUserDto> req = HttpRequest.POST(url,user).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+apiToken);
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
	public MessageDescription updateUser(DataikuUserDto user, String cloudProfile) {
		try {
			String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
			String url =   baseUri  + dataikuClientConfig.getUsersUri() + "/" + user.getLogin().toUpperCase();
			String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
			log.info("Updating user {} with groups {} ", user.getLogin(), Arrays.toString(user.getGroups().toArray()));
			HttpRequest<DataikuUserDto> req = HttpRequest.PUT(url,user).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+apiToken);
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
	public DataikuUserDto getDataikuUser(String loginName, String cloudProfile){
		DataikuUserDto responseBody = new DataikuUserDto();
		String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
		String url =   baseUri + dataikuClientConfig.getUsersUri() + "/" + loginName;
		String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
		HttpRequest<?> req = HttpRequest.GET(url).header("Accept", "application/json")
		.header("Content-Type", "application/json")
		.header("Authorization", "Basic "+apiToken);
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
	public List<DataikuProjectDetailsDto> getDataikuProjects(String cloudProfile){
		List<DataikuProjectDetailsDto> dataikuProjects = new ArrayList<>();
		String responseBody = "";
		String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
		String url =  "https:" + baseUri + dataikuClientConfig.getProjectsUri();
		String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
		HttpRequest<?> req = HttpRequest.GET(url).header("Accept", "application/json")
		.header("Content-Type", "application/json")
		.header("Authorization", "Basic "+apiToken);
		try {
			
			HttpResponse<DataikuProjectsCollectionDto> response = client.toBlocking().exchange(req,DataikuProjectsCollectionDto.class);
			if(response!=null && response.getBody()!=null) {
				dataikuProjects = response.getBody().get();
				log.info("got projects collection from {} ",cloudProfile);
			}
		}catch(Exception e) {
			log.error("Failed to fetch dataiku projects from {} with exception {}", cloudProfile, e.getMessage());
		}
		return dataikuProjects;
	}

	@Override
	public MessageDescription updateScenario(String projectName, String cloudProfile) {
		try {
			String requestJson = dataikuClientConfig.getScenarioUpdateRequest();
			log.info("update request json template is {} " ,requestJson);
			String updatedRequestJson = requestJson.replaceFirst("XXXXdefaultProjectNameXXXX", projectName);
			log.info("Update scenario request for project {} is {}",projectName, updatedRequestJson);
			String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
			String url =  baseUri + "/projects/" + dataikuClientConfig.getScenarioProjectKey() + "/scenarios/" + dataikuClientConfig.getScenarioId();
			String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
			HttpRequest<String> req = HttpRequest.PUT(url,updatedRequestJson)
			.header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic " + apiToken);
			HttpResponse<DataikuResponseDto> response = client.toBlocking().exchange(req,DataikuResponseDto.class);
			if(response!=null && response.getBody()!=null) {
				Optional<DataikuResponseDto> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("Updated scenario for projectName {} with response {} ",projectName, responseBody.get().getMsg());
				}
			}
			client.close();
			return null;
		}catch(Exception e) {
			log.error("Failed while updating scenario for project {} with exception {} ",projectName, e.getMessage());
			return new MessageDescription("Failed while updating scenario for project " + projectName + " with exception " + e.getMessage());
		}
	}

	@Override
	public MessageDescription runScenario(String projectName, String cloudProfile) {
		try {
			String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
			String url =   baseUri + "/projects/" + dataikuClientConfig.getScenarioProjectKey() + "/scenarios/" + dataikuClientConfig.getScenarioId() + "/run";
			String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
			HttpRequest<String> req = HttpRequest.POST(url,"{}")
													.header("Accept", "application/json")
													.header("Content-Type", "application/json")
													.header("Authorization", "Basic "+ apiToken);
			log.info("run scenarion with url {}", url);
			HttpResponse<Object> response = client.toBlocking().exchange(req,Object.class);
			if(response!=null && response.getBody()!=null) {
				Optional<Object> responseBody = response.getBody();
				if(responseBody.isPresent()) {
					log.info("Ran updated scenario for projectName {} with response status {} ",projectName, response.getStatus().toString());
				}
			}
			client.close();
			return null;
		}catch(Exception e) {
			log.error("Failed while calling run scenario after update for project {} with exception {} ",projectName, e.getMessage());
			return new MessageDescription("Failed while calling run scenario for project " + projectName + " with exception " + e.getMessage());
		}
	}
	
	@Override
	public MessageDescription deleteProject(String projectName, String cloudProfile) {
		try {
			String baseUri = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseBaseuri() : dataikuClientConfig.getExtolloBaseuri();
			String url =   baseUri + "/projects/" + projectName.toUpperCase();
			String apiToken = "onPremise".equalsIgnoreCase(cloudProfile) ? dataikuClientConfig.getOnPremiseAuth() : dataikuClientConfig.getExtolloAuth();
			HttpRequest<?> req = HttpRequest.DELETE(url,null).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic "+ apiToken);
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
