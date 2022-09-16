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

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.dto.dataproduct.ProviderResponseVO;
import com.daimler.data.dto.dataproduct.ProviderVO;
import com.daimler.data.dto.dataproduct.TeamMemberVO;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.notifications.common.producer.KafkaProducerService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.department.DepartmentService;

import io.jsonwebtoken.lang.Strings;

@Service
@SuppressWarnings(value = "unused")
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
	public DataProductVO create(DataProductVO vo) {
		updateDepartments(vo);
		return super.create(vo);
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
			String sortOrder) {
		List<DataProductNsql> dataProductEntities = dataProductCustomRepository
				.getAllWithFiltersUsingNativeQuery(published, offset, limit, sortBy, sortOrder);
		if (!ObjectUtils.isEmpty(dataProductEntities))
			return dataProductEntities.stream().map(n -> dataProductAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(Boolean published) {
		return dataProductCustomRepository.getCountUsingNativeQuery(published);
	}

	private void updateDepartments(DataProductVO vo) {
		if (vo != null && vo.getProviderInformation() != null
				&& vo.getProviderInformation().getContactInformation() != null) {
			String providerDepartment = vo.getProviderInformation().getContactInformation().getDepartment();
			if (Strings.hasText(providerDepartment)) {
				DepartmentVO existingDepartmentVO = departmentService.getByUniqueliteral("name", providerDepartment);
				if (existingDepartmentVO != null && existingDepartmentVO.getName() != null
						&& existingDepartmentVO.getName().equalsIgnoreCase(providerDepartment)) {
					return;
				} else {
					DepartmentVO newDepartmentVO = new DepartmentVO();
					newDepartmentVO.setId(null);
					newDepartmentVO.setName(providerDepartment);
					departmentService.create(newDepartmentVO);
				}
			}

		}
		if (vo != null && vo.getConsumerInformation() != null
				&& vo.getConsumerInformation().getContactInformation() != null) {
			String consumerDepartment = vo.getConsumerInformation().getContactInformation().getDepartment();
			if (Strings.hasText(consumerDepartment)) {
				DepartmentVO existingDepartmentVO = departmentService.getByUniqueliteral("name", consumerDepartment);
				if (existingDepartmentVO != null && existingDepartmentVO.getName() != null
						&& existingDepartmentVO.getName().equalsIgnoreCase(consumerDepartment)) {
					return;
				} else {
					DepartmentVO newDepartmentVO = new DepartmentVO();
					newDepartmentVO.setId(null);
					newDepartmentVO.setName(consumerDepartment);
					departmentService.create(newDepartmentVO);
				}

			}
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DataProductProviderResponseVO> createDataProductProvider(ProviderVO requestVO) {
		DataProductProviderResponseVO responseVO = new DataProductProviderResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		ProviderVO providerVO = new ProviderVO();
		try {
			ProviderResponseVO providerResponseVO = requestVO.getProviderInformation();
			String uniqueProductName = requestVO.getDataProductName();
			DataProductVO existingVO = super.getByUniqueliteral("dataProductName", uniqueProductName);
			if (existingVO != null && existingVO.getDataProductName() != null) {
				providerVO.setProviderInformation(existingVO.getProviderInformation());
				providerVO.setId(existingVO.getId());
				providerVO.setDataProductId(existingVO.getDataProductId());
				providerVO.setDataProductName(existingVO.getDataProductName());
				providerVO.setRecordStatus(existingVO.getRecordStatus());
				responseVO.setData(providerVO);
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
			if (providerResponseVO.isProviderFormSubmitted() == null)
				providerResponseVO.setProviderFormSubmitted(false);

			dataProductVO.setProviderInformation(providerResponseVO);
			dataProductVO.setDataProductName(uniqueProductName);
			dataProductVO.setNotifyUsers(requestVO.isNotifyUsers());
			dataProductVO.setPublish(false);
			dataProductVO.setDataProductId("DP-" + String.format("%04d", dataProductRepository.getNextSeqId()));
			dataProductVO.setRecordStatus("OPEN");
			dataProductVO.setId(null);
			DataProductVO vo = this.create(dataProductVO);
			if (vo != null && vo.getId() != null) {
				providerVO.setProviderInformation(vo.getProviderInformation());
				providerVO.setId(vo.getId());
				providerVO.setDataProductId(vo.getDataProductId());
				providerVO.setDataProductName(vo.getDataProductName());
				providerVO.setRecordStatus(vo.getRecordStatus());
				providerVO.setNotifyUsers(vo.isNotifyUsers());
				responseVO.setData(providerVO);
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

	@Override
	@Transactional
	public ResponseEntity<DataProductProviderResponseVO> updateDataProductProvider(ProviderVO requestVO) {
		DataProductProviderResponseVO responseVO = new DataProductProviderResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		ProviderVO providerVO = new ProviderVO();
		try {
			ProviderResponseVO providerResponseVO = requestVO.getProviderInformation();
			String id = requestVO.getId();
			DataProductVO existingVO = super.getById(id);
			DataProductVO mergedVO = null;
			if (providerResponseVO.isProviderFormSubmitted() == null) {
				providerResponseVO.setProviderFormSubmitted(false);
			}
			if (existingVO != null && existingVO.getId() != null) {
				CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
				if (true) {
					providerResponseVO.setCreatedBy(createdBy);
					providerResponseVO.setCreatedDate(existingVO.getProviderInformation().getCreatedDate());
					providerResponseVO.lastModifiedDate(new Date());
					providerResponseVO.setModifiedBy(this.userStore.getVO());
					dataProductVO.setProviderInformation(providerResponseVO);
					dataProductVO.setDataProductId(existingVO.getDataProductId());
					dataProductVO.setDataProductName(requestVO.getDataProductName());
					dataProductVO.setPublish(existingVO.isPublish());
					dataProductVO.setNotifyUsers(requestVO.isNotifyUsers());
					dataProductVO.setRecordStatus("OPEN");
					dataProductVO.setId(id);
					dataProductVO.setConsumerInformation(existingVO.getConsumerInformation());
					mergedVO = this.create(dataProductVO);
					if (mergedVO != null && mergedVO.getId() != null) {
						providerVO.setProviderInformation(mergedVO.getProviderInformation());
						providerVO.setId(mergedVO.getId());
						providerVO.setDataProductName(mergedVO.getDataProductName());
						providerVO.setRecordStatus(mergedVO.getRecordStatus());
						providerVO.setNotifyUsers(mergedVO.isNotifyUsers());
						providerVO.setDataProductId(mergedVO.getDataProductId());
						responseVO.setData(providerVO);
						responseVO.setErrors(null);
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
					notAuthorizedMsg.setMessage(
							"Not authorized to edit dataProduct. Only user who created the dataProduct or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataProduct with id {} cannot be edited. User not authorized", id);
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
			if (existingVO != null && existingVO.getId() != null) {
				if (true) {
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
					dataProductVO.setRecordStatus("OPEN");
					dataProductVO.setDataProductId(existingVO.getDataProductId());
					dataProductVO.setId(id);
					dataProductVO.setNotifyUsers(requestVO.isNotifyUsers());
					dataProductVO.setProviderInformation(existingVO.getProviderInformation());
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
						responseVO.setErrors(null);
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
					notAuthorizedMsg.setMessage(
							"Not authorized to edit dataProduct. Only user who created the dataProduct or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataProduct with id {} cannot be edited. User not authorized", id);
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
			ConsumerResponseVO currConsumerVO = currDataProductVO.getConsumerInformation();
			ConsumerResponseVO prevConsumerVO = prevDataProductVO.getConsumerInformation();
			if (currDataProductVO.isNotifyUsers()) {
				CreatedByVO currentUser = this.userStore.getVO();
				String resourceID = currDataProductVO.getId();
				String dataProductName = currDataProductVO.getDataProductName();
				String eventType = "";
				String eventMessage = "";
				String userName = super.currentUserName(currentUser);
				String userId = currentUser != null ? currentUser.getId() : "dna_system";
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					for (TeamMemberVO user : currProviderVO.getUsers()) {
						if (user != null) {
							String shortId = user.getShortId();
							if (StringUtils.hasText(shortId) && !teamMembers.contains(shortId)) {
								teamMembers.add(shortId);
							}
							String emailId = user.getEmail();
							if (StringUtils.hasText(emailId) && !teamMembersEmails.contains(emailId)) {
								teamMembersEmails.add(emailId);
							}
						}
					}
				}

				if (currProviderVO.getCreatedBy() != null) {
					String providerUserId = currProviderVO.getCreatedBy().getId();
					String providerEmailId = currProviderVO.getCreatedBy().getEmail();

					if (StringUtils.hasText(providerUserId)) {
						teamMembers.add(providerUserId);
					}

					if (StringUtils.hasText(providerEmailId)) {
						teamMembersEmails.add(providerEmailId);
					}
				}

				if (!prevDataProductVO.isPublish() && currDataProductVO.isPublish()) {
					eventType = "DataProduct - Consumer form Published";
					// teamMembers.remove(publishingUserId);
					teamMembersEmails.remove(0);
					eventMessage = "A Minimum Information Documentation data transfer is complete. [view]("
							+ dataProductBaseUrl + "summary/" + resourceID + ")";
					LOGGER.info("Publishing message on consumer form submission for dataProduct {} by userId {}",
							dataProductName, userId);

				} else if (prevDataProductVO.isPublish() && currDataProductVO.isPublish()) {
					eventType = "DataProduct_Update";
					eventMessage = "DataProduct " + dataProductName + " is updated by user " + userName;
					changeLogs = dataProductAssembler.jsonObjectCompare(currDataProductVO, prevDataProductVO,
							currentUser);
					LOGGER.info("Publishing message on update for dataProduct {} by userId {}", dataProductName,
							userId);

				} else if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					eventType = "DataProduct - Provider Form Submitted";
					eventMessage = "A Minimum Information Documentation is ready for you. Please [provide information]("
							+ dataProductBaseUrl + "consume/" + resourceID + ")"
							+ " about the receiving side to finalise the Data Transfer.";
					LOGGER.info("Publishing message on provider form submission for dataProduct {} by userId {}",
							dataProductName, userId);
				}
				if (StringUtils.hasText(eventType)) {
					kafkaProducer.send(eventType, resourceID, "", userId, eventMessage, true, teamMembers,
							teamMembersEmails, changeLogs);
					LOGGER.info("Published successfully event {} for data product {}", eventType, dataProductName);
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
			DataProductVO dataProduct = super.getById(id);
			if (true) {
				this.deleteById(id);
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("DataProduct with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete dataProduct. Only the dataProduct owner or an admin can delete the dataProduct.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				LOGGER.debug("DataProduct with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (EntityNotFoundException e) {
			MessageDescription invalidMsg = new MessageDescription("No dataProduct with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			LOGGER.error("No dataProduct with the given id {} , could not delete.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete dataProduct with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
