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

package com.daimler.data.service.tag;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.TagNsql;
import com.daimler.data.db.repo.tag.TagRepository;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.tag.TagRequestVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.common.BaseCommonService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BaseTagService extends BaseCommonService<TagVO, TagNsql, String> implements TagService {
	
	private static Logger LOGGER = LoggerFactory.getLogger(BaseTagService.class);
	
	@Autowired
	private TagRepository jpaRepo;
	
	@Autowired
	private UserStore userStore;

	public BaseTagService() {
		super();
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteTag(String id) {
		try {
			if (verifyUserRoles()) {
				Optional<TagNsql> tagEntity = jpaRepo.findById(id);
				if (tagEntity.isPresent()) {					
					jpaRepo.deleteById(id);
				}
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("Tag with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to delete tag. Only user with admin role can delete.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Tag with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to delete tag with id {} , due to internal error.", id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@Override
	@Transactional
	public ResponseEntity<TagVO> updateTag(TagRequestVO tagRequestVO) {
		TagVO tagVO = tagRequestVO.getData();
		try {
			if (verifyUserRoles()) {				
				String id = tagVO.getId();
				Optional<TagNsql> tagEntity = jpaRepo.findById(id);
				if (tagEntity.isPresent()) {
					TagVO existingTagVO = getByUniqueliteral("name", tagVO.getName());
					if (existingTagVO != null && existingTagVO.getName() != null) {
						LOGGER.info("Tag  {} already exists, returning as CONFLICT", tagVO.getName());
						return new ResponseEntity<>(existingTagVO, HttpStatus.CONFLICT);
					}
					TagVO vo = super.create(tagVO);
					if (vo != null && vo.getId() != null) {						
						LOGGER.info("Tag with id {} updated successfully", id);
						return new ResponseEntity<>(vo, HttpStatus.OK);
					} else {
						LOGGER.info("Tag with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.info("No tag found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
				}

			} else {				
				LOGGER.info("Tag with id {} cannot be edited. User not authorized", tagVO.getId());
				return new ResponseEntity<>(tagVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Tag with id {} cannot be edited. Failed due to internal error {} ",
					tagVO.getId(), e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public boolean verifyUserRoles() {
		Boolean isAdmin = false;
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		if (StringUtils.hasText(userId)) {
			isAdmin = this.userStore.getUserInfo().hasAdminAccess();
		}
		return isAdmin;
	}

}
