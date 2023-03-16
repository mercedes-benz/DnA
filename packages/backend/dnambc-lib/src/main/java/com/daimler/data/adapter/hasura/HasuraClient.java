package com.daimler.data.adapter.hasura;

import java.util.ArrayList;
import java.util.List;

import com.daimler.data.db.jsonb.UserInfoRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserInfo;
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

	public HasuraUserInfoInsertGenericResponse createTechnicalUser(UserInfoVO userInfoVO) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("x-hasura-admin-secret", authToken);

			String hasuraUri = baseUri + createTechUserUri;
			UserInfo userInfo = new UserInfo();
			UserInfoRole role = new UserInfoRole();
			role.setId("1");
			role.setName("User");
			List<UserInfoRole> roles = new ArrayList<>();
			roles.add(role);
			userInfo.setDivisionAdmins(null);
			userInfo.setDepartment("NA");
			userInfo.setEmail("");
			userInfo.setFirstName("Tech User");
			userInfo.setRoles(roles);
			userInfo.setLastName(userInfoVO.getId());
			userInfo.setMobileNumber("NA");
			userInfo.setFavoriteUsecases(new ArrayList<>());
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
					HasuraUserInfoInsertGenericResponse startSuccessResponse = new HasuraUserInfoInsertGenericResponse();
					startSuccessResponse.setErrorMessage(null);
					startSuccessResponse.setStatus(HttpStatus.OK);
					UserInfoNsql userInfoEntity = new UserInfoNsql();
					userInfoEntity.setData(userInfo);
					userInfoEntity.setId(userInfoVO.getId());
					UserInfoVO createdUserVo = new UserInfoVO();
					createdUserVo = userInfoAssembler.toVo(userInfoEntity);
					startSuccessResponse.setUserInfoVO(createdUserVo);
					LOGGER.info(
							"Technical user created successfully with id {} ",
							userInfoVO.getId());
					return startSuccessResponse;
				} else {
					if (response.getBody() != null && response.getBody().getCode() != null) {
						HasuraUserInfoInsertGenericResponse startClientErrorResponse = new HasuraUserInfoInsertGenericResponse();
						startClientErrorResponse.setErrorMessage("Failed to add Technical user");
						startClientErrorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
						LOGGER.error("Failed to add Technical user {}", response.getBody().getCode());
						return startClientErrorResponse;
					}
				}
			}

			HasuraUserInfoInsertGenericResponse startClientErrorResponse = new HasuraUserInfoInsertGenericResponse();
			startClientErrorResponse.setErrorMessage("Failed to add Technical user");
			startClientErrorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error("Failed to add Technical user {}", response.getBody().getCode());
			return startClientErrorResponse;
			
		} catch (HttpClientErrorException e) {
			HasuraUserInfoInsertGenericResponse startClientErrorResponse = new HasuraUserInfoInsertGenericResponse();
			startClientErrorResponse.setErrorMessage("Failed to add Technical user");
			startClientErrorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error("Technical user, {} failed to add with exception {}", userInfoVO.getId(), e.getMessage());
			return startClientErrorResponse;
		} catch (Exception e) {
			HasuraUserInfoInsertGenericResponse startServerErrorResponse = new HasuraUserInfoInsertGenericResponse();
			startServerErrorResponse.setErrorMessage("Failed to add Technical user");
			startServerErrorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error("Failed to create Technical User {}", e.getMessage());
			return startServerErrorResponse;
		}
	}


}
