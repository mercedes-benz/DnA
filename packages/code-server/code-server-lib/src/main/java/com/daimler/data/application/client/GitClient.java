package com.daimler.data.application.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import org.springframework.web.client.HttpClientErrorException;
import com.daimler.data.dto.GitBranchesCollectionDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitClient {

	@Value("${codeServer.git.baseuri}")
	private String gitBaseUri;
	
	@Value("${codeServer.git.orgname}")
	private String gitOrgName;
	
	@Value("${codeServer.git.pat}")
	private String personalAccessToken;

	@Value("${codeServer.git.appname}")
	private String applicationName;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private RestTemplate proxyRestTemplate;
	
	@Value("${codespace.recipe}")
	private String DnARecipe;

	@Value("${codeServer.git.pid}")
	private String pidValue;
	
	public HttpStatus createRepo(String repoName, String recipeName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);
			String url = gitBaseUri + "/repos/" + applicationName + "/" + recipeName + "-template/generate";
			String requestJsonString = "{\"owner\":\"" + gitOrgName + "\",\"name\":\"" + repoName
					+ "\",\"description\":\"" + recipeName
					+ " Repository creation from DnA\",\"private\":true,\"include_all_branches\":false }";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			if (response != null && response.getStatusCode() != null) {
				log.info("Completed creating git repo {} initated by user with status {}", gitOrgName,
						response.getStatusCode());
				return response.getStatusCode();
			}
		}catch (HttpClientErrorException.UnprocessableEntity ex) {
				log.error("Error: Name already exists while creating git repo {} with exception {}", gitOrgName,
						ex.getMessage());
				return HttpStatus.CONFLICT; // Return HTTP 409 Conflict status for name conflict
		} catch (Exception e) {
			log.error("Error occured while creating git repo {} with exception {} ", gitOrgName, e.getMessage());
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

	public HttpStatus validateGitUser(String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + applicationName + "/"+ repoName+ "/collaborators/" + pidValue +"/permission";
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				String responseBody = response.getBody();
				JSONObject jsonResponse = new JSONObject(responseBody);
				if(jsonResponse!=null) {
					if(jsonResponse.has("permission")) {
						String permission =  jsonResponse.getString("permission");
						if(permission.equalsIgnoreCase("admin")){
							log.info("PID onboarding into git repo successfull");
							return HttpStatus.ACCEPTED;
						} else {
							log.info("PID onboarding into git repo failed");
							return HttpStatus.FORBIDDEN;
						}
					}
				 }
			}
		} catch (Exception e) {
			log.error("Error occured while onboarding PID {} to git repo {} with exception {}", pidValue, repoName, e.getMessage());
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
			// String[] publicUrlArray = publicGitUrl.split(",");
			String url = "https://api.github.com/user/issues";
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
