package com.daimler.data.application.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.daimler.data.dto.CodespaceProjectAdminEligibilityDto;

@ExtendWith(MockitoExtension.class)
class CodeServerClientTest {

	private static final String ELIGIBILITY_URL =
			"https://codeserver.example/api/workspaces/projectadmin/eligibility";

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private CodeServerClient client;

	@BeforeEach
	void setEligibilityUri() throws ReflectiveOperationException {
		setEligibilityUri(ELIGIBILITY_URL);
	}

	@AfterEach
	void clearRequestAttributes() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void invalidEligibilityUriReturnsFalseWithoutCallingCodeServer() throws ReflectiveOperationException {
		setEligibilityUri("XXXX");

		assertFalse(client.isCodespaceProjectOwnerOrAdmin());

		verifyNoInteractions(restTemplate);
	}

	@Test
	void forwardsRequestHeadersAndReturnsTrueEligibility() {
		setRequestHeaders("alice", "FAKE-AUTHORIZATION");
		CodespaceProjectAdminEligibilityDto eligibility = new CodespaceProjectAdminEligibilityDto();
		eligibility.setIsProjectOwnerOrAdmin(true);
		when(restTemplate.exchange(
				anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
				eq(CodespaceProjectAdminEligibilityDto.class)))
				.thenReturn(ResponseEntity.ok(eligibility));

		assertTrue(client.isCodespaceProjectOwnerOrAdmin());

		ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(ELIGIBILITY_URL), eq(HttpMethod.GET), requestCaptor.capture(),
				eq(CodespaceProjectAdminEligibilityDto.class));
		HttpHeaders headers = requestCaptor.getValue().getHeaders();
		assertEquals("alice", headers.getFirst("dna-request-userdetails"));
		assertEquals("FAKE-AUTHORIZATION", headers.getFirst(HttpHeaders.AUTHORIZATION));
		assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
	}

	@Test
	void requestFailureReturnsFalse() {
		setRequestHeaders("alice", null);
		when(restTemplate.exchange(
				anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
				eq(CodespaceProjectAdminEligibilityDto.class)))
				.thenThrow(new RestClientException("request failed"));

		assertFalse(client.isCodespaceProjectOwnerOrAdmin());
	}

	@Test
	void missingUserDetailsHeaderReturnsFalseWithoutCallingCodeServer() {
		setRequestHeaders(null, null);

		assertFalse(client.isCodespaceProjectOwnerOrAdmin());

		verifyNoInteractions(restTemplate);
	}

	private void setRequestHeaders(String userDetails, String authorization) {
		HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
		when(request.getHeader("dna-request-userdetails")).thenReturn(userDetails);
		if (authorization != null) {
			when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authorization);
		}
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	private void setEligibilityUri(String uri) throws ReflectiveOperationException {
		Field eligibilityUri = CodeServerClient.class.getDeclaredField("projectAdminEligibilityUri");
		eligibilityUri.setAccessible(true);
		eligibilityUri.set(client, uri);
	}
}
