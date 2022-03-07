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

package com.daimler.data.service.datawarehouse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.data.assembler.DataWarehouseAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataWarehouseNsql;
import com.daimler.data.db.repo.datawarehouse.DataWarehouseCustomRepository;
import com.daimler.data.db.repo.datawarehouse.DataWarehouseRepository;
import com.daimler.data.dto.datawarehouse.DataWarehouseCollection;
import com.daimler.data.dto.datawarehouse.DataWarehouseInUseVO;
import com.daimler.data.dto.datawarehouse.DataWarehouseResponseVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.report.ReportService;

@Service
public class BaseDataWarehouseService extends BaseCommonService<DataWarehouseInUseVO, DataWarehouseNsql, String>
		implements DataWarehouseService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataWarehouseService.class);

	private DataWarehouseRepository jpaRepo;

	@Autowired
	private ReportService reportService;

	public BaseDataWarehouseService() {
		super();
	}

	@Autowired
	public BaseDataWarehouseService(DataWarehouseRepository dataWarehouseRepository,
			DataWarehouseAssembler dataWarehouseAssembler,
			DataWarehouseCustomRepository dataWarehouseCustomRepository) {
		super(dataWarehouseRepository, dataWarehouseAssembler, dataWarehouseCustomRepository);
		this.jpaRepo = dataWarehouseRepository;
	}

	@Override
	@Transactional
	public ResponseEntity<DataWarehouseResponseVO> createDataWarehouse(DataWarehouseInUseVO vo) {
		DataWarehouseResponseVO dataWarehouseResponseVO = new DataWarehouseResponseVO();
		try {
			if (verifyUserRoles()) {
				String uniqueDataWarehouseName = vo.getDataWarehouse();
				DataWarehouseInUseVO existingDataWarehouseInUseVO = super.getByUniqueliteral("dataWarehouse",
						uniqueDataWarehouseName);
				if (existingDataWarehouseInUseVO != null && existingDataWarehouseInUseVO.getDataWarehouse() != null) {
					dataWarehouseResponseVO.setData(existingDataWarehouseInUseVO);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("DataWarehouse already exists.");
					messages.add(message);
					dataWarehouseResponseVO.setErrors(messages);
					LOGGER.debug("DataWarehouse {} already exists, returning as CONFLICT", uniqueDataWarehouseName);
					return new ResponseEntity<>(dataWarehouseResponseVO, HttpStatus.CONFLICT);
				}
				vo.setId(null);
				DataWarehouseInUseVO dataWarehouseInUseVO = super.create(vo);
				if (dataWarehouseInUseVO != null && dataWarehouseInUseVO.getId() != null) {
					dataWarehouseResponseVO.setData(dataWarehouseInUseVO);
					LOGGER.info("DataWarehouse {} created successfully", uniqueDataWarehouseName);
					return new ResponseEntity<>(dataWarehouseResponseVO, HttpStatus.CREATED);
				} else {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to save due to internal error");
					messages.add(message);
					dataWarehouseResponseVO.setData(vo);
					dataWarehouseResponseVO.setErrors(messages);
					LOGGER.error("DataWarehouse {} , failed to create", uniqueDataWarehouseName);
					return new ResponseEntity<>(dataWarehouseResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to create dataWarehouse. Only user with admin role can create.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				dataWarehouseResponseVO.setErrors(notAuthorizedMsgs);
				return new ResponseEntity<>(dataWarehouseResponseVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating dataWarehouse {} ", e.getMessage(),
					vo.getDataWarehouse());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			dataWarehouseResponseVO.setData(vo);
			dataWarehouseResponseVO.setErrors(messages);
			return new ResponseEntity<>(dataWarehouseResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<DataWarehouseCollection> getAllDataWarehouse() {
		DataWarehouseCollection dataWarehouseCollection = new DataWarehouseCollection();
		try {
			List<DataWarehouseInUseVO> dataWarehouses = super.getAll();
			LOGGER.debug("DataWarehouses fetched successfully");
			if (!ObjectUtils.isEmpty(dataWarehouses)) {
				dataWarehouseCollection.setRecords(dataWarehouses);
				return new ResponseEntity<>(dataWarehouseCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(dataWarehouseCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to fetch dataWarehouses with exception {} ", e.getMessage());
			throw e;
		}

	}

	@Override
	@Transactional
	public ResponseEntity<DataWarehouseResponseVO> updateDataWarehouse(DataWarehouseInUseVO requestVO) {
		DataWarehouseResponseVO response = new DataWarehouseResponseVO();
		try {
			if (verifyUserRoles()) {
				String id = requestVO.getId();
				DataWarehouseInUseVO existingVO = super.getById(id);
				DataWarehouseInUseVO mergedDataWarehouseInUseVO = null;

				if (existingVO != null && existingVO.getId() != null) {
					String uniqueDataWarehouseName = requestVO.getDataWarehouse();
					DataWarehouseInUseVO existingdataWarehouseNameVO = super.getByUniqueliteral("dataWarehouse",
							uniqueDataWarehouseName);
					if (existingdataWarehouseNameVO != null && existingdataWarehouseNameVO.getDataWarehouse() != null
							&& !existingdataWarehouseNameVO.getId().equals(id)) {
						response.setData(existingdataWarehouseNameVO);
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("DataWarehouse already exists.");
						messages.add(message);
						response.setErrors(messages);
						LOGGER.debug("DataWarehouse {} already exists, returning as CONFLICT", uniqueDataWarehouseName);
						return new ResponseEntity<>(response, HttpStatus.CONFLICT);
					}
					reportService.updateForEachReport(existingVO.getDataWarehouse(), uniqueDataWarehouseName,
							ReportService.CATEGORY.DATA_WAREHOUSE, requestVO);
					mergedDataWarehouseInUseVO = super.create(requestVO);
					if (mergedDataWarehouseInUseVO != null && mergedDataWarehouseInUseVO.getId() != null) {
						response.setData(mergedDataWarehouseInUseVO);
						response.setErrors(null);
						LOGGER.debug("DataWarehouse with id {} updated successfully", id);
						return new ResponseEntity<>(response, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						response.setData(requestVO);
						response.setErrors(messages);
						LOGGER.debug("DataWarehouse with id {} cannot be edited. Failed with unknown internal error",
								id);
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				} else {
					List<MessageDescription> notFoundmessages = new ArrayList<>();
					MessageDescription notFoundmessage = new MessageDescription();
					notFoundmessage.setMessage("No DataWarehouse found for given id. Update cannot happen");
					notFoundmessages.add(notFoundmessage);
					response.setErrors(notFoundmessages);
					LOGGER.debug("No DataWarehouse found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
				}
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to update dataWarehouse. Only user with admin role can update.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				response.setErrors(notAuthorizedMsgs);
				LOGGER.debug("DataWarehouse with id {} cannot be edited. User not authorized", requestVO.getId());
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("DataWarehouse with id {} cannot be edited. Failed due to internal error {} ",
					requestVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			response.setData(requestVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataWarehouse(String id) {
		try {
			if (verifyUserRoles()) {
				DataWarehouseInUseVO existingVO = super.getById(id);
				if (existingVO != null && existingVO.getId() != null) {
					reportService.deleteForEachReport(existingVO.getDataWarehouse(),
							ReportService.CATEGORY.DATA_WAREHOUSE);
					jpaRepo.deleteById(id);
				}
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("DataWarehouse with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to delete dataWarehouse. Only user with admin role can delete.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setErrors(notAuthorizedMsgs);
				LOGGER.debug("DataWarehouse with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to delete dataWarehouse with id {} , due to internal error.", id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
