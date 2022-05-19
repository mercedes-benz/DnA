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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.dna.airflow.dto.AirflowPermissionVO;
import com.daimler.dna.airflow.dto.AirflowRoleVO;
import com.daimler.dna.airflow.dto.ViewMenuVO;
import com.daimler.dna.airflow.models.Permission;
import com.daimler.dna.airflow.models.PermissionAndRoleMapping;
import com.daimler.dna.airflow.models.PermissionAndViewMenuMapping;
import com.daimler.dna.airflow.models.Role;
import com.daimler.dna.airflow.models.ViewMenu;

@Component
public class RoleAssembler {

	@Autowired
	private PermissionAssembler assembler;

	public AirflowRoleVO toVo(Role role) {
		AirflowRoleVO vo = null;
		if (Objects.nonNull(role)) {
			vo = new AirflowRoleVO();
			BeanUtils.copyProperties(role, vo);
			for (PermissionAndRoleMapping mapping : role.getPermissionAndRoleMapping()) {
				AirflowPermissionVO permissionItem = assembler.toVo(mapping.getPermissionView().getPermission());
				permissionItem.setPermissionViewId(mapping.getPermissionView().getId());
				vo.addPermissionsItem(permissionItem);
			}
		}
		return vo;
	}

	public Role toEntity(String roleName) {
		Role role = null;
		if (Objects.nonNull(roleName)) {
			role = new Role();
			role.setName(roleName);
		}
		return role;
	}

	/*
	 * public Role toEntity(AirflowRoleVO vo) { Role role = null; if
	 * (Objects.nonNull(vo)) { role = new Role(); BeanUtils.copyProperties(vo,
	 * role); List<PermissionAndRoleMapping> list = new ArrayList<>();
	 * for(AirflowPermissionVO permisionVO:vo.getPermissions()) {
	 * PermissionAndRoleMapping rollMapping = new PermissionAndRoleMapping(); //
	 * rollMapping.setPermissionView(permissionView);
	 * 
	 * list.add(rollMapping); } role.setPermissionAndRoleMapping(list); } return
	 * role; }
	 */

	/*
	 * protected PermissionAndViewMenuMapping getPermissionView(AirflowPermissionVO
	 * vo,List<PermissionAndRoleMapping> list, Permission permission) {
	 * PermissionAndViewMenuMapping permissionView = new
	 * PermissionAndViewMenuMapping(); }
	 */

}
