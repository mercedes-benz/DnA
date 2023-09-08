package com.daimler.data.controller;
import com.daimler.data.api.matomo.MatomoSitesApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.MatomoClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.MatomoSiteResponseDto;
import com.daimler.data.dto.MatomoUserResponseDto;
import com.daimler.data.dto.MatomoSetUserAccessResponseDto;
import com.daimler.data.dto.matomo.*;
import com.daimler.data.service.matomo.MatomoService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Api(value = "Matomo APIs")
@RequestMapping("/api")
@Slf4j
public class MatomoController implements MatomoSitesApi {

    @Autowired
    private UserStore userStore;

    @Autowired
    private MatomoClient matomoClient;
    @Autowired
    private MatomoService service;
    @Override
    @ApiOperation(value = "Initialize/Create matomo site for user.", nickname = "createMatomoSite", notes = "Create matomo site for user ", response = MatomoResponseVO.class, tags={ "matomo-sites", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure ", response = MatomoResponseVO.class),
            @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/matomo",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    public ResponseEntity<MatomoResponseVO> createMatomoSite(@ApiParam(value = "Request Body that contains data required to initialize matomo site for user" ,required=true )  @Valid @RequestBody MatomoSiteCreateRequestWrapperVO matomoRequestVO) {
        MatomoResponseVO responseVO = new MatomoResponseVO();
        MatomoSiteRequestVO matomoSiteCreateVO = matomoRequestVO.getData();
        GenericMessage responseMessage = new GenericMessage();
        CreatedByVO requestUser = this.userStore.getVO();
        boolean isCreatedUser = true;
        MatomoSetUserAccessResponseDto setUserAccess =null;
        MatomoSetUserAccessResponseDto setCollaboratorUserAccess =null;
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
        Date createdOn = new Date();
        Date lastModified = new Date();
        try {
            createdOn = isoFormat.parse(isoFormat.format(createdOn));
            lastModified = isoFormat.parse(isoFormat.format(lastModified));
        }catch(Exception e) {
            log.warn("Failed to format createdOn date to ISO format");
        }
        List<MessageDescription> errors = new ArrayList();
        if (matomoSiteCreateVO !=null && matomoSiteCreateVO.getCollaborators() != null && matomoSiteCreateVO.getCollaborators().size() > 0) {
            // To check if user is collaborator in the getAddCollaborators list.
            CollaboratorVO exstingcollaboratorisCreator = matomoSiteCreateVO.getCollaborators().stream().filter(x -> requestUser.getId().equalsIgnoreCase(x.getId())).findAny().orElse(null);
            if (exstingcollaboratorisCreator != null && exstingcollaboratorisCreator.getId() != null) {

                responseMessage.setSuccess("FAILED");
                MessageDescription errMsg = new MessageDescription(requestUser.getId() + " is already a Creator and can not be added as a collaborator");
                errors.add(errMsg);
                responseMessage.setErrors(errors);
                log.error(errMsg.getMessage());
                responseVO.setResponse(responseMessage);
                return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
            }

            if (matomoSiteCreateVO.getCollaborators() != null) {
                // To check if user is already present in the existingForecast Collaborators list.
                Set<String> seenIds = new HashSet<>();
                for (CollaboratorVO collaborator : matomoSiteCreateVO.getCollaborators()) {
                    if (seenIds.contains(collaborator.getId())) {
                        // duplicate id found.
                        responseMessage.setSuccess("FAILED");
                        com.daimler.data.controller.exceptions.MessageDescription errMsg = new com.daimler.data.controller.exceptions.MessageDescription( "Duplicate entry for collaborator " + collaborator.getId());
                        errors.add(errMsg);
                        responseMessage.setErrors(errors);
                        log.error(errMsg.getMessage());
                        responseVO.setResponse(responseMessage);
                        return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
                    } else {
                        seenIds.add(collaborator.getId());
                    }
                }
            }
        }

        MatomoSiteResponseDto matomoAddSiteResponse = matomoClient.addMatomoSite(matomoSiteCreateVO.getSiteName(),matomoSiteCreateVO.getSiteUrl());
        if(matomoAddSiteResponse==null || (matomoAddSiteResponse!=null && ("error".equalsIgnoreCase(matomoAddSiteResponse.getResult())) && matomoAddSiteResponse.getMessage()!=null)) {

            GenericMessage errorMessage = new GenericMessage();
            com.daimler.data.controller.exceptions.MessageDescription errMsg = new MessageDescription(matomoAddSiteResponse.getMessage());
            errors.add(errMsg);
            errorMessage.setSuccess("FAILED");
            errorMessage.setErrors(errors);
            errorMessage.setWarnings(null);
            responseVO.setData(null);
            responseVO.setResponse(errorMessage);
            return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        }else if(matomoAddSiteResponse.getValue()!=null) {
            /// add user
            String siteId = matomoAddSiteResponse.getValue();
            MatomoUserResponseDto createUserResponse = matomoClient.createMatomoUser(requestUser.getId(), requestUser.getEmail());
            if (createUserResponse != null || (createUserResponse != null && ("SUCCESS".equalsIgnoreCase(createUserResponse.getResult())))) {
                 setUserAccess = matomoClient.setUserAccess(siteId,requestUser.getId(),matomoSiteCreateVO.getPermission(),isCreatedUser);

                if(setUserAccess==null || (setUserAccess!=null && ("error".equalsIgnoreCase(setUserAccess.getResult())) && setUserAccess.getMessage()!=null)) {
                    GenericMessage errorMessage = new GenericMessage();
                    errorMessage.setSuccess("FAILED");
                    errorMessage.setErrors(setUserAccess.getErrors());
                    errorMessage.setWarnings(setUserAccess.getWarnings());
                    responseVO.setData(null);
                    responseVO.setResponse(errorMessage);
                    return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else{
                GenericMessage errorMessage = new GenericMessage();
                errorMessage.setSuccess("FAILED");
                errorMessage.setErrors(createUserResponse.getErrors());
                errorMessage.setWarnings(createUserResponse.getWarnings());
                responseVO.setData(null);
                responseVO.setResponse(errorMessage);
                return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ////add collaborator
            if (matomoSiteCreateVO.getCollaborators() != null) {
                for (CollaboratorVO collaborator : matomoSiteCreateVO.getCollaborators()) {
                    isCreatedUser=false;
                    MatomoUserResponseDto createCollaboratorResponse = matomoClient.createMatomoUser(collaborator.getId(), collaborator.getEmail());
                    if (createCollaboratorResponse != null || (createCollaboratorResponse != null && ("SUCCESS".equalsIgnoreCase(createCollaboratorResponse.getResult())))) {
                         setCollaboratorUserAccess = matomoClient.setUserAccess(siteId,collaborator.getId(),collaborator.getPermission(),isCreatedUser);
                        if(setCollaboratorUserAccess==null || (setCollaboratorUserAccess!=null && ("error".equalsIgnoreCase(setCollaboratorUserAccess.getResult())) && setCollaboratorUserAccess.getMessage()!=null)){
                            GenericMessage errorMessage = new GenericMessage();
                            errorMessage.setSuccess("FAILED");
                            errorMessage.setErrors(setCollaboratorUserAccess.getErrors());
                            errorMessage.setWarnings(setCollaboratorUserAccess.getWarnings());
                            responseVO.setData(null);
                            responseVO.setResponse(errorMessage);
                            return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                        }

                    }
                    else{
                        GenericMessage errorMessage = new GenericMessage();
                        errorMessage.setSuccess("FAILED");
                        errorMessage.setErrors(createUserResponse.getErrors());
                        errorMessage.setWarnings(createUserResponse.getWarnings());
                        responseVO.setData(null);
                        responseVO.setResponse(errorMessage);
                        return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }

            }

            String matomoId = UUID.randomUUID().toString();
            MatomoVO matomoVO = new MatomoVO();
            matomoVO.setId(matomoId);
            matomoVO.setSiteId(siteId);



            if(setUserAccess!=null && setCollaboratorUserAccess!=null && "SUCCESS".equalsIgnoreCase(setUserAccess.getResult()) && "SUCCESS".equalsIgnoreCase(setCollaboratorUserAccess.getResult()) ) {
                MatomoResponseVO createMatomoSiteResponse = service.createMatomoSite(matomoId,siteId,createdOn,lastModified,matomoSiteCreateVO,requestUser);

                if(createMatomoSiteResponse!= null && "SUCCESS".equalsIgnoreCase(createMatomoSiteResponse.getResponse().getSuccess())) {
                    return new ResponseEntity<>(createMatomoSiteResponse, HttpStatus.CREATED);
                }else {
                    MatomoSiteResponseDto deleteMatomoSiteResponse = matomoClient.deleteMatomoSite(siteId);
                    GenericMessage errorMessage = new GenericMessage();
                    errorMessage.setSuccess(deleteMatomoSiteResponse.getStatus());
                    errorMessage.setErrors(deleteMatomoSiteResponse.getErrors());
                    errorMessage.setWarnings(createUserResponse.getWarnings());
                    responseVO.setData(null);
                    responseVO.setResponse(errorMessage);
                    return new ResponseEntity<>(createMatomoSiteResponse, HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }


        }


        return null;
    }

    @Override
    @ApiOperation(value = "delete matomo details for a given Id.", nickname = "deleteById", notes = "delete matomo details for a given Id.", response = GenericMessage.class, tags={ "matomo-sites", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/matomo/{id}/sites",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteById(@ApiParam(value = "matomo ID to be delete",required=true) @PathVariable("id") String id) {
        return null;
    }

    @Override
    @ApiOperation(value = "Get all matomo sites for the user.", nickname = "getAll", notes = "Get all matomo sites for the user.", response = MatomoCollectionVO.class, tags={ "matomo-sites", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = MatomoCollectionVO.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/matomo",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity<MatomoCollectionVO> getAll(@ApiParam(value = "page number from which listing of matomo should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
                                                     @ApiParam(value = "page size to limit the number of matomo, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
        return null;
    }

    @Override
    @ApiOperation(value = "Get matomo details for a given Id.", nickname = "getById", notes = "Get matomo details for a given Id.", response = MatomoVO.class, tags={ "matomo-sites", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns message of success or failure", response = MatomoVO.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/matomo/{id}/sites",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity<MatomoVO> getById(@ApiParam(value = "matomo ID to be fetched",required=true) @PathVariable("id") String id) {
        return null;
    }

    @Override
    @ApiOperation(value = "update matomo details for a given Id.", nickname = "updateById", notes = "update matomo details for a given Id.", response = MatomoResponseVO.class, tags={ "matomo-sites", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns message of success or failure", response = MatomoResponseVO.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/matomo/{id}/sites",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.PUT)
    public ResponseEntity<MatomoResponseVO> updateById(@ApiParam(value = "matomo ID to be updated",required=true) @PathVariable("id") String id,@ApiParam(value = "Request Body that contains data required for updating of collab details" ,required=true )  @Valid @RequestBody MatomoSiteUpdateRequestVO matomoUpdateRequestVO) {
        return null;
    }


}
