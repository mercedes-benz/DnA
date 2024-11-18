package com.daimler.data.auth.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
//@ConditionalOnExpression("${workspace.update}")
public class WorkspaceMigration {

	@Autowired
	private WorkspaceCustomRepository customRepository;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private AuthenticatorClient authenticatorClient;

	@Value("${workspace.update}")
	private boolean updateWorkspaceData;

	@Value("${authenticator.uri}")
	private String authenticatorBaseUri;

	private static final String GET_ALL_SERVICES = "/api/kong/services";
			
	@PostConstruct
	public void init() {
		if (updateWorkspaceData) {
			try {
				log.info("Run after application startup ");
				List<String> workspaceIds = customRepository.getAllWorkspaceIds();
				List<String> oldWsIds = new ArrayList<>();
				List<String> oldWsIdsWithService = new ArrayList<>();
				String kongServiceNames = null;
				String[] kongWsIds = null;
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Content-Type", "application/json");
				String getAllServiceUri = authenticatorBaseUri + GET_ALL_SERVICES;
				HttpEntity entity = new HttpEntity<>(headers);
				ResponseEntity<String> response = restTemplate.exchange(getAllServiceUri, HttpMethod.GET, entity,
						String.class);
				List<String> kongWsIdList = new ArrayList<>();
				if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null
						&& response.getBody().length() > 2) {
					kongServiceNames = response.getBody();
					// split string by ,
					kongServiceNames = kongServiceNames.substring(1, kongServiceNames.length() - 1);
					kongWsIds = kongServiceNames.split(",");
					// Now convert string into ArrayList
					kongWsIdList = new ArrayList<String>(Arrays.asList(kongWsIds));
				}
				final List<String> newKongWsIdList = new ArrayList<>(kongWsIdList);
				if (!ObjectUtils.isEmpty(workspaceIds) ) {
					oldWsIds = workspaceIds.stream().filter(id -> !newKongWsIdList.contains("\"" + id + "\""))
							.collect(Collectors.toList());
					
					for(String id : workspaceIds) {
						if(newKongWsIdList.contains("\"" + id + "\"")) {
							oldWsIdsWithService.add(id);
						}else {
							oldWsIds.add(id);
						}
					}
				}
				log.info("old workspace ids for which service is not created yet are: {}", oldWsIds);
				if (Objects.nonNull(oldWsIds) && oldWsIds.size() > 0) {
					for (String oldWsId : oldWsIds) {
						authenticatorClient.callingKongApis(oldWsId,oldWsId,null,false,null,null,null);
					}
				}
				log.info("old workspace ids for which service already created and attaching authorization plugin as migration are: {}", oldWsIdsWithService);
				if (Objects.nonNull(oldWsIdsWithService) && oldWsIdsWithService.size() > 0) {
					for (String oldWsIdWithService : oldWsIdsWithService) {
						authenticatorClient.attachAppAuthoriserPluginToService(null, oldWsIdWithService , ConstantsUtility.DHC_CAAS);
					}
				}
			} catch (Exception e) {
				log.error("Failed to call kong get all services API at startup with exception {}", e.getMessage());
			}

		}
	}


}
