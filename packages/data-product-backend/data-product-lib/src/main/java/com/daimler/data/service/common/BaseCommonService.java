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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.GenericAssembler;
import com.daimler.data.auth.client.DnaAuthClient;
import com.daimler.data.db.repo.common.CommonDataRepository;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.datatransfer.ChangeLogVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UsersCollection;
import com.daimler.data.notifications.common.producer.KafkaProducerService;

public class BaseCommonService<V, T, ID> implements CommonService<V, T, ID> {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseCommonService.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private DnaAuthClient dnaAuthClient;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private CommonDataRepository<T, ID> customRepo;
	@Autowired
	private JpaRepository<T, ID> jpaRepo;
	@Autowired
	private GenericAssembler<V, T> assembler;

	@Autowired
	private PagingAndSortingRepository<T, ID> sortingAndPagingRepo;

	public BaseCommonService() {
		super();
	}

	public BaseCommonService(JpaRepository<T, ID> jpaRepo, GenericAssembler<V, T> assembler,
							 CommonDataRepository<T, ID> customRepo) {
		super();
		this.jpaRepo = jpaRepo;
		this.assembler = assembler;
		this.customRepo = customRepo;
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
	public V updateByID(V vo) {
		T entity = assembler.toEntity(vo);
		T savedEntity = jpaRepo.save(entity);
		return assembler.toVo(savedEntity);
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

		if (!StringUtils.hasText(uniqueLiteral)) {
			uniqueLiteral = "id";
		}
		List<T> entities = customRepo.findAllSortyByUniqueLiteral(limit, offset, uniqueLiteral, sortOrder);
		if (!ObjectUtils.isEmpty(entities)) {
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		}
		return null;
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
	public boolean deleteById(ID id) {
		Optional<T> entity = jpaRepo.findById(id);
		boolean flag = entity.isPresent();
		if (flag) {
			jpaRepo.deleteById(id);
		}
		return flag;
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
	public Long getCount() {
		return jpaRepo.count();
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
	public boolean verifyUserRoles() {
		return this.userStore.getUserInfo().hasAdminAccess();
	}

	@Override
	public void notifyAllAdminUsers(String eventType, String resourceId, String message, String triggeringUser,
			List<ChangeLogVO> changeLogs) {
		LOGGER.debug("Notifying all Admin users on " + eventType + " for " + message);
		List<UserInfoVO> allUsers = null;
		UsersCollection usersCollection = dnaAuthClient.getAllUsers();
		if (usersCollection != null && !ObjectUtils.isEmpty(usersCollection.getRecords())) {
			allUsers = usersCollection.getRecords();
			List<String> adminUsersIds = new ArrayList<>();
			List<String> adminUsersEmails = new ArrayList<>();
			for (UserInfoVO user : allUsers) {
				boolean isAdmin = false;
				if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getRoles())) {
					isAdmin = user.getRoles().stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName())
							|| "DataComplianceAdmin".equalsIgnoreCase(role.getName()));
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
