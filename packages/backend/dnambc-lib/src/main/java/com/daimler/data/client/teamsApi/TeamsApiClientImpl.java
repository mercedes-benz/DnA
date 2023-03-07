package com.daimler.data.client.teamsApi;

import com.daimler.data.client.dashboard.UpdateDepartmentRequestDto;
import com.daimler.data.client.dashboard.UpdateDepartmentRequestWrapperDto;
import com.daimler.data.client.dataiku.DataikuClientImp;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.dto.dataiku.DataikuProjectVO;
import com.daimler.data.dto.dataiku.DataikuUserRole;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.util.JWTGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TeamsApiClientImpl implements TeamsApiClient {

    private Logger LOGGER = LoggerFactory.getLogger(TeamsApiClientImpl.class);

    @Autowired
    HttpServletRequest httpRequest;

    @Value("${teamsApi.team-api-baseurl}")
    private String teamsApiBaseUri;

    @Value("${teamsApi.person-search-uri}")
    private String personSearchUri;

    @Value("${teamsApi.teams-api-resultFetchSize}")
    private String teamsApiResultFetchSize;

    @Value("${oidc.clientSecret}")
    private String clientSecret;

    @Autowired
    RestTemplate restTemplate;


    @Override
    public TeamsApiResponseWrapperDto getTeamsApiUserInfoDetails(String searchTerm) {
        UserInfo userInfo = null;
        Claims claims;
        int totalCount=0;
        TeamsApiResponseWrapperDto teamsApiOutputResponse=null;
        try {
            String jwt =httpRequest.getHeader("Authorization");
            claims = JWTGenerator.decodeJWT(jwt);
            String SecretKey= claims.get("authToken", String.class);
            String oidcAuthontication = "Bearer " + "SecretKey";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", oidcAuthontication);
            String teamsApiUri = teamsApiBaseUri + personSearchUri +"?order=default&fetchSize="+teamsApiResultFetchSize+ "&query="+ searchTerm;
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<TeamsApiResponseWrapperDto> response = restTemplate.exchange(teamsApiUri, HttpMethod.GET, entity, TeamsApiResponseWrapperDto.class);
            if (response != null && response.hasBody()) {
                LOGGER.debug("Successfully fetched user details");
                teamsApiOutputResponse=response.getBody();
                //LOGGER.info("Successfully fetched user details"+ teamsApiOutputResponse);
             }

             } catch (Exception e) {
            LOGGER.error("JsonParseException occurred:{}", e.getMessage());
            }
        return teamsApiOutputResponse;
    }


}

