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

package com.daimler.data.service.userinfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserFavoriteUseCase;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.db.jsonb.UserInfoRole;
import com.daimler.data.db.repo.userinfo.UserInfoCustomRepository;
import com.daimler.data.db.repo.userinfo.UserInfoRepository;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.userinfo.UserFavoriteUseCaseVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.util.JWTGenerator;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BaseUserInfoService extends BaseCommonService<UserInfoVO, UserInfoNsql, String>
		implements UserInfoService {

	private static Logger logger = LoggerFactory.getLogger(BaseUserInfoService.class);
	
	@Autowired
	private UserInfoCustomRepository customRepo;
	@Autowired
	private UserInfoRepository jpaRepo;

	@Autowired
	private SolutionService solutionService;

	@Autowired
	private UserInfoAssembler userinfoAssembler;

	@Autowired
	private KafkaProducerService kafkaProducer;

	
	public BaseUserInfoService() {
		super();
	}

	/*
	 * @Override public boolean updateUserToken(final String id, String token) {
	 * UserInfoNsql userinfo = jpaRepo.findById(id).get(); String currentToken =
	 * userinfo.getToken(); if (!StringUtils.isEmpty(token)) { StringBuilder tokens
	 * = new StringBuilder(currentToken == null ? "" : currentToken);
	 * tokens.append(token).append("#"); userinfo.setToken(tokens.toString()); }
	 * else { userinfo.setToken(token); } customRepo.update(userinfo); return true;
	 * 
	 * }
	 */

	@Override
	public boolean updateNewUserToken(final String id, boolean isLogin) {
		UserInfoNsql userinfo = jpaRepo.findById(id).get();
		if (isLogin) {
			userinfo.setIsLoggedIn("Y");
		} else {
			userinfo.setIsLoggedIn("N");
		}
		customRepo.update(userinfo);
		return true;

	}

	@Override
	public UserInfoVO updateBookMarkedSolutions(final String id, List<String> bookmarks, boolean deleteBookmarks) {
		log.debug("updating bookmarks {} with delete flag as ", bookmarks, deleteBookmarks);
		UserInfoNsql userInfoEntityExisting = jpaRepo.findById(id).get();
		UserInfoVO userInfoVO = userinfoAssembler.toVo(userInfoEntityExisting);
		for (String bookmark : bookmarks) {
			if (!deleteBookmarks) {
				if (solutionService.getById(bookmark) != null) {
					boolean existingFav = false;
					if (userInfoVO.getFavoriteUsecases() != null) {
						for (UserFavoriteUseCaseVO favUseCase : userInfoVO.getFavoriteUsecases()) {
							if (favUseCase.getUsecaseId().equals(bookmark)) {
								existingFav = true;
								break;
							}
						}
					}
					if (!existingFav) {
						UserFavoriteUseCaseVO newFavorite = new UserFavoriteUseCaseVO();
						newFavorite.setUsecaseId(bookmark);
						userInfoVO.addFavoriteUsecasesItem(newFavorite);
					}
				}
			} else {
				if (userInfoVO.getFavoriteUsecases() != null) {
					Iterator<UserFavoriteUseCaseVO> favUsecases = userInfoVO.getFavoriteUsecases().iterator();
					while (favUsecases.hasNext()) {
						UserFavoriteUseCaseVO favUsecase = favUsecases.next();
						if (bookmark.equals(favUsecase.getUsecaseId())) {
							favUsecases.remove();
						}
					}
				}
			}
		}

		UserInfoNsql userInfoEntity = userinfoAssembler.toEntity(userInfoVO);
		userInfoEntity.setIsLoggedIn(userInfoEntityExisting.getIsLoggedIn());
		customRepo.update(userInfoEntity);
		return userInfoVO;

	}

	@Override
	public List<SolutionVO> getAllBookMarkedSolutionsForUser(final String userId) {
		UserInfoNsql userinfo = jpaRepo.findById(userId).get();
		List<UserFavoriteUseCase> favoriteUseCases = userinfo.getData().getFavoriteUsecases();
		if (favoriteUseCases != null && favoriteUseCases.size() > 0) {
			List<SolutionVO> solutionVOList = new ArrayList<>();
			favoriteUseCases.forEach(fav -> {
				SolutionVO favSolution = solutionService.getById(fav.getUsecaseId());
				solutionVOList.add(favSolution);
			});
			return solutionVOList;
		} else {
			return null;
		}
	}

	@Override
	public boolean validateUserToken(final String id, String token) {
		UserInfoNsql userinfo = jpaRepo.findById(id).get();
		if (userinfo != null && userinfo.getIsLoggedIn().equalsIgnoreCase("Y")) {
			// Validate Claimed roles
			log.debug("Validating claimed roles for user {} and token {}", id, token);
			Claims claims = JWTGenerator.decodeJWT(token);
			List<LinkedHashMap<String, String>> claimedRoles = (List<LinkedHashMap<String, String>>) claims
					.get("digiRole");
			if (claimedRoles != null && !claimedRoles.isEmpty()) {
				List<UserInfoRole> userRoles = userinfo.getData().getRoles();
				if (userRoles != null && !userRoles.isEmpty()) {
					if (userRoles.size() != claimedRoles.size()) {
						// Claimed roles dont match with User Roles
						return false;
					}
					for (LinkedHashMap<String, String> roleClaimed : claimedRoles) {
						boolean roleFound = false;
						for (UserInfoRole userRole : userRoles) {
							if (userRole.getId().equals(roleClaimed.get("id"))) {
								roleFound = true;
								break;
							}
						}
						if (!roleFound) {
							return false;
						}
					}
				} else {
					// Claimed roles dont match with User Roles
					return false;
				}
			}
			log.debug("Roles verified for user checking token validity for user {} and token {}", id, token);
			// Validate Token
			/*
			 * String[] tokens = userinfo.getToken().split("#"); if (tokens != null) { for
			 * (String tkn : tokens) { if (tkn.equals(token)) { return true; } } }
			 */
		} else {
			return false;
		}
		return true;
	}

	public void addUser(UserInfoNsql userinfo) {
		jpaRepo.save(userinfo);
	}

	/**
	 * To check whether user is admin or not
	 * 
	 * @param userId
	 * @return isAdmin
	 */
	@Override
	public Boolean isAdmin(String userId) {
		Boolean isAdmin = false;
		if (StringUtils.hasText(userId)) {
			UserInfoVO userInfoVO = super.getById(userId);
			if (!ObjectUtils.isEmpty(userInfoVO) && !ObjectUtils.isEmpty(userInfoVO.getRoles())) {
				isAdmin = userInfoVO.getRoles().stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
			}
		}
		return isAdmin;
	}

	@Override
	public void notifyAllAdminUsers(String eventType, String resourceId, String message, String triggeringUser,
			List<ChangeLogVO> changeLogs) {
		log.debug("Notifying all Admin users on " + eventType + " for " + message);
		List<UserInfoVO> allUsers = this.getAll();
		List<String> adminUsersIds = new ArrayList<>();
		List<String> adminUsersEmails = new ArrayList<>();
		for (UserInfoVO user : allUsers) {
			boolean isAdmin = false;
			if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getRoles())) {
				isAdmin = user.getRoles().stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
			}
			if (isAdmin) {
				adminUsersIds.add(user.getId());
				adminUsersEmails.add(user.getEmail());
			}
		}
		try {
			kafkaProducer.send(eventType, resourceId, "", triggeringUser, message, true, adminUsersIds,
					adminUsersEmails, changeLogs);
			log.info("Successfully notified all admin users for event {} for {} ", eventType, message);
		} catch (Exception e) {
			log.error("Exception occurred while notifying all Admin users on {}  for {} . Failed with exception {}",
					eventType, message, e.getMessage());
		}
	}

	@Override
	public boolean isLoggedIn(String id) {
		UserInfoNsql userinfo = jpaRepo.findById(id).get();
		return !ObjectUtils.isEmpty(userinfo) && userinfo.getIsLoggedIn().equalsIgnoreCase("Y");
	}

	@Override
	public List<UserInfoVO> getAllWithFilters(String searchTerm, int limit, int offset, String sortBy,
			String sortOrder) {
		logger.info("Fetching user information from table.");
		List<UserInfoNsql> userInfoEntities = customRepo.getAllWithFilters(searchTerm, limit, offset, sortBy,
				sortOrder);
		logger.info("Success from get information from table.");
		if (!ObjectUtils.isEmpty(userInfoEntities)) {
			return userInfoEntities.stream().map(n -> userinfoAssembler.toVo(n)).toList();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Long getCountWithFilters(String searchTerm) {
		logger.info("Fetching total count of user information.");
		return customRepo.getCount(searchTerm);		
	}

	@Override
	@Transactional
	public void updateDivisionForUserRole(String divisionOldValue, String divisionNewValue) {
		List<UserInfoNsql> userInfoEntities = customRepo.getAllWithFilters(divisionOldValue, 0, 0, null, null);
		Optional.ofNullable(userInfoEntities).ifPresent(l -> l.forEach(userInfoNsql -> {
			if (!ObjectUtils.isEmpty(userInfoNsql.getData().getDivisionAdmins())
					&& userInfoNsql.getData().getDivisionAdmins().contains(divisionOldValue)) {
				int index = userInfoNsql.getData().getDivisionAdmins().indexOf(divisionOldValue);
				if (StringUtils.hasText(divisionNewValue)) {
					logger.info("Setting new division value:{} for division:{}", divisionNewValue, divisionOldValue);
					userInfoNsql.getData().getDivisionAdmins().set(index, divisionNewValue);
				} else {
					logger.info("Removing division:{}", divisionOldValue);
					userInfoNsql.getData().getDivisionAdmins().remove(index);
				}
				logger.debug("Saving pdtaed userInfo in database");
				jpaRepo.save(userInfoNsql);
			}
		}));
	}

	@Override
	public Integer getNumberOfUsers() {
		return customRepo.getNumberOfUsers();
	}

	@Override
	public UserInfoVO getById(String id) {
		Optional<UserInfoNsql> userInfo = customRepo.findById(id);
		return userInfo.isPresent() ? userinfoAssembler.toVo(userInfo.get()) : null;
	}

}
