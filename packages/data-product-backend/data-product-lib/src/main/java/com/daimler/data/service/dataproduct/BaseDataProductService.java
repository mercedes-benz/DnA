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

package com.daimler.data.service.dataproduct;

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
import com.daimler.data.assembler.DataProductAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.repo.dataproduct.DataProductCustomRepository;
import com.daimler.data.db.repo.dataproduct.DataProductRepository;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.ChangeLogVO;
import com.daimler.data.dto.dataproduct.ConsumerResponseVO;
import com.daimler.data.dto.dataproduct.ConsumerVO;
import com.daimler.data.dto.dataproduct.DataProductConsumerResponseVO;
import com.daimler.data.dto.dataproduct.DataProductProviderResponseVO;
import com.daimler.data.dto.dataproduct.DataProductTeamMemberVO;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.dto.dataproduct.ProviderResponseVO;
import com.daimler.data.dto.dataproduct.ProviderVO;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.notifications.common.producer.KafkaProducerService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.department.DepartmentService;
import com.daimler.data.util.ConstantsUtility;

import io.jsonwebtoken.lang.Strings;

@Service
public class BaseDataProductService extends BaseCommonService<DataProductVO, DataProductNsql, String>
		implements DataProductService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataProductService.class);

	@Value(value = "${dataproduct.base.url}")
	private String dataProductBaseUrl;

	@Autowired
	private UserStore userStore;

	@Autowired
	private DataProductAssembler dataProductAssembler;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private DataProductCustomRepository dataProductCustomRepository;

	@Autowired
	private DataProductRepository dataProductRepository;

	public BaseDataProductService() {
		super();
	}

	@Override
	@Transactional
	public DataProductVO getById(String id) {
		if (StringUtils.hasText(id)) {
			DataProductVO existingVO = super.getByUniqueliteral("dataProductId", id);
			if (existingVO != null && existingVO.getDataProductId() != null) {
				return existingVO;
			} else {
				return super.getById(id);
			}
		}
		return null;
	}

	@Override
	public List<DataProductVO> getAllWithFilters(Boolean published, int offset, int limit, String sortBy,
			String sortOrder, String recordStatus) {
		List<DataProductNsql> dataProductEntities = dataProductCustomRepository
				.getAllWithFiltersUsingNativeQuery(published, offset, limit, sortBy, sortOrder, recordStatus);
		if (!ObjectUtils.isEmpty(dataProductEntities))
			return dataProductEntities.stream().map(n -> dataProductAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(Boolean published, String recordStatus) {
		return dataProductCustomRepository.getCountUsingNativeQuery(published, recordStatus);
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
	public ResponseEntity<DataProductProviderResponseVO> createDataProductProvider(ProviderVO requestVO) {
		DataProductProviderResponseVO responseVO = new DataProductProviderResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			ProviderResponseVO providerResponseVO = requestVO.getProviderInformation();
			String uniqueProductName = requestVO.getDataProductName();
			if (!ObjectUtils.isEmpty(providerResponseVO.getUsers())) {
				if (providerResponseVO.getUsers().stream().anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()))) {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Provider cannot be added as a consumer");
					messages.add(message);
					responseVO.setData(requestVO);
					responseVO.setErrors(messages);
					LOGGER.error("DataProduct {} , failed to create", uniqueProductName);
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}
			DataProductVO existingVO = super.getByUniqueliteral("dataProductName", uniqueProductName);
			if (existingVO != null && existingVO.getDataProductName() != null) {
				responseVO.setData(getProviderVOFromDataProductVO(existingVO));
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("DataProduct already exists.");
				messages.add(message);
				responseVO.setErrors(messages);
				LOGGER.debug("DataProduct {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			providerResponseVO.setCreatedBy(this.userStore.getVO());
			providerResponseVO.setCreatedDate(new Date());
			if (providerResponseVO.isProviderFormSubmitted() == null) {
				providerResponseVO.setProviderFormSubmitted(false);
			}
			dataProductVO.setProviderInformation(providerResponseVO);
			dataProductVO.setDataProductName(uniqueProductName);
			dataProductVO.setNotifyUsers(requestVO.isNotifyUsers());
			dataProductVO.setPublish(false);
			dataProductVO.setDataProductId("DPF-" + String.format("%05d", dataProductRepository.getNextSeqId()));
			dataProductVO.setRecordStatus(ConstantsUtility.OPEN);
			dataProductVO.setId(null);
			updateDepartments(providerResponseVO.getContactInformation().getDepartment());

			DataProductVO vo = this.create(dataProductVO);
			if (vo != null && vo.getId() != null) {
				responseVO.setData(getProviderVOFromDataProductVO(vo));
				LOGGER.info("DataProduct {} created successfully", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				responseVO.setData(requestVO);
				responseVO.setErrors(messages);
				LOGGER.error("DataProduct {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating dataProduct {} ", e.getMessage(),
					requestVO.getDataProductName());
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
			if (userId.equalsIgnoreCase(createdBy.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Transactional
	public ResponseEntity<DataProductProviderResponseVO> updateDataProductProvider(ProviderVO requestVO) {
		DataProductProviderResponseVO responseVO = new DataProductProviderResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			ProviderResponseVO providerResponseVO = requestVO.getProviderInformation();
			String id = requestVO.getId();
			DataProductVO existingVO = super.getById(id);
			DataProductVO mergedVO = null;
			if (providerResponseVO.isProviderFormSubmitted() == null) {
				providerResponseVO.setProviderFormSubmitted(false);
			}
			if (existingVO != null && existingVO.getRecordStatus() != null
					&& !existingVO.getRecordStatus().equalsIgnoreCase(ConstantsUtility.DELETED)) {
				CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
				if (hasProviderAccess(createdBy)) {
					if (!ObjectUtils.isEmpty(providerResponseVO.getUsers())) {
						if (providerResponseVO.getUsers().stream()
								.anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()))) {
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage("Provider cannot be added as a consumer");
							messages.add(message);
							responseVO.setData(requestVO);
							responseVO.setErrors(messages);
							LOGGER.error("DataProduct with id {} , failed to update", id);
							return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
						}
					}
					providerResponseVO.setCreatedBy(createdBy);
					providerResponseVO.setCreatedDate(existingVO.getProviderInformation().getCreatedDate());
					providerResponseVO.lastModifiedDate(new Date());
					providerResponseVO.setModifiedBy(currentUser);
					dataProductVO.setProviderInformation(providerResponseVO);
					dataProductVO.setDataProductId(existingVO.getDataProductId());
					dataProductVO.setDataProductName(requestVO.getDataProductName());
					dataProductVO.setPublish(existingVO.isPublish());
					dataProductVO.setNotifyUsers(requestVO.isNotifyUsers());
					dataProductVO.setRecordStatus(existingVO.getRecordStatus());
					dataProductVO.setId(id);
					dataProductVO.setConsumerInformation(existingVO.getConsumerInformation());

					updateDepartments(providerResponseVO.getContactInformation().getDepartment());
					mergedVO = this.create(dataProductVO);
					if (mergedVO != null && mergedVO.getId() != null) {
						responseVO.setData(getProviderVOFromDataProductVO(mergedVO));
						LOGGER.info("DataProduct with id {} updated successfully", id);
						this.publishEventMessages(existingVO, mergedVO);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						responseVO.setData(requestVO);
						responseVO.setErrors(messages);
						LOGGER.debug("DataProduct with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to edit dataProduct provider form.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataProduct provider form with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataProduct found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				responseVO.setErrors(notFoundmessages);
				LOGGER.debug("No dataProduct found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataProduct with id {} cannot be edited. Failed due to internal error {} ", requestVO.getId(),
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
	private boolean hasConsumeAccess(DataProductVO existingVO) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		boolean canProceed = false;
		CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
		List<DataProductTeamMemberVO> users = existingVO.getProviderInformation().getUsers();
		if (StringUtils.hasText(userId) && !userId.equalsIgnoreCase(createdBy.getId())) {
			DataProductTeamMemberVO vo = new DataProductTeamMemberVO();
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
				for (DataProductTeamMemberVO member : users) {
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
	public ResponseEntity<DataProductConsumerResponseVO> updateDataProductConsumer(ConsumerVO requestVO) {
		DataProductConsumerResponseVO responseVO = new DataProductConsumerResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		ConsumerVO consumerVO = new ConsumerVO();
		try {
			ConsumerResponseVO consumerResponseVO = requestVO.getConsumerInformation();
			String id = requestVO.getId();
			DataProductVO existingVO = super.getById(id);
			DataProductVO mergedVO = null;
			if (requestVO.isPublish() == null) {
				requestVO.setPublish(false);
			}
			if (existingVO != null && existingVO.getRecordStatus() != null
					&& !existingVO.getRecordStatus().equalsIgnoreCase(ConstantsUtility.DELETED)) {
				List<DataProductTeamMemberVO> existingUsers = null;
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
					dataProductVO.setConsumerInformation(consumerResponseVO);
					dataProductVO.setDataProductName(existingVO.getDataProductName());
					dataProductVO.setPublish(requestVO.isPublish());
					dataProductVO.setRecordStatus(existingVO.getRecordStatus());
					dataProductVO.setDataProductId(existingVO.getDataProductId());
					dataProductVO.setId(id);
					dataProductVO.setNotifyUsers(requestVO.isNotifyUsers());
					dataProductVO.setProviderInformation(existingVO.getProviderInformation());
					updateDepartments(consumerResponseVO.getContactInformation().getDepartment());
					mergedVO = this.create(dataProductVO);
					if (mergedVO != null && mergedVO.getId() != null) {
						consumerVO.setConsumerInformation(mergedVO.getConsumerInformation());
						consumerVO.setId(mergedVO.getId());
						consumerVO.setDataProductId(mergedVO.getDataProductId());
						consumerVO.setPublish(mergedVO.isPublish());
						consumerVO.setNotifyUsers(mergedVO.isNotifyUsers());
						consumerVO.setDataProductName(mergedVO.getDataProductName());
						consumerVO.setRecordStatus(mergedVO.getRecordStatus());
						responseVO.setData(consumerVO);
						LOGGER.info("DataProduct with id {} updated successfully", id);
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
						LOGGER.debug("DataProduct with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to consume dataProduct.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("User not authorized to consume data product with id {}", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataProduct found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				responseVO.setErrors(notFoundmessages);
				LOGGER.debug("No dataProduct found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataProduct with id {} cannot be edited. Failed due to internal error {} ", requestVO.getId(),
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

	private void publishEventMessages(DataProductVO prevDataProductVO, DataProductVO currDataProductVO) {
		try {
			ProviderResponseVO currProviderVO = currDataProductVO.getProviderInformation();
			if (currDataProductVO.isNotifyUsers()) {
				CreatedByVO currentUser = this.userStore.getVO();
				String resourceID = currDataProductVO.getDataProductId();
				String existingDataProductName = prevDataProductVO.getDataProductName();
				String currentDataProductName = currDataProductVO.getDataProductName();
				String eventType = "";
				String eventMessage = "";
				String userName = super.currentUserName(currentUser);
				String userId = currentUser != null ? currentUser.getId() : "dna_system";
				String userEmailId = currentUser != null ? currentUser.getEmail() : "";
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					for (DataProductTeamMemberVO user : currProviderVO.getUsers()) {
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

				if (!prevDataProductVO.isPublish() && currDataProductVO.isPublish()) {
					eventType = "DataProduct - Consumer form Published";
					eventMessage = "A Minimum Information Documentation data transfer is complete. [view]("
							+ dataProductBaseUrl + "summary/" + resourceID + ")";
					LOGGER.info("Publishing message on consumer form submission for dataProduct {} by userId {}",
							currentDataProductName, userId);

				} else if (prevDataProductVO.isPublish() && currDataProductVO.isPublish()) {
					eventType = "DataProduct_Update";
					eventMessage = "DataProduct " + existingDataProductName + " is updated by user " + userName;
					changeLogs = dataProductAssembler.jsonObjectCompare(currDataProductVO, prevDataProductVO,
							currentUser);
					LOGGER.info("Publishing message on update for dataProduct {} by userId {}", existingDataProductName,
							userId);
				} else if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					eventType = "DataProduct - Provider Form Submitted";
					eventMessage = "A Minimum Information Documentation is ready for you. Please [provide information]("
							+ dataProductBaseUrl + "consume/" + resourceID + ")"
							+ " about the receiving side to finalise the Data Transfer.";
					LOGGER.info("Publishing message on provider form submission for dataProduct {} by userId {}",
							currentDataProductName, userId);
				}
				if (StringUtils.hasText(eventType)) {
					kafkaProducer.send(eventType, resourceID, "", userId, eventMessage, true, teamMembers,
							teamMembersEmails, changeLogs);
					LOGGER.info("Published successfully event {} for data product with id {}", eventType, resourceID);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed while publishing dataProduct event msg {} ", e.getMessage());
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataProduct(String id) {
		try {
			DataProductVO existingVO = super.getById(id);
			DataProductVO dataProductVO = null;
			if (existingVO != null && existingVO.getId() != null) {
				CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
				if (hasProviderAccess(createdBy)) {
					ProviderResponseVO providerResponseVO = existingVO.getProviderInformation();
					providerResponseVO.lastModifiedDate(new Date());
					providerResponseVO.setModifiedBy(this.userStore.getVO());
					existingVO.setProviderInformation(providerResponseVO);
					existingVO.setRecordStatus(ConstantsUtility.DELETED);
					dataProductVO = super.create(existingVO);
					if (dataProductVO != null && dataProductVO.getId() != null) {
						GenericMessage successMsg = new GenericMessage();
						successMsg.setSuccess("success");
						LOGGER.info("DataProduct with id {} deleted successfully", id);
						return new ResponseEntity<>(successMsg, HttpStatus.OK);
					} else {
						MessageDescription exceptionMsg = new MessageDescription(
								"Failed to delete dataProduct due to internal error");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.addErrors(exceptionMsg);
						LOGGER.debug("DataProduct with id {} cannot be deleted. Failed with unknown internal error",
								id);
						return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				} else {
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to delete dataProduct.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(notAuthorizedMsg);
					LOGGER.debug("DataProduct with id {} cannot be deleted. User not authorized", id);
					return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
				}
			} else {
				MessageDescription invalidMsg = new MessageDescription("No dataProduct with the given id found");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(invalidMsg);
				LOGGER.debug("No dataProduct with the given id {} found.", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete dataProduct with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ProviderVO getProviderVOFromDataProductVO(DataProductVO vo) {
		ProviderVO providerVO = new ProviderVO();
		providerVO.setProviderInformation(vo.getProviderInformation());
		providerVO.setId(vo.getId());
		providerVO.setDataProductId(vo.getDataProductId());
		providerVO.setDataProductName(vo.getDataProductName());
		providerVO.setRecordStatus(vo.getRecordStatus());
		providerVO.setNotifyUsers(vo.isNotifyUsers());
		return providerVO;

	}

}
