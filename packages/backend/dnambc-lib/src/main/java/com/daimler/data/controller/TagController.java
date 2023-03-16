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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.tag.TagsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.tag.TagCollection;
import com.daimler.data.dto.tag.TagRequestVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Tag API", tags = { "tags" })
@RequestMapping("/api")
@Slf4j
public class TagController implements TagsApi {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private TagService tagService;

	@Override
	@ApiOperation(value = "Adds a new tag.", nickname = "create", notes = "Adds a new non existing tag which is used in providing solution.", response = TagVO.class, tags = {
			"tags", })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Tag added successfully", response = TagVO.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Invalid input"), @ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/tags", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<TagVO> create(@Valid TagRequestVO tagRequestVO) {

		TagVO requestTagVO = tagRequestVO.getData();
		try {
			TagVO existingTagVO = tagService.getByUniqueliteral("name", requestTagVO.getName());
			if (existingTagVO != null && existingTagVO.getName() != null) {
				log.info("Tag with name {} already exists, returning with conflict", requestTagVO.getName());
				return new ResponseEntity<>(existingTagVO, HttpStatus.CONFLICT);
			}
			requestTagVO.setId(null);
			TagVO tagVo = tagService.create(requestTagVO);
			if (tagVo != null && tagVo.getId() != null) {
				log.info("Tag with name {} created successfully", requestTagVO.getName());
				return new ResponseEntity<>(tagVo, HttpStatus.CREATED);
			} else
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Failed to create tag with name {} with exception {}", requestTagVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the tag identified by given ID.", nickname = "delete", notes = "Deletes the tag identified by given ID", response = GenericMessage.class, tags = {
			"tags", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/tags/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(
			@ApiParam(value = "Id of the tag", required = true) @PathVariable("id") String id) {
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						if (userId == null || !isAdmin) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg
									.setMessage("Not authorized to delete tags. User does not have admin privileges.");
							log.debug("User not authorized to delete tags. Doesnt have admin privileges");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			TagVO tag = tagService.getById(id);
			String tagName = tag != null ? tag.getName() : "";
			String userName = tagService.currentUserName(currentUser);
			String eventMessage = "Tag  " + tagName + " has been deleted by Admin " + userName;
			tagService.deleteTag(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			log.info("Tag with id {} deleted successfully", id);
			successMsg.setSuccess("success");
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception {} while deleting tag with id {}, ID not found", e.getLocalizedMessage(), id);
			MessageDescription invalidMsg = new MessageDescription("No tag with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to delete tag with id {} with exception {} ", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all available tags.", nickname = "getAll", notes = "Get all tags. This endpoints will be used to Get all valid available tag maintenance records.", response = TagCollection.class, tags = {
			"tags", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all tags", response = TagCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/tags", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TagCollection> getAll(
			@ApiParam(value = "Sort tags based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		final List<TagVO> tags = tagService.getAll();		
		TagCollection tagCollection = new TagCollection();
		if (tags != null && tags.size() > 0) {
			if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				tags.sort(Comparator.comparing(TagVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				tags.sort(Comparator.comparing(TagVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			tagCollection.addAll(tags);
			log.debug("Returning available tags");
			return new ResponseEntity<>(tagCollection, HttpStatus.OK);
		} else {
			log.debug("No tags available, returning empty");
			return new ResponseEntity<>(tagCollection, HttpStatus.NO_CONTENT);
		}
	}

}
