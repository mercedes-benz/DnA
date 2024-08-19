package com.daimler.data.application.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import org.springframework.web.client.HttpClientErrorException;
import com.daimler.data.dto.GitBranchesCollectionDto;
import com.daimler.data.dto.GitLatestCommitIdDto;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	@Value("${codeserver.recipe.software.foldername}")
	private String gitFoldername;

	@Value("${codeserver.recipe.software.filename}")
	private String gitFileName;

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

	public JSONObject getSoftwareFileFromGit(String repoName, String repoOwner, String gitUrl) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer "+ personalAccessToken );
			String url = gitUrl+"api/v3/repos/"+repoOwner+"/"+repoName+"/contents/.codespaces/"+gitFoldername+"/"+gitFileName;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if(response != null && response.getStatusCode()!=null && response.getStatusCode() == (HttpStatus.OK)) {
				String responseBody = response.getBody();
				JSONObject jsonResponse = new JSONObject(responseBody);
				if(jsonResponse !=null && jsonResponse.has("name") && jsonResponse.has("content")) {
					log.info("Successfully fetched software file from Git repository.");
					return jsonResponse;
				}
			}
		} catch (Exception e) {
			log.error("error in git file", gitUrl,repoOwner,e.getMessage());
		}
		log.info("The software file is not present in the Git repository.");
		return null;
	}

	public HttpStatus createOrValidateSoftwareInGit(String repoName, String repoOwner, String SHA ,String gitUrl, String softwareFileContent) {
		HttpHeaders headers = new HttpHeaders();
		String RequestString = null;
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", "Bearer "+ personalAccessToken);
		String url = gitUrl+"api/v3/repos/"+repoOwner+"/"+repoName+"/contents/.codespaces/"+gitFoldername+"/"+gitFileName;
		if(SHA != null) {
			RequestString ="{\"message\":\"CodeSpacesoftwarefilecommit\",\"committer\":{\"name\":\""+repoOwner+"\",\"email\":\""+repoOwner+"\"},\"sha\":\""+SHA+"\",\"content\":\""+softwareFileContent+"\"}";
		} else {
			RequestString ="{\"message\":\"CodeSpacesoftwarefilecommit\",\"committer\":{\"name\":\""+repoOwner+"\",\"email\":\""+repoOwner+"\"},\"content\":\""+softwareFileContent+"\"}";
		}
		HttpEntity entity = new HttpEntity<>(RequestString , headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
		if (response != null && response.getStatusCode() != null) {
			log.info("Successfully created software file in Git repository.");
			return response.getStatusCode();
		}
		log.info("Failed to create software file in the Git repository.");
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

	public HttpStatus validateGitUser(String gitBaseUrl,String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer "+ personalAccessToken);
			String url = gitBaseUrl+ "/repos/" + applicationName + "/"+ repoName+ "/collaborators/" + pidValue +"/permission";
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

	public HttpStatus addAdminAccessToRepo(String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName+ "/collaborators/" + username;
			String requestJsonString = "{\"permission\":\"admin\"}";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed adding user {}  as admin to git repo {} initated by user , with status {} ", username, gitOrgName,response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while adding {} as admin to git repo {} with exception {}", username, gitOrgName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpStatus removeAdminAccessFromRepo(String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "token " + personalAccessToken);
			String url = gitBaseUri + "/repos/" + gitOrgName + "/" + repoName + "/collaborators/" + username;
			String requestJsonString = "{\"permission\":\"write\"}";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode() != null) {
				log.info("Completed removing user {} as admin from git repo {} initiated by user, with status {}", username, gitOrgName, response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occurred while removing {} as admin from git repo {} with exception {}", username, gitOrgName, e.getMessage());
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
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName+ "/branches?per_page=100";
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

	public GitLatestCommitIdDto getLatestCommitId( String branch, String repoName, String gitOrgName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + gitOrgName + "/"+ repoName+ "/commits?sha="+branch+"per_page=1";
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			GitLatestCommitIdDto commitId = objectMapper.readValue(response.getBody(),GitLatestCommitIdDto.class);
			log.info("completed fetching latest commit id from git repo {} and branch {} ",repoName, branch);
			return commitId;
		} catch (Exception e) {
			log.error("Error occured while  fetching latest commit id from git repo {} and branch {} with exception {}", repoName, branch, e.getMessage());
		}
		return new GitLatestCommitIdDto();
	}
	
	
}
