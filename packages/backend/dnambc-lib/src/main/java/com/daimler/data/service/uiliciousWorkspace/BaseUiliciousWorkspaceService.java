package com.daimler.data.service.uiliciousWorkspace;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.dto.uilicious.LeanGovernanceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;
import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.uiliciousWorkspace.UiliciousWorkspaceService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.daimler.data.controller.LoginController.UserInfo;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.db.repo.uilicious.UiliciousWorkspaceCustomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@Slf4j
public class BaseUiliciousWorkspaceService extends
		BaseCommonService<UiliciousWorkspaceVO, UiliciousWorkspaceNsql, String> implements UiliciousWorkspaceService {
	@Autowired
	private UserStore userStore;

	@Autowired
	private UiLiciousClient uiLiciousClient;

	@Autowired
	private UiliciousWorkspaceCustomRepository uiliciousWorkspaceCustomRepository;

	@Override
	public UiliciousWorkspacesCollectionVO getUiliciousWorkspaces(Integer offset, Integer limit, String sortOrder) {
		UiliciousWorkspacesCollectionVO response = new UiliciousWorkspacesCollectionVO();
		UserInfo currentUser = this.userStore.getUserInfo();
		List<LeanGovernanceVO> leanGovernance = new ArrayList<>();
		// log.info("Logged in user: "+ currentUser.getEmail());
		log.info("Logged in user: " + currentUser.getEmail());
		try {
			// Check if user workspace exists in database
			
			JsonNode existingWorkspace = uiliciousWorkspaceCustomRepository.findUiliciousWorkspacesByEmail(currentUser.getEmail());
			String accountId = null;
			
			if (existingWorkspace != null) {
				log.info("Found existing workspace in database for email: {}", currentUser.getEmail());
				
				// Extract lean governance from database if available
				if (existingWorkspace.has("leanGovernance") && !existingWorkspace.path("leanGovernance").isNull()) {
					JsonNode leanGovNode = existingWorkspace.path("leanGovernance");
					ObjectMapper mapper = new ObjectMapper();
					try {
						LeanGovernanceVO leanGov = mapper.convertValue(leanGovNode, LeanGovernanceVO.class);
						leanGovernance.add(leanGov);
						log.info("Lean governance found and added from database for email: {}", currentUser.getEmail());
					} catch (Exception e) {
						log.warn("Failed to convert lean governance from database: {}", e.getMessage());
					}
				}
				
				// Check if accountId field exists and is not null
				if (existingWorkspace.has("accountId") && !existingWorkspace.path("accountId").isNull() && 
				    !existingWorkspace.path("accountId").asText().trim().isEmpty()) {
					accountId = existingWorkspace.path("accountId").asText();
					log.info("Found existing accountId in database: {}", accountId);
				} else {
					// AccountId is null or empty, make API call to get it and try to update database
					log.info("AccountId is null in database, fetching from API for email: {}", currentUser.getEmail());
					String apiAccountId = uiLiciousClient.getUserAccountId(currentUser.getEmail(), 0, 10);
					
					if (apiAccountId != null && !apiAccountId.trim().isEmpty()) {
						// Try to update the database with the new accountId
						boolean updated = uiliciousWorkspaceCustomRepository.updateAccountIdByEmail(currentUser.getEmail(), apiAccountId);
						if (updated) {
							// Only assign to accountId if database update was successful
							accountId = apiAccountId;
							log.info("Successfully fetched and persisted accountId: {}", accountId);
						} else {
							// Keep accountId as null since database update failed
							log.warn("Failed to persist accountId to database for email: {}, keeping accountId as null", currentUser.getEmail());
						}
					} else {
						log.warn("Failed to fetch accountId from API for email: {}", currentUser.getEmail());
					}
				}
			} else {
				// No existing workspace found in database, accountId remains null
				log.info("No existing workspace found in database for email: {}, accountId will be null", currentUser.getEmail());
				// accountId remains null since we only want database values
			}
			
			// Set default values if not provided
			int start = (offset != null) ? offset : 0;
			int length = (limit != null) ? limit : 10;
			
			// UiliciousWorkspaceNsql workspace = uiliciousWorkspaceCustomRepository.findUiliciousWorkspacesByEmail(currentUser.getEmail());
			// if (workspace != null && workspace.getData() != null) {
			// 	ObjectMapper mapper = new ObjectMapper();
			// 	LeanGovernanceVO leanGov = mapper.convertValue(workspace.getData(), LeanGovernanceVO.class);
			// 	leanGovernance.add(leanGov);
			// }

			//List<LeanGovernanceVO> workspaces = uiLiciousClient.getWorkspaces(currentUser.getEmail(), start,length);
			// Call the client to get workspaces from Uilicious APIs
			List<UiliciousWorkspaceVO> workspaces = uiLiciousClient.getWorkspaces(currentUser.getEmail(), start,
					length);
			log.info("List of Workspaces: " + workspaces);
			if (workspaces != null && !workspaces.isEmpty()) {
				// Apply sorting if specified
				if (sortOrder != null && "desc".equalsIgnoreCase(sortOrder)) {
					workspaces.sort((w1, w2) -> w2.getSpaceName().compareToIgnoreCase(w1.getSpaceName()));
				} else {
					workspaces.sort((w1, w2) -> w1.getSpaceName().compareToIgnoreCase(w2.getSpaceName()));
				}

				response.setAccountId(accountId);
				response.setLeanGovernance(leanGovernance);
				response.setItems(workspaces);
				response.setTotalRecords(workspaces.size());
				log.info("Successfully fetched {} workspaces for email: {}",
						workspaces.size(), currentUser.getEmail());
			} else {
				response.setLeanGovernance(new ArrayList<>());
				response.setItems(new ArrayList<>());
				response.setTotalRecords(0);
				log.info("No workspaces found for email: {}", currentUser.getEmail());
				
			}

		} catch (Exception e) {
			log.error("Error occurred while fetching Uilicious workspaces for email: {}, error: {}",
					currentUser.getEmail(), e.getMessage(), e);
			return null;
		}

		return response;
	}

}
