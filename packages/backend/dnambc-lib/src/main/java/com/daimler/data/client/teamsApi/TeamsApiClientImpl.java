package com.daimler.data.client.teamsApi;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UsersCollection;
import com.daimler.data.util.JWTGenerator;
import io.jsonwebtoken.Claims;
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
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UserInfoAssembler userInfoAssembler;


    @Override
    public UsersCollection getTeamsApiUserInfoDetails(String searchTerm, int offset) {
        UsersCollection usersCollection =null;
        List<UserInfoVO> userInfoVOList = new ArrayList<>();
        Claims claims;
        Integer totalCount = 0;
        TeamsApiResponseWrapperDto teamsApiOutputResponse = null;
        try {
            String jwt = httpRequest.getHeader("Authorization");
            claims = JWTGenerator.decodeJWT(jwt);
            String SecretKey = claims.get("authToken", String.class);
            String oidcAuthontication = "Bearer " + SecretKey;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", oidcAuthontication);
            String teamsApiUri = "";
            if(offset > 0) {
            	 teamsApiUri = teamsApiBaseUri + personSearchUri + "?order=default&fetchSize=" + offset + "&query=" + searchTerm;
            }
            else {
            	 teamsApiUri = teamsApiBaseUri + personSearchUri + "?order=default&fetchSize=" + teamsApiResultFetchSize + "&query=" + searchTerm;
            }
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<TeamsApiResponseWrapperDto> response = restTemplate.exchange(teamsApiUri, HttpMethod.GET, entity, TeamsApiResponseWrapperDto.class);
            if (response != null && response.hasBody()) {
                LOGGER.debug("Successfully fetched user details from teamsApi");
                if (response.getBody().getEntries()!= null) {
                    usersCollection= new UsersCollection();
                    userInfoVOList = userInfoAssembler.toUserInfoVo(response.getBody().getEntries());
                    totalCount = response.getBody().getTotalHits();
                    usersCollection.setRecords(userInfoVOList);
                    usersCollection.setTotalCount(totalCount);
                }
            }
        } catch (Exception e) {
            LOGGER.error("exception occurred calling teamsApi:{}", e.getMessage());
        }

        return usersCollection;
    }


}

