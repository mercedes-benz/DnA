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

import com.daimler.data.db.entities.UserRoleNsql;
import com.daimler.data.db.jsonb.UserRole;
import com.daimler.data.dto.userrole.UserRoleVO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserRoleAssembler implements GenericAssembler<UserRoleVO, UserRoleNsql> {

	@Override
	public UserRoleVO toVo(UserRoleNsql entity) {
		UserRoleVO userroleVO = null;
		if (Objects.nonNull(entity)) {
			userroleVO = new UserRoleVO();
			userroleVO.setId(entity.getId());
			userroleVO.setName(entity.getData().getName());
		}
		return userroleVO;
	}

	@Override
	public UserRoleNsql toEntity(UserRoleVO vo) {
		UserRoleNsql userroleNsql = null;
		if (Objects.nonNull(vo)) {
			userroleNsql = new UserRoleNsql();
			UserRole userrole = new UserRole();
			userrole.setName(vo.getName());
			userroleNsql.setData(userrole);
			if (vo.getId() != null)
				userroleNsql.setId(vo.getId());
		}
		return userroleNsql;
	}

}
