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

package com.daimler.dna.airflow.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowRoleVO;
import com.daimler.dna.airflow.dto.AirflowUserVO;
import com.daimler.dna.airflow.models.CollabInfo;
import com.daimler.dna.airflow.models.Role;
import com.daimler.dna.airflow.models.User;
import com.daimler.dna.airflow.models.UserRoleMapping;

@Component
public class UserAssembler {

	public AirflowUserVO toVo(User user) {
		AirflowUserVO vo = null;
		if (Objects.nonNull(user)) {
			vo = new AirflowUserVO();
			BeanUtils.copyProperties(user, vo);
			for (UserRoleMapping mapping : user.getRoleMaping()) {
				AirflowRoleVO rolesItem = new AirflowRoleVO();
				BeanUtils.copyProperties(mapping.getRole(), rolesItem);
				vo.addRolesItem(rolesItem);
			}
		}
		return vo;
	}

	public User toEntity(AirflowUserVO vo) {
		User user = null;
		if (Objects.nonNull(vo)) {
			user = new User();
			BeanUtils.copyProperties(vo, user);
			List<UserRoleMapping> roleMappings = new ArrayList<UserRoleMapping>();
			for (AirflowRoleVO roleVo : vo.getRoles()) {
				UserRoleMapping roleMapping = new UserRoleMapping();
				roleMapping.setUser(user);
				Role role = new Role();
				BeanUtils.copyProperties(roleVo, role);
				roleMapping.setRole(role);
				roleMappings.add(roleMapping);
			}
			user.setRoleMaping(roleMappings);
			user.setActive(true);
		}
		return user;
	}
	
	public User toEntity(CollabInfo vo) {
		User user = null;
		if (Objects.nonNull(vo)) {
			user = new User();
			BeanUtils.copyProperties(vo, user);
			user.setActive(true);
		}
		return user;
	}

	public User toEntity(AirflowProjectUserVO vo) {
		User user = null;
		if (Objects.nonNull(vo)) {
			user = new User();
			BeanUtils.copyProperties(vo, user);
			user.setActive(true);
		}
		return user;
	}
}
