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
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.ItsmmGameUserDetailNsql;
import com.daimler.data.db.jsonb.ItsmmGameUserDetail;
import com.daimler.data.dto.itsmmgame.ItsmmGameUserDetailsResultVO;
import com.daimler.data.dto.itsmmgame.ItsmmGameUserDetailsVO;


@Component
public class ItsmmGameUserAssembler implements GenericAssembler<ItsmmGameUserDetailsVO, ItsmmGameUserDetailNsql> {

	private static Logger LOGGER = LoggerFactory.getLogger(ItsmmGameUserAssembler.class);
	
	@Override
	public ItsmmGameUserDetailsVO toVo(ItsmmGameUserDetailNsql entity) {
		ItsmmGameUserDetailsVO vo = new ItsmmGameUserDetailsVO();
		if(entity!=null) {
			ItsmmGameUserDetail data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				vo.setSolutionSeen(data.getSolutionSeen());
			}
			vo.setId(entity.getId());
		}
		return vo;
	}

	@Override
	public ItsmmGameUserDetailNsql toEntity(ItsmmGameUserDetailsVO vo) {
		ItsmmGameUserDetailNsql entity = new ItsmmGameUserDetailNsql();
		if(vo!=null) {
			ItsmmGameUserDetail detailsJson = new ItsmmGameUserDetail();
			BeanUtils.copyProperties(vo, detailsJson);
			detailsJson.setSolutionSeen(vo.isSolutionSeen());
			entity.setData(detailsJson);
			entity.setId(vo.getId());
		}
		return entity;
	}
	
	public ItsmmGameUserDetailsResultVO toResultsVO(ItsmmGameUserDetail record) {
		ItsmmGameUserDetailsResultVO vo = new ItsmmGameUserDetailsResultVO();
		if(record!=null) {
			BeanUtils.copyProperties(record, vo);
			Long time= record.getTimeTaken();
			String timeTaken = "";
			if(time == Long.MAX_VALUE) {
				timeTaken = "N.A.";
			}else {
				long millis = time % 1000;
				long second = (time / 1000) % 60;
				long minute = (time / (1000 * 60)) % 60;
				long hour = (time / (1000 * 60 * 60)) % 24;
				timeTaken = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
			}
			vo.setTimeTaken(timeTaken);
		}
		return vo;
	}
	
	public List<ItsmmGameUserDetailsResultVO> toResultsVO(List<ItsmmGameUserDetail> records) {
		List<ItsmmGameUserDetailsResultVO> resultsVO = new ArrayList<>();
		if(records!=null && !records.isEmpty()) {
			resultsVO = records.stream().map(n->toResultsVO(n)).collect(Collectors.toList());
		}
		return resultsVO;
	}
}
