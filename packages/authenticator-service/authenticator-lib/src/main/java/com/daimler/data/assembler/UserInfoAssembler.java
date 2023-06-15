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

package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.daimler.data.controller.LoginController;
import com.daimler.data.controller.LoginController.UserRole;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserFavoriteUseCase;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.db.jsonb.UserInfoRole;
import com.daimler.data.dto.userinfo.UserFavoriteUseCaseVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;

@Component
public class UserInfoAssembler implements GenericAssembler<UserInfoVO, UserInfoNsql> {

	
	@Override
	public UserInfoVO toVo(UserInfoNsql entity) {
		UserInfoVO userInfoVO = null;
		if (entity != null) {
			userInfoVO = new UserInfoVO();
			userInfoVO.setId(entity.getId());
			/* userInfoVO.setToken(entity.getToken()); */
			UserInfo userData = entity.getData();
			if (userData != null) {
				userInfoVO.setDepartment(userData.getDepartment());
				userInfoVO.setEmail(userData.getEmail());
				userInfoVO.setFirstName(userData.getFirstName());
				userInfoVO.setLastName(userData.getLastName());
				userInfoVO.setMobileNumber(userData.getMobileNumber());

				List<UserFavoriteUseCase> favoriteUsecases = userData.getFavoriteUsecases();
				if (favoriteUsecases != null && !favoriteUsecases.isEmpty()) {
					List<UserFavoriteUseCaseVO> favoriteUsecaseVOList = new ArrayList<>();
					favoriteUsecases.forEach(favUseCase -> {
						UserFavoriteUseCaseVO userFavoriteUsecaseVO = new UserFavoriteUseCaseVO();
						userFavoriteUsecaseVO.setId(favUseCase.getId());
						userFavoriteUsecaseVO.setUsecaseId(favUseCase.getUsecaseId());
						favoriteUsecaseVOList.add(userFavoriteUsecaseVO);
					});
					userInfoVO.setFavoriteUsecases(favoriteUsecaseVOList);
				}

				List<UserInfoRole> roles = userData.getRoles();
				if (roles != null && !roles.isEmpty()) {
					List<UserRoleVO> rolesVO = new ArrayList<>();
					rolesVO = roles.stream().map(x -> {
						UserRoleVO roleVO = new UserRoleVO();
						roleVO.setId(x.getId());
						roleVO.setName(x.getName());
						return roleVO;
					}).collect(Collectors.toList());
					userInfoVO.setRoles(rolesVO);
				}
				
				//To set divisionAdmins
				userInfoVO.setDivisionAdmins(userData.getDivisionAdmins());
			}
		}
		return userInfoVO;
	}

	public UserInfoNsql toEntity(LoginController.UserInfo userInfo, List<UserInfoRole> userRoles) {
		UserInfoNsql entity = new UserInfoNsql();
		entity.setId(userInfo.getId());
		UserInfo userData = new UserInfo();
		userData.setEmail(userInfo.getEmail());
		userData.setDepartment(userInfo.getDepartment());
		userData.setFirstName(userInfo.getFirstName());
		userData.setLastName(userInfo.getLastName());
		userData.setMobileNumber(userInfo.getMobileNumber());
		userData.setFavoriteUsecases(new ArrayList<>());
		userData.setRoles(userRoles);
		entity.setData(userData);
		return entity;
	}

	@Override
	public UserInfoNsql toEntity(UserInfoVO vo) {

		UserInfoNsql entity = new UserInfoNsql();
		if (vo != null) {
			String id = vo.getId();
			if (!StringUtils.isEmpty(id)) {
				entity.setId(id);
			}
		}
		/* entity.setToken(vo.getToken()); */
		UserInfo jsonData = new UserInfo();
		BeanUtils.copyProperties(vo, jsonData);

		List<UserInfoRole> jsonRoles = new ArrayList<>();
		vo.getRoles().stream().forEach(userRoleVO -> {
			UserInfoRole jsonRole = new UserInfoRole(userRoleVO.getId(), userRoleVO.getName());
			jsonRoles.add(jsonRole);
		});
		jsonData.setRoles(jsonRoles);

		if (vo.getFavoriteUsecases() != null) {
			List<UserFavoriteUseCase> jsonFavUsecases = new ArrayList<>();
			vo.getFavoriteUsecases().stream().forEach(usecaseVO -> {
				UserFavoriteUseCase jsonFavUsecase = new UserFavoriteUseCase();
				jsonFavUsecase.setUsecaseId(usecaseVO.getUsecaseId());
				jsonFavUsecases.add(jsonFavUsecase);
			});
			jsonData.setFavoriteUsecases(jsonFavUsecases);
		}
		entity.setData(jsonData);
		return entity;
	}

	public List<UserRole> toUserRoles(List<UserRoleVO> rolesVO) {
		List<UserRole> userRoles = new ArrayList<>();
		if (rolesVO != null && !rolesVO.isEmpty()) {
			userRoles = rolesVO.stream().map(x -> {
				UserRole userRole = new UserRole();
				BeanUtils.copyProperties(x, userRole);
				// userRole.setId(x.getId());
				// userRole.setName(x.getName());
				return userRole;
			}).collect(Collectors.toList());
		}
		return userRoles;
	}
	
	/*
	 * To set existing data if key user data is missing in new request payload
	 */
	public void setCurrentUserData(UserInfoVO currentUserData, UserInfoVO newUserData) {
		if (currentUserData != null) {
			newUserData.setDepartment(StringUtils.hasText(newUserData.getDepartment()) ? newUserData.getDepartment()
					: currentUserData.getDepartment());
			newUserData.setEmail(
					StringUtils.hasText(newUserData.getEmail()) ? newUserData.getEmail() : currentUserData.getEmail());
			newUserData
					.setMobileNumber(StringUtils.hasText(newUserData.getMobileNumber()) ? newUserData.getMobileNumber()
							: currentUserData.getMobileNumber());
			newUserData.setFirstName(StringUtils.hasText(newUserData.getFirstName()) ? newUserData.getFirstName()
					: currentUserData.getFirstName());
			newUserData.setLastName(StringUtils.hasText(newUserData.getLastName()) ? newUserData.getLastName()
					: currentUserData.getLastName());
		}
	}

}
