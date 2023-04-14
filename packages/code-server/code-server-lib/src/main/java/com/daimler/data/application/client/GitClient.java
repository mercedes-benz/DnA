package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.GitBranchesCollectionDto;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Component
@Slf4j
public class GitClient {

	@Value("${codeServer.git.baseuri}")
	private String gitBaseUri;
	
	@Value("${codeServer.git.orgname}")
	private String gitOrgName;
	
	@Value("${codeServer.git.pat}")
	private String personalAccessToken;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private RestTemplate proxyRestTemplate;
	
	@Value("${codespace.recipe}")
	private String DnARecipe;
	
	public HttpStatus createRepo(String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/orgs/"+gitOrgName+"/repos";
			String requestJsonString = "{\"name\":\"" + repoName + "\",\"description\":\"Repository creation from DnA codespaces\",\"private\":true,\"has_issues\":true,\"has_projects\":true,\"has_wiki\":true, \"auto_init\": true}";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString,headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("Completed creating git repo {} initated by user with status {}", gitOrgName,response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while creating git repo {} with exception ", gitOrgName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
		
	public HttpStatus deleteRepo(String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed deleting git repo {} initated by user with status {}", gitOrgName,response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while deleting git repo {} with exception {} ", gitOrgName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	public HttpStatus addUserToRepo(String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName+ "/collaborators/" + username;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed adding user {}  as collaborator to git repo {} initated by user , with status {} ", username, gitOrgName,response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while adding collaborator {} to git repo {} with exception {}", username, gitOrgName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpStatus deleteUserFromRepo( String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName+ "/collaborators/" + username;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed removing user {} as collaborator from git repo {} initated by user, with status ", username, gitOrgName,response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while removing collaborator {} from git repo {} with exception {}", username, gitOrgName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	public GitBranchesCollectionDto getBranchesFromRepo( String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName+ "/branches";
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<GitBranchesCollectionDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, GitBranchesCollectionDto.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed fetching branches from git repo {} by user {} ",repoName, username);
				return response.getBody();
			}
		} catch (Exception e) {
			log.error("Error occured while fetching branches from git repo {} with exception {}", username, gitOrgName, e.getMessage());
		}
		return new GitBranchesCollectionDto();
	}
	
	
	public HttpStatus validateGitPat( String username, String pat) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String url = gitBaseUri+"/users/" + username;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed validating user {} PAT with http status {}", username, response.getStatusCode().name());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while validating user {} PAT with exception {}", username, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpStatus validatePublicGitPat(String gitUserName, String pat, String publicGitUrl) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String userRepoName = "";
			String[] publicUrlArray = publicGitUrl.split(",");
			String url = "https://api.github.com/users/" + DnARecipe;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = proxyRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode() != null) {
				log.info("Completed validating public github user {} PAT with http status {}",
						gitUserName, response.getStatusCode().name());
				return response.getStatusCode();
			}

		} catch (Exception e) {
			log.error("Error occured while validating public github user {} PAT with exception {}", gitUserName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
		
	}
	
	
}
