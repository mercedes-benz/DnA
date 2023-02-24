package com.daimler.data.adapter.hasura;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.adapter.jupyter.JupyterNotebookAdapter;
import com.daimler.data.adapter.jupyter.JupyterNotebookGenericResponse;
import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.db.jsonb.solution.Solution;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HasuraClient {
	
	private static Logger LOGGER = LoggerFactory.getLogger(HasuraClient.class);

	@Value("${hasura.baseuri}")
	private String baseUri;

	@Value("${hasura.token}")
	private String authToken;
	
	@Value("${hasura.createTechUserUri}")
	private String createTechUserUri;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	private UserInfoAssembler userInfoAssembler;
	
	public HasuraGenericResponse createTechnicalUser(UserInfoVO userInfoVO) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("x-hasura-admin-secret", authToken);
			
			String hasuraUri = baseUri + createTechUserUri;
			
			UserInfoNsql userInfoEntity = userInfoAssembler.toEntity(userInfoVO);
			UserInfo userInfo = userInfoEntity.getData();
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(userInfo);
			HasuraUserInfoRequestDto tempTechnicalUser = new HasuraUserInfoRequestDto();
			tempTechnicalUser.setData(data);
			tempTechnicalUser.setId(userInfoVO.getId());
			
			HttpEntity<HasuraUserInfoRequestDto> entity = new HttpEntity<>(tempTechnicalUser,headers);
			ResponseEntity<HasuraUserInfoInsertResponseDto> response = restTemplate.exchange(hasuraUri,
					HttpMethod.POST, entity, HasuraUserInfoInsertResponseDto.class);
			if (response != null) {
				HttpStatus responseStatus = response.getStatusCode();
				if (responseStatus != null && responseStatus.is2xxSuccessful()) {
					HasuraGenericResponse startSuccessResponse = new HasuraGenericResponse();
					startSuccessResponse.setErrorMessage(null);
					startSuccessResponse.setStatus(HttpStatus.OK);
					startSuccessResponse.setUserInfoVO(userInfoVO);
					LOGGER.info(
							"Technical user created successfully with id {} ",
							userInfoVO.getId());
					return startSuccessResponse;
				}
				response.getBody();
			}
			
		} catch (HttpClientErrorException e) {
			HasuraGenericResponse startClientErrorResponse = new HasuraGenericResponse();
			
			LOGGER.info("Technical user, {} created Already", userInfoVO.getId());
			return startClientErrorResponse;
		} catch (Exception e) {
			HasuraGenericResponse startServerErrorResponse = new HasuraGenericResponse();
			LOGGER.error("Failed to create Technical User {}", e.getMessage());
			return startServerErrorResponse;
		}
		return null;
	}


}
