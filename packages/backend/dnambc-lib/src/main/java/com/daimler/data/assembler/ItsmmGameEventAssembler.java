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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.ItsmmGameEventDetailNsql;
import com.daimler.data.db.jsonb.ItsmmGameEventDetail;
import com.daimler.data.dto.itsmmgame.ItsmmGameEventDetailsVO;

@Component
public class ItsmmGameEventAssembler implements GenericAssembler<ItsmmGameEventDetailsVO, ItsmmGameEventDetailNsql> {

	private static Logger LOGGER = LoggerFactory.getLogger(ItsmmGameEventAssembler.class);

	@Override
	public ItsmmGameEventDetailsVO toVo(ItsmmGameEventDetailNsql entity) {
		ItsmmGameEventDetailsVO vo = new ItsmmGameEventDetailsVO();
		if (entity != null) {
			ItsmmGameEventDetail data = entity.getData();
			if (data != null) {
				BeanUtils.copyProperties(data, vo);
				vo.setGameStarted(data.getGameStarted());
				vo.setGameStopped(data.getGameStopped());
			}
			vo.setId(entity.getId());
		}
		return vo;
	}

	@Override
	public ItsmmGameEventDetailNsql toEntity(ItsmmGameEventDetailsVO vo) {
		ItsmmGameEventDetailNsql entity = new ItsmmGameEventDetailNsql();
		ItsmmGameEventDetail data = new ItsmmGameEventDetail();
		if (vo != null) {
			BeanUtils.copyProperties(vo, data);
			data.setGameStarted(vo.isGameStarted());
			data.setGameStopped(vo.isGameStopped());
			entity.setId(vo.getId());
		}
		entity.setData(data);
		return entity;
	}

}
