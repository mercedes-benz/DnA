package com.daimler.data.controller;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.dbService.DbServiceApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.repo.DbService.DbServiceCustomRepository;
import com.daimler.data.dto.dbService.CreatedByVO;
import com.daimler.data.dto.dbService.DbServiceCollectionVO;
import com.daimler.data.dto.dbService.DbServiceVO;
import com.daimler.data.dto.dbService.GenericMessage;
import com.daimler.data.dto.dbService.InitializeResponseVo;
import com.daimler.data.dto.dbService.MessageDescription;
import com.daimler.data.dto.dbService.UserInfoVO;
import com.daimler.data.service.DbService.DbServicies;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "DbService API", tags = { "Db-service" })
@RequestMapping("/api")
@Slf4j
public class DbServiceController implements DbServiceApi {

    @Autowired
	private UserStore userStore;

    @Autowired
    private DbServiceCustomRepository dbServiceRepo;

    @Autowired
    private DbServicies service;


    @Override
    @ApiOperation(value = "Initialize/Create dbService. ", nickname = "createdbService", notes = "Initialize/Create dbService ", response = InitializeResponseVo.class, tags={ "dbService", })
    @ApiResponses(value = { 

        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeResponseVo.class),

        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),

        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),

        @ApiResponse(code = 403, message = "Request is not authorized."),

        @ApiResponse(code = 405, message = "Method not allowed"),

        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/dbService",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    public ResponseEntity<InitializeResponseVo> createdbService(@ApiParam(value = "Request Body that contains data required to initialize dbService for user", required = true )  
                 @Valid @RequestBody DbServiceVO serviceVO) {
        InitializeResponseVo response = new InitializeResponseVo();
        try {
            CreatedByVO currentUser = this.userStore.getVO();
            UserInfoVO userVo = new UserInfoVO();
            BeanUtils.copyProperties(currentUser, userVo);            
            if(serviceVO != null && serviceVO.getServiceName() != null &&
             !serviceVO.getServiceName().isBlank() && !serviceVO.getServiceName() .isEmpty()){
                DbServiceNsql entity = dbServiceRepo.findbyUniqueLiteral("serviceName", serviceVO.getServiceName());
                if(entity != null){
                    log.debug("name already present");
                    MessageDescription exceptionMsg = new MessageDescription();
                    exceptionMsg.setMessage("name already present.");
                    response.addErrorsItem(exceptionMsg);
                    response.setSuccess("FAILED");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }else{
                    userVo.setIsAdmin(true);
                    serviceVO.setModifiedBy(userVo);
                    serviceVO.setProjectOwner(userVo);
                    serviceVO.setServiceName(serviceVO.getServiceName().toLowerCase());
                    response = service.createDb(serviceVO);
                    if(response.getSuccess().equalsIgnoreCase("failed")){
                        log.error("Failed to create dbService");
                        MessageDescription exceptionMsg = new MessageDescription();
                        exceptionMsg.setMessage("Failed to create DbService due to internal error.");
                        response.addErrorsItem(exceptionMsg);
                        response.setSuccess("FAILED");
                        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                    }else{
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }

                }

            }else{
                log.debug("name cannot be empty");
				MessageDescription exceptionMsg = new MessageDescription();
                exceptionMsg.setMessage("name cannot be empty.");
			    response.addErrorsItem(exceptionMsg);
                response.setSuccess("FAILED");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Failed to create dbService  exception {}",e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription();
            exceptionMsg.setMessage("Failed to create DbService due to internal error.");
			response.addErrorsItem(exceptionMsg);
            response.setSuccess("FAILED");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    @ApiOperation(value = "edit dbService. ", nickname = "editdbService", notes = "edit dbService ", response = InitializeResponseVo.class, tags={ "dbService", })
    @ApiResponses(value = { 

        @ApiResponse(code = 201, message = "Returns message of success or failure ", response = InitializeResponseVo.class),

        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),

        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),

        @ApiResponse(code = 403, message = "Request is not authorized."),

        @ApiResponse(code = 405, message = "Method not allowed"),

        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(
        method = RequestMethod.PATCH,
        value = "/dbService",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    public ResponseEntity<InitializeResponseVo> editdbService(@ApiParam(value = "Request Body that contains data required to initialize dbService for user", required = true )   @Valid @RequestBody DbServiceVO serviceVO) {
        InitializeResponseVo response = new InitializeResponseVo();
        try {
            CreatedByVO currentUser = this.userStore.getVO();
            UserInfoVO userVo = new UserInfoVO();
            UserInfoVO user = null;
            BeanUtils.copyProperties(currentUser, userVo); 
            DbServiceVO existingVo = service.getById(serviceVO.getId());           
            if(serviceVO != null && serviceVO.getServiceName() != null &&
             !serviceVO.getServiceName().isBlank() && !serviceVO.getServiceName() .isEmpty()){                
                if(existingVo != null){
                    if(existingVo.getProjectOwner() != null && existingVo.getProjectOwner().getId().equalsIgnoreCase(currentUser.getId())){
                        user =   existingVo.getProjectOwner();
                    }else if(existingVo.getProjectCollaborators() != null && existingVo.getProjectCollaborators().stream().anyMatch(i -> i.getId().equalsIgnoreCase(currentUser.getId()))){
                        user  = existingVo.getProjectCollaborators().stream().filter(i -> i.getId().equalsIgnoreCase(currentUser.getId())).findFirst().get();
                    }
                    if(user != null){

                        if(!serviceVO.getServiceName().equalsIgnoreCase(existingVo.getServiceName())){
                            log.debug("DbService cannot be updated");
                            MessageDescription exceptionMsg = new MessageDescription();
                            exceptionMsg.setMessage("DbService cannot be updated.");
                            response.addErrorsItem(exceptionMsg);
                            response.setSuccess("FAILED");
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);  
                        }
                        if(!serviceVO.getDbName().equalsIgnoreCase(existingVo.getDbName())){
                            log.debug("DbName cannot be updated");
                            MessageDescription exceptionMsg = new MessageDescription();
                            exceptionMsg.setMessage("DbName cannot be updated.");
                            response.addErrorsItem(exceptionMsg);
                            response.setSuccess("FAILED");
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }
                        serviceVO.setModifiedBy(userVo);
                        response = service.editDb(serviceVO,user);
                        if(response.getSuccess().equalsIgnoreCase("failed")){
                            log.error("Failed to update dbService");
                            MessageDescription exceptionMsg = new MessageDescription();
                            exceptionMsg.setMessage("Failed to update DbService due to internal error.");
                            response.addErrorsItem(exceptionMsg);
                            response.setSuccess("FAILED");
                            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                        }else{
                            return new ResponseEntity<>(response, HttpStatus.OK);
                        }
                    }else{
                        MessageDescription exceptionMsg = new MessageDescription();
                        exceptionMsg.setMessage("user don't have permission to view this dbService.");
                        response.addErrorsItem(exceptionMsg);
                        response.setData(null);
                        response.setSuccess("failed");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); 
                    }
                      
                }else{
                    log.debug("DbService doesnot exist with given id");
                    MessageDescription exceptionMsg = new MessageDescription();
                    exceptionMsg.setMessage("DbService doesnot exist with given id.");
                    response.addErrorsItem(exceptionMsg);
                    response.setSuccess("FAILED");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);                    

                }

            }else{
                log.debug("name cannot be empty");
				MessageDescription exceptionMsg = new MessageDescription();
                exceptionMsg.setMessage("name cannot be empty.");
			    response.addErrorsItem(exceptionMsg);
                response.setSuccess("FAILED");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Failed to update dbService exception {}",e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription();
            exceptionMsg.setMessage("Failed to update DbService due to internal error.");
			response.addErrorsItem(exceptionMsg);
            response.setSuccess("FAILED");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Override
    @ApiOperation(value = "Get all dbService for the user.", nickname = "getAll", notes = "Get all dbService for the user.", response = DbServiceCollectionVO.class, tags={ "dbService", })
    @ApiResponses(value = { 

        @ApiResponse(code = 201, message = "Returns message of success or failure", response = DbServiceCollectionVO.class),

        @ApiResponse(code = 204, message = "Fetch complete, no content found."),

        @ApiResponse(code = 400, message = "Bad request."),

        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),

        @ApiResponse(code = 403, message = "Request is not authorized."),

        @ApiResponse(code = 405, message = "Method not allowed"),

        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/dbService",
        produces = { "application/json" }
    )
    public ResponseEntity<DbServiceCollectionVO> getAll(@ApiParam(value = "page number from which listing of dbService should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
        @ApiParam(value = "page size to limit the number of dbService, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
            DbServiceCollectionVO response = new DbServiceCollectionVO();
            try {

                if (offset == null) {
                    offset = 0;
                }
                if (limit == null) {
                    limit = 0;
                }
                CreatedByVO currentUser = this.userStore.getVO();
                String id = currentUser.getId();
                    List<DbServiceVO> allService = service.getAllDbService(offset, limit,id);
                    if (Objects.nonNull(allService)) {
                        for (DbServiceVO recipe : allService) {
                            response.addDataItem(recipe);
                        }
                        response.setCount(allService.size());
                        response.setSuccess("SUCCESS");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    } else {
                        response.setData(null);
                        response.setCount(null);
                        MessageDescription exceptionMsg = new MessageDescription();
                        exceptionMsg.setMessage("Failed to fetch all the dbService details");
                        response.addErrorsItem(exceptionMsg);
                        response.setSuccess("FAILED");
                        log.info("Failed to fetch all the dbService details for user "+userStore.getUserInfo().getId());
                        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
                    }
            } catch (Exception e) {
                log.info("Failed to fetch all the dbService details for user {}  exception {}",e.getMessage(),userStore.getUserInfo().getId());
			    MessageDescription exceptionMsg = new MessageDescription();
                exceptionMsg.setMessage("Failed to fetch all the dbService details");
			    response.addErrorsItem(exceptionMsg);
                response.setSuccess("FAILED");
			    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

    }


    @Override
        @ApiOperation(value = "Get dbService by id for the user.", nickname = "getById", notes = "Get dbService by id for the user.", response = InitializeResponseVo.class, tags={ "dbService", })
    @ApiResponses(value = { 

        @ApiResponse(code = 201, message = "Returns message of success or failure", response = InitializeResponseVo.class),

        @ApiResponse(code = 204, message = "Fetch complete, no content found."),

        @ApiResponse(code = 400, message = "Bad request."),

        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),

        @ApiResponse(code = 403, message = "Request is not authorized."),

        @ApiResponse(code = 405, message = "Method not allowed"),

        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/dbService/{id}",
        produces = { "application/json" }
    )
    public ResponseEntity<InitializeResponseVo> getById(@ApiParam(value = "dbService id", required = true) @PathVariable("id") String id) {
        InitializeResponseVo response = new InitializeResponseVo();
        CreatedByVO currentUser = this.userStore.getVO();
        UserInfoVO user = null;
        try {
            if(id != null){
               DbServiceVO data = service.getById(id);
               if (data != null) {
                    if(data.getProjectOwner() != null && data.getProjectOwner().getId().equalsIgnoreCase(currentUser.getId())){
                        user =   data.getProjectOwner();
                    }else if(data.getProjectCollaborators() != null && data.getProjectCollaborators().stream().anyMatch(i -> i.getId().equalsIgnoreCase(currentUser.getId()))){
                        user  = data.getProjectCollaborators().stream().filter(i -> i.getId().equalsIgnoreCase(currentUser.getId())).findFirst().get();
                    }
                    if(user != null){
                        data.setCredentials(service.getCredentials(user, data.getServiceName()));
                        response.setData(data);
                        response.setSuccess("success");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }else{
                        MessageDescription exceptionMsg = new MessageDescription();
                        exceptionMsg.setMessage("user don't have permission to view this dbService.");
                        response.addErrorsItem(exceptionMsg);
                        response.setData(null);
                        response.setSuccess("failed");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); 
                    }
                    
                   
               }
            }
                log.error("Failed to get dbService");
                MessageDescription exceptionMsg = new MessageDescription();
                exceptionMsg.setMessage("Failed to get DbService due to internal error.");
                response.addErrorsItem(exceptionMsg);
                response.setSuccess("Failed");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);  
            
        } catch (Exception e) {
            log.error("Failed to get dbService  exception {}",e.getMessage());
            MessageDescription exceptionMsg = new MessageDescription();
            exceptionMsg.setMessage("Failed to get DbService due to internal error.");
            response.addErrorsItem(exceptionMsg);
            response.setSuccess("Failed");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = "Delete dbService by id for the user.", nickname = "deleteById", notes = "Delete dbService by id for the user.", response = GenericMessage.class, tags={ "dbService", })
    @ApiResponses(value = { 
 
         @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
 
         @ApiResponse(code = 204, message = "Fetch complete, no content found."),
 
         @ApiResponse(code = 400, message = "Bad request."),
 
         @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
 
         @ApiResponse(code = 403, message = "Request is not authorized."),
 
         @ApiResponse(code = 405, message = "Method not allowed"),
 
         @ApiResponse(code = 500, message = "Internal error") })
     @RequestMapping(
         method = RequestMethod.DELETE,
         value = "/dbService/{id}",
         produces = { "application/json" }
     )
    public ResponseEntity<GenericMessage> deleteById(@ApiParam(value = "dbService id", required = true) @PathVariable("id") String id) {
        GenericMessage response = new GenericMessage();
        try {
            CreatedByVO currentUser = this.userStore.getVO();
            UserInfoVO userVo = new UserInfoVO();
            UserInfoVO user = null;
            BeanUtils.copyProperties(currentUser, userVo); 
            DbServiceVO existingVo = service.getById(id);                
                if(existingVo != null){
                    if(existingVo.getProjectOwner() != null && existingVo.getProjectOwner().getId().equalsIgnoreCase(currentUser.getId())){
                        user =   existingVo.getProjectOwner();
                    }else if(existingVo.getProjectCollaborators() != null && existingVo.getProjectCollaborators().stream().anyMatch(i -> i.getId().equalsIgnoreCase(currentUser.getId()))){
                        user  = existingVo.getProjectCollaborators().stream().filter(i -> i.getId().equalsIgnoreCase(currentUser.getId())).findFirst().get();
                    }
                    if(user != null){
                        existingVo.setModifiedBy(userVo);
                        response = service.deleteDb(existingVo);
                        if(response.getSuccess().equalsIgnoreCase("failed")){
                            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                        }else{
                            return new ResponseEntity<>(response, HttpStatus.OK);
                        }
                    }else{
                        MessageDescription exceptionMsg = new MessageDescription();
                        exceptionMsg.setMessage("user don't have permission to view this dbService.");
                        response.addErrorsItem(exceptionMsg);
                        response.setSuccess("failed");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); 
                    }
                      
                }else{
                    log.debug("DbService doesnot exist with given id");
                    MessageDescription exceptionMsg = new MessageDescription();
                    exceptionMsg.setMessage("DbService doesnot exist with given id.");
                    response.addErrorsItem(exceptionMsg);
                    response.setSuccess("FAILED");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);                    

                }
        } catch (Exception e) {
            log.error("Failed to delete dbService exception {}",e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription();
            exceptionMsg.setMessage("Failed to delete DbService due to internal error.");
			response.addErrorsItem(exceptionMsg);
            response.setSuccess("FAILED");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
 
    }

    @Override
    @ApiOperation(value = "updateStatus dbService by id for the user.", nickname = "updateStatus", notes = "updateStatus dbService by id for the user.", response = GenericMessage.class, tags={ "dbService", })
    @ApiResponses(value = { 

        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),

        @ApiResponse(code = 204, message = "Fetch complete, no content found."),

        @ApiResponse(code = 400, message = "Bad request."),

        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),

        @ApiResponse(code = 403, message = "Request is not authorized."),

        @ApiResponse(code = 405, message = "Method not allowed"),

        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(
        method = RequestMethod.PATCH,
        value = "/dbService/{id}",
        produces = { "application/json" }
    )
    public ResponseEntity<GenericMessage> updateStatus(@ApiParam(value = "dbService id", required = true) @PathVariable("id") String id){
        GenericMessage response = new GenericMessage();
        try {
            CreatedByVO currentUser = this.userStore.getVO();
            UserInfoVO userVo = new UserInfoVO();
            UserInfoVO user = null;
            BeanUtils.copyProperties(currentUser, userVo); 
            DbServiceVO existingVo = service.getById(id); 
            if(existingVo != null ){
                existingVo.setModifiedBy(userVo);
                        response = service.updateStatus(existingVo);
                        if(response.getSuccess().equalsIgnoreCase("failed")){
                            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                        }else{
                            return new ResponseEntity<>(response, HttpStatus.OK);
                        }
            }else{
                    log.debug("DbService doesnot exist with given id");
                    MessageDescription exceptionMsg = new MessageDescription();
                    exceptionMsg.setMessage("DbService doesnot exist with given id.");
                    response.addErrorsItem(exceptionMsg);
                    response.setSuccess("FAILED");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);                    

                }
        }catch (Exception e) {
            log.error("Failed to delete dbService exception {}",e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription();
            exceptionMsg.setMessage("Failed to delete DbService due to internal error.");
			response.addErrorsItem(exceptionMsg);
            response.setSuccess("FAILED");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    } 


}
