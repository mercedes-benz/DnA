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

package com.daimler.dna.airflow.service;

/*
import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserFavoriteUseCase;
import com.daimler.data.db.jsonb.UserInfoRole;
import com.daimler.data.db.repo.userinfo.UserInfoCustomRepository;
import com.daimler.data.db.repo.userinfo.UserInfoRepository;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.userinfo.UserFavoriteUseCaseVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.util.JWTGenerator;*/
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Transactional
@Slf4j
public class BaseUserInfoService
		/* extends BaseCommonService<UserInfoVO, UserInfoNsql, String> */
		implements UserInfoService {

	/*
	 * @Autowired private UserInfoCustomRepository customRepo;
	 * 
	 * @Autowired private UserInfoRepository jpaRepo;
	 * 
	 * @Autowired private SolutionService solutionService;
	 * 
	 * @Autowired private UserInfoAssembler userinfoAssembler;
	 */

	public BaseUserInfoService() {
		super();
	}

	@Override
	public boolean updateUserToken(final String id, String token) {
		/*
		 * UserInfoNsql userinfo = jpaRepo.findById(id).get(); String currentToken =
		 * userinfo.getToken(); if (!StringUtils.isEmpty(token)) { StringBuilder tokens
		 * = new StringBuilder(currentToken == null ? "" : currentToken);
		 * tokens.append(token).append("#"); userinfo.setToken(tokens.toString()); }
		 * else { userinfo.setToken(token); } customRepo.update(userinfo);
		 */
		return true;

	}

	/*
	 * @Override public UserInfoVO updateBookMarkedSolutions(final String id,
	 * List<String> bookmarks, boolean deleteBookmarks) { UserInfoNsql
	 * userInfoEntity = jpaRepo.findById(id).get(); UserInfoVO userInfoVO =
	 * userinfoAssembler.toVo(userInfoEntity); for (String bookmark : bookmarks) {
	 * if (!deleteBookmarks) { if (solutionService.getById(bookmark) != null) {
	 * boolean existingFav = false; if (userInfoVO.getFavoriteUsecases() != null) {
	 * for (UserFavoriteUseCaseVO favUseCase : userInfoVO.getFavoriteUsecases()) {
	 * if (favUseCase.getUsecaseId().equals(bookmark)) { existingFav = true; break;
	 * } } } if (!existingFav) { UserFavoriteUseCaseVO newFavorite = new
	 * UserFavoriteUseCaseVO(); newFavorite.setUsecaseId(bookmark);
	 * userInfoVO.addFavoriteUsecasesItem(newFavorite); } } } else { if
	 * (userInfoVO.getFavoriteUsecases() != null) { Iterator<UserFavoriteUseCaseVO>
	 * favUsecases = userInfoVO.getFavoriteUsecases().iterator(); while
	 * (favUsecases.hasNext()) { UserFavoriteUseCaseVO favUsecase =
	 * favUsecases.next(); if (bookmark.equals(favUsecase.getUsecaseId())) {
	 * favUsecases.remove(); } } } } }
	 * 
	 * userInfoEntity = userinfoAssembler.toEntity(userInfoVO);
	 * customRepo.update(userInfoEntity); return userInfoVO;
	 * 
	 * }
	 */

	/*
	 * @Override public List<SolutionVO> getAllBookMarkedSolutionsForUser(final
	 * String userId) { UserInfoNsql userinfo = jpaRepo.findById(userId).get();
	 * List<UserFavoriteUseCase> favoriteUseCases =
	 * userinfo.getData().getFavoriteUsecases(); if (favoriteUseCases != null &&
	 * favoriteUseCases.size() > 0) { List<SolutionVO> solutionVOList = new
	 * ArrayList<>(); favoriteUseCases.forEach(fav -> { SolutionVO favSolution =
	 * solutionService.getById(fav.getUsecaseId()); solutionVOList.add(favSolution);
	 * }); return solutionVOList; } else { return null; } }
	 */

	@Override
	public boolean validateUserToken(final String id, String token) {
		/* UserInfoNsql userinfo = jpaRepo.findById(id).get(); */
		if (token != null && !StringUtils.isEmpty(token)) {
			// Validate Claimed roles
			log.trace("Validating claimed roles for user:" + id + ", token:" + token);
			//Claims claims = JWTGenerator.decodeJWT(token);
			/*
			 * List<LinkedHashMap<String, String>> claimedRoles =
			 * (List<LinkedHashMap<String, String>>) claims.get("digiRole"); if
			 * (claimedRoles != null && !claimedRoles.isEmpty()) { List<UserInfoRole>
			 * userRoles = userinfo.getData().getRoles(); if (userRoles != null &&
			 * !userRoles.isEmpty()) { if (userRoles.size() != claimedRoles.size()) {
			 * //Claimed roles dont match with User Roles return false; } for
			 * (LinkedHashMap<String, String> roleClaimed : claimedRoles) { boolean
			 * roleFound = false; for (UserInfoRole userRole : userRoles) { if
			 * (userRole.getId().equals(roleClaimed.get("id"))) { roleFound = true; break; }
			 * } if (!roleFound) { return false; } } } else { //Claimed roles dont match
			 * with User Roles return false; } }
			 */
			log.trace("Roles verified for user checking token validity for user:" + id + ", token:" + token);
			// Validate Token
			/*
			 * String[] tokens = userinfo.getToken().split("#"); if (tokens != null) { for
			 * (String tkn : tokens) { if (tkn.equals(token)) { return true; } } }
			 */
		}
		return true;
	}

	/*
	 * public void addUser(UserInfoNsql userinfo) { jpaRepo.save(userinfo); }
	 */
}
