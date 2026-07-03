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
import com.daimler.data.util.CommonUtils;

import java.util.Base64;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import org.springframework.web.client.HttpClientErrorException;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import org.springframework.web.client.HttpStatusCodeException;

import com.daimler.data.dto.GitBranchesCollectionDto;
import com.daimler.data.dto.GitHubWorkflowJobsResponseDto;
import com.daimler.data.dto.GitHubWorkflowRunDto;
import com.daimler.data.dto.GitLatestCommitIdDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitClient {

	@Value("${codeServer.git.baseuri}")
	private String gitBaseUri;
	
	@Value("${codeServer.git.orgname}")
	private String gitOrgName;

	@Value("${codeServer.git.gitOrgname}")
	private String codeServerGitOrgName;
	
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

	@Value("${codeServer.git.enterprise.url}")
	private String gheBaseUri;

	@Value("${codeServer.git.ghe.pat}")
	private String ghePat;

	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;

	@Value("${codeserver.git.deploy.appname}")
	private String gitAppName;

	private static String HTTP_HEADER ="https://";

	private HttpHeaders buildHeaders(String baseUrl, String pat) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");

		if (pat == null || pat.isBlank()) {
			log.warn("Git call host={}, PAT is null or empty", baseUrl);
			return headers;
		}
		headers.set("Authorization", "token " + pat.trim());

		return headers;
	}

	public HttpStatus createRepo(String applicationName, String repoName, String recipeName) {
		return createRepo(applicationName, repoName, recipeName, gheBaseUri, ghePat);
	}

	public HttpStatus createRepo(String applicationName, String repoName, String recipeName, String baseUri, String pat) {
		try {
			log.info("Creating repo: name={}, application={}, recipe={}, baseUri={}", 
					repoName, applicationName, recipeName, baseUri);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token " + pat);

			String url = baseUri + "/repos/" + applicationName + "/" + recipeName + "/generate";
			log.info("Create repo URL: {}", url);
			String requestJsonString = "{\"owner\":\"" + gitOrgName + "\",\"name\":\"" + repoName
					+ "\",\"description\":\"" + recipeName
					+ " Repository creation from DnA\",\"private\":true,\"include_all_branches\":false }";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			if (response != null && response.getStatusCode() != null) {
				log.info("Completed creating git repo {} at {} initiated by user with status {}", repoName, baseUri,
						response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (HttpClientErrorException.UnprocessableEntity ex) {
			log.error("Error: Name already exists while creating git repo {} at {} with exception {}", repoName, baseUri,
					ex.getMessage());
			return HttpStatus.CONFLICT;
		} catch (Exception e) {
			log.error("Error occured while creating git repo {} at {} with exception {} ", repoName, baseUri, e.getMessage());
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

	public JSONObject readFileFromGit(String repoName, String repoOwner, String gitUrl, String fileName, String pat)
			throws Exception {
		try {
			String authToken = (pat != null && !pat.isEmpty()) ? pat : personalAccessToken;
			HttpHeaders headers = buildHeaders(gitUrl, authToken);
			String url = gitUrl+"api/v3/repos/"+repoOwner+"/"+repoName+"/contents/.codespaces/"+gitFoldername+"/"+ fileName;
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
			if(e.getMessage().contains("Not Found")) {
				return null;
			} else {
				throw new Exception(e.getMessage());
			}
		}
		log.info("The software file is not present in the Git repository.");
		return null;
	}

	public HttpStatus createOrValidateSoftwareInGit(String repoName, String repoOwner, String SHA, String gitUrl,
			String softwareFileContent, String pat) {
		try {
			String authToken = (pat != null && !pat.isEmpty()) ? pat : personalAccessToken;
			HttpHeaders headers = buildHeaders(gitUrl, authToken);
			String RequestString = null;
			String url = gitUrl + "api/v3/repos/" + repoOwner + "/" + repoName + "/contents/.codespaces/"
					+ gitFoldername + "/" + gitFileName;
			if (SHA != null) {
				RequestString = "{\"message\":\"CodeSpacesoftwarefilecommit\",\"committer\":{\"name\":\"" + repoOwner
						+ "\",\"email\":\"" + repoOwner + "\"},\"sha\":\"" + SHA + "\",\"content\":\""
						+ softwareFileContent + "\"}";
			} else {
				RequestString = "{\"message\":\"CodeSpacesoftwarefilecommit\",\"committer\":{\"name\":\"" + repoOwner
						+ "\",\"email\":\"" + repoOwner + "\"},\"content\":\"" + softwareFileContent + "\"}";
			}
			HttpEntity entity = new HttpEntity<>(RequestString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode() != null) {
				log.info("Successfully created software file in Git repository.");
				return response.getStatusCode();
			}
			log.info("Failed to create software file in the Git repository.");
			return HttpStatus.INTERNAL_SERVER_ERROR;
		} catch (HttpClientErrorException e) {
			String responseBody = e.getResponseBodyAsString();
			log.error("HTTP error while creating software file: {}", e.getStatusCode());
			if (e.getStatusCode().value() == 403 || e.getStatusCode().value() == 422) {
				if (pat != null && (responseBody.contains("protected") || responseBody.contains("branch protection") ||
						responseBody.contains("required status check") || responseBody.contains("Protected branch"))) {
					log.error("Branch protection error for repo {}/{}", repoOwner, repoName);
					throw new RuntimeException("Branch protection error");
				}
			}
			return e.getStatusCode();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			log.error("Error creating software file in repo {}/{}: {}", repoOwner, repoName, e.getMessage());
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	public HttpStatus addUserToRepo(String username, String repoName) {
		return addUserToRepo(username, repoName, null);
	}

	public HttpStatus addUserToRepo(String username, String repoName, Boolean isWorkspaceMigratedToGHE) {
		try {
			String baseUri = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? gheBaseUri : gitBaseUri;
			String pat = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? ghePat : personalAccessToken;
			String orgName = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? gitOrgName : codeServerGitOrgName;
			log.info("Adding user {} to repo {}/{} using {} (isWorkspaceMigratedToGHE={})", 
					username, orgName, repoName, baseUri, isWorkspaceMigratedToGHE);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String url = baseUri+"/repos/" + orgName + "/"+ repoName+ "/collaborators/" + username;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed adding user {}  as collaborator to git repo {} initated by user , with status {} ", username, gitOrgName,response.getStatusCode());
				return response.getStatusCode();
			}
		
		} catch (HttpClientErrorException e) {
            // Catch specific 422 error
            if (e.getStatusCode().value() == 422) {
                log.error("Caught 422 Unprocessable Entity error");
				return HttpStatus.UNPROCESSABLE_ENTITY;
            } else {
                log.error("Caught HTTP client error: " + e.getStatusCode());
            }
        }catch (Exception e) {
			log.error("Error occured while adding collaborator {} to git repo {} with exception {}", username, gitOrgName, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}


	public HttpStatus validateGitUser(String gitBaseUrl,String repoName, String applicationName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUrl+ "api/v3/repos/" + applicationName + "/"+ repoName+ "/collaborators/" + pidValue +"/permission";
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

	public HttpStatus validateGitUserWithPid(String gitBaseUrl, String repoName, String applicationName, String pid, String pat) {
    try {
        gitBaseUrl = gitBaseUrl.trim();
		if (!gitBaseUrl.endsWith("/")) {
			gitBaseUrl += "/";
		}

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + pat);

		String addUrl = gitBaseUrl + "api/v3/repos/" + applicationName + "/" + repoName + "/collaborators/" + pid;
		try {
			HttpEntity<String> addEntity = new HttpEntity<>("{\"permission\":\"admin\"}", headers);
			restTemplate.exchange(addUrl, HttpMethod.PUT, addEntity, String.class);
			log.info("PID {} added as collaborator to {}/{}", pid, applicationName, repoName);
		} catch (Exception ex) {
			log.warn("Could not add PID {} to {}/{}: {}", pid, applicationName, repoName, ex.getMessage());
		}
		
        String url = gitBaseUrl
                + "api/v3/repos/"
                + applicationName + "/"
                + repoName
                + "/collaborators/"
                + pid
                + "/permission";

        log.info("GHE PID Validation URL: {}", url);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        log.info("GHE PID validation status: {}", 
                 response != null ? response.getStatusCode() : "NULL");

        if (response != null && response.getStatusCode() != null) {

            String responseBody = response.getBody();
            JSONObject json = new JSONObject(responseBody);

            if (json.has("permission")) {
                String permission = json.getString("permission");

                if ("admin".equalsIgnoreCase(permission)) {
                    log.info("PID {} has admin access on repo {}/{}", pid, applicationName, repoName);
                    return HttpStatus.OK;
                } else {
                    log.warn("PID {} has '{}' permission on repo {}/{}", pid, permission, applicationName, repoName);
                    return HttpStatus.FORBIDDEN;
                }
            }

            log.error("GHE response has no 'permission' field");
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

    } catch (HttpClientErrorException e) {
        log.error("GHE PID validation failed: HTTP {} for PID {} repo {}/{}",
                e.getStatusCode(), pid, applicationName, repoName);
        return e.getStatusCode();
    } catch (Exception e) {
        log.error("Unexpected GHE PID validation error for PID {} repo {}/{}: {}",
                pid, applicationName, repoName, e.getMessage(), e);
    }

    return HttpStatus.INTERNAL_SERVER_ERROR;
}


	// public HttpStatus addAdminAccessToRepo(String username, String repoName) {
	// 	return addAdminAccessToRepo(username, repoName, gitBaseUri, personalAccessToken);
	// }

	public HttpStatus addAdminAccessToRepo(String username, String repoName) {
		return addAdminAccessToRepo(username, repoName, codeServerGitOrgName, gitBaseUri, personalAccessToken);
	}

	public HttpStatus addAdminAccessToRepo(String username, String repoName, Boolean isWorkspaceMigratedToGHE) {
		String baseUri = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? gheBaseUri : gitBaseUri;
		String pat = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? ghePat : personalAccessToken;
		String orgName = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? gitOrgName : codeServerGitOrgName;
		log.info("Adding admin access for user {} to repo {}/{} using {} (isWorkspaceMigratedToGHE={})", 
				username, orgName, repoName, baseUri, isWorkspaceMigratedToGHE);
		return addAdminAccessToRepo(username, repoName, orgName, baseUri, pat);
	}

	public HttpStatus addAdminAccessToRepo(String username, String repoName, String orgName, String baseUri, String pat) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String url = baseUri+"/repos/" + orgName + "/"+ repoName+ "/collaborators/" + username;
			String requestJsonString = "{\"permission\":\"admin\"}";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed adding user {}  as admin to git repo {} at {} , with status {} ", username, repoName, baseUri, response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while adding {} as admin to git repo {} at {} with exception {}", username, repoName, baseUri, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	// public HttpStatus removeAdminAccessFromRepo(String username, String repoName) {
	// 	return removeAdminAccessFromRepo(username, repoName, gitBaseUri, personalAccessToken);
	// }

	public HttpStatus removeAdminAccessFromRepo(String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "token " + personalAccessToken);
			String url = gitBaseUri + "/repos/" + codeServerGitOrgName + "/" + repoName + "/collaborators/" + username;
			String requestJsonString = "{\"permission\":\"write\"}";
			HttpEntity<String> entity = new HttpEntity<String>(requestJsonString, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (response != null && response.getStatusCode() != null) {
				log.info("Completed removing user {} as admin from git repo {} at {} , with status {}", username, repoName, gitBaseUri, response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occurred while removing {} as admin from git repo {} at {} with exception {}", username, repoName, gitBaseUri, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	public HttpStatus deleteUserFromRepo( String username, String repoName) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken);
			String url = gitBaseUri+"/repos/" + codeServerGitOrgName + "/"+ repoName+ "/collaborators/" + username;
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
	
	public GitBranchesCollectionDto getBranchesFromRepo(String username, String repo, Boolean isWorkspaceMigratedToGHE) {
    GitBranchesCollectionDto allBranches = new GitBranchesCollectionDto();
    try {
        String repoName = null;
        String gitOrg = null;
		int page = 1;
		int pageSize = 100;
		String selectedBaseUri = isWorkspaceMigratedToGHE ? gheBaseUri : gitBaseUri;
		log.info("Fetching branches from repo {} using {} (isWorkspaceMigratedToGHE={})", 
				repo, selectedBaseUri, isWorkspaceMigratedToGHE);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");

		if (isWorkspaceMigratedToGHE) {
			headers.set("Authorization", "token " + ghePat);
		} else {
			headers.set("Authorization", "token " + personalAccessToken);
		}        
		if (repo.startsWith("https://")) {
            if (repo.endsWith(".git")) {
                repo = repo.substring(0, repo.length() - 4);
            }
            if (!repo.endsWith("/")) {
                repo = repo + "/";
            }
            List<String> repoDetails = CommonUtils.getDetailsFromUrl(repo);
            if (repoDetails != null && repoDetails.size() > 2) {
                gitOrg = repoDetails.get(1);
                repoName = repoDetails.get(2);
            }
        } else {
            repoName = repo;
        }
        String orgName = Objects.nonNull(gitOrg) ? gitOrg : gitOrgName;
        String baseApiUrl = isWorkspaceMigratedToGHE ? gheBaseUri : gitBaseUri;

        while (true) {
            String url = baseApiUrl
                    + "/repos/"
                    + orgName
                    + "/"
                    + repoName
                    + "/branches?per_page="
                    + pageSize
                    + "&page="
                    + page;

            log.info("Fetching branches from URL: {}", url);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GitBranchesCollectionDto> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, GitBranchesCollectionDto.class);

            if (response != null && response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                GitBranchesCollectionDto branches = response.getBody();
                allBranches.addAll(branches);

                if (branches.size() < pageSize) break;
                page++;
            } else {
                break;
            }
        }
        log.info("Fetched {} branches from repo {}", allBranches.size(), repoName);
    } catch (Exception e) {
        log.error("Error occurred while fetching branches from git repo {}: {}", repo, e.getMessage(), e);
    }
    return allBranches;
}

	
	public HttpStatus validateGitPat(String username, String pat, String gitBaseUrl) {
		String dnaOrgMembersUrl = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String baseUrl = gitBaseUrl;
			if (!baseUrl.endsWith("/")) {
				baseUrl += "/";
			}

			dnaOrgMembersUrl = baseUrl + "orgs/" + gitOrgName + "/members";
			log.info("Validating PAT and SSO for user {} against org {} URL: {}", username, gitOrgName,
					dnaOrgMembersUrl);
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(dnaOrgMembersUrl, HttpMethod.GET, entity,
					String.class);

			if (response != null && response.getStatusCode() != null) {
				if (response.getStatusCode().is2xxSuccessful()) {
					log.info("PAT is valid and SSO is configured for user {} ({} org)", username, gitOrgName);
					return HttpStatus.OK;
				} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
					log.warn("PAT is valid but SSO is NOT configured for user {} ({} org)", username, gitOrgName);
					return HttpStatus.FORBIDDEN;
				} else {
					log.warn("Unexpected status while validating PAT/SSO for user {}: {}", username,
							response.getStatusCode());
					return response.getStatusCode();
				}
			}
		} catch (HttpClientErrorException e) {
			log.error("HTTP error while validating user {} PAT/SSO: status={}",
					username, e.getStatusCode());
			return e.getStatusCode();
		} catch (Exception e) {
			log.error("Error occurred while validating user {} PAT/SSO against URL {} with exception {}",
					username, dnaOrgMembersUrl, e.getMessage(), e);
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

	public GitLatestCommitIdDto getLatestCommitId( String orgName, String branch, String repoName) {
		return getLatestCommitId(orgName, branch, repoName, null);
	}
	
	public GitLatestCommitIdDto getLatestCommitId( String orgName, String branch, String repoName, Boolean isWorkspaceMigratedToGHE) {
		GitLatestCommitIdDto commitId = null;
		try {
			String baseUri = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? gheBaseUri : gitBaseUri;
			String pat = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? ghePat : personalAccessToken;
			
			log.info("Getting latest commit ID: org={}, repo={}, branch={}, baseUri={} (isWorkspaceMigratedToGHE={})", 
					orgName, repoName, branch, baseUri, isWorkspaceMigratedToGHE);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String url = baseUri+"/repos/" + orgName + "/"+ repoName+ "/commits?sha="+branch+"&per_page=1";
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			GitLatestCommitIdDto[] commits = objectMapper.readValue(response.getBody(), GitLatestCommitIdDto[].class);
				if (commits.length > 0) {
					 commitId = commits[0];
				}
			log.info("completed fetching latest commit id from git repo {} and branch {} ",repoName, branch);
			return commitId;
		} catch (Exception e) {
			log.error("Error occured while  fetching latest commit id from git repo {} and branch {} with exception {}", repoName, branch, e.getMessage());
		}
		return null;
	}
	
	public HttpStatus isUserCollaborator( String orgName,String username, String repoName, Boolean isWorkspaceMigratedToGHE) {
		String baseUri = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? gheBaseUri : gitBaseUri;
		String pat = Boolean.TRUE.equals(isWorkspaceMigratedToGHE) ? ghePat : personalAccessToken;
		log.info("Checking if user is collaborator: user={}, org={}, repo={}, baseUri={} (isWorkspaceMigratedToGHE={})", 
				username, orgName, repoName, baseUri, isWorkspaceMigratedToGHE);
		return isUserCollaborator(orgName, username, repoName, baseUri, pat);
	}
	
	public HttpStatus isUserCollaborator( String orgName,String username, String repoName, String baseUri, String pat) {
  	try {
			log.info("Checking if user is collaborator: user={}, org={}, repo={}, baseUri={}", 
					username, orgName, repoName, baseUri);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String url = baseUri+"/repos/" + orgName + "/"+ repoName+ "/collaborators/" + username;			
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				log.info("completed checking user {} as collaborator for git repo {} at {}, with status ", username, repoName, baseUri, response.getStatusCode());
				return response.getStatusCode();
			}
		} catch (Exception e) {
			log.error("Error occured while checking collaborator {} for git repo {} at {} with exception {}", username, repoName, baseUri, e.getMessage());
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
		
	}
	public Boolean isUserAdmin( String orgName,String username, String repoName) {
		return isUserAdmin(orgName, username, repoName, gitBaseUri, personalAccessToken);
	}
	
	public Boolean isUserAdmin( String orgName,String username, String repoName, String baseUri, String pat) {
		Boolean isAdmin = false;
		try {
			log.info("Checking if user is admin: user={}, org={}, repo={}, baseUri={}", 
					username, orgName, repoName, baseUri);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ pat);
			String url = baseUri+"/repos/" + orgName + "/"+ repoName+ "/collaborators/" + username+"/permission";
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode()!=null) {
				if(response.getStatusCode().is2xxSuccessful()){
					String responseBody = response.getBody();
					JSONObject jsonResponse = new JSONObject(responseBody);
					if(jsonResponse !=null && jsonResponse.has("permission")) {
						log.info("completed checking user {} as admin for git repo {} at {}.", username, repoName, baseUri);
						String permission = jsonResponse.get("permission").toString();
						if("admin".equalsIgnoreCase(permission)){
							isAdmin = true;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error occured while checking admin {} for git repo {} at {} with exception {}", username, repoName, baseUri, e.getMessage());
		}
		return isAdmin;
		
	}

	public JSONObject getFileContent(String repoName, String repoOwner, String gitUrl, String folderPath, String fileName, String branch) throws Exception {
		try {
			log.info("Getting file content: repo={}, owner={}, folder={}, file={}, branch={}, gitUrl={}", 
					repoName, repoOwner, folderPath, fileName, branch, gitUrl);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token "+ personalAccessToken );
			String url = gitUrl+"/api/v3/repos/"+repoOwner+"/"+repoName+"/contents/"+folderPath+"/"+fileName+"?ref="+branch;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if(response != null && response.getStatusCode()!=null && response.getStatusCode() == (HttpStatus.OK)) {
				String responseBody = response.getBody();
				JSONObject jsonResponse = new JSONObject(responseBody);
				if(jsonResponse !=null && jsonResponse.has("name") && jsonResponse.has("content")) {
					log.info("Successfully fetched  file from Git repository.");
					return jsonResponse;
				}
			}
		} catch (Exception e) {
			log.error("Error occured while fetching file from git url :{}, message: {}", gitUrl,e.getMessage());
			if(e.getMessage().contains("Not Found")) {
				return null;
			} else {
				throw new Exception(e.getMessage());
			}
		}
		log.info("The  file is not present in the Git repository.");
		return null;
	}

	public GitHubWorkflowJobsResponseDto.Job getBuildDeployJob(String runId) {
		String repoPath = applicationName + "/codespace-build-deploy-workflows";
		String url = gheBaseUri + "/repos/" + repoPath + "/actions/runs/" + runId + "/jobs";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/vnd.github+json");
		headers.set("Authorization", "Bearer " + ghePat);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		try {
			log.info("Calling GitHub Jobs API: {}", url);
			ResponseEntity<GitHubWorkflowJobsResponseDto> response =
					restTemplate.exchange(url, HttpMethod.GET, entity, GitHubWorkflowJobsResponseDto.class);
			GitHubWorkflowJobsResponseDto body = response.getBody();
			if (body != null && body.getJobs() != null) {
				return body.getJobs().stream()
						.filter(job -> job.getName() != null && job.getName().toLowerCase().contains("build or deploy workspace application"))
						.findFirst()
						.orElse(null);
			}
			return null;
		} catch (HttpStatusCodeException ex) {
			log.error("GitHub Jobs API error {} for runId {}", ex.getStatusCode(), runId);
			return null;
		} catch (Exception ex) {
			log.error("Unexpected error while calling GitHub Jobs API", ex);
			return null;
		}
	}

	public GitHubWorkflowRunDto getWorkflowRun(String runId) {
		String repoPath = applicationName + "/codespace-build-deploy-workflows";
		String url = gheBaseUri + "/repos/" + repoPath + "/actions/runs/" + runId;

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/vnd.github+json");
		headers.set("Authorization", "Bearer " + ghePat);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		try {
			log.info("Calling GitHub API: {}", url);
			ResponseEntity<GitHubWorkflowRunDto> response =
					restTemplate.exchange(url, HttpMethod.GET, entity, GitHubWorkflowRunDto.class);
			return response.getBody();

		} catch (HttpStatusCodeException ex) {
			log.error("GitHub API error {} for runId {}", ex.getStatusCode(), runId);
			return null;
		} catch (Exception ex) {
			log.error("Unexpected error while calling GitHub", ex);
			return null;
		}
	}

	public GenericMessage cancelWorkflowRun(String runId) {

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Authorization", "Bearer " + personalAccessToken);

			String url = gitBaseUri + "/repos/" + applicationName + "/" + gitAppName
					+ "/actions/runs/" + runId + "/cancel";

			HttpEntity<Void> entity = new HttpEntity<>(headers);

			restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

			log.info("Cancelled workflow run {}", runId);
			return new GenericMessage("SUCCESS", null, null);

		} catch (Exception e) {
			log.error("Error cancelling workflow run {}", runId, e);
			return new GenericMessage("FAILED", null, List.of(new MessageDescription("Error cancelling workflow run: " + e.getMessage())));
		}
	}

	public void reRunWorkFlow(String gitRunId) {

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github+json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);

			String url = gitBaseUri + "/repos/" + applicationName + "/" + gitAppName
					+ "/actions/runs/" + gitRunId + "/rerun";

			HttpEntity<Void> entity = new HttpEntity<>(headers);

			restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

			log.info("Triggered workflow {} on branch {}", gitRunId);

		} catch (Exception e) {
			log.error("Error triggering workflow {}", e);
		}
	}

	
}
