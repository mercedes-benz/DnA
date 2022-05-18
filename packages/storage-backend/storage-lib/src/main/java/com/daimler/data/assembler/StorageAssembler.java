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

import com.daimler.data.db.entities.StorageNsql;
import com.daimler.data.db.jsonb.Permission;
import com.daimler.data.db.jsonb.Storage;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.dto.storage.BucketVo;
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
				
			}
		}
		return bucketVo;
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
			if (!StringUtils.hasText(id)) {
				id = UUID.randomUUID().toString();
			}
			// Setting id in entity
			entity.setId(id);

			Storage storage = new Storage();
			BeanUtils.copyProperties(vo, storage);

			storage.setPIIData(vo.isPIIData() != null ? vo.isPIIData() : Boolean.FALSE);
			storage.setTermsOfUse(vo.isTermsOfUse() != null ? vo.isTermsOfUse() : Boolean.FALSE);
			storage.setClassificationType(
					StringUtils.hasText(vo.getClassificationType()) ? vo.getClassificationType() : "Internal");

			// created by
			if (vo.getCreatedBy() != null) {
				UserInfo userDetails = new UserInfo();
				userDetails.setId(vo.getCreatedBy().getId());
				userDetails.setFirstName(vo.getCreatedBy().getFirstName());
				userDetails.setLastName(vo.getCreatedBy().getLastName());
				userDetails.setEmail(vo.getCreatedBy().getEmail());
				userDetails.setDepartment(vo.getCreatedBy().getDepartment());
				userDetails.setMobileNumber(vo.getCreatedBy().getMobileNumber());
				storage.setCreatedBy(userDetails);
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

	private UserInfo toUserInfoJson(UserVO userVO) {
		UserInfo userInfo = new UserInfo();
		BeanUtils.copyProperties(userVO, userInfo);
		userInfo.setId(userVO.getAccesskey());
		if (Objects.nonNull(userVO.getPermission())) {
			Permission permission = new Permission();
			BeanUtils.copyProperties(userVO.getPermission(), permission);
			userInfo.setPermission(permission);
		}

		return userInfo;
	}
	
	private UserVO toUserInfoVO(UserInfo userInfo) {
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(userInfo, userVO);
		if (Objects.nonNull(userVO.getPermission())) {
			
			
			Permission permission = new Permission();
			BeanUtils.copyProperties(userVO.getPermission(), permission);
			userInfo.setPermission(permission);
		}

		return userVO;
	}

}
