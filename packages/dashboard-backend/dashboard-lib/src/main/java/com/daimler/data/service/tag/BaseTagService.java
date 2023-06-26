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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.TagAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.TagSql;
import com.daimler.data.db.repo.tag.TagRepository;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.tag.TagCollection;
import com.daimler.data.dto.tag.TagNameVO;
import com.daimler.data.dto.tag.TagRequestVO;
import com.daimler.data.dto.tag.TagResponseVO;
import com.daimler.data.dto.tag.TagUpdateRequestVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.report.ReportService;

@Service
public class BaseTagService extends BaseCommonService<TagVO, TagSql, Long> implements TagService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseTagService.class);

	private TagRepository jpaRepo;

	private TagAssembler tagAssembler;

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private UserStore userStore;

	public BaseTagService() {
		super();
	}

	@Autowired
	public BaseTagService(TagRepository jpaRepo, TagAssembler tagAssembler) {
		super(jpaRepo, tagAssembler);
		this.jpaRepo = jpaRepo;
		this.tagAssembler = tagAssembler;
	}

	@Override
	public ResponseEntity<TagCollection> getAllTags(String sortOrder) {
		TagCollection tagCollection = new TagCollection();
		try {
			List<TagVO> tags = super.getAll();
			LOGGER.debug("Tags fetched successfully");
			if (!ObjectUtils.isEmpty(tags)) {
				if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
					tags.sort(Comparator.comparing(TagVO :: getName, String.CASE_INSENSITIVE_ORDER));
				}
				if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
					tags.sort(Comparator.comparing(TagVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
				}
				tagCollection.setData(tags);
				return new ResponseEntity<>(tagCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(tagCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch tags with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	@Transactional
	public ResponseEntity<TagResponseVO> createTag(TagRequestVO requestVO) {
		TagResponseVO responseVO = new TagResponseVO();
		try {
			if (verifyUserRoles()) {
				TagNameVO tagNameVO = requestVO.getData();
				String uniqueTagName = tagNameVO.getName();
				TagVO existingTagVO = findTagByName(uniqueTagName);
				if (existingTagVO != null && existingTagVO.getName() != null) {
					responseVO.setData(existingTagVO);
					LOGGER.debug("Tag {} already exists, returning as CONFLICT", uniqueTagName);
					return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
				}
				TagVO tagVO = new TagVO();
				tagVO.setName(uniqueTagName);
				TagVO vo = super.create(tagVO);
				if (vo != null && vo.getId() != null) {
					responseVO.setData(vo);
					LOGGER.info("Tag {} created successfully", uniqueTagName);
					return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
				} else {
					LOGGER.error("Tag {} , failed to create", uniqueTagName);
					return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to create tag. Only user with admin role can create.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				responseVO.setErrors(notAuthorizedMsgs);
				return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating tag ", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<TagResponseVO> updateTag(TagUpdateRequestVO requestVO) {
		TagResponseVO responseVO = new TagResponseVO();
		try {
			if (verifyUserRoles()) {
				TagVO tagVO = requestVO.getData();
				long id = tagVO.getId();
				Optional<TagSql> tagEntity = jpaRepo.findById(id);
				if (tagEntity.isPresent()) {
					TagVO existingTagVO = findTagByName(tagVO.getName());
					if (existingTagVO != null && existingTagVO.getName() != null) {
						responseVO.setData(existingTagVO);
						LOGGER.debug("Tag  {} already exists, returning as CONFLICT", tagVO.getName());
						return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
					}
					reportService.updateForEachReport(tagEntity.get().getName(), tagVO.getName(),
							ReportService.CATEGORY.TAG, null);
					CreatedByVO currentUser = this.userStore.getVO();
					String userId = currentUser != null ? currentUser.getId() : "";
					String userName = currentUserName(currentUser);
					String eventMessage = "Tag " + tagEntity.get().getName() + " has been updated by Admin " + userName;
					super.notifyAllAdminUsers("Dashboard-Report MDM Update", tagEntity.get().getName(), eventMessage, userId, null);
					TagVO vo = super.create(tagVO);
					if (vo != null && vo.getId() != null) {
						responseVO.setData(vo);
						LOGGER.debug("Tag with id {} updated successfully", id);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						LOGGER.debug("Tag with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.debug("No tag found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
				}

			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to update tag. Only user with admin role can update.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				responseVO.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Tag with id {} cannot be edited. User not authorized", requestVO.getData().getId());
				return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Tag with id {} cannot be edited. Failed due to internal error {} ",
					requestVO.getData().getId(), e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteTag(long id) {
		try {
			if (verifyUserRoles()) {
				Optional<TagSql> tagEntity = jpaRepo.findById(id);
				if (tagEntity.isPresent()) {
					String tagName = tagEntity.get().getName();
					reportService.deleteForEachReport(tagName, ReportService.CATEGORY.TAG);
					CreatedByVO currentUser = this.userStore.getVO();
					String userId = currentUser != null ? currentUser.getId() : "";
					String userName = currentUserName(currentUser);
					String eventMessage = "Tag " + tagName + " has been deleted by Admin " + userName;
					super.notifyAllAdminUsers("Dashboard-Report MDM Delete", tagName, eventMessage, userId, null);
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
	public TagVO findTagByName(String tagName) {
		TagSql tagEntity = jpaRepo.findFirstByNameIgnoreCase(tagName);
		return tagAssembler.toVo(tagEntity);
	}

}
