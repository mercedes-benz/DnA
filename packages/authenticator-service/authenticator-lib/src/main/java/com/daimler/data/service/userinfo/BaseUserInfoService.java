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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserInfoRole;
import com.daimler.data.db.repo.userinfo.UserInfoCustomRepository;
import com.daimler.data.db.repo.userinfo.UserInfoRepository;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.JWTGenerator;

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
	private UserInfoAssembler userinfoAssembler;

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
	public boolean isLoggedIn(String id) {
		UserInfoNsql userinfo = jpaRepo.findById(id).get();
		return !ObjectUtils.isEmpty(userinfo) && userinfo.getIsLoggedIn().equalsIgnoreCase("Y");
	}

	@Override
	public UserInfoVO getById(String id) {
		Optional<UserInfoNsql> userInfo = customRepo.findById(id);
		return userInfo.isPresent() ? userinfoAssembler.toVo(userInfo.get()) : null;
	}

}
