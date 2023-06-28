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

package com.daimler.data.service.datatransfer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DataTransferAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataTransferNsql;
import com.daimler.data.db.repo.datatransfer.DataTransferCustomRepository;
import com.daimler.data.db.repo.datatransfer.DataTransferRepository;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.datatransfer.*;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.notifications.common.producer.KafkaProducerService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.department.DepartmentService;
import com.daimler.data.util.ConstantsUtility;

import io.jsonwebtoken.lang.Strings;

@Service
public class BaseDataTransferService extends BaseCommonService<DataTransferVO, DataTransferNsql, String>
		implements DataTransferService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataTransferService.class);

	@Value(value = "${datatransfer.base.url}")
	private String dataTransferBaseUrl;

	@Autowired
	private UserStore userStore;

	@Autowired
	private DataTransferAssembler dataTransferAssembler;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private DataTransferCustomRepository dataTransferCustomRepository;

	@Autowired
	private DataTransferRepository dataTransferRepository;

	public BaseDataTransferService() {
		super();
	}

	@Override
	@Transactional
	public DataTransferVO getById(String id) {
		if (StringUtils.hasText(id)) {
			DataTransferVO existingVO = super.getByUniqueliteral("dataTransferId", id);
			if (existingVO != null && existingVO.getDataTransferId() != null) {
				return existingVO;
			} else {
				return super.getById(id);
			}
		}
		return null;
	}

	@Override
	public List<DataTransferVO> getAllWithFilters(Boolean published, int offset, int limit, String sortBy,
			String sortOrder, String recordStatus, String datatransferIds, Boolean isCreator, Boolean isProviderCreator) {
		String userId = null;
		String providerUserId = null;
		if (isCreator != null && isCreator && this.userStore.getUserInfo() != null) {
			userId = this.userStore.getUserInfo().getId();
		}
		if (isProviderCreator != null && isProviderCreator && this.userStore.getUserInfo() != null) {
			providerUserId = this.userStore.getUserInfo().getId();
		}
		List<DataTransferNsql> dataTransferEntities = dataTransferCustomRepository
				.getAllWithFiltersUsingNativeQuery(published, offset, limit, sortBy, sortOrder, recordStatus, datatransferIds, userId, providerUserId);
		if (!ObjectUtils.isEmpty(dataTransferEntities))
			return dataTransferEntities.stream().map(n -> dataTransferAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(Boolean published, String recordStatus, String datatransferIds, Boolean isCreator, Boolean isProviderCreator) {
		String userId = null;
		String providerUserId = null;
		if (isCreator != null && isCreator  && this.userStore.getUserInfo() != null) {
			userId = this.userStore.getUserInfo().getId();
		}
		if (isProviderCreator != null && isProviderCreator && this.userStore.getUserInfo() != null) {
			providerUserId = this.userStore.getUserInfo().getId();
		}
		return dataTransferCustomRepository.getCountUsingNativeQuery(published, recordStatus, datatransferIds, userId, providerUserId);
	}

	private void updateDepartments(String department) {
		if (Strings.hasText(department)) {
			DepartmentVO existingDepartmentVO = departmentService.getByUniqueliteral("name", department);
			if (existingDepartmentVO != null && existingDepartmentVO.getName() != null
					&& existingDepartmentVO.getName().equalsIgnoreCase(department)) {
				return;
			} else {
				DepartmentVO newDepartmentVO = new DepartmentVO();
				newDepartmentVO.setId(null);
				newDepartmentVO.setName(department);
				departmentService.create(newDepartmentVO);
			}
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DataTransferProviderResponseVO> createDataTransferProvider(ProviderVO requestVO, Boolean isDataProductService) {
		DataTransferProviderResponseVO responseVO = new DataTransferProviderResponseVO();
		DataTransferVO dataTransferVO = new DataTransferVO();
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			ProviderResponseVO providerResponseVO = requestVO.getProviderInformation();
			String uniqueTransferName = requestVO.getDataTransferName();

			if (isDataProductService != null && isDataProductService) {
				CreatedByVO createdBy = requestVO.getProviderInformation().getCreatedBy();

				if (createdBy != null && hasProviderAccess(createdBy)) {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to create dataTransfer provider form.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataTransfer provider form with id {} cannot be created. User not authorized", userId);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			}

			if (!ObjectUtils.isEmpty(providerResponseVO.getUsers())) {
				if (providerResponseVO.getUsers().stream().anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()))) {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Provider cannot be added as a consumer");
					messages.add(message);
					responseVO.setData(requestVO);
					responseVO.setErrors(messages);
					LOGGER.error("DataTransfer {} , failed to create", uniqueTransferName);
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}	
			List<DataTransferVO> dataTransferVOs = getExistingDataTransfer(uniqueTransferName, ConstantsUtility.OPEN);
			if (!ObjectUtils.isEmpty(dataTransferVOs) && (dataTransferVOs.size()>0)) {
				responseVO.setData(getProviderVOFromDataTransferVO(dataTransferVOs.get(0)));
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("DataTransfer already exists.");
				messages.add(message);
				responseVO.setErrors(messages);
				LOGGER.debug("DataTransfer {} already exists, returning as CONFLICT", uniqueTransferName);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			if (isDataProductService !=null && isDataProductService && 
				requestVO.getProviderInformation() != null && 
				requestVO.getProviderInformation().getCreatedBy() != null) {
				providerResponseVO.setCreatedBy(requestVO.getProviderInformation().getCreatedBy());
			} else {
				providerResponseVO.setCreatedBy(this.userStore.getVO());
			}
			providerResponseVO.setCreatedDate(new Date());
			if (providerResponseVO.isProviderFormSubmitted() == null) {
				providerResponseVO.setProviderFormSubmitted(false);
			}
			dataTransferVO.setProviderInformation(providerResponseVO);
			dataTransferVO.setDataTransferName(uniqueTransferName);
			dataTransferVO.setNotifyUsers(requestVO.isNotifyUsers());
			if (isDataProductService != null && isDataProductService) {
				dataTransferVO.setPublish(true);
			} else {
				dataTransferVO.setPublish(false);
			}
			dataTransferVO.setDataTransferId("DTF-" + String.format("%05d", dataTransferRepository.getNextSeqId()));
			dataTransferVO.setRecordStatus(ConstantsUtility.OPEN);
			dataTransferVO.setId(null);
			updateDepartments(providerResponseVO.getContactInformation().getDepartment());

			DataTransferVO vo = this.create(dataTransferVO);
			if (vo != null && vo.getId() != null) {
				responseVO.setData(getProviderVOFromDataTransferVO(vo));
				LOGGER.info("DataTransfer {} created successfully", uniqueTransferName);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				responseVO.setData(requestVO);
				responseVO.setErrors(messages);
				LOGGER.error("DataTransfer {} , failed to create", uniqueTransferName);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating dataTransfer {} ", e.getMessage(),
					requestVO.getDataTransferName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * To check if user has access to proceed with edit or delete of the provider
	 * form.
	 * 
	 */
	private boolean hasProviderAccess(CreatedByVO createdBy) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (StringUtils.hasText(userId)) {
			if (createdBy != null && userId.equalsIgnoreCase(createdBy.getId())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasProviderAccess(DataTransferTeamMemberVO teamMemberVO) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (StringUtils.hasText(userId)) {
			if (teamMemberVO != null && userId.equalsIgnoreCase(teamMemberVO.getShortId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Transactional
	public ResponseEntity<DataTransferProviderResponseVO> updateDataTransferProvider(ProviderVO requestVO) {
		DataTransferProviderResponseVO responseVO = new DataTransferProviderResponseVO();
		DataTransferVO dataTransferVO = new DataTransferVO();
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			ProviderResponseVO providerResponseVO = requestVO.getProviderInformation();
			String id = requestVO.getId();
			DataTransferVO existingVO = super.getById(id);
			DataTransferVO mergedVO = null;
			if (providerResponseVO.isProviderFormSubmitted() == null) {
				providerResponseVO.setProviderFormSubmitted(false);
			}
			if (existingVO != null && existingVO.getRecordStatus() != null
					&& !existingVO.getRecordStatus().equalsIgnoreCase(ConstantsUtility.DELETED)) {
				CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
				DataTransferTeamMemberVO informationOwner = existingVO.getProviderInformation().getContactInformation().getInformationOwner();
				DataTransferTeamMemberVO name = existingVO.getProviderInformation().getContactInformation().getName();
				if (hasProviderAccess(createdBy) || hasProviderAccess(informationOwner) || hasProviderAccess(name)) {
					if (!ObjectUtils.isEmpty(providerResponseVO.getUsers())) {
						if (providerResponseVO.getUsers().stream()
								.anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()))) {
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage("Provider cannot be added as a consumer");
							messages.add(message);
							responseVO.setData(requestVO);
							responseVO.setErrors(messages);
							LOGGER.error("DataTransfer with id {} , failed to update", id);
							return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
						}
					}
					providerResponseVO.setCreatedBy(createdBy);
					providerResponseVO.setCreatedDate(existingVO.getProviderInformation().getCreatedDate());
					providerResponseVO.lastModifiedDate(new Date());
					providerResponseVO.setModifiedBy(currentUser);
					dataTransferVO.setProviderInformation(providerResponseVO);
					dataTransferVO.setDataTransferId(existingVO.getDataTransferId());
					dataTransferVO.setDataTransferName(requestVO.getDataTransferName());
					dataTransferVO.setPublish(existingVO.isPublish());
					dataTransferVO.setNotifyUsers(requestVO.isNotifyUsers());
					dataTransferVO.setRecordStatus(existingVO.getRecordStatus());
					dataTransferVO.setId(id);
					dataTransferVO.setConsumerInformation(existingVO.getConsumerInformation());

					updateDepartments(providerResponseVO.getContactInformation().getDepartment());
					mergedVO = this.create(dataTransferVO);
					if (mergedVO != null && mergedVO.getId() != null) {
						responseVO.setData(getProviderVOFromDataTransferVO(mergedVO));
						LOGGER.info("DataTransfer with id {} updated successfully", id);
						this.publishEventMessages(existingVO, mergedVO);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						responseVO.setData(requestVO);
						responseVO.setErrors(messages);
						LOGGER.debug("DataTransfer with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to edit dataTransfer provider form.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataTransfer provider form with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataTransfer found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				responseVO.setErrors(notFoundmessages);
				LOGGER.debug("No dataTransfer found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataTransfer with id {} cannot be edited. Failed due to internal error {} ", requestVO.getId(),
					e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/*
	 * To check if user has access to proceed with consume of the provider form.
	 * 
	 */
	private boolean hasConsumeAccess(DataTransferVO existingVO) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		boolean canProceed = false;
		CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
		List<DataTransferTeamMemberVO> users = existingVO.getProviderInformation().getUsers();
		if (StringUtils.hasText(userId) && !userId.equalsIgnoreCase(createdBy.getId())) {
			DataTransferTeamMemberVO vo = new DataTransferTeamMemberVO();
			BeanUtils.copyProperties(currentUser, vo);
			vo.setAddedByProvider(false);
			vo.setShortId(userId);
			if (users == null) {
				users = new ArrayList<>();
				users.add(vo);
				existingVO.getProviderInformation().setUsers(users);
				canProceed = true;
			} else {
				boolean isAddedByProvider = false;
				for (DataTransferTeamMemberVO member : users) {
					if (userId.equalsIgnoreCase(member.getShortId())) {
						canProceed = true;
						break;
					}
					if (Boolean.TRUE.equals(member.isAddedByProvider())) {
						isAddedByProvider = true;
					}
				}
				if (!canProceed && !isAddedByProvider) {
					users.add(vo);
					canProceed = true;
				}
			}
		}
		return canProceed;
	}

	@Override
	@Transactional
	public ResponseEntity<DataTransferConsumerResponseVO> updateDataTransferConsumer(ConsumerVO requestVO, Boolean isDataProductService) {
		DataTransferConsumerResponseVO responseVO = new DataTransferConsumerResponseVO();
		DataTransferVO dataTransferVO = new DataTransferVO();
		ConsumerVO consumerVO = new ConsumerVO();
		try {
			ConsumerResponseVO consumerResponseVO = requestVO.getConsumerInformation();
			String id = requestVO.getId();
			DataTransferVO existingVO = super.getById(id);
			DataTransferVO mergedVO = null;
			if (requestVO.isPublish() == null) {
				if (isDataProductService != null && isDataProductService) {
					requestVO.setPublish(true);
				} else {
					requestVO.setPublish(false);
				}
			}
			if (existingVO != null && existingVO.getRecordStatus() != null
					&& !existingVO.getRecordStatus().equalsIgnoreCase(ConstantsUtility.DELETED)) {
				List<DataTransferTeamMemberVO> existingUsers = null;
				if (existingVO.getProviderInformation().getUsers() != null) {
					existingUsers = existingVO.getProviderInformation().getUsers().stream()
							.collect(Collectors.toList());
				}
				if (hasConsumeAccess(existingVO)) {
					if (existingVO.getConsumerInformation() == null) {
						consumerResponseVO.setCreatedBy(this.userStore.getVO());
						consumerResponseVO.setCreatedDate(new Date());
					} else {
						CreatedByVO createdBy = existingVO.getConsumerInformation().getCreatedBy();
						consumerResponseVO.setCreatedBy(createdBy);
						consumerResponseVO.setCreatedDate(existingVO.getConsumerInformation().getCreatedDate());
						consumerResponseVO.lastModifiedDate(new Date());
						consumerResponseVO.setModifiedBy(this.userStore.getVO());
					}
					dataTransferVO.setConsumerInformation(consumerResponseVO);
					dataTransferVO.setDataTransferName(existingVO.getDataTransferName());
					dataTransferVO.setPublish(requestVO.isPublish());
					dataTransferVO.setRecordStatus(existingVO.getRecordStatus());
					dataTransferVO.setDataTransferId(existingVO.getDataTransferId());
					dataTransferVO.setId(id);
					dataTransferVO.setNotifyUsers(requestVO.isNotifyUsers());
					dataTransferVO.setProviderInformation(existingVO.getProviderInformation());
					updateDepartments(consumerResponseVO.getContactInformation().getDepartment());
					mergedVO = this.create(dataTransferVO);
					if (mergedVO != null && mergedVO.getId() != null) {
						consumerVO.setConsumerInformation(mergedVO.getConsumerInformation());
						consumerVO.setId(mergedVO.getId());
						consumerVO.setDataTransferId(mergedVO.getDataTransferId());
						consumerVO.setPublish(mergedVO.isPublish());
						consumerVO.setNotifyUsers(mergedVO.isNotifyUsers());
						consumerVO.setDataTransferName(mergedVO.getDataTransferName());
						consumerVO.setRecordStatus(mergedVO.getRecordStatus());
						responseVO.setData(consumerVO);
						LOGGER.info("DataTransfer with id {} updated successfully", id);
						existingVO.getProviderInformation().setUsers(existingUsers);
						this.publishEventMessages(existingVO, mergedVO);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						responseVO.setData(requestVO);
						responseVO.setErrors(messages);
						LOGGER.debug("DataTransfer with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to consume dataTransfer.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("User not authorized to consume data transfer with id {}", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataTransfer found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				responseVO.setErrors(notFoundmessages);
				LOGGER.debug("No dataTransfer found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataTransfer with id {} cannot be edited. Failed due to internal error {} ", requestVO.getId(),
					e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private void publishEventMessages(DataTransferVO prevDataTransferVO, DataTransferVO currDataTransferVO) {
		try {
			ProviderResponseVO currProviderVO = currDataTransferVO.getProviderInformation();
			if (currDataTransferVO.isNotifyUsers()) {
				CreatedByVO currentUser = this.userStore.getVO();
				String resourceID = currDataTransferVO.getDataTransferId();
				String existingDataTransferName = prevDataTransferVO.getDataTransferName();
				String currentDataTransferName = currDataTransferVO.getDataTransferName();
				String eventType = "";
				String eventMessage = "";
				String userName = super.currentUserName(currentUser);
				String userId = currentUser != null ? currentUser.getId() : "dna_system";
				String userEmailId = currentUser != null ? currentUser.getEmail() : "";
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					for (DataTransferTeamMemberVO user : currProviderVO.getUsers()) {
						if (user != null) {
							String shortId = user.getShortId();
							if (StringUtils.hasText(shortId) && !teamMembers.contains(shortId)
									&& !shortId.equalsIgnoreCase(userId)) {
								teamMembers.add(shortId);
							}
							String emailId = user.getEmail();
							if (StringUtils.hasText(emailId) && !teamMembersEmails.contains(emailId)
									&& !emailId.equalsIgnoreCase(userEmailId)) {
								teamMembersEmails.add(emailId);
							}
						}
					}
				}

				if (currProviderVO.getCreatedBy() != null) {
					String providerUserId = currProviderVO.getCreatedBy().getId();
					String providerEmailId = currProviderVO.getCreatedBy().getEmail();

					if (StringUtils.hasText(providerUserId) && !teamMembers.contains(providerUserId)
							&& !providerUserId.equalsIgnoreCase(userId)) {
						teamMembers.add(providerUserId);
					}

					if (StringUtils.hasText(providerEmailId) && !teamMembersEmails.contains(providerEmailId)
							&& !providerEmailId.equalsIgnoreCase(userEmailId)) {
						teamMembersEmails.add(providerEmailId);
					}
				}

				if (!prevDataTransferVO.isPublish() && currDataTransferVO.isPublish()) {
					eventType = "DataTransfer - Consumer form Published";
					eventMessage = "A Minimum Information Documentation data transfer is complete. [view]("
							+ dataTransferBaseUrl + "summary/" + resourceID + ")";
					LOGGER.info("Publishing message on consumer form submission for dataTransfer {} by userId {}",
							currentDataTransferName, userId);

				} else if (prevDataTransferVO.isPublish() && currDataTransferVO.isPublish()) {
					eventType = "DataTransfer_Update";
					eventMessage = "DataTransfer " + existingDataTransferName + " is updated by user " + userName;
					changeLogs = dataTransferAssembler.jsonObjectCompare(currDataTransferVO, prevDataTransferVO,
							currentUser);
					LOGGER.info("Publishing message on update for dataTransfer {} by userId {}", existingDataTransferName,
							userId);
				} else if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					eventType = "DataTransfer - Provider Form Submitted";
					eventMessage = "A Minimum Information Documentation is ready for you. Please [provide information]("
							+ dataTransferBaseUrl + "consume/" + resourceID + ")"
							+ " about the receiving side to finalise the Data Transfer.";
					LOGGER.info("Publishing message on provider form submission for dataTransfer {} by userId {}",
							currentDataTransferName, userId);
				}
				if (StringUtils.hasText(eventType)) {
					kafkaProducer.send(eventType, resourceID, "", userId, eventMessage, true, teamMembers,
							teamMembersEmails, changeLogs);
					LOGGER.info("Published successfully event {} for data transfer with id {}", eventType, resourceID);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed while publishing dataTransfer event msg {} ", e.getMessage());
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataTransfer(String id) {
		try {
			DataTransferVO existingVO = super.getById(id);
			DataTransferVO dataTransferVO = null;
			if (existingVO != null && existingVO.getId() != null) {
				CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
				DataTransferTeamMemberVO informationOwner = existingVO.getProviderInformation().getContactInformation().getInformationOwner();
				DataTransferTeamMemberVO name = existingVO.getProviderInformation().getContactInformation().getName();
				if (hasProviderAccess(createdBy) || hasProviderAccess(informationOwner) || hasProviderAccess(name)) {
					ProviderResponseVO providerResponseVO = existingVO.getProviderInformation();
					providerResponseVO.lastModifiedDate(new Date());
					providerResponseVO.setModifiedBy(this.userStore.getVO());
					existingVO.setProviderInformation(providerResponseVO);
					existingVO.setRecordStatus(ConstantsUtility.DELETED);
					dataTransferVO = super.create(existingVO);
					if (dataTransferVO != null && dataTransferVO.getId() != null) {
						GenericMessage successMsg = new GenericMessage();
						successMsg.setSuccess("success");
						LOGGER.info("DataTransfer with id {} deleted successfully", id);
						return new ResponseEntity<>(successMsg, HttpStatus.OK);
					} else {
						MessageDescription exceptionMsg = new MessageDescription(
								"Failed to delete dataTransfer due to internal error");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.addErrors(exceptionMsg);
						LOGGER.debug("DataTransfer with id {} cannot be deleted. Failed with unknown internal error",
								id);
						return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				} else {
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to delete dataTransfer.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(notAuthorizedMsg);
					LOGGER.debug("DataTransfer with id {} cannot be deleted. User not authorized", id);
					return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
				}
			} else {
				MessageDescription invalidMsg = new MessageDescription("No dataTransfer with the given id found");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(invalidMsg);
				LOGGER.debug("No dataTransfer with the given id {} found.", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete dataTransfer with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public GenericMessage updateDataTransferData() {
		return dataTransferCustomRepository.updateDataTransferData();
	}

	@Override
	public Integer getCountBasedPublishDatatransfer(Boolean published) {
		return dataTransferCustomRepository.getCountBasedPublishDatatransfer(published);
	}

	private ProviderVO getProviderVOFromDataTransferVO(DataTransferVO vo) {
		ProviderVO providerVO = new ProviderVO();
		providerVO.setProviderInformation(vo.getProviderInformation());
		providerVO.setId(vo.getId());
		providerVO.setDataTransferId(vo.getDataTransferId());
		providerVO.setDataTransferName(vo.getDataTransferName());
		providerVO.setRecordStatus(vo.getRecordStatus());
		providerVO.setNotifyUsers(vo.isNotifyUsers());
		return providerVO;

	}


	private List<DataTransferVO> getExistingDataTransfer(String uniqueTransferName, String status) {
		LOGGER.info("Fetching Data transfer information from table for getExistingDataTransfer.");
		List<DataTransferNsql> dataTransferNsqls = dataTransferCustomRepository.getExistingDataTransfer(uniqueTransferName, status);
		LOGGER.info("Success from get information from table.");
		if (!ObjectUtils.isEmpty(dataTransferNsqls)) {
			return dataTransferNsqls.stream().map(n -> dataTransferAssembler.toVo(n)).toList();
		} else {
			return new ArrayList<>();
		}	
	}
	


}
