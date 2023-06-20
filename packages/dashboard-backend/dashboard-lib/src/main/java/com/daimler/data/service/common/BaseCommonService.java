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

package com.daimler.data.service.common;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.GenericAssembler;
import com.daimler.data.auth.client.DnaAuthClient;
import com.daimler.data.auth.client.UsersCollection;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.repo.common.CommonDataRepository;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.db.repo.common.LovCommonDataRepository;
import com.daimler.data.dto.lov.LovNameVO;
import com.daimler.data.dto.lov.LovRequestVO;
import com.daimler.data.dto.lov.LovResponseVO;
import com.daimler.data.dto.lov.LovUpdateRequestVO;
import com.daimler.data.dto.lov.LovVO;
import com.daimler.data.dto.lov.LovVOCollection;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.UserInfoVO;
import com.daimler.data.service.report.ReportService;
import com.daimler.data.service.report.ReportService.CATEGORY;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

public class BaseCommonService<V, T, ID> implements CommonService<V, T, ID> {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseCommonService.class);

	@Autowired
	private LovCommonDataRepository lovCommonDataRepository;

	@Autowired
	private ReportService reportService;

	@Autowired
	private UserStore userStore;
	
	@Autowired
	private DnaAuthClient dnaAuthClient;
	
	@Autowired
	private KafkaProducerService kafkaProducer;

	private CommonDataRepository<T, ID> customRepo;

	private JpaRepository<T, ID> jpaRepo;

	private GenericAssembler<V, T> assembler;

	@Autowired
	private PagingAndSortingRepository<T, ID> sortingAndPagingRepo;

	public BaseCommonService() {
		super();
	}

	public BaseCommonService(JpaRepository<T, ID> jpaRepo) {
		super();
		this.jpaRepo = jpaRepo;
	}

	public BaseCommonService(JpaRepository<T, ID> jpaRepo, GenericAssembler<V, T> assembler,
			CommonDataRepository<T, ID> customRepo) {
		super();
		this.jpaRepo = jpaRepo;
		this.assembler = assembler;
		this.customRepo = customRepo;
	}

	public BaseCommonService(JpaRepository<T, ID> jpaRepo, GenericAssembler<V, T> assembler) {
		super();
		this.jpaRepo = jpaRepo;
		this.assembler = assembler;
	}

	@Override
	@Transactional
	public List<V> getAll() {
		List<T> entities = jpaRepo.findAll();
		return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public V getById(ID id) {
		Optional<T> entityOptional = jpaRepo.findById(id);
		T entity = !entityOptional.isEmpty() ? entityOptional.get() : null;
		return assembler.toVo(entity);
	}

	@Override
	@Transactional
	public V getByUniqueliteral(String uniqueLiteral, String value) {
		if (value != null) {
			T entity = customRepo.findbyUniqueLiteral(uniqueLiteral, value);
			return assembler.toVo(entity);
		} else
			return null;
	}

	public List<V> getAllSortedByUniqueLiteralAsc(String uniqueLiteral) {
		if (StringUtils.hasText(uniqueLiteral)) {
			List<T> entities = customRepo.findAllSortyByUniqueLiteral(uniqueLiteral,
					CommonDataRepositoryImpl.SORT_TYPE.ASC);
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());

		} else {
			return null;
		}
	}

	public List<V> getAllSortedByUniqueLiteralDesc(String uniqueLiteral) {
		if (StringUtils.hasText(uniqueLiteral)) {
			List<T> entities = customRepo.findAllSortyByUniqueLiteral(uniqueLiteral,
					CommonDataRepositoryImpl.SORT_TYPE.DESC);
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());

		} else {
			return null;
		}
	}

	@Override
	public List<V> getAllSortedByUniqueLiteral(int limit, int offset, String uniqueLiteral,
			CommonDataRepositoryImpl.SORT_TYPE sortOrder) {
		if (StringUtils.hasText(uniqueLiteral)) {
			List<T> entities = customRepo.findAllSortyByUniqueLiteral(limit, offset, uniqueLiteral, sortOrder);
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());

		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public V create(V vo) {
		T entity = assembler.toEntity(vo);
		T savedEntity = jpaRepo.save(entity);
		return assembler.toVo(savedEntity);
	}

	@Override
	@Transactional
	public void insertAll(List<V> voList) {
		if (Objects.nonNull(voList) && !voList.isEmpty()) {
			List<T> entityList = voList.stream().map(n -> (T) assembler.toEntity(n)).collect(Collectors.toList());
			customRepo.insertAll(entityList);
		}
	}

	@Override
	@Transactional
	public void deleteById(ID id) {
		Optional<T> entity = jpaRepo.findById(id);
		boolean flag = entity.isPresent();
		if (flag) {
			jpaRepo.deleteById(id);
		}
	}

	@Override
	@Transactional
	public void deleteAll() {
		customRepo.deleteAll();
	}

	@Override
	public List<V> getAll(int limit, int offset) {
		List<T> entities = customRepo.findAll(limit, offset);
		return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	public Long getCount(int limit, int offset) {
		return customRepo.getCount(limit, offset);
	}

	@Override
	public ResponseEntity<LovVOCollection> getAllLov( String sortOrder) {
		LovVOCollection lovVOCollection = new LovVOCollection();
		try {
			List<T> entities = jpaRepo.findAll();
			List<LovVO> lovs = entities.stream().map(n -> {
				LovVO lov = new LovVO();
				BeanUtils.copyProperties(n, lov);
				return lov;
			}).collect(Collectors.toList());
			LOGGER.debug("Lovs fetched successfully");
			if (!ObjectUtils.isEmpty(lovs)) {
				if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
					lovs.sort(Comparator.comparing(LovVO :: getName,  String.CASE_INSENSITIVE_ORDER));
				}
				if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
					lovs.sort(Comparator.comparing(LovVO :: getName,  String.CASE_INSENSITIVE_ORDER).reversed());
				}
				lovVOCollection.setData(lovs);
				return new ResponseEntity<>(lovVOCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(lovVOCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch lovs with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	@Transactional
	public ResponseEntity<LovResponseVO> createLov(LovRequestVO lovRequestVO, T entity) {
		LovResponseVO response = new LovResponseVO();
		try {
			if (verifyUserRoles()) {
				LovNameVO lovNameVO = lovRequestVO.getData();
				LovVO lovVo = new LovVO();
				Object[] existingEntity = lovCommonDataRepository.findLovByName(lovNameVO.getName(),
						entity.getClass().getAnnotation(Table.class).name());
				if (existingEntity == null) {
					T savedEntity = jpaRepo.save(entity);
					BeanUtils.copyProperties(savedEntity, lovVo);
					if (lovVo != null && lovVo.getId() != null) {
						response.setData(lovVo);
						LOGGER.info("Lov {} created successfully", lovNameVO.getName());
						return new ResponseEntity<>(response, HttpStatus.CREATED);
					} else {
						LOGGER.error("Lov {} , failed to create", lovNameVO.getName());
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				lovVo.setId(((BigInteger) existingEntity[0]).longValue());
				lovVo.setName(existingEntity[1].toString());
				response.setData(lovVo);
				LOGGER.debug("Lov {} already exists, returning as CONFLICT", lovNameVO.getName());
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to create lov. Only user with admin role can create.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				response.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Lov cannot be created. User not authorized");
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating Lov", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<LovResponseVO> updateLov(LovUpdateRequestVO vo, T entity, CATEGORY category) {
		LovResponseVO response = new LovResponseVO();
		try {
			if (verifyUserRoles()) {
				ID id = (ID) vo.getData().getId();
				LovVO lovVo = new LovVO();
				Object[] exists = lovCommonDataRepository.findLovById((long) id,
						entity.getClass().getAnnotation(Table.class).name());
				if (exists != null) {
					Object[] existingEntity = lovCommonDataRepository.findLovByName(vo.getData().getName(),
							entity.getClass().getAnnotation(Table.class).name());
					if (existingEntity == null) {
						reportService.updateForEachReport(exists[1].toString(), vo.getData().getName(), category, null);
						T savedEntity = jpaRepo.save(entity);
						BeanUtils.copyProperties(savedEntity, lovVo);
						if (lovVo != null && lovVo.getId() != null) {
							response.setData(lovVo);
							LOGGER.debug("Lov with id {} updated successfully", id);
							return new ResponseEntity<>(response, HttpStatus.OK);
						} else {
							LOGGER.debug("Lov with id {} cannot be edited. Failed with unknown internal error", id);
							return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
						}

					}
					lovVo.setId(((BigInteger) existingEntity[0]).longValue());
					lovVo.setName(existingEntity[1].toString());
					response.setData(lovVo);
					return new ResponseEntity<>(response, HttpStatus.CONFLICT);
				} else {
					LOGGER.debug("No lov found for given id {} , update cannot happen.", id);
					return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
				}
			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to update lov. Only user with admin role can update.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				response.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Lov cannot be edited. User not authorized");
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			LOGGER.error("Lov cannot be edited. Failed due to internal error {} ", e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteLov(ID id, CATEGORY category, T entity) {
		try {
			if (verifyUserRoles()) {
				Object[] existingEntity = lovCommonDataRepository.findLovById((long) id,
						entity.getClass().getAnnotation(Table.class).name());
				if (existingEntity != null) {
					reportService.deleteForEachReport(existingEntity[1].toString(), category);
					jpaRepo.deleteById(id);
				}
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("Lov with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);

			} else {
				List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage("Not authorized to delete lov. Only user with admin role can delete.");
				notAuthorizedMsgs.add(notAuthorizedMsg);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.setErrors(notAuthorizedMsgs);
				LOGGER.debug("Lov with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to delete lov with id {} , due to internal error.", id);
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public boolean verifyUserRoles() {
		Boolean isAdmin = false;
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		if (StringUtils.hasText(userId)) {
			isAdmin = this.userStore.getUserInfo().hasAdminAccess();
		}
		return isAdmin;
	}
	
	@Override
	public String currentUserName(CreatedByVO currentUser) {
		String userName = "";
		if (Objects.nonNull(currentUser)) {
			if (StringUtils.hasText(currentUser.getFirstName())) {
				userName = currentUser.getFirstName();
			}
			if (StringUtils.hasText(currentUser.getLastName())) {
				userName += " " + currentUser.getLastName();
			}
		}
		if (!StringUtils.hasText(userName)) {
			userName = currentUser != null ? currentUser.getId() : "dna_system";
		}
		return userName;
	}
	
	@Override
	public void notifyAllAdminUsers(String eventType, String resourceId, String message, String triggeringUser,
			List<ChangeLogVO> changeLogs) {
		LOGGER.debug("Notifying all Admin users on " + eventType + " for " + message);
		List<UserInfoVO> allUsers = null;
		UsersCollection usersCollection = dnaAuthClient.getAll();
		if (usersCollection != null && !ObjectUtils.isEmpty(usersCollection.getRecords())) {
			allUsers = usersCollection.getRecords();
			List<String> adminUsersIds = new ArrayList<>();
			List<String> adminUsersEmails = new ArrayList<>();
			for (UserInfoVO user : allUsers) {
				boolean isAdmin = false;
				if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getRoles())) {
					isAdmin = user.getRoles().stream().anyMatch(role -> "Admin".equalsIgnoreCase(role.getName())
							|| "ReportAdmin".equalsIgnoreCase(role.getName()));
				}
				if (isAdmin) {
					adminUsersIds.add(user.getId());
					adminUsersEmails.add(user.getEmail());
				}
			}
			try {
				kafkaProducer.send(eventType, resourceId, "", triggeringUser, message, true, adminUsersIds,
						adminUsersEmails, changeLogs);
				LOGGER.info("Successfully notified all admin users for event {} for {} ", eventType, message);
			} catch (Exception e) {
				LOGGER.error(
						"Exception occured while notifying all Admin users on {}  for {} . Failed with exception {}",
						eventType, message, e.getMessage());
			}
		}
	}

}
