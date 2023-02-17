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

package com.daimler.data.service.datacompliance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DataComplianceAssembler;
import com.daimler.data.assembler.DataTransferAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataComplianceAuditNsql;
import com.daimler.data.db.entities.DataComplianceNsql;
import com.daimler.data.db.jsonb.CreatedBy;
import com.daimler.data.db.jsonb.DataComplianceAudit;
import com.daimler.data.db.repo.datacompliance.DataComplianceCustomRepository;
import com.daimler.data.db.repo.datacompliance.DataComplianceRepository;
import com.daimler.data.db.repo.datacomplianceAudit.DataComplianceAuditRepository;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.datacompliance.DataComplianceResponseVO;
import com.daimler.data.dto.datacompliance.DataComplianceVO;
import com.daimler.data.dto.datatransfer.ChangeLogVO;
import com.daimler.data.dto.entityid.EntityIdVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.entityid.EntityIdService;

@Service
public class BaseDataComplianceService extends BaseCommonService<DataComplianceVO, DataComplianceNsql, String>
		implements DataComplianceService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataComplianceService.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private DataComplianceAssembler dataComplianceAssembler;

	@Autowired
	private EntityIdService entityIdService;

	@Autowired
	private DataTransferAssembler dataProductAssembler;

	@Autowired
	private DataComplianceCustomRepository dataComplianceCustomRepository;

	@Autowired
	private DataComplianceRepository dataComplianceRepository;
	
	@Autowired
	private DataComplianceAuditRepository complianceAuditRepository;

	public BaseDataComplianceService() {
		super();
	}

	@Override
	@Transactional
	public DataComplianceVO create(DataComplianceVO vo) {
		updateEntity(vo);
		return super.create(vo);
	}

	@Override
	public List<DataComplianceVO> getAllWithFilters(String entityId, String entityName, String entityCountry,
			List<String> localComplianceOfficer, List<String> localComplianceResponsible,
			List<String> localComplianceSpecialist, int offset, int limit,
			String sortBy, String sortOrder) {
		List<DataComplianceNsql> entities = dataComplianceCustomRepository.getAllWithFiltersUsingNativeQuery(entityId,
				entityName, entityCountry, localComplianceOfficer, localComplianceResponsible,
				localComplianceSpecialist, offset, limit, sortBy, sortOrder);
		if (!ObjectUtils.isEmpty(entities))
			return entities.stream().map(n -> dataComplianceAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(String entityId, String entityName, String entityCountry, List<String> localComplianceOfficer,
			List<String> localComplianceResponsible, List<String> localComplianceSpecialist) {
		return dataComplianceCustomRepository.getCountUsingNativeQuery(entityId, entityName, entityCountry, localComplianceOfficer,
				localComplianceResponsible, localComplianceSpecialist);
	}

	@Override
	@Transactional
	public ResponseEntity<DataComplianceResponseVO> createDataCompliance(DataComplianceVO requestDataComplianceVO) {
		DataComplianceResponseVO dataComplianceResponseVO = new DataComplianceResponseVO();
		try {
			if (verifyUserRoles()) {
				String uniqueEntityId = requestDataComplianceVO.getEntityId();
				DataComplianceVO existingDataComplianceVO = super.getByUniqueliteral("entityId", uniqueEntityId);
				if (existingDataComplianceVO != null && existingDataComplianceVO.getEntityId() != null) {
					dataComplianceResponseVO.setData(existingDataComplianceVO);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Record already exists for given entityId.");
					messages.add(message);
					dataComplianceResponseVO.setErrors(messages);
					LOGGER.info("EntityId {} already exists, returning as CONFLICT", uniqueEntityId);
					return new ResponseEntity<>(dataComplianceResponseVO, HttpStatus.CONFLICT);
				}
				requestDataComplianceVO.setCreatedBy(this.userStore.getVO());
				requestDataComplianceVO.setCreatedDate(new Date());
				requestDataComplianceVO.setId(null);

				DataComplianceVO dataComplianceVO = this.create(requestDataComplianceVO);
				if (dataComplianceVO != null && dataComplianceVO.getId() != null) {
					String eventType = "DataCompliance_create";
					List<ChangeLogVO> changeLogs = new ArrayList<>();
					CreatedByVO currentUser = this.userStore.getVO();
					String userId = currentUser.getId();
					String userName = super.currentUserName(currentUser);
					changeLogs = dataProductAssembler.jsonObjectCompare(dataComplianceVO, null, currentUser);
					String eventMessage = "Data Compliance Network List with Entity ID %s and Entity Name %s has been added by Admin %s.".formatted(
							dataComplianceVO.getEntityId(),
							dataComplianceVO.getEntityName(),
							userName
						);
					DataComplianceAuditNsql auditNsql = new DataComplianceAuditNsql();
					DataComplianceAudit audit = new DataComplianceAudit();
					List<DataComplianceAuditNsql> auditNsqls = new ArrayList<>();
					audit.setAction("Create");
					audit.setEntityId(dataComplianceVO.getEntityId());
					audit.setMessage(eventMessage);
					audit.setCreatedOn(new Date());
					CreatedBy createdBy = new CreatedBy();
					createdBy.setId(userId);
					createdBy.setFirstName(currentUser.getFirstName());
					createdBy.setLastName(currentUser.getLastName());
					createdBy.setEmail(currentUser.getEmail());
					createdBy.setDepartment(currentUser.getDepartment());
					createdBy.setMobileNumber(currentUser.getMobileNumber());
					audit.setCreatedBy(createdBy);
					auditNsql.setData(audit);
					auditNsqls.add(auditNsql);
					complianceAuditRepository.saveAll(auditNsqls);
					LOGGER.info("Audit logs of Data Compliance entry {} sent successfully",dataComplianceVO.getEntityName());

					super.notifyAllAdminUsers(eventType, dataComplianceVO.getId(), eventMessage, userId, changeLogs);
					dataComplianceResponseVO.setData(dataComplianceVO);
					LOGGER.info("DataCompliance entry {} created successfully", uniqueEntityId);
					return new ResponseEntity<>(dataComplianceResponseVO, HttpStatus.CREATED);
				} else {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to save due to internal error");
					messages.add(message);
					dataComplianceResponseVO.setData(requestDataComplianceVO);
					dataComplianceResponseVO.setErrors(messages);
					LOGGER.error("Failed to create record with entityId {}", uniqueEntityId);
					return new ResponseEntity<>(dataComplianceResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to create dataCompliance entry");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				dataComplianceResponseVO.setErrors(notAuthorizedMsgs);
				LOGGER.debug("DataCompliance entry cannot be created. User not authorized");
				return new ResponseEntity<>(dataComplianceResponseVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating record with entityId {} ", e.getMessage(),
					requestDataComplianceVO.getEntityId());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			dataComplianceResponseVO.setData(requestDataComplianceVO);
			dataComplianceResponseVO.setErrors(messages);
			return new ResponseEntity<>(dataComplianceResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void updateEntity(DataComplianceVO vo) {
		String entityId = vo.getEntityId();
		String entityName = vo.getEntityName();
		EntityIdVO existingVO = entityIdService.getByUniqueliteral("entityId", entityId);
		if (existingVO != null && existingVO.getEntityId().equalsIgnoreCase(entityId))
			return;
		else {
			EntityIdVO newVO = new EntityIdVO();
			newVO.setId(null);
			newVO.setEntityId(entityId);
			newVO.setEntityName(entityName);
			entityIdService.create(newVO);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DataComplianceResponseVO> updateDataCompliance(DataComplianceVO requestDataComplianceVO) {
		DataComplianceResponseVO response = new DataComplianceResponseVO();
		try {
			if (verifyUserRoles()) {
				String id = requestDataComplianceVO.getId();
				DataComplianceVO existingVO = super.getById(id);
				DataComplianceVO mergedDataComplianceVO = null;
				if (existingVO != null && existingVO.getId() != null) {
					String uniqueEntityId = requestDataComplianceVO.getEntityId();
					DataComplianceVO existingEntityIdVO = super.getByUniqueliteral("entityId", uniqueEntityId);
					if (existingEntityIdVO != null && existingEntityIdVO.getEntityId() != null
							&& !existingEntityIdVO.getId().equals(id)) {
						response.setData(existingEntityIdVO);
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("DataCompliance already exists.");
						messages.add(message);
						response.setErrors(messages);
						LOGGER.info("DataCompliance {} already exists, returning as CONFLICT", uniqueEntityId);
						return new ResponseEntity<>(response, HttpStatus.CONFLICT);
					}
					requestDataComplianceVO.setLastModifiedDate(new Date());
					requestDataComplianceVO.setModifiedBy(this.userStore.getVO());
					mergedDataComplianceVO = this.create(requestDataComplianceVO);
					if (mergedDataComplianceVO != null && mergedDataComplianceVO.getId() != null) {
						String eventType = "DataCompliance_update";
						List<ChangeLogVO> changeLogs = new ArrayList<>();
						CreatedByVO currentUser = this.userStore.getVO();
						String userId = currentUser.getId();
						String userName = super.currentUserName(currentUser);
						changeLogs = dataProductAssembler.jsonObjectCompare(mergedDataComplianceVO, existingVO,
								currentUser);
						String eventMessage = "Data Compliance with Entity ID %s and Entity Name %s has been updated by Admin %s.".formatted(
							mergedDataComplianceVO.getEntityId(),
							mergedDataComplianceVO.getEntityName(),
							userName);
						
						DataComplianceAuditNsql auditNsql = new DataComplianceAuditNsql();
						DataComplianceAudit audit = new DataComplianceAudit();
						List<DataComplianceAuditNsql> auditNsqls = new ArrayList<>();
						audit.setAction("Update");
						audit.setEntityId(mergedDataComplianceVO.getEntityId());
						audit.setMessage(eventMessage);
						audit.setCreatedOn(new Date());
						CreatedBy createdBy = new CreatedBy();
						createdBy.setId(userId);
						createdBy.setFirstName(currentUser.getFirstName());
						createdBy.setLastName(currentUser.getLastName());
						createdBy.setEmail(currentUser.getEmail());
						createdBy.setDepartment(currentUser.getDepartment());
						createdBy.setMobileNumber(currentUser.getMobileNumber());
						audit.setCreatedBy(createdBy);
						auditNsql.setData(audit);
						auditNsqls.add(auditNsql);
						complianceAuditRepository.saveAll(auditNsqls);
						LOGGER.info("Audit logs of Data Compliance entry {} sent successfully",mergedDataComplianceVO.getEntityName());
						super.notifyAllAdminUsers(eventType, id, eventMessage, userId, changeLogs);
						response.setData(mergedDataComplianceVO);
						response.setErrors(null);
						LOGGER.info("DataCompliance with id {} updated successfully", id);
						return new ResponseEntity<>(response, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						response.setData(requestDataComplianceVO);
						response.setErrors(messages);
						LOGGER.info("DataCompliance with id {} cannot be edited. Failed with unknown internal error",
								id);
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				} else {
					List<MessageDescription> notFoundmessages = new ArrayList<>();
					MessageDescription notFoundmessage = new MessageDescription();
					notFoundmessage.setMessage("No DataCompliance found for given id. Update cannot happen");
					notFoundmessages.add(notFoundmessage);
					response.setErrors(notFoundmessages);
					LOGGER.info("No DataCompliance found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
				}
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to update dataCompliance entry.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				response.setErrors(notAuthorizedMsgs);
				LOGGER.debug("DataCompliance entry cannot be edited. User not authorized");
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("DataCompliance with id {} cannot be edited. Failed due to internal error {} ",
					requestDataComplianceVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			response.setData(requestDataComplianceVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataCompliance(String id) {
		try {
			if (verifyUserRoles()) {
				CreatedByVO currentUser = this.userStore.getVO();
				String userId = currentUser.getId();
				DataComplianceVO existingVO = super.getById(id);
				if (existingVO != null && existingVO.getId() != null) {
					String userName = super.currentUserName(currentUser);
					String eventMessage = "DataCompliance with entityID " + existingVO.getEntityId()
							+ " and entityName " + existingVO.getEntityName() + " has been deleted by Admin "
							+ userName;
					String auditMessage =  "Data Compliance with entity ID " + existingVO.getEntityId()
					+ " and entity Name " + existingVO.getEntityName() + " has been deleted by Admin "
					+ userName;
					DataComplianceAuditNsql auditNsql = new DataComplianceAuditNsql();
					DataComplianceAudit audit = new DataComplianceAudit();
					List<DataComplianceAuditNsql> auditNsqls = new ArrayList<>();
					audit.setAction("Delete");
					audit.setEntityId(existingVO.getEntityId());
					audit.setMessage(auditMessage);
					audit.setCreatedOn(new Date());
					CreatedBy createdBy = new CreatedBy();
					createdBy.setId(userId);
					createdBy.setFirstName(currentUser.getFirstName());
					createdBy.setLastName(currentUser.getLastName());
					createdBy.setEmail(currentUser.getEmail());
					createdBy.setDepartment(currentUser.getDepartment());
					createdBy.setMobileNumber(currentUser.getMobileNumber());
					audit.setCreatedBy(createdBy);
					auditNsql.setData(audit);
					auditNsqls.add(auditNsql);
					complianceAuditRepository.saveAll(auditNsqls);
					LOGGER.info("Audit logs of Data Compliance entry {} sent successfully",existingVO.getEntityName());
					this.deleteById(id);
					super.notifyAllAdminUsers("DataCompliance_delete", id, eventMessage, userId, null);
					GenericMessage successMsg = new GenericMessage();
					successMsg.setSuccess("success");
					LOGGER.info("DataCompliance entry with id {} deleted successfully", id);
					return new ResponseEntity<>(successMsg, HttpStatus.OK);
				} else {
					MessageDescription invalidMsg = new MessageDescription(
							"No dataCompliance entry with the given id found");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(invalidMsg);
					LOGGER.debug("No dataCompliance entry with the given id {} found.", id);
					return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
				}

			} else {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to delete dataCompliance entry.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				LOGGER.debug("DataCompliance entry cannot be deleted. User not authorized");
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete dataCompliance with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
