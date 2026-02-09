
package com.daimler.data.service.uiliciousWorkspace;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.dto.uilicious.LeanGovernanceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateRequestVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateResponseVO;
import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.uiliciousWorkspace.UiliciousWorkspaceService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.daimler.data.controller.LoginController.UserInfo;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.db.repo.uilicious.UiliciousWorkspaceCustomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.daimler.data.dto.uilicious.CreateUiliciousWorkspaceRequestVO;
import com.daimler.data.controller.LoginController;
import com.daimler.data.dto.UiliciousCreationDTO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.db.jsonb.solution.CreatedBy;

@Service
@Slf4j
public class BaseUiliciousWorkspaceService extends
		BaseCommonService<UiliciousCreationDTO, UiliciousWorkspaceNsql, String> implements UiliciousWorkspaceService {
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
		log.info("Logged in user: " + currentUser.getEmail());
		try {

			// Set default values if not provided
			int start = (offset != null) ? offset : 0;
			int length = (limit != null) ? limit : 200;

			// Call the client to get workspaces from Uilicious APIs
			List<UiliciousWorkspaceVO> workspaces = uiLiciousClient.getWorkspaces(currentUser.getEmail(), start,
					length);
			log.info("List of Workspaces: " + workspaces);
			if (workspaces != null && !workspaces.isEmpty()) {
				for (UiliciousWorkspaceVO workspace : workspaces) {
                    try {
                        String spaceId = workspace.getSpaceId();
                        if (spaceId != null && !spaceId.trim().isEmpty()) {
                            JsonNode leanGovNode = uiliciousWorkspaceCustomRepository
                                    .findLeanGovernanceBySpaceId(spaceId);
                            
                            if (leanGovNode != null && !leanGovNode.isNull()) {
                                ObjectMapper mapper = new ObjectMapper();
                                LeanGovernanceVO leanGov = mapper.convertValue(leanGovNode, LeanGovernanceVO.class);
                                workspace.setLeanGovernance(leanGov);
                                log.info("Lean governance set for spaceId: {}", spaceId);
                            } else {
                                log.info("No lean governance found in database for spaceId: {}", spaceId);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch/set lean governance for workspace {}: {}", 
                                workspace.getSpaceId(), e.getMessage());
					}
                }
				// Apply sorting if specified
				if (sortOrder != null && "desc".equalsIgnoreCase(sortOrder)) {
					workspaces.sort((w1, w2) -> w2.getSpaceName().compareToIgnoreCase(w1.getSpaceName()));
				} else {
					workspaces.sort((w1, w2) -> w1.getSpaceName().compareToIgnoreCase(w2.getSpaceName()));
				}

				// response.setAccountId(accountId);
				// response.setLeanGovernance(leanGovernance);
				response.setItems(workspaces);
				response.setTotalRecords(workspaces.size());
				log.info("Successfully fetched {} workspaces for email: {}",
						workspaces.size(), currentUser.getEmail());
			} else {
				// response.setLeanGovernance(new ArrayList<>());
				response.setItems(new ArrayList<>());
				response.setTotalRecords(0);
				log.info("No workspaces found for email: {}", currentUser.getEmail());

			}

		} catch (RuntimeException e) {
			// Check if it's a Uilicious server unavailability issue
			if (e.getMessage() != null &&
					(e.getMessage().contains("Uilicious server is unavailable") ||
							e.getMessage().contains("Something went wrong with Uilicious server") ||
							e.getMessage().contains("Failed to communicate with Uilicious server"))) {
				log.info("Uilicious server is down or unreachable for email: {}, error: {}",
						currentUser.getEmail(), e.getMessage());
				throw new RuntimeException("Something went wrong with Uilicious server/tool. Please try again later.",
						e);
			}
			log.info("Runtime error occurred while fetching Uilicious workspaces for email: {}, error: {}",
					currentUser.getEmail(), e.getMessage(), e);
			return null;
		} catch (Exception e) {
			log.info("Error occurred while fetching Uilicious workspaces for email: {}, error: {}",
					currentUser.getEmail(), e.getMessage(), e);
			return null;
		}

		return response;
	}

	@Override
	public UiliciousWorkspaceUpdateResponseVO updateUiliciousWorkspace(UiliciousWorkspaceUpdateRequestVO request) {
		log.info("Starting update for Uilicious workspace with spaceId: {}", request.getSpaceId());

		try {
			// Validate input
			if (request.getSpaceId() == null || request.getSpaceId().trim().isEmpty()) {
				log.info("SpaceId is null or empty in update request");
				throw new IllegalArgumentException("SpaceId cannot be null or empty");
			}

			if (request.getLeanGovernance() == null) {
				log.info("LeanGovernance is null in update request for spaceId: {}", request.getSpaceId());
				throw new IllegalArgumentException("LeanGovernance cannot be null");
			}

			// Convert LeanGovernanceVO to JsonNode for database update
			ObjectMapper mapper = new ObjectMapper();
			JsonNode leanGovernanceNode = mapper.valueToTree(request.getLeanGovernance());

			// Update lean governance in database
			boolean updated = uiliciousWorkspaceCustomRepository.updateLeanGovernanceBySpaceId(
					request.getSpaceId(),
					leanGovernanceNode);

			if (!updated) {
				log.info("Failed to update lean governance in database for spaceId: {}", request.getSpaceId());
				throw new RuntimeException("Failed to update workspace in database");
			}

			log.info("Successfully updated lean governance for spaceId: {}", request.getSpaceId());

			// Prepare response
			UiliciousWorkspaceUpdateResponseVO response = new UiliciousWorkspaceUpdateResponseVO();
			response.setSpaceId(request.getSpaceId());
			response.setLeanGovernance(request.getLeanGovernance());
			response.setMessage("Lean governance updated successfully");

			return response;

		} catch (IllegalArgumentException e) {
			log.info("Validation error during workspace update: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.info("Unexpected error occurred while updating Uilicious workspace for spaceId: {}, error: {}",
					request.getSpaceId(), e.getMessage(), e);
			throw new RuntimeException("Failed to update workspace", e);
		}
	}

	public String createUiliciousWorkspace(CreateUiliciousWorkspaceRequestVO request) {
		try {
			CreatedByVO currentUserInfo = this.userStore.getVO();
			UserInfo currentUser = this.userStore.getUserInfo();
			String userId = currentUserInfo.getId();
			String email = currentUser.getEmail();
			String firstName = currentUser.getFirstName();
			log.info("Creating Uilicious workspace with request calling to client: {}", request);
			String response = uiLiciousClient.createUiliciousWorkspace(email, userId, firstName);
			if (response != null && !response.contains("FAILURE")) {
				
				log.info("Successfully created Uilicious workspace with response: {}", response);
				String accountId = response;
				response = "SUCCESS";

				int start = 0;
				int length = 200;
				// fetch workspaces to get spaceId
				List<UiliciousWorkspaceVO> workspaces = uiLiciousClient.getWorkspaces(currentUser.getEmail(), start,
                        length);
                if (workspaces != null && !workspaces.isEmpty()) {
                    for (UiliciousWorkspaceVO workspace : workspaces) {
                        try {
                            // Check if user has owner permission and space name ends with "Space"
                            if (workspace.getUserRole() != null && "owner".equalsIgnoreCase(workspace.getUserRole())
                                    && workspace.getSpaceName() != null && workspace.getSpaceName().endsWith("Space")) {
                                
                                String spaceId = workspace.getSpaceId();
                                if (spaceId != null && !spaceId.trim().isEmpty()) {
                                    log.info("User is owner of spaceId: {} with name: {}, updating lean governance", 
                                            spaceId, workspace.getSpaceName());
                                    
                                    // Convert LeanGovernanceVO to JsonNode
                                    ObjectMapper mapper = new ObjectMapper();
                                    JsonNode leanGovernanceNode = mapper.valueToTree(request.getLeanGovernance());
                                    
                                    // Update lean governance in database
                                    boolean updated = uiliciousWorkspaceCustomRepository.updateLeanGovernanceBySpaceId(
                                            spaceId, leanGovernanceNode);
                                    
                                    if (updated) {
                                        log.info("Successfully updated lean governance for spaceId: {} ({})", 
                                                spaceId, workspace.getSpaceName());
                                    } else {
                                        log.warn("Failed to update lean governance for spaceId: {} ({})", 
                                                spaceId, workspace.getSpaceName());
                                    }
                                }
                            } else {
                                log.debug("Skipping workspace - spaceId: {}, name: {}, role: {}", 
                                        workspace.getSpaceId(), workspace.getSpaceName(), workspace.getUserRole());
                            }
                        } catch (Exception e) {
                            log.error("Error updating lean governance for workspace {} ({}): {}", 
                                    workspace.getSpaceId(), workspace.getSpaceName(), e.getMessage(), e);
                        }
                    }
                }

			} else {
				log.error("Failed to create Uilicious workspace via client, response: {}", response);
				response = "User workspace already exists or creation failed";
			}
			return response;
		} catch (Exception e) {
			log.error("Error occurred while creating Uilicious workspace in service with exception: {}", e.getMessage(),
					e);
			return null;
		}
	}
}
