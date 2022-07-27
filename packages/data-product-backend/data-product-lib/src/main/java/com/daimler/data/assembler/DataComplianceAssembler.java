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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataComplianceNsql;
import com.daimler.data.db.jsonb.CreatedBy;
import com.daimler.data.db.jsonb.DataCompliance;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.datacompliance.DataComplianceVO;

@Component
public class DataComplianceAssembler implements GenericAssembler<DataComplianceVO, DataComplianceNsql> {

	@Override
	public DataComplianceVO toVo(DataComplianceNsql entity) {
		DataComplianceVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new DataComplianceVO();
			DataCompliance dataCompliance = entity.getData();
			BeanUtils.copyProperties(dataCompliance, vo);
			if (Objects.nonNull(dataCompliance.getCreatedBy())) {
				CreatedByVO createdByVO = new CreatedByVO();
				BeanUtils.copyProperties(dataCompliance.getCreatedBy(), createdByVO);
				vo.setCreatedBy(createdByVO);
			}
			if (Objects.nonNull(dataCompliance.getModifiedBy())) {
				CreatedByVO updatedByVO = new CreatedByVO();
				BeanUtils.copyProperties(dataCompliance.getModifiedBy(), updatedByVO);
				vo.setModifiedBy(updatedByVO);
			}
			vo.setId(entity.getId());
		}

		return vo;
	}

	@Override
	public DataComplianceNsql toEntity(DataComplianceVO vo) {
		DataComplianceNsql entity = null;
		if (vo != null) {
			entity = new DataComplianceNsql();
			String id = vo.getId();
			if (StringUtils.hasText(id)) {
				entity.setId(id);
			}
			DataCompliance dataCompliance = new DataCompliance();
			BeanUtils.copyProperties(vo, dataCompliance);

			if (Objects.nonNull(vo.getCreatedBy())) {
				CreatedBy userDetails = new CreatedBy();
				BeanUtils.copyProperties(vo.getCreatedBy(), userDetails);
				dataCompliance.setCreatedBy(userDetails);
			}
			if (Objects.nonNull(vo.getModifiedBy())) {
				CreatedBy userDetails = new CreatedBy();
				BeanUtils.copyProperties(vo.getModifiedBy(), userDetails);
				dataCompliance.setModifiedBy(userDetails);
			}
			entity.setData(dataCompliance);
		}

		return entity;
	}

	public List<String> toList(String parameter) {
		List<String> results = null;
		if (StringUtils.hasText(parameter)) {
			results = new ArrayList<String>();
			String[] parameters = parameter.split(",");
			if (!ObjectUtils.isEmpty(parameters))
				results = Arrays.asList(parameters);
		}
		return results;
	}
}
