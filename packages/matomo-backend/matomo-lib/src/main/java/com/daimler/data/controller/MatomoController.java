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
import java.util.concurrent.atomic.AtomicBoolean;

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
                        MessageDescription errMsg = new MessageDescription( "Duplicate entry for collaborator " + collaborator.getId());
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
            MessageDescription errMsg = new MessageDescription(matomoAddSiteResponse.getMessage());
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
            if ((createUserResponse != null && ("SUCCESS".equalsIgnoreCase(createUserResponse.getStatus())))) {
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
            else if((createUserResponse != null && ("FAILED".equalsIgnoreCase(createUserResponse.getResult())))){
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
                    if ((createCollaboratorResponse != null && ("SUCCESS".equalsIgnoreCase(createCollaboratorResponse.getStatus())))) {
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
                    else if((createCollaboratorResponse != null && ("FAILED".equalsIgnoreCase(createCollaboratorResponse.getResult())))){
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

        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
        GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        MatomoVO existingMatomo = service.getById(id);
        CreatedByVO requestUser = this.userStore.getVO();
        String user = requestUser.getId();
        if(existingMatomo==null || !id.equalsIgnoreCase(existingMatomo.getId())) {
            log.warn("No matomo site found with id {}, failed to fetch details for given matomo id", id);
            responseMessage.setSuccess("FAILED");
            MessageDescription errMsg = new MessageDescription("Matomo ID Not found!");
            errors.add(errMsg);
            responseMessage.setErrors(errors);
            log.error("Matomo ID Not found!");
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }

        responseMessage = service.deleteMatomoByID(id,user);

        if (responseMessage != null && "SUCCESS".equalsIgnoreCase(responseMessage.getSuccess())) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }

        return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
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
        MatomoCollectionVO collection = new MatomoCollectionVO();
        int defaultLimit = 10;
        if (offset == null || offset < 0)
            offset = 0;
        if (limit == null || limit < 0) {
            limit = defaultLimit;
        }
        CreatedByVO requestUser = this.userStore.getVO();
        String user = requestUser.getId();
        MatomoCollectionVO matomoCollectionWrapper = service.getAll(limit, offset, user);


        HttpStatus responseCode = HttpStatus.NO_CONTENT;
        if(matomoCollectionWrapper.getRecords()!=null ) {

            responseCode = HttpStatus.OK;
        }
        return new ResponseEntity<>(matomoCollectionWrapper, responseCode);

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
       MatomoVO existingMatomo = service.getById(id);
        CreatedByVO requestUser = this.userStore.getVO();
        String user = requestUser.getId();
        if(existingMatomo==null || !id.equalsIgnoreCase(existingMatomo.getId())) {
            log.warn("No matomo site found with id {}, failed to fetch details for given matomo id", id);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        MatomoVO matomoData = service.getMatomoById(id,user);
        return new ResponseEntity<>(matomoData, HttpStatus.OK);
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
        MatomoVO existingMatomo = service.getById(id);
        List<MessageDescription> errors = new ArrayList<>();
        GenericMessage responseMessage = new GenericMessage();
        MatomoResponseVO responseVO = new MatomoResponseVO();
        MatomoSetUserAccessResponseDto setCollaboratorUserAccess =null;
        List<CollaboratorVO> allCollaborators = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        CreatedByVO requestUser = this.userStore.getVO();
        String user = requestUser.getId();
        boolean isAdmin = false;
        // if existingMatomo is null return not found.

        if (existingMatomo == null) {
            responseMessage.setSuccess("FAILED");
            MessageDescription errMsg = new MessageDescription("Matomo ID Not found!");
            errors.add(errMsg);
            responseMessage.setErrors(errors);
            log.error("Matomo ID Not found!");
            responseVO.setResponse(responseMessage);
            return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
        }

        List<String> existingCollaborators = new ArrayList<>();
        map = matomoClient.getUsersAccessFromSite(existingMatomo.getSiteId());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase(user)) {
                String userPermission = entry.getValue().toString();
                if ("admin".equalsIgnoreCase(userPermission)) {
                    isAdmin = true;
                }
            }

            existingCollaborators.add(entry.getKey());

        }

        if (matomoUpdateRequestVO.getAddCollaborators() != null && matomoUpdateRequestVO.getAddCollaborators().size() > 0) {
            // To check if user is collaborator in the getAddCollaborators list.
            CollaboratorVO exstingcollaboratorisCreator = matomoUpdateRequestVO.getAddCollaborators().stream().filter(x -> existingMatomo.getCreatedBy().getId().equalsIgnoreCase(x.getId())).findAny().orElse(null);
            if (exstingcollaboratorisCreator != null && exstingcollaboratorisCreator.getId() != null) {
                GenericMessage responseMessg = new GenericMessage();
                responseMessg.setSuccess("FAILED");
                MessageDescription errMsg = new MessageDescription(existingMatomo.getCreatedBy().getId() + " is already a Creator and can not be added as a collaborator");
                errors.add(errMsg);
                responseMessg.setErrors(errors);
                log.error(errMsg.getMessage());
                responseVO.setResponse(responseMessg);
                return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
            }

            AtomicBoolean isCollabExits = new AtomicBoolean(false);

            matomoUpdateRequestVO.getAddCollaborators().stream().forEach(collab -> {
                String existingCollaborator = existingCollaborators.stream().filter(x -> collab.getId().equalsIgnoreCase(x)).findAny().orElse(null);
                GenericMessage responseMessg = new GenericMessage();
                if (existingCollaborator != null) {
                    isCollabExits.set(true);
                    responseMessg.setSuccess("FAILED");
                    MessageDescription errMsg = new MessageDescription(existingCollaborator + " collaborator is already present");
                    errors.add(errMsg);
                    responseMessg.setErrors(errors);
                    log.error(errMsg.getMessage());
                    responseVO.setResponse(responseMessg);
                }
            });
            if (isCollabExits.get()) {
                return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
            }
        }

        if(isAdmin) {
            MatomoSiteResponseDto updateMatomoSiteResponse = matomoClient.updateMatomoSite(matomoUpdateRequestVO.getSiteName(), matomoUpdateRequestVO.getSiteUrl(), existingMatomo.getSiteId());
            if (updateMatomoSiteResponse == null || (updateMatomoSiteResponse != null && ("error".equalsIgnoreCase(updateMatomoSiteResponse.getResult())))) {

                GenericMessage errorMessage = new GenericMessage();
                MessageDescription errMsg = new MessageDescription(updateMatomoSiteResponse.getMessage());
                errors.add(errMsg);
                errorMessage.setSuccess("FAILED");
                errorMessage.setErrors(errors);
                errorMessage.setWarnings(null);
                responseVO.setData(null);
                responseVO.setResponse(errorMessage);
                return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
            } else if ((updateMatomoSiteResponse != null && ("SUCCESS".equalsIgnoreCase(updateMatomoSiteResponse.getResult())))) {

                if (matomoUpdateRequestVO.getAddCollaborators() != null && matomoUpdateRequestVO.getAddCollaborators().size() > 0) {
                    allCollaborators.addAll(matomoUpdateRequestVO.getAddCollaborators());
                }
                if (matomoUpdateRequestVO.getExistingCollaborators() != null && matomoUpdateRequestVO.getExistingCollaborators().size() > 0) {
                    allCollaborators.addAll(matomoUpdateRequestVO.getExistingCollaborators());
                }
                if (allCollaborators != null && allCollaborators.size() > 0) {
                    boolean isCreatedUser = false;
                    for (CollaboratorVO collaborator : allCollaborators) {

                        MatomoUserResponseDto createCollaboratorResponse = matomoClient.createMatomoUser(collaborator.getId(), collaborator.getEmail());
                        if ((createCollaboratorResponse != null && ("SUCCESS".equalsIgnoreCase(createCollaboratorResponse.getStatus())))) {
                            setCollaboratorUserAccess = matomoClient.setUserAccess(existingMatomo.getSiteId(), collaborator.getId(), collaborator.getPermission(), isCreatedUser);
                            if (setCollaboratorUserAccess == null || (setCollaboratorUserAccess != null && ("error".equalsIgnoreCase(setCollaboratorUserAccess.getResult())) && setCollaboratorUserAccess.getMessage() != null)) {
                                GenericMessage errorMessage = new GenericMessage();
                                errorMessage.setSuccess("FAILED");
                                errorMessage.setErrors(setCollaboratorUserAccess.getErrors());
                                errorMessage.setWarnings(setCollaboratorUserAccess.getWarnings());
                                responseVO.setData(null);
                                responseVO.setResponse(errorMessage);
                                return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                            }

                        } else if ((createCollaboratorResponse != null && ("FAILED".equalsIgnoreCase(createCollaboratorResponse.getResult())))) {
                            GenericMessage errorMessage = new GenericMessage();
                            errorMessage.setSuccess("FAILED");
                            errorMessage.setErrors(createCollaboratorResponse.getErrors());
                            errorMessage.setWarnings(createCollaboratorResponse.getWarnings());
                            responseVO.setData(null);
                            responseVO.setResponse(errorMessage);
                            return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }

                }
                if (matomoUpdateRequestVO.getRemoveCollaborators() != null && matomoUpdateRequestVO.getRemoveCollaborators().size() > 0) {
                    boolean isCreatedUser = false;
                    for (CollaboratorVO collaborator : matomoUpdateRequestVO.getRemoveCollaborators()) {
                        setCollaboratorUserAccess = matomoClient.setUserAccess(existingMatomo.getSiteId(), collaborator.getId(), collaborator.getPermission(), isCreatedUser);
                        if (setCollaboratorUserAccess == null || (setCollaboratorUserAccess != null && ("error".equalsIgnoreCase(setCollaboratorUserAccess.getResult())) && setCollaboratorUserAccess.getMessage() != null)) {
                            GenericMessage errorMessage = new GenericMessage();
                            errorMessage.setSuccess("FAILED");
                            errorMessage.setErrors(setCollaboratorUserAccess.getErrors());
                            errorMessage.setWarnings(setCollaboratorUserAccess.getWarnings());
                            responseVO.setData(null);
                            responseVO.setResponse(errorMessage);
                            return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }

                }

                MatomoResponseVO updateSiteResponse = service.updateMatomoSiteById(matomoUpdateRequestVO, id, allCollaborators);
                if(updateSiteResponse!= null && "SUCCESS".equalsIgnoreCase(updateSiteResponse.getResponse().getSuccess())) {
                    return new ResponseEntity<>(updateSiteResponse, HttpStatus.OK);
                }
            }
        }
        else{
            log.error("Failed while updating matomo site {} as user {} is not admin",existingMatomo.getSiteId(), user);
            GenericMessage errorMessage = new GenericMessage();
            MessageDescription msg = new MessageDescription("Failed while updating matomo site as user " +user+" is not admin");
            errors.add(msg);
            errorMessage.setSuccess("FAILED");
            errorMessage.setErrors(errors);
            errorMessage.setWarnings(null);
            responseVO.setData(null);
            responseVO.setResponse(errorMessage);
            return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
