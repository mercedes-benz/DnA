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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DataProductAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.repo.dataproduct.DataProductCustomRepository;
import com.daimler.data.db.repo.dataproduct.DataProductRepository;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.DataProductResponseVO;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
@SuppressWarnings(value = "unused")
public class BaseDataProductService extends BaseCommonService<DataProductVO, DataProductNsql, String>
		implements DataProductService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataProductService.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private DataProductAssembler dataProductAssembler;

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
		return super.create(vo);
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

	@Override
	@Transactional
	public ResponseEntity<DataProductResponseVO> createDataProduct(DataProductVO requestDataProductVO) {
		DataProductResponseVO dataProductResponseVO = new DataProductResponseVO();
		try {
			String uniqueProductName = requestDataProductVO.getDataProductName();
			DataProductVO existingDataProductVO = super.getByUniqueliteral("dataProductName", uniqueProductName);
			if (existingDataProductVO != null && existingDataProductVO.getDataProductName() != null) {
				dataProductResponseVO.setData(existingDataProductVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("DataProduct already exists.");
				messages.add(message);
				dataProductResponseVO.setErrors(messages);
				LOGGER.debug("DataProduct {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(dataProductResponseVO, HttpStatus.CONFLICT);
			}
			requestDataProductVO.setCreatedBy(this.userStore.getVO());
			requestDataProductVO.setCreatedDate(new Date());
			requestDataProductVO.setId(null);

			if (requestDataProductVO.isPublish() == null)
				requestDataProductVO.setPublish(false);

			DataProductVO dataProductVO = this.create(requestDataProductVO);
			if (dataProductVO != null && dataProductVO.getId() != null) {
				dataProductResponseVO.setData(dataProductVO);
				LOGGER.info("DataProduct {} created successfully", uniqueProductName);
				return new ResponseEntity<>(dataProductResponseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				dataProductResponseVO.setData(requestDataProductVO);
				dataProductResponseVO.setErrors(messages);
				LOGGER.error("DataProduct {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(dataProductResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating dataProduct {} ", e.getMessage(),
					requestDataProductVO.getDataProductName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			dataProductResponseVO.setData(requestDataProductVO);
			dataProductResponseVO.setErrors(messages);
			return new ResponseEntity<>(dataProductResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DataProductResponseVO> updateDataProduct(DataProductVO requestDataProductVO) {
		DataProductResponseVO response = new DataProductResponseVO();
		try {
			String id = requestDataProductVO.getId();
			DataProductVO existingDataProductVO = this.getById(id);
			DataProductVO mergedDataProductVO = null;
			if (requestDataProductVO.isPublish() == null) {
				requestDataProductVO.setPublish(false);
			}
			if (existingDataProductVO != null && existingDataProductVO.getId() != null) {
				CreatedByVO createdBy = existingDataProductVO.getCreatedBy();
				if (true) {
					requestDataProductVO.setCreatedBy(createdBy);
					requestDataProductVO.setCreatedDate(existingDataProductVO.getCreatedDate());
					requestDataProductVO.lastModifiedDate(new Date());
					requestDataProductVO.setModifiedBy(this.userStore.getVO());
					mergedDataProductVO = this.create(requestDataProductVO);
					if (mergedDataProductVO != null && mergedDataProductVO.getId() != null) {
						response.setData(mergedDataProductVO);
						response.setErrors(null);
						LOGGER.info("DataProduct with id {} updated successfully", id);
						return new ResponseEntity<>(response, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						response.setData(requestDataProductVO);
						response.setErrors(messages);
						LOGGER.debug("DataProduct with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage(
							"Not authorized to edit dataProduct. Only user who created the dataProduct or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					response.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataProduct with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataProduct found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				response.setErrors(notFoundmessages);
				LOGGER.debug("No dataProduct found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataProduct with id {} cannot be edited. Failed due to internal error {} ",
					requestDataProductVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			response.setData(requestDataProductVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataProduct(String id) {
		try {
			DataProductVO dataProduct = this.getById(id);
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
