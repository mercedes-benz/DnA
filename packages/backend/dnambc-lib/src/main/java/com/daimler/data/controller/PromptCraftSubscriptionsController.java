/* LICENSE START
* 
* MIT License
* 
* Copyright (c) 2019 Daimler TSS GmbH
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
* 
* LICENSE END 
*/

package com.daimler.data.controller;

import com.daimler.data.api.promptCraftSubscriptions.PromptCraftSubscriptionsApi;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.LoginController.UserInfo;
import com.daimler.data.controller.LoginController.UserRole;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsResponseVO;
import com.daimler.data.dto.promptCraftSubscriptions.MemberInfoVO;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionCollectionVO;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionRequestVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysResponseVO;
import com.daimler.data.dto.promptCraftSubscriptions.SubscriptionkeysResponseVOData;
import com.daimler.data.service.promptCraftSubscriptions.AsyncService;
import com.daimler.data.service.promptCraftSubscriptions.PromptCraftSubscriptionsService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.dto.userinfo.UserInfoVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;



@RestController
@Api(value = "PromptCraft Subscriptions API", tags = { "promptCraftSubscriptions" })
@RequestMapping("/api")
@Slf4j
public class PromptCraftSubscriptionsController  implements PromptCraftSubscriptionsApi{

    @Autowired
    private UserInfoAssembler userInfoAssembler;

    @Autowired
	private UserStore userStore;

    @Autowired
    private PromptCraftSubscriptionsService service;

    @Autowired
    private AsyncService asyncService;

    @Autowired
	private UserInfoService userInfoService;

    @Value("${promptsraftsubscriptions.uiLicious.pidUser}")
    private String pidUser;

    @Override
    @ApiOperation(value = "Adds a new Subscription.", nickname = "create", notes = "Adds a new Subscriptions.", response = PromptCraftSubscriptionsResponseVO.class, tags={ "promptCraftSubscriptions", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = PromptCraftSubscriptionsResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/promptCraftSubscriptions/create",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<PromptCraftSubscriptionsResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new Subscriptions" ,required=true )  @Valid @RequestBody SubscriptionRequestVO subscriptionRequestVO){
        
        UserInfo currentUser = this.userStore.getUserInfo();

        PromptCraftSubscriptionsResponseVO response = new PromptCraftSubscriptionsResponseVO();
        PromptCraftSubscriptionsVO requestVO = subscriptionRequestVO.getData();
        List<MessageDescription> errors = new ArrayList<>();
        List<MessageDescription> warnings = new ArrayList<>();

        try{

            MemberInfoVO projectOwner = new MemberInfoVO();
            UserInfoVO userInfoVO = userInfoService.getById(pidUser);
            BeanUtils.copyProperties(userInfoVO, projectOwner);
            requestVO.setProjectOwner(projectOwner);
           
            if(isUserHasAdminAccess(currentUser)){
                PromptCraftSubscriptionsVO existingVO = service.getByUniqueliteral("projectName", requestVO.getProjectName());
                // checking if project name is existing in database
                if (existingVO != null && existingVO.getProjectName() != null) {
                    response.setData(existingVO);
                    response.setSuccess("EXISTING");
                    MessageDescription msg = new MessageDescription("subscription for this Project already exists");
                    errors.add(msg);
                    response.setErrors(errors);
                    log.info("projectName {} already exists ", requestVO.getProjectName());
                    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
                }
                 response = service.createSubscription(requestVO);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{
                log.info("Not Authorized to access create Api for user {}",currentUser.getId());
                MessageDescription msg = new MessageDescription("Not Authorized to access this Api for user " + currentUser.getId());
                errors.add(msg);
                response.setSuccess("FAILED");
                response.setErrors(errors);
                response.setData(null);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                
            }
        }catch(Exception e){
            log.info("Exception occured while calling create : {}",e.getMessage());
            MessageDescription msg = new MessageDescription("Exception occured while calling create :" + e.getMessage());
            errors.add(msg);
            response.setSuccess("FAILED");
            response.setErrors(errors);
            response.setData(null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    @ApiOperation(value = "get all Subscriptions.", nickname = "getAll", notes = "get all Subscriptions.", response = PromptCraftSubscriptionsResponseVO.class, tags={ "promptCraftSubscriptions", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = PromptCraftSubscriptionsResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/promptCraftSubscriptions",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<PromptCraftSubscriptionCollectionVO> getAll(@ApiParam(value = "page number from which listing of subscriptions should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,@ApiParam(value = "page size to limit the number of subscriptions, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit){
        UserInfo currentUser = this.userStore.getUserInfo();

        PromptCraftSubscriptionCollectionVO response = new PromptCraftSubscriptionCollectionVO();
        GenericMessage genericMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        List<MessageDescription> warnings = new ArrayList<>();
        List<PromptCraftSubscriptionsVO> collection = new ArrayList<>();    

        try{

            int defaultLimit = 15;
            if (offset == null || offset < 0)
                offset = 0;
            if (limit == null || limit < 0) {
                limit = defaultLimit;
            }

            if(isUserHasAdminAccess(currentUser)){
                collection = service.getAll(limit,offset);
                    
            }else{
                collection = service.getAll(limit,offset,currentUser.getId());
            }
            if (collection != null && collection.size() > 0) {
                for (PromptCraftSubscriptionsVO subscription : collection) {
                    response.addDataItem(subscription);
                }
                response.setTotalCount(collection.size());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.setData(null);
                response.setTotalCount(null);
                log.info("Failed to fetch all the subscription details for use "+ currentUser.getId());
                return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
            }

        }catch(Exception e){
            log.info("Exception occured while calling create : {}",e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = " get  Subscription keys.", nickname = "getkeys", notes = " get Subscriptionkeys.", response = SubscriptionkeysResponseVO.class, tags={ "promptCraftSubscriptions", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = SubscriptionkeysResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/promptCraftSubscriptions/{projectName}/getkeys",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<SubscriptionkeysResponseVO> getkeys(@ApiParam(value = "",required=true) @PathVariable("projectName") String projectName){
        UserInfo currentUser = this.userStore.getUserInfo();
        SubscriptionkeysResponseVO response = new SubscriptionkeysResponseVO();
        SubscriptionkeysResponseVOData responseData = new SubscriptionkeysResponseVOData();
        
        try{

            PromptCraftSubscriptionsVO existingVO = service.getByUniqueliteral("projectName", projectName);

            if(isUserHasAdminAccess(currentUser)||isUserMemberOfTheProject(existingVO, currentUser.getId()) ){

                SubscriptionkeysVO keys = service.getProjectKeys(projectName);
                
                if(keys!= null) {
                    responseData.setPromptCraftKey(keys.getUserID());
                    response.setData(responseData);
                    log.info("successfully got key for project {}.", projectName);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
                response.setData(responseData);
                return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);

            }else{
                log.info("user {} dont have admin permission or not part of the project, Not Atuthorized.", currentUser);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

        }catch(Exception e){
            log.info("Error occured while getting keys for user {} error : {}", currentUser, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = "refresh to get  Subscription keys.", nickname = "refresh", notes = "refresh to get Subscriptionkeys.", response = GenericMessage.class, tags={ "promptCraftSubscriptions", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/promptCraftSubscriptions/{projectName}/refresh",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> refresh(@ApiParam(value = "",required=true) @PathVariable("projectName") String projectName){
        
        UserInfo currentUser = this.userStore.getUserInfo();

        GenericMessage response = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        List<MessageDescription> warnings = new ArrayList<>();

        try{

            if(isUserHasAdminAccess(currentUser)){
                PromptCraftSubscriptionsVO existingVO = service.getByUniqueliteral("projectName", projectName);
                if (existingVO == null) {
                    response.setSuccess("FAILED");
                    MessageDescription msg = new MessageDescription("subscription for this Project {} not exists"+ projectName);
                    errors.add(msg);
                    response.setErrors(errors);
                    log.info("subscription for this Project {} not exists", projectName);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                if(!"COMPLETED".equalsIgnoreCase(existingVO.getStatus())){
                    if(!"FAILED".equalsIgnoreCase(existingVO.getStatus())){
                        asyncService.checkForKeysFromUiLicious(projectName, existingVO.getRunId());
                        response.setSuccess("SUCCESS");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                    else{
                        PromptCraftSubscriptionsResponseVO createSubscriptionResponse = new PromptCraftSubscriptionsResponseVO();
                        createSubscriptionResponse = service.createSubscription(existingVO);
                        response.setSuccess(createSubscriptionResponse.getSuccess());
                        response.setErrors(createSubscriptionResponse.getErrors());
                        response.setWarnings(createSubscriptionResponse.getWarnings());
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                }else{
                    log.info("Already Subscribed for the project {}, cannot refresh ",existingVO.getProjectName());
                    MessageDescription msg = new MessageDescription("Already Subscribed for the project {}, cannot refresh " + existingVO.getProjectName());
                    errors.add(msg);
                    response.setSuccess("FAILED");
                    response.setErrors(errors);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                
            }else{
                log.info("Not Authorized to access create Api for user {}",currentUser.getId());
                MessageDescription msg = new MessageDescription("Not Authorized to access this Api for user " + currentUser.getId());
                errors.add(msg);
                response.setSuccess("FAILED");
                response.setErrors(errors);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

        }catch(Exception e){
            log.info("Exception occured while calling refresh : {}",e.getMessage());
            MessageDescription msg = new MessageDescription("Exception occured while calling refresh :" + e.getMessage());
            errors.add(msg);
            response.setSuccess("FAILED");
            response.setErrors(errors);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Boolean isUserHasAdminAccess(UserInfo currentUser){
        List<UserRole> roles = currentUser.getDigiRole();
        Boolean isuserAdmin =  roles.stream()
        .anyMatch(role -> "Admin".equals(role.getName()));
        return isuserAdmin;
    }

    public Boolean isUserMemberOfTheProject(PromptCraftSubscriptionsVO vo, String currentUser){
        List<MemberInfoVO> memberList = vo.getProjectMembers();
        Boolean isUserMemberOfTheProject = memberList.stream()
        .anyMatch(member -> currentUser.equals(member.getId()));
        return isUserMemberOfTheProject;
    }

}



