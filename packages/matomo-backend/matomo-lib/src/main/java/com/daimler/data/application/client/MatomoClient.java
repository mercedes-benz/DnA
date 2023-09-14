package com.daimler.data.application.client;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Component
@Slf4j
public class MatomoClient {

    @Value("${matomo.tokenAuth}")
    private String matomoTokenAuth;

    @Value("${matomo.uri.base}")
    private String matomoBaseUri;

    @Value("${matomo.uri.addSite}")
    private String matomoAddSitePath;
    @Value("${matomo.uri.getUser}")
    private String matomoGetUserPath;
    @Value("${matomo.uri.addUser}")
    private String matomoAddUserPath;
    @Value("${matomo.uri.getSite}")
    private String matomoGetSitePath;
    @Value("${matomo.uri.setUserAccess}")
    private String matomoSetUserAccessPath;
    @Value("${matomo.uri.deleteSite}")
    private String matomoDeleteSitePath;
    @Value("${matomo.uri.getUserAccess}")
    private String matomoGetUserAccessPath;

    @Value("${matomo.uri.getSitesAccess}")
    private String matomoGetSitesAccessPath;





    @Autowired
    private RestTemplate restTemplate;

    public MatomoSiteResponseDto addMatomoSite(String siteName, String siteUrl) {
        MatomoSiteResponseDto addMatomoSiteResponse = null;
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String addSiteUrl = matomoBaseUri + matomoAddSitePath + matomoTokenAuth +"&siteName=" +siteName +"&urls=" +siteUrl;
            HttpEntity requestEntity = new HttpEntity<>(headers);
            ResponseEntity<MatomoSiteResponseDto> response = restTemplate.exchange(addSiteUrl, HttpMethod.GET,
                    requestEntity, MatomoSiteResponseDto.class);
            if (response.hasBody()) {
                addMatomoSiteResponse = response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to invoke matomo add site api with {}" + e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed to invoke matomo add site api with exception " + e.getMessage());
            errors.add(errMsg);
            addMatomoSiteResponse.setErrors(errors);
            addMatomoSiteResponse.setStatus("FAILED");
        }
        return addMatomoSiteResponse;
    }

    public MatomoUserResponseDto createMatomoUser(String userId, String userEmail) {
        MatomoUserResponseDto createUserResponse = null;
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String getUserUrl = matomoBaseUri + matomoGetUserPath + matomoTokenAuth + "&userLogin=" + userId;
            HttpEntity requestEntity = new HttpEntity<>(headers);
            ResponseEntity<MatomoUserResponseDto> getMatomoUserResponse = restTemplate.exchange(getUserUrl, HttpMethod.GET,
                    requestEntity, MatomoUserResponseDto.class);
            if (getMatomoUserResponse.hasBody()) {
                createUserResponse = getMatomoUserResponse.getBody();
                if (createUserResponse == null || (createUserResponse != null && ("error".equalsIgnoreCase(createUserResponse.getResult())) && createUserResponse.getMessage() != null)) {

                    MessageDescription getUserResponseErrMsg = new MessageDescription(createUserResponse.getMessage());
                    String userNotExistMessage = "User '" + userId + "' doesn't exist.";
                    if (userNotExistMessage.equalsIgnoreCase(createUserResponse.getMessage())) {


                        String addMatomoUserUrl = matomoBaseUri + matomoAddUserPath + matomoTokenAuth +"&userLogin=" + userId + "&password=" + userId + "&email=" + userEmail;
                        ResponseEntity<MatomoUserResponseDto> addMatomoUserResponse = restTemplate.exchange(addMatomoUserUrl, HttpMethod.GET,
                                requestEntity, MatomoUserResponseDto.class);
                        if (addMatomoUserResponse.hasBody()) {
                            createUserResponse = addMatomoUserResponse.getBody();
                            if (createUserResponse != null || (createUserResponse != null && ("SUCCESS".equalsIgnoreCase(createUserResponse.getResult())))) {
                                createUserResponse.setStatus("SUCCESS");
                            } else {
                                MessageDescription addUserResponseErrMsg = new MessageDescription(createUserResponse.getMessage());
                                errors.add(addUserResponseErrMsg);
                                createUserResponse.setErrors(errors);
                                createUserResponse.setStatus("FAILED");

                            }
                        }
                    } else {
                        errors.add(getUserResponseErrMsg);
                        createUserResponse.setErrors(errors);
                        createUserResponse.setStatus("FAILED");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed adding user to matomo site  userLogin {} with {}" + userId, e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed adding user to matomo site with exception " + e.getMessage());
            errors.add(errMsg);
            createUserResponse.setErrors(errors);
            createUserResponse.setStatus("FAILED");
        }
        return createUserResponse;
    }


    public MatomoSetUserAccessResponseDto setUserAccess(String siteId, String userId, String permission,Boolean isCreatedUser){
        MatomoSetUserAccessResponseDto setUserAcessResponse = null;
        MatomoGetSiteResponseDto getSiteResponse =null;
        List<MessageDescription> errors = new ArrayList<>();
        String access="";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            if(isCreatedUser){
                access="admin";

            }
            else {
                access=permission;

            }
            HttpEntity requestEntity = new HttpEntity<>(headers);
                    //setUserAccess
                    String setUserAccessUrl =  matomoBaseUri + matomoSetUserAccessPath + matomoTokenAuth +"&userLogin=" + userId + "&access=" +access+ "&idSites=" + siteId;

                    ResponseEntity<MatomoSetUserAccessResponseDto> setMatomoUserAccessResponse = restTemplate.exchange(setUserAccessUrl, HttpMethod.GET,
                            requestEntity, MatomoSetUserAccessResponseDto.class);

                    if (setMatomoUserAccessResponse.hasBody()) {
                        setUserAcessResponse = setMatomoUserAccessResponse.getBody();

                        if ((setUserAcessResponse != null && ("success".equalsIgnoreCase(setUserAcessResponse.getResult())))) {
                            setUserAcessResponse.setStatus("SUCCESS");
                        }
                        else{
                            MessageDescription setUserAcessResponseErrMsg = new MessageDescription(setUserAcessResponse.getMessage());
                            errors.add(setUserAcessResponseErrMsg);
                            setUserAcessResponse.setErrors(errors);
                            setUserAcessResponse.setStatus("FAILED");


                    }
                }

        }catch (Exception e) {
            log.error("Failed while setting access to user  of userLogin {} with {}" + userId, e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed while setting access to user  with exception " + e.getMessage());
            errors.add(errMsg);
            setUserAcessResponse.setErrors(errors);
            setUserAcessResponse.setStatus("FAILED");
        }
        return setUserAcessResponse;
    }

    public MatomoSiteResponseDto deleteMatomoSite(String siteId){
        MatomoSiteResponseDto deleteMatomoSiteResponse = null;
        List<MessageDescription> errors = new ArrayList<>();
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Accept", "application/json");
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    String deleteSiteUrl =  matomoBaseUri + matomoDeleteSitePath + matomoTokenAuth +"&idSite=" +siteId;
                    HttpEntity requestEntity = new HttpEntity<>(headers);
                    ResponseEntity<MatomoSiteResponseDto> response = restTemplate.exchange(deleteSiteUrl, HttpMethod.GET,
                            requestEntity, MatomoSiteResponseDto.class);
                    if (response.hasBody()) {
                        deleteMatomoSiteResponse = response.getBody();
                        MessageDescription errMsg = new MessageDescription("Successfully deleted the matomo site");
                        errors.add(errMsg);
                        deleteMatomoSiteResponse.setStatus("SUCCESS");

            }
        } catch (Exception e) {
            log.error("Failed to invoke matomo delete site api with {}" + e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed to delete matomo add site api with exception " + e.getMessage());
            errors.add(errMsg);
            deleteMatomoSiteResponse.setErrors(errors);
            deleteMatomoSiteResponse.setStatus("FAILED");
        }
        return deleteMatomoSiteResponse;
    }


    public MatomoGetSiteResponseDto listParticularMatomoSite(String siteId){
        MatomoGetSiteResponseDto getSiteResponse =null;
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String getSiteFromIdUrl =  matomoBaseUri + matomoGetSitePath + matomoTokenAuth +"&idSite=" + siteId;
            HttpEntity requestEntity = new HttpEntity<>(headers);
            ResponseEntity<MatomoGetSiteResponseDto> response = restTemplate.exchange(getSiteFromIdUrl, HttpMethod.GET,
                    requestEntity, MatomoGetSiteResponseDto.class);
            if (response.hasBody()) {
                getSiteResponse = response.getBody();
                if (getSiteResponse == null || (getSiteResponse != null && ("error".equalsIgnoreCase(getSiteResponse.getResult())) && getSiteResponse.getMessage() != null)) {
                    MessageDescription getSiteResponseErrMsg = new MessageDescription(getSiteResponse.getMessage());
                    errors.add(getSiteResponseErrMsg);
                    getSiteResponse.setErrors(errors);
                    getSiteResponse.setStatus("FAILED");
                }
                else{
                    getSiteResponse.setStatus("SUCCESS");
                }
                }
        } catch (Exception e) {
            log.error("Failed to get particular site details site {} api with {}" + siteId,e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed to get particular site details  with exception " + e.getMessage());
            errors.add(errMsg);
            getSiteResponse.setErrors(errors);
            getSiteResponse.setStatus("FAILED");
        }
        return getSiteResponse;
    }

    public Map<String, Object> getUsersAccessFromSite(String userId, String siteId){
        Map<String, Object> map = new HashMap<>();
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String getSiteAccessFromUserUrl =  matomoBaseUri + matomoGetUserAccessPath + matomoTokenAuth + "&idSite=" +siteId;
            HttpEntity requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(getSiteAccessFromUserUrl, HttpMethod.GET,
                    requestEntity, String.class);
            if (response!=null && response.hasBody()) {
                JSONObject jsonObj = new JSONObject(response.getBody());
                Iterator<String> keys = jsonObj.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObj.get(key);
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get  user access from site {} api with {}" + siteId,e.getMessage());
        }
       return  map;
    }



    public MatomoGetSitesAccessCollectionDto getSitesAccessFromUser(String user){
        MatomoGetSitesAccessCollectionDto getSitesAccessCollection = new MatomoGetSitesAccessCollectionDto();
        List<MatomoGetSitesAccessDto> getSitesAccess = new ArrayList<>();
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String getSitesAccessFromUserUrl =  matomoBaseUri + matomoGetSitesAccessPath + matomoTokenAuth +"&userLogin="+user;
            HttpEntity requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(getSitesAccessFromUserUrl, HttpMethod.GET,
                    requestEntity, String.class);
            if (response.hasBody()) {

                JSONArray response_json = new JSONArray(response.getBody());
                for(int i=0; i<response_json.length(); i++) {
                    MatomoGetSitesAccessDto getSiteAccessObj = new MatomoGetSitesAccessDto();
                    JSONObject jsonObject = (JSONObject) response_json.get(i);
                    if(Objects.nonNull(jsonObject.get("site"))) {
                        String siteId = jsonObject.get("site").toString();
                        getSiteAccessObj.setSite(siteId);

                    }
                    if(Objects.nonNull(jsonObject.get("access"))) {
                        String siteAccess = (String)jsonObject.get("access");
                        getSiteAccessObj.setAccess(siteAccess);

                    }

                    getSitesAccess.add(getSiteAccessObj);
                }
            }
            if (getSitesAccess != null && !getSitesAccess.isEmpty()) {
                getSitesAccessCollection.setData(getSitesAccess);
                getSitesAccessCollection.setStatus("SUCCESS");
            }


        } catch (Exception e) {
            log.error("Failed to get particular site details site {} api with {}" + user,e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed to get particular site details. The user" + user + "doesn't have matomo sites created" );
            errors.add(errMsg);
            getSitesAccessCollection.setErrors(errors);
            getSitesAccessCollection.setStatus("FAILED");
        }
        return getSitesAccessCollection;
    }








}




