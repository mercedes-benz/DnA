package com.daimler.data.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthoriserClient {

	@Value("${authoriser.uri}")
	private String authoriserBaseUrl;

	@Value("${authoriser.ssoUri}")
	private String ssoUri;

	@Value("${authoriser.clientId}")
	private String authoriserClientID;

	@Value("${authoriser.clientSecret}")
	private String authoriserClientSecret;

	@Autowired
	private RestTemplate proxyRestTemplate;
	

	public String getToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        String basicAuthenticationHeader = Base64.getEncoder()
                .encodeToString(new StringBuffer(authoriserClientID).append(":").append(authoriserClientSecret).toString().getBytes());
            //map.add("client_id", authoriserClientID);
            //map.add("client_secret", authoriserClientSecret);
        map.add("grant_type", "client_credentials");
        map.add("scope", "openid authorization_group entitlement_group scoped_entitlement email profile");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + basicAuthenticationHeader);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<String> response = proxyRestTemplate.postForEntity(ssoUri, request, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            AliceOAuthResponse introspectionResponse = objectMapper.readValue(response.getBody(),
            AliceOAuthResponse.class);
            return introspectionResponse.getAccess_token();
        } catch (Exception e) {
            log.error("Failed to fetch OIDC token with error {} ",e.getMessage());
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AliceOAuthResponse implements Serializable{

    private static final long serialVersionUID = 1L;
    private String access_token;

    }
    
}

