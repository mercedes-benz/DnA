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

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.AccessTypesNsql;
import com.daimler.data.db.jsonb.AccessTypes;
import com.daimler.data.dto.dataproductlov.AccessTypesVO;

@Component
public class AccessTypesAssembler implements GenericAssembler<AccessTypesVO, AccessTypesNsql> {

	@Override
	public AccessTypesVO toVo(AccessTypesNsql entity) {
		AccessTypesVO accessTypesVO = null;
		if (Objects.nonNull(entity)) {
			accessTypesVO = new AccessTypesVO();
			accessTypesVO.setId(entity.getId());
			accessTypesVO.setName(entity.getData().getName());
		}
		return accessTypesVO;
	}

	@Override
	public AccessTypesNsql toEntity(AccessTypesVO vo) {
		AccessTypesNsql accessTypesNsql = null;
		if (Objects.nonNull(vo)) {
			accessTypesNsql = new AccessTypesNsql();
			AccessTypes accessTypes = new AccessTypes();
			accessTypes.setName(vo.getName());
			accessTypesNsql.setData(accessTypes);
			if (vo.getId() != null)
				accessTypesNsql.setId(vo.getId());
		}
		return accessTypesNsql;
	}
}
