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

import com.daimler.data.db.entities.DataWarehouseNsql;
import com.daimler.data.db.jsonb.report.DataWarehouse;
import com.daimler.data.dto.datawarehouse.DataWarehouseInUseVO;

@Component
public class DataWarehouseAssembler implements GenericAssembler<DataWarehouseInUseVO, DataWarehouseNsql> {

	@Override
	public DataWarehouseInUseVO toVo(DataWarehouseNsql entity) {
		DataWarehouseInUseVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new DataWarehouseInUseVO();
			DataWarehouse dataWarehouse = entity.getData();
			BeanUtils.copyProperties(dataWarehouse, vo);
			vo.setId(entity.getId());
		}

		return vo;
	}

	@Override
	public DataWarehouseNsql toEntity(DataWarehouseInUseVO vo) {
		DataWarehouseNsql entity = null;
		if (vo != null) {
			entity = new DataWarehouseNsql();
			String id = vo.getId();
			if (id != null && !id.isEmpty() && !id.trim().isEmpty()) {
				entity.setId(id);
			}
			DataWarehouse dataWarehouse = new DataWarehouse();
			BeanUtils.copyProperties(vo, dataWarehouse);
			entity.setData(dataWarehouse);
		}

		return entity;
	}
}
