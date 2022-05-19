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

import com.daimler.data.db.entities.DataVolumeNsql;
import com.daimler.data.db.jsonb.DataVolume;
import com.daimler.data.dto.datavolume.DataVolumeVO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataVolumeAssembler implements GenericAssembler<DataVolumeVO, DataVolumeNsql> {

	@Override
	public DataVolumeVO toVo(DataVolumeNsql entity) {
		DataVolumeVO dataVolumeVO = null;
		if (Objects.nonNull(entity)) {
			dataVolumeVO = new DataVolumeVO();
			dataVolumeVO.setId(entity.getId());
			dataVolumeVO.setName(entity.getData().getName());
		}
		return dataVolumeVO;
	}

	@Override
	public DataVolumeNsql toEntity(DataVolumeVO vo) {
		DataVolumeNsql datavolumeNsql = null;
		if (Objects.nonNull(vo)) {
			datavolumeNsql = new DataVolumeNsql();
			DataVolume datavolume = new DataVolume();
			datavolume.setName(vo.getName());
			datavolumeNsql.setData(datavolume);
			if (vo.getId() != null)
				datavolumeNsql.setId(vo.getId());
		}
		return datavolumeNsql;
	}

}
