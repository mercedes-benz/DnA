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

package com.daimler.data.service.department;

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
import com.daimler.data.assembler.DepartmentAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DepartmentSql;
import com.daimler.data.db.repo.department.DepartmentRepository;
import com.daimler.data.dto.department.DepartmentCollection;
import com.daimler.data.dto.department.DepartmentNameVO;
import com.daimler.data.dto.department.DepartmentRequestVO;
import com.daimler.data.dto.department.DepartmentResponseVO;
import com.daimler.data.dto.department.DepartmentUpdateRequestVO;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.report.ReportService;

@Service
public class BaseDepartmentService extends BaseCommonService<DepartmentVO, DepartmentSql, Long>
		implements DepartmentService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDepartmentService.class);

	private DepartmentRepository jpaRepo;

	private DepartmentAssembler departmentAssembler;

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private UserStore userStore;

	public BaseDepartmentService() {
		super();
	}

	@Autowired
	public BaseDepartmentService(DepartmentRepository jpaRepo, DepartmentAssembler departmentAssembler) {
		super(jpaRepo, departmentAssembler);
		this.jpaRepo = jpaRepo;
		this.departmentAssembler = departmentAssembler;
	}

	@Override
	public ResponseEntity<DepartmentCollection> getAllDepartments(String sortOrder) {
		DepartmentCollection departmentCollection = new DepartmentCollection();
		try {
			List<DepartmentVO> departments = super.getAll();
			LOGGER.debug("Departments fetched successfully");
			if (!ObjectUtils.isEmpty(departments)) {
				if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
					departments.sort(Comparator.comparing(DepartmentVO :: getName, String.CASE_INSENSITIVE_ORDER));
				}
				if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
					departments.sort(Comparator.comparing(DepartmentVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
				}
				departmentCollection.setData(departments);
				return new ResponseEntity<>(departmentCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(departmentCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch departments with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DepartmentResponseVO> createDepartment(DepartmentRequestVO requestVO) {
		DepartmentResponseVO responseVO = new DepartmentResponseVO();
		try {			
				DepartmentNameVO departmentNameVO = requestVO.getData();
				String uniqueDepartmentName = departmentNameVO.getName();
				DepartmentVO existingDepartmentVO = findDepartmentByName(uniqueDepartmentName);
				if (existingDepartmentVO != null && existingDepartmentVO.getName() != null) {
					responseVO.setData(existingDepartmentVO);
					LOGGER.debug("Department {} already exists, returning as CONFLICT", uniqueDepartmentName);
					return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
				}

				DepartmentVO departmentVO = new DepartmentVO();
				departmentVO.setName(uniqueDepartmentName);
				DepartmentVO vo = super.create(departmentVO);
				if (vo != null && vo.getId() != null) {
					responseVO.setData(vo);
					LOGGER.info("Department {} created successfully", uniqueDepartmentName);
					return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
				} else {
					LOGGER.error("Department {} , failed to create", uniqueDepartmentName);
					return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				} 
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating department ", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DepartmentResponseVO> updateDepartment(DepartmentUpdateRequestVO requestVO) {
		DepartmentResponseVO responseVO = new DepartmentResponseVO();
		try {
			if (verifyUserRoles()) {
				DepartmentVO departmentVO = requestVO.getData();
				long id = departmentVO.getId();
				Optional<DepartmentSql> departmentEntity = jpaRepo.findById(id);
				if (departmentEntity.isPresent()) {
					DepartmentVO existingDepartmentVO = findDepartmentByName(departmentVO.getName());
					if (existingDepartmentVO != null && existingDepartmentVO.getName() != null) {
						responseVO.setData(existingDepartmentVO);
						LOGGER.debug("Department {} already exists, returning as CONFLICT", departmentVO.getName());
						return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
					}
					reportService.updateForEachReport(departmentEntity.get().getName(), departmentVO.getName(),
							ReportService.CATEGORY.DEPARTMENT, null);
					CreatedByVO currentUser = this.userStore.getVO();
					String userId = currentUser != null ? currentUser.getId() : "";
					String userName = currentUserName(currentUser);
					String eventMessage = "Department " + departmentEntity.get().getName() + " has been updated by Admin " + userName;
					super.notifyAllAdminUsers("Dashboard-Report MDM Update", departmentEntity.get().getName(), eventMessage, userId, null);
					DepartmentVO vo = super.create(departmentVO);
					if (vo != null && vo.getId() != null) {
						responseVO.setData(vo);
						LOGGER.debug("Department with id {} updated successfully", id);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						LOGGER.debug("Department with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.debug("No department found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
				}

			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to update department. Only user with admin role can update.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				responseVO.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Department with id {} cannot be edited. User not authorized",
						requestVO.getData().getId());
				return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Department with id {} cannot be edited. Failed due to internal error {} ",
					requestVO.getData().getId(), e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDepartment(long id) {
		try {
			if (verifyUserRoles()) {
				Optional<DepartmentSql> departmentEntity = jpaRepo.findById(id);
				if (departmentEntity.isPresent()) {
					String departmentName = departmentEntity.get().getName();
					reportService.deleteForEachReport(departmentName, ReportService.CATEGORY.DEPARTMENT);
					CreatedByVO currentUser = this.userStore.getVO();
					String userId = currentUser != null ? currentUser.getId() : "";
					String userName = currentUserName(currentUser);
					String eventMessage = "Department " + departmentName + " has been deleted by Admin " + userName;
					super.notifyAllAdminUsers("Dashboard-Report MDM Delete", departmentName, eventMessage, userId, null);
					jpaRepo.deleteById(id);
				}
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("Department with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg
						.setMessage("Not authorized to delete department. Only user with admin role can delete.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Department with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to delete department with id {} , due to internal error.", id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public DepartmentVO findDepartmentByName(String departmentName) {
		DepartmentSql departmentEntity = jpaRepo.findFirstByNameIgnoreCase(departmentName);
		return departmentAssembler.toVo(departmentEntity);
	}

}
