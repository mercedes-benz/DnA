package com.daimler.data.application.client;

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.daimler.data.dto.CodespaceProjectAdminEligibilityDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CodeServerClient {

	private static final String USER_DETAILS_HEADER = "dna-request-userdetails";

	@Value("${codeServer.projectAdminEligibilityUri}")
	private String projectAdminEligibilityUri;

	@Autowired
	private RestTemplate restTemplate;

	public boolean isCodespaceProjectOwnerOrAdmin() {
		if (!StringUtils.hasText(projectAdminEligibilityUri) || !projectAdminEligibilityUri.startsWith("http")) {
			log.debug("Codespaces project eligibility is unavailable because the endpoint URI is not configured");
			return false;
		}

		try {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			if (!(requestAttributes instanceof ServletRequestAttributes)) {
				return false;
			}

			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			if (request == null) {
				return false;
			}

			String userDetails = request.getHeader(USER_DETAILS_HEADER);
			if (!StringUtils.hasText(userDetails)) {
				return false;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.set(USER_DETAILS_HEADER, userDetails);
			String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (authorization != null) {
				headers.set(HttpHeaders.AUTHORIZATION, authorization);
			}

			ResponseEntity<CodespaceProjectAdminEligibilityDto> response = restTemplate.exchange(
					projectAdminEligibilityUri,
					HttpMethod.GET,
					new HttpEntity<>(headers),
					CodespaceProjectAdminEligibilityDto.class);
			return response != null && response.getStatusCode().is2xxSuccessful()
					&& response.getBody() != null
					&& Boolean.TRUE.equals(response.getBody().getIsProjectOwnerOrAdmin());
		} catch (Exception e) {
			log.warn("Failed to check Codespaces project owner or admin eligibility");
			return false;
		}
	}
}
