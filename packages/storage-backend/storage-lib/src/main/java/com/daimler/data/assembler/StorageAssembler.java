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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.db.entities.StorageNsql;
import com.daimler.data.db.jsonb.Permission;
import com.daimler.data.db.jsonb.Storage;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.dto.storage.BucketVo;
import com.daimler.data.dto.storage.CreatedByVO;
import com.daimler.data.dto.storage.PermissionVO;
import com.daimler.data.dto.storage.UserVO;

@Component
public class StorageAssembler {

	/**
	 * To convert StorageNsql to BucketVo
	 * 
	 * @param entity the StorageNsql to convert in VO
	 * @return {@code BucketVo} if the object is not {@code null} or <em>empty</em>
	 */
	public BucketVo toBucketVo(StorageNsql entity) {
		BucketVo bucketVo = null;
		if (Objects.nonNull(entity)) {
			bucketVo = new BucketVo();
			bucketVo.setId(entity.getId());
			Storage storage = entity.getData();
			if (Objects.nonNull(storage)) {
				BeanUtils.copyProperties(entity.getData(), bucketVo);
				// setting collaborators
				if (!ObjectUtils.isEmpty(storage.getCollaborators())) {
					storage.getCollaborators();
					List<UserVO> collaborators = storage.getCollaborators().stream().filter(Objects::nonNull)
							.map(this::toUserVO).toList();
					bucketVo.setCollaborators(collaborators);
				}
				// setting createdBy
				if (Objects.nonNull(storage.getCreatedBy())) {
					CreatedByVO createdByVO = new CreatedByVO();
					BeanUtils.copyProperties(storage.getCreatedBy(), createdByVO);
					bucketVo.setCreatedBy(createdByVO);
				}
				// Setting updatedBy
				if (Objects.nonNull(storage.getUpdatedBy())) {
					CreatedByVO updatedByVO = new CreatedByVO();
					BeanUtils.copyProperties(storage.getUpdatedBy(), updatedByVO);
					bucketVo.setUpdatedBy(updatedByVO);
				}
			}
			
		}
		return bucketVo;
	}

	/**
	 * To form BucketVo
	 * 
	 * @param entities {@code List<StorageNsql>}
	 * @param bucketName
	 * @return bucket view object {@code BucketVo}
	 */
	public BucketVo toBucketVo(List<StorageNsql> entities, String bucketName) {
		BucketVo bucketVo = null;
		List<StorageNsql> filteredEntities = entities.stream()
				.filter(item -> item.getData().getBucketName().equals(bucketName)).toList();
		if (!ObjectUtils.isEmpty(filteredEntities)) {
			bucketVo = toBucketVo(filteredEntities.get(0));
		}
		return bucketVo;
	}
	
	/*
	 * To convert UserInfo entity to UserVO
	 * 
	 */
	private UserVO toUserVO(UserInfo userInfo) {
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(userInfo, userVO);
		userVO.setAccesskey(userInfo.getId());
		if (Objects.nonNull(userInfo.getPermission())) {
			PermissionVO permissionVO = new PermissionVO();
			permissionVO.setRead(userInfo.getPermission().isRead());
			permissionVO.setWrite(userInfo.getPermission().isWrite());
			userVO.setPermission(permissionVO);
		}
		return userVO;
	}
	
	/*
	 * To convert CreatedByVO to UserVO
	 * 
	 */
	public UserVO toUserVO(CreatedByVO createdByVO) {
		UserVO userVO = null;
		if (Objects.nonNull(createdByVO)) {
			userVO = new UserVO();
			BeanUtils.copyProperties(createdByVO, userVO);
			userVO.setAccesskey(createdByVO.getId());
			PermissionVO permissionVO = new PermissionVO();
			permissionVO.setRead(true);
			permissionVO.setWrite(true);
			userVO.setPermission(permissionVO);
		}
		return userVO;
	}
	
	/**
	 * To convert BucketVo to StorageNsql
	 * 
	 * @param VO {@code BucketVo} to be converted
	 * @return entity {@code StorageNsql}
	 */
	public StorageNsql toEntity(BucketVo vo) {
		StorageNsql entity = new StorageNsql();
		if (Objects.nonNull(vo)) {
			String id = vo.getId();
			// if (!StringUtils.hasText(id)) {
			// 	id = UUID.randomUUID().toString();
			// }
			// Setting id in entity
			entity.setId(id);

			Storage storage = new Storage();
			BeanUtils.copyProperties(vo, storage);

			storage.setPiiData(vo.isPiiData() != null ? vo.isPiiData() : Boolean.FALSE);
			storage.setTermsOfUse(vo.isTermsOfUse() != null ? vo.isTermsOfUse() : Boolean.FALSE);
			storage.setClassificationType(
					StringUtils.hasText(vo.getClassificationType()) ? vo.getClassificationType() : "Internal");
			storage.setEnablePublicAccess(vo.isEnablePublicAccess());
			// created by
			if (Objects.nonNull(vo.getCreatedBy())) {
				UserInfo userDetails = new UserInfo();
				BeanUtils.copyProperties(vo.getCreatedBy(), userDetails);
				storage.setCreatedBy(userDetails);
			}
			//Updated by
			if(Objects.nonNull(vo.getUpdatedBy())) {
				UserInfo userDetails = new UserInfo();
				BeanUtils.copyProperties(vo.getUpdatedBy(), userDetails);
				storage.setUpdatedBy(userDetails);
			}
			
			// Collaborators
			if (!ObjectUtils.isEmpty(vo.getCollaborators())) {
				List<UserInfo> collaborators = vo.getCollaborators().stream().filter(Objects::nonNull)
						.map(this::toUserInfoJson).toList();
				storage.setCollaborators(collaborators);
			}
			entity.setData(storage);
		}

		return entity;
	}

	/*
	 * to convert UserVO to UserInfo json
	 * 
	 */
	private UserInfo toUserInfoJson(UserVO userVO) {
		UserInfo userInfo = new UserInfo();
		BeanUtils.copyProperties(userVO, userInfo);
		userInfo.setId(userVO.getAccesskey());
		if (Objects.nonNull(userVO.getPermission())) {
			Permission permission = new Permission();
			permission.setRead(userVO.getPermission().isRead());
			permission.setWrite(userVO.getPermission().isWrite());
			userInfo.setPermission(permission);
		}
		return userInfo;
	}

	public UserInfo setUpdatedBy(UserStore userStore) {
		UserInfo userInfo = null;
		if (Objects.nonNull(userStore.getUserInfo())) {
			userInfo = new UserInfo();
			BeanUtils.copyProperties(userStore.getUserInfo(), userInfo);
		}
		return userInfo;
	}
	
}
