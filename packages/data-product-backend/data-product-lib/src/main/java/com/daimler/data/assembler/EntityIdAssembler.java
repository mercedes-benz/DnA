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

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.EntityIdNsql;
import com.daimler.data.db.jsonb.EntityId;
import com.daimler.data.dto.entityid.EntityIdVO;

@Component
public class EntityIdAssembler implements GenericAssembler<EntityIdVO, EntityIdNsql> {

	@Override
	public EntityIdVO toVo(EntityIdNsql entity) {
		EntityIdVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new EntityIdVO();
			EntityId entityId = entity.getData();
			BeanUtils.copyProperties(entityId, vo);
			vo.setId(entity.getId());
		}

		return vo;
	}

	@Override
	public EntityIdNsql toEntity(EntityIdVO vo) {
		EntityIdNsql entity = null;
		if (vo != null) {
			entity = new EntityIdNsql();
			String id = vo.getId();
			if (StringUtils.hasText(id)) {
				entity.setId(id);
			}
			EntityId entityId = new EntityId();
			BeanUtils.copyProperties(vo, entityId);
			entity.setData(entityId);
		}

		return entity;
	}
}
