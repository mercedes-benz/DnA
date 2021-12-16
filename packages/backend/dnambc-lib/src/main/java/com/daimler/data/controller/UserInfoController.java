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

import com.daimler.data.api.userinfo.UsersApi;
import com.daimler.data.controller.exceptions.*;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.solution.SolutionCollectionResponseVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.userinfo.*;
import com.daimler.data.service.userinfo.UserInfoService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@Api(value = "UserInfo API", tags = {"users"})
@RequestMapping("/api")
@Slf4j
public class UserInfoController implements UsersApi {


    @Autowired
    private UserInfoService userInfoService;


    @Override
    @ApiOperation(value = "Get all available users.", nickname = "getAll", notes = "Get all users. This endpoints will be used to Get all valid available user maintenance records.", response = UsersCollection.class, tags = {"users",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = UsersCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/users",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<UsersCollection> getAll(@ApiParam(value = "page size to limit the number of solutions, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit, @ApiParam(value = "page number from which listing of solutions should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset, @ApiParam(value = "Sort users based on given column, example name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy, @ApiParam(value = "Sort users based on given order, example asc,desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        try {
            int defaultLimit = 10;
            if (offset == null)
                offset = 0;
            if (limit == null || limit == 0) {
                limit = defaultLimit;
            }

            if (sortOrder != null && !sortOrder.equals("asc") && !sortOrder.equals("desc")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            if(sortOrder == null){
                sortOrder = "asc";
            }
            List<UserInfoVO> usersInfo =  null;
            if(sortOrder.equals("asc")){
                usersInfo= userInfoService.getAllSortedByUniqueLiteral(limit, offset, sortBy, CommonDataRepositoryImpl.SORT_TYPE.ASC );
            }else if(sortOrder.equals(("desc"))){
                usersInfo= userInfoService.getAllSortedByUniqueLiteral(limit, offset, sortBy, CommonDataRepositoryImpl.SORT_TYPE.DESC );
            }
            Long count = userInfoService.getCount(limit, offset);
            UsersCollection usersCollection = new UsersCollection();
            if (usersInfo != null && usersInfo.size() > 0) {
                usersCollection.setRecords(usersInfo);
                usersCollection.setTotalCount(count.intValue());
                return new ResponseEntity<>(usersCollection, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(usersCollection, HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<SolutionCollectionResponseVO> getAllBookmarks(@NotNull @Valid String userId) {
        SolutionCollectionResponseVO response = new SolutionCollectionResponseVO();
        try {
            List<SolutionVO> solutionVOList = userInfoService.getAllBookMarkedSolutionsForUser(userId);
            if (solutionVOList == null) {
                List<MessageDescription> notFoundMessages = new ArrayList<>();
                MessageDescription notFoundMessage = new MessageDescription();
                notFoundMessage.setMessage("No User favorites found!");
                notFoundMessages.add(notFoundMessage);
                response.setErrors(notFoundMessages);
                return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
            } else {
                response.setRecords(solutionVOList);
                return new ResponseEntity<>(response, HttpStatus.OK);

            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            List<MessageDescription> notFoundMessages = new ArrayList<>();
            MessageDescription notFoundMessage = new MessageDescription();
            notFoundMessage.setMessage("Invalid userid!");
            notFoundMessages.add(notFoundMessage);
            response.setErrors(notFoundMessages);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @Override
    @ApiOperation(value = "Get specific user for a given userid.", nickname = "getById", notes = "Get specific user for a given userid. This endpoints will be used to Get specific user for a given userid.", response = UserInfoVO.class, tags = {"users",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of succes or failure", response = UserInfoVO.class),
            @ApiResponse(code = 400, message = "Malformed syntax."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/users/{id}",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<UserInfoVO> getById(@ApiParam(value = "Id of the user for which information to be fetched", required = true) @PathVariable("id") String id) {
        UserInfoVO userInfoVO = null;
        if (id != null) {
            userInfoVO = userInfoService.getById(id);
            return new ResponseEntity<>(userInfoVO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(userInfoVO, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Update User", nickname = "update", notes = "Update User. This endpoint can be used to update user details", response = UserInfoVO.class, tags = {"users",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = UserInfoVO.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/users",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.PUT)
    public ResponseEntity<UserInfoVO> update(@ApiParam(value = "Request body contains the details of the updated user", required = true) @Valid @RequestBody UserRequestVO userRequestVO) {
        try {
            if (userRequestVO.getData() != null && userRequestVO.getData().getId() != null) {
                UserInfoVO userInfoVO = userRequestVO.getData();
                UserInfoVO currentUserData = userInfoService.getById(userInfoVO.getId());
                if(!rolesUpdated(userRequestVO,currentUserData)){
                    userInfoVO.setToken(currentUserData.getToken());
                }else{
                    userInfoVO.setToken(null);
                }
                userInfoService.create(userInfoVO);
                return new ResponseEntity<>(userInfoVO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(userRequestVO.getData(), HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<BookmarkResponseVO> updateBookmark(@Valid BookmarkRequestVO bookmarkRequestVO) {
        BookmarkResponseVO response = new BookmarkResponseVO();
        try {
            boolean deleteBookmark = bookmarkRequestVO.isDeleteBookmark() != null ? bookmarkRequestVO.isDeleteBookmark() : false;

            UserInfoVO responseVO = userInfoService.updateBookMarkedSolutions(bookmarkRequestVO.getId(), bookmarkRequestVO.getFavoriteUsecases(), deleteBookmark);
            //No need to send token back.
            responseVO.setToken(null);
            response.setData(responseVO);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NoSuchElementException e) {
            e.printStackTrace();
            List<MessageDescription> notFoundmessages = new ArrayList<>();
            MessageDescription notFoundmessage = new MessageDescription();
            notFoundmessage.setMessage("Invalid UserID/Bookmark Id's");
            notFoundmessages.add(notFoundmessage);
            response.setErrors(notFoundmessages);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            List<MessageDescription> notFoundmessages = new ArrayList<>();
            MessageDescription notFoundmessage = new MessageDescription();
            notFoundmessage.setMessage("An  internal error occurred");
            notFoundmessages.add(notFoundmessage);
            response.setErrors(notFoundmessages);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    private boolean rolesUpdated(UserRequestVO updatedUser,UserInfoVO currentUser){
        if(updatedUser!=null && currentUser!=null){
            List<UserRoleVO> updatedUserRoles =  updatedUser.getData().getRoles();
            List<UserRoleVO> currentUserRoles = currentUser.getRoles();
            if(updatedUserRoles != null && currentUserRoles!=null){
                if(updatedUserRoles.size() != currentUserRoles.size()){
                    return true;
                }else{
                    for(UserRoleVO currentUserRole:currentUserRoles){
                        boolean roleFound = false;
                        for(UserRoleVO updatedUserRole:updatedUserRoles){
                            if(updatedUserRole.getId().equals(currentUserRole.getId())){
                                roleFound = true;
                                break;
                            }
                        }
                        if(!roleFound){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}
