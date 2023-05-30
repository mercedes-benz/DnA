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

package com.daimler.data.service.kpiName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.daimler.data.assembler.KpiNameAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.KpiNameSql;
import com.daimler.data.db.repo.kpiName.KpiNameRepository;
import com.daimler.data.dto.KpiName.KpiNameCreateRequestVO;
import com.daimler.data.dto.KpiName.KpiNameRequestVO;
import com.daimler.data.dto.KpiName.KpiNameResponseVO;
import com.daimler.data.dto.KpiName.KpiNameUpdateRequestVO;
import com.daimler.data.dto.KpiName.KpiNameVO;
import com.daimler.data.dto.KpiName.KpiNameVOCollection;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.report.ReportService;

@Service
public class BaseKpiNameService extends BaseCommonService<KpiNameVO, KpiNameSql, Long>
		implements KpiNameService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseKpiNameService.class);

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private KpiNameRepository jpaRepo;
	
	@Autowired
	private KpiNameAssembler kpiNameAssembler;

	public BaseKpiNameService() {
		super();
	}
	
	@Autowired
	public BaseKpiNameService(KpiNameRepository jpaRepo, KpiNameAssembler kpiNameAssembler) {		
		super(jpaRepo, kpiNameAssembler);
		this.kpiNameAssembler = kpiNameAssembler;
		this.jpaRepo = jpaRepo;
	}


	@Override
	public ResponseEntity<KpiNameVOCollection> getAllKpiNames(String sortOrder) {
		KpiNameVOCollection kpiNameVOCollection = new KpiNameVOCollection();
		try {
			List<KpiNameVO> kpiNames = super.getAll();
			LOGGER.info("kpiNames fetched successfully");
			if (!ObjectUtils.isEmpty(kpiNames)) {
				if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
					kpiNames.sort(Comparator.comparing(KpiNameVO :: getKpiName, String.CASE_INSENSITIVE_ORDER));
				}
				if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
					kpiNames.sort(Comparator.comparing(KpiNameVO :: getKpiName, String.CASE_INSENSITIVE_ORDER).reversed());
				}
				kpiNameVOCollection.setData(kpiNames);
				return new ResponseEntity<>(kpiNameVOCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(kpiNameVOCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch kpiNames with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	public ResponseEntity<KpiNameResponseVO> createKpiName(KpiNameRequestVO requestVO) {
		KpiNameResponseVO responseVO = new KpiNameResponseVO();
		try {			
			KpiNameCreateRequestVO kpiNameCreateRequestVO = requestVO.getData();
				String uniqueKpiName = kpiNameCreateRequestVO.getName();
				String KpiClassification = kpiNameCreateRequestVO.getClassification();
				KpiNameVO existingKpiNameVO = findKpiNameByName(uniqueKpiName);
				if (existingKpiNameVO != null && existingKpiNameVO.getKpiName() != null) {
					responseVO.setData(existingKpiNameVO);
					LOGGER.info("KpiName {} already exists, returning as CONFLICT", uniqueKpiName);
					return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
				}
				KpiNameVO kpiNameVO = new KpiNameVO();
				kpiNameVO.setKpiName(uniqueKpiName);
				kpiNameVO.setKpiClassification(KpiClassification);
				KpiNameVO vo = super.create(kpiNameVO);
				if (vo != null && vo.getId() != null) {
					responseVO.setData(vo);
					LOGGER.info("KpiName {} created successfully", uniqueKpiName);
					return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
				} else {
					LOGGER.error("KpiName {} , failed to create", uniqueKpiName);
					return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				} 
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating KpiName ", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<KpiNameResponseVO> updateKpiName(KpiNameUpdateRequestVO requestVO) {
		KpiNameResponseVO responseVO = new KpiNameResponseVO();
		try {
			if (verifyUserRoles()) {
				KpiNameVO kpiNameVO = requestVO.getData();
				Long id = kpiNameVO.getId();
				KpiNameVO existingVO = super.getById(id);
				if (existingVO != null && existingVO.getId() != null) {
					KpiNameVO existingKpiNameVO = findKpiNameByName(kpiNameVO.getKpiName());
					if (existingKpiNameVO != null && existingKpiNameVO.getKpiName() != null) {
						responseVO.setData(existingKpiNameVO);
						LOGGER.info("KpiName {} already exists, returning as CONFLICT", kpiNameVO.getKpiName());
						return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
					}
					reportService.updateForEachReport(existingVO.getKpiName(), kpiNameVO.getKpiName(),
							ReportService.CATEGORY.KPI_NAME, null);
					KpiNameVO vo = super.create(kpiNameVO);
					if (vo != null && vo.getId() != null) {
						responseVO.setData(vo);
						LOGGER.info("KpiName with id {} updated successfully", id);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						LOGGER.info("KpiName with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.debug("No KpiName found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
				}

			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to update KpiName. Only user with admin role can update.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				responseVO.setErrors(notAuthorizedMsgs);
				LOGGER.info("KpiName with id {} cannot be edited. User not authorized",
						requestVO.getData().getId());
				return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("KpiName with id {} cannot be edited. Failed due to internal error {} ",
					requestVO.getData().getId(), e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<GenericMessage> deleteKpiName(Long id) {
		try {
			if (verifyUserRoles()) {
				Optional<KpiNameSql> kpiNameEntity = jpaRepo.findById(id);
				if (kpiNameEntity.isPresent()) {
					String kpiName = kpiNameEntity.get().getName();
					reportService.deleteForEachReport(kpiName, ReportService.CATEGORY.KPI_NAME);
					jpaRepo.deleteById(id);
				}
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("KpiName with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to delete KpiName. Only user with admin role can delete.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setErrors(notAuthorizedMsgs);
				LOGGER.info("KpiName with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to delete KpiName with id {} , due to internal error.", id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	
	@Override
	public KpiNameVO findKpiNameByName(String kpiName) {
		KpiNameSql kpiNameEntity = jpaRepo.findFirstByNameIgnoreCase(kpiName);
		return kpiNameAssembler.toVo(kpiNameEntity);
	}


}
